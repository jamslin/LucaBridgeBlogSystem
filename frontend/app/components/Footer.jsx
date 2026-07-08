import { Link, useParams } from "react-router";
import { t } from "../i18n";

export default function Footer() {
  const { lang } = useParams();
  return (
    <footer style={{ background: "var(--color-ink)", color: "var(--color-paper)", marginTop: "60px", padding: "40px 0" }}>
      <div className="shell" style={{ display: "flex", justifyContent: "space-between", flexWrap: "wrap", gap: "16px" }}>
        <span>&copy; {new Date().getFullYear()} 樂橋 LucaBridge</span>
        <nav style={{ display: "flex", gap: "16px", fontSize: "14px" }}>
          <Link to={`/${lang}/p/about`} style={{ color: "var(--color-paper)" }}>{t(lang, "nav.about")}</Link>
          <Link to={`/${lang}/p/privacy`} style={{ color: "var(--color-paper)" }}>{t(lang, "footer.privacy")}</Link>
          <Link to={`/${lang}/p/terms`} style={{ color: "var(--color-paper)" }}>{t(lang, "footer.terms")}</Link>
        </nav>
      </div>
    </footer>
  );
}
