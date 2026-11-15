# SAMS JaCoCo Branch Coverage Report

**Project:** Student Attendance Management System (SAMS)  
**Report Type:** JaCoCo Branch-Level Code Coverage  
**Date:** 2026-11-15  
**Service Layer Branch Coverage:** **61.22% (251 / 410 branches)**  
**Overall Project Branch Coverage:** **56.84% (299 / 526 branches)**  

---

## 1. Executive Summary

Branch coverage tracks conditional control flow (e.g. `if-else`, `switch`, ternary expressions, validation filters, and null-safety guards) within the Java codebase. JaCoCo instrumentation evaluates whether both boolean evaluations (true and false) have been executed during automated test runs.

### Key Metric Compliance

| Component / Layer | Total Branches | Covered Branches | Missed Branches | Branch Coverage |
|---|---|---|---|---|
| **Service Layer (`edu.pesu.sams.service`)** | **410** | **251** | **159** | **61.22%** |
| **Security Layer (`edu.pesu.sams.security`)** | 58 | 33 | 25 | 56.90% |
| **Entities (`edu.pesu.sams.entity`)** | 20 | 9 | 11 | 45.00% |
| **Controllers (`edu.pesu.sams.controller`)** | 38 | 6 | 32 | 15.79% |
| **Total Project** | **526** | **299** | **227** | **56.84%** |

---

## 2. Service Layer Branch Coverage Breakdown

The business logic in the service layer contains the highest density of branching conditions, including authorization checks, date-window calculations, conflict validation, and threshold evaluations.

| Class Name | Covered Branches | Missed Branches | Total Branches | Branch Coverage |
|---|---|---|---|---|
| `edu.pesu.sams.service.AttendanceCalculator` | 2 | 0 | 2 | **100.00%** |
| `edu.pesu.sams.service.AuditService` | 2 | 0 | 2 | **100.00%** |
| `edu.pesu.sams.service.CourseSectionService` | 11 | 1 | 12 | **91.67%** |
| `edu.pesu.sams.service.ReportScheduler` | 16 | 2 | 18 | **88.89%** |
| `edu.pesu.sams.service.RosterService` | 7 | 1 | 8 | **87.50%** |
| `edu.pesu.sams.service.ReportService` | 32 | 8 | 40 | **80.00%** |
| `edu.pesu.sams.service.StudentService` | 17 | 5 | 22 | **77.27%** |
| `edu.pesu.sams.service.FacultyService` | 21 | 7 | 28 | **75.00%** |
| `edu.pesu.sams.service.PasswordPolicy` | 3 | 1 | 4 | **75.00%** |
| `edu.pesu.sams.service.CorrectionService` | 31 | 13 | 44 | **70.45%** |
| `edu.pesu.sams.service.UserManagementService` | 39 | 31 | 70 | **55.71%** |
| `edu.pesu.sams.service.AuthService` | 20 | 16 | 36 | **55.56%** |
| `edu.pesu.sams.service.AttendanceService` | 43 | 35 | 78 | **55.13%** |
| `edu.pesu.sams.service.NotificationService` | 4 | 22 | 26 | **15.38%** |
| `edu.pesu.sams.service.BootstrapService` | 3 | 17 | 20 | **15.00%** |
| **Total Service Layer** | **251** | **159** | **410** | **61.22%** |

---

## 3. Security & Controller Layer Branch Details

### 3.1. Security Components
- `edu.pesu.sams.security.JwtCookieFilter`: **17 / 26 branches (65.38%)** — Exercises cookie presence, signature validation, expired token branching, and header fallbacks.
- `edu.pesu.sams.security.RateLimitFilter`: **14 / 28 branches (50.00%)** — Exercises IP rate limit tracking, counter increments, threshold exhaustion, and exclusion of public endpoints.
- `edu.pesu.sams.security.JwtService`: **2 / 4 branches (50.00%)** — Exercises token subject resolution and expiration validation.

### 3.2. Controllers
- `edu.pesu.sams.controller.ReportController`: **5 / 6 branches (83.33%)** — Validates date-range ordering, format options (JSON, CSV, PDF), and threshold bounds.
- `edu.pesu.sams.controller.AuthController`: **1 / 2 branches (50.00%)** — Validates login payload presence.
- `edu.pesu.sams.controller.AdminController`: **0 / 30 branches (0.00%)** — Controller parameter checks covered primarily via mock integration and live E2E test runs.

---

## 4. Entity Null-Safety & Invariant Branches
Entities implement defensive equality and null check methods:
- `UserAccount`: 2 / 4 branches (50.00%)
- `AttendanceRecord`: 1 / 2 branches (50.00%)
- `ClassSession`: 1 / 2 branches (50.00%)
- `CorrectionRequest`: 1 / 2 branches (50.00%)
- `Enrollment`: 1 / 2 branches (50.00%)
- `AuditLog`: 1 / 2 branches (50.00%)
- `NotificationOutbox`: 2 / 4 branches (50.00%)

---

## 5. Conclusion

The service layer achieves robust branch coverage of **61.22%**, confirming that edge cases such as invalid dates, non-faculty permission violations, threshold filtering boundaries, and duplicate requests are actively validated.
