package com.team.socialnetwork.dto;

import java.time.Instant;

public class NotificationResponse {
    private Long id;
    private String type;
    private ActorInfo actor;
    private PostInfo post;
    private CommentInfo comment;
    private boolean isRead;
    private Instant createdAt;

    public NotificationResponse() {}

    public NotificationResponse(Long id, String type, ActorInfo actor, PostInfo post, CommentInfo comment, boolean isRead, Instant createdAt) {
        this.id = id;
        this.type = type;
        this.actor = actor;
        this.post = post;
        this.comment = comment;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public ActorInfo getActor() { return actor; }
    public void setActor(ActorInfo actor) { this.actor = actor; }

    public PostInfo getPost() { return post; }
    public void setPost(PostInfo post) { this.post = post; }

    public CommentInfo getComment() { return comment; }
    public void setComment(CommentInfo comment) { this.comment = comment; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // Clases internas para información relacionada
    public static class ActorInfo {
        private Long id;
        private String username;
        private String fullName;

        public ActorInfo(Long id, String username, String fullName) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }

    public static class PostInfo {
        private Long id;
        private String description;

        public PostInfo(Long id, String description) {
            this.id = id;
            this.description = description;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class CommentInfo {
        private Long id;
        private String content;

        public CommentInfo(Long id, String content) {
            this.id = id;
            this.content = content;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}