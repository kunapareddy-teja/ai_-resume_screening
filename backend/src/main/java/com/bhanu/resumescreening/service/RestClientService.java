package com.bhanu.resumescreening.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class RestClientService {

    private static final String MODEL = "gemini-3.5-flash";

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestClient restClient = createRestClient();

    @Value("${gemini.api.key:}")
    private String apiKey;

    public String generateContent(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured.");
        }
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Gemini prompt cannot be empty.");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + MODEL + ":generateContent?key=" + apiKey;
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("temperature", 0.2, "responseMimeType", "application/json")
        );

        try {
            String response = restClient.post().uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve().body(String.class);
            if (response == null || response.isBlank()) {
                throw new IllegalStateException("Gemini returned an empty response.");
            }
            return response;
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(apiError(e), e);
        } catch (RestClientException e) {
            throw new IllegalStateException("Gemini API could not be reached. Please try again.", e);
        }
    }

    private RestClient createRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    private String apiError(RestClientResponseException exception) {
        try {
            JsonNode error = mapper.readTree(exception.getResponseBodyAsString());
            String message = error.path("error").path("message").asText("");
            if (!message.isBlank()) return "Gemini API error: " + message;
        } catch (Exception ignored) {
            // Use HTTP status when Gemini does not return JSON.
        }
        return "Gemini API request failed with HTTP " + exception.getStatusCode().value() + ".";
    }
}