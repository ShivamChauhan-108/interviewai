package com.project.interviewai.service;

import com.project.interviewai.dto.DashboardStatsResponse;
import com.project.interviewai.exception.ResourceNotFoundException;
import com.project.interviewai.model.InterviewQA;
import com.project.interviewai.model.InterviewSession;
import com.project.interviewai.model.User;
import com.project.interviewai.repository.InterviewQARepository;
import com.project.interviewai.repository.InterviewSessionRepository;
import com.project.interviewai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewQARepository qaRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public DashboardStatsResponse getDashboardStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<InterviewSession> allSessions = sessionRepository.findByUserIdOrderByStartedAtDesc(user.getId());

        int totalSessions = allSessions.size();
        int completedSessions = (int) allSessions.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .count();

        // grab all QA records for this user's sessions
        List<InterviewQA> allQAs = new ArrayList<>();
        for (InterviewSession session : allSessions) {
            allQAs.addAll(qaRepository.findBySessionIdOrderByAskedAtAsc(session.getId()));
        }

        List<InterviewQA> answeredQAs = allQAs.stream()
                .filter(qa -> qa.getUserAnswer() != null)
                .toList();

        int totalAnswered = answeredQAs.size();

        // overall average
        BigDecimal overallAvg = BigDecimal.ZERO;
        if (!answeredQAs.isEmpty()) {
            double sum = answeredQAs.stream()
                    .mapToDouble(this::calcAvgScore)
                    .sum();
            overallAvg = BigDecimal.valueOf(sum / answeredQAs.size())
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // group scores by question type to get skill-level stats
        Map<String, List<InterviewQA>> byType = answeredQAs.stream()
                .collect(Collectors.groupingBy(InterviewQA::getQuestionType));

        List<DashboardStatsResponse.SkillStat> skillStats = byType.entrySet().stream()
                .map(entry -> {
                    double avg = entry.getValue().stream()
                            .mapToDouble(this::calcAvgScore)
                            .average()
                            .orElse(0.0);
                    return DashboardStatsResponse.SkillStat.builder()
                            .skillName(entry.getKey())
                            .questionsAttempted(entry.getValue().size())
                            .avgScore(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP))
                            .build();
                })
                .sorted(Comparator.comparing(DashboardStatsResponse.SkillStat::getAvgScore).reversed())
                .toList();

        // last 5 sessions for quick view
        List<DashboardStatsResponse.RecentSession> recentSessions = allSessions.stream()
                .limit(5)
                .map(s -> DashboardStatsResponse.RecentSession.builder()
                        .sessionId(s.getId())
                        .roleTarget(s.getRoleTarget())
                        .difficulty(s.getDifficulty())
                        .avgScore(s.getAvgScore())
                        .status(s.getStatus())
                        .startedAt(s.getStartedAt() != null ? s.getStartedAt().format(DATE_FMT) : null)
                        .build())
                .toList();

        return DashboardStatsResponse.builder()
                .totalSessions(totalSessions)
                .completedSessions(completedSessions)
                .totalQuestionsAnswered(totalAnswered)
                .overallAvgScore(overallAvg)
                .skillStats(skillStats)
                .recentSessions(recentSessions)
                .build();
    }

    private double calcAvgScore(InterviewQA qa) {
        int r = qa.getScoreRelevance() != null ? qa.getScoreRelevance() : 0;
        int d = qa.getScoreDepth() != null ? qa.getScoreDepth() : 0;
        int c = qa.getScoreClarity() != null ? qa.getScoreClarity() : 0;
        return (r + d + c) / 3.0;
    }
}
