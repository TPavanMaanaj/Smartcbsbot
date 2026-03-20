package com.smartcbs.smartbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "training_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingStatus {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String jobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingJobStatus status;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int documentsProcessed;

    @Column(nullable = false)
    private int chunksEmbedded;

    @Column(nullable = false)
    private long durationMs;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (jobId == null) {
            jobId = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}