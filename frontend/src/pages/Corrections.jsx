import React, { useEffect, useState } from 'react';
import { api, messageFrom } from '../api';
import { useNotice } from '../notice';
import { useAuth } from '../auth';
import { PageHeading, EmptyState, ErrorState, LoadingLine, StatusPill } from '../components';
export default function Corrections() {
  const { notify } = useNotice();
  const { user } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notes, setNotes] = useState({});
  const [busyId, setBusyId] = useState(null);
  async function load() {
    setLoading(true);
    try {
      const { data } = await api.get(user.role === 'STUDENT' ? '/corrections/mine' : '/corrections/pending');
      setRows(data);
      setError('');
    } catch (e) {
      setError(messageFrom(e));
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
  }, [user.role]);
  async function decide(row, status) {
    if (
      status === 'APPROVED' &&
      !window.confirm(`Approve this request and change attendance to ${row.requestedStatus}?`)
    )
      return;
    setBusyId(row.id);
    setError('');
    try {
      await api.patch(`/corrections/${row.id}`, { status, decisionNote: notes[row.id] || '' });
      notify(`Request ${status.toLowerCase()}.`);
      await load();
    } catch (e) {
      setError(messageFrom(e));
    } finally {
      setBusyId(null);
    }
  }
  return (
    <>
      <PageHeading
        eyebrow={user.role === 'STUDENT' ? 'STUDENT SUPPORT' : 'FACULTY WORKFLOW'}
        title={user.role === 'STUDENT' ? 'My correction requests' : 'Correction requests'}
        description={
          user.role === 'STUDENT'
            ? 'Track the status of attendance corrections you have requested.'
            : 'Review student requests and record an explicit decision.'
        }
        action={
          <button className="button subtle small" onClick={load}>
            ↻ Refresh
          </button>
        }
      />
      <ErrorState error={error} />
      {loading ? (
        <LoadingLine />
      ) : !rows.length ? (
        <section className="panel">
          <EmptyState
            title="No correction requests"
            description={
              user.role === 'STUDENT'
                ? 'Requests you submit will be listed here.'
                : 'There are no pending correction requests for your assigned course sections.'
            }
          />
        </section>
      ) : (
        <div className="correction-list">
          {rows.map(r => (
            <section className="panel correction-card" key={r.id}>
              <div className="correction-top">
                <div>
                  <div className="eyebrow">
                    REQUEST #{r.id} · {r.courseCode}
                  </div>
                  <h2>
                    {r.studentName} <span className="section-muted">· {r.studentId}</span>
                  </h2>
                  <p>
                    {r.sessionDate} · Session record #{r.attendanceRecordId}
                  </p>
                </div>
                <StatusPill value={r.status} />
              </div>
              <div className="correction-change">
                <div>
                  <small>Current status</small>
                  <strong>{r.currentStatus}</strong>
                </div>
                <span className="change-arrow">→</span>
                <div>
                  <small>Requested status</small>
                  <strong className="requested-status">{r.requestedStatus}</strong>
                </div>
              </div>
              <div className="reason-block">
                <small>Student's reason</small>
                <p>{r.reason}</p>
              </div>
              <div className="correction-meta">
                <span>Submitted {new Date(r.requestedAt).toLocaleString('en-IN')}</span>
                {r.decisionNote && <span>Decision note: {r.decisionNote}</span>}
              </div>
              {user.role !== 'STUDENT' && r.status === 'PENDING' && (
                <div className="decision-area">
                  <label>
                    Decision note (optional)
                    <input
                      value={notes[r.id] || ''}
                      onChange={e => setNotes(old => ({ ...old, [r.id]: e.target.value }))}
                      maxLength={250}
                      placeholder="Add a short note to the student"
                    />
                  </label>
                  <div className="button-row">
                    <button
                      className="button subtle danger-outline"
                      disabled={busyId === r.id}
                      onClick={() => decide(r, 'REJECTED')}
                    >
                      Reject
                    </button>
                    <button
                      className="button primary"
                      disabled={busyId === r.id}
                      onClick={() => decide(r, 'APPROVED')}
                    >
                      {busyId === r.id ? 'Saving…' : 'Approve correction'}
                    </button>
                  </div>
                </div>
              )}
            </section>
          ))}
        </div>
      )}
    </>
  );
}
