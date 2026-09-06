# 03 — Database Design

## 1. Design Principles

| # | Principle |
|---|-----------|
| D1 | **Immutable versioned configuration** — Framework versions, parameter definitions, rubrics, and weights are snapshot-frozen on activation. Assessments reference frozen versions. |
| D2 | **Append-only evaluation history** — Evaluation runs and AI results are never updated, only new runs are created. |
| D3 | **Audit-grade traceability** — Every score traces to evidence, framework version, AI model, prompt version, and human decision. |
| D4 | **Institutional isolation** — All major tables carry `institution_id` for row-level multi-tenancy. |
| D5 | **Temporal integrity** — Assessment periods, evidence dates, and evaluation timestamps are always recorded. |
| D6 | **BigDecimal precision** — All scores stored as `NUMERIC(7,4)` (0.0000–100.0000) or `NUMERIC(5,4)` (0.0000–1.0000) for normalized values. |
| D7 | **UUID primary keys** — All tables use UUID PKs for distributed-readiness and merge safety. |
| D8 | **Soft deletes where required** — Framework entities use status flags; evidence and evaluations are never deleted. |

---

## 2. Conceptual Schema Diagram

```text
┌──────────────────┐     ┌─────────────────────┐
│   institution    │────►│assessment_framework  │
└──────────────────┘     └──────────┬───────────┘
                                    │ 1:N
                         ┌──────────▼───────────┐
                         │  framework_version    │
                         └──┬───────┬───────┬───┘
                            │       │       │
                    ┌───────▼──┐ ┌──▼─────┐ ┌▼──────────────┐
                    │parameter │ │param_  │ │ rating_scale   │
                    │definition│ │weight  │ └───────┬────────┘
                    └──┬───┬───┘ └────────┘  ┌──────▼────────┐
                       │   │                 │rating_mapping  │
               ┌───────▼┐  ▼──────────┐     └───────────────┘
               │ rubric  │ │evidence   │
               │ _level  │ │ _rule     │
               └─────────┘ └──────────┘

┌──────────────────┐     ┌─────────────────┐
│   assessment     │────►│framework_version│ (FK)
└──┬───┬───┬───┬───┘     └─────────────────┘
   │   │   │   │
   │   │   │   └──────────────────┐
   │   │   │                      │
   │   │   ▼                      ▼
   │   │ ┌────────────┐  ┌────────────────┐
   │   │ │evidence_   │  │academic_record │ (student only)
   │   │ │  item      │  └──┬─────────────┘
   │   │ └──┬─────────┘     │
   │   │    │               ▼
   │   │    ▼          ┌──────────────────┐
   │   │ ┌──────────┐  │subject_enrollment│
   │   │ │evidence_ │  └──┬──────────────┘
   │   │ │parameter │     │
   │   │ │_mapping  │     ▼
   │   │ └──────────┘  ┌────────────────┐
   │   │               │academic_result │
   │   ▼               └────────────────┘
   │ ┌─────────────┐
   │ │evaluation_run│
   │ └──┬──────────┘
   │    │
   │    ▼
   │ ┌──────────────────┐
   │ │ai_parameter_     │
   │ │  evaluation       │
   │ └──────────────────┘
   │
   ▼
┌──────────────────┐
│assessment_result │
└──┬───────────────┘
   │
   ▼
┌──────────────────┐     ┌──────────────────┐
│parameter_result  │     │ review_session   │
└──────────────────┘     └──┬───────────────┘
                            │
                    ┌───────▼────────┐  ┌──────────────────┐
                    │parameter_review│  │overall_override  │
                    └────────────────┘  └──────────────────┘

┌──────────────────┐
│   pen_picture    │
└──────────────────┘

┌──────────────────┐
│   audit_event    │
└──────────────────┘

┌──────────────────┐
│  prompt_template │
└──────────────────┘
```

---

## 3. Table Definitions

### 3.1 Framework Tables

#### `assessment_framework`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | Framework ID |
| `institution_id` | `UUID` | NOT NULL, INDEX | Owning institution |
| `code` | `VARCHAR(100)` | NOT NULL, UNIQUE per institution | Short code |
| `name` | `VARCHAR(255)` | NOT NULL | Display name |
| `description` | `TEXT` | | Purpose |
| `domain_type` | `VARCHAR(50)` | NOT NULL | `EMPLOYEE`, `TEACHER`, `STUDENT` |
| `status` | `VARCHAR(50)` | NOT NULL | `DRAFT`, `ACTIVE`, `ARCHIVED` |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | |
| `created_by` | `UUID` | NOT NULL | |

