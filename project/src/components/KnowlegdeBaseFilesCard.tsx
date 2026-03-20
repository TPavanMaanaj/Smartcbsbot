import { useEffect, useState } from "react";

type KBFile = {
  id: string;
  filename: string;
  embeddingStatus: string;
  chunkCount: number;
  uploadTimestamp: string;
  sourceType?: string; // 'file' or 'url'
  url?: string; // URL if sourceType is 'url'
};

export function KnowledgeBaseFilesCard() {
  const [files, setFiles] = useState<KBFile[]>([]);
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchFiles();
  }, []);

  const fetchFiles = async () => {
    try {
      const res = await fetch(`http://localhost:8080/api/kb/files`);

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}`);
      }

      const data = await res.json();
      setFiles(Array.isArray(data) ? data : []);
      setError(null);
    } catch (err) {
      console.error("KB fetch failed:", err);
      setError("Unable to load knowledge base files");
    } finally {
      setLoading(false);
    }
  };

  /* ---------- selection logic ---------- */

  const toggleSelect = (id: string) => {
    setSelected(prev => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };

  const toggleSelectAll = () => {
    if (selected.size === files.length) {
      setSelected(new Set());
    } else {
      setSelected(new Set(files.map(f => f.id)));
    }
  };

  /* ---------- delete logic ---------- */

  const deleteFile = async (id: string) => {
    if (!window.confirm("Delete this file?")) return;
    
    try {
      await fetch(`http://localhost:8080/api/kb/files/${id}`, { method: "DELETE" });
      
      setFiles(prev => prev.filter(f => f.id !== id));
      setSelected(prev => {
        const next = new Set(prev);
        next.delete(id);
        return next;
      });
    } catch {
      alert("Failed to delete file");
    }
  };

  const deleteSelectedFiles = async () => {
    if (selected.size === 0) return;
    
    if (!window.confirm(`Delete ${selected.size} selected file(s)?`)) return;

    try {
      await Promise.all(
        Array.from(selected).map(id => {
          return fetch(`http://localhost:8080/api/kb/files/${id}`, { method: "DELETE" });
        })
      );

      setFiles(prev => prev.filter(f => !selected.has(f.id)));
      setSelected(new Set());
    } catch {
      alert("Failed to delete one or more files");
    }
  };

  /* ---------- render ---------- */

  return (
    <div className="p-8 bg-white rounded-2xl shadow-sm border border-gray-200">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-2xl font-bold text-gray-900">
          Knowledge Base Files
        </h2>

        <button
          onClick={deleteSelectedFiles}
          disabled={selected.size === 0}
          className={`px-4 py-2 rounded-lg text-sm font-medium
            ${
              selected.size === 0
                ? "bg-gray-200 text-gray-400 cursor-not-allowed"
                : "bg-red-600 text-white hover:bg-red-700"
            }`}
        >
          Delete Selected ({selected.size})
        </button>
      </div>

      {/* Select all */}
      {files.length > 0 && (
        <div className="flex items-center gap-2 mb-3">
          <input
            type="checkbox"
            checked={selected.size === files.length}
            onChange={toggleSelectAll}
          />
          <span className="text-sm text-gray-600">Select All</span>
        </div>
      )}

      {loading && <p className="text-gray-500">Loading files…</p>}

      {error && (
        <p className="text-red-600 text-sm">{error}</p>
      )}

      {!loading && !error && files.length === 0 && (
        <p className="text-gray-500">No files uploaded yet</p>
      )}

      {/* Files list */}
      <div className="space-y-4 mt-4">
        {files.map(file => (
          <div
            key={file.id}
            className="flex items-center justify-between p-4 border rounded-xl"
          >
            <div className="flex items-center gap-4">
              <input
                type="checkbox"
                checked={selected.has(file.id)}
                onChange={() => toggleSelect(file.id)}
                className="w-4 h-4"
              />

              <div>
                <p className="font-medium text-gray-900">
                  {file.filename}
                </p>
                <p className="text-sm text-gray-500">
                  {file.sourceType === 'url' ? 'URL' : 'File'} · {file.embeddingStatus} · {file.chunkCount} chunks
                </p>
                {file.url && (
                  <p className="text-sm text-blue-500 truncate max-w-xs">
                    {file.url}
                  </p>
                )}
              </div>
            </div>

            <button
              onClick={() => deleteFile(file.id)}
              className="text-sm font-medium text-red-600 hover:underline"
            >
              Delete
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
