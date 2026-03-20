package com.smartcbs.smartbot.controller;

import com.smartcbs.smartbot.dto.ChatRequest;
import com.smartcbs.smartbot.dto.ChatResponse;
import com.smartcbs.smartbot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse chatResponse = chatService.processMessageWithFollowUps(request.getMessage(), request.getSessionId());
        return ResponseEntity.ok(chatResponse);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("SmartBot Backend is running");
    }
}