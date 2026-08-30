# LucaBridge - Design Brief

For a Claude Design session. Self-contained: paste or attach this whole file at the start.
Supersedes the earlier version of this document - the architecture changed underneath it.

---

## 1. What this is

樂橋 LucaBridge is a Hong Kong community and environmental charity. Programme areas: poverty
relief, environment, campus outreach, elderly care, and volunteering. Trilingual public website -
English / 繁體中文 / 简体中文.

**The site is live but unannounced.** Pre-launch, so a full visual reset carries no migration
risk. The database and backend are being rebuilt from scratch alongside this redesign, which
means the design can ask for content shapes that don't exist yet - within the limits in §3.

This is a **total revamp**, not a refinement. Colour, typography, layout language, photo
treatment and component shapes are all open.

## 2. Audience and goals

In priority order:

1. **Potential volunteers** - mostly students and young working adults looking for something
   concrete to sign up for. **This is the primary conversion, because event registration is the
   one thing the site can actually complete today.**
2. **Potential donors** - individuals and small local businesses deciding whether this charity is
   real and competent. No online payment exists (see §3), so the ask is in-kind: goods, materials,
   time.
3. **Beneficiaries and community partners** - what services exist, how to reach a human.
4. **Job seekers** - small but real; live job postings.

Everything else on the site exists to build enough trust that those actions feel safe.

## 3. Hard constraints

Design **for** these, not around them.

### Trilingual from one layout
Every screen renders in 繁中 / EN / 简中 from the same markup. The most-broken constraint:

- Labels change length 2-3x between languages. `捐款` is two characters, "Donate" is six,
  `物資捐贈` versus "Donate goods and materials". Nothing may rely on a fixed label width or a
  nav bar that only fits at one language.
- CJK body text at 16px/1.8 has different colour and rhythm from Latin at the same size. A
  reading measure that works in English can feel cramped in Chinese.
- Chinese has no word spaces. Justification, hyphenation and mid-word truncation behave
  differently - prefer line-clamping to character truncation.
- **Any font chosen must have full Traditional AND Simplified coverage.** This rules out most
  display faces. Verify before proposing.
- URLs are `/tc/…`, `/sc/…`, `/en/…`.

### Fixed information architecture
Client-approved, already shipped. Do not restructure:

```
關於我們 About      → 背景與發展 / 目標與宗旨 / 組織架構 / 委員名單
服務範圍 Services   (single page)
最新消息 News       → 近期消息 (blog) / 最新資訊 (events)
捐款 Donate         → 捐款方法 / 物資捐贈
招募 Recruitment    → 義工 Volunteer / 職位空缺 Careers
聯絡我們 Contact
```

Five top-level items, four with children. A utility bar above carries phone, email, Instagram,
Facebook, YouTube and the language switcher.

### Technical envelope
React Router v7, server-side rendered. Plain hand-written CSS driven by a token module - no
Tailwind, no component library. Must be expressible in standard CSS grid and flexbox. Avoid
scroll-jacking, canvas/WebGL, or anything needing heavy client JavaScript: SEO and WhatsApp /
Facebook link previews are explicit requirements.

### Things that do NOT exist - do not design them
- **No online payment.** No amount selector, no checkout, no card fields. Deferred to a later
  phase. The donation ask is in-kind or "contact us".
- **No images inside article body text.** Articles are cover image + text + a gallery block.
  Do not design layouts with photos placed mid-paragraph.
- **No Instagram feed.**
- **No page builder.** Page structure lives in React code; the database supplies content only.

### Photography reality
Real volunteer photos taken on phones: variable quality, mixed aspect ratios, some poorly lit,
people in high-vis vests and masks. The design must make ordinary documentary photos look
intentional - consistent crops, tint or duotone treatments, generous framing - not assume
art-directed hero imagery.

### Content volume
Single digits to low tens of posts and events. **Any layout that needs a dense content firehose
to look right will look empty.** Design the three-item state, not the thirty-item state, and
design real empty states.

