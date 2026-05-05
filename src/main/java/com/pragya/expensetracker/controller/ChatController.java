package com.pragya.expensetracker.controller;

import com.pragya.expensetracker.ai.ChatHistoryManager;
import com.pragya.expensetracker.dto.ChatRequest;
import com.pragya.expensetracker.dto.ChatResponse;
import com.pragya.expensetracker.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the AI-like chatbot.
 *
 * Endpoints:
 *   POST /api/chat        → Process a chat message
 *   POST /api/chat/reset  → Clear chat history for a user
 *   GET  /api/chat/history → Get chat history for a user
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Process a user's chat message and return an intelligent response.
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.processMessage(
                request.getUsername(),
                request.getMessage()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Clear chat history for a specific user.
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetHistory(@RequestParam String username) {
        chatService.clearHistory(username);
        return ResponseEntity.ok(Map.of(
                "message", "Chat history cleared for user: " + username
        ));
    }

    /**
     * Retrieve chat history for a specific user.
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatHistoryManager.ChatMessage>> getHistory(
            @RequestParam String username) {
        List<ChatHistoryManager.ChatMessage> history = chatService.getHistory(username);
        return ResponseEntity.ok(history);
    }
}
