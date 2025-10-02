package com.example.aipoc.service;

import com.example.aipoc.model.GeminiConfig;
import com.example.aipoc.model.ProductClassificationRequest;
import com.example.aipoc.model.ProductClassificationResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProductTangibilityService extends BaseGeminiService {

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

    public Mono<ProductClassificationResponse> classifyProduct(ProductClassificationRequest request) {
        logOperationStart("classificação de produto", "Produto: " + request.getProductName());

        try {
            String prompt = buildClassificationPrompt(request);
            GeminiConfig config = createConfig(0.2, 1200);
            Map<String, Object> requestBody = buildBaseRequestBody(prompt, config);

            return callGeminiApi(requestBody, config)
                    .map(response -> parseClassificationResponse(response, request.getProductName()))
                    .doOnError(error -> handleApiError(
                            "classificação de produto",
                            error,
                            ProductClassificationResponse.error("Erro ao processar classificação")));

        } catch (Exception e) {
            return Mono.just(handleApiError("construção de requisição de classificação", e,
                    ProductClassificationResponse.error("Erro interno do servidor")));
        }
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

        return """
                Classifique por TANGIBILIDADE e responda em JSON:

                {
                  "tangibilityType": "TANGIBLE|INTANGIBLE|HYBRID",
                  "tangibilitySubtype": "DURABLE|NON_DURABLE|CONSUMABLE|SERVICE|DIGITAL|EXPERIENCE|KNOWLEDGE|MIXED",
                  "productPriceCategory": "VERY_HIGH_COST|HIGH_COST|MEDIUM_COST|LOW_COST",
                  "lifeCycle": "SHORT|MID|LONG",
                  "confidence": 0.95,
                  "explanation": "Breve explicação da classificação",
                  "characteristics": ["característica 1", "característica 2"]
                }

                TIPOS:
                TANGIBLE: DURABLE (carros, móveis), NON_DURABLE (roupas), CONSUMABLE (alimentos)
                INTANGIBLE: SERVICE (consultoria), DIGITAL (software), EXPERIENCE (viagens), KNOWLEDGE (patentes)
                HYBRID: MIXED (combinação)

                PREÇO: VERY_HIGH_COST (>100k), HIGH_COST (>50k), MEDIUM_COST (>1k), LOW_COST (≤1k)
                VIDA ÚTIL: SHORT (curta), MID (média), LONG (longa)

                Produto: %s

                Responda apenas o JSON.
                """.formatted(productInfo.toString());
    }

    private ProductClassificationResponse parseClassificationResponse(String responseBody, String productName) {
        try {
            String content = extractContentFromResponse(responseBody);

            if (content == null) {
                logger.warn("Nenhum conteúdo extraído da resposta da API para classificação de produto");
                return ProductClassificationResponse.error("Nenhuma classificação gerada");
            }

            return parseJsonFromContent(content, productName);

        } catch (Exception e) {
            return handleApiError("parse da classificação", e,
                    ProductClassificationResponse.error("Erro ao processar classificação"));
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
