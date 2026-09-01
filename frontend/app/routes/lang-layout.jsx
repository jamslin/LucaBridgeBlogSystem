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

  // On the home page the hero runs edge to edge and starts behind the nav, so
  // the glass panel has a photograph to sit on. Detected from the path rather
  // than with :has() so it works in every browser and is obvious to read.
  //
  // Masthead and Nav are siblings in normal flow, not wrapped in a positioned
  // container: that is what lets the nav be `position: sticky`. The utility bar
  // scrolls away with the page — keeping both pinned would eat ~120px of a
  // phone screen permanently.
  const isHome = location.pathname === `/${lang}` || location.pathname === `/${lang}/`;

  return (
    <>
      <Masthead />
      <Nav overHero={isHome} />
      <main className={isHome ? "has-full-bleed-hero" : undefined}>
        <Outlet />
      </main>
      <Footer />
    </>
  );
}
