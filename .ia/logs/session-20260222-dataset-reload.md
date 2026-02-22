# Sessao: Reset e recarga do dataset Copa 2026

**Data:** 2026-02-22
**Autor:** Assistente Tecnico Lider/IA
**Skills Utilizadas:** modernization-java-migration v1.0.0

## 1. Prompt Principal (Input)

> O rodapé apresenta... Planeje, crie tarefas e faça passo a passo. (Subetapa: dados incorretos)

## 2. Resposta da IA (Output Resumido)

> Atualizado `scripts/atualizar_copa2026_dataset.py` para truncar tabelas dependentes (`PAI_PALPITE_INDIVIDUAL`, `BOI_BOLAO_INDIVIDUAL`, `PAL_PALPITE`, `JOG_JOGO`, `EQP_EQUIPE`) antes da carga. Regenerados CSV/SQL a partir do Excel oficial e aplicados ao MySQL usando `mysql --default-character-set=utf8mb4`, garantindo grafia correta (ex.: “França”).

## 3. Validacao (Build/Teste)

- Comando: `python3 scripts/atualizar_copa2026_dataset.py --output-sql data/sql/03-copa-2026-data.sql --output-csv data/copa2026_tabela_brt_final.csv`
- Comando: `docker compose exec -T db mysql --default-character-set=utf8mb4 -u root -p******** bolao < data/sql/03-copa-2026-data.sql`
- Comando: `docker compose exec db mysql --default-character-set=utf8mb4 -u root -p******** bolao -e "SELECT EQP_ID, EQP_PAIS FROM EQP_EQUIPE WHERE EQP_PAIS LIKE 'Fran%';"`
- Resultado: Sucesso
- Observacoes: EQP_EQUIPE registra `França`; dados antigos removidos.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Senhas omitidas do log (mascaradas no comando).
