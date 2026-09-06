# 06 — Scoring Engine

## 1. Scoring Engine Responsibilities

The Scoring Engine is the **deterministic mathematical core** of the platform. It owns all arithmetic, normalization, weighting, conversion, and final-score calculation. It never calls AI. It never makes qualitative judgments.

| Responsibility | Description |
|----------------|-------------|
| Parameter-level scoring | Executes the appropriate scoring strategy for each parameter |
| Score normalization | Converts raw values to the canonical 0–100 scale |
| Weighted aggregation | Applies parameter weights to calculate overall scores |
| Missing-evidence policy | Applies configurable rules when evidence is missing |
| Evidence sufficiency evaluation | Determines if evidence meets minimum requirements |
| Rating/grade conversion | Maps canonical scores to display ratings via configured rating scales |
| Human-approved recalculation | Recalculates overall score when parameter overrides are applied |
| Composite/derived scoring | Calculates scores that depend on other parameters or components |
| Rounding | Applies consistent rounding rules |
| Validation | Validates all scoring inputs and outputs |
| Audit metadata | Records calculation inputs and steps for traceability |

### What the Scoring Engine Does NOT Do

- Interpret qualitative evidence (AI responsibility)
- Assign qualitative scores (AI responsibility)
- Decide what evidence means (AI responsibility)
- Make review decisions (Review module)
- Store evidence (Evidence module)
- Manage assessment lifecycle (Assessment module)

---

## 2. Canonical Score Representation

### 2.1 Internal Representation

```text
Type:       BigDecimal
Scale:      0.0000 – 100.0000
Precision:  NUMERIC(7,4)
Rounding:   HALF_UP at final display
```

### 2.2 Why Not 1–5?

The 1–5 rubric scale is **ordinal**, not interval. There is no mathematical guarantee that the difference between Level 1 and Level 2 equals the difference between Level 4 and Level 5.

Example of the problem with 1–5 as math foundation:

```text
If Level 1 = "Unsatisfactory" and Level 2 = "Needs Improvement"
and Level 4 = "Exceeds Expectations" and Level 5 = "Outstanding"

Then: 5 - 4 = 1 and 2 - 1 = 1
But the performance gap between Outstanding and Exceeds is NOT
the same as the gap between Needs Improvement and Unsatisfactory.
```

A 0–100 canonical scale allows:
- Non-linear mapping from rubric levels
- Precise weighted calculations
- Flexible institutional display mapping
- No systematic errors from treating ordinal data as interval data

### 2.3 Rubric-to-Canonical Mapping

Each rubric level maps to a range on the canonical scale:

```text
Level 1 (Unsatisfactory):        0.0000 –  30.0000  (midpoint: 15.0000)
Level 2 (Needs Improvement):    30.0000 –  50.0000  (midpoint: 40.0000)
Level 3 (Meets Expectations):   50.0000 –  70.0000  (midpoint: 60.0000)
Level 4 (Exceeds Expectations): 70.0000 –  85.0000  (midpoint: 77.5000)
Level 5 (Outstanding):          85.0000 – 100.0000  (midpoint: 92.5000)
```

> These ranges are **framework-configurable**, not hard-coded. The ranges above are sensible defaults but can be changed per framework version.

> The midpoint is the default canonical score assigned when AI suggests a rubric level without providing a precise canonical score.

---

## 3. Scoring Strategy Architecture

### 3.1 Strategy Interface

```java
public interface ScoringStrategy {

    ScoringStrategyType getType();

    ParameterScoreResult calculate(ScoringContext context);

    /**
     * Whether this strategy requires AI evaluation input.
     * OBJECTIVE and DERIVED strategies return false.
     */
    boolean requiresAiEvaluation();
}
```

### 3.2 ScoringContext

```java
public record ScoringContext(
    ParameterDefinition parameterDefinition,
    EvidenceSet evidenceSet,
    AiParameterEvaluation aiEvaluation,      // null for pure OBJECTIVE/DERIVED
    Map<String, ParameterScoreResult> dependencyResults,  // for DERIVED/COMPOSITE
    ObjectiveFacts objectiveFacts,            // pre-calculated metrics
    ScoringRuleSet globalRules
) {}
```

