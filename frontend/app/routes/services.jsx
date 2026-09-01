import { useLoaderData, useParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";

export async function loader({ params }) {
  const services = await api.getServices(params.lang);
  return { services };
}

export function meta({ params }) {
  const title = `${t(params.lang, "services.title")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

export default function Services() {
  const { lang } = useParams();
  const { services } = useLoaderData();

  return (
    <div className="shell static-page__hero" style={{ paddingBottom: "48px" }}>
      <span className="kicker">LucaBridge</span>
      <h1>{t(lang, "services.title")}</h1>

      {services.length === 0 && <p>{t(lang, "services.empty")}</p>}

      <div className="services-grid">
        {services.map((s) => (
          // id matches the code the home page's chip row links to, so a chip
          // lands on its own service rather than the top of the page.
          <div key={s.id} id={s.code} className="service-card">
            {s.iconUrl && <img className="service-card__icon" src={s.iconUrl} alt="" />}
            <h3>{s.name}</h3>
            {s.description && <p>{s.description}</p>}
          </div>
        ))}
      </div>
    </div>
  );
}
