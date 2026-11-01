import { describe, expect, it } from 'vitest';
import { passwordProblems, validateNewUser, validateSection, validateUserUpdate } from '../validation';
import { safeReturnPath, validateLogin } from '../pages/Login.jsx';
import { messageFrom } from '../api';
import { localDate } from '../components';

describe('password policy (SAMS-SEC-002, TC-SEC-02)', () => {
  it.each([
    ['abc123', true],
    ['password', true],
    ['Passw0rd', true],
    ['Pass@w0rd1', false]
  ])('"%s" rejected=%s', (pw, rejected) => {
    expect(passwordProblems(pw).length > 0).toBe(rejected);
  });
});

describe('login form validation', () => {
  it('requires email and password', () => {
    expect(validateLogin('', '')).toEqual({ email: expect.any(String), password: expect.any(String) });
  });
  it('rejects a malformed email', () => {
    expect(validateLogin('not-an-email', 'x').email).toMatch(/valid email/);
  });
  it('accepts a well-formed pair', () => {
    expect(validateLogin('a@pes.edu', 'secret')).toEqual({});
  });
});

describe('user forms (SAMS-F-010)', () => {
  const ok = { institutionId: 'PES1UG24CS999', fullName: 'Test User', email: 't@pes.edu', role: 'STUDENT', password: 'Pass@w0rd1' };
  it('accepts a valid new user', () => expect(validateNewUser(ok)).toEqual({}));
  it('flags every bad field', () => {
    const errors = validateNewUser({ institutionId: '<x>', fullName: ' ', email: 'bad', role: 'STUDENT', password: 'short' });
    expect(Object.keys(errors).sort()).toEqual(['email', 'fullName', 'institutionId', 'password']);
  });
  it('validates edits', () => {
    expect(validateUserUpdate({ fullName: '', email: 'x' })).toHaveProperty('email');
  });
});

describe('course section form (SAMS-F-012, TC-USR-02)', () => {
  const ok = { courseCode: 'CS201', courseName: 'DBMS', semester: '3', section: 'A', academicYear: '2026-27', facultyIds: ['2'] };
  it('accepts a valid section', () => expect(validateSection(ok)).toEqual({}));
  it('rejects a section without faculty', () => expect(validateSection({ ...ok, facultyIds: [] })).toHaveProperty('facultyIds'));
  it('checks academic year format', () => expect(validateSection({ ...ok, academicYear: '2026' })).toHaveProperty('academicYear'));
});

describe('API error messages (SAMS-SEC-009)', () => {
  it('shows field details from validation errors', () => {
    const e = { response: { data: { message: 'Validation failed', details: { email: 'must be a well-formed email address' } } } };
    expect(messageFrom(e)).toBe('Validation failed — email: must be a well-formed email address');
  });
  it('never surfaces raw axios/network internals', () => {
    expect(messageFrom({ message: 'Network Error', request: {} })).toMatch(/Cannot reach the SAMS server/);
    expect(messageFrom(new Error('TypeError: x is undefined'))).toBe('Something went wrong. Please try again.');
  });
});

describe('localDate', () => {
  it('uses the local calendar day', () => {
    expect(localDate(new Date(2026, 10, 2, 1, 30))).toBe('2026-11-02');
  });
});

describe('post-login redirect (open-redirect hardening)', () => {
  it.each([
    ['/admin/users', '/admin/users'],
    ['/reports?x=1', '/reports?x=1'],
    ['//evil.example', '/'],
    ['/\\evil.example', '/'],
    ['https://evil.example', '/'],
    ['/login', '/'],
    [undefined, '/']
  ])('%s -> %s', (from, expected) => expect(safeReturnPath(from)).toBe(expected));
});
