import { Bot, User } from 'lucide-react';
import { Message } from '../types/chat';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

interface ChatMessageProps {
  message: Message;
}

export const ChatMessage = ({ message }: ChatMessageProps) => {
  const isUser = message.role === 'user';

  return (
    <div className={`flex gap-3 mb-3 ${isUser ? 'justify-end' : 'justify-start'}`}>
      {!isUser && (
        <div className="flex-shrink-0 w-7 h-7 rounded-full bg-blue-600 flex items-center justify-center">
          <Bot className="w-4 h-4 text-white" />
        </div>
      )}

      <div
        className={`max-w-[85%] px-3 py-2 rounded-xl text-sm leading-relaxed ${
          isUser
            ? 'bg-blue-600 text-white rounded-br-sm'
            : 'bg-gray-100 text-gray-800 rounded-bl-sm'
        }`}
      >
        <ReactMarkdown
          remarkPlugins={[remarkGfm]}
          components={{
            p: ({ ...props }) => (
              <p className="mb-1 last:mb-0" {...props} />
            ),

            strong: ({ ...props }) => (
              <strong className="font-semibold text-gray-900" {...props} />
            ),

            em: ({ ...props }) => (
              <em className="italic text-gray-700" {...props} />
            ),

            ul: ({ ...props }) => (
              <ul className="list-disc pl-5 my-1 space-y-1" {...props} />
            ),

            ol: ({ ...props }) => (
              <ol className="list-decimal pl-5 my-1 space-y-1" {...props} />
            ),

            li: ({ ...props }) => (
              <li className="text-sm" {...props} />
            ),

            a: ({ ...props }) => (
              <a 
                className="text-blue-600 hover:text-blue-800 underline" 
                target="_blank" 
                rel="noopener noreferrer"
                {...props} 
              />
            ),

            /* 🔑 TABLE HANDLING */
            table: ({ ...props }) => (
              <div className="overflow-x-auto my-2">
                <table
                  className="w-full table-fixed border-collapse border border-gray-300 text-xs"
                  {...props}
                />
              </div>
            ),

            th: ({ ...props }) => (
              <th
                className="border border-gray-300 bg-gray-200 px-2 py-1 font-medium text-left break-words"
                {...props}
              />
            ),

            td: ({ ...props }) => (
              <td
                className="border border-gray-300 px-2 py-1 break-words align-top"
                {...props}
              />
            ),

            blockquote: ({ ...props }) => (
              <blockquote className="border-l-4 border-blue-400 pl-3 italic text-gray-600 my-2" {...props} />
            ),
          }}
        >
          {message.content}
        </ReactMarkdown>

        <div className={`text-[10px] mt-1 ${isUser ? 'text-blue-200' : 'text-gray-500'}`}>
          {message.timestamp.toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit',
          })}
        </div>
        
</div>

      {isUser && (
        <div className="flex-shrink-0 w-7 h-7 rounded-full bg-gray-200 flex items-center justify-center">
          <User className="w-4 h-4 text-blue-600" />
        </div>
      )}
    </div>
  );
};
