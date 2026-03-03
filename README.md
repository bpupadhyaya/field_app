# Field App
[![CI](https://github.com/bpupadhyaya/field_app/actions/workflows/ci.yml/badge.svg)](https://github.com/bpupadhyaya/field_app/actions/workflows/ci.yml)
[![CodeQL](https://github.com/bpupadhyaya/field_app/actions/workflows/codeql.yml/badge.svg)](https://github.com/bpupadhyaya/field_app/actions/workflows/codeql.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Open-source field operations platform with role-based access, device operations, runtime telemetry, and admin/super-admin workflows.

## Tech Stack
- Frontend: React + Vite
- Backend: Java 21 + Spring Boot
- DB: PostgreSQL
- Infra: Docker Compose + Kubernetes manifests

## Default Credentials (Local Dev)
- sadmin / sadmin123
- sadmin1 / sadmin123
- sadmin2 / sadmin123
- sadmin3 / sadmin123
- admin / admin123
- admin1 / admin123
- admin2 / admin123
- admin3 / admin123
- manager1 / manager123
- manager2 / manager123
- manager3 / manager123
- user1 / user123
- user2 / user123
- user3 / user123
- dealer1 / dealer123
- dealer2 / dealer123
- dealer3 / dealer123

## Quick Start
```bash
cd /Users/bhimupadhyaya/coding_common/misc/field_app
./scripts/run-local.sh
```
- Frontend: http://localhost:5174
- Backend health: http://localhost:8084/api/health

## Features
- JWT login
- RBAC roles (super admin, admin, manager, user, dealer)
- Device search/list, control command, price update, price history
- Device essential and health endpoints
- User list/create + whoami
- Admin: snapshots capture/list/restore/cleanup
- Admin: runtime online/offline sessions + traffic + audit page
- Super admin: cloud topology + AI management placeholders
- SPA top menu with Equipment/Finance/Parts/Digital/Company drop-down pages
- Layout modal for table/chart visibility

## Local Quality Checks
```bash
cd frontend && npm ci && npm run test:coverage && npm run build
cd ../backend && mvn test jacoco:report
```

## Kubernetes Local Test (Minikube or Docker Desktop)
```bash
./scripts/build-k8s-images.sh
kubectl apply -f infra/k8s/postgres.yaml
kubectl apply -f infra/k8s/backend.yaml
kubectl apply -f infra/k8s/frontend.yaml
```

## Open Source

- License: [MIT](LICENSE)
- Contributing: [CONTRIBUTING.md](CONTRIBUTING.md)
- Code of Conduct: [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)
- Security: [SECURITY.md](SECURITY.md)
- CI: [.github/workflows/ci.yml](.github/workflows/ci.yml)
- Security Scanning: [.github/workflows/codeql.yml](.github/workflows/codeql.yml)
- Dependency Updates: [.github/dependabot.yml](.github/dependabot.yml)
