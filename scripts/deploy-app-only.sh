#!/bin/bash
set -e
cd /home/rosner/projetosgit/novobolao
echo "=== subindo apenas o container app ==="
docker compose up -d app --no-deps
echo "=== aguardando Tomcat (15s) ==="
sleep 15
echo "=== verificando versao ==="
curl -sk https://localhost:8443/login.action | grep -o 'Versão [^<]*'
