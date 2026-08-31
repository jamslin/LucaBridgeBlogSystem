import { useEffect, useRef, useState } from "react";
import { useOutletContext } from "react-router";
import { adminApi } from "../../lib/adminApi";

const LANGS = [
  { code: "zh-Hant", label: "繁中", required: true },
  { code: "en", label: "EN" },
  { code: "zh-Hans", label: "简中" },
];
const empty = () => { const o = {}; for (const l of LANGS) o[l.code] = { name: "", tagline: "", about: "", address: "", officeHours: "" }; return o; };

export default function Company() {
  const { isAdmin } = useOutletContext();
  const [charityRegNo, setCharityRegNo] = useState("");
  const [foundedYear, setFoundedYear] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [logoMediaId, setLogoMediaId] = useState(null);
  const [logoUrl, setLogoUrl] = useState("");
  const [instagramUrl, setInstagramUrl] = useState("");
  const [facebookUrl, setFacebookUrl] = useState("");
  const [youtubeUrl, setYoutubeUrl] = useState("");
  const [tr, setTr] = useState(empty());
  const [activeLang, setActiveLang] = useState("zh-Hant");
  const [error, setError] = useState(""); const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false); const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(true);
  const logoRef = useRef(null);

  useEffect(() => {
    if (!isAdmin) { setLoading(false); return; }
    let alive = true;
    (async () => {
      try {
        const c = await adminApi.getCompany();
        if (!alive) return;
        setCharityRegNo(c.charityRegNo || ""); setFoundedYear(c.foundedYear != null ? String(c.foundedYear) : "");
        setPhone(c.phone || ""); setEmail(c.email || ""); setLogoMediaId(c.logoMediaId || null);
        setInstagramUrl(c.instagramUrl || ""); setFacebookUrl(c.facebookUrl || ""); setYoutubeUrl(c.youtubeUrl || "");
        setTr({
          "zh-Hant": { name: c.tcName || "", tagline: c.tcTagline || "", about: c.tcAbout || "", address: c.tcAddress || "", officeHours: c.tcOfficeHours || "" },
          en: { name: c.enName || "", tagline: c.enTagline || "", about: c.enAbout || "", address: c.enAddress || "", officeHours: c.enOfficeHours || "" },
          "zh-Hans": { name: c.scName || "", tagline: c.scTagline || "", about: c.scAbout || "", address: c.scAddress || "", officeHours: c.scOfficeHours || "" },
        });
      } catch (e) { if (alive) setError(e.message); } finally { if (alive) setLoading(false); }
    })();
    return () => { alive = false; };
  }, [isAdmin]);

  const setField = (lang, f, v) => setTr((p) => ({ ...p, [lang]: { ...p[lang], [f]: v } }));

  async function onLogo(e) {
    const f = e.target.files && e.target.files[0]; if (!f) return;
    setUploading(true); setError("");
    try { const m = await adminApi.uploadMedia(f); setLogoMediaId(m.id); setLogoUrl(m.url); }
    catch (er) { setError(`Upload failed: ${er.message}`); }
    finally { setUploading(false); if (logoRef.current) logoRef.current.value = ""; }
  }

  async function save() {
    setError(""); setOk("");
    if (!tr["zh-Hant"].name.trim()) { setError("繁中 name is required"); return; }
    setBusy(true);
    const payload = {
      charityRegNo: charityRegNo.trim() || null,
      foundedYear: foundedYear ? Number(foundedYear) : null,
      phone: phone.trim() || null, email: email.trim() || null, logoMediaId,
      instagramUrl: instagramUrl.trim() || null, facebookUrl: facebookUrl.trim() || null, youtubeUrl: youtubeUrl.trim() || null,
      tcName: tr["zh-Hant"].name, enName: tr.en.name, scName: tr["zh-Hans"].name,
      tcTagline: tr["zh-Hant"].tagline, enTagline: tr.en.tagline, scTagline: tr["zh-Hans"].tagline,
      tcAbout: tr["zh-Hant"].about, enAbout: tr.en.about, scAbout: tr["zh-Hans"].about,
      tcAddress: tr["zh-Hant"].address, enAddress: tr.en.address, scAddress: tr["zh-Hans"].address,
      tcOfficeHours: tr["zh-Hant"].officeHours, enOfficeHours: tr.en.officeHours, scOfficeHours: tr["zh-Hans"].officeHours,
    };
    try { await adminApi.saveCompany(payload); setOk("Saved."); }
    catch (e) { setError(e.message); } finally { setBusy(false); }
  }

  if (!isAdmin) {
    return (<><div className="admin-topbar"><h1>Company</h1></div><div className="admin-content"><div className="admin-alert error">Administrators only.</div></div></>);
  }
  if (loading) return (<><div className="admin-topbar"><h1>Company</h1></div><div className="admin-content"><div className="admin-empty">Loading…</div></div></>);
  const a = tr[activeLang];

  return (
    <>
      <div className="admin-topbar"><h1>Company</h1></div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}
        <p style={{ marginTop: 0, color: "var(--color-muted)" }}>
          Single record — shown on every page (masthead, footer, contact page).
        </p>
        <div className="admin-card">
          <div className="admin-row">
            <div className="admin-field"><label>Charity registration no.</label><input value={charityRegNo} onChange={(e) => setCharityRegNo(e.target.value)} /></div>
            <div className="admin-field"><label>Founded year</label><input type="number" value={foundedYear} onChange={(e) => setFoundedYear(e.target.value)} /></div>
          </div>
          <div className="admin-row">
            <div className="admin-field"><label>Phone</label><input value={phone} onChange={(e) => setPhone(e.target.value)} /></div>
            <div className="admin-field"><label>Email</label><input type="email" value={email} onChange={(e) => setEmail(e.target.value)} /></div>
          </div>
          <div className="admin-row">
            <div className="admin-field"><label>Instagram URL</label><input value={instagramUrl} onChange={(e) => setInstagramUrl(e.target.value)} /></div>
            <div className="admin-field"><label>Facebook URL</label><input value={facebookUrl} onChange={(e) => setFacebookUrl(e.target.value)} /></div>
            <div className="admin-field"><label>YouTube URL</label><input value={youtubeUrl} onChange={(e) => setYoutubeUrl(e.target.value)} /></div>
          </div>
          <div className="admin-field">
            <label>Logo</label>
            <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
              <button type="button" className="admin-btn" disabled={uploading} onClick={() => logoRef.current && logoRef.current.click()}>{uploading ? "Uploading…" : "Upload"}</button>
              <input ref={logoRef} type="file" accept="image/*" style={{ display: "none" }} onChange={onLogo} />
              {logoMediaId ? <span className="meta">media #{logoMediaId}</span> : null}
            </div>
            {logoUrl ? <img src={logoUrl} alt="" style={{ marginTop: 10, maxHeight: 60 }} /> : null}
          </div>
        </div>
        <div className="admin-card">
          <div className="lang-tabs">
            {LANGS.map((l) => <button key={l.code} type="button" className={`lang-tab ${activeLang === l.code ? "active" : ""}`} onClick={() => setActiveLang(l.code)}>{l.label}{l.required ? <span className="req"> *</span> : null}</button>)}
          </div>
          <div className="admin-field"><label>Name{activeLang === "zh-Hant" ? " (required)" : ""}</label><input value={a.name} onChange={(e) => setField(activeLang, "name", e.target.value)} /></div>
          <div className="admin-field"><label>Tagline</label><input value={a.tagline} onChange={(e) => setField(activeLang, "tagline", e.target.value)} /></div>
          <div className="admin-field"><label>About</label><textarea value={a.about} onChange={(e) => setField(activeLang, "about", e.target.value)} /></div>
          <div className="admin-field"><label>Address</label><input value={a.address} onChange={(e) => setField(activeLang, "address", e.target.value)} /></div>
          <div className="admin-field"><label>Office hours</label><input value={a.officeHours} onChange={(e) => setField(activeLang, "officeHours", e.target.value)} /></div>
        </div>
        <div className="admin-actions">
          <button className="admin-btn primary" disabled={busy} onClick={save}>{busy ? "Saving…" : "Save"}</button>
        </div>
      </div>
    </>
  );
}
