package com.titlerisk.service.factors;

import com.titlerisk.model.Parcel;
import com.titlerisk.model.PattadarMatch;
import org.springframework.stereotype.Component;

/**
 * Checks that the seller is the pattadar actually recorded in the ROR-1B,
 * with a matching pattadar passbook. Weight: 12%.
 *
 * <p>Distinct from the MeeBhoomi record-match factor, which asks whether the
 * <i>parcel</i> details agree between paper and portal. This asks the sharper
 * question of whether the <i>person selling it</i> is the recorded owner.</p>
 */
@Component
public class PattadarFactor implements RiskFactor {

    private static final double WEIGHT = 0.12;

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public FactorScore evaluate(Parcel parcel) {
        PattadarMatch match = parcel.getPattadarMatch();

        if (match == PattadarMatch.MATCHED) {
            return new FactorScore(
                    "Pattadar / ROR-1B Ownership",
                    100,
                    WEIGHT,
                    "The seller is the pattadar recorded in the ROR-1B and the passbook matches the "
                            + "deed — the revenue record and the paper title agree on who owns this land."
            );
        }

        if (match == PattadarMatch.NAME_MISMATCH) {
            // Often an unmutated inheritance rather than fraud, but it means the
            // revenue record does not currently support the seller's claim.
            return new FactorScore(
                    "Pattadar / ROR-1B Ownership",
                    35,
                    WEIGHT,
                    "The ROR-1B exists but the recorded name or extent differs from the seller's "
                            + "documents. Frequently an inheritance that was never mutated, but until it is "
                            + "corrected the revenue record does not recognise the seller as owner."
            );
        }

        // NOT_IN_RECORD
        return new FactorScore(
                "Pattadar / ROR-1B Ownership",
                10,
                WEIGHT,
                "There is no ROR-1B entry for this survey number in the seller's name. They may be "
                        + "selling on an unregistered power of attorney — which the Supreme Court held in "
                        + "Suraj Lamp conveys no title — or the holding may never have been mutated to them "
                        + "at all."
        );
    }
}
