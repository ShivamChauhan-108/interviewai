package com.project.interviewai.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_skills")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String skillName;

    @Builder.Default
    private Integer proficiency = 0;

    @Builder.Default
    private Integer questionsAsked = 0;

    @Builder.Default
    private BigDecimal avgScore = BigDecimal.ZERO;

    private LocalDateTime lastPracticed;
}
