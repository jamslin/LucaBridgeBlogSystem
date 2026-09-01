import { Outlet, redirect, useLocation, useParams } from "react-router";

import { DEFAULT_LANG, SUPPORTED_LANGS } from "../i18n";
import { api } from "../lib/api.server";
import Masthead from "../components/Masthead";
import Nav from "../components/Nav";
import Footer from "../components/Footer";

// Lang validation lives in the loader (server-side redirect, no hooks-order
// pitfalls). Also loads the company record once for the whole shell so the
// masthead and footer can show contact + social links. Failure is non-fatal —
// the chrome still renders without it.
export async function loader({ params }) {
  if (!SUPPORTED_LANGS.includes(params.lang)) {
    return redirect(`/${DEFAULT_LANG}`, 302);
  }
  let company = {};
  try {
    company = await api.getCompany(params.lang);
  } catch {
    company = {};
  }
  const currentYear = new Date(Date.now() + 8 * 60 * 60 * 1000).getUTCFullYear();
  return { company, currentYear };
}

export default function LangLayout() {
  const { lang } = useParams();
  const location = useLocation();

  // The home page's hero runs edge to edge and starts at the very top, with the
  // utility bar and nav floating over it as glass. Every other page keeps the
  // header in normal flow, so the chrome sits on paper as usual. Detected from
  // the path rather than with :has() so it works in every browser and is
  // obvious to the next reader.
  const isHome = location.pathname === `/${lang}` || location.pathname === `/${lang}/`;

  return (
    <>
      <div className={`site-top${isHome ? " site-top--overlay" : ""}`}>
        <Masthead />
        <Nav />
      </div>
      <main className={isHome ? "has-full-bleed-hero" : undefined}>
        <Outlet />
      </main>
      <Footer />
    </>
  );
}
