# AI Appraisal & Assessment Platform — R&D Handoff Context

## 1. Project Vision

Build a reusable, Bangladesh-centric **AI-powered Appraisal & Assessment Platform** that evaluates a person's full-year performance/assessment evidence across approximately **25 parameters/metrics**, produces **AI-suggested scores**, and generates an evidence-based **pen picture**.

Initial target domains:

1. **DESCO** — AI-based employee performance management/appraisal.
2. **School/College LMS** — student assessment and academic development.
3. **Teachers** — teacher performance appraisal.
4. Other institutional assessment use cases later.

Core R&D question:

> How can an AI engine fairly and consistently analyze a full year's quantitative and qualitative evidence across multiple parameters, produce defensible suggested scores, identify strengths and development areas, and generate a professional pen picture?

---

## 2. Fundamental Architecture Decision

Do **not** build one generic scoring model and merely change parameter names.

Use:

```text
                 AI Assessment Platform
                         |
          +--------------+--------------+
          |              |              |
          v              v              v
      Employee         Teacher       Student
      Engine           Engine        Engine
```

Share infrastructure for:

- AI/model integration
- Evidence processing
- Human review
- Audit
- Pen-picture generation
- Assessment lifecycle

But keep domain-specific:

- Parameters
- Rubrics
- Weights
- Evidence types
- Scoring rules
- Growth/trend rules
- Pen-picture style

### Critical exception

**Students must NOT use the same assessment methodology as employees or teachers.**

Students have substantial objective academic evidence: exams, quizzes, classwork, assignments, projects, attendance, etc. Therefore the student engine needs its own academic scoring model.

---

# 3. Assessment Engines

## 3.1 Employee / DESCO

Potential areas:

- KPI/target achievement
- Job knowledge
- Work quality
- Productivity
- Discipline
- Attendance
- Teamwork
- Communication
- Leadership
- Initiative
- Innovation
- Problem solving
- Customer/service contribution
- Professional development
- Organizational contribution
- Other DESCO-specific parameters

The final ~25 parameters should later be designed and validated against the applicable DESCO/public-sector framework.

The AI should support the official appraisal framework, not automatically replace it.

## 3.2 Teacher

Potential areas:

- Teaching effectiveness
- Lesson planning
- Curriculum delivery
- Student engagement
- Classroom management
- Assessment quality
- Student learning outcomes
- Communication
- Professional development
- Teaching innovation
- Institutional contribution
- Mentoring/support
- Professionalism

Teacher assessment and pen pictures should focus on teaching quality, student engagement, professional contribution, and development.

## 3.3 Student

Dedicated categories:

### Academic Performance
- Final exams
- Midterms
- Class tests
- Quizzes
- Assignments
- Practical exams
- Projects
- Lab work

### Learning Progress
- Improvement over time
- Trend
- Consistency
- Recovery from weaknesses

### Classroom Performance
- Classwork
- Participation
- Presentations
- Questions
- Problem solving
- Group work
- Teacher observations

### Learning Behaviour
- Attendance
- Punctuality
- Assignment submission
- Discipline
- Consistency
- Engagement

### Skills & Development
- Critical thinking
- Creativity
- Communication
- Collaboration
- Leadership
- Practical application
- Digital skills

---

# 4. Objective Data vs AI Interpretation

Locked principle:

> **Objective data should be calculated deterministically. The LLM should interpret combined evidence rather than perform basic mathematical scoring.**

Example:

```text
Final Exam = 87%
Midterm = 82%
Quiz Average = 91%
Classwork = 85%
Assignment = 88%
```

If weights are:

```text
Final Exam       40%
Midterm          20%
Quizzes          15%
Classwork        10%
Assignments      15%
```

Software calculates:

```text
87 × 0.40
+ 82 × 0.20
+ 91 × 0.15
+ 85 × 0.10
+ 88 × 0.15

= 86.35%
```

The LLM should then interpret what the results plus qualitative evidence mean about learning and development.

