package com.srmresolve.controller;

import com.srmresolve.dao.GrievanceTicketDao;
import com.srmresolve.dao.TicketLifecycleDao;
import com.srmresolve.dao.UserDao;
import com.srmresolve.dto.AddMessageRequest;
import com.srmresolve.dto.AiEnhanceRequest;
import com.srmresolve.dto.AiEnhanceResponse;
import com.srmresolve.dto.IssuePredictionRequest;
import com.srmresolve.dto.IssuePredictionResult;
import com.srmresolve.model.GrievanceTicket;
import com.srmresolve.model.TicketMessage;
import com.srmresolve.model.User;
import com.srmresolve.service.AiComplaintEnhancerService;
import com.srmresolve.service.CampusIntelligenceEngine;
import com.srmresolve.service.VivaSimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final GrievanceTicketDao ticketDao;
    private final TicketLifecycleDao lifecycleDao;
    private final UserDao userDao;
    private final AiComplaintEnhancerService aiEnhancerService;
    private final CampusIntelligenceEngine intelligenceEngine;
    private final VivaSimulationService simulationService;
    private final com.srmresolve.service.NotificationService notificationService;

    public ApiController(GrievanceTicketDao ticketDao,
                         TicketLifecycleDao lifecycleDao,
                         UserDao userDao,
                         AiComplaintEnhancerService aiEnhancerService,
                         CampusIntelligenceEngine intelligenceEngine,
                         VivaSimulationService simulationService,
                         com.srmresolve.service.NotificationService notificationService) {
        this.ticketDao = ticketDao;
        this.lifecycleDao = lifecycleDao;
        this.userDao = userDao;
        this.aiEnhancerService = aiEnhancerService;
        this.intelligenceEngine = intelligenceEngine;
        this.simulationService = simulationService;
        this.notificationService = notificationService;
    }

    private User resolveCurrentUser(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userDao.findByEmail(auth.getName())
                .or(() -> userDao.findByRegOrStaffId(auth.getName()))
                .orElse(null);
    }

    private boolean isAuthorizedForTicket(User user, GrievanceTicket ticket) {
        if (user == null || ticket == null) return false;
        String role = user.getRole().name();
        if ("ROLE_ADMIN".equals(role) || "ROLE_EXECUTIVE".equals(role)) {
            return true;
        }
        if ("ROLE_STUDENT".equals(role)) {
            return ticket.getStudentUserId().equals(user.getUserId());
        }
        if ("ROLE_DEPT_STAFF".equals(role) || "ROLE_DEPT_HEAD".equals(role)) {
            return user.getAssignedDeptId() != null && user.getAssignedDeptId().equals(ticket.getAssignedDeptId());
        }
        return false;
    }

    /**
     * Periodic live polling endpoint for department case queue.
     * Returns total cases, active count, and ticket list for the department.
     * Access restricted to department staff and administrators.
     */
    @GetMapping("/department/live-queue")
    public ResponseEntity<Map<String, Object>> getDepartmentLiveQueue(Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        // Students cannot access department queues
        if (currentUser.getRole().name().equals("ROLE_STUDENT")) {
            return ResponseEntity.status(403).build();
        }

        Integer deptId = currentUser.getAssignedDeptId();
        if (deptId == null) {
            if ("ROLE_ADMIN".equals(currentUser.getRole().name()) || "ROLE_EXECUTIVE".equals(currentUser.getRole().name())) {
                deptId = 1;
            } else {
                return ResponseEntity.status(403).build();
            }
        }
        List<GrievanceTicket> tickets = ticketDao.findByDepartmentId(deptId);

        long activeCount = tickets.stream().filter(t -> !"CLOSED".equals(t.getStatus().name())).count();

        return ResponseEntity.ok(Map.of(
                "deptId", deptId,
                "totalCount", tickets.size(),
                "activeCount", activeCount,
                "tickets", tickets
        ));
    }

    /**
     * Periodic live polling endpoint for student's own tickets.
     */
    @GetMapping("/student/live-tickets")
    public ResponseEntity<Map<String, Object>> getStudentLiveTickets(Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null || !currentUser.getRole().name().equals("ROLE_STUDENT")) {
            return ResponseEntity.status(401).build();
        }

        List<GrievanceTicket> tickets = ticketDao.findByStudentUserId(currentUser.getUserId());
        return ResponseEntity.ok(Map.of(
                "totalCount", tickets.size(),
                "tickets", tickets
        ));
    }

    /**
     * Live debounced natural language issue prediction endpoint.
     * Takes unstructured complaint text and predicts department, category, priority, emergency flag,
     * building entity, and SLA without student manual selection.
     */
    @PostMapping("/predict-issue")
    public ResponseEntity<IssuePredictionResult> predictIssue(
            @RequestBody IssuePredictionRequest request,
            Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        String text = (request != null && request.getDescription() != null) ? request.getDescription() : "";
        IssuePredictionResult result = intelligenceEngine.predictIssue(text, currentUser);
        return ResponseEntity.ok(result);
    }

    /**
     * Periodic 5-second polling endpoint for ticket conversation messages (Section 6.5).
     * Enforces strict authorization: students can only access their own tickets; staff can only access department tickets.
     */
    @GetMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<List<TicketMessage>> getTicketMessages(@PathVariable Long ticketId, Authentication auth) {
        Optional<GrievanceTicket> ticketOpt = ticketDao.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GrievanceTicket ticket = ticketOpt.get();
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!isAuthorizedForTicket(currentUser, ticket)) {
            return ResponseEntity.status(403).build();
        }

        List<TicketMessage> messages = lifecycleDao.findMessagesByTicketId(ticketId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Post a new message to a ticket conversation.
     * Strictly verifies participant authorization on the server.
     */
    @PostMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<Map<String, Object>> postMessage(@PathVariable Long ticketId,
                                                          @RequestBody AddMessageRequest request,
                                                          Authentication auth) {
        Optional<GrievanceTicket> ticketOpt = ticketDao.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        GrievanceTicket ticket = ticketOpt.get();
        if (!isAuthorizedForTicket(currentUser, ticket)) {
            return ResponseEntity.status(403).build();
        }

        Long messageId = lifecycleDao.insertMessage(ticketId, currentUser.getUserId(),
                currentUser.getRole().name(), request.getMessageBody(), false);

        return ResponseEntity.ok(Map.of("success", true, "messageId", messageId));
    }

    /**
     * Get attachments for a ticket.
     * Strictly enforces ticket access authorization before returning attachment records.
     */
    @GetMapping("/tickets/{ticketId}/attachments")
    public ResponseEntity<List<com.srmresolve.model.TicketAttachment>> getAttachments(@PathVariable Long ticketId, Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<GrievanceTicket> ticketOpt = ticketDao.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GrievanceTicket ticket = ticketOpt.get();
        if (!isAuthorizedForTicket(currentUser, ticket)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(lifecycleDao.findAttachmentsByTicketId(ticketId));
    }

    /**
     * Download or view an attachment.
     * Strictly verifies user authorization for the owning complaint before serving files.
     */
    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<org.springframework.core.io.Resource> getAttachment(@PathVariable Long attachmentId, Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<com.srmresolve.model.TicketAttachment> attOpt = lifecycleDao.findAttachmentById(attachmentId);
        if (attOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        com.srmresolve.model.TicketAttachment att = attOpt.get();

        Optional<GrievanceTicket> ticketOpt = ticketDao.findById(att.getTicketId());
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        GrievanceTicket ticket = ticketOpt.get();

        if (!isAuthorizedForTicket(currentUser, ticket)) {
            return ResponseEntity.status(403).build();
        }

        java.nio.file.Path path = java.nio.file.Paths.get(att.getStoragePath());
        if (!java.nio.file.Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(path);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(att.getContentType()))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + att.getOriginalFileName() + "\"")
                .body(resource);
    }

    /**
     * Non-hallucinating AI enhancer endpoint.
     */
    @PostMapping("/enhance-ai")
    public ResponseEntity<AiEnhanceResponse> enhanceWithAi(@RequestBody AiEnhanceRequest request) {
        AiEnhanceResponse response = aiEnhancerService.enhanceComplaint(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Viva Simulator: 100-complaint spike.
     */
    @PostMapping("/simulator/spike")
    public ResponseEntity<Map<String, Object>> triggerSpike(Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        Long actorId = currentUser != null ? currentUser.getUserId() : 1L;

        simulationService.simulate100ComplaintSpike(actorId);
        return ResponseEntity.ok(Map.of("success", true, "message", "100 synthetic complaints simulated successfully."));
    }

    /**
     * Viva Simulator: FK-Safe Reset.
     */
    @PostMapping("/simulator/reset")
    public ResponseEntity<Map<String, Object>> resetSimulator(Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        Long actorId = currentUser != null ? currentUser.getUserId() : 1L;

        simulationService.resetDemoData(actorId);
        return ResponseEntity.ok(Map.of("success", true, "message", "All synthetic demo data reset cleanly in child-first order."));
    }

    /**
     * In-app notifications list endpoint.
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<com.srmresolve.model.InAppNotification>> getNotifications(
            @RequestParam(value = "unreadOnly", defaultValue = "false") boolean unreadOnly,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(notificationService.getNotificationsForUser(currentUser.getUserId(), unreadOnly, limit));
    }

    /**
     * In-app notifications unread count endpoint.
     */
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount(Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(currentUser.getUserId())));
    }

    /**
     * Mark notification as read.
     */
    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<Map<String, Object>> markNotificationRead(@PathVariable Long id, Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) return ResponseEntity.status(401).build();

        notificationService.markAsRead(id, currentUser.getUserId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Mark all notifications as read.
     */
    @PostMapping("/notifications/read-all")
    public ResponseEntity<Map<String, Object>> markAllNotificationsRead(Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) return ResponseEntity.status(401).build();

        notificationService.markAllAsRead(currentUser.getUserId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Dynamic parameterized ticket search endpoint.
     * Enforces strict server-side RBAC:
     * - Students can search ONLY their own tickets (client studentUserId/deptId overwritten or ignored).
     * - Department staff and department heads can search ONLY their assigned department's tickets.
     * - Admins and executives retain campus-wide search capability.
     */
    @PostMapping("/tickets/search")
    public ResponseEntity<List<GrievanceTicket>> searchTickets(
            @RequestBody(required = false) com.srmresolve.dto.TicketSearchCriteria criteria,
            Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) return ResponseEntity.status(401).build();

        if (criteria == null) {
            criteria = new com.srmresolve.dto.TicketSearchCriteria();
        }

        String role = currentUser.getRole().name();
        if ("ROLE_STUDENT".equals(role)) {
            // Students can only search their own tickets; never trust client-submitted studentUserId or deptId
            criteria.setStudentUserId(currentUser.getUserId());
            criteria.setDeptId(null);
        } else if ("ROLE_DEPT_STAFF".equals(role) || "ROLE_DEPT_HEAD".equals(role)) {
            // Department staff and heads can only search their assigned department's tickets
            if (currentUser.getAssignedDeptId() == null) {
                return ResponseEntity.status(403).build();
            }
            criteria.setDeptId(currentUser.getAssignedDeptId());
            criteria.setStudentUserId(null); // Never trust client-submitted studentUserId for staff/head searches
        } else if (!"ROLE_ADMIN".equals(role) && !"ROLE_EXECUTIVE".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(ticketDao.searchTickets(criteria));
    }
}

