import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { adminApi } from "../../lib/adminApi";

const LANGS = [
  { code: "zh-Hant", label: "繁中", required: true },
  { code: "en", label: "EN" },
  { code: "zh-Hans", label: "简中" },
];
const empty = () => { const o = {}; for (const l of LANGS) o[l.code] = { title: "", employmentTypeLabel: "", summary: "", bodyMarkdown: "" }; return o; };
const toDate = (iso) => (iso ? iso.slice(0, 10) : "");
const toIso = (d) => (d ? new Date(d + "T00:00:00Z").toISOString() : null);

export default function JobEdit() {
  const { id } = useParams();
  const isNew = id === "new";
  const navigate = useNavigate();
  const [slug, setSlug] = useState(""); const [status, setStatus] = useState("DRAFT");
  const [employmentType, setEmploymentType] = useState(""); const [department, setDepartment] = useState("");
  const [locationText, setLocationText] = useState(""); const [postedAt, setPostedAt] = useState(""); const [closesAt, setClosesAt] = useState("");
  const [tr, setTr] = useState(empty()); const [activeLang, setActiveLang] = useState("zh-Hant");
  const [error, setError] = useState(""); const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false); const [uploading, setUploading] = useState(false); const [loading, setLoading] = useState(!isNew);
  const bodyFileRef = useRef(null); const bodyRef = useRef(null);

  useEffect(() => {
    if (isNew) return;
    let alive = true;
    (async () => {
      try {
        const j = await adminApi.getJob(id); if (!alive) return;
        setSlug(j.slug || ""); setStatus(j.status || "DRAFT"); setEmploymentType(j.employmentType || ""); setDepartment(j.department || "");
        setLocationText(j.locationText || ""); setPostedAt(toDate(j.postedAt)); setClosesAt(toDate(j.closesAt));
        const base = empty();
        for (const t of j.translations || []) if (base[t.lang]) base[t.lang] = { title: t.title || "", employmentTypeLabel: t.employmentTypeLabel || "", summary: t.summary || "", bodyMarkdown: t.bodyMarkdown || "" };
        setTr(base);
      } catch (er) { if (alive) setError(er.message); } finally { if (alive) setLoading(false); }
    })();
    return () => { alive = false; };
  }, [id]);

  const setField = (lang, f, v) => setTr((p) => ({ ...p, [lang]: { ...p[lang], [f]: v } }));

  async function onBodyImg(e) {
    const f = e.target.files && e.target.files[0]; if (!f) return;
    setUploading(true); setError("");
    try {
      const r = await adminApi.uploadMedia(f);
      const md = `![](${r.url})`; const cur = tr[activeLang].bodyMarkdown || ""; const ta = bodyRef.current;
      const next = ta && typeof ta.selectionStart === "number" ? cur.slice(0, ta.selectionStart) + md + cur.slice(ta.selectionEnd) : (cur ? cur + "\n\n" + md : md);
      setField(activeLang, "bodyMarkdown", next);
    } catch (er) { setError(`Upload failed: ${er.message}`); }
    finally { setUploading(false); if (bodyFileRef.current) bodyFileRef.current.value = ""; }
  }

  async function save(publish) {
    setError(""); setOk("");
    const translations = LANGS.map((l) => ({ lang: l.code, ...tr[l.code] })).filter((t) => t.title.trim() || t.bodyMarkdown.trim());
    if (!slug.trim()) { setError("Slug is required"); return; }
    const hant = translations.find((t) => t.lang === "zh-Hant");
    if (!hant || !hant.title.trim()) { setError("繁中 title is required"); return; }
    setBusy(true);
    try {
      const newId = await adminApi.saveJob({ id: isNew ? null : Number(id), slug: slug.trim(), employmentType: employmentType.trim() || null, department: department.trim() || null, locationText: locationText.trim() || null, postedAt: toIso(postedAt), closesAt: toIso(closesAt), translations });
      if (publish) await adminApi.publishJob(newId);
      if (isNew) navigate(`/admin/jobs/${newId}`, { replace: true });
      else { if (publish) setStatus("PUBLISHED"); setOk(publish ? "Published." : "Saved."); }
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }

  if (loading) return (<><div className="admin-topbar"><h1>Edit job</h1></div><div className="admin-content"><div className="admin-empty">Loading…</div></div></>);
  const a = tr[activeLang];

  return (
    <>
      <div className="admin-topbar">
        <h1>{isNew ? "New job" : "Edit job"}{!isNew ? <span className={`admin-badge ${status === "PUBLISHED" ? "published" : "draft"}`} style={{ marginLeft: 10, verticalAlign: "middle" }}>{status === "PUBLISHED" ? "Published" : "Draft"}</span> : null}</h1>
        <button className="admin-btn ghost" onClick={() => navigate("/admin/jobs")}>← Back</button>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}
        <div className="admin-card">
          <div className="admin-row">
            <div className="admin-field"><label>Slug</label><input value={slug} onChange={(e) => setSlug(e.target.value)} placeholder="job-slug" /></div>
            <div className="admin-field"><label>Employment type (key)</label><input value={employmentType} onChange={(e) => setEmploymentType(e.target.value)} placeholder="full-time / part-time" /></div>
            <div className="admin-field"><label>Department</label><input value={department} onChange={(e) => setDepartment(e.target.value)} /></div>
          </div>
          <div className="admin-row">
            <div className="admin-field"><label>Location</label><input value={locationText} onChange={(e) => setLocationText(e.target.value)} /></div>
            <div className="admin-field"><label>Posted</label><input type="date" value={postedAt} onChange={(e) => setPostedAt(e.target.value)} /></div>
            <div className="admin-field"><label>Closes</label><input type="date" value={closesAt} onChange={(e) => setClosesAt(e.target.value)} /></div>
          </div>
        </div>
        <div className="admin-card">
          <div className="lang-tabs">
            {LANGS.map((l) => <button key={l.code} type="button" className={`lang-tab ${activeLang === l.code ? "active" : ""}`} onClick={() => setActiveLang(l.code)}>{l.label}{l.required ? <span className="req"> *</span> : null}</button>)}
          </div>
          <div className="admin-field"><label>Title{activeLang === "zh-Hant" ? " (required)" : ""}</label><input value={a.title} onChange={(e) => setField(activeLang, "title", e.target.value)} /></div>
          <div className="admin-field"><label>Employment type label</label><input value={a.employmentTypeLabel} onChange={(e) => setField(activeLang, "employmentTypeLabel", e.target.value)} placeholder="全職 / Full-time" /></div>
          <div className="admin-field"><label>Summary</label><input value={a.summary} onChange={(e) => setField(activeLang, "summary", e.target.value)} /></div>
          <div className="admin-field">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 6 }}>
              <label style={{ margin: 0 }}>Body (Markdown)</label>
              <button type="button" className="admin-btn admin-btn-sm" disabled={uploading} onClick={() => bodyFileRef.current && bodyFileRef.current.click()}>{uploading ? "Uploading…" : "Insert image"}</button>
              <input ref={bodyFileRef} type="file" accept="image/*" style={{ display: "none" }} onChange={onBodyImg} />
            </div>
            <textarea ref={bodyRef} value={a.bodyMarkdown} onChange={(e) => setField(activeLang, "bodyMarkdown", e.target.value)} />
          </div>
        </div>
        <div className="admin-actions">
          <button className="admin-btn primary" disabled={busy} onClick={() => save(false)}>{busy ? "Saving…" : "Save draft"}</button>
          <button className="admin-btn" disabled={busy} onClick={() => save(true)}>Save &amp; publish</button>
        </div>
      </div>
    </>
  );
}
