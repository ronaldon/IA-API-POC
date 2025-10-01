package com.example.aipoc.service;

import com.example.aipoc.model.AiRequest;
import com.example.aipoc.model.AiResponse;
import com.example.aipoc.model.GeminiConfig;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AiGeneralService extends BaseGeminiService {

    public Mono<AiResponse> processMessage(AiRequest request) {
        logOperationStart("processamento de mensagem", request.getMessage());

        try {
            String prompt = buildPrompt(request);
            GeminiConfig config = createDefaultConfig();
            Map<String, Object> requestBody = buildBaseRequestBody(prompt, config);

            return callGeminiApi(requestBody, config)
                    .map(this::parseGeminiResponse)
                    .doOnNext(response -> {
                        if (response.isSuccess()) {
                            logOperationSuccess("processamento de mensagem", null, response.getTokensUsed());
                        }
                    })
                    .doOnError(error -> handleApiError(
                            "processamento de mensagem",
                            error,
                            AiResponse.error("Erro ao processar solicitação")));

        } catch (Exception e) {
            return Mono.just(handleApiError("construção de requisição", e, 
                                          AiResponse.error("Erro interno do servidor")));
        }
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
            String content = extractContentFromResponse(responseBody);
            
            if (content == null || content.trim().isEmpty()) {
                logger.warn("Nenhum conteúdo extraído da resposta da API para processamento de mensagem");
                return AiResponse.error("Nenhuma resposta válida gerada pela IA");
            }

            int tokensUsed = extractTokenUsage(responseBody);
            return new AiResponse(content, defaultModel, tokensUsed);

        } catch (Exception e) {
            return handleApiError("parsing de resposta da IA", e, 
                                AiResponse.error("Erro ao processar resposta da IA"), 
                                responseBody);
        }
    }
}