package com.team.socialnetwork.dto;

public class NotificationCountResponse {
    private Long unreadCount;

    public NotificationCountResponse() {}

    public NotificationCountResponse(Long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }
}