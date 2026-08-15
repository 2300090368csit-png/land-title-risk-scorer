package com.titlerisk.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * A single land parcel under title due-diligence review.
 *
 * <p>This is a plain JPA entity holding raw facts about the parcel (who is
 * selling it, where it is, and the outcome of the nine due-diligence checks).
 * It deliberately contains no scoring logic — that responsibility belongs to
 * {@link com.titlerisk.service.RiskScoringService} and the individual
 * {@code RiskFactor} implementations, keeping this class a simple data holder.</p>
 */
@Entity
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Revenue survey number identifying the parcel, e.g. "142/2A". */
    private String surveyNo;

    /** Name of the person/entity currently listed as the seller. */
    private String sellerName;

    /** Village/mandal or locality the parcel is in, e.g. "Tullur, Guntur". */
    private String locationArea;

    @Enumerated(EnumType.STRING)
    private ProhibitedPropertyStatus prohibitedStatus;

    @Enumerated(EnumType.STRING)
    private LandClassification landClassification;

    @Enumerated(EnumType.STRING)
    private EcStatus ecStatus;

    @Enumerated(EnumType.STRING)
    private LitigationStatus litigationStatus;

    @Enumerated(EnumType.STRING)
    private PattadarMatch pattadarMatch;

    @Enumerated(EnumType.STRING)
    private LayoutApprovalStatus layoutApproval;

    @Enumerated(EnumType.STRING)
    private NalaStatus nalaStatus;

    @Enumerated(EnumType.STRING)
    private ReraStatus reraStatus;

    @Enumerated(EnumType.STRING)
    private MeeBhoomiMatch meeBhoomiMatch;

    /** No-args constructor required by JPA. */
    public Parcel() {
    }

    public Parcel(String surveyNo, String sellerName, String locationArea,
                  ProhibitedPropertyStatus prohibitedStatus, LandClassification landClassification,
                  EcStatus ecStatus, LitigationStatus litigationStatus, PattadarMatch pattadarMatch,
                  LayoutApprovalStatus layoutApproval, NalaStatus nalaStatus,
                  ReraStatus reraStatus, MeeBhoomiMatch meeBhoomiMatch) {
        this.surveyNo = surveyNo;
        this.sellerName = sellerName;
        this.locationArea = locationArea;
        this.prohibitedStatus = prohibitedStatus;
        this.landClassification = landClassification;
        this.ecStatus = ecStatus;
        this.litigationStatus = litigationStatus;
        this.pattadarMatch = pattadarMatch;
        this.layoutApproval = layoutApproval;
        this.nalaStatus = nalaStatus;
        this.reraStatus = reraStatus;
        this.meeBhoomiMatch = meeBhoomiMatch;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSurveyNo() {
        return surveyNo;
    }

    public void setSurveyNo(String surveyNo) {
        this.surveyNo = surveyNo;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getLocationArea() {
        return locationArea;
    }

    public void setLocationArea(String locationArea) {
        this.locationArea = locationArea;
    }

    public ProhibitedPropertyStatus getProhibitedStatus() {
        return prohibitedStatus;
    }

    public void setProhibitedStatus(ProhibitedPropertyStatus prohibitedStatus) {
        this.prohibitedStatus = prohibitedStatus;
    }

    public LandClassification getLandClassification() {
        return landClassification;
    }

    public void setLandClassification(LandClassification landClassification) {
        this.landClassification = landClassification;
    }

    public EcStatus getEcStatus() {
        return ecStatus;
    }

    public void setEcStatus(EcStatus ecStatus) {
        this.ecStatus = ecStatus;
    }

    public LitigationStatus getLitigationStatus() {
        return litigationStatus;
    }

    public void setLitigationStatus(LitigationStatus litigationStatus) {
        this.litigationStatus = litigationStatus;
    }

    public PattadarMatch getPattadarMatch() {
        return pattadarMatch;
    }

    public void setPattadarMatch(PattadarMatch pattadarMatch) {
        this.pattadarMatch = pattadarMatch;
    }

    public LayoutApprovalStatus getLayoutApproval() {
        return layoutApproval;
    }

    public void setLayoutApproval(LayoutApprovalStatus layoutApproval) {
        this.layoutApproval = layoutApproval;
    }

    public NalaStatus getNalaStatus() {
        return nalaStatus;
    }

    public void setNalaStatus(NalaStatus nalaStatus) {
        this.nalaStatus = nalaStatus;
    }

    public ReraStatus getReraStatus() {
        return reraStatus;
    }

    public void setReraStatus(ReraStatus reraStatus) {
        this.reraStatus = reraStatus;
    }

    public MeeBhoomiMatch getMeeBhoomiMatch() {
        return meeBhoomiMatch;
    }

    public void setMeeBhoomiMatch(MeeBhoomiMatch meeBhoomiMatch) {
        this.meeBhoomiMatch = meeBhoomiMatch;
    }
}
