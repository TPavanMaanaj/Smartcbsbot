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

**Tunguntla Pavan Maanaj**
Full-Stack Developer | AI Enthusiast

GitHub: https://github.com/tpavanmaanaj

---

---

## ☁️ Server Setup & Cloud Deployment (AWS Linux)

This section describes the step-by-step process to deploy SmartBOT on a cloud server using AWS EC2 with Amazon Linux.

---

## 🖥️ Step 1: Create EC2 Instance

1. Login to AWS Console  
2. Navigate to **EC2 → Launch Instance**  
3. Configure instance with:
   * **AMI**: Amazon Linux 2023  
   * **Instance Type**: t2.medium  
   * **Storage**: 20 GB  

4. Configure **Security Group**:
   * Allow `22` (SSH)
   * Allow `8080` (Backend)
   * Allow `5173` or `80` (Frontend)

---

## 🔐 Step 2: Connect to Server

```bash
ssh -i your-key.pem ec2-user@your-public-ip

---

## 📦 Step 3: Install Dependencies

```bash
sudo yum update -y

# Install Java & Maven
sudo yum install java-17-amazon-corretto -y
sudo yum install maven -y

# Install Node.js
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install -y nodejs

# Install Git
sudo yum install git -y

---

## 📥 Step 4: Clone Repository

Clone the SmartBOT project from GitHub and navigate into the project directory:

```bash
git clone https://github.com/tpavanmaanaj/SmartBOT.git
cd SmartBOT

---

## ⚙️ Step 5: Deploy Backend (Spring Boot)

Navigate to the backend directory and build the project using Maven:

```bash
cd smartbot-backend
mvn clean install

---

## 🎨 Step 6: Deploy Frontend (React + Vite)

Navigate to the frontend project directory and install dependencies:

```bash
cd project
npm install

---

## 🤖 Step 7: Setup Ollama (AI Engine)

Install Ollama to run local LLM models:

```bash
curl -fsSL https://ollama.com/install.sh | sh

---

## 🔄 Step 8: Run Services in Background

Install PM2 to manage and keep services alive:

```bash
npm install -g pm2

---

## 🌐 Step 9: Configure Nginx (Reverse Proxy)

Install Nginx:

```bash
sudo yum install nginx -y
sudo systemctl start nginx
sudo systemctl enable nginx

---

## 🔒 Step 10: Enable HTTPS (SSL)

Install Certbot for SSL:

```bash
sudo yum install certbot python3-certbot-nginx -y
