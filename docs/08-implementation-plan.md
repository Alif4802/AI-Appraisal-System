# 08 — Implementation Plan

## Overview

This document defines the phased implementation plan that Gemini will execute. Each phase is a self-contained unit of work with clear acceptance criteria. Phases are strictly sequential — each builds on the previous one.

**Critical Rule:** Gemini should implement one phase at a time. Do NOT attempt to implement multiple phases simultaneously. Each phase must be completed, tested, and verified before proceeding.

---

## Phase 0 — Project Foundation

### Objective
Set up the Spring Boot project skeleton with build configuration, module/package structure, database connection, Docker, and testing infrastructure.

### Scope
- Maven project with Spring Boot 4.1.x (or latest stable)
- Package structure for all modules (empty packages with `package-info.java`)
- PostgreSQL + Flyway configuration
- Docker Compose for local development (PostgreSQL)
- Testing dependencies (JUnit 5, Mockito, AssertJ, Testcontainers, ArchUnit)
- OpenAPI/Swagger configuration
- Spring Security placeholder (permit all initially)
- Application properties for dev/test profiles
- Base entity classes in `common` module
- ArchUnit architecture tests (module boundaries, no circular dependencies)

### Modules/Files
```text
pom.xml
docker-compose.yml
Dockerfile
src/main/java/com/aias/
  ├── AiAssessmentApplication.java
  ├── common/domain/BaseEntity.java
  ├── common/domain/AuditableEntity.java
  ├── common/domain/DomainEvent.java
  ├── common/exception/...
  ├── common/util/...
  ├── framework/  (empty structure)
  ├── assessment/ (empty structure)
  ├── evidence/   (empty structure)
  ├── scoring/    (empty structure)
  ├── evaluation/ (empty structure)
  ├── ai/         (empty structure)
  ├── review/     (empty structure)
  ├── penpicture/ (empty structure)
  ├── academic/   (empty structure)
  ├── audit/      (empty structure)
  └── security/   (empty structure)
src/main/resources/
  ├── application.yml
  ├── application-dev.yml
  ├── application-test.yml
  └── db/migration/  (Flyway directory)
src/test/java/com/aias/
  ├── ArchitectureTest.java
  └── AiAssessmentApplicationTest.java
```

### Database Changes
- Initial Flyway migration: empty schema (just verify Flyway runs)

### Tests
- Application context loads
- ArchUnit tests pass (module boundary rules)
- Docker Compose starts PostgreSQL
- Flyway migration runs successfully

### Acceptance Criteria
- [x] `mvn clean test` passes
- [x] Application starts with Docker Compose PostgreSQL
- [x] ArchUnit enforces module boundaries
- [x] All module packages exist
- [x] OpenAPI/Swagger UI accessible

### Dependencies
None

### Non-Goals
- No domain logic
- No API endpoints
- No real database tables

---

## Phase 1 — Framework & Versioning Module

### Objective
Implement the framework, parameter, rubric, weight, and version management with full versioning support.

### Scope
- `AssessmentFramework` entity, repository, service, and API
- `FrameworkVersion` entity with lifecycle (DRAFT → ACTIVE → SUPERSEDED)
- `ParameterDefinition` entity with rubric levels, scoring strategy config, evidence rules
- `ParameterWeight` entity with sum-to-1.0 validation
- `RatingScale` and `RatingMapping` entities
- Framework version activation (freeze/snapshot behavior)
- REST APIs for all framework operations
- Flyway migrations for framework tables

### Domain Concepts
- `AssessmentFramework`, `FrameworkVersion`, `ParameterDefinition`, `RubricLevel`, `ParameterWeight`, `RatingScale`, `RatingMapping`, `ScoringStrategyType`, `EvidenceRule`, `EvaluationDimension`, `ScoringStrategyConfig`, `MissingDataPolicyType`, `DomainType`

### APIs
- `POST/GET /api/v1/frameworks`
- `POST/GET /api/v1/frameworks/{id}/versions`
- `POST /api/v1/frameworks/{id}/versions/{vid}/activate`
- `POST/GET/PUT/DELETE /api/v1/frameworks/{id}/versions/{vid}/parameters`
- `PUT/GET /api/v1/frameworks/{id}/versions/{vid}/weights`
- `PUT/GET /api/v1/frameworks/{id}/versions/{vid}/rating-scale`

### Database Changes
Flyway migrations for:
- `assessment_framework`
- `framework_version`
- `parameter_definition`
- `rubric_level`
- `parameter_weight`
- `evidence_rule`
- `rating_scale`
- `rating_mapping`

