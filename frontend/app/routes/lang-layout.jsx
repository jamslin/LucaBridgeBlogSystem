import { Outlet, redirect } from "react-router";

import { DEFAULT_LANG, SUPPORTED_LANGS } from "../i18n";
import Masthead from "../components/Masthead";
import Nav from "../components/Nav";
import Footer from "../components/Footer";

// Lang validation lives in the loader (server-side redirect, no hooks-order
// pitfalls — this replaces the old conditional-render-before-useEffect bug).
export function loader({ params }) {
  if (!SUPPORTED_LANGS.includes(params.lang)) {
    return redirect(`/${DEFAULT_LANG}`, 302);
  }
  return null;
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
