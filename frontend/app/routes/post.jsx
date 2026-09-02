import ReactMarkdown from "react-markdown";
import { Link, useLoaderData, useParams } from "react-router";

import { api, siteOrigin } from "../lib/api.server";
import { SUPPORTED_LANGS, t, DEFAULT_LANG } from "../i18n";
import Gallery from "../components/Gallery";
import Photo from "../components/Photo";
import { formatArticleDate } from "../lib/date";

export async function loader({ params, request }) {
  const post = await api.getBlogPost(params.slug, params.lang); // throws 404 Response for unknown slug
  return { post, origin: siteOrigin(request) };
}

// Full OG + hreflang set, server-rendered — WhatsApp/Facebook scrapers (which
// never run JS) and Google all get complete metadata.
export function meta({ data, params }) {
  if (!data) return [{ title: "樂橋 LucaBridge" }];
  const { post, origin } = data;
  const title = `${post.title} — 樂橋 LucaBridge`;
  const url = `${origin}/${params.lang}/blog/${post.slug}`;

  return [
    { title },
    { name: "description", content: post.summary ?? post.title },
    { property: "og:type", content: "article" },
    { property: "og:title", content: post.title },
    { property: "og:description", content: post.summary ?? "" },
    { property: "og:url", content: url },
    ...(post.coverUrl ? [{ property: "og:image", content: post.coverUrl }] : []),
    { tagName: "link", rel: "canonical", href: url },
    ...SUPPORTED_LANGS.map((l) => ({
      tagName: "link",
      rel: "alternate",
      hrefLang: l,
      href: `${origin}/${l}/blog/${post.slug}`,
    })),
    // Where a searcher's locale matches none of the three, send them to the base language
    // rather than letting Google pick.
    {
      tagName: "link",
      rel: "alternate",
      hrefLang: "x-default",
      href: `${origin}/${DEFAULT_LANG}/blog/${post.slug}`,
    },
  ];
}

// Article template (mockup 8d): breadcrumb, date + read time, large serif
// headline, standfirst, full-width cover, then a plain-text reading column.
//
// The body carries no images by design — the brief rules them out inside article
// copy, so photography appears only as the cover and the gallery block. The
// reading measure is set per script in CSS (720px CJK / 680px Latin), both
// landing around 65-75 characters a line.
export default function Post() {
  const { lang } = useParams();
  const { post } = useLoaderData();

  return (
    <article className="article">
      <div className="shell">
        <nav className="breadcrumb" aria-label="Breadcrumb">
          <Link to={`/${lang}/blog`}>{t(lang, "nav.news")}</Link>
          <span aria-hidden="true">›</span>
          <span>{t(lang, "nav.newsRecent")}</span>
        </nav>

        <header className="article__head">
          <span className="meta">
            {formatArticleDate(post.publishedAt, lang)}
            {post.readMinutes
              ? ` · ${t(lang, "blog.readingTime", { count: post.readMinutes })}`
              : null}
          </span>
          <h1>{post.title}</h1>
          {post.summary && <p className="article__standfirst">{post.summary}</p>}
        </header>

        {post.coverUrl && (
          <figure className="article__cover">
            <Photo src={post.coverUrl} ratio="cover" loading="eager" />
          </figure>
        )}

        <div className="reading-column article__body">
          <ReactMarkdown
            components={{
              // The comps set quotes as a red-ruled pull quote, not an indent.
              blockquote: ({ children }) => <blockquote className="pull-quote">{children}</blockquote>,
            }}
          >{post.body}</ReactMarkdown>
        </div>

        <Gallery media={post.gallery} layout={post.galleryLayout} headingId="post-gallery" />

        <p className="article__back">
          <Link to={`/${lang}/blog`} className="btn-text">
            ← {t(lang, "post.browseAll")}
          </Link>
        </p>
      </div>
    </article>
  );
}
