import { useEffect, useState } from "react";
import { Link } from "react-router";
import { adminApi } from "../../lib/adminApi";

export default function Services() {
  const [rows, setRows] = useState(null);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  async function load() {
    try { setError(""); setRows(await adminApi.listServices()); }
    catch (e) { setError(e.message); setRows([]); }
  }
  useEffect(() => { load(); }, []);

  async function remove(s) {
    try {
      const usage = await adminApi.serviceUsage(s.id);
      const inUse = usage.blogCount > 0 || usage.eventCount > 0;
      const msg = inUse
        ? `"${s.tcName}" is used by ${usage.blogCount} blog post(s) and ${usage.eventCount} event(s) — deleting it removes the chip from that content. Continue?`
        : `Delete "${s.tcName}"?`;
      if (!window.confirm(msg)) return;
      setBusyId(s.id);
      await adminApi.deleteService(s.id, inUse);
      await load();
    } catch (e) { setError(e.message); }
    finally { setBusyId(null); }
  }

  return (
    <>
      <div className="admin-topbar">
        <h1>Services</h1>
        <Link className="admin-btn primary" to="/admin/services/new">New service</Link>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        <div className="admin-card" style={{ padding: 0 }}>
          {rows === null ? (
            <div className="admin-empty">Loading…</div>
          ) : rows.length === 0 ? (
            <div className="admin-empty">No services yet.</div>
          ) : (
            <table className="admin-table">
              <thead><tr><th>Name</th><th>Code</th><th>Order</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {rows.map((s) => (
                  <tr key={s.id}>
                    <td><Link to={`/admin/services/${s.id}`} style={{ color: "var(--color-accent)", fontWeight: 600 }}>{s.tcName}</Link></td>
                    <td>{s.code}</td>
                    <td>{s.sortOrder}</td>
                    <td><span className={`admin-badge ${s.active ? "published" : "draft"}`}>{s.active ? "Active" : "Inactive"}</span></td>
                    <td>
                      <div className="row-actions">
                        <Link className="admin-btn admin-btn-sm" to={`/admin/services/${s.id}`}>Edit</Link>
                        <button className="admin-btn admin-btn-sm danger" disabled={busyId === s.id} onClick={() => remove(s)}>Delete</button>
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
