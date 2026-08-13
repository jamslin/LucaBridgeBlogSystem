import { useEffect, useMemo, useState } from "react";
import { Link, useLoaderData, useParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";
import PostCard from "../components/PostCard";

export async function loader({ params }) {
  const [posts, banners] = await Promise.all([
    api.getPosts({ lang: params.lang, size: 7 }),
    api.getBanners(params.lang),
  ]);
  return { posts, banners };
}

export function meta() {
  return [
    { title: "樂橋 LucaBridge" },
    { property: "og:title", content: "樂橋 LucaBridge" },
    { property: "og:type", content: "website" },
  ];
}

const HOME_COPY = {
  "zh-Hant": {
    heroLabel: "與社區同行",
    fallbackTitle: "以同行連結社區，以服務建立希望",
    fallbackSubtitle: "樂橋連繫有需要人士、義工與社區伙伴，讓每一份關懷都能抵達。",
    servicesEyebrow: "我們的工作",
    servicesTitle: "連結資源，回應社區需要",
    servicesIntro: "由直接支援到社區參與，與你一起把關懷化成行動。",
    viewAll: "查看全部消息",
    serviceItems: [
      ["社區服務", "了解樂橋如何與社區同行", "/p/services", "01"],
      ["義工參與", "用時間與專長帶來改變", "/p/volunteer", "02"],
      ["支持我們", "每一份支持都延續服務", "/p/donate", "03"],
    ],
  },
  "zh-Hans": {
    heroLabel: "与社区同行",
    fallbackTitle: "以同行连结社区，以服务建立希望",
    fallbackSubtitle: "乐桥连系有需要人士、义工与社区伙伴，让每一份关怀都能抵达。",
    servicesEyebrow: "我们的工作",
    servicesTitle: "连结资源，回应社区需要",
    servicesIntro: "由直接支援到社区参与，与你一起把关怀化成行动。",
    viewAll: "查看全部消息",
    serviceItems: [
      ["社区服务", "了解乐桥如何与社区同行", "/p/services", "01"],
      ["义工参与", "用时间与专长带来改变", "/p/volunteer", "02"],
      ["支持我们", "每一份支持都延续服务", "/p/donate", "03"],
    ],
  },
  en: {
    heroLabel: "Walking with our community",
    fallbackTitle: "Connecting communities, building hope through service",
    fallbackSubtitle: "LucaBridge brings people, volunteers and community partners together so every act of care can reach further.",
    servicesEyebrow: "What we do",
    servicesTitle: "Connecting resources with community needs",
    servicesIntro: "From direct support to community participation, we turn care into action together.",
    viewAll: "View all news",
    serviceItems: [
      ["Community services", "See how LucaBridge walks with our community", "/p/services", "01"],
      ["Volunteer", "Create change with your time and skills", "/p/volunteer", "02"],
      ["Support us", "Every contribution keeps our work moving", "/p/donate", "03"],
    ],
  },
};

export default function Home() {
  const { lang } = useParams();
  const { posts, banners } = useLoaderData();
  const copy = HOME_COPY[lang] || HOME_COPY.en;
  const [featured, ...rest] = posts.items;
  const slides = useMemo(() => {
    if (banners.length) return banners.map((banner) => ({ ...banner, kind: "banner" }));
    if (!featured) return [];
    return [{
      id: `post-${featured.id}`,
      title: featured.title || copy.fallbackTitle,
      subtitle: featured.excerpt || copy.fallbackSubtitle,
      imageUrl: featured.coverImageUrl,
      linkUrl: `/${lang}/blog/${featured.slug}`,
      buttonLabel: t(lang, "home.readMore"),
      kind: "post",
    }];
  }, [banners, copy, featured, lang]);
  const [activeSlide, setActiveSlide] = useState(0);

  useEffect(() => {
    if (slides.length < 2) return undefined;
    const timer = window.setInterval(
      () => setActiveSlide((current) => (current + 1) % slides.length),
      6500,
    );
    return () => window.clearInterval(timer);
  }, [slides.length]);

  const slide = slides[activeSlide] || null;
  const latestPosts = banners.length ? posts.items.slice(0, 6) : rest.slice(0, 6);

  return (
    <div className="home-page">
      <section className="home-hero shell" aria-label="Homepage highlights">
        {slide ? (
          <article
            className="home-hero__slide"
            style={{ "--hero-image": `url(${slide.imageUrl})` }}
          >
            <div className="home-hero__content">
              <span className="home-hero__eyebrow">{copy.heroLabel}</span>
              <h1>{slide.title}</h1>
              {slide.subtitle && <p>{slide.subtitle}</p>}
              {slide.linkUrl && (
                <a className="home-hero__cta" href={slide.linkUrl}>
                  {slide.buttonLabel || t(lang, "home.readMore")}
                  <span aria-hidden="true">→</span>
                </a>
              )}
            </div>
            {slides.length > 1 && (
              <div className="home-hero__controls" aria-label="Select highlight">
                {slides.map((item, index) => (
                  <button
                    key={item.id}
                    type="button"
                    aria-label={`Slide ${index + 1}`}
                    aria-current={index === activeSlide ? "true" : undefined}
                    onClick={() => setActiveSlide(index)}
                  />
                ))}
              </div>
            )}
          </article>
        ) : (
          <div className="home-hero__empty">
            <span className="home-hero__eyebrow">{copy.heroLabel}</span>
            <h1>{copy.fallbackTitle}</h1>
            <p>{copy.fallbackSubtitle}</p>
          </div>
        )}
      </section>

      <section className="home-services shell" aria-labelledby="home-services-title">
        <div className="home-section-heading">
          <div>
            <span className="kicker">{copy.servicesEyebrow}</span>
            <h2 id="home-services-title">{copy.servicesTitle}</h2>
          </div>
          <p>{copy.servicesIntro}</p>
        </div>
        <div className="home-service-grid">
          {copy.serviceItems.map(([title, description, path, number]) => (
            <Link key={path} to={`/${lang}${path}`} className="home-service-card">
              <span className="home-service-card__number">{number}</span>
              <div>
                <h3>{title}</h3>
                <p>{description}</p>
              </div>
              <span className="home-service-card__arrow" aria-hidden="true">↗</span>
            </Link>
          ))}
        </div>
      </section>

      <section className="home-news">
        <div className="shell">
          <div className="home-news__heading">
            <div>
              <span className="kicker">LucaBridge</span>
              <h2>{t(lang, "home.latest")}</h2>
            </div>
            <Link to={`/${lang}/blog`} className="btn-text">
              {copy.viewAll} <span className="arrow">→</span>
            </Link>
          </div>
          <div className="home-news__grid">
            {latestPosts.map((post) => (
              <div className="home-news-card" key={post.id}>
                <PostCard post={post} />
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
