import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { adminApi } from "../../lib/adminApi";

const LANGS = [
  { code: "zh-Hant", prefix: "tc", label: "繁中", required: true },
  { code: "en", prefix: "en", label: "EN" },
  { code: "zh-Hans", prefix: "sc", label: "简中" },
];
const empty = () => { const o = {}; for (const l of LANGS) o[l.code] = { title: "", summary: "", body: "" }; return o; };

export default function BlogEdit() {
  const { id } = useParams();
  const isNew = id === "new";
  const navigate = useNavigate();

  const [services, setServices] = useState([]);
  const [slug, setSlug] = useState("");
  const [serviceId, setServiceId] = useState("");
  const [coverMediaId, setCoverMediaId] = useState(null);
  const [coverUrl, setCoverUrl] = useState("");
  const [readMinutes, setReadMinutes] = useState("");
  const [galleryLayout, setGalleryLayout] = useState("NONE");
  const [galleryMedia, setGalleryMedia] = useState([]); // [{id,url}]
  const [tr, setTr] = useState(empty());
  const [status, setStatus] = useState("DRAFT");
  const [activeLang, setActiveLang] = useState("zh-Hant");
  const [error, setError] = useState(""); const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false); const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(!isNew);

  const coverRef = useRef(null); const galleryRef = useRef(null);

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        const svc = await adminApi.listServices();
        if (!alive) return;
        setServices(svc);
        if (!isNew) {
          const b = await adminApi.getBlog(id);
          if (!alive) return;
          setSlug(b.slug || "");
          setServiceId(b.serviceId != null ? String(b.serviceId) : "");
          setCoverMediaId(b.coverMediaId || null);
          setReadMinutes(b.readMinutes != null ? String(b.readMinutes) : "");
          setGalleryLayout(b.galleryLayout || "NONE");
          setStatus(b.status || "DRAFT");
          setTr({
            "zh-Hant": { title: b.tcTitle || "", summary: b.tcSummary || "", body: b.tcBody || "" },
            en: { title: b.enTitle || "", summary: b.enSummary || "", body: b.enBody || "" },
            "zh-Hans": { title: b.scTitle || "", summary: b.scSummary || "", body: b.scBody || "" },
          });
          // Media detail (URL) isn't in AdminBlogDetailDto — only ids. Show ids as a lightweight
          // reference list; the media library screen is the source of truth for previews.
          // Prefer the URL-carrying list so existing images show a thumbnail;
          // fall back to bare ids for an older API response.
          setGalleryMedia(
            b.galleryMedia?.length
              ? b.galleryMedia.map((m) => ({ id: m.id, url: m.url }))
              : (b.galleryMediaIds || []).map((mid) => ({ id: mid, url: null })),
          );
        }
      } catch (e) { if (alive) setError(e.message); }
      finally { if (alive) setLoading(false); }
    })();
    return () => { alive = false; };
  }, [id, isNew]);

  const setField = (lang, f, v) => setTr((p) => ({ ...p, [lang]: { ...p[lang], [f]: v } }));

  async function onCover(e) {
    const f = e.target.files && e.target.files[0]; if (!f) return;
    setUploading(true); setError("");
    try { const m = await adminApi.uploadMedia(f); setCoverMediaId(m.id); setCoverUrl(m.url); }
    catch (er) { setError(`Upload failed: ${er.message}`); }
    finally { setUploading(false); if (coverRef.current) coverRef.current.value = ""; }
  }
  async function onGalleryFiles(e) {
    const files = Array.from(e.target.files || []); if (!files.length) return;
    setUploading(true); setError("");
    for (const f of files) {
      try { const m = await adminApi.uploadMedia(f); setGalleryMedia((prev) => [...prev, { id: m.id, url: m.url }]); }
      catch (er) { setError(`Upload failed: ${er.message}`); }
    }
    setUploading(false);
    if (galleryRef.current) galleryRef.current.value = "";
  }
  function removeGalleryItem(mid) {
    setGalleryMedia((prev) => prev.filter((g) => g.id !== mid));
  }

  function buildPayload(withStatus) {
    return {
      slug: slug.trim(),
      serviceId: serviceId ? Number(serviceId) : null,
      coverMediaId,
      readMinutes: readMinutes ? Number(readMinutes) : null,
      galleryLayout,
      status: withStatus,
      publishAt: null,
      unpublishAt: null,
      tcTitle: tr["zh-Hant"].title, enTitle: tr.en.title, scTitle: tr["zh-Hans"].title,
      tcSummary: tr["zh-Hant"].summary, enSummary: tr.en.summary, scSummary: tr["zh-Hans"].summary,
      tcBody: tr["zh-Hant"].body, enBody: tr.en.body, scBody: tr["zh-Hans"].body,
      galleryMediaIds: galleryMedia.map((g) => g.id),
    };
  }

  async function save(withStatus) {
    setError(""); setOk("");
    if (!slug.trim()) { setError("Slug is required"); return; }
    if (!tr["zh-Hant"].title.trim()) { setError("繁中 title is required"); return; }
    setBusy(true);
    try {
      const payload = buildPayload(withStatus);
      const saved = isNew ? await adminApi.createBlog(payload) : await adminApi.updateBlog(id, payload);
      setStatus(withStatus);
      if (isNew) navigate(`/admin/blog/${saved.id}`, { replace: true });
      else setOk(withStatus === "PUBLISHED" ? "Published." : "Saved.");
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }

  if (loading) return (<><div className="admin-topbar"><h1>Edit post</h1></div><div className="admin-content"><div className="admin-empty">Loading…</div></div></>);
  const a = tr[activeLang];

  return (
    <>
      <div className="admin-topbar">
        <h1>{isNew ? "New post" : "Edit post"}{!isNew ? <span className={`admin-badge ${status === "PUBLISHED" ? "published" : "draft"}`} style={{ marginLeft: 10, verticalAlign: "middle" }}>{status}</span> : null}</h1>
        <button className="admin-btn ghost" onClick={() => navigate("/admin/blog")}>← Back</button>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}
        <div className="admin-card">
          <div className="admin-row">
            <div className="admin-field"><label>Slug</label><input value={slug} onChange={(e) => setSlug(e.target.value)} placeholder="my-post-url" /></div>
            <div className="admin-field"><label>Service tag</label>
              <select value={serviceId} onChange={(e) => setServiceId(e.target.value)}>
                <option value="">—</option>
                {services.map((s) => <option key={s.id} value={s.id}>{s.tcName}</option>)}
              </select>
            </div>
            <div className="admin-field"><label>Reading minutes</label><input type="number" min="0" value={readMinutes} onChange={(e) => setReadMinutes(e.target.value)} /></div>
          </div>
          <div className="admin-field">
            <label>Cover image</label>
            <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
              <button type="button" className="admin-btn" disabled={uploading} onClick={() => coverRef.current && coverRef.current.click()}>{uploading ? "Uploading…" : "Upload"}</button>
              <input ref={coverRef} type="file" accept="image/*" style={{ display: "none" }} onChange={onCover} />
              {coverMediaId ? <span className="meta">media #{coverMediaId}</span> : null}
            </div>
            {coverUrl ? <img src={coverUrl} alt="" style={{ marginTop: 10, maxHeight: 120, borderRadius: "var(--radius-photo)", border: "1px solid var(--color-line)" }} /> : null}
          </div>
          <div className="admin-row">
            <div className="admin-field"><label>Gallery layout</label>
              <select value={galleryLayout} onChange={(e) => setGalleryLayout(e.target.value)}>
                <option value="NONE">None</option><option value="CAROUSEL">Carousel</option>
                <option value="GRID">Grid</option><option value="MASONRY">Masonry</option>
              </select>
            </div>
          </div>
          {galleryLayout !== "NONE" && (
            <div className="admin-field">
              <label>Gallery images</label>
              <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 8 }}>
                {galleryMedia.map((g) => (
                  <div key={g.id} style={{ position: "relative" }}>
                    {g.url ? <img src={g.url} alt="" style={{ width: 80, height: 80, objectFit: "cover", borderRadius: 6, border: "1px solid var(--color-line)" }} />
                      : <div style={{ width: 80, height: 80, display: "grid", placeItems: "center", background: "var(--color-photo-idle)", borderRadius: 6, fontSize: 11 }}>#{g.id}</div>}
                    <button type="button" onClick={() => removeGalleryItem(g.id)} className="admin-btn admin-btn-sm danger" style={{ position: "absolute", top: -8, right: -8, padding: "0 6px" }}>×</button>
                  </div>
                ))}
              </div>
              <button type="button" className="admin-btn admin-btn-sm" disabled={uploading} onClick={() => galleryRef.current && galleryRef.current.click()}>{uploading ? "Uploading…" : "Add images"}</button>
              <input ref={galleryRef} type="file" accept="image/*" multiple style={{ display: "none" }} onChange={onGalleryFiles} />
            </div>
          )}
        </div>
        <div className="admin-card">
          <div className="lang-tabs">
            {LANGS.map((l) => <button key={l.code} type="button" className={`lang-tab ${activeLang === l.code ? "active" : ""}`} onClick={() => setActiveLang(l.code)}>{l.label}{l.required ? <span className="req"> *</span> : null}</button>)}
          </div>
          <div className="admin-field"><label>Title{activeLang === "zh-Hant" ? " (required)" : ""}</label><input value={a.title} onChange={(e) => setField(activeLang, "title", e.target.value)} /></div>
          <div className="admin-field"><label>Summary</label><input value={a.summary} onChange={(e) => setField(activeLang, "summary", e.target.value)} /></div>
          <div className="admin-field"><label>Body (Markdown)</label><textarea value={a.body} onChange={(e) => setField(activeLang, "body", e.target.value)} /></div>
          <div className="hint">Leave a language blank to fall back to 繁中.</div>
        </div>
        <div className="admin-actions">
          <button className="admin-btn primary" disabled={busy} onClick={() => save("DRAFT")}>{busy ? "Saving…" : "Save draft"}</button>
          <button className="admin-btn" disabled={busy} onClick={() => save("PUBLISHED")}>Save &amp; publish</button>
          {!isNew && <button className="admin-btn ghost" disabled={busy} onClick={() => save("ARCHIVED")}>Archive</button>}
        </div>
      </div>
    </>
  );
}
