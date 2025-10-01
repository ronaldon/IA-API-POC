package com.example.aipoc.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GeminiConfig Tests")
class GeminiConfigTest {

    private static final double VALID_TEMPERATURE = 0.7;
    private static final int VALID_MAX_TOKENS = 4000;
    private static final String VALID_MODEL = "gemini-2.5-flash";
    private static final String VALID_API_KEY = "test-api-key-123";

    @Test
    @DisplayName("Should create GeminiConfig with valid parameters")
    void shouldCreateGeminiConfigWithValidParameters() {
        // When
        GeminiConfig config = new GeminiConfig(VALID_TEMPERATURE, VALID_MAX_TOKENS, VALID_MODEL, VALID_API_KEY);

        // Then
        assertNotNull(config);
        assertEquals(VALID_TEMPERATURE, config.getTemperature());
        assertEquals(VALID_MAX_TOKENS, config.getMaxTokens());
        assertEquals(VALID_MODEL, config.getModel());
        assertEquals(VALID_API_KEY, config.getApiKey());
    }

    @Test
    @DisplayName("Should trim whitespace from model and apiKey")
    void shouldTrimWhitespaceFromModelAndApiKey() {
        // Given
        String modelWithSpaces = "  " + VALID_MODEL + "  ";
        String apiKeyWithSpaces = "  " + VALID_API_KEY + "  ";

        // When
        GeminiConfig config = new GeminiConfig(VALID_TEMPERATURE, VALID_MAX_TOKENS, modelWithSpaces, apiKeyWithSpaces);

        // Then
        assertEquals(VALID_MODEL, config.getModel());
        assertEquals(VALID_API_KEY, config.getApiKey());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, -1.0, 1.1, 2.0})
    @DisplayName("Should throw IllegalArgumentException for invalid temperature")
    void shouldThrowExceptionForInvalidTemperature(double invalidTemperature) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new GeminiConfig(invalidTemperature, VALID_MAX_TOKENS, VALID_MODEL, VALID_API_KEY)
        );
        assertEquals("Temperature must be between 0.0 and 1.0", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Should throw IllegalArgumentException for invalid maxTokens")
    void shouldThrowExceptionForInvalidMaxTokens(int invalidMaxTokens) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new GeminiConfig(VALID_TEMPERATURE, invalidMaxTokens, VALID_MODEL, VALID_API_KEY)
        );
        assertEquals("Max tokens must be positive", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException for empty or whitespace model")
    void shouldThrowExceptionForEmptyModel(String invalidModel) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new GeminiConfig(VALID_TEMPERATURE, VALID_MAX_TOKENS, invalidModel, VALID_API_KEY)
        );
        assertEquals("Model cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null model")
    void shouldThrowExceptionForNullModel() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new GeminiConfig(VALID_TEMPERATURE, VALID_MAX_TOKENS, null, VALID_API_KEY)
        );
        assertEquals("Model cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException for empty or whitespace apiKey")
    void shouldThrowExceptionForEmptyApiKey(String invalidApiKey) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new GeminiConfig(VALID_TEMPERATURE, VALID_MAX_TOKENS, VALID_MODEL, invalidApiKey)
        );
        assertEquals("API key cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null apiKey")
    void shouldThrowExceptionForNullApiKey() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new GeminiConfig(VALID_TEMPERATURE, VALID_MAX_TOKENS, VALID_MODEL, null)
        );
        assertEquals("API key cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should be immutable - getters return same values")
    void shouldBeImmutable() {
        // Given
        GeminiConfig config = new GeminiConfig(VALID_TEMPERATURE, VALID_MAX_TOKENS, VALID_MODEL, VALID_API_KEY);

        // When - multiple calls to getters
        double temp1 = config.getTemperature();
        double temp2 = config.getTemperature();
        int tokens1 = config.getMaxTokens();
        int tokens2 = config.getMaxTokens();
        String model1 = config.getModel();
        String model2 = config.getModel();
        String key1 = config.getApiKey();
        String key2 = config.getApiKey();

        // Then - values should be consistent
        assertEquals(temp1, temp2);
        assertEquals(tokens1, tokens2);
        assertEquals(model1, model2);
        assertEquals(key1, key2);
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        GeminiConfig config1 = new GeminiConfig(VALID_TEMPERATURE, VALID_MAX_TOKENS, VALID_MODEL, VALID_API_KEY);
        GeminiConfig config2 = new GeminiConfig(VALID_TEMPERATURE, VALID_MAX_TOKENS, VALID_MODEL, VALID_API_KEY);
        GeminiConfig config3 = new GeminiConfig(0.5, VALID_MAX_TOKENS, VALID_MODEL, VALID_API_KEY);

        // Then
        assertEquals(config1, config2);
        assertNotEquals(config1, config3);
        assertEquals(config1, config1); // reflexive
        assertNotEquals(config1, null);
        assertNotEquals(config1, "not a GeminiConfig");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        GeminiConfig config1 = new GeminiConfig(VALID_TEMPERATURE, VALID_MAX_TOKENS, VALID_MODEL, VALID_API_KEY);
        GeminiConfig config2 = new GeminiConfig(VALID_TEMPERATURE, VALID_MAX_TOKENS, VALID_MODEL, VALID_API_KEY);

        // Then
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should hide API key in toString")
    void shouldHideApiKeyInToString() {
        // Given
        GeminiConfig config = new GeminiConfig(VALID_TEMPERATURE, VALID_MAX_TOKENS, VALID_MODEL, VALID_API_KEY);

        // When
        String toString = config.toString();

        // Then
        assertFalse(toString.contains(VALID_API_KEY), "API key should not be visible in toString");
        assertTrue(toString.contains("apiKey='***'"), "API key should be masked in toString");
        assertTrue(toString.contains("temperature=" + VALID_TEMPERATURE));
        assertTrue(toString.contains("maxTokens=" + VALID_MAX_TOKENS));
        assertTrue(toString.contains("model='" + VALID_MODEL + "'"));
    }

    @Test
    @DisplayName("Should accept boundary temperature values")
    void shouldAcceptBoundaryTemperatureValues() {
        // When & Then - should not throw exceptions
        assertDoesNotThrow(() -> new GeminiConfig(0.0, VALID_MAX_TOKENS, VALID_MODEL, VALID_API_KEY));
        assertDoesNotThrow(() -> new GeminiConfig(1.0, VALID_MAX_TOKENS, VALID_MODEL, VALID_API_KEY));
    }

    @Test
    @DisplayName("Should accept minimum valid maxTokens")
    void shouldAcceptMinimumValidMaxTokens() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> new GeminiConfig(VALID_TEMPERATURE, 1, VALID_MODEL, VALID_API_KEY));
    }
}