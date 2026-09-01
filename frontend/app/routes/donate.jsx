import { useParams, useRouteLoaderData } from "react-router";
import { t } from "../i18n";

export function meta({ params }) {
  const title = `${t(params.lang, "donate.title")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

export default function Donate() {
  const { lang } = useParams();
  const layoutData = useRouteLoaderData("routes/lang-layout");
  const company = layoutData?.company ?? {};

  return (
    <div className="shell static-page__hero" style={{ paddingBottom: "48px" }}>
      <div className="reading-column">
      <span className="kicker">LucaBridge</span>
      <h1>{t(lang, "donate.title")}</h1>
      <div className="pending-notice">{t(lang, "donate.pending")}</div>
      {(company.email || company.phone) && (
        <p>
          {company.email && <a href={`mailto:${company.email}`}>{company.email}</a>}
          {company.email && company.phone && " · "}
          {company.phone && <a href={`tel:${company.phone}`}>{company.phone}</a>}
        </p>
      )}
      </div>
    </div>
  );
}
