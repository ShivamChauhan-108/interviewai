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
public class UserProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String resumePath;
    private Boolean hasResume;
    private List<String> extractedSkills;
    private Integer totalSessions;
    private Integer completedSessions;
}
