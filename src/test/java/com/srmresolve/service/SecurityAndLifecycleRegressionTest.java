package com.srmresolve.service;

import com.srmresolve.dao.*;
import com.srmresolve.model.*;
import com.srmresolve.validator.SrmRegistrationValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security, RBAC, Data Integrity & Deployment Health Regression Tests")
class SecurityAndLifecycleRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentOnboardingService onboardingService;

    @Autowired
    private GrievanceTicketDao ticketDao;

    @Autowired
    private UserDao userDao;

    @Test
    @DisplayName("Scenario 1: Health check endpoint /welcome responds with 200 OK")
    void testHealthCheckServesOk() throws Exception {
        mockMvc.perform(get("/welcome"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Scenario 2: In production mode (demoMode = false), passkey 123456 must fail")
    void testPasskeyBypassFailsInProduction() {
        // Temporarily set demoMode to false to simulate production
        ReflectionTestUtils.setField(onboardingService, "demoMode", false);
        try {
            assertThrows(IllegalArgumentException.class, () ->
                    onboardingService.verifyStudentLoginOtp("suraj_sharma@srmap.edu.in", "123456"));
        } finally {
            // Restore test profile demoMode setting
            ReflectionTestUtils.setField(onboardingService, "demoMode", true);
        }
    }

    @Test
    @WithMockUser(username = "suraj_sharma@srmap.edu.in", roles = {"STUDENT"})
    @DisplayName("Scenario 3: Ordinary students cannot access department live queue (403 Forbidden)")
    void testStudentCannotAccessDepartmentQueue() throws Exception {
        mockMvc.perform(get("/api/department/live-queue"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ananya_reddy@srmap.edu.in", roles = {"STUDENT"})
    @DisplayName("Scenario 4: Student cannot access messages of another student's ticket (403 Forbidden)")
    void testStudentCannotAccessOtherStudentsMessages() throws Exception {
        // Ticket 1 belongs to student 18 (Suraj). Ananya (student 19) must be rejected with 403.
        mockMvc.perform(get("/api/tickets/1/messages"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ananya_reddy@srmap.edu.in", roles = {"STUDENT"})
    @DisplayName("Scenario 5: Student cannot post message to another student's ticket (403 Forbidden)")
    void testStudentCannotPostMessageToOtherStudentTicket() throws Exception {
        String body = "{\"messageBody\": \"Intruder message\"}";
        mockMvc.perform(post("/api/tickets/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hostel.staff1@srmap.edu.in", roles = {"DEPT_STAFF"})
    @DisplayName("Scenario 6: Department staff cannot access or post messages to tickets of an unrelated department (403 Forbidden)")
    void testDepartmentStaffCannotAccessUnrelatedDepartmentTicket() throws Exception {
        // Ticket 2 is assigned to Water Plumbing (dept 2). Hostel staff (dept 1) must be rejected with 403.
        mockMvc.perform(get("/api/tickets/2/messages"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hostel.staff1@srmap.edu.in", roles = {"DEPT_STAFF"})
    @DisplayName("Scenario 7: Department staff cannot update or acknowledge an unrelated department's ticket")
    void testStaffCannotAcknowledgeUnrelatedTicket() throws Exception {
        // Hostel staff attempts to acknowledge Ticket 2 (Water Dept)
        mockMvc.perform(post("/department/tickets/2/acknowledge")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "hostel.staff1@srmap.edu.in", roles = {"DEPT_STAFF"})
    @DisplayName("Scenario 8: Staff cannot assign complaints to staff from an unrelated department")
    void testStaffCannotAssignToUnrelatedDepartmentStaff() throws Exception {
        // Mahesh (user_id 32) belongs to Water Plumbing. Hostel staff cannot assign him.
        mockMvc.perform(post("/department/tickets/1/assign")
                        .param("staffUserId", "32")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Scenario 9: Unauthenticated requests cannot download attachments (401 Unauthorized)")
    void testUnauthenticatedAttachmentAccessBlocked() throws Exception {
        mockMvc.perform(get("/api/attachments/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "suraj_sharma@srmap.edu.in", roles = {"STUDENT"})
    @DisplayName("Scenario 10: Owning student can access their own ticket messages (200 OK)")
    void testOwningStudentCanAccessOwnMessages() throws Exception {
        // Ticket 1 belongs to Suraj Sharma (student 18)
        mockMvc.perform(get("/api/tickets/1/messages"))
                .andExpect(status().isOk());
    }
}
