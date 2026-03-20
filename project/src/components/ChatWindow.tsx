import { useState, useRef, useEffect } from 'react';
import { Send, Loader2 } from 'lucide-react';
import { Message, EnhancedChatResponse } from '../types/chat';
import { ChatMessage } from './ChatMessage';
import { sendMessage } from '../services/chatApi';

interface ChatWindowProps {
  isOpen: boolean;
  sessionId?: string;
}

export const ChatWindow = ({ isOpen, sessionId }: ChatWindowProps) => {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      content: 'Hello! I\'m SmartBot Assistant from SmartCBS. How can I help you today?',
      role: 'assistant',
      timestamp: new Date(),
    },
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const sessionIdRef = useRef<string>('');

  // Initialize session ID
  useEffect(() => {
    if (sessionId) {
      // Use provided session ID
      sessionIdRef.current = sessionId;
    } else {
      // Generate a new session ID if none provided
      try {
        // @ts-ignore
        const uuid = (crypto && 'randomUUID' in crypto) ? crypto.randomUUID() : `${Date.now()}-${Math.random().toString(36).slice(2)}`;
        sessionIdRef.current = uuid;
      } catch {
        sessionIdRef.current = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
      }
    }
  }, [sessionId]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async () => {
    if (!inputValue.trim() || isLoading) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      content: inputValue,
      role: 'user',
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputValue('');
    setIsLoading(true);

    try {
      const response: EnhancedChatResponse = await sendMessage(inputValue, sessionIdRef.current);

      // Add source information to the response if available and sources exist
      let content = response.response;
      
      // Only show sources if there are sources and the response contains actual information
      // (not when it says "I don't have enough information")
      const shouldShowSources = response.sources && 
                               response.sources.length > 0 && 
                               !content.includes("I don't have enough information") &&
                               !content.includes("Please train the model with relevant sources");
      
      if (shouldShowSources) {
        // Deduplicate sources based on filename and upload date to ensure uniqueness
        const seen = new Set();
        const uniqueSources = response.sources.filter(source => {
          const key = `${source.filename}-${source.uploadDate || 'unknown'}`;
          if (seen.has(key)) {
            return false;
          }
          seen.add(key);
          return true;
        });
        
        // Only add sources section if we have unique sources
        if (uniqueSources.length > 0) {
          const sourcesSection = '\n\n**Sources Used:**\n' + 
            uniqueSources.map(source => {
              if (source.uploadDate && source.version) {
                // This is a file upload with date and version
                return `- ${source.filename} (Uploaded: ${source.uploadDate}, Version: ${source.version})`;
              } else {
                // This is a URL without date/version
                return `- ${source.filename}`;
              }
            }).join('\n');
          content += sourcesSection;
        }
      }

      const botMessage: Message = {
        id: (Date.now() + 1).toString(),
        content,
        role: 'assistant',
        timestamp: new Date(),
      };

      setMessages((prev) => [...prev, botMessage]);
    } catch (error) {
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        content: error instanceof Error ? error.message : 'An error occurred. Please try again.',
        role: 'assistant',
        timestamp: new Date(),
      };

      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };



  if (!isOpen) return null;

  return (
    <div className="fixed bottom-20 right-6 w-96 h-[550px] bg-white rounded-2xl shadow-2xl flex flex-col z-50 animate-in fade-in slide-in-from-bottom-5 duration-300">
      <div className="bg-gradient-to-r from-blue-600 to-blue-700 text-white px-6 py-4 rounded-t-2xl">
        <h2 className="text-lg font-semibold">SmartBot Assistant</h2>
        <p className="text-xs text-blue-100 mt-0.5">SmartCBS Banking Platform</p>
      </div>

      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-2">
        {messages.map((message) => (
          <ChatMessage key={message.id} message={message} />
        ))}
        {isLoading && (
          <div className="flex gap-3 mb-4">
            <div className="flex-shrink-0 w-8 h-8 rounded-full bg-gray-300 flex items-center justify-center">
              <Loader2 size={18} className="text-gray-700 animate-spin" />
            </div>
            <div className="flex items-center px-4 py-2 bg-gray-100 rounded-2xl rounded-bl-sm">
              <span className="text-sm text-gray-500">Thinking...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <div className="border-t border-gray-200 p-4">
        <div className="flex gap-2">
          <input
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Type your message..."
            disabled={isLoading}
            className="flex-1 px-4 py-2 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50 disabled:text-gray-500"
          />
          <button
            onClick={handleSend}
            disabled={!inputValue.trim() || isLoading}
            className="w-10 h-10 bg-blue-600 hover:bg-blue-700 text-white rounded-full flex items-center justify-center transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
            aria-label="Send message"
          >
            <Send size={18} />
          </button>
        </div>
        <p className="text-xs text-gray-400 mt-2 text-center">Powered by SmartCBS AI</p>
      </div>
    </div>
  );
};