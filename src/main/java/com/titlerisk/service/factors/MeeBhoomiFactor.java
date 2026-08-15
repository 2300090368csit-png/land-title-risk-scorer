package com.titlerisk.service.factors;

import com.titlerisk.model.MeeBhoomiMatch;
import com.titlerisk.model.Parcel;
import org.springframework.stereotype.Component;

/**
 * Cross-checks the parcel against MeeBhoomi, Andhra Pradesh's digital land
 * records portal. Weight: 4% — the lightest factor, since a mismatch here is
 * usually a data-entry problem rather than a title defect. The sharper
 * ownership question is handled by PattadarFactor, which asks whether the
 * seller is the recorded pattadar at all.
 */
@Component
public class MeeBhoomiFactor implements RiskFactor {

    private static final double WEIGHT = 0.04;

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public FactorScore evaluate(Parcel parcel) {
        MeeBhoomiMatch match = parcel.getMeeBhoomiMatch();

        if (match == MeeBhoomiMatch.MATCHED) {
            return new FactorScore(
                    "MeeBhoomi Digital Record",
                    100,
                    WEIGHT,
                    "Digital MeeBhoomi record matches the physical documents (survey number, "
                            + "extent, and owner name all line up). No data consistency issue found."
            );
        }

        // A mismatch usually points to a stale mutation, a surveying/extent
        // discrepancy, or an owner-name transliteration issue rather than a
        // hidden ownership dispute - but since MeeBhoomi is the system the
        // registration department and banks actually rely on day to day, any
        // mismatch still has to be corrected before it causes a delay at
        // registration or loan sanction time.
        return new FactorScore(
                "MeeBhoomi Digital Record",
                25,
                WEIGHT,
                "Physical records do not match the MeeBhoomi digital entry for this parcel. "
                        + "Likely a mutation or data-entry gap rather than a title defect, but it will "
                        + "need to be corrected before registration or loan processing can proceed smoothly."
        );
    }
}
