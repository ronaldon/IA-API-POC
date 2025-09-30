# Spring Boot AI POC

Projeto de Prova de Conceito (POC) integrando Spring Boot com Inteligência Artificial usando o Google Gemini.

## Funcionalidades

- ✅ **Chat Geral** - Conversa livre com IA
- ✅ **Análise de Sentimento** - Detecta emoções em textos
- ✅ **Resumo de Texto** - Sumarização automática
- ✅ **Classificação de Produtos** - Classifica por tangibilidade
- ✅ **API REST** completa e documentada
- ✅ **Integração Gemini 2.5 Flash** - IA de última geração
- ✅ **Validação robusta** de entrada
- ✅ **Tratamento de erros** abrangente
- ✅ **Logs estruturados** para debug
- ✅ **Configuração flexível** via YAML

## Endpoints

### POST /api/ai/chat
Conversa geral com a IA.

**Request:**
```json
{
  "message": "Sua pergunta aqui",
  "context": "Contexto opcional"
}
```

### POST /api/ai/sentiment
Análise de sentimento de texto.

**Request:**
```json
{
  "text": "Texto para analisar",
  "language": "pt"
}
```

**Response:**
```json
{
  "sentiment": "POSITIVE",
  "confidence": 0.85,
  "explanation": "Texto contém palavras positivas",
  "originalText": "...",
  "success": true
}
```

### POST /api/ai/summary
Resumo automático de textos.

**Request:**
```json
{
  "text": "Texto longo para resumir...",
  "maxSentences": 3,
  "style": "conciso"
}
```

**Estilos disponíveis:** `conciso`, `detalhado`, `bullet-points`

### POST /api/ai/product/classify
Classificação de produtos por tangibilidade.

**Request:**
```json
{
  "productName": "iPhone 15",
  "description": "Smartphone com tela de 6.1 polegadas...",
  "category": "Eletrônicos"
}
```

**Response:**
```json
{
  "tangibilityType": "TANGIBLE",
  "tangibilitySubtype": "DURABLE",
  "confidence": 0.95,
  "explanation": "Produto físico durável com características materiais",
  "characteristics": ["Objeto físico", "Durável", "Tecnológico"],
  "productName": "iPhone 15",
  "success": true
}
```

**Tipos de Tangibilidade:**
- **TANGIBLE**: Produtos físicos
  - `DURABLE`: Duráveis (carros, móveis, eletrônicos)
  - `NON_DURABLE`: Não duráveis (roupas, calçados)
  - `CONSUMABLE`: Consumíveis (alimentos, cosméticos)
- **INTANGIBLE**: Produtos intangíveis
  - `SERVICE`: Serviços (consultoria, limpeza)
  - `DIGITAL`: Digitais (software, apps, e-books)
  - `EXPERIENCE`: Experiências (viagens, eventos)
  - `KNOWLEDGE`: Conhecimento (patentes, licenças)
- **HYBRID**: Combinação de elementos tangíveis e intangíveis

### GET /api/ai/health
Status do serviço.

### GET /api/ai/info
Informações e endpoints disponíveis.

## Configuração

1. **Obter API Key do Google Gemini:**
   - Acesse https://aistudio.google.com/app/apikey
   - Faça login com sua conta Google
   - Clique em "Create API Key"
   - Copie a chave gerada

2. **Configurar variável de ambiente:**
   ```bash
   export GEMINI_API_KEY=sua-api-key-aqui
   ```

3. **Ou editar application.yml:**
   ```yaml
   gemini:
     api:
       key: sua-api-key-aqui
   ```

## Como executar

1. **Compilar o projeto:**
   ```bash
   mvn clean compile
   ```

2. **Executar a aplicação:**
   ```bash
   mvn spring-boot:run
   ```

3. **Testar a API:**
   ```bash
   curl -X POST http://localhost:8080/api/ai/chat \
     -H "Content-Type: application/json" \
     -d '{"message": "Olá, como você está?"}'
   ```

## Estrutura do Projeto

```
src/main/java/com/example/aipoc/
├── AiPocApplication.java          # Classe principal
├── controller/
│   └── AiController.java          # Controlador REST
├── service/
│   └── AiService.java             # Lógica de negócio
├── model/
│   ├── AiRequest.java             # Modelo de requisição
│   └── AiResponse.java            # Modelo de resposta
└── config/
    └── GeminiConfig.java          # Configuração do Gemini
```

