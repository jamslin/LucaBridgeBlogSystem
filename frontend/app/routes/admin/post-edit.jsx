import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { adminApi } from "../../lib/adminApi";

const LANGS = [
  { code: "zh-Hant", label: "繁中", required: true },
  { code: "en", label: "EN" },
  { code: "zh-Hans", label: "简中" },
];

function emptyTranslations() {
  const o = {};
  for (const l of LANGS) o[l.code] = { title: "", subtitle: "", excerpt: "", bodyMarkdown: "" };
  return o;
}

export default function PostEdit() {
  const { id } = useParams();
  const isNew = id === "new";
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);
  const [slug, setSlug] = useState("");
  const [categoryKey, setCategoryKey] = useState("");
  const [coverImageUrl, setCoverImageUrl] = useState("");
  const [readingMinutes, setReadingMinutes] = useState("");
  const [tr, setTr] = useState(emptyTranslations());
  const [status, setStatus] = useState("DRAFT");
  const [activeLang, setActiveLang] = useState("zh-Hant");
  const [error, setError] = useState("");
  const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(!isNew);

  const coverInputRef = useRef(null);
  const bodyInputRef = useRef(null);
  const bodyRef = useRef(null);

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        const cats = await adminApi.categories();
        if (!alive) return;
        setCategories(cats);
        if (isNew) {
          if (cats.length) setCategoryKey((prev) => prev || cats[0].key);
        } else {
          const p = await adminApi.getPost(id);
          if (!alive) return;
          setSlug(p.slug || "");
          setCategoryKey(p.categoryKey || (cats[0] && cats[0].key) || "");
          setCoverImageUrl(p.coverImageUrl || "");
          setReadingMinutes(p.readingMinutes != null ? String(p.readingMinutes) : "");
          setStatus(p.status || "DRAFT");
          const base = emptyTranslations();
          for (const t of p.translations || []) {
            if (base[t.lang]) {
              base[t.lang] = { title: t.title || "", subtitle: t.subtitle || "", excerpt: t.excerpt || "", bodyMarkdown: t.bodyMarkdown || "" };
            }
          }
          setTr(base);
        }
      } catch (e) { if (alive) setError(e.message); }
      finally { if (alive) setLoading(false); }
    })();
    return () => { alive = false; };
  }, [id]);

  function setField(lang, field, value) {
    setTr((prev) => ({ ...prev, [lang]: { ...prev[lang], [field]: value } }));
  }

  async function onCoverFile(e) {
    const f = e.target.files && e.target.files[0];
    if (!f) return;
    setError(""); setUploading(true);
    try {
      const r = await adminApi.uploadMedia(f);
      setCoverImageUrl(r.url);
    } catch (err) { setError(`Upload failed: ${err.message}`); }
    finally { setUploading(false); if (coverInputRef.current) coverInputRef.current.value = ""; }
  }

  async function onBodyImageFile(e) {
    const f = e.target.files && e.target.files[0];
    if (!f) return;
    setError(""); setUploading(true);
    try {
      const r = await adminApi.uploadMedia(f);
      const md = `![](${r.url})`;
      const cur = tr[activeLang].bodyMarkdown || "";
      const ta = bodyRef.current;
      let next;
      if (ta && typeof ta.selectionStart === "number") {
        const s = ta.selectionStart, en = ta.selectionEnd;
        next = cur.slice(0, s) + md + cur.slice(en);
      } else {
        next = cur ? cur + "\n\n" + md : md;
      }
      setField(activeLang, "bodyMarkdown", next);
    } catch (err) { setError(`Upload failed: ${err.message}`); }
    finally { setUploading(false); if (bodyInputRef.current) bodyInputRef.current.value = ""; }
  }

  function buildPayload() {
    const translations = LANGS.map((l) => ({ lang: l.code, ...tr[l.code] })).filter((t) => t.title.trim() || t.bodyMarkdown.trim());
    return {
      id: isNew ? null : Number(id),
      slug: slug.trim(),
      categoryKey,
      coverImageUrl: coverImageUrl.trim() || null,
      readingMinutes: readingMinutes ? Number(readingMinutes) : null,
      translations,
    };
  }

  function validate(payload) {
    if (!payload.slug) return "Slug is required";
    if (!payload.categoryKey) return "Category is required";
    const hant = payload.translations.find((t) => t.lang === "zh-Hant");
    if (!hant || !hant.title.trim()) return "繁中 (zh-Hant) title is required";
    return null;
  }

  async function save(thenPublish) {
    setError(""); setOk("");
    const payload = buildPayload();
    const v = validate(payload);
    if (v) { setError(v); return; }
    setBusy(true);
    try {
      const newId = await adminApi.savePost(payload);
      if (thenPublish) await adminApi.publishPost(newId);
      if (isNew) navigate(`/admin/posts/${newId}`, { replace: true });
      else { if (thenPublish) setStatus("PUBLISHED"); setOk(thenPublish ? "Published." : "Saved."); }
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }

  if (loading) return (<><div className="admin-topbar"><h1>Edit post</h1></div><div className="admin-content"><div className="admin-empty">Loading…</div></div></>);

  const active = tr[activeLang];

  return (
    <>
      <div className="admin-topbar">
        <h1>
          {isNew ? "New post" : "Edit post"}
          {!isNew ? <span className={`admin-badge ${status === "PUBLISHED" ? "published" : "draft"}`} style={{ marginLeft: 10, verticalAlign: "middle" }}>{status === "PUBLISHED" ? "Published" : "Draft"}</span> : null}
        </h1>
        <button className="admin-btn ghost" onClick={() => navigate("/admin/posts")}>← Back</button>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}
        <div className="admin-card">
          <div className="admin-row">
            <div className="admin-field"><label>Slug</label><input value={slug} onChange={(e) => setSlug(e.target.value)} placeholder="my-post-url" /><div className="hint">URL: /blog/&lt;slug&gt;</div></div>
            <div className="admin-field"><label>Category</label>
              <select value={categoryKey} onChange={(e) => setCategoryKey(e.target.value)}>
                {categories.map((c) => <option key={c.key} value={c.key}>{c.name} ({c.key})</option>)}
              </select>
            </div>
            <div className="admin-field"><label>Reading minutes</label><input type="number" min="0" value={readingMinutes} onChange={(e) => setReadingMinutes(e.target.value)} /></div>
          </div>
          <div className="admin-field">
            <label>Cover image</label>
            <div style={{ display: "flex", gap: 8 }}>
              <input style={{ flex: 1 }} value={coverImageUrl} onChange={(e) => setCoverImageUrl(e.target.value)} placeholder="https://… or click Upload →" />
              <button type="button" className="admin-btn" disabled={uploading} onClick={() => coverInputRef.current && coverInputRef.current.click()}>{uploading ? "Uploading…" : "Upload"}</button>
              <input ref={coverInputRef} type="file" accept="image/*" style={{ display: "none" }} onChange={onCoverFile} />
            </div>
            {coverImageUrl ? (
              <img src={coverImageUrl} alt="cover preview" style={{ marginTop: 10, maxHeight: 120, borderRadius: "var(--radius-photo)", border: "1px solid var(--color-line)" }} />
            ) : null}
          </div>
        </div>
        <div className="admin-card">
          <div className="lang-tabs">
            {LANGS.map((l) => (
              <button key={l.code} type="button" className={`lang-tab ${activeLang === l.code ? "active" : ""}`} onClick={() => setActiveLang(l.code)}>
                {l.label}{l.required ? <span className="req"> *</span> : null}
              </button>
            ))}
          </div>
          <div className="admin-field"><label>Title{activeLang === "zh-Hant" ? " (required)" : ""}</label><input value={active.title} onChange={(e) => setField(activeLang, "title", e.target.value)} /></div>
          <div className="admin-field"><label>Subtitle</label><input value={active.subtitle} onChange={(e) => setField(activeLang, "subtitle", e.target.value)} /></div>
          <div className="admin-field"><label>Excerpt</label><input value={active.excerpt} onChange={(e) => setField(activeLang, "excerpt", e.target.value)} /></div>
          <div className="admin-field">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 6 }}>
              <label style={{ margin: 0 }}>Body (Markdown)</label>
              <button type="button" className="admin-btn admin-btn-sm" disabled={uploading} onClick={() => bodyInputRef.current && bodyInputRef.current.click()}>{uploading ? "Uploading…" : "Insert image"}</button>
              <input ref={bodyInputRef} type="file" accept="image/*" style={{ display: "none" }} onChange={onBodyImageFile} />
            </div>
            <textarea ref={bodyRef} value={active.bodyMarkdown} onChange={(e) => setField(activeLang, "bodyMarkdown", e.target.value)} />
          </div>
          <div className="hint">Upload inserts image markdown at the cursor. Leave a language blank to fall back to 繁中.</div>
        </div>
        <div className="admin-actions">
          <button className="admin-btn primary" disabled={busy} onClick={() => save(false)}>{busy ? "Saving…" : "Save draft"}</button>
          <button className="admin-btn" disabled={busy} onClick={() => save(true)}>Save &amp; publish</button>
        </div>
      </div>
    </>
  );
}