### Tests
- Unit: Framework creation, version lifecycle, parameter validation, weight sum validation, rubric level ordering, activation freezing
- Integration: Repository CRUD, API endpoints, Flyway migrations
- ArchUnit: Framework module boundaries

### Acceptance Criteria
- [x] Can create a framework with parameters, rubrics, and weights via API
- [x] Can activate a version (becomes immutable)
- [x] Cannot modify an activated version
- [x] Weights validated to sum to 1.0
- [x] Rubric levels properly ordered with canonical score ranges
- [x] Multiple framework versions can coexist
- [x] Different domain types (EMPLOYEE, TEACHER, STUDENT) supported

### Dependencies
Phase 0

### Non-Goals
- No assessment creation
- No evidence management
- No scoring execution

---

## Phase 2 — Assessment Lifecycle Module

### Objective
Implement the assessment lifecycle, subject management, and status transitions.

### Scope
- `Assessment` entity with lifecycle state machine
- `AssessmentSubject` value object
- `AssessmentPeriod` value object
- Status transitions with validation
- Assessment creation linked to a specific `FrameworkVersion`
- REST APIs for assessment lifecycle
- Flyway migration for assessment table

### Domain Concepts
- `Assessment`, `AssessmentSubject`, `AssessmentPeriod`, `AssessmentStatus`, `SubjectType`

### APIs
- `POST/GET /api/v1/assessments`
- `GET /api/v1/assessments/{id}`
- `PATCH /api/v1/assessments/{id}/status`

### Database Changes
- `assessment` table

### Tests
- Unit: Status transitions (valid and invalid), assessment creation validation
- Integration: Repository, API, Flyway
- State machine: All valid transitions pass; invalid transitions throw exceptions

### Acceptance Criteria
- [x] Can create assessment linked to active framework version
- [x] Cannot create assessment with DRAFT or ARCHIVED framework
- [x] Status transitions follow the defined state machine
- [x] Invalid transitions rejected with clear errors
- [x] Assessment period validation (start < end)

### Dependencies
Phase 1

### Non-Goals
- No evidence submission
- No evaluation execution

---

## Phase 3 — Evidence Module

### Objective
Implement evidence submission, provenance tracking, parameter mapping, and sufficiency evaluation.

### Scope
- `EvidenceItem` entity with provenance (source type, reliability, verification status)
- `EvidenceParameterMapping` entity (many-to-many with relationship types)
- Evidence submission API (single and batch)
- Evidence sufficiency evaluation based on framework rules
- Evidence verification status management
- `EvidenceSet` assembly (transient domain service)
- Flyway migrations for evidence tables

### Domain Concepts
- `EvidenceItem`, `EvidenceSource`, `EvidenceSourceType`, `EvidenceReliability`, `EvidenceVerificationStatus`, `EvidenceParameterMapping`, `EvidenceMappingType`, `EvidenceSufficiency`, `EvidenceSet`, `EvidenceContentType`

### APIs
- `POST /api/v1/assessments/{id}/evidence` (single)
- `POST /api/v1/assessments/{id}/evidence/batch`
- `GET /api/v1/assessments/{id}/evidence`
- `GET /api/v1/assessments/{id}/evidence/{eid}`
- `POST /api/v1/assessments/{id}/evidence/{eid}/mappings`
- `GET /api/v1/assessments/{id}/evidence/sufficiency`
- `PATCH /api/v1/assessments/{id}/evidence/{eid}/verification`

### Database Changes
- `evidence_item` table
- `evidence_parameter_mapping` table

### Tests
- Unit: Evidence creation, mapping validation, sufficiency evaluation against rules
- Integration: Repository, API, batch submission, sufficiency query

### Acceptance Criteria
- [x] Can submit evidence with source type and provenance
- [x] Can map evidence to parameters with relationship types (PRIMARY, SUPPORTING, etc.)
- [x] Sufficiency evaluation respects framework evidence rules
- [x] Batch submission works
- [x] Verification status can be updated
- [x] Evidence filtered by assessment and parameter

### Dependencies
Phase 2

### Non-Goals
- No AI-assisted evidence mapping
- No evaluation execution

---

## Phase 4 — Deterministic Scoring Core

### Objective
Implement the scoring engine with all five strategy types, weighting, rating conversion, and missing-data policies.