## Tecnologias Utilizadas

- **Spring Boot 3.2.0** - Framework principal
- **Spring WebFlux** - Cliente HTTP reativo
- **Jackson** - Serialização JSON
- **Bean Validation** - Validação de entrada
- **SLF4J** - Logging

## Próximos Passos

- [ ] Adicionar cache de respostas
- [ ] Implementar rate limiting
- [ ] Adicionar métricas e monitoramento
- [ ] Criar interface web simples
- [ ] Adicionar suporte a diferentes modelos de IA
- [ ] Implementar streaming de respostas
## V
antagens do Google Gemini

- **🆓 Tier Gratuito Generoso**: 15 requisições por minuto, 1 milhão de tokens por minuto
- **⚡ Alta Performance**: Gemini 1.5 Flash é otimizado para velocidade
- **🌍 Multimodal**: Suporte a texto, imagens, áudio e vídeo
- **💰 Custo-benefício**: Preços competitivos para uso comercial
- **🔒 Segurança**: Infraestrutura robusta do Google

## Como obter API Key do Gemini

1. Acesse **https://aistudio.google.com/app/apikey**
2. Faça login com sua conta Google
3. Clique em "**Create API Key**"
4. Escolha um projeto existente ou crie um novo
5. Copie a chave gerada (formato: `AIza...`)

## Modelos Disponíveis

- **gemini-2.5-flash** (padrão) - Mais recente, rápido e eficiente
- **gemini-2.5-pro** - Mais poderoso para tarefas complexas
- **gemini-2.0-flash** - Versão anterior estável

Para trocar o modelo, edite o `application.yml`:
```yaml
gemini:
  api:
    model: gemini-2.5-pro  # ou outro modelo
```
#
# ⚠️ Comportamento do Gemini 2.5

O **Gemini 2.5 Flash** usa "pensamentos internos" (thoughts) para processar as solicitações, o que pode consumir tokens significativos:

- **thoughtsTokenCount**: Tokens usados para processamento interno
- **candidatesTokenCount**: Tokens da resposta real
- **totalTokenCount**: Total de tokens consumidos

**Configurações recomendadas:**
- `max-tokens: 4000` ou mais para respostas complexas
- `temperature: 0.7` para equilíbrio entre criatividade e precisão

**Possíveis finishReason:**
- `STOP`: Resposta completa
- `MAX_TOKENS`: Resposta cortada por limite de tokens
- `SAFETY`: Bloqueado por filtros de segurança#
# 🚀 Exemplos de Uso

### Análise de Sentimento
```bash
curl -X POST http://localhost:8080/api/ai/sentiment \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Adorei este produto! Recomendo muito.",
    "language": "pt"
  }'
```

### Resumo de Texto
```bash
curl -X POST http://localhost:8080/api/ai/summary \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Texto longo aqui...",
    "maxSentences": 2,
    "style": "bullet-points"
  }'
```

### Chat Contextual
```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Como posso melhorar este código?",
    "context": "Estou desenvolvendo uma API REST em Java"
  }'
```

### Classificação de Produto
```bash
curl -X POST http://localhost:8080/api/ai/product/classify \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "MacBook Pro",
    "description": "Laptop profissional com chip M3",
    "category": "Eletrônicos"
  }'
```

## 💡 Ideias para Expansão

### 📊 Análise de Dados
- **Análise de logs** - Detectar padrões e anomalias
- **Métricas de performance** - Interpretação automática
- **Relatórios inteligentes** - Geração de insights

### 🛠️ Ferramentas de Desenvolvimento
- **Code Review** - Análise automática de código
- **Geração de testes** - Criação de unit tests
- **Documentação** - Geração automática de docs
- **Refatoração** - Sugestões de melhorias

### 📝 Processamento de Texto
- **Tradução** - Múltiplos idiomas
- **Correção gramatical** - Revisão automática
- **Geração de conteúdo** - Posts, emails, artigos
- **Extração de entidades** - NER (Named Entity Recognition)

### 🎯 Casos de Uso Específicos
- **Chatbot de suporte** - Atendimento automatizado
- **Análise de feedback** - Processamento de avaliações
- **Classificação de documentos** - Organização automática
- **Detecção de spam** - Filtros inteligentes
- **Gestão de inventário** - Classificação automática de produtos
- **E-commerce** - Categorização inteligente de catálogos
- **Análise de mercado** - Segmentação de produtos por tangibilidade