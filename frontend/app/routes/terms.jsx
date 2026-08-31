import { useParams } from "react-router";
import { t } from "../i18n";

export function meta({ params }) {
  const title = `${t(params.lang, "footer.terms")} — 樂橋 LucaBridge`;
  return [{ title }];
}

export default function Terms() {
  const { lang } = useParams();
  return (
    <div className="shell static-page__hero reading-column" style={{ paddingBottom: "48px" }}>
      <h1>{t(lang, "footer.terms")}</h1>
      <div className="pending-notice">{t(lang, "common.contentPending")}</div>
    </div>
  );
}
