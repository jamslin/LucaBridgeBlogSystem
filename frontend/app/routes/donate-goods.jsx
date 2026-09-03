import { Link, useParams, useRouteLoaderData } from "react-router";

import { t } from "../i18n";

export function meta({ params }) {
  const title = `${t(params.lang, "donateGoods.title")} — 樂橋 LucaBridge`;
  return [
    { title },
    { name: "description", content: t(params.lang, "donateGoods.lead") },
    { property: "og:title", content: title },
  ];
}

/**
 * 物資捐贈 / Donate goods — one child of the 捐款 tab, alongside 捐款方法
 * (money) at /donate. The organisation runs largely on in-kind giving — the
 * home block, the service copy and the imported write-ups all describe
 * 糧食、日用品、全新校服 going straight to Yuen Long families — so this page
 * answers what someone with a box in their hand actually asks, in order: is
 * my thing wanted (needs), what do I do (steps), is there a catch
 * (requirements), where do I bring it (drop-off), who do I tell (CTA).
 *
 * There is deliberately no form and no pickup promise: the backend has no
 * endpoint to receive one, and collection is an operational commitment only the
 * organisation can make. WhatsApp leads, as it does on the contact page.
 */
export default function DonateGoods() {
  const { lang } = useParams();
  const layoutData = useRouteLoaderData("routes/lang-layout");
  const company = layoutData?.company ?? {};

  const waHref = company.phone ? `https://wa.me/${company.phone.replace(/\D/g, "")}` : null;
  const mapHref = company.address
    ? `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(company.address)}`
    : null;

  const needs = [1, 2, 3, 4];
  const steps = [1, 2, 3];
  const notes = [1, 2, 3, 4];

  return (
    <div className="goods-page">
      <header className="shell goods-hero">
        <span className="kicker">{t(lang, "donateGoods.eyebrow")}</span>
        <h1>
          {t(lang, "donateGoods.heading").split("\n").map((line, i) => (
            <span key={i} style={{ display: "block" }}>{line}</span>
          ))}
        </h1>
        <p className="goods-hero__lead">{t(lang, "donateGoods.lead")}</p>
      </header>

      <section className="shell goods-needs" aria-labelledby="goods-needs-title">
        <div className="section-head">
          <div>
            <span className="kicker">{t(lang, "donateGoods.needsEyebrow")}</span>
            <h2 id="goods-needs-title">{t(lang, "donateGoods.needsTitle")}</h2>
          </div>
        </div>
        <div className="need-grid">
          {needs.map((n) => (
            <article className="need-card" key={n}>
              <span className="need-card__num" aria-hidden="true">
                {String(n).padStart(2, "0")}
              </span>
              <h3 className="need-card__title">{t(lang, `donateGoods.need${n}`)}</h3>
              <p className="need-card__desc">{t(lang, `donateGoods.need${n}Desc`)}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="shell goods-steps" aria-labelledby="goods-steps-title">
        <div className="section-head">
          <div>
            <span className="kicker">{t(lang, "donateGoods.stepsEyebrow")}</span>
            <h2 id="goods-steps-title">{t(lang, "donateGoods.stepsTitle")}</h2>
          </div>
        </div>
        {/* An ordered list, not divs: the sequence is the content. */}
        <ol className="step-list">
          {steps.map((n) => (
            <li className="step" key={n}>
              <span className="step__num" aria-hidden="true">{n}</span>
              <div>
                <h3 className="step__title">{t(lang, `donateGoods.step${n}`)}</h3>
                <p className="step__desc">{t(lang, `donateGoods.step${n}Desc`)}</p>
              </div>
            </li>
          ))}
        </ol>
      </section>

      <section className="shell goods-visit" aria-labelledby="goods-notes-title">
        <div className="visit-card">
          <span className="kicker">{t(lang, "donateGoods.dropoffLabel")}</span>
          <p className="visit-card__address">
            {company.address || t(lang, "contact.addressPending")}
          </p>
          {company.officeHours && (
            <p className="visit-card__hours">{company.officeHours}</p>
          )}
          <p className="visit-card__note">{t(lang, "donateGoods.hoursNote")}</p>
          {mapHref && (
            <a className="btn btn-ghost" href={mapHref} target="_blank" rel="noopener noreferrer">
              {t(lang, "contact.openMap")} <span aria-hidden="true">↗</span>
            </a>
          )}
        </div>

        <div className="visit-card visit-card--hours">
          <span className="kicker kicker--muted">{t(lang, "donateGoods.notesEyebrow")}</span>
          <h2 id="goods-notes-title" className="goods-notes__title">
            {t(lang, "donateGoods.notesTitle")}
          </h2>
          <ul className="goods-notes">
            {notes.map((n) => <li key={n}>{t(lang, `donateGoods.note${n}`)}</li>)}
          </ul>
        </div>
      </section>

      <section className="shell goods-cta">
        <div className="cta-band">
          <div>
            <h2>{t(lang, "donateGoods.ctaTitle")}</h2>
            <p className="goods-cta__lead">{t(lang, "donateGoods.ctaLead")}</p>
          </div>
          {waHref ? (
            <a className="btn btn-on-accent" href={waHref} target="_blank" rel="noopener noreferrer">
              {t(lang, "donateGoods.ctaButton")} <span aria-hidden="true">→</span>
            </a>
          ) : (
            <Link className="btn btn-on-accent" to={`/${lang}/contact`}>
              {t(lang, "nav.contact")} <span aria-hidden="true">→</span>
            </Link>
          )}
        </div>

        <p className="goods-cta__alt">
          {t(lang, "donateGoods.otherWays")}{" "}
          <Link to={`/${lang}/volunteer`} className="btn-text">
            {t(lang, "donateGoods.otherWaysLink")} <span aria-hidden="true">→</span>
          </Link>
        </p>
      </section>
    </div>
  );
}
