import { useParams, useRouteLoaderData } from "react-router";

import { t } from "../i18n";

export function meta({ params }) {
  const title = `${t(params.lang, "about.title")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

/**
 * 關於我們 / About us.
 *
 * Comps 8a/8b give About a four-item mega-menu — 背景與發展, 目標與宗旨,
 * 組織架構, 委員名單 — but the v2 schema has no generic CMS page table, so
 * those four cannot be routes with editable bodies. They are sections of this
 * page instead, addressed by the anchors the menu links to. A reader clicking
 * 委員名單 lands on the committee heading either way; when a page table does
 * arrive, each section can be lifted into its own route without the navigation
 * changing shape.
 *
 * company.about is the only piece of this content the backend actually holds.
 * The rest says plainly that it is waiting on the organisation rather than
 * filling the space with invented history.
 */
export default function About() {
  const { lang } = useParams();
  const layoutData = useRouteLoaderData("routes/lang-layout");
  const company = layoutData?.company ?? {};

  // Anchor ids are the contract with nav.js — keep them in step.
  const sections = [
    { id: "background", titleKey: "nav.aboutBackground", bodyKey: "about.backgroundPending" },
    { id: "mission", titleKey: "nav.aboutMission", bodyKey: "about.missionPending" },
    { id: "structure", titleKey: "nav.aboutStructure", bodyKey: "about.structurePending" },
    { id: "committee", titleKey: "nav.aboutCommittee", bodyKey: "about.committeePending" },
  ];

  return (
    <div className="about-page">
      <header className="shell about-hero">
        <span className="kicker">{t(lang, "about.eyebrow")}</span>
        <h1>{t(lang, "about.title")}</h1>
        {company.about
          ? <p className="about-hero__lead">{company.about}</p>
          : <p className="about-hero__lead">{t(lang, "nav.aboutLead")}</p>}
      </header>

      <div className="shell about-sections">
        {sections.map((s, i) => (
          // scroll-margin-top on the section keeps the sticky nav from covering
          // the heading when the mega-menu link jumps here.
          <section key={s.id} id={s.id} className="about-section">
            <span className="about-section__num" aria-hidden="true">
              {String(i + 1).padStart(2, "0")}
            </span>
            <div>
              <h2>{t(lang, s.titleKey)}</h2>
              <p className="about-section__desc">{t(lang, `${s.titleKey}Desc`)}</p>
              <div className="pending-notice">{t(lang, s.bodyKey)}</div>
            </div>
          </section>
        ))}
      </div>
    </div>
  );
}
