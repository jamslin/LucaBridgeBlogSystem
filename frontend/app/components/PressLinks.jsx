import { useParams } from "react-router";
import { t } from "../i18n";

export default function PressLinks({ links }) {
  const { lang } = useParams();
  if (!links || links.length === 0) return null;

  return (
    <section>
      <h4 className="kicker">{t(lang, "post.pressLinks")}</h4>
      <ul style={{ paddingLeft: "1.2em" }}>
        {links.map((link) => (
          <li key={link.id}>
            <a href={link.url} target="_blank" rel="noopener noreferrer">
              {link.label}
            </a>
          </li>
        ))}
      </ul>
    </section>
  );
}
