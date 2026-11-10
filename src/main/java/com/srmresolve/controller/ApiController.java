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

    /**
     * Periodic live polling endpoint for department case queue.
     * Returns total cases, active count, and ticket list for the department.
     */
    @GetMapping("/department/live-queue")
    public ResponseEntity<Map<String, Object>> getDepartmentLiveQueue(Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Integer deptId = currentUser.getAssignedDeptId() != null ? currentUser.getAssignedDeptId() : 1;
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

        // Student isolation check
        if (currentUser.getRole().name().equals("ROLE_STUDENT") && !ticket.getStudentUserId().equals(currentUser.getUserId())) {
            return ResponseEntity.status(403).build();
        }

        // Staff isolation check
        if (currentUser.getRole().name().equals("ROLE_DEPT_STAFF") && !ticket.getAssignedDeptId().equals(currentUser.getAssignedDeptId())) {
            return ResponseEntity.status(403).build();
        }

        List<TicketMessage> messages = lifecycleDao.findMessagesByTicketId(ticketId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Post a new message to a ticket conversation.
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

        Long messageId = lifecycleDao.insertMessage(ticketId, currentUser.getUserId(),
                currentUser.getRole().name(), request.getMessageBody(), false);

        return ResponseEntity.ok(Map.of("success", true, "messageId", messageId));
    }

    /**
     * Get attachments for a ticket.
     */
    @GetMapping("/tickets/{ticketId}/attachments")
    public ResponseEntity<List<com.srmresolve.model.TicketAttachment>> getAttachments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(lifecycleDao.findAttachmentsByTicketId(ticketId));
    }

    /**
     * Download or view an attachment.
     */
    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<org.springframework.core.io.Resource> getAttachment(@PathVariable Long attachmentId) {
        Optional<com.srmresolve.model.TicketAttachment> attOpt = lifecycleDao.findAttachmentById(attachmentId);
        if (attOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        com.srmresolve.model.TicketAttachment att = attOpt.get();
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
     */
    @PostMapping("/tickets/search")
    public ResponseEntity<List<GrievanceTicket>> searchTickets(
            @RequestBody com.srmresolve.dto.TicketSearchCriteria criteria,
            Authentication auth) {
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) return ResponseEntity.status(401).build();

        // Enforce RBAC constraints
        if (currentUser.getRole().name().equals("ROLE_STUDENT")) {
            criteria.setStudentUserId(currentUser.getUserId());
        } else if (currentUser.getRole().name().equals("ROLE_DEPT_STAFF")) {
            criteria.setDeptId(currentUser.getAssignedDeptId());
        }

        return ResponseEntity.ok(ticketDao.searchTickets(criteria));
    }
}

