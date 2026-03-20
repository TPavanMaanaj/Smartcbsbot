import { useState } from 'react';
import { Database, Loader2, CheckCircle, XCircle, Globe } from 'lucide-react';
import { uploadTraining, addUrlToKnowledgeBase } from '../services/trainingApi';
import { TrainResponse } from '../types/training';

export const TrainingPanel = () => {
  const [isTraining, setIsTraining] = useState(false);
  const [lastResult, setLastResult] = useState<TrainResponse | null>(null);
  const [showResult, setShowResult] = useState(false);
  const [uploadedFiles, setUploadedFiles] = useState<File[]>([]);
  const [clearExisting, setClearExisting] = useState(false);
  // New state for URL management
  const [urlInput, setUrlInput] = useState('');
  const [urlTitle, setUrlTitle] = useState('');
  const [isAddingUrl, setIsAddingUrl] = useState(false);


  const handleUploadTrain = async () => {
    if (uploadedFiles.length === 0) return;
    setIsTraining(true);
    setShowResult(false);

    // try {
    //   const result = await trainModel({
    //     includeDefaultUrls: true,
    //   });
    //   setLastResult(result);
    //   setShowResult(true);

    //   setTimeout(() => {
    //     setShowResult(false);
    //   }, 10000);
    // } catch (error) {
    //   setLastResult({
    //     status: 'error',
    //     message: error instanceof Error ? error.message : 'Training failed',
    //     documentsProcessed: 0,
    //     chunksEmbedded: 0,
    //     durationMs: 0,
    //   });
    //   setShowResult(true);
    // } finally {
    //   setIsTraining(false);
    // }
    try {
      const result = await uploadTraining(uploadedFiles, clearExisting);
      setLastResult(result);
      setShowResult(true);
      setTimeout(() => setShowResult(false), 10000);
    } catch (error) {
      setLastResult({
        status: 'error',
        message: error instanceof Error ? error.message : 'Training failed',
        documentsProcessed: 0,
        chunksEmbedded: 0,
        durationMs: 0,
      });
      setShowResult(true);
    } finally {
      setIsTraining(false);
    }
  };

  // New function to handle URL addition
  const handleAddUrl = async () => {
    if (!urlInput.trim()) return;
    
    setIsAddingUrl(true);
    setShowResult(false);

    try {
      const result = await addUrlToKnowledgeBase(urlInput.trim(), urlTitle.trim() || urlInput.trim());

      setLastResult({
        status: 'success',
        message: `Successfully added URL: ${result.url}`,
        documentsProcessed: result.documentsProcessed,
        chunksEmbedded: result.chunksEmbedded,
        durationMs: result.durationMs,
      });
      setShowResult(true);
      setUrlInput('');
      setUrlTitle('');
      setTimeout(() => setShowResult(false), 10000);
    } catch (error) {
      setLastResult({
        status: 'error',
        message: error instanceof Error ? error.message : 'Failed to add URL',
        documentsProcessed: 0,
        chunksEmbedded: 0,
        durationMs: 0,
      });
      setShowResult(true);
    } finally {
      setIsAddingUrl(false);
    }
  };

  return (
    <div className="bg-white border border-gray-200 rounded-xl shadow-sm p-6">
      <div className="flex items-center gap-3 mb-4">
        <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
          <Database className="text-blue-600" size={20} />
        </div>
        <div>
          <h3 className="text-lg font-semibold text-gray-900">Model Training</h3>
          <p className="text-sm text-gray-600">Ingest SmartCBS data into the knowledge base</p>
        </div>
      </div>

     
      <div className="mt-4 space-y-2">
        <label className="block text-sm font-medium text-gray-700">Upload files to train</label>
        <input
          type="file"
          multiple
          onChange={(e) => setUploadedFiles(Array.from(e.target.files || []))}
          className="block w-full text-sm text-gray-600 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
        />
        <label className="inline-flex items-center gap-2 text-sm text-gray-700">
          <input
            type="checkbox"
            checked={clearExisting}
            onChange={(e) => setClearExisting(e.target.checked)}
            className="rounded border-gray-300"
          />
          Clear existing embeddings before training
        </label>
        <button
          onClick={handleUploadTrain}
          disabled={isTraining || uploadedFiles.length === 0}
          className="w-full px-4 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-medium transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          {isTraining ? (
            <>
              <Loader2 size={18} className="animate-spin" />
              Training with uploaded files...
            </>
          ) : (
            <>
              <Database size={18} />
              Train Model with Uploaded Files
            </>
          )}
        </button>
        <span className="text-sm text-gray-600">
          Maximum file size: 50MB
        </span>
      </div>

      {/* URL Input Section */}
      <div className="mt-6 pt-4 border-t border-gray-200">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
            <Globe className="text-green-600" size={20} />
          </div>
          <div>
            <h3 className="text-lg font-semibold text-gray-900">URL Training</h3>
            <p className="text-sm text-gray-600">Add web pages to the knowledge base</p>
          </div>
        </div>
        
        <div className="space-y-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">URL to add</label>
            <input
              type="url"
              value={urlInput}
              onChange={(e) => setUrlInput(e.target.value)}
              placeholder="https://example.com"
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Title (optional)</label>
            <input
              type="text"
              value={urlTitle}
              onChange={(e) => setUrlTitle(e.target.value)}
              placeholder="Optional title for this URL"
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>
          
          <button
            onClick={handleAddUrl}
            disabled={isAddingUrl || !urlInput.trim()}
            className="w-full px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg font-medium transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {isAddingUrl ? (
              <>
                <Loader2 size={16} className="animate-spin" />
                Adding URL...
              </>
            ) : (
              <>
                <Globe size={16} />
                Add URL to Knowledge Base
              </>
            )}
          </button>
        </div>
      </div>

      {showResult && lastResult && (
        <div
          className={`mt-4 p-4 rounded-lg border ${
            lastResult.status === 'success'
              ? 'bg-green-50 border-green-200'
              : 'bg-red-50 border-red-200'
          }`}
        >
          <div className="flex items-center gap-2 mb-2">
            {lastResult.status === 'success' ? (
              <CheckCircle className="text-green-600" size={18} />
            ) : (
              <XCircle className="text-red-600" size={18} />
            )}
            <span
              className={`font-semibold text-sm ${
                lastResult.status === 'success' ? 'text-green-800' : 'text-red-800'
              }`}
            >
              {lastResult.message}
            </span>
          </div>

          {lastResult.status === 'success' && (
            <div className="text-xs text-gray-700 space-y-1">
              <p>Documents Processed: {lastResult.documentsProcessed}</p>
              <p>Chunks Embedded: {lastResult.chunksEmbedded}</p>
              <p>Duration: {(lastResult.durationMs / 1000).toFixed(2)}s</p>
            </div>
          )}
        </div>
      )}

      <div className="mt-4 p-3 bg-gray-50 rounded-lg">
        <p className="text-xs text-gray-600">
          This will scrape and embed content from SmartCBS website and configured sources.
          The process may take a few minutes depending on the amount of data.
        </p>
        <p className="text-xs text-gray-600 mt-1">
          Alternatively, you can upload PDF or text files to train the model directly.
        </p>
      </div>
    </div>
  );
};
