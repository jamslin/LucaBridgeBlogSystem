import { Link, useParams } from "react-router";
import { t } from "../i18n";

export function meta({ params }) {
  const title = `${t(params.lang, "volunteer.title")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

export default function Volunteer() {
  const { lang } = useParams();

  return (
    <div className="shell static-page__hero reading-column" style={{ paddingBottom: "48px" }}>
      <span className="kicker">LucaBridge</span>
      <h1>{t(lang, "volunteer.title")}</h1>
      <div className="pending-notice">{t(lang, "volunteer.pending")}</div>
      <p>
        <Link to={`/${lang}/careers`} className="btn-text">
          {t(lang, "nav.recruitJobs")} <span className="arrow">→</span>
        </Link>
      </p>
    </div>
  );
}
