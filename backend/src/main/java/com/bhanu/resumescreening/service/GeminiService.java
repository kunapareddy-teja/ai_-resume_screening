package com.bhanu.resumescreening.service;

import com.bhanu.resumescreening.model.ScreeningResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final String MODEL = "gemini-3.5-flash";
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestClient restClient = createRestClient();

    @Value("${gemini.api.key:}")
    private String apiKey;

    private RestClient createRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    public ScreeningResponse analyze(String resume, String jobDescription) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured.");
        }

        String prompt = """
                You are an expert technical recruiter and ATS analyst.
                Compare the candidate resume against the job description.
                Return only valid JSON with exactly these fields:
                {
                  "matchScore": 0,
                  "candidateStrengths": ["..."],
                  "missingSkills": ["..."],
                  "interviewQuestions": ["...", "...", "...", "...", "..."],
                  "hiringRecommendation": "...",
                  "roleRecommendations": ["..."],
                  "trainingRecommendations": ["..."]
                }
                Rules: matchScore is 0-100; provide exactly 5 interview questions; use only supplied evidence.

                RESUME:
                %s

                JOB DESCRIPTION:
                %s
                """.formatted(resume, jobDescription);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
            + MODEL + ":generateContent?key=" + apiKey;
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("temperature", 0.2, "responseMimeType", "application/json")
        );

        String raw;
        try {
            raw = restClient.post().uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(apiError(e), e);
        }

        try {
            JsonNode root = mapper.readTree(raw);
            JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
            String text = parts.path(0).path("text").asText("")
                    .replaceFirst("^```json\\s*", "")
                    .replaceFirst("\\s*```$", "").trim();
            if (text.isBlank()) throw new IllegalStateException("Gemini returned an empty analysis.");
            JsonNode result = mapper.readTree(text);
            return new ScreeningResponse(
                    Math.max(0, Math.min(100, result.path("matchScore").asInt())),
                    readList(result, "candidateStrengths"),
                    readList(result, "missingSkills"),
                    normalizeQuestions(readList(result, "interviewQuestions")),
                    result.path("hiringRecommendation").asText("Consider"),
                    readList(result, "roleRecommendations"),
                    readList(result, "trainingRecommendations"));
        } catch (Exception e) {
            throw new IllegalStateException("Gemini response could not be parsed. Please try again.", e);
        }
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

    private String apiError(RestClientResponseException exception) {
        try {
            String message = mapper.readTree(exception.getResponseBodyAsString())
                    .path("error").path("message").asText("");
            if (!message.isBlank()) return "Gemini API error: " + message;
        } catch (Exception ignored) {
            // Use HTTP status when Gemini does not return JSON.
        }
        return "Gemini API request failed with HTTP " + exception.getStatusCode().value() + ".";
    }
}