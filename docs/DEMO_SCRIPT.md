# 5-minute demo script

## 1. Introduction
"This is an AI Resume Screening Assistant. It compares a candidate resume with a job description and generates structured recruitment insights."

## 2. Input
Show the resume upload area and JD textarea.

Say:
"The candidate can either upload a PDF/DOCX resume or paste resume text manually."

## 3. Backend
Mention:
"The React frontend sends the resume and JD to a Spring Boot REST endpoint. The backend extracts PDF/DOCX text and sends a controlled prompt to the AI model."

## 4. Results
Show:
- Match score
- Strengths
- Missing skills
- Hiring recommendation
- Role recommendations
- Training recommendations
- 5 interview questions

## 5. Bonus
Click Download PDF.

Say:
"The report can also be downloaded as a PDF, which is an optional bonus feature from the assessment."

## 6. Error handling
Try Analyze with an empty JD or without a resume and show the validation message.

## 7. Closing
"The architecture keeps frontend, controller, service, parser, and AI responsibilities separated, so another AI provider can be integrated without redesigning the UI."
