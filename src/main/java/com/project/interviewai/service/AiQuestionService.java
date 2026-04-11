package com.project.interviewai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class AiQuestionService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiQuestionService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, String> generateQuestion(
            String resumeText,
            String roleTarget,
            String difficulty,
            int questionNumber,
            int totalQuestions,
            List<String> previousQuestions
    ) {
        String prompt = buildQuestionPrompt(resumeText, roleTarget, difficulty,
                questionNumber, totalQuestions, previousQuestions);
        String aiResponse = callGemini(prompt);
        return parseQuestionResponse(aiResponse);
    }

    private String buildQuestionPrompt(
            String resumeText,
            String roleTarget,
            String difficulty,
            int questionNumber,
            int totalQuestions,
            List<String> previousQuestions
    ) {
        // use resume context if available, otherwise fallback to generic
        String resumeContext = (resumeText != null && !resumeText.isBlank())
                ? "CANDIDATE'S RESUME:\n\"\"\"\n" + resumeText + "\n\"\"\"\n\n"
                : "No resume provided. Generate general questions.\n\n";

        // avoid repeating questions in the same session
        String previousContext = previousQuestions.isEmpty()
                ? "No previous questions asked yet."
                : "PREVIOUSLY ASKED QUESTIONS (do NOT repeat these):\n" +
                  String.join("\n", previousQuestions.stream()
                          .map(q -> "- " + q)
                          .toList());

        return """
                You are an expert technical interviewer conducting a mock interview.
                
                %s
                TARGET ROLE: %s
                DIFFICULTY: %s
                QUESTION NUMBER: %d of %d
                
                %s
                
                Generate the next interview question. Mix question types across the session:
                - TECHNICAL: Language/framework specific (Java, Spring Boot, SQL, etc.)
                - BEHAVIORAL: Teamwork, conflict resolution, leadership scenarios
                - SYSTEM_DESIGN: Architecture, scalability, trade-offs
                - CODING: Algorithm concepts, data structures, problem-solving approach
                
                For question %d of %d:
                - If early in the session (1-2): Start with TECHNICAL or BEHAVIORAL
                - If mid session (3-6): Mix TECHNICAL and SYSTEM_DESIGN
                - If late session (7+): Focus on CODING or deeper TECHNICAL
                
                Respond ONLY with valid JSON (no markdown):
                {
                    "questionText": "Your detailed interview question here",
                    "questionType": "TECHNICAL"
                }
                """.formatted(
                resumeContext, roleTarget, difficulty,
                questionNumber, totalQuestions,
                previousContext,
                questionNumber, totalQuestions
        );
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

            // extract the text part from gemini's nested JSON response
            JsonNode root = objectMapper.readTree(response.getBody());
            String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            log.info("Question generated successfully");
            return text;

        } catch (Exception e) {
            log.error("Failed to generate question from AI", e);
            throw new BadRequestException("AI generation failed: " + e.getMessage());
        }
    }

    private Map<String, String> parseQuestionResponse(String aiResponse) {
        try {
            // strip out markdown formatting if gemini includes it
            String cleanJson = aiResponse.replace("```json", "").replace("```", "").trim();
            JsonNode node = objectMapper.readTree(cleanJson);
            return Map.of(
                    "questionText", node.path("questionText").asText(),
                    "questionType", node.path("questionType").asText("TECHNICAL")
            );
        } catch (Exception e) {
            log.warn("JSON parse failed, falling back to raw response");
            return Map.of(
                    "questionText", aiResponse.trim(),
                    "questionType", "TECHNICAL"
            );
        }
    }
}
