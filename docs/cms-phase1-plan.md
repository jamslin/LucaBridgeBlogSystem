# LucaBridge CMS - Phase 1 Implementation Plan

Status: **scope approved 2026-08-30, no code written.** Written after reading the shipped
backend and admin SPA, so every "current state" claim below is from the code, not from notes.

---

## 0. Scope this plan implements

From the client conversation on 2026-08-30:

| # | Requirement | Decision |
|---|---|---|
| 1 | Update company info | Replace the raw key/value Settings screen with a labelled form: contact details, social links, org identity, donation details |
| 1 | Homepage hero banner | **Already built.** No work needed |
| 2 | Jobs: create / edit / disable / delete | Already built; needs the delete-permission change and closing-date enforcement |
| 3 | Events: create / edit / launch / disable / delete | Already built except scheduling and the delete permission |
| 3+4 | Schedule for live and closing day | **New.** Auto-live at `publishAt`, auto-close at `unpublishAt`, manual override always available |
| 3+4 | Require admin right | **ADMIN required for DELETE only.** EDITOR keeps create, edit, publish, unpublish |
| 4 | Blog gallery / slider / grid / carousel | **New.** Ordered image set per post with captions, plus a per-post display style |
| 4 | Blog banner upload | Already built (cover image) |
| - | Donation page | Static "coming soon" message with an enable/disable toggle. **No payment processing in this phase** |

Explicitly **out of scope**: online donation checkout, section 88 tax receipts, recurring
giving, donor data capture. See section 7.

---

## 1. Blockers to clear before starting

**1. The admin password is unknown.** `app_user` was reseeded from `data.sql` on 29 Aug, so the
bcrypt hash created imperatively on 6 Aug is gone and the current credential is untested. Verify
a login works before building features into a CMS nobody can open. If it fails, reset the hash
directly against the prod database.

**2. There are no database backups.** This is already the largest gap in the whole setup and the
database now holds the client's real content. It is not strictly a Phase 1 blocker, but it
becomes an absolute blocker for anything donation-related, so it is cheaper to solve now.

**3. CRLF churn in the working tree.** `git status` shows ~57 modified files, only ~6 with real
changes. Land `.gitattributes` (`* text=auto eol=lf`) plus a renormalise **as its own commit,
first**, or every commit in this plan carries a 5,000-line whitespace diff and `git blame` is
destroyed.

---

## 2. Work item A - publish scheduling

### Design decision: read-time window, not a scheduled job

The backend has no scheduler at all - no `@EnableScheduling`, no `@Scheduled` anywhere. Do not
add one for this.

`HomepageBannerService.publicList` already solves this problem correctly: it filters on
`active && startsAt <= now && endsAt >= now` at query time. A cron job that flips a status column
can be missed during a restart, fail silently, or run twice; a read-time filter cannot drift, has
no moving parts, and behaves correctly the instant the server comes back up. Copy the banner
pattern.

### Effective visibility rule

`status` stays as the manual override. The window only narrows visibility, it never overrides a
DRAFT.

| status | publishAt | unpublishAt | Public? |
|---|---|---|---|
| DRAFT | any | any | No |
| PUBLISHED | null | null | Yes (current behaviour, unchanged) |
| PUBLISHED | future | any | No, until that time |
| PUBLISHED | past or null | future or null | Yes |
| PUBLISHED | any | past | No |

This keeps every existing row behaving exactly as it does today, because both new columns
default to null.

### Schema - `V4__publish_window.sql`

```sql
ALTER TABLE post         ADD COLUMN publish_at timestamptz, ADD COLUMN unpublish_at timestamptz;
ALTER TABLE event        ADD COLUMN publish_at timestamptz, ADD COLUMN unpublish_at timestamptz;
ALTER TABLE job_posting  ADD COLUMN publish_at timestamptz, ADD COLUMN unpublish_at timestamptz;

CREATE INDEX idx_post_visibility        ON post        (status, publish_at, unpublish_at);
CREATE INDEX idx_event_visibility       ON event       (status, publish_at, unpublish_at);
CREATE INDEX idx_job_posting_visibility ON job_posting (status, publish_at, unpublish_at);
```

Note the prod backend runs Hibernate `validate`, so the migration must ship in the same image as
the entity change or the pods crash-loop on boot. This exact failure already happened on 6 Aug.

### Backend changes

**Entities** - add `publishAt` / `unpublishAt` (`Instant`) to `Post`, `Event`, `JobPosting`.

**The critical part - public queries.** These currently filter on status alone. If only the
editors are updated, scheduled content goes live immediately and the whole feature is a lie.

| File | Method | Change |
|---|---|---|
| `PostRepository` | `findByStatus`, `findByStatusAndCategoryKey`, `findBySlugAndStatus`, both `findFirstByStatusAndPublishedAt*` (prev/next) | Add the window predicate |
| `EventRepository` | `findByStatusOrderByStartsAtDesc`, `findBySlugAndStatus` | Add the window predicate |
| `JobPostingRepository` | `findByStatusOrderByPostedAtDesc`, `findBySlugAndStatus` | Add the window predicate **and** `closesAt` |
| `routes/sitemap[.]xml.jsx` | - | Feeds off the public API, so it inherits the fix. Verify it does |

