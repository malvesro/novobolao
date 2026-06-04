#!/bin/bash
cd /home/rosner/projetosgit/novobolao
echo "--- containers ---"
docker ps --format "table {{.Names}}\t{{.Status}}"
echo "--- versao ---"
curl -sk https://localhost:8443/login.action | grep -o 'Vers[^ ]*[^<]*' | head -1
