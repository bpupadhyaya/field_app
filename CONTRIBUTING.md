# Contributing

## Prerequisites

- Java 21
- Node.js 20+
- Docker and Docker Compose (recommended for full stack)

## Development Setup

1. Fork and clone the repository.
2. Create a branch from `main` using prefix `codex/` or `feature/`.
3. Start the stack:
   - Docker: `./scripts/run-local.sh`
   - Or run services manually (frontend + backend + PostgreSQL)

## Quality Gates

Run these before opening a pull request:

1. Frontend build:
   - `cd frontend && npm ci && npm run build`
2. Backend tests/build:
   - `cd backend && mvn test`

## Pull Request Guidelines

1. Keep PRs focused and small.
2. Add or update docs when behavior changes.
3. Include screenshots for UI changes.
4. Mention any breaking changes clearly.

## Commit Style

Use short imperative commit messages, for example:

- `fix: restrict admin tab visibility for manager role`
- `docs: add security policy and contribution guide`

## Reporting Bugs

Open an issue with:

- Expected behavior
- Actual behavior
- Reproduction steps
- Environment (OS, browser, Java/Node versions)
