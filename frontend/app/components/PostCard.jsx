import { Link, useParams } from "react-router";
import { t } from "../i18n";

export default function PostCard({ post }) {
  const { lang } = useParams();

  return (
    <article style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
      <Link to={`/${lang}/blog/${post.slug}`}>
        {post.coverImageUrl && <img src={post.coverImageUrl} alt="" loading="lazy" />}
      </Link>
      <span className="kicker">{post.category?.name}</span>
      <h3 style={{ fontSize: "20px" }}>
        <Link to={`/${lang}/blog/${post.slug}`}>{post.title}</Link>
      </h3>
      {post.excerpt && <p style={{ color: "var(--color-ink-soft)", margin: 0 }}>{post.excerpt}</p>}
      <div className="meta" style={{ display: "flex", gap: "12px" }}>
        {post.publishedAt && <span>{new Date(post.publishedAt).toLocaleDateString(lang)}</span>}
        {post.readingMinutes && <span>{post.readingMinutes} min</span>}
      </div>
      <Link to={`/${lang}/blog/${post.slug}`} style={{ fontSize: "13px", fontWeight: 600 }}>
        {t(lang, "home.readMore")} &rarr;
      </Link>
    </article>
  );
}
