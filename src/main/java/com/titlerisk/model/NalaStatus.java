package com.titlerisk.model;

/**
 * Non-Agricultural Land Assessment (NALA) conversion status.
 *
 * <p>Agricultural land in Andhra Pradesh has to be formally converted before
 * it can be used for residential, commercial or industrial purposes —
 * an application to the Revenue Divisional Officer under the AP Agricultural
 * Land (Conversion for Non-Agricultural Purposes) Act. Building on
 * unconverted agricultural land is illegal, and the conversion itself can be
 * refused if the land turns out to be assigned, government, wakf, or inside
 * a water-body buffer, which makes this check a useful second signal on the
 * land's underlying status.</p>
 */
public enum NalaStatus {
    /** Conversion granted; the land may lawfully be used for non-agricultural purposes. */
    CONVERTED,
    /** Already a non-agricultural plot, or being bought to remain farmland — conversion isn't in question. */
    NOT_REQUIRED,
    /** Application filed with the RDO but not yet granted. */
    PENDING,
    /** Still agricultural, with no conversion applied for. */
    NOT_CONVERTED
}
