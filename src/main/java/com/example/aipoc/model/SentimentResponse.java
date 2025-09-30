package com.example.aipoc.model;

import java.time.LocalDateTime;

public class SentimentResponse {
    
    private String sentiment; // POSITIVE, NEGATIVE, NEUTRAL
    private double confidence; // 0.0 to 1.0
    private String explanation;
    private String originalText;
    private LocalDateTime timestamp;
    private boolean success;
    private String error;
    
    public SentimentResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public SentimentResponse(String sentiment, double confidence, String explanation, String originalText) {
        this();
        this.sentiment = sentiment;
        this.confidence = confidence;
        this.explanation = explanation;
        this.originalText = originalText;
        this.success = true;
    }
    
    public static SentimentResponse error(String error) {
        SentimentResponse response = new SentimentResponse();
        response.error = error;
        response.success = false;
        return response;
    }
    
    // Getters and Setters
    public String getSentiment() {
        return sentiment;
    }
    
    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public String getOriginalText() {
        return originalText;
    }
    
    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
}