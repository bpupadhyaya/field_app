# Field App

## Stack
- Frontend: React + Vite
- Backend: Java 21 + Spring Boot
- DB: PostgreSQL
- Infra: Docker Compose + Kubernetes manifests

## Default users
- sadmin / sadmin123
- admin / admin123
- manager1 / manager123
- user1 / user123
- dealer1 / dealer123

## Local run
```bash
cd /Users/bhimupadhyaya/coding_common/misc/field_app
./scripts/run-local.sh
```
- Frontend: http://localhost:5174
- Backend health: http://localhost:8081/api/health

## Core implemented surfaces
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

## Kubernetes local test (minikube/docker-desktop)
```bash
./scripts/build-k8s-images.sh
kubectl apply -f infra/k8s/postgres.yaml
kubectl apply -f infra/k8s/backend.yaml
kubectl apply -f infra/k8s/frontend.yaml
```
