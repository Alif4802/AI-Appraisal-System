# AI Appraisal & Assessment Platform
## Architecture Hardening Continuity Handoff

> **Purpose:** Preserve continuity from the previous Claude Opus architecture session and allow a new Claude Code session to continue the work without restarting, reinterpreting, or redesigning the project.

---

# 1. Current Project State

This repository contains the architecture and R&D specification for a configurable:

**AI Appraisal & Assessment Platform**

The platform is intended to support three distinct assessment domains:

1. **Employee / DESCO**
2. **Teacher**
3. **Student / LMS**

The architecture and scoring methodology have already gone through substantial R&D and design work.

**Application implementation has NOT started.**

The project is currently in the:

> **FINAL ARCHITECTURE DOCUMENTATION HARDENING PHASE**

The objective of the current work is to eliminate architectural contradictions, edge-case ambiguity, persistence/API mismatches, scoring-policy leakage, lifecycle inconsistencies, and implementation-plan gaps **before any production implementation begins**.

---

# 2. Repository Source-of-Truth Files

Before modifying anything, read the following files carefully.

## Primary R&D context

### `AI_Appraisal_Assessment_RnD_Handoff.md`

Contains the accumulated R&D decisions, scoring methodology, domain distinctions, AI-role decisions, evidence principles, human-review philosophy, and unresolved areas from the original research discussion.

Treat it as historical and methodological source of truth.

---

## Boilerplate / implementation context

### `AI_Appraisal_Assessment_Boilerplate_Handoff.md`

Contains the architectural and boilerplate direction for turning the R&D conclusions into a reusable reference application.

It describes the intended technology direction, modular-monolith strategy, implementation philosophy, and separation of architecture responsibilities from implementation responsibilities.

---

## Architecture specification

Read all documents under `docs/`:

1. `docs/01-architecture.md`
2. `docs/02-domain-model.md`
3. `docs/03-database-design.md`
4. `docs/04-api-spec.md`
5. `docs/05-ai-evaluation-design.md`
6. `docs/06-scoring-engine.md`
7. `docs/07-testing-strategy.md`
8. `docs/08-implementation-plan.md`
9. `docs/09-ui-ux-design-system.md`

These documents form the evolving Architecture Specification.

They are currently being hardened and are **not yet frozen as Architecture Specification v1**.

---

## Architecture hardening execution plan

### `implementation_plan_v3.md`

This is the **ONLY authoritative execution plan for the current documentation-hardening work**.

Earlier versions of this plan are superseded.

Do not reconstruct or reuse assumptions from implementation plan v1 or v2.

The authoritative rule is:

> `implementation_plan_v3.md` supersedes all earlier hardening plans.

---

# 3. Previous Session State

The previous Claude Opus architecture session completed the planning for the hardening pass and incorporated all final amendments into:

> **Architecture Documentation Hardening — Execution Plan v3**

The approved plan contains **20 workstreams**:

1. Specification governance
2. Module boundaries
3. Domain model
4. Evidence and reproducibility
5. Lifecycle and orchestration
6. Scoring architecture
7. AI architecture
8. Human review
9. Database/versioning
10. Multi-tenancy/security
11. API contracts
12. Student academic architecture
13. Pen picture/finalization
14. Testing/benchmarking
15. UI/backend alignment
16. Frontend scope
17. Localization
18. Observability/audit
19. Implementation sequencing
20. Cross-document verification

The previous session reached:

```text
WS-01 Specification governance        IN PROGRESS
WS-02 Module boundaries               PENDING
WS-03 Domain model                    PENDING
WS-04 Evidence and reproducibility    PENDING
WS-05 Lifecycle and orchestration     PENDING
WS-06 Scoring architecture            PENDING
WS-07 AI architecture                 PENDING
WS-08 Human review                    PENDING
WS-09 Database/versioning             PENDING
WS-10 Multi-tenancy/security          PENDING
WS-11 API contracts                   PENDING
WS-12 Student academic                PENDING
WS-13 Pen picture/finalization        PENDING
WS-14 Testing/benchmarking            PENDING
WS-15 UI/backend alignment            PENDING
WS-16 Frontend scope                  PENDING
WS-17 Localization                    PENDING
WS-18 Observability/audit             PENDING
WS-19 Implementation sequencing       PENDING
WS-20 Cross-document verification     PENDING
```

