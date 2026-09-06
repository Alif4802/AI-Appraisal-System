# AI Appraisal & Assessment Platform — Boilerplate Application Handoff

## Purpose

This document contains the agreed direction for the **boilerplate/reference application** that will eventually become the technical foundation for two major Bangladesh-centric systems:

1. **DESCO AI-based Employee Performance Management/Appraisal**
2. **School/College LMS AI-based Student Assessment**

A separate R&D handoff document covers the broader assessment/scoring methodology. This document focuses specifically on **how the reusable boilerplate should be designed and developed**.

---

# 1. Boilerplate Strategy

The recommendation is:

> **Start the boilerplate after the mathematical scoring methodology is sufficiently defined, but do not wait until every DESCO/LMS parameter is finalized.**

The boilerplate is NOT:

- The final DESCO application.
- The final LMS.
- A hard-coded employee appraisal system.
- A hard-coded student assessment system.

It is a:

> **Reusable AI Assessment Engine / Reference Implementation**

Its purpose is to prove the stable technical architecture and assessment pipeline.

---

# 2. What the Boilerplate Must Prove

The first working version should demonstrate:

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
Final Approved Score
       ↓
Pen Picture
```

A simple fake/demo dataset should be enough initially.

Example:

```text
Employee:
Rahim

Evidence:
- Completed 96% of targets
- Led an 8-person team
- Reduced processing time by 15%
- Received positive supervisor feedback
- Needs improvement in documentation
```

The boilerplate should successfully:

1. Create an assessment framework.
2. Define parameters.
3. Define parameter rubrics.
4. Define weights.
5. Create an assessment subject.
6. Store evidence.
7. Run AI evaluation.
8. Receive structured AI results.
9. Calculate scores deterministically.
10. Allow human review/override.
11. Store the final approved result.
12. Generate a domain-appropriate pen picture.

---

# 3. Do Not Hard-Code the 25 Parameters

The most important architecture principle is:

> **The assessment engine must be framework-driven.**

Do NOT create:

```text
desco_parameter_1
desco_parameter_2
...
desco_parameter_25
```

Instead, create configurable assessment frameworks.

Example:

```text
Employee Framework
    ├── Productivity
    ├── Work Quality
    ├── Leadership
    ├── Teamwork
    └── ...

Teacher Framework
    ├── Teaching Effectiveness
    ├── Classroom Management
    ├── Student Engagement
    └── ...

Student Framework
    ├── Academic Performance
    ├── Learning Growth
    ├── Attendance
    ├── Participation
    └── ...
```

The core engine remains the same.

---

# 4. Separate Assessment Engines

The application should have a shared core and separate domain-specific engines.

```text
                 AI Assessment Platform
                         |
          ┌──────────────┼──────────────┐
          ↓              ↓              ↓
      Employee         Teacher       Student
       Engine           Engine        Engine
          |              |              |
          └──────────────┼──────────────┘
                         ↓
                Shared AI Infrastructure
```

Shared:

- Assessment lifecycle
- Framework management
- Evidence infrastructure
- AI model gateway
- Structured AI output
- Scoring infrastructure
- Human review
- Pen-picture infrastructure
- Audit
- Security
- Configuration

Domain-specific:

- Parameters
- Rubrics
- Weights
- Evidence rules
- Objective calculations
- Growth/trend rules
- Pen-picture instructions

---

# 5. Student Engine Is Fundamentally Different

Do not model students as ordinary employee appraisal subjects.

Students require a dedicated academic evidence layer.

```text
Student
   |
   +-- Exams
   +-- Midterms
   +-- Class Tests
   +-- Quizzes
   +-- Classwork
   +-- Assignments
   +-- Projects
   +-- Practical/Lab
   +-- Attendance
   +-- Participation
   +-- Teacher Feedback
