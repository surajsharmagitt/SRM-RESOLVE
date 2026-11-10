package com.srmresolve.service;

import com.srmresolve.dao.TicketLifecycleDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Viva Demo Simulator Service.
 * Simulates real-time 100-complaint spikes to evaluate SQL anomaly radar views.
 * Implements strict child-first deletion order to guarantee zero FK crashes on reset (Section 4.E).
 */
@Service
public class VivaSimulationService {

    private static final Logger log = LoggerFactory.getLogger(VivaSimulationService.class);

    private final JdbcTemplate jdbcTemplate;
    private final TicketLifecycleDao lifecycleDao;

    @Value("${app.demo-mode:false}")
    private boolean demoMode;

    public VivaSimulationService(JdbcTemplate jdbcTemplate, TicketLifecycleDao lifecycleDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.lifecycleDao = lifecycleDao;
    }

    public boolean isDemoModeEnabled() {
        return demoMode;
    }

    /**
     * Simulates a 100-ticket campus complaint spike:
     * 4 master incidents + 80 linked tickets + 20 independent tickets.
     */
    @Transactional
    public void simulate100ComplaintSpike(Long actorUserId) {
        checkDemoMode();

        log.info("Simulating 100-complaint campus incident spike...");

        // 1. Create 4 synthetic master incidents
        // Incident 1: Water Outage in Vedavathi Tower (Category 2, Dept 2)
        jdbcTemplate.update("INSERT INTO master_incidents (category_id, assigned_dept_id, building_block, incident_title, normalized_tokens, incident_status, affected_student_count, is_synthetic_demo) " +
                "VALUES (2, 2, 'Vedavathi Tower', 'Major water pipeline breach in Vedavathi Tower', 'water pipeline breach vedavathi', 'OPEN', 20, TRUE)");
        Long m1 = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        // Incident 2: Wi-Fi Outage in Yamuna Tower (Category 6, Dept 5)
        jdbcTemplate.update("INSERT INTO master_incidents (category_id, assigned_dept_id, building_block, incident_title, normalized_tokens, incident_status, affected_student_count, is_synthetic_demo) " +
                "VALUES (6, 5, 'Yamuna Tower', 'Access point switch malfunction on floors 3-5', 'wifi access point switch malfunction yamuna', 'OPEN', 20, TRUE)");
        Long m2 = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        // Incident 3: Mess Food Quality in Annapoorna Mess Complex (Category 4, Dept 3)
        jdbcTemplate.update("INSERT INTO master_incidents (category_id, assigned_dept_id, building_block, incident_title, normalized_tokens, incident_status, affected_student_count, is_synthetic_demo) " +
                "VALUES (4, 3, 'Annapoorna Mess Complex', 'Undercooked rice batch served at dinner', 'undercooked rice batch dinner mess food', 'OPEN', 20, TRUE)");
        Long m3 = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        // Incident 4: Hot/Cold Water Dispenser in Ganga Tower - Block A (Category 3, Dept 2)
        jdbcTemplate.update("INSERT INTO master_incidents (category_id, assigned_dept_id, building_block, incident_title, normalized_tokens, incident_status, affected_student_count, is_synthetic_demo) " +
                "VALUES (3, 2, 'Ganga Tower - Block A', 'Water dispenser 2nd floor leaking and no cold water', 'dispenser leaking no cold water ganga', 'OPEN', 20, TRUE)");
        Long m4 = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        // Student IDs to distribute among (Suraj=18, Ananya=19, Rahul=20, Priya=21, Vikram=22)
        Long[] studentIds = {18L, 19L, 20L, 21L, 22L};

        // 2. Insert 80 tickets linked across the 4 master incidents (20 each)
        insertGroupedBatch(m1, 2, 2, "Vedavathi Tower", "Water supply cut off in Vedavathi Tower", studentIds, 1, 20);
        insertGroupedBatch(m2, 6, 5, "Yamuna Tower", "Wi-Fi not working on my floor in Yamuna Tower", studentIds, 21, 40);
        insertGroupedBatch(m3, 4, 3, "Annapoorna Mess Complex", "Food quality issue at Annapoorna dinner mess", studentIds, 41, 60);
        insertGroupedBatch(m4, 3, 2, "Ganga Tower - Block A", "Water dispenser not cooling in Ganga Tower Block A", studentIds, 61, 80);

        // 3. Insert 20 independent tickets (master_incident_id = NULL)
        // 10 AC issues + 10 Room cleaning issues
        for (int i = 81; i <= 90; i++) {
            Long studentId = studentIds[i % studentIds.length];
            String ref = String.format("DEMO-2026-%06d", i);
            jdbcTemplate.update("INSERT INTO grievance_tickets (ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, " +
                    "location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at, is_synthetic_demo) " +
                    "VALUES (?, ?, 1, 1, NULL, 'HOSTEL', 'Ganga Tower', ?, 'AC not cooling in my room', 'MEDIUM', 'SUBMITTED', " +
                    "DATE_ADD(NOW(), INTERVAL 240 MINUTE), DATE_ADD(NOW(), INTERVAL 1440 MINUTE), TRUE)",
                    ref, studentId, "Room-" + (400 + i));
            Long ticketId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            lifecycleDao.insertStatusHistory(ticketId, null, "SUBMITTED", studentId, "Synthetic ticket filed for demo.");
        }

        for (int i = 91; i <= 100; i++) {
            Long studentId = studentIds[i % studentIds.length];
            String ref = String.format("DEMO-2026-%06d", i);
            jdbcTemplate.update("INSERT INTO grievance_tickets (ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, " +
                    "location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at, is_synthetic_demo) " +
                    "VALUES (?, ?, 5, 4, NULL, 'HOSTEL', 'Vedavathi Tower', ?, 'Room cleaning requested', 'LOW', 'SUBMITTED', " +
                    "DATE_ADD(NOW(), INTERVAL 480 MINUTE), DATE_ADD(NOW(), INTERVAL 2880 MINUTE), TRUE)",
                    ref, studentId, "Room-" + (200 + i));
            Long ticketId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            lifecycleDao.insertStatusHistory(ticketId, null, "SUBMITTED", studentId, "Synthetic ticket filed for demo.");
        }

        lifecycleDao.insertAuditLog(actorUserId, "SIMULATE_SPIKE", "GRIEVANCE_TICKETS", "BATCH-100",
                "Simulated 100-complaint spike (4 master incidents, 80 grouped reports, 20 independent reports).");
        log.info("100 synthetic complaints simulated successfully.");
    }

