import { useEffect, useState } from "react";
import { Link } from "react-router";
import { adminApi } from "../../lib/adminApi";

export default function Events() {
  const [events, setEvents] = useState(null);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  async function load() {
    try { setError(""); setEvents(await adminApi.listEvents()); }
    catch (e) { setError(e.message); setEvents([]); }
  }
  useEffect(() => { load(); }, []);

  async function remove(e) {
    if (!window.confirm(`Delete "${e.tcTitle}"? This cannot be undone.`)) return;
    setBusyId(e.id);
    try { await adminApi.deleteEvent(e.id); await load(); }
    catch (err) { setError(err.message); }
    finally { setBusyId(null); }
  }

  return (
    <>
      <div className="admin-topbar">
        <h1>Events</h1>
        <Link className="admin-btn primary" to="/admin/events/new">New event</Link>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        <div className="admin-card" style={{ padding: 0 }}>
          {events === null ? (
            <div className="admin-empty">Loading…</div>
          ) : events.length === 0 ? (
            <div className="admin-empty">No events yet. Create your first one.</div>
          ) : (
            <table className="admin-table">
              <thead><tr><th>Title</th><th>State</th><th>Registration</th><th></th></tr></thead>
              <tbody>
                {events.map((e) => (
                  <tr key={e.id}>
                    <td><Link to={`/admin/events/${e.id}`} style={{ color: "var(--color-accent)", fontWeight: 600 }}>{e.tcTitle}</Link><div style={{ fontSize: 12, color: "var(--color-muted)" }}>{e.slug}</div></td>
                    <td><span className={`admin-badge ${e.state === "LIVE" ? "published" : "draft"}`}>{e.state}</span></td>
                    <td>
                      {e.registration ? `${e.registration.state} (${e.registration.registeredCount}${e.registration.capacity != null ? `/${e.registration.capacity}` : ""})` : "—"}
                    </td>
                    <td>
                      <div className="row-actions">
                        <Link className="admin-btn admin-btn-sm" to={`/admin/events/${e.id}`}>Edit</Link>
                        <Link className="admin-btn admin-btn-sm" to={`/admin/events/${e.id}/registrations`}>Registrations</Link>
                        <button className="admin-btn admin-btn-sm danger" disabled={busyId === e.id} onClick={() => remove(e)}>Delete</button>
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
