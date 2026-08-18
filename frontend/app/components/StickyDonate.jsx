import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";

import { t } from "../i18n";

// Compact donate CTA that slides in once the masthead (which now carries the
// primary 立即捐款 button) has scrolled out of view, so the CTA is never lost
// on long pages. Hidden by default → server and first client render match.
export default function StickyDonate() {
  const { lang } = useParams();
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    let ticking = false;
    const onScroll = () => {
      if (ticking) return;
      ticking = true;
      window.requestAnimationFrame(() => {
        setVisible(window.scrollY > 220);
        ticking = false;
      });
    };
    window.addEventListener("scroll", onScroll, { passive: true });
    onScroll();
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <Link
      to={`/${lang}/p/donate`}
      className="sticky-donate"
      data-visible={visible ? "true" : "false"}
      aria-hidden={visible ? undefined : "true"}
      tabIndex={visible ? undefined : -1}
    >
      {t(lang, "nav.donateCta")}
    </Link>
  );
}
