// Inline-SVG social icons (Instagram / Facebook / YouTube). URLs come from the
// company record; a link is only rendered when its URL is present. `size` and
// `color` let the masthead (dark on light) and footer (light on dark) reuse it.
const ICONS = {
  instagram: (
    <path d="M12 2.2c3.2 0 3.58.01 4.85.07 1.17.05 1.8.25 2.23.41.56.22.96.48 1.38.9.42.42.68.82.9 1.38.16.42.36 1.06.41 2.23.06 1.27.07 1.65.07 4.85s-.01 3.58-.07 4.85c-.05 1.17-.25 1.8-.41 2.23-.22.56-.48.96-.9 1.38-.42.42-.82.68-1.38.9-.42.16-1.06.36-2.23.41-1.27.06-1.65.07-4.85.07s-3.58-.01-4.85-.07c-1.17-.05-1.8-.25-2.23-.41a3.7 3.7 0 0 1-1.38-.9 3.7 3.7 0 0 1-.9-1.38c-.16-.42-.36-1.06-.41-2.23C2.21 15.58 2.2 15.2 2.2 12s.01-3.58.07-4.85c.05-1.17.25-1.8.41-2.23.22-.56.48-.96.9-1.38.42-.42.82-.68 1.38-.9.42-.16 1.06-.36 2.23-.41C8.42 2.21 8.8 2.2 12 2.2Zm0 3.05A6.75 6.75 0 1 0 18.75 12 6.75 6.75 0 0 0 12 5.25Zm0 11.13A4.38 4.38 0 1 1 16.38 12 4.38 4.38 0 0 1 12 16.38Zm6.99-11.4a1.58 1.58 0 1 1-1.58-1.58 1.58 1.58 0 0 1 1.58 1.58Z" />
  ),
  facebook: (
    <path d="M22 12a10 10 0 1 0-11.56 9.88v-6.99H7.9V12h2.54V9.8c0-2.5 1.49-3.89 3.77-3.89 1.09 0 2.24.2 2.24.2v2.46h-1.26c-1.24 0-1.63.77-1.63 1.56V12h2.78l-.44 2.89h-2.34v6.99A10 10 0 0 0 22 12Z" />
  ),
  youtube: (
    <path d="M23.5 6.5a3.02 3.02 0 0 0-2.12-2.14C19.5 3.85 12 3.85 12 3.85s-7.5 0-9.38.51A3.02 3.02 0 0 0 .5 6.5 31.5 31.5 0 0 0 0 12a31.5 31.5 0 0 0 .5 5.5 3.02 3.02 0 0 0 2.12 2.14c1.88.51 9.38.51 9.38.51s7.5 0 9.38-.51a3.02 3.02 0 0 0 2.12-2.14A31.5 31.5 0 0 0 24 12a31.5 31.5 0 0 0-.5-5.5ZM9.6 15.57V8.43L15.82 12Z" />
  ),
};

function Icon({ kind, href, label, size, color }) {
  if (!href) return null;
  return (
    <a href={href} target="_blank" rel="noopener noreferrer" aria-label={label}
       style={{ display: "inline-flex", color }}>
      <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
        {ICONS[kind]}
      </svg>
    </a>
  );
}

export default function SocialLinks({ company = {}, size = 18, color = "var(--color-ink)" }) {
  const items = [
    { kind: "instagram", href: company.instagramUrl, label: "Instagram" },
    { kind: "facebook",  href: company.facebookUrl,  label: "Facebook" },
    { kind: "youtube",   href: company.youtubeUrl,   label: "YouTube" },
  ].filter((i) => i.href);

  if (items.length === 0) return null;

  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: "12px" }}>
      {items.map((i) => <Icon key={i.kind} {...i} size={size} color={color} />)}
    </span>
  );
}