Do not ask the LLM whether `87` is good.

---

# 5. Universal Score Scale

Initial recommended scale:

| Score | Meaning |
|---:|---|
| 1 | Unsatisfactory |
| 2 | Needs Improvement |
| 3 | Meets Expectations |
| 4 | Exceeds Expectations |
| 5 | Outstanding |

Optional reporting conversion:

```text
1 = 20/100
2 = 40/100
3 = 60/100
4 = 80/100
5 = 100/100
```

The scale should ultimately be configurable per framework.

---

# 6. Parameter Rubrics

Every parameter needs an explicit scoring rubric.

Example:

### Leadership

| Score | Definition |
|---:|---|
| 1 | Unable to effectively lead or coordinate |
| 2 | Shows limited leadership |
| 3 | Effectively manages normal responsibilities |
| 4 | Frequently demonstrates strong leadership |
| 5 | Demonstrates exceptional leadership with measurable organizational impact |

The LLM should compare evidence against the rubric rather than freely invent a score.

---

# 7. Four Dimensions for Qualitative Parameters

General starting model:

1. **Achievement** — what was accomplished?
2. **Quality** — how well was it accomplished?
3. **Consistency** — was performance sustained?
4. **Impact** — what benefit resulted?

Initial conceptual weighting:

```text
Achievement   30%
Quality       25%
Consistency   20%
Impact        25%
```

These are starting points, not final rules. They must become configurable and may differ for student parameters.

---

# 8. Evidence-First Evaluation

The system evaluates **evidence**, not writing quality.

Bad:

```text
"I worked very hard throughout the year."
        ↓
High score
```

Better:

```text
Completed 97% of assigned projects
Reduced processing time by 18%
Led a team of 12
Resolved 34 customer complaints
        ↓
Strong evidence
        ↓
AI evaluation
```

Conceptual flow:

```text
Claim
  ↓
Evidence
  ↓
Achievement
  ↓
Quality
  ↓
Consistency
  ↓
Impact
  ↓
Suggested Score
```

---

# 9. Evidence Types

## Quantitative

Examples:

```text
Target = 100
Achieved = 96
Attendance = 94%
Exam = 87%
Quiz = 92%
```

## Qualitative

Examples:

- Supervisor comments
- Teacher observations
- Student reflection
- Annual assessment statements
- Project descriptions

## System-generated

### Employee/DESCO
- Attendance
- Leave
- Task completion
- Complaint resolution
- Project completion
- Training
- Disciplinary records
- Supervisor observations

### LMS
- Exam results
- Assignment marks
- Attendance
- Quiz results
- Learning progress
- Course completion
- Teacher feedback
- Participation

---

# 10. Evidence Strength & Confidence

Every AI evaluation should contain more than a score.

Example:

```json
{
  "score": 4.3,
  "confidence": 0.91,
  "evidenceStrength": "HIGH",
  "justification": "...",
  "strengths": [],
  "improvementAreas": []
}
```

Recommended evidence levels:

- HIGH
- MEDIUM
- LOW
- INSUFFICIENT

Low-evidence or low-confidence cases should be flagged for human review.

---

# 11. Student Growth

Student assessment should preserve two distinct concepts:

### Current Performance
How well is the student performing now?

### Learning Growth
How much has the student improved?

Example:

```text
Student A
Previous = 60%
Current = 82%
Growth = +22 points

Student B
Previous = 90%
Current = 91%
Growth = +1 point
```

Student B has higher current performance; Student A has stronger growth.

Do not hide growth inside a single average.

---

# 12. Student Subject-Level Assessment

Maintain subject-level performance.

Example:

```text
Mathematics = 86%
Physics    = 78%
English    = 91%
ICT        = 94%
Bangla     = 82%
```

The system can identify:

**Strengths**
- ICT
- English
- Mathematics

**Development Areas**
- Physics
- Bangla

The annual pen picture should consider the full academic profile.

---

