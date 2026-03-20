package com.smartcbs.smartbot.repository;

import com.smartcbs.smartbot.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findBySessionId(String sessionId);

    List<Conversation> findBySessionIdOrderByTimestampDesc(String sessionId);

    @Query("SELECT c FROM Conversation c WHERE c.timestamp >= :startDate AND c.timestamp <= :endDate")
    List<Conversation> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM Conversation c WHERE c.intent = :intent")
    List<Conversation> findByIntent(@Param("intent") String intent);

    @Query("SELECT c FROM Conversation c WHERE c.category = :category")
    List<Conversation> findByCategory(@Param("category") String category);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.timestamp >= :startDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(c.responseTimeMs) FROM Conversation c WHERE c.timestamp >= :startDate")
    Double getAverageResponseTime(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT c.intent, COUNT(c) FROM Conversation c WHERE c.timestamp >= :startDate GROUP BY c.intent")
    List<Object[]> getIntentDistribution(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT c.category, COUNT(c) FROM Conversation c WHERE c.timestamp >= :startDate GROUP BY c.category")
    List<Object[]> getCategoryDistribution(@Param("startDate") LocalDateTime startDate);
}