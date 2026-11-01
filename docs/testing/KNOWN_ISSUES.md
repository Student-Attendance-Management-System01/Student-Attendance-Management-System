# Known issues and limitations (sprint 2)

Found while building and testing the frontend (#13–#17). Severity uses the
STP scale (Critical / Major / Minor / Trivial). None of these are claimed fixed
unless marked so.

| ID | Severity | Area | Issue | Impact | Suggested action / owner |
|---|---|---|---|---|---|
| KI-1 | Major | Backend / integration | `AuthController`, `CorrectionController` and `ReportController` are not on `main` (they exist only on the unmerged `sprint1/issue-11-…` and `sprint1/issue-12-…` branches). | The frontend cannot sign in against `main`; login, corrections and reports cannot be tested end-to-end. | Merge the sprint-1 #11/#12 work (backend owners), then run the journeys in `README.md` against the real API. |
| KI-2 | Major | Backend | No `AuditController` (`GET /api/v1/audit-log`) on any branch, although `SecurityConfiguration` already protects that path. | The admin Audit log screen shows an error; TC-AUD-01 / SAMS-F-071 blocked. | Add the audit-log endpoint (backend). |
| KI-3 | Minor | Backend security (SEC-009) | Login returns **423** for a locked account but **401** for an unknown e-mail. The UI shows the same message for both, but the status code in DevTools still reveals that the account exists. | Account enumeration via status code. | Return 401 for locked accounts too (keep the lock server-side), or document as accepted risk. |
| KI-4 | Minor | Backend / UX | JWT lifetime is a fixed 15 min (`sams.jwt.lifetime-minutes`), not sliding. An active user is signed out 15 min after login even while working. The UI adds a 15-min *inactivity* sign-out as SRS F-004 describes. | Faculty marking a long roster may be signed out mid-task. | Add token refresh on activity, or a longer absolute lifetime plus the inactivity timeout. Decide with the team. |
| KI-5 | Minor | Product decision | Admins can call `GET /corrections/pending` but only faculty can decide (`PATCH /corrections/{id}` is FACULTY-only), so the admin nav has no Corrections entry. | None if intended. | Confirm against SRS. |
| KI-6 | Major | Testing gap | No API-level integration tests for the role × endpoint matrix (TC-SEC-01), unassigned faculty (TC-ATT-04) or cross-student access (TC-VIEW-02). Frontend tests only prove the UI hides these paths. | Backend authorisation is unverified by automation. | Add Spring Boot `@SpringBootTest` + MockMvc tests once KI-1 is merged (QA lead + backend owners). |
| KI-7 | Minor | Deployment (SEC-003) | `frontend/nginx.conf` serves HTTP only; there is no TLS server block or HTTP→HTTPS redirect, and `SAMS_COOKIE_SECURE` defaults to false. | Fine for local dev; not for a real deployment. | Add TLS termination before any non-local deployment. |
| KI-8 | Minor | Dependencies (SEC-004) | `npm audit` (1 Nov 2026): **production** — `react-router-dom` 6.x has two moderate advisories (backslash open-redirect in `<Link>`/`useNavigate`; SSR-only constructor injection), fixed only in 7.x. The open-redirect path is mitigated by `safeReturnPath()` in `Login.jsx` (tested). **Dev-only** — `vite` 5 / `vitest` 2 / `esbuild` have high/critical advisories affecting the dev server and test runner; they are not shipped in the production bundle. | Low for production; dev server exposed on the LAN by `npm run dev` (`--host 0.0.0.0`). | Plan an upgrade to React Router 7 and current Vite/Vitest in a later sprint; run `npm run dev` on trusted networks only. |
| KI-9 | Trivial | Frontend | Responsive check done in Chromium only; Firefox / Edge and axe/Lighthouse audits (TC-USA-02) not yet run. | — | Run before UAT. |

## Fixed during this sprint

| Was | Fix | Where |
|---|---|---|
| `Dashboard.jsx` in the source bundle had a missing `)` and would not compile. | Fixed while porting. | #16 |
| Dates were taken from `toISOString()`, giving yesterday’s date before 05:30 IST. | `localDate()` helper used for all default dates. | #16 |
| Dashboard showed `0` when a request failed. | Shows `—` and an error banner. | #16 |
| Report export errors (Blob bodies) showed a generic message. | Blob error bodies are parsed. | #16 |
| Styles loaded fonts from Google Fonts (third-party request, SEC-008). | Fonts bundled via `@fontsource`. | #13 |
| Login could redirect to any `state.from` value. | `safeReturnPath()` allows only internal paths. | #17 |
