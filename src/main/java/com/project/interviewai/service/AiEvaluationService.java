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
                2. DEPTH (0-10): How detailed and thorough is the answer?
                3. CLARITY (0-10): How well-structured and clear is the communication?
                
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

            log.info("Answer evaluation completed");
            return text;

        } catch (Exception e) {
            log.error("Failed to evaluate answer via AI", e);
            throw new BadRequestException("AI evaluation failed: " + e.getMessage());
        }
    }

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
            log.warn("Evaluation JSON parse failed, using defaults");
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
