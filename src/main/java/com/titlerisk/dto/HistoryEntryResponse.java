package com.titlerisk.dto;

/** JSON shape for one row on the "History" page — a snapshot of one past score check. */
public record HistoryEntryResponse(
        Long id,
        Long parcelId,
        String surveyNo,
        String locationArea,
        long score,
        String riskBand,
        String viewedAt
) {
}
