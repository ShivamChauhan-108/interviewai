package com.project.interviewai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private Integer totalSessions;
    private Integer completedSessions;
    private Integer totalQuestionsAnswered;
    private BigDecimal overallAvgScore;

    // per-skill performance breakdown
    private List<SkillStat> skillStats;

    // recent session history (last 5)
    private List<RecentSession> recentSessions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillStat {
        private String skillName;
        private Integer questionsAttempted;
        private BigDecimal avgScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentSession {
        private Long sessionId;
        private String roleTarget;
        private String difficulty;
        private BigDecimal avgScore;
        private String status;
        private String startedAt;
    }
}
