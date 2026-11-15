import React from 'react';
import { Link } from 'react-router-dom';
import { EmptyState } from '../components';

export default function AccessDenied() {
  return <section className="panel main-panel" aria-labelledby="denied-title">
    <EmptyState title={<span id="denied-title">Access denied</span>} description="Your role does not have permission to open this page. If you think this is a mistake, contact your department administrator.">
      <Link className="button primary small empty-action" to="/">Back to overview</Link>
    </EmptyState>
  </section>;
}
