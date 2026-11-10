package com.srmresolve.controller;

import com.srmresolve.dao.DepartmentDao;
import com.srmresolve.dao.GrievanceTicketDao;
import com.srmresolve.dao.TicketLifecycleDao;
import com.srmresolve.dao.UserDao;
import com.srmresolve.dto.UpdateTicketStatusRequest;
import com.srmresolve.model.Department;
import com.srmresolve.model.GrievanceTicket;
import com.srmresolve.model.TicketMessage;
import com.srmresolve.model.User;
import com.srmresolve.service.ResolutionHandshakeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/department")
public class DepartmentController {

    private final UserDao userDao;
    private final DepartmentDao departmentDao;
    private final GrievanceTicketDao ticketDao;
    private final TicketLifecycleDao lifecycleDao;
    private final ResolutionHandshakeService handshakeService;

    public DepartmentController(UserDao userDao,
                                DepartmentDao departmentDao,
                                GrievanceTicketDao ticketDao,
                                TicketLifecycleDao lifecycleDao,
                                ResolutionHandshakeService handshakeService) {
        this.userDao = userDao;
        this.departmentDao = departmentDao;
        this.ticketDao = ticketDao;
        this.lifecycleDao = lifecycleDao;
        this.handshakeService = handshakeService;
    }

    @GetMapping("/hub")
    public String departmentHub(@RequestParam(value = "selectedTicketId", required = false) Long selectedTicketId,
                                Model model,
                                Authentication auth) {
        User staff = getAuthenticatedStaff(auth);
        model.addAttribute("staff", staff);

        Integer deptId = staff.getAssignedDeptId() != null ? staff.getAssignedDeptId() : 1;
        Optional<Department> department = departmentDao.findById(deptId);
        model.addAttribute("department", department.orElse(null));

        // Department tickets (strict RBAC: assigned_dept_id = ?)
        List<GrievanceTicket> tickets = ticketDao.findByDepartmentId(deptId);
        model.addAttribute("tickets", tickets);

        // Staff members for assignment dropdown
        List<User> staffMembers = userDao.findStaffByDepartment(deptId);
        model.addAttribute("staffMembers", staffMembers);

        // Selected ticket for the right split-pane
        GrievanceTicket currentTicket = null;
        if (selectedTicketId != null) {
            currentTicket = ticketDao.findById(selectedTicketId).orElse(null);
        } else if (!tickets.isEmpty()) {
            currentTicket = tickets.get(0);
        }

        model.addAttribute("selectedTicket", currentTicket);
        if (currentTicket != null) {
            List<TicketMessage> messages = lifecycleDao.findMessagesByTicketId(currentTicket.getTicketId());
            model.addAttribute("messages", messages);
            model.addAttribute("history", lifecycleDao.findHistoryByTicketId(currentTicket.getTicketId()));
            model.addAttribute("attachments", lifecycleDao.findAttachmentsByTicketId(currentTicket.getTicketId()));
        }

        return "department/whatsapp-hub";
    }

    @PostMapping("/tickets/{ticketId}/acknowledge")
    public String acknowledge(@PathVariable Long ticketId,
                              @RequestParam(value = "note", required = false) String note,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        User staff = getAuthenticatedStaff(auth);
        try {
            handshakeService.acknowledgeTicket(ticketId, staff.getUserId(), note);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket acknowledged successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/department/hub?selectedTicketId=" + ticketId;
    }

    @PostMapping("/tickets/{ticketId}/assign")
    public String assignStaff(@PathVariable Long ticketId,
                              @RequestParam("staffUserId") Long staffUserId,
                              RedirectAttributes redirectAttributes) {
        try {
            ticketDao.updateStaffAssignment(ticketId, staffUserId);
            redirectAttributes.addFlashAttribute("successMessage", "Technician assigned successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/department/hub?selectedTicketId=" + ticketId;
    }

    @PostMapping("/tickets/{ticketId}/assign-self")
    public String assignSelf(@PathVariable Long ticketId,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        User staff = getAuthenticatedStaff(auth);
        try {
            ticketDao.updateStaffAssignment(ticketId, staff.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Case assigned to yourself (" + staff.getFullName() + ").");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/department/hub?selectedTicketId=" + ticketId;
    }

    @PostMapping("/tickets/{ticketId}/eta")
    public String setEta(@PathVariable Long ticketId,
                         @RequestParam("eta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime eta,
                         @RequestParam(value = "note", required = false) String note,
                         Authentication auth,
                         RedirectAttributes redirectAttributes) {
        User staff = getAuthenticatedStaff(auth);
        try {
            handshakeService.setEtaAndProgress(ticketId, staff.getUserId(), eta, note);
            redirectAttributes.addFlashAttribute("successMessage", "ETA updated and status moved to IN_PROGRESS.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/department/hub?selectedTicketId=" + ticketId;
    }

    @PostMapping("/tickets/{ticketId}/resolve")
    public String markResolved(@PathVariable Long ticketId,
                               @RequestParam(value = "resolutionSummary", required = false) String resolutionSummary,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        User staff = getAuthenticatedStaff(auth);
        try {
            handshakeService.markResolvedByDept(ticketId, staff.getUserId(), resolutionSummary);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket marked resolved. Awaiting student confirmation.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/department/hub?selectedTicketId=" + ticketId;
    }

    @PostMapping("/tickets/{ticketId}/chat")
    public String postMessage(@PathVariable Long ticketId,
                             @RequestParam("messageBody") String messageBody,
                             Authentication auth) {
        User staff = getAuthenticatedStaff(auth);
        if (messageBody != null && !messageBody.isBlank()) {
            lifecycleDao.insertMessage(ticketId, staff.getUserId(), staff.getRole().name(), messageBody.trim(), false);
        }
        return "redirect:/department/hub?selectedTicketId=" + ticketId;
    }

    private User getAuthenticatedStaff(Authentication auth) {
        return userDao.findByEmail(auth.getName())
                .or(() -> userDao.findByRegOrStaffId(auth.getName()))
                .orElseThrow(() -> new IllegalStateException("Authenticated staff user not found: " + auth.getName()));
    }
}

