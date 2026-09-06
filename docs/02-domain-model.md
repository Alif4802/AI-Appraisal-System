# 02 — Domain Model

## 1. Overview

This document defines the major domain concepts, aggregates, entities, value objects, and their relationships. It covers the shared platform infrastructure and the student academic extension.

---

## 2. Aggregate Map

```text
┌─────────────────────────────────────────────────────────────┐
│                     FRAMEWORK Aggregate                      │
│                                                              │
│  AssessmentFramework (Root)                                  │
│   └── FrameworkVersion                                       │
│        ├── ParameterDefinition[]                             │
│        │    ├── Rubric → RubricLevel[]                       │
│        │    ├── ScoringStrategyConfig                        │
│        │    ├── EvidenceRule[]                                │
│        │    └── EvaluationDimension[] (optional)             │
│        ├── ParameterWeight[]                                 │
│        ├── RatingScale → RatingMapping[]                     │
│        └── ScoringRuleSet                                    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    ASSESSMENT Aggregate                       │
│                                                              │
│  Assessment (Root)                                           │
│   ├── AssessmentSubject (VO)                                 │
│   ├── AssessmentPeriod (VO)                                  │
│   └── AssessmentStatus (VO/enum)                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     EVIDENCE Aggregate                        │
│                                                              │
│  EvidenceItem (Root)                                         │
│   ├── EvidenceSource (VO)                                    │
│   ├── EvidenceVerificationStatus (VO/enum)                   │
│   └── EvidenceParameterMapping[] (Entity)                    │
│        └── EvidenceMappingType (VO/enum)                     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                 EVALUATION RUN Aggregate                      │
│                                                              │
│  EvaluationRun (Root)                                        │
│   ├── AiModelMetadata (VO)                                   │
│   ├── PromptVersionRef (VO)                                  │
│   └── AiParameterEvaluation[] (Entity)                       │
│        ├── AiSuggestedScore (VO)                             │
│        ├── EvidenceStrength (VO/enum)                         │
│        ├── EvaluationConfidence (VO)                          │
│        ├── Justification (VO)                                │
│        ├── Strengths[] (VO)                                  │
│        └── ImprovementAreas[] (VO)                           │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│               ASSESSMENT RESULT Aggregate                    │
│                                                              │
│  AssessmentResult (Root)                                     │
│   ├── ParameterResult[] (Entity)                             │
│   │    ├── CanonicalScore (VO)                               │
│   │    ├── AiSuggestedScore (VO)                             │
│   │    ├── HumanApprovedScore (VO)                           │
│   │    ├── FinalScore (VO)                                   │
│   │    ├── Weight (VO)                                       │
│   │    ├── WeightedScore (VO)                                │
│   │    ├── EvidenceSufficiency (VO/enum)                      │
│   │    └── ScoringStrategyType (VO/enum)                     │
│   ├── OverallScore (VO)                                      │
│   ├── Rating (VO)                                            │
│   └── ResultStatus (VO/enum)                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  REVIEW SESSION Aggregate                     │
│                                                              │
│  ReviewSession (Root)                                        │
│   ├── ReviewerInfo (VO)                                      │
│   ├── ParameterReview[] (Entity)                             │
│   │    ├── ReviewAction (VO/enum)                            │
│   │    ├── OriginalScore (VO)                                │
│   │    ├── ApprovedScore (VO)                                │
│   │    └── OverrideReason (VO)                               │
│   └── OverallOverride (Entity, optional)                     │
│        ├── OriginalOverallScore (VO)                         │
│        ├── OverriddenOverallScore (VO)                       │
│        └── Justification (VO)                                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    PEN PICTURE Aggregate                      │
│                                                              │
│  PenPicture (Root)                                           │
│   ├── PenPictureContent (VO)                                 │
│   ├── GenerationContext (VO)                                 │
│   └── PenPictureVersion (VO)                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              ACADEMIC RECORD Aggregate (Student)             │
│                                                              │
│  AcademicRecord (Root)                                       │
│   ├── SubjectEnrollment[] (Entity)                           │
│   │    └── AcademicResult[] (Entity)                         │
│   │         ├── AcademicAssessmentType (VO/enum)             │
│   │         ├── Score (VO)                                   │
│   │         ├── MaxScore (VO)                                │
│   │         └── AssessmentDate (VO)                          │
│   ├── AttendanceRecord[] (Entity)                            │
│   └── CalculatedAcademicFacts (VO, derived)                  │
│        ├── SubjectPerformance[] (VO)                         │
│        ├── OverallAcademicScore (VO)                         │
│        ├── AcademicGrowth (VO)                               │
│        ├── AcademicTrend (VO)                                │
│        └── AcademicConsistency (VO)                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Detailed Domain Concepts

### 3.1 Framework Domain

#### AssessmentFramework (Aggregate Root)

The top-level container for an institutional assessment configuration.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `institutionId` | `UUID` | Owning institution |
| `code` | `String` | Short code (e.g., `DESCO_EMP_2027`) |
| `name` | `String` | Display name |
| `description` | `String` | Purpose description |
| `domainType` | `DomainType` enum | `EMPLOYEE`, `TEACHER`, `STUDENT` |
| `status` | `FrameworkStatus` | `DRAFT`, `ACTIVE`, `ARCHIVED` |
| `versions` | `List<FrameworkVersion>` | Ordered version history |

#### FrameworkVersion (Entity)

An immutable snapshot of a framework configuration. Once activated, a framework version cannot be modified — only superseded by a new version.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `versionNumber` | `Integer` | Sequential version number |
| `effectiveFrom` | `LocalDate` | When this version becomes applicable |
| `effectiveTo` | `LocalDate` | When superseded (null if current) |
| `status` | `VersionStatus` | `DRAFT`, `ACTIVE`, `SUPERSEDED` |
| `parameters` | `List<ParameterDefinition>` | All parameter definitions |
| `weights` | `List<ParameterWeight>` | Weights for this version |
| `ratingScale` | `RatingScale` | Display rating configuration |
| `scoringRuleSet` | `ScoringRuleSet` | Global scoring rules |
| `activatedAt` | `Instant` | Activation timestamp |
| `activatedBy` | `UUID` | Activating user |

#### ParameterDefinition (Entity)

A versioned assessable construct with a scoring contract.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `code` | `String` | Parameter code (e.g., `LEADERSHIP`) |
| `name` | `String` | Display name |
| `description` | `String` | Full definition |
| `purpose` | `String` | Why this parameter exists |
| `category` | `String` | Grouping category |
| `domainType` | `DomainType` | Applicable domain |
| `scoringStrategyType` | `ScoringStrategyType` | `OBJECTIVE`, `QUALITATIVE_RUBRIC`, `HYBRID`, `DERIVED`, `COMPOSITE` |
| `scoringStrategyConfig` | `ScoringStrategyConfig` (VO) | Strategy-specific configuration (JSON) |
| `rubric` | `Rubric` | Rubric definition |
| `evidenceRules` | `List<EvidenceRule>` | What evidence is eligible |
| `evaluationDimensions` | `List<EvaluationDimension>` | Optional parameter-specific dimensions and weights |
| `minimumEvidenceRequirements` | `MinimumEvidenceRequirements` (VO) | Minimum counts/types |
| `missingDataPolicy` | `MissingDataPolicyType` | What happens when evidence is missing |
| `applicability` | `ApplicabilityRule` (VO) | Conditions for when parameter applies |
| `displayOrder` | `Integer` | Presentation order |
| `active` | `Boolean` | Whether included in scoring |

#### Rubric (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `levels` | `List<RubricLevel>` | Ordered from lowest to highest |

#### RubricLevel (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `level` | `Integer` | Ordinal level (e.g., 1, 2, 3, 4, 5) |
| `label` | `String` | Display label (e.g., "Outstanding") |
| `definition` | `String` | What this level means for this parameter |
| `canonicalScoreMin` | `BigDecimal` | Lower bound on canonical 0–100 scale |
| `canonicalScoreMax` | `BigDecimal` | Upper bound on canonical 0–100 scale |
| `canonicalScoreMidpoint` | `BigDecimal` | Default mapping point |

> **Design Note:** `canonicalScoreMin/Max/Midpoint` is how rubric levels map to the 0–100 canonical scale. This explicitly avoids treating ordinal levels as equally spaced. E.g., Level 1 might map to 0–30, Level 2 to 30–50, Level 3 to 50–70, Level 4 to 70–85, Level 5 to 85–100.

#### ScoringStrategyConfig (Value Object)

Strategy-specific configuration stored as structured JSON. Examples:

- **OBJECTIVE:** `{ "metricType": "PERCENTAGE", "targetValue": 100, "normalizationFormula": "LINEAR" }`
- **QUALITATIVE_RUBRIC:** `{ "dimensionsWeighted": true }`
- **HYBRID:** `{ "objectiveWeight": 0.60, "qualitativeWeight": 0.40 }`
- **DERIVED:** `{ "sourceParameters": ["PARAM_A", "PARAM_B"], "derivationFormula": "GROWTH" }`
- **COMPOSITE:** `{ "componentParameters": ["EXAM", "QUIZ", "ASSIGNMENT"], "componentWeights": {...} }`

#### EvidenceRule (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `eligibleSourceTypes` | `Set<EvidenceSourceType>` | Which source types are eligible |
| `requiredSourceTypes` | `Set<EvidenceSourceType>` | Which source types are required |
| `maximumAge` | `Duration` | How old evidence can be |
| `requiresVerification` | `Boolean` | Must evidence be verified? |

#### EvaluationDimension (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `code` | `String` | Dimension code (e.g., `ACHIEVEMENT`) |
| `name` | `String` | Display name |
| `description` | `String` | What is evaluated |
| `weight` | `BigDecimal` | Dimension weight (must sum to 1.0) |

> **Design Note:** These are NOT hard-coded as Achievement/Quality/Consistency/Impact universally. They are parameter-specific and framework-configurable.

#### ParameterWeight (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `parameterDefinitionId` | `UUID` | Which parameter |
| `weight` | `BigDecimal` | Weight value (all weights in a framework version must sum to 1.0) |

#### RatingScale (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `mappings` | `List<RatingMapping>` | Canonical score → display rating mappings |

#### RatingMapping (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `canonicalScoreMin` | `BigDecimal` | Inclusive lower bound (0–100) |
| `canonicalScoreMax` | `BigDecimal` | Exclusive upper bound (0–100) |
| `ratingValue` | `String` | Display value (e.g., "4", "A", "Exceeds Expectations") |
| `label` | `String` | Description |
| `displayOrder` | `Integer` | Presentation order |

---

### 3.2 Assessment Domain

#### Assessment (Aggregate Root)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `institutionId` | `UUID` | Owning institution |
| `frameworkVersionId` | `UUID` | Frozen framework version |
| `subject` | `AssessmentSubject` (VO) | Who is being assessed |
| `period` | `AssessmentPeriod` (VO) | Assessment time window |
| `status` | `AssessmentStatus` | Current lifecycle state |
| `createdBy` | `UUID` | Creating user |
| `createdAt` | `Instant` | Creation timestamp |

#### AssessmentSubject (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `externalSubjectId` | `String` | Reference to HR/LMS subject |
| `subjectType` | `SubjectType` | `EMPLOYEE`, `TEACHER`, `STUDENT` |
| `displayName` | `String` | Name for display |
| `departmentOrClass` | `String` | Organizational unit |

#### AssessmentPeriod (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `startDate` | `LocalDate` | Period start |
| `endDate` | `LocalDate` | Period end |
| `label` | `String` | e.g., "Annual 2027", "Q1 2027" |

#### AssessmentStatus (Enum)

```text
CREATED
  → EVIDENCE_COLLECTION
    → EVALUATION_READY
      → EVALUATING
        → EVALUATED
          → UNDER_REVIEW
            → APPROVED
              → FINALIZED

