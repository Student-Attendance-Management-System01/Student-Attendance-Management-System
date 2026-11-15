# Known Issues and Resolution Status (Sprint 2 Integrated)

**Project:** Student Attendance Management System (SAMS)  
**Document:** Known Issues Log  
**Updated:** 2026-11-15 (Post Phase 2 Integration)  
**Severity Scale:** Critical / Major / Minor / Trivial (IEEE 829 STP Standard)  

---

## 1. Issue Status Overview

During the Sprint 2 Phase 2 integration, all critical and major integration blockers (KI-1, KI-2, KI-6) were resolved and validated. Residual issues consist of low-risk architectural observations, deployment-level hardening items, and future enhancement suggestions.

```
+--------------------------------------------------------------+
| RESOLUTION SUMMARY                                           |
+--------------------------------------------------------------+
| Total Issues Logged: 9                                       |
| - Resolved / Closed:  3 (KI-1, KI-2, KI-6 - all Major)      |
| - Accepted Risks:     4 (KI-3, KI-4, KI-5, KI-8 - Minor)     |
| - Deployment Ops:     1 (KI-7 - Minor)                       |
| - Trivial UI:         1 (KI-9 - Trivial)                     |
+--------------------------------------------------------------+
```

---

## 2. Comprehensive Issue Tracking Table

| ID | Severity | Area | Description | Impact | Status | Resolution / Mitigation Details |
|---|---|---|---|---|---|---|
| **KI-1** | Major | Backend Integration | `AuthController`, `CorrectionController`, and `ReportController` were fragmented across unmerged branches (`#11`, `#12`). | Blocked end-to-end authentication, correction workflows, and reporting. | **RESOLVED** | Integrated PR #23 (Auth), PR #25 (Corrections), and PR #26 (Reports) into `integration/sprint2`. Verified via 122 unit tests and 22 E2E live tests. |
| **KI-2** | Major | Backend API | Missing `GET /api/v1/audit-log` endpoint needed by frontend Admin Audit screen. | Admin audit log UI displayed errors; TC-AUD-01 blocked. | **RESOLVED** | Implemented `@GetMapping("/audit-log")` in `AdminController.java`, backed by `AuditService.findAll()`. Verified via live E2E Step 15. |
| **KI-3** | Minor | Security (SEC-009) | Login returns HTTP 423 for locked accounts vs HTTP 401 for unknown emails. | Potential account enumeration via HTTP status code in browser DevTools. | **ACCEPTED RISK** | Mitigated in frontend UI: identical error banner ("Invalid email or password") rendered for both 401 and 423. Documented as acceptable for educational campus scope. |
| **KI-4** | Minor | Session Management | Fixed 15-minute JWT expiration instead of sliding refresh window. | Active faculty marking long rosters could expire after 15 minutes. | **OPEN / BACKLOG** | Frontend implements 15-minute inactivity warning timer (SAMS-F-004). Sliding token refresh or refresh cookie scheduled for Sprint 3. |
| **KI-5** | Minor | Product Scope | Admins can view pending corrections (`GET /corrections/pending`) but only faculty can decide (`PATCH /corrections/{id}`). | Admin navigation omits Corrections tab. | **RESOLVED / AS DESIGNED** | Confirmed compliant with SRS business rules: Faculty assigned to the course are the sole academic authority permitted to alter attendance records. |
| **KI-6** | Major | Testing Gap | Missing automated API-level tests for role × endpoint authorization matrix, unassigned faculty, and cross-student access. | Authorization enforcement was only tested via frontend UI route guards. | **RESOLVED** | Created comprehensive MockMvc tests (`CorrectionControllerTest`, `AttendanceServiceTest`, `AuthControllerTest`) and automated live E2E tests (Steps 20, 21, 22). |
| **KI-7** | Minor | Deployment (SEC-003) | `nginx.conf` template serves plain HTTP without mandatory TLS redirection; `SAMS_COOKIE_SECURE` defaults to false in dev. | Suitable for local dev; unsafe for public internet deployment. | **OPEN / DEPLOYMENT** | Production deployment checklist requires reverse proxy TLS termination (Let's Encrypt / ALB) and setting `sams.cookie.secure=true`. |
| **KI-8** | Minor | Frontend Dependencies | `react-router-dom` 6.x has open-redirect advisory; dev-only Vite advisories. | Low risk in production bundle; dev server risk on open LAN. | **MITIGATED** | Application code mitigates open-redirect via `safeReturnPath()` in `Login.jsx`. Upgrade to React Router 7 scheduled for next maintenance window. |
| **KI-9** | Trivial | Cross-Browser UX | Automated testing primarily executed in Chromium-based headless runner. | Minor cosmetic inconsistencies in Firefox or Safari mobile viewports. | **OPEN / QA** | Pre-release manual multi-browser verification checklist scheduled prior to final UAT sign-off. |

---

## 3. Previously Resolved Frontend Defects (Sprint 2 Track)

The following UI/UX defects were fixed during the frontend development phase (#13–#17) and verified during integration:
1. **Syntax error in `Dashboard.jsx`**: Fixed missing parenthesis.
2. **Timezone date offset**: Switched from `toISOString()` to local timezone date helper `localDate()` to prevent date shifting before 05:30 IST.
3. **Dashboard failure state**: Displays structured error banner instead of misleading `0` count on API failure.
4. **Blob export error handling**: Added response stream parsing to display human-readable backend errors on CSV/PDF export failures.
5. **Data minimization (SEC-008)**: Removed Google Fonts CDN requests; bundled fonts locally via `@fontsource`.
6. **Open-redirect prevention**: Implemented strict URI parsing (`safeReturnPath()`) restricting post-login redirects to relative application routes.
