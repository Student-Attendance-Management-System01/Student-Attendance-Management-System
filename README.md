# Student Attendance Management System (SAMS)

**Team 1 · Software Engineering Project · PES University, Department of Computer Science & Engineering**

SAMS is a full-stack, enterprise-grade attendance management solution designed for academic institutions. It enables administrators to manage user accounts, course sections, and class rosters; faculty instructors to schedule class sessions and record attendance; and students to track their attendance percentage in real time and submit correction requests. Comprehensive analytics reports (student-wise aggregations and defaulter threshold lists) can be viewed in-browser or exported as CSV and PDF documents.

---

## 1. Architectural Stack & Specifications

| Component | Technology Stack | Documentation |
|---|---|---|
| **Backend API** | Java 17 (Temurin), Spring Boot 3.3.5, Spring Security 6 (JWT in HttpOnly cookies + CSRF protection), MySQL 8.0+, Flyway, Hibernate 6.5 | [`backend/README.md`](backend/README.md) |
| **Frontend** | React 18, Vite 5, React Router 6, Axios | [`frontend/README.md`](frontend/README.md) |
| **Testing** | JUnit 5, Mockito, Spring Boot Test (Backend); Vitest 2, React Testing Library (Frontend) | [`docs/testing/README.md`](docs/testing/README.md) |
| **System Specifications** | IEEE 830 SRS, IEEE 1016 SAD, IEEE 829 Software Test Plan | [`Part-1 Deliverables/`](Part-1%20Deliverables) |
| **Verification Reports** | Unit Test Report, System Validation Report, JaCoCo Coverage Reports (Line & Branch), Traceability Matrix | [`docs/testing/`](docs/testing) |
| **Demonstration Media** | Sprint 1 & Sprint 2 video demonstrations (Apple QuickTime .mov, Git LFS) | [`docs/demos/`](docs/demos) |

---

## 2. Repository Layout

```
.
├── backend/                Spring Boot API source, entities, controllers, services, migrations
│   ├── src/main/resources/db/migration/  Flyway schema migrations (V1__initial_schema.sql)
│   └── pom.xml             Maven dependencies and JaCoCo coverage configuration
├── frontend/               React single-page application
│   ├── src/pages/          Views (Login, Dashboard, Admin, Faculty, Student, Reports, Audit)
│   └── vite.config.js      Vite server configuration and backend proxy definitions
├── docs/
│   ├── demos/              Sprint 1 and Sprint 2 video recordings (Git LFS)
│   └── testing/            Part 3 IEEE 829 test documentation, coverage reports, RTM, known issues
├── Part-1 Deliverables/    Formal SRS, SAD, and STP specification documents
└── .github/workflows/      Integrated CI workflow for automated verification and test retention
```

---

## 3. Prerequisites & Supported Versions

Ensure your local development environment has the following software installed:

- **Java Development Kit (JDK):** Version 17 (Eclipse Temurin or OpenJDK 17 recommended)
- **Apache Maven:** Version 3.9+
- **Node.js & npm:** Node.js v20.x+ (LTS) and npm 10+
- **MySQL Database Server:** Version 8.0+
- **Git & Git LFS:** Git 2.30+ with Git LFS installed (`git lfs install`)

---

## 4. Setup and Installation Guide

### Step 1: Database Initialization (MySQL)

Create an empty database schema and a dedicated user on your local MySQL server:

```sql
CREATE DATABASE sams CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sams'@'localhost' IDENTIFIED BY 'sams_password';
GRANT ALL PRIVILEGES ON sams.* TO 'sams'@'localhost';
FLUSH PRIVILEGES;
```

> **Automatic Schema Migration:** You do not need to manually execute SQL scripts or create tables. On backend startup, **Flyway** automatically detects the empty database and executes `V1__initial_schema.sql`, initializing all 14 tables, composite foreign keys, and indexes. Hibernate validates this schema at runtime (`ddl-auto: validate`).

---

### Step 2: Environment Configuration

Copy the example environment file to `.env`:

```bash
cp .env.example .env
```

Open `.env` and configure your local database credentials and initial administrator account:

