package com.example.aipoc.model;

import java.util.Objects;

/**
 * Immutable value object that encapsulates configuration parameters for Gemini API calls.
 * 
 * <p>This class provides type-safe configuration management for AI service operations,
 * ensuring that all API calls use consistent and validated parameters.
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // For consistent, factual responses (low creativity)
 * GeminiConfig factualConfig = new GeminiConfig(0.1, 500, "gemini-pro", apiKey);
 * 
 * // For creative, varied responses (high creativity)
 * GeminiConfig creativeConfig = new GeminiConfig(0.9, 1000, "gemini-pro", apiKey);
 * 
 * // For balanced responses (medium creativity)
 * GeminiConfig balancedConfig = new GeminiConfig(0.5, 800, "gemini-pro", apiKey);
 * }</pre>
 * 
 * <h3>Parameter Guidelines:</h3>
 * <ul>
 *   <li><strong>Temperature (0.0-1.0):</strong>
 *     <ul>
 *       <li>0.0-0.3: Deterministic, factual responses (classification, analysis)</li>
 *       <li>0.4-0.7: Balanced creativity and consistency (general chat, summaries)</li>
 *       <li>0.8-1.0: High creativity and variation (creative writing, brainstorming)</li>
 *     </ul>
 *   </li>
 *   <li><strong>Max Tokens:</strong>
 *     <ul>
 *       <li>100-500: Short responses (classifications, simple answers)</li>
 *       <li>500-1000: Medium responses (summaries, explanations)</li>
 *       <li>1000+: Long responses (detailed analysis, creative content)</li>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * <p>This class is thread-safe and can be safely shared across multiple service calls.
 * 
 * @author AI POC Team
 * @since 1.0
 * @see BaseGeminiService
 */
public final class GeminiConfig {
    
    private final double temperature;
    private final int maxTokens;
    private final String model;
    private final String apiKey;
    
    /**
     * Creates a new GeminiConfig instance with the specified parameters.
     *
     * @param temperature The temperature parameter for response randomness (0.0 to 1.0)
     * @param maxTokens The maximum number of tokens in the response
     * @param model The Gemini model to use
     * @param apiKey The API key for authentication
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public GeminiConfig(double temperature, int maxTokens, String model, String apiKey) {
        if (temperature < 0.0 || temperature > 1.0) {
            throw new IllegalArgumentException("Temperature must be between 0.0 and 1.0");
        }
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("Max tokens must be positive");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be null or empty");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.model = model.trim();
        this.apiKey = apiKey.trim();
    }
    
    /**
     * Gets the temperature parameter for response randomness.
     *
     * @return the temperature value
     */
    public double getTemperature() {
        return temperature;
    }
    
    /**
     * Gets the maximum number of tokens in the response.
     *
     * @return the max tokens value
     */
    public int getMaxTokens() {
        return maxTokens;
    }
    
    /**
     * Gets the Gemini model to use.
     *
     * @return the model name
     */
    public String getModel() {
        return model;
    }
    
    /**
     * Gets the API key for authentication.
     *
     * @return the API key
     */
    public String getApiKey() {
        return apiKey;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        GeminiConfig that = (GeminiConfig) obj;
        return Double.compare(that.temperature, temperature) == 0 &&
               maxTokens == that.maxTokens &&
               Objects.equals(model, that.model) &&
               Objects.equals(apiKey, that.apiKey);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(temperature, maxTokens, model, apiKey);
    }
    
    @Override
    public String toString() {
        return "GeminiConfig{" +
               "temperature=" + temperature +
               ", maxTokens=" + maxTokens +
               ", model='" + model + '\'' +
               ", apiKey='***'" + // Hide API key in toString
               '}';
    }
}