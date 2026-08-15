package com.titlerisk.service.factors;

import com.titlerisk.model.LandClassification;
import com.titlerisk.model.Parcel;
import org.springframework.stereotype.Component;

/**
 * Checks the ROR-1B "Nature of Land" classification and Adangal remarks.
 * Weight: 16%.
 *
 * <p>This asks whether the land is capable of private sale at all, which is
 * a prior question to whether the paperwork is clean. Its failure modes are
 * therefore ceilings rather than deductions.</p>
 */
@Component
public class LandClassificationFactor implements RiskFactor {

    private static final double WEIGHT = 0.16;

    /**
     * Assigned land is restricted rather than absolutely inalienable — the
     * District Collector can permit a transfer, and the state has regularised
     * assigned land in the past — so it sits marginally above the
     * never-transferable categories.
     */
    private static final double ASSIGNED_CEILING = 10;

    /** Government, endowment and wakf land cannot pass into private hands at all. */
    private static final double INALIENABLE_CEILING = 5;

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public FactorScore evaluate(Parcel parcel) {
        LandClassification classification = parcel.getLandClassification();

        switch (classification) {
            case PRIVATE_PATTA:
                return new FactorScore(
                        "Land Classification",
                        100,
                        WEIGHT,
                        "Recorded as private patta land in the ROR-1B, with no restrictive entry in the "
                                + "Adangal remarks. Freely transferable subject to the other checks."
                );

            case ASSIGNED_DPATTA:
                return new FactorScore(
                        "Land Classification",
                        5,
                        WEIGHT,
                        "This is assigned land (D-Patta), granted to a landless beneficiary. Transfer "
                                + "without the District Collector's prior permission is void under the AP "
                                + "Assigned Lands (Prohibition of Transfers) Act, 1977, and the government can "
                                + "resume the land and restore it to the original assignee even after it has "
                                + "changed hands."
                ).withCeiling(
                        ASSIGNED_CEILING,
                        "Score capped at " + (int) ASSIGNED_CEILING + " because this is assigned (D-Patta) "
                                + "land. A sale without the Collector's permission is void, so the rest of the "
                                + "file cannot make it safe."
                );

            case ENDOWMENT_WAKF:
                return new FactorScore(
                        "Land Classification",
                        0,
                        WEIGHT,
                        "Recorded as endowment or wakf property. Land dedicated to religious or charitable "
                                + "purposes is inalienable and cannot be acquired by a private buyer."
                ).withCeiling(
                        INALIENABLE_CEILING,
                        "Score capped at " + (int) INALIENABLE_CEILING + " because endowment and wakf land "
                                + "cannot lawfully pass into private ownership."
                );

            case GOVERNMENT_PORAMBOKE:
            default:
                return new FactorScore(
                        "Land Classification",
                        0,
                        WEIGHT,
                        "Recorded as government or poramboke land — revenue reserve, water body, road or "
                                + "drain. There is no private title to buy here, whatever documents the seller "
                                + "produces."
                ).withCeiling(
                        INALIENABLE_CEILING,
                        "Score capped at " + (int) INALIENABLE_CEILING + " because government/poramboke land "
                                + "is not privately ownable."
                );
        }
    }
}
