#!/usr/bin/env bash
set -euo pipefail

OUT_DIR="reports/axe"
BASE_URL=${1:-"http://localhost:8080/novobolao"}
CHROME_PATH=${2:-"/usr/bin/chromium-browser"}

mkdir -p "$OUT_DIR"

PAGES=(
  "login.jsp"
  "seguro/principal.jsp"
  "seguro/jogos.jsp"
  "seguro/admin/inclusaoJogo.jsp"
)

for page in "${PAGES[@]}"; do
  file_name=$(echo "$page" | tr '/' '-' | tr '.' '-')
  echo "Running axe for $BASE_URL/$page"
  npx axe "$BASE_URL/$page" --chromium-path "$CHROME_PATH" --save "$OUT_DIR/axe-$file_name.json"
done

echo "Auditoria concluída. Relatórios em $OUT_DIR"
