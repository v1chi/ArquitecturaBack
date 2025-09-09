package com.team.socialnetwork.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class RegisterRequest {
    @NotBlank
    @Size(min = 2, max = 100)
    @Pattern(regexp = "^[a-z0-9._]+$", message = "Username must be lowercase letters, numbers, dots or underscores")
    private String username;

    @NotBlank
    @Size(min = 2, max = 150)
    @Pattern(regexp = "^[\\p{L} ]+$", message = "Full name must contain only letters and spaces")
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
