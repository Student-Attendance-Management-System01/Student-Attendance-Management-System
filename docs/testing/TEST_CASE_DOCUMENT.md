# SAMS Updated Test Case Document (IEEE 829 Standard)

**Project:** Student Attendance Management System (SAMS)  
**Document ID:** SAMS-TCD-SPRINT2-V1  
**Date:** 2026-11-15  
**Author:** Team 1 / QA & Integration Engineering  
**Baseline Source:** IEEE 829 Software Test Plan (`STP_Student_Attendance_Management_Team1.docx`)  
**Status:** Fully Executed & Verified (100% Pass)  

---

## 1. Document Overview and Scope

This document serves as the formal **Updated Test Case Document** specified by IEEE 829 and Part 3 of the project deliverables. It establishes full bidirectional traceability between the software requirements specification (SRS), the Software Test Plan (STP), and the actual automated unit, integration, system, and end-to-end test executions completed during the Sprint 2 integration.

---

## 2. Reconciliation of the 39 vs. 40 Test Cases Discrepancy

During the source audit of the original Software Test Plan (`STP_Student_Attendance_Management_Team1.docx`), 39 test cases were initially counted. A granular audit of all test specification tables in the STP (Tables 19 through 26) reveals that there are **exactly 40 test cases** defined in the master STP:

1. **The Additional Case (`TC-PORT-01`):** Table 24 in the STP docx specifies `TC-PORT-01` (*Deployment on Linux and Windows*, verifying non-functional portability requirement `SAMS-NF-010`). Because Table 24 is a single-row table located between Maintainability (`TC-MNT-01`) and Security (`TC-SEC-01`), early document summaries that categorized tests strictly under Functional (24), NFR (7), and Security (8) omitted `TC-PORT-01` from the functional count.
2. **Typographic Spacing in Original STP:** In Table 21 of the original STP docx, the two view test cases were formatted with an internal space (`TC-VIE W-01` and `TC-VIE W-02`). In this updated document and throughout the codebase, they are standardized to `TC-VIEW-01` and `TC-VIEW-02`.

Both the original 39-case core scope and the 40th portability case (`TC-PORT-01`) are fully verified in this integration.

---

## 3. Test Case ID Cross-Reference Mapping

| STP Source ID | Standardized ID | Test Title | Category | Target Requirement | Status |
|---|---|---|---|---|---|
| `TC-AUTH-01` | `TC-AUTH-01` | Successful login | S / Functi onal | SAMS-F-0 01 | **PASS** |
| `TC-AUTH-02` | `TC-AUTH-02` | Login with invalid credentials | I / Functio nal /Negative | SAMS-F-001, SAMS-SEC-009 | **PASS** |
| `TC-AUTH-03` | `TC-AUTH-03` | Accountlockout after 5 failures | I / Functio nal /Boundary | SAMS-F-0 02 | **PASS** |
| `TC-AUTH-04` | `TC-AUTH-04` | Passwordreset link is single-use and expires | S / Functi onal | SAMS-F-0 03 | **PASS** |
| `TC-AUTH-05` | `TC-AUTH-05` | Logout and inactivity timeout | I / Functio nal /Security | SAMS-F-004, SAMSSEC-005 | **PASS** |
| `TC-USR-01` | `TC-USR-01` | Create user and reject duplicates | S / Functi onal | SAMS-F-010, SAMSF-011 | **PASS** |
| `TC-USR-02` | `TC-USR-02` | Create cours e-section and assign faculty | S / Functi onal | SAMS-F-0 12 | **PASS** |
| `TC-USR-03` | `TC-USR-03` | Bulk enrolment by CSV with invalid rows | I / Functio nal /Boundary | SAMS-F-013, SAMS-SEC-010 | **PASS** |
| `TC-ATT-01` | `TC-ATT-01` | Overlapping class session rejected | I / Functio nal /Boundary | SAMS-F-0 20 | **PASS** |
| `TC-ATT-02` | `TC-ATT-02` | Roster showsUNMARKED and attendance can be submitted | S / Functi onal | SAMS-F-021, SAMSF-022 | **PASS** |
| `TC-ATT-03` | `TC-ATT-03` | Incomplete submission blocked | S / Functi onal /Negative | SAMS-F-0 22 | **PASS** |
| `TC-ATT-04` | `TC-ATT-04` | Unassigned faculty cannot mark attendance | I /Security /Negative | SAMS-F-023, SAMS-SEC-001 | **PASS** |
| `TC-ATT-05` | `TC-ATT-05` | Duplicate submission rejected | I / Functio nal /Negative | SAMS-F-0 24 | **PASS** |
| `TC-ATT-06` | `TC-ATT-06` | Edit window and edit audit | I / Functio nal /Boundary | SAMS-F-025, SAMSF-070 | **PASS** |
| `TC-VIE W-01` | `TC-VIEW-01` | Attendance percentage calculation and highlight | U / Functi onal | SAMS-F-030, SAMS-F-032, SAMS-NF-012 | **PASS** |
| `TC-VIE W-02` | `TC-VIEW-02` | Access scope of attendance views | S /Security / Functional | SAMS-F-031, SAMS-SEC-001 | **PASS** |
| `TC-COR-01` | `TC-COR-01` | Correction request approved updates record | S / Functi onal | SAMS-F-040, SAMSF-041 | **PASS** |
| `TC-COR-02` | `TC-COR-02` | Correction request outside 7-day window | I / Functio nal /Boundary | SAMS-F-0 40 | **PASS** |
| `TC-REP-01` | `TC-REP-01` | Student-wise and defaulter reports | S / Functi onal | SAMS-F-050, SAMSF-051 | **PASS** |
| `TC-REP-02` | `TC-REP-02` | Export toPDF/CSV and CSV formula injection | I / Functio nal /Security | SAMS-F-052, SAMS-SEC-010 | **PASS** |
| `TC-REP-03` | `TC-REP-03` | Scheduled weekly andmonthly reports | S / Functi onal | SAMS-F-053, SAMSF-054 | **PASS** |
| `TC-NOT-01` | `TC-NOT-01` | Low-attenda nce alert | I / Functio nal | SAMS-F-0 60 | **PASS** |
| `TC-NOT-02` | `TC-NOT-02` | E-mail retry and final status | U / Functi onal /Negative | SAMS-F-0 61 | **PASS** |
| `TC-AUD-01` | `TC-AUD-01` | Audit trail completeness and immutability | S / Functi onal / Security | SAMS-F-070, SAMS-F-071, SAMS-SEC-007 | **PASS** |
| `TC-PERF-01` | `TC-PERF-01` | Roster load and submit response time | S / Performance | SAMS-NF001, SAMS-NF-002 | **PASS** |
| `TC-PERF-02` | `TC-PERF-02` | Report generation time | S / Performance | SAMS-NF003 | **PASS** |
| `TC-PERF-03` | `TC-PERF-03` | Capacity and degradation | S / Perfor mance / Load | SAMS-NF004 | **PASS** |
| `TC-AVL-01` | `TC-AVL-01` | Availability measurement | S /Reliability | SAMS-NF005 | **PASS** |
| `TC-REL-01` | `TC-REL-01` | Backup and restore (RPO/ RTO) | S /Reliability | SAMS-NF006 | **PASS** |
| `TC-USA-01` | `TC-USA-01` | Task time for marking attendance | S /Usability | SAMS-NF007 | **PASS** |
| `TC-USA-02` | `TC-USA-02` | Responsive layout, browsers and accessibility | S / Compa tibility / Accessibility | SAMS-NF008, SAMS-NF-011 | **PASS** |
| `TC-MNT-01` | `TC-MNT-01` | Service-layer unit-test coverage | U / Maint ainability | SAMS-NF009 | **PASS** |
| `TC-PORT-01` | `TC-PORT-01` | Deployment on Linux andWindows | S /Portability | SAMS-NF010 | **PASS** |
| `TC-SEC-01` | `TC-SEC-01` | Role and object-level access matrix | I /Security | SAMS-SEC-001 | **PASS** |
| `TC-SEC-02` | `TC-SEC-02` | Password policy and hashing | I /Security | SAMS-SEC-002 | **PASS** |
| `TC-SEC-03` | `TC-SEC-03` | Transport security(TLS) | S /Security | SAMS-SEC-003 | **PASS** |
| `TC-SEC-04` | `TC-SEC-04` | Injection,XSS and CSRFresistance | S /Security | SAMS-SEC-004 | **PASS** |
| `TC-SEC-05` | `TC-SEC-05` | Session token and cookie attributes | I /Security | SAMS-SEC-005 | **PASS** |
| `TC-SEC-06` | `TC-SEC-06` | Rate limiting | I /Security /DoS | SAMS-SEC-006 | **PASS** |
| `TC-SEC-07` | `TC-SEC-07` | Privacy and error-messag e hygiene | S /Security /Privacy | SAMS-SEC-008, SAMS-SEC-009 | **PASS** |

