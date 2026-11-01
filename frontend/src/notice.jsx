import React, { createContext, useCallback, useContext, useRef, useState } from 'react';

const NoticeContext = createContext({ notice: null, notify: () => {}, dismiss: () => {} });

/** App-wide toast notice (success / info / error). */
export function NoticeProvider({ children }) {
  const [notice, setNotice] = useState(null);
  const timer = useRef(null);
  const dismiss = useCallback(() => setNotice(null), []);
  const notify = useCallback((text, type = 'success') => {
    window.clearTimeout(timer.current);
    setNotice({ text, type });
    timer.current = window.setTimeout(() => setNotice(null), 4500);
  }, []);
  return <NoticeContext.Provider value={{ notice, notify, dismiss }}>{children}</NoticeContext.Provider>;
}

export function useNotice() { return useContext(NoticeContext); }

export function NoticeBar() {
  const { notice, dismiss } = useNotice();
  if (!notice) return null;
  const icon = notice.type === 'success' ? '✓' : notice.type === 'error' ? '!' : 'i';
  return <div className={`notice ${notice.type}`} role="status"><span aria-hidden="true">{icon}</span>{notice.text}<button onClick={dismiss} aria-label="Dismiss notification">×</button></div>;
}
