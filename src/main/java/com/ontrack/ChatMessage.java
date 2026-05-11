package com.ontrack;

import java.time.Instant;

public class ChatMessage {
    private final String senderRole;
    private final String senderId;
    private final String message;
    private final Instant createdAt;

    public ChatMessage(String senderRole, String senderId, String message) {
        this.senderRole = senderRole;
        this.senderId = senderId;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public String getSenderRole() {
        return senderRole;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
