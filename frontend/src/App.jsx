import React, { useState } from "react";
import { Upload, FileText, Sparkles, Download, CheckCircle2, AlertCircle, BriefcaseBusiness, GraduationCap, X, RotateCcw, LoaderCircle } from "lucide-react";
import { jsPDF } from "jspdf";
import html2canvas from "html2canvas";

const API = `${window.location.protocol}//${window.location.hostname}:8080/api/screen/analyze`;

function App() {
  const [file, setFile] = useState(null);
  const [resumeText, setResumeText] = useState("");
  const [jobDescription, setJobDescription] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function handleFileChange(event) {
    const selectedFile = event.target.files?.[0];
    if (!selectedFile) return;
    if (!/\.(pdf|docx)$/i.test(selectedFile.name)) {
      setError("Choose a PDF or DOCX resume.");
      return;
    }
    if (selectedFile.size > 5 * 1024 * 1024) {
      setError("Resume files must be smaller than 5 MB.");
      return;
    }
    setError("");
    setFile(selectedFile);
  }

  function resetForm() {
    setFile(null);
    setResumeText("");
    setJobDescription("");
    setResult(null);
    setError("");
  }

  async function analyze() {
    setError("");
    setResult(null);

    if (!file && !resumeText.trim()) {
      setError("Please upload a resume or paste resume text.");
      return;
    }
    if (!jobDescription.trim()) {
      setError("Please enter the job description.");
      return;
    }

    const form = new FormData();
    if (file) form.append("resumeFile", file);
    form.append("resumeText", resumeText);
    form.append("jobDescription", jobDescription);

    setLoading(true);
    try {
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), 70000);
      const response = await fetch(API, { method: "POST", body: form, signal: controller.signal });
      clearTimeout(timeout);
      const data = await response.json().catch(() => ({}));
      if (!response.ok) throw new Error(data.error || `Backend returned ${response.status}.`);
      setResult({
        ...data,
        candidateStrengths: Array.isArray(data.candidateStrengths) ? data.candidateStrengths : [],
        missingSkills: Array.isArray(data.missingSkills) ? data.missingSkills : [],
        interviewQuestions: Array.isArray(data.interviewQuestions) ? data.interviewQuestions : [],
        roleRecommendations: Array.isArray(data.roleRecommendations) ? data.roleRecommendations : [],
        trainingRecommendations: Array.isArray(data.trainingRecommendations) ? data.trainingRecommendations : []
      });
    } catch (e) {
      setError(e.name === "AbortError"
        ? "The AI request timed out. Check the Gemini model/key and try again."
        : e.name === "TypeError"
        ? "Cannot reach the backend. Start Spring Boot on port 8080 and try again."
        : e.message || "Analysis failed.");
    } finally {
      setLoading(false);
    }
  }

  async function downloadPDF() {
    const element = document.getElementById("report");
    if (!element) return;
    const canvas = await html2canvas(element, { scale: 1.5, backgroundColor: "#f7f8fc" });
    const image = canvas.toDataURL("image/png");
    const pdf = new jsPDF("p", "mm", "a4");
    const width = 190;
    const height = canvas.height * width / canvas.width;
    pdf.addImage(image, "PNG", 10, 10, width, height);
    pdf.save("resume-screening-report.pdf");
  }

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand"><span className="brand-mark"><Sparkles size={18}/></span><span>Screenwise <b>AI</b></span></div>
        <div className="topbar-meta"><span className="status-dot" /> Gemini screening workspace</div>
      </header>

      <main className="container">
        <section className="hero">
          <p className="eyebrow"><span /> AI-ASSISTED TALENT REVIEW</p>
          <h1>Turn a resume into a <em>clear next step.</em></h1>
          <p>Compare candidate evidence with a role in seconds, then walk into the interview with a sharper point of view.</p>
        </section>

        <section className="input-grid">
          <div className="card">
            <div className="section-kicker"><span>01</span><h2><FileText size={20}/> Candidate resume</h2></div>
            <label className={`dropzone ${file ? "has-file" : ""}`}>
              <Upload size={30}/>
              <strong>{file ? file.name : "Upload PDF or DOCX"}</strong>
              <span>{file ? `${(file.size / 1024 / 1024).toFixed(2)} MB · ready to analyze` : "Drag and drop or select from your computer"}</span>
              <input type="file" accept=".pdf,.docx" onChange={handleFileChange}/>
            </label>
            {file && <button className="clear-file" onClick={() => setFile(null)}><X size={14}/> Remove file</button>}
            <div className="or">OR</div>
            <textarea
              value={resumeText}
              onChange={e => setResumeText(e.target.value)}
              placeholder="Paste resume text manually..."
              rows="9"
            />
          </div>

          <div className="card">
            <div className="section-kicker"><span>02</span><h2><BriefcaseBusiness size={20}/> Target role</h2></div>
            <textarea
              className="jd"
              value={jobDescription}
              onChange={e => setJobDescription(e.target.value)}
              placeholder="Paste the complete job description here..."
              rows="18"
            />
          </div>
        </section>

        {error && <div className="error"><AlertCircle size={18}/> {error}</div>}

        <div className="action-row">
        <button className="analyze" onClick={analyze} disabled={loading}>
          {loading ? <LoaderCircle className="spin" size={19}/> : <Sparkles size={19}/>} 
          {loading ? "Analyzing with AI..." : "Analyze Resume"}
        </button>
        {(file || resumeText || jobDescription || result) && <button className="reset" onClick={resetForm}><RotateCcw size={16}/> Clear workspace</button>}
        </div>

        {result && (
          <section id="report" className="report">
            <div className="report-head">
              <div>
                <h2>Screening Report</h2>
                <p>AI-generated comparison of the resume against the job description.</p>
              </div>
              <button className="download" onClick={downloadPDF}><Download size={17}/> Export report</button>
            </div>

            <div className="score-row">
              <div className="score-card">
                <span>Resume Match Score</span>
                <strong>{result.matchScore}<small>/100</small></strong>
                <div className="bar"><div style={{width: `${result.matchScore}%`}} /></div>
                <span className="score-note">Evidence-based fit estimate</span>
              </div>
              <div className="recommendation">
                <span>Overall Hiring Recommendation</span>
                <h3><CheckCircle2 size={22}/> {result.hiringRecommendation}</h3>
              </div>
            </div>

            <div className="result-grid">
              <ResultCard title="Candidate Strengths" items={result.candidateStrengths} />
              <ResultCard title="Missing Skills" items={result.missingSkills} />
              <ResultCard title="Role Recommendations" items={result.roleRecommendations} icon={<BriefcaseBusiness size={18}/>} />
              <ResultCard title="Training Recommendations" items={result.trainingRecommendations} icon={<GraduationCap size={18}/>} />
            </div>

            <div className="card questions">
              <div className="questions-head"><h2>Recommended interview questions</h2><span>5 prompts</span></div>
              <ol>{result.interviewQuestions.map((q, i) => <li key={i}>{q}</li>)}</ol>
            </div>
          </section>
        )}
      </main>
    </div>
  );
}

function ResultCard({ title, items = [], icon }) {
  return (
    <div className="card result-card">
      <h2>{icon || <CheckCircle2 size={18}/>} {title}</h2>
      {items.length ? <ul>{items.map((x, i) => <li key={i}>{x}</li>)}</ul> : <p>No items returned.</p>}
    </div>
  );
}

export default App;
