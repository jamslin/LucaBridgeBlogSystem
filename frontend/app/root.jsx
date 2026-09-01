import {
  isRouteErrorResponse,
  Links,
  Meta,
  Outlet,
  Scripts,
  ScrollRestoration,
  useParams,
  useRouteError,
} from "react-router";

import { DEFAULT_LANG, SUPPORTED_LANGS, t } from "./i18n";
import globalStylesHref from "./theme/global.css?url";

// Latin display (Spectral) and the letterspaced label mono are shared by every
// language. The CJK faces are language-scoped in Layout below: Noto *TC* covers
// Traditional and Noto *SC* covers Simplified, so each page loads only the pair
// it needs while still satisfying the brief's requirement that the type has full
// coverage in both scripts.
export const links = () => [
  { rel: "preconnect", href: "https://fonts.googleapis.com" },
  { rel: "preconnect", href: "https://fonts.gstatic.com", crossOrigin: "anonymous" },
  {
    rel: "stylesheet",
    href: "https://fonts.googleapis.com/css2?family=Spectral:ital,wght@0,400;0,600;0,700;1,400&family=Noto+Sans+Mono:wght@400;500&display=swap",
  },
  { rel: "stylesheet", href: globalStylesHref },
  // Served from public/. SVG covers every current browser; a PNG fallback can be
  // added for older Safari if it ever matters.
  { rel: "icon", href: "/favicon.svg", type: "image/svg+xml" },
];

const CJK_FONT_HREF = {
  "zh-Hant":
    "https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@400;500;700&family=Noto+Serif+TC:wght@600;700&display=swap",
  "zh-Hans":
    "https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;700&family=Noto+Serif+SC:wght@600;700&display=swap",
  // English pages still render the 樂橋 wordmark and the Chinese strapline.
  en:
    "https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@400;500;700&family=Noto+Serif+TC:wght@600;700&display=swap",
};

export function Layout({ children }) {
  const params = useParams();
  const lang = SUPPORTED_LANGS.includes(params.lang) ? params.lang : DEFAULT_LANG;

  return (
    <html lang={lang}>
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <meta name="theme-color" content="#ac312a" />
        <Meta />
        <Links />
        <link rel="stylesheet" href={CJK_FONT_HREF[lang] ?? CJK_FONT_HREF[DEFAULT_LANG]} />
      </head>
      <body>
        {children}
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  );
}

export default function App() {
  return <Outlet />;
}

export function ErrorBoundary() {
  const error = useRouteError();
  const params = useParams();
  const lang = SUPPORTED_LANGS.includes(params.lang) ? params.lang : DEFAULT_LANG;
  const is404 = isRouteErrorResponse(error) && error.status === 404;

  return (
    <div className="shell" style={{ padding: "60px 20px", textAlign: "center" }}>
      <h1>{is404 ? t(lang, "post.notFound") : t(lang, "common.error")}</h1>
      <a href={`/${lang}`}>{t(lang, "common.backHome")}</a>
    </div>
  );
}
