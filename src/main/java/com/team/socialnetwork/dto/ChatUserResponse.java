package com.team.socialnetwork.dto;
public class ChatUserResponse {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String profilePicture;

    public ChatUserResponse(Long id, String username, String name, String email, String profilePicture) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.profilePicture = profilePicture;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getProfilePicture() { return profilePicture; }
}