# 13. Annual Timeline

Annual assessment should preserve evidence over time:

```text
January
   ↓
February
   ↓
March
   ↓
...
   ↓
December
```

Possible evidence:

- Exams
- Quizzes
- Classwork
- Assignments
- Attendance
- Teacher observations
- Projects
- Practical work

This lets the system detect:

- Improvement
- Decline
- Consistency
- Sudden changes
- Persistent weaknesses
- Sustained excellence

---

# 14. Final Scoring Principle

The LLM suggests parameter scores.

The application calculates the final weighted score.

```text
Parameter Score
      ×
Parameter Weight
      ↓
Weighted Contribution
      ↓
Overall Score
      ↓
Rating / Grade
```

The exact mathematical model is **not finalized yet**. That is the next R&D task.

---

# 15. Human Approval

The system should be:

> **AI-assisted assessment / decision support**

not:

> **AI makes the final decision.**

Recommended workflow:

```text
Evidence
   ↓
AI Suggested Score
   ↓
Human Reviewer
   ↓
Approve / Modify
   ↓
Final Approved Score
   ↓
Pen Picture
```

Record AI suggestions and human overrides.

---

# 16. Pen Picture Generation

Generate the pen picture **after structured evaluation**.

```text
Parameters
    ↓
Parameter Scores
    ↓
Evidence Analysis
    ↓
Strengths
Weaknesses
Achievements
Development Areas
Growth / Trends
    ↓
Overall Score
    ↓
Pen Picture
```

Pen pictures must be domain-specific.

### Employee
Job performance, responsibilities, achievement, leadership, contribution, professional development.

### Teacher
Teaching effectiveness, student engagement, classroom performance, academic contribution, professional development.

### Student
Academic achievement, learning progress, subject strengths, development areas, participation, learning behaviour, skills, overall development.

---

# 17. Boilerplate Application Strategy

**Start the boilerplate now, but do not build the final DESCO or LMS product yet.**

The boilerplate should be an:

> **AI Assessment Engine / AI Appraisal & Assessment Reference Implementation**

It should prove the stable core architecture while the actual 25-parameter frameworks remain configurable.

Do not wait until every final metric is decided.

---

# 18. First Boilerplate Goal

Prove this pipeline:

```text
Assessment Framework
       ↓
Parameters
       ↓
Evidence
       ↓
AI Evaluation
       ↓
Suggested Parameter Scores
       ↓
Deterministic Score Calculation
       ↓
Human Review
       ↓
Final Score
       ↓
Pen Picture
```

Use fake/demo data initially.

Example:

```text
Employee:
Rahim

Evidence:
- Completed 96% of targets
- Led 8-person team
- Reduced processing time by 15%
- Received positive supervisor feedback
- Needs improvement in documentation
```

The first demo should support:

1. Create framework.
2. Add parameters.
3. Create assessment.
4. Submit evidence.
5. Run AI evaluation.
6. Produce structured parameter scores.
7. Calculate overall score.
8. Human approval/modification.
9. Generate pen picture.

---

# 19. Recommended Technology Stack

The LMS will be Java-based and DESCO is expected/likely to use Java, so the boilerplate should be Java/Spring-based.

Recommended:

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Backend | Spring Boot 4.1.x |
| Build | Maven |
| AI abstraction | Spring AI 2.x |
| Database | PostgreSQL |
| Vector storage | pgvector later |
| ORM | Spring Data JPA |
| Security | Spring Security |
| API | REST |
| Validation | Jakarta Validation |
| JSON | Jackson |
| Migration | Flyway |
| Testing | JUnit + Mockito + Testcontainers |
| API docs | OpenAPI/Swagger |
| Containers | Docker |
| Observability | Micrometer/OpenTelemetry |
| Frontend | Not required initially |
| LLM | Provider-independent |
| RAG | Later |

If final DESCO infrastructure mandates Java 21, Java 21 is also acceptable. The architecture should not depend on one JDK version.

