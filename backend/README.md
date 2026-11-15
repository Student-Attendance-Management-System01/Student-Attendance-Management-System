# SAMS backend

Spring Boot 3.3 (Java 17) backend for the Student Attendance Management System. The database schema is managed by Flyway (`src/main/resources/db/migration`) against MySQL 8.

## Package structure

```
edu.pesu.sams
├── config       application configuration beans (e.g. the Asia/Kolkata clock)
├── entity       JPA entities and enums
├── repository   Spring Data JPA repositories
└── exception    API error type and global JSON error handler
```

Controllers, services and DTOs are added by the feature issues that follow (#4 onwards).

## Prerequisites

- JDK 17 or newer
- Maven 3.9+
- MySQL 8 with an empty `sams` database and a user that can create tables in it

## Configuration

Machine-specific values and secrets come from environment variables; nothing secret is committed. Copy `../.env.example` to `.env` and fill in your values.

| Variable | Required | Default | Purpose |
|---|---|---|---|
| `SAMS_DB_URL` | no | `jdbc:mysql://localhost:3306/sams?...` | JDBC URL |
| `SAMS_DB_USERNAME` | no | `sams` | Database user |
| `SAMS_DB_PASSWORD` | yes | (empty) | Database password |
| `SERVER_PORT` | no | `8080` | HTTP port |
| `SAMS_BOOTSTRAP_ADMIN_EMAIL` | no | (empty) | Email for initial administrator account |
| `SAMS_BOOTSTRAP_ADMIN_PASSWORD` | no | (empty) | Password for initial administrator account |
| `SAMS_BOOTSTRAP_ADMIN_ID` | no | `ADM001` | Institution ID for initial administrator |
| `SAMS_BOOTSTRAP_ADMIN_NAME` | no | `SAMS Administrator` | Full name for initial administrator |

## Build and test

```bash
cd backend
mvn verify
```

The tests use an in-memory H2 database, so they do not need MySQL.

## Run locally

```bash
cd backend
mvn spring-boot:run
```

On first start Flyway applies `V1__initial_schema.sql` to the empty database. Hibernate then validates (`ddl-auto: validate`) that the entities match the schema; it never changes the database itself.

## Health endpoint

```bash
curl http://localhost:8080/actuator/health
```

Expected response (HTTP 200):

```json
{"status":"UP", "groups":["liveness","readiness"]}
```

## Data model

| Table | Purpose | Key constraints |
|---|---|---|
| `users` | Admin, faculty and student accounts (`role`) | unique `institution_id`, unique `email` |
| `course_sections` | A course offering for a semester/section/year | unique (`course_code`, `semester`, `section_name`, `academic_year`) |
| `course_faculty` | Faculty assigned to a course section | FKs to both, unique pair |
| `enrolments` | Students enrolled in a course section | FKs, unique (`course_section_id`, `student_user_id`) |
| `class_sessions` | A lecture/lab/tutorial held for a section | FK to section and creator, index on (`course_section_id`, `session_date`) |
| `attendance_records` | PRESENT / ABSENT / OD per student per session | FKs, unique (`session_id`, `student_user_id`) |
| `correction_requests` | Student requests to correct a record | FKs, index on (`status`, `requested_at`) |
| `audit_log` | Who changed what and when | indexes on time, actor, action |
| `password_reset_tokens`, `revoked_tokens` | Authentication support | unique token hash / JTI |
| `notification_outbox`, `report_log`, `app_settings` | E-mail outbox, report history, settings such as attendance threshold | unique `setting_key` |

Timestamps are stored as `DATETIME(6)` in the Asia/Kolkata time zone, and enum values (roles, statuses, session types) as strings. Schema changes must be made as new Flyway migrations (`V2__...sql`), never by editing an applied migration or the database by hand.
