package com.example.aipoc.service;

import com.example.aipoc.model.AiResponse;
import com.example.aipoc.model.GeminiConfig;
import com.example.aipoc.model.SummaryRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class TextSummaryService extends BaseGeminiService {
    
    public Mono<AiResponse> summarizeText(SummaryRequest request) {
        logOperationStart("resumo de texto", "Texto de %d caracteres".formatted(request.getText().length()));
        
        try {
            String prompt = buildSummaryPrompt(request);
            GeminiConfig config = createConfig(0.3, 1000);
            Map<String, Object> requestBody = buildBaseRequestBody(prompt, config);
            
            long startTime = System.currentTimeMillis();
            
            return callGeminiApi(requestBody, config)
                    .map(responseBody -> parseGeminiResponse(responseBody, config.getModel()))
                    .doOnNext(response -> {
                        long duration = System.currentTimeMillis() - startTime;
                        logOperationSuccess("resumo de texto", duration, response.getTokensUsed());
                    })
                    .doOnError(error -> handleApiError(
                            "Erro ao processar resumo",
                            error,
                            AiResponse.error("Erro ao processar classificação")));
                    
        } catch (Exception e) {
            return Mono.just(handleApiError("construção de requisição de resumo", e, 
                AiResponse.error("Erro interno do servidor")));
        }
    }
    

    
    private String buildSummaryPrompt(SummaryRequest request) {
        String styleInstruction = switch (request.getStyle().toLowerCase()) {
            case "detalhado" -> "Crie um resumo detalhado e explicativo";
            case "bullet-points" -> "Crie um resumo em formato de bullet points (•)";
            default -> "Crie um resumo conciso e direto";
        };
        
        return """
            %s do seguinte texto em no máximo %d sentenças.
            
            Mantenha as informações mais importantes e o contexto principal.
            
            Texto original:
            %s
            """.formatted(styleInstruction, request.getMaxSentences(), request.getText());
    }
    
    private AiResponse parseGeminiResponse(String responseBody, String model) {
        try {
            // Use base class method to extract content
            String content = extractContentFromResponse(responseBody);
            
            if (content == null || content.trim().isEmpty()) {
                return AiResponse.error("Nenhum resumo gerado ou conteúdo vazio");
            }
            
            // Check for specific finish reasons that should return errors
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode candidates = jsonNode.path("candidates");
            
            if (!candidates.isEmpty()) {
                JsonNode candidate = candidates.get(0);
                String finishReason = candidate.path("finishReason").asText();
                
                if ("MAX_TOKENS".equals(finishReason)) {
                    return AiResponse.error("Resumo cortado por limite de tokens. Tente um texto menor.");
                }
            }
            
            // Use base class method to extract token usage
            int tokensUsed = extractTokenUsage(responseBody);
            
            return new AiResponse(content, model, tokensUsed);

        } catch (Exception e) {
            return handleApiError("parsing de resposta de resumo", e, 
                AiResponse.error("Erro ao processar resumo"));
        }
    }
}