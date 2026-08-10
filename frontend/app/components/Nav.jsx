import { useState } from "react";
import { Link, useParams } from "react-router";

import { t } from "../i18n";
import { NAV } from "../nav";
import LanguageSwitch from "./LanguageSwitch";

// Primary navigation. Desktop (>=1024px): horizontal bar; items with children
// reveal a dropdown on hover/focus-within (pure CSS, works pre-hydration).
// Mobile: a hamburger toggles a stacked menu, with per-section disclosure.
export default function Nav() {
  const { lang } = useParams();
  const [openMobile, setOpenMobile] = useState(false);
  const [openSub, setOpenSub] = useState(null);
  const L = (p) => `/${lang}${p}`;
  const closeAll = () => { setOpenMobile(false); setOpenSub(null); };

  return (
    <nav className="site-nav" aria-label="Primary">
      <div className="shell site-nav__bar">
        <button
          type="button"
          className="site-nav__toggle"
          aria-expanded={openMobile}
          aria-controls="primary-menu"
          onClick={() => setOpenMobile((v) => !v)}
        >
          <span className="site-nav__bars" aria-hidden="true"><span /><span /><span /></span>
          {t(lang, "nav.menu")}
        </button>

        <ul id="primary-menu" className="site-nav__list" data-open={openMobile ? "true" : "false"}>
          <li className="site-nav__item">
            <Link to={L("")} className="site-nav__link" onClick={closeAll}>{t(lang, "nav.home")}</Link>
          </li>

          {NAV.map((item) => (
            <li key={item.key} className={`site-nav__item${item.children ? " has-children" : ""}`}>
              <Link to={L(item.to)} className="site-nav__link" onClick={item.children ? undefined : closeAll}>
                {t(lang, `nav.${item.key}`)}
              </Link>

              {item.children && (
                <>
                  <button
                    type="button"
                    className="site-nav__expand"
                    aria-label={t(lang, `nav.${item.key}`)}
                    aria-expanded={openSub === item.key}
                    onClick={() => setOpenSub((k) => (k === item.key ? null : item.key))}
                  >
                    <span aria-hidden="true">▾</span>
                  </button>
                  <ul className="site-nav__submenu" data-open={openSub === item.key ? "true" : "false"}>
                    {item.children.map((c) => (
                      <li key={c.key}>
                        <Link to={L(c.to)} className="site-nav__sublink" onClick={closeAll}>
                          {t(lang, `nav.${c.key}`)}
                        </Link>
                      </li>
                    ))}
                  </ul>
                </>
              )}
            </li>
          ))}

          <li className="site-nav__item">
            <Link to={L("/p/contact")} className="site-nav__link" onClick={closeAll}>{t(lang, "nav.contact")}</Link>
          </li>
        </ul>

        <div className="site-nav__actions">
          <Link to={L("/p/donate")} className="btn btn-primary">{t(lang, "nav.donateCta")}</Link>
          <LanguageSwitch />
        </div>
      </div>
    </nav>
  );
}
