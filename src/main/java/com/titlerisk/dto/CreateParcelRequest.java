package com.titlerisk.dto;

import com.titlerisk.model.EcStatus;
import com.titlerisk.model.LayoutApprovalStatus;
import com.titlerisk.model.LitigationStatus;
import com.titlerisk.model.MeeBhoomiMatch;
import com.titlerisk.model.ReraStatus;

/**
 * Request body for {@code POST /api/parcels} — the outcome of the 5
 * due-diligence checks for a new property, submitted from the "Add a
 * property" form. Kept separate from {@link com.titlerisk.model.Parcel} so
 * the API's input shape doesn't depend on JPA entity internals (no id here —
 * the database assigns that).
 */
public record CreateParcelRequest(
        String surveyNo,
        String sellerName,
        String locationArea,
        EcStatus ecStatus,
        LitigationStatus litigationStatus,
        LayoutApprovalStatus layoutApproval,
        ReraStatus reraStatus,
        MeeBhoomiMatch meeBhoomiMatch
) {
}
