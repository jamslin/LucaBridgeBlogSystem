import { useParams, useRouteLoaderData } from "react-router";
import { t } from "../i18n";

export function meta({ params }) {
  const title = `${t(params.lang, "about.title")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

// company.about is the one piece of "about us" content the backend actually holds
// (see docs/backend review: the v2 schema has no generic CMS page table — those
// deeper sub-pages the mockup shows as an About dropdown were dropped in favour of
// static marketing copy, which the organisation still needs to supply).
export default function About() {
  const { lang } = useParams();
  const layoutData = useRouteLoaderData("routes/lang-layout");
  const company = layoutData?.company ?? {};

  return (
    <div className="shell static-page__hero" style={{ paddingBottom: "48px" }}>
      <div className="reading-column">
      <span className="kicker">LucaBridge</span>
      <h1>{t(lang, "about.title")}</h1>

      {company.about ? <p>{company.about}</p> : (
        <div className="pending-notice">{t(lang, "common.contentPending")}</div>
      )}

      <h2>{t(lang, "about.missionTitle")}</h2>
      <div className="pending-notice">{t(lang, "about.missionPending")}</div>

      <h2>{t(lang, "about.structureTitle")}</h2>
      <div className="pending-notice">{t(lang, "about.structurePending")}</div>
      </div>
    </div>
  );
}