Derived query names get unreadable fast here. Use `@Query` with a named `:now` parameter and a
single shared predicate string per entity.

**Job closing dates.** `JobPosting.closesAt` is stored and displayed today but never enforced, so
expired postings stay live forever. Fold it into the same predicate:
`status = PUBLISHED AND window AND (closes_at IS NULL OR closes_at >= :now)`. Keep `closesAt` as
the donor-facing "applications close on" date and `unpublishAt` as the visibility control - they
are different ideas and should not be merged into one field.

**DTOs** - add both fields to `PostUpsertRequest`, `EventUpsertRequest`, `JobUpsertRequest` and
their admin detail DTOs. Validate `unpublishAt > publishAt` in the service, mirroring the existing
check in `HomepageBannerService.upsert`.

### Admin UI changes

Add a "Publish window" block to `post-edit.jsx`, `event-edit.jsx` and `job-edit.jsx`. **Reuse the
`toInput` / `toIso` helpers from `banners.jsx` verbatim** - they already handle the
`datetime-local` to UTC conversion pinned to `Asia/Hong_Kong`, which is the single most likely
place to introduce a silent off-by-eight-hours bug.

In the list screens, the status badge must reflect the effective state, not the raw column, or
the client will see "Published" next to something not on the site. Four states: **Draft**,
**Scheduled** (published, publishAt in future), **Live**, **Expired** (unpublishAt or closesAt
in the past).

On the events editor, label the two date pairs unmistakably. `startsAt`/`endsAt` is *when the
event happens*; `publishAt`/`unpublishAt` is *when the page is visible*. Four datetime fields on
one form is the usability risk of this work item - group and label them, don't just stack them.

### Acceptance criteria

- A post with `publishAt` one hour ahead is absent from `/blog` and 404s on its slug, then
  appears with no restart and no job run.
- A post with `unpublishAt` in the past disappears from the list, the sitemap, and prev/next.
- An existing post with both fields null behaves exactly as before.
- A job past `closesAt` drops off `/careers`.
- Times entered as HK time round-trip correctly after a save and reload.

---

## 3. Work item B - ADMIN-only delete

Currently only `/api/admin/users/**` is ADMIN-only; everything else is
`hasAnyRole("ADMIN", "EDITOR")`, so an EDITOR can hard-delete any content.

```java
.requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasRole("ADMIN")
```

placed **before** the existing `/api/admin/**` rule. Applies to posts, events, jobs, pages,
categories, banners and media - confirm that breadth is wanted; the client said "delete the post
only", which most likely means content deletion in general.

Frontend: hide delete buttons when the JWT roles claim lacks ADMIN (`layout.jsx` already reads
`user.roles`), and make `adminApi` surface a 403 as "Only an administrator can delete this"
rather than a raw error. Hiding the button is UX; the server rule is the actual control.

**Caveat worth knowing:** the JWT carries the roles claim and has a 720-minute TTL, so a role
change only takes effect at the user's next login.

---

## 4. Work item C - blog gallery and display style

### Current state

The `media` table already exists and is already post-scoped and ordered:

```
media(id, post_id, url, width, height, caption, sort_order)
```

`Gallery.jsx` and `Lightbox.jsx` on the public post page already render from it. What is missing
is the entire write path: `PostUpsertRequest` has no media field, so nothing in the CMS can
create, order, caption or remove those rows. The only workaround today is pasting markdown images
into the body.

So this is smaller than it looks - it is a write path plus a UI, not a new feature.

### Changes

**Schema `V5__post_gallery_style.sql`** - `ALTER TABLE post ADD COLUMN gallery_style varchar(20)
NOT NULL DEFAULT 'GRID';` Values: `CAROUSEL`, `GRID`, `MASONRY`.

**Backend** - add `List<PostMediaInput> media` (url, caption, sortOrder) and `galleryStyle` to
`PostUpsertRequest` and `AdminPostDetailDto`. In `PostService.upsert`, replace the media
collection wholesale (the `@OneToMany` already has `cascade = ALL, orphanRemoval = true`, so
clearing and re-adding is correct and safe).

**`MediaService.computeUsage` must also scan gallery rows.** It currently scans only post covers
and body markdown. Miss this and the media library will offer to delete an image that is live in
a gallery - which is exactly the bug the block-delete feature was built to prevent.

**Admin UI** - a gallery block in `post-edit.jsx`: multi-file upload, thumbnail strip, drag to
reorder, per-image caption, remove, plus a three-way display style selector.

**Public rendering** - `Gallery.jsx` gains the three style variants. **Hand this to the redesign,
don't build it twice.** The data model is Phase 1; what carousel, grid and masonry actually look
like is a design decision that belongs in the UI revamp. Ship the model and keep the current
rendering until the redesign lands.

---

## 5. Work item D - company info

