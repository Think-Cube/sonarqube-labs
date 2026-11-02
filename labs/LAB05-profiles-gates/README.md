# LAB 05 – Quality profiles & quality gates

> **Story:** The architects decided: *"The default profile is too lenient for us, and no code goes to production unless it is tested."* You turn that sentence into a quality profile and a quality gate – and watch the gate block a bad change.

| | |
|---|---|
| **Duration** | ~40 min |
| **You use** | SonarQube UI (+ Web API for the bonus) |

| Concept | Answers the question | Assigned to |
|---|---|---|
| **Quality profile** | *Which rules* are checked? | a language in a project |
| **Quality gate** | *Is this good enough* to release? (conditions on metrics) | a project |

## Step 1 – Compare the built-in profiles

**Quality Profiles → Python**. Compare *Sonar way core* with *Sonar way extended* (**⋯ → Compare**).

- How many rules does *extended* add? Which kind (security / reliability / maintainability)?
- Which profile would you pick for a 10-year-old monolith? For a greenfield service?

## Step 2 – Your own profile (inheritance)

1. **Create** → language *Python*, name `BookShelf Python`, **Parent: Sonar way extended**.
2. In the new profile **Activate more** and enable:
   - `python:S1135` – Track uses of "TODO" tags
   - `python:S125` – Sections of code should not be commented out
3. Find `python:S3776` (Cognitive Complexity) – inherited from the parent. **Change** its parameter *threshold* from 15 to **10**. Note the *overridden* marker.
4. **⋯ → Set as Default** for Python (or assign it only to `bookshelf-py` via **Project Settings → Quality Profiles**).

Re-analyse:

```bash
./bin/lab scan-py
```

✅ **Checkpoint:** new native issues appear – e.g. *Cognitive Complexity* in `shipping_cost`, mutable default argument in `add_tag`, bare `except` in `find_book`. Inheritance means: when *Sonar way extended* gets new rules in the next upgrade, `BookShelf Python` gets them too.

Do the same for Java: `BookShelf Java`, parent *Sonar way extended*, then `./bin/lab scan-java`. Which new issue appears? (Cognitive Complexity in `PriceCalculator.discountedPrice`.)

Look at the `bookshelf-java` overview: the issue counts as **new code** – you changed that method in LAB02 – so the *Sonar way* gate is now **Failed** (*1 new issue*). Refactoring the method is a job for another sprint: open the issue → **Accept** with the comment `Refactoring planned – BOOK-12`. Re-run `./bin/lab scan-java` – the gate is green again (accepted issues don't count). You need a green `bookshelf-java` in LAB06.

## Step 3 – Your own quality gate

1. **Quality Gates → Create** → `BookShelf gate`. It starts with the recommended conditions on new code.
2. Adjust and add conditions:

| On | Metric | Operator | Value |
|---|---|---|---|
| New code | Issues | is greater than | 0 |
| New code | Coverage | is less than | **60** |
| New code | Duplicated lines (%) | is greater than | 3 |
| Overall code | Coverage | is less than | **50** |
| Overall code | Blocker issues | is greater than | 0 |

3. **Projects → Select** → add `bookshelf-py` only (`bookshelf-java` stays on *Sonar way* – you need it in LAB06).

```bash
./bin/lab scan-py
```

✅ **Checkpoint:** the gate is **Failed** – overall coverage ≈ 40 % < 50 %, and the stricter profile found a new issue in the code you added in LAB03. Open the project overview: the failed conditions are listed.

## Step 4 – Make it green the right way

Options: write tests, lower the threshold, exclude files, or *accept* the risk. Discuss which ones are legitimate. Then add tests to `tests/test_orders.py`:

```python
def test_express_shipping_abroad():
    assert orders.shipping_cost(10, "DE", True, False, []) == 59.99


def test_invoice_and_packing():
    order = {"id": "42", "customer": "Ann", "address": "Main St 1",
             "lines": [{"title": "A", "price": 10.0, "qty": 1, "gift": True}]}
    assert "INVOICE 42" in orders.invoice_lines(order)
    assert "PACKING 42" in orders.packing_list(order)
```

Commit, `./bin/lab checks-py && ./bin/lab scan-py` – how close are you now? What else is needed?

## Step 5 – Issue workflow

Open a security issue in `bookshelf-py`:

- **Accept** – a real issue you decide not to fix now (comment required).
- **False positive** – the rule is wrong here.
- **Assign** to a user, add a tag (`tech-debt`), add a comment.

Which of these should a developer be allowed to do on their own? (→ LAB08 permissions)

## Bonus – the same with the Web API

```bash
AUTH="admin:<your admin password>"
curl -s -u "$AUTH" -X POST "http://localhost:9000/api/qualitygates/create" -d "name=API gate"
curl -s -u "$AUTH" -X POST "http://localhost:9000/api/qualitygates/create_condition" \
     -d "gateName=API gate&metric=coverage&op=LT&error=50"
curl -s -u "$AUTH" "http://localhost:9000/api/qualitygates/project_status?projectKey=bookshelf-py" | python3 -m json.tool
```

Gates and profiles can be backed up and restored (`api/qualityprofiles/backup` / `restore`) – keep them in Git like any other configuration.

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| new rules don't produce issues | profile not assigned to the project/language, or the analysis ran before the change |
| gate passes although coverage on new code is 0 % | fewer than 20 new lines – conditions on coverage/duplication are ignored |
| can't edit *Sonar way* | built-in profiles and gates are read-only – copy or inherit |
