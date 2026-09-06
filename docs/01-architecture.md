# 01 — Architecture

## 1. Architecture Goals

| # | Goal | Rationale |
|---|------|-----------|
| G1 | **Configurable assessment engine** | Must support Employee, Teacher, Student, and future domains without core changes |
| G2 | **Evidence-based, AI-assisted scoring** | AI interprets qualitative evidence; application owns deterministic math |
| G3 | **Deterministic reproducibility** | Any historical assessment must be reproducible given the same versioned inputs |
| G4 | **Provider-independent AI** | Must switch LLM providers (Gemini → Claude → Qwen → self-hosted) without domain changes |
| G5 | **Human authority** | AI suggests; humans approve; both records are preserved |
| G6 | **Domain separation** | Employee, Teacher, Student engines share infrastructure but keep domain-specific scoring rules |
| G7 | **Phased extractability** | Modular monolith today; cleanly extractable into services later if needed |
| G8 | **High testability** | Deterministic scoring must be 100% unit-testable; AI evaluation must be structurally validatable |
| G9 | **Institutional readiness** | Multi-tenancy, versioning, audit, and security baked in from the start |

---

## 2. Architectural Principles

| # | Principle |
|---|-----------|
| P1 | **Framework-driven, not hard-coded** — No parameter names, rubrics, or weights live in source code. Everything is configuration data versioned in the database. |
| P2 | **Standardized result contract, not standardized algorithm** — Every domain engine produces a `ParameterResult` and an `AssessmentResult` conforming to a shared contract. The internal scoring strategy differs per parameter type. |
| P3 | **Canonical mathematical score** — Internal calculations use a normalized `BigDecimal` score (0–100, scale 4). Display ratings (1–5, A–F, labels) are derived mappings, never the math foundation. |
| P4 | **Structured AI output** — AI responses are validated Java DTOs, never free-form prose that requires parsing. |
| P5 | **Immutable evaluation history** — Evaluation runs, AI suggestions, and human reviews are append-only. Nothing is silently mutated. |
| P6 | **Dependency flows inward** — Domain/application layers never depend on infrastructure/framework specifics. Infrastructure adapts to domain ports. |
| P7 | **Module boundaries enforced by packages** — Each module owns its `domain`, `application`, `api`, and `infrastructure` packages. Cross-module access is through published interfaces only. |
| P8 | **No circular module dependencies** — The dependency graph is a DAG; violations are compilation errors. |
| P9 | **Missing evidence ≠ poor performance** — The system distinguishes N/A, insufficient, unverified, and genuinely poor. |
| P10 | **Version everything that can affect a score** — Framework, parameter, rubric, weight, scoring rule, prompt template, AI model metadata. |

---

## 3. High-Level Architecture Diagram

```text
┌───────────────────────────────────────────────────────────────────┐
│                        API Layer (REST)                           │
│  Framework API │ Assessment API │ Evidence API │ Review API │ ... │
└───────┬───────────────┬──────────────┬──────────────┬────────────┘
        │               │              │              │
┌───────▼───────────────▼──────────────▼──────────────▼────────────┐
│                   Application Services                           │
│  (Use cases / orchestration — no domain logic here)              │
└───────┬───────────────┬──────────────┬──────────────┬────────────┘
        │               │              │              │
┌───────▼───────────────▼──────────────▼──────────────▼────────────┐
│                     Domain Layer                                 │
│                                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │Framework │  │Assessment│  │ Evidence │  │ Scoring  │         │
│  │ Module   │  │  Module  │  │  Module  │  │  Engine  │         │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │
│                                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │Evaluation│  │  Review  │  │PenPicture│  │ Academic │         │
│  │ Module   │  │  Module  │  │  Module  │  │  Module  │         │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │
│                                                                  │
│  ┌──────────┐  ┌──────────┐                                      │
│  │  Audit   │  │ Security │                                      │
│  │ Module   │  │  Module  │                                      │
│  └──────────┘  └──────────┘                                      │
└───────┬───────────────┬──────────────┬──────────────┬────────────┘
        │               │              │              │
┌───────▼───────────────▼──────────────▼──────────────▼────────────┐
│                   Infrastructure Layer                            │
│                                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │   JPA    │  │ AI Gate- │  │  Flyway  │  │ Spring   │         │
│  │ Repos    │  │   way    │  │Migration │  │ Security │         │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │
│                                                                  │
│  ┌──────────┐  ┌──────────┐                                      │
│  │  Docker  │  │Observa-  │                                      │
│  │          │  │bility    │                                      │
│  └──────────┘  └──────────┘                                      │
└──────────────────────────────────────────────────────────────────┘
```

