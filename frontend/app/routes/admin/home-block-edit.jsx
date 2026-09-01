import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { adminApi } from "../../lib/adminApi";

const SLOTS = ["HERO", "STAT", "FEATURED", "SUPPORT", "VOLUNTEER", "QUICK_LINK"];
const LANGS = [
  { code: "zh-Hant", label: "繁中", required: true },
  { code: "en", label: "EN" },
  { code: "zh-Hans", label: "简中" },
];
// What each slot does with these fields on the public home page. Keeping this
// next to the form is the difference between "Title" meaning something and an
// editor guessing.

const SLOT_CAPABILITIES = {
  HERO:       { media: true,  link: true,  blog: false, button: true,  eyebrow: true,  note: false, repeatable: false },
  SUPPORT:    { media: false, link: true,  blog: false, button: true,  eyebrow: true,  note: true,  repeatable: false },
  FEATURED:   { media: true,  link: true,  blog: true,  button: true,  eyebrow: false, note: false, repeatable: false },
  STAT:       { media: false, link: false, blog: false, button: false, eyebrow: false, note: false, repeatable: true },
  QUICK_LINK: { media: false, link: true,  blog: false, button: false, eyebrow: false, note: false, repeatable: true },
  VOLUNTEER:  { media: true,  link: true,  blog: false, button: true,  eyebrow: false, note: false, repeatable: true },
};
const capsFor = (slot) => SLOT_CAPABILITIES[slot] || SLOT_CAPABILITIES.QUICK_LINK;

const EYEBROW_HINT = {
  HERO: "Small label over the headline, e.g. 與社區同行.",
  SUPPORT: "Small label over the red band's headline, e.g. 招募義工 · VOLUNTEER.",
  STAT: "Not shown on stat tiles — leave blank.",
  QUICK_LINK: "Not shown on quick links — leave blank.",
};

const NOTE_HINT = {
  SUPPORT: "Fine print beside the button — the form's length and the under-18 consent note. Press Enter for a second line.",
  HERO: "Not shown on the hero — leave blank.",
  STAT: "Not shown on stat tiles — leave blank.",
  QUICK_LINK: "Not shown on quick links — leave blank.",
};

const TITLE_HINT = {
  HERO: "The big headline over the hero image. Press Enter to control where it breaks.",
  STAT: "The number only — e.g. 895, 5, 2021.",
  SUPPORT: "The headline in the red band. Press Enter to control where it breaks.",
  FEATURED: "Headline for the featured story card.",
  QUICK_LINK: "The quick-link's label, e.g. 物資捐贈.",
  VOLUNTEER: "The quick-link's label.",
};

const SUBTITLE_HINT = {
  HERO: "The intro paragraph under the headline.",
  STAT: "First line is the label (位支持者); an optional second line is the caption (已與我們同行).",
  SUPPORT: "The paragraph under the red band's headline.",
  FEATURED: "One-sentence summary on the card.",
  QUICK_LINK: "The small line under the label.",
  VOLUNTEER: "The small line under the label.",
};

const empty = () => { const o = {}; for (const l of LANGS) o[l.code] = { eyebrow: "", title: "", subtitle: "", buttonLabel: "", note: "" }; return o; };

