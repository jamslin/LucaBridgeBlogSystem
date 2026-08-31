import { useEffect, useState } from "react";
import { Link } from "react-router";
import { adminApi } from "../../lib/adminApi";

export default function Blog() {
  const [posts, setPosts] = useState(null);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  async function load() {
    try { setError(""); setPosts(await adminApi.listBlog()); }
    catch (e) { setError(e.message); setPosts([]); }
  }
  useEffect(() => { load(); }, []);

  async function remove(p) {
    if (!window.confirm(`Delete "${p.tcTitle}"? This cannot be undone.`)) return;
    setBusyId(p.id);
    try { await adminApi.deleteBlog(p.id); await load(); }
    catch (e) { setError(e.message); }
    finally { setBusyId(null); }
  }

  return (
    <>
      <div className="admin-topbar">
        <h1>Blog</h1>
        <Link className="admin-btn primary" to="/admin/blog/new">New post</Link>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        <div className="admin-card" style={{ padding: 0 }}>
          {posts === null ? (
            <div className="admin-empty">Loading…</div>
          ) : posts.length === 0 ? (
            <div className="admin-empty">No posts yet. Create your first one.</div>
          ) : (
            <table className="admin-table">
              <thead><tr><th>Title</th><th>State</th><th></th></tr></thead>
              <tbody>
                {posts.map((p) => (
                  <tr key={p.id}>
                    <td><Link to={`/admin/blog/${p.id}`} style={{ color: "var(--color-accent)", fontWeight: 600 }}>{p.tcTitle}</Link><div style={{ fontSize: 12, color: "var(--color-muted)" }}>{p.slug}</div></td>
                    <td><span className={`admin-badge ${p.state === "LIVE" ? "published" : "draft"}`}>{p.state}</span></td>
                    <td>
                      <div className="row-actions">
                        <Link className="admin-btn admin-btn-sm" to={`/admin/blog/${p.id}`}>Edit</Link>
                        <button className="admin-btn admin-btn-sm danger" disabled={busyId === p.id} onClick={() => remove(p)}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  );
}
