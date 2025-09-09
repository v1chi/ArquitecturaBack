package com.team.socialnetwork.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "comment_likes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_comment_like_user_comment", columnNames = {"user_id", "comment_id"})
})
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_like_user"))
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_like_comment"))
    private Comment comment;

    public CommentLike() {}

    public CommentLike(User user, Comment comment) {
        this.user = user;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Comment getComment() { return comment; }
    public void setComment(Comment comment) { this.comment = comment; }
}

