package com.srmresolve.service;

import com.srmresolve.dao.MasterIncidentDao;
import com.srmresolve.model.GrievanceCategory;
import com.srmresolve.model.GrievanceTicket;
import com.srmresolve.model.MasterIncident;
import com.srmresolve.model.MasterIncidentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Deterministic Java Incident Grouping Engine.
 * Implements Jaccard token similarity with strict negation safety (Section 6.1).
 * Formula: J(A, B) = |A ∩ B| / |A ∪ B|
 */
@Service
public class IncidentGroupingEngine {

    private static final Logger log = LoggerFactory.getLogger(IncidentGroupingEngine.class);

    private final MasterIncidentDao masterIncidentDao;

    @Value("${app.grouping.window-hours:6}")
    private int windowHours;

    @Value("${app.grouping.jaccard-threshold:0.35}")
    private double jaccardThreshold;

    // Stop words that carry no discriminative utility
    // IMPORTANT (Section 6.1): NEVER include negation tokens (not, no, never, without, cannot, etc.)
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "is", "are", "in", "on", "at", "from", "since",
            "working", "problem", "issue", "sir", "please", "room",
            "floor", "block", "tower", "and", "a", "an", "to", "for", "my", "our"
    );

    public IncidentGroupingEngine(MasterIncidentDao masterIncidentDao) {
        this.masterIncidentDao = masterIncidentDao;
    }

    /**
     * Resolves master incident grouping for a newly filed ticket.
     * Returns the masterIncidentId to link to, or null if ticket is independent.
     */
    public Long resolveGrouping(GrievanceTicket ticket, GrievanceCategory category) {
        // Case 1: Explicit "I'm Affected Too" banner click
        if (ticket.getMasterIncidentId() != null) {
            Optional<MasterIncident> existing = masterIncidentDao.findById(ticket.getMasterIncidentId());
            if (existing.isPresent() &&
                existing.get().getCategoryId().equals(category.getCategoryId()) &&
                existing.get().getBuildingBlock().equalsIgnoreCase(ticket.getBuildingBlock())) {
                log.info("Ticket explicitly linked to active master incident #{}", ticket.getMasterIncidentId());
                return ticket.getMasterIncidentId();
            }
        }

        // Case 2: Private room issue (e.g. room AC, personal room cleaning) -> stays independent
        if (!category.isSharedUtility()) {
            log.info("Category '{}' is private/non-shared; ticket stays independent.", category.getDisplayName());
            return null;
        }

        // Case 3: Shared-utility category -> evaluate deterministic Jaccard grouping
        Set<String> incomingTokens = normalizeAndTokenize(ticket.getRawDescription());
        String incomingTokensStr = String.join(" ", incomingTokens);

        List<MasterIncident> candidates = masterIncidentDao.findActiveCandidates(
                category.getCategoryId(), ticket.getBuildingBlock(), windowHours);

        MasterIncident bestMatch = null;
        double highestSimilarity = 0.0;

        for (MasterIncident candidate : candidates) {
            Set<String> candidateTokens = normalizeAndTokenize(candidate.getNormalizedTokens() + " " + candidate.getIncidentTitle());
            double similarity = computeJaccardSimilarity(incomingTokens, candidateTokens);
            log.debug("Comparing ticket with Master Incident #{}: Jaccard = {}", candidate.getMasterIncidentId(), similarity);

            if (similarity >= jaccardThreshold && similarity > highestSimilarity) {
                highestSimilarity = similarity;
                bestMatch = candidate;
            }
        }

        if (bestMatch != null) {
            log.info("Matching master incident #{} found for ticket with Jaccard = {}",
                    bestMatch.getMasterIncidentId(), highestSimilarity);
            return bestMatch.getMasterIncidentId();
        }

        // If no matching active incident, create a new master incident for this shared utility
        MasterIncident newIncident = new MasterIncident();
        newIncident.setCategoryId(category.getCategoryId());
        newIncident.setAssignedDeptId(ticket.getAssignedDeptId());
        newIncident.setBuildingBlock(ticket.getBuildingBlock());
        newIncident.setIncidentTitle(category.getDisplayName() + " in " + ticket.getBuildingBlock());
        newIncident.setNormalizedTokens(incomingTokensStr.length() > 490 ? incomingTokensStr.substring(0, 490) : incomingTokensStr);
        newIncident.setIncidentStatus(MasterIncidentStatus.OPEN);
        newIncident.setAffectedStudentCount(1);
        newIncident.setSyntheticDemo(false);

        Long newId = masterIncidentDao.insert(newIncident);
        log.info("Created new master incident #{} for category '{}' in {}",
                newId, category.getDisplayName(), ticket.getBuildingBlock());
        return newId;
    }

    /**
     * Normalizes text and tokenizes into a Set<String>.
     * Preserves all negation tokens per Section 6.1.
     */
    public Set<String> normalizeAndTokenize(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptySet();
        }

        String cleaned = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isEmpty()) {
            return Collections.emptySet();
        }

        return Arrays.stream(cleaned.split(" "))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toSet());
    }

    /**
     * Computes Jaccard Similarity: J(A, B) = |A ∩ B| / |A ∪ B|.
     * Edge case: If both token sets are empty, returns 0.0 (Section 6.1).
     */
    public double computeJaccardSimilarity(Set<String> setA, Set<String> setB) {
        if (setA == null || setB == null || (setA.isEmpty() && setB.isEmpty())) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / (double) union.size();
    }
}

