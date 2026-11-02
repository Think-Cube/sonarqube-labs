"""In-house checker: every source file must start with the BookShelf license header.

Writes findings in SonarQube's *generic issue import format* to stdout.
"""
import json
import pathlib
import sys

HEADER = "# Copyright BookShelf"


def main(root):
    issues = []
    for path in sorted(pathlib.Path(root).rglob("*.py")):
        first = path.read_text(encoding="utf-8").splitlines()[:1]
        if not first or not first[0].startswith(HEADER):
            issues.append({
                "ruleId": "license-header",
                "primaryLocation": {
                    "message": f"Add the license header '{HEADER} ...' as the first line.",
                    "filePath": path.as_posix(),
                    "textRange": {"startLine": 1},
                },
            })
    report = {
        "rules": [{
            "id": "license-header",
            "name": "Source files must start with the license header",
            "description": "Legal requires a license header in every source file.",
            "engineId": "bookshelf-license-check",
            "cleanCodeAttribute": "CONVENTIONAL",
            "impacts": [{"softwareQuality": "MAINTAINABILITY", "severity": "LOW"}],
        }],
        "issues": issues,
    }
    json.dump(report, sys.stdout, indent=2)


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else ".")
