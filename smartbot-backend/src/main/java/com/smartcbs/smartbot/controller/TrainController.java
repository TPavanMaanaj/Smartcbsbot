package com.smartcbs.smartbot.controller;

import com.smartcbs.smartbot.dto.StatusResponse;
import com.smartcbs.smartbot.dto.TrainRequest;
import com.smartcbs.smartbot.dto.TrainResponse;
import com.smartcbs.smartbot.entity.TrainingJobStatus;
import com.smartcbs.smartbot.entity.TrainingStatus;
import com.smartcbs.smartbot.repository.TrainingStatusRepository;
import com.smartcbs.smartbot.service.DataIngestionService;
import com.smartcbs.smartbot.service.OllamaService;
import com.smartcbs.smartbot.service.VectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
@Slf4j
public class TrainController {

    private final DataIngestionService dataIngestionService;
    private final OllamaService ollamaService;
    private final VectorStoreService vectorStoreService;
    private final TrainingStatusRepository trainingStatusRepository;

    @Autowired
    public TrainController(
            DataIngestionService dataIngestionService,
            OllamaService ollamaService,
            VectorStoreService vectorStoreService,
            TrainingStatusRepository trainingStatusRepository) {
        this.dataIngestionService = dataIngestionService;
        this.ollamaService = ollamaService;
        this.vectorStoreService = vectorStoreService;
        this.trainingStatusRepository = trainingStatusRepository;
    }

    @PostMapping("/train")
    public ResponseEntity<TrainResponse> train(@RequestBody(required = false) TrainRequest request) {
        log.info("Training request received");

        boolean includeDefaults = request == null || request.isIncludeDefaultUrls();

        try {
            Map<String, Object> result = dataIngestionService.ingestData(
                    request != null ? request.getUrls() : null,
                    includeDefaults
            );

            TrainResponse response = new TrainResponse(
                    "success",
                    "Training completed successfully",
                    (Integer) result.get("documentsProcessed"),
                    (Integer) result.get("chunksEmbedded"),
                    (Long) result.get("durationMs")
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Training failed: {}", e.getMessage(), e);
            TrainResponse response = new TrainResponse(
                    "error",
                    "Training failed: " + e.getMessage(),
                    0,
                    0,
                    0L
            );
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/train/upload")
    public ResponseEntity<TrainResponse> trainUpload(@RequestPart("files") MultipartFile[] files,
                                                     @RequestParam(name = "clearExisting", defaultValue = "false") boolean clearExisting,
                                                     @RequestParam(name = "incremental", defaultValue = "true") boolean incremental) {
        log.info("Training via file upload: {} files, clearExisting={}, incremental={}", files.length, clearExisting, incremental);
        try {
            if (clearExisting) {
                vectorStoreService.clearStore();
            }

            Map<String, Object> result = dataIngestionService.ingestUploadedFiles(Arrays.asList(files), true);

            TrainResponse response = new TrainResponse(
                    "success",
                    "Training from uploaded files completed successfully",
                    (Integer) result.get("documentsProcessed"),
                    (Integer) result.get("chunksEmbedded"),
                    (Long) result.get("durationMs")
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Training via upload failed: {}", e.getMessage(), e);
            TrainResponse response = new TrainResponse(
                    "error",
                    "Training via upload failed: " + e.getMessage(),
                    0,
                    0,
                    0L
            );
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/train/async")
    public ResponseEntity<Map<String, Object>> trainAsync(@RequestBody(required = false) TrainRequest request) {
        log.info("Async training request received");

        boolean includeDefaults = request == null || request.isIncludeDefaultUrls();
        List<String> urls = request != null ? request.getUrls() : null;
        
        String jobId = UUID.randomUUID().toString();

        try {
            dataIngestionService.ingestDataAsync(jobId, urls, includeDefaults);
            
            Map<String, Object> response = Map.of(
                "jobId", jobId,
                "status", "started",
                "message", "Training job started successfully"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Async training initiation failed: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Failed to start training job: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/train/upload/async")
    public ResponseEntity<Map<String, Object>> trainUploadAsync(@RequestPart("files") MultipartFile[] files,
                                                               @RequestParam(name = "clearExisting", defaultValue = "false") boolean clearExisting,
                                                               @RequestParam(name = "incremental", defaultValue = "true") boolean incremental) {
        log.info("Async training via file upload: {} files, clearExisting={}, incremental={}", files.length, clearExisting, incremental);
        
        String jobId = UUID.randomUUID().toString();
        
        try {
            if (clearExisting) {
                vectorStoreService.clearStore();
            }

            dataIngestionService.ingestUploadedFilesAsync(jobId, Arrays.asList(files), true);
            
            Map<String, Object> response = Map.of(
                "jobId", jobId,
                "status", "started",
                "message", "Training job started successfully"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Async upload training initiation failed: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Failed to start upload training job: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/train/status/{jobId}")
    public ResponseEntity<Map<String, Object>> getTrainingStatus(@PathVariable String jobId) {
        Optional<TrainingStatus> trainingStatusOpt = trainingStatusRepository.findByJobId(jobId);
        
        if (trainingStatusOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "Training job not found"
            ));
        }
        
        TrainingStatus trainingStatus = trainingStatusOpt.get();
        
        Map<String, Object> response = Map.of(
            "jobId", trainingStatus.getJobId(),
            "status", trainingStatus.getStatus().toString(),
            "description", trainingStatus.getDescription(),
            "documentsProcessed", trainingStatus.getDocumentsProcessed(),
            "chunksEmbedded", trainingStatus.getChunksEmbedded(),
            "durationMs", trainingStatus.getDurationMs(),
            "createdAt", trainingStatus.getCreatedAt().toString(),
            "updatedAt", trainingStatus.getUpdatedAt().toString()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> status() {
        boolean ollamaAvailable = ollamaService.isOllamaAvailable();
        String ollamaStatus = ollamaAvailable ? "connected" : "disconnected";
        int embeddingsCount = vectorStoreService.getEmbeddingsCount();
        String lastTraining = vectorStoreService.getLastTrainingDate();

        StatusResponse response = new StatusResponse(
                ollamaStatus,
                ollamaService.getModelName(),
                embeddingsCount,
                ollamaAvailable && embeddingsCount > 0,
                lastTraining
        );

        return ResponseEntity.ok(response);
    }
}