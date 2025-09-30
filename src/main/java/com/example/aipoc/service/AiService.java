package com.example.aipoc.service;

import com.example.aipoc.model.AiRequest;
import com.example.aipoc.model.AiResponse;
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

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    @Autowired
    private WebClient geminiWebClient;

    @Value("${gemini.api.model}")
    private String model;

    @Value("${gemini.api.max-tokens}")
    private int maxTokens;

    @Value("${gemini.api.temperature}")
    private double temperature;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<AiResponse> processMessage(AiRequest request) {
        logger.debug("Processando mensagem: {}", request.getMessage());

        try {
            Map<String, Object> requestBody = buildRequestBody(request);
            String endpoint = "/models/" + model + ":generateContent?key=" + apiKey;

            logger.debug("Chamando endpoint: {}", endpoint);
            logger.debug("Request body: {}", objectMapper.writeValueAsString(requestBody));

            return geminiWebClient
                    .post()
                    .uri(endpoint)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(response -> logger.debug("Resposta da API: {}", response))
                    .map(this::parseGeminiResponse)
                    .doOnError(error -> logger.error("Erro ao chamar Gemini API: {}", error.getMessage()))
                    .onErrorReturn(AiResponse.error("Erro ao processar solicitação"));

        } catch (Exception e) {
            logger.error("Erro ao construir requisição: {}", e.getMessage(), e);
            return Mono.just(AiResponse.error("Erro interno do servidor"));
        }
    }

    private Map<String, Object> buildRequestBody(AiRequest request) {
        Map<String, Object> requestBody = new HashMap<>();

        // Configuração de geração
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", temperature);
        generationConfig.put("maxOutputTokens", maxTokens);
        requestBody.put("generationConfig", generationConfig);

        // Conteúdo da mensagem
        String prompt = buildPrompt(request);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        requestBody.put("contents", List.of(content));

        return requestBody;
    }

    private String buildPrompt(AiRequest request) {
        StringBuilder prompt = new StringBuilder();

        if (request.getContext() != null && !request.getContext().trim().isEmpty()) {
            prompt.append("Contexto: ").append(request.getContext()).append("\n\n");
        }

        prompt.append("Pergunta: ").append(request.getMessage());

        return prompt.toString();
    }

    private AiResponse parseGeminiResponse(String responseBody) {
        try {
            logger.debug("Resposta da IA: {}", responseBody);

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode candidates = jsonNode.path("candidates");
            
            if (candidates.isEmpty()) {
                return AiResponse.error("Nenhuma resposta gerada pela IA");
            }
            
            JsonNode candidate = candidates.get(0);
            String finishReason = candidate.path("finishReason").asText();
            
            // Verificar se a resposta foi cortada por limite de tokens
            if ("MAX_TOKENS".equals(finishReason)) {
                return AiResponse.error("Resposta cortada por limite de tokens. Tente uma pergunta mais simples.");
            }
            
            JsonNode contentNode = candidate.path("content");
            JsonNode partsNode = contentNode.path("parts");
            
            if (partsNode.isEmpty()) {
                return AiResponse.error("Resposta vazia da IA. Motivo: " + finishReason);
            }
            
            String content = partsNode.get(0).path("text").asText();
            
            if (content.trim().isEmpty()) {
                return AiResponse.error("Resposta vazia da IA. Motivo: " + finishReason);
            }

            int tokensUsed = jsonNode
                    .path("usageMetadata")
                    .path("totalTokenCount")
                    .asInt(0);

            return new AiResponse(content, model, tokensUsed);

        } catch (Exception e) {
            logger.error("Erro ao fazer parse da resposta do Gemini: {}", e.getMessage());
            logger.error("Response body: {}", responseBody);
            return AiResponse.error("Erro ao processar resposta da IA");
        }
    }
}