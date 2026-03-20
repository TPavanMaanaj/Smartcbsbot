export interface TrainRequest {
  urls?: string[];
  includeDefaultUrls: boolean;
}

export interface TrainResponse {
  status: string;
  message: string;
  documentsProcessed: number;
  chunksEmbedded: number;
  durationMs: number;
}

export interface StatusResponse {
  ollamaStatus: string;
  modelName: string;
  embeddingsCount: number;
  isReady: boolean;
  lastTrainingDate: string;
}
