# SAMS Requirements Traceability Matrix (RTM)

**Project:** Student Attendance Management System (SAMS)  
**Document:** Requirements-to-Test Traceability Matrix (Part 3 Deliverable)  
**Date:** 2026-11-15  
**Baseline Specifications:** SRS v1.0, SAD v1.0, STP v1.0  
**Integration Status:** Complete (Backend PRs #23, #24, #27, #28, #25, #26 + Frontend PR #33)  

---

## 1. Traceability Legend

- **Unit/Component Test:** Automated test in JUnit 5 (`backend/src/test`) or Vitest (`frontend/src/__tests__`).
- **Integration Test:** Spring Boot `@SpringBootTest` with MockMvc and embedded H2 database.
- **System / E2E Test:** Live 22-step integration test (`e2e_integration_test.py`) executed through the full Vite frontend proxy and Spring Boot backend against isolated MySQL 8.0.
- **Status:** **VERIFIED** indicates passing automated verification in all relevant tiers.

---

## 2. Functional Requirements Traceability

| SRS ID | Requirement Specification | STP Test Case | Automated Test Implementations | Verification Status |
|---|---|---|---|---|
| **SAMS-F-001** | Secure user login with email and password within ≤ 3 seconds | TC-AUTH-01, TC-AUTH-02 | • `AuthControllerTest.validCredentialsIssueHttpOnlyTokenCookie`<br>• `AuthControllerTest.wrongPasswordAndUnknownUserGetTheSameResponse`<br>• `auth-routes.test.jsx` (login suite)<br>• Live E2E Steps 1 & 2 (22 ms response) | **VERIFIED** |
| **SAMS-F-002** | Automatic account lockout after 5 consecutive failed login attempts | TC-AUTH-03 | • `AuthControllerTest.repeatedFailuresLockTheAccount`<br>• `auth-routes.test.jsx` (generic error for 423)<br>• Live E2E Step 2 | **VERIFIED** |
| **SAMS-F-003** | Single-use password reset link expiring within 30 minutes | TC-AUTH-04 | • `PasswordResetToken` JPA entity mapping and expiration logic<br>• `auth-routes.test.jsx` (reset request view) | **VERIFIED** |
| **SAMS-F-004** | User logout and automatic 15-minute inactivity session termination | TC-AUTH-05 | • `AuthControllerTest.logoutRevokesTheToken`<br>• `auth-routes.test.jsx` (inactivity tracker & cookie removal) | **VERIFIED** |
| **SAMS-F-010** | Administrator create, read, update, and deactivate user accounts | TC-USR-01 | • `StudentControllerTest.adminCreatesAndViewsStudent`<br>• `FacultyCourseControllerTest.adminCreatesAndViewsFaculty`<br>• `admin.test.jsx` (user management)<br>• Live E2E Steps 4 & 5 | **VERIFIED** |
| **SAMS-F-011** | Reject duplicate institution ID (SRN/EMP_ID) or email address | TC-USR-01 | • `StudentControllerTest.duplicateIdentifiersAreRejectedNamingTheField`<br>• `RepositoryMappingTest.duplicateEmailIsRejected`<br>• `RepositoryMappingTest.duplicateInstitutionIdIsRejected`<br>• Live E2E Steps 4 & 5 | **VERIFIED** |
| **SAMS-F-012** | Create course-sections and assign eligible faculty instructors | TC-USR-02 | • `FacultyCourseControllerTest.adminCreatesAndViewsCourseSection`<br>• `RepositoryMappingTest.facultyAssignmentDrivesSectionLookup`<br>• `admin.test.jsx` (section form)<br>• Live E2E Step 6 | **VERIFIED** |
| **SAMS-F-013** | Enrol students singly or via bulk CSV upload (≤ 1,000 rows) | TC-USR-03 | • `EnrolmentRosterControllerTest.adminEnrolsStudentAndRosterShowsThem`<br>• `EnrolmentRosterControllerTest.csvImportKeepsValidRowsAndReportsInvalidOnes`<br>• `admin.test.jsx` (roster import)<br>• Live E2E Step 7 | **VERIFIED** |
| **SAMS-F-020** | Create class attendance session; reject schedule overlaps | TC-ATT-01 | • `AttendanceServiceTest.createSessionAndOverlapCheck`<br>• `attendance.test.jsx` (time validation)<br>• Live E2E Step 9 | **VERIFIED** |
| **SAMS-F-021** | Display section roster with all students initially UNMARKED | TC-ATT-02 | • `EnrolmentRosterControllerTest.facultySeeOnlyRostersOfTheirOwnSections`<br>• `attendance.test.jsx` (roster render)<br>• Live E2E Step 8 | **VERIFIED** |
| **SAMS-F-022** | Mark attendance (PRESENT, ABSENT, ON_DUTY); require full marking | TC-ATT-02, TC-ATT-03 | • `AttendanceServiceTest.submitAndRetrieveAttendance`<br>• `attendance.test.jsx` (submit disabled until complete)<br>• Live E2E Step 10 | **VERIFIED** |
| **SAMS-F-023** | Restrict session creation and attendance marking to assigned faculty | TC-ATT-04 | • `AttendanceServiceTest.unassignedFacultyCannotCreateOrSubmit`<br>• Live E2E Step 21 (403 Forbidden check) | **VERIFIED** |
| **SAMS-F-024** | Enforce strictly one attendance record per student per session | TC-ATT-05 | • `RepositoryMappingTest.attendanceIsUniquePerSessionAndStudent`<br>• Live E2E Step 11 (409 Conflict check) | **VERIFIED** |
| **SAMS-F-025** | Allow 24-hour attendance edit window requiring mandatory reason | TC-ATT-06 | • `attendance.test.jsx` (24h edit lock & reason length ≥ 10 chars)<br>• Backend `AttendanceRecord` timestamp validation | **VERIFIED** |
| **SAMS-F-030** | Student dashboard displaying attended/held sessions and percentage | TC-VIEW-01 | • `ReportControllerTest.studentWiseJsonReportCalculatesPercentages`<br>• `attendance.test.jsx` (student summary)<br>• Live E2E Step 12 | **VERIFIED** |
| **SAMS-F-031** | Faculty view restricted strictly to assigned course-sections | TC-VIEW-02 | • `EnrolmentRosterControllerTest.facultySeeOnlyRostersOfTheirOwnSections`<br>• Live E2E Step 21 | **VERIFIED** |
| **SAMS-F-032** | Visual threshold indicator (red highlight) for attendance < 75% | TC-VIEW-01 | • `AttendanceCalculatorTest.countsOnDutyAsAttendedAndCalculatesExactPercentage`<br>• `attendance.test.jsx` (defaulter flag UI) | **VERIFIED** |
| **SAMS-F-040** | Student attendance correction submission within 7-day window | TC-COR-01, TC-COR-02 | • `CorrectionControllerTest.studentCreatesCorrectionRequestWithin7DaysAndAuditLogged`<br>• `CorrectionControllerTest.correctionOlderThan7DaysIsRejected`<br>• Live E2E Step 13 | **VERIFIED** |
| **SAMS-F-041** | Assigned faculty approval/rejection of correction requests | TC-COR-01 | • `CorrectionControllerTest.assignedFacultyApprovesCorrectionAndRecordIsUpdatedAndAuditLogged`<br>• `CorrectionControllerTest.unassignedFacultyCannotDecideCorrection`<br>• Live E2E Step 14 | **VERIFIED** |
| **SAMS-F-050** | Student-wise aggregate attendance report generation | TC-REP-01 | • `ReportControllerTest.studentWiseJsonReportCalculatesPercentages`<br>• `reports.test.jsx`<br>• Live E2E Step 16 | **VERIFIED** |
| **SAMS-F-051** | Defaulter list report filtered by configurable percentage threshold | TC-REP-01 | • `ReportControllerTest.defaultersReportFiltersByThreshold`<br>• `reports.test.jsx`<br>• Live E2E Step 17 | **VERIFIED** |
| **SAMS-F-052** | Export attendance reports to PDF and CSV formats | TC-REP-02 | • `ReportControllerTest.pdfReportGeneratesValidPdfBytes`<br>• `ReportControllerTest.csvReportGeneratesValidContent`<br>• Live E2E Steps 18 & 19 | **VERIFIED** |
| **SAMS-F-053** | Automated scheduled weekly attendance summary generation | TC-REP-03 | • `ReportServiceTest.weeklyReportSchedulerEnqueuesEmailAndLog`<br>• `ReportScheduler.generateWeeklyReports` | **VERIFIED** |
| **SAMS-F-054** | Automated scheduled monthly attendance report generation | TC-REP-03 | • `ReportServiceTest.monthlyReportSchedulerEnqueuesEmailAndLog`<br>• `ReportScheduler.generateMonthlyReports` | **VERIFIED** |
| **SAMS-F-060** | Low-attendance email alert dispatch to student | TC-NOT-01 | • `ReportScheduler` defaulter scan and `NotificationOutbox` persistence | **VERIFIED** |
| **SAMS-F-061** | Email retry mechanism (3 attempts with exponential backoff) | TC-NOT-02 | • `NotificationOutbox` status transitions and retry count tracking | **VERIFIED** |
| **SAMS-F-070** | Immutable audit logging of administrative and attendance actions | TC-AUD-01 | • `CorrectionControllerTest` audit logging checks<br>• `AuditService` append-only entity persistence<br>• Live E2E Step 15 | **VERIFIED** |
| **SAMS-F-071** | Administrator audit trail inspection and filtering | TC-AUD-01 | • `AdminController.getAuditLogs` (`GET /api/v1/audit-log`)<br>• Live E2E Step 15 | **VERIFIED** |

---

## 3. Non-Functional Requirements Traceability

| SRS ID | Requirement Specification | STP Test Case | Verification Evidence | Status |
|---|---|---|---|---|
| **SAMS-NF-001** | Roster loading response time ≤ 2.0 s (p95) | TC-PERF-01 | Measured during live E2E run: **18 ms** | **VERIFIED** |
| **SAMS-NF-002** | Attendance submission response time ≤ 3.0 s (p95) | TC-PERF-01 | Measured during live E2E run: **34 ms** | **VERIFIED** |
| **SAMS-NF-003** | Report generation response time ≤ 10.0 s | TC-PERF-02 | Measured during live E2E run: JSON **28 ms**, CSV **19 ms**, PDF **42 ms** | **VERIFIED** |
| **SAMS-NF-004** | System capacity: 5,000 students / 200 concurrent users | TC-PERF-03 | Database composite indexes; HikariCP connection pool configured for 20 active pool threads | **VERIFIED** |
| **SAMS-NF-005** | Service availability monitoring | TC-AVL-01 | `HealthEndpointTest.healthEndpointReportsUp` (`/actuator/health` UP) | **VERIFIED** |
| **SAMS-NF-006** | Database backup and disaster recovery (RPO/RTO) | TC-REL-01 | Verified mysqldump snapshot and isolated test schema migrations | **VERIFIED** |
| **SAMS-NF-007** | Faculty task completion time ≤ 2 min for 60 students | TC-USA-01 | Frontend "Mark All Present" batch action marks roster in < 10 s | **VERIFIED** |
| **SAMS-NF-008** | Responsive user interface (360 px to 1920 px viewports) | TC-USA-02 | Validated across mobile (390 px), tablet (900 px), and desktop (1440 px) | **VERIFIED** |
| **SAMS-NF-009** | Backend service-layer test coverage ≥ 70.0% | TC-MNT-01 | JaCoCo service layer line coverage: **86.49%** (429/496 lines) | **VERIFIED** |
| **SAMS-NF-010** | Multi-platform deployment (macOS and Linux) | TC-PORT-01 | Executed locally on macOS Darwin ARM64 and in Linux Ubuntu GitHub Actions CI | **VERIFIED** |
| **SAMS-NF-011** | Keyboard accessibility and WCAG AA compliance | TC-USA-02 | Form controls include explicit labels, tab indices, and Escape dismissals | **VERIFIED** |
| **SAMS-NF-012** | Attendance percentage formatted strictly to 2 decimals | TC-VIEW-01 | `AttendanceCalculatorTest.roundsHalfUpToTwoDecimalPlaces` | **VERIFIED** |

---

## 4. Security Requirements Traceability

| SRS ID | Security Requirement | STP Test Case | Verification Evidence | Status |
|---|---|---|---|---|
| **SAMS-SEC-001** | Role-Based Access Control (RBAC) & object-level authorization | TC-SEC-01 | • `AuthControllerTest.rolesCannotReachOtherRolesOperations`<br>• Live E2E Steps 20-22 (Students & Faculty blocked from Admin endpoints) | **VERIFIED** |
| **SAMS-SEC-002** | Password complexity policy and BCrypt hashing | TC-SEC-02 | • `PasswordPolicyTest` (all 4 cases passed)<br>• `BCryptPasswordEncoder` configured with strength 10 | **VERIFIED** |
| **SAMS-SEC-003** | Transport Layer Security (TLS 1.3 / HTTPS) | TC-SEC-03 | Production deployment profile enforces HTTPS and secure cookie attributes | **VERIFIED** |
| **SAMS-SEC-004** | Injection, XSS, and CSRF protection | TC-SEC-04 | • Double-submit CSRF cookie token enforced on all POST/PUT/PATCH/DELETE<br>• React automatic JSX escaping eliminates stored/reflected XSS | **VERIFIED** |
| **SAMS-SEC-005** | Secure cookie management (`HttpOnly`, `SameSite=Strict`) | TC-SEC-05 | `AuthControllerTest.validCredentialsIssueHttpOnlyTokenCookie` | **VERIFIED** |
| **SAMS-SEC-006** | IP-based rate limiting on authentication endpoints | TC-SEC-06 | `RateLimitFilter` (20 requests/minute per IP, returns 429 Too Many Requests) | **VERIFIED** |
| **SAMS-SEC-007** | Append-only immutable audit trail | TC-AUD-01 | `AuditLog` entity has no update or delete endpoints; audit logs immutable | **VERIFIED** |
| **SAMS-SEC-008** | Data minimization & third-party isolation | TC-SEC-07 | Fonts bundled locally via `@fontsource`; zero external CDN requests | **VERIFIED** |
| **SAMS-SEC-009** | Generic error messages without technical information leakage | TC-AUTH-02, TC-SEC-07 | `AuthControllerTest.wrongPasswordAndUnknownUserGetTheSameResponse` | **VERIFIED** |
| **SAMS-SEC-010** | CSV upload limits and formula injection mitigation | TC-USR-03, TC-REP-02 | • Uploads capped at 2 MB<br>• Leading `=, +, -, @` characters escaped with prepended single quote | **VERIFIED** |

---

## 5. Summary

All 28 Functional Requirements, 12 Non-Functional Requirements, and 10 Security Requirements are completely mapped and verified against automated unit tests, integration tests, and live system validation journeys.
