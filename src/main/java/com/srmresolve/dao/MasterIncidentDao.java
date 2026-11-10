package com.srmresolve.dao;

import com.srmresolve.model.MasterIncident;
import com.srmresolve.model.MasterIncidentStatus;
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
import java.util.List;
import java.util.Optional;

@Repository
public class MasterIncidentDao {

    private final JdbcTemplate jdbcTemplate;

    public MasterIncidentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MasterIncident> rowMapper = (rs, rowNum) -> {
        MasterIncident mi = new MasterIncident();
        mi.setMasterIncidentId(rs.getLong("master_incident_id"));
        mi.setCategoryId(rs.getInt("category_id"));
        mi.setAssignedDeptId(rs.getInt("assigned_dept_id"));
        mi.setBuildingBlock(rs.getString("building_block"));
        mi.setIncidentTitle(rs.getString("incident_title"));
        mi.setNormalizedTokens(rs.getString("normalized_tokens"));
        mi.setIncidentStatus(MasterIncidentStatus.valueOf(rs.getString("incident_status")));
        mi.setAffectedStudentCount(rs.getInt("affected_student_count"));
        mi.setEtaTimestamp(rs.getTimestamp("eta_timestamp") != null ? rs.getTimestamp("eta_timestamp").toLocalDateTime() : null);
        mi.setSyntheticDemo(rs.getBoolean("is_synthetic_demo"));
        mi.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        mi.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);

        // Try joined columns if present
        try {
            mi.setCategoryDisplayName(rs.getString("category_display_name"));
        } catch (Exception ignored) {}
        try {
            mi.setDepartmentName(rs.getString("dept_name"));
        } catch (Exception ignored) {}

        return mi;
    };

    public Optional<MasterIncident> findById(Long masterIncidentId) {
        String sql = "SELECT m.*, c.display_name AS category_display_name, d.dept_name " +
                     "FROM master_incidents m " +
                     "JOIN grievance_categories c ON m.category_id = c.category_id " +
                     "JOIN departments d ON m.assigned_dept_id = d.dept_id " +
                     "WHERE m.master_incident_id = ?";
        List<MasterIncident> list = jdbcTemplate.query(sql, rowMapper, masterIncidentId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Finds active master incidents for candidate Jaccard comparison within time window.
     */
    public List<MasterIncident> findActiveCandidates(Integer categoryId, String buildingBlock, int windowHours) {
        String sql = "SELECT m.*, c.display_name AS category_display_name, d.dept_name " +
                     "FROM master_incidents m " +
                     "JOIN grievance_categories c ON m.category_id = c.category_id " +
                     "JOIN departments d ON m.assigned_dept_id = d.dept_id " +
                     "WHERE m.category_id = ? " +
                     "AND UPPER(m.building_block) = UPPER(?) " +
                     "AND m.incident_status IN ('OPEN', 'IN_PROGRESS') " +
                     "AND m.created_at >= DATE_SUB(NOW(), INTERVAL ? HOUR) " +
                     "ORDER BY m.created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, categoryId, buildingBlock.trim(), windowHours);
    }

    /**
     * Sanitized block-level active incidents for student banner (zero private student names or room numbers).
     */
    public List<MasterIncident> findActiveByBuildingBlock(String buildingBlock) {
        String sql = "SELECT m.*, c.display_name AS category_display_name, d.dept_name " +
                     "FROM master_incidents m " +
                     "JOIN grievance_categories c ON m.category_id = c.category_id " +
                     "JOIN departments d ON m.assigned_dept_id = d.dept_id " +
                     "WHERE UPPER(m.building_block) = UPPER(?) " +
                     "AND m.incident_status IN ('OPEN', 'IN_PROGRESS') " +
                     "ORDER BY m.created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, buildingBlock.trim());
    }

    public Long insert(MasterIncident mi) {
        String sql = "INSERT INTO master_incidents (category_id, assigned_dept_id, building_block, incident_title, " +
                     "normalized_tokens, incident_status, affected_student_count, eta_timestamp, is_synthetic_demo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, mi.getCategoryId());
            ps.setInt(2, mi.getAssignedDeptId());
            ps.setString(3, mi.getBuildingBlock());
            ps.setString(4, mi.getIncidentTitle());
            ps.setString(5, mi.getNormalizedTokens());
            ps.setString(6, mi.getIncidentStatus().name());
            ps.setInt(7, mi.getAffectedStudentCount());
            if (mi.getEtaTimestamp() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(mi.getEtaTimestamp()));
            } else {
                ps.setNull(8, Types.TIMESTAMP);
            }
            ps.setBoolean(9, mi.isSyntheticDemo());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            mi.setMasterIncidentId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    /**
     * Re-calculates and updates affected_student_count to represent distinct students (Section 6.2).
     */
    public void recalculateDistinctStudentCount(Long masterIncidentId) {
        String sql = "UPDATE master_incidents SET affected_student_count = (" +
                     "  SELECT COUNT(DISTINCT student_user_id) FROM grievance_tickets WHERE master_incident_id = ?" +
                     ") WHERE master_incident_id = ?";
        jdbcTemplate.update(sql, masterIncidentId, masterIncidentId);
    }

    public void updateStatusAndEta(Long masterIncidentId, MasterIncidentStatus status, LocalDateTime eta) {
        String sql = "UPDATE master_incidents SET incident_status = ?, eta_timestamp = ? WHERE master_incident_id = ?";
        Timestamp etaTs = eta != null ? Timestamp.valueOf(eta) : null;
        jdbcTemplate.update(sql, status.name(), etaTs, masterIncidentId);
    }
}

