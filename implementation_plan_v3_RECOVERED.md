# Architecture Documentation Hardening — Execution Plan v2

## Objective

Exhaustive cross-document hardening of `docs/01`–`docs/09` to produce **Architecture Specification v1**, suitable for freezing before implementation begins.

## Execution Philosophy

1. **Preserve** already-approved content wherever possible.
2. **Patch** contradictory or incomplete sections surgically.
3. **Rewrite** full sections only when patching would be more error-prone than a clean rewrite.
4. **Regenerate** an entire document only when cross-cutting fixes affect the majority of its content and surgical patching would risk silent inconsistency.

## Closure Matrix Commitment

The final `CROSS-DOCUMENT ISSUE CLOSURE MATRIX` will contain **every issue from the exhaustive hardening request (A through DH)** — no exceptions. Each issue will carry exactly one of these statuses:

| Status | Meaning |
|--------|---------|
| `CLOSED` | Resolved in the specification. No ambiguity remains. |
| `POLICY-GATED` | Requires institutional/business approval before implementation. Formally documented as a gate. |
| `DEFERRED` | Explicitly out of scope for boilerplate v1 with documented justification. |

No issue may be omitted because it was considered minor.

---

## Checkpoint Reset

All Phase 0–12 acceptance criteria in `08-implementation-plan.md` will be reset from `[x]` to `[ ]` because implementation has not started.

---

## Workstream Plan

### WS-01: Specification Governance and Terminology

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| B | Document precedence and canonical glossary | Add "Specification Governance" section to `01`. Define document hierarchy (01 > 02 > 03 > … > 09). Create canonical glossary of all ambiguous terms listed in issue B. |

**Glossary terms to define canonically:**

- `normalizedScore` — scoring-engine-produced canonical 0–100 score for a parameter
- `suggestedScore` — AI-reported qualitative score suggestion (renamed to `aiSuggestedScore` in domain)
- `aiReportedConfidence` — model's self-reported confidence (not calibrated)
- `evaluationConfidence` — platform-computed composite confidence (policy-gated formula)
- `evidenceStrength` — platform-determined evidence quality level
- `aiReportedEvidenceStrength` — AI's assessment of evidence quality (diagnostic, not authoritative)
- `configuredWeight` — framework-defined parameter weight
- `effectiveWeight` — actual weight used in a specific calculation (after N/A redistribution)
- `finalScore` — effective parameter score used in overall calculation (= humanApprovedScore if present, else normalizedScore)
- `overallScore` — weighted sum of final parameter scores
- `rubricLevel` — platform-resolved ordinal level within a parameter's configured rubric
- `overallRatingValue` / `overallRatingLabel` — framework-level overall assessment rating
- `review` — human evaluation of a parameter's pre-review score
- `override` — human modification of a score with mandatory justification
- `evaluation` — AI-assisted qualitative assessment of evidence against a rubric
- `assessment` — institutional evaluation of a subject over a period using a framework
- `evidenceSufficiency` — rule-based determination of whether minimum evidence requirements are met
- `evidenceVerificationStatus` — authoritative verification state of an evidence item
- `framework` — institutional assessment configuration container
- `frameworkVersion` — immutable snapshot of a framework's complete configuration
- `domainType` — assessment domain (`EMPLOYEE`, `TEACHER`, `STUDENT`)

**Documents changed:** `01`

---

### WS-02: Module Boundaries and Dependencies

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| C | Scoring↔Evaluation dependency violation | Introduce `QualitativeInput` VO in `scoring` module. Remove `AiParameterEvaluation` from `ScoringContext`. Evaluation module maps AI output to scoring's input contract. Update dependency graph. |
| CT | Domain/JPA coupling decision | Document pragmatic JPA annotations on domain entities as the chosen approach. Add tradeoff rationale. |
| CU | Shared kernel boundaries | Add ArchUnit rule protecting `common` from feature-specific code. Document what belongs in common vs. module-owned. |
| CV | Cross-module repository access | Document that cross-module access uses published application/domain interfaces, not direct repository imports. |
| CW | Transactional orchestration | Define transaction boundaries for multi-module workflows. AI calls occur outside DB transactions. |
| BQ | Domain event async language | Correct "asynchronous" wording. Define which listeners are synchronous vs. async. Clarify `@TransactionalEventListener` timing. |
| P6/P7/P8 preservation | Dependency flows inward, package-enforced, no circulars | Verify preserved in corrected dependency graph. |

**Documents changed:** `01`, `02`, `06`, `07`, `08`

---

