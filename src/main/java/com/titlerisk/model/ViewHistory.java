package com.titlerisk.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

/**
 * One record of a signed-in user checking a property's score. Written
 * automatically whenever {@code GET /api/parcels/{id}} is called by an
 * authenticated user — see {@code ParcelApiController}.
 *
 * <p>The parcel's details (survey number, location, score, risk band) are
 * snapshotted at the time of viewing rather than looked up fresh each time
 * the history page loads. That's deliberate: it's a record of "what this
 * user saw when they checked it," not a live re-score, and it means the
 * history page never has to re-run the scoring engine just to render a list.</p>
 */
@Entity
public class ViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private Long parcelId;
    private String surveyNo;
    private String locationArea;
    private long score;
    private String riskBand;
    private Instant viewedAt;

    public ViewHistory() {
    }

    public ViewHistory(String username, Long parcelId, String surveyNo, String locationArea,
                        long score, String riskBand, Instant viewedAt) {
        this.username = username;
        this.parcelId = parcelId;
        this.surveyNo = surveyNo;
        this.locationArea = locationArea;
        this.score = score;
        this.riskBand = riskBand;
        this.viewedAt = viewedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getParcelId() {
        return parcelId;
    }

    public void setParcelId(Long parcelId) {
        this.parcelId = parcelId;
    }

    public String getSurveyNo() {
        return surveyNo;
    }

    public void setSurveyNo(String surveyNo) {
        this.surveyNo = surveyNo;
    }

    public String getLocationArea() {
        return locationArea;
    }

    public void setLocationArea(String locationArea) {
        this.locationArea = locationArea;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public String getRiskBand() {
        return riskBand;
    }

    public void setRiskBand(String riskBand) {
        this.riskBand = riskBand;
    }

    public Instant getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(Instant viewedAt) {
        this.viewedAt = viewedAt;
    }
}
