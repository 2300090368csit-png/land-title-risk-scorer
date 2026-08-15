package com.titlerisk.service.factors;

import java.util.OptionalDouble;

/**
 * Immutable result of evaluating a single {@link RiskFactor} against a parcel.
 *
 * <p>Bundles together everything the UI needs to explain one line item of the
 * overall risk score: which factor this is, how it scored on its own 0-100
 * scale, how much that factor counts towards the total (its weight), and a
 * plain-English legal explanation of why it scored that way.</p>
 *
 * <p>A factor may additionally declare a <b>ceiling</b> on the final total via
 * {@link #withCeiling}. That exists because a purely additive model cannot
 * express a <i>disqualifying</i> condition: before this was added, a parcel
 * with an active lawsuit but otherwise spotless paperwork scored 76 and was
 * labelled "Low risk", which is misleading in a due-diligence tool. A ceiling
 * lets the factor say "whatever else is clean, this cannot read better than
 * X". Keeping the ceiling on the score rather than on the interface means a
 * future factor can declare one without {@code RiskScoringService} changing.</p>
 */
public final class FactorScore {

    private final String factorName;
    private final double rawScore;
    private final double weight;
    private final String explanation;

    /** Null means "this factor imposes no ceiling", which is the common case. */
    private final Double ceiling;
    private final String ceilingReason;

    public FactorScore(String factorName, double rawScore, double weight, String explanation) {
        this(factorName, rawScore, weight, explanation, null, null);
    }

    private FactorScore(String factorName, double rawScore, double weight, String explanation,
                        Double ceiling, String ceilingReason) {
        this.factorName = factorName;
        this.rawScore = rawScore;
        this.weight = weight;
        this.explanation = explanation;
        this.ceiling = ceiling;
        this.ceilingReason = ceilingReason;
    }

    /**
     * Returns a copy of this score that also caps the parcel's final total.
     *
     * @param ceiling      the highest total this parcel may end up with
     * @param ceilingReason plain-English reason, shown to the user so the capped
     *                      number never looks arbitrary
     */
    public FactorScore withCeiling(double ceiling, String ceilingReason) {
        return new FactorScore(factorName, rawScore, weight, explanation, ceiling, ceilingReason);
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

    /** Empty unless this factor found a disqualifying condition. */
    public OptionalDouble getCeiling() {
        return ceiling == null ? OptionalDouble.empty() : OptionalDouble.of(ceiling);
    }

    /** Null unless {@link #getCeiling()} is present. */
    public String getCeilingReason() {
        return ceilingReason;
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
