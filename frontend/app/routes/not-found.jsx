import { Link, useParams } from "react-router";
import { t } from "../i18n";

export function loader() {
  // Real HTTP 404 status for unknown paths (matters for SEO, not just UX).
  return new Response(null, { status: 404 });
}

export default function NotFound() {
  const { lang } = useParams();

  return (
    <div className="shell" style={{ padding: "60px 20px", textAlign: "center" }}>
      <h1>{t(lang, "post.notFound")}</h1>
      <Link to={`/${lang}`}>{t(lang, "common.backHome")}</Link>
    </div>
  );
}
