import { useParams } from "react-router";
import { t } from "../i18n";

export default function Footer() {
  const { lang } = useParams();
  return (
    <footer style={{ background: "var(--color-ink)", color: "var(--color-paper)", marginTop: "60px", padding: "40px 0" }}>
      <div className="shell" style={{ display: "flex", justifyContent: "space-between", flexWrap: "wrap", gap: "16px" }}>
        <span>&copy; {new Date().getFullYear()} 樂橋 LucaBridge</span>
        <span>{t(lang, "nav.about")}</span>
      </div>
    </footer>
  );
}
