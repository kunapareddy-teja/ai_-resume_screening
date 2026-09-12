package com.bhanu.resumescreening.service;

import com.bhanu.resumescreening.model.ScreeningResponse;
import org.springframework.stereotype.Service;

@Service
public class ResumeAnalysisService {

    private final RestClientService restClientService;
    private final GeminiResponseService responseService;

    public ResumeAnalysisService(RestClientService restClientService, GeminiResponseService responseService) {
        this.restClientService = restClientService;
        this.responseService = responseService;
    }

    public ScreeningResponse analyze(String resume, String jobDescription) {
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

        return responseService.toScreeningResponse(restClientService.generateContent(prompt));
    }
}