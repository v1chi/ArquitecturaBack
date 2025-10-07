package com.team.socialnetwork.dto;

import jakarta.validation.constraints.Size;

public class CreatePostRequest {
    @Size(max = 500)
    private String description;

    // Allow images up to ~10MB in base64 (approx. 13MB string length)
    @Size(max = 13_000_000)
    private String image; // Base64 payload or URL/path

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}

