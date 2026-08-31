import { useCallback, useEffect, useRef } from "react";
import { useParams } from "react-router";

import { t } from "../i18n";

// Lightbox (mockup 8f): dark ground, the image contained rather than cropped —
// this is the one place a photo is shown as it was actually framed — plus a
// thumbnail rail marking position. No glass over the photo.
//
// Only ever rendered client-side, after Gallery opens it on click.
export default function Lightbox({ media, index, title, onClose, onNavigate }) {
  const { lang } = useParams();
  const item = media[index];
  const touchStartX = useRef(0);
  const closeRef = useRef(null);

  const go = useCallback(
    (next) => onNavigate((next + media.length) % media.length),
    [media.length, onNavigate],
  );

  const handleKeyDown = useCallback(
    (e) => {
      if (e.key === "Escape") onClose();
      if (e.key === "ArrowRight") go(index + 1);
      if (e.key === "ArrowLeft") go(index - 1);
    },
    [go, index, onClose],
  );

  useEffect(() => {
    document.addEventListener("keydown", handleKeyDown);
    document.body.style.overflow = "hidden";
    closeRef.current?.focus();
    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      document.body.style.overflow = "";
    };
  }, [handleKeyDown]);

  if (!item) return null;

  const onTouchStart = (e) => { touchStartX.current = e.touches[0].clientX; };
  // A horizontal swipe moves between photos rather than closing — closing on any
  // swipe made it impossible to browse a set on a phone.
  const onTouchEnd = (e) => {
    const dx = e.changedTouches[0].clientX - touchStartX.current;
    if (Math.abs(dx) > 60) go(index + (dx < 0 ? 1 : -1));
  };

  return (
    <div
      className="lightbox"
      role="dialog"
      aria-modal="true"
      aria-label={title}
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
      onTouchStart={onTouchStart}
      onTouchEnd={onTouchEnd}
    >
      <div className="lightbox__head">
        <div>
          <div className="lightbox__title">{title}</div>
          <div className="lightbox__position">
            {String(index + 1).padStart(2, "0")} / {media.length}
          </div>
        </div>
        <button
          type="button"
          className="lightbox__close"
          onClick={onClose}
          aria-label={t(lang, "gallery.close")}
          ref={closeRef}
        >
          ✕
        </button>
      </div>

      <div className="lightbox__stage">
        {media.length > 1 && (
          <button type="button" className="lightbox__arrow" onClick={() => go(index - 1)} aria-label={t(lang, "gallery.prev")}>←</button>
        )}

        <img className="lightbox__image" src={item.url} alt="" />

        {media.length > 1 && (
          <button type="button" className="lightbox__arrow lightbox__arrow--next" onClick={() => go(index + 1)} aria-label={t(lang, "gallery.next")}>→</button>
        )}
      </div>

      {media.length > 1 && (
        <div className="lightbox__thumbs">
          {media.map((photo, i) => (
            <button
              key={photo.url ?? i}
              type="button"
              className="lightbox__thumb"
              aria-current={i === index ? "true" : undefined}
              aria-label={t(lang, "gallery.position", { current: i + 1, total: media.length })}
              onClick={() => onNavigate(i)}
            >
              <img src={photo.url} alt="" loading="lazy" />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
