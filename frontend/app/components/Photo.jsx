// The single place a photograph gets rendered (mockup 7e).
//
// Volunteer photos are phone snaps at mixed sizes and light levels, so the design
// leans on a fixed crop, a warm multiply wash and a light saturate/contrast
// correction rather than on art direction. All of that lives in .photo; this
// component picks the ratio and handles the missing-image case, which is common
// while the site is still filling up.
const RATIO_CLASS = {
  hero: "photo--hero",
  card: "photo--card",
  cover: "photo--cover",
  square: "photo--square",
  native: "photo--native",
};

export default function Photo({
  src,
  alt = "",
  ratio = "cover",
  className = "",
  loading = "lazy",
}) {
  const classes = [
    "photo",
    RATIO_CLASS[ratio] ?? RATIO_CLASS.cover,
    src ? "" : "photo--empty",
    className,
  ].filter(Boolean).join(" ");

  // An empty frame in the right shape keeps the grid from collapsing and reads
  // as "no photo yet" rather than as a broken image.
  if (!src) return <span className={classes} aria-hidden="true" />;

  return (
    <span className={classes}>
      <img src={src} alt={alt} loading={loading} decoding="async" />
    </span>
  );
}
