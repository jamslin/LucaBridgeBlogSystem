// Config-driven navigation tree, per design pass 1's IA: About / Services / News /
// Donate / Recruitment / Contact. Labels are i18n keys under `nav.*`; paths are
// lang-agnostic and get the /:lang prefix applied by Nav/Footer.
//
// "recruit" is the one item with a length-sensitive label: past 6 top-level items
// (this tree plus Contact) the spec calls for "Recruitment" to abbreviate to "Join
// us" — see recruitLabelKey() below, computed rather than hardcoded so a future
// 7th item trips it automatically instead of needing a manual relabel.
export const NAV = [
  { key: "about", to: "/about" },
  { key: "services", to: "/services" },
  { key: "news", to: "/blog", children: [
    { key: "newsRecent", to: "/blog" },
    { key: "newsEvents", to: "/events" },
  ] },
  { key: "donate", to: "/donate" },
  { key: "recruit", to: "/careers", children: [
    { key: "recruitVolunteer", to: "/volunteer" },
    { key: "recruitJobs", to: "/careers" },
  ] },
];

// +1 for Contact, which is rendered separately (utility bar + its own nav slot),
// not as a NAV entry — see Nav.jsx.
const TOP_LEVEL_COUNT = NAV.length + 1;

export function recruitLabelKey() {
  return TOP_LEVEL_COUNT > 6 ? "nav.recruitShort" : "nav.recruit";
}