```

Objective academic data should be mathematically calculated by the application.

The AI should interpret the calculated results together with qualitative evidence.

---

# 6. Recommended Technology Stack

The LMS is expected to use Java, and the DESCO system is likely to use Java/Spring as well.

Therefore the boilerplate should be Java/Spring-based.

Recommended initial stack:

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
| DB migration | Flyway |
| Unit tests | JUnit + Mockito |
| Integration tests | Testcontainers |
| API docs | OpenAPI/Swagger |
| Containers | Docker |
| Observability | Micrometer/OpenTelemetry |
| Frontend | Not required initially |
| LLM | Provider-independent |
| RAG | Later |

### JDK flexibility

Java 25 is the preferred new-project target if the eventual deployment environment supports it.

If DESCO or another target environment mandates Java 21, Java 21 is acceptable.

The architecture must not depend on one exact JDK version.

---

# 7. Why Spring AI

Use **Spring AI** as the AI abstraction layer.

Conceptual architecture:

```text
Spring Boot
      |
   Spring AI
      |
AI Model Provider
```

The business/domain layer should not directly depend on Gemini, Claude, or Qwen.

The system should be able to evolve from:

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

# 8. AI Provider Abstraction

Conceptually:

```text
Assessment / Evaluation Service
             |
             ↓
       AI Model Gateway
             |
      +------+-------+----------+
      ↓              ↓          ↓
    Gemini         Claude     Local LLM
```

The actual implementation can use Spring AI's model/provider abstractions.

Provider selection should be configuration-driven.

Conceptually:

```text
AI_PROVIDER=gemini
```

Later:

```text
AI_PROVIDER=local
```

The exact configuration mechanism should be finalized by the architecture design.

---

# 9. Structured AI Output

Never rely on free-form AI prose to extract scores.

Bad:

```text
"The employee appears to deserve a score
of approximately 4.5..."
```

Preferred structured result:

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

Map AI output to validated Java DTOs/POJOs.

The AI result should be treated as a structured domain input to the scoring/review pipeline.

---

# 10. Deterministic vs AI Responsibilities

This separation is fundamental.

## Deterministic application responsibilities

- Basic arithmetic
- Weighted score calculation
- Academic score calculation
- Subject averages
- Attendance percentages
- Growth calculations
- Trend calculations
- Parameter weights
- Grade/rating conversion
- Threshold checks
- Missing-data rules
- Validation
- Final-score calculation
- Audit trail

## AI responsibilities

- Interpret qualitative evidence
- Compare evidence against rubrics
- Suggest qualitative parameter scores
- Explain score suggestions
- Identify strengths
- Identify weaknesses/development areas
- Interpret trends
- Summarize evidence
- Generate pen picture
- Provide recommendations where appropriate

The exact boundary must be refined during the scoring-methodology R&D.

---

# 11. Database Direction

Use **PostgreSQL** for the boilerplate.

Reasons:

- Strong relational capabilities.
- Excellent Java/Spring support.
- Easy future integration with `pgvector`.
- Suitable for AI/RAG workloads.
- Can keep relational and vector data in one ecosystem initially.

Conceptually:

```text
PostgreSQL
    |
    +-- Assessment relational data
    |
    +-- pgvector
           |
           +-- RAG embeddings
```

If the final DESCO implementation requires MSSQL, keep the domain/business layer database-agnostic and isolate persistence-specific concerns.

---

# 12. Framework-Driven Database Model

Conceptual entities:

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

Potential future student-specific entities:

```text
academic_record
academic_assessment
subject_result
exam_result
quiz_result
assignment_result
classwork_result
learning_growth
```

The exact schema must be finalized after the scoring methodology is designed.

---

# 13. Academic Data Layer

Student academic evidence deserves a dedicated model rather than being stored only as generic text.

Conceptual structure:

```text
Academic Evidence
       |
       +-- Exam
       +-- Midterm
       +-- Class Test
       +-- Quiz
       +-- Classwork
       +-- Assignment
       +-- Project
       +-- Practical
