import { useEffect, useState } from "react";
import { adminApi } from "../../lib/adminApi";

const blank = { id: null, imageUrl: "", linkUrl: "", sortOrder: 0, active: true, startsAt: "", endsAt: "", titleZhHant: "", subtitleZhHant: "", buttonLabelZhHant: "", titleEn: "", subtitleEn: "", buttonLabelEn: "", titleZhHans: "", subtitleZhHans: "", buttonLabelZhHans: "" };
const toInput = (v) => v ? new Date(v).toLocaleString("sv-SE", { timeZone: "Asia/Hong_Kong" }).replace(" ", "T").slice(0, 16) : "";
const toIso = (v) => v ? new Date(`${v}:00+08:00`).toISOString() : null;

export default function Banners() {
  const [rows, setRows] = useState(null); const [form, setForm] = useState(blank);
  const [error, setError] = useState(""); const [ok, setOk] = useState(""); const [busy, setBusy] = useState(false);
  async function load() { try { setRows(await adminApi.listBanners()); } catch (e) { setError(e.message); setRows([]); } }
  useEffect(() => { load(); }, []);
  function edit(r) { setForm({ ...r, startsAt: toInput(r.startsAt), endsAt: toInput(r.endsAt) }); setError(""); setOk(""); }
  function field(name, value) { setForm((p) => ({ ...p, [name]: value })); }
  async function upload(e) { const file = e.target.files?.[0]; if (!file) return; setBusy(true); try { const m = await adminApi.uploadMedia(file); field("imageUrl", m.url); } catch (x) { setError(x.message); } finally { setBusy(false); } }
  async function save() {
    setError(""); setOk("");
    if (!form.imageUrl.trim() || !form.titleZhHant.trim()) { setError("Image and Traditional Chinese title are required"); return; }
    if (form.startsAt && form.endsAt && form.endsAt < form.startsAt) { setError("End must not be before start"); return; }
    setBusy(true);
    try { await adminApi.saveBanner({ ...form, imageUrl: form.imageUrl.trim(), linkUrl: form.linkUrl.trim() || null, startsAt: toIso(form.startsAt), endsAt: toIso(form.endsAt), sortOrder: Number(form.sortOrder) }); setForm(blank); setOk("Banner saved."); await load(); }
    catch (e) { setError(e.message); } finally { setBusy(false); }
  }
  async function remove(r) { if (!window.confirm(`Delete "${r.titleZhHant}"?`)) return; try { await adminApi.deleteBanner(r.id); if (form.id === r.id) setForm(blank); await load(); } catch (e) { setError(e.message); } }
  return <><div className="admin-topbar"><h1>Homepage banners</h1><button className="admin-btn primary" onClick={() => setForm(blank)}>New banner</button></div><div className="admin-content">
    {error && <div className="admin-alert error">{error}</div>}{ok && <div className="admin-alert success">{ok}</div>}
    <div className="admin-card"><div className="admin-field"><label>Banner image *</label><div style={{display:"flex",gap:8}}><input value={form.imageUrl} onChange={(e)=>field("imageUrl",e.target.value)} placeholder="Image URL"/><input type="file" accept="image/*" onChange={upload}/></div></div>
      <div className="admin-row"><div className="admin-field"><label>Link URL</label><input value={form.linkUrl || ""} onChange={(e)=>field("linkUrl",e.target.value)}/></div><div className="admin-field"><label>Order</label><input type="number" value={form.sortOrder} onChange={(e)=>field("sortOrder",e.target.value)}/></div><div className="admin-field"><label>Visible</label><input type="checkbox" checked={form.active} onChange={(e)=>field("active",e.target.checked)}/></div></div>
      <div className="admin-row"><div className="admin-field"><label>Starts (Hong Kong time)</label><input type="datetime-local" value={form.startsAt} onChange={(e)=>field("startsAt",e.target.value)}/></div><div className="admin-field"><label>Ends (Hong Kong time)</label><input type="datetime-local" value={form.endsAt} onChange={(e)=>field("endsAt",e.target.value)}/></div></div>
      {[["ZhHant","繁中 *"],["En","English"],["ZhHans","简中"]].map(([suffix,label])=><fieldset key={suffix} style={{border:"1px solid var(--color-line)",margin:"16px 0",padding:16}}><legend>{label}</legend><div className="admin-field"><label>Title</label><input value={form[`title${suffix}`] || ""} onChange={(e)=>field(`title${suffix}`,e.target.value)}/></div><div className="admin-field"><label>Subtitle</label><input value={form[`subtitle${suffix}`] || ""} onChange={(e)=>field(`subtitle${suffix}`,e.target.value)}/></div><div className="admin-field"><label>Button label</label><input value={form[`buttonLabel${suffix}`] || ""} onChange={(e)=>field(`buttonLabel${suffix}`,e.target.value)}/></div></fieldset>)}
      <div className="admin-actions"><button className="admin-btn primary" disabled={busy} onClick={save}>{busy ? "Saving..." : "Save banner"}</button></div></div>
    <div className="admin-card" style={{padding:0}}>{rows===null?<div className="admin-empty">Loading...</div>:rows.length===0?<div className="admin-empty">No banners yet.</div>:<table className="admin-table"><thead><tr><th>Title</th><th>Order</th><th>Visible</th><th></th></tr></thead><tbody>{rows.map(r=><tr key={r.id}><td>{r.titleZhHant}</td><td>{r.sortOrder}</td><td>{r.active?"Yes":"No"}</td><td><div className="row-actions"><button className="admin-btn admin-btn-sm" onClick={()=>edit(r)}>Edit</button><button className="admin-btn admin-btn-sm danger" onClick={()=>remove(r)}>Delete</button></div></td></tr>)}</tbody></table>}</div>
  </div></>;
}
