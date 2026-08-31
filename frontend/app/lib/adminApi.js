// Client-side API for the admin CMS. Runs ONLY in the browser (admin routes are
// client-only), attaches the bearer token, and normalises errors.
//
// In dev the SPA is served by Vite on :5173 while the API is on :8080, so we
// point at localhost:8080 (CORS already allows it). In prod the frontend and
// API share an origin behind nginx / the ingress, so same-origin "/api/..".
const API_BASE = (typeof window !== "undefined" && (window.location.port === "3000" || window.location.port === "5173"))
  ? "http://localhost:8080"
  : (import.meta.env.DEV ? "http://localhost:8080" : "");
const TOKEN_KEY = "lb_admin_token";

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

export function getToken() {
  return typeof window !== "undefined" ? window.localStorage.getItem(TOKEN_KEY) : null;
}
export function setToken(token) {
  if (typeof window !== "undefined") window.localStorage.setItem(TOKEN_KEY, token);
}
export function clearToken() {
  if (typeof window !== "undefined") window.localStorage.removeItem(TOKEN_KEY);
}

async function request(path, { method = "GET", body, form } = {}) {
  const headers = {};
  const token = getToken();
  if (token) headers.Authorization = "Bearer " + token;

  let payload;
  if (form) {
    payload = form; // FormData — let the browser set the multipart boundary
  } else if (body !== undefined) {
    headers["Content-Type"] = "application/json";
    payload = JSON.stringify(body);
  }

  const res = await fetch(API_BASE + path, { method, headers, body: payload });

  if (res.status === 401) {
    clearToken();
    const isLogin = path.endsWith("/auth/login");
    if (!isLogin && typeof window !== "undefined") {
      window.location.assign("/admin/login");
    }
    throw new ApiError(isLogin ? "Invalid credentials" : "Session expired — please sign in again", 401);
  }

  if (!res.ok) {
    let message = res.statusText || "Request failed";
    if (res.status === 403) message = "You don't have permission for this action";
    try {
      const data = await res.json();
      if (data && data.message) message = data.message;
    } catch (_) { /* non-JSON error body */ }
    throw new ApiError(message, res.status);
  }

  if (res.status === 204) return null;
  const contentType = res.headers.get("content-type") || "";
  return contentType.includes("application/json") ? res.json() : res.text();
}

export const adminApi = {
  // auth
  login: (username, password) =>
    request("/api/auth/login", { method: "POST", body: { username, password } }),
  me: () => request("/api/auth/me"),

  // blog
  listBlog: () => request("/api/admin/blog?size=200").then((p) => p.content),
  getBlog: (id) => request(`/api/admin/blog/${id}`),
  createBlog: (payload) => request("/api/admin/blog", { method: "POST", body: payload }),
  updateBlog: (id, payload) => request(`/api/admin/blog/${id}`, { method: "PUT", body: payload }),
  deleteBlog: (id) => request(`/api/admin/blog/${id}`, { method: "DELETE" }),

  // events
  listEvents: () => request("/api/admin/events?size=200").then((p) => p.content),
  getEvent: (id) => request(`/api/admin/events/${id}`),
  createEvent: (payload) => request("/api/admin/events", { method: "POST", body: payload }),
  updateEvent: (id, payload) => request(`/api/admin/events/${id}`, { method: "PUT", body: payload }),
  deleteEvent: (id) => request(`/api/admin/events/${id}`, { method: "DELETE" }),

  // event registrations — ADMIN only
  listRegistrations: (eventId) => request(`/api/admin/registrations/events/${eventId}?size=500`).then((p) => p.content),
  // A plain <a href> can't carry the bearer token, so this fetches with auth and hands back a
  // Blob the caller turns into a temporary download link.
  exportRegistrationsCsv: async (eventId) => {
    const token = getToken();
    const res = await fetch(`${API_BASE}/api/admin/registrations/events/${eventId}/export`, {
      headers: token ? { Authorization: "Bearer " + token } : {},
    });
    if (!res.ok) throw new ApiError("Export failed", res.status);
    return res.blob();
  },

  // jobs
  listJobs: () => request("/api/admin/jobs?size=200").then((p) => p.content),
  getJob: (id) => request(`/api/admin/jobs/${id}`),
  createJob: (payload) => request("/api/admin/jobs", { method: "POST", body: payload }),
  updateJob: (id, payload) => request(`/api/admin/jobs/${id}`, { method: "PUT", body: payload }),
  deleteJob: (id) => request(`/api/admin/jobs/${id}`, { method: "DELETE" }),

  // services (taxonomy)
  listServices: () => request("/api/admin/services"),
  getService: (id) => request(`/api/admin/services/${id}`),
  serviceUsage: (id) => request(`/api/admin/services/${id}/usage`),
  createService: (payload) => request("/api/admin/services", { method: "POST", body: payload }),
  updateService: (id, payload) => request(`/api/admin/services/${id}`, { method: "PUT", body: payload }),
  deleteService: (id, confirm) => request(`/api/admin/services/${id}${confirm ? "?confirm=true" : ""}`, { method: "DELETE" }),

  // company (singleton)
  getCompany: () => request("/api/admin/company"),
  saveCompany: (payload) => request("/api/admin/company", { method: "PUT", body: payload }),

  // home blocks
  listHomeBlocks: () => request("/api/admin/home-blocks"),
  getHomeBlock: (id) => request(`/api/admin/home-blocks/${id}`),
  createHomeBlock: (payload) => request("/api/admin/home-blocks", { method: "POST", body: payload }),
  updateHomeBlock: (id, payload) => request(`/api/admin/home-blocks/${id}`, { method: "PUT", body: payload }),
  deleteHomeBlock: (id) => request(`/api/admin/home-blocks/${id}`, { method: "DELETE" }),

  // referral groups
  listReferralGroups: () => request("/api/admin/referral-groups"),
  getReferralGroup: (id) => request(`/api/admin/referral-groups/${id}`),
  referralGroupUsage: (id) => request(`/api/admin/referral-groups/${id}/usage`),
  createReferralGroup: (payload) => request("/api/admin/referral-groups", { method: "POST", body: payload }),
  updateReferralGroup: (id, payload) => request(`/api/admin/referral-groups/${id}`, { method: "PUT", body: payload }),
  deleteReferralGroup: (id, confirm) => request(`/api/admin/referral-groups/${id}${confirm ? "?confirm=true" : ""}`, { method: "DELETE" }),

  // media
  listMedia: () => request("/api/admin/media"),
  uploadMedia: (file) => {
    const fd = new FormData();
    fd.append("file", file);
    return request("/api/admin/media", { method: "POST", form: fd });
  },
  updateMediaAlt: (id, altText) => request(`/api/admin/media/${id}`, { method: "PUT", body: { altText } }),
  deleteMedia: (id) => request(`/api/admin/media/${id}`, { method: "DELETE" }),
  mediaSweepPreview: () => request("/api/admin/media/sweep-preview"),
  mediaSweep: () => request("/api/admin/media/sweep?confirm=true", { method: "POST" }),

  // users
  listUsers: () => request("/api/admin/users"),
  createUser: (payload) => request("/api/admin/users", { method: "POST", body: payload }),
  updateUser: (id, payload) => request(`/api/admin/users/${id}`, { method: "PUT", body: payload }),
  changeUserPassword: (id, newPassword) =>
    request(`/api/admin/users/${id}/password`, { method: "PUT", body: { newPassword } }),
  deleteUser: (id) => request(`/api/admin/users/${id}`, { method: "DELETE" }),
};
