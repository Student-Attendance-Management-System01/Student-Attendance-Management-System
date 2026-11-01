import React from 'react';

export function PageHeading({ eyebrow, title, description, action }) {
  return <div className="page-heading"><div><div className="eyebrow">{eyebrow || 'SAMS WORKSPACE'}</div><h1>{title}</h1>{description && <p>{description}</p>}</div>{action}</div>;
}
export function StatCard({ label, value, caption, icon, tone = '' }) {
  return <div className={`stat-card ${tone}`}><div className="stat-top"><span>{label}</span><span className="stat-icon" aria-hidden="true">{icon || '◈'}</span></div><strong>{value ?? '—'}</strong><small>{caption || 'Updated from your workspace'}</small></div>;
}
export function EmptyState({ title = 'Nothing here yet', description = 'Data will appear here when it becomes available.', children }) {
  return <div className="empty-state"><div className="empty-icon" aria-hidden="true">◇</div><strong>{title}</strong><p>{description}</p>{children}</div>;
}
export function ErrorState({ error }) {
  return error ? <div className="inline-error" role="alert">{error}</div> : null;
}
export function LoadingLine({ label = 'Loading data…' }) {
  return <div className="loading-line" role="status"><span aria-hidden="true" /> {label}</div>;
}
export function StatusPill({ value }) {
  const key = String(value || '').toLowerCase().replace(/\s+/g, '-');
  return <span className={`status-pill status-${key}`}>{value}</span>;
}
export function fmtDate(value) {
  if (!value) return '—';
  try { return new Date(value).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }); } catch { return value; }
}

/** Labelled form field with an inline validation message. */
export function Field({ label, error, hint, children }) {
  return <label>{label}{children}{hint && <small className="field-hint">{hint}</small>}{error && <small className="field-error" role="alert">{error}</small>}</label>;
}

/** Accessible modal dialog. */
export function Modal({ title, eyebrow, onClose, children }) {
  React.useEffect(() => {
    const onKey = e => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);
  return <div className="modal-backdrop" role="presentation" onMouseDown={e => { if (e.target === e.currentTarget) onClose(); }}>
    <section className="modal-card" role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <button className="icon-button modal-close" onClick={onClose} aria-label="Close">×</button>
      {eyebrow && <div className="eyebrow">{eyebrow}</div>}
      <h2 id="modal-title">{title}</h2>
      {children}
    </section>
  </div>;
}

/** YYYY-MM-DD in the browser's local time zone (toISOString would give the UTC date, which is wrong before 05:30 IST). */
export function localDate(d = new Date()) {
  const pad = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
