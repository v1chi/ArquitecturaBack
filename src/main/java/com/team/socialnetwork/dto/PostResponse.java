package com.team.socialnetwork.dto;

import java.time.Instant;

public class PostResponse {
    private Long id;
    private Instant createdAt;
    private String description;
    private String image;

    public PostResponse() {}

    public PostResponse(Long id, Instant createdAt, String description, String image) {
        this.id = id;
        this.createdAt = createdAt;
        this.description = description;
        this.image = image;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}

