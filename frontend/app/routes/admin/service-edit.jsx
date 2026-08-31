import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { adminApi } from "../../lib/adminApi";

const LANGS = [
  { code: "zh-Hant", prefix: "tc", label: "繁中", required: true },
  { code: "en", prefix: "en", label: "EN" },
  { code: "zh-Hans", prefix: "sc", label: "简中" },
];
const empty = () => { const o = {}; for (const l of LANGS) o[l.code] = { name: "", description: "" }; return o; };

export default function ServiceEdit() {
  const { id } = useParams();
  const isNew = id === "new";
  const navigate = useNavigate();

  const [code, setCode] = useState("");
  const [sortOrder, setSortOrder] = useState("0");
  const [active, setActive] = useState(true);
  const [iconMediaId, setIconMediaId] = useState(null);
  const [iconUrl, setIconUrl] = useState("");
  const [tr, setTr] = useState(empty());
  const [activeLang, setActiveLang] = useState("zh-Hant");
  const [error, setError] = useState(""); const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false); const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(!isNew);
  const iconRef = useRef(null);

  useEffect(() => {
    if (isNew) return;
    let alive = true;
    (async () => {
      try {
        const s = await adminApi.getService(id);
        if (!alive) return;
        setCode(s.code || ""); setSortOrder(String(s.sortOrder ?? 0)); setActive(!!s.active);
        setIconMediaId(s.iconMediaId || null);
        setTr({
          "zh-Hant": { name: s.tcName || "", description: s.tcDescription || "" },
          en: { name: s.enName || "", description: s.enDescription || "" },
          "zh-Hans": { name: s.scName || "", description: s.scDescription || "" },
        });
      } catch (e) { if (alive) setError(e.message); } finally { if (alive) setLoading(false); }
    })();
    return () => { alive = false; };
  }, [id, isNew]);

  const setField = (lang, f, v) => setTr((p) => ({ ...p, [lang]: { ...p[lang], [f]: v } }));

  async function onIcon(e) {
    const f = e.target.files && e.target.files[0]; if (!f) return;
    setUploading(true); setError("");
    try { const m = await adminApi.uploadMedia(f); setIconMediaId(m.id); setIconUrl(m.url); }
    catch (er) { setError(`Upload failed: ${er.message}`); }
    finally { setUploading(false); if (iconRef.current) iconRef.current.value = ""; }
  }

  async function save() {
    setError(""); setOk("");
    if (!code.trim()) { setError("Code is required"); return; }
    if (!tr["zh-Hant"].name.trim()) { setError("繁中 name is required"); return; }
    setBusy(true);
    const payload = {
      code: code.trim(), iconMediaId, sortOrder: Number(sortOrder) || 0, active,
      tcName: tr["zh-Hant"].name, enName: tr.en.name, scName: tr["zh-Hans"].name,
      tcDescription: tr["zh-Hant"].description, enDescription: tr.en.description, scDescription: tr["zh-Hans"].description,
    };
    try {
      const saved = isNew ? await adminApi.createService(payload) : await adminApi.updateService(id, payload);
      if (isNew) navigate(`/admin/services/${saved.id}`, { replace: true });
      else setOk("Saved.");
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }

  if (loading) return (<><div className="admin-topbar"><h1>Edit service</h1></div><div className="admin-content"><div className="admin-empty">Loading…</div></div></>);
  const a = tr[activeLang];

  return (
    <>
      <div className="admin-topbar">
        <h1>{isNew ? "New service" : "Edit service"}</h1>
        <button className="admin-btn ghost" onClick={() => navigate("/admin/services")}>← Back</button>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}
        <div className="admin-card">
          <div className="admin-row">
            <div className="admin-field"><label>Code</label><input value={code} onChange={(e) => setCode(e.target.value)} placeholder="e.g. poverty-relief" /></div>
            <div className="admin-field"><label>Sort order</label><input type="number" value={sortOrder} onChange={(e) => setSortOrder(e.target.value)} /></div>
            <div className="admin-field">
              <label className="checkbox" style={{ display: "flex", alignItems: "center", gap: 8, fontWeight: 400 }}>
                <input type="checkbox" style={{ width: "auto" }} checked={active} onChange={(e) => setActive(e.target.checked)} /> Active
              </label>
            </div>
          </div>
          <div className="admin-field">
            <label>Icon</label>
            <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
              <button type="button" className="admin-btn" disabled={uploading} onClick={() => iconRef.current && iconRef.current.click()}>{uploading ? "Uploading…" : "Upload"}</button>
              <input ref={iconRef} type="file" accept="image/*" style={{ display: "none" }} onChange={onIcon} />
              {iconMediaId ? <span className="meta">media #{iconMediaId}</span> : null}
            </div>
            {iconUrl ? <img src={iconUrl} alt="" style={{ marginTop: 10, maxHeight: 60, borderRadius: "50%" }} /> : null}
          </div>
        </div>
        <div className="admin-card">
          <div className="lang-tabs">
            {LANGS.map((l) => <button key={l.code} type="button" className={`lang-tab ${activeLang === l.code ? "active" : ""}`} onClick={() => setActiveLang(l.code)}>{l.label}{l.required ? <span className="req"> *</span> : null}</button>)}
          </div>
          <div className="admin-field"><label>Name{activeLang === "zh-Hant" ? " (required)" : ""}</label><input value={a.name} onChange={(e) => setField(activeLang, "name", e.target.value)} /></div>
          <div className="admin-field"><label>Description</label><textarea value={a.description} onChange={(e) => setField(activeLang, "description", e.target.value)} /></div>
        </div>
        <div className="admin-actions">
          <button className="admin-btn primary" disabled={busy} onClick={save}>{busy ? "Saving…" : "Save"}</button>
        </div>
      </div>
    </>
  );
}
