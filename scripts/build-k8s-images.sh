#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/.."
docker build -t field-app-backend:latest backend
docker build -t field-app-frontend:latest frontend
