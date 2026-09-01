import { Link, useLoaderData, useParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";
import PostCard from "../components/PostCard";
import { hongKongDayMonth } from "../lib/date";

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
      <section className="home-hero shell" aria-label="Homepage highlights">
        <div
          className={hero?.mediaUrl ? "home-hero__slide" : "home-hero__empty"}
          style={hero?.mediaUrl ? { "--hero-image": `url(${hero.mediaUrl})` } : undefined}
        >
          <div className="home-hero__content">
            <span className="home-hero__eyebrow">{company?.name || "LucaBridge"}</span>
            <h1>{hero?.title || heroFallback.title}</h1>
            <p>{hero?.subtitle || heroFallback.subtitle}</p>
            <Link className="home-hero__cta" to={hero?.linkUrl ? hero.linkUrl : `/${lang}/volunteer`}>
              {hero?.buttonLabel || t(lang, "nav.volunteerCta")}
              <span aria-hidden="true">→</span>
            </Link>
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
        <section className="home-events shell" aria-labelledby="home-events-title">
          <div className="home-section-heading">
            <div>
              <span className="kicker">LucaBridge</span>
              <h2 id="home-events-title">{t(lang, "home.upcomingEvents")}</h2>
            </div>
            <Link to={`/${lang}/events`} className="btn-text">
              {t(lang, "home.viewAllEvents")} <span className="arrow">→</span>
            </Link>
          </div>
          <div className="home-events__list">
            {upcomingEvents.map((e) => {
              const { day, month } = hongKongDayMonth(e.startsAt, lang);
              return (
                <Link key={e.id} to={`/${lang}/events/${e.slug}`} className="home-events__row">
                  <span className="home-events__date">
                    {day && <><b>{day}</b>{month}</>}
                  </span>
                  <span>
                    <span className="home-events__title">{e.title}</span>
                    <span className="home-events__meta">
                      {e.venue && <span className="meta">{e.venue}</span>}
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
        </section>
      )}

      {/* The one ask on the page, with the proof stats inside it. Falls back to
          the approved copy so the band still reads before the CMS is filled. */}
      <section className="home-support shell">
        <div className="support">
          <div>
            <span className="kicker kicker--cream">{t(lang, "home.supportEyebrow")}</span>
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
                {t(lang, "home.supportNoteTime")}
                <br />
                {t(lang, "home.supportNoteMinor")}
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
