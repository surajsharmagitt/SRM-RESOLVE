package com.srmresolve.model;

import java.time.LocalDateTime;

public class EscalationLog {
    private Long logId;
    private Long ticketId;
    private int previousLevel;
    private int newLevel;
    private String escalatedToRole;
    private String reason;
    private LocalDateTime escalatedAt;

    public EscalationLog() {}

    public EscalationLog(Long ticketId, int previousLevel, int newLevel, String escalatedToRole, String reason) {
        this.ticketId = ticketId;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.escalatedToRole = escalatedToRole;
        this.reason = reason;
    }

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public int getPreviousLevel() { return previousLevel; }
    public void setPreviousLevel(int previousLevel) { this.previousLevel = previousLevel; }
    public int getNewLevel() { return newLevel; }
    public void setNewLevel(int newLevel) { this.newLevel = newLevel; }
    public String getEscalatedToRole() { return escalatedToRole; }
    public void setEscalatedToRole(String escalatedToRole) { this.escalatedToRole = escalatedToRole; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(LocalDateTime escalatedAt) { this.escalatedAt = escalatedAt; }
}

