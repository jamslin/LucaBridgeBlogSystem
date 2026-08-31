import { useParams } from "react-router";
import { t } from "../i18n";

export function meta({ params }) {
  const title = `${t(params.lang, "footer.privacy")} — 樂橋 LucaBridge`;
  return [{ title }];
}

export default function Privacy() {
  const { lang } = useParams();
  return (
    <div className="shell static-page__hero reading-column" style={{ paddingBottom: "48px" }}>
      <h1>{t(lang, "footer.privacy")}</h1>
      <div className="pending-notice">{t(lang, "common.contentPending")}</div>
    </div>
  );
}
