# 🤖 SmartBOT — AI Knowledge Assistant

SmartBOT is a full-stack AI chatbot platform designed to help organizations interact with their internal knowledge base through intelligent conversations.
It combines a **Spring Boot backend**, **React + Vite frontend**, and **local LLM integration (Ollama)** to deliver fast, context-aware responses.

---

## 🚀 Overview

SmartBOT allows users to:

* 💬 Chat with an AI assistant trained on internal documents
* 📚 Upload and manage knowledge-base files
* 🧠 Generate vector embeddings for contextual answers
* ⚡ Perform real-time conversations using a modern UI
* 🔄 Track training and system status

This project follows a clean **frontend + backend architecture** suitable for production-scale AI applications.

---

## 🧩 Tech Stack

### 🎨 Frontend

* React + TypeScript
* Vite
* Tailwind CSS
* REST API Integration

### ⚙️ Backend

* Java Spring Boot
* REST Controllers
* Vector Store Service
* Conversation & Training APIs

### 🤖 AI Layer

* Ollama Local Models
* Embedding-based Retrieval
* Knowledge-base Training Pipeline

---

## 📂 Project Structure

```
SmartBOT/
│
├── project/               # React Frontend (UI)
│
└── smartbot-backend/      # Spring Boot Backend
```

---

## ✨ Features

* Intelligent conversational AI
* Document-based training
* Vector search for contextual responses
* Knowledge Base File Management
* Conversation tracking
* Training progress monitoring
* Modern responsive UI

---

## ⚡ Getting Started

### ✅ Prerequisites

* Node.js (v18+)
* Java 17+
* Maven
* Ollama installed locally

---

## 🔧 Backend Setup (Spring Boot)

```
cd smartbot-backend
mvn clean install
mvn spring-boot:run
```

Backend runs by default on:

```
http://localhost:8080
```

---

## 🎨 Frontend Setup (React + Vite)

```
cd project
npm install
npm run dev
```

Frontend runs on:

```
http://localhost:5173
```

---

## 🤖 Ollama Setup

Make sure Ollama is installed and running:

```
ollama run llama3
```

You can configure model settings inside:

```
smartbot-backend/config/
```

---

## 📡 API Modules

| Module                  | Description                   |
| ----------------------- | ----------------------------- |
| ChatController          | Handles AI conversations      |
| TrainController         | Training & embedding creation |
| KnowledgeBaseController | File management               |
| ConversationController  | Session tracking              |

---

## 🧠 How SmartBOT Works

1. Upload documents to Knowledge Base
2. Train embeddings using backend services
3. User sends chat query
4. Vector search retrieves relevant context
5. LLM generates contextual response

---

## 📸 UI Highlights

* Chat Window Interface
* Training Panel
* Status Bar Monitoring
* Knowledge Base File Cards

---

## 🔒 .gitignore Best Practices

The project excludes build artifacts such as:

```
target/
node_modules/
*.jar
dist/
build/
```

---

## 👨‍💻 Author

**Vaibhav Kachhawal**
Full-Stack Developer | AI Enthusiast

GitHub: https://github.com/vaibhav-kachhawal

---

## ⭐ Future Enhancements

* Docker deployment
* Role-based access control
* Multi-model support
* Cloud vector database integration

---

## 📜 License

This project is for educational and research purposes.

---

> ✨ *SmartBOT — Turning Knowledge into Conversations.*