#### `framework_version`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | Version ID |
| `framework_id` | `UUID` | FK → assessment_framework | Parent |
| `version_number` | `INTEGER` | NOT NULL | Sequential |
| `status` | `VARCHAR(50)` | NOT NULL | `DRAFT`, `ACTIVE`, `SUPERSEDED` |
| `effective_from` | `DATE` | NOT NULL | Start date |
| `effective_to` | `DATE` | | End date |
| `scoring_rule_set` | `JSONB` | | Global scoring rules |
| `activated_at` | `TIMESTAMPTZ` | | Activation timestamp |
| `activated_by` | `UUID` | | Activating user |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | |

**Index:** `(framework_id, version_number)` UNIQUE

#### `parameter_definition`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | Parameter ID |
| `framework_version_id` | `UUID` | FK → framework_version | Owning version |
| `code` | `VARCHAR(100)` | NOT NULL | Parameter code |
| `name` | `VARCHAR(255)` | NOT NULL | Display name |
| `description` | `TEXT` | | Full definition |
| `purpose` | `TEXT` | | Why this parameter exists |
| `category` | `VARCHAR(100)` | | Grouping |
| `scoring_strategy_type` | `VARCHAR(50)` | NOT NULL | Strategy type |
| `scoring_strategy_config` | `JSONB` | | Strategy-specific config |
| `evaluation_dimensions` | `JSONB` | | Parameter-specific dimensions + weights |
| `minimum_evidence_requirements` | `JSONB` | | Min evidence rules |
| `missing_data_policy` | `VARCHAR(50)` | NOT NULL, DEFAULT 'FLAG_FOR_REVIEW' | |
| `applicability_rule` | `JSONB` | | When parameter applies |
| `display_order` | `INTEGER` | NOT NULL | Sort order |
| `active` | `BOOLEAN` | NOT NULL, DEFAULT true | |

**Index:** `(framework_version_id, code)` UNIQUE

#### `rubric_level`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `parameter_definition_id` | `UUID` | FK → parameter_definition | Owning parameter |
| `level` | `INTEGER` | NOT NULL | Ordinal level (1–5) |
| `label` | `VARCHAR(100)` | NOT NULL | Display label |
| `definition` | `TEXT` | NOT NULL | Level definition |
| `canonical_score_min` | `NUMERIC(7,4)` | NOT NULL | Lower bound (0–100) |
| `canonical_score_max` | `NUMERIC(7,4)` | NOT NULL | Upper bound (0–100) |
| `canonical_score_midpoint` | `NUMERIC(7,4)` | NOT NULL | Default mapping point |

**Index:** `(parameter_definition_id, level)` UNIQUE

#### `parameter_weight`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `framework_version_id` | `UUID` | FK → framework_version | Version |
| `parameter_definition_id` | `UUID` | FK → parameter_definition | Parameter |
| `weight` | `NUMERIC(5,4)` | NOT NULL | Weight (sum to 1.0000) |

**Index:** `(framework_version_id, parameter_definition_id)` UNIQUE

#### `evidence_rule`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `parameter_definition_id` | `UUID` | FK → parameter_definition | |
| `eligible_source_types` | `JSONB` | NOT NULL | Array of eligible types |
| `required_source_types` | `JSONB` | | Array of required types |
| `maximum_age_days` | `INTEGER` | | Evidence age limit |
| `requires_verification` | `BOOLEAN` | NOT NULL, DEFAULT false | |

#### `rating_scale`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `framework_version_id` | `UUID` | FK → framework_version, UNIQUE | |
| `name` | `VARCHAR(100)` | NOT NULL | Scale name |

#### `rating_mapping`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `rating_scale_id` | `UUID` | FK → rating_scale | |
| `canonical_score_min` | `NUMERIC(7,4)` | NOT NULL | Lower bound |
| `canonical_score_max` | `NUMERIC(7,4)` | NOT NULL | Upper bound |
| `rating_value` | `VARCHAR(50)` | NOT NULL | Display value |
| `label` | `VARCHAR(255)` | | Description |
| `display_order` | `INTEGER` | NOT NULL | |

---

### 3.2 Assessment Tables

