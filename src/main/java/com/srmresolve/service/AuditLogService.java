package com.srmresolve.service;

import com.srmresolve.dao.AuditLogDao;
import com.srmresolve.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogDao auditLogDao;

    public AuditLogService(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    public void logAction(Long actorUserId, String actionType, String targetEntity, String targetId, String details) {
        AuditLog audit = new AuditLog(actorUserId, actionType, targetEntity, targetId, details);
        auditLogDao.insert(audit);
        log.info("AUDIT: [{}] by user {} on {}:{} -> {}", actionType, actorUserId, targetEntity, targetId, details);
    }

    public void logTicketCreated(Long actorUserId, Long ticketId, String reference, String categoryName) {
        logAction(actorUserId, "TICKET_CREATED", "GRIEVANCE_TICKET", String.valueOf(ticketId),
                "Filed ticket " + reference + " under category " + categoryName);
    }

    public void logTicketStatusTransition(Long actorUserId, Long ticketId, String fromStatus, String toStatus, String note) {
        logAction(actorUserId, "STATUS_CHANGE", "GRIEVANCE_TICKET", String.valueOf(ticketId),
                "Transitioned from " + fromStatus + " to " + toStatus + (note != null ? " (" + note + ")" : ""));
    }

    public void logSlaEscalation(Long ticketId, int prevLevel, int newLevel, String reason) {
        logAction(null, "SLA_ESCALATED", "GRIEVANCE_TICKET", String.valueOf(ticketId),
                "Auto-escalated from Level " + prevLevel + " to Level " + newLevel + ". Reason: " + reason);
    }

    public void logPasswordReset(Long userId, String email, boolean successful) {
        logAction(userId, "PASSWORD_RESET", "USER", String.valueOf(userId),
                (successful ? "Password reset completed" : "Password reset attempted") + " for " + email);
    }

    public List<AuditLog> getRecentAuditLogs(int limit) {
        return auditLogDao.findRecent(limit);
    }

    public List<AuditLog> getLogsForTicket(Long ticketId) {
        return auditLogDao.findByTarget("GRIEVANCE_TICKET", String.valueOf(ticketId));
    }

    public Map<String, Long> getActionDistribution() {
        return auditLogDao.countByActionType();
    }
}
