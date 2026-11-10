package com.srmresolve.dao;

import com.srmresolve.model.Department;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DepartmentDao {

    private final JdbcTemplate jdbcTemplate;

    public DepartmentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Department> rowMapper = (rs, rowNum) -> {
        Department dept = new Department();
        dept.setDeptId(rs.getInt("dept_id"));
        dept.setDeptCode(rs.getString("dept_code"));
        dept.setDeptName(rs.getString("dept_name"));
        dept.setContactEmail(rs.getString("contact_email"));
        dept.setMonthlyAlertThreshold(rs.getInt("monthly_alert_threshold"));
        dept.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return dept;
    };

    public List<Department> findAll() {
        String sql = "SELECT dept_id, dept_code, dept_name, contact_email, monthly_alert_threshold, created_at FROM departments ORDER BY dept_id";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Department> findById(Integer deptId) {
        String sql = "SELECT dept_id, dept_code, dept_name, contact_email, monthly_alert_threshold, created_at FROM departments WHERE dept_id = ?";
        List<Department> list = jdbcTemplate.query(sql, rowMapper, deptId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Department> findByCode(String deptCode) {
        String sql = "SELECT dept_id, dept_code, dept_name, contact_email, monthly_alert_threshold, created_at FROM departments WHERE dept_code = ?";
        List<Department> list = jdbcTemplate.query(sql, rowMapper, deptCode);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}

