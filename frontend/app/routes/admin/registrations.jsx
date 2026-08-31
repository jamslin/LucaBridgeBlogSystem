import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { adminApi } from "../../lib/adminApi";

export default function Registrations() {
  const { id } = useParams();
  const [rows, setRows] = useState(null);
  const [error, setError] = useState("");
  const [exporting, setExporting] = useState(false);

  async function exportCsv() {
    setExporting(true); setError("");
    try {
      const blob = await adminApi.exportRegistrationsCsv(id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url; a.download = `registrations-event-${id}.csv`;
      document.body.appendChild(a); a.click(); a.remove();
      URL.revokeObjectURL(url);
    } catch (e) { setError(e.message); }
    finally { setExporting(false); }
  }

  useEffect(() => {
    let alive = true;
    adminApi.listRegistrations(id)
      .then((r) => { if (alive) setRows(r); })
      .catch((e) => { if (alive) { setError(e.message); setRows([]); } });
    return () => { alive = false; };
  }, [id]);

  return (
    <>
      <div className="admin-topbar">
        <h1>Registrations</h1>
        <div style={{ display: "flex", gap: 8 }}>
          <button className="admin-btn" disabled={exporting} onClick={exportCsv}>{exporting ? "Exporting…" : "Export CSV"}</button>
          <Link className="admin-btn ghost" to={`/admin/events/${id}`}>← Back to event</Link>
        </div>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        <div className="admin-card" style={{ padding: 0, overflowX: "auto" }}>
          {rows === null ? (
            <div className="admin-empty">Loading…</div>
          ) : rows.length === 0 ? (
            <div className="admin-empty">No registrations yet.</div>
          ) : (
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Ref</th><th>Name</th><th>Email</th><th>Phone</th><th>Status</th><th>Submitted</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.id}>
                    <td>{r.referenceCode}</td>
                    <td>{r.fullName}</td>
                    <td>{r.email}</td>
                    <td>{r.phone}</td>
                    <td><span className={`admin-badge ${r.status === "CONFIRMED" || r.status === "ATTENDED" ? "published" : "draft"}`}>{r.status}</span></td>
                    <td>{r.submittedAt ? new Date(r.submittedAt).toLocaleString() : "—"}</td>
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
