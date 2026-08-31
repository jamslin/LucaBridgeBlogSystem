import { Outlet, redirect } from "react-router";

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
  return (
    <>
      <Masthead />
      <Nav />
      <main>
        <Outlet />
      </main>
      <Footer />
    </>
  );
}
