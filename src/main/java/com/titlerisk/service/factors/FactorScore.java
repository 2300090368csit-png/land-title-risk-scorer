package com.titlerisk.service.factors;

/**
 * Immutable result of evaluating a single {@link RiskFactor} against a parcel.
 *
 * <p>Bundles together everything the UI needs to explain one line item of the
 * overall risk score: which factor this is, how it scored on its own 0-100
 * scale, how much that factor counts towards the total (its weight), and a
 * plain-English legal explanation of why it scored that way.</p>
 */
public final class FactorScore {

    private final String factorName;
    private final double rawScore;
    private final double weight;
    private final String explanation;

    public FactorScore(String factorName, double rawScore, double weight, String explanation) {
        this.factorName = factorName;
        this.rawScore = rawScore;
        this.weight = weight;
        this.explanation = explanation;
    }

    public String getFactorName() {
        return factorName;
    }

    public double getRawScore() {
        return rawScore;
    }

    public double getWeight() {
        return weight;
    }

    public String getExplanation() {
        return explanation;
    }

    /** This factor's contribution to the final weighted total (rawScore * weight). */
    public double getWeightedContribution() {
        return rawScore * weight;
    }

    /** Weight expressed as a whole-number percentage, convenient for display (e.g. "30%"). */
    public int getWeightPercent() {
        return (int) Math.round(weight * 100);
    }
}
