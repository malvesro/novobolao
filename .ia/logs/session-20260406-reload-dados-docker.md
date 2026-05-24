# Sessao: Rebuild com recarga de dados (Docker Compose)

## 1. Prompt Principal (Input)
Solicitado refazer a carga de dados no rebuild para atualizar os jogos, com reset completo do volume do banco em Docker Compose.

## 2. Resposta da IA (Output Resumido)
- Identificado que a recarga exige remoção do volume `db_data` para reexecutar scripts `docker/mysql/init`.
- Executado shutdown completo com remoção de volumes e rebuild/restart via Docker Compose.
- Banco recriado e carga inicial reaplicada com containers saudáveis.
- Verificado que `JOG_JOGO` estava vazio após rebuild; carga manual de `docker/mysql/init/03-copa-2026-data.sql` aplicada via `docker exec`.

## 3. Validacao (Build/Teste)
- `docker compose down --volumes && docker compose up --build -d`
- `docker exec -i bolao-db mysql -uroot -p*** bolao < docker/mysql/init/03-copa-2026-data.sql`
- `SELECT COUNT(*) AS total_jogos FROM JOG_JOGO;` retornou **104**.

## 4. Analise Humana (Veredito)
A recarga de dados foi efetivada com reset do volume e rebuild do stack. A carga de jogos não foi aplicada automaticamente pelo init; a carga manual corrigiu o estado. Banco confirmado com jogos.

Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
Skill: `docker-expert v1.0.0`
