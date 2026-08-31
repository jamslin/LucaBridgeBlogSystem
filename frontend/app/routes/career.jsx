import ReactMarkdown from "react-markdown";
import { Link, useLoaderData, useParams } from "react-router";

import { api, siteOrigin } from "../lib/api.server";
import { SUPPORTED_LANGS, t } from "../i18n";
import { formatHongKongDate } from "../lib/date";

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
    { name: "description", content: job.title },
    { property: "og:type", content: "website" },
    { property: "og:title", content: job.title },
    { property: "og:url", content: url },
    { tagName: "link", rel: "canonical", href: url },
    ...SUPPORTED_LANGS.map((l) => ({
      tagName: "link", rel: "alternate", hrefLang: l, href: `${origin}/${l}/careers/${job.slug}`,
    })),
  ];
}

export default function Career() {
  const { lang } = useParams();
  const { job } = useLoaderData();
  const facts = [
    job.employmentType,
    job.department,
    job.location,
    job.closesAt ? `${t(lang, "careers.closes")}${formatHongKongDate(job.closesAt, lang, { long: true })}` : null,
  ].filter(Boolean);

  return (
    <article className="shell" style={{ padding: "32px 20px" }}>
      <div className="reading-column">
        <p className="kicker">{t(lang, "nav.recruitJobs")}</p>
        <h1 style={{ fontSize: "clamp(26px, 4vw, 40px)" }}>{job.title}</h1>
        {facts.length > 0 && <p className="meta" style={{ marginBottom: "8px" }}>{facts.join(" · ")}</p>}
        {job.body && <ReactMarkdown>{job.body}</ReactMarkdown>}

        {(job.applyEmail || job.applyUrl) && (
          <p style={{ marginTop: "24px" }}>
            <a
              className="btn btn-primary"
              href={job.applyUrl || `mailto:${job.applyEmail}`}
              target={job.applyUrl ? "_blank" : undefined}
              rel={job.applyUrl ? "noopener noreferrer" : undefined}
            >
              {t(lang, "careers.apply")}
            </a>
          </p>
        )}

        <p style={{ marginTop: "32px" }}>
          <Link to={`/${lang}/careers`}>← {t(lang, "careers.backToList")}</Link>
        </p>
      </div>
    </article>
  );
}
