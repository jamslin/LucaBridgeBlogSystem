import { Link, useLoaderData, useParams } from "react-router";

import { api, siteOrigin } from "../lib/api.server";
import { t, SUPPORTED_LANGS, DEFAULT_LANG } from "../i18n";
import { formatArticleDate } from "../lib/date";
import Photo from "../components/Photo";
import PostCard from "../components/PostCard";

export async function loader({ params, request }) {
  const url = new URL(request.url);
  const page = Math.max(Number(url.searchParams.get("page")) || 0, 0);
  const posts = await api.getBlogList({ lang: params.lang, page, size: 10 });
  return { posts, page, origin: siteOrigin(request) };
}

// Canonical + the full hreflang set. Without these the three language listings compete
// with each other in search instead of being read as one page in three languages.
// hrefLang values must be real BCP-47 tags — Google silently ignores invalid ones.
export function meta({ params, data }) {
  const title = `${t(params.lang, "nav.blog")} — 樂橋 LucaBridge`;
  const origin = data?.origin ?? "";
  const url = `${origin}/${params.lang}/blog`;
  return [
    { title },
    { name: "description", content: t(params.lang, "blog.metaDescription") },
    { property: "og:title", content: title },
    { property: "og:type", content: "website" },
    { property: "og:url", content: url },
    { tagName: "link", rel: "canonical", href: url },
    ...SUPPORTED_LANGS.map((l) => ({
      tagName: "link", rel: "alternate", hrefLang: l, href: `${origin}/${l}/blog`,
    })),
    { tagName: "link", rel: "alternate", hrefLang: "x-default",
      href: `${origin}/${DEFAULT_LANG}/blog` },
  ];
}

// Blog index. The newest story on the first page takes the wide treatment and
// the rest sit in the three-up grid — the fix for every list on the site looking
// like the same card wall at different densities.
//
// Pagination stays plain links so SSR renders each page as real HTML.
export default function BlogIndex() {
  const { lang } = useParams();
  const { posts, page } = useLoaderData();

  const items = posts.content ?? [];
  const showFeatured = page === 0 && items.length > 2;
  const [lead, ...rest] = items;
  const gridItems = showFeatured ? rest : items;

  return (
    <div className="shell">
      <header className="page-head">
        <span className="kicker">{t(lang, "nav.news")}</span>
        <h1>{t(lang, "nav.newsRecent")}</h1>
      </header>

      {items.length === 0 && (
        <div className="empty-state">
          <h3>{t(lang, "blog.empty")}</h3>
          <p>{t(lang, "blog.emptyBody")}</p>
        </div>
      )}

      {showFeatured && (
        <article className="index-featured">
          <Link to={`/${lang}/blog/${lead.slug}`} tabIndex={-1} aria-hidden="true">
            <Photo src={lead.coverUrl} ratio="card" loading="eager" />
          </Link>
          <div>
            <span className="kicker">{t(lang, "blog.featured")}</span>
            <h2><Link to={`/${lang}/blog/${lead.slug}`}>{lead.title}</Link></h2>
            {lead.summary && <p>{lead.summary}</p>}
            <p className="meta">
              {formatArticleDate(lead.publishedAt, lang)}
              {lead.readMinutes
                ? ` · ${t(lang, "blog.readingTime", { count: lead.readMinutes })}`
                : null}
            </p>
            <Link to={`/${lang}/blog/${lead.slug}`} className="btn btn-primary">
              {t(lang, "home.readMore")} <span aria-hidden="true">→</span>
            </Link>
          </div>
        </article>
      )}

      {gridItems.length > 0 && (
        <div className={`card-grid${gridItems.length === 2 ? " card-grid--two" : ""}`}>
          {gridItems.map((post) => (
            <PostCard key={post.id} post={post} headingLevel="h2" />
          ))}
        </div>
      )}

      {posts.totalPages > 1 && (
        <nav className="pagination" aria-label="Pagination">
          {Array.from({ length: posts.totalPages }).map((_, i) => (
            <Link
              key={i}
              to={`/${lang}/blog?page=${i}`}
              aria-current={i === page ? "page" : undefined}
            >
              {i + 1}
            </Link>
          ))}
        </nav>
      )}
    </div>
  );
}
