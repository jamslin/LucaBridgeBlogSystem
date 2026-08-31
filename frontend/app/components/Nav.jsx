import { useEffect, useState } from "react";
import { Link, useLocation, useParams, useRouteLoaderData } from "react-router";

import { t } from "../i18n";
import { NAV, recruitLabelKey } from "../nav";
import LanguageSwitch from "./LanguageSwitch";
import SocialLinks from "./SocialLinks";

// Primary navigation (mockups 8a/8b/8c).
//
// Desktop: a floating pill bar carrying the wordmark, the six top-level items
// and one CTA. Items with children open a mega-menu on hover or keyboard focus —
// pure CSS via :hover/:focus-within, so it works before hydration and stays
// crawlable.
//
// Mobile: a hamburger opens a full-height drawer with one-at-a-time accordions
// and the CTA pinned at the bottom next to the language switch.
//
// The CTA points at volunteering, not donation. There is no online payment in
// this phase (design brief §3), so "Donate Now" would promise a checkout that
// does not exist; §5 is explicit that 成為義工 is the single primary conversion.
export default function Nav() {
  const { lang } = useParams();
  const location = useLocation();
  const layoutData = useRouteLoaderData("routes/lang-layout");
  const company = layoutData?.company ?? {};

  const [drawerOpen, setDrawerOpen] = useState(false);
  const [openSection, setOpenSection] = useState(null);

  const L = (path) => `/${lang}${path}`;
  const closeDrawer = () => { setDrawerOpen(false); setOpenSection(null); };

  // Any navigation closes the drawer, and Escape does too.
  useEffect(() => { closeDrawer(); }, [location.pathname]);
  useEffect(() => {
    if (!drawerOpen) return undefined;
    const onKey = (e) => { if (e.key === "Escape") closeDrawer(); };
    document.addEventListener("keydown", onKey);
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = "";
    };
  }, [drawerOpen]);

  const labelFor = (item) =>
    t(lang, item.key === "recruit" ? recruitLabelKey() : `nav.${item.key}`);

  const isActive = (item) => {
    const paths = [item.to, ...(item.children ?? []).map((c) => c.to)];
    return paths.some((p) => location.pathname === L(p) || location.pathname.startsWith(`${L(p)}/`));
  };

  const items = [...NAV, { key: "contact", to: "/contact" }];

  const brand = (
    <Link to={L("")} className="site-brand">
      <span className="site-brand__mark" aria-hidden="true">
        {company.logoUrl ? <img src={company.logoUrl} alt="" /> : null}
      </span>
      <span>
        <span className="site-brand__name">{lang === "en" ? "LucaBridge" : "樂橋"}</span>
        <span className="site-brand__sub">
          {lang === "en" ? "Charity · Hong Kong" : "LUCA BRIDGE"}
        </span>
      </span>
    </Link>
  );

  return (
    <header className="site-nav">
      <div className="shell">
        <nav className="site-nav__bar" aria-label="Primary">
          {brand}

          <ul className="site-nav__list">
            {items.map((item) => (
              <li
                key={item.key}
                className={`site-nav__item${isActive(item) ? " site-nav__item--active" : ""}`}
              >
                <Link to={L(item.to)} className="site-nav__link">
                  {labelFor(item)}
                  {item.children && <span className="site-nav__caret" aria-hidden="true">▾</span>}
                </Link>

                {item.children && (
                  <div className="mega">
                    <div
                      className="mega__cards"
                      style={{ "--mega-cols": Math.min(item.children.length, 4) }}
                    >
                      {item.children.map((child, index) => (
                        <Link key={child.key} to={L(child.to)} className="mega__card">
                          <span className="mega__card-index">
                            {String(index + 1).padStart(2, "0")}
                          </span>
                          <span className="mega__card-title">{t(lang, `nav.${child.key}`)}</span>
                        </Link>
                      ))}
                    </div>
                  </div>
                )}
              </li>
            ))}
          </ul>

          <div className="site-nav__actions">
            <Link to={L("/volunteer")} className="btn btn-primary site-nav__cta">
              {t(lang, "nav.volunteerCta")}
            </Link>
            <button
              type="button"
              className="site-nav__toggle"
              aria-expanded={drawerOpen}
              aria-controls="mobile-drawer"
              aria-label={t(lang, "nav.menu")}
              onClick={() => setDrawerOpen(true)}
            >
              <span className="site-nav__bars" aria-hidden="true" />
            </button>
          </div>
        </nav>
      </div>

      {/* Mobile drawer */}
      <div
        id="mobile-drawer"
        className="drawer"
        data-open={drawerOpen ? "true" : "false"}
        onClick={(e) => { if (e.target === e.currentTarget) closeDrawer(); }}
      >
        <div className="drawer__panel" role="dialog" aria-modal="true" aria-label={t(lang, "nav.menu")}>
          <div className="drawer__head">
            {brand}
            <button
              type="button"
              className="drawer__close"
              aria-label={t(lang, "nav.closeMenu")}
              onClick={closeDrawer}
            >
              <span aria-hidden="true">✕</span>
            </button>
          </div>

          {items.map((item) => (
            <div key={item.key} className="drawer__section">
              {item.children ? (
                <>
                  <button
                    type="button"
                    className="drawer__row"
                    aria-expanded={openSection === item.key}
                    onClick={() => setOpenSection((k) => (k === item.key ? null : item.key))}
                  >
                    {labelFor(item)}
                    <span className="drawer__row-caret" aria-hidden="true">▾</span>
                  </button>
                  {openSection === item.key && (
                    <ul className="drawer__sub">
                      {item.children.map((child) => (
                        <li key={child.key}>
                          <Link to={L(child.to)} onClick={closeDrawer}>
                            {t(lang, `nav.${child.key}`)}
                          </Link>
                        </li>
                      ))}
                    </ul>
                  )}
                </>
              ) : (
                <Link to={L(item.to)} className="drawer__row" onClick={closeDrawer}>
                  {labelFor(item)}
                </Link>
              )}
            </div>
          ))}

          <div className="drawer__foot">
            <Link to={L("/volunteer")} className="btn btn-primary" onClick={closeDrawer}>
              {t(lang, "nav.volunteerCta")}
            </Link>
            <div className="drawer__meta">
              <LanguageSwitch />
              <SocialLinks company={company} size={18} color="currentColor" />
            </div>
            <p className="drawer__contact">
              {company.phone && (
                <a href={`tel:${company.phone.replace(/\s+/g, "")}`}>{company.phone}</a>
              )}
              {company.phone && company.email ? " · " : null}
              {company.email && <a href={`mailto:${company.email}`}>{company.email}</a>}
            </p>
          </div>
        </div>
      </div>
    </header>
  );
}
