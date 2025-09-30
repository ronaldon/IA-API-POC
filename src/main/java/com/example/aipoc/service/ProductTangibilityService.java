package com.example.aipoc.service;

import com.example.aipoc.model.ProductClassificationRequest;
import com.example.aipoc.model.ProductClassificationResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProductTangibilityService {

    private static final Logger logger = LoggerFactory.getLogger(ProductTangibilityService.class);

    // DTO para mapear o JSON da resposta da IA
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ClassificationDto {
        public String tangibilityType;
        public String tangibilitySubtype;
        public double confidence;
        public String explanation;
        public String productPriceCategory;
        public String lifeCycle;
        public List<String> characteristics;
    }

    @Autowired
    private WebClient geminiWebClient;

    @Value("${gemini.api.model}")
    private String model;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<ProductClassificationResponse> classifyProduct(ProductClassificationRequest request) {
        logger.debug("Classificando produto por tangibilidade: {}", request.getProductName());

        try {
            Map<String, Object> requestBody = buildClassificationRequestBody(request);
            String endpoint = "/models/" + model + ":generateContent?key=" + apiKey;

            return geminiWebClient
                    .post()
                    .uri(endpoint)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> parseClassificationResponse(response, request.getProductName()))
                    .doOnError(error -> logger.error("Erro ao classificar produto: {}", error.getMessage()))
                    .onErrorReturn(ProductClassificationResponse.error("Erro ao processar classificação"));

        } catch (Exception e) {
            logger.error("Erro ao construir requisição de classificação: {}", e.getMessage());
            return Mono.just(ProductClassificationResponse.error("Erro interno do servidor"));
        }
    }

    private Map<String, Object> buildClassificationRequestBody(ProductClassificationRequest request) {
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.2); // Baixa temperatura para classificação consistente
        generationConfig.put("maxOutputTokens", 800);
        requestBody.put("generationConfig", generationConfig);

        String prompt = buildClassificationPrompt(request);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        requestBody.put("contents", List.of(content));

        return requestBody;
    }

    private String buildClassificationPrompt(ProductClassificationRequest request) {
        StringBuilder productInfo = new StringBuilder();
        productInfo.append("Nome: ").append(request.getProductName());

        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            productInfo.append("\nDescrição: ").append(request.getDescription());
        }

        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            productInfo.append("\nCategoria: ").append(request.getCategory());
        }

        return String.format("""
                Classifique o seguinte produto por TANGIBILIDADE e responda EXATAMENTE no formato JSON:

                {
                  "tangibilityType": "TANGIBLE|INTANGIBLE|HYBRID",
                  "tangibilitySubtype": "DURABLE|NON_DURABLE|CONSUMABLE|SERVICE|DIGITAL|EXPERIENCE|KNOWLEDGE|MIXED",
                  "productPriceCategory": "VERY_HIGH_COST|HIGH_COST|MEDIUM_COST|LOW_COST",
                  "lifeCycle": "SHORT|MID|LONG"
                  "confidence": 0.95,
                  "explanation": "Explicação clara do por que esta classificação foi escolhida",
                  "characteristics": ["característica 1", "característica 2", "característica 3"]
                }

                DEFINIÇÕES:
                lifeCycle (tempo de vida útil):
                - SHORT: tempo de vida útil curto
                - MID: tempo de vida útil médio
                - LONG: tempo de vida útil longo

                productPriceCategory (Preço do produto):
                - VERY_HIGH_COST: preço acima de 100 mil reais
                - HIGH_COST: preço acima de 50 mil reais
                - MEDIUM_COST: preço acima de 1 mil reais
                - LOW_COST: preço ate 1 mil reais
                
                TANGIBLE (Tangível):
                - DURABLE: Produtos físicos duráveis (carros, móveis, eletrônicos)
                - NON_DURABLE: Produtos físicos não duráveis (roupas, calçados)
                - CONSUMABLE: Produtos consumíveis (alimentos, cosméticos, medicamentos)

                INTANGIBLE (Intangível):
                - SERVICE: Serviços (consultoria, limpeza, transporte)
                - DIGITAL: Produtos digitais (software, apps, e-books)
                - EXPERIENCE: Experiências (viagens, eventos, cursos)
                - KNOWLEDGE: Conhecimento/informação (patentes, licenças, dados)

                HYBRID (Híbrido):
                - MIXED: Combinação de elementos tangíveis e intangíveis

                Produto para classificar:
                %s

                Responda apenas com o JSON, sem texto adicional.
                """, productInfo.toString());
    }

    private ProductClassificationResponse parseClassificationResponse(String responseBody, String productName) {
        try {
            logger.debug("Resposta da IA: {}", responseBody);

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode candidates = jsonNode.path("candidates");

            if (candidates.isEmpty()) {
                return ProductClassificationResponse.error("Nenhuma classificação gerada");
            }

            JsonNode candidate = candidates.get(0);
            String finishReason = candidate.path("finishReason").asText();

            if (!"STOP".equals(finishReason)) {
                return ProductClassificationResponse.error("Classificação incompleta. Motivo: " + finishReason);
            }

            String content = candidate
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            return parseJsonFromContent(content, productName);

        } catch (Exception e) {
            logger.error("Erro ao fazer parse da classificação: {}", e.getMessage());
            return ProductClassificationResponse.error("Erro ao processar classificação");
        }
    }

    private ProductClassificationResponse parseJsonFromContent(String content, String productName) {
        try {
            // Extrair JSON do conteúdo usando regex
            Pattern jsonPattern = Pattern.compile("\\{[^}]*\\}", Pattern.DOTALL);
            Matcher matcher = jsonPattern.matcher(content);

            if (matcher.find()) {
                String jsonStr = matcher.group();

                // Parse direto para DTO intermediário
                ClassificationDto dto = objectMapper.readValue(jsonStr, ClassificationDto.class);

                // Criar response com os dados do DTO
                ProductClassificationResponse response = new ProductClassificationResponse(
                        dto.tangibilityType,
                        dto.confidence,
                        dto.explanation,
                        dto.characteristics != null ? dto.characteristics : Arrays.asList(),
                        productName,
                        dto.tangibilitySubtype,
                        dto.productPriceCategory,
                        dto.lifeCycle);

                return response;
            } else {
                // Fallback: análise simples baseada em palavras-chave
                return fallbackClassification(content, productName);
            }

        } catch (Exception e) {
            logger.error("Erro ao extrair JSON da classificação: {}", e.getMessage());
            return fallbackClassification(content, productName);
        }
    }

    private ProductClassificationResponse fallbackClassification(String content, String productName) {
        String lowerContent = content.toLowerCase();
        String lowerProductName = productName.toLowerCase();

        // Palavras-chave para produtos tangíveis
        String[] tangibleKeywords = { "físico", "material", "objeto", "produto", "item", "mercadoria",
                "equipamento", "aparelho", "dispositivo", "máquina" };

        // Palavras-chave para produtos intangíveis
        String[] intangibleKeywords = { "serviço", "consultoria", "software", "aplicativo", "curso",
                "experiência", "conhecimento", "licença", "digital" };

        boolean hasTangible = Arrays.stream(tangibleKeywords)
                .anyMatch(keyword -> lowerContent.contains(keyword) || lowerProductName.contains(keyword));

        boolean hasIntangible = Arrays.stream(intangibleKeywords)
                .anyMatch(keyword -> lowerContent.contains(keyword) || lowerProductName.contains(keyword));

        if (hasTangible && hasIntangible) {
            return new ProductClassificationResponse("HYBRID", 0.6,
                    "Classificação baseada em análise de palavras-chave - produto híbrido",
                    Arrays.asList("Elementos tangíveis e intangíveis identificados"),
                    productName, "MIXED", "NA", "NA");
        } else if (hasTangible) {
            return new ProductClassificationResponse("TANGIBLE", 0.7,
                    "Classificação baseada em análise de palavras-chave - produto físico",
                    Arrays.asList("Características físicas identificadas"),
                    productName, "NON_DURABLE", "NA", "NA");
        } else if (hasIntangible) {
            return new ProductClassificationResponse("INTANGIBLE", 0.7,
                    "Classificação baseada em análise de palavras-chave - produto intangível",
                    Arrays.asList("Características de serviço/digital identificadas"),
                    productName, "SERVICE", "NA", "NA");
        } else {
            return new ProductClassificationResponse("TANGIBLE", 0.5,
                    "Classificação padrão - assumindo produto tangível",
                    Arrays.asList("Classificação incerta"),
                    productName, "NON_DURABLE", "NA", "NA");
        }
    }
}
