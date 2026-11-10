package com.srmresolve.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom Error Controller ensuring no user ever sees Spring Boot's default Whitelabel Error Page.
 * Gracefully routes 403 Forbidden, 404 Not Found, and 500 Internal errors with institutional SRM-AP branding.
 */
@Controller
public class AppErrorController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(AppErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Authentication auth, Model model) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = 500;
        if (statusObj != null) {
            try {
                statusCode = Integer.parseInt(statusObj.toString());
            } catch (NumberFormatException ignored) {}
        }

        // Also support query param ?status=403
        String queryStatus = request.getParameter("status");
        if (queryStatus != null) {
            try {
                statusCode = Integer.parseInt(queryStatus);
            } catch (NumberFormatException ignored) {}
        }

        log.warn("Handling HTTP {} error for URI: {}", statusCode, request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));

        model.addAttribute("statusCode", statusCode);

        if (statusCode == 403) {
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                String role = auth.getAuthorities().stream()
                        .findFirst()
                        .map(a -> a.getAuthority())
                        .orElse("UNKNOWN");

                String dashboardUrl = "/welcome";
                String dashboardLabel = "Campus Home";

                if ("ROLE_STUDENT".equals(role)) {
                    dashboardUrl = "/student/dashboard";
                    dashboardLabel = "Student Grievance Dashboard";
                } else if ("ROLE_DEPT_STAFF".equals(role)) {
                    dashboardUrl = "/department/hub";
                    dashboardLabel = "Department Hub";
                } else if ("ROLE_DEPT_HEAD".equals(role)) {
                    dashboardUrl = "/department/head";
                    dashboardLabel = "SLA Operations Dashboard";
                } else if ("ROLE_EXECUTIVE".equals(role) || "ROLE_ADMIN".equals(role)) {
                    dashboardUrl = "/executive/dashboard";
                    dashboardLabel = "Executive Leadership Anomaly Radar";
                }

                model.addAttribute("currentUsername", auth.getName());
                model.addAttribute("currentUserRole", role);
                model.addAttribute("dashboardUrl", dashboardUrl);
                model.addAttribute("dashboardLabel", dashboardLabel);
            }
            return "auth/access-denied";
        }

        if (statusCode == 404) {
            model.addAttribute("errorMessage", "The page or resource you requested could not be found on the SRM-Resolve portal.");
            return "auth/access-denied";
        }

        model.addAttribute("errorMessage", "An unexpected system exception occurred. The SRM-AP technical helpdesk has been notified.");
        return "auth/access-denied";
    }
}

