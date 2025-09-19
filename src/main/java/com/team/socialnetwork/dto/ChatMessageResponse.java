package com.team.socialnetwork.dto;

import java.time.Instant;

public class ChatMessageResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Instant createdAt;
    private boolean isRead;

    public ChatMessageResponse(Long id, Long senderId, Long receiverId, String content, Instant createdAt, boolean isRead) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public Long getId() { return id; }
    public Long getSenderId() { return senderId; }
    public Long getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }
}
