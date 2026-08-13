import { useLoaderData, useParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";
import PostCard from "../components/PostCard";

export async function loader({ params }) {
  const [posts, banners] = await Promise.all([
    api.getPosts({ lang: params.lang, size: 5 }),
    api.getBanners(params.lang),
  ]);
  return { posts, banners };
}

export function meta({ params }) {
  return [
    { title: "樂橋 LucaBridge" },
    { property: "og:title", content: "樂橋 LucaBridge" },
    { property: "og:type", content: "website" },
  ];
}

export default function Home() {
  const { lang } = useParams();
  const { posts, banners } = useLoaderData();
  const [featured, ...rest] = posts.items;

  return (
    <div className="shell" style={{ padding: "32px 20px" }}>
      {banners.length > 0 && (
        <section aria-label="Homepage highlights" style={{ display: "grid", gap: 16, marginBottom: 40 }}>
          {banners.map((banner) => (
            <article key={banner.id} style={{ minHeight: 300, padding: "48px", borderRadius: 18, color: "white", display: "flex", alignItems: "flex-end", background: `linear-gradient(90deg, rgba(0,0,0,.68), rgba(0,0,0,.12)), url(${banner.imageUrl}) center/cover` }}>
              <div style={{ maxWidth: 620 }}>
                <h1 style={{ margin: 0, fontSize: "clamp(30px, 5vw, 54px)" }}>{banner.title}</h1>
                {banner.subtitle && <p style={{ fontSize: 18 }}>{banner.subtitle}</p>}
                {banner.linkUrl && <a href={banner.linkUrl} style={{ display: "inline-block", padding: "11px 18px", borderRadius: 999, background: "white", color: "#222", fontWeight: 700 }}>{banner.buttonLabel || "Learn more"}</a>}
              </div>
            </article>
          ))}
        </section>
      )}
      {featured && (
        <section style={{ marginBottom: "40px" }}>
          <PostCard post={featured} />
        </section>
      )}

      <h2 className="kicker" style={{ fontSize: "13px" }}>{t(lang, "home.latest")}</h2>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
          gap: "32px",
          marginTop: "16px",
        }}
      >
        {rest.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}
      </div>
    </div>
  );
}
