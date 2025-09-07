package com.team.socialnetwork.dto;

import java.time.Instant;

public class SafeUser {
    private Long id;
    private String name;
    private String username;
    private String email;
    private Instant createdAt;

    public SafeUser() {}

    public SafeUser(Long id, String name, String username, String email, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
