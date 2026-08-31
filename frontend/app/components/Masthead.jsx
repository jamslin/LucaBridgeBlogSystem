import { Link, useParams, useRouteLoaderData } from "react-router";

import { t } from "../i18n";
import SocialLinks from "./SocialLinks";

// Two-row masthead: a slim utility bar (contact + social) above the wordmark row.
// Company data comes from the lang-layout loader.
export default function Masthead() {
  const { lang } = useParams();
  const layoutData = useRouteLoaderData("routes/lang-layout");
  const company = layoutData?.company ?? {};

  return (
    <header style={{ borderBottom: "2px solid var(--color-ink)" }}>
      <div className="masthead-utility">
        <div className="shell masthead-utility__inner">
          <span className="masthead-utility__contact">
            {company.phone && <a href={`tel:${company.phone}`}>{company.phone}</a>}
            {company.email && <a href={`mailto:${company.email}`}>{company.email}</a>}
          </span>
          <span className="masthead-utility__right">
            <Link to={`/${lang}/contact`}>{t(lang, "nav.contact")}</Link>
            <SocialLinks company={company} size={16} color="var(--color-paper)" />
          </span>
        </div>
      </div>

      <div className="shell masthead-brand">
        <span aria-hidden="true" className="masthead-brand__mark" />
        <Link to={`/${lang}`} className="masthead-brand__name">
          {company.name || "樂橋 LucaBridge"}
        </Link>
      </div>
    </header>
  );
}