    private void insertGroupedBatch(Long masterId, int categoryId, int deptId, String building, String desc, Long[] studentIds, int startSeq, int endSeq) {
        for (int i = startSeq; i <= endSeq; i++) {
            Long studentId = studentIds[i % studentIds.length];
            String ref = String.format("DEMO-2026-%06d", i);
            jdbcTemplate.update("INSERT INTO grievance_tickets (ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, " +
                    "location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at, is_synthetic_demo) " +
                    "VALUES (?, ?, ?, ?, ?, 'HOSTEL', ?, ?, ?, 'HIGH', 'SUBMITTED', " +
                    "DATE_ADD(NOW(), INTERVAL 60 MINUTE), DATE_ADD(NOW(), INTERVAL 360 MINUTE), TRUE)",
                    ref, studentId, categoryId, deptId, masterId, building, "Zone-" + i, desc + " (Report #" + i + ")");
            Long ticketId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            lifecycleDao.insertStatusHistory(ticketId, null, "SUBMITTED", studentId, "Synthetic ticket filed for demo.");
        }
    }

    /**
     * Foreign-Key Safe Reset:
     * Deletes in strict child-first order so foreign keys never fail (Section 4.E):
     * 1. ticket_messages
     * 2. ticket_attachments
     * 3. complaint_status_history
     * 4. escalation_logs
     * 5. grievance_tickets
     * 6. master_incidents
     * 7. audit_logs record
     */
    @Transactional
    public void resetDemoData(Long actorUserId) {
        checkDemoMode();

        log.info("Executing Foreign-Key Safe Reset of demo data in child-first order...");

        // 0. in_app_notifications
        jdbcTemplate.update("DELETE FROM in_app_notifications WHERE related_ticket_id IN (SELECT ticket_id FROM grievance_tickets WHERE is_synthetic_demo = TRUE)");

        // 1. ticket_messages
        jdbcTemplate.update("DELETE FROM ticket_messages WHERE ticket_id IN (SELECT ticket_id FROM grievance_tickets WHERE is_synthetic_demo = TRUE)");

        // 2. ticket_attachments
        jdbcTemplate.update("DELETE FROM ticket_attachments WHERE ticket_id IN (SELECT ticket_id FROM grievance_tickets WHERE is_synthetic_demo = TRUE)");

        // 3. complaint_status_history
        jdbcTemplate.update("DELETE FROM complaint_status_history WHERE ticket_id IN (SELECT ticket_id FROM grievance_tickets WHERE is_synthetic_demo = TRUE)");

        // 4. escalation_logs
        jdbcTemplate.update("DELETE FROM escalation_logs WHERE ticket_id IN (SELECT ticket_id FROM grievance_tickets WHERE is_synthetic_demo = TRUE)");

        // 5. grievance_tickets
        int deletedTickets = jdbcTemplate.update("DELETE FROM grievance_tickets WHERE is_synthetic_demo = TRUE");

        // 6. master_incidents
        int deletedIncidents = jdbcTemplate.update("DELETE FROM master_incidents WHERE is_synthetic_demo = TRUE");

        // 7. Audit log
        lifecycleDao.insertAuditLog(actorUserId, "RESET_DEMO_DATA", "SYNTHETIC_DATA", null,
                "Cleaned up " + deletedTickets + " synthetic tickets and " + deletedIncidents + " synthetic master incidents.");

        log.info("Demo data reset cleanly. Deleted {} tickets and {} master incidents.", deletedTickets, deletedIncidents);
    }

    private void checkDemoMode() {
        if (!demoMode) {
            throw new SecurityException("Viva demo simulator actions are disabled in production.");
        }
    }
}

