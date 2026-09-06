# 07 — Testing Strategy

## 1. Testing Principles

| # | Principle |
|---|-----------|
| T1 | **Deterministic scoring must be 100% unit-testable** — No external dependencies, no AI calls, pure math. |
| T2 | **AI evaluation must be structurally validatable** — Structured output validation, evidence grounding, rubric adherence. |
| T3 | **Reproducibility is testable** — Given the same inputs, the scoring engine must produce the same outputs. |
| T4 | **Module isolation** — Each module can be tested independently via its interfaces. |
| T5 | **Test pyramid** — Many unit tests, fewer integration tests, fewer E2E tests. |
| T6 | **Benchmark dataset** — A curated dataset of representative cases for regression testing. |

---

## 2. Test Architecture

```text
┌───────────────────────────────────────────────────────────────┐
│                     Test Pyramid                               │
│                                                                │
│                        ╱╲                                      │
│                       ╱  ╲      E2E Tests                      │
│                      ╱ E2E╲     (Full pipeline, Testcontainers)│
│                     ╱──────╲                                   │
│                    ╱        ╲                                   │
│                   ╱Integration╲   Integration Tests            │
│                  ╱   Tests     ╲  (DB, AI mock, Spring context) │
│                 ╱───────────────╲                               │
│                ╱                 ╲                              │
│               ╱    Unit Tests     ╲  Unit Tests                 │
│              ╱     (Pure logic)    ╲ (Scoring, domain, mapping) │
│             ╱───────────────────────╲                           │
│                                                                │
│  + AI Evaluation Tests (structural validation, benchmarks)     │
│  + Architecture Tests (ArchUnit)                               │
└───────────────────────────────────────────────────────────────┘
```

---

## 3. Unit Tests

### 3.1 Scoring Engine Unit Tests

The scoring engine is the most critical area for unit testing. All tests use pure Java — no Spring context, no database, no AI.

#### Score Normalization Tests

| Test Case | Description |
|-----------|-------------|
| `normalizePercentage_validValue_returnsCanonical` | 94% → 94.0000 |
| `normalizeRatio_validValue_returnsCanonical` | 96/100 → 96.0000 |
| `normalizeRatio_exceedsTarget_capsAt100` | 105/100 → 100.0000 (or configured behavior) |
| `normalizeRatio_zeroTarget_throwsException` | Division by zero protection |
| `normalizeRatio_negativeValue_throwsException` | Invalid input |

#### Weighting Tests

| Test Case | Description |
|-----------|-------------|
| `weightedScore_normalCase_correctCalculation` | 77.5 × 0.08 = 6.2000 |
| `weightedScore_zeroPrecision_preservesScale` | Verifies BigDecimal scale |
| `overallScore_allParameters_correctWeightedSum` | Sum of all weighted scores |
| `overallScore_weightsNotSumToOne_throwsException` | Weight validation |
| `overallScore_roundingHalfUp_correctResult` | 72.3456 rounded to 72.35 |

#### Rounding Tests

| Test Case | Description |
|-----------|-------------|
| `round_halfUp_5roundsUp` | 72.345 → 72.35 |
| `round_halfUp_4staysDown` | 72.344 → 72.34 |
| `round_scale4_preservesPrecision` | Internal calculations at 4 decimal places |
| `round_scale2_displayPrecision` | Display at 2 decimal places |

#### Boundary Tests

| Test Case | Description |
|-----------|-------------|
| `score_exactlyZero_mapsToLevel1` | 0.0000 → Level 1 |
| `score_exactly100_mapsToLevel5` | 100.0000 → Level 5 |
| `score_atBoundary_mapsCorrectly` | 70.0000 → Level 4 (not Level 3) |
| `score_justBelowBoundary_mapsCorrectly` | 69.9999 → Level 3 |
| `score_negativeValue_throwsException` | Invalid range |
| `score_above100_throwsException` | Invalid range |

#### Missing Evidence Tests

| Test Case | Description |
|-----------|-------------|
| `missingEvidence_flagForReview_normalScoreWithFlag` | Policy: FLAG_FOR_REVIEW |
| `missingEvidence_assignMinimum_returnsZero` | Policy: ASSIGN_MINIMUM |
| `missingEvidence_assignNeutral_returnsConfiguredNeutral` | Policy: ASSIGN_NEUTRAL |
| `missingEvidence_excludeParameter_redistributesWeights` | Policy: EXCLUDE_PARAMETER |
| `missingEvidence_blockEvaluation_throwsException` | Policy: BLOCK_EVALUATION |

