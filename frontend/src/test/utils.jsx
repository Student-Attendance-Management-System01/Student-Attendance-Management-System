import React from 'react';
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import MockAdapter from 'axios-mock-adapter';
import { api } from '../api';
import { AuthProvider } from '../auth';
import { NoticeProvider } from '../notice';
import { AppRoutes, ROUTER_FUTURE } from '../App.jsx';

export const USERS = {
  ADMIN: { userId: 'ADM001', fullName: 'Rahul Dutta', email: 'admin@pes.edu', role: 'ADMIN' },
  FACULTY: { userId: 'FAC001', fullName: 'Meera Rao', email: 'meera@pes.edu', role: 'FACULTY' },
  STUDENT: { userId: 'PES1UG24CS306', fullName: 'Nyssa Bansal', email: 'nyssa@pes.edu', role: 'STUDENT' }
};

/**
 * Renders the real route tree against a mocked API.
 * `signedInAs` = role name to simulate an existing session, or null for signed out.
 * Returns the axios mock so each test can add the endpoints it needs.
 */
export function renderApp(path = '/', { signedInAs = null, setup } = {}) {
  const mock = new MockAdapter(api);
  mock.onGet('/auth/csrf').reply(200, { token: 't' });
  if (signedInAs) mock.onGet('/auth/me').reply(200, USERS[signedInAs]);
  else mock.onGet('/auth/me').reply(401, { status: 401, message: 'Authentication required' });
  if (setup) setup(mock);
  // Anything a test did not set up answers with an empty list so screens render quietly.
  mock.onGet().reply(200, []);
  const utils = render(
    <NoticeProvider>
      <AuthProvider>
        <MemoryRouter initialEntries={[path]} future={ROUTER_FUTURE}>
          <AppRoutes />
        </MemoryRouter>
      </AuthProvider>
    </NoticeProvider>
  );
  return { mock, ...utils };
}
