import { Link, useLocation, useParams } from "react-router";
import { SUPPORTED_LANGS } from "../i18n";

const LABELS = { "zh-Hant": "繁", en: "EN", "zh-Hans": "简" };

// Segmented control (design spec §06): joined pill, current language filled.
// Plain links that swap the :lang segment — the rest of the path and query are
// preserved, and as real <a> tags they work before hydration and are crawlable.
export default function LanguageSwitch() {
  const { lang } = useParams();
  const location = useLocation();
  const rest = location.pathname.split("/").slice(2).join("/");

  return (
    <div className="segmented" role="group" aria-label="Language">
      {SUPPORTED_LANGS.map((code) => (
        <Link
          key={code}
          to={`/${code}${rest ? `/${rest}` : ""}${location.search}`}
          aria-current={code === lang ? "true" : undefined}
        >
          {LABELS[code]}
        </Link>
      ))}
    </div>
  );
}
