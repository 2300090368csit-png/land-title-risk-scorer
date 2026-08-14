package com.titlerisk.model;

/**
 * Result of checking a parcel's Encumbrance Certificate (EC) — the official
 * record of every registered transaction, mortgage, and lien against a survey
 * number. The EC is the primary documentary evidence of a title's history.
 */
public enum EcStatus {
    /** No registered liens, mortgages, or disputed entries found. */
    CLEAN,
    /** One or more entries (unreleased mortgage, disputed sale, lien, etc.) were found. */
    FLAGGED
}
