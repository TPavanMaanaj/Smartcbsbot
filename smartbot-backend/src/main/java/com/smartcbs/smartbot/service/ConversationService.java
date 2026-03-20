package com.smartcbs.smartbot.service;

import com.smartcbs.smartbot.dto.ContextWithSources;
import com.smartcbs.smartbot.entity.Conversation;
import com.smartcbs.smartbot.repository.ConversationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;

    @Autowired
    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public void logConversation(String sessionId, String userMessage, String botResponse,
            List<ContextWithSources.SourceInfo> sources, Long responseTimeMs) {
        try {
            // Convert sources to a single string with delimiter
            String sourcesString = sources != null ? sources.stream()
                    .map(source -> String.format("%s|%s|%s",
                            source.getFilename(),
                            source.getSourceType(),
                            source.getUrl() != null ? source.getUrl() : "N/A"))
                    .collect(Collectors.joining(";;")) : "";

            Conversation conversation = Conversation.builder()
                    .sessionId(sessionId)
                    .userMessage(userMessage)
                    .botResponse(botResponse)
                    .sources(sourcesString)
                    .responseTimeMs(responseTimeMs)
                    .timestamp(LocalDateTime.now())
                    .intent(detectIntent(userMessage))
                    .category(detectCategory(userMessage))
                    .build();

            conversationRepository.save(conversation);
            log.info("Conversation logged successfully for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Error logging conversation for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    public List<Conversation> getConversationHistory(String sessionId) {
        try {
            return conversationRepository.findBySessionIdOrderByTimestampDesc(sessionId);
        } catch (Exception e) {
            log.error("Error retrieving conversation history for session {}: {}", sessionId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Conversation> getConversationsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return conversationRepository.findByDateRange(startDate, endDate);
        } catch (Exception e) {
            log.error("Error retrieving conversations by date range: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Conversation> getConversationsByIntent(String intent) {
        try {
            return conversationRepository.findByIntent(intent);
        } catch (Exception e) {
            log.error("Error retrieving conversations by intent '{}': {}", intent, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Conversation> getConversationsByCategory(String category) {
        try {
            return conversationRepository.findByCategory(category);
        } catch (Exception e) {
            log.error("Error retrieving conversations by category '{}': {}", category, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Long getConversationCount(LocalDateTime startDate) {
        try {
            return conversationRepository.countByDateRange(startDate);
        } catch (Exception e) {
            log.error("Error getting conversation count: {}", e.getMessage(), e);
            return 0L;
        }
    }

    public Double getAverageResponseTime(LocalDateTime startDate) {
        try {
            return conversationRepository.getAverageResponseTime(startDate);
        } catch (Exception e) {
            log.error("Error getting average response time: {}", e.getMessage(), e);
            return 0.0;
        }
    }

    public List<Object[]> getIntentDistribution(LocalDateTime startDate) {
        try {
            return conversationRepository.getIntentDistribution(startDate);
        } catch (Exception e) {
            log.error("Error getting intent distribution: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Object[]> getCategoryDistribution(LocalDateTime startDate) {
        try {
            return conversationRepository.getCategoryDistribution(startDate);
        } catch (Exception e) {
            log.error("Error getting category distribution: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public void addRating(Long conversationId, Integer rating, String feedback) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation != null) {
                conversation.setUserRating(rating);
                conversation.setFeedback(feedback);
                conversationRepository.save(conversation);
                log.info("Rating added to conversation ID: {}", conversationId);
            }
        } catch (Exception e) {
            log.error("Error adding rating to conversation {}: {}", conversationId, e.getMessage(), e);
        }
    }

    // Simple intent detection - can be enhanced with NLP
    private String detectIntent(String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) {
            return "unknown";
        }

        String lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.contains("what is") || lowerMessage.contains("explain") || lowerMessage.contains("define")) {
            return "information_request";
        } else if (lowerMessage.contains("how to") || lowerMessage.contains("steps")
                || lowerMessage.contains("process")) {
            return "procedure_request";
        } else if (lowerMessage.contains("problem") || lowerMessage.contains("issue")
                || lowerMessage.contains("error")) {
            return "problem_report";
        } else if (lowerMessage.contains("thank") || lowerMessage.contains("thanks")) {
            return "appreciation";
        } else if (lowerMessage.matches("^(hi|hello|hey|good\\s+(morning|afternoon|evening)).*")) {
            return "greeting";
        } else {
            return "general_inquiry";
        }
    }

    // Simple category detection - can be enhanced
    private String detectCategory(String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) {
            return "uncategorized";
        }

        String lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.contains("account") || lowerMessage.contains("balance")
                || lowerMessage.contains("transaction")) {
            return "account_management";
        } else if (lowerMessage.contains("loan") || lowerMessage.contains("credit")
                || lowerMessage.contains("borrow")) {
            return "loan_services";
        } else if (lowerMessage.contains("payment") || lowerMessage.contains("bill")
                || lowerMessage.contains("transfer")) {
            return "payment_services";
        } else if (lowerMessage.contains("smartcbs") || lowerMessage.contains("system")
                || lowerMessage.contains("platform")) {
            return "system_information";
        } else {
            return "general";
        }
    }
}