import { Link, useParams } from "react-router";
import { t } from "../i18n";
import { formatArticleDate } from "../lib/date";

export default function PostCard({ post }) {
  const { lang } = useParams();

  return (
    <article style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
      <Link to={`/${lang}/blog/${post.slug}`}>
        {post.coverUrl && <img src={post.coverUrl} alt="" loading="lazy" />}
      </Link>
      <h3 style={{ fontSize: "20px" }}>
        <Link to={`/${lang}/blog/${post.slug}`}>{post.title}</Link>
      </h3>
      {post.summary && <p style={{ color: "var(--color-ink-soft)", margin: 0 }}>{post.summary}</p>}
      <div className="meta" style={{ display: "flex", gap: "12px" }}>
        {post.publishedAt && <span>{formatArticleDate(post.publishedAt, lang)}</span>}
        {post.readMinutes && <span>{post.readMinutes} min</span>}
      </div>
      <Link to={`/${lang}/blog/${post.slug}`} className="btn-text">
        {t(lang, "home.readMore")} <span className="arrow">&rarr;</span>
      </Link>
    </article>
  );
}
