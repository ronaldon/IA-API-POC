package com.example.aipoc.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AiRequest {
    
    @NotBlank(message = "A mensagem não pode estar vazia")
    @Size(max = 2000, message = "A mensagem deve ter no máximo 2000 caracteres")
    private String message;
    
    private String context;
    
    public AiRequest() {}
    
    public AiRequest(String message, String context) {
        this.message = message;
        this.context = context;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
}