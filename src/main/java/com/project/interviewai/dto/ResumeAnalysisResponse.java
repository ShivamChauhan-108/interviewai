package com.project.interviewai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysisResponse {

    private List<String> skills;
    private String experienceLevel;
    private String suggestedRole;
    private List<String> strengths;
    private List<String> gaps;
    private List<String> recommendations;
    private String overallSummary;
}
