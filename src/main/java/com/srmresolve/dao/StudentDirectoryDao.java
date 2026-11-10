package com.srmresolve.dao;

import com.srmresolve.model.StudentDirectory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StudentDirectoryDao {

    private final JdbcTemplate jdbcTemplate;

    public StudentDirectoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<StudentDirectory> rowMapper = (rs, rowNum) -> {
        StudentDirectory sd = new StudentDirectory();
        sd.setRegNo(rs.getString("reg_no"));
        sd.setFullName(rs.getString("full_name"));
        sd.setOfficialEmail(rs.getString("official_email"));
        sd.setAdmissionYear(rs.getInt("admission_year"));
        sd.setCurrentStudyYear(rs.getInt("current_study_year"));
        sd.setDepartmentCode(rs.getString("department_code"));
        sd.setSection(rs.getString("section"));
        sd.setActive(rs.getBoolean("is_active"));
        return sd;
    };

    public Optional<StudentDirectory> findByRegNoAndEmail(String regNo, String officialEmail) {
        String sql = "SELECT reg_no, full_name, official_email, admission_year, current_study_year, department_code, section, is_active " +
                     "FROM student_directory WHERE UPPER(reg_no) = UPPER(?) AND LOWER(official_email) = LOWER(?) AND is_active = TRUE";
        List<StudentDirectory> list = jdbcTemplate.query(sql, rowMapper, regNo.trim(), officialEmail.trim());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<StudentDirectory> findByRegNo(String regNo) {
        String sql = "SELECT reg_no, full_name, official_email, admission_year, current_study_year, department_code, section, is_active " +
                     "FROM student_directory WHERE UPPER(reg_no) = UPPER(?)";
        List<StudentDirectory> list = jdbcTemplate.query(sql, rowMapper, regNo.trim());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<StudentDirectory> findByOfficialEmail(String officialEmail) {
        String sql = "SELECT reg_no, full_name, official_email, admission_year, current_study_year, department_code, section, is_active " +
                     "FROM student_directory WHERE LOWER(official_email) = LOWER(?) AND is_active = TRUE";
        List<StudentDirectory> list = jdbcTemplate.query(sql, rowMapper, officialEmail.trim());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}