Special transitions:
  UNDER_REVIEW → RETURNED (back to EVIDENCE_COLLECTION or EVALUATION_READY)
  Any non-final state → CANCELLED
```

---

### 3.3 Evidence Domain

#### EvidenceItem (Aggregate Root)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `assessmentId` | `UUID` | Owning assessment |
| `institutionId` | `UUID` | Institution |
| `source` | `EvidenceSource` (VO) | Provenance |
| `content` | `String` | Evidence content (text, or structured data as JSON) |
| `contentType` | `EvidenceContentType` | `TEXT`, `METRIC`, `STRUCTURED`, `DOCUMENT_REFERENCE` |
| `verificationStatus` | `EvidenceVerificationStatus` | `UNVERIFIED`, `VERIFIED`, `REJECTED` |
| `evidenceDate` | `LocalDate` | When evidence occurred |
| `submittedAt` | `Instant` | Submission timestamp |
| `submittedBy` | `UUID` | Submitting user |
| `metadata` | `Map<String, String>` | Flexible metadata |

#### EvidenceSource (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `sourceType` | `EvidenceSourceType` | `SYSTEM_GENERATED`, `OFFICIAL_RECORD`, `SUPERVISOR`, `TEACHER`, `PEER`, `SELF_REPORTED`, `EXTERNAL`, `DERIVED` |
| `sourceIdentifier` | `String` | Who/what provided it |
| `reliability` | `EvidenceReliability` | `HIGH`, `MEDIUM`, `LOW`, `UNKNOWN` |

#### EvidenceParameterMapping (Entity)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `evidenceItemId` | `UUID` | Evidence item |
| `parameterDefinitionId` | `UUID` | Target parameter |
| `mappingType` | `EvidenceMappingType` | `PRIMARY`, `SUPPORTING`, `CONTRADICTORY`, `CONTEXTUAL` |
| `mappedBy` | `MappingSource` | `MANUAL`, `AI_SUGGESTED`, `RULE_BASED` |
| `mappedAt` | `Instant` | Mapping timestamp |

#### EvidenceSet (Domain Service / Transient)

Not persisted directly. Assembled at evaluation time by collecting all eligible `EvidenceItem` instances for a given parameter + assessment context, respecting `EvidenceRule` constraints.

| Field | Type | Description |
|-------|------|-------------|
| `parameterDefinitionId` | `UUID` | Target parameter |
| `assessmentId` | `UUID` | Assessment context |
| `items` | `List<EvidenceItem>` | Eligible evidence |
| `sufficiency` | `EvidenceSufficiency` | `SUFFICIENT`, `INSUFFICIENT`, `MISSING_REQUIRED`, `NOT_APPLICABLE` |

---

### 3.4 Scoring Domain

#### ParameterResult (Entity, within AssessmentResult aggregate)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `assessmentResultId` | `UUID` | Parent result |
| `parameterDefinitionId` | `UUID` | Which parameter |
| `scoringStrategyType` | `ScoringStrategyType` | Strategy used |
| `rawValue` | `BigDecimal` | Raw metric/value (for objective parameters; null for qualitative) |
| `normalizedScore` | `BigDecimal` | Canonical 0–100 score (output of strategy execution) |
| `aiSuggestedScore` | `BigDecimal` | AI's suggestion (canonical; null for OBJECTIVE) |
| `preReviewScore` | `BigDecimal` | Score at the time review begins (= `normalizedScore`). Strategy-neutral. |
| `humanApprovedScore` | `BigDecimal` | Reviewer's approved score (null if not yet reviewed) |
| `finalScore` | `BigDecimal` | Effective score: `humanApprovedScore` if present, else `normalizedScore` |
| `configuredWeight` | `BigDecimal` | Framework-defined parameter weight |
| `effectiveWeight` | `BigDecimal` | Actual weight after N/A redistribution (recorded for audit) |
| `weightedScore` | `BigDecimal` | `finalScore` × `effectiveWeight` |
| `status` | `ParameterResultStatus` | See enum below |
| `evidenceSufficiency` | `EvidenceSufficiency` | Evidence state |
| `evidenceStrength` | `EvidenceStrengthLevel` | `HIGH`, `MEDIUM`, `LOW`, `INSUFFICIENT` |
| `evaluationConfidence` | `BigDecimal` | Platform-computed confidence (0.0–1.0). Formula is POLICY-GATED. |
| `rubricLevel` | `Integer` | Platform-resolved rubric level (from normalizedScore) |
| `displayRating` | `String` | Mapped display rating label |
| `calculatedAt` | `Instant` | Calculation timestamp |

> **Invariant:** `finalScore` is ALWAYS deterministic: `humanApprovedScore` if non-null, else `normalizedScore`. It is never `aiSuggestedScore` directly — AI suggestions enter scoring as input to the strategy, which produces `normalizedScore`.

#### ParameterResultStatus (Enum)

| Value | Meaning |
|-------|-------|
| `SCORED` | Score calculated successfully |
| `INSUFFICIENT_EVIDENCE` | Evidence below minimum requirements; scored with missing-data policy |
| `NOT_APPLICABLE` | Parameter not applicable to this subject/assessment |
| `EVALUATION_FAILED` | AI evaluation failed for this parameter; may require manual review |
| `EXCLUDED` | Excluded from overall calculation (weight redistributed) |

#### AssessmentResult (Aggregate Root)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `assessmentId` | `UUID` | Owning assessment |
| `frameworkVersionId` | `UUID` | Framework version used |
| `evaluationRunId` | `UUID` | Source evaluation (null for pure-objective recalculations) |
| `parameterResults` | `List<ParameterResult>` | Per-parameter results |
| `overallScore` | `BigDecimal` | Weighted sum of `finalScore` × `effectiveWeight` across active parameters |
| `overallRatingValue` | `String` | Mapped rating code from RatingScale |
| `overallRatingLabel` | `String` | Human-readable rating label |
| `completeness` | `AssessmentResultCompleteness` | `COMPLETE`, `PARTIAL`, `BLOCKED` |
| `calculatedAt` | `Instant` | Calculation timestamp |
| `version` | `Integer` | Immutable result version (new version on recalculation) |

> **Immutability Invariant:** Once persisted, an `AssessmentResult` is never modified. Recalculation produces a NEW `AssessmentResult` with an incremented `version`. All versions are retained for audit.

#### AssessmentResultCompleteness (Enum)

| Value | Meaning |
|-------|---------|
| `COMPLETE` | All active parameters scored successfully |
| `PARTIAL` | Some parameters scored, others failed or excluded |
| `BLOCKED` | Insufficient parameters scored to produce a meaningful overall result |

#### QualitativeEvaluationInput (Value Object — Scoring Module Owned)

The scoring module's input contract for AI evaluation results. Defined in the scoring module, NOT in evaluation or AI modules. The evaluation module maps `AiParameterEvaluation` → `QualitativeEvaluationInput` before invoking scoring.

| Field | Type | Description |
|-------|------|-------------|
| `suggestedScore` | `BigDecimal` | AI-suggested canonical score (0–100) |
| `suggestedRubricLevel` | `Integer` | AI-suggested rubric level |
| `aiReportedConfidence` | `BigDecimal` | AI's self-reported confidence (0.0–1.0) |
| `aiReportedEvidenceStrength` | `EvidenceStrengthLevel` | AI's assessment of evidence quality |
| `dimensionScores` | `Map<String, BigDecimal>` | Per-dimension scores (if applicable) |

> **Design Note:** This VO decouples scoring from AI implementation. Scoring strategies consume `QualitativeEvaluationInput` and produce `normalizedScore`. The VO contains no justification text, no evidence references, no raw AI response — only the mathematical inputs needed for scoring.

#### ScoringStrategy (Interface)

```java
public interface ScoringStrategy {
    ScoringStrategyType getType();
    ParameterScoreResult calculate(ScoringContext context);
}
```

Where `ScoringContext` contains:
- `ParameterDefinition` (with rubric, strategy config)
- `EvidenceSet` (assembled evidence for sufficiency/strength)
- `QualitativeEvaluationInput` (scoring-owned VO; null for pure OBJECTIVE)
- `ObjectiveFacts` (pre-calculated deterministic metrics, if any)

And `ParameterScoreResult` contains:
- `normalizedScore` (BigDecimal 0–100)
- `rubricLevel` (Integer — platform-resolved from normalizedScore)
- `status` (`ParameterResultStatus`)
- `evidenceSufficiency`
- `calculationMetadata` (for audit)

---

### 3.5 Evaluation Domain

#### EvaluationRun (Aggregate Root)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `assessmentId` | `UUID` | Assessment being evaluated |
| `frameworkVersionId` | `UUID` | Framework version |
| `status` | `EvaluationStatus` | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED`, `PARTIALLY_COMPLETED`, `STALE` |
| `aiModelMetadata` | `AiModelMetadata` (VO) | Model config used |
| `promptVersionRef` | `PromptVersionRef` (VO) | Prompt version |
| `parameterEvaluations` | `List<AiParameterEvaluation>` | Per-parameter AI results |
| `aiInteractions` | `List<AiInteraction>` | Per-call audit records |
| `evaluationInputSnapshot` | `EvaluationInputSnapshot` (VO) | Immutable record of input context |
| `startedAt` | `Instant` | Start time |
| `completedAt` | `Instant` | Completion time |
| `requestedBy` | `UUID` | Requesting user |
| `requestedLocale` | `String` | Locale for AI qualitative interpretation (e.g., `en`, `bn`) |

