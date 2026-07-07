import { siteOrigin } from "../lib/api.server";

export function loader({ request }) {
  const body = `User-agent: *
Allow: /

Sitemap: ${siteOrigin(request)}/sitemap.xml
`;
  return new Response(body, {
    headers: { "Content-Type": "text/plain", "Cache-Control": "public, max-age=3600" },
  });
}