### 3.3 ParameterScoreResult

```java
public record ParameterScoreResult(
    BigDecimal normalizedScore,              // 0–100
    Integer rubricLevel,                      // mapped rubric level
    EvidenceSufficiency evidenceSufficiency,
    EvidenceStrengthLevel evidenceStrength,
    BigDecimal evaluationConfidence,          // 0.0–1.0
    CalculationMetadata metadata              // audit trail
) {}
```

### 3.4 Strategy Registry

```java
@Component
public class ScoringStrategyRegistry {

    private final Map<ScoringStrategyType, ScoringStrategy> strategies;

    public ScoringStrategyRegistry(List<ScoringStrategy> strategies) {
        this.strategies = strategies.stream()
            .collect(Collectors.toMap(ScoringStrategy::getType, Function.identity()));
    }

    public ScoringStrategy resolve(ScoringStrategyType type) {
        ScoringStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new UnsupportedScoringStrategyException(type);
        }
        return strategy;
    }
}
```

---

## 4. Scoring Strategies

### 4.1 OBJECTIVE Strategy

**Purpose:** Score parameters where the value is a measurable, verifiable metric.

**Examples:** Attendance (94%), Target Achievement (96/100), Training Hours (40).

**Algorithm:**

```text
1. Extract raw metric value from evidence/objective facts
2. Normalize to 0–100 canonical scale
   - PERCENTAGE type: rawValue directly (if already 0–100)
   - RATIO type: (achieved / target) × 100
   - THRESHOLD type: compare against defined thresholds
3. Map to rubric level using configured ranges
4. Set evaluationConfidence = 1.0 (deterministic)
5. Set evidenceSufficiency based on data availability
```

```text
SCORING POLICY TO BE FINALIZED:
- Normalization formulas for different metric types
- Capping rules (can a score exceed 100 if achievement exceeds target?)
- Threshold definitions for non-linear objective metrics
```

**AI evaluation required:** No

---

### 4.2 QUALITATIVE_RUBRIC Strategy

**Purpose:** Score parameters where evidence is qualitative and requires AI interpretation against a rubric.

**Examples:** Leadership, Communication, Innovation, Teamwork.

**Algorithm:**

```text
1. Receive AI evaluation result (suggestedScore, suggestedRubricLevel, confidence, dimensionScores)
2. Validate:
   a. suggestedScore falls within valid rubric range (0–100)
   b. suggestedRubricLevel is consistent with suggestedScore
3. If evaluation dimensions are configured:
   a. Validate dimension scores exist for all configured dimensions
   b. Calculate weighted dimension score:
      canonicalScore = Σ(dimensionScore_i × dimensionWeight_i)
   c. Cross-check against AI's overall suggestedScore
      If discrepancy > threshold → use calculated dimension-weighted score
4. Apply evidence sufficiency adjustment:
   a. If INSUFFICIENT → flag, apply missing-data policy
5. The AI suggestedScore (validated/adjusted) becomes the normalizedScore
6. Map to rubric level
7. Set evaluationConfidence from AI + evidence sufficiency factors
```

```text
SCORING POLICY TO BE FINALIZED:
- Discrepancy threshold between dimension-weighted and overall AI score
- Whether dimension scores are mandatory when dimensions are configured
- Evidence sufficiency adjustment formula (if any)
- Confidence calculation formula incorporating both AI confidence and evidence quality
```

**AI evaluation required:** Yes

---

### 4.3 HYBRID Strategy

**Purpose:** Score parameters that have both objective metrics and qualitative evidence.

**Examples:** Teaching Effectiveness (student outcomes + classroom observation), Work Quality (error rates + supervisor assessment).

**Algorithm:**

```text
1. Calculate objective component:
   a. Extract metric from objective facts
   b. Normalize to 0–100
   c. objectiveScore = normalized objective value

2. Receive AI evaluation for qualitative component:
   a. qualitativeScore = AI suggestedScore (validated)

3. Combine using configured weights:
   hybridScore = (objectiveScore × objectiveWeight) + (qualitativeScore × qualitativeWeight)

   Where objectiveWeight + qualitativeWeight = 1.0
   (from ScoringStrategyConfig)

4. Map to rubric level

5. Set confidence:
   - Objective component confidence = 1.0
   - Qualitative component confidence = AI confidence
   - Combined confidence = weighted average
```

