import { Link, useLoaderData, useParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";
import { formatHongKongDate, hongKongDayMonth } from "../lib/date";

export async function loader({ params }) {
  const events = await api.getEvents({ lang: params.lang, size: 30 });
  return { events };
}

export function meta({ params }) {
  const title = `${t(params.lang, "nav.events")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

const REG_BADGE_KEY = {
  OPEN: "events.registerCta",
  FULL: "events.full",
  CLOSED: "events.closed",
  NOT_OPEN: "events.notOpenYet",
  NOT_REGISTERABLE: "events.notRegisterable",
};

// Event index as a dated schedule rather than another card grid: "when" leads,
// and each row carries the registration state that decides whether a reader can
// act on it. Same treatment as the home page's upcoming list, so the two read as
// one system.
export default function Events() {
  const { lang } = useParams();
  const { events } = useLoaderData();
  const items = events.content ?? [];

  return (
    <div className="shell">
      <header className="page-head">
        <span className="kicker">{t(lang, "nav.news")}</span>
        <h1>{t(lang, "nav.events")}</h1>
      </header>

      {items.length === 0 ? (
        <div className="empty-state empty-state--cta">
          <div>
            <h3>{t(lang, "events.empty")}</h3>
            <p>{t(lang, "events.emptyBody")}</p>
          </div>
          <Link to={`/${lang}/volunteer`} className="btn btn-primary">
            {t(lang, "nav.volunteerCta")}
          </Link>
        </div>
      ) : (
        <div className="home-events__list">
          {items.map((e) => {
            const { day, month } = hongKongDayMonth(e.startsAt, lang);
            return (
              <Link key={e.id} to={`/${lang}/events/${e.slug}`} className="home-events__row">
                <span className="home-events__date">
                  {day && <><b>{day}</b>{month}</>}
                </span>
                <span>
                  <span className="home-events__title">{e.title}</span>
                  <span className="home-events__meta">
                    {e.startsAt && (
                      <span className="meta">{formatHongKongDate(e.startsAt, lang, { long: true })}</span>
                    )}
                    {e.venue && <span className="meta">· {e.venue}</span>}
                  </span>
                </span>
                {e.registration && (
                  <span className={`reg-badge reg-badge--${e.registration.state.toLowerCase().replace(/_/g, "-")}`}>
                    {e.registration.almostFull && e.registration.state === "OPEN"
                      ? t(lang, "events.almostFull")
                      : t(lang, REG_BADGE_KEY[e.registration.state] || "events.registerCta")}
                  </span>
                )}
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}
