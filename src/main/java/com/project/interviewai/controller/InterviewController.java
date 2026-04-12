package com.project.interviewai.controller;

import com.project.interviewai.dto.*;
import com.project.interviewai.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Slf4j
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/start")
    public ResponseEntity<QuestionResponse> startInterview(
            @Valid @RequestBody InterviewStartRequest request,
            Authentication authentication
    ) {
        log.info("Starting interview for user: {}", authentication.getName());
        QuestionResponse response = interviewService.startSession(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sessionId}/question")
    public ResponseEntity<QuestionResponse> getNextQuestion(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        QuestionResponse response = interviewService.getNextQuestion(sessionId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/answer")
    public ResponseEntity<FeedbackResponse> submitAnswer(
            @Valid @RequestBody AnswerSubmitRequest request,
            Authentication authentication
    ) {
        log.info("Answer submitted for question: {}", request.getQuestionId());
        FeedbackResponse response = interviewService.submitAnswer(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<SessionSummaryResponse> endSession(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        log.info("Ending session: {}", sessionId);
        SessionSummaryResponse response = interviewService.endSession(sessionId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<SessionSummaryResponse>> getHistory(Authentication authentication) {
        List<SessionSummaryResponse> history = interviewService.getSessionHistory(authentication.getName());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<SessionSummaryResponse> getSessionDetail(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        SessionSummaryResponse detail = interviewService.getSessionDetail(sessionId, authentication.getName());
        return ResponseEntity.ok(detail);
    }
}
