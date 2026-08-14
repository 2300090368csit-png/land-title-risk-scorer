package com.titlerisk.controller;

import com.titlerisk.dto.CreateParcelRequest;
import com.titlerisk.dto.ParcelDetailResponse;
import com.titlerisk.dto.ParcelMapper;
import com.titlerisk.dto.ParcelSummaryResponse;
import com.titlerisk.model.Parcel;
import com.titlerisk.repository.ParcelRepository;
import com.titlerisk.service.RiskScoringService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    public ParcelApiController(ParcelRepository parcelRepository, RiskScoringService riskScoringService) {
        this.parcelRepository = parcelRepository;
        this.riskScoringService = riskScoringService;
    }

    @GetMapping
    public List<ParcelSummaryResponse> list() {
        return parcelRepository.findAll().stream()
                .map(parcel -> ParcelMapper.toSummary(parcel, riskScoringService.score(parcel)))
                .toList();
    }

    @GetMapping("/{id}")
    public ParcelDetailResponse detail(@PathVariable Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No parcel with id " + id));
        return ParcelMapper.toDetail(parcel, riskScoringService.score(parcel));
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
                request.ecStatus(),
                request.litigationStatus(),
                request.layoutApproval(),
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
        requireNotNull(request.ecStatus(), "Encumbrance Certificate status");
        requireNotNull(request.litigationStatus(), "Litigation status");
        requireNotNull(request.layoutApproval(), "Layout approval status");
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
