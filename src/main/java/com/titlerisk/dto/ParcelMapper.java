package com.titlerisk.dto;

import com.titlerisk.model.Parcel;
import com.titlerisk.service.RiskResult;
import com.titlerisk.service.factors.FactorScore;

/**
 * Turns a {@link Parcel} + its computed {@link RiskResult} into the JSON
 * response shapes the REST API returns. Kept separate from the entity and
 * from the controller so neither has to know about the other's format —
 * the entity stays a plain data holder, the controller stays thin.
 */
public final class ParcelMapper {

    private ParcelMapper() {
        // static helper class, not meant to be instantiated
    }

    public static ParcelSummaryResponse toSummary(Parcel parcel, RiskResult result) {
        return new ParcelSummaryResponse(
                parcel.getId(),
                parcel.getSurveyNo(),
                parcel.getSellerName(),
                parcel.getLocationArea(),
                result.getRoundedScore(),
                result.getRiskBand()
        );
    }

    public static ParcelDetailResponse toDetail(Parcel parcel, RiskResult result) {
        return new ParcelDetailResponse(
                parcel.getId(),
                parcel.getSurveyNo(),
                parcel.getSellerName(),
                parcel.getLocationArea(),
                parcel.getProhibitedStatus().name(),
                parcel.getLandClassification().name(),
                parcel.getEcStatus().name(),
                parcel.getLitigationStatus().name(),
                parcel.getPattadarMatch().name(),
                parcel.getLayoutApproval().name(),
                parcel.getNalaStatus().name(),
                parcel.getReraStatus().name(),
                parcel.getMeeBhoomiMatch().name(),
                result.getRoundedScore(),
                result.getRiskBand(),
                Math.round(result.getUncappedScore()),
                result.getCeilingReason(),
                result.getFactorScores().stream().map(ParcelMapper::toBreakdown).toList()
        );
    }

    private static FactorBreakdownResponse toBreakdown(FactorScore factorScore) {
        return new FactorBreakdownResponse(
                factorScore.getFactorName(),
                factorScore.getRawScore(),
                factorScore.getWeightPercent(),
                factorScore.getExplanation()
        );
    }
}
