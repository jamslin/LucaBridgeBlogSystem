const MONTHS_EN = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];

function hongKongParts(iso) {
  const shifted = new Date(new Date(iso).getTime() + 8 * 60 * 60 * 1000);
  return {
    year: shifted.getUTCFullYear(),
    month: shifted.getUTCMonth() + 1,
    day: shifted.getUTCDate(),
    hour: shifted.getUTCHours(),
    minute: shifted.getUTCMinutes(),
    weekday: shifted.getUTCDay(),
  };
}

export function formatHongKongDate(iso, lang, { long = false } = {}) {
  if (!iso) return "";
  const { year, month, day } = hongKongParts(iso);
  if (lang === "en") return long ? `${MONTHS_EN[month - 1]} ${day}, ${year}` : `${year}/${month}/${day}`;
  return long ? `${year}年${month}月${day}日` : `${year}/${month}/${day}`;
}

const MONTHS_EN_SHORT = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

/** Day + short month, for the home-page events timeline's date badge. */
export function hongKongDayMonth(iso, lang) {
  if (!iso) return { day: "", month: "" };
  const { month, day } = hongKongParts(iso);
  return { day: String(day), month: lang === "en" ? MONTHS_EN_SHORT[month - 1] : `${month}月` };
}

export function formatHongKongDateTime(iso, lang) {
  if (!iso) return "";
  const { year, month, day, hour, minute } = hongKongParts(iso);
  const time = `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
  return lang === "en" ? `${MONTHS_EN[month - 1]} ${day}, ${year}, ${time}` : `${year}年${month}月${day}日 ${time}`;
}

/** Article meta style: "2025.03.18" in Chinese, "18 Mar 2025" in English. */
export function formatArticleDate(iso, lang) {
  if (!iso) return "";
  const { year, month, day } = hongKongParts(iso);
  if (lang === "en") return `${day} ${MONTHS_EN_SHORT[month - 1]} ${year}`;
  return `${year}.${String(month).padStart(2, "0")}.${String(day).padStart(2, "0")}`;
}

const WEEKDAYS = {
  "zh-Hant": ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"],
  "zh-Hans": ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"],
  en: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
};

/** Timeline rail: "04.02". */
export function formatDayMonth(iso) {
  if (!iso) return "";
  const { month, day } = hongKongParts(iso);
  return `${String(month).padStart(2, "0")}.${String(day).padStart(2, "0")}`;
}

export function formatWeekday(iso, lang) {
  if (!iso) return "";
  return (WEEKDAYS[lang] ?? WEEKDAYS["zh-Hant"])[hongKongParts(iso).weekday];
}

/**
 * Which bucket a date falls into relative to now, for the timeline's left-hand
 * label: this week, next week, or the month it lands in. Content volume here is
 * single digits, so "in three weeks" is more useful than a precise countdown.
 */
export function weekBucket(iso, now = new Date()) {
  if (!iso) return "later";
  const days = Math.floor((new Date(iso).getTime() - now.getTime()) / 86400000);
  if (days < 7) return "thisWeek";
  if (days < 14) return "nextWeek";
  return "later";
}

/** "09:00–12:00", or just the start when there is no end time. */
export function formatTimeRange(startIso, endIso) {
  if (!startIso) return "";
  const time = (iso) => {
    const { hour, minute } = hongKongParts(iso);
    return `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
  };
  const start = time(startIso);
  if (!endIso) return start;
  const end = time(endIso);
  return end === start ? start : `${start}–${end}`;
}
