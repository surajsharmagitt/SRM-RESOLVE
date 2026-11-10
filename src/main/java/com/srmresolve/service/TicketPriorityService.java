package com.srmresolve.service;

import com.srmresolve.dao.CategoryRoutingSlaDao;
import com.srmresolve.dao.GrievanceTicketDao;
import com.srmresolve.dto.TicketSearchCriteria;
import com.srmresolve.model.GrievanceCategory;
import com.srmresolve.model.GrievanceTicket;
import com.srmresolve.model.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Intelligent Priority Auto-Adjustment Engine.
 * Triage algorithm based on category defaults, utility impact clustering,
 * and reopening frequency.
 */
@Service
public class TicketPriorityService {

    private static final Logger log = LoggerFactory.getLogger(TicketPriorityService.class);

    private final CategoryRoutingSlaDao categoryDao;
    private final GrievanceTicketDao ticketDao;
    private final JdbcTemplate jdbcTemplate;

    public TicketPriorityService(CategoryRoutingSlaDao categoryDao, GrievanceTicketDao ticketDao, JdbcTemplate jdbcTemplate) {
        this.categoryDao = categoryDao;
        this.ticketDao = ticketDao;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Determines optimal initial ticket priority based on category rules and utility cluster load.
     */
    public Priority determineInitialPriority(int categoryId, String buildingBlock, Priority requestedPriority) {
        Optional<GrievanceCategory> categoryOpt = categoryDao.findCategoryById(categoryId);
        Priority basePriority = categoryOpt.map(GrievanceCategory::getDefaultPriority).orElse(Priority.MEDIUM);

        // If user requested higher priority (e.g. HIGH/CRITICAL) than default, start from requested
        if (requestedPriority != null && requestedPriority.ordinal() > basePriority.ordinal()) {
            basePriority = requestedPriority;
        }

        // Check if shared utility clustering warrants priority escalation
        boolean isShared = categoryOpt.map(GrievanceCategory::isSharedUtility).orElse(false);
        if (isShared && buildingBlock != null && !buildingBlock.isBlank()) {
            String countSql = "SELECT COUNT(*) FROM grievance_tickets " +
                    "WHERE category_id = ? AND building_block = ? " +
                    "AND status NOT IN ('CLOSED') " +
                    "AND created_at >= DATE_SUB(NOW(), INTERVAL 12 HOUR)";
            Long recentCount = jdbcTemplate.queryForObject(countSql, Long.class, categoryId, buildingBlock.trim());
            long count = recentCount != null ? recentCount : 0L;

            if (count >= 8) {
                log.info("Shared utility cluster in {} has {} open reports; auto-elevating priority to CRITICAL", buildingBlock, count);
                return Priority.CRITICAL;
            } else if (count >= 4 && basePriority.ordinal() < Priority.HIGH.ordinal()) {
                log.info("Shared utility cluster in {} has {} open reports; auto-elevating priority to HIGH", buildingBlock, count);
                return Priority.HIGH;
            }
        }

        return basePriority;
    }

    /**
     * Re-evaluates priority upon student rejection and reopening.
     */
    public Priority calculateReopenedPriority(GrievanceTicket ticket) {
        int newReopenCount = ticket.getReopenCount() + 1;
        if (newReopenCount >= 2) {
            log.warn("Ticket {} reopened {} times; elevating priority to CRITICAL", ticket.getTicketReference(), newReopenCount);
            return Priority.CRITICAL;
        } else {
            // First reopen: ensure at least HIGH
            if (ticket.getPriority().ordinal() < Priority.HIGH.ordinal()) {
                log.info("Ticket {} reopened for the first time; elevating priority to HIGH", ticket.getTicketReference());
                return Priority.HIGH;
            }
            return ticket.getPriority();
        }
    }
}
