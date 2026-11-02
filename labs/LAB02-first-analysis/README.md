# LAB 02 – First analysis: Maven and SonarScanner CLI

> **Story:** Before touching any pipeline you want to know how bad things are. You analyse both BookShelf services from your laptop (the lab VM) – Java with the Maven plugin, Python with SonarScanner CLI.

| | |
|---|---|
| **Duration** | ~40 min |
| **You use** | `work/bookshelf-java/pom.xml`, `work/bookshelf-py/sonar-project.properties` |

## Step 1 – Create the projects

Projects can be created automatically by the first analysis, but doing it explicitly lets you control the key, name and new-code definition.

1. **Projects → Create project → Local project**
   - Display name `BookShelf Java`, key `bookshelf-java`, main branch `main`.
   - New code: **Previous version** → *Create project*.
2. Repeat for `BookShelf Python` / `bookshelf-py`.
3. On the *Analysis method* page choose **Locally** – we only need a token.

## Step 2 – A token for analyses

Tokens replace passwords for every tool talking to SonarQube.

1. **My Account (top right) → Security → Generate token**
   - Name `lab-laptop`, Type **Global Analysis Token**, Expires in 30 days.
2. Copy it (it is shown only once) and export it in your shell:

   ```bash
   export SONAR_TOKEN=sqa_...
   ```

| Token type | Prefix | Can do |
|---|---|---|
| User token | `squ_` | everything the user can do – avoid in CI |
| Project analysis token | `sqp_` | analyse **one** project |
| Global analysis token | `sqa_` | analyse any project (and create new ones if allowed) |

## Step 3 – Java: the Maven plugin

Look at `work/bookshelf-java/pom.xml`: the `sonar.*` properties and the pinned `sonar-maven-plugin`. Then:

```bash
./bin/lab scan-java
```

(The helper runs `mvn -B verify sonar:sonar -Dsonar.host.url=http://sonarqube:9000 -Dsonar.token=…` in a Maven container.)

✅ **Checkpoint:** `Tests run: 5, Failures: 0` and `ANALYSIS SUCCESSFUL, you can find the results at: http://localhost:9000/dashboard?id=bookshelf-java`.

## Step 4 – Python: SonarScanner CLI

```bash
cat work/bookshelf-py/sonar-project.properties
./bin/lab scan-py
```

(The helper runs the official `sonarsource/sonar-scanner-cli` image with `SONAR_HOST_URL` and `SONAR_TOKEN`.)

✅ **Checkpoint:** `ANALYSIS SUCCESSFUL` for `bookshelf-py`.

## Step 5 – Read the results

For **bookshelf-java** answer:

1. How many issues per software quality (Security / Reliability / Maintainability)? Which severities?
2. Open the **Blocker** issue in `BookRepository.java`. Read *Why is this an issue?* and *How can I fix it?*
3. Find the hard-coded password. Why is it a *Security* issue and not a code smell?
4. What does the quality gate show – and why is it green although there are blockers?

<details><summary>Answers</summary>

- ~12 issues: resources not closed (S2095, Blocker), string compared with `==` (S4973), hard-coded credentials (S2068 / S6437), SQL built by concatenation (S2077), predictable `Random` (S2245), MD5 (S4790).
- In current SonarQube the former "security hotspot" rules are reported as **vulnerabilities** with a security impact.
- The *Sonar way* gate only judges **new code**. On the first analysis everything is the baseline, so the gate passes. "Clean as You Code": fix what you touch, don't drown in legacy.
</details>

For **bookshelf-py**: why are there only two issues although `catalog.py` uses `eval`, `pickle` and `subprocess(shell=True)`? (Keep the question in mind – LAB04 and LAB05 answer it.)

## Step 6 – Fix one issue, re-analyse

1. In `work/bookshelf-java/src/main/java/com/bookshelf/PriceCalculator.java` replace `customerType == "VIP"` with `"VIP".equals(customerType)`.
2. Commit and analyse again:

   ```bash
   cd work/bookshelf-java && git commit -qam "Fix string comparison" && cd ../..
   ./bin/lab scan-java
   ```

3. **Activity** tab: compare the two analyses. The issue is now *Fixed* (closed automatically).

## Bonus

- Try `./bin/lab scan-py -X` – the debug log shows every resolved property and where it came from.
- `scan-java` with `-Dsonar.projectVersion=1.1` – what happens to "new code" (previous version)?

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| `export SONAR_TOKEN=<your analysis token> first` | the helper needs the token in this shell |
| `401` / `Not authorized` | token copied incompletely, expired or revoked |
| `SCM provider autodetection failed` | you are not in a Git working copy – use `work/`, not `apps/` |
| `java.nio.file.AccessDeniedException .scannerwork` | the scanner container runs as uid 1000 – your user has another uid; `chmod -R a+w work/bookshelf-py` |
