import ReactMarkdown from "react-markdown";
import { useLoaderData, useParams } from "react-router";

import { api, siteOrigin } from "../lib/api.server";
import { SUPPORTED_LANGS, t } from "../i18n";
import Gallery from "../components/Gallery";
import PressLinks from "../components/PressLinks";
import PrevNextBar from "../components/PrevNextBar";

export async function loader({ params, request }) {
  const post = await api.getPost(params.slug, params.lang); // throws 404 Response for unknown slug
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
    { name: "description", content: post.subtitle ?? post.title },
    { property: "og:type", content: "article" },
    { property: "og:title", content: post.title },
    { property: "og:description", content: post.subtitle ?? "" },
    { property: "og:url", content: url },
    ...(post.coverImageUrl ? [{ property: "og:image", content: post.coverImageUrl }] : []),
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
      {post.fallback && (
        <p className="meta" style={{ background: "var(--color-paper-2)", padding: "8px 12px", borderRadius: "2px" }}>
          {t(lang, "post.fallbackNotice")}
        </p>
      )}

      {post.coverImageUrl && <img src={post.coverImageUrl} alt="" style={{ marginBottom: "24px" }} />}

      <div className="reading-column">
        <span className="kicker">{post.category?.name}</span>
        <h1 style={{ fontSize: "clamp(26px, 4vw, 40px)" }}>{post.title}</h1>
        {post.subtitle && <p style={{ fontFamily: "var(--font-kicker)", fontStyle: "italic" }}>{post.subtitle}</p>}
        <div className="meta" style={{ display: "flex", gap: "12px", marginBottom: "24px" }}>
          {post.publishedAt && <span>{new Date(post.publishedAt).toLocaleDateString(lang)}</span>}
          {post.readingMinutes && <span>{post.readingMinutes} min</span>}
        </div>

        <ReactMarkdown>{post.bodyMarkdown}</ReactMarkdown>

        <Gallery media={post.media} />
        <PressLinks links={post.pressLinks} />
        <PrevNextBar previous={post.previous} next={post.next} />
      </div>
    </article>
  );
}
