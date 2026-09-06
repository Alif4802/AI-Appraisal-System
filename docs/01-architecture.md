# 01 — Architecture

> **Specification Version:** DRAFT — Pre-Hardening
> **Status:** NOT FROZEN — Architecture Specification v1 pending WS-20 verification.

---

## 0. Specification Governance

### 0.1 Document Authority by Concern

Each specification document is authoritative for its designated concern. `01` is the highest-level architectural authority. For specialized concerns, the corresponding document governs. Cross-concern conflicts must be explicitly reconciled rather than resolved by document number alone.

| Document | Authoritative Concern |
|----------|-----------------------|
| `01-architecture.md` | System architecture, module boundaries, dependency direction, cross-cutting decisions, ADRs |
| `02-domain-model.md` | Aggregates, entities, value objects, domain invariants, lifecycle concepts |
| `03-database-design.md` | Relational realization, constraints, indexes, persistence integrity, schema |
| `04-api-spec.md` | Public API contracts, HTTP semantics, request/response shapes, authorization surfaces |
| `05-ai-evaluation-design.md` | AI request/response contracts, AI interactions, validation, retry/fallback, grounding |
| `06-scoring-engine.md` | Mathematical scoring semantics, normalization, aggregation, missing-data behavior, strategy contracts |
| `07-testing-strategy.md` | Testing requirements, benchmark methodology, regression strategy |
| `08-implementation-plan.md` | Implementation sequence, phase dependencies, acceptance criteria, architecture gates |
| `09-ui-ux-design-system.md` | User experience, presentation, interaction patterns, responsive/mobile, accessibility |

### 0.2 Change Control

No lower-level document may silently contradict a higher-level architectural decision established in `01`. When a specialized document (e.g., `06` for scoring) establishes a contract within its domain of authority, other documents must conform to it for that concern. If a genuine conflict is discovered, it must be reconciled explicitly with a documented rationale — not resolved by ignoring one document.

### 0.3 Canonical Glossary

The following terms have precise meanings throughout the specification. All documents must use these definitions consistently.

#### Score Concepts

| Term | Definition |
|------|-----------|
| `normalizedScore` | Scoring-engine-produced canonical 0–100 BigDecimal score for a parameter. Output of strategy execution. |
| `aiSuggestedScore` | AI-reported qualitative score suggestion within 0–100. Input to `QUALITATIVE_RUBRIC` and `HYBRID` scoring strategies via `QualitativeEvaluationInput`. |
| `preReviewScore` | The parameter's `normalizedScore` at the time human review begins. Strategy-neutral — works for OBJECTIVE, QUALITATIVE, HYBRID, DERIVED, and COMPOSITE parameters. |
| `humanApprovedScore` | The score a reviewer has approved or set. Equals `preReviewScore` if approved without change; differs if modified. |
| `finalScore` | The effective parameter score used in overall calculation: `humanApprovedScore` if present, else `normalizedScore`. |
| `overallScore` | Weighted sum of all active parameters' `finalScore` values. |
| `configuredWeight` | Framework-defined parameter weight (from `ParameterWeight`). Invariant: all configured weights for active parameters sum to 1.0000. |
| `effectiveWeight` | Actual weight used in a specific calculation after N/A parameter redistribution. Recorded in `ParameterResult` for audit. |

#### Rubric and Rating Concepts

| Term | Definition |
|------|-----------|
| `rubricLevel` | Platform-resolved ordinal level within a parameter's configured rubric. Determined from `normalizedScore` against configured rubric ranges. |
| `aiReportedRubricLevel` | AI's suggested rubric level. Diagnostic — platform derives the authoritative `rubricLevel` from the canonical score. Discrepancies are recorded as validation warnings. |
| `overallRatingValue` | Framework-level overall assessment rating code (e.g., "4", "A"). Derived from `overallScore` via configured `RatingScale`. |
| `overallRatingLabel` | Human-readable label for the overall rating (e.g., "Exceeds Expectations"). |

#### Confidence and Evidence Quality

