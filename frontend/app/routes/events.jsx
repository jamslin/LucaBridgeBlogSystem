import { Link, useLoaderData, useParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";
import { formatHongKongDate } from "../lib/date";

export async function loader({ params }) {
  const events = await api.getEvents({ lang: params.lang, size: 30 });
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

      {events.content.length === 0 && <p>{t(lang, "events.empty")}</p>}

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))", gap: "32px" }}>
        {events.content.map((e) => (
          <article
            key={e.id}
            style={{ background: "var(--color-card)", borderRadius: "8px", overflow: "hidden", border: "1px solid var(--color-line)" }}
          >
            {e.coverUrl && <img src={e.coverUrl} alt="" style={{ borderRadius: 0 }} />}
            <div style={{ padding: "16px" }}>
              <span className="kicker">{formatHongKongDate(e.startsAt, lang, { long: true })}</span>
              <h3 style={{ margin: "4px 0" }}>
                <Link to={`/${lang}/events/${e.slug}`}>{e.title}</Link>
              </h3>
              {e.venue && <p className="meta">{e.venue}</p>}
              {e.summary && <p>{e.summary}</p>}
              {e.registration && (
                <span className={`reg-badge reg-badge--${e.registration.state.toLowerCase().replace(/_/g, "-")}`}>
                  {e.registration.almostFull && e.registration.state === "OPEN"
                    ? t(lang, "events.almostFull")
                    : t(lang, {
                        OPEN: "events.registerCta",
                        FULL: "events.full",
                        CLOSED: "events.closed",
                        NOT_OPEN: "events.notOpenYet",
                        NOT_REGISTERABLE: "events.notRegisterable",
                      }[e.registration.state] || "events.registerCta")}
                </span>
              )}
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
