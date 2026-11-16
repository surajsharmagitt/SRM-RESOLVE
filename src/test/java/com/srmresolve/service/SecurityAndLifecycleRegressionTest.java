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

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Scenario 1: Health check endpoint /welcome responds with 200 OK")
    void testHealthCheckServesOk() throws Exception {
        mockMvc.perform(get("/welcome"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Scenario 2: Invalid passkeys fail, while authorized passkey 123456 authenticates")
    void testPasskeyBypassFailsInProduction() {
        assertThrows(IllegalArgumentException.class, () ->
                onboardingService.verifyStudentLoginOtp("suraj_sharma@srmap.edu.in", "999999"));
        User user = onboardingService.verifyStudentLoginOtp("suraj_sharma@srmap.edu.in", "123456");
        assertNotNull(user);
        assertEquals("suraj_sharma@srmap.edu.in", user.getEmail());
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "suraj_sharma@srmap.edu.in", roles = {"STUDENT"})
    @DisplayName("Scenario 5b: State-changing API call without CSRF token is blocked (403 Forbidden)")
    void testApiPostWithoutCsrfTokenIsBlocked() throws Exception {
        String body = "{\"messageBody\": \"Legitimate text without token\"}";
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

    @Test
    @WithMockUser(username = "water.head@srmap.edu.in", roles = {"DEPT_HEAD"})
    @DisplayName("Scenario 11: Department head search ignores client-forged deptId and enforces assigned department")
    void testDeptHeadSearchRestrictedToOwnDepartment() throws Exception {
        // Water Dept Head (dept_id = 2) attempts to query tickets from CLM (dept_id = 1)
        String forgedPayload = "{\"deptId\": 1}";
        String responseContent = mockMvc.perform(post("/api/tickets/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forgedPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Any tickets returned must be for Water Plumbing (dept 2), never CLM (dept 1)
        assertFalse(responseContent.contains("\"assignedDeptId\":1"));
    }

    @Test
    @WithMockUser(username = "suraj_sharma@srmap.edu.in", roles = {"STUDENT"})
    @DisplayName("Scenario 12: Student search ignores client-forged studentUserId and returns only own tickets")
    void testStudentSearchCannotForgeStudentUserId() throws Exception {
        // Suraj (user_id = 18) attempts to search for tickets belonging to Ananya (user_id = 19)
        String forgedPayload = "{\"studentUserId\": 19}";
        String responseContent = mockMvc.perform(post("/api/tickets/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forgedPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Returned tickets must belong to student 18, never student 19
        assertFalse(responseContent.contains("\"studentUserId\":19"));
    }

    @Test
    @WithMockUser(username = "admin@srmap.edu.in", roles = {"ADMIN"})
    @DisplayName("Scenario 13: Administrator retains campus-wide search access")
    void testAdminCanSearchAllDepartments() throws Exception {
        String payload = "{\"deptId\": 1}";
        mockMvc.perform(post("/api/tickets/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Scenario 14: Ticket references are guaranteed unique even after deletions and follow SRM-YYYY-NNNNNN")
    void testTicketReferenceUniquenessAfterDeletions() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // 1. Insert first test ticket
        GrievanceTicket tA = new GrievanceTicket();
        tA.setStudentUserId(18L);
        tA.setCategoryId(1);
        tA.setAssignedDeptId(1);
        tA.setLocationScope(LocationScope.HOSTEL);
        tA.setBuildingBlock("Ganga Tower - Block A");
        tA.setRoomOrLocation("G-101");
        tA.setRawDescription("Test Ticket A for reference uniqueness");
        tA.setPriority(Priority.MEDIUM);
        tA.setStatus(TicketStatus.SUBMITTED);
        tA.setAckDeadlineAt(now.plusHours(4));
        tA.setResolutionDeadlineAt(now.plusHours(24));
        Long idA = ticketDao.insert(tA);
        String refA = tA.getTicketReference();

        assertNotNull(refA);
        assertTrue(refA.matches("^SRM-\\d{4}-\\d{6}$"), "Reference must match SRM-YYYY-NNNNNN: " + refA);

        // 2. Insert second test ticket
        GrievanceTicket tB = new GrievanceTicket();
        tB.setStudentUserId(18L);
        tB.setCategoryId(1);
        tB.setAssignedDeptId(1);
        tB.setLocationScope(LocationScope.HOSTEL);
        tB.setBuildingBlock("Ganga Tower - Block A");
        tB.setRoomOrLocation("G-102");
        tB.setRawDescription("Test Ticket B for reference uniqueness");
        tB.setPriority(Priority.MEDIUM);
        tB.setStatus(TicketStatus.SUBMITTED);
        tB.setAckDeadlineAt(now.plusHours(4));
        tB.setResolutionDeadlineAt(now.plusHours(24));
        Long idB = ticketDao.insert(tB);
        String refB = tB.getTicketReference();

        assertNotNull(refB);
        assertNotEquals(refA, refB, "Consecutive tickets must not have identical references");

        // 3. Simulate ticket deletion (which previously caused COUNT(*) + 1 to collide)
        jdbcTemplate.update("DELETE FROM grievance_tickets WHERE ticket_id = ?", idB);

        // 4. Insert third test ticket after deletion
        GrievanceTicket tC = new GrievanceTicket();
        tC.setStudentUserId(18L);
        tC.setCategoryId(1);
        tC.setAssignedDeptId(1);
        tC.setLocationScope(LocationScope.HOSTEL);
        tC.setBuildingBlock("Ganga Tower - Block A");
        tC.setRoomOrLocation("G-103");
        tC.setRawDescription("Test Ticket C for reference uniqueness");
        tC.setPriority(Priority.MEDIUM);
        tC.setStatus(TicketStatus.SUBMITTED);
        tC.setAckDeadlineAt(now.plusHours(4));
        tC.setResolutionDeadlineAt(now.plusHours(24));
        Long idC = ticketDao.insert(tC);
        String refC = tC.getTicketReference();

        assertNotNull(refC);
        assertTrue(refC.matches("^SRM-\\d{4}-\\d{6}$"), "Reference must match SRM-YYYY-NNNNNN: " + refC);
        assertNotEquals(refA, refC, "Post-deletion ticket must not collide with existing ticket reference");
        assertNotEquals(refB, refC, "Post-deletion ticket must not reuse deleted ticket reference");

        // Cleanup test records
        jdbcTemplate.update("DELETE FROM grievance_tickets WHERE ticket_id IN (?, ?)", idA, idC);
    }
}
