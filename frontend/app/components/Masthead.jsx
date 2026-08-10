import { Link, useParams, useRouteLoaderData } from "react-router";

import { t } from "../i18n";
import SocialLinks from "./SocialLinks";

// Two-row masthead: a slim utility bar (contact + social, Caritas-style) above
// the wordmark row. Settings come from the lang-layout loader.
export default function Masthead() {
  const { lang } = useParams();
  const layoutData = useRouteLoaderData("routes/lang-layout");
  const settings = layoutData?.settings ?? {};

  return (
    <header style={{ borderBottom: "2px solid var(--color-ink)" }}>
      <div className="masthead-utility">
        <div className="shell masthead-utility__inner">
          <span className="masthead-utility__contact">
            {settings.phone && <a href={`tel:${settings.phone}`}>{settings.phone}</a>}
            {settings.email && <a href={`mailto:${settings.email}`}>{settings.email}</a>}
          </span>
          <span className="masthead-utility__right">
            <Link to={`/${lang}/p/contact`}>{t(lang, "nav.contact")}</Link>
            <SocialLinks settings={settings} size={16} color="var(--color-paper)" />
          </span>
        </div>
      </div>

      <div className="shell" style={{ display: "flex", alignItems: "center", gap: "12px", padding: "20px 0" }}>
        <span
          aria-hidden="true"
          style={{ width: "18px", height: "18px", background: "var(--color-accent)", transform: "rotate(45deg)", display: "inline-block" }}
        />
        <Link to={`/${lang}`} style={{ fontFamily: "var(--font-display)", fontWeight: 600, fontSize: "22px", color: "var(--color-ink)" }}>
          樂橋 LucaBridge
        </Link>
      </div>
    </header>
  );
}
