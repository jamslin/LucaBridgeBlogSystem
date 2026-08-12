import { useEffect, useState } from "react";
import { Link } from "react-router";
import { adminApi } from "../../lib/adminApi";

export default function Jobs() {
  const [rows, setRows] = useState(null);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  async function load() {
    try { setError(""); setRows(await adminApi.listJobs()); }
    catch (e) { setError(e.message); setRows([]); }
  }
  useEffect(() => { load(); }, []);

  async function togglePublish(r) {
    setBusyId(r.id);
    try { if (r.status === "PUBLISHED") await adminApi.unpublishJob(r.id); else await adminApi.publishJob(r.id); await load(); }
    catch (e) { setError(e.message); } finally { setBusyId(null); }
  }
  async function remove(r) {
    if (!window.confirm(`Delete "${r.title}"?`)) return;
    setBusyId(r.id);
    try { await adminApi.deleteJob(r.id); await load(); }
    catch (e) { setError(e.message); } finally { setBusyId(null); }
  }

  return (
    <>
      <div className="admin-topbar"><h1>Job postings</h1><Link className="admin-btn primary" to="/admin/jobs/new">New job</Link></div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        <div className="admin-card" style={{ padding: 0 }}>
          {rows === null ? <div className="admin-empty">Loading…</div>
            : rows.length === 0 ? <div className="admin-empty">No job postings yet.</div>
            : (
            <table className="admin-table">
              <thead><tr><th>Title</th><th>Department</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.id}>
                    <td><Link to={`/admin/jobs/${r.id}`} style={{ color: "var(--color-accent)", fontWeight: 600 }}>{r.title}</Link><div style={{ fontSize: 12, color: "var(--color-muted)" }}>{r.slug}</div></td>
                    <td>{r.department || "—"}</td>
                    <td><span className={`admin-badge ${r.status === "PUBLISHED" ? "published" : "draft"}`}>{r.status === "PUBLISHED" ? "Published" : "Draft"}</span></td>
                    <td><div className="row-actions">
                      <Link className="admin-btn admin-btn-sm" to={`/admin/jobs/${r.id}`}>Edit</Link>
                      <button className="admin-btn admin-btn-sm" disabled={busyId === r.id} onClick={() => togglePublish(r)}>{r.status === "PUBLISHED" ? "Unpublish" : "Publish"}</button>
                      <button className="admin-btn admin-btn-sm danger" disabled={busyId === r.id} onClick={() => remove(r)}>Delete</button>
                    </div></td>
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
