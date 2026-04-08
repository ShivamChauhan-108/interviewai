package com.project.interviewai.repository;

import com.project.interviewai.model.InterviewQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQARepository extends JpaRepository<InterviewQA, Long> {

    List<InterviewQA> findBySessionIdOrderByAskedAtAsc(Long sessionId);
}
