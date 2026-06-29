#!/usr/bin/env bash

set -euo pipefail

prepare_docker_buildx_writable_dirs() {
    local default_buildx_dir="${BUILDX_CONFIG:-${HOME}/.docker/buildx}"
    local default_activity_dir="${default_buildx_dir}/activity"
    if [ -d "$default_activity_dir" ] && [ -w "$default_activity_dir" ]; then
        return 0
    fi

    local fallback_buildx_dir="${PWD}/.tmp/buildx"
    export BUILDX_CONFIG="${BUILDX_CONFIG:-$fallback_buildx_dir}"
    mkdir -p "$BUILDX_CONFIG/activity"

    echo "ℹ️  Docker buildx em modo fallback: BUILDX_CONFIG=$BUILDX_CONFIG"
}

prepare_docker_buildx_writable_dirs

echo "========================================="
echo "🚀 Iniciando build e deploy do sistema"
echo "========================================="

echo
echo "📦 1/4 - Build do frontend (npm)"
npm run build

echo
echo "☕ 2/4 - Build do backend (Maven)"
mvn clean package -Dfrontend.skip=false

echo
echo "🐳 3/4 - Build da imagem Docker (app)"
docker compose build app

echo
echo "▶️ 4/4 - Subindo containers (app e db)"
docker compose up -d app db

echo
echo "✅ Deploy finalizado com sucesso!"
echo "========================================="

# Para acompanhar logs:
# docker compose logs -f app
