package com.project.interviewai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for starting a new interview session.
 * The user specifies what role they want to practice for
 * and at what difficulty level.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewStartRequest {

    @NotBlank(message = "Target role is required")
    private String roleTarget;

    @Builder.Default
    private String difficulty = "MEDIUM";  // EASY, MEDIUM, HARD

    @Builder.Default
    private Integer numberOfQuestions = 5;  // How many questions in the session
}
