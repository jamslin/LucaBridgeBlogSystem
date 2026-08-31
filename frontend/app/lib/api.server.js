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

async function post(path, body) {
  const res = await fetch(API_URL + path, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify(body),
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const message = (data && data.message) || res.statusText || "Request failed";
    const err = new Error(message);
    err.status = res.status;
    throw err;
  }
  return data;
}

export const api = {
  // The composite home-page call — see backend HomeController for why this exists
  // alongside the five endpoints below rather than replacing them.
  getHomePage: (lang) => get("/api/home", { lang }),

  getBlogList: ({ lang, page = 0, size = 10 } = {}) => get("/api/blog", { lang, page, size }),
  getBlogPost: (slug, lang) => get(`/api/blog/${encodeURIComponent(slug)}`, { lang }),

  getEvents: ({ lang, page = 0, size = 20 } = {}) => get("/api/events", { lang, page, size }),
  getEvent: (slug, lang) => get(`/api/events/${encodeURIComponent(slug)}`, { lang }),
  registerForEvent: (eventId, payload) => post(`/api/events/${eventId}/registrations`, payload),

  getJobs: ({ lang, page = 0, size = 20 } = {}) => get("/api/jobs", { lang, page, size }),
  getJob: (slug, lang) => get(`/api/jobs/${encodeURIComponent(slug)}`, { lang }),

  getServices: (lang) => get("/api/services", { lang }),
  getCompany: (lang) => get("/api/company", { lang }),
  getReferralGroups: (lang) => get("/api/referral-groups", { lang }),
};

/** Public origin for absolute og:/canonical URLs (behind nginx, request.url is internal). */
export function siteOrigin(request) {
  return process.env.SITE_ORIGIN || new URL(request.url).origin;
}
