package com.pragya.expensetracker.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Incoming chat request from the user.
 */
public class ChatRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Message cannot be empty")
    private String message;

    public ChatRequest() {}

    public ChatRequest(String username, String message) {
        this.username = username;
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
