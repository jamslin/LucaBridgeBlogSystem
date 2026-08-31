import { useEffect, useState } from "react";
import { Link, useOutletContext } from "react-router";
import { adminApi } from "../../lib/adminApi";

export default function Dashboard() {
  const { user, isAdmin } = useOutletContext();
  const [stats, setStats] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        const [blog, events, jobs] = await Promise.all([
          adminApi.listBlog(), adminApi.listEvents(), adminApi.listJobs(),
        ]);
        const published = (rows) => rows.filter((r) => r.status === "PUBLISHED").length;
        let users = null;
        if (isAdmin) {
          try { users = (await adminApi.listUsers()).length; } catch (_) { /* ignore */ }
        }
        if (alive) setStats({
          blogTotal: blog.length, blogPublished: published(blog),
          eventsTotal: events.length, eventsPublished: published(events),
          jobsTotal: jobs.length, jobsPublished: published(jobs),
          users,
        });
      } catch (e) {
        if (alive) setError(e.message);
      }
    })();
    return () => { alive = false; };
  }, [isAdmin]);

  return (
    <>
      <div className="admin-topbar"><h1>Dashboard</h1></div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        <p style={{ marginTop: 0, color: "var(--color-muted)" }}>
          Welcome back, {user.username}.
        </p>
        <div className="stat-grid">
          <div className="stat-card"><div className="n">{stats ? `${stats.blogPublished}/${stats.blogTotal}` : "—"}</div><div className="l">Blog posts published</div></div>
          <div className="stat-card"><div className="n">{stats ? `${stats.eventsPublished}/${stats.eventsTotal}` : "—"}</div><div className="l">Events published</div></div>
          <div className="stat-card"><div className="n">{stats ? `${stats.jobsPublished}/${stats.jobsTotal}` : "—"}</div><div className="l">Jobs published</div></div>
          {isAdmin ? (
            <div className="stat-card"><div className="n">{stats && stats.users != null ? stats.users : "—"}</div><div className="l">CMS users</div></div>
          ) : null}
        </div>
        <div className="admin-actions" style={{ marginTop: 24 }}>
          <Link className="admin-btn primary" to="/admin/blog/new">New blog post</Link>
          <Link className="admin-btn" to="/admin/events/new">New event</Link>
          <Link className="admin-btn" to="/admin/home-blocks">Home page content</Link>
        </div>
      </div>
    </>
  );
}
