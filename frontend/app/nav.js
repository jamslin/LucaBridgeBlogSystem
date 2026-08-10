// Config-driven navigation tree (Caritas-style hierarchy).
// Labels are i18n keys under `nav.*`; paths are lang-agnostic and get the
// /:lang prefix applied by the Nav/Footer components. Reordering the site menu
// is a data edit here — no component changes needed.
export const NAV = [
  { key: "about", to: "/p/about", children: [
    { key: "aboutBackground", to: "/p/about-background" },
    { key: "aboutGoals",      to: "/p/about-goals" },
    { key: "aboutStructure",  to: "/p/about-structure" },
    { key: "aboutCommittee",  to: "/p/about-committee" },
  ] },
  { key: "services", to: "/p/services" },
  { key: "news", to: "/blog", children: [
    { key: "newsRecent", to: "/blog" },
    { key: "newsInfo",   to: "/events" },
  ] },
  { key: "donate", to: "/p/donate", children: [
    { key: "donateMethods",   to: "/p/donate" },
    { key: "donateMaterials", to: "/p/donate-materials" },
  ] },
  { key: "recruit", to: "/p/volunteer", children: [
    { key: "recruitVolunteer", to: "/p/volunteer" },
    { key: "recruitJobs",      to: "/careers" },
  ] },
];
