package com.smartcbs.smartbot.controller;

import com.smartcbs.smartbot.entity.Conversation;
import com.smartcbs.smartbot.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;

    @Autowired
    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<Conversation>> getConversationHistory(@PathVariable String sessionId) {
        try {
            List<Conversation> conversations = conversationService.getConversationHistory(sessionId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("Error retrieving conversation history for session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Conversation>> getConversationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<Conversation> conversations = conversationService.getConversationsByDateRange(startDate, endDate);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("Error retrieving conversations by date range: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/intent/{intent}")
    public ResponseEntity<List<Conversation>> getConversationsByIntent(@PathVariable String intent) {
        try {
            List<Conversation> conversations = conversationService.getConversationsByIntent(intent);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("Error retrieving conversations by intent '{}': {}", intent, e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Conversation>> getConversationsByCategory(@PathVariable String category) {
        try {
            List<Conversation> conversations = conversationService.getConversationsByCategory(category);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("Error retrieving conversations by category '{}': {}", category, e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getConversationAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        try {
            Map<String, Object> analytics = new HashMap<>();

            Long totalConversations = conversationService.getConversationCount(startDate);
            Double avgResponseTime = conversationService.getAverageResponseTime(startDate);
            List<Object[]> intentDistribution = conversationService.getIntentDistribution(startDate);
            List<Object[]> categoryDistribution = conversationService.getCategoryDistribution(startDate);

            analytics.put("totalConversations", totalConversations);
            analytics.put("averageResponseTimeMs", avgResponseTime);
            analytics.put("intentDistribution", intentDistribution);
            analytics.put("categoryDistribution", categoryDistribution);
            analytics.put("startDate", startDate.toString());

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error retrieving conversation analytics: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/{conversationId}/rating")
    public ResponseEntity<Map<String, Object>> addRating(
            @PathVariable Long conversationId,
            @RequestBody Map<String, Object> ratingData) {
        try {
            Integer rating = (Integer) ratingData.get("rating");
            String feedback = (String) ratingData.get("feedback");

            conversationService.addRating(conversationId, rating, feedback);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Rating added successfully");
            response.put("conversationId", conversationId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding rating to conversation {}: {}", conversationId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to add rating");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getConversationStats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneDayAgo = now.minusDays(1);
            LocalDateTime oneWeekAgo = now.minusWeeks(1);
            LocalDateTime oneMonthAgo = now.minusMonths(1);

            Map<String, Object> stats = new HashMap<>();

            stats.put("today", conversationService.getConversationCount(oneDayAgo));
            stats.put("thisWeek", conversationService.getConversationCount(oneWeekAgo));
            stats.put("thisMonth", conversationService.getConversationCount(oneMonthAgo));

            stats.put("avgResponseTimeToday", conversationService.getAverageResponseTime(oneDayAgo));
            stats.put("avgResponseTimeWeek", conversationService.getAverageResponseTime(oneWeekAgo));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error retrieving conversation stats: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
}