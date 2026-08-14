package com.titlerisk.service.factors;

import com.titlerisk.model.LitigationStatus;
import com.titlerisk.model.Parcel;
import org.springframework.stereotype.Component;

/**
 * Checks whether a parcel is currently or recently tangled up in a court case.
 *
 * Weighted 25% — second only to the EC. A property that is "sub judice" is
 * risky no matter how tidy its paperwork otherwise looks, because a court
 * can still freeze the property or unwind a sale that already went through.
 */
@Component
public class LitigationFactor implements RiskFactor {

    private static final double WEIGHT = 0.25;

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public FactorScore evaluate(Parcel parcel) {
        LitigationStatus status = parcel.getLitigationStatus();

        switch (status) {
            case NONE:
                // No case on record anywhere against this survey number - nothing
                // for a court to freeze or reverse later.
                return new FactorScore(
                        "Litigation Status",
                        100,
                        WEIGHT,
                        "No pending or past litigation found against this parcel. Clear of court "
                                + "involvement that could later freeze or unwind a transaction."
                );

            case PENDING:
                // A case has been filed/noticed but isn't a full contested suit yet.
                // Still worth flagging - it can escalate, and title insurers/banks
                // usually want it resolved or at least disclosed before proceeding.
                return new FactorScore(
                        "Litigation Status",
                        50,
                        WEIGHT,
                        "A case has been filed or noticed against the parcel but has not escalated "
                                + "into a contested suit. Not disqualifying on its own, but it should be "
                                + "resolved or disclosed before the transaction closes."
                );

            case ACTIVE_SUIT:
            default:
                // This is the classic sub-judice scenario. Courts in India can and
                // do issue stay orders on registration/mutation for parcels under
                // active dispute, which can override an otherwise clean paperwork
                // trail entirely - hence the low score even if every other check
                // comes back clean.
                return new FactorScore(
                        "Litigation Status",
                        5,
                        WEIGHT,
                        "Active litigation detected — the property is sub judice. A court can stay "
                                + "registration or reverse a completed sale while the case is pending, which "
                                + "can override an otherwise clean paperwork trail."
                );
        }
    }
}