> **Crash Recovery:** On application startup, all `EvaluationRun` records with status `IN_PROGRESS` and `startedAt` older than a configurable threshold are transitioned to `STALE`. A `STALE` run can be retried (creates a new `EvaluationRun`) or abandoned. This prevents orphaned evaluations from blocking assessments.

#### AiParameterEvaluation (Entity, Immutable)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `parameterDefinitionId` | `UUID` | Parameter evaluated |
| `suggestedScore` | `BigDecimal` | AI-suggested canonical score (0–100) |
| `suggestedRubricLevel` | `Integer` | AI-suggested rubric level (diagnostic) |
| `confidence` | `BigDecimal` | AI-reported confidence (0.0–1.0; diagnostic, not calibrated) |
| `evidenceStrength` | `EvidenceStrengthLevel` | AI assessment of evidence strength (diagnostic) |
| `justification` | `String` | AI explanation (may be null if AI declined) |
| `strengths` | `List<String>` | Identified strengths (may be empty — not forced) |
| `improvementAreas` | `List<String>` | Development areas (may be empty — not forced) |
| `dimensionScores` | `Map<String, BigDecimal>` | Per-dimension scores if applicable |
| `evidenceReferences` | `List<UUID>` | Evidence items referenced by AI |
| `status` | `ParameterEvaluationStatus` | `COMPLETED`, `FAILED`, `SKIPPED` |

