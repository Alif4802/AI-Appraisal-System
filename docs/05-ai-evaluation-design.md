# 05 — AI Evaluation Design

## 1. Design Goals

| # | Goal |
|---|------|
| 1 | **Provider independence** — Switch between Gemini, Claude, Qwen, or self-hosted models without domain changes |
| 2 | **Structured output** — All AI responses are validated Java DTOs; no free-form prose parsing |
| 3 | **Evidence grounding** — AI must reference specific evidence items; unsupported claims are flagged |
| 4 | **Rubric adherence** — AI evaluates against the configured rubric, not its own invented criteria |
| 5 | **Deterministic boundary** — AI suggests qualitative scores; application handles all arithmetic |
| 6 | **Auditability** — Every AI call records model, prompt version, temperature, tokens, and raw response |
| 7 | **Failure resilience** — Retry logic, graceful degradation, human escalation on persistent failures |
| 8 | **Future RAG extension** — Clean extension point for adding retrieved institutional knowledge |

---

## 2. AI Gateway Architecture

```text
┌──────────────────────────────────────────────┐
│              Evaluation Module                │
│                                               │
│  EvaluationService                            │
│    ├── assembles EvaluationContext             │
│    ├── calls AiGateway (port interface)        │
│    ├── validates structured response           │
│    └── stores AiParameterEvaluation            │
└────────────────┬─────────────────────────────┘
                 │ calls port
                 ▼
┌──────────────────────────────────────────────┐
│                AI Module                      │
│                                               │
│  AiGateway (Port Interface)                   │
│    │                                          │
│    ▼                                          │
│  SpringAiGatewayAdapter (Infrastructure)      │
│    ├── resolves provider from config           │
│    ├── builds Spring AI ChatClient request     │
│    ├── applies structured output schema        │
│    ├── sends request                           │
│    ├── maps response to domain DTO             │
│    ├── records metadata                        │
│    └── handles retry/failure                   │
│                                               │
│  Provider Config (application.yml)             │
│    ├── gemini: { api-key, model, temp }       │
│    ├── claude: { api-key, model, temp }       │
│    └── local: { base-url, model, temp }       │
└──────────────────────────────────────────────┘
```

### Port Interface

```java
public interface AiGateway {

    /**
     * Evaluate a single parameter against evidence using AI.
     */
    AiParameterEvaluationResponse evaluateParameter(
        ParameterEvaluationRequest request
    );

    /**
     * Evaluate all parameters for an assessment in a batch.
     */
    List<AiParameterEvaluationResponse> evaluateParameters(
        List<ParameterEvaluationRequest> requests
    );

    /**
     * Generate a pen picture from structured assessment data.
     */
    PenPictureResponse generatePenPicture(
        PenPictureGenerationRequest request
    );

    /**
     * Suggest evidence-to-parameter mappings for unstructured evidence.
     */
    List<EvidenceMappingSuggestion> suggestEvidenceMappings(
        EvidenceMappingRequest request
    );
}
```

> **Key Decision:** The `AiGateway` is defined in the `ai.gateway` package. The evaluation module depends on this interface, never on Spring AI directly. The Spring AI adapter in `ai.provider.springai` implements it.

---

## 3. Spring AI Boundary

### What Spring AI Handles

- `ChatClient` / `ChatModel` abstraction across providers
- Structured output with `BeanOutputConverter` for DTO mapping
- Provider-specific configuration (API keys, endpoints, model names)
- Token counting and usage metadata
- Retry mechanisms

### What the Application Handles (on top of Spring AI)

- Prompt template management and versioning
- Evaluation context assembly
- Response validation against domain rules
- Evidence grounding checks
- Model metadata capture for audit
- Provider selection logic
- Fallback/escalation on persistent failures

### Provider Configuration

```yaml
# application.yml
ai:
  active-provider: gemini  # or claude, openai, local

spring:
  ai:
    gemini:
      api-key: ${GEMINI_API_KEY}
      chat:
        options:
          model: gemini-2.5-pro
          temperature: 0.2
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
          temperature: 0.2
    # Local model via OpenAI-compatible API
    # local:
    #   base-url: http://localhost:8080/v1
    #   chat:
    #     options:
    #       model: qwen2.5-72b
```

---

## 4. Prompt Architecture

### 4.1 Prompt Template Structure

Prompts are versioned, stored in the database (`prompt_template` table), and resolved at evaluation time.

