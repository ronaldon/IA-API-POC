#!/bin/bash

echo "🏷️  Testando Classificação de Produtos por Tangibilidade..."

BASE_URL="http://localhost:8080/api/ai/product/classify"

echo -e "\n1. 📱 Produto Tangível Durável - iPhone"
curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "iPhone 15 Pro",
    "description": "Smartphone premium com tela de 6.1 polegadas, câmera de 48MP",
    "category": "Eletrônicos"
  }' | jq '.tangibilityType, .tangibilitySubtype, .confidence, .explanation'

echo -e "\n2. 🧴 Produto Tangível Consumível - Shampoo"
curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Shampoo Pantene",
    "description": "Shampoo para cabelos oleosos, 400ml",
    "category": "Cosméticos"
  }' | jq '.tangibilityType, .tangibilitySubtype, .confidence'

echo -e "\n3. 💼 Produto Intangível - Serviço"
curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Consultoria em TI",
    "description": "Serviços de consultoria especializada em transformação digital",
    "category": "Serviços"
  }' | jq '.tangibilityType, .tangibilitySubtype, .confidence'

echo -e "\n4. 💻 Produto Intangível - Digital"
curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Adobe Photoshop",
    "description": "Software de edição de imagens profissional",
    "category": "Software"
  }' | jq '.tangibilityType, .tangibilitySubtype, .confidence'

echo -e "\n5. 🎓 Produto Intangível - Experiência"
curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Curso de Python Online",
    "description": "Curso completo de programação Python com certificado",
    "category": "Educação"
  }' | jq '.tangibilityType, .tangibilitySubtype, .confidence'

echo -e "\n6. 🚗 Produto Híbrido - Tesla"
curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Tesla Model 3 com Autopilot",
    "description": "Veículo elétrico com sistema de direção autônoma e atualizações de software",
    "category": "Automotivo"
  }' | jq '.tangibilityType, .tangibilitySubtype, .confidence'

echo -e "\n✅ Testes de classificação concluídos!"