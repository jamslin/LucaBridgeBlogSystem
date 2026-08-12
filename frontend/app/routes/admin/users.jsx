import { useEffect, useState } from "react";
import { useOutletContext } from "react-router";
import { adminApi } from "../../lib/adminApi";

const ALL_ROLES = ["ADMIN", "EDITOR"];

export default function Users() {
  const { user, isAdmin } = useOutletContext();
  const [users, setUsers] = useState(null);
  const [error, setError] = useState("");
  const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false);
  const [form, setForm] = useState({ username: "", password: "", displayName: "", roles: ["EDITOR"] });

  async function load() {
    try { setUsers(await adminApi.listUsers()); }
    catch (e) { setError(e.message); setUsers([]); }
  }
  useEffect(() => { if (isAdmin) load(); }, [isAdmin]);

  if (!isAdmin) {
    return (<><div className="admin-topbar"><h1>Users</h1></div><div className="admin-content"><div className="admin-alert error">Administrators only.</div></div></>);
  }

  function toggleFormRole(r) {
    setForm((f) => ({ ...f, roles: f.roles.includes(r) ? f.roles.filter((x) => x !== r) : [...f.roles, r] }));
  }

  async function create(e) {
    e.preventDefault();
    setError(""); setOk(""); setBusy(true);
    try {
      await adminApi.createUser({
        username: form.username.trim(),
        password: form.password,
        displayName: form.displayName.trim() || null,
        roles: form.roles,
      });
      setOk(`Created ${form.username}.`);
      setForm({ username: "", password: "", displayName: "", roles: ["EDITOR"] });
      load();
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  async function toggleEnabled(u) {
    setError(""); setOk("");
    try { await adminApi.updateUser(u.id, { enabled: !u.enabled }); load(); }
    catch (e) { setError(e.message); }
  }
  async function toggleRole(u, r) {
    setError(""); setOk("");
    const roles = u.roles.includes(r) ? u.roles.filter((x) => x !== r) : [...u.roles, r];
    try { await adminApi.updateUser(u.id, { roles }); load(); }
    catch (e) { setError(e.message); }
  }
  async function resetPw(u) {
    const p = window.prompt(`New password for ${u.username} (min 8 chars):`);
    if (!p) return;
    setError(""); setOk("");
    try { await adminApi.changeUserPassword(u.id, p); setOk(`Password reset for ${u.username}.`); }
    catch (e) { setError(e.message); }
  }
  async function del(u) {
    if (!window.confirm(`Delete ${u.username}? This cannot be undone.`)) return;
    setError(""); setOk("");
    try { await adminApi.deleteUser(u.id); load(); }
    catch (e) { setError(e.message); }
  }

  return (
    <>
      <div className="admin-topbar"><h1>Users</h1></div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}

        <div className="admin-card">
          <h2 style={{ fontFamily: "var(--font-display)", fontSize: 16, marginTop: 0 }}>Add user</h2>
          <form onSubmit={create}>
            <div className="admin-row">
              <div className="admin-field"><label>Username</label><input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} autoComplete="off" /></div>
              <div className="admin-field"><label>Password</label><input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} autoComplete="new-password" /><div className="hint">Min 8 characters</div></div>
              <div className="admin-field"><label>Display name</label><input value={form.displayName} onChange={(e) => setForm({ ...form, displayName: e.target.value })} /></div>
            </div>
            <div className="admin-field">
              <label>Roles</label>
              <div style={{ display: "flex", gap: 16 }}>
                {ALL_ROLES.map((r) => (
                  <label key={r} style={{ display: "flex", alignItems: "center", gap: 6, fontWeight: 400 }}>
                    <input type="checkbox" style={{ width: "auto" }} checked={form.roles.includes(r)} onChange={() => toggleFormRole(r)} /> {r}
                  </label>
                ))}
              </div>
            </div>
            <div className="admin-actions">
              <button className="admin-btn primary" type="submit" disabled={busy || !form.roles.length}>{busy ? "Creating…" : "Create user"}</button>
            </div>
          </form>
        </div>

        <div className="admin-card" style={{ padding: 0 }}>
          {users === null ? (
            <div className="admin-empty">Loading…</div>
          ) : (
            <table className="admin-table">
              <thead><tr><th>User</th><th>Roles</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>
                      <strong>{u.username}</strong>{u.username === user.username ? <span style={{ color: "var(--color-muted)", fontSize: 12 }}> (you)</span> : null}
                      {u.displayName ? <div style={{ fontSize: 12, color: "var(--color-muted)" }}>{u.displayName}</div> : null}
                    </td>
                    <td>
                      {ALL_ROLES.map((r) => (
                        <button key={r} type="button" className={`role-badge ${r === "ADMIN" ? "admin" : ""}`}
                                style={{ opacity: u.roles.includes(r) ? 1 : 0.35, border: "none", cursor: "pointer" }}
                                title={u.roles.includes(r) ? `Remove ${r}` : `Add ${r}`}
                                onClick={() => toggleRole(u, r)}>
                          {r}
                        </button>
                      ))}
                    </td>
                    <td><span className={`admin-badge ${u.enabled ? "published" : "draft"}`}>{u.enabled ? "Active" : "Disabled"}</span></td>
                    <td>
                      <div className="row-actions">
                        <button className="admin-btn admin-btn-sm" onClick={() => toggleEnabled(u)}>{u.enabled ? "Disable" : "Enable"}</button>
                        <button className="admin-btn admin-btn-sm" onClick={() => resetPw(u)}>Reset PW</button>
                        <button className="admin-btn admin-btn-sm danger" disabled={u.username === user.username} onClick={() => del(u)}>Delete</button>
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
