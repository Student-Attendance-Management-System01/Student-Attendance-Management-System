import React, { useEffect, useState } from 'react';
import { api, messageFrom } from '../api';
import { PageHeading, EmptyState, ErrorState, LoadingLine } from '../components';
import { localDate } from '../components';
const nowDate = () => localDate();
export default function AuditLog() {
  const [from, setFrom] = useState('');
  const [to, setTo] = useState(nowDate());
  const [email, setEmail] = useState('');
  const [action, setAction] = useState('');
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  async function load(e) {
    e?.preventDefault();
    setLoading(true);
    setError('');
    try {
      const params = {};
      if (from) params.from = from;
      if (to) params.to = to;
      if (email.trim()) params.userEmail = email.trim();
      if (action.trim()) params.action = action.trim();
      setRows((await api.get('/audit-log', { params })).data);
    } catch (e) {
      setError(messageFrom(e));
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
  }, []);
  return (
    <>
      <PageHeading
        eyebrow="SECURITY & ACCOUNTABILITY"
        title="Audit log"
        description="Read-only history of security- and data-relevant activity."
        action={<span className="count-chip">Latest 500 events max</span>}
      />
      <section className="panel report-controls">
        <form className="audit-filters" onSubmit={load}>
          <label>
            From
            <input type="date" value={from} onChange={e => setFrom(e.target.value)} />
          </label>
          <label>
            To
            <input type="date" value={to} onChange={e => setTo(e.target.value)} />
          </label>
          <label>
            Actor email
            <input
              type="search"
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="Filter by email"
            />
          </label>
          <label>
            Action
            <input
              value={action}
              onChange={e => setAction(e.target.value)}
              placeholder="e.g. ATTENDANCE_EDITED"
            />
          </label>
          <button className="button primary" disabled={loading}>
            Apply filters
          </button>
        </form>
      </section>
      <section className="panel table-panel">
        <ErrorState error={error} />
        <div className="panel-head">
          <div>
            <h2>Recorded events</h2>
            <p>Audit records are append-only and cannot be modified from this screen.</p>
          </div>
          <button className="button subtle small" onClick={() => load()}>
            ↻ Refresh
          </button>
        </div>
        {loading ? (
          <LoadingLine />
        ) : !rows.length ? (
          <EmptyState title="No matching events" description="Try removing one or more filters." />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Time (IST)</th>
                  <th>Actor</th>
                  <th>Action</th>
                  <th>Target</th>
                  <th>Reason / detail</th>
                  <th>Source IP</th>
                </tr>
              </thead>
              <tbody>
                {rows.map(r => (
                  <tr key={r.id}>
                    <td>{new Date(r.createdAt).toLocaleString('en-IN')}</td>
                    <td>{r.actorEmail || 'System'}</td>
                    <td>
                      <span className="code-pill">{r.action}</span>
                    </td>
                    <td>
                      {r.targetType || '—'} {r.targetId ? `#${r.targetId}` : ''}
                    </td>
                    <td>{r.reason || r.newValue || '—'}</td>
                    <td>{r.sourceIp || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
}
