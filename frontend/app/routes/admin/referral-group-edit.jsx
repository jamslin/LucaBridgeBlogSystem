import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { adminApi } from "../../lib/adminApi";

const LANGS = [
  { code: "zh-Hant", label: "繁中", required: true },
  { code: "en", label: "EN" },
  { code: "zh-Hans", label: "简中" },
];
const empty = () => { const o = {}; for (const l of LANGS) o[l.code] = { name: "" }; return o; };

export default function ReferralGroupEdit() {
  const { id } = useParams();
  const isNew = id === "new";
  const navigate = useNavigate();

  const [code, setCode] = useState("");
  const [sortOrder, setSortOrder] = useState("0");
  const [active, setActive] = useState(true);
  const [tr, setTr] = useState(empty());
  const [activeLang, setActiveLang] = useState("zh-Hant");
  const [error, setError] = useState(""); const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(!isNew);

  useEffect(() => {
    if (isNew) return;
    let alive = true;
    (async () => {
      try {
        const g = await adminApi.getReferralGroup(id);
        if (!alive) return;
        setCode(g.code || ""); setSortOrder(String(g.sortOrder ?? 0)); setActive(!!g.active);
        setTr({
          "zh-Hant": { name: g.tcName || "" },
          en: { name: g.enName || "" },
          "zh-Hans": { name: g.scName || "" },
        });
      } catch (e) { if (alive) setError(e.message); } finally { if (alive) setLoading(false); }
    })();
    return () => { alive = false; };
  }, [id, isNew]);

  const setField = (lang, f, v) => setTr((p) => ({ ...p, [lang]: { ...p[lang], [f]: v } }));

  async function save() {
    setError(""); setOk("");
    if (!code.trim()) { setError("Code is required"); return; }
    if (!tr["zh-Hant"].name.trim()) { setError("繁中 name is required"); return; }
    setBusy(true);
    const payload = {
      code: code.trim(), sortOrder: Number(sortOrder) || 0, active,
      tcName: tr["zh-Hant"].name, enName: tr.en.name, scName: tr["zh-Hans"].name,
    };
    try {
      const saved = isNew ? await adminApi.createReferralGroup(payload) : await adminApi.updateReferralGroup(id, payload);
      if (isNew) navigate(`/admin/referral-groups/${saved.id}`, { replace: true });
      else setOk("Saved.");
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }

  if (loading) return (<><div className="admin-topbar"><h1>Edit referral group</h1></div><div className="admin-content"><div className="admin-empty">Loading…</div></div></>);
  const a = tr[activeLang];

  return (
    <>
      <div className="admin-topbar">
        <h1>{isNew ? "New referral group" : "Edit referral group"}</h1>
        <button className="admin-btn ghost" onClick={() => navigate("/admin/referral-groups")}>← Back</button>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}
        <div className="admin-card">
          <div className="admin-row">
            <div className="admin-field"><label>Code</label><input value={code} onChange={(e) => setCode(e.target.value)} /></div>
            <div className="admin-field"><label>Sort order</label><input type="number" value={sortOrder} onChange={(e) => setSortOrder(e.target.value)} /></div>
            <div className="admin-field">
              <label className="checkbox" style={{ display: "flex", alignItems: "center", gap: 8, fontWeight: 400 }}>
                <input type="checkbox" style={{ width: "auto" }} checked={active} onChange={(e) => setActive(e.target.checked)} /> Active
              </label>
            </div>
          </div>
        </div>
        <div className="admin-card">
          <div className="lang-tabs">
            {LANGS.map((l) => <button key={l.code} type="button" className={`lang-tab ${activeLang === l.code ? "active" : ""}`} onClick={() => setActiveLang(l.code)}>{l.label}{l.required ? <span className="req"> *</span> : null}</button>)}
          </div>
          <div className="admin-field"><label>Name{activeLang === "zh-Hant" ? " (required)" : ""}</label><input value={a.name} onChange={(e) => setField(activeLang, "name", e.target.value)} /></div>
        </div>
        <div className="admin-actions">
          <button className="admin-btn primary" disabled={busy} onClick={save}>{busy ? "Saving…" : "Save"}</button>
        </div>
      </div>
    </>
  );
}
