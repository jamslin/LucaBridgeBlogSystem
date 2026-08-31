// Design tokens for LucaBridge — design pass 1, measured values.
//
// PROVENANCE: these hexes are sampled from the delivered mockup PDFs
// (LucaBridge_UI_Mockups.pdf and …Mockups2.pdf) rendered at 150 DPI and read per
// region, not eyeballed from a compressed thumbnail. That matters — the earlier
// "Deep Wine" reading put the accent at #8a2b2b, roughly a third darker and
// noticeably browner than the #c0302a the comps actually use for CTAs, and set
// the page ground to linen #f7f3ec rather than the warm off-white #fbf5f2.
//
// Two reds do real work in the comps and are not interchangeable:
//   #ac312a  the utility bar, a shade calmer so it recedes behind the nav
//   #c0302a  every solid CTA — reserved for the one live conversion
//   #a92721  the end of the support-band gradient only
//
// Keep global.css in sync — this file is the source of truth for JS-side logic.

export const colors = {
  // Core
  paper: "#fbf5f2",       // page background — warm off-white, not linen
  ink: "#2a1a18",         // text, footer, lightbox ground
  accent: "#c0302a",      // primary action — the ONLY solid CTA red
  accentBar: "#ac312a",   // utility bar
  accentDeep: "#a92721",  // support-band gradient end
  cream: "#fdf6ee",       // type and buttons reversed out of red
  tan: "#ede3d8",         // quiet section fill + contact quick-link
  gold: "#8a6421",        // small accents only, never body text

  // Surfaces
  cardSurface: "#ffffff",
  glassPanel: "#f7f2f1",  // mega-menu / nav panel
  photoIdle: "#dfc6bf",   // image placeholder + skeleton
  accentHover: "#cc3b34",
  accentPressed: "#a92721",
  inkSoft: "#4a3733",     // body
  muted: "#7a6660",       // captions, meta
  line: "rgba(42,26,24,.12)",

  // Solid fallbacks for the two glass surfaces (@supports not backdrop-filter).
  glassFallbackLight: "#fdf5f2",
  glassFallbackDark: "#3a221f",

  // Registration-state semantics (mockup 7c component sheet). The state decides
  // badge colour, CTA and whether a capacity bar shows — see GalleryLayout's
  // sibling, RegistrationState, on the backend.
  successInk: "#125c37",
  successFill: "#e7f1ea",
  waitInk: "#6f4f16",     // amber — full / waitlist
  waitFill: "#ece6db",
  neutralInk: "#5c514e",  // not yet open / closed
  neutralFill: "#eee7e4",
  tagInk: "#a8261f",      // service tag
  tagFill: "#f9eae9",
};

export const typography = {
  // Latin display comes from Spectral, CJK falls through to Noto Serif TC/SC.
  // Both Noto faces carry full Traditional AND Simplified coverage, which the
  // design brief §3 requires and which rules out most display faces.
  display: {
    fontFamily: "'Spectral', 'Noto Serif TC', 'Noto Serif SC', Georgia, serif",
    hero: { fontWeight: 700, fontSize: "clamp(40px, 6vw, 76px)", lineHeight: 1.05 },
    h1: { fontWeight: 700, fontSize: "clamp(32px, 4.4vw, 56px)", lineHeight: 1.1 },
    h2: { fontWeight: 700, fontSize: "clamp(26px, 3.2vw, 40px)", lineHeight: 1.18 },
    h3: { fontWeight: 600, fontSize: "22px", lineHeight: 1.3 },
  },
  body: {
    fontFamily: "'Noto Sans TC', 'Noto Sans SC', system-ui, sans-serif",
    fontWeight: 400,
    fontSize: "16px",
    lineHeight: 1.8, // CJK; Latin drops to 1.7
  },
  small: { fontSize: "13px", lineHeight: 1.6 },
  // Eyebrows, dates, phone numbers, the charity number — letterspaced mono in
  // the comps, not a small-caps serif.
  label: {
    fontFamily: "'Noto Sans Mono', ui-monospace, monospace",
    fontWeight: 500,
    fontSize: "12px",
    letterSpacing: "0.22em",
    textTransform: "uppercase",
  },
};

// 4-point rhythm — all spacing derives from a 4px base.
export const spacing = [4, 8, 12, 16, 24, 32, 48, 64, 96];

// The comps are consistently soft-cornered: every button and chip is a full
// pill, and cards sit around 16px rather than the 4-8px of the first reading.
export const radius = {
  pill: "999px", // buttons, chips, badges — the direction's signature
  photo: "12px",
  card: "16px",
  panel: "22px", // hero, mega-menu, support band
};

export const elevation = {
  flat: "none",
  card: "0 1px 2px rgba(42,26,24,.04), 0 8px 24px rgba(42,26,24,.06)",
  raised: "0 2px 6px rgba(42,26,24,.06), 0 18px 44px rgba(42,26,24,.10)",
  overlay: "0 24px 70px rgba(42,26,24,.35)",
};

export const layout = {
  shellMaxWidth: "1200px",
  // Latin sets narrower than CJK at the same size to hold 65-75 characters a
  // line — the comps call this out explicitly on the article page.
  readingColumnLatin: "680px",
  readingColumnCjk: "720px",
  gridColumns: 12,
  gutter: "24px",
  sectionPaddingDesktop: "40px",
  sectionPaddingTablet: "32px",
  sectionPaddingMobile: "20px",
};

export const breakpoints = {
  desktop: "1024px", // ≥1024 full 3-up grid, horizontal nav
  tablet: "640px",   // 640–1023 2-up, nav visible, 32px padding
  // <640 mobile: single column, drawer, 20px gutters
};

// Motion — quiet and quick; supports understanding, never performs.
export const motion = {
  micro: { duration: "120ms", easing: "ease-out" },
  standard: { duration: "220ms", easing: "cubic-bezier(.4,0,.2,1)" },
  enter: { duration: "320ms", easing: "ease-out" },
  // Honour prefers-reduced-motion: fades only.
};

// Photography (mockup 7e). Real volunteer photos are phone snaps at mixed sizes
// and light levels, so the design fixes the crop, warms the colour and corrects
// contrast — deliberately NOT a full duotone, which eats the colour of the
// hi-vis vests volunteers wear.
export const photo = {
  ratios: {
    hero: "10 / 9",   // arch crop, hero only
    card: "3 / 2",    // event cards
    cover: "16 / 10", // article/event covers and 3-up news cards
    square: "1 / 1",  // gallery grid
  },
  filter: "saturate(.92) contrast(1.04)",
  washRed: "rgba(192, 48, 42, .10)",
  washAmber: "rgba(138, 100, 33, .16)",
};

export const supportedLangs = ["zh-Hant", "en", "zh-Hans"];
export const defaultLang = "zh-Hant";
