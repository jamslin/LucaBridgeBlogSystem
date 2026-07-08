// Server-only API client (the .server suffix guarantees it is never bundled
// for the browser). SSR loaders call the Spring API directly over the internal
// network — no CORS, no public API exposure needed for reads.
const API_URL = process.env.API_URL || "http://localhost:8080";

async function get(path, params = {}) {
  const url = new URL(API_URL + path);
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== "") {
      url.searchParams.set(key, value);
    }
  }

  const res = await fetch(url, { headers: { Accept: "application/json" } });

  if (res.status === 404) {
    throw new Response("Not Found", { status: 404 });
  }
  if (!res.ok) {
    throw new Response("Upstream API error", { status: 502 });
  }
  return res.json();
}

export const api = {
  getCategories: (lang) => get("/api/categories", { lang }),
  getPosts: ({ lang, category, page = 0, size = 10 } = {}) =>
    get("/api/posts", { lang, category, page, size }),
  getPost: (slug, lang) => get(`/api/posts/${encodeURIComponent(slug)}`, { lang }),

  getPages: (lang) => get("/api/pages", { lang }),
  getPage: (slug, lang) => get(`/api/pages/${encodeURIComponent(slug)}`, { lang }),
  getEvents: (lang) => get("/api/events", { lang }),
  getEvent: (slug, lang) => get(`/api/events/${encodeURIComponent(slug)}`, { lang }),
  getSettings: () => get("/api/settings"),
};

/** Public origin for absolute og:/canonical URLs (behind nginx, request.url is internal). */
export function siteOrigin(request) {
  return process.env.SITE_ORIGIN || new URL(request.url).origin;
}
