package com.project.interviewai.service;

import com.project.interviewai.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class ResumeParserService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Saves the uploaded PDF file to the configured upload directory.
     * Generates a unique filename using UUID to avoid collisions.
     *
     * @param file the uploaded MultipartFile from the HTTP request
     * @return the absolute file path where the resume was saved
     */
    public String saveFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;

            Path filePath = uploadPath.resolve(uniqueFilename);
            file.transferTo(filePath.toFile());

            log.info("Resume saved to: {}", filePath);
            return filePath.toString();

        } catch (IOException e) {
            log.error("Failed to save resume file", e);
            throw new BadRequestException("Failed to save resume file: " + e.getMessage());
        }
    }

    /*
     * Extracts human-readable text content from a PDF file using Apache PDFBox.
     * Uses try-with-resources to ensure the PDDocument is properly closed
     * even if an exception occurs during text extraction.
     *
     * @param filePath the absolute path to the saved PDF file
     * @return the extracted text content from the PDF
     */
    public String extractTextFromPdf(String filePath) {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Extracted {} characters from PDF", text.length());
            return text.trim();

        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", filePath, e);
            throw new BadRequestException("Failed to parse PDF file: " + e.getMessage());
        }
    }
}
