import React, { useCallback, useEffect, useState } from 'react';
import { api, messageFrom } from '../api';
import { useNotice } from '../notice';
import { EmptyState, ErrorState, Field, LoadingLine, PageHeading, StatusPill } from '../components';
import { validateSection } from '../validation';

const BLANK = { courseCode: '', courseName: '', semester: '5', section: '', academicYear: '2026-27', facultyIds: [] };

export default function AdminCourses() {
  const { notify } = useNotice();
  const [sections, setSections] = useState([]);
  const [faculty, setFaculty] = useState([]);
  const [form, setForm] = useState(BLANK);
  const [fieldErrors, setFieldErrors] = useState({});
  const [selected, setSelected] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [createError, setCreateError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [s, f] = await Promise.all([api.get('/admin/course-sections'), api.get('/admin/faculty')]);
      setSections(s.data); setFaculty(f.data); setError('');
      setSelected(current => current || (s.data.length ? String(s.data[0].id) : ''));
    } catch (e) { setError(messageFrom(e)); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { load(); }, [load]);

  const update = (k, v) => setForm(old => ({ ...old, [k]: v }));

  async function create(e) {
    e.preventDefault();
    const errors = validateSection(form);
    setFieldErrors(errors); setCreateError('');
    if (Object.keys(errors).length) return;
    setBusy(true);
    try {
      const { data } = await api.post('/admin/course-sections', { ...form, facultyIds: form.facultyIds.map(Number) });
      setForm(BLANK); setSelected(String(data.id));
      notify(`Course section ${data.courseCode} · ${data.section} created.`);
      await load();
    } catch (e) { setCreateError(messageFrom(e)); }
    finally { setBusy(false); }
  }

  const current = sections.find(s => String(s.id) === selected);

  return <>
    <PageHeading eyebrow="ACADEMIC SETUP" title="Courses & rosters" description="Create course sections, assign teaching faculty and maintain class rosters." action={<span className="count-chip">{sections.length} sections</span>} />
    <ErrorState error={error} />
    <div className="two-column-layout">
      <section className="panel form-panel" aria-labelledby="new-section-title">
        <div className="panel-head"><div><h2 id="new-section-title">New course section</h2><p>A section needs at least one faculty member.</p></div></div>
        <ErrorState error={createError} />
        <form onSubmit={create} className="form-stack compact" noValidate>
          <div className="form-grid">
            <Field label="Course code" error={fieldErrors.courseCode}><input value={form.courseCode} onChange={e => update('courseCode', e.target.value)} placeholder="UE24CS351A" maxLength={32} aria-invalid={!!fieldErrors.courseCode} /></Field>
            <Field label="Semester" error={fieldErrors.semester}><input value={form.semester} onChange={e => update('semester', e.target.value)} placeholder="5" maxLength={24} aria-invalid={!!fieldErrors.semester} /></Field>
          </div>
          <Field label="Course name" error={fieldErrors.courseName}><input value={form.courseName} onChange={e => update('courseName', e.target.value)} placeholder="Database Management Systems" maxLength={160} aria-invalid={!!fieldErrors.courseName} /></Field>
          <div className="form-grid">
            <Field label="Section" error={fieldErrors.section}><input value={form.section} onChange={e => update('section', e.target.value)} placeholder="F" maxLength={24} aria-invalid={!!fieldErrors.section} /></Field>
            <Field label="Academic year" error={fieldErrors.academicYear}><input value={form.academicYear} onChange={e => update('academicYear', e.target.value)} placeholder="2026-27" maxLength={16} aria-invalid={!!fieldErrors.academicYear} /></Field>
          </div>
          <fieldset className="check-list" aria-invalid={!!fieldErrors.facultyIds}>
            <legend>Assign faculty</legend>
            {!faculty.length ? <p className="warning-text">Create at least one active Faculty account first.</p>
              : faculty.map(f => <label key={f.id} className="check-row"><input type="checkbox" checked={form.facultyIds.includes(String(f.id))} onChange={e => update('facultyIds', e.target.checked ? [...form.facultyIds, String(f.id)] : form.facultyIds.filter(x => x !== String(f.id)))} />{f.fullName} <span className="muted">· {f.institutionId}</span></label>)}
            {fieldErrors.facultyIds && <small className="field-error" role="alert">{fieldErrors.facultyIds}</small>}
          </fieldset>
          <button className="button primary full" disabled={busy || !faculty.length}>{busy ? 'Saving…' : 'Create course section'} <span aria-hidden="true">→</span></button>
        </form>
      </section>

      <section className="panel table-panel" aria-labelledby="sections-title">
        <div className="panel-head"><div><h2 id="sections-title">Current sections</h2><p>Select a section to manage its roster.</p></div><button className="button subtle small" onClick={load}>↻ Refresh</button></div>
        {loading ? <LoadingLine /> : !sections.length ? <EmptyState title="No course sections" description="Create the first section using the form." />
          : <div className="section-list">{sections.map(s => <button type="button" className={`section-row selectable ${String(s.id) === selected ? 'selected' : ''}`} key={s.id} onClick={() => setSelected(String(s.id))} aria-pressed={String(s.id) === selected}>
            <div className="section-symbol" aria-hidden="true">{s.courseCode.slice(0, 2)}</div>
            <div className="section-info">
              <div className="split-line"><strong>{s.courseCode} <span className="section-muted">· {s.section}</span></strong><StatusPill value={s.active ? 'Active' : 'Inactive'} /></div>
              <p>{s.courseName}</p><small>Semester {s.semester} · {s.academicYear}</small>
              <div className="faculty-chips">{s.faculty?.map(f => <span key={f.id}>{f.fullName}</span>)}</div>
            </div>
          </button>)}</div>}
      </section>
    </div>
    {current && <Roster section={current} />}
  </>;
}

