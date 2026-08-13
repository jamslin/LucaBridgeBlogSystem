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

  // reference data
  categories: () => request("/api/categories"),

  // posts
  listPosts: () => request("/api/admin/posts"),
  getPost: (id) => request(`/api/admin/posts/${id}`),
  savePost: (payload) => request("/api/admin/posts", { method: "POST", body: payload }),
  publishPost: (id) => request(`/api/admin/posts/${id}/publish`, { method: "POST" }),
  unpublishPost: (id) => request(`/api/admin/posts/${id}/unpublish`, { method: "POST" }),
  deletePost: (id) => request(`/api/admin/posts/${id}`, { method: "DELETE" }),

  // media
  listMedia: () => request("/api/admin/media"),
  uploadMedia: (file) => {
    const fd = new FormData();
    fd.append("file", file);
    return request("/api/admin/media", { method: "POST", form: fd });
  },
  updateMedia: (id, altText) => request(`/api/admin/media/${id}`, { method: "PUT", body: { altText } }),
  deleteMedia: (id) => request(`/api/admin/media/${id}`, { method: "DELETE" }),
  syncMedia: () => request("/api/admin/media/sync", { method: "POST" }),

  // settings
  getSettings: () => request("/api/settings"),
  saveSettings: (map) => request("/api/admin/settings", { method: "PUT", body: map }),

  // users
  listUsers: () => request("/api/admin/users"),
  createUser: (payload) => request("/api/admin/users", { method: "POST", body: payload }),
  updateUser: (id, payload) => request(`/api/admin/users/${id}`, { method: "PUT", body: payload }),
  changeUserPassword: (id, newPassword) =>
    request(`/api/admin/users/${id}/password`, { method: "PUT", body: { newPassword } }),
  deleteUser: (id) => request(`/api/admin/users/${id}`, { method: "DELETE" }),

  // events
  listEvents: () => request("/api/admin/events"),
  getEvent: (id) => request(`/api/admin/events/${id}`),
  saveEvent: (payload) => request("/api/admin/events", { method: "POST", body: payload }),
  publishEvent: (id) => request(`/api/admin/events/${id}/publish`, { method: "POST" }),
  unpublishEvent: (id) => request(`/api/admin/events/${id}/unpublish`, { method: "POST" }),
  deleteEvent: (id) => request(`/api/admin/events/${id}`, { method: "DELETE" }),

  // jobs
  listJobs: () => request("/api/admin/jobs"),
  getJob: (id) => request(`/api/admin/jobs/${id}`),
  saveJob: (payload) => request("/api/admin/jobs", { method: "POST", body: payload }),
  publishJob: (id) => request(`/api/admin/jobs/${id}/publish`, { method: "POST" }),
  unpublishJob: (id) => request(`/api/admin/jobs/${id}/unpublish`, { method: "POST" }),
  deleteJob: (id) => request(`/api/admin/jobs/${id}`, { method: "DELETE" }),

  // pages
  listPages: () => request("/api/admin/pages"),
  getPage: (id) => request(`/api/admin/pages/${id}`),
  savePage: (payload) => request("/api/admin/pages", { method: "POST", body: payload }),
  publishPage: (id) => request(`/api/admin/pages/${id}/publish`, { method: "POST" }),
  unpublishPage: (id) => request(`/api/admin/pages/${id}/unpublish`, { method: "POST" }),
  deletePage: (id) => request(`/api/admin/pages/${id}`, { method: "DELETE" }),

  // categories (admin)
  listCategoriesAdmin: () => request("/api/admin/categories"),
  saveCategory: (payload) => request("/api/admin/categories", { method: "POST", body: payload }),
  deleteCategory: (id) => request(`/api/admin/categories/${id}`, { method: "DELETE" }),

  // homepage banners
  listBanners: () => request("/api/admin/banners"),
  saveBanner: (payload) => request("/api/admin/banners", { method: "POST", body: payload }),
  deleteBanner: (id) => request(`/api/admin/banners/${id}`, { method: "DELETE" }),
};
