import { useEffect, useState } from "react";
import { Link } from "react-router";
import { adminApi } from "../../lib/adminApi";

export default function ReferralGroups() {
  const [rows, setRows] = useState(null);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  async function load() {
    try { setError(""); setRows(await adminApi.listReferralGroups()); }
    catch (e) { setError(e.message); setRows([]); }
  }
  useEffect(() => { load(); }, []);

  async function remove(g) {
    try {
      const usage = await adminApi.referralGroupUsage(g.id);
      const inUse = usage.registrationCount > 0;
      const msg = inUse
        ? `"${g.tcName}" is referenced by ${usage.registrationCount} registration(s) — deleting it blanks that field on those records. Continue?`
        : `Delete "${g.tcName}"?`;
      if (!window.confirm(msg)) return;
      setBusyId(g.id);
      await adminApi.deleteReferralGroup(g.id, inUse);
      await load();
    } catch (e) { setError(e.message); }
    finally { setBusyId(null); }
  }

  return (
    <>
      <div className="admin-topbar">
        <h1>Referral groups</h1>
        <Link className="admin-btn primary" to="/admin/referral-groups/new">New group</Link>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        <p style={{ marginTop: 0, color: "var(--color-muted)" }}>
          "How did you hear about us?" options shown on the event registration form.
        </p>
        <div className="admin-card" style={{ padding: 0 }}>
          {rows === null ? (
            <div className="admin-empty">Loading…</div>
          ) : rows.length === 0 ? (
            <div className="admin-empty">No referral groups yet.</div>
          ) : (
            <table className="admin-table">
              <thead><tr><th>Name</th><th>Code</th><th>Order</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {rows.map((g) => (
                  <tr key={g.id}>
                    <td><Link to={`/admin/referral-groups/${g.id}`} style={{ color: "var(--color-accent)", fontWeight: 600 }}>{g.tcName}</Link></td>
                    <td>{g.code}</td>
                    <td>{g.sortOrder}</td>
                    <td><span className={`admin-badge ${g.active ? "published" : "draft"}`}>{g.active ? "Active" : "Inactive"}</span></td>
                    <td>
                      <div className="row-actions">
                        <Link className="admin-btn admin-btn-sm" to={`/admin/referral-groups/${g.id}`}>Edit</Link>
                        <button className="admin-btn admin-btn-sm danger" disabled={busyId === g.id} onClick={() => remove(g)}>Delete</button>
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
