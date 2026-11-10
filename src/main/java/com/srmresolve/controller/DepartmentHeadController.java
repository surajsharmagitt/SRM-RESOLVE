package com.srmresolve.controller;

import com.srmresolve.dao.DepartmentDao;
import com.srmresolve.dao.ExecutiveAnalyticsDao;
import com.srmresolve.dao.GrievanceTicketDao;
import com.srmresolve.dao.UserDao;
import com.srmresolve.model.Department;
import com.srmresolve.model.DepartmentSlaPerformance;
import com.srmresolve.model.GrievanceTicket;
import com.srmresolve.model.TicketStatus;
import com.srmresolve.model.User;
import com.srmresolve.service.AuditLogService;
import com.srmresolve.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/department/head")
public class DepartmentHeadController {

    private final UserDao userDao;
    private final DepartmentDao departmentDao;
    private final GrievanceTicketDao ticketDao;
    private final ExecutiveAnalyticsDao analyticsDao;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    @Autowired
    public DepartmentHeadController(UserDao userDao,
                                  DepartmentDao departmentDao,
                                  GrievanceTicketDao ticketDao,
                                  ExecutiveAnalyticsDao analyticsDao,
                                  AuditLogService auditLogService,
                                  NotificationService notificationService) {
        this.userDao = userDao;
        this.departmentDao = departmentDao;
        this.ticketDao = ticketDao;
        this.analyticsDao = analyticsDao;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    public DepartmentHeadController(UserDao userDao,
                                  DepartmentDao departmentDao,
                                  GrievanceTicketDao ticketDao,
                                  ExecutiveAnalyticsDao analyticsDao) {
        this(userDao, departmentDao, ticketDao, analyticsDao, null, null);
    }

    private User getAuthenticatedHead(Authentication auth) {
        return userDao.findByEmail(auth.getName())
                .or(() -> userDao.findByRegOrStaffId(auth.getName()))
                .orElseThrow(() -> new IllegalStateException("Authenticated department head not found: " + auth.getName()));
    }

    @GetMapping
    public String headDashboard(Model model, Authentication auth) {
        User head = getAuthenticatedHead(auth);
        model.addAttribute("head", head);

        Integer deptId = head.getAssignedDeptId() != null ? head.getAssignedDeptId() : 1;
        Optional<Department> department = departmentDao.findById(deptId);
        model.addAttribute("department", department.orElse(null));

        // Department SLA performance metrics from View 2
        List<DepartmentSlaPerformance> allPerf = analyticsDao.getDepartmentSlaPerformance();
        DepartmentSlaPerformance myPerf = allPerf.stream()
                .filter(p -> p.getDeptId().equals(deptId))
                .findFirst()
                .orElse(new DepartmentSlaPerformance());
        model.addAttribute("slaMetrics", myPerf);

        // All department tickets
        List<GrievanceTicket> deptTickets = ticketDao.findByDepartmentId(deptId);
        model.addAttribute("tickets", deptTickets);

        // Level 1 Escalated tickets in this department
        List<GrievanceTicket> escalatedTickets = deptTickets.stream()
                .filter(t -> t.isEscalated() && t.getEscalationLevel() >= 1)
                .collect(Collectors.toList());
        model.addAttribute("escalatedTickets", escalatedTickets);

        // Reopened tickets requiring head review
        List<GrievanceTicket> reopenedTickets = deptTickets.stream()
                .filter(t -> t.getReopenCount() > 0)
                .collect(Collectors.toList());
        model.addAttribute("reopenedTickets", reopenedTickets);

        // Department staff members
        List<User> staffMembers = userDao.findStaffByDepartment(deptId);
        model.addAttribute("staffMembers", staffMembers);

        return "department/head-dashboard";
    }

    @PostMapping("/bulk-assign")
    public String bulkAssignStaff(@RequestParam("ticketIds") List<Long> ticketIds,
                                  @RequestParam("staffUserId") Long staffUserId,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        User head = getAuthenticatedHead(auth);
        try {
            int updated = ticketDao.bulkAssignStaff(ticketIds, staffUserId);
            for (Long tId : ticketIds) {
                if (notificationService != null) {
                    notificationService.notifyStaffAssignment(tId, staffUserId);
                }
                if (auditLogService != null) {
                    auditLogService.logAction(head.getUserId(), "BULK_ASSIGN", "GRIEVANCE_TICKET",
                            String.valueOf(tId), "Assigned to staff user #" + staffUserId);
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", "Successfully assigned " + updated + " ticket(s) to technician.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bulk assignment failed: " + e.getMessage());
        }
        return "redirect:/department/head";
    }

    @PostMapping("/bulk-acknowledge")
    public String bulkAcknowledge(@RequestParam("ticketIds") List<Long> ticketIds,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        User head = getAuthenticatedHead(auth);
        try {
            int updated = ticketDao.bulkAcknowledge(ticketIds, head.getUserId());
            for (Long tId : ticketIds) {
                if (notificationService != null) {
                    notificationService.notifyStudentTicketStatusChange(tId, TicketStatus.ACKNOWLEDGED, "Bulk acknowledged by Department Head.");
                }
                if (auditLogService != null) {
                    auditLogService.logAction(head.getUserId(), "BULK_ACKNOWLEDGE", "GRIEVANCE_TICKET",
                            String.valueOf(tId), "Acknowledged by Department Head");
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", "Successfully acknowledged " + updated + " ticket(s).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bulk acknowledgment failed: " + e.getMessage());
        }
        return "redirect:/department/head";
    }
}
