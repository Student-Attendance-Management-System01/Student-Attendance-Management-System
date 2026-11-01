import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { loginErrorMessage, useAuth } from '../auth';
import { NoticeBar } from '../notice';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validateLogin(email, password) {
  const errors = {};
  if (!email.trim()) errors.email = 'Enter your institutional email.';
  else if (!EMAIL_RE.test(email.trim())) errors.email = 'Enter a valid email address.';
  if (!password) errors.password = 'Enter your password.';
  return errors;
}

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    const errors = validateLogin(email, password);
    setFieldErrors(errors);
    setError('');
    if (Object.keys(errors).length) return;
    setBusy(true);
    try {
      await login(email, password);
      const from = location.state?.from;
      navigate(from && from !== '/login' ? from : '/', { replace: true });
    } catch (e) {
      setError(loginErrorMessage(e));
      setPassword('');
    } finally {
      setBusy(false);
    }
  }

  return <div className="auth-screen"><NoticeBar /><div className="auth-decoration decor-one" /><div className="auth-decoration decor-two" /><div className="auth-card">
    <div className="auth-brand"><div className="brand-mark large">S</div><span>PES UNIVERSITY · CSE</span></div>
    <div className="eyebrow">STUDENT ATTENDANCE MANAGEMENT</div>
    <h1>Welcome back.</h1>
    <p className="auth-subtitle">Sign in to view your courses, attendance and reports.</p>
    {error && <div className="inline-error" role="alert">{error}</div>}
    <form onSubmit={submit} className="form-stack" noValidate>
      <label>Institutional email
        <div className="input-wrap"><span aria-hidden="true">✉</span>
          <input type="email" name="email" autoComplete="username" value={email} onChange={e => setEmail(e.target.value)} placeholder="you@pes.edu" aria-invalid={!!fieldErrors.email} aria-describedby={fieldErrors.email ? 'email-error' : undefined} maxLength={190} />
        </div>
        {fieldErrors.email && <small id="email-error" className="field-error">{fieldErrors.email}</small>}
      </label>
      <label>Password
        <input type="password" name="password" autoComplete="current-password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Enter your password" aria-invalid={!!fieldErrors.password} aria-describedby={fieldErrors.password ? 'password-error' : undefined} maxLength={128} />
        {fieldErrors.password && <small id="password-error" className="field-error">{fieldErrors.password}</small>}
      </label>
      <Link className="text-link forgot-link" to="/reset-password">Forgot password?</Link>
      <button className="button primary full" type="submit" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'} <span aria-hidden="true">→</span></button>
    </form>
    <div className="auth-secure"><span aria-hidden="true">◈</span> Secure access · Your data stays role-protected</div>
    <div className="auth-foot">Need access? Contact your department administrator.</div>
  </div></div>;
}
