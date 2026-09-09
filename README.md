# AI Resume Screening Assistant

## Assessment alignment
This project implements the assessment requirements:
- PDF/DOCX resume upload
- Manual resume text input
- Job description textarea
# AI Resume Screening Assistant

An AI-powered web application that compares a candidate resume with a job description and produces a structured screening report.

## Source Code

The complete source code is organized in this repository:

- `frontend/` - React and Vite user interface
- `backend/` - Java Spring Boot REST API
- `docs/` - Demo and presentation material

## Features

- Upload a PDF or DOCX resume.
- Paste resume text manually.
- Enter a job description.
- Generate a resume match score from 0 to 100.
- Identify candidate strengths and missing skills.
- Generate exactly five interview questions.
- Provide an overall hiring recommendation.
- Recommend suitable roles and training.
- Display results in a structured report.
- Download the report as a PDF.
- Show clear validation and AI-service error messages.

## Technologies Used

### Frontend

- React 19
- Vite
- Lucide React icons
- `html2canvas` and `jsPDF` for PDF report export

### Backend

- Java 17
- Spring Boot 3.5.16
- Spring Web REST API
- Apache PDFBox for PDF text extraction
- Apache POI for DOCX text extraction
- Jackson for JSON processing
- Maven 4.x or Maven 3.9+

### AI Model/API

- Google Gemini API
- Model: `gemini-3.5-flash`
- API operation: Gemini `generateContent`
- Authentication: `GEMINI_API_KEY` environment variable

## Project Structure

```text
ai-resume-screening/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/
│       └── resources/application.properties
├── frontend/
│   ├── package.json
│   └── src/
├── docs/
└── README.md
```

## Requirements

- Java 17 or newer
- Maven 3.9+ or Maven 4.x
- Node.js 18 or newer
- npm
- A valid Google Gemini API key with access to `gemini-3.5-flash`

## Setup and Running

### 1. Configure the Gemini API key

Do not place the API key in source code, `application.properties`, or Git. Set it directly in the terminal.

Windows CMD:

```cmd
set GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

Windows PowerShell:

```powershell
$env:GEMINI_API_KEY="YOUR_GEMINI_API_KEY"
```

### 2. Start the backend

Windows CMD:

```cmd
cd backend
mvn spring-boot:run
```

The backend runs at `http://127.0.0.1:8080`.

The health endpoint is:

```text
GET http://127.0.0.1:8080/
```

### 3. Start the frontend

Open a second terminal:

```cmd
cd frontend
npm install
npm run dev -- --host 127.0.0.1
```

Open the URL printed by Vite, normally:

```text
http://127.0.0.1:5173/
```

## REST API

### Analyze a resume

```text
POST /api/screen/analyze
Content-Type: multipart/form-data
```

Form fields:

- `resumeFile` - optional PDF or DOCX file
- `resumeText` - optional manually pasted resume text
- `jobDescription` - required job description text

The response contains:

```json
{
	"matchScore": 0,
	"candidateStrengths": [],
	"missingSkills": [],
	"interviewQuestions": [],
	"hiringRecommendation": "",
	"roleRecommendations": [],
	"trainingRecommendations": []
}
```

## Demo Flow

1. Open the frontend URL.
2. Upload a PDF/DOCX resume or paste resume text.
3. Paste the target job description.
4. Select **Analyze Resume**.
5. Review the score, strengths, missing skills, recommendation, roles, training, and five interview questions.
6. Select **Export report** to download a PDF.

## Assumptions

- AI results are advisory and support human hiring decisions; they do not replace human review.
- Resume files are processed in memory and are not stored in a database.
- The current assessment does not require authentication, user accounts, or persistent storage.
- Only PDF and DOCX resumes are supported for file upload.
- Uploaded files are limited to 5 MB by the backend.
- The Gemini API key is supplied through `GEMINI_API_KEY` and must not be committed to the repository.
- The application requires network access to call the Gemini API.
- Gemini API availability, quotas, rate limits, and model access are external dependencies.
