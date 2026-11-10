package com.srmresolve.dao;

import com.srmresolve.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Database Schema & JDBC DAOs Integration Test")
class SchemaAndDaoIntegrationTest {

    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private StudentDirectoryDao studentDirectoryDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private OtpDao otpDao;

    @Autowired
    private CategoryRoutingSlaDao categoryRoutingSlaDao;

    @Autowired
    private MasterIncidentDao masterIncidentDao;

    @Autowired
    private GrievanceTicketDao grievanceTicketDao;

    @Autowired
    private TicketLifecycleDao ticketLifecycleDao;

    @Autowired
    private ExecutiveAnalyticsDao executiveAnalyticsDao;

    @Test
    @DisplayName("1. DepartmentDao should load seeded SRM-AP departments")
    void testDepartmentDao() {
        List<Department> depts = departmentDao.findAll();
        assertFalse(depts.isEmpty(), "Departments should not be empty");
        assertTrue(depts.stream().anyMatch(d -> "HOSTEL_MAINT".equals(d.getDeptCode())));
        assertTrue(depts.stream().anyMatch(d -> "WATER_PLUMBING".equals(d.getDeptCode())));
        assertTrue(depts.stream().anyMatch(d -> "ITKM_HELPDESK".equals(d.getDeptCode())));

        Optional<Department> water = departmentDao.findByCode("WATER_PLUMBING");
        assertTrue(water.isPresent());
        assertEquals("Water, RO Plant & Plumbing Services", water.get().getDeptName());
    }

    @Test
    @DisplayName("2. StudentDirectoryDao should verify authorized student AP25110070893")
    void testStudentDirectoryDao() {
        Optional<StudentDirectory> student = studentDirectoryDao.findByRegNoAndEmail(
                "AP25110070893", "suraj_sharma@srmap.edu.in");
        assertTrue(student.isPresent(), "Student directory record must exist");
        assertEquals("Suraj Sharma", student.get().getFullName());
        assertEquals("CSE", student.get().getDepartmentCode());
        assertEquals(2025, student.get().getAdmissionYear());

        // Negative check
        Optional<StudentDirectory> nonExistent = studentDirectoryDao.findByRegNoAndEmail(
                "AP25110070893", "wrong_email@gmail.com");
        assertTrue(nonExistent.isEmpty());
    }

    @Test
    @DisplayName("3. UserDao should find user by ID, email and reg/staff ID")
    void testUserDao() {
        Optional<User> admin = userDao.findByEmail("admin@srmap.edu.in");
        assertTrue(admin.isPresent());
        assertEquals(Role.ROLE_ADMIN, admin.get().getRole());
        assertEquals(VerificationStatus.VERIFIED, admin.get().getVerificationStatus());

        Optional<User> student = userDao.findByRegOrStaffId("AP25110070893");
        assertTrue(student.isPresent());
        assertEquals(Role.ROLE_STUDENT, student.get().getRole());
        assertEquals("Ganga Tower - Block A", student.get().getBuildingBlock());
    }

    @Test
    @Transactional
    @DisplayName("4. OtpDao should handle OTP insert, lookup, rate-limiting, and verification")
    void testOtpDaoLifecycle() {
        EmailOtpVerification otp = new EmailOtpVerification();
        otp.setEmail("temp_test@srmap.edu.in");
        otp.setRegNo("AP25110099999");
        otp.setOtpHash("$2a$10$dummyHashForTestPurposesOnly");
        otp.setPurpose(OtpPurpose.REGISTRATION);
        otp.setAttemptCount(0);
        otp.setMaxAttempts(5);
        otp.setResendAvailableAt(LocalDateTime.now().plusSeconds(60));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otp.setUsed(false);

        Long id = otpDao.insert(otp);
        assertNotNull(id, "OTP insert should return generated ID");

        Optional<EmailOtpVerification> loaded = otpDao.findLatestActive("temp_test@srmap.edu.in", "AP25110099999", OtpPurpose.REGISTRATION);
        assertTrue(loaded.isPresent());
        assertEquals(0, loaded.get().getAttemptCount());

        otpDao.incrementAttemptCount(id);
        Optional<EmailOtpVerification> afterAttempt = otpDao.findLatestActive("temp_test@srmap.edu.in", "AP25110099999", OtpPurpose.REGISTRATION);
        assertTrue(afterAttempt.isPresent());
        assertEquals(1, afterAttempt.get().getAttemptCount());

        otpDao.markVerifiedAndUsed(id);
        Optional<EmailOtpVerification> afterUsed = otpDao.findLatestActive("temp_test@srmap.edu.in", "AP25110099999", OtpPurpose.REGISTRATION);
        assertTrue(afterUsed.isEmpty(), "Used OTP should not be returned as active");
    }

