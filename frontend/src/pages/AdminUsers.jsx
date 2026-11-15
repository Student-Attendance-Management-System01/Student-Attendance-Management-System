import React, { useCallback, useEffect, useState } from 'react';
import { api, messageFrom } from '../api';
import { useAuth } from '../auth';
import { useNotice } from '../notice';
import { EmptyState, ErrorState, Field, LoadingLine, Modal, PageHeading, StatusPill } from '../components';
import { validateNewUser, validateUserUpdate } from '../validation';

const BLANK = { institutionId: '', fullName: '', email: '', role: 'STUDENT', password: '' };
const ROLE_TABS = [['ALL', 'All'], ['STUDENT', 'Students'], ['FACULTY', 'Faculty'], ['ADMIN', 'Admins']];

export default function AdminUsers() {
  const { user: me } = useAuth();
  const { notify } = useNotice();
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState(BLANK);
  const [fieldErrors, setFieldErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [listError, setListError] = useState('');
  const [query, setQuery] = useState('');
  const [roleTab, setRoleTab] = useState('ALL');
  const [editing, setEditing] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try { setUsers((await api.get('/admin/users')).data); setListError(''); }
    catch (e) { setListError(messageFrom(e)); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { load(); }, [load]);

  const update = (k, v) => setForm(old => ({ ...old, [k]: v }));

  async function create(e) {
    e.preventDefault();
    const errors = validateNewUser(form);
    setFieldErrors(errors); setError('');
    if (Object.keys(errors).length) return;
    setBusy(true);
    try {
      await api.post('/admin/users', { ...form, institutionId: form.institutionId.trim(), fullName: form.fullName.trim(), email: form.email.trim() });
      setForm(BLANK);
      notify('User account created.');
      await load();
    } catch (e) { setError(messageFrom(e)); }
    finally { setBusy(false); }
  }

  async function deactivate(u) {
    if (!window.confirm(`Deactivate ${u.fullName} (${u.institutionId})? They will no longer be able to sign in.`)) return;
    setListError('');
    try { await api.patch(`/admin/users/${u.id}/deactivate`); notify('Account deactivated.'); await load(); }
    catch (e) { setListError(messageFrom(e)); }
  }

  const counts = users.reduce((acc, u) => ({ ...acc, [u.role]: (acc[u.role] || 0) + 1 }), {});
  const q = query.trim().toLowerCase();
  const filtered = users
    .filter(u => roleTab === 'ALL' || u.role === roleTab)
    .filter(u => !q || `${u.fullName} ${u.email} ${u.institutionId} ${u.role}`.toLowerCase().includes(q))
    .sort((a, b) => a.fullName.localeCompare(b.fullName));

  return <>
    <PageHeading eyebrow="ADMINISTRATION" title="User accounts" description="Create and maintain student, faculty and administrator accounts." action={<span className="count-chip">{users.length} total users</span>} />
    <div className="two-column-layout">
      <section className="panel form-panel" aria-labelledby="create-user-title">
        <div className="panel-head"><div><h2 id="create-user-title">Create an account</h2><p>Use the institution ID and institutional email.</p></div></div>
        <ErrorState error={error} />
        <form onSubmit={create} className="form-stack compact" noValidate>
          <Field label="Institution ID" error={fieldErrors.institutionId}><input value={form.institutionId} onChange={e => update('institutionId', e.target.value)} placeholder="PES1UG24CS000" maxLength={64} aria-invalid={!!fieldErrors.institutionId} /></Field>
          <Field label="Full name" error={fieldErrors.fullName}><input value={form.fullName} onChange={e => update('fullName', e.target.value)} placeholder="Full name" maxLength={160} aria-invalid={!!fieldErrors.fullName} /></Field>
          <Field label="Institutional email" error={fieldErrors.email}><input type="email" value={form.email} onChange={e => update('email', e.target.value)} placeholder="name@pes.edu" maxLength={190} aria-invalid={!!fieldErrors.email} /></Field>
          <Field label="Role"><select value={form.role} onChange={e => update('role', e.target.value)}><option value="STUDENT">Student</option><option value="FACULTY">Faculty</option><option value="ADMIN">Administrator</option></select></Field>
          <Field label="Initial password" error={fieldErrors.password} hint="Upper and lowercase letters, a number and a special character. Share initial credentials securely.">
            <input type="password" autoComplete="new-password" value={form.password} onChange={e => update('password', e.target.value)} maxLength={64} placeholder="At least 8 characters" aria-invalid={!!fieldErrors.password} />
          </Field>
          <button className="button primary full" disabled={busy}>{busy ? 'Creating…' : 'Create account'} <span aria-hidden="true">→</span></button>
        </form>
      </section>

      <section className="panel table-panel" aria-labelledby="directory-title">
        <div className="panel-head"><div><h2 id="directory-title">Directory</h2><p>Search, edit and deactivate accounts.</p></div><button className="button subtle small" onClick={load}>↻ Refresh</button></div>
        <div className="tab-row" role="tablist" aria-label="Filter by role">
          {ROLE_TABS.map(([key, label]) => <button key={key} role="tab" aria-selected={roleTab === key} className={`tab ${roleTab === key ? 'active' : ''}`} onClick={() => setRoleTab(key)}>{label}<span>{key === 'ALL' ? users.length : counts[key] || 0}</span></button>)}
        </div>
        <div className="search-row"><input type="search" value={query} onChange={e => setQuery(e.target.value)} placeholder="Search name, ID or email…" aria-label="Search user directory" /></div>
        <ErrorState error={listError} />
        {loading ? <LoadingLine /> : filtered.length === 0
          ? <EmptyState title="No accounts found" description={users.length ? 'Try another search or role filter.' : 'Create the first account using the form.'} />
          : <div className="table-wrap"><table><thead><tr><th>User</th><th>Institution ID</th><th>Role</th><th>Status</th><th><span className="sr-only">Actions</span></th></tr></thead><tbody>
            {filtered.map(u => <tr key={u.id}>
              <td><strong>{u.fullName}</strong><small className="table-sub">{u.email}</small></td>
              <td>{u.institutionId}</td>
              <td><span className="role-label">{u.role}</span></td>
              <td><StatusPill value={u.active ? 'Active' : 'Inactive'} /></td>
              <td><div className="row-actions">
                <button className="text-link" onClick={() => setEditing(u)} aria-label={`Edit ${u.fullName}`}>Edit</button>
                {u.active && u.email !== me?.email && <button className="text-link danger-link" onClick={() => deactivate(u)} aria-label={`Deactivate ${u.fullName}`}>Deactivate</button>}
              </div></td>
            </tr>)}
          </tbody></table></div>}
      </section>
    </div>
    {editing && <EditUser user={editing} isSelf={editing.email === me?.email} onClose={() => setEditing(null)} onSaved={async () => { setEditing(null); notify('Account updated.'); await load(); }} />}
  </>;
}

function EditUser({ user, isSelf, onClose, onSaved }) {
  const [form, setForm] = useState({ fullName: user.fullName, email: user.email, role: user.role, active: user.active });
  const [fieldErrors, setFieldErrors] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const update = (k, v) => setForm(old => ({ ...old, [k]: v }));

  async function save(e) {
    e.preventDefault();
    const errors = validateUserUpdate(form);
    setFieldErrors(errors); setError('');
    if (Object.keys(errors).length) return;
    setBusy(true);
    try {
      await api.put(`/admin/users/${user.id}`, { ...form, fullName: form.fullName.trim(), email: form.email.trim() });
      await onSaved();
    } catch (e) { setError(messageFrom(e)); setBusy(false); }
  }

  return <Modal eyebrow={`EDIT ACCOUNT · ${user.institutionId}`} title={user.fullName} onClose={onClose}>
    <ErrorState error={error} />
    <form className="form-stack" onSubmit={save} noValidate>
      <Field label="Full name" error={fieldErrors.fullName}><input value={form.fullName} onChange={e => update('fullName', e.target.value)} maxLength={160} aria-invalid={!!fieldErrors.fullName} /></Field>
      <Field label="Institutional email" error={fieldErrors.email}><input type="email" value={form.email} onChange={e => update('email', e.target.value)} maxLength={190} aria-invalid={!!fieldErrors.email} /></Field>
      <Field label="Role"><select value={form.role} onChange={e => update('role', e.target.value)} disabled={isSelf}><option value="STUDENT">Student</option><option value="FACULTY">Faculty</option><option value="ADMIN">Administrator</option></select></Field>
      <label className="check-row"><input type="checkbox" checked={form.active} onChange={e => update('active', e.target.checked)} disabled={isSelf} /> Account active (can sign in)</label>
      {isSelf && <small className="field-hint">You cannot change your own role or deactivate yourself.</small>}
      <div className="button-row"><button type="button" className="button subtle" onClick={onClose}>Cancel</button><button className="button primary" disabled={busy}>{busy ? 'Saving…' : 'Save changes'}</button></div>
    </form>
  </Modal>;
}
