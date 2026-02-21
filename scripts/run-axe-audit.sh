#!/usr/bin/env bash
set -euo pipefail

OUT_DIR="reports/axe"
BASE_URL=${1:-"http://localhost:8080/novobolao"}
CHROME_PATH=${2:-"/usr/bin/chromium-browser"}
shift 2 || true

if [ $# -gt 0 ]; then
  CHROME_ARGS=("$@")
else
  CHROME_ARGS=("--no-sandbox" "--disable-dev-shm-usage")
fi

CHROME_OPTS_STR="${CHROME_ARGS[*]}"

mkdir -p "$OUT_DIR"

PAGES=(
  "login.jsp"
  "seguro/principal.jsp"
  "seguro/jogos.jsp"
  "seguro/admin/inclusaoJogo.jsp"
)

for page in "${PAGES[@]}"; do
  file_name=$(echo "$page" | tr '/' '-' | tr '.' '-')
  report_file="$OUT_DIR/axe-$file_name.json"
  log_file="$OUT_DIR/axe-$file_name.log"

  echo "Running axe for $BASE_URL/$page"
  if ! npx axe "$BASE_URL/$page" --chrome-path "$CHROME_PATH" --chrome-options="$CHROME_OPTS_STR" --save "$report_file" \
    >"$log_file" 2>&1; then
    echo "Falha na auditoria de $page. Consulte $log_file" >&2
  else
    echo "Relatório salvo em $report_file (log: $log_file)"
  fi
done

echo "Auditoria concluída. Relatórios em $OUT_DIR"
