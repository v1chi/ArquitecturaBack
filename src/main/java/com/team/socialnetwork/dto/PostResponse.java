package com.team.socialnetwork.dto;

import java.time.Instant;


public class PostResponse {
    private Long id;
    private Instant createdAt;
    private String description;
    private String image;
    private String username;
    private Long userId;

    public PostResponse() {}

    public PostResponse(Long id, Instant createdAt, String description, String image, String username, Long userId) {
        this.id = id;
        this.createdAt = createdAt;
        this.description = description;
        this.image = image;
        this.username = username;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}