### Scope
- `ScoringStrategy` interface
- `ObjectiveScoringStrategy` implementation
- `QualitativeRubricScoringStrategy` implementation
- `HybridScoringStrategy` implementation
- `DerivedScoringStrategy` implementation
- `CompositeScoringStrategy` implementation
- `ScoringStrategyRegistry` (strategy resolution)
- `ScoringEngine` service (overall calculation orchestration)
- `ParameterResult` and `AssessmentResult` entities
- Canonical score normalization
- Weighted aggregation
- Rating conversion
- Missing-data policy application
- Rounding rules
- Flyway migrations for result tables

### Domain Concepts
- `ScoringStrategy`, `ScoringContext`, `ParameterScoreResult`, `ParameterResult`, `AssessmentResult`, `CanonicalScore`, `WeightedScore`, `MissingDataPolicy`, `RatingConversion`, `ResultStatus`, `CalculationMetadata`

### APIs
- `GET /api/v1/assessments/{id}/results`
- `GET /api/v1/assessments/{id}/results/latest`
- `POST /api/v1/assessments/{id}/results/recalculate`

### Database Changes
- `assessment_result` table
- `parameter_result` table

### Tests
**This is the most test-intensive phase.** Implement ALL scoring unit tests from 07-testing-strategy.md:
- Score normalization tests
- Weighting tests
- Rounding tests
- Boundary tests
- Missing evidence tests
- N/A evidence tests
- Composite score tests
- Hybrid score tests
- Rating conversion tests
- Consistency calculation tests
- Growth calculation tests
- Override/recalculation tests
- Reproducibility tests
- Integration tests for result persistence

### Acceptance Criteria
- [x] All five scoring strategies implemented and independently testable
- [x] Scoring engine produces deterministic results
- [x] Same inputs always produce same outputs (reproducibility test)
- [x] Weighted aggregation correct with BigDecimal precision
- [x] Missing-data policies work correctly (all 5 policies)
- [x] Rating conversion works with configurable scales
- [x] Result versioning works (recalculation creates new version)
- [x] > 50 unit tests for scoring logic pass

### Dependencies
Phase 3

### Non-Goals
- No AI evaluation (use mock/test data for AI scores)
- No human review
- No pen pictures

---

## Phase 5 — AI Provider Abstraction

### Objective
Implement the AI gateway port, Spring AI adapter, prompt template management, and structured output mapping.

### Scope
- `AiGateway` port interface
- `SpringAiGatewayAdapter` implementing the port
- Provider configuration (application.yml)
- `PromptTemplate` entity with versioning
- Structured output DTOs (`AiParameterEvaluationResponse`, `PenPictureAiResponse`)
- AI model metadata capture
- Prompt template REST API
- Flyway migration for prompt_template table

### Domain Concepts
- `AiGateway`, `AiRequest`, `AiResponse`, `PromptTemplate`, `PromptVersion`, `AiProviderType`, `ModelConfig`, `AiModelMetadata`

### APIs
- `POST/GET /api/v1/admin/prompt-templates`
- `GET /api/v1/admin/ai-providers`

### Database Changes
- `prompt_template` table

### Tests
- Unit: DTO validation, prompt template versioning
- Integration: Spring AI adapter with mock/test provider (or WireMock)
- Provider independence: Verify domain layer has zero Spring AI imports

### Acceptance Criteria
- [x] AiGateway interface defined in domain; no Spring AI in domain
- [x] SpringAiGatewayAdapter calls Spring AI ChatClient
- [x] Structured output DTOs validated via Jakarta Validation
- [x] Prompt templates stored and versioned in database
- [x] Model metadata captured on every AI call
- [x] Provider configurable via application.yml
- [x] ArchUnit confirms scoring/domain doesn't import AI

### Dependencies
Phase 4

### Non-Goals
- No full evaluation pipeline yet
- No pen picture generation yet

---

## Phase 6 — AI Evaluation Pipeline

### Objective
Implement the complete evaluation pipeline: context assembly → AI call → validation → scoring → result storage.

### Scope
- `EvaluationRun` entity and lifecycle
- `AiParameterEvaluation` entity (immutable AI results)
- Evaluation context assembly (evidence sets + rubrics + objective facts)
- Validation pipeline (schema + domain + grounding)
- Retry/failure behavior
- Evaluation orchestration service
- Assessment status transition: EVALUATION_READY → EVALUATING → EVALUATED
- Flyway migrations for evaluation tables

### Domain Concepts
- `EvaluationRun`, `EvaluationStatus`, `EvaluationContext`, `AiParameterEvaluation`, `EvaluationRequest`

