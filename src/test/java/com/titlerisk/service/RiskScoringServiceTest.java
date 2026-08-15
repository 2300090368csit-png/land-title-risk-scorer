package com.titlerisk.service;

import com.titlerisk.model.Parcel;
import com.titlerisk.service.factors.FactorScore;
import com.titlerisk.service.factors.RiskFactor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the scoring engine.
 *
 * <p>Note there is no {@code @SpringBootTest} here and no application context:
 * {@link RiskScoringService} takes its {@code List<RiskFactor>} through the
 * constructor, so a test can hand it whatever fakes it likes. That is the
 * practical payoff of constructor injection over field injection — the class
 * is testable in isolation, in milliseconds.</p>
 */
class RiskScoringServiceTest {

    /** A stand-in factor with a fixed weight and score, so the arithmetic is predictable. */
    private static RiskFactor fake(String name, double rawScore, double weight) {
        return new RiskFactor() {
            @Override
            public double getWeight() {
                return weight;
            }

            @Override
            public FactorScore evaluate(Parcel parcel) {
                return new FactorScore(name, rawScore, weight, "test explanation");
            }
        };
    }

    /** A stand-in factor that also declares the total may not exceed {@code ceiling}. */
    private static RiskFactor capping(String name, double rawScore, double weight, double ceiling) {
        return new RiskFactor() {
            @Override
            public double getWeight() {
                return weight;
            }

            @Override
            public FactorScore evaluate(Parcel parcel) {
                return new FactorScore(name, rawScore, weight, "test explanation")
                        .withCeiling(ceiling, "capped for test");
            }
        };
    }

    @Test
    @DisplayName("combines factors by weight")
    void combinesByWeight() {
        var service = new RiskScoringService(List.of(
                fake("A", 100, 0.5),
                fake("B", 0, 0.5)
        ));

        assertEquals(50.0, service.score(new Parcel()).getTotalScore(), 0.0001);
    }

    @Test
    @DisplayName("uses every injected factor exactly once")
    void evaluatesEachFactorOnce() {
        List<String> calls = new ArrayList<>();
        RiskFactor recording = new RiskFactor() {
            @Override
            public double getWeight() {
                return 1.0;
            }

            @Override
            public FactorScore evaluate(Parcel parcel) {
                calls.add("called");
                return new FactorScore("R", 80, 1.0, "e");
            }
        };

        new RiskScoringService(List.of(recording)).score(new Parcel());

        assertEquals(1, calls.size());
    }

    @Test
    @DisplayName("returns zero rather than throwing when no factors are registered")
    void emptyFactorListScoresZero() {
        RiskResult result = new RiskScoringService(List.of()).score(new Parcel());

        assertEquals(0.0, result.getTotalScore(), 0.0001);
        assertTrue(result.getFactorScores().isEmpty());
    }

    @Test
    @DisplayName("orders the breakdown heaviest factor first, regardless of injection order")
    void sortsBreakdownByWeightDescending() {
        var service = new RiskScoringService(List.of(
                fake("light", 100, 0.10),
                fake("heavy", 100, 0.60),
                fake("mid", 100, 0.30)
        ));

        List<FactorScore> ordered = service.score(new Parcel()).getFactorScores();

        assertEquals("heavy", ordered.get(0).getFactorName());
        assertEquals("mid", ordered.get(1).getFactorName());
        assertEquals("light", ordered.get(2).getFactorName());
    }

    @Test
    @DisplayName("a disqualifying factor caps the total below the weighted sum")
    void ceilingCapsTheTotal() {
        // Weighted sum would be 90; the capping factor says it may not exceed 45.
        var service = new RiskScoringService(List.of(
                fake("clean", 100, 0.8),
                capping("disqualifying", 50, 0.2, 45)
        ));

        RiskResult result = service.score(new Parcel());

        assertEquals(45.0, result.getTotalScore(), 0.0001);
        assertEquals(90.0, result.getUncappedScore(), 0.0001);
        assertTrue(result.isCapped());
    }

    @Test
    @DisplayName("a ceiling above the weighted sum leaves the score alone")
    void ceilingAboveTotalDoesNothing() {
        // Weighted sum is 20; a ceiling of 45 should not raise it.
        var service = new RiskScoringService(List.of(
                capping("disqualifying", 20, 1.0, 45)
        ));

        RiskResult result = service.score(new Parcel());

        assertEquals(20.0, result.getTotalScore(), 0.0001);
        assertFalse(result.isCapped());
        assertNull(result.getCeilingReason());
    }

    @Test
    @DisplayName("when several factors cap, the strictest ceiling wins")
    void strictestCeilingWins() {
        var service = new RiskScoringService(List.of(
                fake("clean", 100, 0.5),
                capping("lenient", 100, 0.25, 60),
                capping("strict", 100, 0.25, 30)
        ));

        assertEquals(30.0, service.score(new Parcel()).getTotalScore(), 0.0001);
    }
}
