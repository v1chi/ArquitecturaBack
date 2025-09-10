package com.team.socialnetwork.dto;

import java.time.Instant;

public class PublicUserResponse {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private Instant createdAt;
    private long followersCount;
    private long followingCount;
    private boolean isPrivate;

    public PublicUserResponse() {}

    public PublicUserResponse(Long id, String fullName, String username, String email, Instant createdAt,
                              long followersCount, long followingCount, boolean isPrivate) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.isPrivate = isPrivate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public long getFollowersCount() { return followersCount; }
    public void setFollowersCount(long followersCount) { this.followersCount = followersCount; }
    public long getFollowingCount() { return followingCount; }
    public void setFollowingCount(long followingCount) { this.followingCount = followingCount; }
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }
}
