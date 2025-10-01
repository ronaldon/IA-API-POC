package com.example.aipoc.service;

import com.example.aipoc.model.AiRequest;
import com.example.aipoc.model.AiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AiGeneralServiceTest {

    private AiGeneralService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiGeneralService();
        // Set up default configuration values using reflection
        ReflectionTestUtils.setField(aiService, "defaultModel", "gemini-pro");
        ReflectionTestUtils.setField(aiService, "defaultMaxTokens", 1000);
        ReflectionTestUtils.setField(aiService, "defaultTemperature", 0.7);
        ReflectionTestUtils.setField(aiService, "defaultApiKey", "test-api-key");
    }

    @Test
    void buildPrompt_ShouldIncludeContextAndMessage_WhenContextProvided() {
        // Arrange
        AiRequest request = new AiRequest();
        request.setMessage("What is AI?");
        request.setContext("Technology discussion");

        // Act
        String prompt = ReflectionTestUtils.invokeMethod(aiService, "buildPrompt", request);

        // Assert
        assertNotNull(prompt);
        assertTrue(prompt.contains("Technology discussion"));
        assertTrue(prompt.contains("What is AI?"));
        assertTrue(prompt.contains("Contexto:"));
        assertTrue(prompt.contains("Pergunta:"));
    }

    @Test
    void buildPrompt_ShouldOnlyIncludeMessage_WhenNoContextProvided() {
        // Arrange
        AiRequest request = new AiRequest();
        request.setMessage("Hello");

        // Act
        String prompt = ReflectionTestUtils.invokeMethod(aiService, "buildPrompt", request);

        // Assert
        assertNotNull(prompt);
        assertTrue(prompt.contains("Hello"));
        assertFalse(prompt.contains("Contexto:"));
        assertTrue(prompt.contains("Pergunta:"));
    }

    @Test
    void buildPrompt_ShouldHandleEmptyContext() {
        // Arrange
        AiRequest request = new AiRequest();
        request.setMessage("Test message");
        request.setContext("");

        // Act
        String prompt = ReflectionTestUtils.invokeMethod(aiService, "buildPrompt", request);

        // Assert
        assertNotNull(prompt);
        assertTrue(prompt.contains("Test message"));
        assertFalse(prompt.contains("Contexto:"));
        assertTrue(prompt.contains("Pergunta:"));
    }

    @Test
    void buildPrompt_ShouldHandleWhitespaceOnlyContext() {
        // Arrange
        AiRequest request = new AiRequest();
        request.setMessage("Test message");
        request.setContext("   ");

        // Act
        String prompt = ReflectionTestUtils.invokeMethod(aiService, "buildPrompt", request);

        // Assert
        assertNotNull(prompt);
        assertTrue(prompt.contains("Test message"));
        assertFalse(prompt.contains("Contexto:"));
        assertTrue(prompt.contains("Pergunta:"));
    }

    @Test
    void parseGeminiResponse_ShouldReturnSuccessfulResponse_WhenValidResponseProvided() {
        // Arrange
        String validResponse = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "Test AI response"
                        }]
                    },
                    "finishReason": "STOP"
                }],
                "usageMetadata": {
                    "totalTokenCount": 50
                }
            }
            """;

        // Act
        AiResponse response = ReflectionTestUtils.invokeMethod(aiService, "parseGeminiResponse", validResponse);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Test AI response", response.getResponse());
        assertEquals("gemini-pro", response.getModel());
        assertEquals(50, response.getTokensUsed());
    }

    @Test
    void parseGeminiResponse_ShouldReturnErrorResponse_WhenEmptyResponse() {
        // Arrange
        String emptyResponse = """
            {
                "candidates": []
            }
            """;

        // Act
        AiResponse response = ReflectionTestUtils.invokeMethod(aiService, "parseGeminiResponse", emptyResponse);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Nenhuma resposta válida gerada pela IA", response.getError());
    }

    @Test
    void parseGeminiResponse_ShouldReturnErrorResponse_WhenMalformedJson() {
        // Arrange
        String malformedResponse = "{ invalid json }";

        // Act
        AiResponse response = ReflectionTestUtils.invokeMethod(aiService, "parseGeminiResponse", malformedResponse);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Nenhuma resposta válida gerada pela IA", response.getError());
    }

    @Test
    void parseGeminiResponse_ShouldHandleTokenUsageCorrectly() {
        // Arrange
        String responseWithoutTokens = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "Response without token info"
                        }]
                    },
                    "finishReason": "STOP"
                }]
            }
            """;

        // Act
        AiResponse response = ReflectionTestUtils.invokeMethod(aiService, "parseGeminiResponse", responseWithoutTokens);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Response without token info", response.getResponse());
        assertEquals(0, response.getTokensUsed()); // Should default to 0 when no token info
    }
}