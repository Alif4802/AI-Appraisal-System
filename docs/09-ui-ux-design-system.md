# 09 — UI/UX Design System

## 1. Design Goals

| # | Goal | Rationale |
|---|------|-----------|
| UX1 | **Evidence-first assessment interface** | The platform evaluates evidence, not prose. The UI must surface evidence provenance, strength, and relationship to scores. |
| UX2 | **Clear score attribution** | Users must always distinguish objective calculation, AI suggestion, human approval, human modification, and final institutional result. |
| UX3 | **Domain-appropriate density** | Employee reviewer workbench needs high density; student mobile view needs low density. One density does not fit all. |
| UX4 | **Institutional credibility** | Assessment outputs become institutional records. The interface must feel authoritative, precise, and trustworthy — never flashy or decorative. |
| UX5 | **Bilingual readiness** | English and Bangla must be first-class. Layout cannot break on longer Bengali labels. |
| UX6 | **Themeable foundation** | Dark mode first; light mode, institutional branding, and accessibility adjustments must be achievable through token changes alone. |
| UX7 | **Auditable interaction** | Every consequential action must provide clear feedback, confirmation, and reversibility. Override history must be inspectable, not buried. |
| UX8 | **Reduced cognitive load for students** | Students are not HR professionals. Their experience must be academically understandable and constructive. |

---

## 2. Core UX Principles

### From Dieter Rams

| Principle | Application |
|-----------|-------------|
| **Less, but better** | Every element earns its place. No decorative cards, nested containers, gradient blobs, or filler content. |
| **Unobtrusive** | The interface recedes; the assessment data is the focus. No animations that compete with content. |
| **Honest** | Scores display what they are. Insufficient evidence is not hidden. AI suggestions are labeled as suggestions. |
| **Thorough** | Attention to detail in spacing, alignment, typographic hierarchy, and state representation. |

> "Less" does NOT mean excessive empty space. It means **no unnecessary elements**. A reviewer workbench with 25 parameters, evidence, scores, and rubrics is inherently information-dense. The design must handle that density cleanly.

### From Jakob Nielsen

| Heuristic | Application |
|-----------|-------------|
| **Visibility of system status** | Assessment lifecycle state is always visible. AI evaluation progress is shown in real time. Long operations show determinate progress where possible. |
| **Recognition over recall** | Parameter names, rubric levels, evidence source types, and status meanings are always labeled — never just color-coded or icon-only. |
| **Consistency and standards** | Score presentation, evidence display, status badges, and review actions use identical patterns across Employee, Teacher, and Student domains. |
| **Error prevention** | Destructive actions (finalize, override) require confirmation. Weight inputs validate to sum = 1.0 before save. |
| **Actionable error recovery** | Errors describe what went wrong and what the user can do. "Evidence insufficient — 2 sources required, 1 provided. Add evidence." |

### From Ben Shneiderman

| Principle | Application |
|-----------|-------------|
| **Internal locus of control** | The user drives the assessment workflow. AI assists; it does not auto-advance states or auto-approve. |
| **Informative feedback** | Every action produces visible confirmation. Score recalculation shows before/after. Override records the reason. |
| **Clear closure** | Assessment lifecycle has explicit completion states. Finalization is a deliberate, confirmed action. |
| **Easy reversal** | Return-from-review sends an assessment back to evidence collection. Pen picture can be regenerated. Parameter review can be re-modified before session completion. |
| **Reduced memory load** | Rubric definitions are visible during review. Evidence is accessible in-context, not on a separate page. Framework configuration shows current weights alongside parameters. |

---

## 3. Domain-Specific UX Differences

The three domains share the design system but present fundamentally different experiences.

```text
┌───────────────────────────────────────────────────────────────────┐
│                    Shared Design System                           │
│  Tokens · Typography · Components · Status · Evidence Patterns   │
├───────────────────┬──────────────────┬────────────────────────────┤
│  Employee / DESCO │    Teacher       │      Student / LMS         │
│                   │                  │                            │
│  Annual appraisal │  Teaching        │  Academic performance      │
│  KPI targets      │  effectiveness   │  Subject-level results     │
│  25 parameters    │  Classroom obs.  │  Exams, quizzes, etc.      │
│  Dense review     │  Student outcomes│  Growth and trends         │
│  Override workflow│  Prof. dev.      │  Mobile-first              │
│                   │                  │                            │
│  HIGH density     │  MODERATE-HIGH   │  MODERATE (desktop)        │
│  Desktop-first    │  Desktop-first   │  LOW (mobile)              │
│  Formal language  │  Professional    │  Constructive, academic    │
└───────────────────┴──────────────────┴────────────────────────────┘
```

### Employee / DESCO

- Annual cycle: single assessment per subject per year
- ~25 parameters across KPI, behavioral, and organizational categories
- Reviewer workbench is the primary interface — optimize for parameter-by-parameter review with evidence inspection
- Override workflows are frequent and must be ergonomic
- Audit/history access is a regular need, not an edge case
- Language: formal, institutional
- Density: high — reviewers are trained professionals who need all data visible

### Teacher

- Focus: teaching quality, student engagement, professional contribution
- Evidence includes classroom observations, student outcomes, peer feedback
- Similar structural workflow to Employee but different vocabulary and parameter types
- Pen pictures emphasize pedagogical effectiveness
- Density: moderate-to-high

### Student / LMS

- Fundamentally different: structured academic data, not narrative evidence
- Subject-level performance is the primary frame, not a parameter list
- Growth, trend, and consistency are first-class concepts
- Student-facing language must be constructive: "Development area" not "Weakness"; "Growth opportunity" not "Low performance"
- Parent/guardian may also view results — language must be appropriate
- Mobile-first: many students will access via phone
- Exam/quiz/assignment results should feel like a gradebook, not an appraisal form
- Density: moderate on desktop, low on mobile

---

## 4. Role-Based Information Architecture

### 4.1 Roles and Primary Views

| Role | Primary Interface | Key Actions |
|------|-------------------|-------------|
| `ADMIN` | Framework management, user management, system configuration | Create/activate frameworks, manage users, view audit logs |
| `FRAMEWORK_MANAGER` | Framework builder | Define parameters, rubrics, weights, scoring strategies, version management |
| `ASSESSOR` | Assessment management | Create assessments, submit evidence, trigger evaluations, view results |
| `REVIEWER` | Reviewer workbench | Review parameter scores, approve/modify/return, write comments |
| `SENIOR_REVIEWER` | Reviewer workbench + override panel | All reviewer actions + overall score override |
| `SUBJECT` (employee/teacher) | Self-service evidence, view results | Submit self-reported evidence, view approved results and pen picture |
| `STUDENT` (or parent/guardian) | Student academic overview | View academic performance, growth, strengths, development areas |
| `AUDITOR` | Audit trail viewer | Read-only access to full assessment history and override records |

### 4.2 Information Architecture

```text
Platform Root
├── Dashboard
│   ├── My Assessments (subject role)
│   ├── Pending Reviews (reviewer role)
│   ├── Framework Status (admin role)
│   └── Recent Activity
│
├── Frameworks (admin/framework manager)
│   ├── Framework List
│   ├── Framework Detail
│   │   ├── Version History
│   │   ├── Parameters
│   │   ├── Rubrics
│   │   ├── Weights
│   │   ├── Rating Scale
│   │   └── Scoring Rules
│   └── Framework Builder (draft version)
│
├── Assessments
│   ├── Assessment List (filtered by status, period, domain)
│   ├── Assessment Detail
│   │   ├── Overview (status, subject, period, overall result)
│   │   ├── Evidence (list, submission, sufficiency)
│   │   ├── Evaluation (AI results, parameter evaluations)
│   │   ├── Results (parameter scores, overall score, rating)
│   │   ├── Review (reviewer workbench)
│   │   ├── Pen Picture
│   │   └── History / Audit Trail
│   └── New Assessment
│
├── Academic Records (student domain only)
│   ├── Student Overview
│   ├── Subject Performance
│   ├── Growth & Trends
│   ├── Attendance
│   └── Academic Import
│
├── Audit (auditor/admin)
│   ├── Event Search
│   ├── Entity History
│   └── Override Log
│
└── Settings (admin)
    ├── AI Provider Configuration
    ├── Prompt Template Management
    ├── User/Role Management
    └── Institution Settings
```

---

## 5. Navigation Strategy