### WS-03: Domain Model

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| F | ParameterResult finalScore semantics | Fix to `finalScore = humanApprovedScore if present, else normalizedScore`. Works for all strategy types. |
| G | Strategy-neutral score attribution | Add explicit fields: deterministic component, AI qualitative suggestion, human-approved value. For HYBRID: preserve objective/qualitative component scores, weights, and provenance. |
| H | Strategy-neutral human review | Rename `originalAiScore` to `preReviewScore`. Review operates on current parameter result, not AI-specific concept. |
| I | Explicit ParameterResultStatus | Add `ParameterResultStatus` enum: `SCORED`, `EXCLUDED`, `REQUIRES_REVIEW`, `BLOCKED`, `EVALUATION_FAILED`, `NOT_APPLICABLE`. Define overall score behavior per status. |
| J | Configured vs effective weights | Add `configuredWeight` and `effectiveWeight` to ParameterResult (or structured calculation metadata). |
| K | Immutable result versions | Make rule explicit: existing AssessmentResult versions are immutable. Any change creates a NEW row. Add `UNIQUE(assessment_id, version)`. |
| AA | ParameterDefinition domainType redundancy | Remove `domainType` from ParameterDefinition. Derive from owning framework. |
| AG | AiInteraction entity | Add `AiInteraction` entity for per-AI-call audit. Covers parameter evaluation and pen-picture calls. |
| AP | Institution model | Add minimal `Institution` entity: id, code, name, status, settings JSONB, createdAt, updatedAt. |
| DB | Assessment subject/framework domain invariant | AssessmentSubject.subjectType must match framework's DomainType. Enforce at creation. |
| DC | Academic record subject ID invariant | Derive/validate `studentExternalId` from AssessmentSubject. Prevent inconsistency. |
| DE | Append-only vs mutable entity classification | Add classification table: mutable draft, mutable workflow, immutable historical. |

**Documents changed:** `01`, `02`, `03`, `04`, `06`, `07`, `09`

---

### WS-04: Evidence and Historical Reproducibility

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| D | Evidence sufficiency ownership | Evidence module owns eligibility, assembly, sufficiency determination. Scoring module consumes sufficiency and applies MissingDataPolicy. Single owner per concept. |
| AB | EvidenceRule vs MinimumEvidenceRequirements | Normalize into one canonical model. EvidenceRule covers eligibility (allowed sources, age, verification). Sufficiency rules (min items, required sources) are a subsection. Remove duplication. |
| AC | Evidence history reproducibility | Choose solution: EvaluationRun stores `EvaluationInputSnapshot` (immutable snapshot of evidence IDs, versions, verification status, mapping types, sufficiency at evaluation time). |
| AD | Evidence corrections and deletion | Evidence items are not physically deleted if referenced by evaluation. Corrections create new evidence items. Rejected evidence remains readable. |
| AE | Evidence source reliability ownership | Reliability determined by framework source policy or authorized verifier, NOT by the submitter. Self-reported evidence cannot self-assign HIGH reliability. |
| AF | DOCUMENT_REFERENCE semantics | v1: references externally managed document URI. No file upload/storage. Clearly document this. |
| CI | Evidence source visual semantics in UI | Source type badge ≠ verification status. SUPERVISOR source does NOT automatically mean VERIFIED. Separate visual treatments. |

**Documents changed:** `01`, `02`, `03`, `04`, `05`, `06`, `07`, `09`

---

### WS-05: Assessment Lifecycle and Orchestration

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| T | Lifecycle alignment across all docs | Use one canonical lifecycle in all documents. `01` currently skips `EVALUATION_READY`; add it. Define every state, transition, trigger, validation, failure behavior, and terminal states. |
| T-sub | RETURNED semantics | `RETURNED` is a `ReviewSession.status`, NOT a persisted `AssessmentStatus`. Assessment transitions directly back to `EVIDENCE_COLLECTION`. Return event retained in audit. |
| T-sub | APPROVED cancellation | Define: APPROVED assessments may NOT be cancelled. Only FINALIZED is also terminal. Specific list of cancellable states. |
| U | Evaluation readiness | Define explicit readiness validation. Evidence collection explicitly completed + no BLOCK_EVALUATION policy unmet. Deterministic and testable. |
| V | Failed and partial evaluation behavior | Define: total failure → assessment returns to `EVALUATION_READY`. Partial completion → assessment moves to `EVALUATED` with failed parameters marked `EVALUATION_FAILED`. Manual review can proceed. |
| W | Evaluation execution model | v1: `POST /evaluate` → `202 Accepted` → bounded in-process background executor → persisted progress → UI polls. No external message broker. |
| X | Idempotency and concurrency | Define behavior for duplicate/concurrent commands. Framework activation, evaluation trigger, review completion, result recalculation, pen-picture generation, batch imports. Use transaction constraints, optimistic locking, unique constraints. |
| AM | Finalization and pen-picture version pinning | FINALIZED assessment pins exact result version and pen-picture version. Post-finalization mutation blocked. Amendments are future work. |
| DA | Model/prompt change effect on historical data | New model/prompt affects new evaluations only. Re-evaluation creates new EvaluationRun + new AssessmentResult version. |

