package com.smartcbs.smartbot.repository;

import com.smartcbs.smartbot.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    
    Optional<UserSession> findBySessionId(String sessionId);
    
    List<UserSession> findByUserId(String userId);
    
    @Modifying
    @Query("DELETE FROM UserSession u WHERE u.lastAccessedAt < :expiryDate")
    int deleteExpiredSessions(@Param("expiryDate") LocalDateTime expiryDate);
    
    boolean existsBySessionId(String sessionId);
}