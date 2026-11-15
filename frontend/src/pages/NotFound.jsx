import React from 'react';
import { Link } from 'react-router-dom';
import { EmptyState } from '../components';

export default function NotFound() {
  return <section className="panel main-panel"><EmptyState title="Page not found" description="The page you are looking for does not exist or has moved."><Link className="button primary small empty-action" to="/">Go to overview</Link></EmptyState></section>;
}
