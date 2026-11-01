// Sidebar entries per backend role. This only controls what is *shown*;
// the backend's role checks remain the security boundary.
export const NAV = {
  ADMIN: [
    { to: '/', label: 'Overview', icon: '⌂' },
    { to: '/admin/users', label: 'User accounts', icon: '♙' },
    { to: '/admin/courses', label: 'Courses & rosters', icon: '▦' },
    { to: '/reports', label: 'Reports', icon: '▤' },
    { to: '/audit', label: 'Audit log', icon: '⌕' }
  ],
  FACULTY: [
    { to: '/', label: 'Overview', icon: '⌂' },
    { to: '/sessions', label: 'Sessions & attendance', icon: '✓' },
    { to: '/corrections', label: 'Correction requests', icon: '↺' },
    { to: '/reports', label: 'Reports', icon: '▤' }
  ],
  STUDENT: [
    { to: '/', label: 'Overview', icon: '⌂' },
    { to: '/my-attendance', label: 'My attendance', icon: '✓' },
    { to: '/corrections', label: 'My corrections', icon: '↺' }
  ]
};

export function navFor(role) { return NAV[role] || []; }

export function roleLabel(role) {
  return ({ ADMIN: 'Administrator', FACULTY: 'Faculty', STUDENT: 'Student' })[role] || role || 'Guest';
}
