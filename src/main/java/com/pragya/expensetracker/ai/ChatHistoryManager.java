package com.pragya.expensetracker.ai;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory, per-user chat history manager.
 * Stores the last N message pairs (user + bot) for each user.
 * Thread-safe via ConcurrentHashMap.
 */
@Component
public class ChatHistoryManager {

    private static final int MAX_HISTORY_PER_USER = 5;

    private final Map<String, List<ChatMessage>> history = new ConcurrentHashMap<>();

    /**
     * Add a user message + bot reply pair to history.
     */
    public void addExchange(String username, String userMessage, String botReply) {
        List<ChatMessage> messages = history.computeIfAbsent(username, k -> new ArrayList<>());

        synchronized (messages) {
            messages.add(new ChatMessage("user", userMessage, LocalDateTime.now()));
            messages.add(new ChatMessage("bot", botReply, LocalDateTime.now()));

            // Trim to keep only the last N exchanges (N * 2 messages)
            while (messages.size() > MAX_HISTORY_PER_USER * 2) {
                messages.remove(0);
            }
        }
    }

    /**
     * Get chat history for a user.
     */
    public List<ChatMessage> getHistory(String username) {
        List<ChatMessage> messages = history.get(username);
        if (messages == null) {
            return Collections.emptyList();
        }
        synchronized (messages) {
            return new ArrayList<>(messages);
        }
    }

    /**
     * Clear chat history for a user.
     */
    public void clearHistory(String username) {
        history.remove(username);
    }

    /**
     * Simple chat message record.
     */
    public static class ChatMessage {
        private final String role;      // "user" or "bot"
        private final String content;
        private final LocalDateTime timestamp;

        public ChatMessage(String role, String content, LocalDateTime timestamp) {
            this.role = role;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
