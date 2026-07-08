import ReactMarkdown from "react-markdown";
import { useLoaderData, useParams } from "react-router";

import { api, siteOrigin } from "../lib/api.server";
import { SUPPORTED_LANGS, t } from "../i18n";

export async function loader({ params, request }) {
  const event = await api.getEvent(params.slug, params.lang); // throws 404 Response for unknown slug
  return { event, origin: siteOrigin(request) };
}

export function meta({ data, params }) {
  if (!data) return [{ title: "樂橋 LucaBridge" }];
  const { event, origin } = data;
  const title = `${event.title} — 樂橋 LucaBridge`;
  const url = `${origin}/${params.lang}/events/${event.slug}`;

  return [
    { title },
    { name: "description", content: event.summary ?? event.title },
    { property: "og:type", content: "website" },
    { property: "og:title", content: event.title },
    { property: "og:url", content: url },
    ...(event.coverImageUrl ? [{ property: "og:image", content: event.coverImageUrl }] : []),
    { tagName: "link", rel: "canonical", href: url },
    ...SUPPORTED_LANGS.map((l) => ({
      tagName: "link",
      rel: "alternate",
      hrefLang: l,
      href: `${origin}/${l}/events/${event.slug}`,
    })),
  ];
}

export default function EventDetail() {
  const { lang } = useParams();
  const { event } = useLoaderData();

  return (
    <article className="shell" style={{ padding: "32px 20px" }}>
      {event.fallback && (
        <p className="meta" style={{ background: "var(--color-card)", padding: "8px 12px", borderRadius: "4px" }}>
          {t(lang, "page.fallbackNotice")}
        </p>
      )}

      {event.coverImageUrl && <img src={event.coverImageUrl} alt="" style={{ marginBottom: "24px" }} />}

      <div className="reading-column">
        <h1 style={{ fontSize: "clamp(26px, 4vw, 40px)" }}>{event.title}</h1>
        <div className="meta" style={{ display: "flex", gap: "12px", marginBottom: "24px" }}>
          {event.startsAt && <span>{new Date(event.startsAt).toLocaleString(lang)}</span>}
          {event.locationText && <span>{event.locationText}</span>}
        </div>
        <ReactMarkdown>{event.bodyMarkdown}</ReactMarkdown>
      </div>
    </article>
  );
}