### APIs
- `POST /api/v1/assessments/{id}/evaluate`
- `GET /api/v1/assessments/{id}/evaluations`
- `GET /api/v1/assessments/{id}/evaluations/{runId}`
- `GET /api/v1/assessments/{id}/evaluations/latest`

### Database Changes
- `evaluation_run` table
- `ai_parameter_evaluation` table

### Tests
- Unit: Context assembly, validation pipeline, grounding checks
- Integration: Full pipeline with mocked AI gateway
- AI tests: Structured output validity, rubric adherence (with real AI provider if available)
- Error handling: Retry on validation failure, fallback on provider error

### Acceptance Criteria
- [x] Full pipeline: evidence → AI evaluation → scoring → result
- [x] AI responses validated against schema and domain rules
- [x] Evidence grounding checked (referenced evidence exists)
- [x] OBJECTIVE parameters skip AI, scored directly
- [x] QUALITATIVE parameters scored via AI
- [x] HYBRID parameters combine objective + AI
- [x] EvaluationRun records all metadata (model, prompt, timestamps)
- [x] Retry on validation failure (up to configured max)
- [x] Partial completion on per-parameter failures

### Dependencies
Phase 5

### Non-Goals
- No human review yet
- No pen pictures yet

---

## Phase 7 — Human Review Module

### Objective
Implement the human review workflow with parameter-level approve/modify/return and deterministic recalculation.

### Scope
- `ReviewSession` entity and lifecycle
- `ParameterReview` entity (per-parameter decision)
- `OverallOverride` entity (exceptional case)
- Review workflow: approve, modify (with mandatory reason), return
- Score recalculation after parameter overrides
- Assessment status transitions: EVALUATED → UNDER_REVIEW → APPROVED (or RETURNED)
- Flyway migrations for review tables

### Domain Concepts
- `ReviewSession`, `ParameterReview`, `ReviewAction`, `OverrideRecord`, `ReviewerInfo`, `OverallOverride`, `ReviewStatus`

### APIs
- `POST /api/v1/assessments/{id}/reviews`
- `GET /api/v1/assessments/{id}/reviews`
- `PUT /api/v1/assessments/{id}/reviews/{rid}/parameters/{paramCode}`
- `POST /api/v1/assessments/{id}/reviews/{rid}/complete`
- `POST /api/v1/assessments/{id}/reviews/{rid}/return`
- `POST /api/v1/assessments/{id}/reviews/{rid}/overall-override`

### Database Changes
- `review_session` table
- `parameter_review` table
- `overall_override` table

### Tests
- Unit: Review actions, mandatory override reason, recalculation after override
- Integration: Full review workflow, API endpoints
- Override: AI suggestion preserved, human score replaces, overall recalculated

### Acceptance Criteria
- [x] Reviewer can approve each parameter's AI-suggested score
- [x] Reviewer can modify a parameter score with mandatory reason
- [x] Overall score recalculated deterministically after parameter overrides
- [x] AI suggested score NEVER erased by human review
- [x] Return sends assessment back to EVIDENCE_COLLECTION or EVALUATION_READY
- [x] Overall override requires SENIOR_REVIEWER permission and mandatory justification
- [x] Review session history preserved (append-only)

### Dependencies
Phase 6

### Non-Goals
- No pen pictures yet
- No full security enforcement yet

---

## Phase 8 — Pen Picture Module

### Objective
Implement domain-specific pen picture generation using AI based on structured assessment data.

### Scope
- `PenPicture` entity with versioning
- Pen picture generation using approved scores, strengths, development areas, evidence summaries
- Domain-specific generation templates (Employee, Teacher, Student)
- PenPicture AI request/response DTOs
- Assessment status transition: APPROVED → FINALIZED (after pen picture)
- Flyway migration for pen_picture table

### Domain Concepts
- `PenPicture`, `PenPictureRequest`, `PenPictureTemplate`, `DomainPenPictureInstructions`, `PenPictureGenerationContext`

### APIs
- `POST /api/v1/assessments/{id}/pen-pictures`
- `GET /api/v1/assessments/{id}/pen-pictures`
- `GET /api/v1/assessments/{id}/pen-pictures/latest`
- `POST /api/v1/assessments/{id}/pen-pictures/{ppId}/regenerate`

### Database Changes
- `pen_picture` table

### Tests
- Unit: Request assembly, template resolution
- Integration: Generation with mocked AI, storage, versioning

