#!/bin/bash

echo "🧪 Testando API do Gemini..."

# Teste 1: Health check
echo "1. Testando health check..."
curl -s http://localhost:8080/api/ai/health | jq .

echo -e "\n2. Testando chat com IA..."
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Olá! Você está funcionando?"}' | jq .

echo -e "\n3. Testando análise de sentimento..."
curl -X POST http://localhost:8080/api/ai/sentiment \
  -H "Content-Type: application/json" \
  -d '{"text": "Estou muito feliz com este projeto! Está funcionando perfeitamente."}' | jq .

echo -e "\n4. Testando resumo de texto..."
curl -X POST http://localhost:8080/api/ai/summary \
  -H "Content-Type: application/json" \
  -d '{"text": "A inteligência artificial é uma tecnologia revolucionária que está transformando diversos setores da economia. Ela permite que máquinas aprendam, raciocinem e tomem decisões de forma autônoma. No setor de saúde, a IA ajuda no diagnóstico de doenças. Na educação, personaliza o aprendizado. No transporte, possibilita veículos autônomos.", "maxSentences": 2, "style": "conciso"}' | jq .

echo -e "\n5. Testando classificação de produto por tangibilidade..."
curl -X POST http://localhost:8080/api/ai/product/classify \
  -H "Content-Type: application/json" \
  -d '{"productName": "iPhone 15", "description": "Smartphone com tela de 6.1 polegadas, câmera de 48MP e chip A17", "category": "Eletrônicos"}' | jq .

echo -e "\n6. Testando classificação de serviço..."
curl -X POST http://localhost:8080/api/ai/product/classify \
  -H "Content-Type: application/json" \
  -d '{"productName": "Consultoria em TI", "description": "Serviços de consultoria especializada em transformação digital", "category": "Serviços"}' | jq .

echo -e "\n7. Testando informações da API..."
curl -s http://localhost:8080/api/ai/info | jq .

echo -e "\n✅ Todos os testes concluídos!"