> **Invariant:** Once persisted, an `AiParameterEvaluation` is never modified. It is an immutable historical record.

#### AiInteraction (Entity, Immutable)

Per-AI-call audit record. One EvaluationRun may produce multiple AiInteraction records (one per parameter, or one per batch, plus retries).

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `evaluationRunId` | `UUID` | Parent evaluation run |
| `parameterDefinitionId` | `UUID` | Target parameter (null for batch calls) |
| `requestTimestamp` | `Instant` | When the AI call was made |
| `responseTimestamp` | `Instant` | When the AI response was received |
| `durationMs` | `Long` | Call duration in milliseconds |
| `providerType` | `String` | AI provider type |
| `modelName` | `String` | Model used |
| `promptVersionRef` | `String` | Prompt template version |
| `inputTokenCount` | `Integer` | Approximate input tokens |
| `outputTokenCount` | `Integer` | Approximate output tokens |
| `httpStatus` | `Integer` | HTTP status code (or equivalent) |
| `success` | `Boolean` | Whether the call produced valid output |
| `failureReason` | `String` | Error description if failed |
| `attemptNumber` | `Integer` | Retry attempt number (1 = first try) |

> **Design Note:** `AiInteraction` does NOT store raw prompt/response content at the domain level. Raw AI content is stored in `rawAiResponse` on successful `AiParameterEvaluation` records. This keeps `AiInteraction` focused on operational audit (latency, cost, reliability) while `AiParameterEvaluation` holds evaluation-quality audit.

