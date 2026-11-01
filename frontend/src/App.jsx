import React from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import AppShell from './layout/AppShell.jsx';
import Home from './pages/Home.jsx';
import NotFound from './pages/NotFound.jsx';
import { NoticeProvider } from './notice';

// Application shell only. Authentication and role-aware routes are added in #14.
export default function App() {
  return <NoticeProvider>
    <BrowserRouter>
      <Routes>
        <Route element={<AppShell user={null} />}>
          <Route index element={<Home />} />
          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </BrowserRouter>
  </NoticeProvider>;
}
