package com.team.socialnetwork.dto;
public class ChatUserResponse {
    private Long id;
    private String username;

    public ChatUserResponse(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
}