```text
SCORING POLICY TO BE FINALIZED:
- Default objectiveWeight / qualitativeWeight split
- Whether weights can be overridden per parameter instance
- Whether the hybrid formula is always linear weighted or can be configurable
```

**AI evaluation required:** Yes (for qualitative component)

---

### 4.4 DERIVED Strategy

**Purpose:** Score parameters whose value is mathematically derived from other parameters or calculated facts.

**Examples:** Learning Growth (derived from current vs. previous performance), Consistency (derived from score variation across parameters/time periods).

**Algorithm:**

```text
1. Retrieve dependency parameter results from context
2. Apply derivation formula:
   - GROWTH: currentScore - previousScore (or percentage change)
   - CONSISTENCY: 100 - (coefficientOfVariation × adjustmentFactor)
   - TREND: slope of time-series regression, normalized
   - CUSTOM: evaluate configured formula

3. Normalize result to 0–100
4. Map to rubric level
5. Set confidence = 1.0 (deterministic)
6. Set evidenceSufficiency based on dependency availability
```

```text
SCORING POLICY TO BE FINALIZED:
- Growth formula: absolute difference vs. percentage change vs. effect size
- Consistency formula: coefficient of variation, standard deviation, or range-based
- Trend formula: simple slope, weighted recent, or regression
- Normalization for growth (what growth % maps to what canonical score?)
- How to handle missing dependency parameters
```

**AI evaluation required:** No

---

### 4.5 COMPOSITE Strategy

**Purpose:** Score parameters that aggregate multiple sub-component scores with defined weights.

**Examples:** Academic Performance (exam 40% + midterm 20% + quiz 15% + classwork 10% + assignment 15%), Overall Subject Score.

**Algorithm:**

```text
1. Retrieve component scores from:
   a. Other parameter results, OR
   b. Academic facts (for student), OR
   c. Objective metrics

2. Apply configured component weights:
   compositeScore = Σ(componentScore_i × componentWeight_i)

   Where Σ componentWeight_i = 1.0

3. Validate all required components are present
   a. If any component missing → apply missing-component policy

4. Normalize (if not already 0–100)
5. Map to rubric level
6. Set confidence based on component completeness
```

**AI evaluation required:** No (components are pre-calculated)

---

## 5. Parameter Weighting

### 5.1 Weight Contract

```text
For a given FrameworkVersion:
  Σ(weight_i for all active parameters) = 1.0000

  Each weight_i ∈ (0.0000, 1.0000)
  Precision: NUMERIC(5,4)
```

### 5.2 Weighted Overall Score Calculation

```text
overallScore = Σ(parameterFinalScore_i × parameterWeight_i)

Where parameterFinalScore_i is:
  - humanApprovedScore if human reviewed and approved/modified
  - normalizedScore (from scoring strategy) otherwise
```

### 5.3 Handling N/A Parameters

When a parameter's `evidenceSufficiency = NOT_APPLICABLE`:

```text
Option A: Exclude and redistribute weight
  adjustedWeight_i = weight_i / (1.0 - Σ excludedWeights)
  overallScore = Σ(score_i × adjustedWeight_i) for applicable parameters only

Option B: Assign neutral score
  score_i = frameworkNeutralScore (e.g., 60.0)
  Use original weight
```

```text
SCORING POLICY TO BE FINALIZED:
- Which option (A or B) is the default
- Whether this is configurable per framework
- How many parameters can be N/A before the assessment is invalid
```

---

## 6. Missing Evidence Behavior

### 6.1 Evidence Sufficiency States

| State | Meaning | Scoring Behavior |
|-------|---------|------------------|
| `SUFFICIENT` | Meets all minimum requirements | Normal scoring |
| `INSUFFICIENT` | Below minimum but some evidence exists | Apply missing-data policy |
| `MISSING_REQUIRED` | Required source types missing | Apply missing-data policy |
| `NOT_APPLICABLE` | Parameter doesn't apply to this subject | Exclude or neutral (per policy) |

