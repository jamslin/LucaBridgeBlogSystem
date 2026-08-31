import { useEffect, useState } from "react";
import { Link } from "react-router";
import { adminApi } from "../../lib/adminApi";

const SLOTS = ["HERO", "STAT", "FEATURED", "SUPPORT", "VOLUNTEER", "QUICK_LINK"];

export default function HomeBlocks() {
  const [rows, setRows] = useState(null);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  async function load() {
    try { setError(""); setRows(await adminApi.listHomeBlocks()); }
    catch (e) { setError(e.message); setRows([]); }
  }
  useEffect(() => { load(); }, []);

  async function remove(b) {
    if (!window.confirm(`Delete "${b.tcTitle}"?`)) return;
    setBusyId(b.id);
    try { await adminApi.deleteHomeBlock(b.id); await load(); }
    catch (e) { setError(e.message); }
    finally { setBusyId(null); }
  }

  return (
    <>
      <div className="admin-topbar">
        <h1>Home page content</h1>
        <Link className="admin-btn primary" to="/admin/home-blocks/new">New block</Link>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        <p style={{ marginTop: 0, color: "var(--color-muted)" }}>
          One row per home-page block. Content only — layout is fixed in React. Slots: {SLOTS.join(", ")}.
        </p>
        <div className="admin-card" style={{ padding: 0 }}>
          {rows === null ? (
            <div className="admin-empty">Loading…</div>
          ) : rows.length === 0 ? (
            <div className="admin-empty">No home-page blocks yet.</div>
          ) : (
            <table className="admin-table">
              <thead><tr><th>Slot</th><th>Title</th><th>Order</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {rows.map((b) => (
                  <tr key={b.id}>
                    <td><span className="chip">{b.slot}</span></td>
                    <td><Link to={`/admin/home-blocks/${b.id}`} style={{ color: "var(--color-accent)", fontWeight: 600 }}>{b.tcTitle}</Link></td>
                    <td>{b.sortOrder}</td>
                    <td><span className={`admin-badge ${b.active ? "published" : "draft"}`}>{b.active ? "Active" : "Inactive"}</span></td>
                    <td>
                      <div className="row-actions">
                        <Link className="admin-btn admin-btn-sm" to={`/admin/home-blocks/${b.id}`}>Edit</Link>
                        <button className="admin-btn admin-btn-sm danger" disabled={busyId === b.id} onClick={() => remove(b)}>Delete</button>
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