---

# 20. Spring AI

Use **Spring AI** as the AI abstraction layer.

Conceptually:

```text
Spring Boot
      +
Spring AI
      +
AI Model Provider
```

Do not architect the business system around Gemini or Claude.

The model may later change from:

```text
Gemini
```

to:

```text
Claude
```

to:

```text
Self-hosted Qwen
```

without rewriting the assessment engine.

---

# 21. AI Provider Abstraction

Conceptually:

```text
AI Evaluation Service
        |
        ↓
AI Model Gateway / Spring AI
        |
   +----+-------------+
   ↓    ↓             ↓
Gemini Claude      Local Model
```

The provider should be configurable.

The domain/business layer should not know which model is being used.

---

# 22. Structured AI Output

Do NOT parse free-form AI prose to extract scores.

Preferred:

```json
{
  "parameterId": 12,
  "suggestedScore": 4.5,
  "confidence": 0.91,
  "evidenceStrength": "HIGH",
  "justification": "...",
  "strengths": [],
  "improvementAreas": []
}
```

Use Spring AI structured output to map responses into Java DTOs/POJOs and validate them.

---

# 23. Database Direction

Use PostgreSQL for the boilerplate.

Reasons:

- Strong relational database
- Excellent Spring/Java support
- Good future AI/RAG ecosystem
- pgvector support
- Can initially keep relational and vector data together

Conceptually:

```text
PostgreSQL
   |
   +-- Relational assessment data
   |
   +-- pgvector
          |
          +-- RAG embeddings
```

If DESCO later requires MSSQL, keep the domain/business layer database-agnostic so the persistence layer can adapt.

---

# 24. Framework-Driven Database Design

Do NOT hard-code:

```text
desco_parameter_1
desco_parameter_2
...
desco_parameter_25
```

Use configurable entities such as:

```text
assessment_framework
assessment_parameter
parameter_rubric
parameter_weight
assessment
assessment_subject
evidence
evaluation
evaluation_score
human_review
pen_picture
audit_log
```

Relationship:

```text
Assessment Framework
       |
       +-- Parameter 1
       +-- Parameter 2
       +-- Parameter 3
       +-- ...
       +-- Parameter 25
```

Student can use a completely different framework.

---

# 25. Academic Data Must Have Its Own Layer

Do not reduce all student data to generic text evidence.

Use:

```text
Academic Evidence
       |
       +-- Exam
       +-- Quiz
       +-- Classwork
       +-- Assignment
       +-- Project
       +-- Practical
```

The academic engine can calculate:

- Academic score
- Subject score
- Growth score
- Trend
- Consistency

Then pass those derived facts plus relevant qualitative evidence to the AI.

---

# 26. Initial API Direction

Conceptual API surface:

### Framework

```http
POST /api/frameworks
POST /api/frameworks/{id}/parameters
```

### Assessment

```http
POST /api/assessments
POST /api/assessments/{id}/evidence
```

### AI Evaluation

```http
POST /api/assessments/{id}/evaluate
GET  /api/assessments/{id}/evaluation
```

### Human Review

```http
POST /api/assessments/{id}/review
```

### Pen Picture

```http
POST /api/assessments/{id}/pen-picture
```

These are conceptual only; final API contracts should be designed by Claude.

---

# 27. Backend Architecture Direction

Use a **modular monolith** first.

Do NOT begin with microservices.

Conceptual structure:

```text
ai-assessment-engine/
│
├── assessment/
│   ├── domain/
│   ├── application/
│   ├── infrastructure/
│   └── api/
│
├── evaluation/
│   ├── domain/
│   ├── application/
│   └── infrastructure/
│
├── scoring/
│   ├── domain/
│   └── application/
│
├── evidence/
├── penpicture/
│
├── ai/
│   ├── provider/
│   ├── prompt/
│   ├── structured/
│   └── evaluation/
│
├── framework/
├── review/
├── audit/
└── common/
```

