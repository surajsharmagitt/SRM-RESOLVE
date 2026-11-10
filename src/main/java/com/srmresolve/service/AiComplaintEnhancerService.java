package com.srmresolve.service;

import com.srmresolve.dto.AiEnhanceRequest;
import com.srmresolve.dto.AiEnhanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * AI Complaint Enhancer Service.
 * Follows strict anti-hallucination constraint (Section 4.D):
 * Never invents equipment parts, root causes, room numbers, or urgency not stated by the student.
 * Formats facts clearly with verified location context.
 */
@Service
public class AiComplaintEnhancerService {

    private static final Logger log = LoggerFactory.getLogger(AiComplaintEnhancerService.class);

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    public AiEnhanceResponse enhanceComplaint(AiEnhanceRequest request) {
        String raw = request.getRawStudentText() != null ? request.getRawStudentText().trim() : "";
        if (raw.isBlank()) {
            return new AiEnhanceResponse("No issue description provided.", true);
        }

        // Clean up text and capitalize sentences
        String cleanNotes = cleanText(raw);

        // Structured, non-hallucinating formal statement
        StringBuilder sb = new StringBuilder();
        sb.append("Complaint Category: ").append(request.getCategoryDisplayName()).append("\n");
        sb.append("Location: ").append(request.getBuildingBlock()).append(", Room/Zone: ").append(request.getRoomOrLocation()).append("\n\n");
        sb.append("Student Statement:\n");
        sb.append(cleanNotes).append("\n\n");
        sb.append("Requested Action:\n");
        sb.append("Please inspect and resolve the reported issue at the specified location.");

        return new AiEnhanceResponse(sb.toString(), true);
    }

    private String cleanText(String input) {
        // Normalize spaces and basic grammar capitalization
        String normalized = input.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) return normalized;

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : normalized.toCharArray()) {
            if (capitalizeNext && Character.isLetter(c)) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
            if (c == '.' || c == '!' || c == '?') {
                capitalizeNext = true;
            }
        }
        if (!normalized.endsWith(".")) {
            result.append(".");
        }
        return result.toString();
    }
}