#### `assessment`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `institution_id` | `UUID` | NOT NULL, INDEX | |
| `framework_version_id` | `UUID` | FK → framework_version | Frozen reference |
| `external_subject_id` | `VARCHAR(255)` | NOT NULL | External ID |
| `subject_type` | `VARCHAR(50)` | NOT NULL | `EMPLOYEE`, `TEACHER`, `STUDENT` |
| `subject_display_name` | `VARCHAR(255)` | NOT NULL | |
| `department_or_class` | `VARCHAR(255)` | | |
| `period_start` | `DATE` | NOT NULL | |
| `period_end` | `DATE` | NOT NULL | |
| `period_label` | `VARCHAR(100)` | | |
| `status` | `VARCHAR(50)` | NOT NULL | Assessment status |
| `created_by` | `UUID` | NOT NULL | |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | |

**Indexes:**
- `(institution_id, external_subject_id, period_start)` — subject per period lookup
- `(institution_id, status)` — status queries
- `(framework_version_id)` — framework queries

---

### 3.3 Evidence Tables

#### `evidence_item`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `assessment_id` | `UUID` | FK → assessment | |
| `institution_id` | `UUID` | NOT NULL, INDEX | |
| `source_type` | `VARCHAR(50)` | NOT NULL | Evidence source type |
| `source_identifier` | `VARCHAR(255)` | | Who provided it |
| `source_reliability` | `VARCHAR(50)` | NOT NULL, DEFAULT 'UNKNOWN' | |
| `content` | `TEXT` | NOT NULL | Evidence content |
| `content_type` | `VARCHAR(50)` | NOT NULL | `TEXT`, `METRIC`, `STRUCTURED`, `DOCUMENT_REFERENCE` |
| `verification_status` | `VARCHAR(50)` | NOT NULL, DEFAULT 'UNVERIFIED' | |
| `evidence_date` | `DATE` | | When evidence occurred |
| `metadata` | `JSONB` | | Flexible metadata |
| `submitted_by` | `UUID` | NOT NULL | |
| `submitted_at` | `TIMESTAMPTZ` | NOT NULL | |

**Index:** `(assessment_id, source_type)` — filter by source

#### `evidence_parameter_mapping`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `evidence_item_id` | `UUID` | FK → evidence_item | |
| `parameter_definition_id` | `UUID` | FK → parameter_definition | |
| `mapping_type` | `VARCHAR(50)` | NOT NULL | `PRIMARY`, `SUPPORTING`, `CONTRADICTORY`, `CONTEXTUAL` |
| `mapped_by` | `VARCHAR(50)` | NOT NULL | `MANUAL`, `AI_SUGGESTED`, `RULE_BASED` |
| `mapped_at` | `TIMESTAMPTZ` | NOT NULL | |

**Index:** `(evidence_item_id, parameter_definition_id)` UNIQUE

---

### 3.4 Evaluation Tables

#### `evaluation_run`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `assessment_id` | `UUID` | FK → assessment | |
| `framework_version_id` | `UUID` | FK → framework_version | |
| `status` | `VARCHAR(50)` | NOT NULL | |
| `provider_type` | `VARCHAR(50)` | NOT NULL | AI provider |
| `model_name` | `VARCHAR(100)` | NOT NULL | Model name |
| `model_version` | `VARCHAR(100)` | | Model version |
| `temperature` | `NUMERIC(3,2)` | | Temperature |
| `additional_ai_config` | `JSONB` | | Other AI config |
| `prompt_template_id` | `UUID` | FK → prompt_template | |
| `prompt_version` | `INTEGER` | NOT NULL | Prompt version |
| `requested_by` | `UUID` | NOT NULL | |
| `started_at` | `TIMESTAMPTZ` | | |
| `completed_at` | `TIMESTAMPTZ` | | |

**Index:** `(assessment_id, started_at DESC)` — latest evaluation per assessment

#### `ai_parameter_evaluation`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `evaluation_run_id` | `UUID` | FK → evaluation_run | |
| `parameter_definition_id` | `UUID` | FK → parameter_definition | |
| `suggested_score` | `NUMERIC(7,4)` | | Canonical 0–100 |
| `suggested_rubric_level` | `INTEGER` | | Rubric level |
| `confidence` | `NUMERIC(5,4)` | | 0.0000–1.0000 |
| `evidence_strength` | `VARCHAR(50)` | | `HIGH`, `MEDIUM`, `LOW`, `INSUFFICIENT` |
| `justification` | `TEXT` | | AI explanation |
| `strengths` | `JSONB` | | Array of strengths |
| `improvement_areas` | `JSONB` | | Array of areas |
| `dimension_scores` | `JSONB` | | Per-dimension scores |
| `evidence_references` | `JSONB` | | Array of evidence_item IDs |
| `raw_ai_response` | `TEXT` | | Full raw response |