#### N/A Evidence Tests

| Test Case | Description |
|-----------|-------------|
| `notApplicable_excludePolicy_weightsRedistributed` | Remaining weights sum to 1.0 |
| `notApplicable_multipleNA_weightsStillValid` | Multiple exclusions handled |
| `notApplicable_allNA_throwsException` | Cannot calculate with no parameters |
| `notApplicable_neutralPolicy_usesNeutralScore` | Fixed neutral score applied |

#### Composite Score Tests

| Test Case | Description |
|-----------|-------------|
| `composite_allComponents_correctWeightedAverage` | 87×0.4 + 82×0.2 + 91×0.15 + 85×0.1 + 88×0.15 = 86.35 |
| `composite_missingComponent_appliesPolicy` | One component missing |
| `composite_weightsNotSumToOne_throwsException` | Invalid weights |
| `composite_singleComponent_returnsComponentScore` | Only one component |

#### Hybrid Score Tests

| Test Case | Description |
|-----------|-------------|
| `hybrid_normalCase_correctWeightedCombination` | objective × 0.6 + qualitative × 0.4 |
| `hybrid_objectiveOnly_usesObjectiveWeight` | No qualitative available |
| `hybrid_qualitativeOnly_usesQualitativeWeight` | No objective available |

#### Rating Conversion Tests

| Test Case | Description |
|-----------|-------------|
| `ratingConversion_eachLevel_correctMapping` | Test all levels |
| `ratingConversion_boundaryValues_correctLevel` | Test at exact boundaries |
| `ratingConversion_customScale_usesCustomMappings` | Non-default scale |
| `ratingConversion_noMatchingRange_throwsException` | Gap in rating scale |

#### Consistency Calculation Tests

| Test Case | Description |
|-----------|-------------|
| `consistency_allSame_highConsistency` | All scores identical |
| `consistency_highVariation_lowConsistency` | Wide score range |
| `consistency_singleValue_notApplicable` | Cannot calculate consistency |

#### Trend Calculation Tests

| Test Case | Description |
|-----------|-------------|
| `trend_improving_correctDirection` | Scores increasing over time |
| `trend_declining_correctDirection` | Scores decreasing |
| `trend_stable_correctDirection` | Minimal change |
| `trend_insufficientData_notApplicable` | Fewer than minimum data points |

#### Growth Calculation Tests

| Test Case | Description |
|-----------|-------------|
| `growth_positiveGrowth_correctCalculation` | 60→82 = +22 absolute, +36.7% |
| `growth_negativeGrowth_correctCalculation` | 90→85 = -5 absolute, -5.6% |
| `growth_zeroPrevious_handledGracefully` | Division by zero prevention |
| `growth_noPreviousData_notApplicable` | No previous period |

#### Override Tests

| Test Case | Description |
|-----------|-------------|
| `override_parameterLevel_recalculatesOverall` | Changed parameter → new overall |
| `override_multipleParameters_correctRecalculation` | Multiple overrides |
| `override_preservesOriginalAiScore` | AI score unchanged |
| `override_overallOverride_overridesRecalculation` | Senior override wins |

#### Versioning/Reproducibility Tests

| Test Case | Description |
|-----------|-------------|
| `reproducibility_sameInputs_sameOutput` | Deterministic guarantee |
| `reproducibility_differentFrameworkVersion_differentRubric` | Version isolation |
| `reproducibility_historicalAssessment_usesSnapshotConfig` | Not affected by current config |

---

### 3.2 Domain Model Unit Tests

| Area | Key Tests |
|------|-----------|
| Assessment lifecycle | Valid state transitions; invalid transitions throw exceptions |
| Evidence sufficiency | Rule-based sufficiency calculation |
| Evidence mapping | Valid mapping types; constraint violations |
| Framework version activation | Freezing behavior; immutability enforcement |
| Academic facts calculation | Subject performance, growth, trend, consistency |

---

### 3.3 Application Service Unit Tests

Each application service is tested with mocked dependencies:

| Service | Key Tests |
|---------|-----------|
| `FrameworkService` | Create/update framework; version management; activation |
| `AssessmentService` | Create assessment; status transitions; validation |
| `EvidenceService` | Submit evidence; mapping; sufficiency check |
| `EvaluationService` | Orchestration with mocked AI gateway |
| `ScoringService` | Delegation to scoring engine |
| `ReviewService` | Review workflow; override validation |
| `PenPictureService` | Generation request assembly |
| `AcademicService` | Record management; calculation triggering |

---

## 4. Integration Tests

### 4.1 Database Integration Tests (Testcontainers)

```java
@Testcontainers
@SpringBootTest
class FrameworkRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    // Tests:
    // - Framework CRUD
    // - Version creation and activation
    // - Parameter definition persistence
    // - Rubric level persistence
    // - Weight persistence
    // - Rating scale persistence
    // - Flyway migrations run successfully
}
```

| Area | Key Tests |
|------|-----------|
| Framework persistence | CRUD; version snapshots; activation freezing |
| Assessment persistence | Creation; status updates; queries |
| Evidence persistence | Submission; mapping; queries by parameter |
| Evaluation persistence | Run storage; parameter evaluation storage |
| Result persistence | Result versioning; parameter results |
| Review persistence | Session creation; parameter reviews |
| Academic persistence | Record; enrollment; results; attendance |
| Audit persistence | Event recording; queries |
| Multi-tenancy | Institution isolation; cross-institution queries return nothing |

### 4.2 API Integration Tests

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class FrameworkApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Tests:
    // - Create framework → 201
    // - Invalid request → 400 with validation errors
    // - Unauthorized → 401
    // - Forbidden → 403
    // - Not found → 404
    // - Version activation → parameters frozen
}
```

### 4.3 AI Integration Tests (Mock Gateway)

```java
class EvaluationIntegrationTest {

    // Mock AiGateway returns pre-defined structured responses
    // Tests:
    // - Full evaluation pipeline: evidence → AI → scoring → result
    // - Validation failures → retry behavior
    // - Provider error → fallback behavior
    // - Partial failure → PARTIALLY_COMPLETED status
}
```

---

## 5. AI Evaluation Tests

### 5.1 Structured Output Validity Tests

| Test Case | Description |
|-----------|-------------|
| `structuredOutput_allFieldsPresent` | All required fields returned |
| `structuredOutput_scoreInRange` | suggestedScore within 0–100 |
| `structuredOutput_confidenceInRange` | confidence within 0.0–1.0 |
| `structuredOutput_validRubricLevel` | rubricLevel within configured levels |
| `structuredOutput_scoreMatchesRubricLevel` | Score falls within the declared rubric level's range |
| `structuredOutput_evidenceReferencesExist` | Referenced evidence IDs are real |
| `structuredOutput_strengthsNotEmpty` | At least one strength identified |

### 5.2 Rubric Adherence Tests

| Test Case | Description |
|-----------|-------------|
| `rubricAdherence_strongEvidence_highScore` | Clear high-performance evidence → level 4–5 |
| `rubricAdherence_weakEvidence_lowScore` | Clear poor-performance evidence → level 1–2 |
| `rubricAdherence_averageEvidence_midScore` | Average evidence → level 3 |
| `rubricAdherence_justificationReferencesRubric` | Justification mentions rubric criteria |

### 5.3 Evidence Grounding Tests

| Test Case | Description |
|-----------|-------------|
| `grounding_allClaimsTraceableToEvidence` | Every strength/improvement traceable to evidence |
| `grounding_noInventedAchievements` | No claims not present in evidence |
| `grounding_evidenceReferencesValid` | All evidence IDs in response are real |
| `grounding_selfReportedNotOverweighted` | Self-reported evidence alone doesn't drive high scores |

### 5.4 Hallucination Tests

| Test Case | Description |
|-----------|-------------|
| `hallucination_noEvidenceProvided_lowScore` | No evidence → low score, not hallucinated high score |
| `hallucination_vagueEvidence_conservativeScore` | Vague evidence → conservative score |
| `hallucination_noPhantomMetrics` | AI doesn't invent specific numbers not in evidence |
| `hallucination_noPhantomEvents` | AI doesn't invent events not in evidence |

### 5.5 Unsupported Claims Tests

| Test Case | Description |
|-----------|-------------|
| `unsupportedClaims_claimWithoutEvidence_flagged` | Claims not traceable to evidence are detected |
| `unsupportedClaims_exaggeratedDescription_detected` | "excellent" when evidence says "adequate" |

### 5.6 Contradictory Evidence Tests

| Test Case | Description |
|-----------|-------------|
| `contradiction_opposingEvidence_acknowledgedInJustification` | AI discusses the contradiction |
| `contradiction_opposingEvidence_lowerConfidence` | Confidence is reduced |
| `contradiction_opposingEvidence_notIgnored` | Both sides represented |

### 5.7 Self-Reported Exaggeration Tests

| Test Case | Description |
|-----------|-------------|
| `selfReported_exaggeratedClaims_notFullyTrusted` | Self-reported "outstanding" without corroboration → conservative score |
| `selfReported_corroborated_appropriateScore` | Self-reported + supervisor confirmed → normal score |
| `selfReported_onlySource_lowerConfidence` | Only self-reported → lower confidence |

### 5.8 Prompt Regression Tests

| Test Case | Description |
|-----------|-------------|
| `promptRegression_sameInput_stableOutput` | Same evidence → scores within acceptable range |
| `promptRegression_versionChange_documentedDelta` | Prompt version change → delta recorded |
| `promptRegression_benchmarkDataset_withinTolerance` | Full benchmark → aggregate metrics within bounds |

### 5.9 Model Regression Tests

| Test Case | Description |
|-----------|-------------|
| `modelRegression_benchmarkDataset_compareAgainstBaseline` | New model scores vs. baseline |
| `modelRegression_outlierDetection_flaggedCases` | Cases where new model diverges significantly |

---

## 6. Architecture Tests (ArchUnit)

```java
@AnalyzeClasses(packages = "com.aias")
class ArchitectureTest {