**Documents changed:** `01`, `02`, `04`, `05`, `06`, `08`, `09`

---

### WS-06: Scoring Architecture

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| E | Scoring policy approval gate | Add explicit `SCORING POLICY APPROVAL GATE` between Phase 3 and Phase 4. Create centralized "Unresolved Institutional Scoring Policies" section. Remove "implement reasonable defaults" instruction. |
| L | Dynamic rubrics — no 1–5 assumption | Remove `@Max(5)` from AI DTO. DB says "positive sequential framework-defined ordinal level". RubricViewer supports any count. Tests include non-5-level rubric. |
| M | Rubric/rating range boundaries | Standardize: intermediate ranges `[min, max)`, final/top range `[min, 100]`. Score 100.0000 always maps. Activation validation rejects gaps, overlaps, incorrect ordering, missing 0/100 boundaries, midpoint outside range. |
| N | Rubric midpoint contract | v1: AI must provide `suggestedScore`. Midpoint is useful for framework display but not an undocumented fallback. If fallback retained, document exactly when and test it. |
| Q | COMPOSITE AI boundary | COMPOSITE is deterministic. Not grouped with AI-required strategies. Correct the evaluation context assembly. |
| R | Safe scoring configuration | Remove CUSTOM arbitrary expression execution. v1: enumerated safe formula types only. Schema-validate strategy config. Future safe DSL is marked as future work. |
| S | Missing-evidence test semantics | Correct: No evidence → sufficiency state → MissingDataPolicy → policy outcome. NOT "no evidence → low score." |
| DD | Derived data storage rules | For every derived field: calculated-on-read vs. persisted snapshot. If persisted: system-calculated, never client-authoritative. |
| BG | Stored derived academic percentage integrity | `percentage` and `attendancePercentage` are system-calculated. API rejects client percentages when raw facts are present. |

**Documents changed:** `02`, `03`, `04`, `05`, `06`, `07`, `08`, `09`

---

### WS-07: AI Architecture and Interaction Audit

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| O | AI rubric-level handling | AI reports `aiSuggestedScore` + optional `aiReportedRubricLevel`. Platform derives `resolvedRubricLevel`. Discrepancy recorded as validation warning. Original AI value preserved. |
| P-name | AI-reported confidence vs Evaluation Confidence | Rename AI-side to `aiReportedConfidence`. Platform-side: `evaluationConfidence`. Likewise `aiReportedEvidenceStrength` vs `evidenceStrength`. |
| AH | Raw AI response retention | Choose canonical storage: AiInteraction record. Define retention config, access restrictions, redaction policy, encryption expectations. No unrestricted retention of PII-containing payloads. |
| AI | AI evidence references integrity | Add relational join `ai_evaluation_evidence_reference` for FK integrity. Historical reference integrity guaranteed. |
| AJ | AI validation auto-correction behavior | Define severity levels. Hard violation → retry. Recoverable discrepancy → preserve original, derive platform value, record warning. Persistent invalid → mark failure. All transformations auditable. |
| AK | AI grounding claims | Document as layered controls: ID validity, lexical validation, heuristic checks, benchmark evaluation, human review. Do not describe heuristics as mathematical guarantees. |
| AL | PenPicture response/persistence mismatch | Persist and expose structured fields (`keyHighlights`, `developmentRecommendations`). |
| BD | PenPicture AI metadata | Use same AiInteraction mechanism for pen-picture generation audit. |
| CX | AI call timeouts and resource limits | Define: request timeout config, bounded retry count, bounded executor concurrency, max evidence/context/prompt/output size, batch limits. |

**Documents changed:** `02`, `03`, `04`, `05`, `06`, `07`, `08`

---

### WS-08: Human Review and Overrides

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| H | Strategy-neutral review (details) | For HYBRID: distinguish changing qualitative component vs. overriding complete final score. If both supported, model as separate actions. |
| AN | Review assignment | v1: explicitly declare reviewer assignment as external/future. Remove "assignment" from review module responsibility. "Pending Reviews" in UI filtered by authorized assessments, not formal assignment. |
| AO | One active review rule | At most one IN_PROGRESS ReviewSession per assessment+result_version. Database unique constraint where practical. |
| CM | UI override log non-AI parameters | Use "Pre-review score → Approved score" and display AI attribution separately only when AI contributed. |

