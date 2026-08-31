import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { adminApi } from "../../lib/adminApi";

const LANGS = [
  { code: "zh-Hant", label: "繁中", required: true },
  { code: "en", label: "EN" },
  { code: "zh-Hans", label: "简中" },
];
const empty = () => { const o = {}; for (const l of LANGS) o[l.code] = { title: "", summary: "", body: "", venue: "" }; return o; };
const toHongKongInput = (iso) => iso ? new Date(iso).toLocaleString("sv-SE", { timeZone: "Asia/Hong_Kong" }).replace(" ", "T").slice(0, 16) : "";
const toIso = (value) => value ? new Date(`${value}:00+08:00`).toISOString() : null;

export default function EventEdit() {
  const { id } = useParams();
  const isNew = id === "new";
  const navigate = useNavigate();

  const [services, setServices] = useState([]);
  const [slug, setSlug] = useState("");
  const [serviceId, setServiceId] = useState("");
  const [status, setStatus] = useState("DRAFT");
  const [startsAt, setStartsAt] = useState("");
  const [endsAt, setEndsAt] = useState("");
  const [venueMapUrl, setVenueMapUrl] = useState("");
  const [coverMediaId, setCoverMediaId] = useState(null);
  const [coverUrl, setCoverUrl] = useState("");
  const [galleryLayout, setGalleryLayout] = useState("NONE");
  const [registerable, setRegisterable] = useState(false);
  const [registrationOpensAt, setRegistrationOpensAt] = useState("");
  const [registrationClosesAt, setRegistrationClosesAt] = useState("");
  const [capacity, setCapacity] = useState("");
  const [tr, setTr] = useState(empty());
  const [activeLang, setActiveLang] = useState("zh-Hant");
  const [error, setError] = useState(""); const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false); const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(!isNew);
  const coverRef = useRef(null);

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        const svc = await adminApi.listServices();
        if (!alive) return;
        setServices(svc);
        if (!isNew) {
          const e = await adminApi.getEvent(id);
          if (!alive) return;
          setSlug(e.slug || ""); setServiceId(e.serviceId != null ? String(e.serviceId) : "");
          setStatus(e.status || "DRAFT");
          setStartsAt(toHongKongInput(e.startsAt)); setEndsAt(toHongKongInput(e.endsAt));
          setVenueMapUrl(e.venueMapUrl || ""); setCoverMediaId(e.coverMediaId || null);
          setGalleryLayout(e.galleryLayout || "NONE");
          setRegisterable(!!e.registerable);
          setRegistrationOpensAt(toHongKongInput(e.registrationOpensAt));
          setRegistrationClosesAt(toHongKongInput(e.registrationClosesAt));
          setCapacity(e.capacity != null ? String(e.capacity) : "");
          setTr({
            "zh-Hant": { title: e.tcTitle || "", summary: e.tcSummary || "", body: e.tcBody || "", venue: e.tcVenue || "" },
            en: { title: e.enTitle || "", summary: e.enSummary || "", body: e.enBody || "", venue: e.enVenue || "" },
            "zh-Hans": { title: e.scTitle || "", summary: e.scSummary || "", body: e.scBody || "", venue: e.scVenue || "" },
          });
        }
      } catch (er) { if (alive) setError(er.message); } finally { if (alive) setLoading(false); }
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

  function buildPayload(withStatus) {
    return {
      slug: slug.trim(),
      serviceId: serviceId ? Number(serviceId) : null,
      coverMediaId,
      galleryLayout,
      startsAt: toIso(startsAt), endsAt: toIso(endsAt),
      venueMapUrl: venueMapUrl.trim() || null,
      registerable,
      registrationOpensAt: registerable ? toIso(registrationOpensAt) : null,
      registrationClosesAt: registerable ? toIso(registrationClosesAt) : null,
      capacity: registerable && capacity !== "" ? Number(capacity) : null,
      status: withStatus,
      publishAt: null, unpublishAt: null,
      tcTitle: tr["zh-Hant"].title, enTitle: tr.en.title, scTitle: tr["zh-Hans"].title,
      tcSummary: tr["zh-Hant"].summary, enSummary: tr.en.summary, scSummary: tr["zh-Hans"].summary,
      tcBody: tr["zh-Hant"].body, enBody: tr.en.body, scBody: tr["zh-Hans"].body,
      tcVenue: tr["zh-Hant"].venue, enVenue: tr.en.venue, scVenue: tr["zh-Hans"].venue,
      galleryMediaIds: [],
    };
  }

  async function save(withStatus) {
    setError(""); setOk("");
    if (!slug.trim()) { setError("Slug is required"); return; }
    if (startsAt && endsAt && endsAt < startsAt) { setError("Event end must not be before start"); return; }
    if (withStatus === "PUBLISHED" && !startsAt) { setError("Event start date and time are required before publishing"); return; }
    if (!tr["zh-Hant"].title.trim()) { setError("繁中 title is required"); return; }
    setBusy(true);
    try {
      const payload = buildPayload(withStatus);
      const saved = isNew ? await adminApi.createEvent(payload) : await adminApi.updateEvent(id, payload);
      setStatus(withStatus);
      if (isNew) navigate(`/admin/events/${saved.id}`, { replace: true });
      else setOk(withStatus === "PUBLISHED" ? "Published." : "Saved.");
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }

  if (loading) return (<><div className="admin-topbar"><h1>Edit event</h1></div><div className="admin-content"><div className="admin-empty">Loading…</div></div></>);
  const a = tr[activeLang];

  return (
    <>
      <div className="admin-topbar">
        <h1>{isNew ? "New event" : "Edit event"}{!isNew ? <span className={`admin-badge ${status === "PUBLISHED" ? "published" : "draft"}`} style={{ marginLeft: 10, verticalAlign: "middle" }}>{status}</span> : null}</h1>
        <button className="admin-btn ghost" onClick={() => navigate("/admin/events")}>← Back</button>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}
        <div className="admin-card">
          <div className="admin-row">
            <div className="admin-field"><label>Slug</label><input value={slug} onChange={(e) => setSlug(e.target.value)} placeholder="event-slug" /></div>
            <div className="admin-field"><label>Service tag</label>
              <select value={serviceId} onChange={(e) => setServiceId(e.target.value)}>
                <option value="">—</option>
                {services.map((s) => <option key={s.id} value={s.id}>{s.tcName}</option>)}
              </select>
            </div>
            <div className="admin-field"><label>Gallery layout</label>
              <select value={galleryLayout} onChange={(e) => setGalleryLayout(e.target.value)}>
                <option value="NONE">None</option><option value="CAROUSEL">Carousel</option>
                <option value="GRID">Grid</option><option value="MASONRY">Masonry</option>
              </select>
            </div>
          </div>
          <div className="admin-row">
            <div className="admin-field"><label>Starts (Hong Kong time)</label><input type="datetime-local" value={startsAt} onChange={(e) => setStartsAt(e.target.value)} /></div>
            <div className="admin-field"><label>Ends (Hong Kong time)</label><input type="datetime-local" value={endsAt} onChange={(e) => setEndsAt(e.target.value)} /></div>
            <div className="admin-field"><label>Venue map URL</label><input value={venueMapUrl} onChange={(e) => setVenueMapUrl(e.target.value)} placeholder="https://maps.google.com/…" /></div>
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
        </div>

        <div className="admin-card">
          <h2 style={{ fontFamily: "var(--font-display)", fontSize: 16, marginTop: 0 }}>Registration</h2>
          <div className="admin-field">
            <label className="checkbox" style={{ display: "flex", alignItems: "center", gap: 8, fontWeight: 400 }}>
              <input type="checkbox" style={{ width: "auto" }} checked={registerable} onChange={(e) => setRegisterable(e.target.checked)} />
              This event accepts registrations
            </label>
          </div>
          {registerable && (
            <div className="admin-row">
              <div className="admin-field"><label>Registration opens</label><input type="datetime-local" value={registrationOpensAt} onChange={(e) => setRegistrationOpensAt(e.target.value)} /></div>
              <div className="admin-field"><label>Registration closes</label><input type="datetime-local" value={registrationClosesAt} onChange={(e) => setRegistrationClosesAt(e.target.value)} /></div>
              <div className="admin-field"><label>Capacity</label><input type="number" min="0" value={capacity} onChange={(e) => setCapacity(e.target.value)} placeholder="Blank = unlimited" /></div>
            </div>
          )}
        </div>

        <div className="admin-card">
          <div className="lang-tabs">
            {LANGS.map((l) => <button key={l.code} type="button" className={`lang-tab ${activeLang === l.code ? "active" : ""}`} onClick={() => setActiveLang(l.code)}>{l.label}{l.required ? <span className="req"> *</span> : null}</button>)}
          </div>
          <div className="admin-field"><label>Title{activeLang === "zh-Hant" ? " (required)" : ""}</label><input value={a.title} onChange={(e) => setField(activeLang, "title", e.target.value)} /></div>
          <div className="admin-field"><label>Venue</label><input value={a.venue} onChange={(e) => setField(activeLang, "venue", e.target.value)} /></div>
          <div className="admin-field"><label>Summary</label><input value={a.summary} onChange={(e) => setField(activeLang, "summary", e.target.value)} /></div>
          <div className="admin-field"><label>Body (Markdown)</label><textarea value={a.body} onChange={(e) => setField(activeLang, "body", e.target.value)} /></div>
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
