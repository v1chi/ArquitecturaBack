package com.team.socialnetwork.dto;

import jakarta.validation.constraints.Size;

public class CreatePostRequest {
    @Size(max = 500)
    private String description;

    @Size(max = 500)
    private String image; // URL or path

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}