#### EvaluationInputSnapshot (Value Object, Immutable)

Captures the complete input context used for an evaluation, enabling historical reproducibility. Stored as structured JSON on `EvaluationRun`.

| Field | Type | Description |
|-------|------|-------------|
| `frameworkVersionId` | `UUID` | Framework version used |
| `parameterDefinitionIds` | `List<UUID>` | Parameters evaluated |
| `evidenceItemIds` | `List<UUID>` | Evidence items available at evaluation time |
| `evidenceVerificationStates` | `Map<UUID, String>` | Verification status of each evidence item at snapshot time |
| `objectiveFacts` | `Map<String, BigDecimal>` | Pre-calculated deterministic facts (attendance %, etc.) |
| `academicFacts` | `Map<String, Object>` | Academic calculation results (student domain) |
| `locale` | `String` | Requested locale |
| `snapshotTimestamp` | `Instant` | When the snapshot was captured |

> **Reproducibility Invariant:** Given the same `EvaluationInputSnapshot` content and the same AI model/prompt, a re-evaluation SHOULD produce comparable results. Deterministic scoring replay using stored `AiParameterEvaluation` output MUST produce identical results.

#### AiModelMetadata (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `providerType` | `String` | e.g., "gemini", "claude", "qwen" |
| `modelName` | `String` | e.g., "gemini-2.5-pro" |
| `modelVersion` | `String` | Version string |
| `temperature` | `BigDecimal` | Temperature setting used |
| `additionalConfig` | `Map<String, String>` | Other config |

