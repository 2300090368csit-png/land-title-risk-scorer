package com.titlerisk.service.factors;

import com.titlerisk.model.Parcel;

/**
 * A single, independent dimension of land title risk (e.g. Encumbrance
 * Certificate status, litigation status).
 *
 * <p>Each implementation encapsulates the domain/legal reasoning for exactly
 * one factor and is registered as a Spring {@code @Component}, so
 * {@link com.titlerisk.service.RiskScoringService} can discover and combine
 * all of them without knowing any concrete factor class. See the Javadoc on
 * {@code RiskScoringService} for why this matters architecturally.</p>
 */
public interface RiskFactor {

    /**
     * How much this factor counts towards the final weighted score, as a
     * fraction of 1.0. Across all registered factors these should sum to 1.0.
     */
    double getWeight();

    /**
     * Evaluate this single factor for the given parcel.
     *
     * @param parcel the parcel under review
     * @return a {@link FactorScore} with a raw 0-100 score and a plain-English
     *         explanation of the legal reasoning behind that score
     */
    FactorScore evaluate(Parcel parcel);
}