---

### 3.5 Result Tables

#### `assessment_result`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `assessment_id` | `UUID` | FK → assessment | |
| `framework_version_id` | `UUID` | FK → framework_version | |
| `evaluation_run_id` | `UUID` | FK → evaluation_run | Source evaluation |
| `overall_score` | `NUMERIC(7,4)` | | 0.0000–100.0000 |
| `overall_rating` | `VARCHAR(50)` | | Display rating |
| `result_status` | `VARCHAR(50)` | NOT NULL | `PRELIMINARY`, `REVIEWED`, `APPROVED`, `OVERRIDDEN` |
| `version` | `INTEGER` | NOT NULL, DEFAULT 1 | Result version |
| `calculated_at` | `TIMESTAMPTZ` | NOT NULL | |

**Index:** `(assessment_id, version DESC)` — latest result

#### `parameter_result`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `assessment_result_id` | `UUID` | FK → assessment_result | |
| `parameter_definition_id` | `UUID` | FK → parameter_definition | |
| `scoring_strategy_type` | `VARCHAR(50)` | NOT NULL | Strategy used |
| `raw_value` | `NUMERIC(12,4)` | | Raw metric |
| `normalized_score` | `NUMERIC(7,4)` | | Canonical 0–100 |
| `ai_suggested_score` | `NUMERIC(7,4)` | | AI suggestion |
| `human_approved_score` | `NUMERIC(7,4)` | | Human decision |
| `final_score` | `NUMERIC(7,4)` | | Effective final score |
| `weight` | `NUMERIC(5,4)` | NOT NULL | Weight at calculation time |
| `weighted_score` | `NUMERIC(7,4)` | | final × weight |
| `evidence_sufficiency` | `VARCHAR(50)` | | `SUFFICIENT`, `INSUFFICIENT`, `MISSING_REQUIRED`, `NOT_APPLICABLE` |
| `evidence_strength` | `VARCHAR(50)` | | `HIGH`, `MEDIUM`, `LOW`, `INSUFFICIENT` |
| `evaluation_confidence` | `NUMERIC(5,4)` | | 0.0000–1.0000 |
| `rubric_level` | `INTEGER` | | Mapped rubric level |
| `display_rating` | `VARCHAR(50)` | | Display rating |
| `calculation_metadata` | `JSONB` | | Audit data for calculation |
| `calculated_at` | `TIMESTAMPTZ` | NOT NULL | |

---

### 3.6 Review Tables

#### `review_session`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `assessment_id` | `UUID` | FK → assessment | |
| `assessment_result_id` | `UUID` | FK → assessment_result | |
| `reviewer_user_id` | `UUID` | NOT NULL | |
| `reviewer_name` | `VARCHAR(255)` | NOT NULL | |
| `reviewer_role` | `VARCHAR(100)` | NOT NULL | |
| `status` | `VARCHAR(50)` | NOT NULL | |
| `comments` | `TEXT` | | |
| `started_at` | `TIMESTAMPTZ` | NOT NULL | |
| `completed_at` | `TIMESTAMPTZ` | | |

#### `parameter_review`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `review_session_id` | `UUID` | FK → review_session | |
| `parameter_definition_id` | `UUID` | FK → parameter_definition | |
| `action` | `VARCHAR(50)` | NOT NULL | `APPROVED`, `MODIFIED`, `RETURNED` |
| `original_ai_score` | `NUMERIC(7,4)` | | AI score at review time |
| `approved_score` | `NUMERIC(7,4)` | | Approved score |
| `override_reason` | `TEXT` | | Mandatory if MODIFIED |
| `reviewed_at` | `TIMESTAMPTZ` | NOT NULL | |

#### `overall_override`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `review_session_id` | `UUID` | FK → review_session | |
| `original_overall_score` | `NUMERIC(7,4)` | NOT NULL | |
| `overridden_overall_score` | `NUMERIC(7,4)` | NOT NULL | |
| `justification` | `TEXT` | NOT NULL | |
| `approved_by` | `UUID` | NOT NULL | Senior reviewer |
| `approved_at` | `TIMESTAMPTZ` | NOT NULL | |

