package com.team.socialnetwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class ChangeUsernameRequest {
    @NotBlank
    @Size(min = 2, max = 100)
    @Pattern(regexp = "^[a-z0-9._]+$", message = "Username must be lowercase letters, numbers, dots or underscores")
    private String username;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
