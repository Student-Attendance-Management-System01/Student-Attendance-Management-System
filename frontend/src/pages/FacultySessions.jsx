import React, { useEffect, useState } from 'react';
import { api, messageFrom } from '../api';
import { useNotice } from '../notice';
import { PageHeading, EmptyState, ErrorState, LoadingLine, StatusPill } from '../components';
import { localDate } from '../components';
const today = () => localDate();
export default function FacultySessions() {
  const { notify } = useNotice();
  const [sections, setSections] = useState([]);
  const [sessions, setSessions] = useState([]);
  const [sectionId, setSectionId] = useState('');
  const [date, setDate] = useState(today());
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('10:00');
  const [type, setType] = useState('LECTURE');
  const [roster, setRoster] = useState(null);
  const [statuses, setStatuses] = useState({});
  const [original, setOriginal] = useState({});
  const [reasons, setReasons] = useState({});
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  async function load() {
    setLoading(true);
    try {
      const [s, ss] = await Promise.all([api.get('/course-sections'), api.get('/sessions')]);
      setSections(s.data);
      setSessions(ss.data);
      if (!sectionId && s.data.length) setSectionId(String(s.data[0].id));
      setError('');
    } catch (e) {
      setError(messageFrom(e));
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
  }, []);
  async function createSession(e) {
    e.preventDefault();
    if (!sectionId) {
      setError('Choose a course section.');
      return;
    }
    if (endTime <= startTime) {
      setError('End time must be after the start time.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      await api.post('/sessions', { courseSectionId: Number(sectionId), date, startTime, endTime, type });
      notify('Class session created.');
      await load();
    } catch (e) {
      setError(messageFrom(e));
    } finally {
      setBusy(false);
    }
  }
  async function openRoster(session) {
    setError('');
    try {
      const { data } = await api.get(`/sessions/${session.id}/roster`);
      const next = {};
      const orig = {};
      for (const s of data.students) {
        next[s.studentId] = s.status;
        orig[s.studentId] = s.status;
      }
      setRoster(data);
      setStatuses(next);
      setOriginal(orig);
      setReasons({});
    } catch (e) {
      setError(messageFrom(e));
    }
  }
  function setStudentStatus(id, value) {
    setStatuses(old => ({ ...old, [id]: value }));
  }
  async function submit() {
    if (!roster) return;
    const unmarked = roster.students.filter(
      s => !statuses[s.studentId] || statuses[s.studentId] === 'UNMARKED',
    );
    if (unmarked.length) {
      setError(`Mark all ${unmarked.length} remaining student(s) before submitting.`);
      return;
    }
    if (
      !window.confirm(
        `Submit attendance for ${roster.session.courseCode} on ${roster.session.date}? This submits the entire class roster.`,
      )
    )
      return;
    setBusy(true);
    setError('');
    try {
      const records = roster.students.map(s => ({ studentId: s.studentId, status: statuses[s.studentId] }));
      await api.post(`/sessions/${roster.session.id}/attendance`, { records });
      notify('Attendance submitted successfully.');
      await load();
      await openRoster(roster.session);
    } catch (e) {
      setError(messageFrom(e));
    } finally {
      setBusy(false);
    }
  }
  async function saveEdit(student) {
    const status = statuses[student.studentId];
    if (status === original[student.studentId]) return;
    const reason = reasons[student.studentId] || '';
    if (reason.trim().length < 10) {
      setError(`Add a correction reason of at least 10 characters for ${student.name}.`);
      return;
    }
    setBusy(true);
    setError('');
    try {
      await api.put(`/sessions/${roster.session.id}/attendance/${encodeURIComponent(student.studentId)}`, {
        status,
        reason,
      });
      setOriginal(old => ({ ...old, [student.studentId]: status }));
      notify(`Attendance updated for ${student.name}.`);
      await load();
      await openRoster(roster.session);
    } catch (e) {
      setError(messageFrom(e));
    } finally {
      setBusy(false);
    }
  }
  const unmarked = roster
    ? roster.students.filter(s => !statuses[s.studentId] || statuses[s.studentId] === 'UNMARKED').length
    : 0;
  return (
    <>
      <PageHeading
        eyebrow="FACULTY TOOLS"
        title="Sessions & attendance"
        description="Create a class session, complete the roster, and review submitted attendance."
        action={
          <button className="button subtle small" onClick={load}>
            ↻ Refresh
          </button>
        }
      />
      <ErrorState error={error} />
      <div className="two-column-layout">
        <section className="panel form-panel">
          <div className="panel-head">
            <div>
              <h2>Create a session</h2>
              <p>Only course sections assigned to your faculty account are shown.</p>
            </div>
          </div>
          <form className="form-stack compact" onSubmit={createSession}>
            <label>
              Course section
              <select value={sectionId} onChange={e => setSectionId(e.target.value)} required>
                <option value="">Choose a section</option>
                {sections.map(s => (
                  <option key={s.id} value={s.id}>
                    {s.courseCode} · {s.courseName} · {s.section}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Class date
              <input type="date" value={date} onChange={e => setDate(e.target.value)} required />
            </label>
            <div className="form-grid">
              <label>
                Start time
                <input type="time" value={startTime} onChange={e => setStartTime(e.target.value)} required />
              </label>
              <label>
                End time
                <input type="time" value={endTime} onChange={e => setEndTime(e.target.value)} required />
              </label>
            </div>
            <label>
              Session type
              <select value={type} onChange={e => setType(e.target.value)}>
                <option value="LECTURE">Lecture</option>
                <option value="LAB">Lab</option>
                <option value="TUTORIAL">Tutorial</option>
              </select>
            </label>
            <button className="button primary full" disabled={busy || !sectionId || !sections.length}>
              {busy ? 'Saving…' : 'Create session'} <span>→</span>
            </button>
          </form>
          <div className="info-note">
            <span>i</span>
            <p>
              Sessions cannot overlap another session for the same section. Submitted attendance can be edited
              for 24 hours; after that, an administrator must make changes.
            </p>
          </div>
        </section>
        <section className="panel table-panel">
          <div className="panel-head">
            <div>
              <h2>Class sessions</h2>
              <p>Choose a session to open its roster.</p>
            </div>
            <span className="count-chip">{sessions.length}</span>
          </div>
          {loading ? (
            <LoadingLine />
          ) : !sessions.length ? (
            <EmptyState
              title="No class sessions yet"
              description="Create your first class session using the form."
            />
          ) : (
            <div className="session-list">
              {sessions.map(s => (
                <div className={`session-row ${roster?.session?.id === s.id ? 'selected' : ''}`} key={s.id}>
                  <div className="session-date">
                    <strong>
                      {new Date(`${s.date}T00:00:00`).toLocaleDateString('en-IN', { day: '2-digit' })}
                    </strong>
                    <span>
                      {new Date(`${s.date}T00:00:00`).toLocaleDateString('en-IN', { month: 'short' })}
                    </span>
                  </div>
                  <div className="session-details">
                    <strong>
                      {s.courseCode} <span className="section-muted">· {s.section}</span>
                    </strong>
                    <p>{s.courseName}</p>
                    <small>
                      {s.date} · {s.startTime}–{s.endTime} · {s.type}
                    </small>
                  </div>
                  <div className="session-action">
                    <StatusPill value={s.submitted ? 'Submitted' : 'Open'} />
                    <button className="text-link" onClick={() => openRoster(s)}>
                      {s.submitted ? 'Review roster' : 'Mark now'} →
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
      {roster && (
        <section className="panel roster-panel">
          <div className="panel-head roster-head">
            <div>
              <div className="eyebrow">ATTENDANCE ROSTER</div>
              <h2>
                {roster.session.courseCode} · Section {roster.session.section}
              </h2>
              <p>
                {roster.session.date} · {roster.session.startTime}–{roster.session.endTime} ·{' '}
                {roster.students.length} students
              </p>
            </div>
            <div className="roster-head-actions">
              <StatusPill value={roster.submitted ? 'Submitted' : 'In progress'} />
              <button className="icon-button" onClick={() => setRoster(null)} aria-label="Close roster">
                ×
              </button>
            </div>
          </div>
          {!roster.submitted && (
            <div className="roster-toolbar">
              <div className="roster-counter">
                <b>{roster.students.length - unmarked}</b> of {roster.students.length} marked{' '}
                <span>· {unmarked} unmarked</span>
              </div>
              <button
                className="button subtle small"
                onClick={() =>
                  setStatuses(Object.fromEntries(roster.students.map(s => [s.studentId, 'PRESENT'])))
                }
              >
                ✓ Mark all present
              </button>
            </div>
          )}
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Institution ID</th>
                  <th>Attendance status</th>
                  {roster.submitted && <th>Correction reason</th>}
                  {roster.submitted && <th />}
                </tr>
              </thead>
              <tbody>
                {roster.students.map(s => (
                  <tr key={s.studentId}>
                    <td>
                      <strong>{s.name}</strong>
                    </td>
                    <td>{s.studentId}</td>
                    <td>
                      <select
                        className={`status-select status-select-${(statuses[s.studentId] || 'unmarked').toLowerCase()}`}
                        value={statuses[s.studentId] || 'UNMARKED'}
                        aria-label={`Attendance status for ${s.name}`}
                        onChange={e => setStudentStatus(s.studentId, e.target.value)}
                        disabled={roster.submitted && busy}
                      >
                        <option value="UNMARKED" disabled>
                          Unmarked
                        </option>
                        <option value="PRESENT">Present</option>
                        <option value="ABSENT">Absent</option>
                        <option value="OD">On-duty (OD)</option>
                      </select>
                    </td>
                    {roster.submitted && (
                      <>
                        <td>
                          <input
                            className="reason-input"
                            aria-label={`Correction reason for ${s.name}`}
                            value={reasons[s.studentId] || ''}
                            onChange={e => setReasons(old => ({ ...old, [s.studentId]: e.target.value }))}
                            placeholder="Reason (10+ characters)"
                            maxLength={250}
                          />
                        </td>
                        <td>
                          {statuses[s.studentId] !== original[s.studentId] && (
                            <button className="text-link" disabled={busy} onClick={() => saveEdit(s)}>
                              Save change
                            </button>
                          )}
                        </td>
                      </>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {!roster.submitted && (
            <div className="roster-submit">
              <span className="field-hint">
                Every enrolled student must have a status. Duplicate submissions are prevented.
              </span>
              <button className="button primary" disabled={busy || unmarked > 0} onClick={submit}>
                {busy ? 'Submitting…' : `Submit attendance (${roster.students.length})`}
              </button>
            </div>
          )}
        </section>
      )}
    </>
  );
}
