import { describe, expect, it } from 'vitest';
import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderApp } from '../test/utils';

const rows = [
  { studentId: 'S1', name: 'A Student', email: 'a@pes.edu', held: 20, attended: 17, percentage: 85.0 },
  { studentId: 'S2', name: 'B Student', email: 'b@pes.edu', held: 20, attended: 13, percentage: 65.0 }
];
const setup = extra => m => {
  m.onGet('/course-sections').reply(200, [{ id: 10, courseCode: 'CS201', courseName: 'DBMS', section: 'A' }]);
  extra?.(m);
};

describe('reports (SAMS-F-050/051, TC-REP-01)', () => {
  it('totals match the rows returned by the backend', async () => {
    const user = userEvent.setup();
    renderApp('/reports', { signedInAs: 'FACULTY', setup: setup(m => m.onGet('/reports/student-wise').reply(200, { rows })) });
    await user.click(await screen.findByRole('button', { name: /generate report/i }));
    await screen.findByText('A Student');
    const card = label => screen.getByText(label).closest('.stat-card');
    expect(within(card('Students')).getByText('2')).toBeInTheDocument();
    expect(within(card('Average attendance')).getByText('75.00%')).toBeInTheDocument();
    expect(within(card('Below threshold')).getByText('1')).toBeInTheDocument();
  });

  it('sends the chosen threshold for the defaulter report', async () => {
    const user = userEvent.setup();
    const { mock } = renderApp('/reports', { signedInAs: 'ADMIN', setup: setup(m => m.onGet('/reports/defaulters').reply(200, { rows: [rows[1]] })) });
    await screen.findByRole('option', { name: /CS201/ });
    await user.selectOptions(screen.getByLabelText(/report type/i), 'defaulters');
    const threshold = screen.getByLabelText(/threshold/i);
    await user.clear(threshold);
    await user.type(threshold, '70');
    await user.click(screen.getByRole('button', { name: /generate report/i }));
    await screen.findByText('B Student');
    const call = mock.history.get.find(r => r.url === '/reports/defaulters');
    expect(call.params).toMatchObject({ courseSectionId: '10', threshold: 70, format: 'json' });
  });

  it('rejects a From date after the To date without calling the API', async () => {
    const user = userEvent.setup();
    const { mock } = renderApp('/reports', { signedInAs: 'FACULTY', setup: setup() });
    await screen.findByRole('option', { name: /CS201/ });
    const from = screen.getByLabelText(/^from$/i);
    await user.clear(from);
    await user.type(from, '2099-01-01');
    await user.click(screen.getByRole('button', { name: /generate report/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent('The From date must be on or before the To date.');
    expect(mock.history.get.some(r => r.url.startsWith('/reports'))).toBe(false);
  });

  it('shows a readable error when an export fails (TC-REP-02)', async () => {
    const user = userEvent.setup();
    renderApp('/reports', {
      signedInAs: 'FACULTY',
      setup: setup(m => m.onGet('/reports/student-wise').reply(config =>
        config.params.format === 'json' ? [200, { rows }] : [403, new Blob([JSON.stringify({ message: 'You are not permitted to perform this action' })], { type: 'application/json' })]))
    });
    await user.click(await screen.findByRole('button', { name: /generate report/i }));
    await user.click(await screen.findByRole('button', { name: /csv/i }));
    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('You are not permitted to perform this action'));
  });
});

describe('dashboard (failure handling)', () => {
  it('shows a dash and an error instead of a misleading zero when a figure fails to load', async () => {
    renderApp('/', {
      signedInAs: 'ADMIN',
      setup: m => { m.onGet('/admin/users').reply(500, { message: 'An unexpected error occurred' }); m.onGet('/admin/notifications').reply(200, { pending: 2, failed: 0 }); }
    });
    expect(await screen.findByText(/some figures could not be loaded/i)).toBeInTheDocument();
    const usersCard = screen.getByText('User accounts', { selector: '.stat-top span' }).closest('.stat-card');
    expect(within(usersCard).getByText('—')).toBeInTheDocument();
  });
});