**Documents changed:** `01`, `02`, `03`, `04`, `07`, `08`, `09`

---

### WS-09: Database / Versioning / Integrity

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| AX | Schema uniqueness/check constraints | Add explicit constraints: `assessment_framework(institution_id, code)` UNIQUE, at most one ACTIVE version per framework (partial unique index), `assessment_result(assessment_id, version)` UNIQUE, `pen_picture(assessment_id, version)` UNIQUE, score check constraints, academic validation, etc. |
| AY | FK delete/archive behavior | RESTRICT for historical data. Activated versions not deletable. Evidence referenced by evaluations not physically deletable. Draft-only config removable. Document per-table. |
| AZ | Optimistic concurrency control | Add `@Version` / version field to mutable aggregate roots: draft framework config, Assessment, ReviewSession. |
| BA | Standardized audit metadata | Consistent createdAt/createdBy/updatedAt/updatedBy across mutable tables. Intentional rule for which immutable children need which fields. |
| Y | Framework vs FrameworkVersion lifecycle | Define relationship. Framework becomes ACTIVE when first version activates. Exactly one ACTIVE version per ACTIVE framework (partial unique index). ARCHIVED framework → historical versions readable. |
| Z | Effective-date semantics | `effective_from` nullable while DRAFT (set at activation). Assessment period must correspond to framework-version effective period. Existing assessments remain attached to superseded versions. |
| BB | Prompt template versioning model | PromptTemplate (stable key, purpose, scope) + PromptTemplateVersion (immutable content, version number, active flag). Or documented single-table approach with stable template key. |
| BC | Prompt scope: global vs institution | v1: platform-global templates. Optional institution override is future work. Remove misleading institution-level prompt editing. |
| AQ | Multi-tenancy database principle | Precise rule: aggregate roots carry `institution_id`. Child records inherit via FK. Direct duplication only when justified for security/query. |

**Documents changed:** `03`, `01`, `02`, `04`, `08`

---

### WS-10: Multi-tenancy / Security

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| AR | Early InstitutionContext | From first persistence phase: repositories operate under InstitutionContext. Dev/test InstitutionContext provider acceptable. Phase 10 replaces with authenticated principal. |
| AS | X-Institution-Id header security | Production tenant identity from JWT/authenticated principal, NOT raw client header. Header restricted to dev/test/trusted gateway. |
| AT | Authorization across all APIs | Define per-role/tenant/ownership authorization. SUBJECT accesses only own data. Reviewer accesses authorized assessments. Auditor is read-only. |
| AU | Role alignment | Reconcile roles. STUDENT = SUBJECT with SubjectType=STUDENT. PARENT_GUARDIAN deferred to future LMS integration. |
| AV | User management scope | Security consumes external UserPrincipal. Full user-directory management is outside core assessment engine. Remove/mark User/Role Management UI as host-integrated. |
| AW | Pre-Phase-10 deployment restrictions | Permit-all is dev/test only. Security profiles must fail safe outside dev/test. |
| CZ | AI secret handling | API keys never in DB plaintext, never returned by API, never logged. Environment/secret infrastructure only. |

**Documents changed:** `01`, `03`, `04`, `08`, `09`

---

### WS-11: API Contracts

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| CB | HTTP response semantics | Standardize: POST create → 201, async → 202, reads → 200, no-content → 204, validation → 400, unauth → 401, forbidden → 403, not found → 404, conflict → 409. |
| CC | Idempotency keys | Define for evaluate, batch imports, pen-picture generation. Network retry must not create duplicates. |
| CD | Pagination/sorting contracts | Verify every list endpoint follows same envelope. Set safe max page size (e.g., 100). No unbounded lists. |
| CG | Stable machine-readable error codes | Ensure `code` values are stable, documented, UI-localizable. No stack traces returned. traceId links to logs. |
| CH | Real-time UI claims | v1: polling. Correct "real-time" to "progress updates". SSE/WebSocket deferred. |

**Documents changed:** `04`, `05`, `08`, `09`

---

