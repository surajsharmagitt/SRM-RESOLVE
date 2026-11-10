package com.srmresolve.dao;

import com.srmresolve.model.ResidenceType;
import com.srmresolve.model.Role;
import com.srmresolve.model.User;
import com.srmresolve.model.VerificationStatus;
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
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> rowMapper = (rs, rowNum) -> {
        User u = new User();
        u.setUserId(rs.getLong("user_id"));
        u.setRegOrStaffId(rs.getString("reg_or_staff_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setVerificationStatus(VerificationStatus.valueOf(rs.getString("verification_status")));
        u.setDepartmentCode(rs.getString("department_code"));
        u.setStudyYear(rs.getObject("study_year") != null ? rs.getInt("study_year") : null);
        u.setSection(rs.getString("section"));
        u.setResidenceType(ResidenceType.valueOf(rs.getString("residence_type")));
        u.setBuildingBlock(rs.getString("building_block"));
        u.setRoomOrZone(rs.getString("room_or_zone"));
        u.setAssignedDeptId(rs.getObject("assigned_dept_id") != null ? rs.getInt("assigned_dept_id") : null);
        u.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return u;
    };

    public Optional<User> findById(Long userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> list = jdbcTemplate.query(sql, rowMapper, userId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        List<User> list = jdbcTemplate.query(sql, rowMapper, email.trim());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<User> findByRegOrStaffId(String id) {
        String sql = "SELECT * FROM users WHERE UPPER(reg_or_staff_id) = UPPER(?)";
        List<User> list = jdbcTemplate.query(sql, rowMapper, id.trim());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Long insert(User user) {
        String sql = "INSERT INTO users (reg_or_staff_id, full_name, email, password_hash, role, verification_status, " +
                     "department_code, study_year, section, residence_type, building_block, room_or_zone, assigned_dept_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getRegOrStaffId());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getVerificationStatus().name());
            if (user.getDepartmentCode() != null) ps.setString(7, user.getDepartmentCode()); else ps.setNull(7, Types.VARCHAR);
            if (user.getStudyYear() != null) ps.setInt(8, user.getStudyYear()); else ps.setNull(8, Types.INTEGER);
            if (user.getSection() != null) ps.setString(9, user.getSection()); else ps.setNull(9, Types.VARCHAR);
            ps.setString(10, user.getResidenceType().name());
            if (user.getBuildingBlock() != null) ps.setString(11, user.getBuildingBlock()); else ps.setNull(11, Types.VARCHAR);
            if (user.getRoomOrZone() != null) ps.setString(12, user.getRoomOrZone()); else ps.setNull(12, Types.VARCHAR);
            if (user.getAssignedDeptId() != null) ps.setInt(13, user.getAssignedDeptId()); else ps.setNull(13, Types.INTEGER);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            user.setUserId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    public void updateVerificationStatus(Long userId, VerificationStatus status) {
        String sql = "UPDATE users SET verification_status = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, status.name(), userId);
    }

    public void updatePassword(Long userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, passwordHash, userId);
    }

    public List<User> findStaffByDepartment(Integer deptId) {
        String sql = "SELECT * FROM users WHERE assigned_dept_id = ? AND role IN ('ROLE_DEPT_STAFF', 'ROLE_DEPT_HEAD') ORDER BY full_name";
        return jdbcTemplate.query(sql, rowMapper, deptId);
    }

    public List<User> findPendingVerifications() {
        String sql = "SELECT * FROM users WHERE verification_status = 'PENDING_VERIFICATION' ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public void updateStudentProfile(Long userId, String regNo, String fullName, String departmentCode,
                                     Integer studyYear, String section, ResidenceType residenceType,
                                     String buildingBlock, String roomOrZone) {
        String sql = "UPDATE users SET reg_or_staff_id = ?, full_name = ?, department_code = ?, study_year = ?, " +
                     "section = ?, residence_type = ?, building_block = ?, room_or_zone = ?, verification_status = 'VERIFIED' " +
                     "WHERE user_id = ?";
        jdbcTemplate.update(sql, regNo, fullName, departmentCode, studyYear, section,
                residenceType != null ? residenceType.name() : "NA", buildingBlock, roomOrZone, userId);
    }
}

