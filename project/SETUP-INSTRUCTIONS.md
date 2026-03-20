# SmartBot - Complete Setup Instructions

Production-ready SmartBot Banking Assistant for SmartCBS Platform

## Project Structure

```
project/
├── smartbot-backend/              # Spring Boot REST API
│   ├── src/
│   │   └── main/
│   │       ├── java/com/smartcbs/smartbot/
│   │       │   ├── SmartBotApplication.java
│   │       │   ├── config/
│   │       │   │   └── CorsConfig.java
│   │       │   ├── controller/
│   │       │   │   └── ChatController.java
│   │       │   ├── dto/
│   │       │   │   ├── ChatRequest.java
│   │       │   │   └── ChatResponse.java
│   │       │   └── service/
│   │       │       └── ChatService.java
│   │       └── resources/
│   │           └── application.properties
│   ├── pom.xml
│   └── README.md
│
├── src/                           # React Frontend (Vite)
│   ├── components/
│   │   ├── ChatBubble.tsx         # Floating chat button
│   │   ├── ChatMessage.tsx        # Message bubbles
│   │   └── ChatWindow.tsx         # Chat panel UI
│   ├── services/
│   │   └── chatApi.ts             # API integration
│   ├── types/
│   │   └── chat.ts                # TypeScript types
│   ├── App.tsx                    # Main application
│   └── main.tsx
│
├── .env                           # Environment variables
├── package.json
└── SETUP-INSTRUCTIONS.md          # This file
```

---

## Prerequisites

### Frontend
- Node.js 18+ and npm
- Modern web browser

### Backend
- Java 17 or higher
- Maven 3.6+ (or use Maven wrapper)

---

## Setup & Installation

### 1. Backend Setup (Spring Boot)

#### Step 1: Navigate to backend directory
```bash
cd smartbot-backend
```

#### Step 2: Install dependencies
```bash
mvn clean install
```

#### Step 3: Run the backend server
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

#### Step 4: Verify backend is running
Open a new terminal and test:
```bash
curl http://localhost:8080/api/health
```

Expected response: `SmartBot Backend is running`

#### Test the chat endpoint:
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello"}'
```

Expected response:
```json
{
  "response": "Hello! Welcome to SmartCBS Banking. How can I assist you today?"
}
```

---

### 2. Frontend Setup (React + Vite)

#### Step 1: Navigate to project root
```bash
cd ..
# You should now be in the root directory with package.json
```

#### Step 2: Install dependencies
```bash
npm install
```

#### Step 3: Verify environment variables
Check `.env` file contains:
```
VITE_API_URL=http://localhost:8080
```

#### Step 4: Run the frontend dev server
```bash
npm run dev
```

The frontend will start on `http://localhost:5173` (or another port if 5173 is busy)

---

## Usage

1. **Open your browser** and navigate to `http://localhost:5173`

2. **You'll see the SmartBot landing page** with information about the assistant

3. **Click the blue chat bubble** in the bottom-right corner to open the chat widget

4. **Start chatting!** Type a message and press Enter or click Send

5. **Try these sample messages:**
   - "Hello"
   - "I need help with my account"
   - "Tell me about loans"
   - "How do I make a transfer?"
   - "Help"

---

## API Integration

The frontend communicates with the Spring Boot backend via REST API:

**Endpoint:** `POST http://localhost:8080/api/chat`

**Request:**
```json
{
  "message": "user's message here"
}
```

**Response:**
```json
{
  "response": "SmartBot's response here"
}
```

---

## Features

### Frontend (React)
- Floating chat widget (Intercom-style)
- Smooth animations and transitions
- Message history with scroll
- Real-time typing indicators
- Error handling and loading states
- Responsive design with TailwindCSS
- TypeScript for type safety

### Backend (Spring Boot)
- RESTful API endpoints
- CORS configuration for cross-origin requests
- Input validation
- Rule-based response system (ready for AI/LLM integration)
- Modular architecture
- Health check endpoint

---

## Architecture

### Frontend Stack
- **React 18** - UI framework
- **Vite** - Build tool and dev server
- **TypeScript** - Type safety
- **TailwindCSS** - Styling
- **Lucide React** - Icons

### Backend Stack
- **Java 17** - Programming language
- **Spring Boot 3.2.1** - Framework
- **Maven** - Dependency management
- **Lombok** - Reduce boilerplate code

### Communication
- REST API with JSON payloads
- CORS enabled for cross-origin requests

---

## Development

### Build Frontend for Production
```bash
npm run build
```

### Build Backend JAR
```bash
cd smartbot-backend
mvn clean package
java -jar target/smartbot-backend-1.0.0.jar
```

### Type Check Frontend
```bash
npm run typecheck
```

### Lint Frontend
```bash
npm run lint
```

---

## Future Enhancements

### Planned Features
1. **AI/LLM Integration**
   - OpenAI GPT integration
   - Custom prompt engineering
   - Context-aware responses

2. **Vector Database**
   - Knowledge base storage
   - Semantic search
   - Document retrieval

3. **Advanced Features**
   - User authentication
   - Chat history persistence
   - Multi-language support
   - Voice input/output
   - File attachments

4. **Analytics**
   - User interaction tracking
   - Performance metrics
   - Conversation analytics

---

## Troubleshooting

### Backend not starting?
- Verify Java 17+ is installed: `java -version`
- Check if port 8080 is available
- Review logs in the terminal

### Frontend not connecting to backend?
- Verify backend is running on port 8080
- Check `.env` file has correct `VITE_API_URL`
- Open browser console for error messages
- Verify CORS settings in backend

### CORS errors?
- Ensure `CorsConfig.java` is properly configured
- Restart the backend server after changes

---

## Production Deployment

### Frontend
1. Build: `npm run build`
2. Deploy `dist/` folder to hosting (Vercel, Netlify, etc.)
3. Update `VITE_API_URL` to production backend URL

### Backend
1. Build JAR: `mvn clean package`
2. Deploy to cloud (AWS, Azure, Heroku, etc.)
3. Update frontend environment variable with production URL

---

## License
Proprietary - SmartCBS Banking Platform

## Support
For questions or issues, contact the SmartCBS development team.
