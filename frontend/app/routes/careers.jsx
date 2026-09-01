import { Link, useLoaderData, useParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";
import { formatHongKongDate } from "../lib/date";

export async function loader({ params }) {
  const jobs = await api.getJobs({ lang: params.lang, size: 30 });
  return { jobs };
}

export function meta({ params }) {
  const title = `${t(params.lang, "nav.recruitJobs")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

export default function Careers() {
  const { lang } = useParams();
  const { jobs } = useLoaderData();

  return (
    <div className="shell">
      <header className="page-head">
        <span className="kicker">{t(lang, "nav.recruit")}</span>
        <h1>{t(lang, "nav.recruitJobs")}</h1>
      </header>

      {jobs.content.length === 0 ? (
        // A charity this size is often not hiring; the empty state still offers
        // the two routes that are always open.
        <div className="empty-state empty-state--cta">
          <div>
            <h3>{t(lang, "careers.empty")}</h3>
            <p>{t(lang, "careers.emptyBody")}</p>
          </div>
          <Link to={`/${lang}/volunteer`} className="btn btn-primary">
            {t(lang, "nav.volunteerCta")}
          </Link>
        </div>
      ) : (
        <div className="job-list">
          {jobs.content.map((j) => (
            <article className="job-card" key={j.id}>
              <div>
                <h3><Link to={`/${lang}/careers/${j.slug}`}>{j.title}</Link></h3>
                <p className="job-card__meta">
                  {[j.department, j.location, j.employmentType].filter(Boolean).map((part) => (
                    <span key={part}>{part}</span>
                  ))}
                </p>
              </div>
              <div className="job-card__aside">
                <span className="meta">
                  {j.closesAt
                    ? `${t(lang, "careers.closes")}${formatHongKongDate(j.closesAt, lang, { long: true })}`
                    : t(lang, "careers.openUntilFilled")}
                </span>
                <Link to={`/${lang}/careers/${j.slug}`} className="btn btn-ghost btn--sm">
                  {t(lang, "home.readMore")}
                </Link>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
