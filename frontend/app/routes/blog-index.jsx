import { Link, useLoaderData, useParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";
import PostCard from "../components/PostCard";

export async function loader({ params, request }) {
  const url = new URL(request.url);
  const page = Math.max(Number(url.searchParams.get("page")) || 0, 0);
  const posts = await api.getBlogList({ lang: params.lang, page, size: 10 });
  return { posts, page };
}

export function meta({ params }) {
  const title = `${t(params.lang, "nav.blog")} — 樂橋 LucaBridge`;
  return [{ title }, { property: "og:title", content: title }];
}

export default function BlogIndex() {
  const { lang } = useParams();
  const { posts, page } = useLoaderData();

  return (
    <div className="shell" style={{ padding: "32px 20px" }}>
      <h1 style={{ fontSize: "clamp(26px, 4vw, 40px)", marginBottom: "24px" }}>{t(lang, "nav.blog")}</h1>

      {posts.content.length === 0 && <p>{t(lang, "blog.empty")}</p>}

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
          gap: "32px",
        }}
      >
        {posts.content.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}
      </div>

      {posts.totalPages > 1 && (
        <nav style={{ display: "flex", gap: "8px", justifyContent: "center", marginTop: "32px" }}>
          {Array.from({ length: posts.totalPages }).map((_, i) => (
            <Link key={i} to={`/${lang}/blog?page=${i}`} style={{ fontWeight: i === page ? 700 : 400 }}>
              {i + 1}
            </Link>
          ))}
        </nav>
      )}
    </div>
  );
}
