package com.titlerisk.controller;

import com.titlerisk.dto.ParcelDetailResponse;
import com.titlerisk.dto.ParcelMapper;
import com.titlerisk.dto.ParcelSummaryResponse;
import com.titlerisk.model.Parcel;
import com.titlerisk.repository.ParcelRepository;
import com.titlerisk.service.RiskScoringService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * JSON API backing the frontend. Two endpoints: the parcel list with a
 * quick score per row, and a single parcel's full factor-by-factor
 * breakdown. The static frontend under {@code src/main/resources/static}
 * is the only consumer of this today, but nothing here is frontend-specific
 * — this could just as easily sit behind a mobile app or a curl script.
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
}
