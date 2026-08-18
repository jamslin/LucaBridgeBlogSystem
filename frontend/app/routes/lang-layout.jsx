import { Outlet, redirect } from "react-router";

import { DEFAULT_LANG, SUPPORTED_LANGS } from "../i18n";
import { api } from "../lib/api.server";
import Masthead from "../components/Masthead";
import Nav from "../components/Nav";
import Footer from "../components/Footer";
import StickyDonate from "../components/StickyDonate";

// Lang validation lives in the loader (server-side redirect, no hooks-order
// pitfalls). Also loads site settings once for the whole shell so the masthead
// and footer can show contact + social links. Settings failure is non-fatal —
// the chrome still renders without them.
export async function loader({ params }) {
  if (!SUPPORTED_LANGS.includes(params.lang)) {
    return redirect(`/${DEFAULT_LANG}`, 302);
  }
  let settings = {};
  try {
    settings = await api.getSettings();
  } catch {
    settings = {};
  }
  const currentYear = new Date(Date.now() + 8 * 60 * 60 * 1000).getUTCFullYear();
  return { settings, currentYear };
}

export default function LangLayout() {
  return (
    <>
      <Masthead />
      <Nav />
      <main>
        <Outlet />
      </main>
      <Footer />
      <StickyDonate />
    </>
  );
}
