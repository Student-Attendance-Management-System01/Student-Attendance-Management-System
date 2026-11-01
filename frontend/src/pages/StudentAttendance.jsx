import React, { useEffect, useState } from 'react';
import { api, messageFrom } from '../api';
import { useNotice } from '../notice';
import { useAuth } from '../auth';
import { PageHeading, EmptyState, ErrorState, LoadingLine, StatusPill } from '../components';
export default function StudentAttendance() {
  const { notify } = useNotice();
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [requests, setRequests] = useState([]);
  const [selected, setSelected] = useState(null);
  const [requestedStatus, setRequestedStatus] = useState('PRESENT');
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  async function load() {
    setLoading(true);
    try {
      const [a, c] = await Promise.all([
        api.get(`/students/${encodeURIComponent(user.userId)}/attendance`),
        api.get('/corrections/mine'),
      ]);
      setData(a.data);
      setRequests(c.data);
      setError('');
    } catch (e) {
      setError(messageFrom(e));
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
  }, [user.userId]);
  async function submitCorrection(e) {
    e.preventDefault();
    if (!selected) return;
    if (requestedStatus === selected.status) {
      setError('Choose a status different from the current one.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      await api.post('/corrections', {
        attendanceRecordId: selected.attendanceRecordId,
        requestedStatus,
        reason,
      });
      notify('Correction request submitted to your faculty.');
      setSelected(null);
      setReason('');
      await load();
    } catch (e) {
      setError(messageFrom(e));
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageHeading
        eyebrow="STUDENT PORTAL"
        title="My attendance"
        description="Review your attendance by course, see the sessions counted, and request a correction when needed."
        action={<span className="count-chip">Threshold · {data?.threshold ?? 75}%</span>}
      />
      <ErrorState error={error} />
      {loading ? (
        <LoadingLine />
      ) : !data?.courses?.length ? (
        <section className="panel">
          <EmptyState
            title="No attendance to show yet"
            description="Your enrolled course sections will appear here when an administrator adds your enrolments."
          />
        </section>
      ) : (
        <>
          <div className="stats-grid student-stats">
            <div className="stat-card">
              <div className="stat-top">
                <span>Courses enrolled</span>
                <span className="stat-icon">▦</span>
              </div>
              <strong>{data.courses.length}</strong>
              <small>Current sections</small>
            </div>
            <div className="stat-card violet">
              <div className="stat-top">
                <span>Sessions attended</span>
                <span className="stat-icon">✓</span>
              </div>
              <strong>{data.courses.reduce((n, c) => n + c.attended, 0)}</strong>
              <small>Present plus On-duty</small>
            </div>
            <div className="stat-card green">
              <div className="stat-top">
                <span>Below threshold</span>
                <span className="stat-icon">!</span>
              </div>
              <strong>{data.courses.filter(c => c.belowThreshold).length}</strong>
              <small>Courses requiring attention</small>
            </div>
          </div>
          <div className="course-grid">
            {data.courses.map(c => (
              <section className="panel attendance-course" key={c.courseSectionId}>
                <div className="course-title-line">
                  <div>
                    <div className="eyebrow">
                      {c.courseCode} · {c.section}
                    </div>
                    <h2>{c.courseName}</h2>
                  </div>
                  <StatusPill
                    value={c.belowThreshold ? 'Below threshold' : c.held ? 'On track' : 'No sessions'}
                  />
                </div>
                <div className="attendance-score">
                  <strong className={c.belowThreshold ? 'below' : ''}>
                    {c.held ? Number(c.percentage).toFixed(2) + '%' : '—'}
                  </strong>
                  <span>
                    {c.attended} / {c.held} attended
                  </span>
                </div>
                <div className="progress-track large">
                  <div
                    className={`progress-fill ${c.belowThreshold ? 'red' : ''}`}
                    style={{ width: `${Math.min(Number(c.percentage) || 0, 100)}%` }}
                  />
                </div>
                {c.belowThreshold && (
                  <div className="warning-text">
                    ⚠ Your attendance is below the configured {data.threshold}% threshold.
                  </div>
                )}
                <div className="course-table-heading">
                  <strong>Session history</strong>
                  <small>{c.sessions.length} records</small>
                </div>
                {!c.sessions.length ? (
                  <p className="muted">No sessions recorded yet.</p>
                ) : (
                  <div className="mini-session-list">
                    {c.sessions.map(s => (
                      <div className="mini-session" key={s.attendanceRecordId}>
                        <div>
                          <b>
                            {new Date(`${s.date}T00:00:00`).toLocaleDateString('en-IN', {
                              day: '2-digit',
                              month: 'short',
                              year: 'numeric',
                            })}
                          </b>
                          <small>{s.type}</small>
                        </div>
                        <StatusPill value={s.status} />
                        <button
                          className="text-link"
                          onClick={() => {
                            setSelected({ ...s, courseCode: c.courseCode });
                            setRequestedStatus(s.status === 'ABSENT' ? 'PRESENT' : 'ABSENT');
                            setReason('');
                          }}
                        >
                          Request correction
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </section>
            ))}
          </div>
        </>
      )}
      {selected && (
        <div className="modal-backdrop" role="presentation">
          <section className="modal-card" role="dialog" aria-modal="true" aria-labelledby="correction-title">
            <button className="icon-button modal-close" onClick={() => setSelected(null)} aria-label="Close">
              ×
            </button>
            <div className="eyebrow">ATTENDANCE SUPPORT</div>
            <h2 id="correction-title">Request a correction</h2>
            <p className="auth-subtitle">
              {selected.courseCode} · {selected.date} · Current status: <b>{selected.status}</b>
            </p>
            <ErrorState error={error} />
            <form className="form-stack" onSubmit={submitCorrection}>
              <label>
                Requested status
                <select value={requestedStatus} onChange={e => setRequestedStatus(e.target.value)}>
                  <option value="PRESENT">Present</option>
                  <option value="ABSENT">Absent</option>
                  <option value="OD">On-duty (OD)</option>
                </select>
              </label>
              <label>
                Reason
                <textarea
                  value={reason}
                  onChange={e => setReason(e.target.value)}
                  maxLength={250}
                  placeholder="Explain what needs to be corrected (max 250 characters)."
                  required
                  rows={4}
                />
                <small className="field-hint">
                  Submit within 7 days of the class date. {reason.length}/250
                </small>
              </label>
              <div className="button-row">
                <button type="button" className="button subtle" onClick={() => setSelected(null)}>
                  Cancel
                </button>
                <button className="button primary" disabled={busy || !reason.trim()}>
                  {busy ? 'Submitting…' : 'Submit request'}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </>
  );
}