### Current state

Settings is a raw key/value table: free-text key, free-text value, single-line input, no labels,
no validation, no per-language values, and no way to delete a key. The frontend consumes exactly
five keys today - `address`, `phone`, `email`, `instagram_url`, `facebook_url`, `youtube_url` -
read once by the lang-layout loader and used by `Masthead.jsx`, `Footer.jsx` and
`SocialLinks.jsx`.

### Approach

Keep the `site_setting(key, value)` table exactly as it is - it is fine, and changing it would
break the loader. Add a **definition list in code**: key, label, group, input type, and whether
it is trilingual. The form renders from that definition; unknown keys still in the database are
shown under an "Advanced" section so nothing is silently lost.

Trilingual values use a key suffix convention, consistent with `HomepageBanner`'s columns:
`address_zh_hant`, `address_en`, `address_zh_hans`. **The existing bare `address` key must keep
working** - resolve as `<key>_<lang>`, falling back to `<key>_zh_hant`, then bare `<key>`.

Proposed groups:

- **Contact** - phone, email, address (trilingual), office hours (trilingual)
- **Social** - Instagram, Facebook, YouTube URLs (validate they parse as URLs)
- **Organisation** - name (trilingual), tagline (trilingual), logo (upload via the media
  library), charity registration number
- **Donation** - see work item E

Long values (address, hours, tagline) need a textarea, not a single-line input.

---

## 6. Work item E - donation page "coming soon"

No payment processing exists and none is being built in this phase. What ships is the switch and
the placeholder that the real flow will later replace.

- Settings keys: `donation_payments_enabled` (boolean) and `donation_notice_zh_hant` /
  `_en` / `_zh_hans` (editable trilingual message, default "coming soon").
- The `/p/donate` page renders the notice block when the flag is off. When it is later turned on,
  the same flag switches to the real payment section - so the toggle written now is the toggle
  the payment work will reuse.
- Surface it as a clear on/off control in the Settings "Donation" group, not a free-text key.

**One ambiguity to confirm with the client:** does the toggle hide the *payment section* (page
still reachable, shows the notice), or the *whole donation page and its nav entry*? This plan
assumes the first, because it is what "instead of a fee selection and payment process" describes,
and because hiding the nav item would leave the mega-menu with an empty parent. Note that Pages
already have publish/unpublish, but unpublishing `/p/donate` produces a 404, not a notice - which
is not what was asked for.

---

## 7. Deferred, and what it will need when it returns

Online donation checkout is deferred because the client has not chosen payment methods. When it
comes back, these are the prerequisites, not the implementation:

1. **A merchant account in 樂橋有限公司's legal name.** KYC, charity registration documents, a
   bank account. Weeks of lead time and entirely a client task - start it before the code.
2. **Database backups.** Non-negotiable once the database holds financial records.
3. **Card data must never touch these servers.** A hosted checkout keeps the site in the lightest
   PCI tier and means the payment step visually leaves the site - a constraint the donate page
   design has to accommodate.
4. **Section 88 receipts.** The client leaned towards wanting them. Numbered PDF receipts emailed
   for qualifying donations, which means a receipt sequence, PDF generation and outbound email -
   none of which exists today. Confirm the exact obligation with the charity's accountant rather
   than inferring it.
5. **Donor personal data** brings a retention policy and a privacy notice on the form.

Also deferred: recurring giving, designated donations by programme.

---

## 8. Suggested commit order

Each step should build and deploy on its own.

1. `.gitattributes` + renormalise. Alone. No functional change.
2. Verify admin login; reset the password hash if needed. No commit.
3. **B** - ADMIN-only delete. One line plus UI gating. Smallest safe change, ships in an hour.
4. **A** - publish scheduling. Migration + entities + repository predicates + three editors +
   list badges. The largest item and the one with real regression risk on the public site.
5. **C** - gallery write path and `gallery_style`. Public rendering variants left to the redesign.
6. **D** - settings definition list and grouped form.
7. **E** - donation toggle and notice. Depends on D.

Deploy note: the cluster is a **single node with ~440m of CPU request headroom**. Nothing in this
plan adds a pod, so it should not trigger a second node - but do not add a scheduler sidecar,
worker or cron pod without checking that budget first. This is the reason work item A avoids a
background job, quite apart from the correctness argument.

## 9. Verification checklist before calling Phase 1 done

- [ ] Login works for both an ADMIN and an EDITOR account
- [ ] EDITOR cannot delete anything; the API returns 403 and the UI hides the buttons
- [ ] A scheduled post, event and job each appear and disappear on time with no restart
- [ ] An expired job is gone from `/careers`
- [ ] Pre-existing content with null windows is unaffected on the live site
- [ ] `sitemap.xml` never lists scheduled or expired content
- [ ] A gallery survives a save, reload and reorder, in all three languages
- [ ] The media library refuses to delete an image used in a gallery
- [ ] Contact, social and organisation fields render on the public shell in all three languages
- [ ] The donation toggle flips the page between notice and placeholder without a deploy