```

The academic engine can calculate:

- Current academic performance
- Subject performance
- Growth
- Trend
- Consistency

The resulting structured facts are then available to the AI evaluation layer.

---

# 14. Modular Monolith

Start with a **modular monolith**.

Do NOT start with microservices.

Reason:

- Faster R&D.
- Easier debugging.
- Simpler deployment.
- Easier database consistency.
- Easier AI experimentation.
- Clear module boundaries can later be extracted if required.

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
├── framework/
│
├── evidence/
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
├── academic/
│
├── penpicture/
│
├── ai/
│   ├── provider/
│   ├── prompt/
│   ├── structured/
│   └── evaluation/
│
├── review/
│
├── audit/
│
└── common/
```

This is conceptual. Claude Opus must produce the final package/module structure.

---

# 15. Suggested Core Modules

The final architecture should likely contain concepts similar to:

### Framework Module

Responsible for:

- Assessment frameworks
- Parameters
- Rubrics
- Weights
- Versioning/configuration

### Assessment Module

Responsible for:

- Assessment lifecycle
- Assessment subject
- Assessment period
- Status

### Evidence Module

Responsible for:

- Evidence records
- Evidence source
- Evidence type
- Time period
- Evidence quality/strength

### Evaluation Module

Responsible for:

- AI evaluation requests
- Evaluation results
- Structured reasoning
- Confidence

### Scoring Module

Responsible for:

- Parameter scoring
- Weighting
- Deterministic calculations
- Overall result

### Academic Module

Responsible for student-specific objective academic evidence.

### Review Module

Responsible for:

- Human review
- Approval
- Override
- Reviewer comments

### Pen Picture Module

Responsible for:

- Structured summary generation
- Domain-specific generation
- Final pen picture
- Versioning

### AI Module

Responsible for:

- Model/provider integration
- Prompts
- Structured output
- AI configuration
- AI request/response metadata

### Audit Module

Responsible for immutable assessment history and traceability.

---

# 16. Version Everything That Can Affect a Score

A major production requirement:

The system should eventually version:

- Assessment framework
- Parameter definitions
- Rubrics
- Weights
- Scoring rules
- Prompt templates
- AI model
- AI configuration

Why?

Suppose an assessment was scored in 2027 and the rubric changes in 2028.

The old assessment must still be reproducible.

Conceptually:

```text
Assessment
   |
   +-- Framework Version
   +-- Rubric Version
   +-- Scoring Rule Version
   +-- Prompt Version
   +-- AI Model Version
```

This should be considered from the beginning.

---

# 17. Human Review Model

Suggested workflow:

```text
AI Evaluation
      ↓
AI Suggested Result
      ↓
Reviewer
      ↓
+-------------+
| Approve     |
| Modify      |
| Return      |
+-------------+
      ↓
Final Approved Result
```

A human override should not erase the original AI suggestion.

Store both:

```text
AI Suggested Score
Human Final Score
Override Reason
Reviewer
Timestamp
```

This historical information will also become valuable training data later.

---

# 18. Pen Picture Architecture

Do not generate the pen picture independently from the score.

Recommended:

```text
Parameter Scores
      +
Evidence Summary
      +
Strengths
      +
Development Areas
      +
Growth / Trends
      +
Overall Result
       ↓
Pen Picture Generator
```

The pen picture generator should use domain-specific templates/instructions:

```text
Employee Pen Picture
Teacher Pen Picture
Student Pen Picture
```

The final text should be based on structured facts.

---

# 19. Initial Boilerplate API Direction

Conceptual endpoints:

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

### Evaluation

```http
POST /api/assessments/{id}/evaluate
GET  /api/assessments/{id}/evaluation
```

### Review

```http
POST /api/assessments/{id}/review
```

### Pen Picture

```http
POST /api/assessments/{id}/pen-picture
```

These are only starting concepts. Final API contracts should be designed by Claude.

---

# 20. Security & Governance

The boilerplate should plan for:

- Authentication
- Authorization
- Role-based access
- Assessment ownership
- Reviewer permissions
- Sensitive evidence protection
- Audit logs
- Model/version tracking
- Human override tracking
- Secure AI API credentials
- Data minimization
- Environment-specific secrets

Employee/student assessment data can be highly sensitive, so security should not be treated as an afterthought.

---

# 21. RAG Strategy

