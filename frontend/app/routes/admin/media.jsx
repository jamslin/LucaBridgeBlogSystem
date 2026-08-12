import { useEffect, useRef, useState } from "react";
import { adminApi } from "../../lib/adminApi";

function MediaCard({ m, onSaveAlt, onCopy, onDelete }) {
  const [alt, setAlt] = useState(m.altText || "");
  const dirty = alt !== (m.altText || "");
  return (
    <div className="admin-card" style={{ padding: 14, marginBottom: 0 }}>
      <img src={m.url} alt={m.altText || ""} loading="lazy"
           style={{ width: "100%", height: 140, objectFit: "cover", borderRadius: "var(--radius-photo)", border: "1px solid var(--color-line)" }} />
      <div style={{ fontSize: 12, color: "var(--color-muted)", marginTop: 8, wordBreak: "break-all" }}>{m.filename || "(unnamed)"}</div>
      <div style={{ margin: "8px 0" }}>
        {m.inUse
          ? <span className="admin-badge published">In use ({m.usages.length})</span>
          : <span className="admin-badge draft">Unused</span>}
      </div>
      {m.inUse ? (
        <div style={{ fontSize: 11, color: "var(--color-muted)", marginBottom: 8 }}>
          {m.usages.map((u, i) => <div key={i}>{u.title} · {u.field}</div>)}
        </div>
      ) : null}
      <label style={{ fontSize: 11, fontWeight: 600, color: "var(--color-ink-soft)" }}>Alt text</label>
      <input value={alt} onChange={(e) => setAlt(e.target.value)} placeholder="Describe the image" style={{ marginTop: 4, marginBottom: 8 }} />
      <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
        <button className="admin-btn admin-btn-sm" onClick={() => onCopy(m.url)}>Copy URL</button>
        <button className="admin-btn admin-btn-sm" disabled={!dirty} onClick={() => onSaveAlt(m, alt)}>Save alt</button>
        <button className="admin-btn admin-btn-sm danger" onClick={() => onDelete(m)}>Delete</button>
      </div>
    </div>
  );
}

export default function Media() {
  const [items, setItems] = useState(null);
  const [error, setError] = useState("");
  const [ok, setOk] = useState("");
  const [busy, setBusy] = useState(false);
  const fileRef = useRef(null);

  async function load() {
    try { setItems(await adminApi.listMedia()); }
    catch (e) { setError(e.message); setItems([]); }
  }
  useEffect(() => { load(); }, []);

  async function onFiles(e) {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;
    setBusy(true); setError(""); setOk("");
    let n = 0;
    for (const f of files) {
      try { await adminApi.uploadMedia(f); n++; }
      catch (err) { setError(`${f.name}: ${err.message}`); }
    }
    setBusy(false);
    if (fileRef.current) fileRef.current.value = "";
    if (n) setOk(`Uploaded ${n} image${n > 1 ? "s" : ""}.`);
    load();
  }

  async function sync() {
    setBusy(true); setError(""); setOk("");
    try {
      const r = await adminApi.syncMedia();
      setOk(`Synced — ${r.added} new image${r.added === 1 ? "" : "s"} catalogued from storage.`);
      load();
    } catch (e) { setError(e.message); }
    finally { setBusy(false); }
  }

  function copy(url) {
    if (typeof navigator !== "undefined" && navigator.clipboard) {
      navigator.clipboard.writeText(url);
      setError(""); setOk("URL copied to clipboard.");
    }
  }

  async function saveAlt(m, alt) {
    setError(""); setOk("");
    try { await adminApi.updateMedia(m.id, alt); setOk("Alt text saved."); load(); }
    catch (e) { setError(e.message); }
  }

  async function del(m) {
    if (!window.confirm(`Delete ${m.filename || "this image"}? This removes the file from storage.`)) return;
    setError(""); setOk("");
    try { await adminApi.deleteMedia(m.id); setOk("Deleted."); load(); }
    catch (e) { setError(e.message); } // backend blocks in-use images with a helpful message
  }

  return (
    <>
      <div className="admin-topbar"><h1>Media library</h1></div>
      <div className="admin-content">
        {error ? <div className="admin-alert error">{error}</div> : null}
        {ok ? <div className="admin-alert success">{ok}</div> : null}

        <div className="admin-card">
          <div className="admin-field" style={{ marginBottom: 0 }}>
            <label>Upload images</label>
            <input ref={fileRef} type="file" accept="image/*" multiple onChange={onFiles} disabled={busy} />
            <div className="hint">JPG / PNG / WebP / GIF / AVIF, up to 10 MB each. Every upload is catalogued here — including images added from the post editor.</div>
          </div>
          <div className="admin-actions" style={{ marginTop: 12 }}>
            <button className="admin-btn" disabled={busy} onClick={sync}>Sync from storage</button>
            <span className="hint" style={{ margin: 0 }}>Catalogues images already in the bucket (e.g. seeded covers).</span>
          </div>
          {busy ? <p style={{ color: "var(--color-muted)", marginBottom: 0 }}>Uploading…</p> : null}
        </div>

        {items === null ? (
          <div className="admin-empty">Loading…</div>
        ) : items.length === 0 ? (
          <div className="admin-empty">No images yet. Upload one above, or add a cover/inline image in the post editor.</div>
        ) : (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))", gap: 16 }}>
            {items.map((m) => (
              <MediaCard key={m.id} m={m} onSaveAlt={saveAlt} onCopy={copy} onDelete={del} />
            ))}
          </div>
        )}
      </div>
    </>
  );
}
