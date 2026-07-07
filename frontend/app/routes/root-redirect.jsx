import { redirect } from "react-router";
import { DEFAULT_LANG } from "../i18n";

export function loader() {
  return redirect(`/${DEFAULT_LANG}`, 302);
}
