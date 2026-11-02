#!/usr/bin/env sh
# Runs tests + coverage and the third-party analyzers, writing reports to ./reports
# Usage (in a container):  docker run --rm -v "$PWD":/usr/src -w /usr/src python:3.12-slim sh run-checks.sh
set -u
mkdir -p reports
pip install --quiet --disable-pip-version-check pytest pytest-cov bandit ruff pylint

pytest --cov=bookshelf --cov-report=xml:reports/coverage.xml --junitxml=reports/junit.xml -q
bandit -r bookshelf -f json -o reports/bandit.json -q || true
ruff check bookshelf --output-format json -o reports/ruff.json || true
pylint bookshelf --exit-zero --output-format=parseable > reports/pylint.txt
python tools/license_check.py bookshelf > reports/license-issues.json
echo "reports written to ./reports"
