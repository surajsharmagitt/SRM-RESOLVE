package com.srmresolve.service;

import com.srmresolve.dao.GrievanceTicketDao;
import com.srmresolve.dao.NotificationDao;
import com.srmresolve.dao.UserDao;
import com.srmresolve.model.GrievanceTicket;
import com.srmresolve.model.InAppNotification;
import com.srmresolve.model.NotificationType;
import com.srmresolve.model.TicketStatus;
import com.srmresolve.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationDao notificationDao;
    private final GrievanceTicketDao ticketDao;
    private final UserDao userDao;

    public NotificationService(NotificationDao notificationDao, GrievanceTicketDao ticketDao, UserDao userDao) {
        this.notificationDao = notificationDao;
        this.ticketDao = ticketDao;
        this.userDao = userDao;
    }

    public void notifyStudentTicketStatusChange(Long ticketId, TicketStatus newStatus, String note) {
        Optional<GrievanceTicket> opt = ticketDao.findById(ticketId);
        if (opt.isEmpty()) return;
        GrievanceTicket ticket = opt.get();

        String title = "Ticket " + ticket.getTicketReference() + " Updated";
        String body = "Your ticket status has been updated to: " + newStatus.name() +
                (note != null && !note.isBlank() ? ". Note: " + note : ".");

        InAppNotification notif = new InAppNotification(
                ticket.getStudentUserId(),
                title,
                body,
                NotificationType.TICKET_UPDATE,
                ticketId
        );
        notificationDao.insert(notif);
        log.info("Sent status update notification to student user {} for ticket {}", ticket.getStudentUserId(), ticket.getTicketReference());
    }

    public void notifyStudentResolutionRequested(Long ticketId) {
        Optional<GrievanceTicket> opt = ticketDao.findById(ticketId);
        if (opt.isEmpty()) return;
        GrievanceTicket ticket = opt.get();

        String title = "Resolution Handshake: " + ticket.getTicketReference();
        String body = "The department has marked your grievance as resolved. Please inspect the fix and confirm resolution or request rework.";

        InAppNotification notif = new InAppNotification(
                ticket.getStudentUserId(),
                title,
                body,
                NotificationType.RESOLUTION_REQUEST,
                ticketId
        );
        notificationDao.insert(notif);
    }

    public void notifyDepartmentResolutionConfirmed(Long ticketId, Integer rating, String feedback) {
        Optional<GrievanceTicket> opt = ticketDao.findById(ticketId);
        if (opt.isEmpty()) return;
        GrievanceTicket ticket = opt.get();

        String title = "Student Confirmed Fix: " + ticket.getTicketReference();
        String ratingText = rating != null ? " Rating: " + rating + "/5 stars." : "";
        String body = "The student confirmed the issue is resolved." + ratingText;

        if (ticket.getAssignedStaffUserId() != null) {
            notificationDao.insert(new InAppNotification(
                    ticket.getAssignedStaffUserId(),
                    title,
                    body,
                    NotificationType.RESOLUTION_CONFIRMED,
                    ticketId
            ));
        }
    }

    public void notifyDepartmentResolutionRejected(Long ticketId, String studentReason) {
        Optional<GrievanceTicket> opt = ticketDao.findById(ticketId);
        if (opt.isEmpty()) return;
        GrievanceTicket ticket = opt.get();

        String title = "REOPENED: Fix Rejected on " + ticket.getTicketReference();
        String body = "The student rejected the fix and reopened the ticket. Reason: " +
                (studentReason != null ? studentReason : "Issue not resolved.");

        if (ticket.getAssignedStaffUserId() != null) {
            notificationDao.insert(new InAppNotification(
                    ticket.getAssignedStaffUserId(),
                    title,
                    body,
                    NotificationType.RESOLUTION_REJECTED,
                    ticketId
            ));
        }

        // Also notify the Department Head
        List<User> heads = userDao.findStaffByDepartment(ticket.getAssignedDeptId()).stream()
                .filter(u -> u.getRole() != null && u.getRole().name().equals("ROLE_DEPT_HEAD"))
                .toList();
        for (User head : heads) {
            notificationDao.insert(new InAppNotification(
                    head.getUserId(),
                    title,
                    body + " (Reopen count: " + (ticket.getReopenCount() + 1) + ")",
                    NotificationType.RESOLUTION_REJECTED,
                    ticketId
            ));
        }
    }

    public void notifyStaffAssignment(Long ticketId, Long staffUserId) {
        Optional<GrievanceTicket> opt = ticketDao.findById(ticketId);
        if (opt.isEmpty()) return;
        GrievanceTicket ticket = opt.get();

        String title = "New Ticket Assigned: " + ticket.getTicketReference();
        String body = "You have been assigned to handle grievance: " + ticket.getRawDescription();

        notificationDao.insert(new InAppNotification(
                staffUserId,
                title,
                body,
                NotificationType.ASSIGNMENT,
                ticketId
        ));
    }

    public void notifyEscalation(Long ticketId, int level, String escalatedToRole, String reason) {
        Optional<GrievanceTicket> opt = ticketDao.findById(ticketId);
        if (opt.isEmpty()) return;
        GrievanceTicket ticket = opt.get();

        String title = "CRITICAL SLA Escalation (Level " + level + "): " + ticket.getTicketReference();
        String body = "Ticket in department " + ticket.getDepartmentName() + " escalated to " + escalatedToRole + ". Reason: " + reason;

        if ("ROLE_DEPT_HEAD".equals(escalatedToRole)) {
            List<User> heads = userDao.findStaffByDepartment(ticket.getAssignedDeptId());
            for (User u : heads) {
                if (u.getRole() != null && "ROLE_DEPT_HEAD".equals(u.getRole().name())) {
                    notificationDao.insert(new InAppNotification(u.getUserId(), title, body, NotificationType.ESCALATION, ticketId));
                }
            }
        }
    }

    public List<InAppNotification> getNotificationsForUser(Long userId, boolean unreadOnly, int limit) {
        return notificationDao.findByRecipient(userId, unreadOnly, limit);
    }

    public long getUnreadCount(Long userId) {
        return notificationDao.countUnread(userId);
    }

    public void markAsRead(Long notificationId, Long userId) {
        notificationDao.markAsRead(notificationId, userId);
    }

    public void markAllAsRead(Long userId) {
        notificationDao.markAllAsRead(userId);
    }
}
