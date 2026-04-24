package com.project.interviewai.controller;

import com.project.interviewai.dto.DashboardStatsResponse;
import com.project.interviewai.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats(Authentication authentication) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats(authentication.getName());
        return ResponseEntity.ok(stats);
    }
}