---

### 3.7 Pen Picture Table

#### `pen_picture`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `assessment_id` | `UUID` | FK → assessment | |
| `assessment_result_id` | `UUID` | FK → assessment_result | |
| `domain_type` | `VARCHAR(50)` | NOT NULL | |
| `content` | `TEXT` | NOT NULL | Generated text |
| `generation_context` | `JSONB` | | Input data summary |
| `provider_type` | `VARCHAR(50)` | | AI provider used |
| `model_name` | `VARCHAR(100)` | | Model used |
| `prompt_template_id` | `UUID` | | Prompt used |
| `prompt_version` | `INTEGER` | | |
| `version` | `INTEGER` | NOT NULL, DEFAULT 1 | Pen picture version |
| `generated_at` | `TIMESTAMPTZ` | NOT NULL | |
| `generated_by` | `UUID` | NOT NULL | |

---

### 3.8 Academic Tables (Student Extension)

#### `academic_record`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `assessment_id` | `UUID` | FK → assessment, UNIQUE | |
| `institution_id` | `UUID` | NOT NULL | |
| `student_external_id` | `VARCHAR(255)` | NOT NULL | |
| `academic_year` | `VARCHAR(20)` | NOT NULL | |
| `grade_level` | `VARCHAR(50)` | | Class/grade |
| `section` | `VARCHAR(50)` | | |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | |

#### `subject_enrollment`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `academic_record_id` | `UUID` | FK → academic_record | |
| `subject_code` | `VARCHAR(50)` | NOT NULL | |
| `subject_name` | `VARCHAR(255)` | NOT NULL | |

**Index:** `(academic_record_id, subject_code)` UNIQUE

#### `academic_result`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `subject_enrollment_id` | `UUID` | FK → subject_enrollment | |
| `assessment_type` | `VARCHAR(50)` | NOT NULL | `EXAM`, `MIDTERM`, `CLASS_TEST`, `QUIZ`, etc. |
| `title` | `VARCHAR(255)` | | e.g., "Final Exam" |
| `score` | `NUMERIC(7,2)` | NOT NULL | Obtained |
| `max_score` | `NUMERIC(7,2)` | NOT NULL | Maximum possible |
| `percentage` | `NUMERIC(7,4)` | NOT NULL | score/max_score × 100 |
| `assessment_date` | `DATE` | NOT NULL | |
| `weight` | `NUMERIC(5,4)` | | Weight within subject |

**Index:** `(subject_enrollment_id, assessment_type, assessment_date)` — time-series queries

#### `attendance_record`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `academic_record_id` | `UUID` | FK → academic_record | |
| `period_label` | `VARCHAR(100)` | NOT NULL | Month or term |
| `period_start` | `DATE` | NOT NULL | |
| `period_end` | `DATE` | NOT NULL | |
| `total_days` | `INTEGER` | NOT NULL | |
| `present_days` | `INTEGER` | NOT NULL | |
| `absent_days` | `INTEGER` | NOT NULL | |
| `late_days` | `INTEGER` | NOT NULL, DEFAULT 0 | |
| `attendance_percentage` | `NUMERIC(7,4)` | NOT NULL | |

---

### 3.9 Prompt & AI Configuration Tables

#### `prompt_template`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `name` | `VARCHAR(255)` | NOT NULL | Template name |
| `purpose` | `VARCHAR(100)` | NOT NULL | `PARAMETER_EVALUATION`, `PEN_PICTURE`, `EVIDENCE_MAPPING` |
| `domain_type` | `VARCHAR(50)` | | Domain-specific or null for generic |
| `template_content` | `TEXT` | NOT NULL | The prompt template text |
| `version` | `INTEGER` | NOT NULL | Sequential version |
| `active` | `BOOLEAN` | NOT NULL, DEFAULT true | |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | |
| `created_by` | `UUID` | | |

**Index:** `(name, version)` UNIQUE

---

### 3.10 Audit Table

#### `audit_event`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | PK | |
| `institution_id` | `UUID` | NOT NULL | |
| `event_type` | `VARCHAR(100)` | NOT NULL | Event type |
| `entity_type` | `VARCHAR(100)` | NOT NULL | Affected entity type |
| `entity_id` | `UUID` | NOT NULL | Affected entity |
| `user_id` | `UUID` | | Acting user |
| `details` | `JSONB` | | Event details |
| `occurred_at` | `TIMESTAMPTZ` | NOT NULL | |