| Term | Definition |
|------|-----------|
| `aiReportedConfidence` | Model's self-reported confidence (0.0–1.0). Diagnostic metadata, NOT a calibrated probability. |
| `evaluationConfidence` | Platform-computed composite confidence incorporating evidence quality, source diversity, and optionally dampened AI confidence. Formula is POLICY-GATED. |
| `methodDeterminism` | Whether the scoring method is mathematically deterministic (e.g., OBJECTIVE = deterministic; QUALITATIVE = non-deterministic). Separate from `evaluationConfidence` — a deterministic method can still have low confidence if input data is incomplete or low-quality. |
| `evidenceStrength` | Platform-determined evidence quality level (`HIGH`, `MEDIUM`, `LOW`, `INSUFFICIENT`). Determined by evidence module based on source diversity, reliability, verification, corroboration, quantity, and recency. |
| `aiReportedEvidenceStrength` | AI's assessment of evidence quality. Diagnostic, not authoritative. |
| `evidenceSufficiency` | Rule-based determination of whether minimum evidence requirements are met for a parameter. States: `SUFFICIENT`, `INSUFFICIENT`, `MISSING_REQUIRED`, `NOT_APPLICABLE`. Owned by the evidence module. |

#### Lifecycle Concepts

| Term | Definition |
|------|-----------|
| `assessment` | Institutional evaluation of a subject over a period using a specific framework version. |
| `evaluation` | AI-assisted qualitative assessment of evidence against a rubric for one or more parameters. |
| `review` | Human examination of a parameter's score. Possible actions: approve, modify (with justification), or return. |
| `override` | Human modification of a score from its pre-review value. Always requires justification and creates audit trail. |
| `framework` | Institutional assessment configuration container. |
| `frameworkVersion` | Immutable snapshot of a framework's complete configuration (parameters, rubrics, weights, scoring rules, rating scale). |
| `domainType` | Assessment domain classification: `EMPLOYEE`, `TEACHER`, `STUDENT`. |

#### Entity and Data Concepts

| Term | Definition |
|------|-----------|
| `evidenceVerificationStatus` | Authoritative verification state of an evidence item: `UNVERIFIED`, `VERIFIED`, `REJECTED`. Set by authorized verifiers, not submitters. |
| `EvidenceSourceType` | Classification of evidence origin: `SYSTEM_GENERATED`, `OFFICIAL_RECORD`, `SUPERVISOR`, `TEACHER`, `PEER`, `SELF_REPORTED`, `EXTERNAL`, `DERIVED`. |
| `EvidenceReliability` | Framework-configured or verifier-assigned reliability level. Self-reported evidence cannot self-assign HIGH reliability. |
| `QualitativeEvaluationInput` | Scoring-module-owned value object containing AI evaluation results in a provider-neutral form. Mapped from `AiParameterEvaluation` by the evaluation module. Scoring never imports AI/evaluation types. |

### 0.4 Entity Mutability Classification

Every persistent entity in the platform falls into one of three categories:

| Category | Behavior | Examples |
|----------|----------|---------|
| **Mutable-Draft** | Editable while in DRAFT state; frozen on activation/finalization | `FrameworkVersion` (draft), `ParameterDefinition` (draft), `Assessment` (status field) |
| **Mutable-Workflow** | Updated during normal workflow; protected by optimistic locking (`@Version`) | `Assessment`, `ReviewSession`, `EvaluationRun` |
| **Immutable-Historical** | Never modified after creation; append-only versioning for corrections | `AssessmentResult`, `ParameterResult`, `AiParameterEvaluation`, `EvaluationRun` (completed), `ParameterReview`, `OverallOverride`, `AuditEvent`, `AiInteraction`, `PenPicture` |

> **Invariant:** No immutable-historical record may be modified. Corrections create new versioned records. The scoring engine must produce identical results given the same immutable inputs.

---

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
- Assessment lifecycle/state management: `CREATED → EVIDENCE_COLLECTION → EVALUATION_READY → EVALUATING → EVALUATED → UNDER_REVIEW → APPROVED → FINALIZED`
- Assessment period management
- Subject identity (reference to external HR/LMS subject)
- Domain invariant: `AssessmentSubject.subjectType` must match the framework's `DomainType`

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
- `ParameterResult`, `AssessmentResult`, `CanonicalScore`, `WeightedScore`, `ScoringStrategy` (interface), `ObjectiveScoringStrategy`, `QualitativeRubricScoringStrategy`, `HybridScoringStrategy`, `DerivedScoringStrategy`, `CompositeScoringStrategy`, `MissingDataPolicy`, `RatingConversion`, `ScoreCalculation`, `QualitativeEvaluationInput` (VO — scoring-owned input contract for AI evaluation results), `ParameterResultStatus`, `AssessmentResultStatus`

