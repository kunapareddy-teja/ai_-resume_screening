package com.bhanu.resumescreening.service;

import com.bhanu.resumescreening.model.ScreeningResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiResponseService {

    private final ObjectMapper mapper = new ObjectMapper();

    public ScreeningResponse toScreeningResponse(String rawResponse) {
        try {
            String responseText = extractResponseText(rawResponse);
            return mapResponse(mapper.readTree(responseText));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Gemini response could not be parsed. Please try again.", e);
        }
    }

    private String extractResponseText(String rawResponse) throws JsonProcessingException {
        JsonNode response = mapper.readTree(rawResponse);
        String text = response.path("candidates").path(0).path("content").path("parts")
                .path(0).path("text").asText("")
                .replaceFirst("^```json\\s*", "")
                .replaceFirst("\\s*```$", "").trim();
        if (text.isBlank()) {
            throw new IllegalStateException("Gemini returned an empty analysis.");
        }
        return text;
    }

    private ScreeningResponse mapResponse(JsonNode result) {
        return new ScreeningResponse(
                Math.max(0, Math.min(100, result.path("matchScore").asInt())),
                readList(result, "candidateStrengths"),
                readList(result, "missingSkills"),
                normalizeQuestions(readList(result, "interviewQuestions")),
                result.path("hiringRecommendation").asText("Consider"),
                readList(result, "roleRecommendations"),
                readList(result, "trainingRecommendations"));
    }

    private List<String> readList(JsonNode result, String field) {
        JsonNode value = result.path(field);
        return value.isArray()
                ? mapper.convertValue(value, mapper.getTypeFactory().constructCollectionType(List.class, String.class))
                : List.of();
    }

    private List<String> normalizeQuestions(List<String> questions) {
        List<String> normalized = new ArrayList<>(questions);
        normalized.addAll(List.of(
                "Which project best demonstrates the skills required for this role?",
                "How would you approach a difficult problem in this role?",
                "What trade-off did you make in a recent technical decision?",
                "How do you validate the quality of your work?",
                "What would you want to accomplish in your first 90 days?"));
        return normalized.subList(0, 5);
    }
}