---

### 3.6 Review Domain

#### ReviewSession (Aggregate Root)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `assessmentId` | `UUID` | Assessment reviewed |
| `assessmentResultId` | `UUID` | Result being reviewed |
| `reviewer` | `ReviewerInfo` (VO) | Who reviewed |
| `parameterReviews` | `List<ParameterReview>` | Per-parameter decisions |
| `overallOverride` | `OverallOverride` (opt.) | Exceptional overall override |
| `status` | `ReviewStatus` | `IN_PROGRESS`, `COMPLETED`, `RETURNED` |
| `startedAt` | `Instant` | Review start |
| `completedAt` | `Instant` | Review completion |
| `comments` | `String` | General comments |

#### ParameterReview (Entity)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `parameterDefinitionId` | `UUID` | Parameter |
| `action` | `ReviewAction` | `APPROVED`, `MODIFIED`, `RETURNED` |
| `preReviewScore` | `BigDecimal` | Score before review (= `ParameterResult.normalizedScore`). Strategy-neutral. |
| `approvedScore` | `BigDecimal` | Reviewer-approved score (= `preReviewScore` if APPROVED; different if MODIFIED) |
| `overrideReason` | `String` | Mandatory if `MODIFIED`; describes why score was changed |
| `scoringStrategyType` | `ScoringStrategyType` | Strategy of the parameter (for strategy-aware override validation) |
| `reviewedAt` | `Instant` | Timestamp |

