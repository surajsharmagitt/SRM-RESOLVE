package com.srmresolve.dao;

import com.srmresolve.model.AuditLog;
import com.srmresolve.model.ComplaintStatusHistory;
import com.srmresolve.model.EscalationLog;
import com.srmresolve.model.TicketAttachment;
import com.srmresolve.model.TicketMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class TicketLifecycleDao {

    private final JdbcTemplate jdbcTemplate;

    public TicketLifecycleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- 1. Complaint Status History ---
    private final RowMapper<ComplaintStatusHistory> historyRowMapper = (rs, rowNum) -> {
        ComplaintStatusHistory h = new ComplaintStatusHistory();
        h.setHistoryId(rs.getLong("history_id"));
        h.setTicketId(rs.getLong("ticket_id"));
        h.setFromStatus(rs.getString("from_status"));
        h.setToStatus(rs.getString("to_status"));
        h.setChangedByUserId(rs.getLong("changed_by_user_id"));
        h.setActionNote(rs.getString("action_note"));
        h.setChangedAt(rs.getTimestamp("changed_at").toLocalDateTime());
        try {
            h.setChangedByUserName(rs.getString("user_full_name"));
        } catch (Exception ignored) {}
        return h;
    };

    public void insertStatusHistory(Long ticketId, String fromStatus, String toStatus, Long changedByUserId, String actionNote) {
        String sql = "INSERT INTO complaint_status_history (ticket_id, from_status, to_status, changed_by_user_id, action_note) " +
                     "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, ticketId, fromStatus, toStatus, changedByUserId, actionNote);
    }

    public List<ComplaintStatusHistory> findHistoryByTicketId(Long ticketId) {
        String sql = "SELECT h.*, u.full_name AS user_full_name " +
                     "FROM complaint_status_history h " +
                     "JOIN users u ON h.changed_by_user_id = u.user_id " +
                     "WHERE h.ticket_id = ? " +
                     "ORDER BY h.changed_at ASC";
        return jdbcTemplate.query(sql, historyRowMapper, ticketId);
    }

    // --- 2. Ticket Messages (WhatsApp-style conversation) ---
    private final RowMapper<TicketMessage> messageRowMapper = (rs, rowNum) -> {
        TicketMessage m = new TicketMessage();
        m.setMessageId(rs.getLong("message_id"));
        m.setTicketId(rs.getLong("ticket_id"));
        m.setSenderUserId(rs.getLong("sender_user_id"));
        m.setSenderRole(rs.getString("sender_role"));
        m.setMessageBody(rs.getString("message_body"));
        m.setSystemAlert(rs.getBoolean("is_system_alert"));
        m.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        try {
            m.setSenderName(rs.getString("sender_name"));
        } catch (Exception ignored) {}
        return m;
    };

    public Long insertMessage(Long ticketId, Long senderUserId, String senderRole, String messageBody, boolean isSystemAlert) {
        String sql = "INSERT INTO ticket_messages (ticket_id, sender_user_id, sender_role, message_body, is_system_alert) " +
                     "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, ticketId);
            ps.setLong(2, senderUserId);
            ps.setString(3, senderRole);
            ps.setString(4, messageBody);
            ps.setBoolean(5, isSystemAlert);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public List<TicketMessage> findMessagesByTicketId(Long ticketId) {
        String sql = "SELECT m.*, u.full_name AS sender_name " +
                     "FROM ticket_messages m " +
                     "JOIN users u ON m.sender_user_id = u.user_id " +
                     "WHERE m.ticket_id = ? " +
                     "ORDER BY m.created_at ASC";
        return jdbcTemplate.query(sql, messageRowMapper, ticketId);
    }

    // --- 3. Ticket Attachments ---
    private final RowMapper<TicketAttachment> attachmentRowMapper = (rs, rowNum) -> {
        TicketAttachment a = new TicketAttachment();
        a.setAttachmentId(rs.getLong("attachment_id"));
        a.setTicketId(rs.getLong("ticket_id"));
        a.setUploadedByUserId(rs.getLong("uploaded_by_user_id"));
        a.setOriginalFileName(rs.getString("original_file_name"));
        a.setContentType(rs.getString("content_type"));
        a.setStoragePath(rs.getString("storage_path"));
        a.setFileSizeBytes(rs.getLong("file_size_bytes"));
        a.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
        return a;
    };

    public Long insertAttachment(TicketAttachment attachment) {
        String sql = "INSERT INTO ticket_attachments (ticket_id, uploaded_by_user_id, original_file_name, content_type, storage_path, file_size_bytes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, attachment.getTicketId());
            ps.setLong(2, attachment.getUploadedByUserId());
            ps.setString(3, attachment.getOriginalFileName());
            ps.setString(4, attachment.getContentType());
            ps.setString(5, attachment.getStoragePath());
            ps.setLong(6, attachment.getFileSizeBytes());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public List<TicketAttachment> findAttachmentsByTicketId(Long ticketId) {
        String sql = "SELECT * FROM ticket_attachments WHERE ticket_id = ? ORDER BY uploaded_at ASC";
        return jdbcTemplate.query(sql, attachmentRowMapper, ticketId);
    }

    public Optional<TicketAttachment> findAttachmentById(Long attachmentId) {
        String sql = "SELECT * FROM ticket_attachments WHERE attachment_id = ?";
        List<TicketAttachment> list = jdbcTemplate.query(sql, attachmentRowMapper, attachmentId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    // --- 4. Escalation Logs ---
    private final RowMapper<EscalationLog> escalationRowMapper = (rs, rowNum) -> {
        EscalationLog log = new EscalationLog();
        log.setLogId(rs.getLong("log_id"));
        log.setTicketId(rs.getLong("ticket_id"));
        log.setPreviousLevel(rs.getInt("previous_level"));
        log.setNewLevel(rs.getInt("new_level"));
        log.setEscalatedToRole(rs.getString("escalated_to_role"));
        log.setReason(rs.getString("reason"));
        log.setEscalatedAt(rs.getTimestamp("escalated_at").toLocalDateTime());
        return log;
    };

    public void insertEscalationLog(Long ticketId, int previousLevel, int newLevel, String escalatedToRole, String reason) {
        String sql = "INSERT INTO escalation_logs (ticket_id, previous_level, new_level, escalated_to_role, reason) " +
                     "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, ticketId, previousLevel, newLevel, escalatedToRole, reason);
    }

    public List<EscalationLog> findEscalationsByTicketId(Long ticketId) {
        String sql = "SELECT * FROM escalation_logs WHERE ticket_id = ? ORDER BY escalated_at ASC";
        return jdbcTemplate.query(sql, escalationRowMapper, ticketId);
    }

    // --- 5. Audit Logs ---
    public void insertAuditLog(Long actorUserId, String actionType, String targetEntity, String targetId, String details) {
        String sql = "INSERT INTO audit_logs (actor_user_id, action_type, target_entity, target_id, details) " +
                     "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            if (actorUserId != null) ps.setLong(1, actorUserId); else ps.setNull(1, Types.BIGINT);
            ps.setString(2, actionType);
            ps.setString(3, targetEntity);
            if (targetId != null) ps.setString(4, targetId); else ps.setNull(4, Types.VARCHAR);
            ps.setString(5, details);
            return ps;
        });
    }
}

