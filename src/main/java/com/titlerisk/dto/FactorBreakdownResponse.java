package com.titlerisk.dto;

/**
 * JSON shape for a single factor's contribution to a parcel's score, as
 * shown on the detail page — one of these per {@code RiskFactor}.
 */
public record FactorBreakdownResponse(
        String factorName,
        double rawScore,
        int weightPercent,
        String explanation
) {
}