### WS-12: Student Academic Architecture

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| BE | Student academic weighting model | Define two levels: category weight within subject + individual weight within category. Prevent quiz weight ambiguity. Configuration-driven, not hard-coded. |
| BF | Previous-period data source for growth | Define: prior period identified by matching AssessmentSubject + previous AssessmentPeriod. Subject matching rules. Behavior when no prior period or subject mix changes. |
| BH | Academic result corrections | Before evaluation: controlled correction allowed. After evaluation: historical reproducibility via versioning/snapshotting. No silent retroactive changes. |
| BI | Growth/trend/consistency policy gating | Raw mathematical facts computable early. Classification thresholds (IMPROVING, STABLE, DECLINING, etc.) are policy-gated. Update Phase 9 acceptance criteria accordingly. |
| BJ | Student pen-picture phase sequencing | Phase 8: generic pen-picture infra + Employee/Teacher templates. Phase 9: Student academic module + student-specific pen-picture context enabled. |

**Documents changed:** `02`, `03`, `04`, `06`, `08`

---

### WS-13: Pen Picture / Finalization

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| AL | PenPicture structured fields | Persist `keyHighlights` and `developmentRecommendations` alongside `content`. |
| AM | Finalization version pinning | FINALIZED assessment records exact result version and pen-picture version. Post-finalization mutation blocked. |
| BD | PenPicture AI metadata audit | Same AiInteraction model covers pen-picture AI calls. |
| CF | Pen picture locale | Generation request includes requested locale. Stored pen-picture records its locale. Prompt resolution accounts for locale. |

**Documents changed:** `02`, `03`, `04`, `05`, `08`

---

### WS-14: Testing / Benchmarking

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| BW | Test fixtures after scoring/evaluation decoupling | Scoring tests use `QualitativeInput`. Evaluation tests use `AiParameterEvaluation`. Update fixtures. |
| BX | Benchmark targets as provisional | Label as "INITIAL R&D BENCHMARK TARGETS". Require recalibration against human-approved data. Version baseline. |
| BY | Benchmark ground truth provenance | Record who established expected output, human-reviewed expected range, benchmark version, framework version. Label synthetic cases. |
| BZ | Privacy in regression replay | Authorized test environments only. De-identification. No production PII in developer fixtures. |
| CA | Reproducibility clarification | Define two guarantees: deterministic (identical given same inputs) and generative (LLM not guaranteed identical). Historical replay reuses stored AI outputs. |
| BT | Phase-aware security tests | Before Phase 10: dev/test principal. After Phase 10: 401/403/tenant/role tests become mandatory. |
| CR | Test ↔ implementation phase alignment | Every test category runs at phase where its dependencies exist. No AI tests before AI module, no security tests before security phase. |
| S | Missing-evidence test semantics | No evidence ≠ low score. Update test descriptions accordingly. |

**Documents changed:** `07`, `08`

---

### WS-15: UI / Backend Scope Alignment

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| BK | UI features ↔ backend scope | Audit every UI feature against domain/API/phase. Mark unimplemented as FUTURE/HOST-INTEGRATED/DESIGN-ONLY. |
| BL | PDF/reporting scope | Mark as deferred. Remove active UI affordances from v1 implementation target. |
| BM | AI provider configuration UI | v1: provider credentials remain environment-managed. UI may display non-secret status. Secrets not exposed. |
| CI | Evidence provenance vs verification | Source badge = provenance. Verification badge = separate. SUPERVISOR ≠ automatically VERIFIED. |
| CJ | Teacher UX wording | Replace "structurally identical to Employee" with accurate description of shared primitives + domain-specific needs. |
| CK | Confidence UI wording | Replace "factual value" with "model-reported confidence is diagnostic metadata, not a calibrated probability." |
| CL | Parameter rubric vs overall rating terminology | Parameter: `rubricLevel`/`rubricLabel`. Assessment: `overallRatingValue`/`overallRatingLabel`. No ambiguous `displayRating` for both. |
| CM | Override log non-AI parameters | "Pre-review score → Approved score" with separate AI attribution. |
| CS | UI features ↔ role/permission | Verify per-screen authorization, read-only variants, tenant/ownership restriction. |

**Documents changed:** `04`, `09`

---

### WS-16: Frontend Scope

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| BN | Frontend implementation plan | `09` is design specification for a later frontend workstream. No frontend phases in `08`. State this clearly. |
| BO | Pinned technology versions | Replace "latest" with "version to be pinned before Phase 0 begins." Add concrete pinning requirement. |

**Documents changed:** `08`, `09`

---

### WS-17: Localization

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| CE | Dynamic content localization | Define strategy for framework-configurable text. v1: default locale fields + optional translation JSONB. Locale tags `en`/`bn`. Fallback behavior. |
| CF | Pen picture locale | Covered also in WS-13. Included here for localization consistency. |

**Documents changed:** `02`, `03`, `04`, `09`

---

