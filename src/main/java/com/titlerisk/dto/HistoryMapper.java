package com.titlerisk.dto;

import com.titlerisk.model.ViewHistory;

/** Turns a {@link ViewHistory} row into the JSON shape the History page consumes. */
public final class HistoryMapper {

    private HistoryMapper() {
    }

    public static HistoryEntryResponse toResponse(ViewHistory entry) {
        return new HistoryEntryResponse(
                entry.getId(),
                entry.getParcelId(),
                entry.getSurveyNo(),
                entry.getLocationArea(),
                entry.getScore(),
                entry.getRiskBand(),
                entry.getViewedAt().toString()
        );
    }
}
