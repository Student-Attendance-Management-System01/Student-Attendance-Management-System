export const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// Mirrors the backend PasswordPolicy so users see problems before submitting.
export function passwordProblems(pw) {
  const problems = [];
  if (pw.length < 8) problems.push('at least 8 characters');
  if (!/[A-Z]/.test(pw)) problems.push('an uppercase letter');
  if (!/[a-z]/.test(pw)) problems.push('a lowercase letter');
  if (!/\d/.test(pw)) problems.push('a number');
  if (!/[^A-Za-z0-9]/.test(pw)) problems.push('a special character');
  return problems;
}

/** Client-side checks for the create-user form. Returns { field: message }. */
export function validateNewUser(form) {
  const errors = {};
  if (!form.institutionId.trim()) errors.institutionId = 'Institution ID is required.';
  else if (!/^[A-Za-z0-9-]{3,64}$/.test(form.institutionId.trim())) errors.institutionId = 'Use 3–64 letters, digits or hyphens.';
  if (!form.fullName.trim()) errors.fullName = 'Full name is required.';
  if (!EMAIL_RE.test(form.email.trim())) errors.email = 'Enter a valid email address.';
  const problems = passwordProblems(form.password);
  if (problems.length) errors.password = `Password needs ${problems.join(', ')}.`;
  return errors;
}

/** Client-side checks for the edit-user form. */
export function validateUserUpdate(form) {
  const errors = {};
  if (!form.fullName.trim()) errors.fullName = 'Full name is required.';
  if (!EMAIL_RE.test(form.email.trim())) errors.email = 'Enter a valid email address.';
  return errors;
}

/** Client-side checks for the new course section form. */
export function validateSection(form) {
  const errors = {};
  if (!form.courseCode.trim()) errors.courseCode = 'Course code is required.';
  if (!form.courseName.trim()) errors.courseName = 'Course name is required.';
  if (!form.semester.trim()) errors.semester = 'Semester is required.';
  if (!form.section.trim()) errors.section = 'Section is required.';
  if (!/^\d{4}-\d{2}$/.test(form.academicYear.trim())) errors.academicYear = 'Use the format 2026-27.';
  if (!form.facultyIds.length) errors.facultyIds = 'Assign at least one faculty member.';
  return errors;
}
