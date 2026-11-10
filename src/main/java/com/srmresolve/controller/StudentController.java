package com.srmresolve.controller;

import com.srmresolve.dao.*;
import com.srmresolve.dto.CreateTicketRequest;
import com.srmresolve.model.*;
import com.srmresolve.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/student")
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    private final UserDao userDao;
    private final GrievanceTicketDao ticketDao;
    private final CategoryRoutingSlaDao routingSlaDao;
    private final MasterIncidentDao masterIncidentDao;
    private final TicketLifecycleDao lifecycleDao;
    private final IncidentGroupingEngine groupingEngine;
    private final ResolutionHandshakeService handshakeService;
    private final TicketPriorityService priorityService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final StudentDirectoryDao studentDirectoryDao;
    private final CampusIntelligenceEngine intelligenceEngine;
    private final com.srmresolve.validator.SrmRegistrationValidator srmValidator;

    @Value("${app.upload.dir:${user.home}/srm-resolve-uploads}")
    private String uploadDir;

    public StudentController(UserDao userDao,
                             GrievanceTicketDao ticketDao,
                             CategoryRoutingSlaDao routingSlaDao,
                             MasterIncidentDao masterIncidentDao,
                             TicketLifecycleDao lifecycleDao,
                             IncidentGroupingEngine groupingEngine,
                             ResolutionHandshakeService handshakeService,
                             TicketPriorityService priorityService,
                             AuditLogService auditLogService,
                             NotificationService notificationService,
                             StudentDirectoryDao studentDirectoryDao,
                             CampusIntelligenceEngine intelligenceEngine,
                             com.srmresolve.validator.SrmRegistrationValidator srmValidator) {
        this.userDao = userDao;
        this.ticketDao = ticketDao;
        this.routingSlaDao = routingSlaDao;
        this.masterIncidentDao = masterIncidentDao;
        this.lifecycleDao = lifecycleDao;
        this.groupingEngine = groupingEngine;
        this.handshakeService = handshakeService;
        this.priorityService = priorityService;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.studentDirectoryDao = studentDirectoryDao;
        this.intelligenceEngine = intelligenceEngine;
        this.srmValidator = srmValidator;
    }

    @GetMapping("/dashboard")
    public String studentDashboard(Model model, Authentication auth) {
        User student = getAuthenticatedStudent(auth);
        model.addAttribute("student", student);

        // Check if student has not completed their profile yet (first-time login)
        boolean profileNeedsCompletion = student.getBuildingBlock() == null 
                || student.getBuildingBlock().isBlank()
                || student.getRoomOrZone() == null 
                || student.getRoomOrZone().isBlank()
                || student.getRegOrStaffId() == null 
                || student.getRegOrStaffId().isBlank()
                || student.getRegOrStaffId().startsWith("TEMP_")
                || student.getRegOrStaffId().startsWith("AP_EMAIL");

        if (profileNeedsCompletion) {
            log.info("First-time student login detected for {}. Redirecting to profile onboarding.", student.getEmail());
            return "redirect:/student/complete-profile";
        }

        // Section 6.6 Guard: Students with PENDING_VERIFICATION cannot file complaints
        if (student.getVerificationStatus() == VerificationStatus.PENDING_VERIFICATION) {
            model.addAttribute("isPendingVerification", true);
            return "student/pending-verification";
        }

        // Active block incident banner (sanitized, zero other student names/rooms)
        String building = student.getBuildingBlock() != null ? student.getBuildingBlock() : "Ganga Tower";
        List<MasterIncident> activeBanners = masterIncidentDao.findActiveByBuildingBlock(building);
        model.addAttribute("activeBanners", activeBanners);

        // Categories for quick-tap chips
        List<GrievanceCategory> categories = routingSlaDao.findAllCategories();
        model.addAttribute("categories", categories);

        // Student's own tickets (strict RBAC isolation)
        List<GrievanceTicket> myTickets = ticketDao.findByStudentUserId(student.getUserId());
        model.addAttribute("tickets", myTickets);

        model.addAttribute("locationScopes", LocationScope.values());
        return "student/dashboard";
    }

    @GetMapping({"/complete-profile", "/profile"})
    public String completeProfilePage(Model model, Authentication auth) {
        User student = getAuthenticatedStudent(auth);

        // Try pre-filling from institutional directory if student fields are blank
        if (student.getEmail() != null) {
            Optional<StudentDirectory> dirOpt = studentDirectoryDao.findByOfficialEmail(student.getEmail());
            if (dirOpt.isPresent()) {
                StudentDirectory dir = dirOpt.get();
                if (student.getRegOrStaffId() == null || student.getRegOrStaffId().startsWith("AP_EMAIL") || student.getRegOrStaffId().startsWith("TEMP_")) {
                    student.setRegOrStaffId(dir.getRegNo());
                }
                if (student.getFullName() == null || student.getFullName().startsWith("New Student (")) {
                    student.setFullName(dir.getFullName());
                }
                if (student.getDepartmentCode() == null) {
                    student.setDepartmentCode(dir.getDepartmentCode());
                }
                if (student.getStudyYear() == null) {
                    student.setStudyYear(dir.getCurrentStudyYear());
                }
                if (student.getSection() == null) {
                    student.setSection(dir.getSection());
                }
            }
        }

        if (student.getRegOrStaffId() != null && (student.getRegOrStaffId().startsWith("TEMP_") || student.getRegOrStaffId().startsWith("AP_EMAIL"))) {
            student.setRegOrStaffId("");
        }
        if (student.getFullName() != null && student.getFullName().startsWith("New Student (")) {
            student.setFullName("");
        }

        model.addAttribute("student", student);
        model.addAttribute("residenceTypes", ResidenceType.values());
        return "student/complete-profile";
    }

    @PostMapping({"/complete-profile", "/profile"})
    public String saveStudentProfile(@RequestParam("regNo") String regNo,
                                     @RequestParam("fullName") String fullName,
                                     @RequestParam("departmentCode") String departmentCode,
                                     @RequestParam("studyYear") Integer studyYear,
                                     @RequestParam("section") String section,
                                     @RequestParam("residenceType") ResidenceType residenceType,
                                     @RequestParam(value = "buildingBlock", required = false) String buildingBlock,
                                     @RequestParam(value = "roomOrZone", required = false) String roomOrZone,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        User student = getAuthenticatedStudent(auth);

        // Validate SRM Registration Number format
        String cleanReg = regNo.trim().toUpperCase();
        if (!srmValidator.isValidFlexibleCampusId(cleanReg)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Invalid registration number format. Please enter a valid institutional ID (e.g. AP25110070893).");
            return "redirect:/student/complete-profile";
        }

        Optional<User> existingWithReg = userDao.findByRegOrStaffId(cleanReg);
        if (existingWithReg.isPresent() && !existingWithReg.get().getUserId().equals(student.getUserId())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Registration number " + cleanReg + " is already associated with another student account.");
            return "redirect:/student/complete-profile";
        }

        try {
            userDao.updateStudentProfile(student.getUserId(), cleanReg, fullName.trim(),
                    departmentCode.trim().toUpperCase(), studyYear, section.trim().toUpperCase(),
                    residenceType, buildingBlock, roomOrZone);

            if (auditLogService != null) {
                auditLogService.logAction(student.getUserId(), "PROFILE_UPDATED", "USER",
                        String.valueOf(student.getUserId()), "Updated student profile details: " + cleanReg);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Profile details saved successfully!");
            return "redirect:/student/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
            return "redirect:/student/complete-profile";
        }
    }

    @PostMapping("/tickets")
    public String fileTicket(@ModelAttribute CreateTicketRequest request,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        User student = getAuthenticatedStudent(auth);

        if (student.getVerificationStatus() != VerificationStatus.VERIFIED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only verified accounts can file grievances.");
            return "redirect:/student/dashboard";
        }

        try {
            // Predict category, department, emergency status, and location via Campus Intelligence Engine
            com.srmresolve.dto.IssuePredictionResult prediction = intelligenceEngine.predictIssue(request.getRawDescription(), student);

            Integer categoryId = request.getCategoryId();
            if (categoryId == null || categoryId <= 0) {
                categoryId = prediction.getCategoryId();
            }

            GrievanceCategory category = routingSlaDao.findCategoryById(categoryId)
                    .orElseGet(() -> {
                        GrievanceCategory fallback = new GrievanceCategory();
                        fallback.setCategoryId(17);
                        fallback.setCategoryCode("GENERAL_CAMPUS_ISSUE");
                        fallback.setDisplayName("General Campus Infrastructure Concern");
                        fallback.setDefaultPriority(Priority.MEDIUM);
                        return fallback;
                    });

            // 1. Resolve Target Department via routing rules or intelligence engine prediction
            Integer targetDeptId = null;
            if (request.getLocationScope() != null) {
                targetDeptId = routingSlaDao.resolveTargetDepartment(category.getCategoryId(), request.getLocationScope()).orElse(null);
            }
            if (targetDeptId == null) {
                targetDeptId = prediction.getTargetDeptId();
            }
            if (targetDeptId == null) {
                targetDeptId = 1; // Default to Hostel Maintenance
            }

            // 2. Resolve Scope and Locations with automatic entity fallback
            LocationScope locationScope = request.getLocationScope() != null ? request.getLocationScope() : prediction.getLocationScope();
            if (locationScope == null) locationScope = LocationScope.HOSTEL;

            String buildingBlock = (request.getBuildingBlock() != null && !request.getBuildingBlock().isBlank())
                    ? request.getBuildingBlock()
                    : prediction.getBuildingBlock();

            String roomOrLocation = (request.getRoomOrLocation() != null && !request.getRoomOrLocation().isBlank())
                    ? request.getRoomOrLocation()
                    : prediction.getRoomOrLocation();

            // 3. Resolve SLA Deadlines with Intelligent Priority Engine
            Priority priority;
            if (prediction.isEmergency()) {
                priority = Priority.CRITICAL;
            } else if (priorityService != null) {
                priority = priorityService.determineInitialPriority(category.getCategoryId(), buildingBlock, category.getDefaultPriority());
            } else {
                priority = prediction.getPriority() != null ? prediction.getPriority() : category.getDefaultPriority();
            }

            Optional<SlaPolicy> slaPolicy = routingSlaDao.findSlaPolicy(category.getCategoryId(), priority);
            int ackMinutes = prediction.getAckMinutes() > 0 ? prediction.getAckMinutes() : slaPolicy.map(SlaPolicy::getAckDeadlineMinutes).orElse(120);
            int resMinutes = prediction.getResolutionMinutes() > 0 ? prediction.getResolutionMinutes() : slaPolicy.map(SlaPolicy::getResolutionDeadlineMinutes).orElse(1440);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime ackDeadline = now.plusMinutes(ackMinutes);
            LocalDateTime resDeadline = now.plusMinutes(resMinutes);

            // 4. Generate Ticket Reference (e.g. SRM-2026-000006)
            long seq = ticketDao.getNextTicketSequence();
            String ticketRef = String.format("SRM-2026-%06d", seq);

            GrievanceTicket ticket = new GrievanceTicket();
            ticket.setTicketReference(ticketRef);
            ticket.setStudentUserId(student.getUserId());
            ticket.setCategoryId(category.getCategoryId());
            ticket.setAssignedDeptId(targetDeptId);
            ticket.setLocationScope(locationScope);
            ticket.setBuildingBlock(buildingBlock);
            ticket.setRoomOrLocation(roomOrLocation);
            ticket.setRawDescription(request.getRawDescription());
            ticket.setAiEnhancedDescription(request.getAiEnhancedDescription());
            ticket.setPriority(priority);
            ticket.setStatus(TicketStatus.SUBMITTED);
            ticket.setEscalated(false);
            ticket.setEscalationLevel(0);
            ticket.setAckDeadlineAt(ackDeadline);
            ticket.setResolutionDeadlineAt(resDeadline);
            ticket.setSyntheticDemo(false);
            ticket.setMasterIncidentId(request.getLinkedMasterIncidentId());

            // 5. Jaccard Incident Grouping Engine (Section 4.A & Section 6.1)
            Long resolvedMasterId = groupingEngine.resolveGrouping(ticket, category);
            ticket.setMasterIncidentId(resolvedMasterId);

            Long ticketId = ticketDao.insert(ticket);

            // 6. Update distinct student count on master incident if linked (Section 6.2)
            if (resolvedMasterId != null) {
                masterIncidentDao.recalculateDistinctStudentCount(resolvedMasterId);
            }

            // 7. Record Initial Status History
            lifecycleDao.insertStatusHistory(ticketId, null, TicketStatus.SUBMITTED.name(),
                    student.getUserId(), prediction.isEmergency() ? "EMERGENCY SOS: Rapid incident recorded." : "Grievance filed by student.");

            if (auditLogService != null) {
                auditLogService.logTicketCreated(student.getUserId(), ticketId, ticketRef, category.getDisplayName());
            }
            if (notificationService != null) {
                notificationService.notifyStudentTicketStatusChange(ticketId, TicketStatus.SUBMITTED,
                        prediction.isEmergency() ? "🚨 EMERGENCY SOS DISPATCHED: Quick response team alerted." : "Grievance registered in department queue.");
            }

            // 8. Handle file attachment upload (Section 6.7)
            handleAttachmentUpload(ticketId, student.getUserId(), request.getAttachment());

            if (prediction.isEmergency()) {
                redirectAttributes.addFlashAttribute("emergencyAlertNotice", prediction.getEmergencyActionNotice());
                redirectAttributes.addFlashAttribute("successMessage",
                        "🚨 EMERGENCY DISPATCHED: " + ticketRef + " routed to " + prediction.getTargetDeptName() + " with 15-min critical response SLA!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Grievance " + ticketRef + " filed successfully! Assigned to " + prediction.getTargetDeptName() + "." +
                        (resolvedMasterId != null ? " (Linked to block incident #" + resolvedMasterId + " for faster coordination)" : ""));
            }
        } catch (Exception e) {
            log.error("Failed to file grievance", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error filing grievance: " + e.getMessage());
        }

        return "redirect:/student/dashboard";
    }

    @PostMapping("/tickets/{ticketId}/confirm")
    public String confirmFixed(@PathVariable Long ticketId,
                               @RequestParam(value = "rating", required = false) Integer rating,
                               @RequestParam(value = "feedback", required = false) String feedback,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        User student = getAuthenticatedStudent(auth);
        try {
            handshakeService.confirmFixedByStudent(ticketId, student.getUserId(), rating, feedback);
            redirectAttributes.addFlashAttribute("successMessage", "Resolution confirmed! Ticket is now CLOSED. Thank you for your feedback.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/dashboard";
    }

    @PostMapping("/tickets/{ticketId}/reopen")
    public String rejectAndReopen(@PathVariable Long ticketId,
                                 @RequestParam("rejectionReason") String rejectionReason,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        User student = getAuthenticatedStudent(auth);
        try {
            handshakeService.rejectFixAndReopen(ticketId, student.getUserId(), rejectionReason);
            redirectAttributes.addFlashAttribute("warningMessage", "Ticket REOPENED and returned to department queue.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/dashboard";
    }

    private void handleAttachmentUpload(Long ticketId, Long studentUserId, MultipartFile file) {
        if (file == null || file.isEmpty()) return;

        try {
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "attachment.jpg";
            String ext = "";
            int i = originalName.lastIndexOf('.');
            if (i > 0) ext = originalName.substring(i);

            // Server-side random filename outside public directory (Section 6.7)
            String safeFileName = UUID.randomUUID() + ext;
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path target = uploadPath.resolve(safeFileName);
            file.transferTo(target.toFile());

            TicketAttachment attachment = new TicketAttachment();
            attachment.setTicketId(ticketId);
            attachment.setUploadedByUserId(studentUserId);
            attachment.setOriginalFileName(originalName);
            attachment.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            attachment.setStoragePath(target.toString());
            attachment.setFileSizeBytes(file.getSize());

            lifecycleDao.insertAttachment(attachment);
        } catch (IOException e) {
            log.warn("Could not save ticket attachment: {}", e.getMessage());
        }
    }

    private User getAuthenticatedStudent(Authentication auth) {
        return userDao.findByEmail(auth.getName())
                .or(() -> userDao.findByRegOrStaffId(auth.getName()))
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }
}