### Accessibility
WCAG 2.1 AA. A charity serving vulnerable people; contrast and touch-target size are not
optional.

---

## 4. What content actually exists

This is the part that matters most. Every field below is real and available. Anything not listed
does not exist and would have to be justified.

### Global (every page)
- Organisation name, tagline, about text, address, office hours - all trilingual
- Phone, email, charity registration number (91/17604), founded year (2021)
- Logo image
- Instagram / Facebook / YouTube URLs

### Home page
Built from ordered, schedulable content blocks in named slots. Each block has an image, a link, a
title, a subtitle and a button label - all trilingual. The slots:

| Slot | Holds | Notes |
|---|---|---|
| `HERO` | Rotating slides: image, title, subtitle, button label, link | Currently one primary CTA plus one secondary - see §5 |
| `STAT` | A number and a label, e.g. 895 / 位支持者 | Client-editable. Also 2021 成立年份, 91/17604 稅務編號, 5 服務範圍 |
| `FEATURED` | A pinned blog post | Pulls the post's own cover, title, summary, date, service tag |
| `SUPPORT` | The conversion band - in-kind donation or volunteer recruitment | See §5. This replaces the donation band |
| `VOLUNTEER` | Image card driving to volunteer signup | |
| `QUICK_LINK` | Small rows: title, subtitle, link | Currently 物資捐贈 / 職位空缺 / 聯絡我們 |

Plus two derived lists, not stored as blocks:
- **Latest posts** - newest N from the blog, each with cover, title, summary, date, service tag
- **Upcoming events** - date, title, summary, and whether registration is open

### Services (the taxonomy)
One list drives the home chip row, the home service cards, the services page, and blog tagging.
Each service has: an icon image, a trilingual name, a trilingual description, and a sort order.
Currently five: 環保保育 / 扶貧支援 / 長者關懷 / 校園計劃 / 義工發展.

### Blog post
- Cover image, title, summary, body (markdown), service tag, author, published date, read minutes
- **A gallery** of ordered images with trilingual captions, rendered as one of three layouts the
  editor picks per post: **carousel, grid, or masonry**. All three need designing.
- Previous / next navigation

### Event
- Cover image, title, summary, body, venue (trilingual), start and end datetime, map link
- A gallery, same three layouts
- **Registration state**, which drives the whole page's call to action:
  - not registerable
  - registration not yet open
  - open, with places left
  - open, but full → waitlist
  - closed
- Capacity and places remaining

### Event registration form - a new screen
Public, unauthenticated. Eleven fields plus two consents. Currently a Google Form; the whole
point of rebuilding it is that it should not feel like one.

- 所屬團體 / 推薦團體 - select from a fixed list of ten, plus "其他" with a free-text field
- 申請人中文姓名
- 性別 - 男性 / 女性
- 出生年份 - year only
- 電郵
- 手機號碼 - used for WhatsApp contact
- 通訊地址 - a real postal address, needs room
- A WhatsApp confirmation checkbox with instructions and a wa.me link
- Terms and conditions - four bullets, must be accepted
- Personal Information Collection Statement - four numbered clauses under the PDPO, must be
  accepted
- A separate, **optional** mailing-list opt-in - never bundled into the mandatory consent

Needs designing: the filled form, inline validation errors, the success state with a reference
code, the full/waitlist state, and the closed state.

### Job posting
Title, body, location, employment type, department, posted date, closing date, apply email/URL.

### Static React pages
About (four sub-pages), Services, Contact, Volunteer, Donation methods. These are hand-built in
React - the designer has full freedom on layout, and the words are fixed in code.

---

## 5. Where the current design falls short

Honest read of the existing site and the first-draft mockup, to aim away from.

**The home page never asks for anything.** The hero rotates a news story; the second band is
three equal-weight cards; the third is a news grid. A first-time visitor is never asked to
volunteer. Fix: the `SUPPORT` band is the emotional peak of the page - full-bleed, biggest type,
one ask. Give it the weight the removed donation band had.

