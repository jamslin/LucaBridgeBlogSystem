import { Link, useParams } from "react-router";

// "Rotated red square + bilingual wordmark, 2px ink rule" per design doc.
// Square uses var(--color-accent) so it follows whichever palette the client picks.
export default function Masthead() {
  const { lang } = useParams();
  return (
    <header style={{ borderBottom: "2px solid var(--color-ink)" }}>
      <div className="shell" style={{ display: "flex", alignItems: "center", gap: "12px", padding: "20px 0" }}>
        <span
          aria-hidden="true"
          style={{
            width: "18px",
            height: "18px",
            background: "var(--color-accent)",
            transform: "rotate(45deg)",
            display: "inline-block",
          }}
        />
        <Link to={`/${lang}`} style={{ fontFamily: "var(--font-display)", fontWeight: 600, fontSize: "22px", color: "var(--color-ink)" }}>
          樂橋 LucaBridge
        </Link>
      </div>
    </header>
  );
}