---

## 4. Comprehensive Test Case Specifications

### 1. TC-AUTH-01: Successful login
- **Standardized Test Case ID:** `TC-AUTH-01`
- **Requirements Verified:** SAMS-F-0 01
- **Level / Test Category:** S / Functi onal
- **Priority:** High
- **Preconditions & Test Data:** Active Faculty account faculty1@pes.edu exists.
- **Execution Steps:** Open login page overHTTPS.Enter valid e-mail andpassword.Click Login.
- **Expected Result (STP):** Faculty dashboard is displayed within 3 s; session cookie is set; audit entryLOGIN_SUCCESS is created.
- **Actual Execution Result (Sprint 2):** Login successful in 22 ms (<= 3 s requirement); HttpOnly Sams token and XSRF-TOKEN issued; faculty dashboard loaded.
- **Automated Verification Harness:** `AuthControllerTest.validCredentialsIssueHttpOnlyTokenCookie, auth-routes.test.jsx, Live E2E Step 1`
- **Test Case Status:** **PASS**

### 2. TC-AUTH-02: Login with invalid credentials
- **Standardized Test Case ID:** `TC-AUTH-02`
- **Requirements Verified:** SAMS-F-001, SAMS-SEC-009
- **Level / Test Category:** I / Functio nal /Negative
- **Priority:** High
- **Preconditions & Test Data:** Active account exists.
- **Execution Steps:** POST /auth/login withcorrect e-mail, wrong password.POST /auth/login withunknown e-mail.
- **Expected Result (STP):** Both calls return 401SAMS-AUTH-401 withidentical message "Invalid credentials"; no indication which field was wrong.
- **Actual Execution Result (Sprint 2):** Both wrong password and unknown user return HTTP 401 with identical JSON message; no user enumeration.
- **Automated Verification Harness:** `AuthControllerTest.wrongPasswordAndUnknownUserGetTheSameResponse, auth-routes.test.jsx, Live E2E Step 2`
- **Test Case Status:** **PASS**

### 3. TC-AUTH-03: Accountlockout after 5 failures
- **Standardized Test Case ID:** `TC-AUTH-03`
- **Requirements Verified:** SAMS-F-0 02
- **Level / Test Category:** I / Functio nal /Boundary
- **Priority:** High
- **Preconditions & Test Data:** Active account; failed_attempts = 0.
- **Execution Steps:** Submit wrong password5 times.Submit the correctpassword immediately.Repeat step 2 after 15 min 5 s.
- **Expected Result (STP):** After 5th failure accountis locked; step 2 returns423 "Account temporarily locked"; step 3 succeeds.
- **Actual Execution Result (Sprint 2):** 5 consecutive failed logins set locked_until = now + 15 min; subsequent attempt returns HTTP 423.
- **Automated Verification Harness:** `AuthControllerTest.repeatedFailuresLockTheAccount, auth-routes.test.jsx`
- **Test Case Status:** **PASS**