### 6.2 Missing Data Policies

| Policy | Behavior |
|--------|----------|
| `FLAG_FOR_REVIEW` | Score normally but flag for mandatory human review; set low confidence |
| `ASSIGN_MINIMUM` | Assign the minimum canonical score (e.g., 0.0) |
| `ASSIGN_NEUTRAL` | Assign a neutral canonical score (e.g., 50.0 or configured value) |
| `EXCLUDE_PARAMETER` | Exclude from overall calculation; redistribute weights |
| `BLOCK_EVALUATION` | Prevent evaluation until minimum evidence is provided |

> **Design Note:** `FLAG_FOR_REVIEW` is the default. `ASSIGN_MINIMUM` is explicitly NOT the default because missing evidence ≠ poor performance.

---

## 7. Evidence Strength

### 7.1 Strength Levels

| Level | Description |
|-------|-------------|
| `HIGH` | Multiple corroborated evidence from verified/official sources |
| `MEDIUM` | Adequate evidence but limited corroboration or mixed source reliability |
| `LOW` | Minimal evidence, primarily self-reported or unverified |
| `INSUFFICIENT` | Below minimum requirements |

### 7.2 Strength Determination

Evidence strength is determined by a combination of:

```text
1. Source diversity: How many different source types?
2. Source reliability: Average reliability of sources
3. Verification status: How much evidence is verified?
4. Corroboration: Do multiple sources agree?
5. Quantity: How many evidence items?
6. Recency: How recent is the evidence?
```

```text
SCORING POLICY TO BE FINALIZED:
- Exact formula/rules for combining these factors into a strength level
- Whether strength is deterministically calculated or AI-assisted
- Weight given to each factor
- Thresholds for each strength level
```

---

## 8. Evaluation Confidence

### 8.1 Confidence Components

| Component | Source | Range |
|-----------|--------|-------|
| `aiConfidence` | AI response | 0.0–1.0 |
| `evidenceConfidence` | Evidence sufficiency + strength | 0.0–1.0 |
| `methodConfidence` | Scoring strategy determinism | 0.0–1.0 |

### 8.2 Composite Confidence

```text
For OBJECTIVE parameters:
  confidence = 1.0 (fully deterministic)

For QUALITATIVE parameters:
  confidence = f(aiConfidence, evidenceConfidence)
  
  where evidenceConfidence is derived from:
    - evidence sufficiency
    - evidence strength
    - source diversity

For HYBRID parameters:
  confidence = objectiveWeight × 1.0 + qualitativeWeight × qualitativeConfidence
```

```text
SCORING POLICY TO BE FINALIZED:
- Exact formula for combining aiConfidence and evidenceConfidence
- How evidenceConfidence is calculated from evidence quality metrics
- Whether AI-reported confidence should be dampened (not taken at face value)
- Low-confidence threshold for mandatory human review
```

### 8.3 Critical Design Decision

**AI-reported confidence is NOT automatically authoritative.**

An LLM saying `confidence: 0.95` does not mean the evaluation is 95% reliable. The system should:

1. Record the AI-reported confidence
2. Calculate evidence-based confidence independently
3. Combine both, potentially dampening AI confidence
4. Use the composite confidence for workflow decisions (human review triggers)
5. Allow calibration over time as human-approval data accumulates

---

## 9. Contradiction Handling

### 9.1 Contradiction Detection

Evidence mapped as `CONTRADICTORY` or evidence items from different sources that suggest opposite conclusions.

```text
Example:
  Supervisor says: "Strong team leadership throughout the year"
  Peer says: "Does not share information or collaborate well"

These are contradictory for the Leadership parameter.
```

### 9.2 Scoring Behavior

```text
1. AI evaluation receives all evidence, including contradictions
2. AI justification should address contradictions
3. Confidence should be LOWER when contradictions exist
4. Evidence strength should be LOWER (lack of corroboration)
5. Parameter is flagged for mandatory human review
6. The scoring engine does NOT automatically resolve contradictions
```

```text
SCORING POLICY TO BE FINALIZED:
- Whether contradictory evidence automatically triggers human review
  (recommended: yes)
- Whether confidence should have a hard ceiling when contradictions exist
- How the AI prompt should instruct handling of contradictions
```

