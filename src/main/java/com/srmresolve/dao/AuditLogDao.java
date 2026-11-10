package com.srmresolve.dao;

import com.srmresolve.model.AuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class AuditLogDao {

    private final JdbcTemplate jdbcTemplate;

    public AuditLogDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AuditLog> rowMapper = (rs, rowNum) -> {
        AuditLog a = new AuditLog();
        a.setAuditId(rs.getLong("audit_id"));
        a.setActorUserId(rs.getObject("actor_user_id") != null ? rs.getLong("actor_user_id") : null);
        a.setActionType(rs.getString("action_type"));
        a.setTargetEntity(rs.getString("target_entity"));
        a.setTargetId(rs.getString("target_id"));
        a.setDetails(rs.getString("details"));
        a.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        try {
            a.setActorName(rs.getString("actor_name"));
        } catch (Exception ignored) {}
        try {
            a.setActorRegNo(rs.getString("actor_reg_no"));
        } catch (Exception ignored) {}
        return a;
    };

    private static final String BASE_SELECT =
            "SELECT a.*, u.full_name AS actor_name, u.reg_or_staff_id AS actor_reg_no " +
            "FROM audit_logs a " +
            "LEFT JOIN users u ON a.actor_user_id = u.user_id ";

    public Long insert(AuditLog a) {
        String sql = "INSERT INTO audit_logs (actor_user_id, action_type, target_entity, target_id, details) " +
                     "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (a.getActorUserId() != null) ps.setLong(1, a.getActorUserId()); else ps.setNull(1, Types.BIGINT);
            ps.setString(2, a.getActionType());
            ps.setString(3, a.getTargetEntity());
            if (a.getTargetId() != null) ps.setString(4, a.getTargetId()); else ps.setNull(4, Types.VARCHAR);
            ps.setString(5, a.getDetails());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            a.setAuditId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    public List<AuditLog> findRecent(int limit) {
        String sql = BASE_SELECT + "ORDER BY a.created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public List<AuditLog> findByActor(Long actorUserId, int limit) {
        String sql = BASE_SELECT + "WHERE a.actor_user_id = ? ORDER BY a.created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, actorUserId, limit);
    }

    public List<AuditLog> findByTarget(String targetEntity, String targetId) {
        String sql = BASE_SELECT + "WHERE a.target_entity = ? AND a.target_id = ? ORDER BY a.created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, targetEntity, targetId);
    }

    public List<AuditLog> findByActionType(String actionType, int limit) {
        String sql = BASE_SELECT + "WHERE a.action_type = ? ORDER BY a.created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, actionType, limit);
    }

    public Map<String, Long> countByActionType() {
        String sql = "SELECT action_type, COUNT(*) AS count_val FROM audit_logs GROUP BY action_type";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                Map.entry(rs.getString("action_type"), rs.getLong("count_val"))
        ).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
