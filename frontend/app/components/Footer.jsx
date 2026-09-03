import { Link, useParams, useRouteLoaderData } from "react-router";

import { t } from "../i18n";
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
          <ul className="footer-brand__contact">
            {company.address && <li>{company.address}</li>}
            {company.phone && (
              <li><a href={`tel:${company.phone.replace(/\s+/g, "")}`}>{company.phone}</a></li>
            )}
            {company.email && (
              <li>
                <a href={`mailto:${company.email}`}>
                  {/* The address is wider than this column, so it has to wrap. The
                      break opportunity after "@" keeps it splitting at a boundary a
                      reader expects rather than mid-domain. */}
                  {company.email.split("@")[0]}@<wbr />{company.email.split("@").slice(1).join("@")}
                </a>
              </li>
            )}
            {company.officeHours && <li>{company.officeHours}</li>}
          </ul>
        </div>

        {/* Comps 8a/8b: About (its four mega-menu destinations), Get involved,
            and the networks as text links. News is not a footer column there —
            it keeps its own dropdown in the primary nav. */}
        <div className="footer-col">
          <Link to={L("/about")} className="footer-col__head">{t(lang, "nav.about")}</Link>
          <ul>
            <li><Link to={L("/about#background")}>{t(lang, "nav.aboutBackground")}</Link></li>
            <li><Link to={L("/about#mission")}>{t(lang, "nav.aboutMission")}</Link></li>
            <li><Link to={L("/about#structure")}>{t(lang, "nav.aboutStructure")}</Link></li>
            <li><Link to={L("/about#committee")}>{t(lang, "nav.aboutCommittee")}</Link></li>
          </ul>
        </div>

        <div className="footer-col">
          <span className="footer-col__head">{t(lang, "footer.involvedHead")}</span>
          <ul>
            <li><Link to={L("/donate")}>{t(lang, "nav.donate")}</Link></li>
            <li><Link to={L("/volunteer")}>{t(lang, "nav.recruitVolunteer")}</Link></li>
            <li><Link to={L("/careers")}>{t(lang, "nav.recruitJobs")}</Link></li>
          </ul>
        </div>

        <div className="footer-col">
          <span className="footer-col__head">{t(lang, "footer.followHead")}</span>
          <div className="footer-col__social">
            <SocialLinks company={company} withLabels />
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
