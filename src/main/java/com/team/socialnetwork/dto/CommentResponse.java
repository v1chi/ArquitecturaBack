package com.team.socialnetwork.dto;

import java.time.Instant;

public class CommentResponse {
    private Long id;
    private Instant createdAt;
    private String text;

    public CommentResponse() {}

    public CommentResponse(Long id, Instant createdAt, String text) {
        this.id = id;
        this.createdAt = createdAt;
        this.text = text;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}

