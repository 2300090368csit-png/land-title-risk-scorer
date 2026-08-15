package com.titlerisk.controller;

import com.titlerisk.dto.CreateParcelRequest;
import com.titlerisk.dto.ParcelDetailResponse;
import com.titlerisk.dto.ParcelMapper;
import com.titlerisk.dto.ParcelSummaryResponse;
import com.titlerisk.model.Parcel;
import com.titlerisk.model.ViewHistory;
import com.titlerisk.repository.ParcelRepository;
import com.titlerisk.repository.ViewHistoryRepository;
import com.titlerisk.service.RiskResult;
import com.titlerisk.service.RiskScoringService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

/**
 * JSON API backing the frontend: list every property with its score, fetch
 * one property's full breakdown, and add a new one. The static frontend
 * under {@code src/main/resources/static} is the only consumer of this
 * today, but nothing here is frontend-specific — this could just as easily
 * sit behind a mobile app or a curl script.
 */
@RestController
@RequestMapping("/api/parcels")
public class ParcelApiController {

    private final ParcelRepository parcelRepository;
    private final RiskScoringService riskScoringService;
    private final ViewHistoryRepository viewHistoryRepository;

    public ParcelApiController(ParcelRepository parcelRepository, RiskScoringService riskScoringService,
                                ViewHistoryRepository viewHistoryRepository) {
        this.parcelRepository = parcelRepository;
        this.riskScoringService = riskScoringService;
        this.viewHistoryRepository = viewHistoryRepository;
    }

    @GetMapping
    public List<ParcelSummaryResponse> list() {
        return parcelRepository.findAll().stream()
                .map(parcel -> ParcelMapper.toSummary(parcel, riskScoringService.score(parcel)))
                .toList();
    }

    @GetMapping("/{id}")
    public ParcelDetailResponse detail(@PathVariable Long id, Authentication authentication) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No parcel with id " + id));
        RiskResult result = riskScoringService.score(parcel);

        // Every time a signed-in user looks up a property's score, that's exactly
        // the moment the History page is meant to capture — so log it right here,
        // rather than requiring the frontend to remember to make a second call.
        if (authentication != null) {
            viewHistoryRepository.save(new ViewHistory(
                    authentication.getName(), parcel.getId(), parcel.getSurveyNo(),
                    parcel.getLocationArea(), result.getRoundedScore(), result.getRiskBand(), Instant.now()
            ));
        }

        return ParcelMapper.toDetail(parcel, result);
    }

    /**
     * Adds a new property from the "Add a property" form and returns its
     * computed score right away, the same shape {@code GET /{id}} returns —
     * so the frontend can redirect straight to the detail page after saving.
     */
    @PostMapping
    public ResponseEntity<ParcelDetailResponse> create(@RequestBody CreateParcelRequest request) {
        validate(request);

        Parcel parcel = new Parcel(
                request.surveyNo().trim(),
                request.sellerName().trim(),
                request.locationArea().trim(),
                request.prohibitedStatus(),
                request.landClassification(),
                request.ecStatus(),
                request.litigationStatus(),
                request.pattadarMatch(),
                request.layoutApproval(),
                request.nalaStatus(),
                request.reraStatus(),
                request.meeBhoomiMatch()
        );
        Parcel saved = parcelRepository.save(parcel);

        ParcelDetailResponse response = ParcelMapper.toDetail(saved, riskScoringService.score(saved));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Manual field-by-field validation rather than pulling in the Bean
     * Validation starter for one endpoint — every field here is required
     * because every field feeds directly into the score.
     */
    private void validate(CreateParcelRequest request) {
        requireText(request.surveyNo(), "Survey number");
        requireText(request.sellerName(), "Seller name");
        requireText(request.locationArea(), "Location");
        requireNotNull(request.prohibitedStatus(), "Section 22A prohibited list status");
        requireNotNull(request.landClassification(), "Land classification");
        requireNotNull(request.ecStatus(), "Encumbrance Certificate status");
        requireNotNull(request.litigationStatus(), "Litigation status");
        requireNotNull(request.pattadarMatch(), "Pattadar / ROR-1B status");
        requireNotNull(request.layoutApproval(), "Layout approval status");
        requireNotNull(request.nalaStatus(), "NALA conversion status");
        requireNotNull(request.reraStatus(), "RERA status");
        requireNotNull(request.meeBhoomiMatch(), "MeeBhoomi match status");
    }

    private void requireText(String value, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldLabel + " is required.");
        }
    }

    private void requireNotNull(Object value, String fieldLabel) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldLabel + " is required.");
        }
    }
}
