import { Link, useLoaderData, useParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";

export async function loader({ params }) {
  const jobs = await api.getJobs(params.lang);
  return { jobs };
}

export function meta({ params }) {
  const title = `${t(params.lang, "nav.recruitJobs")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

function formatDate(iso, lang) {
  if (!iso) return "";
  return new Date(iso).toLocaleDateString(lang, { year: "numeric", month: "long", day: "numeric" });
}

export default function Careers() {
  const { lang } = useParams();
  const { jobs } = useLoaderData();

  return (
    <div className="shell" style={{ padding: "32px 20px" }}>
      <h1 style={{ fontSize: "clamp(26px, 4vw, 40px)", marginBottom: "24px" }}>{t(lang, "nav.recruitJobs")}</h1>

      {jobs.length === 0 && <p>{t(lang, "careers.empty")}</p>}

      <ul style={{ listStyle: "none", padding: 0, margin: 0, display: "grid", gap: "16px" }}>
        {jobs.map((j) => (
          <li
            key={j.id}
            style={{ background: "var(--color-card)", border: "1px solid var(--color-line)", borderRadius: "8px", padding: "20px 24px" }}
          >
            <div style={{ display: "flex", flexWrap: "wrap", gap: "8px", alignItems: "baseline", justifyContent: "space-between" }}>
              <h3 style={{ margin: 0 }}>
                <Link to={`/${lang}/careers/${j.slug}`}>{j.title}</Link>
              </h3>
              {j.employmentTypeLabel && <span className="chip">{j.employmentTypeLabel}</span>}
            </div>
            <p className="meta" style={{ margin: "6px 0" }}>
              {[j.department, j.locationText].filter(Boolean).join(" · ")}
            </p>
            {j.summary && <p style={{ margin: "8px 0 0" }}>{j.summary}</p>}
            <p className="meta" style={{ marginTop: "10px" }}>
              {j.closesAt
                ? `${t(lang, "careers.closes")}${formatDate(j.closesAt, lang)}`
                : t(lang, "careers.openUntilFilled")}
            </p>
          </li>
        ))}
      </ul>
    </div>
  );
}
