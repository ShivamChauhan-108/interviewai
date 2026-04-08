package com.project.interviewai.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String roleTarget;

    @Builder.Default
    private String difficulty = "MEDIUM";

    @Builder.Default
    private Integer totalQuestions = 0;

    @Builder.Default
    private BigDecimal avgScore = BigDecimal.ZERO;

    @Builder.Default
    private String status = "IN_PROGRESS";

    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;
}
