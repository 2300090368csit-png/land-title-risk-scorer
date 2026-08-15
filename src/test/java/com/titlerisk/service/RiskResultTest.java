package com.titlerisk.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the banding thresholds, including the boundary case that used to
 * be inconsistent: banding read the raw double while the UI displayed the
 * rounded figure, so 69.6 showed as "70" but was classified medium.
 */
class RiskResultTest {

    private static RiskResult of(double score) {
        return new RiskResult(score, List.of());
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "100.0, low",
            "70.6,  low",     // rounds to 71
            "70.5,  low",     // rounds to 71
            "70.4,  medium",  // rounds to 70, and 70 is not "above 70"
            "70.0,  medium",
            "40.0,  medium",
            "39.6,  medium",  // rounds to 40, so it must band medium, not high
            "39.4,  high",    // rounds to 39
            "0.0,   high"
    })
    @DisplayName("bands off the number actually shown to the user")
    void bandsOffRoundedScore(double score, String expectedBand) {
        assertEquals(expectedBand, of(score).getRiskBand());
    }

    @Test
    @DisplayName("the displayed number and its band never contradict each other")
    void displayedScoreAndBandAgree() {
        // Regression guard for the old bug: walk the whole range in 0.1 steps and
        // assert the band always matches what the rounded, displayed number implies.
        for (int tenths = 0; tenths <= 1000; tenths++) {
            double raw = tenths / 10.0;
            RiskResult result = of(raw);
            long shown = result.getRoundedScore();

            String expected = shown > 70 ? "low" : shown >= 40 ? "medium" : "high";
            assertEquals(expected, result.getRiskBand(),
                    "raw " + raw + " displays as " + shown + " but banded wrongly");
        }
    }

    @Test
    @DisplayName("an uncapped result reports its own score as the uncapped figure")
    void uncappedByDefault() {
        RiskResult result = of(63.2);

        assertEquals(63.2, result.getUncappedScore(), 0.0001);
        assertEquals(63, result.getRoundedScore());
    }
}
