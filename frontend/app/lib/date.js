const MONTHS_EN = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];

function hongKongParts(iso) {
  const shifted = new Date(new Date(iso).getTime() + 8 * 60 * 60 * 1000);
  return {
    year: shifted.getUTCFullYear(),
    month: shifted.getUTCMonth() + 1,
    day: shifted.getUTCDate(),
    hour: shifted.getUTCHours(),
    minute: shifted.getUTCMinutes(),
  };
}

export function formatHongKongDate(iso, lang, { long = false } = {}) {
  if (!iso) return "";
  const { year, month, day } = hongKongParts(iso);
  if (lang === "en") return long ? `${MONTHS_EN[month - 1]} ${day}, ${year}` : `${year}/${month}/${day}`;
  return long ? `${year}年${month}月${day}日` : `${year}/${month}/${day}`;
}

export function formatHongKongDateTime(iso, lang) {
  if (!iso) return "";
  const { year, month, day, hour, minute } = hongKongParts(iso);
  const time = `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
  return lang === "en" ? `${MONTHS_EN[month - 1]} ${day}, ${year}, ${time}` : `${year}年${month}月${day}日 ${time}`;
}
