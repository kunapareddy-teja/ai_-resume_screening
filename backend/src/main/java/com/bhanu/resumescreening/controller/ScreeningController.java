package com.bhanu.resumescreening.controller;

import com.bhanu.resumescreening.model.ScreeningResponse;
import com.bhanu.resumescreening.service.GeminiService;
import com.bhanu.resumescreening.service.ResumeParserService;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/screen")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ScreeningController {

    private final ResumeParserService parserService;
    private final GeminiService geminiService;

    public ScreeningController(ResumeParserService parserService, GeminiService geminiService) {
        this.parserService = parserService;
        this.geminiService = geminiService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyze(
            @RequestPart(value = "resumeFile", required = false) MultipartFile resumeFile,
            @RequestPart(value = "resumeText", required = false) String resumeText,
            @RequestPart("jobDescription") String jobDescription) {

        try {
            if (jobDescription == null || jobDescription.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Job description is required."));
            }

            String resume;
            if (resumeFile != null && !resumeFile.isEmpty()) {
                resume = parserService.extractText(resumeFile);
            } else {
                resume = resumeText == null ? "" : resumeText.trim();
            }

            if (resume.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Upload a PDF/DOCX resume or paste resume text."));
            }

            ScreeningResponse result = geminiService.analyze(resume, jobDescription);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    Map.of("error", e.getMessage() == null ? "AI service is temporarily unavailable." : e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", e.getMessage() == null ? "Unexpected server error." : e.getMessage()));
        }
    }
}