The previous session stopped because of usage quota exhaustion.

Do **not** assume WS-01 was completed.

Inspect the repository first and determine whether any partial changes from WS-01 are already present.

Continue from the actual repository state.

---

# 4. Claude's Role

Claude is the:

> **PRIMARY ARCHITECTURE AUTHOR AND ARCHITECTURE HARDENING AGENT**

Claude's responsibilities during this stage are:

- architecture analysis;
- domain-boundary analysis;
- dependency analysis;
- scoring-model correctness;
- AI/scoring separation;
- evidence architecture;
- historical reproducibility;
- lifecycle/state-machine design;
- persistence integrity;
- API/domain consistency;
- concurrency and idempotency analysis;
- tenant isolation;
- security architecture;
- audit durability;
- human-review semantics;
- student academic-model correctness;
- failure-mode analysis;
- operational edge cases;
- UI/backend contract alignment;
- implementation sequencing;
- final architecture freeze verification.

Claude should actively examine:

- trade-offs;
- edge cases;
- failure scenarios;
- invalid state transitions;
- race conditions;
- historical-data integrity;
- auditability;
- tenant leakage;
- partial AI failure;
- provider retry/fallback behavior;
- configuration ambiguity;
- scoring-policy leakage;
- mutable-vs-immutable data;
- implementation-order hazards.

Do not provide generic enterprise architecture recommendations merely because they are common patterns.

Every architectural mechanism should exist because it solves a concrete requirement of this platform.

---

# 5. Implementation Role Separation

The project intentionally separates architecture ownership from implementation ownership.

## Claude

Owns:

- architecture;
- scoring methodology;
- domain model;
- database design;
- API design;
- AI-evaluation contract;
- security architecture;
- hardening;
- implementation planning;
- architecture review.

## Gemini

Will later own:

- production-code implementation;
- migrations;
- repositories;
- services;
- controllers;
- tests;
- implementation fixes.

Gemini must implement the approved architecture.

Gemini must **not redesign the architecture independently**.

Therefore:

> Do not leave architectural ambiguity for Gemini to resolve.

Any software-architecture decision that can reasonably be resolved during hardening should be resolved now.

---

# 6. Do Not Start Implementation

During this hardening task:

- do not implement production application code;
- do not create production migrations;
- do not begin Phase 0;
- do not build the application;
- do not begin frontend implementation;
- do not introduce microservices;
- do not implement RAG;
- do not implement fine-tuning.

The current job is:

> **DOCUMENTATION + ARCHITECTURE HARDENING ONLY**

---

# 7. Preserve Approved Core Architecture

The following decisions are already approved and must not be casually redesigned.

## Platform architecture

- Modular monolith.
- Java + Spring Boot.
- Maven.
- PostgreSQL.
- Spring Data JPA.
- Flyway.
- Spring Security.
- REST + OpenAPI.
- Testcontainers.
- ArchUnit.
- Micrometer/OpenTelemetry.
- Spring AI behind an application-owned abstraction.

No microservices are required for v1.

Services should remain cleanly extractable later if genuine operational reasons arise.

---

## Score representation

The canonical mathematical score is:

> **BigDecimal 0–100**

The 1–5 rubric is not the mathematical foundation.

Keep separate:

- raw metric;
- normalized score;
- rubric level;
- display rating;
- AI suggestion;
- human-approved result.

Avoid early rounding.

---

## Scoring strategies

Conceptual strategies remain:

- `OBJECTIVE`
- `QUALITATIVE_RUBRIC`
- `HYBRID`
- `DERIVED`
- `COMPOSITE`

Objective arithmetic is deterministic.

AI must not perform mathematical calculations that software can deterministically perform.

---

## AI role

AI is primarily used for:

- rubric comparison;
- qualitative evidence interpretation;
- evidence-grounded explanation;
- strengths/development interpretation;
- pen-picture generation.

AI is **not** the authoritative arithmetic engine.

Structured AI output must be validated.

