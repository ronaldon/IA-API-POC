package com.example.aipoc.service;

import com.example.aipoc.model.GeminiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseGeminiServiceTest {

    @Mock
    private WebClient geminiWebClient;

    @Mock
    private Logger logger;

    private TestableBaseGeminiService service;

    @BeforeEach
    void setUp() {
        service = new TestableBaseGeminiService();
        service.geminiWebClient = geminiWebClient;
        
        // Set default configuration values
        ReflectionTestUtils.setField(service, "defaultModel", "gemini-pro");
        ReflectionTestUtils.setField(service, "defaultMaxTokens", 1000);
        ReflectionTestUtils.setField(service, "defaultTemperature", 0.7);
        ReflectionTestUtils.setField(service, "defaultApiKey", "test-api-key");
        
        // Replace logger with mock
        ReflectionTestUtils.setField(service, "logger", logger);
    }

    @Test
    void testHandleApiError_WithGenericException() {
        Exception testException = new RuntimeException("Test error message");
        String errorResponse = "Error occurred";
        
        String result = service.handleApiError("test operation", testException, errorResponse);
        
        assertEquals(errorResponse, result);
        verify(logger).error("Erro em operação [{}]: {}", "test operation", "Test error message");
    }

    @Test
    void testHandleApiError_WithWebClientException() {
        @SuppressWarnings("null")
        WebClientResponseException webEx = WebClientResponseException.create(
            500, "Internal Server Error", null, "Server error".getBytes(), null);
        String errorResponse = "API Error";
        
        String result = service.handleApiError("API call", webEx, errorResponse);
        
        assertEquals(errorResponse, result);
        verify(logger).error("Erro em operação [{}]: {}", "API call", webEx.getMessage());
        verify(logger).error("Status HTTP: {}, Response Body: {}", webEx.getStatusCode(), "Server error");
    }

    @Test
    void testHandleApiError_WithContext() {
        Exception testException = new RuntimeException("Test error");
        String errorResponse = "Error with context";
        String context = "processing user request";
        
        String result = service.handleApiError("test operation", testException, errorResponse, context);
        
        assertEquals(errorResponse, result);
        verify(logger).error("Erro em operação [{}] com contexto [{}]: {}", 
                           "test operation", context, "Test error");
    }

    @Test
    void testLogOperationStart() {
        service.logOperationStart("sentiment analysis", "analyzing user text");
        
        verify(logger).info("Iniciando operação [{}]: {}", "sentiment analysis", "analyzing user text");
    }

    @Test
    void testLogOperationSuccess_WithDurationAndTokens() {
        service.logOperationSuccess("text summary", 1500L, 250);
        
        verify(logger).info("Operação [text summary] concluída com sucesso em 1500ms, tokens utilizados: 250");
    }

    @Test
    void testLogOperationSuccess_WithoutOptionalParams() {
        service.logOperationSuccess("classification", null, null);
        
        verify(logger).info("Operação [classification] concluída com sucesso");
    }

    @Test
    void testLogValidationWarning() {
        service.logValidationWarning("response parsing", "empty response received");
        
        verify(logger).warn("Aviso de validação em [{}]: {}", "response parsing", "empty response received");
    }

    @Test
    void testCreateConfig() {
        GeminiConfig config = service.createConfig(0.8, 500);
        
        assertEquals(0.8, config.getTemperature());
        assertEquals(500, config.getMaxTokens());
        assertEquals("gemini-pro", config.getModel());
        assertEquals("test-api-key", config.getApiKey());
    }

    @Test
    void testCreateConfig_WithInvalidValues() {
        GeminiConfig config = service.createConfig(-0.1, -100);
        
        // Should fall back to default values
        assertEquals(0.7, config.getTemperature());
        assertEquals(1000, config.getMaxTokens());
    }

    @Test
    void testCreateDefaultConfig() {
        GeminiConfig config = service.createDefaultConfig();
        
        assertEquals(0.7, config.getTemperature());
        assertEquals(1000, config.getMaxTokens());
        assertEquals("gemini-pro", config.getModel());
        assertEquals("test-api-key", config.getApiKey());
    }

    @Test
    void testExtractTokenUsage_ValidResponse() {
        String responseBody = """
            {
                "usageMetadata": {
                    "promptTokenCount": 10,
                    "candidatesTokenCount": 15,
                    "totalTokenCount": 25
                }
            }
            """;
        
        int tokens = service.extractTokenUsage(responseBody);
        
        assertEquals(25, tokens);
    }

    @Test
    void testExtractTokenUsage_InvalidResponse() {
        String responseBody = "invalid json";
        
        int tokens = service.extractTokenUsage(responseBody);
        
        assertEquals(0, tokens);
    }

    @Test
    void testBuildBaseRequestBody() {
        GeminiConfig config = new GeminiConfig(0.5, 200, "gemini-pro", "api-key");
        
        Map<String, Object> requestBody = service.buildBaseRequestBody("Test prompt", config);
        
        assertNotNull(requestBody);
        assertTrue(requestBody.containsKey("generationConfig"));
        assertTrue(requestBody.containsKey("contents"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> generationConfig = (Map<String, Object>) requestBody.get("generationConfig");
        assertEquals(0.5, generationConfig.get("temperature"));
        assertEquals(200, generationConfig.get("maxOutputTokens"));
    }

    // Testable concrete implementation of BaseGeminiService
    private static class TestableBaseGeminiService extends BaseGeminiService {
        // Expose protected methods for testing
        @Override
        public <T> T handleApiError(String operation, Throwable e, T errorResponse) {
            return super.handleApiError(operation, e, errorResponse);
        }
        
        @Override
        public <T> T handleApiError(String operation, Exception e, T errorResponse, String context) {
            return super.handleApiError(operation, e, errorResponse, context);
        }
        
        @Override
        public void logOperationStart(String operation, String details) {
            super.logOperationStart(operation, details);
        }
        
        @Override
        public void logOperationSuccess(String operation, Long duration, Integer tokensUsed) {
            super.logOperationSuccess(operation, duration, tokensUsed);
        }
        
        @Override
        public void logValidationWarning(String operation, String warning) {
            super.logValidationWarning(operation, warning);
        }
        
        @Override
        public GeminiConfig createConfig(double temperature, int maxTokens) {
            return super.createConfig(temperature, maxTokens);
        }
        
        @Override
        public GeminiConfig createDefaultConfig() {
            return super.createDefaultConfig();
        }
        
        @Override
        public int extractTokenUsage(String responseBody) {
            return super.extractTokenUsage(responseBody);
        }
        
        @Override
        public Map<String, Object> buildBaseRequestBody(String prompt, GeminiConfig config) {
            return super.buildBaseRequestBody(prompt, config);
        }
    }
}