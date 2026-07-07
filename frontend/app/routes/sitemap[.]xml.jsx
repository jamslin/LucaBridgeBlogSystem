import { api, siteOrigin } from "../lib/api.server";
import { SUPPORTED_LANGS } from "../i18n";

// SSR resource route: sitemap is generated from the live DB on request,
// so it can never go stale.
export async function loader({ request }) {
  const origin = siteOrigin(request);
  const posts = await api.getPosts({ size: 50 }); // grow via pagination when needed

  const staticPaths = SUPPORTED_LANGS.flatMap((l) => [`/${l}`, `/${l}/blog`]);
  const postPaths = posts.items.flatMap((p) =>
    SUPPORTED_LANGS.map((l) => `/${l}/blog/${p.slug}`)
  );

  const xml = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${[...staticPaths, ...postPaths].map((p) => `  <url><loc>${origin}${p}</loc></url>`).join("\n")}
</urlset>`;

  return new Response(xml, {
    headers: { "Content-Type": "application/xml", "Cache-Control": "public, max-age=3600" },
  });
}
