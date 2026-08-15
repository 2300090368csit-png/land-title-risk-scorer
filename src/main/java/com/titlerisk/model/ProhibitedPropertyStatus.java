package com.titlerisk.model;

/**
 * Whether the parcel appears on the prohibited properties list maintained
 * under <b>Section 22A of the Registration Act, 1908</b>.
 *
 * <p>This is the single most decisive check in Andhra Pradesh land
 * due diligence. Section 22A lets the state block registration of property
 * it considers "opposed to public policy" — typically government land, land
 * assigned to the landless poor, endowment and wakf property, and forest
 * land. If a survey number is on that list the Sub-Registrar's system rejects
 * the sale deed outright: no officer can override it, and banks reject loan
 * applications against such land automatically.</p>
 *
 * <p>Checked on the IGRS AP portal under Prohibited Properties, by district,
 * mandal, village and survey number.</p>
 */
public enum ProhibitedPropertyStatus {
    /** Survey number does not appear on the district's 22A list. */
    NOT_LISTED,
    /** Listed once but subject to a pending removal/appeal application. */
    UNDER_REVIEW,
    /** On the 22A list — registration is blocked until formally removed. */
    LISTED
}