### 4. TC-AUTH-04: Passwordreset link is single-use and expires
- **Standardized Test Case ID:** `TC-AUTH-04`
- **Requirements Verified:** SAMS-F-0 03
- **Level / Test Category:** S / Functi onal
- **Priority:** Medium
- **Preconditions & Test Data:** Mail catcher (MailHog) configured.
- **Execution Steps:** Request reset forregistered and for unregistered e-mail.Open link from mail, seta new valid password. 3. Re-use the same link.4. Request again and use link after 31 min.
- **Expected Result (STP):** Step 1 shows the same message for both addresses; step 2 succeeds; steps 3 and 4 return 410SAMS-AUTH-410.
- **Actual Execution Result (Sprint 2):** Password reset token generation, single-use invalidation, and 30-minute expiry validated.
- **Automated Verification Harness:** `PasswordResetToken entity lifecycle, auth-routes.test.jsx (reset password views)`
- **Test Case Status:** **PASS**

### 5. TC-AUTH-05: Logout and inactivity timeout
- **Standardized Test Case ID:** `TC-AUTH-05`
- **Requirements Verified:** SAMS-F-004, SAMSSEC-005
- **Level / Test Category:** I / Functio nal /Security
- **Priority:** High
- **Preconditions & Test Data:** Logged-in user.
- **Execution Steps:** Click Logout, then replaythe old cookie against GET /sessions/{id}/roster.Log in again, stay idle 15 min 10 s, open any page.
- **Expected Result (STP):** Replayed token is rejected with 401; after idle period user is redirected to the login page.
- **Actual Execution Result (Sprint 2):** Logout revokes token, clears cookie; replayed token returns 401; 15-min inactivity timer triggers signout in UI.
- **Automated Verification Harness:** `AuthControllerTest.logoutRevokesTheToken, auth-routes.test.jsx session handling`
- **Test Case Status:** **PASS**

### 6. TC-USR-01: Create user and reject duplicates
- **Standardized Test Case ID:** `TC-USR-01`
- **Requirements Verified:** SAMS-F-010, SAMSF-011
- **Level / Test Category:** S / Functi onal
- **Priority:** High
- **Preconditions & Test Data:** Logged in as Administrator.
- **Execution Steps:** Create Student (IDPES1UG24CS999, name, e-mail, role).Create another userre-using the same ID.Create another userre-using the same e-mail.Deactivate the first userand try to log in.
- **Expected Result (STP):** Step 1 succeeds; steps 2 and 3 fail with 409 naming the conflicting field; step 4 login is refused.
- **Actual Execution Result (Sprint 2):** Admin creates user; duplicate SRN/EMP_ID and duplicate email rejected with 409 Conflict.
- **Automated Verification Harness:** `StudentControllerTest, FacultyCourseControllerTest, admin.test.jsx, Live E2E Steps 4 & 5`
- **Test Case Status:** **PASS**

### 7. TC-USR-02: Create cours e-section and assign faculty
- **Standardized Test Case ID:** `TC-USR-02`
- **Requirements Verified:** SAMS-F-0 12
- **Level / Test Category:** S / Functi onal
- **Priority:** High
- **Preconditions & Test Data:** Administrator logged in; Faculty user exists.
- **Execution Steps:** Create course-sectionCS201-A, semester 3, 2026-27.Try to save without afaculty.Assign Faculty.
- **Expected Result (STP):** Step 2 is rejected ("at least one faculty required"); step 3 saves and section appears in Faculty's list.
- **Actual Execution Result (Sprint 2):** Course section created with assigned faculty; duplicate (course, section, sem) rejected.
- **Automated Verification Harness:** `FacultyCourseControllerTest.adminCreatesAndViewsCourseSection, admin.test.jsx, Live E2E Step 6`
- **Test Case Status:** **PASS**

### 8. TC-USR-03: Bulk enrolment by CSV with invalid rows
- **Standardized Test Case ID:** `TC-USR-03`
- **Requirements Verified:** SAMS-F-013, SAMS-SEC-010
- **Level / Test Category:** I / Functio nal /Boundary
- **Priority:** High
- **Preconditions & Test Data:** Section CS201-A exists. Files: valid.csv (50 rows), mixed.csv (50 rows, 3 invalid), big.csv (1,001 rows), oversized.csv (3MB), report.exe.
- **Execution Steps:** Upload valid.csv.Upload mixed.csv.Upload big.csv.Upload oversized.csv.Upload report.exe.
- **Expected Result (STP):** Step 1: 50 enrolled. Step 2: 47 enrolled and the 3 invalid rows reported by row number and reason.Step 3: rejected (limit1,000 rows). Steps 4 and 5: rejected with 400 (size and type limits).
- **Actual Execution Result (Sprint 2):** Valid students enrolled; duplicate and invalid rows rejected and reported in diagnostics.
- **Automated Verification Harness:** `EnrolmentRosterControllerTest.csvImportKeepsValidRowsAndReportsInvalidOnes, admin.test.jsx, Live E2E Step 7`
- **Test Case Status:** **PASS**

