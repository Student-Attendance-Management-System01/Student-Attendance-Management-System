# SAMS frontend

React 18 + Vite single-page app for the Student Attendance Management System.
It talks to the Spring Boot API under `/api/v1` using an HttpOnly session cookie
and Spring Security's `XSRF-TOKEN` cookie for CSRF protection.

## Prerequisites

- Node.js 20+ and npm 10+
- The backend running locally on port 8080 (see `../backend/README.md`) for anything beyond the static shell

## Run locally

```bash
cd frontend
npm ci            # clean install from package-lock.json
npm run dev       # http://localhost:5173
```

The Vite dev server proxies `/api/*` to `http://localhost:8080`, so the browser
only ever sees one origin. To point it somewhere else, create
`frontend/.env.local` (see `.env.example`):

| Variable | Used by | Default | Purpose |
|---|---|---|---|
| `VITE_API_BASE_URL` | browser bundle | `/api/v1` | Base URL for API calls |
| `SAMS_API_PROXY` | Vite dev server only | `http://localhost:8080` | Where `/api` is proxied in dev |

`VITE_*` values are compiled into public JavaScript, so never put secrets in them.

## Production build

```bash
npm run build     # outputs dist/
npm run preview   # serves dist/ on http://localhost:4173
```

The `Dockerfile` builds the app and serves it with nginx (`nginx.conf`), which
also proxies `/api/` to the `api` container and falls back to `index.html` for
client-side routes.

## Structure

```
src/
├── main.jsx            entry point
├── App.jsx             routes
├── api.js              axios client (base URL, credentials, XSRF header) + error messages
├── notice.jsx          app-wide toast notice
├── navigation.js       sidebar entries per role
├── components.jsx      shared UI: headings, stat cards, loading / empty / error states
├── layout/AppShell.jsx responsive shell (sidebar → bottom bar on phones)
├── pages/              one file per screen
└── styles.css
```

## Responsive layout

Checked at 1440px (full sidebar), 900px (icon-only sidebar) and 390px (bottom
navigation bar, single-column content).
