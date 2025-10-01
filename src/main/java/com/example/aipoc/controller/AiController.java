package com.example.aipoc.controller;

import com.example.aipoc.model.AiRequest;
import com.example.aipoc.model.AiResponse;
import com.example.aipoc.model.SentimentRequest;
import com.example.aipoc.model.SentimentResponse;
import com.example.aipoc.service.AiGeneralService;
import com.example.aipoc.service.SentimentAnalysisService;
import com.example.aipoc.model.SummaryRequest;
import com.example.aipoc.service.TextSummaryService;
import com.example.aipoc.model.ProductClassificationRequest;
import com.example.aipoc.model.ProductClassificationResponse;
import com.example.aipoc.service.ProductTangibilityService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    @Autowired
    private AiGeneralService aiService;

    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;

    @Autowired
    private TextSummaryService textSummaryService;

    @Autowired
    private ProductTangibilityService productTangibilityService;

    @PostMapping("/chat")
    public Mono<ResponseEntity<AiResponse>> chat(@Valid @RequestBody AiRequest request) {
        logger.info("Recebida solicitação de chat: {}", request.getMessage());

        return aiService.processMessage(request)
                .map(response -> {
                    if (response.isSuccess()) {
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.internalServerError().body(response);
                    }
                });
    }

    @PostMapping("/sentiment")
    public Mono<ResponseEntity<SentimentResponse>> analyzeSentiment(@Valid @RequestBody SentimentRequest request) {
        logger.info("Recebida solicitação de análise de sentimento");

        return sentimentAnalysisService.analyzeSentiment(request)
                .map(response -> {
                    if (response.isSuccess()) {
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.internalServerError().body(response);
                    }
                });
    }

    @PostMapping("/summary")
    public Mono<ResponseEntity<AiResponse>> summarizeText(@Valid @RequestBody SummaryRequest request) {
        logger.info("Recebida solicitação de resumo de texto");

        return textSummaryService.summarizeText(request)
                .map(response -> {
                    if (response.isSuccess()) {
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.internalServerError().body(response);
                    }
                });
    }

    @PostMapping("/product/classify")
    public Mono<ResponseEntity<ProductClassificationResponse>> classifyProduct(
            @Valid @RequestBody ProductClassificationRequest request) {
        logger.info("Recebida solicitação de classificação de produto: {}", request.getProductName());

        return productTangibilityService.classifyProduct(request)
                .map(response -> {
                    if (response.isSuccess()) {
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.internalServerError().body(response);
                    }
                });
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "AI POC Service");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Spring Boot AI POC");
        info.put("version", "1.0.0");
        info.put("description", "Projeto POC integrando Spring Boot com IA");
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("chat", "POST /api/ai/chat - Enviar mensagem para IA");
        endpoints.put("sentiment", "POST /api/ai/sentiment - Análise de sentimento");
        endpoints.put("summary", "POST /api/ai/summary - Resumo de texto");
        endpoints.put("product-classify", "POST /api/ai/product/classify - Classificação de produto por tangibilidade");
        endpoints.put("health", "GET /api/ai/health - Status do serviço");
        endpoints.put("info", "GET /api/ai/info - Informações do serviço");
        info.put("endpoints", endpoints);
        return ResponseEntity.ok(info);
    }
}