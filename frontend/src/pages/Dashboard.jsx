import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, messageFrom } from '../api';
import { useAuth } from '../auth';
import {
  PageHeading,
  StatCard,
  StatusPill,
  EmptyState,
  LoadingLine,
  ErrorState,
  localDate,
} from '../components';

const PATHS = {
  'my-attendance': '/my-attendance',
  sessions: '/sessions',
  courses: '/admin/courses',
  users: '/admin/users',
  audit: '/audit',
  reports: '/reports',
  corrections: '/corrections',
};
export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const onNavigate = key => navigate(PATHS[key] || '/');
  const [data, setData] = useState({
    sections: [],
    sessions: [],
    users: [],
    pending: [],
    summary: null,
    notice: null,
    loading: true,
  });
  useEffect(() => {
    let active = true;
    const failed = [];
    // Each figure loads independently; a failure shows "—" for that card and an error
    // banner instead of silently pretending the value is zero.
    const fetch = (label, path, fallback) =>
      api.get(path).then(
        r => r.data,
        e => {
          failed.push(`${label}: ${messageFrom(e)}`);
          return fallback;
        },
      );
    async function load() {
      const role = user.role;
      const [sections, sessions, users, extras] = await Promise.all([
        fetch('Course sections', '/course-sections', null),
        role === 'STUDENT' ? Promise.resolve([]) : fetch('Sessions', '/sessions', null),
        role === 'ADMIN' ? fetch('Users', '/admin/users', null) : Promise.resolve([]),
        role === 'ADMIN'
          ? fetch('Notifications', '/admin/notifications', null)
          : role === 'STUDENT'
            ? fetch('Attendance', `/students/${encodeURIComponent(user.userId)}/attendance`, null)
            : fetch('Corrections', '/corrections/pending', null),
      ]);
      if (!active) return;
      setData({
        sections: sections || [],
        sessions: sessions || [],
        users: users || [],
        pending: role === 'FACULTY' ? extras || [] : [],
        summary: role === 'STUDENT' ? extras : null,
        notice: role === 'ADMIN' ? extras : null,
        failed: { sections: !sections, sessions: !sessions, users: !users, extras: !extras },
        error: failed.length ? `Some figures could not be loaded. ${failed.join(' · ')}` : '',
        loading: false,
      });
    }
    load();
    return () => {
      active = false;
    };
  }, [user]);
  const today = localDate();
  return (
    <>
      <PageHeading
        eyebrow={`GOOD TO SEE YOU, ${user.fullName.split(' ')[0].toUpperCase()}`}
        title="Your workspace"
        description="A clear view of attendance activity and the next steps for your role."
        action={
          <span className="date-chip">
            ◷ &nbsp;
            {new Date().toLocaleDateString('en-IN', {
              weekday: 'short',
              day: '2-digit',
              month: 'short',
              year: 'numeric',
            })}
          </span>
        }
      />
      {data.loading ? (
        <LoadingLine />
      ) : (
        <>
          <ErrorState error={data.error} />
          <div className="stats-grid">
            <StatCard
              label={user.role === 'STUDENT' ? 'Enrolled courses' : 'Course sections'}
              value={data.failed?.sections ? '—' : data.sections.length}
              caption={user.role === 'STUDENT' ? 'Your current enrolments' : 'Accessible course sections'}
              icon="▦"
            />
            <StatCard
              label={
                user.role === 'ADMIN'
                  ? 'User accounts'
                  : user.role === 'FACULTY'
                    ? 'Class sessions'
                    : 'Recorded sessions'
              }
              value={
                (user.role === 'ADMIN' && data.failed?.users) ||
                (user.role === 'FACULTY' && data.failed?.sessions) ||
                (user.role === 'STUDENT' && data.failed?.extras)
                  ? '—'
                  : user.role === 'ADMIN'
                    ? data.users.length
                    : user.role === 'STUDENT'
                      ? (data.summary?.courses || []).reduce((n, c) => n + c.held, 0)
                      : data.sessions.length
              }
              caption={
                user.role === 'ADMIN'
                  ? 'Registered SAMS users'
                  : user.role === 'FACULTY'
                    ? 'Across your sections'
                    : 'Sessions with submitted attendance'
              }
              icon="◷"
              tone="violet"
            />
            <StatCard
              label={
                user.role === 'ADMIN'
                  ? 'Pending notifications'
                  : user.role === 'FACULTY'
                    ? 'Pending corrections'
                    : 'Attendance threshold'
              }
              value={
                data.failed?.extras
                  ? '—'
                  : user.role === 'ADMIN'
                    ? (data.notice?.pending ?? 0)
                    : user.role === 'FACULTY'
                      ? data.pending.length
                      : `${data.summary?.threshold ?? 75}%`
              }
              caption={
                user.role === 'ADMIN'
                  ? `${data.notice?.failed ?? 0} failed delivery items`
                  : user.role === 'FACULTY'
                    ? 'Requests awaiting your decision'
                    : 'Institutional minimum'
              }
              icon="↗"
              tone="green"
            />
          </div>
          <div className="dashboard-grid">
            <section className="panel main-panel">
              <div className="panel-head">
                <div>
                  <h2>
                    {user.role === 'STUDENT'
                      ? 'Attendance snapshot'
                      : user.role === 'FACULTY'
                        ? 'Your upcoming sessions'
                        : 'Course sections'}
                  </h2>
                  <p>
                    {user.role === 'STUDENT'
                      ? 'Your attendance, grouped by course.'
                      : user.role === 'FACULTY'
                        ? 'Open a session to mark or review the roster.'
                        : 'The course sections configured for your institution.'}
                  </p>
                </div>
                <button
                  className="button subtle small"
                  onClick={() =>
                    onNavigate(
                      user.role === 'STUDENT'
                        ? 'my-attendance'
                        : user.role === 'FACULTY'
                          ? 'sessions'
                          : 'courses',
                    )
                  }
                >
                  View all <span>→</span>
                </button>
              </div>
              {user.role === 'STUDENT' ? (
                !data.summary?.courses?.length ? (
                  <EmptyState
                    title="No course attendance yet"
                    description="Your course cards will appear once an administrator enrolls you."
                  />
                ) : (
                  <div className="course-card-list">
                    {data.summary.courses.slice(0, 5).map(c => (
                      <div className="course-progress" key={c.courseSectionId}>
                        <div className="course-code">{c.courseCode}</div>
                        <div className="course-progress-main">
                          <div className="split-line">
                            <strong>{c.courseName}</strong>
                            <b className={c.belowThreshold ? 'below' : 'good'}>
                              {c.held ? `${Number(c.percentage).toFixed(2)}%` : '—'}
                            </b>
                          </div>
                          <div className="progress-track">
                            <div
                              className={`progress-fill ${c.belowThreshold ? 'red' : ''}`}
                              style={{ width: `${Math.min(Number(c.percentage) || 0, 100)}%` }}
                            />
                          </div>
                          <small>
                            {c.attended} attended of {c.held} sessions · Section {c.section}
                          </small>
                        </div>
                      </div>
                    ))}
                  </div>
                )
              ) : user.role === 'FACULTY' ? (
                !data.sessions.length ? (
                  <EmptyState
                    title="No sessions created"
                    description="Create a class session for one of your assigned course sections."
                  />
                ) : (
                  <div className="table-wrap">
                    <table>
                      <thead>
                        <tr>
                          <th>Course</th>
                          <th>Date</th>
                          <th>Time</th>
                          <th>Status</th>
                          <th />
                        </tr>
                      </thead>
                      <tbody>
                        {data.sessions
                          .filter(s => s.date >= today || !s.submitted)
                          .slice(0, 6)
                          .map(s => (
                            <tr key={s.id}>
                              <td>
                                <strong>{s.courseCode}</strong>
                                <small className="table-sub">
                                  {s.courseName} · {s.section}
                                </small>
                              </td>
                              <td>{s.date}</td>
                              <td>
                                {s.startTime}–{s.endTime}
                              </td>
                              <td>
                                <StatusPill value={s.submitted ? 'Submitted' : 'Open'} />
                              </td>
                              <td>
                                <button className="text-link" onClick={() => onNavigate('sessions')}>
                                  Open →
                                </button>
                              </td>
                            </tr>
                          ))}
                      </tbody>
                    </table>
                  </div>
                )
              ) : !data.sections.length ? (
                <EmptyState
                  title="No sections configured"
                  description="Create a course section and assign faculty to start managing attendance."
                />
              ) : (
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Course</th>
                        <th>Term</th>
                        <th>Faculty</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data.sections.slice(0, 6).map(s => (
                        <tr key={s.id}>
                          <td>
                            <strong>{s.courseCode}</strong>
                            <small className="table-sub">{s.courseName}</small>
                          </td>
                          <td>
                            {s.semester} · {s.section}
                            <small className="table-sub">{s.academicYear}</small>
                          </td>
                          <td>{s.faculty?.map(f => f.fullName).join(', ') || '—'}</td>
                          <td>
                            <StatusPill value={s.active ? 'Active' : 'Inactive'} />
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
            <section className="panel quick-panel">
              <div className="panel-head">
                <div>
                  <h2>Quick actions</h2>
                  <p>Jump into common tasks.</p>
                </div>
              </div>
              <div className="quick-list">
                {user.role === 'ADMIN' && (
                  <>
                    <Quick
                      title="Manage user accounts"
                      desc="Create users and maintain roles."
                      icon="♙"
                      onClick={() => onNavigate('users')}
                    />
                    <Quick
                      title="Set up a course section"
                      desc="Assign faculty and enroll students."
                      icon="▦"
                      onClick={() => onNavigate('courses')}
                    />
                    <Quick
                      title="Review system audit"
                      desc="Check traceable actions and events."
                      icon="⌕"
                      onClick={() => onNavigate('audit')}
                    />
                    <Quick
                      title="Generate reports"
                      desc="Download attendance in PDF or CSV."
                      icon="▤"
                      onClick={() => onNavigate('reports')}
                    />
                  </>
                )}
                {user.role === 'FACULTY' && (
                  <>
                    <Quick
                      title="Mark attendance"
                      desc="Create a session and complete its roster."
                      icon="✓"
                      onClick={() => onNavigate('sessions')}
                    />
                    <Quick
                      title="Review corrections"
                      desc="Respond to student correction requests."
                      icon="↺"
                      onClick={() => onNavigate('corrections')}
                    />
                    <Quick
                      title="Generate a report"
                      desc="See student-wise attendance and defaulters."
                      icon="▤"
                      onClick={() => onNavigate('reports')}
                    />
                  </>
                )}
                {user.role === 'STUDENT' && (
                  <>
                    <Quick
                      title="View my attendance"
                      desc="Review percentages and each session."
                      icon="✓"
                      onClick={() => onNavigate('my-attendance')}
                    />
                    <Quick
                      title="My correction requests"
                      desc="Track requests sent to your faculty."
                      icon="↺"
                      onClick={() => onNavigate('corrections')}
                    />
                  </>
                )}
              </div>
              <div className="tip-box">
                <span>✦</span>
                <div>
                  <b>Keep your records accurate</b>
                  <p>Every attendance change is traceable in the system audit trail.</p>
                </div>
              </div>
            </section>
          </div>
        </>
      )}
    </>
  );
}
function Quick({ title, desc, icon, onClick }) {
  return (
    <button className="quick-action" onClick={onClick}>
      <span className="quick-icon">{icon}</span>
      <span className="quick-copy">
        <b>{title}</b>
        <small>{desc}</small>
      </span>
      <span className="quick-arrow">↗</span>
    </button>
  );
}
