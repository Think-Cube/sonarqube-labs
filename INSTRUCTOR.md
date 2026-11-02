# Instructor guide

## Before the course

1. One VM per participant: Ubuntu 24.04, **4 vCPU / 8 GB RAM / 30 GB**, Docker + Compose, `vm.max_map_count=524288` persisted in `/etc/sysctl.d/`.
2. Clone the repo and pre-build/pull everything (the first `up` builds Jenkins with plugins – ~3–5 min):

   ```bash
   chmod +x bin/* && ./bin/lab up
   docker pull maven:3.9-eclipse-temurin-21 && docker pull python:3.12-slim && docker pull sonarsource/sonar-scanner-cli
   ./bin/lab down
   ```

3. Pin versions in `env/.env` (`SONARQUBE_TAG`, `GITEA_TAG`). Community Build releases monthly – check the release notes for changed rules or profiles.
4. LAB07: tell participants a week ahead to bring a GitHub account **or** access to an Azure DevOps organisation (they need to create a project and register a self-hosted agent). Otherwise demo it.
5. Internet access is needed for Maven Central, PyPI, the Jenkins scanner auto-install and GitHub/Azure.

## Suggested schedule (2 days, 9:00–16:00)

| | Day 1 – Foundations | Day 2 – DevOps integration & operations |
|---|---|---|
| 09:00 | Intro: code quality, SonarQube family, architecture | Recap + quiz |
| 09:45 | **LAB01** environment & tour | DevOps integration: Jenkins → **LAB06** |
| 10:45 | ☕ | ☕ |
| 11:00 | Scanners & analysis → **LAB02** | GitHub Actions / Azure DevOps → **LAB07** |
| 12:00 | Tests, coverage, parameters → **LAB03** | 🍽 |
| 12:45 | 🍽 | Security → **LAB08** |
| 13:30 | Third-party analyzers → **LAB04** | Web API & custom rules → **LAB09** |
| 14:30 | ☕ | ☕ |
| 14:45 | Profiles & gates → **LAB05** | Troubleshooting → **LAB10** |
| 15:45 | Wrap-up day 1 | Best practices, next steps, survey |

## Teaching notes

- **SonarQube 26.x specifics** (verified on 26.10): built-in profiles are *Sonar way core* (default), *extended*, *comprehensive* and *Sonar agentic AI*; there are **no** rules of type *Security Hotspot* any more – former hotspot rules report vulnerabilities. The *Sonar way* gate still contains the "hotspots reviewed" condition (harmless). Instance runs in MQR mode by default.
- **LAB02** – the green gate despite blockers is the key "Clean as You Code" moment. Python shows only 2 native issues with *core* – keep the tension until LAB04/05.
- **LAB03** – the < 20 new lines rule surprises everybody; let them discover it.
- **LAB04** – Bandit finds `eval`, `pickle`, `shell=True` that the Community Build Python analyzer doesn't report: good discussion about complementary tools.
- **LAB05** – the gate fails on overall coverage (≈ 40 % < 50 %). Don't let them "fix" it with exclusions. The Java profile raises Cognitive Complexity on the method edited in LAB02 – it is new code and fails *Sonar way*; make sure everybody **accepts** it, otherwise the first Jenkins build in LAB06 is red for the wrong reason.
- **LAB06** – most common mistakes: *Enable local webhooks validation* still on (webhook to `jenkins` rejected as a private address), webhook URL without trailing `/`, `localhost` instead of container names, tool/server names not matching the Jenkinsfile.
- **LAB08** – Alice also sees `bookshelf-java`: public by default, and `sonar-users` keeps *Browse* on private projects created from the default template. Good moment to show why templates matter.
- **LAB07** – self-hosted runner is the only way to reach a private SonarQube; discuss the security trade-off. Community Build has no PR decoration/branch analysis.
- **LAB09** – stress the difference between public and *internal* API endpoints.

## LAB10 – solutions

| # | Root cause | Symptom in the log | Fix |
|---|---|---|---|
| BF1 | `sonar.sources=src` – folder doesn't exist | `ERROR Invalid value of sonar.sources` / `The folder 'src' does not exist` | `sonar.sources=bookshelf` |
| BF2 | coverage report path `coverage.xml` instead of `reports/coverage.xml` | `WARN No report was found for sonar.python.coverage.reportPaths using pattern coverage.xml`, coverage 0 % | correct path |
| BF3 | `sonar.sources=.` without `sonar.tests` – tests and tools analysed as production code; coverage paths no longer match | 14 files indexed, ~155 LOC instead of ~100 – tests and `tools/` count as code, coverage is calculated over them too | `sonar.sources=bookshelf`, `sonar.tests=tests` |
| BF4 | project key `bookshelf-python` – the `sqp_` token is bound to `bookshelf-py` | `ERROR You're not authorized to analyze this project or the project doesn't exist…` | `sonar.projectKey=bookshelf-py` |
| BF5 | Bandit and Pylint report paths swapped | `ERROR No issues information will be saved as the report file '…/pylint.txt' can't be read` | swap the two paths |
| BF6 | `sonar.exclusions=bookshelf/**` – production code excluded, 0 LOC → green gate | only 3 files indexed (tests), no lines of code | remove the exclusion; bonus: set exclusions centrally in project settings and restrict who may administer the project |
