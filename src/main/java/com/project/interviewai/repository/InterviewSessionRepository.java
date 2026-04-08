package com.project.interviewai.repository;

import com.project.interviewai.model.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    List<InterviewSession> findByUserIdOrderByStartedAtDesc(Long userId);

    long countByUserIdAndStatus(Long userId, String status);
}
