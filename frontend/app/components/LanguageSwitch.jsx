import { Link, useLocation, useParams } from "react-router";
import { SUPPORTED_LANGS } from "../i18n";

const LABELS = { "zh-Hant": "繁", en: "EN", "zh-Hans": "简" };

// Plain links that swap the :lang segment — the rest of the path and query are
// preserved ("language switch never reloads or loses place"), and as real <a>
// tags they work before hydration and are crawlable as hreflang alternates.
export default function LanguageSwitch() {
  const { lang } = useParams();
  const location = useLocation();
  const rest = location.pathname.split("/").slice(2).join("/");

  return (
    <div role="group" aria-label="Language" style={{ display: "flex", gap: "8px" }}>
      {SUPPORTED_LANGS.map((code) => (
        <Link
          key={code}
          to={`/${code}${rest ? `/${rest}` : ""}${location.search}`}
          aria-current={code === lang ? "true" : undefined}
          style={{
            border: "1px solid var(--color-line)",
            borderRadius: "999px",
            padding: "4px 10px",
            fontSize: "12px",
            background: code === lang ? "var(--color-ink)" : "transparent",
            color: code === lang ? "var(--color-paper)" : "var(--color-ink-soft)",
          }}
        >
          {LABELS[code]}
        </Link>
      ))}
    </div>
  );
}
