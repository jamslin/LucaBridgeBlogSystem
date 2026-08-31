import { useState } from "react";
import { useFetcher, useParams } from "react-router";
import { t } from "../i18n";

const BADGE_CLASS = {
  OPEN: "reg-badge--open",
  FULL: "reg-badge--full",
  CLOSED: "reg-badge--closed",
  NOT_OPEN: "reg-badge--not-open",
  NOT_REGISTERABLE: "reg-badge--not-registerable",
};

const BADGE_KEY = {
  OPEN: "events.registerCta",
  FULL: "events.full",
  CLOSED: "events.closed",
  NOT_OPEN: "events.notOpenYet",
  NOT_REGISTERABLE: "events.notRegisterable",
};

/**
 * The five-state registration contract (RegistrationState from the backend) drives every
 * pixel here — this component never re-derives OPEN/FULL/etc. from raw dates/capacity itself.
 */
export default function EventRegistrationPanel({ eventId, registration, referralGroups }) {
  const { lang } = useParams();
  const fetcher = useFetcher();
  const [referralGroupId, setReferralGroupId] = useState("");
  const isOther = referralGroups.find((g) => String(g.id) === referralGroupId)?.code === "other";

  const { state, capacity, remaining, almostFull } = registration;
  const canSubmit = state === "OPEN" || state === "FULL";
  const result = fetcher.data;

  if (result && result.ok) {
    const isConfirmed = result.status === "CONFIRMED";
    return (
      <div className="reg-success">
        <p style={{ marginTop: 0, fontWeight: 700 }}>{t(lang, "events.successTitle")}</p>
        <p>
          {t(lang, isConfirmed ? "events.successConfirmed" : "events.successWaitlist")}{" "}
          <span className="code">{result.referenceCode}</span>
        </p>
      </div>
    );
  }

  return (
    <section>
      <div style={{ display: "flex", alignItems: "center", gap: "10px", flexWrap: "wrap" }}>
        <span className={`reg-badge ${BADGE_CLASS[state] || ""}`}>
          {almostFull && state === "OPEN" ? t(lang, "events.almostFull") : t(lang, BADGE_KEY[state] || "events.registerCta")}
        </span>
        {capacity != null && (state === "OPEN" || state === "FULL") && (
          <span className="meta">
            {state === "FULL" ? 0 : remaining} {t(lang, "events.spotsLeft")}
          </span>
        )}
      </div>

      {capacity != null && (state === "OPEN" || state === "FULL") && (
        <div className="reg-progress" aria-hidden="true">
          <span style={{ width: `${Math.min(100, Math.round(((capacity - (remaining ?? 0)) / capacity) * 100))}%` }} />
        </div>
      )}

      {canSubmit && (
        <fetcher.Form method="post" className="reg-form">
          <input type="hidden" name="eventId" value={eventId} />
          <input type="hidden" name="lang" value={lang} />
          <div className="admin-field">
            <label>{t(lang, "events.fullName")} *</label>
            <input type="text" name="fullName" required />
          </div>
          <div className="admin-field">
            <label>{t(lang, "events.gender")}</label>
            <select name="gender" defaultValue="">
              <option value="">{t(lang, "events.genderSkip")}</option>
              <option value="M">{t(lang, "events.genderM")}</option>
              <option value="F">{t(lang, "events.genderF")}</option>
            </select>
          </div>
          <div className="admin-field">
            <label>{t(lang, "events.birthYear")}</label>
            <input type="number" name="birthYear" min="1900" max="2100" />
          </div>
          <div className="admin-field">
            <label>{t(lang, "events.email")} *</label>
            <input type="email" name="email" required />
          </div>
          <div className="admin-field">
            <label>{t(lang, "events.phone")} *</label>
            <input type="tel" name="phone" required />
          </div>
          <div className="admin-field">
            <label>{t(lang, "events.postalAddress")}</label>
            <input type="text" name="postalAddress" />
          </div>
          {referralGroups.length > 0 && (
            <div className="admin-field">
              <label>{t(lang, "events.referral")}</label>
              <select name="referralGroupId" value={referralGroupId} onChange={(e) => setReferralGroupId(e.target.value)}>
                <option value="">{t(lang, "events.referralSelect")}</option>
                {referralGroups.map((g) => (
                  <option key={g.id} value={g.id}>{g.name}</option>
                ))}
              </select>
            </div>
          )}
          {isOther && (
            <div className="admin-field">
              <label>{t(lang, "events.referralOther")}</label>
              <input type="text" name="referralGroupOther" />
            </div>
          )}
          <div className="admin-field">
            <label className="checkbox">
              <input type="checkbox" name="friendsOptIn" value="true" />
              {t(lang, "events.friendsOptIn")}
            </label>
          </div>
          <div className="admin-field">
            <label className="checkbox">
              <input type="checkbox" required />
              {t(lang, "events.termsAgree")}
            </label>
          </div>
          {result && !result.ok && <div className="admin-alert error">{result.message}</div>}
          <button type="submit" className="btn btn-primary" disabled={fetcher.state !== "idle"}>
            {fetcher.state !== "idle" ? t(lang, "events.submitting") : t(lang, "events.submit")}
          </button>
        </fetcher.Form>
      )}
    </section>
  );
}
