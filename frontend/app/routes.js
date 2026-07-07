import { index, route } from "@react-router/dev/routes";

export default [
  // "/" → redirect to the default language (loader-only route, no UI)
  index("routes/root-redirect.jsx"),

  // /:lang layout — validates the lang segment, renders Masthead/Nav/Footer
  route(":lang", "routes/lang-layout.jsx", [
    index("routes/home.jsx"),
    route("blog", "routes/blog-index.jsx"),
    route("blog/:slug", "routes/post.jsx"),
    route("*", "routes/not-found.jsx"),
  ]),

  // SEO plumbing served by SSR — always in sync with the DB
  route("sitemap.xml", "routes/sitemap[.]xml.jsx"),
  route("robots.txt", "routes/robots[.]txt.jsx"),
];
