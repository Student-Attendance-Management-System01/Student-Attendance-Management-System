# Requirements traceability (sprint 2)

Links SRS requirements to STP test cases and to the automated tests that now
exist. Status reflects what has actually been run, not what is planned.

**Status key**
- **UI ✔** – automated frontend test passes (API mocked; proves UI behaviour only)
- **Unit ✔** – backend unit test exists in `backend/src/test` and runs in CI
- **Not run** – no automated or recorded manual execution yet
- **Blocked** – cannot be run until a dependency lands (see `KNOWN_ISSUES.md`)

## Functional

| SRS ID | Requirement (short) | STP case | Automated test(s) | Status |
|---|---|---|---|---|
| SAMS-F-001 | Login with e-mail and password | TC-AUTH-01, TC-AUTH-02 | `auth-routes` › login: blocks empty submit; same generic error for wrong password/locked; signs in and returns to requested page | UI ✔ · API Blocked (KI-1) |
| SAMS-F-002 | Lock account after 5 failures | TC-AUTH-03 | `auth-routes` › generic error for 423 | UI ✔ (message only) · API Not run |
| SAMS-F-003 | Single-use password-reset link | TC-AUTH-04 | — (reset screens built in #14) | Not run |
| SAMS-F-004 | Logout and 15-min inactivity timeout | TC-AUTH-05 | `auth-routes` › logs out through the API; 401 → expired session; signs out after 15 minutes without activity | UI ✔ · token replay Not run |
| SAMS-F-010 | Create / update / deactivate users | TC-USR-01 | `admin` › creates a user…; edits a user through PUT…; does not offer to deactivate the signed-in admin | UI ✔ |
| SAMS-F-011 | Reject duplicate ID / e-mail | TC-USR-01 | `admin` › shows a duplicate-ID conflict from the backend | UI ✔ · API Not run |
| SAMS-F-012 | Create course-section, assign faculty | TC-USR-02 | `admin` › requires a faculty member before creating a section; `validation` › course section form | UI ✔ |
| SAMS-F-013 | Enrol singly or by CSV (≤ 1,000 rows) | TC-USR-03 | `admin` › duplicate-enrolment error; reports rejected CSV rows | UI ✔ · big/oversized CSV Not run |
| SAMS-F-020 | Create session; reject overlap | TC-ATT-01 | `attendance` › rejects end time before start time | UI ✔ · overlap (API) Not run |
| SAMS-F-021 | Roster shows all students UNMARKED | TC-ATT-02 | `attendance` › keeps submit disabled until every student is marked… | UI ✔ |
| SAMS-F-022 | Mark P/A/OD; submit only when complete | TC-ATT-02, TC-ATT-03 | `attendance` › same as above (posts full roster with P and OD) | UI ✔ |
| SAMS-F-023 | Only assigned faculty can mark | TC-ATT-04 | — | Blocked (KI-6) |
| SAMS-F-024 | One record per student per session | TC-ATT-05 | `attendance` › shows the API error and no success message when submission is rejected | UI ✔ · API Not run |
| SAMS-F-025 | 24 h edit window, edit with reason | TC-ATT-06 | — (edit UI present, reason ≥ 10 chars enforced in UI) | Not run |
| SAMS-F-030 | Student views own held/attended/% | TC-VIEW-01 | `attendance` › shows percentages from the backend…; only requests own attendance | UI ✔ |
| SAMS-F-031 | Faculty views assigned sections only | TC-VIEW-02 | — | Blocked (KI-6) |
| SAMS-F-032 | Red highlight below threshold | TC-VIEW-01 | `attendance` › …flags the course below threshold | UI ✔ |
| SAMS-F-040 | Correction request within 7 days | TC-COR-01, TC-COR-02 | `attendance` › submits a correction request for a session | UI ✔ · 7-day rule (API) Blocked (KI-1) |
| SAMS-F-041 | Faculty approves / rejects | TC-COR-01 | `attendance` › approves a pending request | UI ✔ · e-mail Not run |
| SAMS-F-050 | Student-wise report | TC-REP-01 | `reports` › totals match the rows returned by the backend; date-range validation | UI ✔ · API Blocked (KI-1) |
| SAMS-F-051 | Defaulter report | TC-REP-01 | `reports` › sends the chosen threshold for the defaulter report | UI ✔ |
| SAMS-F-052 | Export PDF and CSV | TC-REP-02 | `reports` › shows a readable error when an export fails | UI ✔ (error path) · file content Not run |
| SAMS-F-053/054 | Scheduled weekly / monthly reports | TC-REP-03 | — (backend only) | Not run |
| SAMS-F-060/061 | Low-attendance e-mail, retry | TC-NOT-01/02 | — (backend only) | Not run |
| SAMS-F-070/071 | Audit log records, filter, read-only | TC-AUD-01 | — (audit screen built in #16) | Blocked (KI-2) |

## Non-functional

| SRS ID | Requirement (short) | STP case | Evidence | Status |
|---|---|---|---|---|
| SAMS-NF-001/002/003/004 | Response times, capacity | TC-PERF-01/02/03 | — | Not run |
| SAMS-NF-005/006 | Availability, backup/restore | TC-AVL-01, TC-REL-01 | — | Not run |
| SAMS-NF-007 | Mark 60 students ≤ 2 min | TC-USA-01 | “Mark all present” + per-row select in roster | Not run (needs volunteers) |
| SAMS-NF-008 | Responsive 360–1920 px | TC-USA-02 | Manual Chromium check at 390 / 900 / 1440 px (see `README.md`) | Partially done (Chromium only) |
| SAMS-NF-009 | Service-layer coverage ≥ 70 % | TC-MNT-01 | JaCoCo gate not yet configured | Not run |
| SAMS-NF-011 | Keyboard operable, WCAG AA | TC-USA-02 | Labels on all form controls; Escape closes dialogs; axe/Lighthouse not run | Not run |
| SAMS-NF-012 | Percentage to 2 decimals | TC-VIEW-01 | `AttendanceCalculatorTest` (backend); UI shows backend value with `toFixed(2)` | Unit ✔ · UI ✔ |

## Security

See [`SECURITY_REGRESSION.md`](SECURITY_REGRESSION.md) for the activity-level record.

| SRS ID | STP case | Automated test(s) | Status |
|---|---|---|---|
| SAMS-SEC-001 RBAC / object-level | TC-SEC-01, TC-ATT-04, TC-VIEW-02 | `auth-routes` › role × route matrix and navigation; `attendance` › own-ID only | UI ✔ · API Blocked (KI-6) |
| SAMS-SEC-002 Password policy, bcrypt | TC-SEC-02 | `validation` › password policy (STP sample passwords); `PasswordPolicyTest` (backend) | UI ✔ · Unit ✔ · hash inspection Not run |
| SAMS-SEC-003 TLS | TC-SEC-03 | — | Not run (no deployed server) |
| SAMS-SEC-004 Injection / XSS / CSRF | TC-SEC-04 | CSRF: `auth-routes` checks the XSRF cookie is fetched before login; React escapes all rendered text (no `dangerouslySetInnerHTML` in the codebase) | Partially done · ZAP Not run |
| SAMS-SEC-005 Cookie flags | TC-SEC-05 | Frontend never reads or stores the token (no `localStorage`/`document.cookie` use) | Partially done · header inspection Not run |
| SAMS-SEC-006 Rate limiting | TC-SEC-06 | `auth-routes` › explains rate limiting | UI ✔ · API Not run |
| SAMS-SEC-007 Append-only audit | TC-AUD-01 | — | Blocked (KI-2) |
| SAMS-SEC-008 Data minimisation | TC-SEC-07 | Fonts bundled locally: the browser makes no third-party requests | Partially done |
| SAMS-SEC-009 Generic errors | TC-AUTH-02, TC-SEC-07 | `auth-routes` › generic login error; `validation` › error-message hygiene | UI ✔ · KI-3 open |
| SAMS-SEC-010 CSV limits / injection | TC-USR-03, TC-REP-02 | UI blocks CSV > 2 MB before upload; backend `ReportService.safe()` prefixes `= + - @` | Partially done |
