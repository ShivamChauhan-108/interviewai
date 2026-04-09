package com.project.interviewai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for the session summary shown at the end of an interview.
 * Contains overall scores, per-question feedback, and improvement areas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummaryResponse {

    private Long sessionId;
    private String roleTarget;
    private String difficulty;
    private Integer totalQuestions;
    private BigDecimal avgScore;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Per-question details
    private List<QuestionSummary> questions;

    // AI-generated overall session feedback
    private String overallFeedback;
    private List<String> strongAreas;
    private List<String> weakAreas;

    /**
     * Inner DTO for individual question summary within a session.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionSummary {
        private Long questionId;
        private String questionType;
        private String questionText;
        private String userAnswer;
        private String aiFeedback;
        private String idealAnswer;
        private Integer scoreRelevance;
        private Integer scoreDepth;
        private Integer scoreClarity;
        private Double overallScore;
    }
}
