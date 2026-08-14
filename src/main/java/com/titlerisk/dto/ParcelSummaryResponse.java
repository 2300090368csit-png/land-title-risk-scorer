package com.titlerisk.dto;

/**
 * JSON shape for one row of the parcel list ({@code GET /api/parcels}).
 * Deliberately thin — just enough for the list view, so the frontend isn't
 * pulling down the full factor breakdown for every row on the page.
 */
public record ParcelSummaryResponse(
        Long id,
        String surveyNo,
        String sellerName,
        String locationArea,
        long score,
        String riskBand
) {
}
