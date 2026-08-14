package com.titlerisk.model;

/**
 * Registration status under the Real Estate (Regulation and Development) Act,
 * 2016 (RERA). RERA registration is mandatory for projects/plots that are
 * marketed for sale; its absence on a marketed project signals a compliance gap.
 */
public enum ReraStatus {
    /** Project/layout is registered with Andhra Pradesh RERA (AP RERA). */
    REGISTERED,
    /** Registration is required (project is being marketed for sale) but missing. */
    NOT_REGISTERED,
    /** RERA registration does not apply, e.g. a plain agricultural/resale land parcel. */
    NOT_APPLICABLE
}