    @ArchTest
    static final ArchRule no_circular_dependencies =
        slices().matching("com.aias.(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule domain_does_not_depend_on_infrastructure =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule domain_does_not_depend_on_spring =
        noClasses().that().resideInAPackage("..domain.model..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..");

    @ArchTest
    static final ArchRule scoring_does_not_depend_on_ai =
        noClasses().that().resideInAPackage("..scoring..")
            .should().dependOnClassesThat().resideInAPackage("..ai..");

    @ArchTest
    static final ArchRule evaluation_does_not_depend_on_review =
        noClasses().that().resideInAPackage("..evaluation..")
            .should().dependOnClassesThat().resideInAPackage("..review..");

    @ArchTest
    static final ArchRule only_api_layer_uses_controllers =
        classes().that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..api.controller..");
}
```

---

## 7. Benchmark / Evaluation Dataset

### 7.1 Dataset Structure

```text
test-data/
├── benchmark/
│   ├── employee/
│   │   ├── strong-performer.json
│   │   ├── average-performer.json
│   │   ├── weak-performer.json
│   │   ├── improving-performer.json
│   │   ├── declining-performer.json
│   │   ├── insufficient-evidence.json
│   │   ├── contradictory-evidence.json
│   │   ├── self-reported-heavy.json
│   │   └── hybrid-evidence.json
│   ├── teacher/
│   │   ├── effective-teacher.json
│   │   ├── average-teacher.json
│   │   ├── new-teacher.json
│   │   ├── improving-teacher.json
│   │   └── mixed-evidence.json
│   └── student/
│       ├── high-achiever.json
│       ├── average-student.json
│       ├── struggling-student.json
│       ├── improving-student.json
│       ├── declining-student.json
│       ├── inconsistent-student.json
│       └── subject-specific-strength.json
```

### 7.2 Benchmark Case Structure

Each benchmark case contains:

```json
{
  "caseId": "EMP-STRONG-001",
  "caseName": "Strong Employee Performer",
  "description": "Employee with consistently high performance across multiple parameters",
  "domainType": "EMPLOYEE",
  "framework": {
    "parameters": ["LEADERSHIP", "PRODUCTIVITY", "ATTENDANCE", "TEAMWORK"],
    "weights": { "LEADERSHIP": 0.25, "PRODUCTIVITY": 0.30, "ATTENDANCE": 0.15, "TEAMWORK": 0.30 }
  },
  "evidence": [
    {
      "id": "ev-001",
      "sourceType": "SUPERVISOR",
      "content": "Led a team of 12 to deliver the billing project 2 weeks ahead of schedule...",
      "parameterMappings": [
        { "parameterCode": "LEADERSHIP", "mappingType": "PRIMARY" }
      ]
    }
  ],
  "expectedOutputs": {
    "parameterScoreRanges": {
      "LEADERSHIP": { "min": 70.0, "max": 95.0 },
      "PRODUCTIVITY": { "min": 75.0, "max": 100.0 }
    },
    "overallScoreRange": { "min": 70.0, "max": 90.0 },
    "expectedRating": "4 or 5",
    "requiredStrengthMentions": ["project leadership", "team management"],
    "requiredImprovementMentions": [],
    "evidenceGroundingRequired": true,
    "contradictionHandlingExpected": false
  }
}
```

### 7.3 Benchmark Metrics

| Metric | Description | Target |
|--------|-------------|--------|
| **Score range adherence** | % of cases where AI score falls within expected range | > 85% |
| **Rubric level accuracy** | % of cases where rubric level matches expected | > 80% |
| **Evidence grounding rate** | % of claims traceable to evidence | > 95% |
| **Hallucination rate** | % of cases with invented evidence | < 5% |
| **Contradiction acknowledgment** | % of contradiction cases where AI addresses contradiction | > 90% |
| **Self-reported dampening** | % of self-reported-heavy cases with appropriately conservative scores | > 80% |
| **Structured output validity** | % of responses passing schema validation | > 99% |
| **Confidence calibration** | Correlation between stated confidence and actual accuracy | r > 0.5 |

---

## 8. Regression Testing

### 8.1 Scoring Regression

On every code change to the scoring module:
1. Run all scoring unit tests
2. Run reproducibility tests (same inputs → same outputs)
3. Run benchmark dataset through scoring engine
4. Compare results against baseline; fail if delta exceeds threshold

### 8.2 AI Evaluation Regression

On prompt template changes or model updates:
1. Run benchmark dataset through AI evaluation
2. Compare against baseline results
3. Generate delta report
4. Flag cases where:
   - Score changed by > 10 points
   - Rubric level changed
   - Evidence grounding degraded
   - New hallucinations detected

### 8.3 Reproducibility Regression

Monthly (or per-release):
1. Select random historical assessments
2. Replay with original framework version + evidence + AI responses
3. Verify scoring engine produces identical results
4. Any deviation = critical bug

---

## 9. Testing Tools & Fixtures

### 9.1 Test Fixtures

```java
public class TestFixtures {

