package com.pragya.expensetracker.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chat response returned to the user.
 * Includes the AI-like reply, detected intent metadata, and follow-up suggestions.
 */
public class ChatResponse {

    private String reply;
    private String detectedIntent;
    private double confidence;
    private LocalDateTime timestamp;
    private List<String> suggestions;

    public ChatResponse() {}

    public ChatResponse(String reply, String detectedIntent, double confidence, List<String> suggestions) {
        this.reply = reply;
        this.detectedIntent = detectedIntent;
        this.confidence = confidence;
        this.timestamp = LocalDateTime.now();
        this.suggestions = suggestions;
    }

    // --- Getters & Setters ---

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getDetectedIntent() {
        return detectedIntent;
    }

    public void setDetectedIntent(String detectedIntent) {
        this.detectedIntent = detectedIntent;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
