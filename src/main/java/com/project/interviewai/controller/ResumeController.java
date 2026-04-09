package com.project.interviewai.controller;

import com.project.interviewai.dto.ResumeAnalysisResponse;
import com.project.interviewai.exception.BadRequestException;
import com.project.interviewai.model.User;
import com.project.interviewai.repository.UserRepository;
import com.project.interviewai.service.GeminiAiService;
import com.project.interviewai.service.ResumeParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeParserService resumeParserService;
    private final GeminiAiService geminiAiService;
    private final UserRepository userRepository;

    /**
     * POST /api/resume/upload
     *
     * Accepts a PDF file upload, extracts text from it,
     * sends the text to Gemini AI for analysis, saves the
     * resume path and extracted text to the user's profile,
     * and returns the structured AI analysis.
     *
     * Requires JWT authentication (user must be logged in).
     *
     * @param file           the uploaded PDF resume file
     * @param authentication Spring Security authentication object (injected from JWT)
     * @return AI-generated resume analysis with skills, strengths, gaps
     */
    @PostMapping("/upload")
    public ResponseEntity<ResumeAnalysisResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        // Validate: file must not be empty and must be a PDF
        String contentType = file.getContentType();
        if (file.isEmpty() || contentType == null || !contentType.equals("application/pdf")) {
            throw new BadRequestException("Please upload a valid PDF file");
        }

        // Step 1: Save the PDF to local storage
        String filePath = resumeParserService.saveFile(file);

        // Step 2: Extract text from the saved PDF
        String resumeText = resumeParserService.extractTextFromPdf(filePath);

        // Step 3: Update the user's profile with resume info
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setResumePath(filePath);
        user.setResumeText(resumeText);
        userRepository.save(user);

        // Step 4: Send to Gemini AI for analysis and return result
        log.info("Analyzing resume for user: {}", user.getEmail());
        return ResponseEntity.ok(geminiAiService.analyzeResume(resumeText));
    }
}
