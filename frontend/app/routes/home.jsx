import { Link, useLoaderData, useParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";
import Photo from "../components/Photo";
import PostCard from "../components/PostCard";
import { formatDayMonth, formatTimeRange, formatWeekday, hongKongDayMonth, weekBucket } from "../lib/date";

// One call for the whole page — see backend HomeController for why (RR7 SSR pays
// every one of five separate calls as a real backend round trip on every render
// of the busiest page on the site).
export async function loader({ params }) {
  const home = await api.getHomePage(params.lang);
  return { home };
}

export function meta() {
  return [
    { title: "樂橋 LucaBridge" },
    { property: "og:title", content: "樂橋 LucaBridge" },
    { property: "og:type", content: "website" },
  ];
}

const REG_BADGE_KEY = {
  OPEN: "events.registerCta",
  FULL: "events.full",
  CLOSED: "events.closed",
  NOT_OPEN: "events.notOpenYet",
  NOT_REGISTERABLE: "events.notRegisterable",
};

const HERO_FALLBACK = {
  "zh-Hant": { title: "用愛築橋，連結希望", subtitle: "樂橋連繫有需要人士、義工與社區伙伴，讓每一份關懷都能抵達。" },
  "zh-Hans": { title: "用爱筑桥，连结希望", subtitle: "乐桥连系有需要人士、义工与社区伙伴，让每一份关怀都能抵达。" },
  en: { title: "Bridging hearts, building hope", subtitle: "LucaBridge connects people, volunteers and community partners so every act of care can reach further." },
};

export default function Home() {
  const { lang } = useParams();
  const { home } = useLoaderData();
  const { homeBlocks, upcomingEvents, latestPosts, services, company } = home;

  const hero = homeBlocks.HERO?.[0];
  const heroFallback = HERO_FALLBACK[lang] || HERO_FALLBACK.en;
  const stats = homeBlocks.STAT || [];
  const support = homeBlocks.SUPPORT?.[0];
  const featured = homeBlocks.FEATURED?.[0];
  const quickLinks = [...(homeBlocks.VOLUNTEER || []), ...(homeBlocks.QUICK_LINK || [])];

  return (
    <div className="home-page">
      {/* No `shell` here: the hero runs edge to edge and the header floats over
          it, so the content column does its own centring. */}
      <section className="home-hero" aria-label="Homepage highlights">
        <div
          className={hero?.mediaUrl ? "home-hero__slide" : "home-hero__empty"}
          style={hero?.mediaUrl ? { "--hero-image": `url(${hero.mediaUrl})` } : undefined}
        >
          <div className="home-hero__content">
            <span className="home-hero__eyebrow">{hero?.eyebrow || company?.name || "LucaBridge"}</span>
            <h1>
              {(hero?.title || heroFallback.title).split("\n").map((line, i) => (
                <span key={i} style={{ display: "block" }}>{line}</span>
              ))}
            </h1>
            {/* The comp pairs the headline with the other-language line. Skip it
                when the tagline is the same words as the headline, which is the
                default in 繁中 and would just repeat the H1. */}
            {company?.tagline && company.tagline !== (hero?.title || heroFallback.title) && (
              <p className="home-hero__alt">{company.tagline}</p>
            )}
            <p>{hero?.subtitle || heroFallback.subtitle}</p>

            <div className="home-hero__actions">
              <Link className="home-hero__cta" to={hero?.linkUrl ? hero.linkUrl : `/${lang}/volunteer`}>
                {hero?.buttonLabel || t(lang, "nav.volunteerCta")}
                <span aria-hidden="true">→</span>
              </Link>
              <Link className="home-hero__secondary" to={`/${lang}/about`}>
                {t(lang, "home.heroSecondary")}
              </Link>
            </div>

            {/* The registration line the comp sets under the CTA — for a charity
                asking strangers for time, the licence number is the proof. */}
            <p className="home-hero__charity">
              <span>{t(lang, "home.charityLine")}</span>
              {company?.charityRegNo && (
                <span>{t(lang, "home.charityOrdinance")} · {company.charityRegNo}</span>
              )}
            </p>
          </div>
        </div>
      </section>

      {services.length > 0 && (
        <section className="service-row" aria-label={t(lang, "home.servicesLabel")}>
          <div className="shell service-row__inner">
            <span className="service-row__label">{t(lang, "home.servicesLabel")}</span>
            <div className="chip-row">
              {services.map((s) => (
                <Link key={s.id} to={`/${lang}/services#${s.code}`} className="chip">
                  {s.name}
                  <span className="chip__arrow" aria-hidden="true">→</span>
                </Link>
              ))}
            </div>
          </div>
        </section>
      )}

      {upcomingEvents.length > 0 && (
        <section className="section shell" aria-labelledby="home-events-title">
          <div className="section-head">
            <div>
              <span className="kicker">{t(lang, "home.upcomingEyebrow")}</span>
              <h2 id="home-events-title">{t(lang, "home.upcomingTitle")}</h2>
            </div>
            <Link to={`/${lang}/events`} className="btn-text">
              {t(lang, "home.viewAllEvents")} <span className="arrow" aria-hidden="true">→</span>
            </Link>
          </div>

          {/* A dated rail, not another card grid: "when" leads, and the row's
              dot and badge say at a glance whether a reader can act on it. */}
          <div className="timeline">
            {upcomingEvents.map((e, index) => {
              const state = e.registration?.state;
              const bucket = weekBucket(e.startsAt);
              return (
                <div
                  key={e.id}
                  className={[
                    "timeline__row",
                    index === 0 ? "timeline__row--soon" : "",
                    state === "OPEN" ? "timeline__row--open" : "",
                    state === "FULL" ? "timeline__row--wait" : "",
                  ].filter(Boolean).join(" ")}
                >
                  <div className="timeline__when">
                    <span className="timeline__when-label">
                      {bucket === "later"
                        ? t(lang, "timeline.month", { month: hongKongDayMonth(e.startsAt, lang).month })
                        : t(lang, `timeline.${bucket}`)}
                    </span>
                    <span className="timeline__when-date">{formatDayMonth(e.startsAt)}</span>
                    <span className="timeline__when-day">{formatWeekday(e.startsAt, lang)}</span>
                  </div>

                  <div className="timeline__rail" aria-hidden="true"><span className="timeline__dot" /></div>

                  <EventRow event={e} lead={index === 0} lang={lang} services={services} />
                </div>
              );
            })}
          </div>
        </section>
      )}

      {/* The one ask on the page, with the proof stats inside it. Falls back to
          the approved copy so the band still reads before the CMS is filled. */}
      <section className="home-support shell">
        <div className="support">
          <div>
            <span className="kicker kicker--cream">{support?.eyebrow || t(lang, "home.supportEyebrow")}</span>
            <h2>
              {(support?.title || t(lang, "home.supportTitle"))
                .split("\n")
                .map((line, i) => <span key={i}>{line}</span>)}
            </h2>
            <p className="support__lead">{support?.subtitle || t(lang, "home.supportLead")}</p>
            <div className="support__actions">
              <Link className="btn btn-on-red" to={support?.linkUrl || `/${lang}/volunteer`}>
                {support?.buttonLabel || t(lang, "nav.volunteerCta")}
                <span aria-hidden="true">→</span>
              </Link>
              <p className="support__notes">
                {(support?.note
                  || `${t(lang, "home.supportNoteTime")}\n${t(lang, "home.supportNoteMinor")}`)
                  .split("\n")
                  .map((line, i) => <span key={i}>{i > 0 && <br />}{line}</span>)}
              </p>
            </div>
          </div>

          {stats.length > 0 && (
            <div className="support__stats">
              {stats.map((s) => {
                // A STAT block carries a number and a label; an optional second
                // line after a newline in the subtitle gives the comp's
                // supporting caption without needing a schema change.
                const [label, sub] = (s.subtitle || "").split("\n");
                return (
                  <div className="stat" key={s.id}>
                    <span className="stat__value">{s.title}</span>
                    <span className="stat__label">
                      {label}
                      {sub && <span className="stat__sub">{sub}</span>}
                    </span>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </section>

      {featured && (
        <section className="home-promo shell" style={{ paddingTop: 0 }}>
          <div className="home-promo__card home-promo__card--light" style={{ display: "grid", gridTemplateColumns: featured.mediaUrl ? "1fr 1fr" : "1fr", gap: 24, alignItems: "center" }}>
            {featured.mediaUrl && <img src={featured.mediaUrl} alt="" style={{ borderRadius: 12 }} />}
            <div>
              <h2>{featured.title}</h2>
              {featured.subtitle && <p>{featured.subtitle}</p>}
              <Link className="btn btn-primary" to={featured.blogSlug ? `/${lang}/blog/${featured.blogSlug}` : (featured.linkUrl || `/${lang}/blog`)}>
                {featured.buttonLabel || t(lang, "home.readMore")}
              </Link>
            </div>
          </div>
        </section>
      )}

      {quickLinks.length > 0 && (
        <section className="home-quicklinks shell">
          <div className="home-quicklinks__grid">
            {quickLinks.map((q) => (
              <Link key={q.id} to={q.linkUrl || `/${lang}`} className="home-quicklinks__card">
                <h3>{q.title}</h3>
                {q.subtitle && <p className="meta" style={{ margin: 0 }}>{q.subtitle}</p>}
              </Link>
            ))}
          </div>
        </section>
      )}

      {latestPosts.length > 0 && (
        <section className="home-news">
          <div className="shell">
            <div className="home-news__heading">
              <div>
                <span className="kicker">LucaBridge</span>
                <h2>{t(lang, "home.latest")}</h2>
              </div>
              <Link to={`/${lang}/blog`} className="btn-text">
                {t(lang, "home.readMore")} <span className="arrow">→</span>
              </Link>
            </div>
            <div className="home-news__grid">
              {latestPosts.slice(0, 6).map((post) => (
                <div className="home-news-card" key={post.id}>
                  <PostCard post={post} />
                </div>
              ))}
            </div>
          </div>
        </section>
      )}
    </div>
  );
}

const REG_CTA = {
  OPEN: { key: "events.registerCta", cls: "btn btn-primary" },
  FULL: { key: "events.joinWaitlist", cls: "btn btn-wait" },
  CLOSED: { key: "events.closed", cls: "btn", disabled: true },
  NOT_OPEN: { key: "events.notOpenYet", cls: "btn btn-ghost" },
  NOT_REGISTERABLE: { key: "events.contactUs", cls: "btn btn-ghost" },
};

/**
 * One event on the timeline. The first row is the "lead": it gets the summary
 * and a photo, so the nearest thing to act on is also the biggest thing on the
 * rail. Later rows stay compact so three events still read as a schedule
 * rather than a sparse grid.
 */
function EventRow({ event, lead, lang, services = [] }) {
  const reg = event.registration;
  const cta = REG_CTA[reg?.state] || REG_CTA.NOT_REGISTERABLE;
  const href = `/${lang}/events/${event.slug}`;
  const showCapacity = reg && reg.capacity != null && (reg.state === "OPEN" || reg.state === "FULL");
  // The comp tags every event with its service area — the same taxonomy that
  // drives the chip row and blog tagging, so the site reads as one system.
  const serviceName = event.serviceName
    ?? services.find((s) => s.id === event.serviceId)?.name;
  const timeRange = formatTimeRange(event.startsAt, event.endsAt);
  const pct = showCapacity ? Math.min(100, Math.round((reg.registeredCount / reg.capacity) * 100)) : 0;

  return (
    <article className={`event-card${lead ? " event-card--lead" : ""}`}>
      <div>
        <div className="event-card__badges">
          {serviceName && <span className="badge-tag">{serviceName}</span>}
          {reg && (
            <span className={`reg-badge reg-badge--${reg.state.toLowerCase().replace(/_/g, "-")}`}>
              {reg.almostFull && reg.state === "OPEN"
                ? t(lang, "events.almostFull")
                : t(lang, REG_BADGE_KEY[reg.state] || "events.registerCta")}
              {reg.state === "OPEN" && reg.remaining != null
                ? ` · ${t(lang, "events.spotsLeftCount", { count: reg.remaining })}`
                : null}
            </span>
          )}
        </div>

        <h3 className="event-card__title"><Link to={href}>{event.title}</Link></h3>

        <p className="event-card__meta">
          {[timeRange, event.venue].filter(Boolean).join(" · ")}
          {lead && event.summary ? <><br />{event.summary}</> : null}
        </p>

        {showCapacity && (
          <div className="event-card__capacity">
            <span className={`capacity${reg.state === "FULL" ? " capacity--full" : ""}`}>
              <span className="capacity__fill" style={{ width: `${Math.max(pct, 4)}%` }} />
            </span>
            <span className="capacity__label">
              {t(lang, "events.capacityLine", { total: reg.capacity, count: reg.registeredCount })}
            </span>
          </div>
        )}

        <div className="event-card__actions">
          {cta.disabled
            ? <span className={cta.cls} aria-disabled="true">{t(lang, cta.key)}</span>
            : <Link to={href} className={cta.cls}>{t(lang, cta.key)}</Link>}
          <Link to={href} className="btn-text btn-text--underline">{t(lang, "events.details")}</Link>
        </div>
      </div>

      {lead && (
        <Link to={href} className="event-card__media" tabIndex={-1} aria-hidden="true">
          <Photo src={event.coverUrl} ratio="card" />
        </Link>
      )}
    </article>
  );
}
