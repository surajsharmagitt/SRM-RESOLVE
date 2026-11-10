package com.srmresolve.service;

import com.srmresolve.dao.GrievanceTicketDao;
import com.srmresolve.dao.TicketLifecycleDao;
import com.srmresolve.model.GrievanceTicket;
import com.srmresolve.model.Priority;
import com.srmresolve.model.TicketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ResolutionHandshakeService {

    private static final Logger log = LoggerFactory.getLogger(ResolutionHandshakeService.class);

    private final GrievanceTicketDao ticketDao;
    private final TicketLifecycleDao lifecycleDao;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final TicketPriorityService ticketPriorityService;

    @Autowired
    public ResolutionHandshakeService(GrievanceTicketDao ticketDao,
                                      TicketLifecycleDao lifecycleDao,
                                      NotificationService notificationService,
                                      AuditLogService auditLogService,
                                      TicketPriorityService ticketPriorityService) {
        this.ticketDao = ticketDao;
        this.lifecycleDao = lifecycleDao;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.ticketPriorityService = ticketPriorityService;
    }

    public ResolutionHandshakeService(GrievanceTicketDao ticketDao, TicketLifecycleDao lifecycleDao) {
        this(ticketDao, lifecycleDao, null, null, null);
    }

    /**
     * Department Staff acknowledges ticket.
     * SUBMITTED -> ACKNOWLEDGED
     */
    @Transactional
    public void acknowledgeTicket(Long ticketId, Long staffUserId, String note) {
        GrievanceTicket ticket = getTicketOrThrow(ticketId);
        TicketStatus from = ticket.getStatus();
        LocalDateTime now = LocalDateTime.now();

        ticketDao.recordAcknowledgment(ticketId, staffUserId, now);
        lifecycleDao.insertStatusHistory(ticketId, from.name(), TicketStatus.ACKNOWLEDGED.name(),
                staffUserId, note != null ? note : "Ticket acknowledged by department staff.");

        lifecycleDao.insertMessage(ticketId, staffUserId, "ROLE_DEPT_STAFF",
                "Your grievance has been acknowledged and assigned to a technician.", true);

        if (notificationService != null) {
            notificationService.notifyStudentTicketStatusChange(ticketId, TicketStatus.ACKNOWLEDGED, note);
        }
        if (auditLogService != null) {
            auditLogService.logTicketStatusTransition(staffUserId, ticketId, from.name(), TicketStatus.ACKNOWLEDGED.name(), note);
        }
    }

    /**
     * Department sets ETA and moves ticket to IN_PROGRESS.
     * ACKNOWLEDGED / REOPENED -> IN_PROGRESS
     */
    @Transactional
    public void setEtaAndProgress(Long ticketId, Long staffUserId, LocalDateTime eta, String note) {
        GrievanceTicket ticket = getTicketOrThrow(ticketId);
        TicketStatus from = ticket.getStatus();

        ticketDao.updateEta(ticketId, eta);
        lifecycleDao.insertStatusHistory(ticketId, from.name(), TicketStatus.IN_PROGRESS.name(),
                staffUserId, note != null ? note : "Work started. Expected resolution by " + eta);

        lifecycleDao.insertMessage(ticketId, staffUserId, "ROLE_DEPT_STAFF",
                "Technician has begun repair work. Estimated completion: " + eta, false);

        if (notificationService != null) {
            notificationService.notifyStudentTicketStatusChange(ticketId, TicketStatus.IN_PROGRESS, "Expected completion: " + eta);
        }
        if (auditLogService != null) {
            auditLogService.logTicketStatusTransition(staffUserId, ticketId, from.name(), TicketStatus.IN_PROGRESS.name(), note);
        }
    }

    /**
     * Department marks ticket resolved.
     * IN_PROGRESS -> AWAITING_STUDENT_CONFIRMATION
     */
    @Transactional
    public void markResolvedByDept(Long ticketId, Long staffUserId, String resolutionSummary) {
        GrievanceTicket ticket = getTicketOrThrow(ticketId);
        TicketStatus from = ticket.getStatus();
        LocalDateTime now = LocalDateTime.now();

        ticketDao.recordResolutionByDept(ticketId, now);
        lifecycleDao.insertStatusHistory(ticketId, from.name(), TicketStatus.AWAITING_STUDENT_CONFIRMATION.name(),
                staffUserId, resolutionSummary != null ? resolutionSummary : "Department completed work. Awaiting student verification.");

        lifecycleDao.insertMessage(ticketId, staffUserId, "ROLE_DEPT_STAFF",
                "Work has been marked as completed by the department: " +
                (resolutionSummary != null ? resolutionSummary : "Please verify and confirm if the issue is resolved."), false);

        if (notificationService != null) {
            notificationService.notifyStudentResolutionRequested(ticketId);
        }
        if (auditLogService != null) {
            auditLogService.logTicketStatusTransition(staffUserId, ticketId, from.name(), TicketStatus.AWAITING_STUDENT_CONFIRMATION.name(), resolutionSummary);
        }
    }

    /**
     * Student confirms the fix.
     * AWAITING_STUDENT_CONFIRMATION -> CLOSED
     * Only the ticket's student can execute this (checked by studentUserId).
     */
    @Transactional
    public void confirmFixedByStudent(Long ticketId, Long studentUserId, String studentFeedback) {
        GrievanceTicket ticket = getTicketOrThrow(ticketId);
        if (!ticket.getStudentUserId().equals(studentUserId)) {
            throw new SecurityException("Only the owning student can confirm ticket resolution.");
        }

        TicketStatus from = ticket.getStatus();
        LocalDateTime now = LocalDateTime.now();

        ticketDao.recordClosureByStudent(ticketId, now);
        lifecycleDao.insertStatusHistory(ticketId, from.name(), TicketStatus.CLOSED.name(),
                studentUserId, studentFeedback != null ? studentFeedback : "Student confirmed issue is fully resolved.");

        lifecycleDao.insertMessage(ticketId, studentUserId, "ROLE_STUDENT",
                "Confirmed fixed! Thank you. " + (studentFeedback != null ? studentFeedback : ""), false);

        if (notificationService != null) {
            notificationService.notifyDepartmentResolutionConfirmed(ticketId, null, studentFeedback);
        }
        if (auditLogService != null) {
            auditLogService.logTicketStatusTransition(studentUserId, ticketId, from.name(), TicketStatus.CLOSED.name(), studentFeedback);
        }
    }

    @Transactional
    public void confirmFixedByStudent(Long ticketId, Long studentUserId, Integer rating, String studentFeedback) {
        if (rating == null) {
            confirmFixedByStudent(ticketId, studentUserId, studentFeedback);
            return;
        }

        GrievanceTicket ticket = getTicketOrThrow(ticketId);
        if (!ticket.getStudentUserId().equals(studentUserId)) {
            throw new SecurityException("Only the owning student can confirm ticket resolution.");
        }

        TicketStatus from = ticket.getStatus();
        LocalDateTime now = LocalDateTime.now();

        ticketDao.recordClosureByStudent(ticketId, now, rating, studentFeedback);
        lifecycleDao.insertStatusHistory(ticketId, from.name(), TicketStatus.CLOSED.name(),
                studentUserId, "[" + rating + "/5 Stars] " +
                (studentFeedback != null ? studentFeedback : "Student confirmed issue is fully resolved."));

        lifecycleDao.insertMessage(ticketId, studentUserId, "ROLE_STUDENT",
                "⭐ Confirmed fixed! Rating: " + rating + "/5 stars. " +
                (studentFeedback != null ? studentFeedback : ""), false);

        if (notificationService != null) {
            notificationService.notifyDepartmentResolutionConfirmed(ticketId, rating, studentFeedback);
        }
        if (auditLogService != null) {
            auditLogService.logTicketStatusTransition(studentUserId, ticketId, from.name(), TicketStatus.CLOSED.name(), "[" + rating + "/5 Stars] " + studentFeedback);
        }
    }

    /**
     * Student rejects the fix ("Still Not Fixed").
     * AWAITING_STUDENT_CONFIRMATION -> REOPENED
     * Only the ticket's student can execute this (checked by studentUserId).
     */
    @Transactional
    public void rejectFixAndReopen(Long ticketId, Long studentUserId, String rejectionReason) {
        GrievanceTicket ticket = getTicketOrThrow(ticketId);
        if (!ticket.getStudentUserId().equals(studentUserId)) {
            throw new SecurityException("Only the owning student can reopen their ticket.");
        }

        TicketStatus from = ticket.getStatus();
        LocalDateTime now = LocalDateTime.now();

        ticketDao.recordReopenedByStudent(ticketId, now);

        // Intelligent Priority Auto-Adjustment upon reopen
        if (ticketPriorityService != null) {
            Priority elevated = ticketPriorityService.calculateReopenedPriority(ticket);
            if (elevated != ticket.getPriority()) {
                ticketDao.updatePriority(ticketId, elevated);
                log.info("Elevated priority on reopened ticket #{} from {} to {}", ticketId, ticket.getPriority(), elevated);
            }
        }

        lifecycleDao.insertStatusHistory(ticketId, from.name(), TicketStatus.REOPENED.name(),
                studentUserId, "Resolution rejected by student: " + rejectionReason);

        lifecycleDao.insertMessage(ticketId, studentUserId, "ROLE_STUDENT",
                "Still Not Fixed: " + rejectionReason, false);

        log.warn("Ticket #{} reopened by student. Reopen count: {}", ticketId, ticket.getReopenCount() + 1);

        if (notificationService != null) {
            notificationService.notifyDepartmentResolutionRejected(ticketId, rejectionReason);
        }
        if (auditLogService != null) {
            auditLogService.logTicketStatusTransition(studentUserId, ticketId, from.name(), TicketStatus.REOPENED.name(), rejectionReason);
        }
    }

    private GrievanceTicket getTicketOrThrow(Long ticketId) {
        return ticketDao.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
    }
}
