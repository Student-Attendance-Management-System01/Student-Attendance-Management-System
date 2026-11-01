import React from 'react';
import { PageHeading, EmptyState } from '../components';

/** Placeholder overview until the dashboard screens land. */
export default function Home() {
  return <>
    <PageHeading eyebrow="SAMS WORKSPACE" title="Your workspace" description="Attendance activity and next steps for your role will appear here." />
    <section className="panel main-panel"><EmptyState title="Nothing to show yet" description="Screens for your role will appear in the navigation as they become available." /></section>
  </>;
}
