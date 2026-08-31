import ReactMarkdown from "react-markdown";
import { useLoaderData, useParams } from "react-router";

import { api, siteOrigin } from "../lib/api.server";
import { SUPPORTED_LANGS } from "../i18n";
import Gallery from "../components/Gallery";
import { formatHongKongDate } from "../lib/date";

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
  ];
}

export default function Post() {
  const { lang } = useParams();
  const { post } = useLoaderData();

  return (
    <article className="shell" style={{ padding: "32px 20px" }}>
      {post.coverUrl && <img src={post.coverUrl} alt="" style={{ marginBottom: "24px" }} />}

      <div className="reading-column">
        <h1 style={{ fontSize: "clamp(26px, 4vw, 40px)" }}>{post.title}</h1>
        <div className="meta" style={{ display: "flex", gap: "12px", marginBottom: "24px" }}>
          {post.publishedAt && <span>{formatHongKongDate(post.publishedAt, lang)}</span>}
          {post.readMinutes && <span>{post.readMinutes} min</span>}
        </div>

        <ReactMarkdown
          components={{
            // The comps set quotes as a red-ruled pull quote, not an indent.
            blockquote: ({ children }) => <blockquote className="pull-quote">{children}</blockquote>,
          }}
        >{post.body}</ReactMarkdown>

        <Gallery media={post.gallery} layout={post.galleryLayout} />
      </div>
    </article>
  );
}