### WS-18: Observability / Audit Durability

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| BP | Observability implementation | Include baseline Micrometer/metrics in Phase 0 infrastructure. Full tracing staged later. |
| BR | Durable audit delivery | Critical audit records written in same transaction. Non-critical: transactional outbox or after-commit listener with documented risk. No Kafka for v1. |
| BS | Log/PII data minimization | Do NOT log full evidence bodies, raw prompts, raw AI responses, PII at normal log levels. Use IDs, correlation IDs, status, durations. Add to architecture guidance. |
| CY | Input size validation | Define configurable limits for evidence text, metadata, batch size, parameter count, rubric levels, prompt template size, pen picture length, academic import rows. |

**Documents changed:** `01`, `03`, `05`, `07`, `08`

---

### WS-19: Implementation Sequencing

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| BU | Reset all acceptance checkboxes | Change ALL `[x]` to `[ ]` across Phases 0–12. Add legend. |
| BV | Phase completion evidence | Define what Gemini must provide: build output, test results, migration verification, ArchUnit results. |
| DG | Implementation plan status | All phases NOT STARTED. Frontend also NOT STARTED. |
| DF | Architecture freeze checklist | Add "Pre-Gemini Architecture Freeze Checklist" section. |
| E | Scoring policy gate (sequencing) | Insert explicit gate between Phase 3 and Phase 4 in phase dependency graph. |
| BJ | Student pen-picture sequencing | Phase 8 = generic + Employee/Teacher. Phase 9 = Student academic + student pen-picture integration. |
| CP | API ↔ implementation phase audit | Every API in `04` has one implementation phase. Every phase API exists in `04`. |
| CQ | DB table ↔ implementation phase audit | Every table in `03` created in exactly one phase. |

**Documents changed:** `08`

---

### WS-20: Final Cross-Document Verification

**Issues covered:**

| Issue | Title | Resolution Approach |
|-------|-------|---------------------|
| CN | Enum consistency | Audit all enums across Domain, DB, API, Tests, UI. Canonical values aligned. |
| CO | Entity/field cross-document audit | Domain ↔ DB ↔ API ↔ Test ↔ UI ↔ Phase. No orphaned or missing fields. |
| CP | API ↔ phase coverage | Every API has an implementation owner. |
| CQ | DB ↔ phase coverage | Every table has a migration phase. |
| CR | Test ↔ phase alignment | Tests run at phases where dependencies exist. |
| CS | UI ↔ role/permission coverage | Every screen/action has authorization defined. |

**Verification artifacts produced:**
- Enum consistency table
- Entity/field cross-reference
- API-to-phase mapping
- DB-to-phase mapping
- UI-feature-to-backend mapping
- Final CROSS-DOCUMENT ISSUE CLOSURE MATRIX

**Documents changed:** All (`01`–`09`), as corrections found during verification.

---

## Complete Issue-to-Workstream Mapping

Every issue from the hardening request mapped to its workstream:

| Issue | Short Title | WS |
|-------|------------|-----|
| A | Preserve approved core decisions | All (constraint) |
| B | Specification governance & glossary | WS-01 |
| C | Scoring↔Evaluation dependency violation | WS-02 |
| D | Evidence sufficiency ownership | WS-04 |
| E | Scoring policy approval gate | WS-06, WS-19 |
| F | ParameterResult finalScore semantics | WS-03 |
| G | Strategy-neutral score attribution | WS-03 |
| H | Strategy-neutral human review | WS-03, WS-08 |
| I | Explicit ParameterResultStatus | WS-03 |
| J | Configured and effective weights | WS-03 |
| K | Immutable result versions | WS-03 |
| L | Dynamic rubrics, no 1–5 assumption | WS-06 |
| M | Rubric/rating range boundary rules | WS-06 |
| N | Rubric midpoint contract | WS-06 |
| O | AI rubric-level handling | WS-07 |
| P | AI-reported confidence vs Evaluation Confidence | WS-07 |
| Q | COMPOSITE AI boundary | WS-06 |
| R | Safe scoring configuration, no arbitrary expressions | WS-06 |
| S | Missing-evidence test semantics | WS-06, WS-14 |
| T | Assessment lifecycle alignment | WS-05 |
| U | Evaluation readiness rules | WS-05 |
| V | Failed and partial evaluation behavior | WS-05 |
| W | Evaluation execution model | WS-05 |
| X | Idempotency and concurrency | WS-05 |
| Y | Framework vs FrameworkVersion lifecycle | WS-09 |
| Z | Effective-date semantics | WS-09 |
| AA | ParameterDefinition domainType redundancy | WS-03 |
| AB | EvidenceRule vs MinimumEvidenceRequirements | WS-04 |
| AC | Evidence history reproducibility | WS-04 |
| AD | Evidence corrections and deletion | WS-04 |
| AE | Evidence source reliability ownership | WS-04 |
| AF | DOCUMENT_REFERENCE semantics | WS-04 |
| AG | Per-AI-interaction audit records | WS-03, WS-07 |
| AH | Raw AI response retention/security | WS-07 |
| AI | AI evidence reference integrity | WS-07 |
| AJ | AI validation auto-correction behavior | WS-07 |
| AK | AI grounding claims | WS-07 |
| AL | PenPicture response/persistence mismatch | WS-07, WS-13 |
| AM | Finalization and pen-picture version pinning | WS-05, WS-13 |
| AN | Review assignment scope | WS-08 |
| AO | One active review rule | WS-08 |
| AP | Institution model | WS-03 |
| AQ | Multi-tenancy database principle | WS-09 |
| AR | Early InstitutionContext | WS-10 |
| AS | X-Institution-Id header security | WS-10 |
| AT | Authorization across all APIs | WS-10 |
| AU | Role alignment | WS-10 |
| AV | User management scope | WS-10 |
| AW | Pre-Phase-10 deployment restrictions | WS-10 |
| AX | Schema uniqueness/check constraints | WS-09 |
| AY | FK delete/archive behavior | WS-09 |
| AZ | Optimistic concurrency control | WS-09 |
| BA | Standardized audit metadata | WS-09 |
| BB | Prompt template versioning model | WS-09 |
| BC | Prompt scope global vs institution | WS-09 |
| BD | PenPicture AI metadata | WS-07, WS-13 |
| BE | Student academic weighting model | WS-12 |
| BF | Previous-period data source for growth | WS-12 |
| BG | Stored derived academic percentage integrity | WS-06 |
| BH | Academic result corrections | WS-12 |
| BI | Growth/trend/consistency policy gating | WS-12 |
| BJ | Student pen-picture phase sequencing | WS-12, WS-19 |
| BK | UI features ↔ backend scope | WS-15 |
| BL | PDF/reporting scope | WS-15 |
| BM | AI provider configuration UI scope | WS-15 |
| BN | Frontend implementation plan | WS-16 |
| BO | Pinned technology versions | WS-16 |
| BP | Observability implementation | WS-18 |
| BQ | Domain event async language | WS-02 |
| BR | Durable audit delivery | WS-18 |
| BS | Log/PII data minimization | WS-18 |
| BT | Phase-aware security tests | WS-14 |
| BU | Reset acceptance checkboxes | WS-19 |
| BV | Phase completion evidence | WS-19 |
| BW | Test fixtures after decoupling | WS-14 |
| BX | Benchmark targets provisional | WS-14 |
| BY | Benchmark ground-truth provenance | WS-14 |
| BZ | Privacy in regression replay | WS-14 |
| CA | Deterministic vs generative reproducibility | WS-14 |
| CB | HTTP response semantics | WS-11 |
| CC | Idempotency keys | WS-11 |
| CD | Pagination/sorting contracts | WS-11 |
| CE | Dynamic content localization | WS-17 |
| CF | Pen picture locale | WS-13, WS-17 |
| CG | Stable machine-readable API error codes | WS-11 |
| CH | Polling vs real-time evaluation progress | WS-11 |
| CI | Evidence provenance vs verification semantics | WS-04, WS-15 |
| CJ | Teacher UX wording | WS-15 |
| CK | Confidence UI wording | WS-15 |
| CL | Parameter rubric vs overall rating terminology | WS-15 |
| CM | Override log non-AI parameters | WS-08, WS-15 |
| CN | Enum consistency | WS-20 |
| CO | Entity/field cross-document audit | WS-20 |
| CP | API ↔ implementation phase coverage | WS-19, WS-20 |
| CQ | DB table ↔ implementation phase coverage | WS-19, WS-20 |
| CR | Test ↔ implementation phase alignment | WS-14, WS-20 |
| CS | UI feature ↔ role/permission | WS-15, WS-20 |
| CT | Domain/JPA coupling decision | WS-02 |
| CU | Shared kernel boundaries | WS-02 |
| CV | Cross-module repository access | WS-02 |
| CW | Transactional orchestration | WS-02 |
| CX | AI call timeouts and resource limits | WS-07 |
| CY | Input size validation | WS-18 |
| CZ | AI secret handling | WS-10 |
| DA | Model/prompt change effect on historical data | WS-05 |
| DB | Assessment subject/framework domain invariant | WS-03 |
| DC | Academic record subject ID invariant | WS-03 |
| DD | Derived data storage rules | WS-06 |
| DE | Append-only vs mutable entity classification | WS-03 |
| DF | Architecture freeze checklist | WS-19 |
| DG | Implementation plan status | WS-19 |
| DH | Final deliverable format | All (output requirement) |