AI provider/model must remain replaceable.

---

## Human authority

AI suggests.

Humans approve or modify where policy permits.

Both must remain historically preserved.

Human review must not erase AI history.

Objective source-data corrections should normally correct the underlying facts and rerun deterministic calculations instead of arbitrarily replacing mathematical truth.

---

## Versioning and reproducibility

Anything capable of changing an assessment outcome must be versioned or historically reproducible.

This includes where applicable:

- framework;
- framework version;
- parameters;
- rubrics;
- weights;
- scoring rules;
- prompts;
- AI model/provider/options;
- evidence/input state;
- AI output;
- human review;
- override;
- algorithms/policies.

Historical assessment results must not silently change because current configuration or source data changed.

---

# 8. Preserve Domain Separation

Do not collapse the three assessment domains into one renamed UI or one universal scoring formula.

## Employee / DESCO

Typical concerns include:

- KPI/targets;
- productivity;
- job knowledge;
- quality;
- attendance;
- discipline;
- teamwork;
- communication;
- leadership;
- initiative;
- innovation;
- problem solving;
- organizational contribution.

UX may use a higher-density reviewer workbench.

---

## Teacher

Teacher assessment may share reusable assessment/reviewer primitives with Employee assessment.

However it must preserve teacher-specific:

- parameters;
- terminology;
- evidence;
- observations;
- student outcomes;
- professional-development context;
- instructional effectiveness;
- classroom-management context.

Do not describe Teacher as merely structurally identical to Employee.

---

## Student / LMS

Student assessment is fundamentally different.

Academic performance is primarily quantitative and structured.

Examples:

- exams;
- midterms;
- quizzes;
- assignments;
- class tests;
- labs;
- projects;
- attendance.

Software performs deterministic calculations.

AI interprets already-calculated academic facts plus qualitative evidence.

Student assessment must preserve both:

- current achievement;
- growth/progress.

For example:

```text
Student A: 60 → 82
Student B: 90 → 91
```

Student A demonstrates greater growth, while Student B still has higher current performance.

These concepts must not be collapsed into a single naïve score.

The Student UX remains mobile-first and lower-density than Employee reviewer workflows.

---

# 9. Evidence Principles

The conceptual evaluation chain remains:

> Claim → Evidence → Evaluation → Suggested Result → Human Review → Approved Result

Evidence quality and writing quality are not the same thing.

The system evaluates evidence, not eloquence.

Missing evidence is not poor performance.

Evidence states/policies must distinguish concepts such as:

- available;
- not applicable;
- missing required;
- insufficient;
- unverified;
- optional missing where applicable.

Do not silently turn missing information into a low performance score.

---

# 10. Confidence Principles

Do not interpret an LLM's self-reported confidence as calibrated probability.

Keep separate:

- AI/model-reported confidence;
- evidence strength;
- evidence sufficiency;
- evaluation confidence;
- method determinism.

Confidence must never directly multiply or reduce the subject's performance score.

Confidence controls:

- review requirements;
- diagnostics;
- escalation;
- uncertainty display.

It does not redefine performance.

---

# 11. Scoring Policy Gate

Some scoring questions genuinely require institutional/business approval.

These must remain explicitly policy-gated rather than guessed.

Examples include unresolved:

- objective normalization policies;
- threshold behavior;
- qualitative dimension aggregation;
- HYBRID component weighting;
- evidence-strength formula;
- Evaluation Confidence formula;
- contradiction/confidence policy;
- N/A redistribution policy;
- growth scoring;
- trend thresholds;
- consistency methodology;
- classification thresholds.

Do not invent "reasonable defaults" for institutional outcomes.

The approved implementation sequence contains:

```text
Phase 0
Phase 1
Phase 2
Phase 3

        ↓

SCORING POLICY APPROVAL GATE

        ↓

Phase 4+
```

Phase 4 outcome-affecting scoring implementations remain blocked until required policies are approved.

---

# 12. Hardening Plan Authority

`implementation_plan_v3.md` contains the detailed hardening requirements and amendments.

It includes **112 tracked issue entries** from the exhaustive architecture review.

Every issue must eventually be classified as exactly one of:

