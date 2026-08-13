import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { adminApi } from "../../lib/adminApi";

const LANGS = [
  { code: "zh-Hant", label: "繁中", required: true },
  { code: "en", label: "EN" },
  { code: "zh-Hans", label: "简中" },
];
const empty = () => { const o = {}; for (const l of LANGS) o[l.code] = { title: "", summary: "", bodyMarkdown: "" }; return o; };
const toHongKongInput = (iso) => iso ? new Date(iso).toLocaleString("sv-SE", { timeZone: "Asia/Hong_Kong" }).replace(" ", "T").slice(0, 16) : "";
const toIso = (value) => value ? new Date(`${value}:00+08:00`).toISOString() : null;

export default function EventEdit() {
  const { id } = useParams();
  const isNew = id === "new";
  const navigate = useNavigate();
  const [slug, setSlug] = useState("");
  const [status, setStatus] = useState("DRAFT");
  const [startsAt, setStartsAt] = useState("");
  const [endsAt, setEndsAt] = useState("");
  const [locationText, setLocationText] = useState("");
  const [coverImageUrl, setCoverImageUrl] = useState("");
  const [tr, setTr] = useState(empty());
  const [activeLang, setActiveLang] = useState("zh-Hant");
  const [error, setError] = useState(""); const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false); const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(!isNew);
  const coverRef = useRef(null); const bodyFileRef = useRef(null); const bodyRef = useRef(null);

  useEffect(() => {
    if (isNew) return;
    let alive = true;
    (async () => {
      try {
        const e = await adminApi.getEvent(id);
        if (!alive) return;
        setSlug(e.slug || ""); setStatus(e.status || "DRAFT");
        setStartsAt(toHongKongInput(e.startsAt)); setEndsAt(toHongKongInput(e.endsAt));
        setLocationText(e.locationText || ""); setCoverImageUrl(e.coverImageUrl || "");
        const base = empty();
        for (const t of e.translations || []) if (base[t.lang]) base[t.lang] = { title: t.title || "", summary: t.summary || "", bodyMarkdown: t.bodyMarkdown || "" };
        setTr(base);
      } catch (er) { if (alive) setError(er.message); } finally { if (alive) setLoading(false); }
    })();
    return () => { alive = false; };
  }, [id]);

  const setField = (lang, f, v) => setTr((p) => ({ ...p, [lang]: { ...p[lang], [f]: v } }));

  async function onCover(e) {
    const f = e.target.files && e.target.files[0]; if (!f) return;
    setUploading(true); setError("");
    try { const r = await adminApi.uploadMedia(f); setCoverImageUrl(r.url); }
    catch (er) { setError(`Upload failed: ${er.message}`); }
    finally { setUploading(false); if (coverRef.current) coverRef.current.value = ""; }
  }
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
    if (startsAt && endsAt && endsAt < startsAt) { setError("Event end must not be before start"); return; }
    if (publish && !startsAt) { setError("Event start date and time are required before publishing"); return; }
    const hant = translations.find((t) => t.lang === "zh-Hant");
    if (!hant || !hant.title.trim()) { setError("繁中 title is required"); return; }
    setBusy(true);
    try {
      const newId = await adminApi.saveEvent({ id: isNew ? null : Number(id), slug: slug.trim(), startsAt: toIso(startsAt), endsAt: toIso(endsAt), locationText: locationText.trim() || null, coverImageUrl: coverImageUrl.trim() || null, translations });
      if (publish) await adminApi.publishEvent(newId);
      if (isNew) navigate(`/admin/events/${newId}`, { replace: true });
      else { setStatus(publish ? "PUBLISHED" : "DRAFT"); setOk(publish ? "Published." : "Saved as draft."); }
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }

  if (loading) return (<><div className="admin-topbar"><h1>Edit event</h1></div><div className="admin-content"><div className="admin-empty">Loading…</div></div></>);
  const a = tr[activeLang];

  return (
    <>
      <div className="admin-topbar">
        <h1>{isNew ? "New event" : "Edit event"}{!isNew ? <span className={`admin-badge ${status === "PUBLISHED" ? "published" : "draft"}`} style={{ marginLeft: 10, verticalAlign: "middle" }}>{status === "PUBLISHED" ? "Published" : "Draft"}</span> : null}</h1>
        <button className="admin-btn ghost" onClick={() => navigate("/admin/events")}>← Back</button>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}
        <div className="admin-card">
          <div className="admin-row">
            <div className="admin-field"><label>Slug</label><input value={slug} onChange={(e) => setSlug(e.target.value)} placeholder="event-slug" /></div>
            <div className="admin-field"><label>Starts (Hong Kong time)</label><input type="datetime-local" value={startsAt} onChange={(e) => setStartsAt(e.target.value)} /></div>
            <div className="admin-field"><label>Ends (Hong Kong time)</label><input type="datetime-local" value={endsAt} onChange={(e) => setEndsAt(e.target.value)} /></div>
          </div>
          <div className="admin-field"><label>Location</label><input value={locationText} onChange={(e) => setLocationText(e.target.value)} /></div>
          <div className="admin-field">
            <label>Cover image</label>
            <div style={{ display: "flex", gap: 8 }}>
              <input style={{ flex: 1 }} value={coverImageUrl} onChange={(e) => setCoverImageUrl(e.target.value)} placeholder="https://… or Upload →" />
              <button type="button" className="admin-btn" disabled={uploading} onClick={() => coverRef.current && coverRef.current.click()}>{uploading ? "Uploading…" : "Upload"}</button>
              <input ref={coverRef} type="file" accept="image/*" style={{ display: "none" }} onChange={onCover} />
            </div>
            {coverImageUrl ? <img src={coverImageUrl} alt="" style={{ marginTop: 10, maxHeight: 120, borderRadius: "var(--radius-photo)", border: "1px solid var(--color-line)" }} /> : null}
          </div>
        </div>
        <div className="admin-card">
          <div className="lang-tabs">
            {LANGS.map((l) => <button key={l.code} type="button" className={`lang-tab ${activeLang === l.code ? "active" : ""}`} onClick={() => setActiveLang(l.code)}>{l.label}{l.required ? <span className="req"> *</span> : null}</button>)}
          </div>
          <div className="admin-field"><label>Title{activeLang === "zh-Hant" ? " (required)" : ""}</label><input value={a.title} onChange={(e) => setField(activeLang, "title", e.target.value)} /></div>
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
