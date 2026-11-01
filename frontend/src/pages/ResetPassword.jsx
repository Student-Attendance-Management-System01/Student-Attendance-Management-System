import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api, messageFrom } from '../api';
import { useNotice } from '../notice';
import { passwordProblems } from '../validation';

export default function ResetPassword() {
  const { notify } = useNotice();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [email, setEmail] = useState('');
  const [token, setToken] = useState(params.get('token') || '');
  const [newPassword, setNewPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [requested, setRequested] = useState(false);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const hasToken = !!params.get('token');

  async function request(event) {
    event.preventDefault();
    setBusy(true); setError('');
    try {
      await api.get('/auth/csrf');
      await api.post('/auth/password-reset/request', { email: email.trim() });
      setRequested(true);
    } catch (e) { setError(messageFrom(e)); } finally { setBusy(false); }
  }

  async function confirmReset(event) {
    event.preventDefault();
    const problems = passwordProblems(newPassword);
    if (problems.length) { setError(`Password needs ${problems.join(', ')}.`); return; }
    if (newPassword !== confirm) { setError('The two passwords do not match.'); return; }
    setBusy(true); setError('');
    try {
      await api.get('/auth/csrf');
      await api.post('/auth/password-reset/confirm', { token: token.trim(), newPassword });
      notify('Password reset. You can sign in now.');
      navigate('/login', { replace: true });
    } catch (e) { setError(messageFrom(e)); } finally { setBusy(false); }
  }

  return <div className="auth-screen"><div className="auth-card">
    <Link className="text-link" to="/login">← Back to sign in</Link>
    <div className="eyebrow auth-eyebrow">ACCOUNT RECOVERY</div>
    <h1>Reset password</h1>
    <p className="auth-subtitle">{hasToken ? 'Choose a new password for your account.' : 'Enter your institutional email. We will send a reset link if the account exists.'}</p>
    {error && <div className="inline-error" role="alert">{error}</div>}
    {hasToken
      ? <form className="form-stack" onSubmit={confirmReset}>
          <label>Reset token<input value={token} onChange={e => setToken(e.target.value)} required /></label>
          <label>New password<input type="password" autoComplete="new-password" value={newPassword} onChange={e => setNewPassword(e.target.value)} maxLength={64} required /></label>
          <label>Confirm new password<input type="password" autoComplete="new-password" value={confirm} onChange={e => setConfirm(e.target.value)} maxLength={64} required /></label>
          <small className="field-hint">At least 8 characters with uppercase, lowercase, number and special character.</small>
          <button className="button primary full" disabled={busy}>{busy ? 'Saving…' : 'Set new password'}</button>
        </form>
      : <form className="form-stack" onSubmit={request}>
          <label>Institutional email<input type="email" autoComplete="username" value={email} onChange={e => setEmail(e.target.value)} required /></label>
          <button className="button primary full" disabled={busy || requested}>{busy ? 'Sending…' : 'Send reset link'}</button>
          {requested && <p className="success-text" role="status">If the account exists, a password reset link has been sent.</p>}
        </form>}
  </div></div>;
}
