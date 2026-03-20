import { useEffect, useState } from 'react';
import { CheckCircle, XCircle, Database, Calendar } from 'lucide-react';
import { StatusResponse } from '../types/training';
import { getSystemStatus } from '../services/trainingApi';

export const StatusBar = () => {
  const [status, setStatus] = useState<StatusResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchStatus = async () => {
    try {
      const data = await getSystemStatus();
      setStatus(data);
    } catch (error) {
      console.error('Failed to fetch status:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStatus();
    const interval = setInterval(fetchStatus, 30000);
    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return null;
  }

  const isConnected = status?.ollamaStatus === 'connected';

  return (
    <div className="fixed top-4 right-4 bg-white rounded-lg shadow-md p-3 min-w-[280px] border border-gray-200 z-40">
      <div className="flex items-center gap-2 mb-2">
        {isConnected ? (
          <CheckCircle className="text-green-500" size={18} />
        ) : (
          <XCircle className="text-red-500" size={18} />
        )}
        <span className="text-sm font-semibold text-gray-700">
          Ollama {isConnected ? 'Connected' : 'Disconnected'}
        </span>
      </div>

      {status && (
        <>
          <div className="text-xs text-gray-600 space-y-1">
            <div className="flex items-center gap-2">
              <span className="font-medium">Model:</span>
              <span className="text-gray-800">{status.modelName}</span>
            </div>

            <div className="flex items-center gap-2">
              <Database size={12} />
              <span className="font-medium">Embeddings:</span>
              <span className="text-gray-800">{status.embeddingsCount}</span>
            </div>

            <div className="flex items-center gap-2">
              <Calendar size={12} />
              <span className="font-medium">Last Training:</span>
              <span className="text-gray-800 text-[10px]">{status.lastTrainingDate}</span>
            </div>
          </div>

          
        </>
      )}
    </div>
  );
};
