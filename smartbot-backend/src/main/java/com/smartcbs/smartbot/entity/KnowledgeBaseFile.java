package com.smartcbs.smartbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_base_files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseFile {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String filename;

    @Column
    private String storagePath;

    @Column
    private String url;

    @Column(nullable = false, unique = true, length = 64)
    private String fileHash;

    @Column(nullable = false)
    private String sourceType; // 'file' or 'url'


    @Column(nullable = false)
    private LocalDateTime uploadTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmbeddingStatus embeddingStatus;

    @Column(nullable = false)
    private Integer chunkCount;

    @PrePersist
    protected void onCreate() {
        if (uploadTimestamp == null) {
            uploadTimestamp = LocalDateTime.now();
        }
        if (embeddingStatus == null) {
            embeddingStatus = EmbeddingStatus.PENDING;
        }
        if (chunkCount == null) {
            chunkCount = 0;
        }
        if (sourceType == null) {
            sourceType = "file"; // Default to file if not specified
        }
    }
}