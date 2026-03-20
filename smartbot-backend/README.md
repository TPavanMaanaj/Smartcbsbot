# SmartBot Backend - AI-Powered Banking Assistant

Production-ready Spring Boot backend with Ollama integration, RAG capabilities, and web scraping for SmartBot banking assistant (SmartCBS Platform).

## Tech Stack
- Java 17
- Spring Boot 3.2.1
- Maven
- LangChain4j 0.36.2
- Ollama (Local LLM)
- In-Memory Vector Store
- Jsoup (Web Scraping)
- Apache Tika (PDF Parsing)

## Project Structure
```
smartbot-backend/
├── src/
│   └── main/
│       ├── java/com/smartcbs/smartbot/
│       │   ├── SmartBotApplication.java          # Main application entry point
│       │   ├── config/
│       │   │   ├── CorsConfig.java               # CORS configuration
│       │   │   ├── OllamaConfig.java             # Ollama model configuration
│       │   │   └── VectorStoreConfig.java        # Vector store setup
│       │   ├── controller/
│       │   │   ├── ChatController.java           # Chat endpoints
│       │   │   └── TrainController.java          # Training & status endpoints
│       │   ├── dto/
│       │   │   ├── ChatRequest.java              # Chat request DTO
│       │   │   ├── ChatResponse.java             # Chat response DTO
│       │   │   ├── TrainRequest.java             # Training request DTO
│       │   │   ├── TrainResponse.java            # Training response DTO
│       │   │   └── StatusResponse.java           # System status DTO
│       │   └── service/
│       │       ├── ChatService.java              # RAG-powered chat logic
│       │       ├── OllamaService.java            # Ollama integration
│       │       ├── VectorStoreService.java       # Embeddings management
│       │       └── DataIngestionService.java     # Web scraping & ingestion
│       └── resources/
│           └── application.properties            # Configuration
├── pom.xml                                       # Maven dependencies
└── README.md
```

## Prerequisites
- Java 17 or higher
- Maven 3.6+ (or use included Maven wrapper)
- **Ollama installed and running locally**

## Ollama Setup

### 1. Install Ollama
Download and install Ollama from: https://ollama.ai/download

### 2. Pull a Model
```bash
ollama pull llama3.2
```

Or use another model:
```bash
ollama pull mistral
ollama pull llama2
```

### 3. Verify Ollama is Running
```bash
ollama list
curl http://localhost:11434/api/tags
```

Ollama runs on `http://localhost:11434` by default.

### 4. Update Configuration (Optional)
If using a different model, edit `src/main/resources/application.properties`:
```properties
ollama.model-name=mistral
```

## Installation & Setup

### 1. Navigate to Backend Directory
```bash
cd smartbot-backend
```

### 2. Install Dependencies
```bash
mvn clean install
```

This will download:
- LangChain4j libraries
- Ollama integration
- Web scraping tools (Jsoup, Tika)
- Embedding models

### 3. Run the Application
```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

### 4. Verify Server is Running
```bash
curl http://localhost:8080/api/health
```

Expected response: `SmartBot Backend is running`

### 5. Check System Status
```bash
curl http://localhost:8080/api/status
```

This will show:
- Ollama connection status
- Model name
- Number of embeddings
- Last training date

## API Endpoints

### POST /api/chat
Send a message to SmartBot (uses RAG with vector store context)

**Request:**
```json
{
  "message": "What banking services does SmartCBS offer?"
}
```

**Response:**
```json
{
  "response": "SmartCBS offers comprehensive banking services including..."
}
```

**Example with curl:**
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What is SmartCBS?"}'
```

### POST /api/train
Train the model by ingesting SmartCBS website data

**Request (optional):**
```json
{
  "urls": ["https://example.com/page1", "https://example.com/page2"],
  "includeDefaultUrls": true
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Training completed successfully",
  "documentsProcessed": 15,
  "chunksEmbedded": 234,
  "durationMs": 45320
}
```

**Example with curl:**
```bash
curl -X POST http://localhost:8080/api/train \
  -H "Content-Type: application/json" \
  -d '{"includeDefaultUrls": true}'
```

### GET /api/status
Check Ollama connection and system status

**Response:**
```json
{
  "ollamaStatus": "connected",
  "modelName": "llama3.2",
  "embeddingsCount": 234,
  "isReady": true,
  "lastTrainingDate": "2025-10-24 10:30:00"
}
```

**Example with curl:**
```bash
curl http://localhost:8080/api/status
```

### GET /api/health
Basic health check endpoint

**Response:**
```
SmartBot Backend is running
```

## CORS Configuration
The backend is configured to accept requests from any origin. CORS settings in `CorsConfig.java`:
- Allowed origins: All (*)
- Allowed methods: GET, POST, PUT, DELETE, OPTIONS
- Allowed headers: Origin, Content-Type, Accept, Authorization, X-Requested-With

## Features

### Current Implementation
- **Ollama Integration**: Local LLM (Llama 3.2, Mistral, etc.)
- **RAG (Retrieval-Augmented Generation)**: Context-aware responses using vector store
- **Web Scraping**: Automated content ingestion from SmartCBS and other websites
- **PDF Support**: Extract and embed content from PDF documents
- **Vector Embeddings**: Local all-MiniLM-L6-v2 embedding model
- **In-Memory Vector Store**: Fast semantic search
- **Smart Text Chunking**: Recursive document splitting with overlap
- **REST API**: Clean endpoints for chat, training, and status
- **CORS Enabled**: Frontend integration ready
- **Comprehensive Logging**: Track all operations

### How It Works

1. **Data Ingestion** (`/api/train`):
   - Crawls SmartCBS website and configured URLs
   - Extracts clean text from HTML and PDFs
   - Splits documents into semantic chunks
   - Generates embeddings using local model
   - Stores in vector database

2. **Chat with RAG** (`/api/chat`):
   - Receives user query
   - Generates query embedding
   - Searches vector store for relevant context
   - Sends query + context to Ollama
   - Returns AI-generated response

3. **Status Monitoring** (`/api/status`):
   - Checks Ollama connectivity
   - Reports embedding count
   - Shows last training date
   - Indicates system readiness

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Ollama Configuration
ollama.base-url=http://localhost:11434
ollama.model-name=llama3.2
ollama.timeout=120

# Data Ingestion Configuration
ingestion.smartcbs.urls=https://www.smartcbs.com,https://www.smartcbs.com/about
ingestion.max-pages=50
ingestion.chunk-size=500
ingestion.chunk-overlap=50
```

## Development

### Build JAR
```bash
mvn clean package
```

The JAR file will be created in `target/smartbot-backend-1.0.0.jar`

### Run JAR
```bash
java -jar target/smartbot-backend-1.0.0.jar
```

### Run Tests
```bash
mvn test
```

## Troubleshooting

### Ollama Not Connected
- Ensure Ollama is installed and running
- Check if the model is pulled: `ollama list`
- Verify Ollama URL: `curl http://localhost:11434/api/tags`

### Training Fails
- Check network connectivity
- Verify URLs in configuration are accessible
- Review logs for specific errors

### Out of Memory
- Reduce `ingestion.max-pages` in configuration
- Increase JVM heap size: `MAVEN_OPTS="-Xmx2g" mvn spring-boot:run`

## Future Enhancements
- Persistent vector store (ChromaDB, Qdrant)
- Multi-model support
- User authentication & personalization
- Chat history persistence in database
- Advanced document processing (Excel, Word)
- Multi-language support
- Fine-tuning capabilities
- Scheduled automatic retraining

## License
Proprietary - SmartCBS Banking Platform
