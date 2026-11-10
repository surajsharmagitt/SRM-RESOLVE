package com.srmresolve.dao;

import com.srmresolve.dto.TicketSearchCriteria;
import com.srmresolve.model.GrievanceTicket;
import com.srmresolve.model.LocationScope;
import com.srmresolve.model.Priority;
import com.srmresolve.model.TicketStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class GrievanceTicketDao {

    private final JdbcTemplate jdbcTemplate;

    public GrievanceTicketDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<GrievanceTicket> rowMapper = (rs, rowNum) -> {
        GrievanceTicket t = new GrievanceTicket();
        t.setTicketId(rs.getLong("ticket_id"));
        t.setTicketReference(rs.getString("ticket_reference"));
        t.setStudentUserId(rs.getLong("student_user_id"));
        t.setCategoryId(rs.getInt("category_id"));
        t.setAssignedDeptId(rs.getInt("assigned_dept_id"));
        t.setAssignedStaffUserId(rs.getObject("assigned_staff_user_id") != null ? rs.getLong("assigned_staff_user_id") : null);
        t.setMasterIncidentId(rs.getObject("master_incident_id") != null ? rs.getLong("master_incident_id") : null);
        t.setLocationScope(LocationScope.valueOf(rs.getString("location_scope")));
        t.setBuildingBlock(rs.getString("building_block"));
        t.setRoomOrLocation(rs.getString("room_or_location"));
        t.setRawDescription(rs.getString("raw_description"));
        t.setAiEnhancedDescription(rs.getString("ai_enhanced_description"));
        t.setPriority(Priority.valueOf(rs.getString("priority")));
        t.setStatus(TicketStatus.valueOf(rs.getString("status")));
        t.setEscalated(rs.getBoolean("is_escalated"));
        t.setEscalationLevel(rs.getInt("escalation_level"));
        t.setAckDeadlineAt(rs.getTimestamp("ack_deadline_at").toLocalDateTime());
        t.setResolutionDeadlineAt(rs.getTimestamp("resolution_deadline_at").toLocalDateTime());
        t.setAcknowledgedAt(rs.getTimestamp("acknowledged_at") != null ? rs.getTimestamp("acknowledged_at").toLocalDateTime() : null);
        t.setEtaGivenAt(rs.getTimestamp("eta_given_at") != null ? rs.getTimestamp("eta_given_at").toLocalDateTime() : null);
        t.setResolvedByDeptAt(rs.getTimestamp("resolved_by_dept_at") != null ? rs.getTimestamp("resolved_by_dept_at").toLocalDateTime() : null);
        t.setReopenedAt(rs.getTimestamp("reopened_at") != null ? rs.getTimestamp("reopened_at").toLocalDateTime() : null);
        t.setReopenCount(rs.getInt("reopen_count"));
        t.setClosedByStudentAt(rs.getTimestamp("closed_by_student_at") != null ? rs.getTimestamp("closed_by_student_at").toLocalDateTime() : null);
        try {
            t.setStudentRating(rs.getObject("student_rating") != null ? rs.getInt("student_rating") : null);
            t.setStudentFeedback(rs.getString("student_feedback"));
        } catch (Exception ignored) {}
        t.setSyntheticDemo(rs.getBoolean("is_synthetic_demo"));
        t.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        t.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);

        // Joined labels
        try {
            t.setStudentName(rs.getString("student_name"));
        } catch (Exception ignored) {}
        try {
            t.setStudentRegNo(rs.getString("student_reg_no"));
        } catch (Exception ignored) {}
        try {
            t.setCategoryDisplayName(rs.getString("category_display_name"));
        } catch (Exception ignored) {}
        try {
            t.setDepartmentName(rs.getString("dept_name"));
        } catch (Exception ignored) {}
        try {
            t.setAssignedStaffName(rs.getString("assigned_staff_name"));
        } catch (Exception ignored) {}

        return t;
    };

    private static final String BASE_SELECT =
            "SELECT t.*, " +
            "       u.full_name AS student_name, u.reg_or_staff_id AS student_reg_no, " +
            "       c.display_name AS category_display_name, " +
            "       d.dept_name, " +
            "       s.full_name AS assigned_staff_name " +
            "FROM grievance_tickets t " +
            "JOIN users u ON t.student_user_id = u.user_id " +
            "JOIN grievance_categories c ON t.category_id = c.category_id " +
            "JOIN departments d ON t.assigned_dept_id = d.dept_id " +
            "LEFT JOIN users s ON t.assigned_staff_user_id = s.user_id ";

    public Optional<GrievanceTicket> findById(Long ticketId) {
        String sql = BASE_SELECT + "WHERE t.ticket_id = ?";
        List<GrievanceTicket> list = jdbcTemplate.query(sql, rowMapper, ticketId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<GrievanceTicket> findByReference(String reference) {
        String sql = BASE_SELECT + "WHERE t.ticket_reference = ?";
        List<GrievanceTicket> list = jdbcTemplate.query(sql, rowMapper, reference);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Strict Student RBAC: Only view student's own tickets.
     */
    public List<GrievanceTicket> findByStudentUserId(Long studentUserId) {
        String sql = BASE_SELECT + "WHERE t.student_user_id = ? ORDER BY t.created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, studentUserId);
    }

    /**
     * Strict Department Staff/Head RBAC: Only view tickets assigned to their department.
     */
    public List<GrievanceTicket> findByDepartmentId(Integer deptId) {
        String sql = BASE_SELECT + "WHERE t.assigned_dept_id = ? ORDER BY t.created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, deptId);
    }

    /**
     * Executive / VC view: institution-wide view with optional escalation filter.
     */
    public List<GrievanceTicket> findAll(boolean escalatedOnly) {
        String sql = BASE_SELECT + (escalatedOnly ? "WHERE t.is_escalated = TRUE " : "") + "ORDER BY t.created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Long insert(GrievanceTicket t) {
        String sql = "INSERT INTO grievance_tickets (" +
                     "  ticket_reference, student_user_id, category_id, assigned_dept_id, assigned_staff_user_id, " +
                     "  master_incident_id, location_scope, building_block, room_or_location, raw_description, " +
                     "  ai_enhanced_description, priority, status, is_escalated, escalation_level, " +
                     "  ack_deadline_at, resolution_deadline_at, is_synthetic_demo" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, t.getTicketReference());
            ps.setLong(2, t.getStudentUserId());
            ps.setInt(3, t.getCategoryId());
            ps.setInt(4, t.getAssignedDeptId());
            if (t.getAssignedStaffUserId() != null) ps.setLong(5, t.getAssignedStaffUserId()); else ps.setNull(5, Types.BIGINT);
            if (t.getMasterIncidentId() != null) ps.setLong(6, t.getMasterIncidentId()); else ps.setNull(6, Types.BIGINT);
            ps.setString(7, t.getLocationScope().name());
            ps.setString(8, t.getBuildingBlock());
            ps.setString(9, t.getRoomOrLocation());
            ps.setString(10, t.getRawDescription());
            if (t.getAiEnhancedDescription() != null) ps.setString(11, t.getAiEnhancedDescription()); else ps.setNull(11, Types.VARCHAR);
            ps.setString(12, t.getPriority().name());
            ps.setString(13, t.getStatus().name());
            ps.setBoolean(14, t.isEscalated());
            ps.setInt(15, t.getEscalationLevel());
            ps.setTimestamp(16, Timestamp.valueOf(t.getAckDeadlineAt()));
            ps.setTimestamp(17, Timestamp.valueOf(t.getResolutionDeadlineAt()));
            ps.setBoolean(18, t.isSyntheticDemo());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            t.setTicketId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    public void updateStatus(Long ticketId, TicketStatus status) {
        String sql = "UPDATE grievance_tickets SET status = ? WHERE ticket_id = ?";
        jdbcTemplate.update(sql, status.name(), ticketId);
    }

    public void recordAcknowledgment(Long ticketId, Long staffUserId, LocalDateTime ackAt) {
        String sql = "UPDATE grievance_tickets SET status = 'ACKNOWLEDGED', acknowledged_at = ?, assigned_staff_user_id = ? WHERE ticket_id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(ackAt), staffUserId, ticketId);
    }

    public void updateEta(Long ticketId, LocalDateTime eta) {
        String sql = "UPDATE grievance_tickets SET status = 'IN_PROGRESS', eta_given_at = ? WHERE ticket_id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(eta), ticketId);
    }

    public void recordResolutionByDept(Long ticketId, LocalDateTime resolvedAt) {
        String sql = "UPDATE grievance_tickets SET status = 'AWAITING_STUDENT_CONFIRMATION', resolved_by_dept_at = ? WHERE ticket_id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(resolvedAt), ticketId);
    }

    public void recordClosureByStudent(Long ticketId, LocalDateTime closedAt) {
        recordClosureByStudent(ticketId, closedAt, null, null);
    }

    public void recordClosureByStudent(Long ticketId, LocalDateTime closedAt, Integer rating, String feedback) {
        String sql = "UPDATE grievance_tickets SET status = 'CLOSED', closed_by_student_at = ?, student_rating = ?, student_feedback = ? WHERE ticket_id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(closedAt), rating, feedback, ticketId);
    }

    public void recordReopenedByStudent(Long ticketId, LocalDateTime reopenedAt) {
        String sql = "UPDATE grievance_tickets SET status = 'REOPENED', reopened_at = ?, reopen_count = reopen_count + 1 WHERE ticket_id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(reopenedAt), ticketId);
    }

    public void updateStaffAssignment(Long ticketId, Long staffUserId) {
        String sql = "UPDATE grievance_tickets SET assigned_staff_user_id = ? WHERE ticket_id = ?";
        jdbcTemplate.update(sql, staffUserId, ticketId);
    }

    public void updateEscalation(Long ticketId, int escalationLevel, boolean isEscalated) {
        String sql = "UPDATE grievance_tickets SET escalation_level = ?, is_escalated = ? WHERE ticket_id = ?";
        jdbcTemplate.update(sql, escalationLevel, isEscalated, ticketId);
    }

    public void updatePriority(Long ticketId, Priority priority) {
        String sql = "UPDATE grievance_tickets SET priority = ? WHERE ticket_id = ?";
        jdbcTemplate.update(sql, priority.name(), ticketId);
    }

    /**
     * Level 1 SLA Escalation Finder:
     * escalation_level = 0 AND status NOT IN ('AWAITING_STUDENT_CONFIRMATION', 'CLOSED')
     * AND ((acknowledged_at IS NULL AND NOW() > ack_deadline_at) OR (NOW() > resolution_deadline_at))
     */
    public List<GrievanceTicket> findCandidatesForLevel1Escalation() {
        String sql = BASE_SELECT +
                     "WHERE t.escalation_level = 0 " +
                     "AND t.status NOT IN ('AWAITING_STUDENT_CONFIRMATION', 'CLOSED') " +
                     "AND ((t.acknowledged_at IS NULL AND NOW() > t.ack_deadline_at) OR (NOW() > t.resolution_deadline_at))";
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * Level 2 SLA Escalation Finder:
     * escalation_level = 1 AND status NOT IN ('AWAITING_STUDENT_CONFIRMATION', 'CLOSED')
     * AND TIMESTAMPDIFF(MINUTE, resolution_deadline_at, NOW()) > 120
     */
    public List<GrievanceTicket> findCandidatesForLevel2Escalation() {
        String sql = BASE_SELECT +
                     "WHERE t.escalation_level = 1 " +
                     "AND t.status NOT IN ('AWAITING_STUDENT_CONFIRMATION', 'CLOSED') " +
                     "AND TIMESTAMPDIFF(MINUTE, t.resolution_deadline_at, NOW()) > 120";
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * Count tickets for reference sequence generator (e.g. SRM-2026-000006).
     */
    public long getNextTicketSequence() {
        String sql = "SELECT COUNT(*) FROM grievance_tickets";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return (count != null ? count : 0) + 1;
    }

    /**
     * Advanced dynamic SQL search engine.
     */
    public List<GrievanceTicket> searchTickets(TicketSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            String kw = "%" + criteria.getKeyword().trim() + "%";
            sql.append("AND (t.ticket_reference LIKE ? OR t.raw_description LIKE ? OR t.room_or_location LIKE ? OR u.full_name LIKE ? OR u.reg_or_staff_id LIKE ?) ");
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (criteria.getDeptId() != null) {
            sql.append("AND t.assigned_dept_id = ? ");
            params.add(criteria.getDeptId());
        }

        if (criteria.getCategoryId() != null) {
            sql.append("AND t.category_id = ? ");
            params.add(criteria.getCategoryId());
        }

        if (criteria.getStatus() != null) {
            sql.append("AND t.status = ? ");
            params.add(criteria.getStatus().name());
        }

        if (criteria.getPriority() != null) {
            sql.append("AND t.priority = ? ");
            params.add(criteria.getPriority().name());
        }

        if (Boolean.TRUE.equals(criteria.getEscalatedOnly())) {
            sql.append("AND t.is_escalated = TRUE ");
        }

        if (Boolean.TRUE.equals(criteria.getOverdueOnly())) {
            sql.append("AND t.status NOT IN ('CLOSED', 'AWAITING_STUDENT_CONFIRMATION') AND NOW() > t.resolution_deadline_at ");
        }

        if (criteria.getBuildingBlock() != null && !criteria.getBuildingBlock().isBlank()) {
            sql.append("AND t.building_block = ? ");
            params.add(criteria.getBuildingBlock().trim());
        }

        if (criteria.getLocationScope() != null) {
            sql.append("AND t.location_scope = ? ");
            params.add(criteria.getLocationScope().name());
        }

        if (criteria.getStudentUserId() != null) {
            sql.append("AND t.student_user_id = ? ");
            params.add(criteria.getStudentUserId());
        }

        if (criteria.getStaffUserId() != null) {
            sql.append("AND t.assigned_staff_user_id = ? ");
            params.add(criteria.getStaffUserId());
        }

        if (criteria.getDateFrom() != null) {
            sql.append("AND t.created_at >= ? ");
            params.add(Timestamp.valueOf(criteria.getDateFrom()));
        }

        if (criteria.getDateTo() != null) {
            sql.append("AND t.created_at <= ? ");
            params.add(Timestamp.valueOf(criteria.getDateTo()));
        }

        sql.append("ORDER BY t.created_at DESC ");

        if (criteria.getLimit() != null && criteria.getLimit() > 0) {
            sql.append("LIMIT ? ");
            params.add(criteria.getLimit());
            if (criteria.getOffset() != null && criteria.getOffset() > 0) {
                sql.append("OFFSET ? ");
                params.add(criteria.getOffset());
            }
        }

        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    /**
     * KPI Aggregate: Count tickets by status with optional department filter.
     */
    public Map<TicketStatus, Long> countTicketsByStatus(Integer deptId) {
        String sql = "SELECT status, COUNT(*) AS count_val FROM grievance_tickets " +
                     (deptId != null ? "WHERE assigned_dept_id = ? " : "") +
                     "GROUP BY status";
        List<Map.Entry<TicketStatus, Long>> list;
        if (deptId != null) {
            list = jdbcTemplate.query(sql, (rs, rowNum) ->
                Map.entry(TicketStatus.valueOf(rs.getString("status")), rs.getLong("count_val")), deptId);
        } else {
            list = jdbcTemplate.query(sql, (rs, rowNum) ->
                Map.entry(TicketStatus.valueOf(rs.getString("status")), rs.getLong("count_val")));
        }
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * KPI Aggregate: Count tickets by priority with optional department filter.
     */
    public Map<Priority, Long> countTicketsByPriority(Integer deptId) {
        String sql = "SELECT priority, COUNT(*) AS count_val FROM grievance_tickets " +
                     (deptId != null ? "WHERE assigned_dept_id = ? " : "") +
                     "GROUP BY priority";
        List<Map.Entry<Priority, Long>> list;
        if (deptId != null) {
            list = jdbcTemplate.query(sql, (rs, rowNum) ->
                Map.entry(Priority.valueOf(rs.getString("priority")), rs.getLong("count_val")), deptId);
        } else {
            list = jdbcTemplate.query(sql, (rs, rowNum) ->
                Map.entry(Priority.valueOf(rs.getString("priority")), rs.getLong("count_val")));
        }
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Fetch all currently overdue unresolved tickets.
     */
    public List<GrievanceTicket> findOverdueTickets(Integer deptId) {
        String sql = BASE_SELECT +
                     "WHERE t.status NOT IN ('CLOSED', 'AWAITING_STUDENT_CONFIRMATION') " +
                     "AND NOW() > t.resolution_deadline_at " +
                     (deptId != null ? "AND t.assigned_dept_id = ? " : "") +
                     "ORDER BY t.resolution_deadline_at ASC";
        if (deptId != null) {
            return jdbcTemplate.query(sql, rowMapper, deptId);
        } else {
            return jdbcTemplate.query(sql, rowMapper);
        }
    }

    /**
     * Batch operation: Assign staff to multiple tickets in a single SQL update.
     */
    public int bulkAssignStaff(List<Long> ticketIds, Long staffUserId) {
        if (ticketIds == null || ticketIds.isEmpty()) return 0;
        String inSql = String.join(",", Collections.nCopies(ticketIds.size(), "?"));
        String sql = "UPDATE grievance_tickets SET assigned_staff_user_id = ? WHERE ticket_id IN (" + inSql + ")";
        List<Object> params = new ArrayList<>();
        params.add(staffUserId);
        params.addAll(ticketIds);
        return jdbcTemplate.update(sql, params.toArray());
    }

    /**
     * Batch operation: Acknowledge multiple tickets in a single SQL update.
     */
    public int bulkAcknowledge(List<Long> ticketIds, Long staffUserId) {
        if (ticketIds == null || ticketIds.isEmpty()) return 0;
        String inSql = String.join(",", Collections.nCopies(ticketIds.size(), "?"));
        String sql = "UPDATE grievance_tickets SET status = 'ACKNOWLEDGED', acknowledged_at = NOW(), assigned_staff_user_id = ? " +
                     "WHERE ticket_id IN (" + inSql + ") AND status = 'SUBMITTED'";
        List<Object> params = new ArrayList<>();
        params.add(staffUserId);
        params.addAll(ticketIds);
        return jdbcTemplate.update(sql, params.toArray());
    }
}

