import axios from 'axios';

// Base URL comes from the build environment (VITE_API_BASE_URL). It defaults to
// the same-origin path served by nginx / the Vite proxy. No secrets live here.
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  timeout: 20000
});

/** Turns an axios error into a message that is safe to show to the user. */
export function messageFrom(error) {
  const data = error?.response?.data;
  if (data?.details && typeof data.details === 'object') {
    const fields = Object.entries(data.details).map(([field, msg]) => `${field}: ${msg}`);
    if (fields.length) return `${data.message || 'Validation failed'} — ${fields.join('; ')}`;
  }
  if (data?.message) return data.message;
  if (error?.code === 'ECONNABORTED') return 'The server took too long to respond. Please try again.';
  if (error?.request && !error?.response) return 'Cannot reach the SAMS server. Check your connection and try again.';
  return 'Something went wrong. Please try again.';
}
