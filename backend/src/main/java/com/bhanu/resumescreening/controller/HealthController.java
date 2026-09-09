package com.bhanu.resumescreening.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private static final String GEMINI_MODEL = "gemini-3.5-flash";

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @GetMapping("/")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "AI Resume Screening Assistant",
                "geminiConfigured", String.valueOf(geminiApiKey != null && !geminiApiKey.isBlank()),
                "geminiModel", GEMINI_MODEL,
                "message", "Backend is running. Use the frontend at http://127.0.0.1:5173/.");
    }
}
