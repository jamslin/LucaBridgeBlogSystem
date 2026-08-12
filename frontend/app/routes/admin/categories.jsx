import { useEffect, useState } from "react";
import { adminApi } from "../../lib/adminApi";

const LANGS = [
  { code: "zh-Hant", label: "繁中", required: true },
  { code: "en", label: "EN" },
  { code: "zh-Hans", label: "简中" },
];

function namesFrom(translations) {
  const o = { "zh-Hant": "", en: "", "zh-Hans": "" };
  for (const t of translations || []) if (o[t.lang] !== undefined) o[t.lang] = t.name || "";
  return o;
}
function payload(id, key, sortOrder, names) {
  const translations = LANGS.map((l) => ({ lang: l.code, name: names[l.code] })).filter((t) => t.name && t.name.trim());
  return { id, key: key.trim(), sortOrder: sortOrder === "" ? 0 : Number(sortOrder), translations };
}

function CategoryRow({ c, onSaved, onError, onDeleted }) {
  const [key, setKey] = useState(c.key);
  const [sortOrder, setSortOrder] = useState(String(c.sortOrder ?? 0));
  const [names, setNames] = useState(namesFrom(c.translations));
  const [busy, setBusy] = useState(false);

  async function save() {
    if (!names["zh-Hant"].trim()) { onError("繁中 name is required"); return; }
    setBusy(true);
    try { await adminApi.saveCategory(payload(c.id, key, sortOrder, names)); onSaved(); }
    catch (e) { onError(e.message); } finally { setBusy(false); }
  }
  async function del() {
    if (!window.confirm(`Delete category "${c.key}"?`)) return;
    setBusy(true);
    try { await adminApi.deleteCategory(c.id); onDeleted(); }
    catch (e) { onError(e.message); } finally { setBusy(false); }
  }

  return (
    <tr>
      <td><input value={key} onChange={(e) => setKey(e.target.value)} style={{ width: 130 }} /></td>
      {LANGS.map((l) => (
        <td key={l.code}><input value={names[l.code]} onChange={(e) => setNames({ ...names, [l.code]: e.target.value })} /></td>
      ))}
      <td><input type="number" value={sortOrder} onChange={(e) => setSortOrder(e.target.value)} style={{ width: 64 }} /></td>
      <td>{c.postCount > 0 ? <span className="admin-badge published">{c.postCount} post{c.postCount === 1 ? "" : "s"}</span> : <span className="admin-badge draft">unused</span>}</td>
      <td><div className="row-actions">
        <button className="admin-btn admin-btn-sm" disabled={busy} onClick={save}>Save</button>
        <button className="admin-btn admin-btn-sm danger" disabled={busy} onClick={del}>Delete</button>
      </div></td>
    </tr>
  );
}

export default function Categories() {
  const [rows, setRows] = useState(null);
  const [error, setError] = useState(""); const [ok, setOk] = useState("");
  const [nk, setNk] = useState(""); const [nso, setNso] = useState(""); const [nn, setNn] = useState({ "zh-Hant": "", en: "", "zh-Hans": "" });
  const [busy, setBusy] = useState(false);

  async function load() {
    try { setRows(await adminApi.listCategoriesAdmin()); }
    catch (e) { setError(e.message); setRows([]); }
  }
  useEffect(() => { load(); }, []);

  async function add() {
    setError(""); setOk("");
    if (!nk.trim()) { setError("Key is required"); return; }
    if (!nn["zh-Hant"].trim()) { setError("繁中 name is required"); return; }
    setBusy(true);
    try { await adminApi.saveCategory(payload(null, nk, nso, nn)); setNk(""); setNso(""); setNn({ "zh-Hant": "", en: "", "zh-Hans": "" }); setOk("Category added."); load(); }
    catch (e) { setError(e.message); } finally { setBusy(false); }
  }

  return (
    <>
      <div className="admin-topbar"><h1>Categories</h1></div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}
        <div className="admin-card" style={{ padding: 0 }}>
          {rows === null ? <div className="admin-empty">Loading…</div> : (
            <table className="admin-table">
              <thead><tr><th>Key</th><th>繁中</th><th>EN</th><th>简中</th><th>Order</th><th>Usage</th><th></th></tr></thead>
              <tbody>
                {rows.map((c) => (
                  <CategoryRow key={c.id} c={c}
                    onSaved={() => { setError(""); setOk("Saved."); load(); }}
                    onError={(m) => { setOk(""); setError(m); }}
                    onDeleted={() => { setError(""); setOk("Deleted."); load(); }} />
                ))}
              </tbody>
            </table>
          )}
        </div>
        <div className="admin-card">
          <h2 style={{ fontFamily: "var(--font-display)", fontSize: 16, marginTop: 0 }}>Add category</h2>
          <div className="admin-row">
            <div className="admin-field"><label>Key</label><input value={nk} onChange={(e) => setNk(e.target.value)} placeholder="poverty-relief" /></div>
            <div className="admin-field"><label>繁中 name</label><input value={nn["zh-Hant"]} onChange={(e) => setNn({ ...nn, "zh-Hant": e.target.value })} /></div>
            <div className="admin-field"><label>EN name</label><input value={nn.en} onChange={(e) => setNn({ ...nn, en: e.target.value })} /></div>
            <div className="admin-field"><label>简中 name</label><input value={nn["zh-Hans"]} onChange={(e) => setNn({ ...nn, "zh-Hans": e.target.value })} /></div>
            <div className="admin-field"><label>Order</label><input type="number" value={nso} onChange={(e) => setNso(e.target.value)} /></div>
          </div>
          <div className="admin-actions"><button className="admin-btn primary" disabled={busy} onClick={add}>Add category</button></div>
        </div>
      </div>
    </>
  );
}
