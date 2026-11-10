package com.srmresolve.dto;

import com.srmresolve.model.TicketStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class UpdateTicketStatusRequest {

    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotNull(message = "Target status is required")
    private TicketStatus targetStatus;

    private Long assignedStaffUserId;
    private LocalDateTime eta;
    private String note;

    public UpdateTicketStatusRequest() {}

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public TicketStatus getTargetStatus() { return targetStatus; }
    public void setTargetStatus(TicketStatus targetStatus) { this.targetStatus = targetStatus; }
    public Long getAssignedStaffUserId() { return assignedStaffUserId; }
    public void setAssignedStaffUserId(Long assignedStaffUserId) { this.assignedStaffUserId = assignedStaffUserId; }
    public LocalDateTime getEta() { return eta; }
    public void setEta(LocalDateTime eta) { this.eta = eta; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

