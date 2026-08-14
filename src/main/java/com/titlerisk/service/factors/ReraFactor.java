package com.titlerisk.service.factors;

import com.titlerisk.model.Parcel;
import com.titlerisk.model.ReraStatus;
import org.springframework.stereotype.Component;

/**
 * Checks RERA (Real Estate Regulation and Development Act, 2016) registration.
 * Weight: 15% — lower than the other factors because RERA is a marketing/sale
 * compliance requirement, not a title-history check like the EC or litigation.
 */
@Component
public class ReraFactor implements RiskFactor {

    private static final double WEIGHT = 0.15;

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public FactorScore evaluate(Parcel parcel) {
        ReraStatus status = parcel.getReraStatus();

        if (status == ReraStatus.REGISTERED) {
            return new FactorScore(
                    "RERA Registration",
                    100,
                    WEIGHT,
                    "Project/layout is registered with Andhra Pradesh RERA (AP RERA), satisfying the "
                            + "disclosure and compliance obligations required for anything actively marketed "
                            + "for sale."
            );
        }

        if (status == ReraStatus.NOT_APPLICABLE) {
            // Plain resale/agricultural land that isn't being marketed as part of a
            // "project" doesn't fall under RERA at all, so this isn't really a red
            // flag - just a small deduction since there's no positive compliance
            // signal to point to either way.
            return new FactorScore(
                    "RERA Registration",
                    85,
                    WEIGHT,
                    "RERA registration does not apply — this is a direct land parcel transaction, not "
                            + "a marketed real estate project. Minor deduction only because there is no "
                            + "positive compliance record either way."
            );
        }

        // NOT_REGISTERED: the parcel/project is being marketed for sale and should
        // be registered but isn't. That's a genuine compliance gap, not a
        // technicality - RERA registration is what gives buyers recourse if
        // possession/construction promises aren't kept.
        return new FactorScore(
                "RERA Registration",
                30,
                WEIGHT,
                "This project is being marketed for sale but is not RERA-registered. That's a "
                        + "compliance gap under the RERA Act, and it removes the regulatory recourse a "
                        + "buyer would otherwise have if delivery promises aren't kept."
        );
    }
}
