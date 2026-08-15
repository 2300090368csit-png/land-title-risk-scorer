package com.titlerisk.dto;

import java.util.List;

/**
 * JSON shape for the parcel detail page ({@code GET /api/parcels/{id}}) —
 * the raw parcel facts plus the full weighted score breakdown.
 */
public record ParcelDetailResponse(
        Long id,
        String surveyNo,
        String sellerName,
        String locationArea,
        String ecStatus,
        String litigationStatus,
        String layoutApproval,
        String reraStatus,
        String meeBhoomiMatch,
        long score,
        String riskBand,
        /** Weighted sum before any cap. Equals {@code score} when nothing capped it. */
        long uncappedScore,
        /** Why the score was capped, or null when it wasn't. */
        String ceilingReason,
        List<FactorBreakdownResponse> factors
) {
}
