import { useEffect, useState } from "react";
import { useParams } from "react-router";

// Text-size control (a11y — WCAG 2.1 SC 1.4.4). Three steps written to
// <html data-fontsize> which scales the rem unit (see global.css); the choice
// persists in localStorage. No dependency. A pre-paint script in root.jsx
// applies the stored value before first paint, so this only mirrors state.
const STEPS = ["sm", "md", "lg"];
const GLYPH = { sm: "A-", md: "A", lg: "A+" };
const ARIA = {
  "zh-Hant": { group: "文字大小", sm: "細字型", md: "標準字型", lg: "大字型" },
  "zh-Hans": { group: "文字大小", sm: "小字体", md: "标准字体", lg: "大字体" },
  en: { group: "Text size", sm: "Small text", md: "Default text", lg: "Large text" },
};
const STORAGE_KEY = "lb_fontsize";

export default function FontSizeControl() {
  const { lang } = useParams();
  const labels = ARIA[lang] || ARIA.en;
  const [size, setSize] = useState("md");

  // Mirror whatever the pre-paint script already applied (avoids a hydration
  // mismatch — server + first client render both start at "md").
  useEffect(() => {
    const applied = document.documentElement.dataset.fontsize;
    if (applied && STEPS.includes(applied)) setSize(applied);
  }, []);

  const apply = (next) => {
    setSize(next);
    document.documentElement.dataset.fontsize = next;
    try {
      window.localStorage.setItem(STORAGE_KEY, next);
    } catch {
      /* private mode / storage disabled — the choice just won't persist */
    }
  };

  return (
    <div className="fontsize" role="group" aria-label={labels.group}>
      {STEPS.map((step) => (
        <button
          key={step}
          type="button"
          className="fontsize__btn"
          aria-label={labels[step]}
          aria-pressed={size === step}
          onClick={() => apply(step)}
        >
          {GLYPH[step]}
        </button>
      ))}
    </div>
  );
}
