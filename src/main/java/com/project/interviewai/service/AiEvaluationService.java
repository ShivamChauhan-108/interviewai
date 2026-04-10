package com.project.interviewai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.interviewai.dto.FeedbackResponse;
import com.project.interviewai.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service responsible for evaluating user answers using Google Gemini AI.
 * Takes the question, user's answer, and context to produce scored feedback
 * with improvement suggestions.
 */
@Service
@Slf4j
public class AiEvaluationService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiEvaluationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Evaluates a user's answer to an interview question.
     * Returns detailed scoring, feedback, and an ideal answer for comparison.
     *
     * @param questionText the interview question that was asked
     * @param questionType the type of question (TECHNICAL, BEHAVIORAL, etc.)
     * @param userAnswer   the answer the user provided
     * @param roleTarget   the job role context
     * @return FeedbackResponse with scores and written feedback
     */
    public FeedbackResponse evaluateAnswer(
            String questionText,
            String questionType,
            String userAnswer,
            String roleTarget
    ) {
        String prompt = buildEvaluationPrompt(questionText, questionType, userAnswer, roleTarget);
        String aiResponse = callGemini(prompt);
        return parseEvaluationResponse(aiResponse);
    }

    /**
     * Constructs a detailed evaluation prompt that instructs Gemini to:
     * 1. Score the answer on three axes (relevance, depth, clarity) from 0-10
     * 2. Provide specific written feedback
     * 3. Generate an ideal answer for comparison
     * 4. Suggest improvement tips
     */
    private String buildEvaluationPrompt(
            String questionText,
            String questionType,
            String userAnswer,
            String roleTarget
    ) {
        return """
                You are an expert technical interviewer evaluating a candidate's answer.
                
                ROLE BEING INTERVIEWED FOR: %s
                QUESTION TYPE: %s
                
                QUESTION ASKED:
                \"\"\"%s\"\"\"
                
                CANDIDATE'S ANSWER:
                \"\"\"%s\"\"\"
                
                Evaluate the answer on these criteria (score 0-10 for each):
                
                1. RELEVANCE (0-10): Does the answer directly address the question?
                   - 0: Completely off-topic
                   - 5: Partially relevant but misses key points
                   - 10: Perfectly addresses every aspect of the question
                
                2. DEPTH (0-10): How detailed and thorough is the answer?
                   - 0: No substance at all
                   - 5: Surface-level understanding
                   - 10: Deep expertise with examples, trade-offs, and edge cases
                
                3. CLARITY (0-10): How well-structured and clear is the communication?
                   - 0: Incoherent or confusing
                   - 5: Understandable but poorly organized
                   - 10: Crystal clear, well-structured, professional communication
                
                Respond ONLY with valid JSON (no markdown):
                {
                    "scoreRelevance": 8,
                    "scoreDepth": 7,
                    "scoreClarity": 9,
                    "feedback": "Detailed feedback about what was good and what was missing...",
                    "idealAnswer": "A comprehensive ideal answer to this question...",
                    "improvementTips": "Specific tips on how to improve this answer..."
                }
                """.formatted(roleTarget, questionType, questionText, userAnswer);
    }

    /**
     * Makes the HTTP POST call to the Gemini API.
     */
    private String callGemini(String prompt) {
        try {
            String url = apiUrl + "?key=" + apiKey;
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            log.info("Answer evaluation completed successfully");
            return text;

        } catch (Exception e) {
            log.error("Failed to evaluate answer via AI", e);
            throw new BadRequestException("AI evaluation failed: " + e.getMessage());
        }
    }

    /**
     * Parses the Gemini JSON response into a FeedbackResponse DTO.
     * Handles markdown fences and falls back gracefully on parse failure.
     */
    private FeedbackResponse parseEvaluationResponse(String aiResponse) {
        try {
            String cleanJson = aiResponse.replace("```json", "").replace("```", "").trim();
            JsonNode node = objectMapper.readTree(cleanJson);

            int relevance = node.path("scoreRelevance").asInt(5);
            int depth = node.path("scoreDepth").asInt(5);
            int clarity = node.path("scoreClarity").asInt(5);
            double overall = (relevance + depth + clarity) / 3.0;

            return FeedbackResponse.builder()
                    .scoreRelevance(relevance)
                    .scoreDepth(depth)
                    .scoreClarity(clarity)
                    .overallScore(Math.round(overall * 10.0) / 10.0)
                    .feedback(node.path("feedback").asText("No feedback available"))
                    .idealAnswer(node.path("idealAnswer").asText("No ideal answer provided"))
                    .improvementTips(node.path("improvementTips").asText("Keep practicing!"))
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse evaluation JSON, returning default scores", e);
            return FeedbackResponse.builder()
                    .scoreRelevance(5)
                    .scoreDepth(5)
                    .scoreClarity(5)
                    .overallScore(5.0)
                    .feedback(aiResponse)
                    .idealAnswer("Could not generate ideal answer")
                    .improvementTips("Try to provide more structured and detailed answers")
                    .build();
        }
    }
}