### Domain Engine Separation

```text
                    AI Assessment Platform
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
     Employee           Teacher            Student
      Engine             Engine             Engine
   (strategy +        (strategy +        (strategy +
    config data)       config data)       academic module)
        │                  │                  │
        └──────────── Shared Infrastructure ──┘
              │               │              │
        Scoring Engine   AI Gateway    Evidence Infra
        Review Infra     Pen Picture   Audit / Security
```

Domain engines are NOT separate Spring modules or Maven sub-projects.
They are **configuration data** (frameworks, parameters, rubrics, weights, scoring-strategy registrations) combined with **strategy implementations** registered with the shared Scoring Engine.

The Student engine additionally activates the **Academic Module** for structured academic evidence and deterministic academic calculations.

---

## 4. Module Catalogue

### 4.1 `framework` — Framework & Configuration Management

**Responsibilities:**
- Assessment framework CRUD and versioning
- Parameter definition and versioning
- Rubric definition and versioning
- Parameter weight configuration and versioning
- Scoring strategy assignment per parameter
- Evidence eligibility rules per parameter
- Rating/grade mapping configuration
- Framework version snapshots (immutable once activated)

**Domain Concepts:**
- `AssessmentFramework`, `FrameworkVersion`, `ParameterDefinition`, `ParameterVersion`, `Rubric`, `RubricLevel`, `ParameterWeight`, `ScoringStrategyConfig`, `EvidenceRule`, `RatingScale`, `RatingMapping`

**Dependencies:** `common`

**Does NOT own:** Actual scoring execution, evidence storage, assessment lifecycle.

---

### 4.2 `assessment` — Assessment Lifecycle

**Responsibilities:**
- Assessment creation (linking a subject to a framework version and assessment period)
- Assessment lifecycle/state management: `CREATED → EVIDENCE_COLLECTION → EVALUATING → EVALUATED → UNDER_REVIEW → APPROVED → FINALIZED`
- Assessment period management
- Subject identity (reference to external HR/LMS subject)

**Domain Concepts:**
- `Assessment`, `AssessmentSubject`, `AssessmentPeriod`, `AssessmentStatus`

**Dependencies:** `framework`, `common`

**Does NOT own:** Evidence, scoring, evaluation, review.

---

### 4.3 `evidence` — Evidence Infrastructure

**Responsibilities:**
- Evidence item storage with provenance
- Evidence source type classification
- Evidence verification status tracking
- Evidence-to-parameter mapping with relationship types
- Evidence set assembly for a parameter + assessment context
- Evidence sufficiency evaluation (rule-based, not AI)
- Temporal evidence organization

**Domain Concepts:**
- `EvidenceItem`, `EvidenceSource`, `EvidenceSourceType`, `EvidenceVerificationStatus`, `EvidenceParameterMapping`, `EvidenceMappingType`, `EvidenceSet`, `EvidenceSufficiency`

**Dependencies:** `assessment`, `framework`, `common`