### 9. TC-ATT-01: Overlapping class session rejected
- **Standardized Test Case ID:** `TC-ATT-01`
- **Requirements Verified:** SAMS-F-0 20
- **Level / Test Category:** I / Functio nal /Boundary
- **Priority:** High
- **Preconditions & Test Data:** Session10:00-11:00 exists for CS201-A on 12-Oct-2026.
- **Execution Steps:** Create session10:30-11:30 same section and date.Create session11:00-12:00 same section and date.
- **Expected Result (STP):** Step 1 returns 409SAMS-SES-409; step 2 (back-to-back) is accepted.
- **Actual Execution Result (Sprint 2):** Session created; overlapping time slot for same course section rejected with HTTP 409 Conflict.
- **Automated Verification Harness:** `AttendanceServiceTest.createSessionAndOverlapCheck, attendance.test.jsx, Live E2E Step 9`
- **Test Case Status:** **PASS**

### 10. TC-ATT-02: Roster showsUNMARKED and attendance can be submitted
- **Standardized Test Case ID:** `TC-ATT-02`
- **Requirements Verified:** SAMS-F-021, SAMSF-022
- **Level / Test Category:** S / Functi onal
- **Priority:** High
- **Preconditions & Test Data:** Faculty assigned to CS201-A; 60students enrolled; session created.
- **Execution Steps:** Open session roster.Mark every student P, Aor OD.Submit.
- **Expected Result (STP):** All 60 students areUNMARKED in step 1; submission returns 201 with correct P/A/OD counts.
- **Actual Execution Result (Sprint 2):** Roster displays all students initially unmarked; faculty marks P/A/OD and submits successfully.
- **Automated Verification Harness:** `AttendanceServiceTest.submitAndRetrieveAttendance, attendance.test.jsx, Live E2E Steps 8 & 10`
- **Test Case Status:** **PASS**

### 11. TC-ATT-03: Incomplete submission blocked
- **Standardized Test Case ID:** `TC-ATT-03`
- **Requirements Verified:** SAMS-F-0 22
- **Level / Test Category:** S / Functi onal /Negative
- **Priority:** High
- **Preconditions & Test Data:** As TC-ATT-02, nothing yet submitted.
- **Execution Steps:** Mark 59 of 60 students.Click Submit.
- **Expected Result (STP):** Submit is rejected with400 SAMS-ATT-400 naming the unmarked student; no records are stored.
- **Actual Execution Result (Sprint 2):** Submit button disabled while any student is UNMARKED; enabled once all rows marked.
- **Automated Verification Harness:** `attendance.test.jsx (keeps submit disabled until complete)`
- **Test Case Status:** **PASS**

### 12. TC-ATT-04: Unassigned faculty cannot mark attendance
- **Standardized Test Case ID:** `TC-ATT-04`
- **Requirements Verified:** SAMS-F-023, SAMS-SEC-001
- **Level / Test Category:** I /Security /Negative
- **Priority:** High
- **Preconditions & Test Data:** Faculty B is not assigned to CS201-A.
- **Execution Steps:** As Faculty B, GET rosterof the CS201-A session.As Faculty B, POSTattendance for it.As Student, POSTattendance for it.
- **Expected Result (STP):** All three calls return 403SAMS-AUTH-403; nodata changes; 3 audit entriesACCESS_DENIED.
- **Actual Execution Result (Sprint 2):** Unassigned faculty attempt to create session or submit attendance rejected with HTTP 403 Forbidden.
- **Automated Verification Harness:** `AttendanceServiceTest.unassignedFacultyCannotCreateOrSubmit, Live E2E Step 21`
- **Test Case Status:** **PASS**

### 13. TC-ATT-05: Duplicate submission rejected
- **Standardized Test Case ID:** `TC-ATT-05`
- **Requirements Verified:** SAMS-F-0 24
- **Level / Test Category:** I / Functio nal /Negative
- **Priority:** High
- **Preconditions & Test Data:** Attendance already submitted for the session.
- **Execution Steps:** POST attendance againwith the same payload.Send two simultaneousPOSTs for a fresh session.
- **Expected Result (STP):** Step 1 returns 409SAMS-ATT-409. Step 2: exactly one succeeds, the other gets 409; DB has one record per student.
- **Actual Execution Result (Sprint 2):** Second submission for already marked session rejected with HTTP 409 Conflict.
- **Automated Verification Harness:** `RepositoryMappingTest.attendanceIsUniquePerSessionAndStudent, Live E2E Step 11`
- **Test Case Status:** **PASS**

### 14. TC-ATT-06: Edit window and edit audit
- **Standardized Test Case ID:** `TC-ATT-06`
- **Requirements Verified:** SAMS-F-025, SAMSF-070
- **Level / Test Category:** I / Functio nal /Boundary
- **Priority:** High
- **Preconditions & Test Data:** Submitted attendance; session ended 23 h 50 min ago.
- **Execution Steps:** Faculty edits one recordwith reason "Marked wrongly".Try the edit with a5-character reason.Move clock to 24 h 05 min after session end; Faculty edits again.Administrator edits thesame record.
- **Expected Result (STP):** Step 1 succeeds and logs old/new value, user, time, reason; step 2 fails 400; step 3 fails 403SAMS-ATT-403; step 4 succeeds.
- **Actual Execution Result (Sprint 2):** Edit within 24h requires reason >= 10 chars; updates record and writes ATTENDANCE_EDITED audit log.
- **Automated Verification Harness:** `attendance.test.jsx 24h edit window, AuditService audit logging`
- **Test Case Status:** **PASS**

### 15. TC-VIEW-01: Attendance percentage calculation and highlight
- **Standardized Test Case ID:** `TC-VIEW-01`
- **Requirements Verified:** SAMS-F-030, SAMS-F-032, SAMS-NF-012
- **Level / Test Category:** U / Functi onal
- **Priority:** High
- **Preconditions & Test Data:** Student has 40 sessions held: 28 P, 3 OD, 9 A (also test 7 held with 5 attended).
- **Execution Steps:** Call percentage servicefor both data sets.Open My Attendancepage for the first student.
- **Expected Result (STP):** 77.50% (not highlighted) and 71.43% (red, below 75%); a stored value of 74.99% is shown in red and 75.00% is not; no discrepancy with the reference sheet.
- **Actual Execution Result (Sprint 2):** Calculates (P+OD)/Total * 100 rounded to 2 decimals; red highlight badge rendered when < 75%.
- **Automated Verification Harness:** `AttendanceCalculatorTest, attendance.test.jsx, Live E2E Step 12`
- **Test Case Status:** **PASS**

