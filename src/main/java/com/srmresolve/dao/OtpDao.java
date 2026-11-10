package com.srmresolve.dao;

import com.srmresolve.model.EmailOtpVerification;
import com.srmresolve.model.OtpPurpose;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class OtpDao {

    private final JdbcTemplate jdbcTemplate;

    public OtpDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<EmailOtpVerification> rowMapper = (rs, rowNum) -> {
        EmailOtpVerification otp = new EmailOtpVerification();
        otp.setOtpId(rs.getLong("otp_id"));
        otp.setEmail(rs.getString("email"));
        otp.setRegNo(rs.getString("reg_no"));
        otp.setOtpHash(rs.getString("otp_hash"));
        otp.setPurpose(OtpPurpose.valueOf(rs.getString("purpose")));
        otp.setAttemptCount(rs.getInt("attempt_count"));
        otp.setMaxAttempts(rs.getInt("max_attempts"));
        otp.setResendAvailableAt(rs.getTimestamp("resend_available_at") != null ? rs.getTimestamp("resend_available_at").toLocalDateTime() : null);
        otp.setExpiresAt(rs.getTimestamp("expires_at") != null ? rs.getTimestamp("expires_at").toLocalDateTime() : null);
        otp.setUsed(rs.getBoolean("is_used"));
        otp.setVerifiedAt(rs.getTimestamp("verified_at") != null ? rs.getTimestamp("verified_at").toLocalDateTime() : null);
        otp.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return otp;
    };

    public Long insert(EmailOtpVerification otp) {
        String sql = "INSERT INTO email_otp_verifications (email, reg_no, otp_hash, purpose, attempt_count, max_attempts, " +
                     "resend_available_at, expires_at, is_used) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, otp.getEmail());
            ps.setString(2, otp.getRegNo());
            ps.setString(3, otp.getOtpHash());
            ps.setString(4, otp.getPurpose().name());
            ps.setInt(5, otp.getAttemptCount());
            ps.setInt(6, otp.getMaxAttempts());
            ps.setTimestamp(7, Timestamp.valueOf(otp.getResendAvailableAt()));
            ps.setTimestamp(8, Timestamp.valueOf(otp.getExpiresAt()));
            ps.setBoolean(9, otp.isUsed());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public Optional<EmailOtpVerification> findLatestActive(String email, String regNo, OtpPurpose purpose) {
        String sql = "SELECT * FROM email_otp_verifications " +
                     "WHERE LOWER(email) = LOWER(?) AND UPPER(reg_no) = UPPER(?) AND purpose = ? AND is_used = FALSE " +
                     "ORDER BY created_at DESC LIMIT 1";
        List<EmailOtpVerification> list = jdbcTemplate.query(sql, rowMapper, email.trim(), regNo.trim(), purpose.name());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void incrementAttemptCount(Long otpId) {
        String sql = "UPDATE email_otp_verifications SET attempt_count = attempt_count + 1 WHERE otp_id = ?";
        jdbcTemplate.update(sql, otpId);
    }

    public void markVerifiedAndUsed(Long otpId) {
        String sql = "UPDATE email_otp_verifications SET is_used = TRUE, verified_at = CURRENT_TIMESTAMP WHERE otp_id = ?";
        jdbcTemplate.update(sql, otpId);
    }

    public void invalidateExistingOtps(String email, String regNo, OtpPurpose purpose) {
        String sql = "UPDATE email_otp_verifications SET is_used = TRUE WHERE LOWER(email) = LOWER(?) AND UPPER(reg_no) = UPPER(?) AND purpose = ? AND is_used = FALSE";
        jdbcTemplate.update(sql, email.trim(), regNo.trim(), purpose.name());
    }

    public List<EmailOtpVerification> findAllActiveByEmail(String email, OtpPurpose purpose) {
        String sql = "SELECT * FROM email_otp_verifications " +
                     "WHERE LOWER(email) = LOWER(?) AND purpose = ? AND is_used = FALSE " +
                     "ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, email.trim(), purpose.name());
    }

    public Optional<EmailOtpVerification> findLatestActiveByEmail(String email, OtpPurpose purpose) {
        String sql = "SELECT * FROM email_otp_verifications " +
                     "WHERE LOWER(email) = LOWER(?) AND purpose = ? AND is_used = FALSE " +
                     "ORDER BY created_at DESC LIMIT 1";
        List<EmailOtpVerification> list = jdbcTemplate.query(sql, rowMapper, email.trim(), purpose.name());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void invalidateExistingOtpsByEmail(String email, OtpPurpose purpose) {
        String sql = "UPDATE email_otp_verifications SET is_used = TRUE WHERE LOWER(email) = LOWER(?) AND purpose = ? AND is_used = FALSE";
        jdbcTemplate.update(sql, email.trim(), purpose.name());
    }
}

