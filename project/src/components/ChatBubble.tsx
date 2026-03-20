import { MessageCircle, X } from 'lucide-react';

interface ChatBubbleProps {
  isOpen: boolean;
  onClick: () => void;
}

export const ChatBubble = ({ isOpen, onClick }: ChatBubbleProps) => {
  return (
    <button
      onClick={onClick}
      className="fixed bottom-4 right-6 w-12 h-12 bg-gradient-to-br from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white rounded-full shadow-lg flex items-center justify-center transition-all duration-200 hover:scale-110 z-50"
      aria-label={isOpen ? 'Close chat' : 'Open chat'}
    >
      {isOpen ? <X size={24} /> : <MessageCircle size={24} />}
    </button>
  );
};
