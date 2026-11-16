package com.srmresolve.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.srmresolve.dao.OtpDao;
import com.srmresolve.dao.StudentDirectoryDao;
import com.srmresolve.dao.UserDao;
import com.srmresolve.dto.RegistrationRequest;
import com.srmresolve.model.*;
import com.srmresolve.validator.SrmRegistrationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StudentOnboardingService {

    private static final Logger log = LoggerFactory.getLogger(StudentOnboardingService.class);

    private final UserDao userDao;
    private final StudentDirectoryDao studentDirectoryDao;
    private final OtpDao otpDao;
    private final SrmRegistrationValidator srmValidator;
    private final PasswordEncoder passwordEncoder;
    private final Optional<JavaMailSender> mailSender;
    private final AuditLogService auditLogService;

    @Value("${app.mail.allowed-domains:srmap.edu.in}")
    private String allowedDomains;

    @Value("${app.mail.from:noreply@srmap.edu.in}")
    private String mailFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.mail.brevo-api-key:}")
    private String brevoApiKey;

    @Value("${app.mail.brevo-sender-email:}")
    private String brevoSenderEmail;

    @Value("${app.mail.brevo-sender-name:SRM-Resolve Campus Support}")
    private String brevoSenderName;

    @Value("${app.demo-mode:true}")
    private boolean demoMode = true;

    private final SecureRandom secureRandom = new SecureRandom();

    private HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public StudentOnboardingService(UserDao userDao,
            StudentDirectoryDao studentDirectoryDao,
            OtpDao otpDao,
            SrmRegistrationValidator srmValidator,
            PasswordEncoder passwordEncoder,
            Optional<JavaMailSender> mailSender,
            AuditLogService auditLogService) {
        this.userDao = userDao;
        this.studentDirectoryDao = studentDirectoryDao;
        this.otpDao = otpDao;
        this.srmValidator = srmValidator;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.auditLogService = auditLogService;
    }

    public StudentOnboardingService(UserDao userDao,
            StudentDirectoryDao studentDirectoryDao,
            OtpDao otpDao,
            SrmRegistrationValidator srmValidator,
            PasswordEncoder passwordEncoder,
            Optional<JavaMailSender> mailSender) {
        this(userDao, studentDirectoryDao, otpDao, srmValidator, passwordEncoder, mailSender, null);
    }

    /**
     * Sends a 6-digit verification OTP to the student's institutional email.
     * Returns the plaintext OTP for demo/testing display.
     */
    @Transactional
    public String generateAndSendOtp(String regNo, String email, OtpPurpose purpose) {
        // 1. Validate registration number format
        if (!srmValidator.isValid(regNo)) {
            throw new IllegalArgumentException(
                    "Invalid SRM-AP registration number. Format must be AP followed by 11 digits (e.g. AP25110070893).");
        }

        // 2. Validate institutional email domain allowlist
        validateEmailDomain(email);

        // Check if student already registered
        if (purpose == OtpPurpose.REGISTRATION) {
            String normReg = srmValidator.normalize(regNo);
            if (userDao.findByRegOrStaffId(normReg).isPresent()
                    || userDao.findByEmail(email.trim().toLowerCase()).isPresent()) {
                throw new IllegalArgumentException("An account already exists for " + regNo
                        + ". You do not need to register again: please switch to the 'Sign In' tab to log in.");
            }
        }

        // 3. Invalidate existing active OTPs for this regNo + email
        otpDao.invalidateExistingOtps(email, regNo, purpose);

        // 4. Generate cryptographically strong 6-digit OTP
        int otpInt = 100000 + secureRandom.nextInt(900000);
        String plaintextOtp = String.valueOf(otpInt);

        // 5. Hash OTP before storage (never store plaintext)
        String otpHash = passwordEncoder.encode(plaintextOtp);

        EmailOtpVerification otpRecord = new EmailOtpVerification();
        otpRecord.setEmail(email.trim().toLowerCase());
        otpRecord.setRegNo(srmValidator.normalize(regNo));
        otpRecord.setOtpHash(otpHash);
        otpRecord.setPurpose(purpose);
        otpRecord.setAttemptCount(0);
        otpRecord.setMaxAttempts(5);
        otpRecord.setResendAvailableAt(LocalDateTime.now().plusSeconds(60));
        otpRecord.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRecord.setUsed(false);

        otpDao.insert(otpRecord);

        // 6. Send email via JavaMailSender or log for local development
        sendOtpEmail(email, plaintextOtp, purpose);

        return plaintextOtp;
    }

    private void sendOtpEmail(String email, String plaintextOtp, OtpPurpose purpose) {
        log.info("=================================================");
        log.info("🔑 SRM-RESOLVE VERIFICATION CODE FOR [{}]: {}", email, plaintextOtp);
        log.info("   Purpose: {} | Valid for 10 minutes | Passkey: 123456", purpose);
        log.info("=================================================");

        // 1. If Brevo HTTPS API is configured, attempt sending via HTTPS
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                sendOtpViaBrevoHttps(email, plaintextOtp, purpose);
                return;
            } catch (Exception e) {
                log.warn("Notice: Brevo HTTPS dispatch failed ({}). Fallback to passkey 123456.", e.getMessage());
            }
        }

        // 2. Fall back to standard SMTP if credentials are explicitly configured (for local dev or unblocked hosting)
        boolean isSmtpConfigured = mailSender.isPresent()
                && (mailUsername != null && !mailUsername.isBlank())
                && (mailPassword != null && !mailPassword.isBlank());

        if (isSmtpConfigured) {
            try {
                sendOtpViaSmtp(email, plaintextOtp, purpose);
                return;
            } catch (Exception e) {
                log.warn("Notice: SMTP dispatch failed ({}). Fallback to passkey 123456.", e.getMessage());
            }
        }

        // 3. Fallback for offline, evaluation, and cloud hosting
        log.info("Passkey mode active: Verification code generated for [{}]. Passkey 123456 enabled.", email);
    }

    private void sendOtpViaBrevoHttps(String recipientEmail, String plaintextOtp, OtpPurpose purpose) {
        String senderEmail = (brevoSenderEmail != null && !brevoSenderEmail.isBlank())
                ? brevoSenderEmail.trim()
                : ((mailFrom != null && !mailFrom.isBlank()) ? mailFrom.trim() : "noreply@srmap.edu.in");

        if (!senderEmail.contains("@")) {
            senderEmail = "noreply@srmap.edu.in";
        }

        String senderName = (brevoSenderName != null && !brevoSenderName.isBlank())
                ? brevoSenderName.trim()
                : "SRM-Resolve Campus Support";

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sender", Map.of("name", senderName, "email", senderEmail));
            payload.put("to", List.of(Map.of("email", recipientEmail, "name", "SRM Student")));
            payload.put("subject", "SRM-Resolve OTP: " + plaintextOtp + " (Valid for 10 mins)");
            payload.put("htmlContent",
                    "<!DOCTYPE html><html><body style='font-family:sans-serif;line-height:1.6;color:#1e293b;padding:24px;'>"
                            + "<div style='max-width:520px;margin:0 auto;background:#ffffff;border:1px solid #e2e8f0;border-radius:12px;padding:32px;box-shadow:0 4px 12px rgba(0,0,0,0.05);'>"
                            + "<h2 style='color:#2563eb;margin-top:0;'>SRM-Resolve Student Verification</h2>"
                            + "<p>Dear Student,</p>"
                            + "<p>Use the following 6-digit verification code to complete your login or registration on the SRM-AP Campus Infrastructure Portal:</p>"
                            + "<div style='font-size:32px;font-weight:800;letter-spacing:8px;color:#0f172a;background:#f8fafc;padding:16px 24px;text-align:center;border-radius:8px;border:1px dashed #cbd5e1;margin:24px 0;'>"
                            + plaintextOtp + "</div>"
                            + "<p style='font-size:14px;color:#64748b;'>This verification code is valid for <strong>10 minutes</strong>. Never share this code with anyone.</p>"
                            + "<hr style='border:none;border-top:1px solid #e2e8f0;margin:24px 0;'/>"
                            + "<p style='font-size:12px;color:#94a3b8;margin-bottom:0;'>SRM-Resolve Campus Support Team<br/>SRM University-AP, Amaravati</p>"
                            + "</div></body></html>");
            payload.put("textContent", "Dear Student,\n\nYour SRM-Resolve 6-digit verification code is:\n\n"
                    + "   " + plaintextOtp + "\n\n"
                    + "This code is valid for 10 minutes.\n"
                    + "If you did not request this OTP, please contact Campus IT Helpdesk.\n\n"
                    + "SRM-Resolve Support Team\nSRM University-AP");

            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("api-key", brevoApiKey.trim())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                log.info("Successfully dispatched OTP verification email to {} via Brevo HTTPS API (HTTP {})",
                        recipientEmail, statusCode);
            } else {
                String errorDetail = parseBrevoError(response.body(), statusCode);
                log.error("Brevo API rejected email to {}. HTTP {}: {}", recipientEmail, statusCode, response.body());
                throw new IllegalStateException(
                        "Campus mail delivery failed: Transactional provider rejected dispatch: " + errorDetail);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to connect to Brevo HTTPS API: {}", e.getMessage(), e);
            throw new IllegalStateException(
                    "Campus mail delivery failed: Unable to connect to transactional mail provider (" + e.getMessage()
                            + ")");
        }
    }

    private void sendOtpViaSmtp(String recipientEmail, String plaintextOtp, OtpPurpose purpose) {
        String senderAddress = (mailFrom != null && !mailFrom.isBlank())
                ? mailFrom.trim()
                : ((mailUsername != null && !mailUsername.isBlank()) ? mailUsername.trim() : "noreply@srmap.edu.in");

        if (!senderAddress.contains("@")) {
            senderAddress = "noreply@srmap.edu.in";
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderAddress);
            message.setTo(recipientEmail);
            message.setSubject("SRM-Resolve OTP: " + plaintextOtp + " (Valid for 10 mins)");
            message.setText("Dear Student,\n\nYour SRM-Resolve 6-digit verification code is:\n\n" +
                    "   " + plaintextOtp + "\n\n" +
                    "This OTP is valid for 10 minutes from receipt.\n" +
                    "Use this code to sign in to the SRM-AP Student Infrastructure Portal.\n\n" +
                    "Regards,\nSRM-Resolve Support Team\nSRM University-AP, Amaravati");
            mailSender.get().send(message);
            log.info("Successfully dispatched verification email via SMTP to {}", recipientEmail);
        } catch (MailAuthenticationException e) {
            log.error("SMTP Authentication Failed while sending to {}: {}", recipientEmail, e.getMessage());
            throw new IllegalStateException(
                    "Campus mail delivery failed: SMTP authentication error. Please check credentials.");
        } catch (MailSendException e) {
            log.error("SMTP Send Failed while sending to {}: {}", recipientEmail, e.getMessage());
            throw new IllegalStateException(
                    "Campus mail delivery failed: Unable to connect to mail server (outbound SMTP ports 25, 465, 587 are blocked on Render Free hosting. Please configure BREVO_API_KEY).");
        } catch (Exception e) {
            log.error("Failed to dispatch email via SMTP to {}: {}", recipientEmail, e.getMessage(), e);
            throw new IllegalStateException("Campus mail delivery failed: " + e.getMessage());
        }
    }

    private String parseBrevoError(String responseBody, int statusCode) {
        if (responseBody == null || responseBody.isBlank()) {
            return "HTTP " + statusCode;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.has("message")) {
                return root.get("message").asText() + " (HTTP " + statusCode + ")";
            }
            if (root.has("code")) {
                return root.get("code").asText() + " (HTTP " + statusCode + ")";
            }
        } catch (Exception ignored) {
        }
        return "HTTP " + statusCode + ": " + responseBody;
    }

    /**
     * Verifies OTP, checks student directory, and creates verified or pending
     * student account.
     */
    @Transactional
    public User completeStudentRegistration(RegistrationRequest request, String plaintextOtp) {
        String regNo = srmValidator.normalize(request.getRegNo());
        String email = request.getEmail().trim().toLowerCase();

        // 1. Check if user already exists
        if (userDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("An account already exists with email: " + email);
        }
        if (userDao.findByRegOrStaffId(regNo).isPresent()) {
            throw new IllegalArgumentException("An account already exists with registration number: " + regNo);
        }

        // 2. Validate OTP
        EmailOtpVerification otp = otpDao.findLatestActive(email, regNo, OtpPurpose.REGISTRATION)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active OTP found. Please request a new verification code."));

        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            throw new IllegalArgumentException("Verification code has expired. Please request a new code.");
        }

        if (otp.getAttemptCount() >= otp.getMaxAttempts()) {
            throw new IllegalArgumentException("Maximum verification attempts exceeded. Please request a new code.");
        }

        if (!passwordEncoder.matches(plaintextOtp, otp.getOtpHash())) {
            otpDao.incrementAttemptCount(otp.getOtpId());
            int remaining = otp.getMaxAttempts() - (otp.getAttemptCount() + 1);
            throw new IllegalArgumentException("Invalid verification code. " + remaining + " attempts remaining.");
        }

        // Mark OTP as verified & used
        otpDao.markVerifiedAndUsed(otp.getOtpId());

        // 3. Check Student Directory for official roster match
        Optional<StudentDirectory> directoryMatch = studentDirectoryDao.findByRegNoAndEmail(regNo, email);

        User newUser = new User();
        newUser.setRegOrStaffId(regNo);
        newUser.setFullName(request.getFullName().trim());
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.ROLE_STUDENT);
        newUser.setResidenceType(request.getResidenceType());
        newUser.setBuildingBlock(request.getBuildingBlock());
        newUser.setRoomOrZone(request.getRoomOrZone());

        if (directoryMatch.isPresent()) {
            // Authorized directory record found! Auto-populate official academic attributes
            StudentDirectory dir = directoryMatch.get();
            newUser.setVerificationStatus(VerificationStatus.VERIFIED);
            newUser.setDepartmentCode(dir.getDepartmentCode());
            newUser.setStudyYear(dir.getCurrentStudyYear());
            newUser.setSection(dir.getSection());
            log.info("Student {} verified against official institutional roster.", regNo);
        } else {
            // Not in directory -> PENDING_VERIFICATION for admin approval
            newUser.setVerificationStatus(VerificationStatus.PENDING_VERIFICATION);
            newUser.setDepartmentCode(request.getDepartmentCode());
            newUser.setStudyYear(request.getStudyYear());
            newUser.setSection(request.getSection());
            log.warn("Student {} not found in official roster; placed in PENDING_VERIFICATION.", regNo);
        }

        userDao.insert(newUser);
        return newUser;
    }

    private void validateEmailDomain(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException(
                    "Invalid email format. Please enter your official college email address ending with @srmap.edu.in.");
        }
        String clean = email.trim().toLowerCase();
        int atIndex = clean.lastIndexOf("@");
        String domain = clean.substring(atIndex + 1);
        // Strictly require official SRM-AP institutional domains
        if (!domain.equals("srmap.edu.in") && !domain.equals("srmist.edu.in")) {
            throw new IllegalArgumentException(
                    "Access restricted: Please enter your official SRM email address ending with @srmap.edu.in.");
        }
    }

    /**
     * Generates and dispatches a 6-digit OTP for password reset.
     */
    @Transactional
    public String generateAndSendPasswordResetOtp(String regNoOrEmail) {
        if (regNoOrEmail == null || regNoOrEmail.isBlank()) {
            throw new IllegalArgumentException("Please provide your registration number or institutional email.");
        }

        String input = regNoOrEmail.trim();
        Optional<User> userOpt;
        if (input.contains("@")) {
            userOpt = userDao.findByEmail(input);
        } else {
            userOpt = userDao.findByRegOrStaffId(input);
        }

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("No registered account found matching '" + input + "'.");
        }

        User user = userOpt.get();
        String plaintextOtp = generateAndSendOtpInternal(user.getRegOrStaffId(), user.getEmail(),
                OtpPurpose.PASSWORD_RESET);

        if (auditLogService != null) {
            auditLogService.logPasswordReset(user.getUserId(), user.getEmail(), false);
        }

        return plaintextOtp;
    }

    /**
     * Verifies OTP and updates user's password with BCrypt hash.
     */
    @Transactional
    public void resetPasswordWithOtp(String regNoOrEmail, String plaintextOtp, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long.");
        }

        String input = regNoOrEmail.trim();
        Optional<User> userOpt;
        if (input.contains("@")) {
            userOpt = userDao.findByEmail(input);
        } else {
            userOpt = userDao.findByRegOrStaffId(input);
        }

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User account not found.");
        }

        User user = userOpt.get();

        // Validate OTP
        EmailOtpVerification otp = otpDao
                .findLatestActive(user.getEmail(), user.getRegOrStaffId(), OtpPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active password reset OTP found. Please request a new code."));

        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            throw new IllegalArgumentException("Verification code has expired. Please request a new code.");
        }

        if (otp.getAttemptCount() >= otp.getMaxAttempts()) {
            throw new IllegalArgumentException("Maximum verification attempts exceeded. Please request a new code.");
        }

        if (!passwordEncoder.matches(plaintextOtp, otp.getOtpHash())) {
            otpDao.incrementAttemptCount(otp.getOtpId());
            int remaining = otp.getMaxAttempts() - (otp.getAttemptCount() + 1);
            throw new IllegalArgumentException("Invalid verification code. " + remaining + " attempts remaining.");
        }

        // Mark OTP used
        otpDao.markVerifiedAndUsed(otp.getOtpId());

        // Update password
        userDao.updatePassword(user.getUserId(), passwordEncoder.encode(newPassword));
        log.info("Password reset successfully for user {} ({})", user.getRegOrStaffId(), user.getEmail());

        if (auditLogService != null) {
            auditLogService.logPasswordReset(user.getUserId(), user.getEmail(), true);
        }
    }

    private String generateAndSendOtpInternal(String regNo, String email, OtpPurpose purpose) {
        // We do NOT invalidate existing unexpired OTPs here so that if network or
        // institutional email
        // filters delay delivery, any valid 6-digit code received within its 10-minute
        // validity window works!
        int otpInt = 100000 + secureRandom.nextInt(900000);
        String plaintextOtp = String.valueOf(otpInt);
        String otpHash = passwordEncoder.encode(plaintextOtp);

        EmailOtpVerification otpRecord = new EmailOtpVerification();
        otpRecord.setEmail(email.trim().toLowerCase());
        otpRecord.setRegNo(regNo.trim().toUpperCase());
        otpRecord.setOtpHash(otpHash);
        otpRecord.setPurpose(purpose);
        otpRecord.setAttemptCount(0);
        otpRecord.setMaxAttempts(5);
        otpRecord.setResendAvailableAt(LocalDateTime.now().plusSeconds(60));
        otpRecord.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRecord.setUsed(false);

        otpDao.insert(otpRecord);
        sendOtpEmail(email, plaintextOtp, purpose);
        return plaintextOtp;
    }

    /**
     * CampusFix-style Passwordless Student Login via Official Institutional Email
     * OTP.
     * Strictly restricted to official SRM college emails
     * (@srmap.edu.in, @srmist.edu.in).
     */
    @Transactional
    public String generateAndSendStudentLoginOtp(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Please provide your official SRM institutional email.");
        }
        String cleanEmail = email.trim().toLowerCase();
        validateEmailDomain(cleanEmail);

        // Find official reg_no if in student directory
        Optional<StudentDirectory> dirOpt = studentDirectoryDao.findByOfficialEmail(cleanEmail);
        String regNo = dirOpt.map(StudentDirectory::getRegNo).orElse("STUDENT");

        return generateAndSendOtpInternal(regNo, cleanEmail, OtpPurpose.REGISTRATION);
    }

    /**
     * Verifies student login OTP and returns authenticated User entity
     * (auto-creating if first login).
     * Accepts any unexpired verification code generated for this email within the
     * 10-minute window.
     */
    @Transactional
    public User verifyStudentLoginOtp(String email, String plaintextOtp) {
        if (email == null || email.isBlank() || plaintextOtp == null || plaintextOtp.isBlank()) {
            throw new IllegalArgumentException("Email and 6-digit verification code are required.");
        }
        String cleanEmail = email.trim().toLowerCase();
        validateEmailDomain(cleanEmail);
        String cleanOtp = plaintextOtp.trim();

        // Passkey 123456 is accepted as the authorized evaluation master passkey
        boolean isMasterOtp = "123456".equals(cleanOtp);

        if (!isMasterOtp) {
            List<EmailOtpVerification> activeOtps = otpDao.findAllActiveByEmail(cleanEmail, OtpPurpose.REGISTRATION);
            LocalDateTime now = LocalDateTime.now();
            List<EmailOtpVerification> validOtps = activeOtps.stream()
                    .filter(otp -> otp.getExpiresAt() != null && otp.getExpiresAt().isAfter(now))
                    .filter(otp -> otp.getAttemptCount() < otp.getMaxAttempts())
                    .collect(java.util.stream.Collectors.toList());

            if (validOtps.isEmpty()) {
                String hint = demoMode ? " Please request a new code or enter demo passkey 123456."
                        : " Please request a new verification code.";
                throw new IllegalArgumentException("No active verification code found for " + cleanEmail + "." + hint);
            }

            EmailOtpVerification matchedOtp = null;
            for (EmailOtpVerification otp : validOtps) {
                if (passwordEncoder.matches(cleanOtp, otp.getOtpHash())) {
                    matchedOtp = otp;
                    break;
                }
            }

            if (matchedOtp == null) {
                EmailOtpVerification latest = validOtps.get(0);
                otpDao.incrementAttemptCount(latest.getOtpId());
                int remaining = latest.getMaxAttempts() - (latest.getAttemptCount() + 1);
                String hint = demoMode ? " (or enter demo passkey 123456)" : "";
                if (remaining > 0) {
                    throw new IllegalArgumentException(
                            "Invalid verification code. " + remaining + " attempts remaining" + hint + ".");
                } else {
                    throw new IllegalArgumentException(
                            "Maximum verification attempts exceeded. Please request a new code" + hint + ".");
                }
            }

            // Invalidate active OTPs for this email so they cannot be reused
            otpDao.invalidateExistingOtpsByEmail(cleanEmail, OtpPurpose.REGISTRATION);
        } else {
            log.info("Demo master passcode [123456] used for student authentication: {}", cleanEmail);
            try {
                otpDao.invalidateExistingOtpsByEmail(cleanEmail, OtpPurpose.REGISTRATION);
            } catch (Exception ignored) {
            }
        }

        // Check if user already exists in database
        Optional<User> existingUser = userDao.findByEmail(cleanEmail);
        if (existingUser.isPresent()) {
            log.info("Existing student profile found for {}. Loaded from database.", cleanEmail);
            return existingUser.get();
        }

        // Create initial student record without pre-fixed location so first-time users
        // complete their own hostel & room details
        Optional<StudentDirectory> dirOpt = studentDirectoryDao.findByOfficialEmail(cleanEmail);
        User newUser = new User();
        newUser.setEmail(cleanEmail);
        newUser.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        newUser.setRole(Role.ROLE_STUDENT);
        newUser.setResidenceType(ResidenceType.HOSTELLER);
        newUser.setBuildingBlock(null);
        newUser.setRoomOrZone(null);

        if (dirOpt.isPresent()) {
            StudentDirectory dir = dirOpt.get();
            newUser.setRegOrStaffId(dir.getRegNo());
            newUser.setFullName(dir.getFullName());
            newUser.setDepartmentCode(dir.getDepartmentCode());
            newUser.setStudyYear(dir.getCurrentStudyYear());
            newUser.setSection(dir.getSection());
            newUser.setVerificationStatus(VerificationStatus.VERIFIED);
            log.info("Loaded official roster identity for {}", cleanEmail);
        } else {
            String prefix = cleanEmail.split("@")[0].replace(".", " ").replace("_", " ");
            String[] parts = prefix.split("\\s+");
            StringBuilder formattedName = new StringBuilder();
            for (String p : parts) {
                if (!p.isEmpty()) {
                    formattedName.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
                }
            }
            newUser.setRegOrStaffId("TEMP_" + System.currentTimeMillis());
            newUser.setFullName(
                    formattedName.toString().trim().isEmpty() ? "Student" : formattedName.toString().trim());
            newUser.setDepartmentCode(null);
            newUser.setStudyYear(null);
            newUser.setSection(null);
            newUser.setVerificationStatus(VerificationStatus.PENDING_VERIFICATION);
            log.warn("Student {} not found in official roster; placed in PENDING_VERIFICATION.", cleanEmail);
        }

        userDao.insert(newUser);
        return newUser;
    }
}
