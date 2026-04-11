package com.project.interviewai.service;

import com.project.interviewai.dto.*;
import com.project.interviewai.exception.BadRequestException;
import com.project.interviewai.exception.ResourceNotFoundException;
import com.project.interviewai.model.InterviewQA;
import com.project.interviewai.model.InterviewSession;
import com.project.interviewai.model.User;
import com.project.interviewai.repository.InterviewQARepository;
import com.project.interviewai.repository.InterviewSessionRepository;
import com.project.interviewai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewQARepository qaRepository;
    private final UserRepository userRepository;
    private final AiQuestionService aiQuestionService;
    private final AiEvaluationService aiEvaluationService;

    @Transactional
    public QuestionResponse startSession(String email, InterviewStartRequest request) {
        User user = findUserByEmail(email);

        InterviewSession session = InterviewSession.builder()
                .user(user)
                .roleTarget(request.getRoleTarget())
                .difficulty(request.getDifficulty())
                .totalQuestions(request.getNumberOfQuestions())
                .status("IN_PROGRESS")
                .startedAt(LocalDateTime.now())
                .build();
        session = sessionRepository.save(session);

        log.info("Started session {} for user {}", session.getId(), email);
        return generateNextQuestion(session, user);
    }

    @Transactional
    public QuestionResponse getNextQuestion(Long sessionId, String email) {
        User user = findUserByEmail(email);
        InterviewSession session = findSessionById(sessionId);
        validateSessionOwnership(session, user);

        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new BadRequestException("Session already completed");
        }

        List<InterviewQA> existingQAs = qaRepository.findBySessionIdOrderByAskedAtAsc(sessionId);
        long answeredCount = existingQAs.stream().filter(qa -> qa.getUserAnswer() != null).count();

        // return the existing unanswered question if one exists to prevent skipping
        boolean hasUnanswered = existingQAs.stream().anyMatch(qa -> qa.getUserAnswer() == null);
        if (hasUnanswered) {
            InterviewQA unanswered = existingQAs.stream()
                    .filter(qa -> qa.getUserAnswer() == null)
                    .findFirst()
                    .orElseThrow();

            return QuestionResponse.builder()
                    .questionId(unanswered.getId())
                    .questionText(unanswered.getQuestionText())
                    .questionType(unanswered.getQuestionType())
                    .questionNumber(existingQAs.size())
                    .totalQuestions(session.getTotalQuestions())
                    .build();
        }

        if (answeredCount >= session.getTotalQuestions()) {
            throw new BadRequestException("All questions answered. End the session.");
        }

        return generateNextQuestion(session, user);
    }

    @Transactional
    public FeedbackResponse submitAnswer(String email, AnswerSubmitRequest request) {
        User user = findUserByEmail(email);

        InterviewQA qa = qaRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        InterviewSession session = qa.getSession();
        validateSessionOwnership(session, user);

        if (qa.getUserAnswer() != null) {
            throw new BadRequestException("Question already answered");
        }

        log.info("Evaluating answer for question {}", qa.getId());
        FeedbackResponse feedback = aiEvaluationService.evaluateAnswer(
                qa.getQuestionText(),
                qa.getQuestionType(),
                request.getAnswer(),
                session.getRoleTarget()
        );

        qa.setUserAnswer(request.getAnswer());
        qa.setAiFeedback(feedback.getFeedback());
        qa.setIdealAnswer(feedback.getIdealAnswer());
        qa.setScoreRelevance(feedback.getScoreRelevance());
        qa.setScoreDepth(feedback.getScoreDepth());
        qa.setScoreClarity(feedback.getScoreClarity());
        qa.setAnsweredAt(LocalDateTime.now());
        qaRepository.save(qa);

        feedback.setQuestionId(qa.getId());
        feedback.setQuestionText(qa.getQuestionText());
        feedback.setUserAnswer(request.getAnswer());

        return feedback;
    }

    @Transactional
    public SessionSummaryResponse endSession(Long sessionId, String email) {
        User user = findUserByEmail(email);
        InterviewSession session = findSessionById(sessionId);
        validateSessionOwnership(session, user);

        List<InterviewQA> allQAs = qaRepository.findBySessionIdOrderByAskedAtAsc(sessionId);
        List<InterviewQA> answeredQAs = allQAs.stream()
                .filter(qa -> qa.getUserAnswer() != null)
                .toList();

        // calculate final average score
        BigDecimal avgScore = BigDecimal.ZERO;
        if (!answeredQAs.isEmpty()) {
            double totalScore = answeredQAs.stream()
                    .mapToDouble(qa -> {
                        int r = qa.getScoreRelevance() != null ? qa.getScoreRelevance() : 0;
                        int d = qa.getScoreDepth() != null ? qa.getScoreDepth() : 0;
                        int c = qa.getScoreClarity() != null ? qa.getScoreClarity() : 0;
                        return (r + d + c) / 3.0;
                    })
                    .sum();
            avgScore = BigDecimal.valueOf(totalScore / answeredQAs.size())
                    .setScale(2, RoundingMode.HALF_UP);
        }

        session.setStatus("COMPLETED");
        session.setCompletedAt(LocalDateTime.now());
        session.setAvgScore(avgScore);
        session.setTotalQuestions(answeredQAs.size());
        sessionRepository.save(session);

        List<SessionSummaryResponse.QuestionSummary> questionSummaries = allQAs.stream()
                .map(this::mapToQuestionSummary)
                .collect(Collectors.toList());

        // basic analysis of strengths/weaknesses
        List<String> strongAreas = answeredQAs.stream()
                .filter(qa -> getOverallScore(qa) >= 7.0)
                .map(qa -> qa.getQuestionType() + ": " + qa.getQuestionText().substring(0, Math.min(60, qa.getQuestionText().length())) + "...")
                .toList();

        List<String> weakAreas = answeredQAs.stream()
                .filter(qa -> getOverallScore(qa) < 5.0)
                .map(qa -> qa.getQuestionType() + ": " + qa.getQuestionText().substring(0, Math.min(60, qa.getQuestionText().length())) + "...")
                .toList();

        return SessionSummaryResponse.builder()
                .sessionId(session.getId())
                .roleTarget(session.getRoleTarget())
                .difficulty(session.getDifficulty())
                .totalQuestions(answeredQAs.size())
                .avgScore(avgScore)
                .status("COMPLETED")
                .startedAt(session.getStartedAt())
                .completedAt(session.getCompletedAt())
                .questions(questionSummaries)
                .overallFeedback("Answered " + answeredQAs.size() + " questions with avg score " + avgScore + "/10.")
                .strongAreas(strongAreas)
                .weakAreas(weakAreas)
                .build();
    }

    public List<SessionSummaryResponse> getSessionHistory(String email) {
        User user = findUserByEmail(email);
        List<InterviewSession> sessions = sessionRepository.findByUserIdOrderByStartedAtDesc(user.getId());

        return sessions.stream().map(session -> {
            List<InterviewQA> qas = qaRepository.findBySessionIdOrderByAskedAtAsc(session.getId());
            int answeredCount = (int) qas.stream().filter(qa -> qa.getUserAnswer() != null).count();

            return SessionSummaryResponse.builder()
                    .sessionId(session.getId())
                    .roleTarget(session.getRoleTarget())
                    .difficulty(session.getDifficulty())
                    .totalQuestions(answeredCount)
                    .avgScore(session.getAvgScore())
                    .status(session.getStatus())
                    .startedAt(session.getStartedAt())
                    .completedAt(session.getCompletedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    public SessionSummaryResponse getSessionDetail(Long sessionId, String email) {
        User user = findUserByEmail(email);
        InterviewSession session = findSessionById(sessionId);
        validateSessionOwnership(session, user);

        List<InterviewQA> qas = qaRepository.findBySessionIdOrderByAskedAtAsc(sessionId);
        List<SessionSummaryResponse.QuestionSummary> questionSummaries = qas.stream()
                .map(this::mapToQuestionSummary)
                .collect(Collectors.toList());

        return SessionSummaryResponse.builder()
                .sessionId(session.getId())
                .roleTarget(session.getRoleTarget())
                .difficulty(session.getDifficulty())
                .totalQuestions(session.getTotalQuestions())
                .avgScore(session.getAvgScore())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .completedAt(session.getCompletedAt())
                .questions(questionSummaries)
                .build();
    }

    private QuestionResponse generateNextQuestion(InterviewSession session, User user) {
        List<InterviewQA> existingQAs = qaRepository.findBySessionIdOrderByAskedAtAsc(session.getId());
        List<String> previousQuestions = existingQAs.stream()
                .map(InterviewQA::getQuestionText)
                .toList();

        int nextQuestionNumber = existingQAs.size() + 1;

        Map<String, String> generated = aiQuestionService.generateQuestion(
                user.getResumeText(),
                session.getRoleTarget(),
                session.getDifficulty(),
                nextQuestionNumber,
                session.getTotalQuestions(),
                previousQuestions
        );

        InterviewQA qa = InterviewQA.builder()
                .session(session)
                .questionText(generated.get("questionText"))
                .questionType(generated.get("questionType"))
                .askedAt(LocalDateTime.now())
                .build();
        qa = qaRepository.save(qa);

        return QuestionResponse.builder()
                .questionId(qa.getId())
                .questionText(qa.getQuestionText())
                .questionType(qa.getQuestionType())
                .questionNumber(nextQuestionNumber)
                .totalQuestions(session.getTotalQuestions())
                .build();
    }

    private SessionSummaryResponse.QuestionSummary mapToQuestionSummary(InterviewQA qa) {
        return SessionSummaryResponse.QuestionSummary.builder()
                .questionId(qa.getId())
                .questionType(qa.getQuestionType())
                .questionText(qa.getQuestionText())
                .userAnswer(qa.getUserAnswer())
                .aiFeedback(qa.getAiFeedback())
                .idealAnswer(qa.getIdealAnswer())
                .scoreRelevance(qa.getScoreRelevance())
                .scoreDepth(qa.getScoreDepth())
                .scoreClarity(qa.getScoreClarity())
                .overallScore(qa.getUserAnswer() != null ? getOverallScore(qa) : null)
                .build();
    }

    private double getOverallScore(InterviewQA qa) {
        int r = qa.getScoreRelevance() != null ? qa.getScoreRelevance() : 0;
        int d = qa.getScoreDepth() != null ? qa.getScoreDepth() : 0;
        int c = qa.getScoreClarity() != null ? qa.getScoreClarity() : 0;
        return Math.round((r + d + c) / 3.0 * 10.0) / 10.0;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private InterviewSession findSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    private void validateSessionOwnership(InterviewSession session, User user) {
        if (!session.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Unauthorized access to session");
        }
    }
}
