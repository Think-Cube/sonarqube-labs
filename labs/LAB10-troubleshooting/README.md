# LAB 10 – Troubleshooting break-fix 🔧

> **Story:** It's release week and the support channel is on fire. Six tickets about "SonarQube is broken" – in reality, every one of them is a configuration mistake. Fix them like an on-call engineer: read the logs, form a hypothesis, change one thing, verify.

| | |
|---|---|
| **Duration** | ~35 min (pairs work great) |
| **You edit** | `work/bookshelf-py/sonar-project.properties` (each scenario replaces it) |

Save your working configuration first:

```bash
cp work/bookshelf-py/sonar-project.properties /tmp/sonar-project.properties.good
./bin/lab checks-py          # fresh reports
```

## How to debug an analysis

| Where | What you learn |
|---|---|
| scanner output (add `-X` for debug) | resolved properties, indexed files, sensors, report import errors |
| **Project → Background Tasks** | did the compute engine process the report? errors and stack traces |
| **Administration → System → Logs** (`ce.log`, `web.log`, `es.log`) | server-side problems |
| `api/webhooks/deliveries?webhook=…` | what SonarQube sent to Jenkins and the response |
| `./bin/lab logs sonarqube` | startup problems (Elasticsearch, database) |

## The tickets

For each scenario: `./bin/lab breakfix <N>`, run `./bin/lab scan-py`, find the cause, fix `sonar-project.properties`, verify.

| # | Ticket | Hint (only if stuck) |
|---|---|---|
| BF1 | *The pipeline fails at the SonarQube step since we restructured the repo* | Read the first `ERROR` line. |
| BF2 | *Coverage dropped to 0 % although nobody deleted a test* | Search the log for `WARN`. |
| BF3 | *Lines of code doubled overnight and there are issues in our tests and tools* | Compare *Lines of Code* and the file list in the *Code* tab. |
| BF4 | *The new CI token does not work: "You're not authorized to analyze this project"* | Run it with the project analysis token from LAB08. What does a `sqp_` token allow? |
| BF5 | *Bandit and Pylint findings disappeared from SonarQube* | `ERROR … can't be read`. Which tool wrote which file? |
| BF6 | *Great news: the quality gate is green and we have zero issues!* | How many lines of code were analysed? |

✅ **Goal:** after each fix the scan succeeds and the dashboard shows the expected numbers (≈ 100 lines of code, the same coverage as before the scenario, external issues present).

Restore your configuration at the end:

```bash
cp /tmp/sonar-project.properties.good work/bookshelf-py/sonar-project.properties
```

## Server-side scenarios (discussion / demo)

| Symptom | Cause | Fix |
|---|---|---|
| Container restarts, `es.log`: `max virtual memory areas vm.max_map_count [65530] is too low` | host kernel setting | `sysctl -w vm.max_map_count=524288` (persist in `/etc/sysctl.d`) |
| Jenkins *Quality gate* stage waits until timeout | webhook missing / wrong URL / Jenkins unreachable | check *Webhooks → Recent deliveries* |
| Analysis OK but *Background task* failed | compute engine error (DB, disk, memory) | Background Tasks → *Show error details*, `ce.log` |
| `SCM provider autodetection failed`, no blame, everything is "new code" | analysis outside a Git checkout or shallow clone | full clone (`fetch-depth: 0`), run in the repository root |
| Upgrade: UI shows *maintenance mode* | database migration required | open `/setup` and confirm the migration (after a DB backup!) |
| Very slow analysis / OutOfMemoryError in the scanner | big project, low heap | `SONAR_SCANNER_JAVA_OPTS=-Xmx2g`, exclude generated code |

## Wrap-up

Which of the six tickets would a check in the pipeline have caught automatically? (Ideas: fail when `ncloc` drops > 30 %, require coverage report present, use `sonar.qualitygate.wait`, protect `sonar.exclusions` in project settings instead of the repository.)
