import { describe, expect, it } from 'vitest';
import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderApp } from '../test/utils';

const users = [
  { id: 1, institutionId: 'ADM001', fullName: 'Rahul Dutta', email: 'admin@pes.edu', role: 'ADMIN', active: true },
  { id: 2, institutionId: 'FAC001', fullName: 'Meera Rao', email: 'meera@pes.edu', role: 'FACULTY', active: true },
  { id: 3, institutionId: 'PES1UG24CS306', fullName: 'Nyssa Bansal', email: 'nyssa@pes.edu', role: 'STUDENT', active: true }
];

describe('user management (SAMS-F-010/011, TC-USR-01)', () => {
  it('blocks invalid input on the client and does not call the API', async () => {
    const user = userEvent.setup();
    const { mock } = renderApp('/admin/users', { signedInAs: 'ADMIN', setup: m => m.onGet('/admin/users').reply(200, users) });
    await screen.findByText('Nyssa Bansal');
    await user.type(screen.getByLabelText(/initial password/i), 'abc123');
    await user.click(screen.getByRole('button', { name: /create account/i }));
    expect(screen.getByText('Institution ID is required.')).toBeInTheDocument();
    expect(screen.getByText(/password needs/i)).toBeInTheDocument();
    expect(mock.history.post.filter(r => r.url === '/admin/users')).toHaveLength(0);
  });

  it('creates a user with trimmed values and shows success only after the API confirms', async () => {
    const user = userEvent.setup();
    const { mock } = renderApp('/admin/users', {
      signedInAs: 'ADMIN',
      setup: m => {
        m.onGet('/admin/users').reply(200, users);
        m.onPost('/admin/users').reply(201, { id: 9 });
      }
    });
    await screen.findByText('Nyssa Bansal');
    await user.type(screen.getByLabelText(/institution id/i), ' PES1UG24CS999 ');
    await user.type(screen.getByLabelText(/full name/i), 'Test Student');
    await user.type(screen.getByLabelText(/institutional email/i), 'test@pes.edu');
    await user.type(screen.getByLabelText(/initial password/i), 'Pass@w0rd1');
    await user.click(screen.getByRole('button', { name: /create account/i }));
    expect(await screen.findByText('User account created.')).toBeInTheDocument();
    expect(JSON.parse(mock.history.post.find(r => r.url === '/admin/users').data)).toMatchObject({ institutionId: 'PES1UG24CS999', role: 'STUDENT' });
  });

  it('shows a duplicate-ID conflict from the backend', async () => {
    const user = userEvent.setup();
    renderApp('/admin/users', {
      signedInAs: 'ADMIN',
      setup: m => {
        m.onGet('/admin/users').reply(200, users);
        m.onPost('/admin/users').reply(409, { code: 'SAMS-USR-409', message: 'institutionId already exists' });
      }
    });
    await screen.findByText('Nyssa Bansal');
    await user.type(screen.getByLabelText(/institution id/i), 'PES1UG24CS306');
    await user.type(screen.getByLabelText(/full name/i), 'Dup');
    await user.type(screen.getByLabelText(/institutional email/i), 'dup@pes.edu');
    await user.type(screen.getByLabelText(/initial password/i), 'Pass@w0rd1');
    await user.click(screen.getByRole('button', { name: /create account/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent('institutionId already exists');
    expect(screen.queryByText('User account created.')).not.toBeInTheDocument();
  });

  it('edits a user through PUT and filters by role', async () => {
    const user = userEvent.setup();
    const { mock } = renderApp('/admin/users', {
      signedInAs: 'ADMIN',
      setup: m => {
        m.onGet('/admin/users').reply(200, users);
        m.onPut('/admin/users/3').reply(200, {});
      }
    });
    await screen.findByText('Nyssa Bansal');
    await user.click(screen.getByRole('tab', { name: /faculty/i }));
    expect(screen.queryByText('Nyssa Bansal')).not.toBeInTheDocument();
    await user.click(screen.getByRole('tab', { name: /all/i }));
    await user.click(screen.getByRole('button', { name: 'Edit Nyssa Bansal' }));
    const dialog = screen.getByRole('dialog');
    const name = within(dialog).getByLabelText(/full name/i);
    await user.clear(name);
    await user.type(name, 'Nyssa B');
    await user.click(within(dialog).getByRole('button', { name: /save changes/i }));
    await waitFor(() => expect(mock.history.put).toHaveLength(1));
    expect(JSON.parse(mock.history.put[0].data)).toMatchObject({ fullName: 'Nyssa B', role: 'STUDENT', active: true });
  });

  it('does not offer to deactivate the signed-in admin', async () => {
    renderApp('/admin/users', { signedInAs: 'ADMIN', setup: m => m.onGet('/admin/users').reply(200, users) });
    await screen.findByText('Nyssa Bansal');
    expect(screen.queryByRole('button', { name: 'Deactivate Rahul Dutta' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Deactivate Nyssa Bansal' })).toBeInTheDocument();
  });
});

describe('courses and rosters (SAMS-F-012/013, TC-USR-02/03)', () => {
  const sections = [{ id: 10, courseCode: 'CS201', courseName: 'DBMS', semester: '3', section: 'A', academicYear: '2026-27', active: true, faculty: [{ id: 2, fullName: 'Meera Rao' }] }];
  const setup = m => {
    m.onGet('/admin/course-sections').reply(200, sections);
    m.onGet('/admin/faculty').reply(200, [{ id: 2, institutionId: 'FAC001', fullName: 'Meera Rao' }]);
    m.onGet('/admin/course-sections/10/students').reply(200, [{ id: 3, institutionId: 'PES1UG24CS306', fullName: 'Nyssa Bansal', email: 'nyssa@pes.edu' }]);
  };

  it('requires a faculty member before creating a section', async () => {
    const user = userEvent.setup();
    const { mock } = renderApp('/admin/courses', { signedInAs: 'ADMIN', setup });
    await screen.findByText('DBMS');
    await user.type(screen.getByLabelText(/course code/i), 'CS202');
    await user.type(screen.getByLabelText(/course name/i), 'OS');
    await user.type(screen.getByLabelText(/^section$/i), 'B');
    await user.click(screen.getByRole('button', { name: /create course section/i }));
    expect(screen.getByText('Assign at least one faculty member.')).toBeInTheDocument();
    expect(mock.history.post).toHaveLength(0);
    await user.click(screen.getByRole('checkbox', { name: /meera rao/i }));
    mock.onPost('/admin/course-sections').reply(201, { id: 11, courseCode: 'CS202', section: 'B' });
    await user.click(screen.getByRole('button', { name: /create course section/i }));
    await waitFor(() => expect(mock.history.post).toHaveLength(1));
    expect(JSON.parse(mock.history.post[0].data).facultyIds).toEqual([2]);
  });

  it('shows the roster and a duplicate-enrolment error', async () => {
    const user = userEvent.setup();
    renderApp('/admin/courses', {
      signedInAs: 'ADMIN',
      setup: m => { setup(m); m.onPost('/admin/course-sections/10/enrolments').reply(409, { message: 'Student is already enrolled in this section' }); }
    });
    expect(await screen.findByText('PES1UG24CS306')).toBeInTheDocument();
    await user.type(screen.getByLabelText(/enroll a student/i), 'PES1UG24CS306');
    await user.click(screen.getByRole('button', { name: /^enroll$/i }));
    expect(await screen.findByText('Student is already enrolled in this section')).toBeInTheDocument();
  });

  it('reports rejected CSV rows from an import', async () => {
    const user = userEvent.setup();
    renderApp('/admin/courses', {
      signedInAs: 'ADMIN',
      setup: m => { setup(m); m.onPost('/admin/course-sections/10/enrolments/import').reply(200, { imported: 47, rejected: 1, errors: [{ row: 5, institutionId: 'BAD', reason: 'Student not found' }] }); }
    });
    await screen.findByText('PES1UG24CS306');
    const file = new File(['institutionId\nBAD\n'], 'mixed.csv', { type: 'text/csv' });
    await user.upload(screen.getByLabelText(/import csv/i), file);
    await user.click(screen.getByRole('button', { name: /^import$/i }));
    expect((await screen.findAllByText(/47 enrolled, 1 rejected/)).length).toBeGreaterThan(0);
    expect(screen.getAllByText('Student not found').length).toBeGreaterThan(0);
  });
});
