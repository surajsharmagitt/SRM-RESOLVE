package com.srmresolve.dao;

import com.srmresolve.model.GrievanceCategory;
import com.srmresolve.model.LocationScope;
import com.srmresolve.model.Priority;
import com.srmresolve.model.RoutingRule;
import com.srmresolve.model.SlaPolicy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRoutingSlaDao {

    private final JdbcTemplate jdbcTemplate;

    public CategoryRoutingSlaDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<GrievanceCategory> categoryRowMapper = (rs, rowNum) -> {
        GrievanceCategory c = new GrievanceCategory();
        c.setCategoryId(rs.getInt("category_id"));
        c.setCategoryCode(rs.getString("category_code"));
        c.setDisplayName(rs.getString("display_name"));
        c.setIconClass(rs.getString("icon_class"));
        c.setSharedUtility(rs.getBoolean("is_shared_utility"));
        c.setDefaultPriority(Priority.valueOf(rs.getString("default_priority")));
        c.setMonthlyThresholdLimit(rs.getInt("monthly_threshold_limit"));
        return c;
    };

    private final RowMapper<RoutingRule> routingRowMapper = (rs, rowNum) -> {
        RoutingRule r = new RoutingRule();
        r.setRuleId(rs.getInt("rule_id"));
        r.setCategoryId(rs.getInt("category_id"));
        r.setLocationScope(LocationScope.valueOf(rs.getString("location_scope")));
        r.setTargetDeptId(rs.getInt("target_dept_id"));
        return r;
    };

    private final RowMapper<SlaPolicy> slaRowMapper = (rs, rowNum) -> {
        SlaPolicy s = new SlaPolicy();
        s.setSlaId(rs.getInt("sla_id"));
        s.setCategoryId(rs.getInt("category_id"));
        s.setPriority(Priority.valueOf(rs.getString("priority")));
        s.setAckDeadlineMinutes(rs.getInt("ack_deadline_minutes"));
        s.setResolutionDeadlineMinutes(rs.getInt("resolution_deadline_minutes"));
        s.setLevel1EscalateToRole(rs.getString("level1_escalate_to_role"));
        s.setLevel2EscalateToRole(rs.getString("level2_escalate_to_role"));
        return s;
    };

    public List<GrievanceCategory> findAllCategories() {
        String sql = "SELECT * FROM grievance_categories ORDER BY category_id";
        return jdbcTemplate.query(sql, categoryRowMapper);
    }

    public Optional<GrievanceCategory> findCategoryById(Integer categoryId) {
        String sql = "SELECT * FROM grievance_categories WHERE category_id = ?";
        List<GrievanceCategory> list = jdbcTemplate.query(sql, categoryRowMapper, categoryId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<GrievanceCategory> findCategoryByCode(String categoryCode) {
        String sql = "SELECT * FROM grievance_categories WHERE category_code = ?";
        List<GrievanceCategory> list = jdbcTemplate.query(sql, categoryRowMapper, categoryCode);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Integer> resolveTargetDepartment(Integer categoryId, LocationScope locationScope) {
        // Try exact match first, then fallback to 'ANY'
        String sql = "SELECT target_dept_id FROM routing_rules WHERE category_id = ? AND location_scope = ?";
        List<Integer> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("target_dept_id"), categoryId, locationScope.name());
        if (!list.isEmpty()) {
            return Optional.of(list.get(0));
        }

        String fallbackSql = "SELECT target_dept_id FROM routing_rules WHERE category_id = ? AND location_scope = 'ANY'";
        List<Integer> fallbackList = jdbcTemplate.query(fallbackSql, (rs, rowNum) -> rs.getInt("target_dept_id"), categoryId);
        return fallbackList.isEmpty() ? Optional.empty() : Optional.of(fallbackList.get(0));
    }

    public Optional<SlaPolicy> findSlaPolicy(Integer categoryId, Priority priority) {
        String sql = "SELECT * FROM sla_policies WHERE category_id = ? AND priority = ?";
        List<SlaPolicy> list = jdbcTemplate.query(sql, slaRowMapper, categoryId, priority.name());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<SlaPolicy> findAllSlaPolicies() {
        String sql = "SELECT * FROM sla_policies ORDER BY category_id, priority";
        return jdbcTemplate.query(sql, slaRowMapper);
    }
}