- `CLOSED`
- `POLICY-GATED`
- `DEFERRED` with explicit justification

No issue may disappear because it appears minor.

---

# 13. Document Authority by Concern

Do not use a naïve numeric precedence such as:

`01 > 02 > 03 > ...`

Use concern-based authority.

## `01-architecture.md`

Authoritative for:

- system architecture;
- module boundaries;
- dependency direction;
- cross-cutting architectural decisions;
- ADRs.

## `02-domain-model.md`

Authoritative for:

- aggregates;
- entities;
- value objects;
- domain invariants;
- lifecycle concepts.

## `03-database-design.md`

Authoritative for:

- relational realization;
- constraints;
- indexes;
- persistence integrity;
- schema representation.

## `04-api-spec.md`

Authoritative for:

- public API contracts;
- HTTP semantics;
- request/response shapes;
- authorization surfaces.

## `05-ai-evaluation-design.md`

Authoritative for:

- AI request/response contracts;
- AI interactions;
- validation;
- retry/fallback semantics;
- grounding mechanisms.

## `06-scoring-engine.md`

Authoritative for:

- mathematical scoring semantics;
- normalization;
- aggregation;
- missing-data behavior;
- strategy contracts.

## `07-testing-strategy.md`

Authoritative for:

- testing requirements;
- benchmark methodology;
- regression strategy.

## `08-implementation-plan.md`

Authoritative for:

- implementation sequence;
- phase dependencies;
- acceptance criteria;
- architecture gates.

## `09-ui-ux-design-system.md`

Authoritative for:

- user experience;
- presentation;
- interaction patterns;
- responsive/mobile behavior;
- accessibility.

Cross-concern conflicts must be explicitly reconciled.

---

# 14. Hardening Execution Rules

Execute:

> WS-01 → WS-20

sequentially.

Do not skip directly to WS-20.

However, because changing one document may affect others, cross-document updates are allowed when necessary.

After each major conceptual change:

1. search the repository for conflicting terminology;
2. update every affected contract;
3. verify domain ↔ database ↔ API ↔ testing ↔ UI consistency;
4. ensure implementation sequencing remains valid.

Do not fix one document while knowingly leaving another contradictory.

---

# 15. Editing Strategy

Do not blindly regenerate every document.

Preferred approach:

1. preserve already-approved content;
2. patch contradictory/incomplete sections;
3. rewrite full sections when necessary;
4. rewrite an entire document only when that is genuinely safer.

Avoid architectural drift caused by unnecessary rewriting.

---

# 16. Repository Inspection Before Editing

Before making changes:

1. inspect the complete repository;
2. inspect Git status/diff if available;
3. determine whether the previous session partially modified any files;
4. identify the exact current WS-01 state;
5. do not overwrite useful partial work blindly.

The repository state, not assumptions about the previous conversation, determines where execution resumes.

---

# 17. Architectural Decision Standard

When resolving a software-architecture ambiguity:

Do not merely choose the most popular pattern.

Evaluate:

- complexity;
- implementation cost;
- operational cost;
- failure behavior;
- auditability;
- testability;
- future extensibility;
- current project scale;
- institutional requirements;
- domain correctness.

Prefer the simplest mechanism that satisfies the actual requirements.

Examples:

Microservices should not be introduced merely for conceptual separation.

Kafka should not be introduced merely because events exist.

A modular monolith + durable transactional outbox may be more appropriate.

Strict DDD persistence separation should not be introduced unless its benefits justify the added mapping complexity.

Architecture should remain deliberate rather than fashionable.

---

# 18. Edge Cases Must Be Resolved

During hardening, explicitly examine at least:

- AI request failure;
- AI timeout;
- malformed structured output;
- retry exhaustion;
- provider fallback;
- partial parameter evaluation failure;
- application restart during evaluation;
- duplicate evaluation command;
- duplicate batch import;
- simultaneous framework activation;
- simultaneous review;
- concurrent result recalculation;
- missing required evidence;
- contradictory evidence;
- rejected evidence;
- corrected evidence;
- corrected academic facts;
- historical re-evaluation;
- framework supersession;
- assessment cancellation;
- review return;
- partial result;
- finalization;
- pen-picture regeneration;
- tenant boundary attack;
- unauthorized subject access;
- stale workflow state.