```text
Prompt Template
    │
    ├── System Prompt (role, constraints, output schema)
    ├── Context Section (framework, parameter, rubric)
    ├── Evidence Section (assembled evidence items)
    ├── Instruction Section (what to evaluate)
    └── Output Format Section (structured JSON schema)
```

### 4.2 Parameter Evaluation Prompt (Conceptual)

```text
SYSTEM:
You are an assessment evaluation assistant. You evaluate employee/teacher/student
performance evidence against a defined rubric. You must:
- Base your evaluation ONLY on the provided evidence.
- Compare evidence against the rubric levels provided.
- Reference specific evidence items by their IDs.
- Do NOT invent achievements or evidence not provided.
- Do NOT perform arithmetic calculations; only interpret qualitative evidence.
- Return your evaluation in the exact JSON schema specified.

CONTEXT:
Framework: {{frameworkName}} ({{domainType}})
Parameter: {{parameterName}}
Definition: {{parameterDefinition}}
Purpose: {{parameterPurpose}}

Rubric:
{{#each rubricLevels}}
Level {{level}} ({{label}}): {{definition}}
  Canonical Score Range: {{canonicalScoreMin}} - {{canonicalScoreMax}}
{{/each}}

{{#if evaluationDimensions}}
Evaluation Dimensions:
{{#each evaluationDimensions}}
- {{name}} (Weight: {{weight}}): {{description}}
{{/each}}
{{/if}}

EVIDENCE:
{{#each evidenceItems}}
[Evidence {{id}}] Source: {{sourceType}} | Date: {{evidenceDate}} | Verified: {{verificationStatus}}
{{content}}
{{/each}}

{{#if objectiveFacts}}
CALCULATED OBJECTIVE FACTS (do NOT recalculate; interpret these):
{{objectiveFacts}}
{{/if}}

INSTRUCTIONS:
Evaluate the evidence against the rubric for parameter "{{parameterName}}".
{{#if evaluationDimensions}}
Score each evaluation dimension separately, then provide an overall suggested score.
{{/if}}

OUTPUT (respond with valid JSON only):
{output_schema}
```

### 4.3 Pen Picture Prompt (Conceptual)

```text
SYSTEM:
You are writing a professional annual pen picture for an {{domainType}}.
Base your writing ONLY on the structured assessment data provided.
Do NOT invent achievements or metrics not present in the data.
Write in third person, professional tone.
The pen picture should cover: {{domainSpecificSections}}.

DATA:
Subject: {{subjectName}}
Period: {{periodLabel}}
Overall Score: {{overallScore}} ({{overallRating}})

Parameter Results:
{{#each parameterResults}}
- {{parameterName}}: {{finalScore}} ({{displayRating}})
  Strengths: {{strengths}}
  Areas for improvement: {{improvementAreas}}
{{/each}}

{{#if academicFacts}}
Academic Performance:
{{academicFactsSummary}}
{{/if}}

INSTRUCTIONS:
Generate a professional pen picture of approximately 300-500 words.
```

---

## 5. Structured Output Contracts

### 5.1 AI Parameter Evaluation Response (DTO)

```java
public record AiParameterEvaluationResponse(
    @NotNull String parameterCode,
    @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal suggestedScore,
    @NotNull @Min(1) @Max(5) Integer suggestedRubricLevel,
    @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal confidence,
    @NotNull EvidenceStrengthLevel evidenceStrength,
    @NotBlank @Size(max = 2000) String justification,
    @NotNull @Size(max = 10) List<@NotBlank String> strengths,
    @NotNull @Size(max = 10) List<@NotBlank String> improvementAreas,
    Map<String, @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal> dimensionScores,
    @NotNull @Size(min = 1) List<String> evidenceReferences
) {}
```

### 5.2 Pen Picture Response (DTO)

```java
public record PenPictureAiResponse(
    @NotBlank @Size(min = 200, max = 3000) String content,
    @NotNull @Size(min = 1) List<@NotBlank String> keyHighlights,
    @NotNull List<@NotBlank String> developmentRecommendations
) {}
```

### 5.3 Evidence Mapping Suggestion (DTO)

```java
public record EvidenceMappingSuggestion(
    @NotNull String evidenceItemId,
    @NotNull String parameterCode,
    @NotNull EvidenceMappingType mappingType,
    @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal relevanceScore,
    @NotBlank String rationale
) {}
```

---

## 6. Validation Pipeline

Every AI response goes through a validation pipeline before entering the domain:

