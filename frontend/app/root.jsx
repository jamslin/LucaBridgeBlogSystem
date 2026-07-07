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

export const links = () => [
  { rel: "preconnect", href: "https://fonts.googleapis.com" },
  { rel: "preconnect", href: "https://fonts.gstatic.com", crossOrigin: "anonymous" },
  {
    rel: "stylesheet",
    href: "https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@600;700&family=Noto+Sans+TC:wght@400;600&family=Spectral:ital,wght@0,400;0,600;1,400&display=swap",
  },
  { rel: "stylesheet", href: globalStylesHref },
];

export function Layout({ children }) {
  const params = useParams();
  const lang = SUPPORTED_LANGS.includes(params.lang) ? params.lang : DEFAULT_LANG;

  return (
    <html lang={lang}>
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <Meta />
        <Links />
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
