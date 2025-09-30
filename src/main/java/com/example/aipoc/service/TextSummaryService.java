package com.example.aipoc.service;

import com.example.aipoc.model.AiResponse;
import com.example.aipoc.model.SummaryRequest;
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
public class TextSummaryService {
    
    private static final Logger logger = LoggerFactory.getLogger(TextSummaryService.class);
    
    @Autowired
    private WebClient geminiWebClient;
    
    @Value("${gemini.api.model}")
    private String model;
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Mono<AiResponse> summarizeText(SummaryRequest request) {
        logger.debug("Resumindo texto de {} caracteres", request.getText().length());
        
        try {
            Map<String, Object> requestBody = buildSummaryRequestBody(request);
            String endpoint = "/models/" + model + ":generateContent?key=" + apiKey;
            
            return geminiWebClient
                    .post()
                    .uri(endpoint)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(this::parseGeminiResponse)
                    .doOnError(error -> logger.error("Erro ao resumir texto: {}", error.getMessage()))
                    .onErrorReturn(AiResponse.error("Erro ao processar resumo"));
                    
        } catch (Exception e) {
            logger.error("Erro ao construir requisição de resumo: {}", e.getMessage());
            return Mono.just(AiResponse.error("Erro interno do servidor"));
        }
    }
    
    private Map<String, Object> buildSummaryRequestBody(SummaryRequest request) {
        Map<String, Object> requestBody = new HashMap<>();
        
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.3);
        generationConfig.put("maxOutputTokens", 1000);
        requestBody.put("generationConfig", generationConfig);
        
        String prompt = buildSummaryPrompt(request);
        
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));
        
        requestBody.put("contents", List.of(content));
        
        return requestBody;
    }
    
    private String buildSummaryPrompt(SummaryRequest request) {
        String styleInstruction = switch (request.getStyle().toLowerCase()) {
            case "detalhado" -> "Crie um resumo detalhado e explicativo";
            case "bullet-points" -> "Crie um resumo em formato de bullet points (•)";
            default -> "Crie um resumo conciso e direto";
        };
        
        return String.format("""
            %s do seguinte texto em no máximo %d sentenças.
            
            Mantenha as informações mais importantes e o contexto principal.
            
            Texto original:
            %s
            """, styleInstruction, request.getMaxSentences(), request.getText());
    }
    
    private AiResponse parseGeminiResponse(String responseBody) {
        try {
            logger.debug("Resposta da IA: {}", responseBody);
            
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode candidates = jsonNode.path("candidates");
            
            if (candidates.isEmpty()) {
                return AiResponse.error("Nenhum resumo gerado");
            }
            
            JsonNode candidate = candidates.get(0);
            String finishReason = candidate.path("finishReason").asText();
            
            if ("MAX_TOKENS".equals(finishReason)) {
                return AiResponse.error("Resumo cortado por limite de tokens. Tente um texto menor.");
            }
            
            String content = candidate
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
            
            if (content.trim().isEmpty()) {
                return AiResponse.error("Resumo vazio gerado");
            }

            int tokensUsed = jsonNode
                    .path("usageMetadata")
                    .path("totalTokenCount")
                    .asInt(0);

            return new AiResponse(content, model, tokensUsed);

        } catch (Exception e) {
            logger.error("Erro ao fazer parse do resumo: {}", e.getMessage());
            return AiResponse.error("Erro ao processar resumo");
        }
    }
}