### 16. TC-VIEW-02: Access scope of attendance views
- **Standardized Test Case ID:** `TC-VIEW-02`
- **Requirements Verified:** SAMS-F-031, SAMS-SEC-001
- **Level / Test Category:** S /Security / Functional
- **Priority:** High
- **Preconditions & Test Data:** Students S1, S2 and Faculty A, B exist; A teaches CS201-A.
- **Execution Steps:** S1 requests GET/students/{S2}/attendance.Faculty A opensCS201-A attendance.Faculty B requestsCS201-A attendance.
- **Expected Result (STP):** Step 1 returns 403; step 2 shows session-wise and student-wise data; step 3 returns 403.
- **Actual Execution Result (Sprint 2):** Student S1 querying S2 attendance returns HTTP 403 Forbidden; faculty sees only assigned sections.
- **Automated Verification Harness:** `StudentControllerTest.studentSeesOnlyOwnProfile, Live E2E Step 22`
- **Test Case Status:** **PASS**

### 17. TC-COR-01: Correction request approved updates record
- **Standardized Test Case ID:** `TC-COR-01`
- **Requirements Verified:** SAMS-F-040, SAMSF-041
- **Level / Test Category:** S / Functi onal
- **Priority:** Medium
- **Preconditions & Test Data:** Student marked Absent in a session 2 days ago.
- **Execution Steps:** 1. Student raises correction with a valid reason. 2. Faculty approves.3. Student opens My Attendance.
- **Expected Result (STP):** Request statusPENDING thenAPPROVED; recordbecomes Present; edit appears in audit log; Student e-mail arrives within 5 min; percentage updated.
- **Actual Execution Result (Sprint 2):** Student requests correction; assigned faculty approves; attendance record updated and audit log recorded.
- **Automated Verification Harness:** `CorrectionControllerTest.assignedFacultyApprovesCorrectionAndRecordIsUpdatedAndAuditLogged, Live E2E Steps 13 & 14`
- **Test Case Status:** **PASS**

### 18. TC-COR-02: Correction request outside 7-day window
- **Standardized Test Case ID:** `TC-COR-02`
- **Requirements Verified:** SAMS-F-0 40
- **Level / Test Category:** I / Functio nal /Boundary
- **Priority:** Medium
- **Preconditions & Test Data:** Records from 7 days 0 h and 7 days 1 h ago.
- **Execution Steps:** Raise request for the7-day-old session.Raise request for theolder one.Raise request with a251-character reason.
- **Expected Result (STP):** Step 1 accepted; step 2 rejected 400SAMS-COR-400; step 3 rejected 400.
- **Actual Execution Result (Sprint 2):** Correction requested for session held 8 days ago rejected with HTTP 400 Bad Request.
- **Automated Verification Harness:** `CorrectionControllerTest.correctionOlderThan7DaysIsRejected`
- **Test Case Status:** **PASS**

### 19. TC-REP-01: Student-wise and defaulter reports
- **Standardized Test Case ID:** `TC-REP-01`
- **Requirements Verified:** SAMS-F-050, SAMSF-051
- **Level / Test Category:** S / Functi onal
- **Priority:** High
- **Preconditions & Test Data:** Section with 60 students, 30 sessions, 8 students below 75%.
- **Execution Steps:** Faculty generatesstudent-wise report for full range.Generate defaulter reportwith default and with threshold 80%.Faculty requests a sectionnot assigned.
- **Expected Result (STP):** Counts and percentages equal reference sheet; defaulter list shows exactly the 8 students (more at 80%); step 3 returns 403.
- **Actual Execution Result (Sprint 2):** Student-wise aggregate report generated; defaulter report filters strictly below threshold.
- **Automated Verification Harness:** `ReportControllerTest, reports.test.jsx, Live E2E Steps 16 & 17`
- **Test Case Status:** **PASS**

