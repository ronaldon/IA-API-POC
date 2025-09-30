package com.example.aipoc.model;

import java.time.LocalDateTime;

public class AiResponse {
    
    private String response;
    private String model;
    private int tokensUsed;
    private LocalDateTime timestamp;
    private boolean success;
    private String error;
    
    public AiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public AiResponse(String response, String model, int tokensUsed) {
        this();
        this.response = response;
        this.model = model;
        this.tokensUsed = tokensUsed;
        this.success = true;
    }
    
    public static AiResponse error(String error) {
        AiResponse response = new AiResponse();
        response.error = error;
        response.success = false;
        return response;
    }
    
    // Getters and Setters
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public int getTokensUsed() {
        return tokensUsed;
    }
    
    public void setTokensUsed(int tokensUsed) {
        this.tokensUsed = tokensUsed;
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