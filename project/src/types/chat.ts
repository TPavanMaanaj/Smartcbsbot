export interface Message {
  id: string;
  content: string;
  role: 'user' | 'assistant';
  timestamp: Date;
}

export interface ChatRequest {
  message: string;
  sessionId?: string;
}

export interface ChatResponse {
  response: string;
}

export interface SourceInfo {
  id: string;
  filename: string;
  url: string;
  sourceType: string;
  relevanceScore: number;
  snippet: string;
  uploadDate?: string;
  version?: string;
}

export interface EnhancedChatResponse {
  response: string;
  followUpQuestions: string[];
  sources: SourceInfo[];
}