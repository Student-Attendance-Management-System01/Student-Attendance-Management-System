// Sidebar entries per backend role. This only controls what is *shown*;
// the backend's role checks remain the security boundary.
export const NAV = {
  ADMIN: [
    { to: '/', label: 'Overview', icon: '⌂' }
  ],
  FACULTY: [
    { to: '/', label: 'Overview', icon: '⌂' }
  ],
  STUDENT: [
    { to: '/', label: 'Overview', icon: '⌂' }
  ]
};

export function navFor(role) { return NAV[role] || []; }

export function roleLabel(role) {
  return ({ ADMIN: 'Administrator', FACULTY: 'Faculty', STUDENT: 'Student' })[role] || role || 'Guest';
}
