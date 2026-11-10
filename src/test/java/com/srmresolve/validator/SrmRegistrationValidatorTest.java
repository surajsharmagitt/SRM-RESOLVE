package com.srmresolve.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SRM Registration Number Validator Tests")
class SrmRegistrationValidatorTest {

    private SrmRegistrationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SrmRegistrationValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "AP25110070893",
            "ap25110070893",
            "AP24110060050",
            "AP23110050200",
            "AP21110010001",
            "AP26110099999"
    })
    @DisplayName("Should accept valid SRM registration numbers matching ^AP\\d{11}$")
    void testValidRegistrationNumbers(String regNo) {
        assertTrue(validator.isValid(regNo), "Expected valid: " + regNo);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "AP2511007089",      // 10 digits instead of 11
            "AP251100708931",    // 12 digits
            "RA25110070893",     // Non-AP campus code
            "25110070893",       // Missing AP prefix
            "APABC10070893",     // Letters in serial block
            "INVALID"
    })
    @DisplayName("Should reject invalid registration number formats")
    void testInvalidRegistrationNumbers(String regNo) {
        assertFalse(validator.isValid(regNo), "Expected invalid: " + regNo);
    }

    @Test
    @DisplayName("Should return false for null input")
    void testNullRegistrationNumber() {
        assertFalse(validator.isValid(null));
    }

    @Test
    @DisplayName("Should accurately extract 2-digit cohort admission year")
    void testExtractAdmissionYear() {
        assertEquals(2025, validator.extractAdmissionYear("AP25110070893"));
        assertEquals(2024, validator.extractAdmissionYear("AP24110060050"));
        assertEquals(2023, validator.extractAdmissionYear("AP23110050200"));
        assertEquals(2021, validator.extractAdmissionYear("AP21110010001"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when extracting year from invalid format")
    void testExtractAdmissionYearInvalid() {
        assertThrows(IllegalArgumentException.class, () -> validator.extractAdmissionYear("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> validator.extractAdmissionYear(null));
    }
}

