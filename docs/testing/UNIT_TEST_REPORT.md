# SAMS Unit and Integration Test Execution Report

**Project:** Student Attendance Management System (SAMS)  
**Sprint:** Sprint 2 Phase 2 Integration  
**Date:** 2026-11-15  
**Environment:** OpenJDK 17.0.21, Maven 3.9+, Node 20.x, Vitest 2.1.9, Spring Boot 3.3.5  
**Result:** **122 / 122 PASSED (100% Success Rate)**  

---

## 1. Executive Summary

All automated backend and frontend unit/integration test suites were executed against the integrated Sprint 2 codebase on branch `integration/sprint2`. The test suites validate domain business logic, data persistence invariants, security constraints, role-based authorization, REST API contract fulfillment, frontend rendering, input sanitization, and state management.

| Test Tier | Framework | Suites / Classes | Tests Executed | Passed | Failed | Errors | Execution Time |
|---|---|---|---|---|---|---|---|
| **Backend Unit & Integration** | JUnit 5 / Spring Boot Test / MockMvc | 12 classes | 61 | 61 | 0 | 0 | 33.25 s |
| **Frontend Unit & Component** | Vitest 2 / React Testing Library | 5 suites | 61 | 61 | 0 | 0 | 6.00 s |
| **Total Automated Tests** | — | **17 suites** | **122** | **122** | **0** | **0** | **39.25 s** |

---

## 2. Backend Test Execution Details

Executed via Maven: `mvn --batch-mode clean test verify` with Java 17.

### 2.1. `edu.pesu.sams.repository.RepositoryMappingTest`
- **Tests:** 7 | **Pass:** 7 | **Fail:** 0 | **Duration:** 6.607 s
- **Scope:** Verifies JPA database schema constraints, foreign key mappings, multi-tenant and unique indexes.
  1. `duplicateCourseSectionIdentityIsRejected` (0.527s) — Asserts duplicate (course_id, section_name, semester) raises `DataIntegrityViolationException`.
  2. `duplicateEmailIsRejected` (0.013s) — Enforces email uniqueness across all user roles.
  3. `facultyAssignmentDrivesSectionLookup` (0.101s) — Verifies faculty-to-course-section bidirectional associations.
  4. `userLookupsIgnoreCaseAndSetTimestamps` (0.016s) — Verifies case-insensitive email indexing and `@PrePersist` timestamping.
  5. `duplicateInstitutionIdIsRejected` (0.006s) — Asserts unique constraint on student/faculty institution IDs (`SRN`/`EMP_ID`).
  6. `attendanceIsUniquePerSessionAndStudent` (0.017s) — Confirms composite unique constraint on `(session_id, student_id)` prevents duplicate records.
  7. `enrolmentIsUniquePerSectionAndStudent` (0.016s) — Confirms unique constraint on `(section_id, student_id)`.

### 2.2. `edu.pesu.sams.controller.AuthControllerTest`
- **Tests:** 6 | **Pass:** 6 | **Fail:** 0 | **Duration:** 9.281 s
- **Scope:** Authentication, token issuance, security filters, rate limiting, and account lockouts.
  1. `validCredentialsIssueHttpOnlyTokenCookie` (2.265s) — Asserts successful login issues `sams_token` cookie with `HttpOnly` and `SameSite=Strict`.
  2. `protectedEndpointsRequireAuthentication` (0.467s) — Asserts unauthenticated requests to protected endpoints return `401 Unauthorized`.
  3. `rolesCannotReachOtherRolesOperations` (0.919s) — Asserts Students/Faculty cannot reach Admin endpoints (`403 Forbidden`).
  4. `repeatedFailuresLockTheAccount` (2.258s) — Verifies account lockout after 5 consecutive failed login attempts (returns HTTP 423 / locked status).
  5. `wrongPasswordAndUnknownUserGetTheSameResponse` (1.422s) — Prevents user enumeration by ensuring identical error responses.
  6. `logoutRevokesTheToken` (0.524s) — Asserts `/auth/logout` clears auth cookies and invalidates session token.

