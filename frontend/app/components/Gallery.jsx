import { useRef, useState } from "react";
import { useParams } from "react-router";

import { t } from "../i18n";
import Lightbox from "./Lightbox";

// Gallery block for blog posts and events (mockup 8e).
//
// The backend already stores a GalleryLayout per record and the admin editor
// lets staff pick one — this component is what makes that choice mean something:
//
//   CAROUSEL — one photo leads and the next peeks in, for a set with an obvious
//              hero shot. Scroll-snap, so it works before hydration.
//   GRID     — square crops in even rows, for photos shot at one event in one
//              consistent ratio.
//   MASONRY  — CSS columns keeping native long edges, for a mixed batch of
//              portrait and landscape phone shots.
//   NONE     — the editor has chosen not to show a gallery.
//
// Below FEW images none of them show scroll hints, counters or a "view all"
// tile: three photos are just three photos.
//
// The v2 GalleryImageDto is {url, width, height} — no id, no caption — so keys
// come from the url and the lightbox renders no caption line.
const FEW = 4;
const GRID_VISIBLE = 11;
const MASONRY_VISIBLE = 8;

export default function Gallery({ media, layout = "GRID", title, headingId = "gallery" }) {
  const { lang } = useParams();
  const [openIndex, setOpenIndex] = useState(null);
  const [position, setPosition] = useState(0);
  const trackRef = useRef(null);

  if (!media || media.length === 0) return null;
  if (layout === "NONE") return null;

  const normalised = ["CAROUSEL", "GRID", "MASONRY"].includes(layout) ? layout : "GRID";
  const few = media.length < FEW;

  const scrollBy = (direction) => {
    const track = trackRef.current;
    if (!track) return;
    track.scrollBy({ left: direction * track.clientWidth * 0.6, behavior: "smooth" });
  };

  const onScroll = () => {
    const track = trackRef.current;
    if (!track) return;
    const max = track.scrollWidth - track.clientWidth;
    const ratio = max > 0 ? track.scrollLeft / max : 0;
    setPosition(Math.min(Math.round(ratio * (media.length - 1)), media.length - 1));
  };

  const heading = title || t(lang, "gallery.title");

  const tile = (photo, index, ratioClass) => (
    <button
      key={photo.url ?? index}
      type="button"
      className="gallery__item"
      onClick={() => setOpenIndex(index)}
      aria-label={t(lang, "gallery.open")}
    >
      <span className={`photo ${ratioClass}`}>
        <img src={photo.url} alt="" loading="lazy" decoding="async" />
      </span>
    </button>
  );

  return (
    <section className="gallery" aria-labelledby={headingId}>
      <div className="gallery__head">
        <div>
          <span className="kicker">{t(lang, "gallery.label")}</span>
          <h2 id={headingId}>{heading}</h2>
          <span className="gallery__count">{t(lang, "gallery.count", { count: media.length })}</span>
        </div>

        {/* Controls only earn their place once the set actually overflows. */}
        {normalised === "CAROUSEL" && !few && (
          <div className="gallery__nav">
            <span className="gallery__count">
              {String(position + 1).padStart(2, "0")} / {media.length}
            </span>
            <button type="button" className="gallery__btn" onClick={() => scrollBy(-1)} aria-label={t(lang, "gallery.prev")}>←</button>
            <button type="button" className="gallery__btn gallery__btn--primary" onClick={() => scrollBy(1)} aria-label={t(lang, "gallery.next")}>→</button>
          </div>
        )}
      </div>

      {normalised === "CAROUSEL" && (
        <>
          <div
            className={`gallery__track${few ? " gallery__track--few" : ""}`}
            ref={trackRef}
            onScroll={onScroll}
          >
            {media.map((photo, index) => tile(photo, index, "photo--card"))}
          </div>
          {!few && (
            <div className="gallery__progress" aria-hidden="true">
              <span style={{ width: `${((position + 1) / media.length) * 100}%` }} />
            </div>
          )}
        </>
      )}

      {normalised === "GRID" && (
        <div className={`gallery__grid${few ? " gallery__grid--few" : ""}`}>
          {media.slice(0, few ? media.length : GRID_VISIBLE).map((photo, index) =>
            tile(photo, index, "photo--square"))}
          {!few && media.length > GRID_VISIBLE && (
            <button type="button" className="gallery__more" onClick={() => setOpenIndex(GRID_VISIBLE)}>
              {t(lang, "gallery.viewAll")}
              <br />
              {t(lang, "gallery.count", { count: media.length })} →
            </button>
          )}
        </div>
      )}

      {normalised === "MASONRY" && (
        <div className={`gallery__masonry${few ? " gallery__masonry--few" : ""}`}>
          {media.slice(0, few ? media.length : MASONRY_VISIBLE).map((photo, index) =>
            tile(photo, index, "photo--native"))}
          {!few && media.length > MASONRY_VISIBLE && (
            <button
              type="button"
              className="gallery__masonry-more"
              onClick={() => setOpenIndex(MASONRY_VISIBLE)}
            >
              {t(lang, "gallery.more", { count: media.length - MASONRY_VISIBLE })}
              <br />
              <span className="btn-text">{t(lang, "gallery.viewAll")} →</span>
            </button>
          )}
        </div>
      )}

      {openIndex !== null && (
        <Lightbox
          media={media}
          index={openIndex}
          title={heading}
          onClose={() => setOpenIndex(null)}
          onNavigate={setOpenIndex}
        />
      )}
    </section>
  );
}
