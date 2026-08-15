package com.titlerisk.dto;

import com.titlerisk.model.EcStatus;
import com.titlerisk.model.LandClassification;
import com.titlerisk.model.LayoutApprovalStatus;
import com.titlerisk.model.LitigationStatus;
import com.titlerisk.model.MeeBhoomiMatch;
import com.titlerisk.model.NalaStatus;
import com.titlerisk.model.PattadarMatch;
import com.titlerisk.model.ProhibitedPropertyStatus;
import com.titlerisk.model.ReraStatus;

/**
 * Request body for {@code POST /api/parcels} — the outcome of the nine
 * due-diligence checks for a new property, submitted from the "Add a
 * property" form. Kept separate from {@link com.titlerisk.model.Parcel} so
 * the API's input shape doesn't depend on JPA entity internals (no id here —
 * the database assigns that).
 */
public record CreateParcelRequest(
        String surveyNo,
        String sellerName,
        String locationArea,
        ProhibitedPropertyStatus prohibitedStatus,
        LandClassification landClassification,
        EcStatus ecStatus,
        LitigationStatus litigationStatus,
        PattadarMatch pattadarMatch,
        LayoutApprovalStatus layoutApproval,
        NalaStatus nalaStatus,
        ReraStatus reraStatus,
        MeeBhoomiMatch meeBhoomiMatch
) {
}
