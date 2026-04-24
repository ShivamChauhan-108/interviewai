package com.project.interviewai.service;

import com.project.interviewai.dto.UserProfileResponse;
import com.project.interviewai.exception.ResourceNotFoundException;
import com.project.interviewai.model.InterviewSession;
import com.project.interviewai.model.User;
import com.project.interviewai.model.UserSkill;
import com.project.interviewai.repository.InterviewSessionRepository;
import com.project.interviewai.repository.UserRepository;
import com.project.interviewai.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final InterviewSessionRepository sessionRepository;

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<String> skills = userSkillRepository.findByUserId(user.getId()).stream()
                .map(UserSkill::getSkillName)
                .toList();

        List<InterviewSession> sessions = sessionRepository.findByUserIdOrderByStartedAtDesc(user.getId());
        int completed = (int) sessions.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .count();

        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .resumePath(user.getResumePath())
                .hasResume(user.getResumePath() != null)
                .extractedSkills(skills)
                .totalSessions(sessions.size())
                .completedSessions(completed)
                .build();
    }
}