### 5.1 Structure

```text
┌──────────────────────────────────────────────────────────────┐
│  Top Bar                                                      │
│  ┌──────────┬──────────────────────────┬──────────────────┐  │
│  │ App Logo │     Breadcrumbs          │ User · Settings  │  │
│  └──────────┴──────────────────────────┴──────────────────┘  │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────┐  ┌───────────────────────────────────────┐  │
│  │            │  │                                       │  │
│  │  Sidebar   │  │          Main Content                 │  │
│  │  (primary  │  │                                       │  │
│  │   nav)     │  │                                       │  │
│  │            │  │                                       │  │
│  │  Dashboard │  │                                       │  │
│  │  Frameorks │  │                                       │  │
│  │  Assess-   │  │                                       │  │
│  │  ments     │  │                                       │  │
│  │  Academic  │  │                                       │  │
│  │  Audit     │  │                                       │  │
│  │  Settings  │  │                                       │  │
│  │            │  │                                       │  │
│  └────────────┘  └───────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### 5.2 Navigation Rules

- Sidebar shows only items the current user's roles can access
- Student role gets a simplified navigation without Framework, Audit, or Settings
- Sidebar collapses to icon-only rail on tablet; becomes bottom navigation on mobile
- Breadcrumbs always visible: `Assessments / Rahim Ahmed / Annual 2027 / Review`
- Assessment detail uses horizontal tabs for sub-sections (Overview · Evidence · Evaluation · Results · Review · Pen Picture · History)
- Active assessment status badge always visible in assessment detail header

### 5.3 Mobile Navigation (Student)

```text
┌──────────────────────────────────────┐
│  ┌──────────────────────────────┐    │
│  │         Content Area         │    │
│  │                              │    │
│  │                              │    │
│  │                              │    │
│  │                              │    │
│  │                              │    │
│  └──────────────────────────────┘    │
│                                      │
│  ┌──────────────────────────────┐    │
│  │  Overview  Subjects  Growth  │    │
│  │  Attend.   Profile          │    │
│  └──────────────────────────────┘    │
└──────────────────────────────────────┘
```

Bottom tab bar with 4–5 primary destinations. No sidebar. No breadcrumbs on mobile — use back navigation.

---

## 6. Semantic Design Token Architecture

### 6.1 Token Hierarchy

```text
Primitive Tokens (raw values)
    ↓
Semantic Tokens (purpose-driven names)
    ↓
Component Tokens (component-specific overrides)
```

### 6.2 Color Tokens

```css
/* === Canvas & Surface === */
--color-canvas:                    /* Page background */
--color-surface-primary:           /* Primary content surface */
--color-surface-secondary:         /* Secondary/nested surface */
--color-surface-elevated:          /* Modal, drawer, tooltip bg */
--color-surface-interactive:       /* Hover/active surface */
--color-surface-selected:          /* Selected row, active tab */

/* === Border === */
--color-border-default:            /* Standard borders */
--color-border-subtle:             /* Faint separators */
--color-border-strong:             /* Emphasized borders */
--color-border-interactive:        /* Input borders */
--color-border-focus:              /* Focus ring */

/* === Text === */
--color-text-primary:              /* Headings, important text */
--color-text-secondary:            /* Body text */
--color-text-muted:                /* Captions, meta info */
--color-text-disabled:             /* Disabled state */
--color-text-inverse:              /* Text on filled surfaces */
--color-text-link:                 /* Interactive text links */

/* === Accent === */
--color-accent-primary:            /* Primary interactive color */
--color-accent-primary-hover:
--color-accent-primary-subtle:     /* Low-emphasis accent (bg tint) */

/* === Status: Semantic === */
--color-status-success:            /* Approved, finalized, sufficient */
--color-status-success-subtle:     /* Low-emphasis success bg */
--color-status-warning:            /* Under review, needs attention */
--color-status-warning-subtle:
--color-status-danger:             /* Error, failed, rejected */
--color-status-danger-subtle:
--color-status-info:               /* Informational, in progress */
--color-status-info-subtle:
--color-status-neutral:            /* Draft, pending, not started */
--color-status-neutral-subtle:

/* === Score Attribution (platform-specific) === */
--color-score-objective:           /* Deterministic/system calculated */
--color-score-ai-suggested:        /* AI generated */
--color-score-human-approved:      /* Human confirmed (no change) */
--color-score-human-modified:      /* Human changed from AI suggestion */
--color-score-final:               /* Final institutional result */

