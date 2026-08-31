// Design tokens for LucaBridge — direction 2 "Deep Wine" (design pass 1, superseding
// the earlier "Warm Clay" v1.0 direction below).
// Source: design-pass-1 mockups (shell, mega-menu, blog detail, gallery). The exact
// hex values here are read off a compressed thumbnail export, not a Figma/token
// source of truth — treat as a close approximation pending the real design file.
// Keep global.css in sync — this file is the source of truth for JS-side logic.

export const colors = {
  // Core five
  paper: "#f7f3ec",       // Linen 亞麻 — page background
  ink: "#1c1712",         // Ink 墨黑 — text & footer (near-black, darker than v1.0)
  accent: "#8a2b2b",      // Deep Wine 酒紅 — primary action & links (was Warm Clay #b85c3c)
  gold: "#b08534",        // Ochre gold 金 — 1px rules & small accents only (never text)
  sand: "#ddd0bd",        // Sand 沙 — quiet section fills

  // Supporting tints
  cardSurface: "#fffdf8", // card surface
  photoIdle: "#e8c9be",   // photo / chip idle fill — dusty blush, matching the mockup's placeholder blocks
  accentHover: "#a13a3a", // wine hover
  accentPressed: "#6e2020", // wine pressed
  inkSoft: "#423c31",     // body muted
  captions: "#6f6657",    // captions
  muted: "#423c31",       // alias kept for existing components
  line: "rgba(28,23,18,.12)",
};

export const typography = {
  // Headlines — Noto Serif TC, eased to 600–700 so they read calm, not newspaper-loud.
  display: {
    fontFamily: "'Noto Serif TC', serif",
    h1: { fontWeight: 700, fontSize: "54px", lineHeight: 1.06 },
    h2: { fontWeight: 700, fontSize: "32px", lineHeight: 1.15 },
    h3: { fontWeight: 600, fontSize: "20px", lineHeight: 1.3 },
  },
  body: {
    fontFamily: "'Noto Sans TC', sans-serif",
    fontWeight: 400,
    fontSize: "16px",
    lineHeight: 1.8,
  },
  small: {
    fontFamily: "'Noto Sans TC', sans-serif",
    fontWeight: 400,
    fontSize: "13px",
    lineHeight: 1.6,
  },
  // Eyebrow / labels — Spectral, small caps, wide tracking.
  eyebrow: {
    fontFamily: "'Spectral', serif",
    fontWeight: 600,
    fontSize: "11px",
    letterSpacing: "0.24em",
    textTransform: "uppercase",
  },
};

// 4-point rhythm — all spacing derives from a 4px base.
export const spacing = [4, 8, 12, 16, 24, 32, 48, 64];

export const radius = {
  chip: "4px",   // chips · inputs · buttons
  photo: "6px",  // photos
  card: "8px",   // cards
  pill: "999px", // filter tags
};

export const elevation = {
  flat: "none",                       // border only
  card: "0 2px 8px rgba(33,28,21,.06)", // soft 6%
  overlay: "0 12px 40px rgba(33,28,21,.18)", // modal
};

export const layout = {
  shellMaxWidth: "1120px",
  readingColumnWidth: "680px",
  gridColumns: 12,
  gutter: "24px",
  sectionPaddingDesktop: "40px",
  sectionPaddingTablet: "32px",
  sectionPaddingMobile: "20px",
};

export const breakpoints = {
  desktop: "1024px", // ≥1024 full 3-up grid, horizontal nav
  tablet: "640px",   // 640–1023 2-up, nav visible, 32px padding
  // <640 mobile: single column, hamburger, sticky donate, 20px gutters
};

// Motion — quiet and quick; supports understanding, never performs.
export const motion = {
  micro: { duration: "120ms", easing: "ease-out" },                 // hover, press, focus
  standard: { duration: "220ms", easing: "cubic-bezier(.4,0,.2,1)" }, // filters, tabs, disclosure
  enter: { duration: "320ms", easing: "ease-out" },                 // page & lightbox fade + 8px rise
  // Honour prefers-reduced-motion: fades only.
};

export const categoryKeys = ["poverty-relief", "environment", "campus", "volunteering"];

export const supportedLangs = ["zh-Hant", "en", "zh-Hans"];
export const defaultLang = "zh-Hant";
