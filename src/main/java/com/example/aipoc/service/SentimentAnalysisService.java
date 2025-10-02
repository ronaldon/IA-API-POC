package com.example.aipoc.service;

import com.example.aipoc.model.GeminiConfig;
import com.example.aipoc.model.SentimentRequest;
import com.example.aipoc.model.SentimentResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SentimentAnalysisService extends BaseGeminiService {

    public Mono<SentimentResponse> analyzeSentiment(SentimentRequest request) {
        logOperationStart("análise de sentimento", 
                "Texto: " + request.getText().substring(0, Math.min(50, request.getText().length())));

        try {
            String prompt = buildSentimentPrompt(request);
            GeminiConfig config = createConfig(0.1, 500);
            Map<String, Object> requestBody = buildBaseRequestBody(prompt, config);

            return callGeminiApi(requestBody, config)
                    .map(response -> parseSentimentResponse(response, request.getText()))
                    .doOnError(error -> logger.error("Erro ao analisar sentimento: {}", error.getMessage()))
                    .onErrorReturn(SentimentResponse.error("Erro ao processar análise de sentimento"));

        } catch (Exception e) {
            return Mono.just(handleApiError("construção de requisição de sentimento", e, 
                    SentimentResponse.error("Erro interno do servidor")));
        }
    }



    private String buildSentimentPrompt(SentimentRequest request) {
        return """
                Analise o sentimento do seguinte texto e responda EXATAMENTE no formato JSON:

                {
                  "sentiment": "POSITIVE|NEGATIVE|NEUTRAL",
                  "confidence": 0.85,
                  "explanation": "Breve explicação do por que este sentimento foi identificado"
                }

                Texto para análise:
                "%s"

                Responda apenas com o JSON, sem texto adicional.
                """.formatted(request.getText());
    }

    private SentimentResponse parseSentimentResponse(String responseBody, String originalText) {
        try {
            String content = extractContentFromResponse(responseBody);
            
            if (content == null) {
                logger.warn("Nenhum conteúdo extraído da resposta da API para análise de sentimento");
                return SentimentResponse.error("Nenhuma análise gerada");
            }

            return parseJsonFromContent(content, originalText);

        } catch (Exception e) {
            return handleApiError("parse da análise de sentimento", e, 
                    SentimentResponse.error("Erro ao processar análise"));
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