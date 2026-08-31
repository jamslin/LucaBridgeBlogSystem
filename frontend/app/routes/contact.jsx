import { useParams, useRouteLoaderData } from "react-router";
import { t } from "../i18n";
import SocialLinks from "../components/SocialLinks";

export function meta({ params }) {
  const title = `${t(params.lang, "contact.title")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

export default function Contact() {
  const { lang } = useParams();
  const layoutData = useRouteLoaderData("routes/lang-layout");
  const company = layoutData?.company ?? {};

  return (
    <div className="shell static-page__hero" style={{ paddingBottom: "48px" }}>
      <span className="kicker">LucaBridge</span>
      <h1>{t(lang, "contact.title")}</h1>

      <dl className="contact-grid">
        {company.address && (<><dt>{t(lang, "contact.address")}</dt><dd>{company.address}</dd></>)}
        {company.phone && (<><dt>{t(lang, "contact.phone")}</dt><dd><a href={`tel:${company.phone}`}>{company.phone}</a></dd></>)}
        {company.email && (<><dt>{t(lang, "contact.email")}</dt><dd><a href={`mailto:${company.email}`}>{company.email}</a></dd></>)}
        {company.officeHours && (<><dt>{t(lang, "contact.officeHours")}</dt><dd>{company.officeHours}</dd></>)}
        {company.charityRegNo && (<><dt>{t(lang, "contact.charityRegNo")}</dt><dd>{company.charityRegNo}</dd></>)}
      </dl>

      <SocialLinks company={company} size={24} />
    </div>
  );
}