This is a conceptual direction. Claude Opus should create the final architecture.

---

# 28. Claude Opus → Gemini Workflow

Planned development workflow:

```text
Claude Opus
    ↓
Architecture + Implementation Planning
    ↓
Gemini
    ↓
Code + Tests
```

### Claude Opus responsibilities

- Architecture
- Domain design
- Database design
- API design
- AI evaluation design
- Scoring architecture
- Implementation sequencing
- Testing strategy
- Architecture/code review

### Gemini responsibilities

- Execute approved plan
- Write code
- Write tests
- Fix implementation issues

Do not ask Gemini to redesign the architecture independently during implementation unless the architecture is intentionally revised.

---

# 29. Recommended Planning Documents

Claude should eventually produce:

```text
01-architecture.md
02-domain-model.md
03-database-design.md
04-api-spec.md
05-ai-evaluation-design.md
06-scoring-engine.md
07-testing-strategy.md
08-implementation-plan.md
```

These documents become the source of truth for Gemini.

Recommended cycle:

```text
Claude:
Design Phase 1
        ↓
Gemini:
Implement Phase 1
        ↓
Tests
        ↓
Claude:
Review
        ↓
Gemini:
Fix
        ↓
Next Phase
```

Do not have Gemini build the entire system in one shot.

---

# 30. What NOT to Build Yet

Do not immediately build:

- Final DESCO 25 parameters
- Final LMS 25 parameters
- Fine-tuning
- Complex RAG
- Production GPU infrastructure
- Payment/subscription
- Large reporting suite
- Mobile application
- Full enterprise frontend

First prove the assessment engine.

---

# 31. RAG & Fine-Tuning Strategy

Do not train a model from scratch.

## Phase 1 — Prompt + Rubric

Use:

- Strong prompts
- Structured outputs
- Parameter rubrics
- Deterministic scoring

## Phase 2 — RAG

Potential knowledge sources:

- HR policies
- Competency frameworks
- Job descriptions
- Academic policies
- Assessment rules
- Institutional guidelines
- Historical approved examples

Potential vector technologies:

- PostgreSQL + pgvector
- Qdrant
- Milvus

## Phase 3 — Human-Approved Dataset

Capture:

```text
Evidence
+
Parameter
+
AI Suggested Score
+
Human Final Score
+
Human Justification
+
Final Pen Picture
```

## Phase 4 — Fine-Tuning

Eventually:

```text
Base Open-Weight Model
        +
Bangladesh-specific approved assessment data
        ↓
LoRA / QLoRA
        ↓
Domain-adapted assessment model
```

Fine-tuning should happen only after sufficient high-quality approved data exists.

---

# 32. Model Selection Direction

Candidate families discussed:

- **Qwen** — primary candidate
- **Llama** — strong alternative
- **Mistral** — efficient alternative

Final model should be selected through an actual benchmark using representative Bangladesh-specific examples.

Evaluate:

- Reasoning
- Bangla/English capability
- Structured JSON reliability
- Score consistency
- Evidence grounding
- Hallucination
- Fine-tuning support
- On-premise deployment
- Cost/performance

Keep the model replaceable.

---

# 33. Dataset Strategy

The most valuable training data is **human-approved evaluation data**, not simply user-written descriptions.

Eventually collect:

```text
Employee / Student / Teacher
        +
Framework
        +
Parameter
        +
Evidence
        +
AI Suggested Score
        +
Human Final Score
        +
Human Reason / Override
        +
Final Pen Picture
```

Include:

- High performers
- Average performers
- Low performers
- Conflicting evidence
- Missing evidence
- Improvement cases
- Declining performance
- Different job roles
- Different student levels
- Different subjects
- Different institutional policies

---

# 34. Governance / Audit

Retain:

- Evidence used
- AI model name/version
- Prompt/rubric version
- Suggested score
- Confidence
- Evidence strength
- AI explanation
- Human override
- Final approved score
- Timestamp

