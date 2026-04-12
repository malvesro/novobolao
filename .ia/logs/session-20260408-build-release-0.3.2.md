# Sessao: Release 0.3.2 - Build e Deploy Docker

## 1. Prompt Principal (Input)

Atualize a versão do sistema, refaça o build do frontend, do backend e publique novamente via docker compose.

## 2. Resposta da IA (Output Resumido)

- Versao aplicada: 0.3.2 em `pom.xml` e `package.json`/`package-lock.json`.
- ADR criado justificando a tabela dedicada de tokens de recuperacao (`.ia/historico/ADR-20260408-recuperacao-senha-token-db.md`).
- Build frontend executado com Vite.
- Build backend executado via Maven (WAR).
- Imagem Docker reconstruida e container atualizado via docker compose.

## 3. Validacao (Build/Teste)

- `npm install --no-audit --no-fund`
- `npm run build`
- `mvn -q -Dfrontend.skip=true clean package`
- `docker compose up -d --build`

## 4. Analise Humana (Veredito)

- Pendente