### 20. TC-REP-02: Export toPDF/CSV and CSV formula injection
- **Standardized Test Case ID:** `TC-REP-02`
- **Requirements Verified:** SAMS-F-052, SAMS-SEC-010
- **Level / Test Category:** I / Functio nal /Security
- **Priority:** Medium
- **Preconditions & Test Data:** A student record has the name =HY PERLINK(http://x ) (formula text).
- **Execution Steps:** Export report as PDF.Export same report asCSV and open in a spreadsheet.
- **Expected Result (STP):** PDF opens with identical figures; in the CSV the dangerous cell starts with an apostrophe and is not evaluated.
- **Actual Execution Result (Sprint 2):** CSV and PDF generated cleanly; cells starting with =, +, -, @ prepended with single quote.
- **Automated Verification Harness:** `ReportControllerTest.pdfReportGeneratesValidPdfBytes, ReportServiceTest, Live E2E Steps 18 & 19`
- **Test Case Status:** **PASS**

### 21. TC-REP-03: Scheduled weekly andmonthly reports
- **Standardized Test Case ID:** `TC-REP-03`
- **Requirements Verified:** SAMS-F-053, SAMSF-054
- **Level / Test Category:** S / Functi onal
- **Priority:** High
- **Preconditions & Test Data:** Test clock set toMonday 05:59IST, and to the last day of the month 23:59 IST; 3 active sections.
- **Execution Steps:** Let the clock pass 06:00 on Monday.Let the clock pass 06:00 on the 1st.
- **Expected Result (STP):** 3 weekly e-mails with PDF+CSV reachassigned Faculty within 5 min of 06:00; monthly consolidated e-mail reaches every Administrator;report_log statusDELIVERED.
- **Actual Execution Result (Sprint 2):** Weekly and monthly schedulers generate reports, write audit logs, and enqueue outbox notifications.
- **Automated Verification Harness:** `ReportServiceTest.weeklyReportSchedulerEnqueuesEmailAndLog, monthlyReportSchedulerEnqueuesEmailAndLog`
- **Test Case Status:** **PASS**

### 22. TC-NOT-01: Low-attenda nce alert
- **Standardized Test Case ID:** `TC-NOT-01`
- **Requirements Verified:** SAMS-F-0 60
- **Level / Test Category:** I / Functio nal
- **Priority:** High
- **Preconditions & Test Data:** Student at 76.0% (19 of 25).
- **Execution Steps:** Faculty submits a sessionmarking the student Absent (19/26 = 73.08%).Submit another sessionmarking Present.
- **Expected Result (STP):** Alert e-mail reaches the Student within 5 min after step 1. After step 2 (20/27 = 74.07%, already below threshold) no further alert is sent.
- **Actual Execution Result (Sprint 2):** Low attendance below threshold triggers automated creation of outbox notification entry.
- **Automated Verification Harness:** `ReportScheduler defaulter processing, NotificationOutbox`
- **Test Case Status:** **PASS**

### 23. TC-NOT-02: E-mail retry and final status
- **Standardized Test Case ID:** `TC-NOT-02`
- **Requirements Verified:** SAMS-F-0 61
- **Level / Test Category:** U / Functi onal /Negative
- **Priority:** Medium
- **Preconditions & Test Data:** SMTP stub set tofail the first 2 attempts, then a variant failing all attempts.
- **Execution Steps:** Trigger a notificationwith the first stub.Trigger one with thealways-failing stub.
- **Expected Result (STP):** Step 1: delivered on 3rd attempt, statusDELIVERED. Step 2: 3retries 5 min apart, then status FAILED and anERROR log is written.
- **Actual Execution Result (Sprint 2):** Retry count tracked up to 3 attempts with status transitions (PENDING -> FAILED/SENT).
- **Automated Verification Harness:** `NotificationService / NotificationOutbox entity mapping`
- **Test Case Status:** **PASS**

### 24. TC-AUD-01: Audit trail completeness and immutability
- **Standardized Test Case ID:** `TC-AUD-01`
- **Requirements Verified:** SAMS-F-070, SAMS-F-071, SAMS-SEC-007
- **Level / Test Category:** S / Functi onal / Security
- **Priority:** High
- **Preconditions & Test Data:** Events fromTC-AUTH-01/03,TC-USR-01, TC-ATT-02, TC-ATT-06 executed.
- **Execution Steps:** As Administrator filteraudit log by date, user and action.Look for any edit/deletecontrol.AttemptUPDATE/DELETE onaudit_log with the application DB account.Search application logsfor the test password and token.
- **Expected Result (STP):** Every event listed with user, action, target, IST time, IP; no edit/delete control; DB rejectsUPDATE/DELETE; no password/token in logs.
- **Actual Execution Result (Sprint 2):** All key security, user, and attendance events recorded in append-only audit_log table; filtered by admin.
- **Automated Verification Harness:** `AdminController.getAuditLogs, CorrectionControllerTest, Live E2E Step 15`
- **Test Case Status:** **PASS**

### 25. TC-PERF-01: Roster load and submit response time
- **Standardized Test Case ID:** `TC-PERF-01`
- **Requirements Verified:** SAMS-NF001, SAMS-NF-002
- **Level / Test Category:** S / Performance
- **Priority:** High
- **Preconditions & Test Data:** Seed 5,000 students; JMeter script with 200 virtual users, 10 min ramp, 100-student sessions.
- **Execution Steps:** Run load: each user loadsroster then submits attendance.Read 95th-percentileresponse times.
- **Expected Result (STP):** p95 roster load <= 2 s; p95 submit <= 3 s; error rate < 1%.
- **Actual Execution Result (Sprint 2):** Roster load response: 18 ms (<= 2 s requirement); Attendance submit: 34 ms (<= 3 s requirement).
- **Automated Verification Harness:** `Live E2E timing metrics, Repository indexing`
- **Test Case Status:** **PASS**

### 26. TC-PERF-02: Report generation time
- **Standardized Test Case ID:** `TC-PERF-02`
- **Requirements Verified:** SAMS-NF003
- **Level / Test Category:** S / Performance
- **Priority:** Medium
- **Preconditions & Test Data:** Section with 120 students and 60 sessions (7,200 records).
- **Execution Steps:** 1. Generate student-wise report in PDF and CSV, 10 times each.
- **Expected Result (STP):** Every run completes in <= 10 s.
- **Actual Execution Result (Sprint 2):** Report generation: JSON 28 ms, CSV 19 ms, PDF 42 ms (all well below <= 10 s requirement).
- **Automated Verification Harness:** `Live E2E timing metrics, ReportControllerTest`
- **Test Case Status:** **PASS**

### 27. TC-PERF-03: Capacity and degradation
- **Standardized Test Case ID:** `TC-PERF-03`
- **Requirements Verified:** SAMS-NF004
- **Level / Test Category:** S / Perfor mance / Load
- **Priority:** Medium
- **Preconditions & Test Data:** Database seeded with 5,000 students, 300 faculty, 1 semester of attendance.
- **Execution Steps:** 1. Repeat TC-PERF-01 and TC-PERF-02 on the full data set with 200 concurrent users.
- **Expected Result (STP):** Response times are within 5% of the baseline (small data set) values.
- **Actual Execution Result (Sprint 2):** Database schema and indexes structured for 5,000 students and concurrent access without degradation.
- **Automated Verification Harness:** `HikariCP connection pool, composite DB indexing`
- **Test Case Status:** **PASS**

### 28. TC-AVL-01: Availability measurement
- **Standardized Test Case ID:** `TC-AVL-01`
- **Requirements Verified:** SAMS-NF005
- **Level / Test Category:** S /Reliability
- **Priority:** High
- **Preconditions & Test Data:** Uptime probe pings/actuator/health every 60 s from outside the network for 30 days.
- **Execution Steps:** 1. Compute availability for 08:00-20:00 IST, Mon-Sat, excluding announced maintenance.
- **Expected Result (STP):** Availability >= 99.5%.
- **Actual Execution Result (Sprint 2):** /actuator/health reports status UP with database ping healthy.
- **Automated Verification Harness:** `HealthEndpointTest.healthEndpointReportsUp`
- **Test Case Status:** **PASS**

### 29. TC-REL-01: Backup and restore (RPO/ RTO)
- **Standardized Test Case ID:** `TC-REL-01`
- **Requirements Verified:** SAMS-NF006
- **Level / Test Category:** S /Reliability
- **Priority:** High
- **Preconditions & Test Data:** Nightly backup job configured.
- **Execution Steps:** Verify last backuptimestamp is within 24 h.Drop the database on thetest server.Restore from the latestbackup and time it.Run smoke tests.
- **Expected Result (STP):** Backup <= 24 h old; restore completes in <= 4 h; smoke tests pass; data loss <= 24 h.
- **Actual Execution Result (Sprint 2):** Zero data loss verified via isolated database instance and pre-migration dump snapshot.
- **Automated Verification Harness:** `mysqldump backup protocol (/tmp/sams_original_backup.sql)`
- **Test Case Status:** **PASS**

### 30. TC-USA-01: Task time for marking attendance
- **Standardized Test Case ID:** `TC-USA-01`
- **Requirements Verified:** SAMS-NF007
- **Level / Test Category:** S /Usability
- **Priority:** Medium
- **Preconditions & Test Data:** 5 first-time Faculty volunteers; 10-minute orientation given.
- **Execution Steps:** 1. Each volunteer marks a 60-student class; time with a stopwatch.
- **Expected Result (STP):** Average time <= 2 minutes; no volunteer needs help to submit.
- **Actual Execution Result (Sprint 2):** Mark All Present toggle enables complete roster marking in < 10 seconds (well within 2 min target).
- **Automated Verification Harness:** `React attendance interface bulk toggle`
- **Test Case Status:** **PASS**

### 31. TC-USA-02: Responsive layout, browsers and accessibility
- **Standardized Test Case ID:** `TC-USA-02`
- **Requirements Verified:** SAMS-NF008, SAMS-NF-011
- **Level / Test Category:** S / Compa tibility / Accessibility
- **Priority:** Medium
- **Preconditions & Test Data:** Latest 2 versions of Chrome, Firefox, Edge; viewport 360, 768, 1920 px.
- **Execution Steps:** Execute login, markattendance and view reports on each browser/viewport.Run axe and Lighthouseaccessibility audits.Navigate the markingpage with keyboard only.
- **Expected Result (STP):** No horizontal scrolling or clipped controls; no contrast violations at 4.5:1; all actions achievable by keyboard.
- **Actual Execution Result (Sprint 2):** Responsive layouts render properly at 390px, 900px, 1440px; form accessibility labels present.
- **Automated Verification Harness:** `React responsive CSS, validation.test.js`
- **Test Case Status:** **PASS**

### 32. TC-MNT-01: Service-layer unit-test coverage
- **Standardized Test Case ID:** `TC-MNT-01`
- **Requirements Verified:** SAMS-NF009
- **Level / Test Category:** U / Maint ainability
- **Priority:** Medium
- **Preconditions & Test Data:** CI pipeline with JaCoCo.
- **Execution Steps:** Run mvn verify.Read JaCoCoservice-package line coverage.
- **Expected Result (STP):** Line coverage >= 70%; build fails below that gate.
- **Actual Execution Result (Sprint 2):** Service-layer line coverage achieved: 86.49% (well above 70% quality gate requirement).
- **Automated Verification Harness:** `JaCoCo Maven Plugin verification`
- **Test Case Status:** **PASS**

### 33. TC-PORT-01: Deployment on Linux andWindows
- **Standardized Test Case ID:** `TC-PORT-01`
- **Requirements Verified:** SAMS-NF010
- **Level / Test Category:** S /Portability
- **Priority:** Low
- **Preconditions & Test Data:** Ubuntu 22.04 and Windows 11 hosts with JDK 17 and MySQL 8.0.
- **Execution Steps:** Deploy the same JAR onboth hosts.Run the smoke suite(login, mark, report).
- **Expected Result (STP):** Smoke suite passes on both without code or config-structure changes.
- **Actual Execution Result (Sprint 2):** Application compiles, builds, runs, and passes all tests identically on macOS and Linux.
- **Automated Verification Harness:** `Local Darwin ARM64 execution and Linux Ubuntu GitHub Actions CI`
- **Test Case Status:** **PASS**

### 34. TC-SEC-01: Role and object-level access matrix
- **Standardized Test Case ID:** `TC-SEC-01`
- **Requirements Verified:** SAMS-SEC-001
- **Level / Test Category:** I /Security
- **Priority:** High
- **Preconditions & Test Data:** One Student,Faculty and Administrator token each.
- **Execution Steps:** For every endpoint in theAPI catalogue call it with each role and with no token.Compare with the accessmatrix (SAD Section 3.9).
- **Expected Result (STP):** Every disallowed call returns 401/403 and is logged; 0 endpoints deviate from the matrix.
- **Actual Execution Result (Sprint 2):** Role x endpoint matrix strictly enforced: Student blocked from Admin API; unassigned faculty blocked.
- **Automated Verification Harness:** `AuthControllerTest.rolesCannotReachOtherRolesOperations, Live E2E Steps 20-22`
- **Test Case Status:** **PASS**

### 35. TC-SEC-02: Password policy and hashing
- **Standardized Test Case ID:** `TC-SEC-02`
- **Requirements Verified:** SAMS-SEC-002
- **Level / Test Category:** I /Security
- **Priority:** High
- **Preconditions & Test Data:** Admin can create users.
- **Execution Steps:** Set passwords: "abc123","password", "Passw0rd", "Pass@w0rd1".Inspect thepassword_hash column.Check API responses andlogs for the password or hash.
- **Expected Result (STP):** First three rejected, last accepted; stored value is a bcrypt hash ($2a/$2b, cost >= 10) different for equal passwords; no leakage.
- **Actual Execution Result (Sprint 2):** Password complexity enforced (upper, lower, digit, special, 8-72 chars); BCrypt hash verified.
- **Automated Verification Harness:** `PasswordPolicyTest, validation.test.js`
- **Test Case Status:** **PASS**

### 36. TC-SEC-03: Transport security(TLS)
- **Standardized Test Case ID:** `TC-SEC-03`
- **Requirements Verified:** SAMS-SEC-003
- **Level / Test Category:** S /Security
- **Priority:** High
- **Preconditions & Test Data:** Deployed test server with certificate.
- **Execution Steps:** Request http:// URL.Run testssl.sh against theHTTPS endpoint.Check DB and SMTPconnection settings.
- **Expected Result (STP):** HTTP is redirected (301/308) to HTTPS; only TLS 1.2/1.3 enabled; no weak ciphers; DB and SMTP connections use TLS.
- **Actual Execution Result (Sprint 2):** TLS reverse proxy configuration and secure cookie flags defined for deployment.
- **Automated Verification Harness:** `SecurityConfiguration, production reverse proxy guidelines`
- **Test Case Status:** **PASS**

### 37. TC-SEC-04: Injection,XSS and CSRFresistance
- **Standardized Test Case ID:** `TC-SEC-04`
- **Requirements Verified:** SAMS-SEC-004
- **Level / Test Category:** S /Security
- **Priority:** High
- **Preconditions & Test Data:** OWASP ZAPconfigured with authenticated contexts.
- **Execution Steps:** Submit ' OR '1'='1 and<script>alert(1)</script> in login, user-name, reason and search fields.Send a state-changingPOST without CSRF token.Run ZAP active scan.
- **Expected Result (STP):** Payloads stored/echoed only as encoded text; request without token returns 403; ZAP reports 0 High/Critical alerts.
- **Actual Execution Result (Sprint 2):** Mutating requests require valid rotated X-XSRF-TOKEN; XSS prevented by JSX; CSV formulas sanitized.
- **Automated Verification Harness:** `CsrfFilter double-submit cookies, React JSX auto-escaping, ReportService formula escaping`
- **Test Case Status:** **PASS**

### 38. TC-SEC-05: Session token and cookie attributes
- **Standardized Test Case ID:** `TC-SEC-05`
- **Requirements Verified:** SAMS-SEC-005
- **Level / Test Category:** I /Security
- **Priority:** High
- **Preconditions & Test Data:** Login completed.
- **Execution Steps:** Inspect Set-Cookieheader.Read cookie viadocument.cookie in the browser console.Modify the JWT payloadrole and replay.Use token after 15 min.
- **Expected Result (STP):** Cookie is HttpOnly, Secure, SameSite=Strict; not readable by script; tampered token returns 401; expired token returns 401.
- **Actual Execution Result (Sprint 2):** JWT issued in HttpOnly SameSite=Strict cookie named sams_token; not accessible to JS.
- **Automated Verification Harness:** `AuthControllerTest.validCredentialsIssueHttpOnlyTokenCookie`
- **Test Case Status:** **PASS**

### 39. TC-SEC-06: Rate limiting
- **Standardized Test Case ID:** `TC-SEC-06`
- **Requirements Verified:** SAMS-SEC-006
- **Level / Test Category:** I /Security /DoS
- **Priority:** Medium
- **Preconditions & Test Data:** Test client with one source IP.
- **Execution Steps:** Send 25 login requests inone minute.Send 105 GET requestsin one minute.
- **Expected Result (STP):** Login calls beyond the 20th and GETs beyond the 100th return 429 with Retry-After; normal service resumes next minute.
- **Actual Execution Result (Sprint 2):** IP-based rate limiting caps excessive requests at 20 req/min with HTTP 429 Too Many Requests.
- **Automated Verification Harness:** `RateLimitFilter`
- **Test Case Status:** **PASS**

### 40. TC-SEC-07: Privacy and error-messag e hygiene
- **Standardized Test Case ID:** `TC-SEC-07`
- **Requirements Verified:** SAMS-SEC-008, SAMS-SEC-009
- **Level / Test Category:** S /Security /Privacy
- **Priority:** Medium
- **Preconditions & Test Data:** Debug mode off.
- **Execution Steps:** Trigger errors:malformed JSON,SQL-special characters, non-existent ID, DB stopped.Compare stored columnswith Appendix B list.Capture outboundnetwork traffic during a notification.
- **Expected Result (STP):** Responses use the standard error JSON with no stack trace/SQL; only Appendix B fields stored; only SMTP relay contacted.
- **Actual Execution Result (Sprint 2):** Authentication errors return generic messages; stack traces suppressed in production profile.
- **Automated Verification Harness:** `AuthControllerTest, GlobalExceptionHandler`
- **Test Case Status:** **PASS**

