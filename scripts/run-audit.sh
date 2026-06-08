#!/bin/bash
# Script para executar auditoria de dependências localmente

# Carrega a chave do arquivo local se existir
if [ -f ".nvd_api_key" ]; then
    export NVD_API_KEY=$(cat .nvd_api_key)
    echo "✅ NVD_API_KEY carregada de .nvd_api_key"
else
    echo "⚠️  Aviso: .nvd_api_key não encontrado. O build será lento."
fi

echo "🚀 Iniciando auditoria de dependências (OWASP Dependency Check)..."

# Executa o plugin ignorando o skip que está no Dockerfile
mvn dependency-check:check -Ddependency-check.skip=false

echo "--------------------------------------------------------"
echo "📊 Relatório gerado em: target/dependency-check-report.html"
echo "--------------------------------------------------------"
