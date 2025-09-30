package com.example.aipoc.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SentimentRequest {
    
    @NotBlank(message = "O texto não pode estar vazio")
    @Size(max = 5000, message = "O texto deve ter no máximo 5000 caracteres")
    private String text;
    
    private String language = "pt";
    
    public SentimentRequest() {}
    
    public SentimentRequest(String text, String language) {
        this.text = text;
        this.language = language;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
}