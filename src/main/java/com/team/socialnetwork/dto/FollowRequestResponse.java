package com.team.socialnetwork.dto;

import java.time.Instant;

public class FollowRequestResponse {
    private Long id;
    private Long followerId;
    private String followerUsername;
    private String followerFullName;
    private Instant createdAt;

    public FollowRequestResponse() {}

    public FollowRequestResponse(Long id, Long followerId, String followerUsername, String followerFullName, Instant createdAt) {
        this.id = id;
        this.followerId = followerId;
        this.followerUsername = followerUsername;
        this.followerFullName = followerFullName;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFollowerId() { return followerId; }
    public void setFollowerId(Long followerId) { this.followerId = followerId; }
    public String getFollowerUsername() { return followerUsername; }
    public void setFollowerUsername(String followerUsername) { this.followerUsername = followerUsername; }
    public String getFollowerFullName() { return followerFullName; }
    public void setFollowerFullName(String followerFullName) { this.followerFullName = followerFullName; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}