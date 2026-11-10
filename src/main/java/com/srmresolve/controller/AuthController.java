package com.srmresolve.controller;

import com.srmresolve.dto.RegistrationRequest;
import com.srmresolve.model.OtpPurpose;
import com.srmresolve.model.ResidenceType;
import com.srmresolve.model.User;
import com.srmresolve.model.VerificationStatus;
import com.srmresolve.service.StudentOnboardingService;
import com.srmresolve.service.VivaSimulationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final StudentOnboardingService onboardingService;
    private final VivaSimulationService simulationService;

    public AuthController(StudentOnboardingService onboardingService, VivaSimulationService simulationService) {
        this.onboardingService = onboardingService;
        this.simulationService = simulationService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid registration number, email, or password.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }

        model.addAttribute("demoMode", simulationService.isDemoModeEnabled());
        model.addAttribute("residenceTypes", ResidenceType.values());
        if (!model.containsAttribute("otpSent")) {
            model.addAttribute("otpSent", false);
        }
        return "auth/login-register";
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("regNo") String regNo,
                          @RequestParam("email") String email,
                          RedirectAttributes redirectAttributes) {
        try {
            String plaintextOtp = onboardingService.generateAndSendOtp(regNo, email, OtpPurpose.REGISTRATION);
            redirectAttributes.addFlashAttribute("otpSent", true);
            redirectAttributes.addFlashAttribute("pendingRegNo", regNo);
            redirectAttributes.addFlashAttribute("pendingEmail", email);
            redirectAttributes.addFlashAttribute("demoOtp", plaintextOtp);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Verification code generated! [Demo/Dev Mode OTP: " + plaintextOtp + "]. Real email dispatch to " + email + " was also attempted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("pendingRegNo", regNo);
            redirectAttributes.addFlashAttribute("pendingEmail", email);
        }
        return "redirect:/auth/login#register";
    }

    @PostMapping("/register")
    public String completeRegistration(@ModelAttribute RegistrationRequest request,
                                       @RequestParam("otp") String otp,
                                       RedirectAttributes redirectAttributes) {
        try {
            User newUser = onboardingService.completeStudentRegistration(request, otp);
            if (newUser.getVerificationStatus() == VerificationStatus.VERIFIED) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Registration successful! Your institutional credentials match the student directory. Please log in.");
            } else {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Registration submitted! Your profile is in PENDING_VERIFICATION awaiting administrative approval.");
            }
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("otpSent", true);
            redirectAttributes.addFlashAttribute("pendingRegNo", request.getRegNo());
            redirectAttributes.addFlashAttribute("pendingEmail", request.getEmail());
            redirectAttributes.addFlashAttribute("pendingFullName", request.getFullName());
            return "redirect:/auth/login#register";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("demoMode", simulationService.isDemoModeEnabled());
        return "auth/access-denied";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("demoMode", simulationService.isDemoModeEnabled());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@RequestParam("regNoOrEmail") String regNoOrEmail,
                                       RedirectAttributes redirectAttributes) {
        try {
            String otp = onboardingService.generateAndSendPasswordResetOtp(regNoOrEmail);
            redirectAttributes.addFlashAttribute("otpSent", true);
            redirectAttributes.addFlashAttribute("regNoOrEmail", regNoOrEmail);
            redirectAttributes.addFlashAttribute("demoOtp", otp);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Password reset code generated! [Demo/Dev Mode OTP: " + otp + "].");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/auth/forgot-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("regNoOrEmail") String regNoOrEmail,
                                @RequestParam("otp") String otp,
                                @RequestParam("newPassword") String newPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            onboardingService.resetPasswordWithOtp(regNoOrEmail, otp, newPassword);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Password reset successfully! Please log in with your new password.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("otpSent", true);
            redirectAttributes.addFlashAttribute("regNoOrEmail", regNoOrEmail);
            return "redirect:/auth/forgot-password";
        }
    }
}
