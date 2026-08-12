import { useEffect, useState } from "react";
import { adminApi } from "../../lib/adminApi";

export default function Settings() {
  const [rows, setRows] = useState(null);
  const [error, setError] = useState("");
  const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false);
  const [newKey, setNewKey] = useState("");
  const [newVal, setNewVal] = useState("");

  useEffect(() => {
    (async () => {
      try {
        const m = await adminApi.getSettings();
        setRows(Object.entries(m).map(([key, value]) => ({ key, value })));
      } catch (e) { setError(e.message); setRows([]); }
    })();
  }, []);

  function setVal(i, v) {
    setRows((prev) => prev.map((r, idx) => (idx === i ? { ...r, value: v } : r)));
  }
  function addRow() {
    const key = newKey.trim();
    if (!key) return;
    if (rows.some((r) => r.key === key)) { setError(`Key "${key}" already exists`); return; }
    setRows((prev) => [...prev, { key, value: newVal }]);
    setNewKey(""); setNewVal(""); setError("");
  }

  async function save() {
    setBusy(true); setError(""); setOk("");
    const map = {};
    for (const r of rows) map[r.key] = r.value;
    try {
      const updated = await adminApi.saveSettings(map);
      setRows(Object.entries(updated).map(([key, value]) => ({ key, value })));
      setOk("Settings saved.");
    } catch (e) { setError(e.message); }
    finally { setBusy(false); }
  }

  return (
    <>
      <div className="admin-topbar"><h1>Site settings</h1></div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}
        <div className="admin-card">
          {rows === null ? (
            <div className="admin-empty">Loading…</div>
          ) : (
            <>
              {rows.map((r, i) => (
                <div className="admin-field" key={r.key}>
                  <label>{r.key}</label>
                  <input value={r.value} onChange={(e) => setVal(i, e.target.value)} />
                </div>
              ))}
              <div className="admin-row" style={{ borderTop: "1px solid var(--color-line)", paddingTop: 16, marginTop: 4 }}>
                <div className="admin-field"><label>New key</label><input value={newKey} onChange={(e) => setNewKey(e.target.value)} placeholder="e.g. phone" /></div>
                <div className="admin-field"><label>Value</label><input value={newVal} onChange={(e) => setNewVal(e.target.value)} /></div>
                <div style={{ display: "flex", alignItems: "flex-end" }}><button className="admin-btn" onClick={addRow}>Add</button></div>
              </div>
            </>
          )}
        </div>
        <div className="admin-actions">
          <button className="admin-btn primary" disabled={busy || rows === null} onClick={save}>{busy ? "Saving…" : "Save settings"}</button>
        </div>
      </div>
    </>
  );
}
