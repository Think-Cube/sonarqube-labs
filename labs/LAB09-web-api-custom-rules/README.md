# LAB 09 – Web API, custom rules & quality as code

> **Story:** Management wants a weekly quality report in a spreadsheet, the team lead wants a rule forbidding `FIXME` in production code, and the platform team wants gates and profiles reproducible from Git. All three are Web API jobs.

| | |
|---|---|
| **Duration** | ~40 min |
| **You use** | `curl`, `python3`, the built-in API docs at <http://localhost:9000/web_api> |

## Step 1 – Authenticate to the API

Create a **User token** (`squ_…`) in *My Account → Security* – the API acts with *your* permissions.

```bash
export SQ=http://localhost:9000
export SQ_TOKEN=squ_...
api() { curl -s -H "Authorization: Bearer $SQ_TOKEN" "$SQ/api/$1"; }

api system/status | python3 -m json.tool
api authentication/validate
```

Browse <http://localhost:9000/web_api>: every page of the UI uses these endpoints (*internal* ones may change without notice – don't build on them).

## Step 2 – Reporting: measures and issues

```bash
api "measures/component?component=bookshelf-java&metricKeys=ncloc,coverage,duplicated_lines_density,software_quality_security_issues,software_quality_reliability_issues,software_quality_maintainability_issues" \
  | python3 -m json.tool

api "qualitygates/project_status?projectKey=bookshelf-py" | python3 -c \
  'import sys,json; s=json.load(sys.stdin)["projectStatus"]; print(s["status"]); [print(" ",c["metricKey"],c["status"],c.get("actualValue")) for c in s["conditions"]]'
```

**Task:** write `report.sh` that prints one CSV line per project:

```
project,ncloc,coverage,security_issues,gate
bookshelf-java,206,27.2,7,OK
bookshelf-py,97,40.4,11,ERROR
```

Hints: `api "projects/search"` → keys; `issues/search?componentKeys=…&impactSoftwareQualities=SECURITY&ps=1` → `total`. Large result sets are paged (`p`, `ps`, max 10 000 issues per search).

## Step 3 – A custom rule from a template

SonarQube rules are implemented in analyzers (plugins), but some rules are **templates** you can instantiate with your own parameters.

```bash
api "rules/search?is_template=true&languages=py,java" | python3 -c \
  'import sys,json; [print(r["key"],"-",r["name"]) for r in json.load(sys.stdin)["rules"]]'
```

You get e.g. `python:CommentRegularExpression`, `java:S124` (comments), `java:S3688` (disallowed classes), `java:S2253` (disallowed methods).

Create a rule (needs *Administer Quality Profiles*):

```bash
curl -s -H "Authorization: Bearer $SQ_TOKEN" -X POST "$SQ/api/rules/create" \
  --data-urlencode "customKey=no-fixme" \
  --data-urlencode "templateKey=python:CommentRegularExpression" \
  --data-urlencode "name=No FIXME comments in production code" \
  --data-urlencode "markdownDescription=FIXME marks code known to be broken. Fix it or open a ticket." \
  --data-urlencode "params=regularExpression=(?i).*FIXME.*;message=Resolve this FIXME before merging" \
  | python3 -m json.tool | head -20
```

Activate it in your profile from LAB05:

```bash
PROFILE=$(api "qualityprofiles/search?language=py&qualityProfile=BookShelf%20Python" | python3 -c 'import sys,json;print(json.load(sys.stdin)["profiles"][0]["key"])')
curl -s -H "Authorization: Bearer $SQ_TOKEN" -X POST "$SQ/api/qualityprofiles/activate_rule" -d "key=$PROFILE&rule=python:no-fixme"
```

Add a `FIXME`, commit and analyse:

```bash
cd work/bookshelf-py
sed -i 's/^def order_total(lines):/def order_total(lines):\n    # FIXME: discounts are ignored/' bookshelf/orders.py
git commit -qam "Note discount bug" && cd ../..
./bin/lab scan-py
api "issues/search?componentKeys=bookshelf-py&rules=python:no-fixme" | python3 -c \
  'import sys,json; [print(i["component"], i["line"], i["message"]) for i in json.load(sys.stdin)["issues"]]'
```

✅ **Checkpoint:** `bookshelf-py:bookshelf/orders.py 38 Resolve this FIXME before merging` (line may differ).

**Java task:** create `java:no-system-exit` from `java:S2253` (*Track uses of disallowed methods*) forbidding `java.lang.System#exit`. (Look at the template's parameters with `api "rules/show?key=java:S2253"`.)

> Beyond templates: fully custom rules are written as **analyzer plugins** (e.g. the Java custom-rules API) and installed into `extensions/plugins` – powerful, but you then own a plugin across every SonarQube upgrade. Prefer templates, external analyzers (LAB04) or the generic format.

## Step 4 – Quality configuration as code

Export what you built:

```bash
mkdir -p quality-config
curl -s -H "Authorization: Bearer $SQ_TOKEN" \
  "$SQ/api/qualityprofiles/backup?language=py&qualityProfile=BookShelf%20Python" > quality-config/bookshelf-python.xml
api "qualitygates/show?name=BookShelf%20gate" | python3 -m json.tool > quality-config/bookshelf-gate.json
ls -l quality-config
```

Restore on another instance with `api/qualityprofiles/restore` (multipart upload of the XML) and recreate gates with `qualitygates/create` + `create_condition`. Commit `quality-config/` to Git – reviewable, reproducible, auditable.

## Bonus

- Bulk-change issues: tag every open `external_pylint:C0116` issue as `docs` with `api/issues/bulk_change`.
- Webhooks via API: `api/webhooks/list`, `api/webhooks/deliveries?webhook=…` – debug what SonarQube sent to Jenkins.
- Terraform: the community `sonarqube` provider manages projects, gates, profiles and permissions as code.

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| `401` | token missing/expired; header must be `Authorization: Bearer …` |
| `403` | your user lacks the permission (e.g. *Administer Quality Profiles*) |
| `400 … already exists` | custom key or condition exists – use `update_condition` / another key |
| custom rule produces no issue | not activated in the profile used by the project, or the regex doesn't match the whole comment |