/* === Evidence Provenance === */
--color-evidence-verified:         /* Verified institutional evidence */
--color-evidence-unverified:       /* Unverified evidence */
--color-evidence-self-reported:    /* Self-reported evidence */
```

### 6.3 Dark Theme Default Values

```css
:root[data-theme="dark"] {
  --color-canvas:                  #0a0d12;
  --color-surface-primary:         #121722;
  --color-surface-secondary:       #1a1f2e;
  --color-surface-elevated:        #1e2433;
  --color-surface-interactive:     #252b3b;
  --color-surface-selected:        rgba(59, 130, 246, 0.08);

  --color-border-default:          #2a3040;
  --color-border-subtle:           #1e2433;
  --color-border-strong:           #3a4050;
  --color-border-interactive:      #3a4050;
  --color-border-focus:            #3b82f6;

  --color-text-primary:            #e2e8f0;  /* slate-200 */
  --color-text-secondary:          #94a3b8;  /* slate-400 */
  --color-text-muted:              #64748b;  /* slate-500 */
  --color-text-disabled:           #475569;  /* slate-600 */
  --color-text-inverse:            #0f172a;
  --color-text-link:               #60a5fa;

  --color-accent-primary:          #3b82f6;  /* blue-500 */
  --color-accent-primary-hover:    #2563eb;  /* blue-600 */
  --color-accent-primary-subtle:   rgba(59, 130, 246, 0.10);

  --color-status-success:          #10b981;  /* emerald-500 */
  --color-status-success-subtle:   rgba(16, 185, 129, 0.10);
  --color-status-warning:          #f59e0b;  /* amber-500 */
  --color-status-warning-subtle:   rgba(245, 158, 11, 0.10);
  --color-status-danger:           #f43f5e;  /* rose-500 */
  --color-status-danger-subtle:    rgba(244, 63, 94, 0.10);
  --color-status-info:             #3b82f6;  /* blue-500 */
  --color-status-info-subtle:      rgba(59, 130, 246, 0.10);
  --color-status-neutral:          #64748b;  /* slate-500 */
  --color-status-neutral-subtle:   rgba(100, 116, 139, 0.10);

  --color-score-objective:         #10b981;  /* emerald */
  --color-score-ai-suggested:      #8b5cf6;  /* violet */
  --color-score-human-approved:    #3b82f6;  /* blue */
  --color-score-human-modified:    #f59e0b;  /* amber */
  --color-score-final:             #e2e8f0;  /* primary text */

  --color-evidence-verified:       #10b981;
  --color-evidence-unverified:     #64748b;
  --color-evidence-self-reported:  #f59e0b;
}
```

### 6.4 Light Theme Strategy

Light theme inverts canvas/surface luminance and adjusts contrast. Defined by overriding the same semantic tokens:

```css
:root[data-theme="light"] {
  --color-canvas:                  #f8fafc;
  --color-surface-primary:         #ffffff;
  --color-surface-secondary:       #f1f5f9;
  /* ... all tokens redefined for light context */
}
```

No component code changes required. Components reference only semantic tokens.

### 6.5 Institutional Branding Extension

Institutions can override `--color-accent-primary` and related accent tokens:

```css
:root[data-institution="desco"] {
  --color-accent-primary:          #0369a1;  /* DESCO brand blue */
}
```

---

## 7. Theme Strategy

### 7.1 Supported Modes

| Mode | Priority | Status |
|------|----------|--------|
| Dark | Primary | Design-time default |
| Light | Required | Achievable via token override |
| High contrast | Accessibility | WCAG AAA contrast ratios via token override |
| Institutional | Optional | Accent/branding customization via token override |
| Print | Required | Separate stylesheet; always light; no decorative surfaces |

### 7.2 Theme Selection

- System preference detection via `prefers-color-scheme`
- User preference override stored in local settings
- Institutional default set by admin configuration

---

## 8. Typography

### 8.1 Font Stack

```css
--font-family-sans:      'Inter', 'Noto Sans Bengali', system-ui, -apple-system, sans-serif;
--font-family-mono:      'JetBrains Mono', 'Fira Code', ui-monospace, monospace;
```

**Inter** for all interface text (excellent Latin readability, good Unicode coverage).
**Noto Sans Bengali** for Bangla script (ensures consistent rendering).

### 8.2 Type Scale

| Token | Size | Weight | Use |
|-------|------|--------|-----|
| `--text-display` | 28px / 1.75rem | 600 | Page titles |
| `--text-heading-lg` | 22px / 1.375rem | 600 | Section headings |
| `--text-heading` | 18px / 1.125rem | 600 | Card/panel headings |
| `--text-heading-sm` | 15px / 0.9375rem | 600 | Sub-headings |
| `--text-body` | 14px / 0.875rem | 400 | Body text, table cells |
| `--text-body-sm` | 13px / 0.8125rem | 400 | Secondary body, captions |
| `--text-caption` | 12px / 0.75rem | 400 | Timestamps, metadata |
| `--text-label` | 12px / 0.75rem | 500 | Form labels, badges |
| `--text-mono` | 13px / 0.8125rem | 400 | IDs, model versions, formulas |

### 8.3 Monospace Usage Rules

Use monospace (`--font-family-mono`) ONLY for:

- UUIDs and entity IDs
- Timestamps in audit logs
- AI model identifiers and versions (e.g., `gemini-2.5-pro`)
- Framework version codes (e.g., `DESCO_EMP_2027_v3`)
- Score formulas and calculation breakdowns
- API technical metadata

Do NOT use monospace for: headings, labels, names, descriptions, evidence text, pen pictures, or general UI text.

### 8.4 Bangla/English Considerations

- All text containers must accommodate ~30% wider Bangla text without overflow or truncation
- Use `word-break: break-word` for long Bangla compound words
- RTL is not required (Bangla is LTR) but bidi text mixing must render correctly
- Date formatting respects locale: `১৫ জানুয়ারী ২০২৭` or `15 January 2027`
- Numbers may display in Bengali digits (`৮৭`) or Western digits (`87`) per user preference
- All labels, status names, error messages, and headings must be translatable via an i18n system (React Intl or equivalent)

---

## 9. Spacing & Grid

### 9.1 Spacing Scale

Base unit: `4px`

| Token | Value | Common Use |
|-------|-------|------------|
| `--space-1` | 4px | Inline icon gap, tight padding |
| `--space-2` | 8px | Compact component padding |
| `--space-3` | 12px | Standard inner padding |
| `--space-4` | 16px | Standard component gap |
| `--space-5` | 20px | Section inner padding |
| `--space-6` | 24px | Section gap |
| `--space-8` | 32px | Major section separation |
| `--space-10` | 40px | Page-level spacing |
| `--space-12` | 48px | Large separation |

### 9.2 Grid System

```text
Desktop:  12-column grid, max-width 1440px, gutter 24px
Tablet:   8-column grid, gutter 16px
Mobile:   4-column grid, gutter 16px
```

### 9.3 Density Modes

| Mode | Row height | Padding | Use |
|------|-----------|---------|-----|
| Compact | 32px | 4px 8px | Reviewer workbench tables, framework parameter lists |
| Default | 40px | 8px 12px | Standard tables, form layouts |
| Comfortable | 48px | 12px 16px | Student mobile, card layouts |

Density mode is set per-view, not globally. The reviewer workbench uses Compact; student mobile uses Comfortable.

---

## 10. Component Hierarchy

### 10.1 Primitive Components

These are the atomic building blocks. They carry no domain semantics.

#### Interactive

| Component | Notes |
|-----------|-------|
| `Button` | Variants: `primary`, `secondary`, `ghost`, `danger`. Sizes: `sm`, `md`, `lg`. Loading state. |
| `IconButton` | Square button with icon only. Tooltip required for accessibility. |
| `Input` | Text input. Variants: default, error, disabled. Prefix/suffix slots. |
| `TextArea` | Multi-line input. Character count. Auto-resize option. |
| `Select` | Dropdown. Single and multi-select. Searchable for long lists. |
| `Checkbox` | Standard and indeterminate states. |
| `Radio` | Radio group with accessible fieldset. |
| `Switch` | Toggle for boolean settings. |

#### Display

| Component | Notes |
|-----------|-------|
| `Badge` | Small label. Variants follow status colors. |
| `StatusBadge` | Badge with icon + label for assessment states. Never color-only. |
| `Tooltip` | Triggered on hover/focus. Accessible via `aria-describedby`. |
| `Tag` | Removable tag for categories, source types. |

#### Layout

| Component | Notes |
|-----------|-------|
| `Tabs` | Horizontal tab bar with accessible `role="tablist"`. |
| `Table` / `DataGrid` | Sortable, filterable columns. Fixed header. Density-aware row height. |
| `Pagination` | Page numbers + per-page selector. |
| `Drawer` | Side panel for detail views. Does not obscure primary content unnecessarily. |
| `Modal` | Confirmation dialogs, destructive action confirmations. Trap focus. |
| `Card` | Simple bordered container. Use sparingly — not every piece of content needs a card. |

#### Feedback

| Component | Notes |
|-----------|-------|
| `Alert` | Inline alert with semantic variants: `info`, `success`, `warning`, `danger`. Icon + text + optional action. |
| `Toast` | Temporary notification. Auto-dismiss with configurable duration. Accessible announcements. |
| `Skeleton` | Loading placeholder matching content shape. |
| `Spinner` | Indeterminate loading for smaller areas. |
| `EmptyState` | Icon + heading + description + primary action. Used when a list or view has no data. |
| `ErrorState` | Icon + error description + recovery action. Used when data loading fails. |

#### Navigation

| Component | Notes |
|-----------|-------|
| `Breadcrumb` | Path-based navigation. Always visible on desktop. |
| `FilterBar` | Horizontal filter controls (status, domain, period, search). Collapsible on mobile. |
| `Search` | Search input with debounce. Context-aware placeholder. |
| `Timeline` | Vertical timeline for audit history and lifecycle events. |

---

### 10.2 Assessment-Specific Components

These encode domain semantics from [02-domain-model.md](file:///e:/AI Appraisal System/docs/02-domain-model.md) and [06-scoring-engine.md](file:///e:/AI Appraisal System/docs/06-scoring-engine.md).

#### `AssessmentStatusBadge`

Displays assessment lifecycle state from the domain model's `AssessmentStatus` enum.

```text
┌────────────────────────────┐
│  ● Evidence Collection     │  ← icon + label + background tint
└────────────────────────────┘
```

State-to-treatment mapping:

| State | Icon | Color Token | Label |
|-------|------|-------------|-------|
| `CREATED` | circle-outline | `neutral` | Created |
| `EVIDENCE_COLLECTION` | file-plus | `info` | Collecting Evidence |
| `EVALUATION_READY` | check-circle | `info` | Ready for Evaluation |
| `EVALUATING` | loader (animated) | `info` | AI Evaluating |
| `EVALUATED` | brain | `info` | Evaluated |
| `UNDER_REVIEW` | eye | `warning` | Under Review |
| `APPROVED` | check-double | `success` | Approved |
| `FINALIZED` | lock | `success` | Finalized |
| `RETURNED` | arrow-back | `warning` | Returned |
| `CANCELLED` | x-circle | `danger` | Cancelled |

Never rely on color alone. Every state has icon + label.

#### `ParameterScoreCard`

Displays a single parameter's score with full attribution.

```text
┌─────────────────────────────────────────────────────┐
│  Leadership                              QUALITATIVE│
│  ─────────────────────────────────────────────────  │
│                                                     │
│  Score Basis          Value         Status           │
│  ──────────────────── ──────────── ─────────────── │
│  AI Suggested         82.00        ·               │
│  Reviewer Approved    78.00        Modified         │
│  Final Score          78.00                         │
│                                                     │
│  Rating: Exceeds Expectations (Level 4)             │
│  Weight: 8.00%   Weighted: 6.24                     │
│                                                     │
│  Evidence: 5 items · High strength                  │
│  Confidence: 0.88                                   │
│                                                     │
│  Override Reason                                     │
│  "Evidence limited to single project scope"          │
│                                                     │
│  [View Evidence]  [View Rubric]  [View Justification]│
└─────────────────────────────────────────────────────┘
```

Key design rules:
- AI Suggested and Human Approved scores are ALWAYS both visible (when both exist)
- Score basis uses `--color-score-*` tokens to distinguish attribution
- Scoring strategy type shown as subtle label (`OBJECTIVE`, `QUALITATIVE`, `HYBRID`, `DERIVED`, `COMPOSITE`)
- Override reason is visible inline, not behind a click

#### `EvidenceItem`

Displays a single evidence item with provenance.

```text
┌─────────────────────────────────────────────────────┐
│  ┌──────────┐                                       │
│  │SUPERVISOR│  Verified  ·  15 Jun 2027             │
│  └──────────┘                                       │
│                                                     │
│  Rahim led an 8-person team to deliver the billing  │
│  modernization project on schedule. The project     │
│  reduced processing time by 15%.                    │
│                                                     │
│  Mapped to: Leadership (Primary)                    │
│             Productivity (Supporting)               │
└─────────────────────────────────────────────────────┘
```

- Source type badge uses distinct visual treatment per type
- Verification status shown explicitly: `Verified` / `Unverified` / `Rejected`
- `SELF_REPORTED` evidence uses `--color-evidence-self-reported` tint — visually distinct but NOT visually "lesser" or struck-through
- Evidence date always visible
- Parameter mappings shown with relationship type

#### `EvidenceSourceBadge`

Consistent badge for evidence source types:

| Source Type | Treatment |
|-------------|-----------|
| `SYSTEM_GENERATED` | Solid, `verified` color |
| `OFFICIAL_RECORD` | Solid, `verified` color |
| `SUPERVISOR` | Solid, `verified` color |
| `TEACHER` | Solid, `verified` color |
| `PEER` | Outline, default |
| `SELF_REPORTED` | Outline, `self-reported` color |
| `EXTERNAL` | Outline, default |
| `DERIVED` | Dashed outline, `objective` color |

#### `EvidenceSufficiencyIndicator`

Shows evidence state for a parameter per the domain model's `EvidenceSufficiency` enum.

| State | Display |
|-------|---------|
| `SUFFICIENT` | `✓ Sufficient` · "5 items from 3 sources" |
| `INSUFFICIENT` | `⚠ Insufficient` · "1 item · 2 required" |
| `MISSING_REQUIRED` | `✕ Missing Required` · "No supervisor evidence" |
| `NOT_APPLICABLE` | `— Not Applicable` |

Never displayed as just a color. Always icon + label + explanation.

#### `RubricViewer`

Displays a parameter's rubric levels alongside the current score position.

```text
  Level 5 │ Outstanding                    │  85–100
  Level 4 │ Exceeds Expectations      ◄──  │  70–85     ← current: 78.00
  Level 3 │ Meets Expectations              │  50–70
  Level 2 │ Needs Improvement               │  30–50
  Level 1 │ Unsatisfactory                  │   0–30
