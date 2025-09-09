package com.team.socialnetwork.dto;

import java.time.Instant;

public class PostDetailResponse {
    private Long id;
    private Instant createdAt;
    private String description;
    private String image;
    private SafeUser author;
    private long likesCount;
    private long commentsCount;
    private boolean viewerLiked;

    public PostDetailResponse() {}

    public PostDetailResponse(Long id, Instant createdAt, String description, String image,
                              SafeUser author, long likesCount, long commentsCount, boolean viewerLiked) {
        this.id = id;
        this.createdAt = createdAt;
        this.description = description;
        this.image = image;
        this.author = author;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.viewerLiked = viewerLiked;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public SafeUser getAuthor() { return author; }
    public void setAuthor(SafeUser author) { this.author = author; }
    public long getLikesCount() { return likesCount; }
    public void setLikesCount(long likesCount) { this.likesCount = likesCount; }
    public long getCommentsCount() { return commentsCount; }
    public void setCommentsCount(long commentsCount) { this.commentsCount = commentsCount; }
    public boolean isViewerLiked() { return viewerLiked; }
    public void setViewerLiked(boolean viewerLiked) { this.viewerLiked = viewerLiked; }
}

