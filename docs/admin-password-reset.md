# Admin password reset

Two different situations, two different procedures. Don't confuse them.

## Situation A — you can still log in as *an* admin

Use the API. No SQL needed.

```
PUT /api/admin/users/{id}/password
Authorization: Bearer <an ADMIN token>
Content-Type: application/json

{"newPassword": "the new password"}
```

`id` is the target `app_user.id` (`GET /api/admin/users` lists everyone). ADMIN-only —
see `SecurityConfig`. This is the normal way to rotate anyone's password, including
your own.

## Situation B — nobody can log in at all (the lockout case)

This is what actually happened once on this project: the only admin account's password
was lost and there was no working session to reset it through the API. Recovery is a
direct database update.

**`APP_ADMIN_PASSWORD_HASH` will not help here.** Look at `AdminUserInitializer`: it
only seeds the bootstrap admin when the account *doesn't exist yet*
(`!userRepository.existsByUsername(...)`). Once the `admin` row exists — which it does,
permanently, in prod — changing the env var and restarting the pod does nothing. The
row is already there, so the initializer skips it. That env var is for bootstrapping a
brand-new deployment, not for recovering a lost password on an existing one.

### 1. Generate a BCrypt hash of the new password

Any standard BCrypt generator works — Spring Security's `BCryptPasswordEncoder` accepts
`$2a$`, `$2b$` and `$2y$` hashes interchangeably. Two options that need nothing beyond
what's normally on a Linux box:

**Option 1 — `htpasswd`** (same tool `application.yml`'s own comment on
`APP_ADMIN_PASSWORD_HASH` already points at):

```bash
htpasswd -bnBC 10 "" 'the-new-password' | tr -d ':\n'
```

**Option 2 — Python, if `bcrypt` is installed** (`pip install bcrypt` if not):

```bash
python3 -c "import bcrypt; print(bcrypt.hashpw(b'the-new-password', bcrypt.gensalt(rounds=10)).decode())"
```

Either way you get back a single line starting `$2a$10$…` or `$2b$10$…`. That's the
hash — never the plaintext password — that goes in the database.

### 2. Update the row directly

```sql
UPDATE app_user
   SET password_hash = '<the hash from step 1>',
       updated_at = now()
 WHERE username = 'admin';
```

Connect however you already connect for anything else on this project (`kubectl exec`
into the Postgres pod, a port-forward + `psql`, etc. — see `docs/k3s-deployment-checklist.md`
/ `docs/azure-aks-setup.md` for cluster access). Confirm the account is still active
while you're in there:

```sql
SELECT username, is_active FROM app_user WHERE username = 'admin';
```

If `is_active` is `false`, that's a separate problem — `UserService` refuses to disable
the last enabled ADMIN, so if you're seeing this, either there's more than one admin
account or something bypassed the guard. Re-enable with
`UPDATE app_user SET is_active = true WHERE username = 'admin';` and then create a
second admin account once you're back in, so a single lost password can't lock
everyone out again.

### 3. Log in and rotate the password again through the API

The SQL step exists purely to regain access. Once `POST /api/auth/login` works again,
use Situation A's endpoint to set the *real* password you intend to keep — don't leave
the account on the value you typed into a shell command (`htpasswd`/`python3 -c` leave
plaintext passwords in your shell history unless you take care to avoid that).

## What must never happen

- Never commit a real password or a real password hash to this repository, in this doc
  or anywhere else. Every hash above is an example placeholder, not a working
  credential.
- Never put a credential in a Flyway migration. `V2__reference_data.sql` says exactly
  why: *"app_user — the bootstrap admin is created by AdminUserInitializer from an env
  var. A credential in a SQL file is exactly how the previous admin password got
  clobbered by a reseed."* The SQL in this doc is something a human runs by hand once,
  during an incident — it is not something that goes in `db/migration/`.
