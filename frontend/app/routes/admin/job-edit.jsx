import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { adminApi } from "../../lib/adminApi";

const LANGS = [
  { code: "zh-Hant", label: "繁中", required: true },
  { code: "en", label: "EN" },
  { code: "zh-Hans", label: "简中" },
];
const empty = () => { const o = {}; for (const l of LANGS) o[l.code] = { title: "", body: "", location: "" }; return o; };
const toDateInput = (iso) => iso ? iso.slice(0, 10) : "";
const toIso = (dateStr) => dateStr ? new Date(`${dateStr}T00:00:00+08:00`).toISOString() : null;

export default function JobEdit() {
  const { id } = useParams();
  const isNew = id === "new";
  const navigate = useNavigate();

  const [slug, setSlug] = useState("");
  const [employmentType, setEmploymentType] = useState("");
  const [department, setDepartment] = useState("");
  const [postedAt, setPostedAt] = useState("");
  const [closesAt, setClosesAt] = useState("");
  const [applyEmail, setApplyEmail] = useState("");
  const [applyUrl, setApplyUrl] = useState("");
  const [status, setStatus] = useState("DRAFT");
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
        const j = await adminApi.getJob(id);
        if (!alive) return;
        setSlug(j.slug || ""); setEmploymentType(j.employmentType || ""); setDepartment(j.department || "");
        setPostedAt(toDateInput(j.postedAt)); setClosesAt(toDateInput(j.closesAt));
        setApplyEmail(j.applyEmail || ""); setApplyUrl(j.applyUrl || "");
        setStatus(j.status || "DRAFT");
        setTr({
          "zh-Hant": { title: j.tcTitle || "", body: j.tcBody || "", location: j.tcLocation || "" },
          en: { title: j.enTitle || "", body: j.enBody || "", location: j.enLocation || "" },
          "zh-Hans": { title: j.scTitle || "", body: j.scBody || "", location: j.scLocation || "" },
        });
      } catch (e) { if (alive) setError(e.message); } finally { if (alive) setLoading(false); }
    })();
    return () => { alive = false; };
  }, [id, isNew]);

  const setField = (lang, f, v) => setTr((p) => ({ ...p, [lang]: { ...p[lang], [f]: v } }));

  function buildPayload(withStatus) {
    return {
      slug: slug.trim(),
      employmentType: employmentType.trim() || null,
      department: department.trim() || null,
      postedAt: toIso(postedAt), closesAt: toIso(closesAt),
      applyEmail: applyEmail.trim() || null,
      applyUrl: applyUrl.trim() || null,
      status: withStatus,
      publishAt: null, unpublishAt: null,
      tcTitle: tr["zh-Hant"].title, enTitle: tr.en.title, scTitle: tr["zh-Hans"].title,
      tcBody: tr["zh-Hant"].body, enBody: tr.en.body, scBody: tr["zh-Hans"].body,
      tcLocation: tr["zh-Hant"].location, enLocation: tr.en.location, scLocation: tr["zh-Hans"].location,
    };
  }

  async function save(withStatus) {
    setError(""); setOk("");
    if (!slug.trim()) { setError("Slug is required"); return; }
    if (!tr["zh-Hant"].title.trim()) { setError("繁中 title is required"); return; }
    setBusy(true);
    try {
      const payload = buildPayload(withStatus);
      const saved = isNew ? await adminApi.createJob(payload) : await adminApi.updateJob(id, payload);
      setStatus(withStatus);
      if (isNew) navigate(`/admin/jobs/${saved.id}`, { replace: true });
      else setOk(withStatus === "PUBLISHED" ? "Published." : "Saved.");
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }

  if (loading) return (<><div className="admin-topbar"><h1>Edit job</h1></div><div className="admin-content"><div className="admin-empty">Loading…</div></div></>);
  const a = tr[activeLang];

  return (
    <>
      <div className="admin-topbar">
        <h1>{isNew ? "New job" : "Edit job"}{!isNew ? <span className={`admin-badge ${status === "PUBLISHED" ? "published" : "draft"}`} style={{ marginLeft: 10, verticalAlign: "middle" }}>{status}</span> : null}</h1>
        <button className="admin-btn ghost" onClick={() => navigate("/admin/jobs")}>← Back</button>
      </div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}
        <div className="admin-card">
          <div className="admin-row">
            <div className="admin-field"><label>Slug</label><input value={slug} onChange={(e) => setSlug(e.target.value)} /></div>
            <div className="admin-field"><label>Employment type</label><input value={employmentType} onChange={(e) => setEmploymentType(e.target.value)} placeholder="Full-time" /></div>
            <div className="admin-field"><label>Department</label><input value={department} onChange={(e) => setDepartment(e.target.value)} /></div>
          </div>
          <div className="admin-row">
            <div className="admin-field"><label>Posted</label><input type="date" value={postedAt} onChange={(e) => setPostedAt(e.target.value)} /></div>
            <div className="admin-field"><label>Closes</label><input type="date" value={closesAt} onChange={(e) => setClosesAt(e.target.value)} /></div>
          </div>
          <div className="admin-row">
            <div className="admin-field"><label>Apply email</label><input type="email" value={applyEmail} onChange={(e) => setApplyEmail(e.target.value)} /></div>
            <div className="admin-field"><label>Apply URL</label><input value={applyUrl} onChange={(e) => setApplyUrl(e.target.value)} /></div>
          </div>
        </div>
        <div className="admin-card">
          <div className="lang-tabs">
            {LANGS.map((l) => <button key={l.code} type="button" className={`lang-tab ${activeLang === l.code ? "active" : ""}`} onClick={() => setActiveLang(l.code)}>{l.label}{l.required ? <span className="req"> *</span> : null}</button>)}
          </div>
          <div className="admin-field"><label>Title{activeLang === "zh-Hant" ? " (required)" : ""}</label><input value={a.title} onChange={(e) => setField(activeLang, "title", e.target.value)} /></div>
          <div className="admin-field"><label>Location</label><input value={a.location} onChange={(e) => setField(activeLang, "location", e.target.value)} /></div>
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