### Acceptance Criteria
- [x] Pen picture generated from structured assessment data (not independently)
- [x] Domain-specific templates used (Employee vs. Teacher vs. Student)
- [x] Generated text based on approved scores, strengths, development areas
- [x] Model metadata captured
- [x] Versioning: regeneration creates new version
- [x] Assessment finalization after pen picture

### Dependencies
Phase 7

### Non-Goals
- No student academic extension yet

---

## Phase 9 — Student Academic Extension

### Objective
Implement the student-specific academic data layer with structured academic records, deterministic academic calculations, and integration with the assessment/scoring pipeline.

### Scope
- `AcademicRecord` entity
- `SubjectEnrollment` entity
- `AcademicResult` entity (exam, quiz, assignment, etc.)
- `AttendanceRecord` entity
- Deterministic calculations: subject performance, weighted academic score, growth, trend, consistency
- Academic facts → evidence bridge (converting calculated facts into structured evidence)
- Academic data ingestion APIs (single + batch)
- Student-specific scoring strategies (COMPOSITE for academic performance)
- Flyway migrations for academic tables

### Domain Concepts
- `AcademicRecord`, `SubjectEnrollment`, `AcademicResult`, `AcademicAssessmentType`, `AttendanceRecord`, `SubjectPerformance`, `AcademicGrowth`, `AcademicTrend`, `AcademicConsistency`, `CalculatedAcademicFacts`

### APIs
- `POST/GET /api/v1/assessments/{id}/academic-record`
- `POST /api/v1/assessments/{id}/academic-record/subjects`
- `POST /api/v1/assessments/{id}/academic-record/subjects/{sid}/results`
- `POST /api/v1/assessments/{id}/academic-record/subjects/{sid}/results/batch`
- `POST /api/v1/assessments/{id}/academic-record/attendance`
- `POST /api/v1/assessments/{id}/academic-record/attendance/batch`
- `GET /api/v1/assessments/{id}/academic-record/calculated-facts`

### Database Changes
- `academic_record` table
- `subject_enrollment` table
- `academic_result` table
- `attendance_record` table

### Tests
- Unit: Academic calculations (weighted averages, growth, trend, consistency, attendance)
- Unit: Student-specific composite scoring
- Integration: Full student evaluation pipeline (academic data → calculation → AI interpretation → scoring → result)
- Academic calculation: Verify Example from R&D handoff:
  ```
  87×0.4 + 82×0.2 + 91×0.15 + 85×0.1 + 88×0.15 = 86.35
  ```

### Acceptance Criteria
- [x] Student academic records with structured data (not free-text)
- [x] All academic assessment types supported (EXAM, MIDTERM, QUIZ, etc.)
- [x] Deterministic academic calculations correct
- [x] Growth calculated: absolute and percentage
- [x] Trend detected: IMPROVING, STABLE, DECLINING
- [x] Consistency calculated
- [x] Subject-level strengths and weaknesses identified
- [x] Calculated facts available as structured input to AI evaluation
- [x] Full student assessment pipeline works end-to-end

### Dependencies
Phase 8

### Non-Goals
- No final LMS parameter definitions
- No real LMS data integration

---

## Phase 10 — Audit & Security Hardening

### Objective
Implement comprehensive audit logging, RBAC, and security controls.

### Scope
- `AuditEvent` entity and recording
- Domain event → audit event translation
- Audit query API
- Spring Security RBAC configuration
- Role definitions: ADMIN, FRAMEWORK_MANAGER, ASSESSOR, REVIEWER, SENIOR_REVIEWER, AUDITOR
- Assessment ownership enforcement
- Reviewer permission validation
- API credential management (AI provider keys)
- Institution isolation enforcement
- Flyway migration for audit_event table

### Domain Concepts
- `AuditEvent`, `AuditEventType`, `AuditContext`, `Role`, `Permission`, `UserPrincipal`, `InstitutionContext`

### APIs
- `GET /api/v1/audit/events`
- `GET /api/v1/audit/events/{id}`
- `GET /api/v1/audit/entities/{type}/{id}`
- All existing APIs now enforced with proper roles

### Database Changes
- `audit_event` table

### Tests
- Unit: Audit event creation, role checks
- Integration: End-to-end audit trail for assessment lifecycle
- Security: Unauthorized access blocked; wrong institution blocked; insufficient role blocked

