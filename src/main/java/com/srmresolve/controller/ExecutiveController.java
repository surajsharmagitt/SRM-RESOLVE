package com.srmresolve.controller;

import com.srmresolve.dao.ExecutiveAnalyticsDao;
import com.srmresolve.dao.GrievanceTicketDao;
import com.srmresolve.dao.UserDao;
import com.srmresolve.model.*;
import com.srmresolve.service.VivaSimulationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/executive")
public class ExecutiveController {

    private final UserDao userDao;
    private final GrievanceTicketDao ticketDao;
    private final ExecutiveAnalyticsDao analyticsDao;
    private final VivaSimulationService simulationService;

    public ExecutiveController(UserDao userDao,
                               GrievanceTicketDao ticketDao,
                               ExecutiveAnalyticsDao analyticsDao,
                               VivaSimulationService simulationService) {
        this.userDao = userDao;
        this.ticketDao = ticketDao;
        this.analyticsDao = analyticsDao;
        this.simulationService = simulationService;
    }

    @GetMapping("/dashboard")
    public String vcDashboard(Model model, Authentication auth) {
        User executive = userDao.findByEmail(auth.getName())
                .or(() -> userDao.findByRegOrStaffId(auth.getName()))
                .orElseThrow(() -> new IllegalStateException("Authenticated executive user not found: " + auth.getName()));
        model.addAttribute("executive", executive);

        // View 1: Category Anomaly Radar
        List<CategoryAnomalyRadar> anomalyRadar = analyticsDao.getMonthlyAnomalyRadar();
        model.addAttribute("anomalyRadar", anomalyRadar);

        // KPI Summary Counters
        long totalStudentReports = anomalyRadar.stream().mapToLong(CategoryAnomalyRadar::getTotalStudentReports).sum();
        long totalMasterIncidents = anomalyRadar.stream().mapToLong(CategoryAnomalyRadar::getGroupedMasterIncidents).sum();
        long totalIndependent = anomalyRadar.stream().mapToLong(CategoryAnomalyRadar::getIndependentIncidents).sum();
        long totalUnderlying = totalMasterIncidents + totalIndependent;
        long totalClosed = anomalyRadar.stream().mapToLong(CategoryAnomalyRadar::getClosedReports).sum();
        long totalEscalated = anomalyRadar.stream().mapToLong(CategoryAnomalyRadar::getEscalatedReports).sum();
        long breachedCategoriesCount = anomalyRadar.stream().filter(CategoryAnomalyRadar::isThresholdBreached).count();

        model.addAttribute("totalStudentReports", totalStudentReports);
        model.addAttribute("totalMasterIncidents", totalMasterIncidents);
        model.addAttribute("totalIndependent", totalIndependent);
        model.addAttribute("totalUnderlying", totalUnderlying);
        model.addAttribute("totalClosed", totalClosed);
        model.addAttribute("totalEscalated", totalEscalated);
        model.addAttribute("breachedCategoriesCount", breachedCategoriesCount);

        // View 2: Department SLA Performance
        List<DepartmentSlaPerformance> deptPerformance = analyticsDao.getDepartmentSlaPerformance();
        model.addAttribute("deptPerformance", deptPerformance);

        // View 3: Location Heatmap (Section 6.3)
        List<LocationCategoryHeatmap> locationHeatmap = analyticsDao.getLocationCategoryHeatmap();
        model.addAttribute("locationHeatmap", locationHeatmap);

        // Level 2 Escalated Tickets
        List<GrievanceTicket> level2Tickets = ticketDao.findAll(true).stream()
                .filter(t -> t.getEscalationLevel() >= 2)
                .collect(Collectors.toList());
        model.addAttribute("level2Tickets", level2Tickets);

        // Pending student verifications (Section 6.6)
        List<User> pendingUsers = userDao.findPendingVerifications();
        model.addAttribute("pendingUsers", pendingUsers);

        model.addAttribute("demoMode", simulationService.isDemoModeEnabled());
        return "executive/vc-dashboard";
    }

    @PostMapping("/users/{userId}/verify")
    public String approveUser(@PathVariable Long userId,
                              @RequestParam("status") VerificationStatus status,
                              RedirectAttributes redirectAttributes) {
        try {
            userDao.updateVerificationStatus(userId, status);
            redirectAttributes.addFlashAttribute("successMessage", "User verification status updated to " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/executive/dashboard";
    }
}