This supports institutional trust, auditing, debugging, and model validation.

---

# 35. R&D Roadmap

## Phase 1 — Scoring Methodology
**Current immediate phase.**

Finalize:

1. Universal score scale
2. Parameter structure
3. Parameter-specific rubrics
4. Weighting methodology
5. Evidence strength
6. Confidence
7. Missing-data handling
8. Consistency calculation
9. Student growth calculation
10. Trend calculation
11. Outlier handling
12. Human override rules
13. Final-score calculation

## Phase 2 — Boilerplate Architecture

1. Claude creates architecture.
2. Claude creates implementation plan.
3. Gemini implements.
4. Tests run.
5. Claude reviews.
6. Gemini fixes.
7. Continue phase-by-phase.

## Phase 3 — DESCO Framework

Design and validate approximately 25 employee performance parameters.

## Phase 4 — Teacher Framework

Create dedicated teacher-performance parameters and rules.

## Phase 5 — Student Framework

Create dedicated academic/student framework incorporating:

- Exams
- Quizzes
- Classwork
- Assignments
- Projects
- Attendance
- Participation
- Behaviour
- Learning growth
- Skills
- Teacher feedback

## Phase 6 — AI/RAG

Implement:

```text
Evidence Engine
+
RAG
+
LLM
+
Structured Evaluation
+
Scoring Engine
+
Pen Picture Generator
```

## Phase 7 — Data Collection

Capture:

```text
AI Suggestion
        ↓
Human Decision
        ↓
Final Result
```

## Phase 8 — Validation

Measure:

- Agreement with human evaluators
- Consistency
- Calibration
- False high/low scores
- Bias
- Robustness to vague inputs
- Robustness to exaggerated claims
- Missing-data behaviour
- Domain-specific performance

## Phase 9 — Fine-Tuning

Only after sufficient approved data exists.

---

# 36. Locked Core Principles

1. One platform, multiple assessment engines.
2. Students are not employees.
3. Teachers are not students.
4. Objective data is calculated deterministically.
5. LLM interprets evidence; it does not replace mathematical calculations.
6. Every parameter needs a defined rubric.
7. Scores must be evidence-backed.
8. Confidence and evidence strength accompany AI scores.
9. Student growth is separate from current performance.
10. Student subject-level performance is retained.
11. Annual timelines matter.
12. Pen pictures are generated after structured evaluation.
13. Pen-picture style is domain-specific.
14. Human approval remains part of final decisions.
15. The AI model must be replaceable.
16. Boilerplate is framework-driven, not DESCO/LMS hard-coded.
17. Start with a modular monolith, not microservices.
18. PostgreSQL is preferred for the boilerplate, with pgvector available later.
19. Spring AI should provide the AI abstraction layer.
20. Human-approved historical evaluations are the foundation for future fine-tuning.
21. The first boilerplate is a reference implementation, not the final DESCO/LMS product.

---

# 37. Immediate Next R&D Task

**Do not start coding yet.**

The next task is:

> **Design the mathematical scoring model.**

We need to determine precisely:

1. What is a parameter?
2. What is evidence?
3. How is evidence converted into a parameter score?
4. How are objective and qualitative evidence combined?
5. How are weights applied?
6. How is consistency calculated?
7. How is student growth calculated?
8. How are missing/insufficient data handled?
9. How are contradictory evidence handled?
10. How is confidence calculated?
11. How is the final score calculated?
12. How is the final rating/grade determined?
13. How does human override affect the final result?
14. Which parts are deterministic and which parts are AI-generated?

Once the scoring model is sufficiently stable:

> **Stop broad brainstorming and build the boilerplate.**

---

# 38. One-Sentence Project Definition

> **A configurable, evidence-based, AI-assisted assessment platform that evaluates employees, teachers, students, and other institutional members through domain-specific assessment engines, combines objective and qualitative evidence, produces explainable AI-suggested scores and professional pen pictures, and keeps humans responsible for final decisions.**
