# Testing guide

How SAMS is tested, how to run the suites, and where the results are recorded.
The plan itself is the Software Test Plan in `Part-1 Deliverables/` (STP);
this folder records what has actually been implemented and run.

| Document | What it records |
|---|---|
| [`TRACEABILITY.md`](TRACEABILITY.md) | SRS requirement → STP test case → automated test → current status |
| [`SECURITY_REGRESSION.md`](SECURITY_REGRESSION.md) | Outcome of each STP §5.1 security validation activity |
| [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md) | Open defects, gaps and limitations found while testing |

## Running the suites

| Suite | Command | Needs | CI workflow |
|---|---|---|---|
| Backend unit + repository | `cd backend && mvn verify` | JDK 17, Maven (H2 in-memory, no MySQL) | `Backend CI` |
| Frontend | `cd frontend && npm ci && npm test` | Node 20 | `Frontend CI` |

## Frontend suite

The frontend tests render the real route tree (`AppRoutes`) inside the real
`AuthProvider`, with the axios client replaced by `axios-mock-adapter`. Each
test declares the API responses it needs, so the suite exercises routing,
role guards, client-side validation, and how screens react to API success,
validation errors, conflicts, 401/403/429 and 5xx — with no backend running.

| File | Journeys covered |
|---|---|
| `src/__tests__/auth-routes.test.jsx` | Login validation, generic login errors (401 = 423), rate-limit message, return-to-requested-page, signed-out redirect, role × route access matrix, role navigation, logout, 401 → session expired, 15-minute inactivity sign-out |
| `src/__tests__/admin.test.jsx` | Create user (client validation, trimmed payload, 409 duplicate), edit user, self-deactivation hidden, role filter, create section requires faculty, roster view, duplicate enrolment, CSV import rejection report |
| `src/__tests__/attendance.test.jsx` | Faculty: submit blocked until all marked, full roster posted, 409 rejected submission, session time check. Student: percentages and threshold highlight, own-ID only, correction request. Faculty: approve correction |
| `src/__tests__/reports.test.jsx` | Report totals equal backend rows, defaulter threshold parameter, date-range validation, readable export error, dashboard failure shows “—” not 0 |
| `src/__tests__/validation.test.js` | Password policy (TC-SEC-02 sample passwords), login/user/section form rules, error-message hygiene, post-login redirect hardening, local-date helper |

Latest run (sprint 2, branch `sprint2/issue-17-tests-security-docs`):
**5 files, 61 tests, all passing** (`npm test`, Node 22, Vitest 2.1.9).

### What these tests do not prove

They mock the API, so they show the UI sends the right requests and handles
each response correctly. They do **not** show that the backend enforces the
same rules. Backend enforcement (TC-SEC-01 access matrix, TC-ATT-04,
TC-VIEW-02, TC-AUTH-03, TC-SEC-05/06) needs API-level integration tests
against a running backend — see `KNOWN_ISSUES.md` (KI-1, KI-6).

## Manual UI check

Each screen was also checked in Chromium (Playwright) against a local stub
API, signed in as each role, at 1440 px, 900 px and 390 px widths: login
errors, session-expired notice, access-denied page, user/course management,
roster marking and submission, student attendance on mobile, correction
modal, reports and audit log. No console errors apart from the stub's
deliberate 409/401 responses.

## Adding a test

1. Put it in `frontend/src/__tests__/<area>.test.jsx`.
2. Use `renderApp(path, { signedInAs: 'ADMIN' | 'FACULTY' | 'STUDENT' | null, setup: mock => … })`
   from `src/test/utils.jsx`; register only the endpoints the screen calls.
3. Name the `describe` block after the SRS requirement / STP case it covers,
   and add a row to `TRACEABILITY.md`.
