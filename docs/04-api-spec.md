# 04 — API Specification

## 1. API Architecture Principles

| # | Principle |
|---|-----------|
| A1 | **RESTful** — Resources and standard HTTP methods. |
| A2 | **Versioned** — All endpoints under `/api/v1/`. |
| A3 | **Institution-scoped** — Institutional context via JWT claim or header; enforced server-side. |
| A4 | **Command/query separation** — Write operations (POST/PUT/PATCH) are separated from reads (GET) where it improves clarity. |
| A5 | **Consistent error format** — Standard error response body across all endpoints. |
| A6 | **Pagination** — All list endpoints support `page`, `size`, `sort`. |
| A7 | **OpenAPI documented** — All endpoints documented with Swagger/OpenAPI annotations. |

### Standard Error Response

```json
{
  "timestamp": "2027-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    { "field": "name", "message": "must not be blank" }
  ],
  "traceId": "abc-123"
}
```

### Standard Page Response

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3
}
```

---

## 2. Framework API

Manages assessment frameworks, parameters, rubrics, weights, and versioning.

### 2.1 Framework Lifecycle

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/api/v1/frameworks` | Create a new framework | `FRAMEWORK_MANAGER` |
| `GET` | `/api/v1/frameworks` | List frameworks (paginated, filtered) | `AUTHENTICATED` |
| `GET` | `/api/v1/frameworks/{frameworkId}` | Get framework details | `AUTHENTICATED` |
| `PUT` | `/api/v1/frameworks/{frameworkId}` | Update framework metadata | `FRAMEWORK_MANAGER` |
| `PATCH` | `/api/v1/frameworks/{frameworkId}/status` | Archive/reactivate | `ADMIN` |

#### Create Framework Request

```json
{
  "code": "DESCO_EMP_2027",
  "name": "DESCO Employee Performance Framework 2027",
  "description": "Annual employee performance assessment framework",
  "domainType": "EMPLOYEE"
}
```

#### Framework Response

```json
{
  "id": "uuid",
  "institutionId": "uuid",
  "code": "DESCO_EMP_2027",
  "name": "DESCO Employee Performance Framework 2027",
  "description": "...",
  "domainType": "EMPLOYEE",
  "status": "DRAFT",
  "currentVersionNumber": null,
  "createdAt": "2027-01-01T00:00:00Z"
}
```

### 2.2 Framework Versions

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/api/v1/frameworks/{frameworkId}/versions` | Create a new draft version | `FRAMEWORK_MANAGER` |
| `GET` | `/api/v1/frameworks/{frameworkId}/versions` | List versions | `AUTHENTICATED` |
| `GET` | `/api/v1/frameworks/{frameworkId}/versions/{versionId}` | Get version details (includes all parameters, rubrics, weights) | `AUTHENTICATED` |
| `POST` | `/api/v1/frameworks/{frameworkId}/versions/{versionId}/activate` | Activate version (freezes it) | `ADMIN` |

#### Create Version Request

```json
{
  "effectiveFrom": "2027-01-01",
  "scoringRuleSet": {
    "roundingMode": "HALF_UP",
    "roundingScale": 2,
    "minimumParametersRequired": 20
  }
}
```

### 2.3 Parameter Definitions

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/api/v1/frameworks/{fId}/versions/{vId}/parameters` | Add parameter to draft version | `FRAMEWORK_MANAGER` |
| `GET` | `/api/v1/frameworks/{fId}/versions/{vId}/parameters` | List parameters | `AUTHENTICATED` |
| `GET` | `/api/v1/frameworks/{fId}/versions/{vId}/parameters/{paramId}` | Get parameter detail | `AUTHENTICATED` |
| `PUT` | `/api/v1/frameworks/{fId}/versions/{vId}/parameters/{paramId}` | Update parameter (draft only) | `FRAMEWORK_MANAGER` |
| `DELETE` | `/api/v1/frameworks/{fId}/versions/{vId}/parameters/{paramId}` | Remove parameter (draft only) | `FRAMEWORK_MANAGER` |

#### Create Parameter Request