```

- Shows level label, definition (expandable), and canonical score range
- Current score position indicated with marker
- Rubric levels are NOT rendered as a progress bar — they are discrete descriptive levels with defined ranges

#### `AIJustification`

Displays AI evaluation justification with evidence grounding.

```text
┌─────────────────────────────────────────────────────┐
│  AI Evaluation — Leadership                         │
│  Model: gemini-2.5-pro · Prompt v3                  │
│                                                     │
│  Suggested Score: 82.00 / 100  (Level 4)            │
│  Confidence: 0.88 · Evidence Strength: High         │
│                                                     │
│  Justification                                      │
│  "Strong evidence of project leadership through     │
│   the billing modernization delivery [EV-001].      │
│   15% processing time reduction demonstrates        │
│   measurable impact [EV-001]. Limited evidence       │
│   of mentoring junior staff."                        │
│                                                     │
│  Strengths                                          │
│  · Led 8-person team to on-schedule delivery        │
│  · 15% processing time reduction                    │
│                                                     │
│  Development Areas                                   │
│  · Limited evidence of mentoring junior staff       │
│                                                     │
│  Referenced Evidence: EV-001, EV-003, EV-007        │
└─────────────────────────────────────────────────────┘
```

- AI model metadata always visible (small, muted — not prominent)
- Evidence references are clickable links to the actual evidence items
- Clearly labeled as "AI Evaluation" — never presented as the final institutional decision

#### `ReviewDecision`

Displays a reviewer's decision for a parameter.

```text
┌─────────────────────────────────────────────────────┐
│  Review — Leadership                                │
│  Reviewer: Karim Rahman · Senior Reviewer           │
│  Reviewed: 21 Jan 2027 14:30                        │
│                                                     │
│  Action: Modified                                   │
│  AI Score:  82.00  →  Approved Score: 78.00         │
│                                                     │
│  Reason                                             │
│  "Evidence of leadership is strong but limited to   │
│   a single project. Adjusted to account for scope." │
└─────────────────────────────────────────────────────┘
```

#### `OverridePanel`

Used for overall score override (exceptional, `SENIOR_REVIEWER` only).

- Prominent warning banner: "Overall score override bypasses parameter-level scoring"
- Requires text justification (minimum character count enforced)
- Shows before/after score comparison
- Confirmation dialog before submission

#### `AuditEntry`

Used in the audit timeline.

```text
│
├── 21 Jan 2027 14:30
│   Karim Rahman · Reviewer
│   Modified parameter score: Leadership
│   82.00 → 78.00
│   "Evidence limited to single project scope"
│
├── 20 Jan 2027 14:03
│   System
│   Scoring engine calculated assessment result
│   Overall: 72.35 · Rating: Exceeds Expectations
│
├── 20 Jan 2027 14:02
│   System · gemini-2.5-pro
│   AI evaluation completed
│   25/25 parameters evaluated
│
```

#### `SubjectPerformanceCard` (Student)

```text
┌─────────────────────────────────────────────┐
│  Mathematics                    86.35%      │
│  ────────────────────────────────────────── │
│  Final Exam      87%   (40%)                │
│  Midterm         82%   (20%)                │
│  Quizzes         91%   (15%)                │
│  Classwork       85%   (10%)                │
│  Assignments     88%   (15%)                │
│                                             │
│  Trend: ▲ Improving  (+5.2 from previous)   │
└─────────────────────────────────────────────┘
```

- Shows component breakdown with weights
- Trend direction with icon + label, not just color
- Growth value shown as numeric fact

#### `GrowthSummary` (Student)

```text
┌─────────────────────────────────────────────┐
│  Learning Growth                            │
│                                             │
│  Previous Period    77.3                    │
│  Current Period     82.5                    │
│  Change            +5.2  (+6.7%)           │
│  Category          Moderate Improvement     │
│                                             │
│  Subject Growth                             │
│  Mathematics    +5.2   ▲                   │
│  Physics        +1.1   ─                   │
│  English        +3.8   ▲                   │
│  ICT            +0.5   ─                   │
│  Bangla         -2.1   ▼                   │
└─────────────────────────────────────────────┘
```

#### `TrendSummary` (Student)

Simple directional indicators with text labels.

| Direction | Icon | Label |
|-----------|------|-------|
| `IMPROVING` | ▲ | Improving |
| `STABLE` | ─ | Stable |
| `DECLINING` | ▼ | Declining |

Always icon + label. Never color-only.

#### `ScoringBasis`

Explains how a score was calculated.

```text
┌─────────────────────────────────────────────┐
│  Scoring Basis — Attendance                 │
│  Strategy: OBJECTIVE                        │
│                                             │
│  Raw Value:        94.00%                   │
│  Canonical Score:  94.0000 / 100            │
│  Rubric Level:     5 (Outstanding)          │
│  Weight:           5.00%                    │
│  Weighted Score:   4.7000                   │
│                                             │
│  Calculation: deterministic                 │
│  Confidence:  1.00                          │
└─────────────────────────────────────────────┘
```

---

## 11. Assessment-Specific Components: Score Presentation Rules

### 11.1 The Five Score Concepts

The UI must NEVER conflate these:

| Concept | Source | Token | Visual Treatment |
|---------|--------|-------|------------------|
| **Raw Value / Objective Metric** | Application calculation | `--color-score-objective` | Labeled "Calculated" or "System" |
| **AI Suggested Score** | AI evaluation | `--color-score-ai-suggested` | Labeled "AI Suggested" |
| **Human Approved Score** (unchanged) | Reviewer approval | `--color-score-human-approved` | Labeled "Approved" |
| **Human Modified Score** | Reviewer override | `--color-score-human-modified` | Labeled "Modified" + reason visible |
| **Final Score** | Deterministic (= human if exists, else AI) | `--color-score-final` | Labeled "Final" — prominently displayed |

### 11.2 Score Display Rules

1. When AI has suggested but human has not yet reviewed: show AI Suggested with clear "Pending Review" status.
2. When human approved without change: show Final = AI Suggested, status "Approved".
3. When human modified: show BOTH AI Suggested and Human Modified side by side. Override reason visible inline.
4. For OBJECTIVE parameters: show "Calculated" (no AI involved).
5. For DERIVED/COMPOSITE parameters: show "Calculated" with component breakdown available.
6. The Final Score is always the most prominent number. Attribution appears beneath or beside it.

---

## 12. Status / State System

### 12.1 Assessment Status Treatments

| Status | Visual | Icon | Badge Color |
|--------|--------|------|-------------|
| `CREATED` | Outlined badge | `circle-dashed` | `neutral` |
| `EVIDENCE_COLLECTION` | Filled badge | `file-text` | `info` |
| `EVALUATION_READY` | Filled badge | `clipboard-check` | `info` |
| `EVALUATING` | Animated badge | `cpu` | `info` |
| `EVALUATED` | Filled badge | `chart-bar` | `info` |
| `UNDER_REVIEW` | Filled badge | `user-check` | `warning` |
| `APPROVED` | Filled badge | `check-circle` | `success` |
| `FINALIZED` | Filled badge | `lock` | `success` |
| `RETURNED` | Outlined badge | `arrow-left` | `warning` |
| `CANCELLED` | Outlined badge | `x-circle` | `danger` |

### 12.2 Evidence Sufficiency States

| State | Visual | Label |
|-------|--------|-------|
| `SUFFICIENT` | ✓ icon + success tint | "Sufficient — 5 items from 3 sources" |
| `INSUFFICIENT` | ⚠ icon + warning tint | "Insufficient — requires 2 sources, found 1" |
| `MISSING_REQUIRED` | ✕ icon + danger tint | "Missing — supervisor evidence required" |
| `NOT_APPLICABLE` | — icon + neutral | "Not applicable to this assessment" |

### 12.3 Evidence Strength

| Level | Display |
|-------|---------|
| `HIGH` | Text label "High" + contextual summary: "3 verified sources covering 9 months" |
| `MEDIUM` | "Medium" + "2 sources, limited corroboration" |
| `LOW` | "Low" + "1 unverified source" |
| `INSUFFICIENT` | "Insufficient" + specific deficiency |

No gauges. No progress bars. No radial meters. Text label + factual basis.

### 12.4 AI Confidence Display

AI confidence is displayed as a factual value, NOT as a pseudo-scientific gauge.

```text
Confidence: 0.88

