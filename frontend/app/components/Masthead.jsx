import { useRouteLoaderData } from "react-router";

import LanguageSwitch from "./LanguageSwitch";
import SocialLinks from "./SocialLinks";

// The red utility strip above the nav (mockups 8a/8b): contact details on the
// left, social links and the language switch on the right.
//
// The wordmark moved down into the nav pill itself, which is where the comps put
// it — so this component is now the strip alone. On narrow screens the email and
// address drop out via CSS so the phone number always survives.
export default function Masthead() {
  const layoutData = useRouteLoaderData("routes/lang-layout");
  const company = layoutData?.company ?? {};

  return (
    <div className="masthead-utility">
      <div className="shell masthead-utility__inner">
        <span className="masthead-utility__contact">
          {company.phone && (
            <a href={`tel:${company.phone.replace(/\s+/g, "")}`}>{company.phone}</a>
          )}
          {company.email && (
            <a className="masthead-utility__email" href={`mailto:${company.email}`}>{company.email}</a>
          )}
          {company.address && (
            <span className="masthead-utility__location">{company.address}</span>
          )}
        </span>

        <span className="masthead-utility__right">
          <span className="masthead-utility__social">
            <SocialLinks company={company} withLabels />
          </span>
          <span className="masthead-utility__divider" aria-hidden="true">|</span>
          <LanguageSwitch />
        </span>
      </div>
    </div>
  );
}
