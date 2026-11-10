package com.srmresolve.model;

import java.time.LocalDateTime;

public class GrievanceTicket {
    private Long ticketId;
    private String ticketReference;
    private Long studentUserId;
    private Integer categoryId;
    private Integer assignedDeptId;
    private Long assignedStaffUserId;
    private Long masterIncidentId;
    private LocationScope locationScope;
    private String buildingBlock;
    private String roomOrLocation;
    private String rawDescription;
    private String aiEnhancedDescription;
    private Priority priority;
    private TicketStatus status;
    private boolean escalated;
    private int escalationLevel;
    private LocalDateTime ackDeadlineAt;
    private LocalDateTime resolutionDeadlineAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime etaGivenAt;
    private LocalDateTime resolvedByDeptAt;
    private LocalDateTime reopenedAt;
    private int reopenCount;
    private LocalDateTime closedByStudentAt;
    private Integer studentRating;
    private String studentFeedback;
    private boolean syntheticDemo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined display fields
    private String studentName;
    private String studentRegNo;
    private String categoryDisplayName;
    private String departmentName;
    private String assignedStaffName;

    public GrievanceTicket() {}

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public String getTicketReference() { return ticketReference; }
    public void setTicketReference(String ticketReference) { this.ticketReference = ticketReference; }
    public Long getStudentUserId() { return studentUserId; }
    public void setStudentUserId(Long studentUserId) { this.studentUserId = studentUserId; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public Integer getAssignedDeptId() { return assignedDeptId; }
    public void setAssignedDeptId(Integer assignedDeptId) { this.assignedDeptId = assignedDeptId; }
    public Long getAssignedStaffUserId() { return assignedStaffUserId; }
    public void setAssignedStaffUserId(Long assignedStaffUserId) { this.assignedStaffUserId = assignedStaffUserId; }
    public Long getMasterIncidentId() { return masterIncidentId; }
    public void setMasterIncidentId(Long masterIncidentId) { this.masterIncidentId = masterIncidentId; }
    public LocationScope getLocationScope() { return locationScope; }
    public void setLocationScope(LocationScope locationScope) { this.locationScope = locationScope; }
    public String getBuildingBlock() { return buildingBlock; }
    public void setBuildingBlock(String buildingBlock) { this.buildingBlock = buildingBlock; }
    public String getRoomOrLocation() { return roomOrLocation; }
    public void setRoomOrLocation(String roomOrLocation) { this.roomOrLocation = roomOrLocation; }
    public String getRawDescription() { return rawDescription; }
    public void setRawDescription(String rawDescription) { this.rawDescription = rawDescription; }
    public String getAiEnhancedDescription() { return aiEnhancedDescription; }
    public void setAiEnhancedDescription(String aiEnhancedDescription) { this.aiEnhancedDescription = aiEnhancedDescription; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public boolean isEscalated() { return escalated; }
    public void setEscalated(boolean escalated) { this.escalated = escalated; }
    public int getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(int escalationLevel) { this.escalationLevel = escalationLevel; }
    public LocalDateTime getAckDeadlineAt() { return ackDeadlineAt; }
    public void setAckDeadlineAt(LocalDateTime ackDeadlineAt) { this.ackDeadlineAt = ackDeadlineAt; }
    public LocalDateTime getResolutionDeadlineAt() { return resolutionDeadlineAt; }
    public void setResolutionDeadlineAt(LocalDateTime resolutionDeadlineAt) { this.resolutionDeadlineAt = resolutionDeadlineAt; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public LocalDateTime getEtaGivenAt() { return etaGivenAt; }
    public void setEtaGivenAt(LocalDateTime etaGivenAt) { this.etaGivenAt = etaGivenAt; }
    public LocalDateTime getResolvedByDeptAt() { return resolvedByDeptAt; }
    public void setResolvedByDeptAt(LocalDateTime resolvedByDeptAt) { this.resolvedByDeptAt = resolvedByDeptAt; }
    public LocalDateTime getReopenedAt() { return reopenedAt; }
    public void setReopenedAt(LocalDateTime reopenedAt) { this.reopenedAt = reopenedAt; }
    public int getReopenCount() { return reopenCount; }
    public void setReopenCount(int reopenCount) { this.reopenCount = reopenCount; }
    public LocalDateTime getClosedByStudentAt() { return closedByStudentAt; }
    public void setClosedByStudentAt(LocalDateTime closedByStudentAt) { this.closedByStudentAt = closedByStudentAt; }
    public Integer getStudentRating() { return studentRating; }
    public void setStudentRating(Integer studentRating) { this.studentRating = studentRating; }
    public String getStudentFeedback() { return studentFeedback; }
    public void setStudentFeedback(String studentFeedback) { this.studentFeedback = studentFeedback; }
    public boolean isSyntheticDemo() { return syntheticDemo; }
    public void setSyntheticDemo(boolean syntheticDemo) { this.syntheticDemo = syntheticDemo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }
    public String getCategoryDisplayName() { return categoryDisplayName; }
    public void setCategoryDisplayName(String categoryDisplayName) { this.categoryDisplayName = categoryDisplayName; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getAssignedStaffName() { return assignedStaffName; }
    public void setAssignedStaffName(String assignedStaffName) { this.assignedStaffName = assignedStaffName; }
}

