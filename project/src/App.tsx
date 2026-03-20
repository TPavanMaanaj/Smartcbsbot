import { useState } from 'react';
import { ChatBubble } from './components/ChatBubble';
import { ChatWindow } from './components/ChatWindow';
import { StatusBar } from './components/StatusBar';
import { TrainingPanel } from './components/TrainingPanel';
import { KnowledgeBaseFilesCard } from './components/KnowlegdeBaseFilesCard';

function App() {
  const [isChatOpen, setIsChatOpen] = useState(false);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <StatusBar />

      <div className="container mx-auto px-4 py-16">
        <div className="max-w-4xl mx-auto text-center">
          <div className="mb-8">
            <div className="inline-block px-4 py-2 bg-blue-100 text-blue-700 rounded-full text-sm font-medium mb-4">
              SmartCBS Banking Platform
            </div>
            <h1 className="text-5xl font-bold text-gray-900 mb-4">
              Welcome to SmartBot
            </h1>
            <p className="text-xl text-gray-600 mb-8">
              Your intelligent banking assistant powered by AI
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-6 mt-16">
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4 mx-auto">
                <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">Instant Responses</h3>
              <p className="text-gray-600 text-sm">Get immediate answers to your banking queries 24/7</p>
            </div>

            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4 mx-auto">
                <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">Secure & Private</h3>
              <p className="text-gray-600 text-sm">Bank-grade security for all your conversations</p>
            </div>

            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4 mx-auto">
                <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                </svg>
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">Smart Assistance</h3>
              <p className="text-gray-600 text-sm">AI-powered help for complex banking operations</p>
            </div>
          </div>

          <div className="mt-16 grid md:grid-cols-2 gap-6">
            <KnowledgeBaseFilesCard />

            <TrainingPanel />
          </div>
        </div>
      </div>

      <ChatBubble isOpen={isChatOpen} onClick={() => setIsChatOpen(!isChatOpen)} />
      <ChatWindow isOpen={isChatOpen} />
    </div>
  );
}

export default App;
