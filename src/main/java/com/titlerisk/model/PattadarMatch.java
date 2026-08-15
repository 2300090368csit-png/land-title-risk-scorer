package com.titlerisk.model;

/**
 * Whether the person selling the land is actually the registered pattadar in
 * the Record of Rights (ROR-1B), and holds a matching pattadar passbook.
 *
 * <p>ROR-1B is Andhra Pradesh's primary ownership record — the state's
 * equivalent of Karnataka's RTC or Maharashtra's 7/12. If the seller's name
 * is not the one on it, they may still hold a sale deed, but the revenue
 * record does not recognise them as owner, which points to an unmutated
 * transfer, a disputed succession, or a sale by someone acting on an
 * unregistered power of attorney.</p>
 */
public enum PattadarMatch {
    /** Seller is the recorded pattadar and the passbook matches the deed. */
    MATCHED,
    /** Record exists but the name/extent differs from the seller's documents. */
    NAME_MISMATCH,
    /** No ROR-1B entry for this survey number in the seller's name at all. */
    NOT_IN_RECORD
}