**Total issues: 88 (A through DH). All mapped. None omitted.**

---

## Per-Document Change Summary

| Document | Workstreams Touching It |
|----------|------------------------|
| `01-architecture.md` | WS-01, WS-02, WS-03, WS-04, WS-05, WS-08, WS-09, WS-10, WS-18 |
| `02-domain-model.md` | WS-02, WS-03, WS-04, WS-05, WS-06, WS-07, WS-08, WS-12, WS-13, WS-17 |
| `03-database-design.md` | WS-03, WS-04, WS-07, WS-08, WS-09, WS-10, WS-12, WS-13, WS-17, WS-18 |
| `04-api-spec.md` | WS-03, WS-04, WS-05, WS-06, WS-07, WS-08, WS-10, WS-11, WS-12, WS-13, WS-15, WS-17 |
| `05-ai-evaluation-design.md` | WS-04, WS-05, WS-06, WS-07, WS-11, WS-13, WS-18 |
| `06-scoring-engine.md` | WS-02, WS-03, WS-04, WS-05, WS-06, WS-07, WS-12 |
| `07-testing-strategy.md` | WS-02, WS-03, WS-04, WS-07, WS-08, WS-14, WS-18 |
| `08-implementation-plan.md` | WS-02, WS-05, WS-06, WS-07, WS-08, WS-10, WS-12, WS-14, WS-16, WS-18, WS-19 |
| `09-ui-ux-design-system.md` | WS-03, WS-04, WS-05, WS-10, WS-11, WS-15, WS-16, WS-17 |

---

## Structural Decisions (Software Architecture — Resolved by Me)

These do NOT require institutional approval:

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | `QualitativeInput` VO owned by `scoring` module | Breaks scoring→evaluation circular dependency |
| 2 | `RETURNED` is ReviewSession.status, not AssessmentStatus | Simpler state machine; assessment transitions directly back |
| 3 | New result = new immutable row with incremented version | Append-only audit guarantee |
| 4 | Async evaluation via bounded in-process executor + 202 + poll | Simplest v1 without message broker |
| 5 | Pragmatic JPA annotations on domain entities | Documented tradeoff; simpler for modular monolith |
| 6 | Security consumes external UserPrincipal; no local user store | Assessment engine scope; not a user directory product |
| 7 | `09` = design spec for later frontend workstream; no frontend phases in `08` | Scope clarity |
| 8 | PDF export = deferred | Not in backend implementation scope |
| 9 | v1 prompts are platform-global, not institution-scoped | Simplest first; override is future |
| 10 | Reviewer assignment = external/future; not in v1 domain model | Scope reduction |
| 11 | DOCUMENT_REFERENCE = external URI; no file upload in v1 | Scope clarity |
| 12 | CUSTOM scoring strategy removed; v1 uses enumerated safe types | Security |

## Items Policy-Gated (Require Institutional Approval)

| # | Policy | Gate Location |
|---|--------|---------------|
| 1 | Objective normalization/capping formulas | Scoring Policy Gate |
| 2 | Threshold metric behavior | Scoring Policy Gate |
| 3 | Qualitative dimension aggregation discrepancy threshold | Scoring Policy Gate |
| 4 | HYBRID objective/qualitative default proportions | Scoring Policy Gate |
| 5 | Growth canonical normalization formula | Scoring Policy Gate |
| 6 | Trend methodology and thresholds | Scoring Policy Gate |
| 7 | Consistency methodology and thresholds | Scoring Policy Gate |
| 8 | N/A parameter handling policy (exclude vs neutral) | Scoring Policy Gate |
| 9 | Maximum N/A parameter threshold | Scoring Policy Gate |
| 10 | Evidence-strength formula | Scoring Policy Gate |
| 11 | Evaluation-confidence formula | Scoring Policy Gate |
| 12 | Contradiction confidence behavior | Scoring Policy Gate |
| 13 | Low-confidence mandatory-review threshold | Scoring Policy Gate |
| 14 | Student growth classification thresholds | Scoring Policy Gate |
| 15 | Student trend classification thresholds | Scoring Policy Gate |
| 16 | Student consistency classification thresholds | Scoring Policy Gate |

## Execution Order

Workstreams will be executed in the numbered order (WS-01 through WS-20). Within each workstream, documents are edited in precedence order (01 → 02 → 03 → … → 09). WS-20 runs last as a verification pass and may touch any document.

---

## Status

**Plan status:** AWAITING APPROVAL

**Next step after approval:** Begin execution at WS-01.
