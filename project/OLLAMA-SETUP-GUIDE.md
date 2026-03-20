# SmartBot with Ollama - Complete Setup Guide

This guide will walk you through setting up SmartBot with local Ollama integration for AI-powered banking assistance.

## Prerequisites Checklist

- [ ] Java 17 or higher installed
- [ ] Maven 3.6+ installed
- [ ] Node.js 18+ and npm installed
- [ ] Ollama installed

## Step 1: Install and Setup Ollama

### For macOS:
```bash
brew install ollama
```

### For Linux:
```bash
curl -fsSL https://ollama.ai/install.sh | sh
```

### For Windows:
Download from: https://ollama.ai/download

### Start Ollama Service:
```bash
ollama serve
```

This will start Ollama on `http://localhost:11434`

### Pull a Model:
```bash
# Recommended: Llama 3.2 (smaller, faster)
ollama pull llama3.2

# Alternative: Mistral (good balance)
ollama pull mistral

# Alternative: Llama 2 (larger, more capable)
ollama pull llama2
```

### Verify Installation:
```bash
ollama list
curl http://localhost:11434/api/tags
```

You should see your pulled models listed.

---

## Step 2: Setup Backend (Spring Boot)

### Navigate to backend directory:
```bash
cd smartbot-backend
```

### Install dependencies:
```bash
mvn clean install
```

This will download:
- LangChain4j 0.36.2
- Ollama integration libraries
- Web scraping tools (Jsoup)
- PDF parsing (Apache Tika)
- Embedding models (all-MiniLM-L6-v2)

### Configure Ollama Model (Optional):
Edit `src/main/resources/application.properties` if using a different model:

```properties
ollama.model-name=mistral  # Change to your preferred model
```

### Run the backend:
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Verify backend is running:
```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/status
```

The status endpoint should show:
- Ollama status: "connected"
- Model name: "llama3.2" (or your chosen model)
- Ready: true

---

## Step 3: Setup Frontend (React + Vite)

### Navigate to project root:
```bash
cd ..
```

### Install dependencies:
```bash
npm install
```

### Verify environment variables:
Check `.env` file contains:
```
VITE_API_URL=http://localhost:8080
```

### Run the frontend:
```bash
npm run dev
```

The frontend will start on `http://localhost:5173`

---

## Step 4: Train the Model with SmartCBS Data

### Option A: Using the Web UI

1. Open `http://localhost:5173` in your browser
2. Look for the "Model Training" panel on the homepage
3. Click the "Train Model with Data" button
4. Wait for training to complete (this may take 1-5 minutes)

### Option B: Using curl

```bash
curl -X POST http://localhost:8080/api/train \
  -H "Content-Type: application/json" \
  -d '{"includeDefaultUrls": true}'
```

### What happens during training:
1. Scrapes SmartCBS website content
2. Extracts clean text from HTML pages
3. Processes PDFs if found
4. Splits content into semantic chunks
5. Generates embeddings using local model
6. Stores in vector database

You should see output like:
```json
{
  "status": "success",
  "message": "Training completed successfully",
  "documentsProcessed": 15,
  "chunksEmbedded": 234,
  "durationMs": 45320
}
```

---

## Step 5: Start Chatting!

### Using the Web UI:

1. Click the blue chat bubble in the bottom-right corner
2. Type a message like:
   - "What is SmartCBS?"
   - "Tell me about your banking services"
   - "How do I open an account?"
3. SmartBot will use RAG (Retrieval-Augmented Generation) to answer using the trained knowledge base

### Using curl:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What banking services does SmartCBS offer?"}'
```

---

## Architecture Overview

```
┌─────────────────┐
│  React Frontend │
│  (Port 5173)    │
└────────┬────────┘
         │ HTTP REST
         ▼
┌─────────────────┐
│  Spring Boot    │
│  Backend        │
│  (Port 8080)    │
└────────┬────────┘
         │
         ├──────────────┐
         │              │
         ▼              ▼
┌─────────────┐  ┌─────────────┐
│   Ollama    │  │   Vector    │
│   (LLM)     │  │   Store     │
│  Port 11434 │  │ (Embeddings)│
└─────────────┘  └─────────────┘
         │              │
         └──────┬───────┘
                ▼
         RAG Response
```

### How RAG Works:

1. **User asks a question** → Frontend sends to `/api/chat`
2. **Query embedding** → Backend generates embedding for the question
3. **Semantic search** → Finds relevant chunks from vector store
4. **Context enrichment** → Combines question + relevant context
5. **LLM generation** → Ollama generates answer using context
6. **Response** → User gets contextually accurate answer

---

## Monitoring and Status

### Check System Status:
```bash
curl http://localhost:8080/api/status
```

### View Status in UI:
Look at the top-right corner of the web UI for:
- Ollama connection status
- Model name
- Number of embeddings
- Last training date

---

## Troubleshooting

### Problem: Ollama not connected

**Check:**
```bash
ps aux | grep ollama
curl http://localhost:11434/api/tags
```

**Solution:**
```bash
ollama serve
```

### Problem: Model not found

**Check:**
```bash
ollama list
```

**Solution:**
```bash
ollama pull llama3.2
```

### Problem: Training takes too long

**Reduce pages in configuration:**

Edit `smartbot-backend/src/main/resources/application.properties`:
```properties
ingestion.max-pages=20  # Reduce from 50
```

### Problem: Out of memory during training

**Increase JVM heap:**
```bash
MAVEN_OPTS="-Xmx2g" mvn spring-boot:run
```

### Problem: Backend errors during chat

**Check logs in terminal running backend**

Common issues:
- Ollama not running
- No embeddings in vector store (train first)
- Network issues

---

## Advanced Configuration

### Use Different URLs for Training:

```bash
curl -X POST http://localhost:8080/api/train \
  -H "Content-Type: application/json" \
  -d '{
    "urls": [
      "https://yourcompany.com",
      "https://yourcompany.com/docs",
      "https://yourcompany.com/support"
    ],
    "includeDefaultUrls": false
  }'
```

### Adjust Chunk Size for Better Context:

Edit `application.properties`:
```properties
ingestion.chunk-size=800       # Larger chunks = more context
ingestion.chunk-overlap=100    # More overlap = better continuity
```

### Use Different Ollama Model:

```properties
ollama.model-name=mistral
ollama.timeout=180  # 3 minutes for slower models
```

---

## Production Deployment

### Backend:
```bash
cd smartbot-backend
mvn clean package
java -jar target/smartbot-backend-1.0.0.jar
```

### Frontend:
```bash
npm run build
# Deploy dist/ folder to your hosting service
```

### Environment Variables for Production:
Update `.env`:
```
VITE_API_URL=https://your-backend-domain.com
```

---

## Next Steps

1. **Add More Data Sources**: Configure additional URLs in `application.properties`
2. **Improve Responses**: Experiment with different Ollama models
3. **Persistent Storage**: Upgrade to ChromaDB or Qdrant for production
4. **Authentication**: Add user management and personalized responses
5. **Analytics**: Track popular queries and response quality

---

## Support

For issues or questions:
1. Check logs in backend terminal
2. Verify Ollama status: `curl http://localhost:11434/api/tags`
3. Review backend README: `smartbot-backend/README.md`
4. Check system status: `http://localhost:8080/api/status`

## License

Proprietary - SmartCBS Banking Platform
