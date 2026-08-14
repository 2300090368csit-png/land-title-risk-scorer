package com.titlerisk.model;

/**
 * Layout approval status with the relevant Andhra Pradesh planning authority.
 * Andhra Pradesh doesn't have one single authority the way Hyderabad has
 * HMDA — approval comes from whichever body covers that area: APCRDA for
 * parcels in the Amaravati capital region, VMRDA around Visakhapatnam, or
 * the Directorate of Town and Country Planning (DTCP) elsewhere in the state.
 * An unapproved layout carries regularization risk and can block
 * construction permissions and future resale regardless of which authority
 * would have covered it.
 */
public enum LayoutApprovalStatus {
    /** Layout has been formally approved by the applicable authority (CRDA/VMRDA/DTCP). */
    APPROVED,
    /** Approval has been applied for but is not yet granted. */
    PENDING,
    /** No layout approval exists or has been sought. */
    UNAPPROVED
}
