#!/bin/bash
set -e
cd /home/rosner/projetosgit/novobolao
echo "=== mvn clean package ==="
mvn clean package -Dfrontend.skip=true -q
echo "=== docker compose build ==="
docker compose build app 2>&1 | tail -5
echo "=== docker compose up ==="
docker compose up -d app 2>&1
echo "=== aguardando Tomcat ==="
sleep 12
echo "=== verificando versao ==="
curl -sk https://localhost:8443/login.action | grep -o 'Versão [^<]*'
