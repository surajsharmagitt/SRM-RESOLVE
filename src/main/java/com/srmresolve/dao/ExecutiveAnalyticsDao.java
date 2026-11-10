package com.srmresolve.dao;

import com.srmresolve.model.CategoryAnomalyRadar;
import com.srmresolve.model.DepartmentSatisfactionMetric;
import com.srmresolve.model.DepartmentSlaPerformance;
import com.srmresolve.model.LocationCategoryHeatmap;
import com.srmresolve.model.LocationScope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExecutiveAnalyticsDao {

    private final JdbcTemplate jdbcTemplate;

    public ExecutiveAnalyticsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<DepartmentSatisfactionMetric> satisfactionRowMapper = (rs, rowNum) -> {
        DepartmentSatisfactionMetric m = new DepartmentSatisfactionMetric();
        m.setDeptId(rs.getInt("dept_id"));
        m.setDeptCode(rs.getString("dept_code"));
        m.setDeptName(rs.getString("dept_name"));
        m.setTotalClosed(rs.getLong("total_closed"));
        Object rating = rs.getObject("avg_student_rating");
        m.setAvgStudentRating(rating != null ? ((Number) rating).doubleValue() : null);
        m.setPositiveRatingsCount(rs.getLong("positive_ratings_count"));
        m.setNegativeRatingsCount(rs.getLong("negative_ratings_count"));
        Object resp = rs.getObject("avg_first_response_minutes");
        m.setAvgFirstResponseMinutes(resp != null ? ((Number) resp).doubleValue() : null);
        Object turn = rs.getObject("avg_turnaround_minutes");
        m.setAvgTurnaroundMinutes(turn != null ? ((Number) turn).doubleValue() : null);
        Object sla = rs.getObject("sla_resolution_compliance_pct");
        m.setSlaResolutionCompliancePct(sla != null ? ((Number) sla).doubleValue() : null);
        return m;
    };

    private final RowMapper<CategoryAnomalyRadar> radarRowMapper = (rs, rowNum) -> {
        CategoryAnomalyRadar r = new CategoryAnomalyRadar();
        r.setCategoryId(rs.getInt("category_id"));
        r.setCategoryCode(rs.getString("category_code"));
        r.setDisplayName(rs.getString("display_name"));
        r.setMonthlyThresholdLimit(rs.getInt("monthly_threshold_limit"));
        r.setTotalStudentReports(rs.getLong("total_student_reports"));
        r.setGroupedMasterIncidents(rs.getLong("grouped_master_incidents"));
        r.setIndependentIncidents(rs.getLong("independent_incidents"));
        r.setTotalUnderlyingIncidents(rs.getLong("total_underlying_incidents"));
        r.setClosedReports(rs.getLong("closed_reports"));
        r.setEscalatedReports(rs.getLong("escalated_reports"));
        Object avg = rs.getObject("avg_resolution_minutes");
        r.setAvgResolutionMinutes(avg != null ? ((Number) avg).doubleValue() : null);
        r.setThresholdBreached(rs.getInt("is_threshold_breached") == 1);
        return r;
    };

    private final RowMapper<DepartmentSlaPerformance> slaPerformanceRowMapper = (rs, rowNum) -> {
        DepartmentSlaPerformance p = new DepartmentSlaPerformance();
        p.setDeptId(rs.getInt("dept_id"));
        p.setDeptCode(rs.getString("dept_code"));
        p.setDeptName(rs.getString("dept_name"));
        p.setTotalAssignedTickets(rs.getLong("total_assigned_tickets"));
        p.setTotalClosedTickets(rs.getLong("total_closed_tickets"));
        p.setTotalReopenedTickets(rs.getLong("total_reopened_tickets"));
        p.setAckSlaMetCount(rs.getLong("ack_sla_met_count"));
        p.setAckSlaBreachedCount(rs.getLong("ack_sla_breached_count"));
        p.setResolutionSlaMetCount(rs.getLong("resolution_sla_met_count"));
        p.setResolutionSlaBreachedCount(rs.getLong("resolution_sla_breached_count"));
        p.setCurrentlyOverdueCount(rs.getLong("currently_overdue_count"));
        p.setEscalatedTicketCount(rs.getLong("escalated_ticket_count"));
        Object avg = rs.getObject("avg_closure_minutes");
        p.setAvgClosureMinutes(avg != null ? ((Number) avg).doubleValue() : null);
        return p;
    };

    private final RowMapper<LocationCategoryHeatmap> heatmapRowMapper = (rs, rowNum) -> {
        LocationCategoryHeatmap h = new LocationCategoryHeatmap();
        h.setBuildingBlock(rs.getString("building_block"));
        h.setLocationScope(LocationScope.valueOf(rs.getString("location_scope")));
        h.setCategoryId(rs.getInt("category_id"));
        h.setCategoryCode(rs.getString("category_code"));
        h.setCategoryName(rs.getString("category_name"));
        h.setTicketCount(rs.getLong("ticket_count"));
        h.setOpenTicketCount(rs.getLong("open_ticket_count"));
        h.setEscalatedCount(rs.getLong("escalated_count"));
        h.setUniqueAffectedStudents(rs.getLong("unique_affected_students"));
        return h;
    };

    public List<CategoryAnomalyRadar> getMonthlyAnomalyRadar() {
        String sql = "SELECT * FROM v_category_monthly_anomaly_radar ORDER BY total_student_reports DESC, category_id ASC";
        return jdbcTemplate.query(sql, radarRowMapper);
    }

    public List<DepartmentSlaPerformance> getDepartmentSlaPerformance() {
        String sql = "SELECT * FROM v_department_sla_performance ORDER BY dept_id ASC";
        return jdbcTemplate.query(sql, slaPerformanceRowMapper);
    }

    public List<LocationCategoryHeatmap> getLocationCategoryHeatmap() {
        String sql = "SELECT * FROM v_location_category_heatmap ORDER BY ticket_count DESC, building_block ASC";
        return jdbcTemplate.query(sql, heatmapRowMapper);
    }

    public List<DepartmentSatisfactionMetric> getDepartmentSatisfaction() {
        String sql = "SELECT * FROM v_department_satisfaction ORDER BY avg_student_rating DESC, total_closed DESC";
        return jdbcTemplate.query(sql, satisfactionRowMapper);
    }
}

