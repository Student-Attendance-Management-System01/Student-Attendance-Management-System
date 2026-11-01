import React from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import AppShell from './layout/AppShell.jsx';
import Home from './pages/Home.jsx';
import Login from './pages/Login.jsx';
import ResetPassword from './pages/ResetPassword.jsx';
import NotFound from './pages/NotFound.jsx';
import AdminUsers from './pages/AdminUsers.jsx';
import AdminCourses from './pages/AdminCourses.jsx';
import { NoticeProvider } from './notice';
import { AuthProvider, useAuth } from './auth';
import { PublicOnly, RequireAuth, RequireRole } from './guards';

function ProtectedShell() {
  const { user, logout } = useAuth();
  return <RequireAuth><AppShell user={user} onLogout={logout} /></RequireAuth>;
}

export function AppRoutes() {
  return <Routes>
    <Route path="/login" element={<PublicOnly><Login /></PublicOnly>} />
    <Route path="/reset-password" element={<PublicOnly><ResetPassword /></PublicOnly>} />
    <Route element={<ProtectedShell />}>
      <Route index element={<Home />} />
      <Route path="admin" element={<RequireRole roles={['ADMIN']} />}>
        <Route path="users" element={<AdminUsers />} />
        <Route path="courses" element={<AdminCourses />} />
      </Route>
      <Route path="*" element={<NotFound />} />
    </Route>
  </Routes>;
}

export default function App() {
  return <NoticeProvider>
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  </NoticeProvider>;
}
