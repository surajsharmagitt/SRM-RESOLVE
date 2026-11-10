package com.srmresolve.service;

import com.srmresolve.dao.ExecutiveAnalyticsDao;
import com.srmresolve.model.CategoryAnomalyRadar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Viva Simulation Service & FK-Safe Reset Integration Test")
class VivaSimulationIntegrationTest {

    @Autowired
    private VivaSimulationService simulationService;

    @Autowired
    private ExecutiveAnalyticsDao executiveAnalyticsDao;

    @Test
    @DisplayName("Simulate 100-ticket spike and execute FK-safe reset cleanly")
    void testSpikeSimulationAndReset() {
        // 1. Simulate 100 spike
        simulationService.simulate100ComplaintSpike(1L);

        // 2. Query Anomaly Radar view to verify counts
        List<CategoryAnomalyRadar> radar = executiveAnalyticsDao.getMonthlyAnomalyRadar();
        assertFalse(radar.isEmpty());

        long totalReports = radar.stream().mapToLong(CategoryAnomalyRadar::getTotalStudentReports).sum();
        assertTrue(totalReports >= 100, "Total reports across categories must reflect the 100-complaint spike");

        // 3. Reset demo data in strict child-first order (zero FK crashes!)
        assertDoesNotThrow(() -> simulationService.resetDemoData(1L));

        // 4. Verify post-reset view returns to baseline seed data
        List<CategoryAnomalyRadar> postResetRadar = executiveAnalyticsDao.getMonthlyAnomalyRadar();
        long postResetReports = postResetRadar.stream().mapToLong(CategoryAnomalyRadar::getTotalStudentReports).sum();
        assertEquals(5, postResetReports, "Reports must return to original 5 seeded tickets after reset");
    }
}

