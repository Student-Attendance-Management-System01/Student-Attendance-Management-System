import React from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { navFor, roleLabel } from '../navigation';
import { NoticeBar } from '../notice';

function initials(name) {
  return (name || 'U').split(' ').filter(Boolean).map(x => x[0]).join('').slice(0, 2).toUpperCase();
}

/** Shared responsive layout: sidebar (bottom bar on phones), top bar, notice, page outlet, footer. */
export default function AppShell({ user, onLogout }) {
  const location = useLocation();
  const menu = navFor(user?.role);
  const current = [...menu].sort((a, b) => b.to.length - a.to.length)
    .find(item => (item.to === '/' ? location.pathname === '/' : location.pathname.startsWith(item.to)));
  const pageTitle = current?.label || (location.pathname === '/' ? 'Overview' : 'Unavailable');

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><div className="brand-mark">S</div><div><strong>SAMS</strong><span>Attendance Management</span></div></div>
      <div className="workspace-label">WORKSPACE</div>
      <nav aria-label="Main navigation">
        {menu.map(item => <NavLink key={item.to} to={item.to} end={item.to === '/'} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <span className="nav-icon" aria-hidden="true">{item.icon}</span>{item.label}
        </NavLink>)}
      </nav>
      <div className="sidebar-bottom">
        <div className="security-note"><span className="status-dot" /> <span><b>Protected workspace</b><small>Role-based access enabled</small></span></div>
        {onLogout && <button className="nav-item" onClick={onLogout}><span className="nav-icon" aria-hidden="true">↪</span> Sign out</button>}
      </div>
    </aside>
    <main className="main-area">
      <header className="topbar">
        <div className="mobile-brand"><div className="brand-mark">S</div><strong>SAMS</strong></div>
        <div className="crumb"><span>Workspace</span><span className="crumb-slash">/</span><b>{pageTitle}</b></div>
        <div className="top-user">
          <div className="avatar" aria-hidden="true">{initials(user?.fullName)}</div>
          <div className="top-user-info"><strong>{user?.fullName || 'Guest'}</strong><span>{roleLabel(user?.role)}</span></div>
          {onLogout && <button className="icon-button logout-mobile" onClick={onLogout} aria-label="Sign out">↪</button>}
        </div>
      </header>
      <NoticeBar />
      <div className="page-content"><Outlet /></div>
      <footer className="page-footer"><span>Student Attendance Management System</span><span>PES University · CSE</span></footer>
    </main>
  </div>;
}
