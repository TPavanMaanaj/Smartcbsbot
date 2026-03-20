package com.smartcbs.smartbot.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class VectorStoreConfig {

    @Value("${vector.store.path}")
    private String vectorStorePath;
    
    private InMemoryEmbeddingStore<TextSegment> store;
    private Path storeFile;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        Path storePath = Paths.get(vectorStorePath);
        
        try {
            // Ensure directory exists
            if (!Files.exists(storePath)) {
                Files.createDirectories(storePath);
                log.info("Created vector store directory: {}", storePath);
            }
            
            // Use file path for persistence
            storeFile = storePath.resolve("embeddings.json");
            
            store = new InMemoryEmbeddingStore<>();
            
            // Load existing embeddings if file exists
            if (Files.exists(storeFile)) {
                try {
                    String json = Files.readString(storeFile);
                    store = InMemoryEmbeddingStore.fromJson(json);
                    log.info("Loaded existing embeddings from {}", storeFile);
                } catch (Exception e) {
                    log.warn("Failed to load existing embeddings, starting fresh: {}", e.getMessage());
                }
            } else {
                log.info("No existing embeddings found, starting with empty store");
            }
            
            return store;
            
        } catch (IOException e) {
            log.error("Failed to initialize vector store: {}", e.getMessage());
            log.warn("Falling back to in-memory only store");
            store = new InMemoryEmbeddingStore<>();
            return store;
        }
    }
    
    @PreDestroy
    public void saveStoreOnShutdown() {
        if (store != null && storeFile != null) {
            try {
                String json = store.serializeToJson();
                Files.writeString(storeFile, json);
                log.info("Saved embeddings to {} on shutdown", storeFile);
            } catch (IOException e) {
                log.error("Failed to save embedding store on shutdown: {}", e.getMessage());
            }
        }
    }
}