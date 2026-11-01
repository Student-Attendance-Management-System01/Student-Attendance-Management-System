import React, { useEffect, useState } from 'react';
import { api, messageFrom } from '../api';
import { useNotice } from '../notice';
import { PageHeading, EmptyState, ErrorState, LoadingLine, StatCard, localDate } from '../components';

// Error bodies for blob downloads arrive as Blobs; read them back as JSON for a useful message.
async function blobErrorMessage(e) {
  const body = e?.response?.data;
  if (body instanceof Blob) {
    try {
      const text = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsText(body);
      });
      const parsed = JSON.parse(text);
      return parsed.message || messageFrom(e);
    } catch {
      /* fall through */
    }
  }
  return messageFrom(e);
}
const dateDaysAgo = days => {
  const d = new Date();
  d.setDate(d.getDate() - days);
  return localDate(d);
};
export default function Reports() {
  const { notify } = useNotice();
  const [sections, setSections] = useState([]);
  const [sectionId, setSectionId] = useState('');
  const [from, setFrom] = useState(dateDaysAgo(30));
  const [to, setTo] = useState(localDate());
  const [threshold, setThreshold] = useState(75);
  const [mode, setMode] = useState('student-wise');
  const [rows, setRows] = useState(null);
  const [generated, setGenerated] = useState(null);
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => {
    api
      .get('/course-sections')
      .then(({ data }) => {
        setSections(data);
        if (data.length) setSectionId(String(data[0].id));
      })
      .catch(e => setError(messageFrom(e)))
      .finally(() => setLoading(false));
  }, []);
  async function generate(e) {
    e?.preventDefault();
    if (!sectionId) return;
    if (!from || !to) {
      setError('Choose both a From and a To date.');
      return;
    }
    if (from > to) {
      setError('The From date must be on or before the To date.');
      return;
    }
    if (mode === 'defaulters' && !(threshold >= 1 && threshold <= 100)) {
      setError('Threshold must be between 1 and 100.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const path = mode === 'defaulters' ? '/reports/defaulters' : '/reports/student-wise';
      const { data } = await api.get(path, {
        params: {
          courseSectionId: sectionId,
          from,
          to,
          format: 'json',
          ...(mode === 'defaulters' ? { threshold } : {}),
        },
      });
      setRows(data.rows || []);
      setGenerated({ from, to, mode, threshold: data.threshold ?? threshold });
      notify('Report generated.', 'info');
    } catch (e) {
      setError(messageFrom(e));
    } finally {
      setBusy(false);
    }
  }
  async function download(format) {
    if (!sectionId) return;
    setBusy(true);
    setError('');
    try {
      const path = mode === 'defaulters' ? '/reports/defaulters' : '/reports/student-wise';
      const g = generated || { from, to, mode, threshold };
      const { data } = await api.get(path, {
        params: {
          courseSectionId: sectionId,
          from: g.from,
          to: g.to,
          format,
          ...(mode === 'defaulters' ? { threshold: g.threshold } : {}),
        },
        responseType: 'blob',
      });
      const url = URL.createObjectURL(data);
      const a = document.createElement('a');
      a.href = url;
      a.download = `sams-${mode}-${sectionId}.${format}`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      notify(`${format.toUpperCase()} downloaded.`);
    } catch (e) {
      setError(await blobErrorMessage(e));
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageHeading
        eyebrow="ATTENDANCE ANALYTICS"
        title="Reports"
        description="Generate student-wise attendance or identify students below the configured threshold."
      />
      <section className="panel report-controls">
        <div className="panel-head">
          <div>
            <h2>Report parameters</h2>
            <p>Select a section and date range before generating the report.</p>
          </div>
        </div>
        <ErrorState error={error} />
        <form className="report-form" onSubmit={generate} noValidate>
          <label>
            Course section
            <select
              value={sectionId}
              onChange={e => {
                setSectionId(e.target.value);
                setRows(null);
                setGenerated(null);
              }}
              required
            >
              <option value="">Choose a section</option>
              {sections.map(s => (
                <option value={s.id} key={s.id}>
                  {s.courseCode} · Section {s.section} — {s.courseName}
                </option>
              ))}
            </select>
          </label>
          <label>
            Report type
            <select
              value={mode}
              onChange={e => {
                setMode(e.target.value);
                setRows(null);
                setGenerated(null);
              }}
            >
              <option value="student-wise">Student-wise report</option>
              <option value="defaulters">Defaulter report</option>
            </select>
          </label>
          <label>
            From
            <input type="date" value={from} onChange={e => setFrom(e.target.value)} required />
          </label>
          <label>
            To
            <input type="date" value={to} onChange={e => setTo(e.target.value)} required min={from} />
          </label>
          {mode === 'defaulters' && (
            <label>
              Threshold (%)
              <input
                type="number"
                min="1"
                max="100"
                value={threshold}
                onChange={e => setThreshold(Number(e.target.value))}
              />
            </label>
          )}
          <button className="button primary" disabled={busy || loading || !sectionId}>
            {busy ? 'Generating…' : 'Generate report'} <span>→</span>
          </button>
        </form>
      </section>
      <section className="panel report-results">
        <div className="panel-head">
          <div>
            <h2>Results</h2>
            <p>
              {rows && generated
                ? `${rows.length} students · ${generated.from} to ${generated.to}`
                : 'Generate a report to see the results here.'}
            </p>
          </div>
          {rows && (
            <div className="button-row compact-buttons">
              <button className="button subtle small" disabled={busy} onClick={() => download('csv')}>
                ↓ CSV
              </button>
              <button className="button secondary small" disabled={busy} onClick={() => download('pdf')}>
                ↓ PDF
              </button>
            </div>
          )}
        </div>
        {loading ? (
          <LoadingLine />
        ) : rows === null ? (
          <EmptyState
            title="No report generated"
            description="Choose your parameters above and select Generate report."
          />
        ) : !rows.length ? (
          <EmptyState
            title="No matching attendance data"
            description="Try a wider date range or check whether attendance has been submitted."
          />
        ) : (
          <>
            <div className="stats-grid report-stats">
              <StatCard
                label="Students"
                value={rows.length}
                caption={
                  generated?.mode === 'defaulters' ? `Below ${generated.threshold}%` : 'In this report'
                }
                icon="♙"
              />
              <StatCard
                label="Average attendance"
                value={`${(rows.reduce((n, r) => n + Number(r.percentage || 0), 0) / rows.length).toFixed(2)}%`}
                caption="Mean of student percentages"
                icon="◷"
                tone="violet"
              />
              <StatCard
                label="Below threshold"
                value={rows.filter(r => Number(r.percentage) < (generated?.threshold ?? threshold)).length}
                caption={`Threshold ${generated?.threshold ?? threshold}%`}
                icon="!"
                tone="green"
              />
            </div>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Student</th>
                    <th>Held</th>
                    <th>Attended</th>
                    <th>Percentage</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map(r => (
                    <tr key={r.studentId}>
                      <td>
                        <strong>{r.name}</strong>
                        <small className="table-sub">
                          {r.studentId} · {r.email}
                        </small>
                      </td>
                      <td>{r.held}</td>
                      <td>{r.attended}</td>
                      <td>
                        <span
                          className={`percentage-badge ${Number(r.percentage) < (generated?.threshold ?? threshold) ? 'below' : ''}`}
                        >
                          {Number(r.percentage).toFixed(2)}%
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </section>
    </>
  );
}