Do not make RAG the first feature.

First prove:

```text
Evidence
→
AI
→
Structured Evaluation
→
Score
```

Then add RAG.

Potential RAG sources:

### Employee/DESCO

- HR policies
- Competency framework
- Job descriptions
- Appraisal guidelines
- Institutional policies

### Teacher

- Teaching standards
- Institutional policies
- Curriculum guidelines
- Evaluation criteria

### Student

- Academic policies
- Assessment rules
- Curriculum information
- Grading rules
- Institutional guidelines

Potential vector technologies:

- PostgreSQL + pgvector
- Qdrant
- Milvus

Initial preference: PostgreSQL + pgvector unless R&D identifies a strong reason to use a separate vector database.

---

# 22. Fine-Tuning Strategy

Do NOT train a model from scratch.

First:

```text
Prompt + Rubric
```

Then:

```text
Prompt + Rubric + RAG
```

Then collect:

```text
Evidence
+
AI Suggestion
+
Human Decision
+
Final Score
+
Human Reason
+
Pen Picture
```

Only after enough high-quality data exists:

```text
Base Open-Weight Model
        +
Approved Historical Data
        ↓
LoRA / QLoRA
        ↓
Domain-adapted Model
```

The model should be evaluated against human decisions before being trusted.

---

# 23. Model Independence

The boilerplate should not be permanently coupled to the initial LLM.

Candidate families discussed:

- Qwen — primary candidate
- Llama — strong alternative
- Mistral — efficient alternative

However, the boilerplate should support model benchmarking rather than assuming one model is permanently best.

Benchmark criteria:

- Reasoning
- Bangla/English
- Structured output reliability
- Score consistency
- Evidence grounding
- Hallucination
- Fine-tuning capability
- On-premise deployment
- Cost/performance

---

# 24. Claude Opus → Gemini Development Workflow

The development process is intentionally divided.

## Claude Opus

Responsible for:

- Architecture
- Domain design
- Database design
- API design
- AI evaluation design
- Scoring architecture
- Module boundaries
- Implementation sequencing
- Testing strategy
- Technical review

## Gemini

Responsible for:

- Executing the approved plan
- Writing code
- Writing tests
- Implementing APIs
- Fixing implementation issues

Workflow:

```text
Claude
  ↓
Architecture / Plan
  ↓
Gemini
  ↓
Implementation
  ↓
Tests
  ↓
Claude Review
  ↓
Gemini Fixes
  ↓
Next Phase
```

Do not ask Gemini to build the entire application in one prompt.

---

# 25. Recommended Architecture Documents

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

These documents should be treated as the implementation source of truth.

---

# 26. Testing Strategy

Testing must cover both deterministic and AI components.

## Deterministic tests

- Weight calculations
- Score calculations
- Grade conversion
- Growth calculations
- Trend calculations
- Missing data
- Boundary conditions
- Rounding
- Validation

## AI tests

- Structured output validity
- Rubric adherence
- Evidence grounding
- Consistency
- Hallucination
- Prompt regression
- Model regression

## Integration tests

Use Testcontainers where appropriate.

## Evaluation dataset

Maintain a fixed benchmark dataset containing representative:

- Employee cases
- Teacher cases
- Student cases
- High/average/low performers
- Ambiguous cases
- Missing evidence
- Contradictory evidence
- Improvement/decline cases

---

# 27. What the First Boilerplate Should NOT Contain

Do not initially implement:

- Final DESCO 25 parameters
- Final LMS 25 parameters
- Full production RAG
- Fine-tuning
- Production GPU infrastructure
- Payment/subscription
- Mobile applications
- Large enterprise reporting
- Full production frontend
- Microservices

The first objective is to prove the **assessment engine architecture**.

---

# 28. Development Sequence

Recommended:

## Step 0 — R&D

Finalize the mathematical scoring methodology first.

## Step 1 — Architecture

Claude Opus designs the modular monolith and produces the architecture documents.

## Step 2 — Project Skeleton

Gemini creates:

