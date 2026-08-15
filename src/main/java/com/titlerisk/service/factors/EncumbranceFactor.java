package com.titlerisk.service.factors;

import com.titlerisk.model.EcStatus;
import com.titlerisk.model.Parcel;
import org.springframework.stereotype.Component;

/**
 * Scores title risk based on the Encumbrance Certificate (EC) — the official
 * record of every registered transaction, mortgage, and lien against a survey
 * number over its recorded history.
 *
 * <p>Weighted 16%. The EC is the primary documentary evidence of a clean chain
 * of title, and is the heaviest of the checks that assume the land is saleable
 * at all — only the 22A listing and land classification outrank it, because
 * those decide whether there is anything lawful to buy in the first place.</p>
 */
@Component
public class EncumbranceFactor implements RiskFactor {

    private static final double WEIGHT = 0.16;

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public FactorScore evaluate(Parcel parcel) {
        EcStatus status = parcel.getEcStatus();

        // Legal reasoning: the EC is a chronological ledger of every registered
        // dealing on the survey number (sales, mortgages, gift deeds, court
        // attachments). A CLEAN EC means no unresolved claim of any kind is on
        // record, which is the strongest single indicator of a marketable title.
        // A FLAGGED EC means some entry — an unreleased mortgage, a prior sale
        // that was never properly closed out, a lien, or a court attachment —
        // remains open against the parcel, and that claim survives a change of
        // seller: buying from a clean-looking owner does not erase it.
        if (status == EcStatus.CLEAN) {
            return new FactorScore(
                    "Encumbrance Certificate",
                    100,
                    WEIGHT,
                    "Encumbrance Certificate shows no registered liens, mortgages, or unresolved "
                            + "entries for this survey number — the strongest available evidence of a "
                            + "clean chain of title."
            );
        }

        return new FactorScore(
                "Encumbrance Certificate",
                15,
                WEIGHT,
                "Encumbrance Certificate is flagged with an unresolved entry (unreleased mortgage, "
                        + "prior disputed sale, or lien). This claim runs with the land, not the current "
                        + "seller, so it survives a change of ownership and is the single strongest "
                        + "signal of title risk here."
        );
    }
}
