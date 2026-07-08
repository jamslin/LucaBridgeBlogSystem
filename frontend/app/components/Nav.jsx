import { Link, useParams } from "react-router";
import { t } from "../i18n";
import LanguageSwitch from "./LanguageSwitch";

export default function Nav() {
  const { lang } = useParams();

  return (
    <div className="shell" style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "12px 0" }}>
      <nav style={{ display: "flex", gap: "20px", fontSize: "14px" }}>
        <Link to={`/${lang}`}>{t(lang, "nav.home")}</Link>
        <Link to={`/${lang}/p/about`}>{t(lang, "nav.about")}</Link>
        <Link to={`/${lang}/blog`}>{t(lang, "nav.blog")}</Link>
        <Link to={`/${lang}/events`}>{t(lang, "nav.events")}</Link>
      </nav>
      <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
        <Link to={`/${lang}/p/donate`} className="btn btn-primary">
          {t(lang, "nav.donate")}
        </Link>
        <LanguageSwitch />
      </div>
    </div>
  );
}
