package com.bhanu.resumescreening.model;

import java.util.List;

public record ScreeningResponse(
        int matchScore,
        List<String> candidateStrengths,
        List<String> missingSkills,
        List<String> interviewQuestions,
        String hiringRecommendation,
        List<String> roleRecommendations,
        List<String> trainingRecommendations
) {}