**Dependencies:** `framework`, `evidence`, `common`

**Critical Boundary:** Scoring does NOT depend on `evaluation` or `ai` modules. AI evaluation results enter scoring as `QualitativeEvaluationInput`, a value object defined and owned by the scoring module. The evaluation module maps `AiParameterEvaluation` → `QualitativeEvaluationInput` before invoking scoring.

**Does NOT own:** AI evaluation, evidence storage, human review decisions, pen pictures.

---

### 4.5 `evaluation` — AI Evaluation Pipeline

**Responsibilities:**
- Orchestrating AI evaluation requests for an assessment
- Assembling evaluation context (evidence sets, rubrics, calculated objective facts)
- Dispatching to AI gateway
- Receiving and validating structured AI responses
- Storing AI evaluation results immutably (`AiParameterEvaluation`)
- Recording per-AI-call audit metadata (`AiInteraction`)
- Creating `EvaluationInputSnapshot` for historical reproducibility
- Mapping validated AI output to scoring-module input (`QualitativeEvaluationInput`)
- Invoking the scoring engine with assembled context
- Retry/failure handling with crash recovery for in-progress evaluations

**Domain Concepts:**
- `EvaluationRun`, `EvaluationRequest`, `EvaluationContext`, `AiParameterEvaluation`, `EvaluationStatus`, `AiInteraction`, `EvaluationInputSnapshot`

**Dependencies:** `assessment`, `evidence`, `framework`, `ai` (port), `scoring` (invokes scoring engine; maps to `QualitativeEvaluationInput`), `common`

**Does NOT own:** AI provider implementation, scoring strategy logic, human review.

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
- Strategy-aware override permissions (OBJECTIVE parameters prefer source-data correction; QUALITATIVE allows direct modification; HYBRID distinguishes component correction; DERIVED/COMPOSITE prefer dependency correction)
- Review history (append-only)
- Overall-result override (exceptional, permission-controlled)
- Triggering score recalculation after parameter-level overrides
- Invariant: at most one IN_PROGRESS ReviewSession per assessment + result version

> **Note:** Reviewer assignment is external/host-managed in v1. The review module does not own formal assignment workflows.

**Domain Concepts:**
- `ReviewSession`, `ParameterReview`, `ReviewAction` (`APPROVED`, `MODIFIED`, `RETURNED`), `OverrideRecord`, `ReviewerInfo`, `OverallOverride`, `ReviewSessionStatus`

**Dependencies:** `assessment`, `scoring`, `framework`, `common`

> **Dependency note:** Review does NOT depend on `evaluation`. Review operates on `ParameterResult` (which contains the `preReviewScore`), not directly on AI evaluation data. AI justification may be surfaced via API joins, not compile-time module dependency.

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
- Shared domain primitives (`InstitutionId`, `UserId`, `Percentage`, etc.)
- Domain event infrastructure
- Validation utilities
- Temporal utilities (assessment period helpers)
- Exception hierarchy
- Shared DTOs for cross-module communication
- `InstitutionContext` — tenant resolution for all repository operations

**Domain Concepts:**
- `BaseEntity`, `AuditableEntity`, `DomainEvent`, `EntityId`, `VersionedEntity`, `InstitutionContext`

**Dependencies:** None (leaf module)

**Does NOT own:** Any business logic.

**Protection Rule:** `common` must not contain feature-specific code. It provides infrastructure shared across all modules. ArchUnit enforces that `common` does not import from any feature module.

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

Modules communicate through domain events published via Spring's `ApplicationEventPublisher`:

- `AssessmentCreatedEvent`
- `EvidenceSubmittedEvent`
- `EvaluationCompletedEvent`
- `EvaluationFailedEvent`
- `ReviewCompletedEvent`
- `ScoreRecalculatedEvent`
- `PenPictureGeneratedEvent`
- `FrameworkVersionActivatedEvent`
- `AssessmentFinalizedEvent`

