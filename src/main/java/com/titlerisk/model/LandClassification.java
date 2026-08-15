package com.titlerisk.model;

/**
 * How the land is classified in the Record of Rights (ROR-1B) "Nature of
 * Land" column and the Adangal remarks — read from MeeBhoomi.
 *
 * <p>Classification decides whether the land is capable of private sale at
 * all, which is a different question from whether the paperwork is tidy.
 * Land assigned to the landless poor under the AP Assigned Lands
 * (Prohibition of Transfers) Act, 1977 cannot be transferred without the
 * District Collector's permission; a sale in breach is void and the
 * government can resume the land and restore it to the original assignee
 * even after it has changed hands several times. Government (poramboke),
 * endowment and wakf land cannot pass into private ownership at all.</p>
 */
public enum LandClassification {
    /** Ordinary private patta land, freely transferable. */
    PRIVATE_PATTA,
    /** Assigned land / D-Patta — transfer restricted under the 1977 Act. */
    ASSIGNED_DPATTA,
    /** Government or poramboke land — roads, drains, tanks, revenue reserve. */
    GOVERNMENT_PORAMBOKE,
    /** Temple/endowment or wakf property — inalienable. */
    ENDOWMENT_WAKF
}