### Acceptance Criteria
- [x] Every significant action generates an audit event
- [x] Assessment lifecycle fully auditable
- [x] RBAC enforced on all APIs
- [x] Institution isolation enforced (cannot access other institution's data)
- [x] AI provider credentials secured
- [x] Audit query API functional

### Dependencies
Phase 9

### Non-Goals
- No OAuth2/SSO integration (placeholder JWT)
- No full production security review

---

## Phase 11 — Benchmarking & Evaluation Dataset

### Objective
Create the benchmark dataset and evaluation infrastructure for testing AI quality and scoring correctness.

### Scope
- Benchmark case data files (JSON) for Employee, Teacher, Student
- Benchmark runner infrastructure
- Score range adherence metrics
- Evidence grounding metrics
- Hallucination detection metrics
- AI evaluation regression baseline
- Scoring regression baseline

### Files
```text
src/test/resources/benchmark/
├── employee/
│   ├── strong-performer.json
│   ├── average-performer.json
│   ├── weak-performer.json
│   ├── insufficient-evidence.json
│   ├── contradictory-evidence.json
│   ├── improving-performer.json
│   └── self-reported-heavy.json
├── teacher/
│   ├── effective-teacher.json
│   ├── average-teacher.json
│   └── mixed-evidence.json
└── student/
    ├── high-achiever.json
    ├── average-student.json
    ├── struggling-student.json
    ├── improving-student.json
    └── inconsistent-student.json
```

### Tests
- Benchmark suite (run against live AI provider if configured; skip otherwise)
- Scoring regression suite
- Reproducibility verification

### Acceptance Criteria
- [x] Benchmark dataset with ≥15 representative cases
- [x] Benchmark runner executes all cases and produces metrics report
- [x] Scoring regression tests verify deterministic reproducibility
- [x] Baseline results recorded for future regression comparison

### Dependencies
Phase 10

### Non-Goals
- No production deployment
- No real institutional data

---

## Phase 12 — RAG Extension (Future)

### Objective
Add retrieval-augmented generation using PostgreSQL + pgvector.

### Scope
- pgvector extension enabled
- Knowledge embedding table
- `KnowledgeRetriever` port interface
- `PgVectorKnowledgeRetriever` adapter
- Evaluation context enrichment with retrieved knowledge
- Prompt template extension for retrieved context
- Embedding generation pipeline

### Dependencies
Phase 11

### Non-Goals
- Fine-tuning
- External vector database

> **Note:** This phase is explicitly deferred. The architecture provides the extension point; implementation will occur after Phases 0–11 are stable.

---

## Phase Dependency Graph

```text
Phase 0  (Foundation)
    │
    ▼
Phase 1  (Framework/Versioning)
    │
    ▼
Phase 2  (Assessment Lifecycle)
    │
    ▼
Phase 3  (Evidence)
    │
    ▼
Phase 4  (Scoring Engine)     ←── Most test-intensive phase
    │
    ▼
Phase 5  (AI Abstraction)
    │
    ▼
Phase 6  (AI Evaluation Pipeline)
    │
    ▼
Phase 7  (Human Review)
    │
    ▼
Phase 8  (Pen Picture)
    │
    ▼
Phase 9  (Student Academic)
    │
    ▼
Phase 10 (Audit/Security)
    │
    ▼
Phase 11 (Benchmarking)
    │
    ▼
Phase 12 (RAG - Future)
```

---

## Implementation Notes for Gemini

1. **Read the architecture documents** (01 through 07) before starting any phase. They are the source of truth.

2. **One phase at a time.** Complete Phase N, verify all acceptance criteria, then proceed to Phase N+1.

3. **Tests are not optional.** Every phase specifies required tests. Write tests alongside production code.

4. **Use the defined package structure.** Do not reorganize modules without explicit architectural approval.

5. **BigDecimal everywhere for scores.** Never use `double` or `float` for financial/score calculations.

6. **Domain layer is framework-free.** Domain model classes in `domain/model/` must not import Spring annotations (except for JPA/validation where necessary for persistence).

7. **Mocked AI during scoring tests.** Phases 1–4 should use test fixtures for AI scores. Do not require a live AI provider for scoring engine tests.

8. **Flyway migrations are sequential.** Use naming convention: `V1__create_framework_tables.sql`, `V2__create_assessment_table.sql`, etc.

9. **ArchUnit tests must pass at every phase.** Run architecture tests after every module addition.

10. **Refer to scoring engine document (06) for scoring policies marked `SCORING POLICY TO BE FINALIZED`.** Implement reasonable defaults with clear documentation. Do not invent arbitrary formulas.
