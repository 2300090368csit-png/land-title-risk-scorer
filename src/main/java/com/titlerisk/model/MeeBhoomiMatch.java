package com.titlerisk.model;

/**
 * Whether the parcel's physical/paper records match its entry in MeeBhoomi,
 * Andhra Pradesh's digital land records portal (meebhoomi.ap.gov.in). A
 * mismatch signals a data consistency risk between what's on paper and what
 * the state's own digital system of record shows.
 */
public enum MeeBhoomiMatch {
    /** Digital record matches physical documents (survey number, extent, owner). */
    MATCHED,
    /** Digital record differs from physical documents in some material way. */
    MISMATCH
}
