import { useState } from "react";
import Lightbox from "./Lightbox";

// Responsive 3-up gallery from a GalleryImageDto[] ({url, width, height} — no id or
// caption in the v2 contract). Interactivity (lightbox) activates after hydration;
// the image grid itself is server-rendered.
export default function Gallery({ media }) {
  const [openIndex, setOpenIndex] = useState(null);
  if (!media || media.length === 0) return null;

  return (
    <>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))",
          gap: "8px",
          margin: "24px 0",
        }}
      >
        {media.map((item, i) => (
          <button
            key={item.url}
            onClick={() => setOpenIndex(i)}
            style={{ padding: 0, border: "none", background: "none", cursor: "pointer" }}
            aria-label="Enlarge photo"
          >
            <img src={item.url} alt="" loading="lazy" style={{ width: "100%", height: "160px", objectFit: "cover" }} />
          </button>
        ))}
      </div>
      {openIndex !== null && (
        <Lightbox media={media} index={openIndex} onClose={() => setOpenIndex(null)} onNavigate={setOpenIndex} />
      )}
    </>
  );
}
