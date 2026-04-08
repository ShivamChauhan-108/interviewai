package com.project.interviewai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    /**
     * GET /api/health
     * Simple health check endpoint (no auth required)
     */
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "AI Interview Prep Platform",
                "timestamp", LocalDateTime.now().toString(),
                "version", "1.0.0"
        ));
    }
}
