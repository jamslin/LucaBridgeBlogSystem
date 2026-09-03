import { Link, useParams } from "react-router";

import { t } from "../i18n";
import { formatArticleDate } from "../lib/date";
import Photo from "./Photo";

// A routine news card: cover, date + read time, title, summary.
//
// The whole card is deliberately not one link — the cover and the title both
// point at the post, but the summary stays selectable text. Wrapping the lot in
// an <a> would make the summary un-selectable and hand screen readers a single
// enormous link label.
export default function PostCard({ post, ratio = "cover", headingLevel: H = "h3" }) {
  const { lang } = useParams();
  const href = `/${lang}/blog/${post.slug}`;

  return (
    <article className="post-card">
      <Link to={href} className="post-card__media" tabIndex={-1} aria-hidden="true">
        <Photo src={post.coverUrl} ratio={ratio} />
      </Link>

      <div className="post-card__meta">
        {post.serviceName && <span className="badge-tag">{post.serviceName}</span>}
        {post.publishedAt && <span>{formatArticleDate(post.publishedAt, lang)}</span>}
        {post.readMinutes ? (
          <span>· {t(lang, "blog.readingTime", { count: post.readMinutes })}</span>
        ) : null}
      </div>

      <H className="post-card__title">
        <Link to={href}>{post.title}</Link>
      </H>

      {post.summary && <p className="post-card__summary">{post.summary}</p>}
    </article>
  );
}
