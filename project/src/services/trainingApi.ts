import { TrainRequest, TrainResponse, StatusResponse } from '../types/training';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const trainModel = async (request: TrainRequest): Promise<TrainResponse> => {
  try {
    const response = await fetch(`${API_URL}/api/train`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error(`Training failed with status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Error training model:', error);
    throw new Error('Failed to train model. Please ensure the backend server is running.');
  }
};

export const getSystemStatus = async (): Promise<StatusResponse> => {
  try {
    const response = await fetch(`${API_URL}/api/status`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Status check failed with status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Error getting system status:', error);
    throw new Error('Failed to get system status. Please ensure the backend server is running.');
  }
};

export const uploadTraining = async (files: File[], clearExisting = false): Promise<TrainResponse> => {
  try {
    const formData = new FormData();
    files.forEach((f) => formData.append('files', f));
    formData.append('clearExisting', String(clearExisting));

    const response = await fetch(`${API_URL}/api/train/upload`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      throw new Error(`Upload training failed with status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Error training with uploaded files:', error);
    throw new Error('Failed to train with uploaded files. Please ensure the backend server is running.');
  }
};

// services/trainingApi.ts

export const fetchUploadedFiles = async () => {
  const res = await fetch(`${API_URL}/api/kb/files`);
  if (!res.ok) throw new Error('Failed to fetch uploaded files');
  return res.json();
};

export const deleteUploadedFile = async (fileId: string) => {
  const res = await fetch(`${API_URL}/api/kb/files/${fileId}`, {
    method: 'DELETE',
  });
  if (!res.ok) throw new Error('Failed to delete file');
  return res.json();
};

// New URL-related API functions
export const addUrlToKnowledgeBase = async (url: string, title?: string) => {
  const res = await fetch(`${API_URL}/api/kb/urls`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      url,
      title: title || url,
    }),
  });
  if (!res.ok) throw new Error('Failed to add URL');
  return res.json();
};

export const deleteUrlFromKnowledgeBase = async (urlId: string) => {
  const res = await fetch(`${API_URL}/api/kb/urls/${urlId}`, {
    method: 'DELETE',
  });
  if (!res.ok) throw new Error('Failed to delete URL');
  return res.json();
};



