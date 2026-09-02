import { api, siteOrigin } from "../lib/api.server";
import { SUPPORTED_LANGS, DEFAULT_LANG } from "../i18n";

// SSR resource route: the sitemap is generated from the live DB on request, so it can
// never go stale.
//
// Two things this has to get right, both of which bit us once the 73-post archive landed:
//
//  1. PAGINATION. The API returns a Spring Page and the previous version asked for a single
//     page of 50. With 72 published posts that silently hid 22 of them from Google — no
//     error, just missing URLs. Always walk to the last page.
//
//  2. hreflang ALTERNATES. Each post exists at three URLs, one per language. Without
//     <xhtml:link> tying them together Google sees three unrelated pages competing with
//     each other instead of one page in three languages. The tags must carry real BCP-47
//     tags (zh-Hant / en / zh-Hans) — Google silently ignores invalid ones, so an internal
//     token like "tc" would look fine and do nothing.

const XML_ESCAPES = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&apos;" };
const esc = (s) => String(s).replace(/[&<>"']/g, (c) => XML_ESCAPES[c]);

/** Walk every page of a Spring Page endpoint. `cap` is a guard against an endless loop. */
async function fetchAll(fetchPage, { size = 100, cap = 50 } = {}) {
  const out = [];
  for (let page = 0; page < cap; page += 1) {
    let res;
    try {
      res = await fetchPage(page, size);
    } catch {
      break; // a failing section must not take the whole sitemap down
    }
    const items = res?.content ?? [];
    out.push(...items);
    const totalPages = res?.totalPages ?? 1;
    if (res?.last === true || page + 1 >= totalPages || items.length === 0) break;
  }
  return out;
}

/**
 * One <url> per language, each carrying the full alternate set plus x-default.
 * x-default points at the base language, which is what a searcher with no matching
 * locale should land on.
 */
function urlEntries(origin, pathFor, lastmod) {
  const alternates = SUPPORTED_LANGS.map(
    (l) => `    <xhtml:link rel="alternate" hreflang="${l}" href="${esc(origin + pathFor(l))}"/>`
  ).join("\n");
  const xDefault =
    `    <xhtml:link rel="alternate" hreflang="x-default" href="${esc(origin + pathFor(DEFAULT_LANG))}"/>`;
  const mod = lastmod ? `\n    <lastmod>${esc(String(lastmod).slice(0, 10))}</lastmod>` : "";

  return SUPPORTED_LANGS.map(
    (l) => `  <url>
    <loc>${esc(origin + pathFor(l))}</loc>${mod}
${alternates}
${xDefault}
  </url>`
  );
}

export async function loader({ request }) {
  const origin = siteOrigin(request);

  const [posts, events, jobs] = await Promise.all([
    fetchAll((page, size) => api.getBlogList({ page, size })),
    fetchAll((page, size) => api.getEvents({ page, size })),
    fetchAll((page, size) => api.getJobs({ page, size })),
  ]);

  const staticPaths = [
    "", "/blog", "/events", "/careers", "/services",
    "/about", "/donate", "/volunteer", "/contact",
  ];

  const urls = [
    ...staticPaths.flatMap((p) => urlEntries(origin, (l) => `/${l}${p}`, null)),
    ...posts.flatMap((p) =>
      urlEntries(origin, (l) => `/${l}/blog/${encodeURIComponent(p.slug)}`, p.publishedAt)),
    ...events.flatMap((e) =>
      urlEntries(origin, (l) => `/${l}/events/${encodeURIComponent(e.slug)}`, e.publishedAt)),
    ...jobs.flatMap((j) =>
      urlEntries(origin, (l) => `/${l}/careers/${encodeURIComponent(j.slug)}`, j.publishedAt)),
  ];

  const xml = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
        xmlns:xhtml="http://www.w3.org/1999/xhtml">
${urls.join("\n")}
</urlset>`;

  return new Response(xml, {
    headers: { "Content-Type": "application/xml", "Cache-Control": "public, max-age=3600" },
  });
}