export default function HomeBlockEdit() {
  const { id } = useParams();
  const isNew = id === "new";
  const navigate = useNavigate();

  const [slot, setSlot] = useState("HERO");
  const [mediaId, setMediaId] = useState(null);
  const [mediaUrl, setMediaUrl] = useState("");
  const [blogId, setBlogId] = useState("");
  const [linkUrl, setLinkUrl] = useState("");
  const [sortOrder, setSortOrder] = useState("0");
  const [active, setActive] = useState(true);
  const [tr, setTr] = useState(empty());
  const [activeLang, setActiveLang] = useState("zh-Hant");
  const [error, setError] = useState(""); const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false); const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(!isNew);
  const [takenSlots, setTakenSlots] = useState([]);
  const mediaRef = useRef(null);

  useEffect(() => {
    if (isNew) return;
    let alive = true;
    (async () => {
      try {
        const b = await adminApi.getHomeBlock(id);
        if (!alive) return;
        setSlot(b.slot || "HERO"); setMediaId(b.mediaId || null); setBlogId(b.blogId != null ? String(b.blogId) : "");
        setLinkUrl(b.linkUrl || ""); setSortOrder(String(b.sortOrder ?? 0)); setActive(!!b.active);
        setTr({
          "zh-Hant": { eyebrow: b.tcEyebrow || "", title: b.tcTitle || "", subtitle: b.tcSubtitle || "", buttonLabel: b.tcButtonLabel || "", note: b.tcNote || "" },
          en: { eyebrow: b.enEyebrow || "", title: b.enTitle || "", subtitle: b.enSubtitle || "", buttonLabel: b.enButtonLabel || "", note: b.enNote || "" },
          "zh-Hans": { eyebrow: b.scEyebrow || "", title: b.scTitle || "", subtitle: b.scSubtitle || "", buttonLabel: b.scButtonLabel || "", note: b.scNote || "" },
        });
      } catch (e) { if (alive) setError(e.message); } finally { if (alive) setLoading(false); }
    })();
    return () => { alive = false; };
  }, [id, isNew]);

  // Which single-block slots are already used. Fetched only when creating, so
  // editing an existing HERO never hides its own slot.
  useEffect(() => {
    if (!isNew) return undefined;
    let alive = true;
    adminApi.listHomeBlocks()
      .then((blocks) => {
        if (!alive) return;
        const taken = [...new Set(blocks.map((b) => b.slot).filter((sl) => !capsFor(sl).repeatable))];
        setTakenSlots(taken);
        // Land on a slot that can actually take a new block.
        const firstFree = SLOTS.find((sl) => capsFor(sl).repeatable || !taken.includes(sl));
        if (firstFree) setSlot(firstFree);
      })
      .catch(() => { /* the save path enforces this server-side regardless */ });
    return () => { alive = false; };
  }, [isNew]);

  const setField = (lang, f, v) => setTr((p) => ({ ...p, [lang]: { ...p[lang], [f]: v } }));

  async function onMedia(e) {
    const f = e.target.files && e.target.files[0]; if (!f) return;
    setUploading(true); setError("");
    try { const m = await adminApi.uploadMedia(f); setMediaId(m.id); setMediaUrl(m.url); }
    catch (er) { setError(`Upload failed: ${er.message}`); }
    finally { setUploading(false); if (mediaRef.current) mediaRef.current.value = ""; }
  }

  async function save() {
    setError(""); setOk("");
    if (!tr["zh-Hant"].title.trim()) { setError("繁中 title is required"); return; }
    setBusy(true);
    const payload = {
      slot, mediaId, blogId: blogId ? Number(blogId) : null, linkUrl: linkUrl.trim() || null,
      sortOrder: Number(sortOrder) || 0, active, publishAt: null, unpublishAt: null,
      tcTitle: tr["zh-Hant"].title, enTitle: tr.en.title, scTitle: tr["zh-Hans"].title,
      tcEyebrow: tr["zh-Hant"].eyebrow, enEyebrow: tr.en.eyebrow, scEyebrow: tr["zh-Hans"].eyebrow,
      tcSubtitle: tr["zh-Hant"].subtitle, enSubtitle: tr.en.subtitle, scSubtitle: tr["zh-Hans"].subtitle,
      tcNote: tr["zh-Hant"].note, enNote: tr.en.note, scNote: tr["zh-Hans"].note,
      tcButtonLabel: tr["zh-Hant"].buttonLabel, enButtonLabel: tr.en.buttonLabel, scButtonLabel: tr["zh-Hans"].buttonLabel,
    };
    try {
      const saved = isNew ? await adminApi.createHomeBlock(payload) : await adminApi.updateHomeBlock(id, payload);
      if (isNew) navigate(`/admin/home-blocks/${saved.id}`, { replace: true });
      else setOk("Saved.");
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }

  if (loading) return (<><div className="admin-topbar"><h1>Edit block</h1></div><div className="admin-content"><div className="admin-empty">Loading…</div></div></>);
  const a = tr[activeLang];
  const caps = capsFor(slot);

  return (
    <>
      <div className="admin-topbar">
        <h1>{isNew ? "New home-page block" : "Edit home-page block"}</h1>
        <button className="admin-btn ghost" onClick={() => navigate("/admin/home-blocks")}>← Back</button>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}
        <div className="admin-card">
          <div className="admin-row">
            <div className="admin-field"><label>Slot</label>
              <select value={slot} onChange={(e) => setSlot(e.target.value)}>
                {SLOTS.filter((sl) => !isNew || capsFor(sl).repeatable || !takenSlots.includes(sl))
                  .map((sl) => <option key={sl} value={sl}>{sl}</option>)}
              </select>
              <p className="admin-hint">
                {caps.repeatable
                  ? "This slot shows every block you add, in sort order."
                  : "The home page shows one block in this slot, so there can only be one."}
              </p>
            </div>
            {caps.repeatable && (
              <div className="admin-field">
                <label>Sort order (within slot)</label>
                <input type="number" value={sortOrder} onChange={(e) => setSortOrder(e.target.value)} />
                <p className="admin-hint">Low numbers first. Only this slot shows more than one block.</p>
              </div>
            )}
            <div className="admin-field">
              <label className="checkbox" style={{ display: "flex", alignItems: "center", gap: 8, fontWeight: 400 }}>
                <input type="checkbox" style={{ width: "auto" }} checked={active} onChange={(e) => setActive(e.target.checked)} /> Active
              </label>
            </div>
          </div>
          <div className="admin-row">
            {caps.link && (
              <div className="admin-field">
                <label>Link URL</label>
                <input value={linkUrl} onChange={(e) => setLinkUrl(e.target.value)} placeholder="/volunteer" />
                <p className="admin-hint">Where the block's button or card goes. Start with / for a page on this site.</p>
              </div>
            )}
            {caps.blog && (
              <div className="admin-field">
                <label>Pin a blog post</label>
                <input type="number" value={blogId} onChange={(e) => setBlogId(e.target.value)} placeholder="Blog post ID" />
                <p className="admin-hint">The story this block links to. Find the id in the Blog list.</p>
              </div>
            )}
          </div>
          {caps.media && (
          <div className="admin-field">
            <label>Media</label>
            <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
              <button type="button" className="admin-btn" disabled={uploading} onClick={() => mediaRef.current && mediaRef.current.click()}>{uploading ? "Uploading…" : "Upload"}</button>
              <input ref={mediaRef} type="file" accept="image/*" style={{ display: "none" }} onChange={onMedia} />
              {mediaId ? <span className="meta">media #{mediaId}</span> : null}
            </div>
            {mediaUrl ? <img src={mediaUrl} alt="" style={{ marginTop: 10, maxHeight: 120, borderRadius: "var(--radius-photo)", border: "1px solid var(--color-line)" }} /> : null}
          </div>
          )}
        </div>
        <div className="admin-card">
          <div className="lang-tabs">
            {LANGS.map((l) => <button key={l.code} type="button" className={`lang-tab ${activeLang === l.code ? "active" : ""}`} onClick={() => setActiveLang(l.code)}>{l.label}{l.required ? <span className="req"> *</span> : null}</button>)}
          </div>
          {/* Textareas, not inputs: the home page keeps line breaks in these two
              fields — a HERO/SUPPORT headline wraps where the editor breaks it,
              and a STAT's caption is the second line of its subtitle. An <input>
              cannot produce either. */}
          {caps.eyebrow && (
          <div className="admin-field">
            <label>Eyebrow</label>
            <input value={a.eyebrow} onChange={(e) => setField(activeLang, "eyebrow", e.target.value)} />
            <p className="admin-hint">{EYEBROW_HINT[slot] || "Small label above the title. Leave blank to hide it."}</p>
          </div>
          )}
          <div className="admin-field">
            <label>Title{activeLang === "zh-Hant" ? " (required)" : ""}</label>
            <textarea className="compact" rows={2} value={a.title} onChange={(e) => setField(activeLang, "title", e.target.value)} />
            <p className="admin-hint">{TITLE_HINT[slot] || "Shown as the block heading."}</p>
          </div>
          <div className="admin-field">
            <label>Subtitle</label>
            <textarea className="compact" rows={2} value={a.subtitle} onChange={(e) => setField(activeLang, "subtitle", e.target.value)} />
            <p className="admin-hint">{SUBTITLE_HINT[slot] || "Supporting line under the title."}</p>
          </div>
          {caps.button && (
            <div className="admin-field">
              <label>Button label</label>
              <input value={a.buttonLabel} onChange={(e) => setField(activeLang, "buttonLabel", e.target.value)} />
            </div>
          )}
          {caps.note && (
          <div className="admin-field">
            <label>Note (beside the button)</label>
            <textarea className="compact" rows={2} value={a.note} onChange={(e) => setField(activeLang, "note", e.target.value)} />
            <p className="admin-hint">{NOTE_HINT[slot] || "Fine print next to the button. Press Enter for a second line."}</p>
          </div>
          )}
        </div>
        <div className="admin-actions">
          <button className="admin-btn primary" disabled={busy} onClick={save}>{busy ? "Saving…" : "Save"}</button>
        </div>
      </div>
    </>
  );
}
