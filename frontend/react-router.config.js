// React Router v7 framework mode, SSR enabled.
// Pages render on the Node server per request, fetching live from the Spring API —
// crawlers and WhatsApp/Facebook link scrapers receive complete HTML.
// (Decision log: SSR over SSG so publishing never depends on a rebuild pipeline.)
export default {
  ssr: true,
};
