package com.titlerisk.service;

import com.titlerisk.service.factors.FactorScore;

import java.util.List;

/**
 * The outcome of scoring one parcel: a total weighted score plus the
 * per-factor breakdown that produced it. Simple data holder returned by
 * {@link RiskScoringService#score}.
 *
 * <p>When a factor reports a disqualifying condition, {@link #getTotalScore()}
 * is the capped figure while {@link #getUncappedScore()} keeps the raw
 * weighted sum, so the UI can show both ("would have scored 76; capped to
 * 45") rather than presenting a number the arithmetic doesn't explain.</p>
 */
public final class RiskResult {

    private final double totalScore;
    private final List<FactorScore> factorScores;
    private final double uncappedScore;
    private final String ceilingReason;

    public RiskResult(double totalScore, List<FactorScore> factorScores) {
        this(totalScore, factorScores, totalScore, null);
    }

    public RiskResult(double totalScore, List<FactorScore> factorScores,
                      double uncappedScore, String ceilingReason) {
        this.totalScore = totalScore;
        this.factorScores = factorScores;
        this.uncappedScore = uncappedScore;
        this.ceilingReason = ceilingReason;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public List<FactorScore> getFactorScores() {
        return factorScores;
    }

    /** The weighted sum before any ceiling was applied. Equals the total when nothing capped. */
    public double getUncappedScore() {
        return uncappedScore;
    }

    /** Plain-English reason the score was capped, or null when it wasn't. */
    public String getCeilingReason() {
        return ceilingReason;
    }

    public boolean isCapped() {
        return ceilingReason != null;
    }

    /** Rounded to a whole number, since fractional risk scores aren't meaningful to a reader. */
    public long getRoundedScore() {
        return Math.round(totalScore);
    }

    /**
     * Bucket label used to colour-code the score in the UI.
     *
     * <p>Deliberately banded off {@link #getRoundedScore()} rather than the raw
     * double: the UI only ever displays the rounded figure, so banding the raw
     * value meant a parcel scoring 69.6 rendered as "70" while being classified
     * medium — a number and a label that contradicted each other on screen.</p>
     */
    public String getRiskBand() {
        long shown = getRoundedScore();
        if (shown > 70) {
            return "low";
        } else if (shown >= 40) {
            return "medium";
        }
        return "high";
    }
}
