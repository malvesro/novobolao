#!/bin/bash
# Script para regenerar o SQL da Copa 2026 a partir do CSV normalizado
# Autor: Antigravity AI
# Data: 2026-03-28

echo "----------------------------------------------------------"
echo "Iniciando geração do SQL de carga da Copa 2026..."
echo "----------------------------------------------------------"

# Caminhos padrão
INPUT_CSV="data/copa2026_tabela_brt_normalizado.csv"
OUTPUT_SQL="data/sql/03-copa-2026-data.sql"
PYTHON_SCRIPT="scripts/atualizar_copa2026_dataset.py"

# Executa o script de geração
python3 "$PYTHON_SCRIPT" \
  --input "$INPUT_CSV" \
  --output-sql "$OUTPUT_SQL"

if [ $? -eq 0 ]; then
    echo "----------------------------------------------------------"
    echo "SUCESSO! SQL gerado com êxito."
    echo "Local: $OUTPUT_SQL"
    echo "----------------------------------------------------------"
else
    echo "----------------------------------------------------------"
    echo "ERRO! Falha ao gerar o script SQL."
    echo "Verifique os logs acima para detalhes."
    echo "----------------------------------------------------------"
    exit 1
fi
