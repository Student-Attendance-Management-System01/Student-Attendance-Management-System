import { describe, expect, it, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderApp } from '../test/utils';

const section = { id: 10, courseCode: 'CS201', courseName: 'DBMS', section: 'A' };
const session = { id: 100, courseSectionId: 10, courseCode: 'CS201', courseName: 'DBMS', section: 'A', date: '2026-11-02', startTime: '10:00', endTime: '11:00', type: 'LECTURE', submitted: false };
const roster = {
  session, submitted: false,
  students: [
    { studentId: 'PES1UG24CS306', name: 'Nyssa Bansal', status: 'UNMARKED' },
    { studentId: 'PES1UG24CS101', name: 'Arjun Mehta', status: 'UNMARKED' }
  ]
};
const facultySetup = extra => m => {
  m.onGet('/course-sections').reply(200, [section]);
  m.onGet('/sessions').reply(200, [session]);
  m.onGet('/sessions/100/roster').reply(200, roster);
  extra?.(m);
};

describe('faculty attendance marking (SAMS-F-021/022, TC-ATT-02/03)', () => {
  it('keeps submit disabled until every student is marked, then posts the full roster', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const user = userEvent.setup();
    const { mock } = renderApp('/sessions', { signedInAs: 'FACULTY', setup: facultySetup(m => m.onPost('/sessions/100/attendance').reply(201, {})) });
    await user.click(await screen.findByRole('button', { name: /mark now/i }));
    const submit = await screen.findByRole('button', { name: /submit attendance/i });
    expect(submit).toBeDisabled();
    await user.selectOptions(screen.getByLabelText('Attendance status for Nyssa Bansal'), 'PRESENT');
    expect(submit).toBeDisabled();
    await user.selectOptions(screen.getByLabelText('Attendance status for Arjun Mehta'), 'OD');
    expect(submit).toBeEnabled();
    await user.click(submit);
    expect(await screen.findByText('Attendance submitted successfully.')).toBeInTheDocument();
    expect(JSON.parse(mock.history.post[0].data)).toEqual({
      records: [{ studentId: 'PES1UG24CS306', status: 'PRESENT' }, { studentId: 'PES1UG24CS101', status: 'OD' }]
    });
  });

  it('shows the API error and no success message when submission is rejected (TC-ATT-05)', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const user = userEvent.setup();
    renderApp('/sessions', { signedInAs: 'FACULTY', setup: facultySetup(m => m.onPost('/sessions/100/attendance').reply(409, { message: 'Attendance has already been submitted for this session' })) });
    await user.click(await screen.findByRole('button', { name: /mark now/i }));
    await user.click(await screen.findByRole('button', { name: /mark all present/i }));
    await user.click(screen.getByRole('button', { name: /submit attendance/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Attendance has already been submitted');
    expect(screen.queryByText('Attendance submitted successfully.')).not.toBeInTheDocument();
  });

  it('rejects a session whose end time is before its start time', async () => {
    const user = userEvent.setup();
    const { mock } = renderApp('/sessions', { signedInAs: 'FACULTY', setup: facultySetup() });
    await screen.findByRole('button', { name: /mark now/i });
    const end = screen.getByLabelText(/end time/i);
    await user.clear(end);
    await user.type(end, '08:00');
    await user.click(screen.getByRole('button', { name: /create session/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent('End time must be after the start time.');
    expect(mock.history.post).toHaveLength(0);
  });
});

describe('student attendance view (SAMS-F-030/032, TC-VIEW-01)', () => {
  const summary = {
    threshold: 75,
    courses: [
      { courseSectionId: 10, courseCode: 'CS201', courseName: 'DBMS', section: 'A', held: 40, attended: 31, percentage: 77.5, belowThreshold: false, sessions: [] },
      { courseSectionId: 11, courseCode: 'CS202', courseName: 'OS', section: 'A', held: 7, attended: 5, percentage: 71.43, belowThreshold: true, sessions: [{ attendanceRecordId: 9, date: '2026-10-30', type: 'LECTURE', status: 'ABSENT' }] }
    ]
  };

  it('shows percentages from the backend and flags the course below threshold', async () => {
    renderApp('/my-attendance', {
      signedInAs: 'STUDENT',
      setup: m => { m.onGet('/students/PES1UG24CS306/attendance').reply(200, summary); m.onGet('/corrections/mine').reply(200, []); }
    });
    expect(await screen.findByText('77.50%')).toBeInTheDocument();
    expect(screen.getByText('71.43%')).toHaveClass('below');
    expect(screen.getByText(/below the configured 75% threshold/i)).toBeInTheDocument();
  });

  it('only ever requests the signed-in student’s own attendance', async () => {
    const { mock } = renderApp('/my-attendance', {
      signedInAs: 'STUDENT',
      setup: m => { m.onGet('/students/PES1UG24CS306/attendance').reply(200, summary); m.onGet('/corrections/mine').reply(200, []); }
    });
    await screen.findByText('77.50%');
    const attendanceCalls = mock.history.get.filter(r => /\/students\//.test(r.url)).map(r => r.url);
    expect(new Set(attendanceCalls)).toEqual(new Set(['/students/PES1UG24CS306/attendance']));
  });

  it('submits a correction request for a session', async () => {
    const user = userEvent.setup();
    const { mock } = renderApp('/my-attendance', {
      signedInAs: 'STUDENT',
      setup: m => {
        m.onGet('/students/PES1UG24CS306/attendance').reply(200, summary);
        m.onGet('/corrections/mine').reply(200, []);
        m.onPost('/corrections').reply(201, { id: 1 });
      }
    });
    await user.click(await screen.findByRole('button', { name: /request correction/i }));
    await user.type(screen.getByLabelText(/reason/i), 'I was on OD for the hackathon.');
    await user.click(screen.getByRole('button', { name: /submit request/i }));
    expect(await screen.findByText(/correction request submitted/i)).toBeInTheDocument();
    expect(JSON.parse(mock.history.post[0].data)).toEqual({ attendanceRecordId: 9, requestedStatus: 'PRESENT', reason: 'I was on OD for the hackathon.' });
  });
});

describe('faculty correction decisions (SAMS-F-041, TC-COR-01)', () => {
  it('approves a pending request', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const user = userEvent.setup();
    const pending = [{ id: 7, courseCode: 'CS201', studentName: 'Nyssa Bansal', studentId: 'PES1UG24CS306', sessionDate: '2026-10-30', attendanceRecordId: 9, currentStatus: 'ABSENT', requestedStatus: 'OD', reason: 'OD letter', status: 'PENDING', requestedAt: '2026-10-31T10:00:00' }];
    const { mock } = renderApp('/corrections', { signedInAs: 'FACULTY', setup: m => { m.onGet('/corrections/pending').reply(200, pending); m.onPatch('/corrections/7').reply(200, {}); } });
    await user.click(await screen.findByRole('button', { name: /approve correction/i }));
    await waitFor(() => expect(mock.history.patch).toHaveLength(1));
    expect(JSON.parse(mock.history.patch[0].data)).toEqual({ status: 'APPROVED', decisionNote: '' });
  });
});
