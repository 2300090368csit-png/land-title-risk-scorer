package com.titlerisk.service;

import com.titlerisk.service.factors.FactorScore;

import java.util.List;

/**
 * The outcome of scoring one parcel: a total weighted score plus the
 * per-factor breakdown that produced it. Simple data holder returned by
 * {@link RiskScoringService#score}.
 */
public final class RiskResult {

    private final double totalScore;
    private final List<FactorScore> factorScores;

    public RiskResult(double totalScore, List<FactorScore> factorScores) {
        this.totalScore = totalScore;
        this.factorScores = factorScores;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public List<FactorScore> getFactorScores() {
        return factorScores;
    }

    /** Rounded to a whole number, since fractional risk scores aren't meaningful to a reader. */
    public long getRoundedScore() {
        return Math.round(totalScore);
    }

    /** Bucket label used to color-code the score in the UI. */
    public String getRiskBand() {
        if (totalScore > 70) {
            return "low";
        } else if (totalScore >= 40) {
            return "medium";
        }
        return "high";
    }
}
