package com.team.socialnetwork.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications")
public class Notification {

    public enum NotificationType {
        LIKE,           // Alguien le dio like a tu post
        COMMENT,        // Alguien comentó en tu post
        COMMENT_LIKE,   // Alguien le dio like a tu comentario
        FOLLOW,         // Alguien te siguió
        FOLLOW_REQUEST  // Alguien solicitó seguirte (perfil privado)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario que recibe la notificación
    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // Usuario que realizó la acción
    @ManyToOne(optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // Post relacionado (opcional, para likes y comentarios)
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // Comentario relacionado (opcional, para likes de comentarios)
    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "is_read", nullable = false, columnDefinition = "boolean not null default false")
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructores
    public Notification() {}

    public Notification(User recipient, User actor, NotificationType type) {
        this.recipient = recipient;
        this.actor = actor;
        this.type = type;
    }

    public Notification(User recipient, User actor, NotificationType type, Post post) {
        this.recipient = recipient;
        this.actor = actor;
        this.type = type;
        this.post = post;
    }

    public Notification(User recipient, User actor, NotificationType type, Post post, Comment comment) {
        this.recipient = recipient;
        this.actor = actor;
        this.type = type;
        this.post = post;
        this.comment = comment;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }

    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public Comment getComment() { return comment; }
    public void setComment(Comment comment) { this.comment = comment; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}