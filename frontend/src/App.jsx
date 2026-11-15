import React from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import AppShell from './layout/AppShell.jsx';
import Login from './pages/Login.jsx';
import ResetPassword from './pages/ResetPassword.jsx';
import NotFound from './pages/NotFound.jsx';
import Dashboard from './pages/Dashboard.jsx';
import AdminUsers from './pages/AdminUsers.jsx';
import AdminCourses from './pages/AdminCourses.jsx';
import FacultySessions from './pages/FacultySessions.jsx';
import StudentAttendance from './pages/StudentAttendance.jsx';
import Corrections from './pages/Corrections.jsx';
import Reports from './pages/Reports.jsx';
import AuditLog from './pages/AuditLog.jsx';
import { NoticeProvider } from './notice';
import { AuthProvider, useAuth } from './auth';
import { PublicOnly, RequireAuth, RequireRole } from './guards';

// Opt in to React Router v7 behaviour now so the upgrade is a no-op.
export const ROUTER_FUTURE = { v7_startTransition: true, v7_relativeSplatPath: true };

function ProtectedShell() {
  const { user, logout } = useAuth();
  return <RequireAuth><AppShell user={user} onLogout={logout} /></RequireAuth>;
}

// Route-level role checks mirror the backend's @PreAuthorize rules. They decide what
// the UI shows; the API still rejects anything a role is not allowed to do.
export function AppRoutes() {
  return <Routes>
    <Route path="/login" element={<PublicOnly><Login /></PublicOnly>} />
    <Route path="/reset-password" element={<PublicOnly><ResetPassword /></PublicOnly>} />
    <Route element={<ProtectedShell />}>
      <Route index element={<Dashboard />} />
      <Route path="admin" element={<RequireRole roles={['ADMIN']} />}>
        <Route path="users" element={<AdminUsers />} />
        <Route path="courses" element={<AdminCourses />} />
      </Route>
      <Route path="audit" element={<RequireRole roles={['ADMIN']}><AuditLog /></RequireRole>} />
      <Route path="sessions" element={<RequireRole roles={['FACULTY']}><FacultySessions /></RequireRole>} />
      <Route path="my-attendance" element={<RequireRole roles={['STUDENT']}><StudentAttendance /></RequireRole>} />
      <Route path="corrections" element={<RequireRole roles={['FACULTY', 'STUDENT']}><Corrections /></RequireRole>} />
      <Route path="reports" element={<RequireRole roles={['ADMIN', 'FACULTY']}><Reports /></RequireRole>} />
      <Route path="*" element={<NotFound />} />
    </Route>
  </Routes>;
}

export default function App() {
  return <NoticeProvider>
    <AuthProvider>
      <BrowserRouter future={ROUTER_FUTURE}>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  </NoticeProvider>;
}
