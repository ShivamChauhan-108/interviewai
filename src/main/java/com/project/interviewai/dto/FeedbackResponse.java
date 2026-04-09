package com.project.interviewai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing AI-generated feedback for a user's answer.
 * Includes granular scoring across three dimensions plus written feedback.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private Long questionId;
    private String questionText;
    private String userAnswer;

    // Granular scores (0-10)
    private Integer scoreRelevance;
    private Integer scoreDepth;
    private Integer scoreClarity;
    private Double overallScore;

    // AI-generated written feedback
    private String feedback;
    private String idealAnswer;
    private String improvementTips;
}
