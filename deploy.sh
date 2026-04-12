#!/usr/bin/env bash

set -euo pipefail

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
``
