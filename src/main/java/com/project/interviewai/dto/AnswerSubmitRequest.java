package com.project.interviewai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting an answer to an interview question.
 * Contains the question ID and the user's typed answer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSubmitRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotBlank(message = "Answer cannot be empty")
    private String answer;
}