```text
AI Raw Response
      │
      ▼
┌─────────────────┐
│ JSON Parsing     │  ← Spring AI BeanOutputConverter
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Schema Validation│  ← Jakarta Bean Validation (@NotNull, @DecimalMin, etc.)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Domain Validation│  ← Application rules
│                  │
│ • Score within   │
│   rubric range?  │
│ • Rubric level   │
│   matches score? │
│ • Evidence refs  │
│   exist?         │
│ • Dimension      │
│   weights sum    │
│   to 1.0?        │
│ • No hallucinated│
│   evidence?      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Grounding Check  │
│                  │
│ • Are claimed    │
│   evidence refs  │
│   real?          │
│ • Are strengths  │
│   traceable to   │
│   evidence?      │
│ • Flag           │
│   unsupported    │
│   claims         │
└────────┬────────┘
         │
         ▼
   Valid Domain DTO
```

### Validation Rules

| Rule | Description | On Failure |
|------|-------------|------------|
| Score in rubric range | `suggestedScore` must fall within the parameter's rubric level boundaries (min of level 1 to max of level N) | Retry with clarification |
| Rubric level consistency | `suggestedRubricLevel` must be consistent with `suggestedScore` (score falls within that level's range) | Auto-correct rubric level |
| Evidence references exist | All IDs in `evidenceReferences` must match actual evidence items in the assessment | Remove invalid references; flag if all invalid |
| No phantom evidence | Strengths/improvements must be traceable to provided evidence | Flag for review |
| Dimension completeness | If dimensions configured, all dimensions must have scores | Retry |
| Dimension weight sum | Dimension scores, when weighted, should be consistent with overall suggestion | Log warning |
| Confidence range | `confidence` must be 0.0–1.0 | Clamp |

---

## 7. Evidence Grounding

### 7.1 Grounding Strategy

AI evaluations are grounded by:

1. **Explicit evidence references** — AI must list evidence item IDs it relied on.
2. **Source type tracking** — System tracks which evidence source types were available.
3. **Cross-reference validation** — Claimed strengths are checked against evidence content.
4. **Self-reported dampening** — Self-reported evidence is not automatically treated with the same weight as supervisor/official evidence.

### 7.2 Hallucination Controls

| Control | Implementation |
|---------|---------------|
| Evidence-only instruction | System prompt explicitly prohibits inventing evidence |
| Closed-book evaluation | Only provided evidence is available; no external knowledge for factual claims |
| Reference requirement | All strengths/improvements must cite evidence IDs |
| Phantom detection | Post-response check: do referenced evidence IDs exist? |
| Claim verification | Post-response check: are stated achievements present in evidence text? |
| Low-evidence flag | If fewer than minimum evidence items exist, flag for human review regardless of AI confidence |

---

## 8. Retry & Failure Behavior

```text
AI Request
    │
    ├── Success → Validate → Domain DTO
    │
    ├── Validation Failure → Retry with reformulated prompt (max 2 retries)
    │     │
    │     └── Still failing → Record partial result + flag for human evaluation
    │
    ├── Provider Error (rate limit, timeout) → Exponential backoff retry (max 3)
    │     │
    │     └── Still failing → Try fallback provider (if configured)
    │           │
    │           └── Still failing → Mark parameter as AI_EVALUATION_FAILED
    │
    └── Total failure → Mark EvaluationRun as PARTIALLY_COMPLETED
                        Flag affected parameters for human evaluation
```

### Configuration

```yaml
ai:
  retry:
    max-attempts: 3
    backoff-initial-ms: 1000
    backoff-multiplier: 2.0
    backoff-max-ms: 10000
  validation:
    max-reformulation-retries: 2
  fallback:
    enabled: false
    provider: local
```

---

## 9. Model Metadata Capture

Every AI interaction records:

| Metadata | Source | Purpose |
|----------|--------|---------|
| `providerType` | Configuration | Which provider was used |
| `modelName` | Configuration/response | Model identifier |
| `modelVersion` | Response header (if available) | Exact model version |
| `temperature` | Configuration | Randomness setting |
| `promptTemplateId` | Database lookup | Which prompt template |
| `promptVersion` | Database lookup | Which version of the template |
| `tokenUsage.input` | Response | Input token count |
| `tokenUsage.output` | Response | Output token count |
| `latencyMs` | Measured | Round-trip time |
| `rawResponse` | Response body | Full raw response for debugging |
| `timestamp` | System clock | When the call was made |

This metadata is stored immutably with the `evaluation_run` and enables:
- Reproducibility analysis
- Cost tracking
- Model regression detection
- Performance monitoring

---

## 10. Evaluation Context Assembly

Before calling AI, the `EvaluationService` assembles a complete context:

```text
For each parameter in the framework version:
    │
    ├── 1. Load ParameterDefinition (rubric, scoring config, dimensions)
    │
    ├── 2. Assemble EvidenceSet
    │   ├── Query evidence items mapped to this parameter
    │   ├── Apply evidence rules (eligible source types, age, verification)
    │   ├── Evaluate sufficiency
    │   └── If INSUFFICIENT or MISSING_REQUIRED → apply missing data policy
    │
    ├── 3. For OBJECTIVE parameters:
    │   ├── Calculate deterministic score directly
    │   └── Skip AI evaluation (unless HYBRID)
    │
    ├── 4. For QUALITATIVE/HYBRID/COMPOSITE:
    │   ├── Include pre-calculated objective facts (if hybrid)
    │   ├── Include academic facts (if student)
    │   └── Build ParameterEvaluationRequest for AI
    │
    └── 5. Resolve prompt template and version
```

---

## 11. Provider Independence Verification

The architecture ensures provider independence through:

| Layer | Isolation |
|-------|-----------|
| Domain layer | References only `AiGateway` port interface; zero Spring AI imports |
| Application layer | Orchestrates via `AiGateway`; no provider knowledge |
| AI module (port) | Defines `AiGateway`, `AiRequest`, `AiResponse` — pure Java interfaces |
| AI module (adapter) | Spring AI adapter lives in `ai.provider.springai`; all provider coupling here |
| Configuration | Provider selected via `ai.active-provider` property |
| Testing | Domain/application tests mock `AiGateway`; no AI calls needed |

To add a new provider:
1. Add Spring AI dependency for that provider
2. Configure in `application.yml`
3. The `SpringAiGatewayAdapter` routes via Spring AI's `ChatModel` abstraction
4. No domain/application code changes

---

## 12. Future RAG Extension Point

```text
Current:
  EvaluationService → AiGateway → Spring AI → Provider

Future with RAG:
  EvaluationService
    │
    ├── assembles evidence
    │
    ├── calls KnowledgeRetriever (new port)
    │   └── retrieves relevant institutional knowledge
    │       ├── HR policies
    │       ├── Competency frameworks
    │       ├── Historical evaluations
    │       └── Assessment guidelines
    │
    ├── enriches context with retrieved knowledge
    │
    └── calls AiGateway (unchanged)
```

### Extension Points

1. **New port:** `KnowledgeRetriever` interface in the `ai.gateway` package
2. **New adapter:** `PgVectorKnowledgeRetriever` using Spring AI's vector store abstraction
3. **Context enrichment:** `EvaluationContext` gains an optional `retrievedKnowledge` field
4. **Prompt extension:** Prompt templates gain a `{{retrievedContext}}` section

No existing interfaces need to change.

---

## 13. AI Evaluation Flow Summary

```text
┌─────────────────────────────────────────────────────────────────┐
│                    EVALUATION PIPELINE                           │
│                                                                  │
│  1. Trigger evaluation for Assessment                            │
│     └── Validate: status = EVALUATION_READY                      │
│                                                                  │
│  2. Create EvaluationRun (PENDING)                               │
│                                                                  │
│  3. For each parameter:                                          │
│     ├── Assemble EvidenceSet                                     │
│     ├── Check sufficiency                                        │
│     │   ├── OBJECTIVE → deterministic score, skip AI             │
│     │   ├── QUALITATIVE → full AI evaluation                     │
│     │   ├── HYBRID → pre-calc objective + AI for qualitative     │
│     │   ├── DERIVED → deterministic from source params            │
│     │   └── COMPOSITE → deterministic from component params       │
│     │                                                            │
│     ├── (For AI-required) Build ParameterEvaluationRequest       │
│     ├── Call AiGateway                                           │
│     ├── Validate response                                        │
│     ├── Ground-check evidence references                         │
│     └── Store AiParameterEvaluation                              │
│                                                                  │
│  4. Run deterministic scoring engine                             │
│     ├── Calculate normalized scores for all parameters           │
│     ├── Apply weights                                            │
│     ├── Calculate overall score                                  │
│     ├── Map to rating                                            │
│     └── Store AssessmentResult                                   │
│                                                                  │
│  5. Update EvaluationRun status (COMPLETED / PARTIALLY_COMPLETED)│
│                                                                  │
│  6. Transition Assessment status → EVALUATED                     │
│                                                                  │
│  7. Emit EvaluationCompletedEvent                                │
└─────────────────────────────────────────────────────────────────┘
```
