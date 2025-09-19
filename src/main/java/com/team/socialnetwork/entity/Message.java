package com.team.socialnetwork.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, length = 1000)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public Message() {}

    public Message(User sender, User receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    public Long getId() { return id; }
    public User getSender() { return sender; }
    public User getReceiver() { return receiver; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