### 2.3. `edu.pesu.sams.controller.CorrectionControllerTest`
- **Tests:** 8 | **Pass:** 8 | **Fail:** 0 | **Duration:** 1.338 s
- **Scope:** Correction workflow, role permissions, 7-day policy, audit logging.
  1. `correctionOlderThan7DaysIsRejected` (0.063s) — Verifies business rule rejecting corrections submitted >7 days post-session.
  2. `unassignedFacultyCannotDecideCorrection` (0.037s) — Ensures faculty not assigned to section receive 403 Forbidden.
  3. `assignedFacultyApprovesCorrectionAndRecordIsUpdatedAndAuditLogged` (0.061s) — Verifies faculty approval updates `AttendanceRecord` status and creates audit log.
  4. `studentViewsOnlyOwnCorrections` (0.030s) — Verifies students cannot view other students' requests.
  5. `pendingPermissionsForFacultyAndAdmin` (0.050s) — Verifies authorization on `/corrections/pending` for Faculty and Admin.
  6. `studentCreatesCorrectionRequestWithin7DaysAndAuditLogged` (0.035s) — Verifies student request persistence and `CORRECTION_REQUESTED` audit entry.
  7. `studentCannotRequestCorrectionForAnotherStudent` (0.029s) — Prevents IDOR horizontal privilege escalation.
  8. `adminCanDecideCorrection` (0.029s) — Verifies administrative override permissions on correction requests.

### 2.4. `edu.pesu.sams.controller.ReportControllerTest`
- **Tests:** 6 | **Pass:** 6 | **Fail:** 0 | **Duration:** 5.098 s
- **Scope:** Attendance reporting, threshold analytics, CSV/PDF generation, formula sanitization.
  1. `pdfReportGeneratesValidPdfBytes` (0.602s) — Verifies OpenPDF stream starts with `%PDF-` header.
  2. `studentWiseJsonReportCalculatesPercentages` (0.144s) — Asserts accurate percentages and threshold evaluations.
  3. `invalidDateRangeOrThresholdRejected` (0.077s) — Validates bad request rejection on malformed date windows or thresholds (<0 or >100).
  4. `rolePermissionsEnforced` (0.091s) — Verifies access matrix on `/reports/defaulters` and `/reports/student-wise`.
  5. `csvReportGeneratesValidContent` (0.063s) — Validates CSV structure and column headers.
  6. `defaultersReportFiltersByThreshold` (0.083s) — Verifies filtering students strictly below specified threshold.

### 2.5. `edu.pesu.sams.controller.StudentControllerTest`
- **Tests:** 7 | **Pass:** 7 | **Fail:** 0 | **Duration:** 2.643 s
- **Scope:** Student entity CRUD, pagination, filtering, authorization.
  1. `listSupportsSearchFilterAndPagination` (0.093s) — Verifies page size, sort order, and term searching.
  2. `nonAdminsCannotManageStudents` (0.049s) — Enforces 403 Forbidden for non-admin mutation attempts.
  3. `studentSeesOnlyOwnProfile` (0.027s) — Enforces isolation of student profile access.
  4. `invalidOrMissingFieldsAreReported` (0.045s) — Verifies RFC 7807 validation error payloads.
  5. `adminUpdatesAndDeactivatesStudent` (0.041s) — Verifies update and soft deactivation transitions.
  6. `duplicateIdentifiersAreRejectedNamingTheField` (0.043s) — Verifies conflict response naming duplicated SRN or email.
  7. `adminCreatesAndViewsStudent` (0.533s) — Verifies end-to-end admin creation of student account.

### 2.6. `edu.pesu.sams.controller.FacultyCourseControllerTest`
- **Tests:** 7 | **Pass:** 7 | **Fail:** 0 | **Duration:** 2.880 s
- **Scope:** Faculty CRUD, Course Section lifecycle, assignment validation.
  1. `adminCreatesAndViewsCourseSection` (0.196s)
  2. `onlyAdminsCanManageFacultyAndCourses` (0.129s)
  3. `invalidCourseSectionsAreRejected` (0.126s)
  4. `adminUpdatesCourseSection` (0.092s)
  5. `adminCreatesAndViewsFaculty` (0.602s)
  6. `lastActiveFacultyOfASectionCannotBeDeactivated` (0.116s)
  7. `facultyValidationAndDuplicates` (0.058s)

### 2.7. `edu.pesu.sams.controller.EnrolmentRosterControllerTest`
- **Tests:** 5 | **Pass:** 5 | **Fail:** 0 | **Duration:** 1.569 s
- **Scope:** Enrolments, class rosters, bulk CSV parsing.
  1. `onlyAdminsCanEnrolStudents` (0.051s)
  2. `facultySeeOnlyRostersOfTheirOwnSections` (0.043s)
  3. `adminEnrolsStudentAndRosterShowsThem` (0.061s)
  4. `duplicateAndInvalidEnrolmentsAreRejected` (0.105s)
  5. `csvImportKeepsValidRowsAndReportsInvalidOnes` (0.074s)

