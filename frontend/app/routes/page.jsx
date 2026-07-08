import ReactMarkdown from "react-markdown";
import { useLoaderData, useParams } from "react-router";

import { api, siteOrigin } from "../lib/api.server";
import { SUPPORTED_LANGS, t } from "../i18n";

export async function loader({ params, request }) {
  const page = await api.getPage(params.slug, params.lang); // throws 404 Response for unknown slug
  return { page, origin: siteOrigin(request) };
}

export function meta({ data, params }) {
  if (!data) return [{ title: "樂橋 LucaBridge" }];
  const { page, origin } = data;
  const title = `${page.title} — 樂橋 LucaBridge`;
  const url = `${origin}/${params.lang}/p/${page.slug}`;

  return [
    { title },
    { name: "description", content: page.subtitle ?? page.title },
    { property: "og:type", content: "website" },
    { property: "og:title", content: page.title },
    { property: "og:url", content: url },
    ...(page.heroImageUrl ? [{ property: "og:image", content: page.heroImageUrl }] : []),
    { tagName: "link", rel: "canonical", href: url },
    ...SUPPORTED_LANGS.map((l) => ({
      tagName: "link",
      rel: "alternate",
      hrefLang: l,
      href: `${origin}/${l}/p/${page.slug}`,
    })),
  ];
}

export default function Page() {
  const { lang } = useParams();
  const { page } = useLoaderData();

  return (
    <article className="shell" style={{ padding: "32px 20px" }}>
      {page.fallback && (
        <p className="meta" style={{ background: "var(--color-card)", padding: "8px 12px", borderRadius: "4px" }}>
          {t(lang, "page.fallbackNotice")}
        </p>
      )}

      {page.heroImageUrl && <img src={page.heroImageUrl} alt="" style={{ marginBottom: "24px" }} />}

      <div className="reading-column">
        <h1 style={{ fontSize: "clamp(26px, 4vw, 40px)" }}>{page.title}</h1>
        {page.subtitle && (
          <p style={{ fontFamily: "var(--font-kicker)", fontStyle: "italic" }}>{page.subtitle}</p>
        )}
        <ReactMarkdown>{page.bodyMarkdown}</ReactMarkdown>
      </div>
    </article>
  );
}
