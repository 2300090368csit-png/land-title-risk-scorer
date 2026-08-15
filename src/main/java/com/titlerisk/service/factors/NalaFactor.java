package com.titlerisk.service.factors;

import com.titlerisk.model.NalaStatus;
import com.titlerisk.model.Parcel;
import org.springframework.stereotype.Component;

/**
 * Checks Non-Agricultural Land Assessment (NALA) conversion status.
 * Weight: 6%.
 *
 * <p>Weighted modestly because it constrains what the buyer may <i>do</i>
 * with the land rather than whether they can own it — and because it doesn't
 * apply at all to a plot that is already non-agricultural or to farmland
 * bought to stay farmland.</p>
 */
@Component
public class NalaFactor implements RiskFactor {

    private static final double WEIGHT = 0.06;

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public FactorScore evaluate(Parcel parcel) {
        NalaStatus status = parcel.getNalaStatus();

        switch (status) {
            case CONVERTED:
                return new FactorScore(
                        "NALA Conversion",
                        100,
                        WEIGHT,
                        "Conversion to non-agricultural use has been granted, so the land may lawfully be "
                                + "built on and the conversion process has already tested it against the "
                                + "restrictions the RDO checks."
                );

            case NOT_REQUIRED:
                return new FactorScore(
                        "NALA Conversion",
                        95,
                        WEIGHT,
                        "Conversion does not arise — the land is already non-agricultural, or is being "
                                + "bought to remain farmland."
                );

            case PENDING:
                return new FactorScore(
                        "NALA Conversion",
                        50,
                        WEIGHT,
                        "A conversion application is with the Revenue Divisional Officer but has not been "
                                + "granted. Worth tracking: the RDO refuses conversion where land turns out to "
                                + "be assigned, government, wakf, or inside a water-body buffer, so a refusal "
                                + "would signal a deeper problem than the paperwork shows."
                );

            case NOT_CONVERTED:
            default:
                return new FactorScore(
                        "NALA Conversion",
                        20,
                        WEIGHT,
                        "The land is still classified agricultural with no conversion applied for. Building "
                                + "on it in that state is illegal, and conversion is not guaranteed."
                );
        }
    }
}
