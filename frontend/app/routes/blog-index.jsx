import { Link, useLoaderData, useParams, useSearchParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";
import PostCard from "../components/PostCard";

export async function loader({ params, request }) {
  const url = new URL(request.url);
  const category = url.searchParams.get("category") || undefined;
  const page = Math.max(Number(url.searchParams.get("page")) || 0, 0);

  const [categories, posts] = await Promise.all([
    api.getCategories(params.lang),
    api.getPosts({ lang: params.lang, category, page, size: 10 }),
  ]);
  return { categories, posts, category: category ?? "", page };
}

export function meta({ params }) {
  const title = `${t(params.lang, "nav.blog")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

// Category filter + pagination are plain links (SSR renders each state as real
// HTML — crawlable, and works before/without JavaScript).
export default function BlogIndex() {
  const { lang } = useParams();
  const { categories, posts, category, page } = useLoaderData();
  const [searchParams] = useSearchParams();

  const categoryHref = (key) => {
    const next = new URLSearchParams(searchParams);
    if (key) next.set("category", key);
    else next.delete("category");
    next.delete("page");
    const qs = next.toString();
    return `/${lang}/blog${qs ? `?${qs}` : ""}`;
  };

  const pageHref = (i) => {
    const next = new URLSearchParams(searchParams);
    next.set("page", String(i));
    return `/${lang}/blog?${next.toString()}`;
  };

  return (
    <div className="shell" style={{ padding: "32px 20px" }}>
      <nav style={{ display: "flex", gap: "12px", flexWrap: "wrap", marginBottom: "24px" }}>
        <Link to={categoryHref("")} className="kicker" style={{ opacity: category ? 0.5 : 1 }}>
          {t(lang, "blog.allCategories")}
        </Link>
        {categories.map((c) => (
          <Link
            key={c.key}
            to={categoryHref(c.key)}
            className="kicker"
            style={{ opacity: category === c.key ? 1 : 0.5 }}
          >
            {c.name}
          </Link>
        ))}
      </nav>

      {posts.items.length === 0 && <p>{t(lang, "blog.empty")}</p>}

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
          gap: "32px",
        }}
      >
        {posts.items.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}
      </div>

      {posts.totalPages > 1 && (
        <nav style={{ display: "flex", gap: "8px", justifyContent: "center", marginTop: "32px" }}>
          {Array.from({ length: posts.totalPages }).map((_, i) => (
            <Link key={i} to={pageHref(i)} style={{ fontWeight: i === page ? 700 : 400 }}>
              {i + 1}
            </Link>
          ))}
        </nav>
      )}
    </div>
  );
}
