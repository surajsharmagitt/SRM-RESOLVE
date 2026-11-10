package com.srmresolve.model;

import java.time.LocalDateTime;

public class TicketMessage {
    private Long messageId;
    private Long ticketId;
    private Long senderUserId;
    private String senderRole;
    private String messageBody;
    private boolean systemAlert;
    private LocalDateTime createdAt;

    private String senderName;

    public TicketMessage() {}

    public TicketMessage(Long ticketId, Long senderUserId, String senderRole, String messageBody, boolean systemAlert) {
        this.ticketId = ticketId;
        this.senderUserId = senderUserId;
        this.senderRole = senderRole;
        this.messageBody = messageBody;
        this.systemAlert = systemAlert;
    }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public Long getSenderUserId() { return senderUserId; }
    public void setSenderUserId(Long senderUserId) { this.senderUserId = senderUserId; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }
    public boolean isSystemAlert() { return systemAlert; }
    public void setSystemAlert(boolean systemAlert) { this.systemAlert = systemAlert; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
}

