package com.titlerisk.service.factors;

import com.titlerisk.model.Parcel;
import com.titlerisk.model.ProhibitedPropertyStatus;
import org.springframework.stereotype.Component;

/**
 * Checks the Section 22A prohibited properties list. Weight: 18% — the
 * heaviest factor, because it is the only check whose failure makes the
 * transaction legally impossible rather than merely risky.
 */
@Component
public class ProhibitedPropertyFactor implements RiskFactor {

    private static final double WEIGHT = 0.18;

    /**
     * A 22A listing is an absolute bar, not a discount. The Sub-Registrar's
     * system refuses the deed and re-checks at registration even if the
     * listing was missed earlier, so no combination of clean paperwork can
     * make such a parcel purchasable. The ceiling is therefore set low enough
     * to land firmly in the "high risk" band whatever else is true.
     */
    private static final double LISTED_CEILING = 5;

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public FactorScore evaluate(Parcel parcel) {
        ProhibitedPropertyStatus status = parcel.getProhibitedStatus();

        if (status == ProhibitedPropertyStatus.NOT_LISTED) {
            return new FactorScore(
                    "Section 22A Prohibited List",
                    100,
                    WEIGHT,
                    "This survey number does not appear on the district's Section 22A prohibited "
                            + "properties list, so registration is not administratively barred."
            );
        }

        if (status == ProhibitedPropertyStatus.UNDER_REVIEW) {
            // Removal applications do succeed - the state has periodically de-listed
            // whole categories - so this is scored as serious but not terminal.
            return new FactorScore(
                    "Section 22A Prohibited List",
                    40,
                    WEIGHT,
                    "The parcel was listed under Section 22A and a removal application is pending. "
                            + "Registration stays blocked until the listing is formally lifted, so the "
                            + "outcome of that application decides whether this land is buyable at all."
            );
        }

        // LISTED
        return new FactorScore(
                "Section 22A Prohibited List",
                0,
                WEIGHT,
                "This parcel is on the Section 22A prohibited properties list. The Sub-Registrar's "
                        + "system will reject the sale deed, no officer can override the listing, and banks "
                        + "decline loans against listed land automatically."
        ).withCeiling(
                LISTED_CEILING,
                "Score capped at " + (int) LISTED_CEILING + " because this parcel is on the Section 22A "
                        + "prohibited list. Registration is legally blocked, so no amount of clean paperwork "
                        + "elsewhere makes this land purchasable."
        );
    }
}
