package com.example.aipoc.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class SummaryRequest {
    
    @NotBlank(message = "O texto não pode estar vazio")
    @Size(max = 10000, message = "O texto deve ter no máximo 10000 caracteres")
    private String text;
    
    @Min(value = 1, message = "Número de sentenças deve ser pelo menos 1")
    @Max(value = 10, message = "Número de sentenças deve ser no máximo 10")
    private int maxSentences = 3;
    
    private String style = "conciso"; // conciso, detalhado, bullet-points
    
    public SummaryRequest() {}
    
    public SummaryRequest(String text, int maxSentences, String style) {
        this.text = text;
        this.maxSentences = maxSentences;
        this.style = style;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public int getMaxSentences() {
        return maxSentences;
    }
    
    public void setMaxSentences(int maxSentences) {
        this.maxSentences = maxSentences;
    }
    
    public String getStyle() {
        return style;
    }
    
    public void setStyle(String style) {
        this.style = style;
    }
}