If the architecture cannot explain what happens in these scenarios, the hardening is incomplete.

---

# 19. Historical Reproducibility Standard

Historical replay must preserve the distinction between:

## Deterministic reproducibility

Given the same:

- framework snapshot;
- scoring configuration;
- source/input snapshot;
- validated AI output;
- review decisions;

the Scoring Engine must reproduce the same result.

## Generative reproducibility

Re-calling an LLM is not guaranteed to produce an identical response.

Therefore deterministic historical replay should use the stored validated AI output rather than requiring the model to generate identical content again.

---

# 20. AI Evaluation Snapshot Requirement

An EvaluationRun must be able to identify or preserve the exact context used during evaluation.

This includes as appropriate:

- evidence versions;
- evidence verification state;
- evidence mappings;
- evidence sufficiency;
- objective facts;
- academic facts;
- derived deterministic facts supplied to AI;
- framework version;
- parameter/scoring configuration reference;
- locale;
- relevant source-data versions.

Do not reconstruct historical AI input using mutable current records.

---

# 21. No Hidden Implementation Decisions

Before architecture freeze, Gemini should not need to decide things such as:

- how partial evaluation behaves;
- what a result status means;
- whether history is mutable;
- whether an institution admin can change global prompts;
- how tenant context works;
- whether AI retries create duplicate results;
- how stale evaluations recover;
- whether objective scores can be arbitrarily overwritten;
- how academic weighting works;
- how prompt versions are preserved;
- how evidence correction affects previous results.

Those are architecture responsibilities and must be resolved during hardening.

---

# 22. Deferred Features Must Be Explicit

Features intentionally outside reusable boilerplate v1 should be marked clearly.

Previously discussed likely deferred/host-integrated concerns include:

- RAG;
- model fine-tuning;
- microservices;
- external message broker;
- full user-directory management;
- parent/guardian workflow;
- built-in document/file storage if only references are required;
- arbitrary executable custom scoring expressions;
- potentially PDF/report generation if not adopted into v1;
- future LMS-specific extensions;
- advanced reviewer assignment if host-managed.

Do not silently implement a deferred feature.

---

# 23. Implementation Plan State

The application phases have **not started**.

Every acceptance criterion in `docs/08-implementation-plan.md` must initially be:

```text
[ ]
```

not:

```text
[x]
```

`[x]` means:

> implemented AND verified by objective evidence.

No phase is complete merely because it has been designed.

Expected current application state:

```text
Phase 0  — NOT STARTED
Phase 1  — NOT STARTED
Phase 2  — NOT STARTED
Phase 3  — NOT STARTED

Scoring Policy Gate — NOT YET APPROVED

Phase 4+ — BLOCKED BY SEQUENCING
```

---

# 24. Frontend State

`docs/09-ui-ux-design-system.md` is currently the UI/UX architecture/design specification.

Unless the final hardening explicitly changes this decision:

> frontend implementation is a later workstream and is NOT part of backend implementation Phases 0–12.

Do not start React/frontend implementation during architecture hardening.

---

# 25. Technology Baseline

Current intended technical direction:

- Java 25 preferred / Java 21-compatible where practical;
- Spring Boot 4.1.x baseline to be pinned before implementation;
- Maven;
- PostgreSQL;
- Spring Data JPA;
- Flyway;
- Spring Security;
- Spring AI 2.x;
- JUnit;
- Mockito;
- AssertJ;
- Testcontainers;
- ArchUnit;
- OpenAPI;
- Docker;
- Micrometer;
- OpenTelemetry.

Verify concrete versions before implementation rather than silently relying on "latest".

---

# 26. Definition of Architecture Freeze

The architecture is NOT frozen until WS-20 confirms:

- all 112 hardening issues accounted for;
- all architectural contradictions resolved;
- unresolved institutional scoring questions explicitly policy-gated;
- deferred features explicitly declared;
- module dependency graph acyclic;
- scoring does not depend on evaluation/AI implementation details;
- domain ↔ DB ↔ API contracts aligned;
- evidence historical reproducibility resolved;
- AI interaction audit model resolved;
- tenant isolation contract resolved;
- lifecycle aligned;
- review semantics aligned;
- result/version semantics aligned;
- academic model internally coherent;
- UI features either supported or explicitly deferred;
- APIs assigned to implementation phases;
- DB tables assigned to implementation phases;
- tests aligned to implementation phases;
- all implementation checkboxes reset appropriately;
- technology baseline documented;
- no unresolved architecture TODO is being delegated to Gemini.

Only then may the repository declare:

> **ARCHITECTURE SPECIFICATION v1 — FROZEN FOR IMPLEMENTATION**

---

# 27. Required Final Hardening Output

After WS-01 through WS-20 are complete, provide:

## 1. FINAL HARDENING SUMMARY

Concise explanation of the completed hardening.

## 2. CHANGED DOCUMENTS

For each of docs 01–09:

- sections changed;
- contradictions fixed;
- architectural contracts introduced;
- intentionally preserved decisions.

## 3. CROSS-DOCUMENT ISSUE CLOSURE MATRIX

For every tracked hardening issue:

- Issue ID
- Severity
- Documents affected
- Resolution
- Final source of truth
- Status

Allowed status:

- CLOSED
- POLICY-GATED
- DEFERRED

## 4. REMAINING POLICY GATES

Only genuine business/institutional/scoring decisions.

Do not place unresolved software architecture in this section.

## 5. DEFERRED FEATURES

Explicit list with rationale.

## 6. IMPLEMENTATION READINESS

State individually:

- Phase 0 ready?
- Phase 1 ready?
- Phase 2 ready?
- Phase 3 ready?
- Phase 4 ready?
- frontend ready?

Explain any NO.

## 7. DEPENDENCY GRAPH VERIFICATION

Confirm there is no prohibited compile-time cycle.

## 8. DOMAIN/SCHEMA/API/UI/TEST COVERAGE VERIFICATION

Confirm each in-scope concept has an implementation owner and phase.

## 9. ARCHITECTURE FREEZE CHECKLIST

Show final checklist status.

## 10. NO-CODE CONFIRMATION

Confirm:

- no production application code implemented;
- no application migration executed;
- no implementation phase started.

Finish with:

> `Architecture documentation hardening pass complete.`

---

# 28. Behavior When a Genuine New Contradiction Is Found

Do not stop for minor decisions that can be resolved under the approved principles.

For a genuine architectural contradiction:

1. identify it clearly;
2. explain why the existing documents cannot simultaneously be correct;
3. analyze the realistic alternatives;
4. compare trade-offs;
5. choose the simplest solution consistent with approved principles where possible;
6. document the decision;
7. propagate it to every affected document.

Only leave something unresolved when it genuinely requires institutional/business/scoring-policy input.

---

# 29. Anti-Drift Rules

While continuing this project:

Do NOT:

- restart the R&D from scratch;
- replace the modular monolith with microservices without compelling evidence;
- make AI responsible for deterministic mathematics;
- collapse Student into Employee scoring;
- use confidence as a performance-score multiplier;
- treat missing evidence as poor performance;
- make LLM self-reported confidence a calibrated probability;
- overwrite historical AI suggestions after human review;
- mutate frozen framework versions;
- mutate historical result versions;
- invent institutional scoring formulas;
- introduce unnecessary distributed infrastructure;
- make tenant security an afterthought;
- hide unresolved architecture inside implementation details.

---

# 30. Immediate Instruction to the New Claude Session

Your immediate task is:

1. Read this file.
2. Read both R&D/boilerplate handoffs.
3. Read `implementation_plan_v3.md`.
4. Read docs `01` through `09`.
5. Inspect repository/Git state for partial WS-01 changes.
6. Reconstruct the current architecture state from those sources.
7. Resume WS-01 from the actual repository state.
8. Continue sequentially through WS-20.
9. Perform final cross-document verification.
10. Do not implement production code.

The goal is not to create a new architecture.

The goal is to make the already-developed architecture:

> **internally coherent, reproducible, auditable, secure, implementation-ready, and frozen before Gemini begins Phase 0.**