---

## 10. Rating Conversion

### 10.1 Canonical Score → Display Rating

```text
Given a canonical score (0–100) and a RatingScale:

For each RatingMapping in the scale:
  if canonicalScoreMin ≤ score < canonicalScoreMax:
    return ratingMapping

Example:
  Score = 77.5
  Mapping: 70.0–85.0 → "4" (Exceeds Expectations)
  Result: rating = "4", label = "Exceeds Expectations"
```

### 10.2 Configurable Rating Scales

Different frameworks can define different rating scales:

```text
Employee Framework:
  0–30   → "1" (Unsatisfactory)
  30–50  → "2" (Needs Improvement)
  50–70  → "3" (Meets Expectations)
  70–85  → "4" (Exceeds Expectations)
  85–100 → "5" (Outstanding)

Student Framework:
  0–33   → "F"
  33–40  → "D"
  40–50  → "C"
  50–60  → "B"
  60–70  → "A-"
  70–80  → "A"
  80–100 → "A+"
```

---

## 11. Human Override Integration

### 11.1 Parameter-Level Override

```text
When a reviewer modifies a parameter score:

1. ParameterResult.humanApprovedScore = reviewer's score
2. ParameterResult.finalScore = humanApprovedScore
3. ParameterResult.weightedScore = humanApprovedScore × weight
4. Recalculate overallScore = Σ(finalScore_i × weight_i)
5. Re-map overallScore to rating
6. Increment AssessmentResult.version
7. Record override in ReviewSession
```

### 11.2 Overall Override (Exceptional)

```text
When a senior reviewer overrides the overall score:

1. Original overall score preserved in OverallOverride
2. AssessmentResult.overallScore = overridden score
3. Re-map to rating
4. AssessmentResult.resultStatus = OVERRIDDEN
5. Increment version
6. Mandatory justification recorded
7. Audit event emitted
```

> **Design Note:** Overall overrides are exceptional and should be rare. The primary workflow is parameter-level override → deterministic recalculation.

---

## 12. Deterministic Final Score Calculation

### 12.1 Full Calculation Flow

```text
┌─────────────────────────────────────────────────────────┐
│                FINAL SCORE CALCULATION                    │
│                                                          │
│  For each active parameter in framework version:         │
│    │                                                     │
│    ├── Resolve ScoringStrategy                           │
│    ├── Execute strategy.calculate(context)               │
│    │   → normalizedScore (0–100)                         │
│    │   → rubricLevel                                     │
│    │   → evidenceSufficiency                             │
│    │   → evidenceStrength                                │
│    │   → evaluationConfidence                            │
│    │                                                     │
│    ├── Determine finalScore:                             │
│    │   if humanApprovedScore != null:                    │
│    │     finalScore = humanApprovedScore                 │
│    │   else:                                             │
│    │     finalScore = normalizedScore                    │
│    │                                                     │
│    ├── Calculate weightedScore:                          │
│    │   weightedScore = finalScore × weight               │
│    │                                                     │
│    └── Map to display rating                             │
│                                                          │
│  Handle N/A parameters:                                  │
│    Apply configured policy (exclude or neutral)          │
│    Adjust weights if excluding                           │
│                                                          │
│  Calculate overallScore:                                 │
│    overallScore = Σ(weightedScore_i)                     │
│    (or Σ(weightedScore_i) / Σ(weight_i) if weights      │
│     were redistributed for N/A parameters)               │
│                                                          │
│  Apply rounding:                                         │
│    overallScore = round(overallScore, scale, HALF_UP)    │
│                                                          │
│  Map to overall rating:                                  │
│    overallRating = ratingScale.map(overallScore)         │
│                                                          │
│  Store AssessmentResult with all ParameterResults         │
└─────────────────────────────────────────────────────────┘
```

### 12.2 Reproducibility Guarantee

Given the same:
- `framework_version_id` (frozen parameters, rubrics, weights, scoring rules)
- Evidence set
- AI evaluation results (immutable)
- Human review decisions (immutable)

The scoring engine **MUST** produce identical results. This is ensured by:

1. All configuration snapshotted in the framework version
2. `BigDecimal` arithmetic (no floating-point drift)
3. Explicit rounding rules
4. Deterministic strategy implementations
5. No external state dependencies

---

## 13. Configuration & Versioning Requirements

### 13.1 What Must Be Versioned

| Configuration | Versioning Mechanism | Impact |
|---------------|---------------------|--------|
| Parameter definition | FrameworkVersion snapshot | Rubric, strategy, rules |
| Rubric levels | FrameworkVersion snapshot | Score ranges |
| Parameter weights | FrameworkVersion snapshot | Overall score |
| Scoring strategy config | FrameworkVersion snapshot | Calculation logic |
| Rating scale/mapping | FrameworkVersion snapshot | Display ratings |
| Scoring rule set | FrameworkVersion snapshot | Rounding, policies |
| Missing data policy | Per-parameter in FrameworkVersion | Missing evidence behavior |
| Evidence rules | Per-parameter in FrameworkVersion | Evidence eligibility |

### 13.2 What Is Recorded Per Evaluation

| Data | Stored In | Immutable? |
|------|-----------|------------|
| AI model used | `evaluation_run` | Yes |
| Prompt version used | `evaluation_run` | Yes |
| AI suggested scores | `ai_parameter_evaluation` | Yes |
| Scoring strategy results | `parameter_result` | Yes (per result version) |
| Weights at calculation time | `parameter_result.weight` | Yes |
| Human overrides | `parameter_review` | Yes |
| Overall score | `assessment_result` | Yes (per version) |

---

## 14. Scoring Engine Service Interface

```java
public interface ScoringEngine {

    /**
     * Calculate all parameter scores and overall result for an assessment.
     * Uses AI evaluation results where available.
     */
    AssessmentResult calculateAssessmentResult(
        Assessment assessment,
        FrameworkVersion frameworkVersion,
        Map<UUID, EvidenceSet> evidenceSets,           // paramId → evidenceSet
        Map<UUID, AiParameterEvaluation> aiEvaluations, // paramId → aiEval
        Map<UUID, ObjectiveFacts> objectiveFacts         // paramId → facts
    );

    /**
     * Recalculate overall score after human review overrides.
     */
    AssessmentResult recalculateAfterReview(
        AssessmentResult currentResult,
        FrameworkVersion frameworkVersion,
        List<ParameterReview> reviews
    );

    /**
     * Calculate a single parameter score.
     */
    ParameterScoreResult calculateParameterScore(ScoringContext context);

    /**
     * Convert a canonical score to a display rating.
     */
    RatingResult mapToRating(BigDecimal canonicalScore, RatingScale scale);
}
```

---

## 15. Rounding Rules

| Context | Rule |
|---------|------|
| Internal calculations | No rounding; full BigDecimal precision |
| Stored canonical scores | `NUMERIC(7,4)` — 4 decimal places |
| Weighted scores | `NUMERIC(7,4)` — 4 decimal places |
| Overall score (display) | Configurable via `ScoringRuleSet.roundingScale` (default: 2) |
| Rounding mode | `HALF_UP` (configurable per framework) |
| Percentage display | 2 decimal places |

---

## 16. Architectural Contracts vs. Configurable Policies

### Architectural Contracts (Fixed)

These are structural decisions that do not change per framework:

- Canonical score range: 0.0000–100.0000
- Score type: `BigDecimal`
- Strategy pattern with `ScoringStrategy` interface
- Scoring context structure
- Result structure (`ParameterScoreResult`, `AssessmentResult`)
- Weight sum = 1.0 invariant
- `finalScore` = `humanApproved` if present, else `normalizedScore`
- Overall score = weighted sum of final scores
- Immutable results (versioned, append-only)

### Configurable Institutional Policies

These vary per framework and are specified in configuration:

- Rubric level ranges (canonical score min/max per level)
- Parameter weights
- Rating scale mappings
- Missing data policy per parameter
- Rounding scale and mode
- Evaluation dimensions and dimension weights per parameter
- Objective normalization formulas
- Hybrid objective/qualitative weight split
- N/A parameter handling (exclude vs. neutral)
- Maximum N/A parameters threshold
- Low-confidence human-review trigger threshold
- Growth/trend calculation formulas
