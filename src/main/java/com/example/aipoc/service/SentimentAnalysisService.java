package com.example.aipoc.service;

import com.example.aipoc.model.SentimentRequest;
import com.example.aipoc.model.SentimentResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SentimentAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(SentimentAnalysisService.class);

    @Autowired
    private WebClient geminiWebClient;

    @Value("${gemini.api.model}")
    private String model;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<SentimentResponse> analyzeSentiment(SentimentRequest request) {
        logger.debug("Analisando sentimento do texto: {}",
                request.getText().substring(0, Math.min(50, request.getText().length())));

        try {
            Map<String, Object> requestBody = buildSentimentRequestBody(request);
            String endpoint = "/models/" + model + ":generateContent?key=" + apiKey;

            return geminiWebClient
                    .post()
                    .uri(endpoint)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> parseSentimentResponse(response, request.getText()))
                    .doOnError(error -> logger.error("Erro ao analisar sentimento: {}", error.getMessage()))
                    .onErrorReturn(SentimentResponse.error("Erro ao processar análise de sentimento"));

        } catch (Exception e) {
            logger.error("Erro ao construir requisição de sentimento: {}", e.getMessage());
            return Mono.just(SentimentResponse.error("Erro interno do servidor"));
        }
    }

    private Map<String, Object> buildSentimentRequestBody(SentimentRequest request) {
        Map<String, Object> requestBody = new HashMap<>();

        // Configuração de geração
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.1); // Baixa temperatura para análise mais consistente
        generationConfig.put("maxOutputTokens", 500);
        requestBody.put("generationConfig", generationConfig);

        // Prompt especializado para análise de sentimento
        String prompt = buildSentimentPrompt(request);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        requestBody.put("contents", List.of(content));

        return requestBody;
    }

    private String buildSentimentPrompt(SentimentRequest request) {
        return String.format("""
                Analise o sentimento do seguinte texto e responda EXATAMENTE no formato JSON:

                {
                  "sentiment": "POSITIVE|NEGATIVE|NEUTRAL",
                  "confidence": 0.85,
                  "explanation": "Breve explicação do por que este sentimento foi identificado"
                }

                Texto para análise:
                "%s"

                Responda apenas com o JSON, sem texto adicional.
                """, request.getText());
    }

    private SentimentResponse parseSentimentResponse(String responseBody, String originalText) {
        try {
            logger.debug("Resposta da IA: {}", responseBody);
            
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode candidates = jsonNode.path("candidates");

            if (candidates.isEmpty()) {
                return SentimentResponse.error("Nenhuma análise gerada");
            }

            JsonNode candidate = candidates.get(0);
            String finishReason = candidate.path("finishReason").asText();

            if (!"STOP".equals(finishReason)) {
                return SentimentResponse.error("Análise incompleta. Motivo: " + finishReason);
            }

            String content = candidate
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            return parseJsonFromContent(content, originalText);

        } catch (Exception e) {
            logger.error("Erro ao fazer parse da análise de sentimento: {}", e.getMessage());
            return SentimentResponse.error("Erro ao processar análise");
        }
    }

    private SentimentResponse parseJsonFromContent(String content, String originalText) {
        try {
            // Extrair JSON do conteúdo usando regex
            Pattern jsonPattern = Pattern.compile("\\{[^}]*\\}", Pattern.DOTALL);
            Matcher matcher = jsonPattern.matcher(content);

            if (matcher.find()) {
                String jsonStr = matcher.group();
                JsonNode sentimentJson = objectMapper.readTree(jsonStr);

                String sentiment = sentimentJson.path("sentiment").asText();
                double confidence = sentimentJson.path("confidence").asDouble();
                String explanation = sentimentJson.path("explanation").asText();

                return new SentimentResponse(sentiment, confidence, explanation, originalText);
            } else {
                // Fallback: análise simples baseada em palavras-chave
                return fallbackSentimentAnalysis(content, originalText);
            }

        } catch (Exception e) {
            logger.error("Erro ao extrair JSON da resposta: {}", e.getMessage());
            return fallbackSentimentAnalysis(content, originalText);
        }
    }

    private SentimentResponse fallbackSentimentAnalysis(String content, String originalText) {
        String lowerContent = content.toLowerCase();

        if (lowerContent.contains("positiv") || lowerContent.contains("bom") || lowerContent.contains("feliz")) {
            return new SentimentResponse("POSITIVE", 0.7, "Análise baseada em palavras-chave positivas", originalText);
        } else if (lowerContent.contains("negativ") || lowerContent.contains("ruim")
                || lowerContent.contains("triste")) {
            return new SentimentResponse("NEGATIVE", 0.7, "Análise baseada em palavras-chave negativas", originalText);
        } else {
            return new SentimentResponse("NEUTRAL", 0.6, "Sentimento neutro identificado", originalText);
        }
    }
}