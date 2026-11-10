package com.srmresolve.model;

import java.time.LocalDateTime;

public class InAppNotification {
    private Long notificationId;
    private Long recipientUserId;
    private String title;
    private String body;
    private NotificationType notificationType;
    private Long relatedTicketId;
    private boolean read;
    private LocalDateTime createdAt;

    public InAppNotification() {}

    public InAppNotification(Long recipientUserId, String title, String body, NotificationType notificationType, Long relatedTicketId) {
        this.recipientUserId = recipientUserId;
        this.title = title;
        this.body = body;
        this.notificationType = notificationType;
        this.relatedTicketId = relatedTicketId;
        this.read = false;
    }

    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }

    public Long getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(Long recipientUserId) { this.recipientUserId = recipientUserId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public Long getRelatedTicketId() { return relatedTicketId; }
    public void setRelatedTicketId(Long relatedTicketId) { this.relatedTicketId = relatedTicketId; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
