package com.srmresolve.validator;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Strict validator for SRM University-AP Student Registration Numbers.
 * Follows the pattern: ^AP\d{11}$ (e.g., AP25110070893)
 * Extracts the 2-digit cohort admission year ('25' -> 2025).
 * Does NOT infer department, programme, or section from serial blocks.
 * NEVER scrapes external ERP portals.
 */
@Component
public class SrmRegistrationValidator {

    private static final Pattern SRM_REG_PATTERN = Pattern.compile("^AP(\\d{2})\\d{9}$");

    /**
     * Validates whether a registration number strictly matches SRM-AP format (^AP\d{11}$).
     */
    public boolean isValid(String regNo) {
        if (regNo == null) {
            return false;
        }
        return SRM_REG_PATTERN.matcher(regNo.trim().toUpperCase()).matches();
    }

    /**
     * Validates general SRM campus registration IDs (SRM-AP, KTR RA codes, or institutional IDs)
     * for flexible passwordless onboarding without blocking valid student test inputs.
     */
    public boolean isValidFlexibleCampusId(String regNo) {
        if (regNo == null || regNo.isBlank()) {
            return false;
        }
        return regNo.trim().toUpperCase().matches("^[A-Z0-9_\\-]{4,25}$");
    }

    /**
     * Extracts admission cohort year (e.g., "AP25110070893" -> 2025).
     * Strictly throws IllegalArgumentException if non-standard format.
     */
    public int extractAdmissionYear(String regNo) {
        if (regNo == null || !isValid(regNo)) {
            throw new IllegalArgumentException("Invalid SRM-AP registration number: " + regNo);
        }
        Matcher matcher = SRM_REG_PATTERN.matcher(regNo.trim().toUpperCase());
        if (matcher.matches()) {
            int twoDigitYear = Integer.parseInt(matcher.group(1));
            return 2000 + twoDigitYear;
        }
        throw new IllegalArgumentException("Invalid SRM-AP registration number: " + regNo);
    }

    /**
     * Normalizes registration number to uppercase trimmed string.
     */
    public String normalize(String regNo) {
        if (regNo == null) {
            return "";
        }
        return regNo.trim().toUpperCase();
    }
}

