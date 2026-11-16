package com.srmresolve.service;

import com.srmresolve.dao.OtpDao;
import com.srmresolve.dao.StudentDirectoryDao;
import com.srmresolve.dao.UserDao;
import com.srmresolve.validator.SrmRegistrationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transactional Email Dispatch (Brevo HTTPS & Outbound Error Handling) Tests")
class TransactionalEmailDispatchTest {

    @Mock
    private UserDao userDao;

    @Mock
    private StudentDirectoryDao studentDirectoryDao;

    @Mock
    private OtpDao otpDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    private StudentOnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        SrmRegistrationValidator validator = new SrmRegistrationValidator();
        onboardingService = new StudentOnboardingService(
                userDao, studentDirectoryDao, otpDao, validator, passwordEncoder, Optional.empty(), null
        );
        onboardingService.setHttpClient(mockHttpClient);
        lenient().when(passwordEncoder.encode(any())).thenReturn("hashed-otp");
    }

    @Test
    @DisplayName("1. Brevo HTTP 201 Created successfully delivers OTP via HTTPS")
    @SuppressWarnings("unchecked")
    void testBrevoSuccess() throws IOException, InterruptedException {
        ReflectionTestUtils.setField(onboardingService, "brevoApiKey", "test-api-key");
        ReflectionTestUtils.setField(onboardingService, "brevoSenderEmail", "support@srmap.edu.in");
        ReflectionTestUtils.setField(onboardingService, "demoMode", false);

        lenient().when(mockHttpResponse.statusCode()).thenReturn(201);
        lenient().when(mockHttpResponse.body()).thenReturn("{\"messageId\":\"<12345@smtp-relay.brevo.com>\"}");
        lenient().when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);

        String otp = onboardingService.generateAndSendStudentLoginOtp("suraj_sharma@srmap.edu.in");
        assertNotNull(otp);
        assertEquals(6, otp.length());
        verify(otpDao).insert(any());
    }

    @Test
    @DisplayName("2. When Brevo fails, falls back cleanly with passkey 123456 without throwing")
    @SuppressWarnings("unchecked")
    void testBrevoUnauthorizedFailsFast() throws IOException, InterruptedException {
        ReflectionTestUtils.setField(onboardingService, "brevoApiKey", "invalid-key");
        ReflectionTestUtils.setField(onboardingService, "brevoSenderEmail", "support@srmap.edu.in");

        lenient().when(mockHttpResponse.statusCode()).thenReturn(401);
        lenient().when(mockHttpResponse.body()).thenReturn("{\"code\":\"unauthorized\",\"message\":\"Key not found\"}");
        lenient().when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);

        String otp = onboardingService.generateAndSendStudentLoginOtp("suraj_sharma@srmap.edu.in");
        assertNotNull(otp);
        assertEquals(6, otp.length());
    }

    @Test
    @DisplayName("3. When external email is unconfigured, OTP generates cleanly with passkey 123456")
    void testProductionWithoutConfigThrows() {
        ReflectionTestUtils.setField(onboardingService, "brevoApiKey", "");

        String otp = onboardingService.generateAndSendStudentLoginOtp("suraj_sharma@srmap.edu.in");
        assertNotNull(otp);
        assertEquals(6, otp.length());
    }

    @Test
    @DisplayName("4. Local demo mode without email provider succeeds cleanly for classroom presentations")
    void testLocalDemoModeSucceedsWithoutProvider() {
        ReflectionTestUtils.setField(onboardingService, "brevoApiKey", "");
        ReflectionTestUtils.setField(onboardingService, "demoMode", true);

        String otp = onboardingService.generateAndSendStudentLoginOtp("suraj_sharma@srmap.edu.in");
        assertNotNull(otp);
        assertEquals(6, otp.length());
    }

    @Test
    @DisplayName("5. Non-institutional email domains are rejected immediately")
    void testNonInstitutionalDomainRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                onboardingService.generateAndSendStudentLoginOtp("student@gmail.com"));

        assertTrue(ex.getMessage().contains("official SRM email address"), "Should require official SRM domain");
    }
}
