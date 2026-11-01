import { describe, expect, it, vi } from 'vitest';
import { act, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderApp, USERS } from '../test/utils';
import { loginErrorMessage } from '../auth';

async function signIn(user, email, password) {
  await user.type(await screen.findByLabelText(/institutional email/i), email);
  await user.type(screen.getByLabelText(/^password$/i), password);
  await user.click(screen.getByRole('button', { name: /sign in/i }));
}

describe('login (SAMS-F-001, TC-AUTH-01/02)', () => {
  it('blocks an empty submit on the client without calling the API', async () => {
    const user = userEvent.setup();
    const { mock } = renderApp('/login');
    await user.click(await screen.findByRole('button', { name: /sign in/i }));
    expect(screen.getByText('Enter your institutional email.')).toBeInTheDocument();
    expect(screen.getByText('Enter your password.')).toBeInTheDocument();
    expect(mock.history.post).toHaveLength(0);
  });

  it('shows the same generic error for wrong password and locked account (no account enumeration)', async () => {
    expect(loginErrorMessage({ response: { status: 401 } })).toBe(loginErrorMessage({ response: { status: 423 } }));
    const user = userEvent.setup();
    renderApp('/login', { setup: m => m.onPost('/auth/login').reply(401, { message: 'Invalid credentials' }) });
    await signIn(user, 'nobody@pes.edu', 'wrong');
    expect(await screen.findByRole('alert')).toHaveTextContent('Sign-in failed. Check your email and password, or try again later.');
    expect(screen.queryByText(/invalid credentials/i)).not.toBeInTheDocument();
  });

  it('explains rate limiting (SAMS-SEC-006)', async () => {
    const user = userEvent.setup();
    renderApp('/login', { setup: m => m.onPost('/auth/login').reply(429, {}) });
    await signIn(user, 'a@pes.edu', 'x');
    expect(await screen.findByRole('alert')).toHaveTextContent(/too many sign-in attempts/i);
  });

  it('signs in and returns the user to the page they asked for', async () => {
    const user = userEvent.setup();
    const { mock } = renderApp('/admin/users', {
      setup: m => m.onPost('/auth/login').reply(200, { ...USERS.ADMIN, expiresInSeconds: 900 })
    });
    expect(await screen.findByText('Welcome back.')).toBeInTheDocument();
    await signIn(user, ' admin@pes.edu ', 'Admin@123');
    expect(await screen.findByRole('heading', { name: 'User accounts' })).toBeInTheDocument();
    expect(JSON.parse(mock.history.post[0].data)).toEqual({ username: 'admin@pes.edu', password: 'Admin@123' });
    // CSRF cookie is requested before the login POST.
    expect(mock.history.get.some(r => r.url === '/auth/csrf')).toBe(true);
  });
});

describe('protected, role-aware routes (SAMS-SEC-001)', () => {
  it('redirects a signed-out visitor to the login page', async () => {
    renderApp('/my-attendance');
    expect(await screen.findByText('Welcome back.')).toBeInTheDocument();
  });

  it.each([
    ['STUDENT', '/admin/users'],
    ['STUDENT', '/reports'],
    ['STUDENT', '/sessions'],
    ['FACULTY', '/admin/courses'],
    ['FACULTY', '/audit'],
    ['ADMIN', '/sessions'],
    ['ADMIN', '/my-attendance']
  ])('%s opening %s sees Access denied', async (role, path) => {
    renderApp(path, { signedInAs: role });
    expect(await screen.findByText('Access denied')).toBeInTheDocument();
  });

  it.each([
    ['ADMIN', ['Overview', 'User accounts', 'Courses & rosters', 'Reports', 'Audit log'], ['Sessions & attendance', 'My attendance']],
    ['FACULTY', ['Overview', 'Sessions & attendance', 'Correction requests', 'Reports'], ['User accounts', 'Audit log', 'My attendance']],
    ['STUDENT', ['Overview', 'My attendance', 'My corrections'], ['User accounts', 'Reports', 'Audit log', 'Sessions & attendance']]
  ])('%s navigation shows only permitted sections', async (role, shown, hidden) => {
    renderApp('/', { signedInAs: role });
    const nav = await screen.findByRole('navigation', { name: /main navigation/i });
    for (const label of shown) expect(within(nav).getByRole('link', { name: new RegExp(label) })).toBeInTheDocument();
    for (const label of hidden) expect(within(nav).queryByRole('link', { name: new RegExp(label) })).not.toBeInTheDocument();
  });
});

describe('session handling (SAMS-F-004, TC-AUTH-05)', () => {
  it('logs out through the API and returns to login', async () => {
    const user = userEvent.setup();
    const { mock } = renderApp('/', { signedInAs: 'FACULTY', setup: m => m.onPost('/auth/logout').reply(204) });
    await user.click((await screen.findAllByRole('button', { name: /sign out/i }))[0]);
    expect(await screen.findByText('Welcome back.')).toBeInTheDocument();
    expect(mock.history.post.map(r => r.url)).toContain('/auth/logout');
  });

  it('treats a 401 from any protected call as an expired session', async () => {
    renderApp('/my-attendance', {
      signedInAs: 'STUDENT',
      setup: m => m.onGet(/\/students\/.*\/attendance/).reply(401, { message: 'Authentication required' })
    });
    expect(await screen.findByText('Welcome back.')).toBeInTheDocument();
    expect(screen.getByText(/your session has expired/i)).toBeInTheDocument();
  });

  it('signs out after 15 minutes without activity', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    try {
      const { mock } = renderApp('/', { signedInAs: 'STUDENT', setup: m => m.onPost('/auth/logout').reply(204) });
      await screen.findByRole('navigation', { name: /main navigation/i });
      await act(async () => { vi.advanceTimersByTime(14 * 60 * 1000); });
      expect(screen.queryByText('Welcome back.')).not.toBeInTheDocument();
      await act(async () => { vi.advanceTimersByTime(60 * 1000 + 50); });
      await waitFor(() => expect(screen.getByText('Welcome back.')).toBeInTheDocument());
      expect(screen.getByText(/signed out after 15 minutes of inactivity/i)).toBeInTheDocument();
      expect(mock.history.post.map(r => r.url)).toContain('/auth/logout');
    } finally {
      vi.useRealTimers();
    }
  });
});