The audit module subscribes to all events. Other modules subscribe selectively.

**Event delivery semantics:**

| Listener Type | Timing | Use Case |
|---------------|--------|----------|
| `@TransactionalEventListener(AFTER_COMMIT)` | After the originating transaction commits | Audit recording, notification dispatch, non-critical side effects |
| `@EventListener` (synchronous) | Within the originating transaction | Validation, cross-module state checks that must be consistent |

> **Design Note:** v1 uses Spring's in-process event bus. No external message broker. Events are not guaranteed durable by default — see §10.2 for audit durability.

### 10.2 Transaction Boundaries and Audit Durability

Each application service method defines its own transaction boundary. Cross-module operations that must be atomic use the orchestrating application service's transaction.

**AI calls occur OUTSIDE database transactions.** The evaluation orchestrator:
1. Persists `EvaluationRun` with status `IN_PROGRESS` (committed)
2. Executes AI calls outside any transaction
3. Persists results in a new transaction
4. Updates `EvaluationRun` status (committed)

This prevents long-running AI calls from holding database connections/locks.

**Audit durability model:**

Critical audit records (score changes, overrides, lifecycle transitions, AI interactions) use a **transactional outbox** pattern:

```text
Business transaction:
  1. Persist business state change
  2. Persist durable audit outbox record(s)
  3. COMMIT

Local outbox processor (after commit):
  4. Read unprocessed outbox records
  5. Write to audit_event table
  6. Mark outbox records as processed
```

An application crash after step 3 does NOT lose the audit record — the outbox processor picks it up on restart. No external message broker is required for v1.

### 10.3 Observability

- **Phase 0:** Baseline Micrometer metrics, Spring Boot Actuator health/info/metrics endpoints.
- **Phase 10:** OpenTelemetry tracing, security/audit observability hardening, AI call instrumentation.
- All AI gateway calls are instrumented with Micrometer timers and counters.
- Scoring calculations log correlation IDs and durations (NOT full evidence bodies or PII).
- Assessment state transitions emit structured log events.
- OpenTelemetry trace context propagates through the evaluation pipeline.

> **PII/logging rule:** Do not log full evidence content, raw AI prompts/responses, or subject PII at normal log levels. Use entity IDs, correlation IDs, status codes, and durations. Raw AI content is stored in `AiInteraction` records with access restrictions, not in application logs.

### 10.4 Concurrency Control

Mutable aggregate roots (`Assessment`, `ReviewSession`, draft `FrameworkVersion`) use JPA `@Version` for optimistic locking. Concurrent modifications produce `409 Conflict` at the API layer.

Specific concurrency invariants:
- Framework activation: at most one ACTIVE version per framework (partial unique index)
- Evaluation trigger: duplicate/concurrent evaluation commands rejected via `EvaluationRun` status check
- Review: at most one IN_PROGRESS `ReviewSession` per assessment + result version
- Result recalculation: serialized via optimistic locking on `AssessmentResult`

### 10.5 Domain/JPA Coupling Decision (ADR-11)

**Decision:** Domain entity classes carry JPA annotations (`@Entity`, `@Id`, `@Column`, etc.) directly.

**Rationale:** In a modular monolith with a single persistence technology (PostgreSQL/JPA), introducing a separate persistence mapping layer adds significant complexity (manual mappers, parallel class hierarchies) without proportional benefit. The `@Entity` annotation is the only Spring/Jakarta coupling in domain model classes — domain logic remains framework-free.

**Constraint:** Domain model classes in `domain/model/` must NOT import Spring Framework annotations (e.g., `@Service`, `@Component`, `@Autowired`). JPA/Jakarta Validation annotations are permitted as a pragmatic coupling. ArchUnit enforces this boundary.

**Trade-off:** If the persistence technology changes (unlikely for v1), domain entities must be modified. Acceptable for current project scale.

### 10.6 Cross-Module Access Rules

Modules access each other through published application-layer interfaces (services), never through direct repository imports:

```text
✓  EvaluationService → EvidenceService.getEvidenceSet(assessmentId, parameterId)
✗  EvaluationService → EvidenceRepository.findByAssessmentId(assessmentId)
```

Repositories are package-private within their owning module. ArchUnit enforces this.
