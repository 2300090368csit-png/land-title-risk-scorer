package com.titlerisk.service.factors;

import com.titlerisk.model.LayoutApprovalStatus;
import com.titlerisk.model.Parcel;
import org.springframework.stereotype.Component;

/**
 * Checks layout approval status with the relevant Andhra Pradesh planning
 * authority — APCRDA for parcels in the Amaravati capital region, VMRDA
 * around Visakhapatnam, or DTCP elsewhere in the state. Weight: 9%.
 *
 * An unapproved layout doesn't necessarily mean the title itself is bad, but
 * it means no authority has signed off on the plotting/roads/open space of
 * the layout the parcel sits in - which matters a lot in practice for
 * getting a building permission or a bank loan later.
 */
@Component
public class LayoutApprovalFactor implements RiskFactor {

    private static final double WEIGHT = 0.09;

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public FactorScore evaluate(Parcel parcel) {
        LayoutApprovalStatus approval = parcel.getLayoutApproval();

        if (approval == LayoutApprovalStatus.APPROVED) {
            // Layout has cleared the applicable authority's scrutiny already, so
            // future construction permissions and loan sanctions have one less hurdle.
            return new FactorScore(
                    "Layout Approval",
                    100,
                    WEIGHT,
                    "Layout has formal approval from the applicable authority. Roads, open space, "
                            + "and plotting have already passed regulatory review, which removes a major "
                            + "hurdle for future construction permissions and bank financing."
            );
        }

        if (approval == LayoutApprovalStatus.PENDING) {
            // Application is in the pipeline. Risk is real but bounded - it may
            // simply come through, unlike an UNAPPROVED layout with no application
            // filed at all.
            return new FactorScore(
                    "Layout Approval",
                    55,
                    WEIGHT,
                    "Layout approval has been applied for but is not yet granted. There is a real "
                            + "chance it is denied or delayed, so this should be tracked before relying on "
                            + "the layout for construction or financing."
            );
        }

        // UNAPPROVED: no application on file at all. This is the layout-level
        // equivalent of an unregistered building - state regularization schemes
        // for unapproved layouts do exist from time to time, but they're neither
        // guaranteed nor free, so buyers should price that uncertainty in.
        return new FactorScore(
                "Layout Approval",
                10,
                WEIGHT,
                "No layout approval exists for this parcel. Unapproved layouts carry real "
                        + "regularization risk and can block construction permissions until (and unless) "
                        + "the layout is regularized under a future state scheme."
        );
    }
}
