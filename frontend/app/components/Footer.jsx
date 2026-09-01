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
        {/* Brand column. The logo is uploaded in Company; the footer used to
            ignore it entirely, so an uploaded logo only showed in the nav. */}
        <div className="footer-brandcol">
          <div className="footer-brand">
            <span className="site-brand__mark" aria-hidden="true">
              {company.logoUrl ? <img src={company.logoUrl} alt="" /> : null}
            </span>
            <span className="footer-brand__text">
              <span className="site-brand__name">{lang === "en" ? "LucaBridge" : "樂橋"}</span>
              <span className="site-brand__sub">
                {lang === "en" ? "Charity · Hong Kong" : "LUCA BRIDGE"}
              </span>
            </span>
          </div>
          {company.officeHours && <p className="footer-brand__hours">{company.officeHours}</p>}
          <div className="footer-social">
            <SocialLinks company={company} size={20} color="currentColor" />
          </div>
        </div>

        {/* Explicit columns rather than one per NAV entry: mapping the nav gave
            a column each to Services and Donate, which have no children, so the
            footer rendered two empty headings and wrapped onto a second row. */}
        <div className="footer-col">
          <span className="footer-col__head">{t(lang, "nav.about")}</span>
          <ul>
            <li><Link to={L("/about")}>{t(lang, "nav.about")}</Link></li>
            <li><Link to={L("/services")}>{t(lang, "nav.services")}</Link></li>
          </ul>
        </div>

        <div className="footer-col">
          <span className="footer-col__head">{t(lang, "nav.news")}</span>
          <ul>
            <li><Link to={L("/blog")}>{t(lang, "nav.newsRecent")}</Link></li>
            <li><Link to={L("/events")}>{t(lang, "nav.newsEvents")}</Link></li>
          </ul>
        </div>

        <div className="footer-col">
          <span className="footer-col__head">{t(lang, "footer.involvedHead")}</span>
          <ul>
            <li><Link to={L("/volunteer")}>{t(lang, "nav.recruitVolunteer")}</Link></li>
            <li><Link to={L("/careers")}>{t(lang, "nav.recruitJobs")}</Link></li>
            <li><Link to={L("/donate")}>{t(lang, "nav.donate")}</Link></li>
          </ul>
        </div>

        <div className="footer-col">
          <Link to={L("/contact")} className="footer-col__head">{t(lang, "nav.contact")}</Link>
          <ul>
            {company.address && <li>{company.address}</li>}
            {company.phone && <li><a href={`tel:${company.phone.replace(/\s+/g, "")}`}>{company.phone}</a></li>}
            {company.email && <li><a href={`mailto:${company.email}`}>{company.email}</a></li>}
          </ul>
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
