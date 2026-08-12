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
        const posts = await adminApi.listPosts();
        const published = posts.filter((p) => p.status === "PUBLISHED").length;
        let users = null;
        if (isAdmin) {
          try { users = (await adminApi.listUsers()).length; } catch (_) { /* ignore */ }
        }
        if (alive) setStats({ total: posts.length, published, drafts: posts.length - published, users });
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
          <div className="stat-card"><div className="n">{stats ? stats.total : "—"}</div><div className="l">Posts total</div></div>
          <div className="stat-card"><div className="n">{stats ? stats.published : "—"}</div><div className="l">Published</div></div>
          <div className="stat-card"><div className="n">{stats ? stats.drafts : "—"}</div><div className="l">Drafts</div></div>
          {isAdmin ? (
            <div className="stat-card"><div className="n">{stats && stats.users != null ? stats.users : "—"}</div><div className="l">CMS users</div></div>
          ) : null}
        </div>
        <div className="admin-actions" style={{ marginTop: 24 }}>
          <Link className="admin-btn primary" to="/admin/posts/new">New post</Link>
          <Link className="admin-btn" to="/admin/posts">Manage posts</Link>
          <Link className="admin-btn" to="/admin/settings">Site settings</Link>
        </div>
      </div>
    </>
  );
}
