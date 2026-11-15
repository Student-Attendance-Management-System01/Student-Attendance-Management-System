import React, { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { api, setUnauthorizedHandler } from './api';
import { useNotice } from './notice';

const AuthContext = createContext(null);

// SAMS-F-004: sign out after 15 minutes without user activity (configurable for testing).
export const IDLE_TIMEOUT_MS = Number(import.meta.env.VITE_IDLE_TIMEOUT_MINUTES || 15) * 60 * 1000;
const ACTIVITY_EVENTS = ['mousedown', 'keydown', 'touchstart', 'scroll'];

/** Message for a failed sign-in. Deliberately generic so it never reveals whether an account exists. */
export function loginErrorMessage(error) {
  const status = error?.response?.status;
  if (status === 401 || status === 423) return 'Sign-in failed. Check your email and password, or try again later.';
  if (status === 429) return 'Too many sign-in attempts. Please wait a minute and try again.';
  if (status === 400) return 'Enter a valid email and password.';
  if (error?.request && !error?.response) return 'Cannot reach the SAMS server. Check your connection and try again.';
  return 'Sign-in is unavailable right now. Please try again shortly.';
}

/**
 * Holds the signed-in user (from /auth/me or /auth/login). The JWT itself stays in an
 * HttpOnly cookie that JavaScript never reads; this context only mirrors who the backend
 * says is signed in so the UI can show the right navigation.
 */
export function AuthProvider({ children }) {
  const { notify } = useNotice();
  const [user, setUser] = useState(null);
  const [status, setStatus] = useState('loading');
  const expiryTimer = useRef(null);

  const clearExpiry = () => window.clearTimeout(expiryTimer.current);

  const userRef = useRef(null);
  userRef.current = user;

  const expire = useCallback(() => {
    clearExpiry();
    if (userRef.current) notify('Your session has expired. Please sign in again.', 'info');
    userRef.current = null;
    setUser(null);
  }, [notify]);

  // Inactivity timeout: any user interaction resets the clock; when it runs out the
  // session is ended on the server too (logout revokes the token).
  useEffect(() => {
    if (!user) return undefined;
    let timer;
    const onIdle = async () => {
      clearExpiry();
      try { await api.post('/auth/logout'); } catch { /* local sign-out still happens */ }
      setUser(null);
      notify('You were signed out after 15 minutes of inactivity.', 'info');
    };
    const reset = () => { window.clearTimeout(timer); timer = window.setTimeout(onIdle, IDLE_TIMEOUT_MS); };
    reset();
    ACTIVITY_EVENTS.forEach(e => window.addEventListener(e, reset, { passive: true }));
    return () => { window.clearTimeout(timer); ACTIVITY_EVENTS.forEach(e => window.removeEventListener(e, reset)); };
  }, [user, notify]);

  useEffect(() => {
    setUnauthorizedHandler(expire);
    return () => setUnauthorizedHandler(null);
  }, [expire]);

  useEffect(() => {
    let mounted = true;
    api.get('/auth/csrf').catch(() => {});
    api.get('/auth/me')
      .then(({ data }) => { if (mounted) setUser(data); })
      .catch(() => { if (mounted) setUser(null); })
      .finally(() => { if (mounted) setStatus('ready'); });
    return () => { mounted = false; clearExpiry(); };
  }, []);

  const login = useCallback(async (email, password) => {
    await api.get('/auth/csrf');
    const { data } = await api.post('/auth/login', { username: email.trim(), password });
    const { expiresInSeconds, ...profile } = data;
    clearExpiry();
    if (expiresInSeconds > 0) expiryTimer.current = window.setTimeout(expire, expiresInSeconds * 1000);
    setUser(profile);
    return profile;
  }, [expire]);

  const logout = useCallback(async () => {
    clearExpiry();
    try { await api.post('/auth/logout'); } catch { /* Always clear the local view. */ }
    setUser(null);
    notify('You have been signed out.', 'info');
    api.get('/auth/csrf').catch(() => {});
  }, [notify]);

  const value = useMemo(() => ({ user, status, login, logout }), [user, status, login, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
