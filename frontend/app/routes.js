import { index, route } from "@react-router/dev/routes";

export default [
  // "/" → redirect to the default language (loader-only route, no UI)
  index("routes/root-redirect.jsx"),

  // /:lang layout — validates the lang segment, renders Masthead/Nav/Footer
  route(":lang", "routes/lang-layout.jsx", [
    index("routes/home.jsx"),
    route("blog", "routes/blog-index.jsx"),
    route("blog/:slug", "routes/post.jsx"),
    route("events", "routes/events.jsx"),
    route("events/:slug", "routes/event.jsx"),
    route("careers", "routes/careers.jsx"),
    route("careers/:slug", "routes/career.jsx"),
    route("services", "routes/services.jsx"),
    route("about", "routes/about.jsx"),
    route("donate", "routes/donate.jsx"),
    route("donate/goods", "routes/donate-goods.jsx"),
    route("volunteer", "routes/volunteer.jsx"),
    route("contact", "routes/contact.jsx"),
    route("privacy", "routes/privacy.jsx"),
    route("terms", "routes/terms.jsx"),
    route("*", "routes/not-found.jsx"),
  ]),

  // Admin CMS — client-only subtree (auth + data fetching happen in the browser).
  // Static "admin" segment out-ranks ":lang", so these never hit the public layout.
  route("admin/login", "routes/admin/login.jsx"),
  route("admin", "routes/admin/layout.jsx", [
    index("routes/admin/dashboard.jsx"),
    route("blog", "routes/admin/blog.jsx"),
    route("blog/:id", "routes/admin/blog-edit.jsx"),
    route("events", "routes/admin/events.jsx"),
    route("events/:id", "routes/admin/event-edit.jsx"),
    route("events/:id/registrations", "routes/admin/registrations.jsx"),
    route("jobs", "routes/admin/jobs.jsx"),
    route("jobs/:id", "routes/admin/job-edit.jsx"),
    route("services", "routes/admin/services.jsx"),
    route("services/:id", "routes/admin/service-edit.jsx"),
    route("home-blocks", "routes/admin/home-blocks.jsx"),
    route("home-blocks/:id", "routes/admin/home-block-edit.jsx"),
    route("referral-groups", "routes/admin/referral-groups.jsx"),
    route("referral-groups/:id", "routes/admin/referral-group-edit.jsx"),
    route("company", "routes/admin/company.jsx"),
    route("media", "routes/admin/media.jsx"),
    route("users", "routes/admin/users.jsx"),
  ]),

  // SEO plumbing served by SSR — always in sync with the DB
  route("sitemap.xml", "routes/sitemap[.]xml.jsx"),
  route("robots.txt", "routes/robots[.]txt.jsx"),
];
