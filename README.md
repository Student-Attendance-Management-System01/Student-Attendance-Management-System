# Student Attendance Management System (SAMS)

Team 1 · Software Engineering mini-project · PES University, Dept. of CSE

SAMS lets administrators set up users, course sections and rosters, lets
faculty run class sessions and mark attendance, and lets students track their
own attendance and request corrections. Reports (student-wise and defaulter)
can be exported as CSV or PDF.

| Part | Stack | Docs |
|---|---|---|
| Backend API | Java 17, Spring Boot 3.3, Spring Security (JWT in HttpOnly cookie + CSRF), MySQL 8, Flyway | [`backend/README.md`](backend/README.md) |
| Frontend | React 18, Vite 5, React Router 6, axios | [`frontend/README.md`](frontend/README.md) |
| Testing | JUnit 5 (backend), Vitest + Testing Library (frontend) | [`docs/testing/`](docs/testing/README.md) |
| Design | SRS, SAD, STP | [`Part-1 Deliverables/`](Part-1%20Deliverables) |

## Repository layout

```
backend/                Spring Boot API (controllers, services, entities, Flyway migrations)
frontend/               React single-page app
docs/testing/           test guide, security regression record, traceability, known issues
Part-1 Deliverables/    SRS, SAD and Software Test Plan
.github/workflows/      CI: backend (mvn verify) and frontend (tests + build)
```

## Run it locally

Prerequisites: JDK 17, Maven 3.9+, MySQL 8, Node.js 20+.

1. **Database** – create an empty `sams` database and a user that can create tables in it.
2. **Configuration** – `cp .env.example .env` and fill in the values (never commit `.env`).
3. **Backend** – in one terminal:
   ```bash
   cd backend
   mvn spring-boot:run        # API on http://localhost:8080
   ```
4. **Frontend** – in another terminal:
   ```bash
   cd frontend
   npm ci
   npm run dev                # app on http://localhost:5173, /api is proxied to :8080
   ```
5. Open http://localhost:5173 and sign in with an account created by an administrator.

## Run the tests

```bash
cd backend  && mvn verify     # backend unit + repository tests
cd frontend && npm test       # frontend route, role and workflow tests
```

CI runs both on every pull request to `main`.

## Branches and pull requests

Work happens on `sprintN/issue-<number>-<short-name>` branches. Open a pull
request into `main`; only the repository owner merges.
