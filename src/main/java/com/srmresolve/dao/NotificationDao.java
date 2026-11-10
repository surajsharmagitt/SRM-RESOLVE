package com.srmresolve.dao;

import com.srmresolve.model.InAppNotification;
import com.srmresolve.model.NotificationType;
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
public class NotificationDao {

    private final JdbcTemplate jdbcTemplate;

    public NotificationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<InAppNotification> rowMapper = (rs, rowNum) -> {
        InAppNotification n = new InAppNotification();
        n.setNotificationId(rs.getLong("notification_id"));
        n.setRecipientUserId(rs.getLong("recipient_user_id"));
        n.setTitle(rs.getString("title"));
        n.setBody(rs.getString("body"));
        n.setNotificationType(NotificationType.valueOf(rs.getString("notification_type")));
        n.setRelatedTicketId(rs.getObject("related_ticket_id") != null ? rs.getLong("related_ticket_id") : null);
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return n;
    };

    public Long insert(InAppNotification n) {
        String sql = "INSERT INTO in_app_notifications (recipient_user_id, title, body, notification_type, related_ticket_id, is_read) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, n.getRecipientUserId());
            ps.setString(2, n.getTitle());
            ps.setString(3, n.getBody());
            ps.setString(4, n.getNotificationType().name());
            if (n.getRelatedTicketId() != null) {
                ps.setLong(5, n.getRelatedTicketId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            ps.setBoolean(6, n.isRead());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            n.setNotificationId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    public List<InAppNotification> findByRecipient(Long recipientUserId, boolean unreadOnly, int limit) {
        String sql = "SELECT * FROM in_app_notifications WHERE recipient_user_id = ? " +
                     (unreadOnly ? "AND is_read = FALSE " : "") +
                     "ORDER BY created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, recipientUserId, limit);
    }

    public long countUnread(Long recipientUserId) {
        String sql = "SELECT COUNT(*) FROM in_app_notifications WHERE recipient_user_id = ? AND is_read = FALSE";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, recipientUserId);
        return count != null ? count : 0L;
    }

    public void markAsRead(Long notificationId, Long recipientUserId) {
        String sql = "UPDATE in_app_notifications SET is_read = TRUE WHERE notification_id = ? AND recipient_user_id = ?";
        jdbcTemplate.update(sql, notificationId, recipientUserId);
    }

    public void markAllAsRead(Long recipientUserId) {
        String sql = "UPDATE in_app_notifications SET is_read = TRUE WHERE recipient_user_id = ? AND is_read = FALSE";
        jdbcTemplate.update(sql, recipientUserId);
    }

    public Optional<InAppNotification> findById(Long notificationId) {
        String sql = "SELECT * FROM in_app_notifications WHERE notification_id = ?";
        List<InAppNotification> list = jdbcTemplate.query(sql, rowMapper, notificationId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