Note: AI-reported confidence. Final evaluation supported by
3 verified evidence items.
```

When evidence quality independently contradicts AI confidence (e.g., AI says 0.95 but only 1 self-reported evidence item exists), the UI shows both:

```text
AI Confidence: 0.95
Evidence Basis: 1 self-reported source (Low strength)
```

This prevents the interface from implying false authority from AI confidence values.

---

## 13. Evidence Presentation

### 13.1 Evidence List View

```text
┌──────────────────────────────────────────────────────────────┐
│  Evidence for Assessment: Rahim Ahmed · Annual 2027          │
│                                                              │
│  Filter: [All Sources ▾] [All Params ▾] [Verified ▾] 🔍    │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ SUPERVISOR · Verified · 15 Jun 2027                   │  │
│  │ Led 8-person team to deliver billing project...       │  │
│  │ → Leadership (Primary) · Productivity (Supporting)    │  │
│  └────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ OFFICIAL_RECORD · Verified · System Import            │  │
│  │ Attendance: 94% (221/235 working days)                │  │
│  │ → Attendance (Primary)                                │  │
│  └────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ SELF_REPORTED · Unverified · 10 Dec 2027              │  │
│  │ I contributed significantly to department training...  │  │
│  │ → Professional Development (Primary)                  │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  Sufficiency: 23/25 parameters sufficient · 2 insufficient  │
└──────────────────────────────────────────────────────────────┘
```

### 13.2 Evidence-to-Parameter View

On the reviewer workbench, evidence is shown in context of the parameter being reviewed — not as a separate page the reviewer must navigate to.

### 13.3 Self-Reported Evidence Treatment

Self-reported evidence is:
- Visually distinct (subtle `--color-evidence-self-reported` tint on the source badge)
- Labeled explicitly as `SELF_REPORTED`
- NOT struck-through, greyed-out, or visually diminished
- Contextually noted in the AI justification as self-reported
- Available for reviewer inspection with full provenance

---

## 14. Objective vs AI vs Human Result Presentation

### 14.1 Layout Pattern — Parameter Result

```text
┌─────────────────────────────────────────────────────────────┐
│  Parameter Name                        Strategy Badge       │
│  ─────────────────────────────────────────────────────────  │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ FINAL SCORE                                         │   │
│  │ 78.00 / 100                                         │   │
│  │ Exceeds Expectations (Level 4)                      │   │
│  │ Weight: 8.00% · Weighted: 6.24                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Score Attribution                                          │
│  ┌────────────┬────────────────────┬──────────────────┐    │
│  │ Source     │ Score              │ Status            │    │
│  ├────────────┼────────────────────┼──────────────────┤    │
│  │ AI Sugg.  │ 82.00              │ ·                 │    │
│  │ Reviewer  │ 78.00              │ Modified          │    │
│  └────────────┴────────────────────┴──────────────────┘    │
│                                                             │
│  Override: "Evidence limited to single project scope"       │
│                                                             │
│  Evidence (5 items) · Strength: High                        │
│  Confidence: 0.88                                           │
│                                                             │
│  [Expand: Evidence · Rubric · AI Justification · History]   │
└─────────────────────────────────────────────────────────────┘
```

### 14.2 For Objective Parameters (no AI)

```text
│  Score Attribution                                          │
│  ┌────────────┬────────────────────┬──────────────────┐    │
│  │ Source     │ Score              │ Status            │    │
│  ├────────────┼────────────────────┼──────────────────┤    │
│  │ Calculated │ 94.00              │ Deterministic     │    │
│  └────────────┴────────────────────┴──────────────────┘    │
```

No AI row. No review row (unless the reviewer overrode an objective score, which is unusual but permitted).

---

## 15. Reviewer Workbench Architecture

This is the single most important interface in the platform.

### 15.1 Layout

```text
┌──────────────────────────────────────────────────────────────────┐
│  Assessment: Rahim Ahmed · Annual 2027 · ● Under Review         │
│  Framework: DESCO Employee v3 · Overall: 72.35                  │
├────────────────────────┬─────────────────────────────────────────┤
│                        │                                         │
│  Parameter List        │  Parameter Detail                       │
│  (scrollable)          │                                         │
│                        │  ┌──────────────────────────────────┐   │
│  ● Leadership    82/78│  │  Leadership                      │   │
│  ● Productivity  88   │  │  ─────────────────────────────── │   │
│  ○ Attendance    94   │  │                                   │   │
│  ○ Teamwork      71   │  │  Rubric (Level 4 highlighted)    │   │
│  ○ Communication 65   │  │                                   │   │
│  ⚠ Innovation    --   │  │  Evidence (inline, expandable)    │   │
│  ...                   │  │                                   │   │
│                        │  │  AI Justification                │   │
│  Legend:               │  │                                   │   │
│  ● Reviewed            │  │  Score Attribution               │   │
│  ○ Pending             │  │  AI: 82.00                       │   │
│  ⚠ Insufficient        │  │                                   │   │
│                        │  │  ┌──────────────────────────┐    │   │
│                        │  │  │ Your Decision            │    │   │
│                        │  │  │ ○ Approve (82.00)        │    │   │
│                        │  │  │ ○ Modify  [____]         │    │   │
│                        │  │  │ ○ Return for evidence    │    │   │
│                        │  │  │                          │    │   │
│                        │  │  │ Reason (if modifying):   │    │   │
│                        │  │  │ [________________________]│    │   │
│                        │  │  │ [Submit Decision]        │    │   │
│                        │  │  └──────────────────────────┘    │   │
│                        │  │                                   │   │
│                        │  └──────────────────────────────────┘   │
│                        │                                         │
│  Review Progress       │                                         │
│  15/25 reviewed        │                                         │
│  [Complete Review]     │                                         │
├────────────────────────┴─────────────────────────────────────────┤
│  Keyboard: ↑↓ navigate · Enter select · A approve · M modify    │
└──────────────────────────────────────────────────────────────────┘
```

### 15.2 Design Rules

- Split-panel: parameter list on left, detail on right. Both scroll independently.
- Parameter list shows: name, AI score, approved score (if reviewed), review status icon
- Insufficient-evidence parameters visually flagged with `⚠` and sortable to top
- Low-confidence parameters visually indicated
- The reviewer can see evidence, rubric, and AI justification WITHOUT leaving the current view
- Review decision is in-context: Approve / Modify / Return radio group
- Modify requires entering a score and mandatory reason text
- Keyboard shortcuts for power users (navigate with arrows, approve with `A`)
- Review progress indicator: "15/25 reviewed"
- "Complete Review" button only enabled when all parameters have decisions

### 15.3 Tablet Adaptation

On tablet, the split panel becomes a stacked view:
- Parameter list as a compact horizontal scroll or collapsed panel
- Tapping a parameter expands its full detail
- One parameter visible at a time in detail

### 15.4 Mobile

Reviewer workbench is NOT designed for phone use. Mobile displays a read-only assessment summary. Review actions require tablet or desktop.

---

## 16. Framework Builder Architecture

### 16.1 Layout

```text
┌──────────────────────────────────────────────────────────────┐
│  Framework: DESCO Employee · Version 3 (Draft)               │
├──────────────────────────────────────────────────────────────┤
│  [Parameters]  [Weights]  [Rating Scale]  [Scoring Rules]    │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  Parameters (25)                               [+ Add]       │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ #  Code          Name              Strategy    Weight  │  │
│  ├────────────────────────────────────────────────────────┤  │
│  │ 1  LEADERSHIP    Leadership        QUALITATIVE  8.00% │  │
│  │ 2  PRODUCTIVITY  Productivity      HYBRID      10.00% │  │
│  │ 3  ATTENDANCE    Attendance        OBJECTIVE    5.00% │  │
│  │ ...                                                    │  │
│  │                                     Total:    100.00%  │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  Clicking a parameter opens inline editor:                   │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Parameter: Leadership                                  │  │
│  │                                                        │  │
│  │ Code:        [LEADERSHIP]                              │  │
│  │ Name:        [Leadership]                              │  │
│  │ Category:    [BEHAVIORAL ▾]                            │  │
│  │ Strategy:    [QUALITATIVE_RUBRIC ▾]                    │  │
│  │                                                        │  │
│  │ Rubric Levels:                                         │  │
│  │ Lvl  Label                 Range       Definition      │  │
│  │  1   [Unsatisfactory]      [0-30]      [Unable to...] │  │
│  │  2   [Needs Improvement]   [30-50]     [Shows lim...] │  │
│  │  3   [Meets Expectations]  [50-70]     [Effectivel...]│  │
│  │  4   [Exceeds Expectations][70-85]     [Frequently...]│  │
│  │  5   [Outstanding]         [85-100]    [Exceptional...]│  │
│  │                                                        │  │
│  │ Evidence Rules:                                        │  │
│  │ Eligible: [✓SUPERVISOR ✓PEER ✓OFFICIAL ✓SELF_REPORTED]│  │
│  │ Required: [✓SUPERVISOR]                                │  │
│  │ Min items: [2]                                         │  │
│  │                                                        │  │
│  │ Missing Data Policy: [FLAG_FOR_REVIEW ▾]               │  │
│  │                                                        │  │
│  │ [Save Parameter]  [Cancel]                             │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  Version Actions:                                            │
│  [Save Draft] [Validate] [Activate Version]                  │
│                                                              │
│  Activate Warning:                                           │
│  "Activating this version will freeze all parameters,        │
│   rubrics, and weights. This cannot be undone."              │
└──────────────────────────────────────────────────────────────┘
```

### 16.2 Validation

Before activation, the builder validates:
- All weights sum to 1.0000
- Every parameter has at least one rubric level
- Rubric level ranges are contiguous and cover 0–100
- Scoring strategy config is valid for the chosen strategy
- Rating scale covers the full 0–100 range

Validation errors shown inline with clear descriptions.

---

## 17. Employee Assessment UX

### 17.1 Assessment Overview

```text
┌──────────────────────────────────────────────────────────────┐
│  Rahim Ahmed                                                 │
│  Engineering Department · Employee ID: EMP-001               │
│  Assessment Period: Jan 2027 – Dec 2027                      │
│  Framework: DESCO Employee v3                                │
│  Status: ● Approved                                          │
│                                                              │
│  Overall Score: 72.35 / 100                                  │
│  Rating: Exceeds Expectations (Level 4)                      │
│                                                              │
│  ─────────────────────────────────────────────────────────── │
│                                                              │
│  Parameter Results (25)                                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Parameter       Final   Rating  Evidence  Review     │   │
│  │ Leadership      78.00   4       High      Modified   │   │
│  │ Productivity    88.00   5       High      Approved   │   │
│  │ Attendance      94.00   5       High      (Calc'd)   │   │
│  │ Teamwork        71.00   4       Medium    Approved   │   │
│  │ Communication   65.00   3       Medium    Approved   │   │
│  │ Innovation      —       N/A     Insuff.   Excluded   │   │
│  │ ...                                                  │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  [View Pen Picture]  [View Full Review History]              │
│  [Export PDF]                                                │
└──────────────────────────────────────────────────────────────┘
```

---

## 18. Teacher Assessment UX

Structurally identical to Employee assessment but with:
- Different parameter names (Teaching Effectiveness, Classroom Management, etc.)
- Different evidence source emphasis (classroom observations, student outcomes)
- Different pen picture template (pedagogical focus)
- Vocabulary: "Teaching performance" not "Job performance"

The same components (`ParameterScoreCard`, `ReviewDecision`, etc.) are reused with domain-appropriate labels.

---

## 19. Student Academic UX

### 19.1 Desktop: Student Academic Overview

```text
┌──────────────────────────────────────────────────────────────┐
│  Fatima Khan · Class 10-A                                    │
│  Academic Year: 2027 · Assessment: Annual                    │
│                                                              │
│  Overall Academic Score: 82.50 / 100                         │
│  Growth: +5.2 (+6.7%) · Moderate Improvement                 │
│  Attendance: 94.5%                                           │
│                                                              │
│  ─── Subject Performance ─────────────────────────────────  │
│                                                              │
│  Subject       Score   Trend    Growth   Exam   Cont. Asmt  │
│  Mathematics   86.35   ▲ +5.2   Improving  87%    85.75%    │
│  English       91.20   ▲ +3.8   Improving  93%    89.50%    │
│  Physics       78.40   ─ +1.1   Stable     75%    80.00%    │
│  ICT           94.00   ─ +0.5   Stable     95%    93.00%    │
│  Bangla        72.80   ▼ -2.1   Declining  70%    75.00%    │
│                                                              │
│  ─── Strengths ───────────────────────────────────────────  │
│  ICT · English · Mathematics                                │
│                                                              │
│  ─── Areas for Development ───────────────────────────────  │
│  Bangla · Physics                                            │
│                                                              │
│  [View Subject Details]  [View Assessment]  [View Pen Picture]│
└──────────────────────────────────────────────────────────────┘
```

### 19.2 Mobile: Student Academic View

```text
┌────────────────────────────────┐
│  Fatima Khan                   │
│  Class 10-A · Annual 2027      │
│                                │
│  Overall: 82.50                │
│  Growth: +5.2 ▲                │
│  Attendance: 94.5%             │
│                                │
│  ─── My Subjects ────────     │
│                                │
│  ┌──────────────────────────┐ │
│  │ Mathematics        86.35 │ │
│  │ ▲ Improving · +5.2      │ │
│  └──────────────────────────┘ │
│  ┌──────────────────────────┐ │
│  │ English            91.20 │ │
│  │ ▲ Improving · +3.8      │ │
│  └──────────────────────────┘ │
│  ┌──────────────────────────┐ │
│  │ Physics            78.40 │ │
│  │ ─ Stable · +1.1         │ │
│  └──────────────────────────┘ │
│  ┌──────────────────────────┐ │
│  │ Bangla             72.80 │ │
│  │ ▼ Declining · -2.1      │ │
│  └──────────────────────────┘ │
│                                │
│  Tap a subject for details     │
│                                │
├────────────────────────────────┤
│ Overview  Subjects  Growth     │
│ Attend.   Profile              │
└────────────────────────────────┘
```

### 19.3 Student Language Rules

| System Term | Student-Facing Term |
|-------------|---------------------|
| Weakness | Area for development |
| Low performance | Growth opportunity |
| Insufficient evidence | More information needed |
| Declining | Needs attention |
| Failed | Below target |
| Override | Adjusted |
| Evidence strength: LOW | Limited basis |

---

## 20. Responsive Strategy

### 20.1 Breakpoints

| Breakpoint | Width | Target |
|------------|-------|--------|
| `mobile` | < 640px | Phone |
| `tablet` | 640–1024px | Tablet |
| `desktop` | 1024–1440px | Desktop |
| `wide` | > 1440px | Wide desktop |

### 20.2 Adaptation Rules

| View | Desktop | Tablet | Mobile |
|------|---------|--------|--------|
| Reviewer Workbench | Split panel | Stacked/collapsible | Read-only summary |
| Framework Builder | Full inline editing | Full inline editing | Not available (admin tool) |
| Assessment Overview | Table + detail | Table (compact) | Card stack |
| Student Academic | Table layout | Table (compact) | Card list |
| Evidence List | Full content | Full content | Truncated, expandable |
| Audit Timeline | Full detail | Full detail | Condensed |
| Parameter table | All columns | Essential columns | Card per parameter |

### 20.3 Table Responsive Strategy

Dense tables (parameter results, evidence, audit) do NOT become horizontally scrolling on mobile.

Instead:
- Table transforms to a vertical card list
- Each row becomes a card with key-value pairs
- Sorting/filtering preserved via filter bar
- Secondary columns hidden; expandable for full detail

---

## 21. Accessibility

### 21.1 Target: WCAG 2.2 AA

| Requirement | Implementation |
|-------------|----------------|
| Contrast | 4.5:1 minimum for normal text; 3:1 for large text. Token system ensures this per theme. |
| Focus visibility | 2px solid focus ring using `--color-border-focus`. Visible in both dark and light themes. |
| Keyboard navigation | All interactive elements reachable via Tab. Reviewer workbench supports arrow-key navigation. |
| Screen reader | Semantic HTML. ARIA labels on icon-only buttons. Status changes announced via `aria-live`. |
| Motion | `prefers-reduced-motion` disables all transition animations. Loading spinners become static indicators. |
| Touch targets | Minimum 44×44px on mobile. Comfortable density mode enforces this. |
| Color independence | No information conveyed by color alone. All statuses use icon + label + color. |
| Form errors | Associated via `aria-describedby`. Error messages describe the problem and the fix. |
| Tables | `scope` attributes on headers. `aria-sort` on sortable columns. |

### 21.2 High Contrast Mode

Achieved via token override:
- All borders become `--color-border-strong`
- Text contrast increased to AAA ratios
- Badge backgrounds solidified (no subtle tints)
- Focus ring widened to 3px

---

## 22. Bangla / English Support

### 22.1 Architecture

- All user-facing strings externalized via i18n key-value system (React Intl / `useTranslations` pattern)
- Locale context provided at application root
- Language selection: user preference → institution default → browser locale
- Fallback: English if Bangla translation missing

### 22.2 Layout Considerations

- All containers use flexible widths; no fixed-width labels
- Tables allow columns to expand for longer Bangla text
- Buttons use `min-width` not `width`
- Card headings truncate with ellipsis + tooltip for very long text
- Form labels positioned above inputs (not inline beside) — avoids width conflicts

### 22.3 Number and Date Formatting

| Context | English | Bangla |
|---------|---------|--------|
| Score | 82.50 | ৮২.৫০ (optional) |
| Date | 15 Jan 2027 | ১৫ জানুয়ারী ২০২৭ |
| Percentage | 94.5% | ৯৪.৫% |
| Currency (if ever needed) | BDT 5,000 | ৫,০০০ টাকা |

User preference controls whether Bengali digits are used. Default: Western digits for scores (institutional standard), Bengali digits available.

---

## 23. Data Visualization

### 23.1 Appropriate Use

| Chart Type | When | Example |
|------------|------|---------|
| Line chart | Time-series with ≥ 3 data points | Student subject performance across terms |
| Horizontal bar | Comparing values across categories | Parameter scores for an assessment |
| Small multiples | Subject-by-subject trend comparison | Student growth per subject |

### 23.2 Inappropriate Use (DO NOT)

| Anti-Pattern | Why |
|--------------|-----|
| Pie/donut for score breakdown | Weight distribution is better shown as a table |
| Radar/spider chart | Misleading area encoding for ordinal data |
| Gauge/speedometer | Decorative; a number communicates the value better |
| Progress bar for score | Score is not "progress toward completion" |
| 3D charts | Distort perception |
| Axis manipulation | Y-axis must start at 0 for bar charts; scale must be honest |

### 23.3 Chart Design Rules

- Muted palette from design tokens (no neon accents)
- Axis labels in `--font-family-sans`, not monospace
- Grid lines: `--color-border-subtle`
- Data points labeled with values (no hover-only data)
- Text alternative for every chart (table fallback or `aria-label`)
- `prefers-reduced-motion`: no animated chart transitions

---

## 24. Audit / History UX

### 24.1 Assessment Audit Trail

Accessible from the "History" tab on any assessment detail.

Uses the `Timeline` component with `AuditEntry` items:

```text
│
├── 21 Jan 2027 15:00  ·  System
│   Assessment FINALIZED
│   Overall Score: 72.35 · Rating: Exceeds Expectations
│
├── 21 Jan 2027 14:45  ·  System · gemini-2.5-pro
│   Pen picture generated (version 1)
│
├── 21 Jan 2027 14:30  ·  Karim Rahman · Reviewer
│   Review completed: 23 approved, 2 modified, 0 returned
│   Leadership: 82.00 → 78.00 "Evidence limited to single project"
│   Communication: 68.00 → 65.00 "Primarily internal evidence"
│
├── 20 Jan 2027 14:03  ·  System
│   Assessment result calculated (version 1)
│   Overall: 74.10
│
├── 20 Jan 2027 14:00  ·  System · gemini-2.5-pro · Prompt v3
│   AI evaluation completed (25/25 parameters)
│
├── 15 Jan 2027 09:00  ·  Admin User
│   12 evidence items submitted (batch import)
│
├── 10 Jan 2027 08:00  ·  Admin User
│   Assessment created for Rahim Ahmed
│   Framework: DESCO Employee v3 · Period: Jan–Dec 2027
│
```

### 24.2 Override Log

A filtered view showing only human modifications:

- Who modified, when, which parameter
- Original AI score → Approved score
- Justification text
- Whether overall score changed as a result

### 24.3 Framework Version History

Shows version-over-version changes to parameters, rubrics, and weights.

---

## 25. Print / Report Strategy

### 25.1 Principles

- Print output is ALWAYS light background, dark text
- No dark-theme colors carry to print
- Assessment summaries must be printable as institutional records
- Print stylesheet removes navigation, interactive controls, and expandable sections (expand all before print)

### 25.2 Print Stylesheet

```css
@media print {
  /* Reset to light theme */
  :root {
    --color-canvas: #ffffff;
    --color-surface-primary: #ffffff;
    --color-text-primary: #1a1a1a;
    --color-text-secondary: #4a4a4a;
    --color-border-default: #cccccc;
  }

  /* Hide interactive elements */
  nav, .sidebar, .filter-bar, button:not(.print-visible),
  .toast, .modal-overlay { display: none; }

  /* Expand all collapsible sections */
  [data-collapsed] { max-height: none !important; }

  /* Page breaks */
  .assessment-result { page-break-inside: avoid; }
  .parameter-result { page-break-inside: avoid; }

  /* Ensure tables don't overflow */
  table { font-size: 11px; }
}
```

### 25.3 PDF Export

PDF generation uses server-side rendering (or headless browser) with the print stylesheet. Output includes:
- Assessment header (subject, period, framework, overall result)
- Parameter results table
- Pen picture
- Reviewer information
- Institutional header/footer

---

## 26. Error / Loading / Empty States

### 26.1 Loading States

| Context | Treatment |
|---------|-----------|
| Page load | Full-page skeleton matching expected layout |
| Table data | Skeleton rows (5 rows of grey bars) |
| AI evaluation in progress | Status badge with animated icon + real-time parameter progress |
| Single component | Inline spinner, minimum 200ms display to prevent flash |

### 26.2 Empty States

| Context | Message | Action |
|---------|---------|--------|
| No assessments | "No assessments found for the selected filters" | "Create Assessment" button |
| No evidence | "No evidence has been submitted for this assessment" | "Submit Evidence" button |
| No frameworks | "No assessment frameworks configured" | "Create Framework" button |
| No results | "This assessment has not been evaluated yet" | "Trigger Evaluation" button (if eligible status) |
| No review history | "This assessment has not been reviewed yet" | Status-appropriate guidance |

### 26.3 Error States

| Context | Treatment |
|---------|-----------|
| API failure | Error alert with specific message + retry button |
| Validation error | Inline field-level errors + summary at form top |
| AI evaluation failure | Per-parameter failure indication + "Retry" or "Review manually" |
| Permission denied | Clear message about required role |
| Not found | 404 with breadcrumb back to parent list |

Error messages follow the pattern: **What happened** + **Why** (if known) + **What to do**.

---

## 27. Frontend Implementation Guidance

### 27.1 Technology Direction

```text
React (latest)
Tailwind CSS (v4+)
React Router (routing)
React Query / TanStack Query (server state)
React Hook Form + Zod (forms + validation)
React Intl (i18n)
Recharts or Nivo (data viz, if needed)
```

### 27.2 Component Library Structure

```text
src/
├── components/
│   ├── primitives/          ← Design system primitives
│   │   ├── Button/
│   │   ├── Input/
│   │   ├── Table/
│   │   ├── Badge/
│   │   ├── StatusBadge/
│   │   ├── Modal/
│   │   ├── Drawer/
│   │   ├── Tabs/
│   │   ├── Timeline/
│   │   ├── Alert/
│   │   ├── Toast/
│   │   ├── Skeleton/
│   │   ├── EmptyState/
│   │   ├── ErrorState/
│   │   └── ...
│   │
│   ├── assessment/          ← Domain-specific components
│   │   ├── AssessmentStatusBadge/
│   │   ├── ParameterScoreCard/
│   │   ├── RubricViewer/
│   │   ├── ScoringBasis/
│   │   ├── ReviewDecision/
│   │   ├── OverridePanel/
│   │   └── ...
│   │
│   ├── evidence/
│   │   ├── EvidenceItem/
│   │   ├── EvidenceSourceBadge/
│   │   ├── EvidenceSufficiencyIndicator/
│   │   └── ...
│   │
│   ├── ai/
│   │   ├── AIJustification/
│   │   └── ...
│   │
│   ├── academic/            ← Student-specific
│   │   ├── SubjectPerformanceCard/
│   │   ├── GrowthSummary/
│   │   ├── TrendSummary/
│   │   └── ...
│   │
│   ├── audit/
│   │   ├── AuditEntry/
│   │   └── AuditTimeline/
│   │
│   └── layout/
│       ├── Sidebar/
│       ├── TopBar/
│       ├── Breadcrumb/
│       └── PageLayout/
│
├── pages/                   ← Route-level page components
│   ├── dashboard/
│   ├── frameworks/
│   ├── assessments/
│   ├── academic/
│   ├── audit/
│   └── settings/
│
├── hooks/                   ← Custom hooks
│   ├── useAssessment.ts
│   ├── useFramework.ts
│   ├── useEvidence.ts
│   └── ...
│
├── api/                     ← API client (matches 04-api-spec.md)
│   ├── frameworks.ts
│   ├── assessments.ts
│   ├── evidence.ts
│   ├── evaluations.ts
│   ├── reviews.ts
│   └── ...
│
├── i18n/                    ← Translations
│   ├── en.json
│   └── bn.json
│
├── tokens/                  ← Design tokens
│   ├── colors.css
│   ├── typography.css
│   ├── spacing.css
│   └── themes/
│       ├── dark.css
│       └── light.css
│
└── types/                   ← TypeScript types (from API contracts)
    ├── framework.ts
    ├── assessment.ts
    ├── evidence.ts
    ├── scoring.ts
    └── ...
```

### 27.3 API Client Alignment

The frontend API client must align 1:1 with [04-api-spec.md](file:///e:/AI Appraisal System/docs/04-api-spec.md). TypeScript types are derived from the REST response contracts. Use OpenAPI codegen if the backend produces an OpenAPI spec.

### 27.4 State Management

- Server state: React Query (caching, refetching, optimistic updates)
- Local UI state: React component state + context
- No global state management library needed initially
- Form state: React Hook Form with Zod schemas matching API validation rules

---

## 28. Anti-Patterns / Design DO NOTs

| Anti-Pattern | Why Prohibited |
|--------------|----------------|
| Progress bars for scores | Scores are not progress toward a goal. 78/100 is a measurement, not 78% completion. |
| Star ratings | Ordinal scale misrepresentation. Use rubric level label + canonical score. |
| Skill bars | Meaningless without defined scale. Use the actual score and rubric level. |
| Neon/glow effects | Undermines institutional credibility. |
| Gradient background blobs | Decorative clutter that competes with data. |
| Glassmorphism on data surfaces | Reduces readability of assessment data. |
| Blueprint/wireframe aesthetics | Design affectation that adds no information. |
| `FIG.01` decorations | Non-functional ornament. |
| Crosshairs / crop marks | Non-functional ornament. |
| Cards-inside-cards | Increases nesting without adding hierarchy. Use flat sections. |
| AI animation on every page | AI is a tool, not a feature to celebrate with animation. |
| Auto-playing charts | Decorative motion that distracts from reading data. |
| Fake analytics dashboards | Every chart must represent real assessment data. |
| Color-only status indication | Fails accessibility. Always use icon + label + color. |
| Horizontal scrolling tables on mobile | Transform to card layout instead. |
| Truncated evidence without expansion | Evidence must be fully readable. |
| Hiding override reasons | Override justification is an institutional record; it must be visible. |
| Monospace for non-technical text | Reserve for IDs, timestamps, model versions only. |
| Identical Employee/Student layouts | Students are not employees. Academic data requires fundamentally different presentation. |

---

## 29. Consistency Verification

| # | Question | Answer |
|---|----------|--------|
| 1 | Does the design clearly distinguish objective, AI-suggested, human-approved, and final results? | **Yes** — Five distinct score concepts with dedicated color tokens, labels, and attribution layout (Section 11). |
| 2 | Can a reviewer inspect the evidence behind a result? | **Yes** — Evidence shown inline in the reviewer workbench, expandable per parameter, with provenance and mapping type (Section 15). |
| 3 | Is Employee UX different from Student UX where appropriate? | **Yes** — Employee uses high-density reviewer workbench; Student uses mobile-first academic overview with subject-level cards and constructive language (Sections 17, 19). |
| 4 | Is the Student experience mobile-first? | **Yes** — Student mobile layout defined as primary; bottom tab navigation; card-based subject list; comfortable density (Section 19.2). |
| 5 | Can the system support Bangla and English? | **Yes** — i18n architecture, Noto Sans Bengali font, flexible-width containers, locale-aware formatting, Bengali digit support (Section 22). |
| 6 | Can the theme later support institutional branding/light mode? | **Yes** — All visuals driven by semantic tokens; light theme and institutional accent overrides documented (Sections 6, 7). |
| 7 | Are insufficient and contradictory evidence clearly represented? | **Yes** — `EvidenceSufficiencyIndicator` with icon + label + factual explanation; contradictory evidence flagged for review (Section 12.2). |
| 8 | Does the UX preserve the audit/history requirements from the architecture? | **Yes** — Timeline component, audit trail tab on every assessment, override log, framework version history (Section 24). Aligns with `audit` module in [01-architecture.md](file:///e:/AI Appraisal System/docs/01-architecture.md). |
| 9 | Does the design system avoid generic AI-dashboard visual tropes? | **Yes** — Explicit anti-pattern list prohibits gauges, progress bars, neon effects, gradient blobs, fake analytics, and decorative AI animations (Section 28). |
| 10 | Can Gemini later implement the UI without needing to invent a new design system? | **Yes** — Complete token system, component hierarchy, page archetypes, responsive rules, and implementation guidance provided (Sections 6–27). |
