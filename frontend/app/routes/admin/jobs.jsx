import { useEffect, useState } from "react";
import { Link } from "react-router";
import { adminApi } from "../../lib/adminApi";

export default function Jobs() {
  const [jobs, setJobs] = useState(null);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  async function load() {
    try { setError(""); setJobs(await adminApi.listJobs()); }
    catch (e) { setError(e.message); setJobs([]); }
  }
  useEffect(() => { load(); }, []);

  async function remove(j) {
    if (!window.confirm(`Delete "${j.tcTitle}"? This cannot be undone.`)) return;
    setBusyId(j.id);
    try { await adminApi.deleteJob(j.id); await load(); }
    catch (e) { setError(e.message); }
    finally { setBusyId(null); }
  }

  return (
    <>
      <div className="admin-topbar">
        <h1>Jobs</h1>
        <Link className="admin-btn primary" to="/admin/jobs/new">New job</Link>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        <div className="admin-card" style={{ padding: 0 }}>
          {jobs === null ? (
            <div className="admin-empty">Loading…</div>
          ) : jobs.length === 0 ? (
            <div className="admin-empty">No jobs yet.</div>
          ) : (
            <table className="admin-table">
              <thead><tr><th>Title</th><th>Closes</th><th>State</th><th></th></tr></thead>
              <tbody>
                {jobs.map((j) => (
                  <tr key={j.id}>
                    <td><Link to={`/admin/jobs/${j.id}`} style={{ color: "var(--color-accent)", fontWeight: 600 }}>{j.tcTitle}</Link><div style={{ fontSize: 12, color: "var(--color-muted)" }}>{j.slug}</div></td>
                    <td>{j.closesAt ? new Date(j.closesAt).toLocaleDateString() : "Open until filled"}</td>
                    <td><span className={`admin-badge ${j.state === "LIVE" ? "published" : "draft"}`}>{j.state}</span></td>
                    <td>
                      <div className="row-actions">
                        <Link className="admin-btn admin-btn-sm" to={`/admin/jobs/${j.id}`}>Edit</Link>
                        <button className="admin-btn admin-btn-sm danger" disabled={busyId === j.id} onClick={() => remove(j)}>Delete</button>
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