/** Enrolment (single + CSV) and the current roster for one section. */
function Roster({ section }) {
  const { notify } = useNotice();
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [institutionId, setInstitutionId] = useState('');
  const [csv, setCsv] = useState(null);
  const [result, setResult] = useState(null);
  const [busy, setBusy] = useState(false);
  const [query, setQuery] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try { setStudents((await api.get(`/admin/course-sections/${section.id}/students`)).data); setError(''); }
    catch (e) { setError(messageFrom(e)); }
    finally { setLoading(false); }
  }, [section.id]);
  useEffect(() => { setResult(null); load(); }, [load]);

  async function enrol(e) {
    e.preventDefault();
    if (!institutionId.trim()) { setError('Enter the student’s institution ID.'); return; }
    setBusy(true); setError(''); setResult(null);
    try {
      const { data } = await api.post(`/admin/course-sections/${section.id}/enrolments`, { institutionId: institutionId.trim() });
      setInstitutionId('');
      notify(`Enrolled ${data.studentName} (${data.studentId}).`);
      await load();
    } catch (e) { setError(messageFrom(e)); }
    finally { setBusy(false); }
  }

  async function importCsv(e) {
    e.preventDefault();
    if (!csv) return;
    if (csv.size > 2 * 1024 * 1024) { setError('CSV must be 2 MB or smaller.'); return; }
    setBusy(true); setError(''); setResult(null);
    try {
      const body = new FormData(); body.append('file', csv);
      const { data } = await api.post(`/admin/course-sections/${section.id}/enrolments/import`, body);
      setResult(data); setCsv(null); e.target.reset();
      notify(`Import finished: ${data.imported} enrolled, ${data.rejected} rejected.`, data.rejected ? 'info' : 'success');
      await load();
    } catch (e) { setError(messageFrom(e)); }
    finally { setBusy(false); }
  }

  const q = query.trim().toLowerCase();
  const shown = students.filter(s => !q || `${s.fullName} ${s.institutionId} ${s.email}`.toLowerCase().includes(q));

  return <section className="panel roster-panel" aria-labelledby="roster-title">
    <div className="panel-head roster-head"><div><div className="eyebrow">CLASS ROSTER</div><h2 id="roster-title">{section.courseCode} · Section {section.section}</h2><p>{section.courseName} · {students.length} enrolled</p></div><button className="button subtle small" onClick={load}>↻ Refresh</button></div>
    <ErrorState error={error} />
    <div className="roster-forms">
      <form className="inline-form" onSubmit={enrol}>
        <label>Enroll a student<input value={institutionId} onChange={e => setInstitutionId(e.target.value)} placeholder="PES1UG24CS000" maxLength={64} /></label>
        <button className="button primary" disabled={busy}>Enroll</button>
      </form>
      <form className="inline-form" onSubmit={importCsv}>
        <label>Import CSV <small className="field-hint">Header <code>institutionId</code>, max 2 MB / 1,000 rows</small><input type="file" accept=".csv,text/csv" onChange={e => setCsv(e.target.files?.[0] || null)} /></label>
        <button className="button secondary" disabled={busy || !csv}>Import</button>
      </form>
    </div>
    {result && <div className="results-inline"><div className="split-line"><strong>Import result: {result.imported} enrolled, {result.rejected} rejected</strong><button className="icon-button" onClick={() => setResult(null)} aria-label="Dismiss import result">×</button></div>
      {result.errors?.length > 0 && <div className="table-wrap"><table><thead><tr><th>CSV row</th><th>Institution ID</th><th>Reason</th></tr></thead><tbody>{result.errors.map((r, i) => <tr key={i}><td>{r.row}</td><td>{r.institutionId || '—'}</td><td>{r.reason}</td></tr>)}</tbody></table></div>}
    </div>}
    <div className="search-row roster-search"><input type="search" value={query} onChange={e => setQuery(e.target.value)} placeholder="Search roster…" aria-label="Search roster" /></div>
    {loading ? <LoadingLine /> : !shown.length ? <EmptyState title={students.length ? 'No matching students' : 'No students enrolled'} description={students.length ? 'Try another search.' : 'Enroll students individually or import a CSV.'} />
      : <div className="table-wrap"><table><thead><tr><th>Student</th><th>Institution ID</th><th>Email</th></tr></thead><tbody>{shown.map(s => <tr key={s.id}><td><strong>{s.fullName}</strong></td><td>{s.institutionId}</td><td>{s.email}</td></tr>)}</tbody></table></div>}
  </section>;
}
