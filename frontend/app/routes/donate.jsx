import { Link, useParams, useRouteLoaderData } from "react-router";

import { t } from "../i18n";

export function meta({ params }) {
  const title = `${t(params.lang, "donate.title")} — 樂橋 LucaBridge`;
  return [
    { title },
    { name: "description", content: t(params.lang, "donate.lead") },
    { property: "og:title", content: title },
  ];
}

/**
 * 捐款 / Donate (money) — the parent tab's own landing page, restored at the
 * user's request as a sibling of 物資捐贈 (/donate/goods) rather than being
 * replaced by it, per mockup 8a's original two-child menu (捐款方法 + 物資捐贈).
 *
 * The Company record carries no bank-details or payment-method field, so this
 * stays an honest placeholder rather than inventing figures — the same content
 * the site shipped with before, restyled to the current design system, with a
 * direct route to the goods page and to a person, so a donor with cash in hand
 * right now is not left with nothing to do.
 */
export default function Donate() {
  const { lang } = useParams();
  const layoutData = useRouteLoaderData("routes/lang-layout");
  const company = layoutData?.company ?? {};

  return (
    <div className="goods-page">
      <header className="shell goods-hero">
        <span className="kicker">{t(lang, "donate.eyebrow")}</span>
        <h1>{t(lang, "donate.title")}</h1>
        <p className="goods-hero__lead">{t(lang, "donate.lead")}</p>
      </header>

      <section className="shell goods-visit" aria-label={t(lang, "donate.title")}>
        <div className="visit-card">
          <span className="kicker">{t(lang, "donate.title")}</span>
          <p className="visit-card__note">{t(lang, "donate.pending")}</p>
          {(company.email || company.phone) && (
            <p className="visit-card__hours">
              {company.email && <a href={`mailto:${company.email}`}>{company.email}</a>}
              {company.email && company.phone && " · "}
              {company.phone && <a href={`tel:${company.phone}`}>{company.phone}</a>}
            </p>
          )}
        </div>

        <div className="visit-card visit-card--hours">
          <span className="kicker kicker--muted">{t(lang, "donate.goodsCrossLabel")}</span>
          <p className="visit-card__hours">{t(lang, "donate.goodsCrossLead")}</p>
          <Link className="btn btn-ghost" to={`/${lang}/donate/goods`}>
            {t(lang, "nav.donateGoods")} <span aria-hidden="true">→</span>
          </Link>
        </div>
      </section>
    </div>
  );
}
