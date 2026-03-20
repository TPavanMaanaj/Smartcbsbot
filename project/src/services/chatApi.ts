import { ChatRequest, EnhancedChatResponse } from '../types/chat';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const sendMessage = async (message: string, sessionId?: string): Promise<EnhancedChatResponse> => {
  const request: ChatRequest = { message, sessionId };

  try {
    const response = await fetch(`${API_URL}/api/chat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data: EnhancedChatResponse = await response.json();
    return data;
  } catch (error) {
    console.error('Error sending message:', error);
    throw Error('Failed to connect to SmartBot backend. Please ensure the server is running.');
  }
};