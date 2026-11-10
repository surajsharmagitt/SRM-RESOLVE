package com.srmresolve.model;

import java.time.LocalDateTime;

public class AuditLog {
    private Long auditId;
    private Long actorUserId;
    private String actionType;
    private String targetEntity;
    private String targetId;
    private String details;
    private LocalDateTime createdAt;

    // Joined fields
    private String actorName;
    private String actorRegNo;

    public AuditLog() {}

    public AuditLog(Long actorUserId, String actionType, String targetEntity, String targetId, String details) {
        this.actorUserId = actorUserId;
        this.actionType = actionType;
        this.targetEntity = targetEntity;
        this.targetId = targetId;
        this.details = details;
    }

    public Long getAuditId() { return auditId; }
    public void setAuditId(Long auditId) { this.auditId = auditId; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getTargetEntity() { return targetEntity; }
    public void setTargetEntity(String targetEntity) { this.targetEntity = targetEntity; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public String getActorRegNo() { return actorRegNo; }
    public void setActorRegNo(String actorRegNo) { this.actorRegNo = actorRegNo; }
}