```json
{
  "code": "LEADERSHIP",
  "name": "Leadership",
  "description": "Ability to lead teams, make decisions, and inspire others",
  "purpose": "Assess leadership competency relevant to role level",
  "category": "BEHAVIORAL",
  "scoringStrategyType": "QUALITATIVE_RUBRIC",
  "scoringStrategyConfig": {
    "dimensionsWeighted": true
  },
  "rubricLevels": [
    {
      "level": 1,
      "label": "Unsatisfactory",
      "definition": "Unable to effectively lead or coordinate",
      "canonicalScoreMin": 0.0,
      "canonicalScoreMax": 30.0,
      "canonicalScoreMidpoint": 15.0
    },
    {
      "level": 2,
      "label": "Needs Improvement",
      "definition": "Shows limited leadership",
      "canonicalScoreMin": 30.0,
      "canonicalScoreMax": 50.0,
      "canonicalScoreMidpoint": 40.0
    },
    {
      "level": 3,
      "label": "Meets Expectations",
      "definition": "Effectively manages normal responsibilities",
      "canonicalScoreMin": 50.0,
      "canonicalScoreMax": 70.0,
      "canonicalScoreMidpoint": 60.0
    },
    {
      "level": 4,
      "label": "Exceeds Expectations",
      "definition": "Frequently demonstrates strong leadership",
      "canonicalScoreMin": 70.0,
      "canonicalScoreMax": 85.0,
      "canonicalScoreMidpoint": 77.5
    },
    {
      "level": 5,
      "label": "Outstanding",
      "definition": "Exceptional leadership with measurable organizational impact",
      "canonicalScoreMin": 85.0,
      "canonicalScoreMax": 100.0,
      "canonicalScoreMidpoint": 92.5
    }
  ],
  "evaluationDimensions": [
    { "code": "ACHIEVEMENT", "name": "Achievement", "weight": 0.30 },
    { "code": "QUALITY", "name": "Quality", "weight": 0.25 },
    { "code": "CONSISTENCY", "name": "Consistency", "weight": 0.20 },
    { "code": "IMPACT", "name": "Impact", "weight": 0.25 }
  ],
  "evidenceRules": [
    {
      "eligibleSourceTypes": ["SUPERVISOR", "PEER", "OFFICIAL_RECORD", "SELF_REPORTED"],
      "requiredSourceTypes": ["SUPERVISOR"],
      "maximumAgeDays": 365,
      "requiresVerification": false
    }
  ],
  "minimumEvidenceRequirements": {
    "minimumItems": 2,
    "requiredSourceTypes": ["SUPERVISOR"]
  },
  "missingDataPolicy": "FLAG_FOR_REVIEW",
  "displayOrder": 1
}
```

### 2.4 Parameter Weights

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `PUT` | `/api/v1/frameworks/{fId}/versions/{vId}/weights` | Set all parameter weights (batch) | `FRAMEWORK_MANAGER` |
| `GET` | `/api/v1/frameworks/{fId}/versions/{vId}/weights` | Get current weights | `AUTHENTICATED` |

#### Set Weights Request

```json
{
  "weights": [
    { "parameterCode": "LEADERSHIP", "weight": 0.08 },
    { "parameterCode": "PRODUCTIVITY", "weight": 0.10 },
    { "parameterCode": "ATTENDANCE", "weight": 0.05 }
  ]
}
```

> **Validation:** All weights must sum to 1.0000.

### 2.5 Rating Scale

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `PUT` | `/api/v1/frameworks/{fId}/versions/{vId}/rating-scale` | Set rating scale | `FRAMEWORK_MANAGER` |
| `GET` | `/api/v1/frameworks/{fId}/versions/{vId}/rating-scale` | Get rating scale | `AUTHENTICATED` |

---

## 3. Assessment API

Manages assessment lifecycle.

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/api/v1/assessments` | Create a new assessment | `ASSESSOR` |
| `GET` | `/api/v1/assessments` | List assessments (filtered, paginated) | `AUTHENTICATED` |
| `GET` | `/api/v1/assessments/{assessmentId}` | Get assessment details | `AUTHENTICATED` |
| `PATCH` | `/api/v1/assessments/{assessmentId}/status` | Transition status | `ASSESSOR` |

#### Create Assessment Request

```json
{
  "frameworkVersionId": "uuid",
  "subject": {
    "externalSubjectId": "EMP-001",
    "subjectType": "EMPLOYEE",
    "displayName": "Rahim Ahmed",
    "departmentOrClass": "Engineering"
  },
  "period": {
    "startDate": "2027-01-01",
    "endDate": "2027-12-31",
    "label": "Annual 2027"
  }
}
```

#### Assessment Detail Response

```json
{
  "id": "uuid",
  "institutionId": "uuid",
  "frameworkVersionId": "uuid",
  "frameworkCode": "DESCO_EMP_2027",
  "subject": {
    "externalSubjectId": "EMP-001",
    "subjectType": "EMPLOYEE",
    "displayName": "Rahim Ahmed",
    "departmentOrClass": "Engineering"
  },
  "period": {
    "startDate": "2027-01-01",
    "endDate": "2027-12-31",
    "label": "Annual 2027"
  },
  "status": "EVIDENCE_COLLECTION",
  "evidenceCount": 12,
  "latestEvaluationRunId": null,
  "latestResultId": null,
  "createdAt": "2027-01-15T10:00:00Z"
}
```

---

## 4. Evidence API

Manages evidence submission and management.

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/api/v1/assessments/{aId}/evidence` | Submit evidence item | `ASSESSOR`, `SUBJECT` |
| `POST` | `/api/v1/assessments/{aId}/evidence/batch` | Batch submit evidence | `ASSESSOR` |
| `GET` | `/api/v1/assessments/{aId}/evidence` | List evidence items | `ASSESSOR`, `REVIEWER` |
| `GET` | `/api/v1/assessments/{aId}/evidence/{evidenceId}` | Get evidence detail | `ASSESSOR`, `REVIEWER` |
| `POST` | `/api/v1/assessments/{aId}/evidence/{eId}/mappings` | Map evidence to parameter | `ASSESSOR` |
| `GET` | `/api/v1/assessments/{aId}/evidence/sufficiency` | Get evidence sufficiency per parameter | `ASSESSOR` |
| `PATCH` | `/api/v1/assessments/{aId}/evidence/{eId}/verification` | Update verification status | `ASSESSOR` |

