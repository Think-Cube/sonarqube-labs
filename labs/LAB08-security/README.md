# LAB 08 – Securing SonarQube & handling security findings

> **Story:** An auditor asks two questions: *"Who can see your vulnerabilities?"* and *"What happens when SonarQube finds one?"* Today the answer to the first one is "everybody with the admin password". Fix that, then build a triage routine for security issues.

| | |
|---|---|
| **Duration** | ~35 min |
| **You use** | SonarQube **Administration** (as `admin`) |

SonarQube holds a map of your weaknesses – protect it like production.

## Step 1 – Baseline checks

**Administration → Configuration → General Settings → Security**

- *Force user authentication* – must be **on** (default). Verify:

  ```bash
  curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:9000/api/components/show?component=bookshelf-py"   # 401
  ```

- **Administration → Security → Users**: is anyone still using a default password? (The startup log warns while `admin/admin` is active.)

## Step 2 – Groups, users and least privilege

1. **Administration → Security → Groups → Create** `bookshelf-devs`.
2. **Users → Create**: login `alice`, name *Alice Developer*, password `Alice-Training-2026!`. Add her to `bookshelf-devs`.
3. **Project `bookshelf-py` → Project Settings → Permissions**
   - Visibility: **Private**.
   - `bookshelf-devs`: *Browse*, *See Source Code*, *Administer Issues*.
   - Remove *Browse* from the group `sonar-users` (everybody).
4. Log in as `alice` in a private browser window: she sees `bookshelf-py` – and `bookshelf-java` too. Why? New projects are **public** by default (**Administration → Projects → Management → Default visibility**), and even a private project keeps *Browse* for `sonar-users` from the default permission template. Making a project private is not enough – remove the permission from `sonar-users`, as you did for `bookshelf-py`, or use a template (Step 3).
5. Still as `alice`: can she change the quality gate? Can she mark an issue *False positive*?

## Step 3 – Permission templates

Manually setting permissions doesn't scale. **Administration → Security → Permission Templates → Create**

- Name `BookShelf projects`, project key pattern **`bookshelf-.*`**
- Grant `bookshelf-devs`: Browse, See Source Code, Administer Issues; `sonar-administrators`: Administer.

Every new project whose key matches the pattern gets these permissions automatically. Test: create a local project `bookshelf-web` and check its permissions.

## Step 4 – Tokens done right

1. As `admin`: **My Account → Security** – list your tokens. Revoke `lab-laptop` from LAB02 when you're done today.
2. Create a **Project Analysis Token** `py-ci` for `bookshelf-py` (prefix `sqp_`). Verify it can only analyse that project:

   ```bash
   SONAR_TOKEN=sqp_... ./bin/lab scan-py                                  # works
   SONAR_TOKEN=sqp_... ./bin/lab scan-java                                # Not authorized
   ```

3. **Administration → Security → Users → ⋯ → Tokens**: admins can see and revoke any user's tokens. Policy question: how long may a CI token live, and who rotates it? (Enterprise editions can enforce a maximum token lifetime.)

## Step 5 – Triage security findings

Open **Issues** for `bookshelf-java`, filter **Software quality = Security**.

For each finding decide and record:

| Issue | Exploitable here? | Decision |
|---|---|---|
| Hard-coded DB password (`BookRepository`) | | fix: env var / secret store |
| SQL built by concatenation | | fix: `PreparedStatement` |
| MD5 for passwords | | fix: bcrypt / Argon2 / PBKDF2 |
| `java.util.Random` for session tokens | | fix: `SecureRandom` |

Fix the token generator:

```java
private final java.security.SecureRandom random = new java.security.SecureRandom();
```

Commit, push, analyse. Use *Accept* (with a comment and a ticket number) only for risks the team consciously takes.

## Step 6 – Hardening checklist (discussion)

- HTTPS: SonarQube behind a reverse proxy (NGINX, Traefik, cloud LB) with `sonar.core.serverBaseURL` set.
- SSO: delegate authentication (SAML, LDAP, GitHub, GitLab) – and keep local accounts to a minimum. Check which methods your edition supports.
- Database: dedicated user, network isolation, backups; SonarQube data = source code snippets.
- Keep up to date: Community Build releases monthly; Server has an LTA release each year.
- Audit: who changed which gate/profile? (*Audit logs* are an Enterprise feature – in Community Build keep profiles and gates as code via the Web API.)

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| Alice still sees `bookshelf-java` | the project is public, or `sonar-users` has Browse |
| template not applied | key doesn't match the regex, or the project existed before the template |
| analysis fails with a user token after permission change | the user lost *Execute Analysis* |
