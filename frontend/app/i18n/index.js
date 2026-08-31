// Lean trilingual dictionary — replaces i18next. The lang always comes from the
// URL (/:lang/...), so there is no client-side language state to manage, which
// also makes this trivially SSR-safe. Keys must mirror app.i18n.supported-langs
// in backend application.yml.
import zhHant from "./locales/zh-Hant.json";
import en from "./locales/en.json";
import zhHans from "./locales/zh-Hans.json";

const dictionaries = { "zh-Hant": zhHant, en, "zh-Hans": zhHans };

export const SUPPORTED_LANGS = ["zh-Hant", "en", "zh-Hans"];
export const DEFAULT_LANG = "zh-Hant";

/**
 * t("en", "nav.home") → "Home"; falls back to the default language, then the key.
 * Optional `vars` fill {placeholders}: t(lang, "gallery.count", { count: 15 }).
 */
export function t(lang, path, vars) {
  const lookup = (dict) => path.split(".").reduce((node, key) => node?.[key], dict);
  const value = lookup(dictionaries[lang]) ?? lookup(dictionaries[DEFAULT_LANG]) ?? path;
  if (!vars || typeof value !== "string") return value;
  return value.replace(/\{(\w+)\}/g, (match, key) => (key in vars ? String(vars[key]) : match));
}
