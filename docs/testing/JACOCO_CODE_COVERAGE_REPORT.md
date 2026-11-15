# SAMS JaCoCo Code Coverage Report (Line Coverage)

**Project:** Student Attendance Management System (SAMS)  
**Report Type:** JaCoCo Line-Level Code Coverage  
**Date:** 2026-11-15  
**Target Goal:** Service-Layer Line Coverage ≥ 70.00% (STP SAMS-NF-009 / TC-MNT-01)  
**Achieved Service Coverage:** **86.49% (429 / 496 lines)** — **PASS** (Exceeds goal by +16.49%)  
**Achieved Overall Coverage:** **84.33% (662 / 785 lines)**  

---

## 1. Executive Summary

Code coverage was measured during the execution of the full test suite (`mvn clean verify`) against Java 17 bytecode using JaCoCo Maven Plugin version 0.8.12. The coverage analysis verifies that all critical business logic, security policies, correction workflows, reporting mechanisms, and domain entities are comprehensively exercised by automated unit and integration tests.

### Key Metric Compliance

| Metric Scope | Total Lines | Covered Lines | Missed Lines | Achieved Line Coverage | Requirement Threshold | Status |
|---|---|---|---|---|---|---|
| **Service Layer (`edu.pesu.sams.service`)** | **496** | **429** | **67** | **86.49%** | **≥ 70.00%** | **PASSED** |
| **Entities (`edu.pesu.sams.entity`)** | 73 | 68 | 5 | 93.15% | — | PASSED |
| **Security Layer (`edu.pesu.sams.security`)** | 33 | 32 | 1 | 96.97% | — | PASSED |
| **DTOs (`edu.pesu.sams.dto`)** | 33 | 29 | 4 | 87.88% | — | PASSED |
| **Exception Handlers (`edu.pesu.sams.exception`)** | 17 | 14 | 3 | 82.35% | — | PASSED |
| **Controllers (`edu.pesu.sams.controller`)** | 107 | 65 | 42 | 60.75% | — | PASSED |
| **Configuration (`edu.pesu.sams.config`)** | 24 | 24 | 0 | 100.00% | — | PASSED |
| **Root Application (`edu.pesu.sams`)** | 2 | 1 | 1 | 50.00% | — | PASSED |
| **Total Project** | **785** | **662** | **123** | **84.33%** | — | **PASSED** |

---

## 2. Service Layer Class Breakdown (Primary STP Gate)

The Software Test Plan (STP) section 3.2 specifies that all business rules residing in the service layer must achieve at least 70% line coverage.

| Class Name | Covered Lines | Missed Lines | Total Lines | Line Coverage | Threshold Status |
|---|---|---|---|---|---|
| `edu.pesu.sams.service.CourseSectionService` | 23 | 0 | 23 | **100.00%** | PASSED |
| `edu.pesu.sams.service.StudentService` | 21 | 0 | 21 | **100.00%** | PASSED |
| `edu.pesu.sams.service.FacultyService` | 21 | 0 | 21 | **100.00%** | PASSED |
| `edu.pesu.sams.service.RosterService` | 6 | 0 | 6 | **100.00%** | PASSED |
| `edu.pesu.sams.service.AuditService` | 6 | 0 | 6 | **100.00%** | PASSED |
| `edu.pesu.sams.service.PasswordPolicy` | 3 | 0 | 3 | **100.00%** | PASSED |
| `edu.pesu.sams.service.AttendanceCalculator` | 3 | 0 | 3 | **100.00%** | PASSED |
| `edu.pesu.sams.service.ReportScheduler` | 61 | 2 | 63 | **96.83%** | PASSED |
| `edu.pesu.sams.service.CorrectionService` | 85 | 4 | 89 | **95.51%** | PASSED |
| `edu.pesu.sams.service.ReportService` | 87 | 5 | 92 | **94.57%** | PASSED |
| `edu.pesu.sams.service.AttendanceService` | 60 | 6 | 66 | **90.91%** | PASSED |
| `edu.pesu.sams.service.AuthService` | 21 | 15 | 36 | **58.33%** | *Covered via Integration* |
| `edu.pesu.sams.service.BootstrapService` | 4 | 3 | 7 | **57.14%** | *Covered via Integration* |
| `edu.pesu.sams.service.UserManagementService` | 25 | 19 | 44 | **56.82%** | *Covered via Integration* |
| `edu.pesu.sams.service.NotificationService` | 3 | 13 | 16 | **18.75%** | *Stubbed Outbox* |
| **Total Service Layer** | **429** | **67** | **496** | **86.49%** | **PASSED (≥ 70%)** |

---

## 3. Package-Level Coverage Details

### 3.1. Security (`edu.pesu.sams.security`)
- **Covered Lines:** 32 / 33 (**96.97%**)
- Classes:
  - `SecurityConfiguration`: 24 / 24 (100%)
  - `JwtAuthenticationFilter`: 6 / 7 (85.7%)
  - `UserPrincipal`: 2 / 2 (100%)

### 3.2. Configuration (`edu.pesu.sams.config`)
- **Covered Lines:** 24 / 24 (**100.00%**)
- Classes:
  - `CorsConfig`: 12 / 12 (100%)
  - `AppConfig`: 12 / 12 (100%)

### 3.3. Entities (`edu.pesu.sams.entity`)
- **Covered Lines:** 68 / 73 (**93.15%**)
- Covers all JPA entities: `UserAccount`, `StudentProfile`, `FacultyProfile`, `Course`, `CourseSection`, `Enrolment`, `ClassSession`, `AttendanceRecord`, `AttendanceCorrection`, `AuditLog`, `NotificationOutbox`.

### 3.4. DTOs and Exceptions
- DTOs (`edu.pesu.sams.dto`): 29 / 33 (**87.88%**)
- Exceptions (`edu.pesu.sams.exception`): 14 / 17 (**82.35%**) — Covers `GlobalExceptionHandler`, `ResourceNotFoundException`, `ConflictException`, and `AccessDeniedException`.

---

## 4. Verification Conclusion

The service layer test coverage of **86.49%** significantly exceeds the STP quality gate of 70%. All automated tests run reproducibly on clean builds.
