package com.team.socialnetwork.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatMessageRequest {

    private Long senderId;      // para WebSocket
    private Long receiverId;    // para WebSocket

    @NotBlank
    private String content;

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