- Spring Boot project
- Maven setup
- Module/package structure
- PostgreSQL connection
- Flyway
- Basic configuration
- Docker setup
- Testing foundation

## Step 3 — Framework Module

Implement:

- Framework
- Parameter
- Rubric
- Weight
- Versioning

## Step 4 — Assessment Module

Implement:

- Assessment
- Assessment subject
- Assessment period
- Lifecycle/status

## Step 5 — Evidence Module

Implement:

- Evidence
- Evidence type
- Source
- Time period
- Metadata
- Evidence strength

## Step 6 — Scoring Engine

Implement deterministic:

- Parameter score
- Weighting
- Overall score
- Rating/grade
- Missing-data rules

## Step 7 — AI Module

Implement:

- AI provider abstraction
- Spring AI
- Prompt management
- Structured output
- AI evaluation DTOs
- AI metadata

## Step 8 — AI Evaluation Pipeline

Implement:

```text
Evidence
→
AI
→
Structured Evaluation
→
Suggested Scores
```

## Step 9 — Human Review

Implement:

- Approve
- Modify
- Reject/return
- Override reason
- Audit trail

## Step 10 — Pen Picture

Implement domain-aware pen-picture generation.

## Step 11 — Student Academic Layer

Add:

- Exams
- Quizzes
- Classwork
- Assignments
- Projects
- Subject results
- Attendance
- Growth
- Trends

## Step 12 — RAG

Add institutional knowledge retrieval.

## Step 13 — Benchmarking

Compare candidate models.

## Step 14 — Real Frameworks

Add DESCO and LMS frameworks.

---

# 29. Final Architecture Goal

Eventually:

```text
                         AI Assessment Platform
                                  |
          +-----------------------+-----------------------+
          |                       |                       |
          v                       v                       v
      Employee                 Teacher                 Student
       Engine                  Engine                  Engine
          |                       |                       |
          +-----------------------+-----------------------+
                                  |
                          Shared AI Platform
                                  |
             +--------------------+--------------------+
             |                    |                    |
             v                    v                    v
        Evidence Engine     Scoring Engine       Pen Picture
             |                    |                    |
             +--------------------+--------------------+
                                  |
                           Human Review
                                  |
                             Final Result
```

Student-specific academic processing sits alongside the shared evidence/scoring infrastructure but has its own rules.

---

# 30. Core Boilerplate Principles

1. Build a reusable assessment engine, not a DESCO-specific application.
2. Use a framework-driven architecture.
3. Keep employee, teacher, and student assessment logic separate.
4. Treat student academic evidence as a first-class domain.
5. Calculate objective data deterministically.
6. Use AI primarily for evidence interpretation and qualitative reasoning.
7. Require structured AI output.
8. Keep AI provider/model replaceable.
9. Start as a modular monolith.
10. Use Java/Spring Boot.
11. Use Spring AI.
12. Use PostgreSQL initially.
13. Keep pgvector available for future RAG.
14. Version frameworks, rubrics, scoring rules, prompts, and models.
15. Preserve AI suggestions and human overrides.
16. Generate pen pictures after structured evaluation.
17. Build the scoring engine before the final parameter sets.
18. Do not fine-tune before sufficient human-approved data exists.
19. Do not start with microservices.
20. Do not let the LLM become the mathematical source of truth.

---

# 31. Immediate Starting Point

Before Gemini writes significant production code:

### Claude Opus should first design:

1. Final modular-monolith architecture
2. Domain boundaries
3. Entity relationships
4. Database schema
5. Scoring-engine interface
6. AI-provider abstraction
7. Structured evaluation contract
8. Evidence model
9. Human-review model
10. Pen-picture architecture
11. Student academic extension strategy
12. Testing strategy
13. Implementation sequence

But the **mathematical scoring methodology must be designed first**.

Once that methodology is stable enough to become a contract:

```text
Scoring Methodology
        ↓
Claude Architecture
        ↓
Gemini Implementation
        ↓
Working Boilerplate
```

The boilerplate should then become the technical foundation for both the future **DESCO AI appraisal system** and the **school/college LMS assessment system**.
