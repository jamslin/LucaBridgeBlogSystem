import { Link, useParams } from "react-router";
import { t } from "../i18n";

export default function PrevNextBar({ previous, next }) {
  const { lang } = useParams();
  if (!previous && !next) return null;

  return (
    <nav style={{ display: "flex", justifyContent: "space-between", borderTop: "1px solid var(--color-line)", paddingTop: "16px", marginTop: "24px" }}>
      <div>
        {previous && (
          <Link to={`/${lang}/blog/${previous.slug}`}>
            &larr; {t(lang, "post.prev")}: {previous.title}
          </Link>
        )}
      </div>
      <div style={{ textAlign: "right" }}>
        {next && (
          <Link to={`/${lang}/blog/${next.slug}`}>
            {t(lang, "post.next")}: {next.title} &rarr;
          </Link>
        )}
      </div>
    </nav>
  );
}