    @Test
    @DisplayName("5. CategoryRoutingSlaDao should correctly route categories and retrieve SLAs")
    void testCategoryRoutingSlaDao() {
        List<GrievanceCategory> categories = categoryRoutingSlaDao.findAllCategories();
        assertTrue(categories.size() >= 8, "Should have all campus categories configured");

        // AC_COOLING in HOSTEL -> HOSTEL_MAINT (dept 1)
        Optional<Integer> hostelDept = categoryRoutingSlaDao.resolveTargetDepartment(1, LocationScope.HOSTEL);
        assertTrue(hostelDept.isPresent());
        assertEquals(1, hostelDept.get());

        // WATER_OUTAGE in HOSTEL -> WATER_PLUMBING (dept 2)
        Optional<Integer> waterDept = categoryRoutingSlaDao.resolveTargetDepartment(2, LocationScope.HOSTEL);
        assertTrue(waterDept.isPresent());
        assertEquals(2, waterDept.get());

        // SLA policy check
        Optional<SlaPolicy> sla = categoryRoutingSlaDao.findSlaPolicy(2, Priority.HIGH);
        assertTrue(sla.isPresent());
        assertEquals(60, sla.get().getAckDeadlineMinutes());
        assertEquals(360, sla.get().getResolutionDeadlineMinutes());
    }

    @Test
    @DisplayName("6. MasterIncidentDao should load candidate incidents and active building banners")
    void testMasterIncidentDao() {
        List<MasterIncident> candidates = masterIncidentDao.findActiveCandidates(2, "Ganga Tower - Block A", 6);
        assertFalse(candidates.isEmpty(), "Should find active candidate for water outage in Ganga Tower - Block A");

        List<MasterIncident> banner = masterIncidentDao.findActiveByBuildingBlock("Ganga Tower - Block A");
        assertFalse(banner.isEmpty(), "Should find active banner for students in Ganga Tower - Block A");
        assertEquals("Water Supply Cut", banner.get(0).getCategoryDisplayName());
    }

    @Test
    @DisplayName("7. GrievanceTicketDao should enforce student isolation and query tickets")
    void testGrievanceTicketDao() {
        // Suraj (user_id = 18 from seed data)
        Optional<User> suraj = userDao.findByRegOrStaffId("AP25110070893");
        assertTrue(suraj.isPresent());

        List<GrievanceTicket> studentTickets = grievanceTicketDao.findByStudentUserId(suraj.get().getUserId());
        assertFalse(studentTickets.isEmpty(), "Suraj should have seeded tickets");
        for (GrievanceTicket t : studentTickets) {
            assertEquals(suraj.get().getUserId(), t.getStudentUserId(), "Strict RBAC: student user ID must match");
        }

        // Dept 2 (Water) isolation
        List<GrievanceTicket> deptTickets = grievanceTicketDao.findByDepartmentId(2);
        assertFalse(deptTickets.isEmpty());
        for (GrievanceTicket t : deptTickets) {
            assertEquals(2, t.getAssignedDeptId());
        }
    }

    @Test
    @Transactional
    @DisplayName("8. TicketLifecycleDao should track status history, messages, and escalations")
    void testTicketLifecycleDao() {
        List<GrievanceTicket> tickets = grievanceTicketDao.findByDepartmentId(2);
        assertFalse(tickets.isEmpty());
        Long ticketId = tickets.get(0).getTicketId();

        // Status history
        List<ComplaintStatusHistory> history = ticketLifecycleDao.findHistoryByTicketId(ticketId);
        assertNotNull(history);

        // Add message
        Long msgId = ticketLifecycleDao.insertMessage(ticketId, 1L, "ROLE_ADMIN", "Test message from integration test", false);
        assertNotNull(msgId);
        List<TicketMessage> messages = ticketLifecycleDao.findMessagesByTicketId(ticketId);
        assertTrue(messages.stream().anyMatch(m -> m.getMessageId().equals(msgId)));

        // Escalation log
        ticketLifecycleDao.insertEscalationLog(ticketId, 0, 1, "ROLE_DEPT_HEAD", "Test SLA breach");
        List<EscalationLog> logs = ticketLifecycleDao.findEscalationsByTicketId(ticketId);
        assertFalse(logs.isEmpty());
    }

    @Test
    @DisplayName("9. ExecutiveAnalyticsDao should execute SQL views with zero errors")
    void testExecutiveAnalyticsViews() {
        // View 1: Anomaly Radar
        List<CategoryAnomalyRadar> radar = executiveAnalyticsDao.getMonthlyAnomalyRadar();
        assertFalse(radar.isEmpty(), "Anomaly radar view must return all categories");
        assertTrue(radar.size() >= 8, "Expected at least 8 categories in anomaly radar");

        // View 2: Department SLA Performance
        List<DepartmentSlaPerformance> perf = executiveAnalyticsDao.getDepartmentSlaPerformance();
        assertFalse(perf.isEmpty(), "SLA performance view must return all departments");
        assertTrue(perf.size() >= 5, "Expected at least 5 departments in SLA metrics");

        // View 3: Location Heatmap
        List<LocationCategoryHeatmap> heatmap = executiveAnalyticsDao.getLocationCategoryHeatmap();
        assertNotNull(heatmap);
    }
}

