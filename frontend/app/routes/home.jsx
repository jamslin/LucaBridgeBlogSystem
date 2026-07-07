import { useLoaderData, useParams } from "react-router";

import { api } from "../lib/api.server";
import { t } from "../i18n";
import PostCard from "../components/PostCard";

export async function loader({ params }) {
  const posts = await api.getPosts({ lang: params.lang, size: 5 });
  return { posts };
}

export function meta({ params }) {
  return [
    { title: "樂橋 LucaBridge" },
    { property: "og:title", content: "樂橋 LucaBridge" },
    { property: "og:type", content: "website" },
  ];
}

export default function Home() {
  const { lang } = useParams();
  const { posts } = useLoaderData();
  const [featured, ...rest] = posts.items;

  return (
    <div className="shell" style={{ padding: "32px 20px" }}>
      {featured && (
        <section style={{ marginBottom: "40px" }}>
          <PostCard post={featured} />
        </section>
      )}

      <h2 className="kicker" style={{ fontSize: "13px" }}>{t(lang, "home.latest")}</h2>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
          gap: "32px",
          marginTop: "16px",
        }}
      >
        {rest.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}
      </div>
    </div>
  );
}
