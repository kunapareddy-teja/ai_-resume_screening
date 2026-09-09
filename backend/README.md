# Backend - AI Resume Screening Assistant

## Run
1. Install Java 17 and Maven.
2. Set your Gemini API key:
   - Windows PowerShell: `$env:GEMINI_API_KEY="YOUR_KEY"`
   - Windows CMD: `set GEMINI_API_KEY=YOUR_KEY`
3. From this folder:
   `mvn spring-boot:run`

Backend runs on http://localhost:8080

## API
POST `/api/screen/analyze`

Multipart fields:
- `resumeFile` (optional PDF/DOCX)
- `resumeText` (optional text)
- `jobDescription` (required)
