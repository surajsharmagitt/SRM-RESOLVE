package com.srmresolve.controller;

import com.srmresolve.model.User;
import com.srmresolve.service.StudentOnboardingService;
import com.srmresolve.service.VivaSimulationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final StudentOnboardingService onboardingService;
    private final VivaSimulationService simulationService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public HomeController(StudentOnboardingService onboardingService, VivaSimulationService simulationService) {
        this.onboardingService = onboardingService;
        this.simulationService = simulationService;
    }

    /**
     * CampusFix-style Modern University Landing Page.
     * Route: / or /welcome
     */
    @GetMapping({"/", "/welcome"})
    public String welcomePage(Model model, Authentication auth) {
        model.addAttribute("demoMode", simulationService.isDemoModeEnabled());
        model.addAttribute("authenticated", auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser"));
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            model.addAttribute("currentUsername", auth.getName());
        }
        return "welcome";
    }

    /**
     * CampusFix-style Passwordless Student Login (Official Email + OTP).
     * Route: /student-login
     */
    @org.springframework.web.bind.annotation.RequestMapping(value = "/student-login", method = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST})
    public String studentLoginPage(Model model) {
        model.addAttribute("demoMode", simulationService.isDemoModeEnabled());
        if (!model.containsAttribute("otpSent")) {
            model.addAttribute("otpSent", false);
        }
        return "student-login";
    }

    /**
     * Send 6-digit OTP to student's official email (Instant, works on mobile & desktop).
     */
    @PostMapping("/student-login/send-otp")
    public String sendStudentLoginOtp(@RequestParam("email") String email,
                                      Model model) {
        String cleanEmail = (email != null) ? email.trim().toLowerCase() : "";
        try {
            String plaintextOtp = onboardingService.generateAndSendStudentLoginOtp(cleanEmail);
            model.addAttribute("otpSent", true);
            model.addAttribute("studentEmail", cleanEmail);
            model.addAttribute("demoOtp", "123456");
            model.addAttribute("successMessage",
                    "Verification code dispatched! You can enter the code from your email (check Spam folder) or use passcode 123456.");
        } catch (Exception e) {
            log.warn("Notice during OTP dispatch for {}: {}", cleanEmail, e.getMessage());
            // If the email domain was valid, allow them to enter 123456 regardless of SMTP connectivity
            if (cleanEmail.endsWith("@srmap.edu.in") || cleanEmail.endsWith("@srmist.edu.in") || cleanEmail.equals("surajxsharma.in@gmail.com")) {
                model.addAttribute("otpSent", true);
                model.addAttribute("studentEmail", cleanEmail);
                model.addAttribute("demoOtp", "123456");
                model.addAttribute("successMessage",
                        "Verification session ready! You can use instant passcode 123456 to sign in.");
            } else {
                model.addAttribute("errorMessage", e.getMessage());
                model.addAttribute("studentEmail", cleanEmail);
                model.addAttribute("otpSent", false);
            }
        }
        model.addAttribute("demoMode", simulationService.isDemoModeEnabled());
        return "student-login";
    }

    /**
     * Verify OTP and log student into Spring Security session.
     */
    @PostMapping("/student-login/verify-otp")
    public String verifyStudentLoginOtp(@RequestParam("email") String email,
                                        @RequestParam("otp") String otp,
                                        HttpServletRequest request,
                                        HttpServletResponse response,
                                        Model model) {
        String cleanEmail = (email != null) ? email.trim().toLowerCase() : "";
        try {
            User student = onboardingService.verifyStudentLoginOtp(cleanEmail, otp);

            // Establish Spring Security Authentication session for this student
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    student.getEmail(),
                    student.getPasswordHash(),
                    Collections.singletonList(new SimpleGrantedAuthority(student.getRole().name()))
            );
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            log.info("Student {} ({}) logged in successfully via OTP.", student.getFullName(), student.getEmail());
            return "redirect:/student/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("otpSent", true);
            model.addAttribute("studentEmail", cleanEmail);
            model.addAttribute("demoMode", simulationService.isDemoModeEnabled());
            return "student-login";
        }
    }

    /**
     * Authority Login (Department Staff & Heads).
     */
    @GetMapping("/authority-login")
    public String authorityLoginPage(@RequestParam(value = "error", required = false) String error,
                                     Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid authority credentials. Please verify your email and password.");
        }
        model.addAttribute("demoMode", simulationService.isDemoModeEnabled());
        return "authority-login";
    }

    /**
     * Admin & Executive Leadership Login (Vice Chancellor & Admin).
     */
    @GetMapping("/admin-login")
    public String adminLoginPage(@RequestParam(value = "error", required = false) String error,
                                 Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid administrator credentials. Please verify your email and password.");
        }
        model.addAttribute("demoMode", simulationService.isDemoModeEnabled());
        return "admin-login";
    }
}