**Indexes:**
- `(institution_id, entity_type, entity_id)` — entity history
- `(institution_id, event_type, occurred_at)` — type queries
- `(occurred_at)` — time-range queries

---

## 4. Versioning Strategy

### 4.1 Framework Version Snapshots

When a `framework_version` is activated:
1. Its status changes from `DRAFT` to `ACTIVE`.
2. All child `parameter_definition`, `rubric_level`, `parameter_weight`, `evidence_rule`, `rating_scale`, and `rating_mapping` records become immutable.
3. The previous active version's status changes to `SUPERSEDED` with `effective_to` set.
4. New assessments created after activation reference the new version.
5. Existing in-progress assessments continue to reference their original version.

### 4.2 Assessment Result Versioning

`assessment_result.version` increments when:
- Human review modifies parameter scores, triggering recalculation.
- An overall override is applied.

Previous result versions are NOT deleted. New versions reference the same `assessment_id` with an incremented `version` number. This provides a complete recalculation history.

### 4.3 Prompt Version Tracking

`prompt_template.version` increments on template edits. Evaluation runs capture the exact `prompt_template_id` and `prompt_version` used, ensuring reproducibility.

### 4.4 AI Model Metadata

Every `evaluation_run` records the exact `provider_type`, `model_name`, `model_version`, and `temperature`. This metadata is immutable and tied to the evaluation results.

---

## 5. Historical Reproducibility

To reproduce any historical assessment:

```text
assessment
  → framework_version_id  → frozen parameters, rubrics, weights, scoring rules
  → evidence_items        → frozen evidence with provenance
  → evaluation_run        → frozen AI model metadata + prompt version
  → assessment_result     → frozen parameter results with scoring metadata
  → review_session        → frozen human decisions with justifications
  → pen_picture           → frozen generated text with generation context
```

All links are via immutable IDs. No mutable relationship can silently change historical results.

---

## 6. Indexing Strategy

### Primary Indexes
- All PKs (UUID) — automatically indexed
- All FKs — indexed for join performance

### Business Indexes
- `assessment(institution_id, external_subject_id, period_start)` — subject lookup
- `assessment(institution_id, status)` — workflow queries
- `evidence_item(assessment_id, source_type)` — evidence filtering
- `evaluation_run(assessment_id, started_at DESC)` — latest evaluation
- `assessment_result(assessment_id, version DESC)` — latest result
- `audit_event(institution_id, entity_type, entity_id)` — audit trail
- `academic_result(subject_enrollment_id, assessment_type, assessment_date)` — time-series

### Future Indexes
- `pgvector` index on a future `embedding` column for RAG (GiST/IVFFlat/HNSW)

---

## 7. Data Integrity Constraints

| Constraint | Table(s) | Rule |
|------------|----------|------|
| Weight sum validation | `parameter_weight` | All weights for a `framework_version_id` must sum to 1.0000 (application-enforced) |
| Rubric level ordering | `rubric_level` | Levels must be sequential; `canonical_score_min` of level N+1 = `canonical_score_max` of level N |
| Score range | `parameter_result` | `normalized_score`, `final_score` ∈ [0.0000, 100.0000] |
| Override reason required | `parameter_review` | If `action = 'MODIFIED'`, `override_reason` must be non-null |
| Academic score range | `academic_result` | `score` ≤ `max_score`; both > 0 |
| Attendance consistency | `attendance_record` | `present_days + absent_days ≤ total_days` |

---

## 8. Future RAG Extension Point

When pgvector is added, the schema extension will be:

```sql
-- Future migration
ALTER TABLE evidence_item ADD COLUMN embedding VECTOR(1536);
CREATE INDEX idx_evidence_embedding ON evidence_item USING hnsw (embedding vector_cosine_ops);

-- Or a dedicated table:
CREATE TABLE knowledge_embedding (
    id UUID PRIMARY KEY,
    institution_id UUID NOT NULL,
    source_type VARCHAR(50) NOT NULL,      -- HR_POLICY, COMPETENCY_FRAMEWORK, etc.
    source_reference VARCHAR(255) NOT NULL,
    chunk_text TEXT NOT NULL,
    embedding VECTOR(1536) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL
);
```

The current schema design does not depend on or preclude this extension.
