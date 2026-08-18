import { Link, useParams } from "react-router";
import { t } from "../i18n";
import { formatHongKongDate } from "../lib/date";

export default function PostCard({ post }) {
  const { lang } = useParams();

  return (
    <article className="post-card">
      <Link to={`/${lang}/blog/${post.slug}`} className="post-card__thumb">
        {post.coverImageUrl && <img src={post.coverImageUrl} alt="" loading="lazy" />}
      </Link>
      <span className="kicker">{post.category?.name}</span>
      <h3 className="post-card__title">
        <Link to={`/${lang}/blog/${post.slug}`}>{post.title}</Link>
      </h3>
      {post.excerpt && <p className="post-card__excerpt">{post.excerpt}</p>}
      <div className="meta" style={{ display: "flex", gap: "12px" }}>
        {post.publishedAt && <span>{formatHongKongDate(post.publishedAt, lang)}</span>}
        {post.readingMinutes && <span>{post.readingMinutes} min</span>}
      </div>
      <Link to={`/${lang}/blog/${post.slug}`} className="btn-text">
        {t(lang, "home.readMore")} <span className="arrow">&rarr;</span>
      </Link>
    </article>
  );
}