#### OverallOverride (Entity)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `originalOverallScore` | `BigDecimal` | Before override |
| `overriddenOverallScore` | `BigDecimal` | After override |
| `justification` | `String` | Mandatory |
| `approvedBy` | `UUID` | Senior reviewer |
| `approvedAt` | `Instant` | Timestamp |

---

### 3.7 Pen Picture Domain

#### PenPicture (Aggregate Root)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `assessmentId` | `UUID` | Assessment |
| `assessmentResultId` | `UUID` | Result used |
| `domainType` | `DomainType` | Employee/Teacher/Student |
| `content` | `String` | Generated pen picture text |
| `generationContext` | `PenPictureGenerationContext` (VO) | Input summary |
| `aiModelMetadata` | `AiModelMetadata` (VO) | Model used |
| `promptVersionRef` | `PromptVersionRef` (VO) | Prompt version |
| `version` | `Integer` | Pen picture version |
| `generatedAt` | `Instant` | Generation time |
| `generatedBy` | `UUID` | Requesting user |

---

### 3.8 Student Academic Extension

#### AcademicRecord (Aggregate Root)

Represents a student's academic data for an assessment period.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `assessmentId` | `UUID` | Linked assessment |
| `studentSubjectId` | `String` | External student ID |
| `academicYear` | `String` | Academic year |
| `grade` | `String` | Class/grade |
| `section` | `String` | Section |
| `enrollments` | `List<SubjectEnrollment>` | Subject registrations |
| `attendanceRecords` | `List<AttendanceRecord>` | Attendance data |

#### SubjectEnrollment (Entity)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `subjectCode` | `String` | Subject code |
| `subjectName` | `String` | Subject name |
| `results` | `List<AcademicResult>` | Assessment results |

#### AcademicResult (Entity)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `assessmentType` | `AcademicAssessmentType` | `EXAM`, `MIDTERM`, `CLASS_TEST`, `QUIZ`, `ASSIGNMENT`, `CLASSWORK`, `PROJECT`, `PRACTICAL`, `LAB` |
| `title` | `String` | e.g., "Final Exam", "Quiz 3" |
| `score` | `BigDecimal` | Obtained score |
| `maxScore` | `BigDecimal` | Maximum possible |
| `percentage` | `BigDecimal` | Calculated percentage |
| `assessmentDate` | `LocalDate` | When taken |
| `weight` | `BigDecimal` | Weight within subject (if applicable) |

#### AttendanceRecord (Entity)

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique identifier |
| `period` | `AttendancePeriod` | Month or term |
| `totalDays` | `Integer` | Total working days |
| `presentDays` | `Integer` | Days present |
| `absentDays` | `Integer` | Days absent |
| `lateDays` | `Integer` | Days late |
| `attendancePercentage` | `BigDecimal` | Calculated |

#### CalculatedAcademicFacts (Value Object, Derived)

Deterministically calculated by the academic module:

| Field | Type | Description |
|-------|------|-------------|
| `subjectPerformances` | `List<SubjectPerformance>` | Per-subject results |
| `overallAcademicScore` | `BigDecimal` | Weighted academic score |
| `academicGrowth` | `AcademicGrowth` (VO) | Improvement metrics |
| `academicTrend` | `AcademicTrend` (VO) | Trend direction |
| `academicConsistency` | `AcademicConsistency` (VO) | Consistency metrics |
| `overallAttendancePercentage` | `BigDecimal` | Attendance summary |
| `strengths` | `List<String>` | Strong subjects |
| `developmentAreas` | `List<String>` | Weak subjects |

#### SubjectPerformance (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `subjectCode` | `String` | Subject |
| `subjectName` | `String` | Name |
| `weightedAverage` | `BigDecimal` | Weighted average across assessment types |
| `examScore` | `BigDecimal` | Exam performance |
| `continuousAssessmentScore` | `BigDecimal` | Non-exam performance |
| `trend` | `TrendDirection` | `IMPROVING`, `STABLE`, `DECLINING` |
| `growth` | `BigDecimal` | Change from previous period |

