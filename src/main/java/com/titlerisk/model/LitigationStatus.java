package com.titlerisk.model;

/**
 * Whether a parcel is, or has recently been, the subject of a court case.
 * A parcel that is "sub judice" carries risk regardless of how clean its
 * paperwork otherwise looks, because a court can freeze or reverse
 * transactions made while a case is pending.
 */
public enum LitigationStatus {
    /** No known litigation history or pending case. */
    NONE,
    /** A case has been filed or noticed but is not yet an active contested suit. */
    PENDING,
    /** An active, contested lawsuit is pending over this parcel. */
    ACTIVE_SUIT
}
