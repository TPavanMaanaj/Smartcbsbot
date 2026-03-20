package com.smartcbs.smartbot.service;

import com.smartcbs.smartbot.dto.ContextWithSources;
import com.smartcbs.smartbot.entity.KnowledgeBaseFile;
import com.smartcbs.smartbot.repository.KnowledgeBaseFileRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VectorStoreService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final KnowledgeBaseFileRepository fileRepository;
    private DocumentSplitter documentSplitter;
    private LocalDateTime lastTrainingDate;

    @Value("${ingestion.chunk-size}")
    private int chunkSize;

    @Value("${ingestion.chunk-overlap}")
    private int chunkOverlap;

    @Value("${vector.store.path}")
    private String vectorStorePath;

    @Autowired
    public VectorStoreService(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel,
            KnowledgeBaseFileRepository fileRepository) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.fileRepository = fileRepository;
        this.documentSplitter = DocumentSplitters.recursive(500, 50);
    }

    @jakarta.annotation.PostConstruct
    void initSplitter() {
        this.documentSplitter = DocumentSplitters.recursive(chunkSize, chunkOverlap);
    }

    public int embedDocuments(List<Document> documents) {
        log.info("Starting to embed {} documents", documents.size());
        int totalChunks = 0;

        for (Document document : documents) {
            List<TextSegment> segments = documentSplitter.split(document);

            for (TextSegment segment : segments) {
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
                totalChunks++;
            }
        }

        lastTrainingDate = LocalDateTime.now();
        log.info("Successfully embedded {} chunks from {} documents", totalChunks, documents.size());

        // Save the store to disk
        saveStore();

        return totalChunks;
    }

    public ContextWithSources retrieveRelevantContext(String query, int maxResults) {
        log.info("Searching for relevant context for query: {}", query);

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        List<EmbeddingMatch<TextSegment>> relevantMatches = embeddingStore.findRelevant(queryEmbedding, maxResults);

        if (relevantMatches.isEmpty()) {
            log.info("No relevant context found");
            return new ContextWithSources("", new ArrayList<>());
        }

        StringBuilder context = new StringBuilder();
        List<ContextWithSources.SourceInfo> sources = new ArrayList<>();

        for (EmbeddingMatch<TextSegment> match : relevantMatches) {
            String text = match.embedded().text();
            context.append(text).append("\n\n");

            // Extract source information from metadata
            String fileId = match.embedded().metadata("fileId");
            String source = match.embedded().metadata("source");
            String filename = match.embedded().metadata("filename");
            String type = match.embedded().metadata("type");

            // Determine source type
            String sourceType = "file";
            if (type != null && type.equals("pdf")) {
                sourceType = "url"; // This is a PDF from URL
            } else if (source != null && source.startsWith("http")) {
                sourceType = "url";
            }

            // Get the knowledge base file info if available
            String finalSourceType = sourceType;
            String finalFileId = fileId;

            String uploadDate = null;
            String version = null; // No default version

            // Get file details from database if available
            if (finalFileId != null) {
                Optional<KnowledgeBaseFile> kbFileOpt = fileRepository.findById(finalFileId);
                if (kbFileOpt.isPresent()) {
                    KnowledgeBaseFile kbFile = kbFileOpt.get();
                    // Only set upload date and version for file uploads, not URLs
                    if ("file".equals(kbFile.getSourceType())) {
                        uploadDate = kbFile.getUploadTimestamp() != null ? kbFile.getUploadTimestamp().toString()
                                : null;
                        version = "1.0"; // Default version for uploaded files
                    }
                    // For URLs, we leave uploadDate and version as null
                }
            }

            ContextWithSources.SourceInfo sourceInfo = new ContextWithSources.SourceInfo(
                    fileId,
                    filename != null ? filename : (finalSourceType.equals("url") ? source : "Unknown"),
                    finalSourceType.equals("url") ? source : null,
                    finalSourceType,
                    match.score(),
                    text.length() > 100 ? text.substring(0, 100) + "..." : text,
                    uploadDate,
                    version);

            sources.add(sourceInfo);
        }

        log.info("Retrieved {} relevant chunks", relevantMatches.size());
        return new ContextWithSources(context.toString().trim(), sources);
    }

    public int getEmbeddingsCount() {
        return embeddingStore.findRelevant(
                embeddingModel.embed("test").content(),
                Integer.MAX_VALUE).size();
    }

    public String getLastTrainingDate() {
        if (lastTrainingDate == null) {
            return "Never";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return lastTrainingDate.format(formatter);
    }

    public void clearStore() {
        log.info("Clearing embedding store");
        embeddingStore.removeAll();
        lastTrainingDate = null;
        saveStore();
    }

    public void saveStore() {
        try {
            if (embeddingStore instanceof InMemoryEmbeddingStore) {
                Path storeFile = Paths.get(vectorStorePath, "embeddings.json");
                String json = ((InMemoryEmbeddingStore<TextSegment>) embeddingStore).serializeToJson();
                Files.writeString(storeFile, json);
                log.info("Saved embeddings to {}", storeFile);
            }
        } catch (IOException e) {
            log.error("Failed to save embedding store: {}", e.getMessage());
        }
    }

    public void removeEmbeddingsByFileId(String fileId) {
        log.info("Removing embeddings for file ID: {}", fileId);

        if (!(embeddingStore instanceof InMemoryEmbeddingStore)) {
            log.warn("Cannot remove by file ID: store does not support filtering");
            return;
        }

        try {
            // Get all embeddings
            Embedding dummyEmbedding = embeddingModel.embed("dummy").content();
            List<EmbeddingMatch<TextSegment>> allMatches = embeddingStore.findRelevant(dummyEmbedding,
                    Integer.MAX_VALUE, 0.0);

            // Filter out embeddings for this file
            List<String> idsToRemove = allMatches.stream()
                    .filter(match -> {
                        TextSegment segment = match.embedded();
                        String segmentFileId = segment.metadata("fileId");
                        // Handle case where metadata might be null
                        return segmentFileId != null && fileId.equals(segmentFileId);
                    })
                    .map(match -> match.embeddingId())
                    .collect(Collectors.toList());

            // Remove embeddings
            for (String id : idsToRemove) {
                embeddingStore.remove(id);
            }

            log.info("Removed {} embeddings for file ID: {}", idsToRemove.size(), fileId);
            saveStore();

        } catch (Exception e) {
            log.error("Error removing embeddings for file {}: {} - {}", fileId, e.getClass().getSimpleName(),
                    e.getMessage());
        }
    }

    public int getEmbeddingCountByFileId(String fileId) {
        try {
            Embedding dummyEmbedding = embeddingModel.embed("dummy").content();
            List<EmbeddingMatch<TextSegment>> allMatches = embeddingStore.findRelevant(dummyEmbedding,
                    Integer.MAX_VALUE, 0.0);

            return (int) allMatches.stream()
                    .filter(match -> {
                        TextSegment segment = match.embedded();
                        String segmentFileId = segment.metadata("fileId");
                        return fileId.equals(segmentFileId);
                    })
                    .count();
        } catch (Exception e) {
            log.error("Error counting embeddings for file {}: {}", fileId, e.getMessage());
            return 0;
        }
    }
}