#### AcademicGrowth (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `previousOverallScore` | `BigDecimal` | Previous period score |
| `currentOverallScore` | `BigDecimal` | Current period score |
| `absoluteGrowth` | `BigDecimal` | Difference |
| `percentageGrowth` | `BigDecimal` | Percentage change |
| `growthCategory` | `GrowthCategory` | `SIGNIFICANT_IMPROVEMENT`, `MODERATE_IMPROVEMENT`, `STABLE`, `MODERATE_DECLINE`, `SIGNIFICANT_DECLINE` |

---

## 4. Assessment Lifecycle State Machine

```text
    ┌──────────┐
    │ CREATED  │
    └────┬─────┘
         │  (evidence submission begins)
         ▼
┌──────────────────┐
│EVIDENCE_COLLECTION│◄──────────────┐
└────────┬─────────┘               │
         │  (sufficiency satisfied    │  (RETURNED from review)
         │   or manually marked)     │
         ▼                          │
┌──────────────────┐               │
│EVALUATION_READY  │               │
└────────┬─────────┘               │
         │  (evaluation triggered)  │
         ▼                          │
┌──────────────────┐               │
│   EVALUATING     │               │
└────────┬─────────┘               │
         │  (AI + scoring complete) │
         ▼                          │
┌──────────────────┐               │
│   EVALUATED      │               │
└────────┬─────────┘               │
         │  (sent for review)       │
         ▼                          │
┌──────────────────┐               │
│  UNDER_REVIEW    │───────────────┘
└────────┬─────────┘
         │  (all parameters reviewed)
         ▼
┌──────────────────┐
│    APPROVED      │
└────────┬─────────┘
         │  (pen picture generated, assessment locked)
         ▼
┌──────────────────┐
│   FINALIZED      │
└──────────────────┘

Any non-final state → CANCELLED
```

### State Transition Rules

| Transition | Trigger | Guard Condition |
|-----------|---------|----------------|
| `CREATED` → `EVIDENCE_COLLECTION` | First evidence submitted | None |
| `EVIDENCE_COLLECTION` → `EVALUATION_READY` | Evidence sufficiency check or manual mark | Evidence sufficiency satisfied for evaluable parameters, OR assessor explicitly marks ready |
| `EVALUATION_READY` → `EVALUATING` | Evaluation triggered | No `IN_PROGRESS` evaluation exists for this assessment |
| `EVALUATING` → `EVALUATED` | Evaluation + scoring complete | `EvaluationRun` status = `COMPLETED` or `PARTIALLY_COMPLETED`, `AssessmentResult` persisted |
| `EVALUATED` → `UNDER_REVIEW` | Sent for review | At least one parameter result exists |
| `UNDER_REVIEW` → `RETURNED` | Reviewer returns | Any `ParameterReview.action` = `RETURNED` |
| `RETURNED` → `EVIDENCE_COLLECTION` | Evidence correction begins | Assessment status reset; evidence may be added/corrected |
| `UNDER_REVIEW` → `APPROVED` | Review completed | All parameters reviewed; no `RETURNED` actions; `ReviewSession.status` = `COMPLETED` |
| `APPROVED` → `FINALIZED` | Finalization triggered | Pen picture generated (or explicitly waived); authorized user confirms |
| Any non-final → `CANCELLED` | Cancellation | Authorized user; assessment not `FINALIZED` |

---

## 5. Key Relationships

```text
Institution ──1:N──► AssessmentFramework
AssessmentFramework ──1:N──► FrameworkVersion
FrameworkVersion ──1:N──► ParameterDefinition
ParameterDefinition ──1:1──► Rubric ──1:N──► RubricLevel
FrameworkVersion ──1:N──► ParameterWeight

Assessment ──N:1──► FrameworkVersion
Assessment ──1:N──► EvidenceItem
EvidenceItem ──N:M──► ParameterDefinition  (via EvidenceParameterMapping)

Assessment ──1:N──► EvaluationRun
EvaluationRun ──1:N──► AiParameterEvaluation

Assessment ──1:N──► AssessmentResult
AssessmentResult ──1:N──► ParameterResult

Assessment ──1:N──► ReviewSession
ReviewSession ──1:N──► ParameterReview

Assessment ──1:N──► PenPicture

Assessment ──0:1──► AcademicRecord  (student only)
AcademicRecord ──1:N──► SubjectEnrollment
SubjectEnrollment ──1:N──► AcademicResult
```
