import { Link, useParams, useRouteLoaderData } from "react-router";

import { t } from "../i18n";
import { NAV } from "../nav";
import SocialLinks from "./SocialLinks";

export default function Footer() {
  const { lang } = useParams();
  const layoutData = useRouteLoaderData("routes/lang-layout");
  const company = layoutData?.company ?? {};
  const L = (p) => `/${lang}${p}`;

  return (
    <footer className="site-footer">
      <div className="shell footer-grid">
        {/* Brand block. The logo is uploaded in Company; the footer previously
            ignored it, so an uploaded logo only ever appeared in the nav. */}
        <div className="footer-brand">
          <span className="site-brand__mark" aria-hidden="true">
            {company.logoUrl ? <img src={company.logoUrl} alt="" /> : null}
          </span>
          <span className="site-brand__name">{lang === "en" ? "LucaBridge" : "樂橋"}</span>
          <span className="site-brand__sub">
            {lang === "en" ? "Charity · Hong Kong" : "LUCA BRIDGE"}
          </span>
          {company.officeHours && <p className="footer-brand__hours">{company.officeHours}</p>}
        </div>

        {NAV.map((item) => (
          <div key={item.key} className="footer-col">
            <Link to={L(item.to)} className="footer-col__head">{t(lang, `nav.${item.key}`)}</Link>
            {item.children && (
              <ul>
                {item.children.map((c) => (
                  <li key={c.key}><Link to={L(c.to)}>{t(lang, `nav.${c.key}`)}</Link></li>
                ))}
              </ul>
            )}
          </div>
        ))}
        <div className="footer-col">
          <Link to={L("/contact")} className="footer-col__head">{t(lang, "nav.contact")}</Link>
          <ul>
            {company.address && <li>{company.address}</li>}
            {company.phone && <li><a href={`tel:${company.phone}`}>{company.phone}</a></li>}
            {company.email && <li><a href={`mailto:${company.email}`}>{company.email}</a></li>}
          </ul>
          <div style={{ marginTop: "12px" }}>
            <SocialLinks company={company} size={20} color="var(--color-paper)" />
          </div>
        </div>
      </div>

      <div className="shell footer-legal">
        <span>
          &copy; {layoutData?.currentYear} {company.name || "樂橋 LucaBridge"}
          {company.charityRegNo ? ` · ${company.charityRegNo}` : ""}
        </span>
        <nav style={{ display: "flex", gap: "16px", fontSize: "14px" }}>
          <Link to={L("/privacy")}>{t(lang, "footer.privacy")}</Link>
          <Link to={L("/terms")}>{t(lang, "footer.terms")}</Link>
        </nav>
      </div>
    </footer>
  );
}