    public static ParameterDefinition leadershipParameter() { /* standard leadership param */ }
    public static ParameterDefinition attendanceParameter() { /* OBJECTIVE attendance param */ }
    public static FrameworkVersion sampleEmployeeFramework() { /* complete framework version */ }
    public static List<EvidenceItem> strongLeadershipEvidence() { /* high-quality evidence */ }
    public static List<EvidenceItem> insufficientEvidence() { /* below minimum */ }
    public static AiParameterEvaluation sampleAiEvaluation() { /* valid AI response */ }
    public static AcademicRecord sampleStudentRecord() { /* complete academic record */ }
}
```

### 9.2 Test Builders

```java
public class ParameterDefinitionBuilder {
    public ParameterDefinitionBuilder withCode(String code) { ... }
    public ParameterDefinitionBuilder withScoringStrategy(ScoringStrategyType type) { ... }
    public ParameterDefinitionBuilder withRubricLevels(List<RubricLevel> levels) { ... }
    public ParameterDefinitionBuilder withMissingDataPolicy(MissingDataPolicyType policy) { ... }
    public ParameterDefinition build() { ... }
}
```

### 9.3 Technology

| Tool | Purpose |
|------|---------|
| JUnit 5 | Test framework |
| Mockito | Mocking for unit tests |
| AssertJ | Fluent assertions (especially for BigDecimal comparisons) |
| Testcontainers | PostgreSQL for integration tests |
| ArchUnit | Architecture rule enforcement |
| Spring MockMvc | API integration tests |
| WireMock | AI provider mock for integration tests |
| Custom benchmark runner | Benchmark dataset execution and reporting |
