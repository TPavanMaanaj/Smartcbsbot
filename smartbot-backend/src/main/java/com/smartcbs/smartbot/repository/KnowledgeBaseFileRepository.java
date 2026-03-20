package com.smartcbs.smartbot.repository;

import com.smartcbs.smartbot.entity.EmbeddingStatus;
import com.smartcbs.smartbot.entity.KnowledgeBaseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeBaseFileRepository extends JpaRepository<KnowledgeBaseFile, String> {
    
    Optional<KnowledgeBaseFile> findByFileHash(String fileHash);
    
    List<KnowledgeBaseFile> findByEmbeddingStatus(EmbeddingStatus status);
    
    Optional<KnowledgeBaseFile> findByFilename(String filename);
    
    boolean existsByFileHash(String fileHash);
    
    // Additional methods for URL-based knowledge base files
    List<KnowledgeBaseFile> findBySourceType(String sourceType);
    
    Optional<KnowledgeBaseFile> findByUrl(String url);
}