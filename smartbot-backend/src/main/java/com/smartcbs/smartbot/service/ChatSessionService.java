package com.smartcbs.smartbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.smartcbs.smartbot.entity.UserSession;
import com.smartcbs.smartbot.repository.UserSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ChatSessionService {

    /* ---------------- Message Model ---------------- */

    public static class Message {
        private String sender; // "user" or "assistant"
        private String text;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;

        public Message() {}

        public Message(String sender, String text) {
            this.sender = sender;
            this.text = text;
            this.timestamp = LocalDateTime.now();
        }

        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /* ---------------- Dependencies ---------------- */

    @Autowired
    private UserSessionRepository userSessionRepository;

    // ✅ Use Spring-managed ObjectMapper (FIX)
    @Autowired
    private ObjectMapper objectMapper;

    private final int maxHistoryPerSession = 50;

    @Value("${session.expiration.hours:12}")
    private int sessionExpirationHours;

    /* ---------------- Public APIs ---------------- */

    public void addUserMessage(String sessionId, String text) {
        if (sessionId == null || sessionId.isBlank()) return;

        try {
            UserSession userSession = userSessionRepository
                    .findBySessionId(sessionId)
                    .orElseGet(() -> createNewSession(sessionId));

            List<Message> messages = deserializeHistory(userSession.getConversationHistory());
            messages.add(new Message("user", text));
            trimMessages(messages);

            userSession.setConversationHistory(serializeHistory(messages));
            userSessionRepository.save(userSession);

        } catch (Exception e) {
            log.error("Error adding user message to session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    public void addAssistantMessage(String sessionId, String text) {
        if (sessionId == null || sessionId.isBlank()) return;

        try {
            UserSession userSession = userSessionRepository
                    .findBySessionId(sessionId)
                    .orElseGet(() -> createNewSession(sessionId));

            List<Message> messages = deserializeHistory(userSession.getConversationHistory());
            messages.add(new Message("assistant", text));
            trimMessages(messages);

            userSession.setConversationHistory(serializeHistory(messages));
            userSessionRepository.save(userSession);

        } catch (Exception e) {
            log.error("Error adding assistant message to session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    public List<Message> getHistory(String sessionId, int limitLast) {
        if (sessionId == null || sessionId.isBlank()) return Collections.emptyList();

        try {
            Optional<UserSession> userSessionOpt = userSessionRepository.findBySessionId(sessionId);
            if (userSessionOpt.isEmpty()) return Collections.emptyList();

            List<Message> messages = deserializeHistory(userSessionOpt.get().getConversationHistory());
            if (messages.isEmpty()) return Collections.emptyList();

            int start = Math.max(0, messages.size() - Math.max(1, limitLast));
            return new ArrayList<>(messages.subList(start, messages.size()));

        } catch (Exception e) {
            log.error("Error retrieving history for session {}: {}", sessionId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public String formatHistory(String sessionId, int limitLast) {
        List<Message> history = getHistory(sessionId, limitLast);
        if (history.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (Message m : history) {
            sb.append(m.getSender()).append(": ").append(m.getText()).append("\n");
        }
        return sb.toString();
    }

    /* ---------------- Internal Helpers ---------------- */

    private UserSession createNewSession(String sessionId) {
        return UserSession.builder()
                .sessionId(sessionId)
                .userId("anonymous")
                .conversationHistory("[]")
                .build();
    }

    private void trimMessages(List<Message> messages) {
        while (messages.size() > maxHistoryPerSession) {
            messages.remove(0);
        }
    }

    private List<Message> deserializeHistory(String historyJson) {
        try {
            if (historyJson == null || historyJson.isBlank()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(historyJson, new TypeReference<List<Message>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error deserializing conversation history: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private String serializeHistory(List<Message> messages) {
        try {
            return objectMapper.writeValueAsString(messages);
        } catch (JsonProcessingException e) {
            log.error("Error serializing conversation history: {}", e.getMessage(), e);
            return "[]";
        }
    }

    /* ---------------- Cleanup Task ---------------- */

    @Scheduled(fixedRate = 3600000) // every hour
    @Transactional
    public void cleanupExpiredSessions() {
        try {
            LocalDateTime expiryDate = LocalDateTime.now().minusHours(sessionExpirationHours);
            int deletedCount = userSessionRepository.deleteExpiredSessions(expiryDate);

            if (deletedCount > 0) {
                log.info("Cleaned up {} expired sessions", deletedCount);
            }
        } catch (Exception e) {
            log.error("Error cleaning up expired sessions: {}", e.getMessage(), e);
        }
    }
}
