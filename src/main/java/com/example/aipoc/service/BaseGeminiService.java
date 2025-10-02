package com.example.aipoc.service;

import com.example.aipoc.model.GeminiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for services that interact with the Gemini API.
 * 
 * <p>
 * This class provides common functionality for all AI services including:
 * <ul>
 * <li>WebClient operations with standardized error handling</li>
 * <li>Request building with consistent structure</li>
 * <li>Response parsing with validation</li>
 * <li>Centralized logging and monitoring</li>
 * <li>Configuration management</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * 
 * <pre>{@code
 * @Service
 * public class MyAiService extends BaseGeminiService {
 * 
 *     public Mono<MyResponse> processRequest(MyRequest request) {
 *         logOperationStart("my operation", request.getInput());
 * 
 *         try {
 *             String prompt = buildMyPrompt(request);
 *             GeminiConfig config = createConfig(0.7, 1000);
 *             Map<String, Object> requestBody = buildBaseRequestBody(prompt, config);
 * 
 *             return callGeminiApi(requestBody, config)
 *                     .map(this::parseMyResponse)
 *                     .onErrorReturn(handleApiError("my operation",
 *                             new RuntimeException("Error"),
 *                             MyResponse.error("Error message")));
 *         } catch (Exception e) {
 *             return Mono.just(handleApiError("request building", e,
 *                     MyResponse.error("Internal error")));
 *         }
 *     }
 * 
 *     private MyResponse parseMyResponse(String responseBody) {
 *         String content = extractContentFromResponse(responseBody);
 *         int tokens = extractTokenUsage(responseBody);
 *         return new MyResponse(content, defaultModel, tokens);
 *     }
 * }
 * }</pre>
 * 
 * <h3>Best Practices:</h3>
 * <ul>
 * <li>Always use {@link #logOperationStart(String, String)} at the beginning of
 * public methods</li>
 * <li>Use appropriate temperature values: 0.1-0.3 for consistent results,
 * 0.7-1.0 for creative responses</li>
 * <li>Handle errors consistently using
 * {@link #handleApiError(String, Exception, Object)} methods</li>
 * <li>Use {@link #extractContentFromResponse(String)} for basic content
 * extraction</li>
 * <li>Implement service-specific parsing while leveraging base validation
 * methods</li>
 * </ul>
 * 
 * @author AI POC Team
 * @since 1.0
 * @see GeminiConfig
 */
public abstract class BaseGeminiService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected WebClient geminiWebClient;

    @Value("${gemini.api.model}")
    protected String defaultModel;

    @Value("${gemini.api.max-tokens}")
    protected int defaultMaxTokens;

    @Value("${gemini.api.temperature}")
    protected double defaultTemperature;

    @Value("${gemini.api.key}")
    protected String defaultApiKey;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Makes a call to the Gemini API with the provided request body using default
     * configuration.
     *
     * @param requestBody The request body to send to the API
     * @return A Mono containing the response body as a String
     */
    protected Mono<String> callGeminiApi(Map<String, Object> requestBody) {
        return callGeminiApi(requestBody, createDefaultConfig());
    }

    /**
     * Makes a call to the Gemini API with the provided request body.
     *
     * @param requestBody The request body to send to the API
     * @param config      The configuration to use for this API call
     * @return A Mono containing the response body as a String
     */
    protected Mono<String> callGeminiApi(Map<String, Object> requestBody, GeminiConfig config) {
        try {
            String endpoint = "/models/" + config.getModel() + ":generateContent?key=" + config.getApiKey();

            // Use standardized logging
            logApiRequest(endpoint, requestBody);

            long startTime = System.currentTimeMillis();

            return geminiWebClient
                    .post()
                    .uri(endpoint)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(response -> {
                        logApiResponse(response);
                        long duration = System.currentTimeMillis() - startTime;
                        logger.debug("Chamada API concluída em {}ms", duration);
                    })
                    .doOnError(error -> {
                        long duration = System.currentTimeMillis() - startTime;
                        logger.error("Erro na chamada API após {}ms: {}", duration, error.getMessage());
                    });

        } catch (Exception e) {
            logger.error("Erro na construção de requisição API: {}", e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Detalhes do erro de construção de requisição:", e);
            }
            return Mono.error(e);
        }
    }

    /**
     * Builds the base request body structure for Gemini API calls.
     *
     * @param prompt The text prompt to send to the API
     * @param config The configuration containing temperature and max tokens
     * @return A Map representing the request body
     */
    protected Map<String, Object> buildBaseRequestBody(String prompt, GeminiConfig config) {
        Map<String, Object> requestBody = new HashMap<>();

        // Generation configuration
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", config.getTemperature());
        generationConfig.put("maxOutputTokens", config.getMaxTokens());
        requestBody.put("generationConfig", generationConfig);

        // Content structure
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        requestBody.put("contents", List.of(content));

        return requestBody;
    }

    /**
     * Extracts the text content from a Gemini API response.
     *
     * @param responseBody The raw response body from the API
     * @return The extracted text content, or null if extraction fails
     */
    protected String extractContentFromResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode candidates = jsonNode.path("candidates");

            if (candidates.isEmpty()) {
                logValidationWarning("extração de conteúdo", "Nenhum candidato encontrado na resposta da API");
                return null;
            }

            JsonNode candidate = candidates.get(0);

            if (!isValidResponse(candidate)) {
                return null;
            }

            JsonNode contentNode = candidate.path("content");
            JsonNode partsNode = contentNode.path("parts");

            if (partsNode.isEmpty()) {
                logValidationWarning("extração de conteúdo", "Nenhuma parte encontrada no conteúdo da resposta");
                return null;
            }

            String extractedText = partsNode.get(0).path("text").asText();

            if (logger.isDebugEnabled()) {
                logger.debug("Conteúdo extraído com sucesso: {} caracteres", extractedText.length());
            }

            return extractedText;

        } catch (Exception e) {
            logger.debug("Erro ao extrair conteúdo da resposta: {}", e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Detalhes do erro de extração:", e);
            }
            return null;
        }
    }

    /**
     * Validates if a candidate response is valid and complete.
     *
     * @param candidate The candidate JsonNode from the API response
     * @return true if the response is valid, false otherwise
     */
    protected boolean isValidResponse(JsonNode candidate) {
        String finishReason = candidate.path("finishReason").asText();

        if ("MAX_TOKENS".equals(finishReason)) {
            logValidationWarning("validação de resposta", "Resposta truncada devido ao limite de tokens");
            return false;
        }

        if ("SAFETY".equals(finishReason)) {
            logValidationWarning("validação de resposta", "Resposta bloqueada pelos filtros de segurança");
            return false;
        }

        if ("RECITATION".equals(finishReason)) {
            logValidationWarning("validação de resposta", "Resposta bloqueada por possível recitação de conteúdo");
            return false;
        }

        if ("OTHER".equals(finishReason)) {
            logValidationWarning("validação de resposta", "Resposta finalizada por motivo desconhecido");
            return false;
        }

        // STOP is the expected finish reason for successful responses
        if (!"STOP".equals(finishReason) && !finishReason.isEmpty()) {
            logValidationWarning("validação de resposta", "Motivo de finalização inesperado: " + finishReason);
        }

        return true;
    }

    /**
     * Handles API errors in a consistent manner across all services.
     * Provides standardized logging patterns for different types of errors.
     *
     * @param operation     The operation that failed (for logging context)
     * @param e             The exception that occurred
     * @param errorResponse The error response to return
     * @param <T>           The type of the error response
     * @return The error response
     */
    protected <T> T handleApiError(String operation, Throwable e, T errorResponse) {
        // Log error with consistent format
        logger.error("Erro em operação [{}]: {}", operation, e.getMessage());

        // Log additional context based on exception type
        if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException webEx) {
            logger.error("Status HTTP: {}, Response Body: {}", webEx.getStatusCode(), webEx.getResponseBodyAsString());
        } else if (e instanceof java.net.ConnectException || e instanceof java.net.SocketTimeoutException) {
            logger.error("Erro de conectividade na operação [{}]: Verifique a conexão com a API Gemini", operation);
        } else if (e instanceof com.fasterxml.jackson.core.JsonProcessingException) {
            logger.error("Erro de parsing JSON na operação [{}]: Resposta malformada da API", operation);
        }

        // Always log full stack trace in debug mode
        if (logger.isDebugEnabled()) {
            logger.debug("Detalhes completos do erro para operação [{}]:", operation, e);
        }

        return errorResponse;
    }

    /**
     * Handles API errors with additional context information.
     *
     * @param operation     The operation that failed
     * @param e             The exception that occurred
     * @param errorResponse The error response to return
     * @param context       Additional context information (e.g., request details)
     * @param <T>           The type of the error response
     * @return The error response
     */
    protected <T> T handleApiError(String operation, Exception e, T errorResponse, String context) {
        logger.error("Erro em operação [{}] com contexto [{}]: {}", operation, context, e.getMessage());

        // Log additional context based on exception type
        if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException webEx) {
            logger.error("Status HTTP: {}, Response Body: {}", webEx.getStatusCode(), webEx.getResponseBodyAsString());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Detalhes completos do erro para operação [{}] com contexto [{}]:", operation, context, e);
        }

        return errorResponse;
    }

    /**
     * Logs the start of an AI operation with consistent formatting.
     *
     * @param operation The operation being started
     * @param details   Additional details about the operation
     */
    protected void logOperationStart(String operation, String details) {
        logger.info("Iniciando operação [{}]: {}", operation, details);
        if (logger.isDebugEnabled()) {
            logger.debug("Detalhes da operação [{}]: {}", operation, details);
        }
    }

    /**
     * Logs the successful completion of an AI operation.
     *
     * @param operation  The operation that completed
     * @param duration   The duration in milliseconds (optional, can be null)
     * @param tokensUsed The number of tokens used (optional, can be null)
     */
    protected void logOperationSuccess(String operation, Long duration, Integer tokensUsed) {
        StringBuilder message = new StringBuilder();
        message.append("Operação [").append(operation).append("] concluída com sucesso");

        if (duration != null) {
            message.append(" em ").append(duration).append("ms");
        }

        if (tokensUsed != null) {
            message.append(", tokens utilizados: ").append(tokensUsed);
        }

        logger.info(message.toString());
    }

    /**
     * Logs API request details in debug mode with consistent formatting.
     *
     * @param endpoint    The API endpoint being called
     * @param requestBody The request body (will be truncated if too long)
     */
    protected void logApiRequest(String endpoint, Map<String, Object> requestBody) {
        if (logger.isDebugEnabled()) {
            try {
                String requestJson = objectMapper.writeValueAsString(requestBody);
                // Truncate long requests for readability
                if (requestJson.length() > 1000) {
                    requestJson = requestJson.substring(0, 1000) + "... [truncated]";
                }
                logger.debug("Chamando endpoint Gemini: {}", endpoint);
                logger.debug("Request body: {}", requestJson);
            } catch (Exception e) {
                logger.debug("Chamando endpoint Gemini: {} (erro ao serializar request body)", endpoint);
            }
        }
    }

    /**
     * Logs API response details in debug mode with consistent formatting.
     *
     * @param responseBody The response body (will be truncated if too long)
     */
    protected void logApiResponse(String responseBody) {
        if (logger.isDebugEnabled()) {
            // Truncate long responses for readability
            String logResponse = responseBody;
            if (responseBody.length() > 1000) {
                logResponse = responseBody.substring(0, 1000) + "... [truncated]";
            }
            logger.debug("Resposta da API Gemini: {}", logResponse);
        }
    }

    /**
     * Logs validation warnings with consistent formatting.
     *
     * @param operation The operation context
     * @param warning   The warning message
     */
    protected void logValidationWarning(String operation, String warning) {
        logger.warn("Aviso de validação em [{}]: {}", operation, warning);
    }

    /**
     * Creates a GeminiConfig instance using the provided parameters.
     * Falls back to default values from application configuration if parameters are
     * invalid.
     *
     * @param temperature The temperature parameter (0.0 to 1.0)
     * @param maxTokens   The maximum number of tokens
     * @return A new GeminiConfig instance
     */
    protected GeminiConfig createConfig(double temperature, int maxTokens) {
        return new GeminiConfig(
                temperature > 0 ? temperature : defaultTemperature,
                maxTokens > 0 ? maxTokens : defaultMaxTokens,
                defaultModel,
                defaultApiKey);
    }

    /**
     * Creates a GeminiConfig instance using default values from application
     * configuration.
     *
     * @return A new GeminiConfig instance with default values
     */
    protected GeminiConfig createDefaultConfig() {
        return new GeminiConfig(defaultTemperature, defaultMaxTokens, defaultModel, defaultApiKey);
    }

    /**
     * Extracts token usage information from a Gemini API response.
     *
     * @param responseBody The raw response body from the API
     * @return The number of tokens used, or 0 if extraction fails
     */
    protected int extractTokenUsage(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            int tokensUsed = jsonNode
                    .path("usageMetadata")
                    .path("totalTokenCount")
                    .asInt(0);

            if (logger.isDebugEnabled()) {
                int promptTokens = jsonNode.path("usageMetadata").path("promptTokenCount").asInt(0);
                int candidatesTokens = jsonNode.path("usageMetadata").path("candidatesTokenCount").asInt(0);
                logger.debug("Uso de tokens - Prompt: {}, Candidatos: {}, Total: {}",
                        promptTokens, candidatesTokens, tokensUsed);
            }

            return tokensUsed;

        } catch (Exception e) {
            logger.debug("Não foi possível extrair informações de uso de tokens: {}", e.getMessage());
            return 0;
        }
    }
}