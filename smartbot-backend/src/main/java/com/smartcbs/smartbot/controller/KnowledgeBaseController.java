package com.smartcbs.smartbot.controller;

import com.smartcbs.smartbot.dto.TrainResponse;
import com.smartcbs.smartbot.entity.KnowledgeBaseFile;
import com.smartcbs.smartbot.repository.KnowledgeBaseFileRepository;
import com.smartcbs.smartbot.service.DataIngestionService;
import com.smartcbs.smartbot.service.VectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/kb")
@Slf4j
public class KnowledgeBaseController {

    private final DataIngestionService dataIngestionService;
    private final VectorStoreService vectorStoreService;
    private final KnowledgeBaseFileRepository fileRepository;

    @Autowired
    public KnowledgeBaseController(DataIngestionService dataIngestionService,
                                   VectorStoreService vectorStoreService,
                                   KnowledgeBaseFileRepository fileRepository) {
        this.dataIngestionService = dataIngestionService;
        this.vectorStoreService = vectorStoreService;
        this.fileRepository = fileRepository;
    }

    @GetMapping("/files")
    public ResponseEntity<List<Map<String, Object>>> listFiles() {
        List<Map<String, Object>> files = dataIngestionService.listUploadedFiles();
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String fileId) {
        boolean deleted = dataIngestionService.deleteUploadedFile(fileId); // Hard delete
        Map<String, Object> result = new HashMap<>();
        result.put("fileId", fileId);
        result.put("deleted", deleted);
        return deleted ? ResponseEntity.ok(result) : ResponseEntity.status(404).body(result);
    }
    


    @GetMapping("/files/{fileId}/status")
    public ResponseEntity<Map<String, Object>> getFileStatus(@PathVariable String fileId) {
        Optional<KnowledgeBaseFile> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "File not found"));
        }
        
        KnowledgeBaseFile file = fileOpt.get();
        Map<String, Object> status = new HashMap<>();
        status.put("id", file.getId());
        status.put("filename", file.getFilename());
        status.put("embeddingStatus", file.getEmbeddingStatus().toString());
        status.put("chunkCount", file.getChunkCount());
        status.put("uploadTimestamp", file.getUploadTimestamp().toString());
        
        return ResponseEntity.ok(status);
    }

    @PostMapping("/files/{fileId}/retrain")
    public ResponseEntity<TrainResponse> retrainFile(@PathVariable String fileId) {
        log.info("Retraining file: {}", fileId);
        
        Optional<KnowledgeBaseFile> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            TrainResponse response = new TrainResponse(
                    "error",
                    "File not found",
                    0,
                    0,
                    0L
            );
            return ResponseEntity.status(404).body(response);
        }
        
        KnowledgeBaseFile kbFile = fileOpt.get();
        
        try {
            // Remove existing embeddings for this file
            vectorStoreService.removeEmbeddingsByFileId(fileId);
            
            // Re-embed the file
            Map<String, Object> result = dataIngestionService.ingestStoredFiles(false);
            
            TrainResponse response = new TrainResponse(
                    "success",
                    "File retraining completed",
                    (Integer) result.get("documentsProcessed"),
                    (Integer) result.get("chunksEmbedded"),
                    (Long) result.get("durationMs")
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("File retraining failed: {}", e.getMessage(), e);
            TrainResponse response = new TrainResponse(
                    "error",
                    "File retraining failed: " + e.getMessage(),
                    0,
                    0,
                    0L
            );
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/reindex")
    public ResponseEntity<TrainResponse> reindex(@RequestParam(name = "clearExisting", defaultValue = "false") boolean clearExisting) {
        log.info("Reindexing stored uploaded files, clearExisting={}", clearExisting);

        if (clearExisting) {
            vectorStoreService.clearStore();
        }

        Map<String, Object> result = dataIngestionService.ingestStoredFiles(false);

        TrainResponse response = new TrainResponse(
                "success",
                "Reindexing completed",
                (Integer) result.get("documentsProcessed"),
                (Integer) result.get("chunksEmbedded"),
                (Long) result.get("durationMs")
        );
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/urls")
    public ResponseEntity<Map<String, Object>> addUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        String title = request.get("title");
        
        if (url == null || url.trim().isEmpty()) {
            Map<String, Object> errorResult = Map.of("error", "URL is required");
            return ResponseEntity.badRequest().body(errorResult);
        }
        
        try {
            Map<String, Object> result = dataIngestionService.ingestUrl(url, title);
            
            Map<String, Object> response = new HashMap<>();
            response.put("url", url);
            response.put("title", title);
            response.put("documentsProcessed", result.get("documentsProcessed"));
            response.put("chunksEmbedded", result.get("chunksEmbedded"));
            response.put("durationMs", result.get("durationMs"));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding URL {}: {}", url, e.getMessage());
            Map<String, Object> errorResult = Map.of(
                "error", "Failed to add URL: " + e.getMessage(),
                "url", url
            );
            return ResponseEntity.status(500).body(errorResult);
        }
    }
    
    @DeleteMapping("/urls/{urlId}")
    public ResponseEntity<Map<String, Object>> deleteUrl(@PathVariable String urlId) {
        boolean deleted = dataIngestionService.deleteUrl(urlId);
        Map<String, Object> result = new HashMap<>();
        result.put("urlId", urlId);
        result.put("deleted", deleted);
        return deleted ? ResponseEntity.ok(result) : ResponseEntity.status(404).body(result);
    }
}