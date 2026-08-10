import ReactMarkdown from "react-markdown";
import { Link, useLoaderData, useParams } from "react-router";

import { api, siteOrigin } from "../lib/api.server";
import { SUPPORTED_LANGS, t } from "../i18n";

export async function loader({ params, request }) {
  const job = await api.getJob(params.slug, params.lang);
  return { job, origin: siteOrigin(request) };
}

export function meta({ data, params }) {
  if (!data) return [{ title: "樂橋 LucaBridge" }];
  const { job, origin } = data;
  const title = `${job.title} — 樂橋 LucaBridge`;
  const url = `${origin}/${params.lang}/careers/${job.slug}`;
  return [
    { title },
    { name: "description", content: job.summary ?? job.title },
    { property: "og:type", content: "website" },
    { property: "og:title", content: job.title },
    { property: "og:url", content: url },
    { tagName: "link", rel: "canonical", href: url },
    ...SUPPORTED_LANGS.map((l) => ({
      tagName: "link", rel: "alternate", hrefLang: l, href: `${origin}/${l}/careers/${job.slug}`,
    })),
  ];
}

function formatDate(iso, lang) {
  if (!iso) return "";
  return new Date(iso).toLocaleDateString(lang, { year: "numeric", month: "long", day: "numeric" });
}

export default function Career() {
  const { lang } = useParams();
  const { job } = useLoaderData();
  const facts = [
    job.employmentTypeLabel,
    job.department,
    job.locationText,
    job.closesAt ? `${t(lang, "careers.closes")}${formatDate(job.closesAt, lang)}` : null,
  ].filter(Boolean);

  return (
    <article className="shell" style={{ padding: "32px 20px" }}>
      {job.fallback && (
        <p className="meta" style={{ background: "var(--color-card)", padding: "8px 12px", borderRadius: "4px" }}>
          {t(lang, "page.fallbackNotice")}
        </p>
      )}
      <div className="reading-column">
        <p className="kicker">{t(lang, "nav.recruitJobs")}</p>
        <h1 style={{ fontSize: "clamp(26px, 4vw, 40px)" }}>{job.title}</h1>
        {facts.length > 0 && <p className="meta" style={{ marginBottom: "8px" }}>{facts.join(" · ")}</p>}
        {job.summary && <p style={{ fontFamily: "var(--font-kicker)", fontStyle: "italic" }}>{job.summary}</p>}
        <ReactMarkdown>{job.bodyMarkdown}</ReactMarkdown>
        <p style={{ marginTop: "32px" }}>
          <Link to={`/${lang}/careers`}>← {t(lang, "careers.backToList")}</Link>
        </p>
      </div>
    </article>
  );
}
