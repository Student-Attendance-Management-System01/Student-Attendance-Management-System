# Security regression record — STP §5.1

Outcome of each security validation activity in the Software Test Plan
(§5.1), as of sprint 2. “Done” means a check was actually executed and the
result is recorded here; anything not executed says so.

Environment for the checks below: frontend from branch
`sprint2/issue-17-tests-security-docs`, Vitest suite (API mocked) and a manual
pass in Chromium against a local stub API. **No check below was run against
the real Spring Boot backend**, because the authentication API is not yet on
`main` (KNOWN_ISSUES KI-1).

| # | STP §5.1 activity | SRS | Check performed | Outcome |
|---|---|---|---|---|
| 1 | Authentication and secret handling | SEC-002, SEC-007 | UI password rules reject the STP samples `abc123`, `password`, `Passw0rd` and accept `Pass@w0rd1` (`validation.test.js`). Frontend code searched: the token is never read, stored or logged (`localStorage`, `sessionStorage`, `document.cookie` unused). | **Pass (UI)**. Backend hash cost / log search: *not run*. |
| 2 | Brute-force and lockout | F-002, SEC-006 | UI shows one generic message for 401 and 423, and a specific “too many attempts” message for 429 (`auth-routes.test.jsx`). | **Pass (UI)**. Lockout after 5 failures and 429 threshold against the API: *not run*. |
| 3 | Access control (vertical / horizontal, IDOR) | SEC-001 | Role × route matrix: 7 disallowed role/route pairs show *Access denied*; each role’s navigation lists only permitted screens; student screens only request the signed-in student’s own ID. | **Pass (UI)**. UI guards are convenience only; the API matrix (TC-SEC-01) is *blocked* (KI-6). |
| 4 | Transport security | SEC-003 | — | *Not run* — no TLS deployment yet. nginx config has no TLS server block (KI-7). |
| 5 | Injection, XSS, CSRF | SEC-004, SEC-010 | XSS: all user-supplied text is rendered through React (escaped); no `dangerouslySetInnerHTML`. CSRF: XSRF cookie is fetched before login and sent as `X-XSRF-TOKEN` by axios on every state-changing call. CSV: UI rejects files > 2 MB before upload. | **Pass (code review + UI test)**. ZAP active scan and payload fuzzing: *not run*. |
| 6 | Session management | SEC-005, F-004 | Logout calls `/auth/logout` and clears the UI; any 401 ends the UI session with “session expired”; 15 minutes without input signs the user out and revokes the token via `/auth/logout`; client timer also honours `expiresInSeconds` from login. | **Pass (UI)**. Cookie flags, tampered-JWT replay and post-logout replay against the API: *not run*. |
| 7 | Information leakage and privacy | SEC-008, SEC-009 | UI never shows raw exception text (`messageFrom` falls back to a generic message; tested). Google Fonts removed — fonts are bundled, so the browser contacts no third party. | **Pass (UI)**. Backend 423 still reveals that an account exists (KI-3). |
| 8 | Audit trail integrity | SEC-007, F-070 | — | *Blocked* — no audit API on `main` (KI-2). |
| 9 | Dependency and configuration check | SEC-004 | `npm audit --omit=dev` run on the frontend lock file. | See KI-8 for the result at time of writing. OWASP Dependency-Check for the backend: *not run*. |

## Next steps to close this record

1. Once the auth/correction/report controllers are merged, add Spring Boot
   integration tests for the TC-SEC-01 role × endpoint matrix and TC-AUTH-02/03/05.
2. Run OWASP ZAP (authenticated contexts for each role) against a Docker
   Compose deployment and attach the report.
3. Inspect `Set-Cookie` headers and replay a logged-out / tampered token.
4. Add OWASP Dependency-Check to Backend CI.
