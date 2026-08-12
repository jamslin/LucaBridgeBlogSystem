import { useEffect, useState } from "react";
import { Link } from "react-router";
import { adminApi } from "../../lib/adminApi";

export default function Posts() {
  const [posts, setPosts] = useState(null);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  async function load() {
    try {
      setError("");
      setPosts(await adminApi.listPosts());
    } catch (e) {
      setError(e.message);
      setPosts([]);
    }
  }
  useEffect(() => { load(); }, []);

  async function togglePublish(p) {
    setBusyId(p.id);
    try {
      if (p.status === "PUBLISHED") await adminApi.unpublishPost(p.id);
      else await adminApi.publishPost(p.id);
      await load();
    } catch (e) { setError(e.message); }
    finally { setBusyId(null); }
  }

  async function remove(p) {
    if (!window.confirm(`Delete "${p.title}"? This cannot be undone.`)) return;
    setBusyId(p.id);
    try {
      await adminApi.deletePost(p.id);
      await load();
    } catch (e) { setError(e.message); }
    finally { setBusyId(null); }
  }

  return (
    <>
      <div className="admin-topbar">
        <h1>Posts</h1>
        <Link className="admin-btn primary" to="/admin/posts/new">New post</Link>
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
              <thead>
                <tr><th>Title</th><th>Category</th><th>Status</th><th></th></tr>
              </thead>
              <tbody>
                {posts.map((p) => (
                  <tr key={p.id}>
                    <td><Link to={`/admin/posts/${p.id}`} style={{ color: "var(--color-accent)", fontWeight: 600 }}>{p.title}</Link><div style={{ fontSize: 12, color: "var(--color-muted)" }}>{p.slug}</div></td>
                    <td>{p.categoryKey || "—"}</td>
                    <td><span className={`admin-badge ${p.status === "PUBLISHED" ? "published" : "draft"}`}>{p.status === "PUBLISHED" ? "Published" : "Draft"}</span></td>
                    <td>
                      <div className="row-actions">
                        <Link className="admin-btn admin-btn-sm" to={`/admin/posts/${p.id}`}>Edit</Link>
                        <button className="admin-btn admin-btn-sm" disabled={busyId === p.id} onClick={() => togglePublish(p)}>
                          {p.status === "PUBLISHED" ? "Unpublish" : "Publish"}
                        </button>
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
