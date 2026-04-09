package com.project.interviewai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.interviewai.dto.ResumeAnalysisResponse;
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
public class GeminiAiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Constructor injection for RestTemplate and ObjectMapper.
     * This makes the service easier to test (we can mock RestTemplate)
     * and follows Spring best practices over inline instantiation.
     */
    public GeminiAiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Orchestrates the full resume analysis flow:
     * 1. Builds a structured prompt from the resume text
     * 2. Sends it to Google Gemini API
     * 3. Parses the AI response into a structured DTO
     *
     * @param resumeText the extracted text from the user's PDF resume
     * @return structured analysis with skills, strengths, gaps, etc.
     */
    public ResumeAnalysisResponse analyzeResume(String resumeText) {
        log.info("Starting AI resume analysis, text length: {} chars", resumeText.length());
        String prompt = buildResumeAnalysisPrompt(resumeText);
        String aiResponse = callGemini(prompt);
        return parseAnalysisResponse(aiResponse);
    }

    /**
     * Constructs the prompt that tells Gemini exactly what format to respond in.
     * Uses a text block with the resume content embedded inside triple quotes.
     * The prompt instructs the AI to return ONLY valid JSON — no markdown fences.
     */
    private String buildResumeAnalysisPrompt(String resumeText) {
        return """
                You are an expert technical recruiter. 
                Analyze this resume text and provide a STRUCTURED analysis.
                
                RESUME TEXT:
                \"\"\"
                %s
                \"\"\"
                
                Respond ONLY with a valid JSON (no markdown):
                {
                    "skills": ["java", "spring boot", ...],
                    "experienceLevel": "Fresher/Junior/Mid/Senior",
                    "suggestedRole": "Job title",
                    "strengths": ["..."],
                    "gaps": ["..."],
                    "recommendations": ["..."],
                    "overallSummary": "..."
                }
                """.formatted(resumeText);
    }

    /**
     * Makes the actual HTTP POST call to Google's Gemini API.
     * Constructs the request body in Gemini's expected format:
     *   { "contents": [{ "parts": [{ "text": "prompt" }] }] }
     * Extracts the text response from Gemini's nested JSON response structure.
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

            // Navigate Gemini's response JSON:
            // root -> candidates[0] -> content -> parts[0] -> text
            JsonNode root = objectMapper.readTree(response.getBody());
            String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            log.info("Gemini API responded successfully");
            return text;

        } catch (Exception e) {
            log.error("AI API call failed", e);
            throw new BadRequestException("AI analysis failed: " + e.getMessage());
        }
    }

    /**
     * Parses the AI's JSON string response into a ResumeAnalysisResponse DTO.
     * Handles cases where Gemini might wrap the JSON in markdown code fences
     * by stripping ```json and ``` markers before parsing.
     * Falls back to a summary-only response if JSON parsing fails.
     */
    private ResumeAnalysisResponse parseAnalysisResponse(String aiResponse) {
        try {
            String cleanJson = aiResponse.replace("```json", "").replace("```", "").trim();
            return objectMapper.readValue(cleanJson, ResumeAnalysisResponse.class);
        } catch (Exception e) {
            log.warn("JSON parsing failed, returning summary wrapper", e);
            return ResumeAnalysisResponse.builder().overallSummary(aiResponse).build();
        }
    }
}
