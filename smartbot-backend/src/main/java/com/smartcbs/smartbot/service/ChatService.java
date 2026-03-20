package com.smartcbs.smartbot.service;

import com.smartcbs.smartbot.dto.ChatResponse;
import com.smartcbs.smartbot.dto.ContextWithSources;
import com.smartcbs.smartbot.entity.UserSession;
import com.smartcbs.smartbot.repository.UserSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ChatService {

    private final OllamaService ollamaService;
    private final VectorStoreService vectorStoreService;
    private final ChatSessionService chatSessionService;
    private final UserSessionRepository userSessionRepository;
    private final ConversationService conversationService;

    @Autowired
    public ChatService(OllamaService ollamaService, VectorStoreService vectorStoreService,
            ChatSessionService chatSessionService,
            UserSessionRepository userSessionRepository,
            ConversationService conversationService) {
        this.ollamaService = ollamaService;
        this.vectorStoreService = vectorStoreService;
        this.chatSessionService = chatSessionService;
        this.userSessionRepository = userSessionRepository;
        this.conversationService = conversationService;
    }

    public String processMessage(String userMessage, String sessionId) {
        log.info("Processing message: {}", userMessage);

        // Validate session if provided
        if (sessionId != null && !sessionId.isBlank()) {
            if (!isValidSession(sessionId)) {
                log.warn("Invalid session ID: {}", sessionId);
                // Create a new session or handle appropriately
            }
            chatSessionService.addUserMessage(sessionId, userMessage);
        }

        ContextWithSources contextWithSources = vectorStoreService.retrieveRelevantContext(userMessage, 3);
        String context = contextWithSources.getContext();
        String history = (sessionId != null && !sessionId.isBlank() && isValidSession(sessionId))
                ? chatSessionService.formatHistory(sessionId, 10)
                : "";

        String response;

        if (context != null && !context.isEmpty()) {
            log.info("Using RAG with context from vector store");
            response = ollamaService.chatWithContextAndHistory(userMessage, context, history);
        } else {
            // Allow basic greetings and simple responses when KB lacks context
            if (isGreeting(userMessage)) {
                response = greetingResponse();
            } else {
                log.info("No relevant context available");
                response = "I don't have enough information to answer that. " +
                        "Please train the model with relevant sources or upload files in the Training panel.";
            }
        }

        if (sessionId != null && !sessionId.isBlank() && isValidSession(sessionId)) {
            chatSessionService.addAssistantMessage(sessionId, response);
        }

        return response;
    }

    public ChatResponse processMessageWithFollowUps(String userMessage, String sessionId) {
        log.info("Processing message with follow-ups: {}", userMessage);

        // Validate session if provided
        if (sessionId != null && !sessionId.isBlank()) {
            if (!isValidSession(sessionId)) {
                log.warn("Invalid session ID: {}", sessionId);
                // Create a new session or handle appropriately
            }
            chatSessionService.addUserMessage(sessionId, userMessage);
        }

        long startTime = System.currentTimeMillis();
        ContextWithSources contextWithSources = vectorStoreService.retrieveRelevantContext(userMessage, 3);
        String context = contextWithSources.getContext();
        String history = (sessionId != null && !sessionId.isBlank() && isValidSession(sessionId))
                ? chatSessionService.formatHistory(sessionId, 10)
                : "";

        String response;

        if (context != null && !context.isEmpty()) {
            log.info("Using RAG with context from vector store");
            response = ollamaService.chatWithContextAndHistory(userMessage, context, history);
        } else {
            // Allow basic greetings and simple responses when KB lacks context
            if (isGreeting(userMessage)) {
                response = greetingResponse();
            } else {
                log.info("No relevant context available");
                response = "I don't have enough information to answer that. " +
                        "Please train the model with relevant sources or upload files in the Training panel.";
            }
        }

        if (sessionId != null && !sessionId.isBlank() && isValidSession(sessionId)) {
            chatSessionService.addAssistantMessage(sessionId, response);
        }

        // For now, return empty follow-up questions - this could be enhanced later
        List<String> followUpQuestions = new ArrayList<>();

        // Only return sources if there was relevant context used to generate the
        // response
        // and the response indicates information was found
        boolean hasRelevantContext = (context != null && !context.isEmpty());
        boolean responseIndicatesNoInfo = response.contains("I don't have enough information") ||
                response.contains("Please train the model with relevant sources");

        List<ContextWithSources.SourceInfo> sources = (hasRelevantContext && !responseIndicatesNoInfo)
                ? contextWithSources.getSources()
                : new ArrayList<>();

        // Log the conversation
        long responseTimeMs = System.currentTimeMillis() - startTime;
        conversationService.logConversation(sessionId, userMessage, response, sources, responseTimeMs);

        return new ChatResponse(response, followUpQuestions, sources);
    }

    private boolean isGreeting(String text) {
        if (text == null)
            return false;
        String t = text.trim().toLowerCase();
        return t.matches("^(hi|hello|hey|good\\s+morning|good\\s+afternoon|good\\s+evening)[!,. ]*$");
    }

    private String greetingResponse() {
        return "Hello! How can I help you today?";
    }

    private boolean isValidSession(String sessionId) {
        try {
            Optional<UserSession> session = userSessionRepository.findBySessionId(sessionId);
            return session.isPresent();
        } catch (Exception e) {
            log.error("Error validating session {}: {}", sessionId, e.getMessage(), e);
            return false;
        }
    }
}