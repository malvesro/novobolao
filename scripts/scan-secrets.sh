#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v rg >/dev/null 2>&1; then
  echo "Ripgrep (rg) não encontrado. Instale antes de executar este script." >&2
  exit 2
fi

IGNORE_PATTERNS=(
  "--glob"
  "!**/target/**"
  "--glob"
  "!**/node_modules/**"
)

SUSPICIOUS_REGEX='(?i)(password|passwd|secret|api[_-]?key|token|authorization:|BEGIN RSA PRIVATE KEY)'
HARDCODED_HOST_REGEX='jdbc:mysql://[^:$]*:[0-9]+/[a-z0-9_-]+.*(user|password)|smtp.*(user|pass)'

echo "Executando varredura por padrões de segredos..."
rg --line-number --color never "${IGNORE_PATTERNS[@]}" "${SUSPICIOUS_REGEX}" || true

echo "Verificando strings de conexão com credenciais embutidas..."
rg --line-number --color never "${IGNORE_PATTERNS[@]}" "${HARDCODED_HOST_REGEX}" || true

echo "Verificação concluída. Analise os resultados acima para garantir que não há segredos versionados."