### 2.8. `edu.pesu.sams.service.AttendanceServiceTest`
- **Tests:** 3 | **Pass:** 3 | **Fail:** 0 | **Duration:** 0.996 s
- **Scope:** Session creation, schedule overlap checks, attendance marking.
  1. `createSessionAndOverlapCheck` (0.026s) — Rejects overlapping sessions for same section/slot.
  2. `submitAndRetrieveAttendance` (0.065s) — Validates marking PRESENT/ABSENT/ON_DUTY and retrieval.
  3. `unassignedFacultyCannotCreateOrSubmit` (0.013s) — Verifies 403 authorization guard on session marking.

### 2.9. `edu.pesu.sams.service.ReportServiceTest`
- **Tests:** 3 | **Pass:** 3 | **Fail:** 0 | **Duration:** 0.925 s
- **Scope:** CSV formula injection protection, scheduled reporting triggers.
  1. `monthlyReportSchedulerEnqueuesEmailAndLog` (0.030s) — Verifies monthly cron generates outbox entries.
  2. `weeklyReportSchedulerEnqueuesEmailAndLog` (0.013s) — Verifies weekly cron generates outbox entries.
  3. `csvFormulaInjectionEscapesSpecialCharacters` (0.009s) — Ensures `=, +, -, @` leading characters are prepended with single quotes.

### 2.10. `edu.pesu.sams.service.AttendanceCalculatorTest`
- **Tests:** 4 | **Pass:** 4 | **Fail:** 0 | **Duration:** 0.002 s
- **Scope:** Mathematical attendance calculation rules.
  1. `countsOnDutyAsAttendedAndCalculatesExactPercentage` (0.000s) — Verifies `(PRESENT + ON_DUTY) / TOTAL * 100`.
  2. `returnsZeroWhenNoSessionsHaveBeenHeld` (0.000s) — Handles division-by-zero safely.
  3. `roundsHalfUpToTwoDecimalPlaces` (0.000s) — Verifies IEEE round-half-up to 2 decimals.
  4. `doesNotReturnMoreThanOneHundredForValidCounts` (0.001s) — Verifies upper bound boundary conditions.

### 2.11. `edu.pesu.sams.service.PasswordPolicyTest`
- **Tests:** 4 | **Pass:** 4 | **Fail:** 0 | **Duration:** 0.003 s
- **Scope:** NIST/PES password complexity enforcement.
  1. `rejectsPasswordLongerThanBcryptLimit` (0.000s) — Rejects strings > 72 bytes.
  2. `acceptsPasswordWithEveryRequiredCharacterClass` (0.001s) — Accepts compliant strings with upper, lower, digit, special.
  3. `rejectsPasswordWithoutUppercase` (0.001s)
  4. `rejectsPasswordWithoutNumber` (0.000s)

### 2.12. `edu.pesu.sams.HealthEndpointTest`
- **Tests:** 1 | **Pass:** 1 | **Fail:** 0 | **Duration:** 0.989 s
- **Scope:** Spring Boot Actuator `/actuator/health` verification.
  1. `healthEndpointReportsUp` (0.044s) — Verifies `{"status":"UP"}`.

---

## 3. Frontend Test Execution Details

Executed via Vitest: `npm test -- --run` in `frontend/`.

| Test File | Category / Feature | Tests | Pass | Fail | Duration |
|---|---|---|---|---|---|
| `src/__tests__/validation.test.js` | Input sanitization, password policy, SRN & email regex, time range validation | 23 | 23 | 0 | 0.42 s |
| `src/__tests__/auth-routes.test.jsx` | Login form, generic error display, rate limit banner, role routing, inactivity timeout | 18 | 18 | 0 | 1.28 s |
| `src/__tests__/admin.test.jsx` | User CRUD, course section creation, single & CSV bulk enrolment, duplicate rejection | 8 | 8 | 0 | 1.89 s |
| `src/__tests__/attendance.test.jsx` | Attendance session marking, mark all present, submit disabling, 24h edit window, correction request & decision | 7 | 7 | 0 | 1.42 s |
| `src/__tests__/reports.test.jsx` | Student-wise table, defaulter threshold filtering, CSV/PDF export error handling | 5 | 5 | 0 | 0.91 s |
| **Total** | — | **61** | **61** | **0** | **6.00 s** |

---

## 4. Conclusion

- **Total Automated Test Suites:** 17
- **Total Automated Tests:** 122
- **Pass Rate:** 100.0%
- **Regressions Identified:** None
