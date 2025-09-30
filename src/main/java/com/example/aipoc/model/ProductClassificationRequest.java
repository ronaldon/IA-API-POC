package com.example.aipoc.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductClassificationRequest {
    
    @NotBlank(message = "O nome do produto não pode estar vazio")
    @Size(max = 200, message = "O nome do produto deve ter no máximo 200 caracteres")
    private String productName;
    
    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    private String description;
    
    @Size(max = 100, message = "A categoria deve ter no máximo 100 caracteres")
    private String category;
    
    public ProductClassificationRequest() {}
    
    public ProductClassificationRequest(String productName, String description, String category) {
        this.productName = productName;
        this.description = description;
        this.category = category;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
}