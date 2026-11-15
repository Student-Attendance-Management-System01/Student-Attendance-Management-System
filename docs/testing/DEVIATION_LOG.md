# SAMS Integration Deviation Log

**Project:** Student Attendance Management System (SAMS)  
**Sprint:** Sprint 2 Phase 2 Integration  
**Date:** 2026-11-15  
**Document Purpose:** Records intentional architectural, environmental, and implementation deviations from the original Sprint 1 design and Sprint 2 PR branches.

---

## 1. Deviation Summary Table

| Deviation ID | Category | Original Specification / State | Integrated Implementation | Rationale | Impact & Risk Assessment |
|---|---|---|---|---|---|
| **DEV-01** | Infrastructure / Database | Database on `localhost:3306`, Backend on `8080` | Isolated MySQL 8.0 on `3307`, Backend on `8082`, Vite proxy to `8082` | Avoid colliding with user's active local `sams` database and running Sprint 1 processes | **Zero risk.** Original database backed up and untouched; complete test isolation. |
| **DEV-02** | Security / CSRF | Static CSRF cookie expectation in manual test scripts | Dynamic CSRF rotation on `/login`; client reads new `XSRF-TOKEN` cookie | Spring Security 6 best practice rotates CSRF tokens across auth boundaries | **Enhanced security.** Prevents session fixation and CSRF token fixation attacks. |
| **DEV-03** | Backend JPA Mapping | `byte[] attachmentContent` mapped without column definition | `@Column(name = "attachment_content", columnDefinition = "LONGBLOB")` | Hibernate 6.5 schema validation rejected `byte[]` against Flyway's `LONGBLOB` | **Defect resolved.** Allows application startup under strict `ddl-auto=validate`. |
| **DEV-04** | API Endpoint / Contract | Missing `GET /api/v1/audit-log` (documented as KI-2 in PR #33) | Added `@GetMapping("/audit-log")` in `AdminController.java` | Fulfills frontend contract and enables Admin audit trail viewing | **Functional improvement.** Resolves KI-2; allows full TC-AUD-01 validation. |
| **DEV-05** | Integration Strategy | Naive git merge of PR #25 and PR #26 | Selective functional integration preserving PR #23 Auth components | PR #25 and #26 carried obsolete versions of `AuthController` and `AuthService` | **Regression prevention.** Preserved JWT cookie security and rate limiting from PR #23. |
| **DEV-06** | CI / CD Pipeline | Disjoint `main.yml` and `frontend-ci.yml` | Unified `SAMS Integrated CI` (`.github/workflows/main.yml`) | Ensures backend, frontend, JaCoCo, and artifact retention run on all PRs/pushes to `main` | **Operational reliability.** Complete CI coverage with zero hardcoded credentials. |

---

## 2. Detailed Technical Descriptions

### DEV-01: Isolated Database Instance and Environment Isolation
- **Background:** The workstation had an active Sprint 1 backend on port 8080 and MySQL instance on port 3306 containing pre-existing student data.
- **Action:**
  1. Created a distinct mysqld datadir at `/tmp/sams_sprint2_data` with socket `/tmp/sams_sprint2.sock`.
  2. Initialized database `sams_integration` listening on port `3307`.
  3. Configured `sams-sprint2-integration/.env` with `SERVER_PORT=8082`, `SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/sams_integration`.
  4. Configured Vite dev server with proxy target `http://localhost:8082`.
- **Outcome:** Total isolation between Sprint 1 demo workspace and Sprint 2 integration workspace.

### DEV-02: Spring Security 6 CSRF Token Rotation
- **Background:** Automated scripts attempting to reuse the initial CSRF token post-login received `403 Forbidden: Invalid CSRF Token`.
- **Action:** Spring Security `CsrfFilter` rotates the token upon authentication transition. Automated integration test clients (`e2e_integration_test.py`) capture the post-login `XSRF-TOKEN` cookie and update the `X-XSRF-TOKEN` header for subsequent requests.
- **Outcome:** Adheres strictly to OWASP and Spring Security specifications.

### DEV-03: `NotificationOutbox` BLOB Column Definition
- **Background:** Flyway migration `V1__initial_schema.sql` creates table `notification_outbox` with `attachment_content LONGBLOB`. When Spring Boot runs with `spring.jpa.hibernate.ddl-auto=validate`, Hibernate expects `TINYBLOB` for standard `byte[]` fields unless explicitly overridden, crashing startup.
- **Action:** Explicitly declared `@Column(name = "attachment_content", columnDefinition = "LONGBLOB")` on `NotificationOutbox.attachmentContent`.
- **Outcome:** Clean Flyway and Hibernate startup validation.

### DEV-04: Implementation of `/api/v1/audit-log`
- **Background:** PR #33 implemented the frontend Audit Log screen and documented Known Issue KI-2 ("No AuditController or GET /api/v1/audit-log on any branch").
- **Action:** Added endpoint to `AdminController`:
  ```java
  @GetMapping("/audit-log")
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ResponseEntity<List<AuditLogResponse>> getAuditLogs() {
      return ResponseEntity.ok(auditService.findAll().stream().map(AuditLogResponse::fromEntity).toList());
  }
  ```
- **Outcome:** Closed Known Issue KI-2; enabled complete verification of TC-AUD-01.

### DEV-05: Selective Merge of PR #25 and PR #26
- **Background:** PR #25 (Corrections) and PR #26 (Reports) branched off older Sprint 1 commits and contained obsolete, conflicting versions of authentication code. Blind cherry-picking or git merging would have wiped out PR #23 security fixes.
- **Action:** Selectively incorporated only the new domain controllers, services, schedulers, and tests (`CorrectionController`, `CorrectionService`, `ReportController`, `ReportService`, `ReportScheduler`) while preserving PR #23's `AuthController`, `AuthService`, and security infrastructure.
- **Outcome:** Clean integration without security regressions.

### DEV-06: Unified GitHub Actions CI Workflow
- **Background:** Two workflow files existed with differing triggers and missing artifact capture.
- **Action:** Merged all CI tasks into `.github/workflows/main.yml` with two dedicated jobs:
  - `backend`: Java 17 setup, `mvn clean verify`, Surefire XML upload, JaCoCo coverage upload.
  - `frontend`: Node 20 setup, `npm ci`, `npm test`, `npm run build`, dist upload.
- **Outcome:** Unified pipeline running on pushes and pull requests to `main`.
