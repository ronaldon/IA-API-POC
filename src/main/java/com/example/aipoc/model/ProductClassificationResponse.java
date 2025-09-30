package com.example.aipoc.model;

import java.time.LocalDateTime;
import java.util.List;

public class ProductClassificationResponse {
    
    private String tangibilityType; // TANGIBLE, INTANGIBLE, HYBRID
    private double confidence; // 0.0 to 1.0
    private String explanation;
    private List<String> characteristics;
    private String productName;
    private LocalDateTime timestamp;
    private boolean success;
    private String error;
    
    // Subcategorias de tangibilidade
    private String tangibilitySubtype; // Para produtos tangíveis: DURABLE, NON_DURABLE, CONSUMABLE
                                      // Para produtos intangíveis: SERVICE, DIGITAL, EXPERIENCE, KNOWLEDGE
    
    private String productPriceCategory;

    private String lifeCycle;

    public String getLifeCycle() {
        return lifeCycle;
    }

    public void setLifeCycle(String lifeCycle) {
        this.lifeCycle = lifeCycle;
    }

    public String getProductPriceCategory() {
        return productPriceCategory;
    }

    public void setProductPriceCategory(String productPriceType) {
        this.productPriceCategory = productPriceType;
    }

    public ProductClassificationResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ProductClassificationResponse(
        String tangibilityType, double confidence, String explanation, 
        List<String> characteristics, String productName, String tangibilitySubtype, String productPriceCategory, String lifeCycle) {
        this();
        this.tangibilityType = tangibilityType;
        this.confidence = confidence;
        this.explanation = explanation;
        this.characteristics = characteristics;
        this.productName = productName;
        this.tangibilitySubtype = tangibilitySubtype;
        this.success = true;
        this.productPriceCategory = productPriceCategory;
        this.lifeCycle = lifeCycle;
    }
    
    public static ProductClassificationResponse error(String error) {
        ProductClassificationResponse response = new ProductClassificationResponse();
        response.error = error;
        response.success = false;
        return response;
    }
    
    // Getters and Setters
    public String getTangibilityType() {
        return tangibilityType;
    }
    
    public void setTangibilityType(String tangibilityType) {
        this.tangibilityType = tangibilityType;
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
    
    public List<String> getCharacteristics() {
        return characteristics;
    }
    
    public void setCharacteristics(List<String> characteristics) {
        this.characteristics = characteristics;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getTangibilitySubtype() {
        return tangibilitySubtype;
    }
    
    public void setTangibilitySubtype(String tangibilitySubtype) {
        this.tangibilitySubtype = tangibilitySubtype;
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