#### Submit Evidence Request

```json
{
  "sourceType": "SUPERVISOR",
  "sourceIdentifier": "Supervisor: Karim Rahman",
  "content": "Rahim led an 8-person team to deliver the billing modernization project on schedule. The project reduced processing time by 15%.",
  "contentType": "TEXT",
  "evidenceDate": "2027-06-15",
  "parameterMappings": [
    { "parameterCode": "LEADERSHIP", "mappingType": "PRIMARY" },
    { "parameterCode": "PRODUCTIVITY", "mappingType": "SUPPORTING" }
  ],
  "metadata": {
    "projectName": "Billing Modernization"
  }
}
```

#### Evidence Sufficiency Response

```json
{
  "assessmentId": "uuid",
  "parameterSufficiency": [
    {
      "parameterCode": "LEADERSHIP",
      "parameterName": "Leadership",
      "sufficiency": "SUFFICIENT",
      "totalItems": 5,
      "requiredSourcesMet": true,
      "minimumItemsMet": true
    },
    {
      "parameterCode": "INNOVATION",
      "parameterName": "Innovation",
      "sufficiency": "INSUFFICIENT",
      "totalItems": 1,
      "requiredSourcesMet": false,
      "minimumItemsMet": false
    }
  ]
}
```

---

## 5. Evaluation API

