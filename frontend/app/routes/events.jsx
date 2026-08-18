import { Link, useLoaderData, useParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";
import { formatHongKongDate } from "../lib/date";

export async function loader({ params }) {
  const events = await api.getEvents(params.lang);
  return { events };
}

export function meta({ params }) {
  const title = `${t(params.lang, "nav.events")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

export default function Events() {
  const { lang } = useParams();
  const { events } = useLoaderData();

  return (
    <div className="shell" style={{ padding: "32px 20px" }}>
      <h1 style={{ fontSize: "clamp(26px, 4vw, 40px)", marginBottom: "24px" }}>{t(lang, "nav.events")}</h1>

      {events.length === 0 && <p>{t(lang, "events.empty")}</p>}

      <div className="events-grid">
        {events.map((e) => (
          <article key={e.id} className="events-card">
            {e.coverImageUrl && (
              <Link to={`/${lang}/events/${e.slug}`} className="events-card__media">
                <img src={e.coverImageUrl} alt="" loading="lazy" />
              </Link>
            )}
            <div style={{ padding: "16px" }}>
              <span className="kicker">{formatHongKongDate(e.startsAt, lang, { long: true })}</span>
              <h3 style={{ margin: "4px 0" }}>
                <Link to={`/${lang}/events/${e.slug}`}>{e.title}</Link>
              </h3>
              {e.locationText && <p className="meta">{e.locationText}</p>}
              {e.summary && <p>{e.summary}</p>}
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
