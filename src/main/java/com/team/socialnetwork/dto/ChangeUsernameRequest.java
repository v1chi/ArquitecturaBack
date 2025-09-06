package com.team.socialnetwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangeUsernameRequest {
    @NotBlank
    @Size(min = 2, max = 100)
    private String username;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

