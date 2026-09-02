import { Link, useParams, useRouteLoaderData } from "react-router";

import { t } from "../i18n";
import SocialLinks from "../components/SocialLinks";

export function meta({ params }) {
  const title = `${t(params.lang, "contact.title")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

/**
 * Contact.
 *
 * The page it replaces was a definition list of five fields — address, phone,
 * email, hours, charity number — which is a record of the organisation rather
 * than a way to reach it. The rebuild is organised around what a visitor is
 * actually trying to do:
 *
 * 1. Reach a person now. Three channels as large tap targets, each saying what
 *    it is good for, so nobody has to guess whether to phone or write.
 * 2. Find the office. Address, hours, and a maps link.
 * 3. Not actually a general enquiry. Most "contact us" traffic on a charity site
 *    is really "how do I volunteer / give goods / apply for a job", so those are
 *    routed here rather than answered by email one at a time.
 *
 * There is deliberately no message form: the backend has no endpoint to receive
 * one, and a form that silently goes nowhere is worse than no form. WhatsApp
 * leads instead — it is the channel this organisation already runs event
 * confirmations through, so it is the one most likely to be answered.
 */
export default function Contact() {
  const { lang } = useParams();
  const layoutData = useRouteLoaderData("routes/lang-layout");
  const company = layoutData?.company ?? {};

  const telHref = company.phone ? `tel:${company.phone.replace(/[^\d+]/g, "")}` : null;
  const waHref = company.phone
    ? `https://wa.me/${company.phone.replace(/\D/g, "")}`
    : null;
  const mapHref = company.address
    ? `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(company.address)}`
    : null;

  const routes = [
    { to: "/volunteer", key: "routeVolunteer" },
    { to: "/donate", key: "routeDonate" },
    { to: "/careers", key: "routeCareers" },
  ];

  return (
    <div className="contact-page">
      <header className="shell contact-hero">
        <span className="kicker">{t(lang, "contact.eyebrow")}</span>
        <h1>
          {t(lang, "contact.heading").split("\n").map((line, i) => (
            <span key={i} style={{ display: "block" }}>{line}</span>
          ))}
        </h1>
        <p className="contact-hero__lead">{t(lang, "contact.lead")}</p>
      </header>

      <section className="shell contact-channels" aria-label={t(lang, "contact.channelsLabel")}>
        {waHref && (
          // WhatsApp first and in the accent colour: it is the channel this
          // organisation already answers fastest.
          <a className="channel channel--primary" href={waHref} target="_blank" rel="noopener noreferrer">
            <span className="channel__label">WhatsApp</span>
            <span className="channel__value">{company.phone}</span>
            <span className="channel__hint">{t(lang, "contact.whatsappHint")}</span>
            <span className="channel__go" aria-hidden="true">→</span>
          </a>
        )}

        {telHref && (
          <a className="channel" href={telHref}>
            <span className="channel__label">{t(lang, "contact.phone")}</span>
            <span className="channel__value">{company.phone}</span>
            <span className="channel__hint">{t(lang, "contact.phoneHint")}</span>
            <span className="channel__go" aria-hidden="true">→</span>
          </a>
        )}

        {company.email && (
          <a className="channel" href={`mailto:${company.email}`}>
            <span className="channel__label">{t(lang, "contact.email")}</span>
            <span className="channel__value channel__value--email">{company.email}</span>
            <span className="channel__hint">{t(lang, "contact.emailHint")}</span>
            <span className="channel__go" aria-hidden="true">→</span>
          </a>
        )}
      </section>

      <section className="shell contact-visit" aria-label={t(lang, "contact.visitLabel")}>
        <div className="visit-card">
          <span className="kicker">{t(lang, "contact.visitLabel")}</span>
          <p className="visit-card__address">{company.address || t(lang, "contact.addressPending")}</p>
          {mapHref && (
            <a className="btn btn-ghost" href={mapHref} target="_blank" rel="noopener noreferrer">
              {t(lang, "contact.openMap")} <span aria-hidden="true">↗</span>
            </a>
          )}
        </div>

        <div className="visit-card visit-card--hours">
          <span className="kicker kicker--muted">{t(lang, "contact.officeHours")}</span>
          <p className="visit-card__hours">{company.officeHours || "—"}</p>
          <p className="visit-card__note">{t(lang, "contact.hoursNote")}</p>
        </div>
      </section>

      {/* Most "contact us" traffic is one of these three, so answer it here
          rather than one email at a time. */}
      <section className="shell contact-routes" aria-labelledby="contact-routes-title">
        <div className="section-head">
          <div>
            <span className="kicker">{t(lang, "contact.routesEyebrow")}</span>
            <h2 id="contact-routes-title">{t(lang, "contact.routesTitle")}</h2>
          </div>
        </div>
        <div className="route-grid">
          {routes.map((r) => (
            <Link key={r.key} to={`/${lang}${r.to}`} className="route-card">
              <span className="route-card__title">{t(lang, `contact.${r.key}`)}</span>
              <span className="route-card__desc">{t(lang, `contact.${r.key}Desc`)}</span>
              <span className="route-card__go" aria-hidden="true">→</span>
            </Link>
          ))}
        </div>
      </section>

      <section className="shell contact-follow">
        <div className="follow-band">
          <div>
            <span className="kicker kicker--cream">{t(lang, "contact.followEyebrow")}</span>
            <h2>{t(lang, "contact.followTitle")}</h2>
          </div>
          <SocialLinks company={company} size={26} color="currentColor" />
        </div>

        <p className="contact-legal">
          {company.charityRegNo && (
            <span>{t(lang, "contact.charityRegNo")} {company.charityRegNo}</span>
          )}
          {company.foundedYear && <span>{t(lang, "footer.founded")}</span>}
        </p>
      </section>
    </div>
  );
}
