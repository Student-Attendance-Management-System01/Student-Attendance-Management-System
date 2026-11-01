import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './auth';
import AccessDenied from './pages/AccessDenied.jsx';

export function FullPageLoader() {
  return <div className="loading-screen" role="status"><div className="brand-mark">S</div><p>Loading SAMS…</p></div>;
}

/** Sends signed-out visitors to /login, remembering where they were going. */
export function RequireAuth({ children }) {
  const { user, status } = useAuth();
  const location = useLocation();
  if (status === 'loading') return <FullPageLoader />;
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
  return children ?? <Outlet />;
}

/**
 * Shows an access-denied state when the signed-in role is not allowed here.
 * UI convenience only; the backend enforces the same rules on every request.
 */
export function RequireRole({ roles, children }) {
  const { user } = useAuth();
  if (!user || !roles.includes(user.role)) return <AccessDenied />;
  return children ?? <Outlet />;
}

/** For /login and /reset-password: signed-in users go straight to the app. */
export function PublicOnly({ children }) {
  const { user, status } = useAuth();
  if (status === 'loading') return <FullPageLoader />;
  if (user) return <Navigate to="/" replace />;
  return children;
}
