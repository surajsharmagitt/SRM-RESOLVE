package com.srmresolve.service;

import com.srmresolve.dao.MasterIncidentDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Deterministic Java Incident Grouping & Jaccard Negation Tests")
class IncidentGroupingEngineTest {

    private IncidentGroupingEngine engine;
    private MasterIncidentDao masterIncidentDao;

    @BeforeEach
    void setUp() {
        masterIncidentDao = Mockito.mock(MasterIncidentDao.class);
        engine = new IncidentGroupingEngine(masterIncidentDao);
    }

    @Test
    @DisplayName("1. Negation Safety: Must preserve negation tokens ('not', 'no', 'never', 'cannot', etc.)")
    void testPreserveNegationTokens() {
        Set<String> tokens1 = engine.normalizeAndTokenize("Water is not coming in the room");
        assertTrue(tokens1.contains("not"), "Negation token 'not' must be preserved");
        assertTrue(tokens1.contains("water"));
        assertTrue(tokens1.contains("coming"));

        Set<String> tokens2 = engine.normalizeAndTokenize("There is no electricity at all");
        assertTrue(tokens2.contains("no"), "Negation token 'no' must be preserved");

        Set<String> tokens3 = engine.normalizeAndTokenize("AC cannot cool the room without fan");
        assertTrue(tokens3.contains("cannot"), "Negation token 'cannot' must be preserved");
        assertTrue(tokens3.contains("without"), "Negation token 'without' must be preserved");
    }

    @Test
    @DisplayName("2. Contradictory Meanings: Should differentiate opposite-meaning complaints")
    void testContradictoryComplaintsDoNotHaveIdenticalTokens() {
        // "water is coming" vs "water is not coming"
        Set<String> positive = engine.normalizeAndTokenize("water is coming properly");
        Set<String> negative = engine.normalizeAndTokenize("water is not coming properly");

        assertFalse(positive.contains("not"));
        assertTrue(negative.contains("not"));

        double similarity = engine.computeJaccardSimilarity(positive, negative);
        // Union = {water, coming, properly, not} (size 4), Intersection = {water, coming, properly} (size 3)
        // J = 3/4 = 0.75 != 1.0 (they are explicitly differentiated by the negation token)
        assertTrue(similarity < 1.0, "Complaints with negation must not have 1.0 similarity");
    }

    @Test
    @DisplayName("3. Empty Token Sets: Edge case should return 0.0 without division by zero")
    void testEmptyTokenSetsReturnZero() {
        assertEquals(0.0, engine.computeJaccardSimilarity(Set.of(), Set.of()));
        assertEquals(0.0, engine.computeJaccardSimilarity(null, null));
        assertEquals(0.0, engine.computeJaccardSimilarity(Set.of("water"), Set.of()));
    }

    @Test
    @DisplayName("4. Formula Verification: Standard Jaccard formula computation")
    void testJaccardCalculation() {
        // A = {water, supply, cut}
        // B = {water, supply, outage}
        // Intersection = {water, supply} (2)
        // Union = {water, supply, cut, outage} (4)
        // J = 2/4 = 0.50
        Set<String> a = Set.of("water", "supply", "cut");
        Set<String> b = Set.of("water", "supply", "outage");

        double jaccard = engine.computeJaccardSimilarity(a, b);
        assertEquals(0.50, jaccard, 0.001);
    }

    @Test
    @DisplayName("5. Stop Words Removal: Non-discriminative terms removed while keeping core nouns/verbs")
    void testStopWordsRemoval() {
        Set<String> tokens = engine.normalizeAndTokenize("Sir please problem in Ganga tower room 412 water supply cut");
        assertFalse(tokens.contains("sir"));
        assertFalse(tokens.contains("please"));
        assertFalse(tokens.contains("problem"));
        assertFalse(tokens.contains("room"));
        assertFalse(tokens.contains("tower"));

        assertTrue(tokens.contains("water"));
        assertTrue(tokens.contains("supply"));
        assertTrue(tokens.contains("cut"));
    }
}

