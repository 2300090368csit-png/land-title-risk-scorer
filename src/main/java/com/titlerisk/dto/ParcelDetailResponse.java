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
        List<FactorBreakdownResponse> factors
) {
}
