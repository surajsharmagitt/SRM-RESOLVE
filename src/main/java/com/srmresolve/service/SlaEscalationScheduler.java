package com.srmresolve.service;

import com.srmresolve.dao.GrievanceTicketDao;
import com.srmresolve.dao.TicketLifecycleDao;
import com.srmresolve.model.GrievanceTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Multi-Tier SLA Escalation Engine.
 * Evaluates pending tickets against SLA deadlines.
 * Level 1: Escalates to ROLE_DEPT_HEAD.
 * Level 2: Escalates to ROLE_EXECUTIVE (VC / Director).
 * Preserves operational ticket status while updating escalation level independently (Section 4.C).
 */
@Service
@EnableScheduling
public class SlaEscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlaEscalationScheduler.class);

    private final GrievanceTicketDao ticketDao;
    private final TicketLifecycleDao lifecycleDao;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Autowired
    public SlaEscalationScheduler(GrievanceTicketDao ticketDao,
                                  TicketLifecycleDao lifecycleDao,
                                  NotificationService notificationService,
                                  AuditLogService auditLogService) {
        this.ticketDao = ticketDao;
        this.lifecycleDao = lifecycleDao;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    public SlaEscalationScheduler(GrievanceTicketDao ticketDao, TicketLifecycleDao lifecycleDao) {
        this(ticketDao, lifecycleDao, null, null);
    }

    @Scheduled(fixedDelayString = "${app.sla.check-interval-ms:60000}")
    @Transactional
    public void executeSlaCheckCycle() {
        log.debug("Running SLA Escalation evaluation cycle...");
        processLevel1Escalations();
        processLevel2Escalations();
    }

    private void processLevel1Escalations() {
        List<GrievanceTicket> level1Candidates = ticketDao.findCandidatesForLevel1Escalation();
        for (GrievanceTicket ticket : level1Candidates) {
            String reason;
            if (ticket.getAcknowledgedAt() == null) {
                reason = "Acknowledgement SLA breached (deadline was " + ticket.getAckDeadlineAt() + ")";
            } else {
                reason = "Resolution SLA breached (deadline was " + ticket.getResolutionDeadlineAt() + ")";
            }

            ticketDao.updateEscalation(ticket.getTicketId(), 1, true);
            lifecycleDao.insertEscalationLog(ticket.getTicketId(), 0, 1, "ROLE_DEPT_HEAD", reason);
            lifecycleDao.insertMessage(ticket.getTicketId(), 1L, "ROLE_ADMIN",
                    "⚠️ [SYSTEM SLA ALERT] Ticket has breached SLA deadline and is now escalated to Level 1 (Department Head). Reason: " + reason,
                    true);

            if (notificationService != null) {
                notificationService.notifyEscalation(ticket.getTicketId(), 1, "ROLE_DEPT_HEAD", reason);
            }
            if (auditLogService != null) {
                auditLogService.logSlaEscalation(ticket.getTicketId(), 0, 1, reason);
            }

            log.warn("Ticket #{} escalated to Level 1 (Dept Head). Reason: {}", ticket.getTicketId(), reason);
        }
    }

    private void processLevel2Escalations() {
        List<GrievanceTicket> level2Candidates = ticketDao.findCandidatesForLevel2Escalation();
        for (GrievanceTicket ticket : level2Candidates) {
            String reason = "Resolution SLA critically overdue by > 120 minutes (deadline was " + ticket.getResolutionDeadlineAt() + ")";

            ticketDao.updateEscalation(ticket.getTicketId(), 2, true);
            lifecycleDao.insertEscalationLog(ticket.getTicketId(), 1, 2, "ROLE_EXECUTIVE", reason);
            lifecycleDao.insertMessage(ticket.getTicketId(), 1L, "ROLE_ADMIN",
                    "🚨 [CRITICAL SLA ALERT] Ticket is overdue by > 2 hours and is now escalated to Level 2 (Executive/VC Dashboard).",
                    true);

            if (notificationService != null) {
                notificationService.notifyEscalation(ticket.getTicketId(), 2, "ROLE_EXECUTIVE", reason);
            }
            if (auditLogService != null) {
                auditLogService.logSlaEscalation(ticket.getTicketId(), 1, 2, reason);
            }

            log.error("Ticket #{} escalated to Level 2 (Executive/VC). Reason: {}", ticket.getTicketId(), reason);
        }
    }
}
