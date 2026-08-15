package com.titlerisk.controller;

import com.titlerisk.dto.HistoryEntryResponse;
import com.titlerisk.dto.HistoryMapper;
import com.titlerisk.repository.ViewHistoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * One endpoint: the signed-in user's own score-check history, most recent
 * first. There's no way to fetch another user's history through this API —
 * it always scopes to whoever the current session belongs to.
 */
@RestController
@RequestMapping("/api/history")
public class HistoryApiController {

    private final ViewHistoryRepository viewHistoryRepository;

    public HistoryApiController(ViewHistoryRepository viewHistoryRepository) {
        this.viewHistoryRepository = viewHistoryRepository;
    }

    @GetMapping
    public List<HistoryEntryResponse> myHistory(Authentication authentication) {
        return viewHistoryRepository.findByUsernameOrderByViewedAtDesc(authentication.getName()).stream()
                .map(HistoryMapper::toResponse)
                .toList();
    }
}