Triggers AI evaluation and retrieves results.

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/api/v1/assessments/{aId}/evaluate` | Trigger AI evaluation | `ASSESSOR` |
| `GET` | `/api/v1/assessments/{aId}/evaluations` | List evaluation runs | `ASSESSOR`, `REVIEWER` |
| `GET` | `/api/v1/assessments/{aId}/evaluations/{runId}` | Get evaluation run details | `ASSESSOR`, `REVIEWER` |
| `GET` | `/api/v1/assessments/{aId}/evaluations/latest` | Get latest evaluation | `ASSESSOR`, `REVIEWER` |

#### Trigger Evaluation Request

```json
{
  "parameterCodes": null,
  "forceReEvaluation": false
}
```

> `parameterCodes = null` evaluates all parameters. Specific codes allow re-evaluation of individual parameters.

#### Evaluation Run Response

```json
{
  "id": "uuid",
  "assessmentId": "uuid",
  "status": "COMPLETED",
  "aiModel": {
    "providerType": "gemini",
    "modelName": "gemini-2.5-pro",
    "modelVersion": "2027-01"
  },
  "promptVersion": 3,
  "parameterEvaluations": [
    {
      "parameterCode": "LEADERSHIP",
      "parameterName": "Leadership",
      "suggestedScore": 77.5,
      "suggestedRubricLevel": 4,
      "confidence": 0.88,
      "evidenceStrength": "HIGH",
      "justification": "Strong evidence of project leadership...",
      "strengths": [
        "Led 8-person team to on-schedule delivery",
        "15% processing time reduction"
      ],
      "improvementAreas": [
        "Limited evidence of mentoring junior staff"
      ],
      "dimensionScores": {
        "ACHIEVEMENT": 82.0,
        "QUALITY": 78.0,
        "CONSISTENCY": 70.0,
        "IMPACT": 80.0
      }
    }
  ],
  "startedAt": "2027-01-20T14:00:00Z",
  "completedAt": "2027-01-20T14:02:30Z"
}
```

---

## 6. Scoring / Result API

Retrieves deterministic scoring results.

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `GET` | `/api/v1/assessments/{aId}/results` | List result versions | `ASSESSOR`, `REVIEWER` |
| `GET` | `/api/v1/assessments/{aId}/results/latest` | Get latest result | `ASSESSOR`, `REVIEWER` |
| `GET` | `/api/v1/assessments/{aId}/results/{resultId}` | Get specific result version | `ASSESSOR`, `REVIEWER` |
| `POST` | `/api/v1/assessments/{aId}/results/recalculate` | Force recalculation | `ASSESSOR` |

#### Assessment Result Response

```json
{
  "id": "uuid",
  "assessmentId": "uuid",
  "version": 1,
  "overallScore": 72.3500,
  "overallRating": "4",
  "overallRatingLabel": "Exceeds Expectations",
  "resultStatus": "PRELIMINARY",
  "parameterResults": [
    {
      "parameterCode": "LEADERSHIP",
      "parameterName": "Leadership",
      "scoringStrategyType": "QUALITATIVE_RUBRIC",
      "rawValue": null,
      "normalizedScore": 77.5000,
      "aiSuggestedScore": 77.5000,
      "humanApprovedScore": null,
      "finalScore": 77.5000,
      "weight": 0.0800,
      "weightedScore": 6.2000,
      "evidenceSufficiency": "SUFFICIENT",
      "evidenceStrength": "HIGH",
      "evaluationConfidence": 0.8800,
      "rubricLevel": 4,
      "displayRating": "Exceeds Expectations"
    },
    {
      "parameterCode": "ATTENDANCE",
      "parameterName": "Attendance",
      "scoringStrategyType": "OBJECTIVE",
      "rawValue": 94.0000,
      "normalizedScore": 94.0000,
      "aiSuggestedScore": null,
      "humanApprovedScore": null,
      "finalScore": 94.0000,
      "weight": 0.0500,
      "weightedScore": 4.7000,
      "evidenceSufficiency": "SUFFICIENT",
      "evidenceStrength": "HIGH",
      "evaluationConfidence": 1.0000,
      "rubricLevel": 5,
      "displayRating": "Outstanding"
    }
  ],
  "calculatedAt": "2027-01-20T14:03:00Z"
}
```

---

## 7. Review API

Manages human review workflow.

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/api/v1/assessments/{aId}/reviews` | Start a review session | `REVIEWER` |
| `GET` | `/api/v1/assessments/{aId}/reviews` | List review sessions | `REVIEWER`, `AUDITOR` |
| `GET` | `/api/v1/assessments/{aId}/reviews/{reviewId}` | Get review details | `REVIEWER`, `AUDITOR` |
| `PUT` | `/api/v1/assessments/{aId}/reviews/{rId}/parameters/{paramCode}` | Submit parameter review | `REVIEWER` |
| `POST` | `/api/v1/assessments/{aId}/reviews/{rId}/complete` | Complete review session | `REVIEWER` |
| `POST` | `/api/v1/assessments/{aId}/reviews/{rId}/return` | Return for more evidence | `REVIEWER` |
| `POST` | `/api/v1/assessments/{aId}/reviews/{rId}/overall-override` | Override overall score | `SENIOR_REVIEWER` |

#### Submit Parameter Review Request

```json
{
  "action": "MODIFIED",
  "approvedScore": 72.0000,
  "overrideReason": "Evidence of leadership is strong but limited to a single project. Adjusted to account for scope."
}
```

#### Overall Override Request

```json
{
  "overriddenOverallScore": 75.0000,
  "justification": "Multiple parameters showed consistent above-expectations performance not fully captured by individual scores."
}
```

---

