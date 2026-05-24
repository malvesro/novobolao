# Plano — Script de Geração do SQL da Copa 2026

## 1. Objetivo
Automatizar a criação do script `03-copa-2026-data.sql` (e atualizações incrementais) a partir do dataset normalizado `data/copa2026_tabela_brt_normalizado.csv`, garantindo consistência com o modelo de dados do Sistema Bolão.

## 2. Entradas
1. `data/copa2026_tabela_brt_normalizado.csv`  
   - Colunas: `fase`, `fase_codigo`, `fase_ordem`, `grupo`, `rodada`, `data`, `data_iso`, `hora_brt`, `mandante`, `mandante_slot`, `visitante`, `visitante_slot`, `estadio`, `cidade`, `pais`, `fuso_lista`.
2. Arquivo auxiliar `data/copa2026_placeholders.json` (criado em 21/02/2026)  
   - Mapeia placeholders para seleções confirmadas e atualiza nomes de equipes e chaves.
3. Parâmetros CLI (ex.: `--output`, `--placeholders`, `--dry-run`).

## 3. Saídas
1. `data/sql/03-copa-2026-data.sql` (novo arquivo):
   - Inserts em `EQP_EQUIPE` (seleções) e `JOG_JOGO`.
   - Ordem por `fase_ordem`, `data_iso`, `hora_brt`.
   - Comentários com metadados (data de geração, origem do dataset, número de partidas).
2. `data/copa2026_tabela_brt_final.csv` (opcional):
   - Dataset com placeholders resolvidos após rodadas de atualização.
3. Diff/resumo impresso em stdout (quantidade de seleções/jogos inseridos/atualizados).

## 4. Requisitos Funcionais
1. **Carga de dados**
   - Ler o CSV normalizado preservando encoding UTF-8.
   - Validar colunas obrigatórias e formato de data/hora.
2. **Resolução de placeholders**
   - Aplicar mapa de substituição opcional (seleções definidas após playoffs).
   - Atualizar colunas `mandante`, `visitante`, `mandante_slot`, `visitante_slot`.
   - Recalcular grupos quando uma seleção substitui placeholder.
3. **Geração de entidades**
   - Derivar tabela de seleções únicas com `grupo`.
   - Definir IDs determinísticos (ex.: sequência incremental, ou mapear via `EQP_ID` existente).
   - Preparar statements `INSERT` com `ON DUPLICATE KEY UPDATE` (evita duplicidades).
4. **Geração de jogos**
   - Converter `data_iso` + `hora_brt` para `DATE`/`TIME`.
   - Mapear `mandante`/`visitante` para `EQP_ID`.
   - Definir `JOG_FASE` usando `fase_codigo` (ex.: 11/12/13 grupos, 16 para 32-avos, etc.).
   - Definir placeholders de gols como `NULL`.
5. **Validações**
   - Garantir 48 seleções e 104 partidas após dataset completo.
   - Alertar se existirem grupos incompletos (menos de 4 seleções) ou IDs inexistentes.
6. **CLI**
   - Flags sugeridas:  
     `--input <csv>` (default dataset normalizado)  
     `--placeholders <json>` (opcional)  
     `--output-sql <file>` (default `data/sql/03-copa-2026-data.sql`)  
     `--output-csv <file>` (opcional dataset final)  
     `--dry-run` (exibe resumo sem gravar arquivos).

## 5. Arquitetura do Script
```
main()
 ├─ carregar_config_cli()
 ├─ dataset = carregar_csv(path)
 ├─ dataset = aplicar_placeholders(dataset, mapa)
 ├─ selecoes = extrair_selecoes(dataset)
 ├─ jogos = extrair_jogos(dataset, selecoes)
 ├─ validar_dataset(selecoes, jogos)
 ├─ sql = gerar_sql(selecoes, jogos)
 ├─ salvar_sql(sql, path)
 ├─ if output_csv: salvar_csv_final(dataset, path)
 └─ imprimir_resumo(selecoes, jogos)
```

## 6. Validações Técnicas
- `fase_codigo` deve corresponder ao mapeamento atualizado (incluir 16 para 32-avos).
- `rodada` obrigatório apenas para `fase = 'Grupos'`.
- `hora_brt` deve seguir `HH:MM` (completar com `:00` para converter em `TIME`).
- Garantir que `grupo` esteja entre `A` e `L`; lançar aviso para valores inesperados.

## 7. Próximos Passos
1. Implementar o script em `scripts/atualizar_copa2026_dataset.py` seguindo o fluxo acima.
2. Criar JSON de placeholders padrão (`data/copa2026_placeholders.json`) com estrutura básica.
3. Associar execução do script ao processo de build/documentação (`README-migracao.md`).
4. Executar o script em modo `--dry-run` e registrar log da simulação.