**Does NOT own:** Evidence interpretation (that's AI), scoring, academic record structure.

---

### 4.4 `scoring` — Deterministic Scoring Engine

**Responsibilities:**
- Parameter-level scoring execution via pluggable strategies
- Canonical score normalization (0–100 BigDecimal scale 4)
- Weighted score aggregation
- Composite score calculation
- Derived score calculation
- Missing-evidence policy application
- Rating/grade conversion from canonical scores
- Final assessment score calculation
- Human-approved-score recalculation (when override changes propagate)
- Rounding rules
- Validation of scoring inputs/outputs
- All deterministic arithmetic

**Domain Concepts:**
- `ParameterResult`, `AssessmentResult`, `CanonicalScore`, `WeightedScore`, `ScoringStrategy` (interface), `ObjectiveScoringStrategy`, `QualitativeRubricScoringStrategy`, `HybridScoringStrategy`, `DerivedScoringStrategy`, `CompositeScoringStrategy`, `MissingDataPolicy`, `RatingConversion`, `ScoreCalculation`

**Dependencies:** `framework`, `evidence`, `common`

**Does NOT own:** AI evaluation, evidence storage, human review decisions, pen pictures.

---

### 4.5 `evaluation` — AI Evaluation Pipeline

**Responsibilities:**
- Orchestrating AI evaluation requests for an assessment
- Assembling evaluation context (evidence sets, rubrics, calculated objective facts)
- Dispatching to AI gateway
- Receiving and validating structured AI responses
- Storing AI evaluation results immutably
- Recording AI model metadata per evaluation run
- Retry/failure handling

**Domain Concepts:**
- `EvaluationRun`, `EvaluationRequest`, `EvaluationContext`, `AiParameterEvaluation`, `AiEvaluationResult`, `EvaluationStatus`, `AiModelMetadata`

**Dependencies:** `assessment`, `evidence`, `framework`, `ai` (port), `scoring` (for objective pre-calculations), `common`

**Does NOT own:** AI provider implementation, scoring execution, human review.

---

### 4.6 `ai` — AI Gateway & Provider Abstraction

**Responsibilities:**
- Application-owned AI gateway interface (port)
- Spring AI integration (adapter)
- Provider configuration and selection
- Prompt template management and versioning
- Structured output mapping and validation
- Token/cost tracking metadata
- Model metadata capture
- Future RAG extension point

**Domain Concepts:**
- `AiGateway` (port interface), `AiRequest`, `AiResponse`, `PromptTemplate`, `PromptVersion`, `ModelConfig`, `AiProviderType`

**Dependencies:** `common`

**Does NOT own:** Evaluation orchestration, scoring, domain logic. This module is pure infrastructure/integration.

---

### 4.7 `review` — Human Review & Approval

**Responsibilities:**
- Human review workflow management
- Parameter-level approve/modify/return actions
- Override recording with mandatory justification
- Reviewer assignment and permissions
- Review history (append-only)
- Overall-result override (exceptional, permission-controlled)
- Triggering score recalculation after parameter-level overrides

**Domain Concepts:**
- `ReviewSession`, `ParameterReview`, `ReviewAction` (`APPROVED`, `MODIFIED`, `RETURNED`), `OverrideRecord`, `ReviewerInfo`, `OverallOverride`

**Dependencies:** `assessment`, `evaluation`, `scoring`, `framework`, `common`

**Does NOT own:** AI evaluation execution, evidence management, pen-picture generation.

---

### 4.8 `penpicture` — Pen Picture Generation

**Responsibilities:**
- Generating structured pen pictures after evaluation + review
- Domain-specific generation templates/instructions
- Using approved scores, strengths, development areas, trends, and evidence summaries
- Pen picture versioning and storage

**Domain Concepts:**
- `PenPicture`, `PenPictureRequest`, `PenPictureTemplate`, `DomainPenPictureInstructions`

**Dependencies:** `assessment`, `evaluation`, `scoring`, `review`, `ai` (port), `framework`, `common`

**Does NOT own:** Scoring, evidence storage, review decisions.

---

### 4.9 `academic` — Student Academic Extension

**Responsibilities:**
- Structured academic record storage (exams, quizzes, assignments, classwork, projects, practicals, labs)
- Subject-level result management
- Deterministic academic metric calculations: subject performance, weighted academic score, growth, trend, consistency
- Academic evidence → parameter evidence bridge (converting calculated academic facts into structured evidence for the scoring engine)
- Attendance metric calculation

**Domain Concepts:**
- `AcademicRecord`, `SubjectResult`, `AcademicAssessmentType` (`EXAM`, `MIDTERM`, `CLASS_TEST`, `QUIZ`, `ASSIGNMENT`, `CLASSWORK`, `PROJECT`, `PRACTICAL`, `LAB`), `SubjectPerformance`, `AcademicGrowth`, `AcademicTrend`, `AcademicConsistency`, `AttendanceRecord`

**Dependencies:** `assessment`, `evidence`, `framework`, `scoring`, `common`

**Does NOT own:** Generic evidence interpretation (AI), non-academic scoring strategies, employee/teacher logic.

---

### 4.10 `audit` — Audit & Traceability

**Responsibilities:**
- Immutable audit event recording
- Assessment action logging
- Score change tracking
- Configuration change tracking
- AI evaluation metadata archival
- Human override logging
- Query API for audit trails

**Domain Concepts:**
- `AuditEvent`, `AuditEventType`, `AuditContext`, `AuditQuery`

**Dependencies:** `common` (and receives events from all other modules)

**Does NOT own:** Business logic of any kind.

---

### 4.11 `security` — Security & Authorization

**Responsibilities:**
- Authentication integration (Spring Security)
- RBAC/authorization model
- Role definitions: `ADMIN`, `FRAMEWORK_MANAGER`, `ASSESSOR`, `REVIEWER`, `SENIOR_REVIEWER`, `SUBJECT` (the assessed person), `AUDITOR`
- Assessment ownership enforcement
- Reviewer permission validation
- Sensitive data access controls
- API credential management (AI provider keys)
- Multi-tenancy readiness (institutional isolation via `institution_id`)

**Domain Concepts:**
- `UserPrincipal`, `Role`, `Permission`, `InstitutionContext`

**Dependencies:** `common`

**Does NOT own:** Business logic, assessment workflow.

---

### 4.12 `common` — Shared Kernel

**Responsibilities:**
- Base entity/value object abstractions
- Shared domain primitives (`InstitutionId`, `UserId`, `Money`, `Percentage`, etc.)
- Domain event infrastructure
- Validation utilities
- Temporal utilities (assessment period helpers)
- Exception hierarchy
- Shared DTOs for cross-module communication

**Domain Concepts:**
- `BaseEntity`, `AuditableEntity`, `DomainEvent`, `EntityId`, `VersionedEntity`

**Dependencies:** None (leaf module)

**Does NOT own:** Any business logic.

---

## 5. Module Dependency Graph

```text
                    common
                   ▲  ▲  ▲
                  /   |   \
                 /    |    \
           security  audit  ai
              ▲       ▲     ▲
              │       │     │
         framework ───┘     │
           ▲  ▲             │
          /    \            │
    assessment  \           │
      ▲  ▲       \          │
     /    \     evidence     │
    /      \     ▲  ▲       │
   /        \   /    \      │
  /     scoring ◄─────┘     │
  │      ▲  ▲               │
  │     /    \              │
  │    /   academic         │
  │   /      ▲              │
  │  /       │              │
  evaluation─┘──────────────┘
      ▲
      │
    review
      ▲
      │
  penpicture
```

**No circular dependencies.** Every arrow points from a higher-level module to a lower-level dependency.

The `audit` module is special: it receives events from all modules but has no compile-time dependency on them. Modules publish `DomainEvent` instances; audit subscribes.

---

## 6. Architectural Decisions Record (ADR)

### ADR-01: Modular Monolith over Microservices

**Decision:** Start as a single deployable Spring Boot application with package-enforced module boundaries.

**Rationale:** Faster development, simpler debugging, easier database consistency, easier AI experimentation. Module boundaries remain clean enough for future extraction.

**Trade-off:** Must enforce module boundaries by convention and tooling (e.g., ArchUnit tests) rather than network isolation.

---

### ADR-02: Canonical Score as 0–100 BigDecimal(scale=4)

**Decision:** All internal score calculations use `BigDecimal` on a 0.0000–100.0000 scale.

**Rationale:** The 1–5 rubric scale is ordinal and unevenly spaced. Using it as a mathematical foundation introduces systematic errors in weighted calculations. A 0–100 normalized scale allows precise arithmetic, proper weighting, and flexible mapping to any institutional display scale.

**Trade-off:** Requires explicit rubric-level-to-canonical-score mapping configuration. Adds a mapping step but eliminates mathematical ambiguity.

---

### ADR-03: Strategy Pattern for Parameter Scoring

**Decision:** Each parameter declares a `ScoringStrategyType`. The Scoring Engine resolves the appropriate `ScoringStrategy` implementation at runtime.

**Rationale:** Employee attendance (OBJECTIVE), Teacher leadership (QUALITATIVE_RUBRIC), Student academic performance (COMPOSITE), and Learning growth (DERIVED) use fundamentally different scoring logic. A strategy pattern keeps each algorithm focused and testable while the engine remains generic.

**Trade-off:** New strategy types require implementing a new class and registering it.

---

### ADR-04: Application-Owned AI Gateway

**Decision:** The domain layer defines an `AiGateway` port interface. The `ai` module provides the Spring AI adapter implementation. Provider selection is configuration-driven.

**Rationale:** Provider independence. The domain never imports Spring AI or any provider SDK.

**Trade-off:** Slight indirection. Justified by the explicit requirement to support Gemini, Claude, Qwen, and self-hosted models.

---

### ADR-05: Immutable Framework Version Snapshots

**Decision:** When a framework version is activated, a complete snapshot of all parameters, rubrics, weights, and scoring rules is frozen. Assessments reference a specific `framework_version_id`, not the mutable head.

**Rationale:** Assessments must remain reproducible. If the 2027 rubric changes in 2028, existing 2027 assessments must still produce identical results.

**Trade-off:** Storage cost for snapshots. Acceptable for institutional-grade auditability.

---

### ADR-06: Structured AI Output via Spring AI DTO Mapping

**Decision:** AI responses are mapped to validated Java POJOs using Spring AI's structured output support. Free-form prose responses are rejected.

**Rationale:** Eliminates parsing fragility. Enables programmatic validation before scores enter the pipeline.

**Trade-off:** Requires careful prompt engineering and AI model cooperation with structured output schemas.

---

### ADR-07: Parameter-Level Human Override with Deterministic Recalculation

**Decision:** Human reviewers modify parameter-level approved scores. The overall assessment score is then deterministically recalculated from approved parameter scores. Overall-result overrides are exceptional, requiring senior reviewer permission and mandatory justification.

**Rationale:** Parameter-level overrides maintain scoring transparency. The overall score always traces back to parameter scores and weights.

**Trade-off:** Slightly more work for reviewers who want to change only the overall score. Justified by auditability requirements.

---

### ADR-08: Evidence Strength and Evaluation Confidence Are Separate

**Decision:** `EvidenceStrength` describes the quality/reliability/sufficiency of available evidence. `EvaluationConfidence` describes how well-supported the resulting evaluation is. Both are tracked independently.

**Rationale:** Strong evidence can still yield low confidence if it's contradictory. Weak evidence can be unambiguous but unreliable. Conflating them loses information.

**Trade-off:** More complex model, but more accurate representation.

---

### ADR-09: Student Academic Module as First-Class Domain

**Decision:** Student academic evidence (exams, quizzes, assignments, etc.) is stored in structured domain entities, not as generic free-text evidence. Academic calculations are deterministic.

**Rationale:** Students have substantial quantitative academic data. Flattening it into text evidence loses precision and forces AI to perform arithmetic it shouldn't.

**Trade-off:** Additional module and schema. Required by the fundamental difference between student and employee assessment.

---

### ADR-10: Multi-Tenancy via Institution ID

**Decision:** All major entities carry an `institution_id` discriminator. Initial implementation uses shared-schema multi-tenancy with row-level isolation.

**Rationale:** The platform will serve multiple institutions (DESCO, schools, colleges). Full schema-per-tenant adds operational complexity too early.

**Trade-off:** Must enforce institution isolation at every query and API boundary. Acceptable for early phases.

---

## 7. Risks and Trade-offs

| Risk | Severity | Mitigation |
|------|----------|------------|
| AI structured output reliability varies across models | High | Validate all AI responses against schema; retry with reformulated prompt on validation failure; flag for human review if retries exhaust |
| Scoring policy not finalized for all parameter types | Medium | Architecture defines strategy contracts; policy details are explicitly marked `SCORING POLICY TO BE FINALIZED`; strategies are pluggable |
| Framework version snapshots become large | Low | Snapshot only changed configurations; use copy-on-write semantics; acceptable storage cost for institutional use |
| ArchUnit-only module boundary enforcement | Medium | Enforce via CI; consider Java module system (JPMS) later if discipline proves insufficient |
| AI hallucination affecting qualitative scores | High | Evidence grounding validation; contradictory evidence detection; mandatory human review for low-confidence evaluations |
| BigDecimal performance for batch calculations | Low | Assessment scoring is per-subject, not high-frequency trading; BigDecimal performance is adequate |
| Multi-tenancy security gaps | High | Row-level security tests; integration tests with cross-institution assertions; Spring Security filters |
| Rubric-to-canonical-score mapping complexity | Medium | Provide sensible defaults; make mapping configurable per framework version |

---

## 8. Technology Stack Confirmation

| Concern | Technology | Notes |
|---------|------------|-------|
| Language | Java 25 (21-compatible) | No Java 25-only features in architecture |
| Framework | Spring Boot 4.1.x | Modular monolith |
| Build | Maven | Single-module Maven project with package-based modules |
| AI | Spring AI 2.x | Provider abstraction |
| Database | PostgreSQL | + pgvector later |
| ORM | Spring Data JPA | Repositories per module |
| Migrations | Flyway | Versioned migrations |
| Security | Spring Security | JWT/session + RBAC |
| Validation | Jakarta Validation | Bean validation |
| Serialization | Jackson | JSON |
| API | REST + OpenAPI/Swagger | Documented |
| Testing | JUnit 5 + Mockito + Testcontainers | Full pyramid |
| Containers | Docker + docker-compose | Dev + CI |
| Observability | Micrometer + OpenTelemetry | Metrics + tracing |
| Architecture tests | ArchUnit | Module boundary enforcement |

---

## 9. Package Structure

```text
com.aias/                                    (AI Assessment System)
├── common/
│   ├── domain/
│   │   ├── BaseEntity.java
│   │   ├── AuditableEntity.java
│   │   ├── DomainEvent.java
│   │   └── ...
│   ├── exception/
│   └── util/
│
├── framework/
│   ├── domain/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   ├── application/
│   │   └── service/
│   ├── api/
│   │   ├── controller/
│   │   └── dto/
│   └── infrastructure/
│       └── persistence/
│
├── assessment/
│   ├── domain/
│   ├── application/
│   ├── api/
│   └── infrastructure/
│
├── evidence/
│   ├── domain/
│   ├── application/
│   ├── api/
│   └── infrastructure/
│
├── scoring/
│   ├── domain/
│   │   ├── model/
│   │   ├── strategy/
│   │   │   ├── ScoringStrategy.java          (interface)
│   │   │   ├── ObjectiveScoringStrategy.java
│   │   │   ├── QualitativeRubricScoringStrategy.java
│   │   │   ├── HybridScoringStrategy.java
│   │   │   ├── DerivedScoringStrategy.java
│   │   │   └── CompositeScoringStrategy.java
│   │   └── policy/
│   │       └── MissingDataPolicy.java
│   ├── application/
│   └── infrastructure/
│
├── evaluation/
│   ├── domain/
│   ├── application/
│   ├── api/
│   └── infrastructure/
│
├── ai/
│   ├── gateway/
│   │   └── AiGateway.java                   (port interface)
│   ├── prompt/
│   ├── structured/
│   ├── provider/
│   │   └── springai/                         (Spring AI adapter)
│   └── config/
│
├── review/
│   ├── domain/
│   ├── application/
│   ├── api/
│   └── infrastructure/
│
├── penpicture/
│   ├── domain/
│   ├── application/
│   ├── api/
│   └── infrastructure/
│
├── academic/
│   ├── domain/
│   ├── application/
│   ├── api/
│   └── infrastructure/
│
├── audit/
│   ├── domain/
│   ├── application/
│   ├── api/
│   └── infrastructure/
│
├── security/
│   ├── domain/
│   ├── config/
│   └── filter/
│
└── AiAssessmentApplication.java
```

---

## 10. Cross-Cutting Concerns

### 10.1 Domain Events

Modules communicate asynchronously through domain events published via Spring's `ApplicationEventPublisher`:

- `AssessmentCreatedEvent`
- `EvidenceSubmittedEvent`
- `EvaluationCompletedEvent`
- `ReviewCompletedEvent`
- `ScoreRecalculatedEvent`
- `PenPictureGeneratedEvent`
- `FrameworkVersionActivatedEvent`

The audit module subscribes to all events. Other modules subscribe selectively.

### 10.2 Transaction Boundaries

Each application service method defines its own transaction boundary. Cross-module operations that must be atomic use the orchestrating application service's transaction. Events that trigger actions in other modules use eventual consistency via `@TransactionalEventListener`.

### 10.3 Observability

- All AI gateway calls are instrumented with Micrometer timers and counters.
- Scoring calculations log input/output for traceability.
- Assessment state transitions emit structured log events.
- OpenTelemetry trace context propagates through the evaluation pipeline.
