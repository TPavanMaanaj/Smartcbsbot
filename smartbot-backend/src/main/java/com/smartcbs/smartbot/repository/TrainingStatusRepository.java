package com.smartcbs.smartbot.repository;

import com.smartcbs.smartbot.entity.TrainingJobStatus;
import com.smartcbs.smartbot.entity.TrainingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingStatusRepository extends JpaRepository<TrainingStatus, String> {
    
    Optional<TrainingStatus> findByJobId(String jobId);
    
    List<TrainingStatus> findByStatus(TrainingJobStatus status);
    
    List<TrainingStatus> findByStatusOrderByCreatedAtDesc(TrainingJobStatus status);
}