**Two equal-weight hero CTAs split attention.** With donation gone, there is one live conversion.
Make 成為義工 primary and everything else secondary.

**Card-grid monotony.** Home, blog, events and careers are currently the same rectangular card
grid at different densities. Nothing distinguishes "this is our flagship story" from "this is a
routine update". The featured slot exists precisely so one story can be treated differently.

**Photos carry no treatment.** Raw phone photos in rectangles at mixed crops.

**Nothing conveys proof.** For a charity asking for time and goods, the biggest trust gap. The
stat blocks (895 supporters, since 2021, charity no. 91/17604) exist to close it - they should
read as evidence, not decoration.

**The service chip row is decorative.** Make it functional - each chip goes somewhere.

**Registration must not look like a Google Form.** Eleven fields and two walls of legal text is
a lot to ask of a volunteer on a phone. Consider grouping, progressive disclosure for the legal
text, and a visible sense of progress. The consents must remain clearly readable and separately
ticked - do not hide them to make the form look shorter.

**The admin CMS is unstyled scaffolding.** Separate problem, separate pass - see §6.

---

## 6. Screens to design

### Public - 12 templates plus the shell
Shell: utility bar, nav (desktop mega-menu + mobile drawer), footer, language switcher.

1. Home
2. Blog index (with service filtering)
3. Blog detail (+ three gallery layouts)
4. Event index (upcoming / past)
5. Event detail (five registration states)
6. Event registration form (+ validation, success, full, closed states)
7. Careers index
8. Job detail
9. About
10. Services
11. Contact
12. 404

### Admin CMS - later, separate pass
Dense data UI for one or two staff, English-only, desktop-first, judged on speed of entry, not
brand feel. Shares a token layer with the public site but not its layout language.

Login · Dashboard · Blog list + editor · Event list + editor · **Event registrations list, detail
and CSV export** · Job list + editor · Services · Home blocks · Media library · Company info ·
Users.

The hard screen is the **trilingual record editor**: every content type is three parallel
versions of one record with fallback to 繁中. Solving "edit one record in three languages without
losing your place" is the most valuable thing the admin pass can deliver.

---

## 7. Real content to design against

Do not use lorem ipsum. Chinese placeholder text of the wrong length is how a layout passes
review and then collapses.

- Flagship story: **青龍頭碼頭海岸清潔行動** - volunteers cleared 32kg of rubbish from the Tsing
  Lung Tau shoreline. Service tag 環保保育. Dated 2025/4/2, ~1 min read.
- Home hero copy in use: eyebrow 與社區同行, headline 用愛築橋・連結希望, English line "Bridging
  Hearts, Building Hope".
- Home section heading: 連結資源，回應社區需要
- Services: 環保保育 / 扶貧支援 / 長者關懷 / 校園計劃 / 義工發展
- Stats: 895 位支持者 · 2021 成立年份 · 91/17604 稅務編號 · 5 服務範圍
- Contact: +852 2608 9577 · enquiry@lucabridge.org.hk · 元朗

Assume article bodies range from three paragraphs to two thousand words.

---

## 8. What to deliver, in order

Do not attempt all of this in one session.

1. **Two or three genuinely distinct home page directions**, each in 繁中 and English, each with
   a short rationale. Distinct means different points of view, not three tints of one layout.
   Stop and choose one.
2. The chosen direction applied to the **shell** (utility bar, nav open and closed, footer) and
   the **blog detail** template - the two things every other screen inherits from.
3. Blog index, event index, event detail, and the three gallery layouts.
4. The **event registration form** and all its states. This is the most important new screen and
   deserves its own pass.
5. Careers, job detail, about, services, contact, 404.
6. Mobile for everything above.
7. A token set - colour, type scale, spacing, radii, motion - that can drop straight into the
   codebase.
8. Admin CMS, starting with the trilingual editor.
