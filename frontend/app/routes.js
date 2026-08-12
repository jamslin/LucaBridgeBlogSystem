import { index, route } from "@react-router/dev/routes";

export default [
  // "/" → redirect to the default language (loader-only route, no UI)
  index("routes/root-redirect.jsx"),

  // /:lang layout — validates the lang segment, renders Masthead/Nav/Footer
  route(":lang", "routes/lang-layout.jsx", [
    index("routes/home.jsx"),
    route("blog", "routes/blog-index.jsx"),
    route("blog/:slug", "routes/post.jsx"),
    route("p/:slug", "routes/page.jsx"),
    route("events", "routes/events.jsx"),
    route("events/:slug", "routes/event.jsx"),
    route("careers", "routes/careers.jsx"),
    route("careers/:slug", "routes/career.jsx"),
    route("*", "routes/not-found.jsx"),
  ]),

  // Admin CMS — client-only subtree (auth + data fetching happen in the browser).
  // Static "admin" segment out-ranks ":lang", so these never hit the public layout.
  route("admin/login", "routes/admin/login.jsx"),
  route("admin", "routes/admin/layout.jsx", [
    index("routes/admin/dashboard.jsx"),
    route("posts", "routes/admin/posts.jsx"),
    route("posts/:id", "routes/admin/post-edit.jsx"),
    route("events", "routes/admin/events.jsx"),
    route("events/:id", "routes/admin/event-edit.jsx"),
    route("jobs", "routes/admin/jobs.jsx"),
    route("jobs/:id", "routes/admin/job-edit.jsx"),
    route("pages", "routes/admin/pages.jsx"),
    route("pages/:id", "routes/admin/page-edit.jsx"),
    route("categories", "routes/admin/categories.jsx"),
    route("media", "routes/admin/media.jsx"),
    route("settings", "routes/admin/settings.jsx"),
    route("users", "routes/admin/users.jsx"),
  ]),

  // SEO plumbing served by SSR — always in sync with the DB
  route("sitemap.xml", "routes/sitemap[.]xml.jsx"),
  route("robots.txt", "routes/robots[.]txt.jsx"),
];
