import { useCallback, useEffect, useRef } from "react";

// Click-to-enlarge lightbox: keyboard (Esc, arrows) + swipe-to-close on touch.
// Only ever rendered client-side (Gallery opens it on click, after hydration).
export default function Lightbox({ media, index, onClose, onNavigate }) {
  const item = media[index];
  const touchStartX = useRef(0);

  const handleKeyDown = useCallback(
    (e) => {
      if (e.key === "Escape") onClose();
      if (e.key === "ArrowRight") onNavigate((index + 1) % media.length);
      if (e.key === "ArrowLeft") onNavigate((index - 1 + media.length) % media.length);
    },
    [index, media, onClose, onNavigate]
  );

  useEffect(() => {
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [handleKeyDown]);

  if (!item) return null;

  function onTouchStart(e) {
    touchStartX.current = e.touches[0].clientX;
  }
  function onTouchEnd(e) {
    const dx = e.changedTouches[0].clientX - touchStartX.current;
    if (Math.abs(dx) > 60) {
      onClose();
    }
  }

  return (
    <div
      role="dialog"
      aria-modal="true"
      onClick={onClose}
      onTouchStart={onTouchStart}
      onTouchEnd={onTouchEnd}
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(33,28,21,0.92)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 1000,
      }}
    >
      <img
        src={item.url}
        alt={item.caption || ""}
        onClick={(e) => e.stopPropagation()}
        style={{ maxWidth: "90vw", maxHeight: "85vh", borderRadius: 0 }}
      />
      {item.caption && (
        <p style={{ position: "absolute", bottom: "24px", color: "var(--color-paper)", fontSize: "13px" }}>
          {item.caption}
        </p>
      )}
    </div>
  );
}
