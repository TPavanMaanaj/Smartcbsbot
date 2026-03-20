package com.smartcbs.smartbot.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class OllamaService {

    private final ChatLanguageModel chatLanguageModel;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model-name}")
    private String modelName;

    @Value("${smartbot.base-prompt}")
    private String basePrompt;

    private final OkHttpClient httpClient;

    @Autowired
    public OllamaService(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
        this.httpClient = new OkHttpClient();
    }

    public String chat(String userMessage, String context) {
        try {
            // Check for conflicts in context before building prompt
            List<String> conflictingContexts = findConflictingContexts(userMessage, context);

            if (!conflictingContexts.isEmpty()) {
                return buildConflictResponse(userMessage, conflictingContexts);
            }

            String prompt = buildPromptWithContext(userMessage, context);
            log.info("Sending query to Ollama: {}", userMessage);
            String response = chatLanguageModel.generate(prompt);
            log.info("Received response from Ollama");
            return response;
        } catch (Exception e) {
            log.error("Error querying Ollama model: {}", e.getMessage());
            return "I'm having trouble connecting to the AI model. Please ensure Ollama is running and try again.";
        }
    }

    public String chatWithoutContext(String userMessage) {
        try {
            log.info("Sending query to Ollama without context: {}", userMessage);
            String response = chatLanguageModel.generate(userMessage);
            log.info("Received response from Ollama");
            return response;
        } catch (Exception e) {
            log.error("Error querying Ollama model: {}", e.getMessage());
            return "I'm having trouble connecting to the AI model. Please ensure Ollama is running and try again.";
        }
    }

    public boolean isOllamaAvailable() {
        try {
            Request request = new Request.Builder()
                    .url(ollamaBaseUrl + "/api/tags")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            log.error("Ollama is not available: {}", e.getMessage());
            return false;
        }
    }

    public String getModelName() {
        return modelName;
    }

    private String buildPromptWithContext(String userMessage, String context) {
        if (context == null || context.isEmpty()) {
            // If no context, caller should decide fallback behavior
            return "I don't have enough information in the knowledge base to answer that.";
        }
        String base = basePrompt;
        StringBuilder sb = new StringBuilder(base);
        if (context != null && !context.isEmpty()) {
            sb.append("\n\nContext:\n").append(context);
        }
        sb.append("\n\nUser Question: ").append(userMessage)
                .append("\n\nProvide a clear, concise and short answer:");

        return sb.toString();
    }

    public String chatWithContextAndHistory(String userMessage, String context, String history) {
        try {
            // Check for conflicts in context before building prompt
            List<String> conflictingContexts = findConflictingContexts(userMessage, context);

            if (!conflictingContexts.isEmpty()) {
                return buildConflictResponse(userMessage, conflictingContexts);
            }

            String prompt = buildPromptWithContextAndHistory(userMessage, context, history);
            log.info("Sending query to Ollama with history: {}", userMessage);
            String response = chatLanguageModel.generate(prompt);
            log.info("Received response from Ollama");
            return response;
        } catch (Exception e) {
            log.error("Error querying Ollama model: {}", e.getMessage());
            return "I'm having trouble connecting to the AI model. Please ensure Ollama is running and try again.";
        }
    }

    private String buildPromptWithContextAndHistory(String userMessage, String context, String history) {

        String base = basePrompt;
        StringBuilder sb = new StringBuilder(base);
        if (history != null && !history.isEmpty()) {
            sb.append("\n\nHistory:\n").append(history);
        }
        if (context != null && !context.isEmpty()) {
            sb.append("\n\nContext:\n").append(context);
        }
        sb.append("\n\nUser Question: ").append(userMessage)
                .append("\n\nProvide a clear, concise and short answer:");

        return sb.toString();
    }

    // New method to find conflicting contexts
    private List<String> findConflictingContexts(String userMessage, String context) {
        List<String> conflictingContexts = new ArrayList<>();

        if (context == null || context.isEmpty() || userMessage == null) {
            return conflictingContexts;
        }

        // Extract the main subject from the user message
        String subject = extractSubject(userMessage.toLowerCase());
        if (subject == null || subject.isEmpty()) {
            return conflictingContexts;
        }

        // Look for different sentences or phrases about the subject
        String[] sentences = context.split("[.!?]");

        // Use a set to track already processed content
        Set<String> processedContent = new HashSet<>();

        for (String sentence : sentences) {
            String trimmedSentence = sentence.trim();
            if (!trimmedSentence.isEmpty() &&
                    containsSubject(trimmedSentence.toLowerCase(), subject)) {

                // Create a normalized version for duplicate detection
                String normalizedSentence = trimmedSentence.toLowerCase().trim();

                // Skip if we've already processed this content
                if (processedContent.contains(normalizedSentence)) {
                    continue;
                }

                // Check if this sentence is different from existing ones
                if (isDistinctContext(trimmedSentence, conflictingContexts)) {
                    conflictingContexts.add(trimmedSentence);
                    processedContent.add(normalizedSentence);
                }
            }
        }

        // Only return if we found multiple distinct contexts
        return conflictingContexts.size() > 1 ? conflictingContexts : new ArrayList<>();
    }

    private String extractSubject(String question) {
        // Simple subject extraction
        if (question.startsWith("what is ")) {
            String afterWhatIs = question.substring(8);
            // Remove trailing question mark if present
            if (afterWhatIs.endsWith("?")) {
                afterWhatIs = afterWhatIs.substring(0, afterWhatIs.length() - 1);
            }
            return afterWhatIs.trim();
        }
        // Add more patterns as needed
        return question.replace("?", "").trim();
    }

    private boolean containsSubject(String text, String subject) {
        return text.contains(subject) ||
                (subject.length() > 5 && text.matches(".*\\b" + subject + "\\b.*"));
    }

    private boolean isDistinctContext(String newContext, List<String> existingContexts) {
        if (newContext.length() < 20) {
            return false; // Too short to be meaningful
        }

        // Normalize the new context for comparison
        String normalizedNewContext = newContext.toLowerCase().trim();

        for (String existing : existingContexts) {
            // Normalize existing context for comparison
            String normalizedExisting = existing.toLowerCase().trim();

            // Check if new context is very similar to an existing one
            if (calculateSimilarity(normalizedExisting, normalizedNewContext) > 0.8) {
                return false;
            }

            // Additional check for exact or near-exact matches
            if (normalizedNewContext.equals(normalizedExisting) ||
                    (normalizedNewContext.length() > 50 && normalizedExisting.contains(
                            normalizedNewContext.substring(0, Math.min(50, normalizedNewContext.length()))))) {
                return false;
            }
        }
        return true;
    }

    private double calculateSimilarity(String str1, String str2) {
        // Simple similarity calculation
        String[] words1 = str1.split("\\s+");
        String[] words2 = str2.split("\\s+");

        int commonWords = 0;
        for (String word1 : words1) {
            if (word1.length() > 3) { // Only consider meaningful words
                for (String word2 : words2) {
                    if (word2.contains(word1) || word1.contains(word2)) {
                        commonWords++;
                        break;
                    }
                }
            }
        }

        int totalWords = Math.max(words1.length, words2.length);
        return totalWords > 0 ? (double) commonWords / totalWords : 0;
    }

    private String buildConflictResponse(String userMessage, List<String> conflictingContexts) {
        String subject = extractSubject(userMessage.toLowerCase());
        StringBuilder response = new StringBuilder();

        response.append("I found multiple references to '").append(subject).append("' in the knowledge base:\n\n");

        for (int i = 0; i < conflictingContexts.size(); i++) {
            response.append(i + 1).append(". ").append(conflictingContexts.get(i)).append("\n");
        }

        response.append("\nPlease specify which aspect of '").append(subject)
                .append("' you're interested in so I can provide a more accurate answer.");

        return response.toString();
    }

}