```bash
# Backend Database Connection (MySQL 8)
SAMS_DB_URL=jdbc:mysql://localhost:3306/sams?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
SAMS_DB_USERNAME=sams
SAMS_DB_PASSWORD=sams_password

# Optional: HTTP Port for Spring Boot API (defaults to 8080)
SERVER_PORT=8080

# Initial Administrator Account Bootstrapping (Applied on first startup when database has no users)
SAMS_BOOTSTRAP_ADMIN_EMAIL=admin@pes.edu
SAMS_BOOTSTRAP_ADMIN_PASSWORD=REPLACE_WITH_STRONG_ADMIN_PASSWORD
SAMS_BOOTSTRAP_ADMIN_ID=ADM001
SAMS_BOOTSTRAP_ADMIN_NAME=SAMS Administrator
```

> **Security Note:** Never commit your active `.env` file to version control. The `.gitignore` file is pre-configured to exclude all `.env` files.

---

### Step 3: Start the Backend API Server

In a new terminal window, navigate to the `backend` directory and start Spring Boot:

```bash
cd backend
mvn spring-boot:run
```

Once started, the backend API will listen on `http://localhost:8080`. You can confirm server health by querying the actuator endpoint:

```bash
curl http://localhost:8080/actuator/health
# Expected JSON response: {"status":"UP","groups":["liveness","readiness"]}
```

---

### Step 4: Start the Frontend Development Server

In another terminal window, navigate to the `frontend` directory, install dependencies, and launch Vite:

```bash
cd frontend
npm ci
npm run dev
```

The frontend application will be accessible at **`http://localhost:5173`**.

#### Non-Default Backend Port Configuration (`SAMS_API_PROXY`):
If your backend is configured to listen on a port other than `8080` (for example, port `8082` to avoid local port conflicts), configure the Vite proxy by setting `SAMS_API_PROXY`:

```bash
# Option A: Command line export before running dev server
export SAMS_API_PROXY=http://localhost:8082
npm run dev

# Option B: Local environment override in frontend/.env.local
echo "SAMS_API_PROXY=http://localhost:8082" > frontend/.env.local
```

---

### Step 5: Access the Application

1. Open your web browser and navigate to **`http://localhost:5173`**.
2. Sign in using the bootstrapped administrator credentials configured in your `.env` (e.g. `admin@pes.edu` / your chosen strong password).
3. As an administrator, you can now:
   - Create student and faculty accounts under **User accounts** (`/admin/users`).
   - Create course sections and enrol students under **Courses & rosters** (`/admin/courses`).
   - Inspect immutable audit records under **Audit log** (`/audit`).
   - View real-time attendance analytics and download reports under **Reports** (`/reports`).
4. Faculty instructors and students can sign in using the accounts created by the administrator to mark attendance, review percentage statistics, and request corrections.

---

## 5. Testing & Quality Verification

### Run Backend Unit, Integration & JaCoCo Coverage Tests

```bash
cd backend
mvn clean verify
```

- Executes 61 JUnit 5 unit and MockMvc integration tests.
- Measures JaCoCo line and branch coverage (Service layer quality gate: $\ge 70\%$).
- Generates HTML reports in `backend/target/site/jacoco/index.html`.

### Run Frontend Component & Route Tests

```bash
cd frontend
npm test
```

- Executes 61 Vitest tests across authentication, routing, validation, management screens, and reporting.

### Build Production Frontend

To compile the production-ready React client bundle:

```bash
cd frontend
npm run build
```

- Produces minified, optimized HTML, CSS, and JavaScript in `frontend/dist/`.
- All Google fonts are bundled locally via `@fontsource` with zero third-party CDN requests.

---

## 6. Continuous Integration (CI)

The GitHub Actions CI pipeline (`.github/workflows/main.yml`) runs on all pushes and pull requests targeting `main`. It validates:
- OpenJDK 17 setup and Maven caching.
- Backend compilation, unit tests, and integration tests.
- JaCoCo coverage reports generation.
- Node.js 20 setup and dependency installation.
- Frontend test suite execution (`vitest`).
- Production frontend bundle build.
- Uploads and retains Surefire XML test reports, JaCoCo reports, and frontend build artifacts for 14 days.
