import ReactMarkdown from "react-markdown";
import { useLoaderData, useParams } from "react-router";

import { api, siteOrigin } from "../lib/api.server";
import { SUPPORTED_LANGS } from "../i18n";
import { formatHongKongDateTime } from "../lib/date";
import Gallery from "../components/Gallery";
import EventRegistrationPanel from "../components/EventRegistrationPanel";

const BCP47_TO_INTERNAL = { "zh-Hant": "tc", "zh-Hans": "sc", en: "en" };
const TERMS_VERSION = "v1";
const PRIVACY_VERSION = "v1";

export async function loader({ params, request }) {
  const [event, referralGroups] = await Promise.all([
    api.getEvent(params.slug, params.lang), // throws 404 Response for unknown slug
    api.getReferralGroups(params.lang).catch(() => []),
  ]);
  return { event, referralGroups, origin: siteOrigin(request) };
}

export async function action({ request }) {
  const form = await request.formData();
  const rawLang = String(form.get("lang") || "");
  const referralGroupId = form.get("referralGroupId");
  const birthYear = form.get("birthYear");

  const payload = {
    fullName: String(form.get("fullName") || "").trim(),
    gender: form.get("gender") || null,
    birthYear: birthYear ? Number(birthYear) : null,
    email: String(form.get("email") || "").trim(),
    phone: String(form.get("phone") || "").trim(),
    postalAddress: String(form.get("postalAddress") || "").trim() || null,
    referralGroupId: referralGroupId ? Number(referralGroupId) : null,
    referralGroupOther: String(form.get("referralGroupOther") || "").trim() || null,
    lang: BCP47_TO_INTERNAL[rawLang] || "tc",
    termsVersion: TERMS_VERSION,
    privacyVersion: PRIVACY_VERSION,
    friendsOptIn: form.get("friendsOptIn") === "true",
  };

  try {
    const result = await api.registerForEvent(Number(form.get("eventId")), payload);
    return { ok: true, referenceCode: result.referenceCode, status: result.status };
  } catch (err) {
    return { ok: false, message: err.message || "Registration failed" };
  }
}

export function meta({ data, params }) {
  if (!data) return [{ title: "樂橋 LucaBridge" }];
  const { event, origin } = data;
  const title = `${event.title} — 樂橋 LucaBridge`;
  const url = `${origin}/${params.lang}/events/${event.slug}`;

  return [
    { title },
    { name: "description", content: event.summary ?? event.title },
    { property: "og:type", content: "website" },
    { property: "og:title", content: event.title },
    { property: "og:url", content: url },
    ...(event.coverUrl ? [{ property: "og:image", content: event.coverUrl }] : []),
    { tagName: "link", rel: "canonical", href: url },
    ...SUPPORTED_LANGS.map((l) => ({
      tagName: "link",
      rel: "alternate",
      hrefLang: l,
      href: `${origin}/${l}/events/${event.slug}`,
    })),
  ];
}

export default function EventDetail() {
  const { lang } = useParams();
  const { event, referralGroups } = useLoaderData();

  return (
    <article className="shell" style={{ padding: "32px 20px" }}>
      {event.coverUrl && <img src={event.coverUrl} alt="" style={{ marginBottom: "24px" }} />}

      <div className="reading-column">
        <h1 style={{ fontSize: "clamp(26px, 4vw, 40px)" }}>{event.title}</h1>
        <div className="meta" style={{ display: "flex", gap: "12px", marginBottom: "24px", alignItems: "center" }}>
          {event.serviceName && <span className="badge-tag">{event.serviceName}</span>}
          {event.startsAt && <span>{formatHongKongDateTime(event.startsAt, lang)}</span>}
          {event.venue && <span>{event.venue}</span>}
        </div>

        {event.body && <ReactMarkdown
          components={{
            // The comps set quotes as a red-ruled pull quote, not an indent.
            blockquote: ({ children }) => <blockquote className="pull-quote">{children}</blockquote>,
          }}
        >{event.body}</ReactMarkdown>}

        <Gallery media={event.gallery} layout={event.galleryLayout} />

        <hr className="rule" />

        {event.registration && (
          <EventRegistrationPanel eventId={event.id} registration={event.registration} referralGroups={referralGroups} />
        )}
      </div>
    </article>
  );
}