## 8. Pen Picture API

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/api/v1/assessments/{aId}/pen-pictures` | Generate pen picture | `ASSESSOR`, `REVIEWER` |
| `GET` | `/api/v1/assessments/{aId}/pen-pictures` | List pen picture versions | `AUTHENTICATED` |
| `GET` | `/api/v1/assessments/{aId}/pen-pictures/latest` | Get latest pen picture | `AUTHENTICATED` |
| `POST` | `/api/v1/assessments/{aId}/pen-pictures/{ppId}/regenerate` | Regenerate pen picture | `ASSESSOR`, `REVIEWER` |

#### Pen Picture Response

```json
{
  "id": "uuid",
  "assessmentId": "uuid",
  "domainType": "EMPLOYEE",
  "content": "Rahim Ahmed has demonstrated strong performance throughout the 2027 assessment period...",
  "version": 1,
  "aiModel": {
    "providerType": "gemini",
    "modelName": "gemini-2.5-pro"
  },
  "generatedAt": "2027-01-21T10:00:00Z"
}
```

---

## 9. Student Academic API

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/api/v1/assessments/{aId}/academic-record` | Create academic record | `ASSESSOR` |
| `GET` | `/api/v1/assessments/{aId}/academic-record` | Get academic record | `ASSESSOR`, `REVIEWER` |
| `POST` | `/api/v1/assessments/{aId}/academic-record/subjects` | Add subject enrollment | `ASSESSOR` |
| `POST` | `/api/v1/assessments/{aId}/academic-record/subjects/{sId}/results` | Add academic result | `ASSESSOR` |
| `POST` | `/api/v1/assessments/{aId}/academic-record/subjects/{sId}/results/batch` | Batch import results | `ASSESSOR` |
| `POST` | `/api/v1/assessments/{aId}/academic-record/attendance` | Add attendance record | `ASSESSOR` |
| `POST` | `/api/v1/assessments/{aId}/academic-record/attendance/batch` | Batch import attendance | `ASSESSOR` |
| `GET` | `/api/v1/assessments/{aId}/academic-record/calculated-facts` | Get deterministically calculated academic facts | `ASSESSOR`, `REVIEWER` |

#### Add Academic Result Request

```json
{
  "assessmentType": "EXAM",
  "title": "Final Exam - Mathematics",
  "score": 87.0,
  "maxScore": 100.0,
  "assessmentDate": "2027-11-15",
  "weight": 0.40
}
```

#### Calculated Academic Facts Response

```json
{
  "assessmentId": "uuid",
  "studentExternalId": "STU-001",
  "subjectPerformances": [
    {
      "subjectCode": "MATH",
      "subjectName": "Mathematics",
      "weightedAverage": 86.35,
      "examScore": 87.0,
      "continuousAssessmentScore": 85.75,
      "trend": "IMPROVING",
      "growth": 5.2
    },
    {
      "subjectCode": "PHY",
      "subjectName": "Physics",
      "weightedAverage": 78.40,
      "examScore": 75.0,
      "continuousAssessmentScore": 80.0,
      "trend": "STABLE",
      "growth": 1.1
    }
  ],
  "overallAcademicScore": 82.5,
  "academicGrowth": {
    "previousOverallScore": 77.3,
    "currentOverallScore": 82.5,
    "absoluteGrowth": 5.2,
    "percentageGrowth": 6.73,
    "growthCategory": "MODERATE_IMPROVEMENT"
  },
  "academicTrend": "IMPROVING",
  "academicConsistency": {
    "standardDeviation": 4.8,
    "coefficientOfVariation": 5.8,
    "consistencyRating": "HIGH"
  },
  "overallAttendancePercentage": 94.5,
  "strengths": ["ICT", "English", "Mathematics"],
  "developmentAreas": ["Physics", "Bangla"]
}
```

---

## 10. Audit API

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `GET` | `/api/v1/audit/events` | Query audit events (filtered, paginated) | `AUDITOR`, `ADMIN` |
| `GET` | `/api/v1/audit/events/{eventId}` | Get event details | `AUDITOR`, `ADMIN` |
| `GET` | `/api/v1/audit/entities/{entityType}/{entityId}` | Get entity audit trail | `AUDITOR`, `ADMIN` |

#### Query Parameters

- `entityType` — filter by entity type
- `entityId` — filter by entity
- `eventType` — filter by event type
- `userId` — filter by acting user
- `from` / `to` — time range
- `page`, `size`, `sort` — pagination

---

## 11. Health & Admin API

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `GET` | `/actuator/health` | Application health | Public |
| `GET` | `/actuator/info` | Application info | `ADMIN` |
| `GET` | `/api/v1/admin/ai-providers` | List configured AI providers | `ADMIN` |
| `GET` | `/api/v1/admin/ai-providers/{provider}/status` | AI provider health | `ADMIN` |

---

## 12. API Versioning Strategy

- URL-based versioning: `/api/v1/`, `/api/v2/`
- Breaking changes require a new version
- Non-breaking additions (new fields, new endpoints) are backward-compatible
- Deprecation policy: v(N-1) supported for 12 months after v(N) release

---

## 13. Authentication & Authorization Headers

```http
Authorization: Bearer <JWT>
X-Institution-Id: <uuid>       (optional, derived from JWT if not present)
X-Request-Id: <uuid>           (for tracing)
Content-Type: application/json
Accept: application/json
```

The JWT payload carries:

```json
{
  "sub": "user-uuid",
  "institutionId": "institution-uuid",
  "roles": ["ASSESSOR", "REVIEWER"],
  "exp": 1735689600
}
```
