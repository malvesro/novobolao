# Fase 6 — Normalização do Dataset Copa 2026

**Data:** 21/02/2026  
**Objetivo:** definir a estrutura alvo do arquivo `data/copa2026_tabela_brt.csv` e dos scripts SQL derivados para garantir consistência com as necessidades do sistema Bolão.

## 1. Situação Atual (`data/copa2026_tabela_brt.csv`)
- Colunas existentes: `fase`, `data`, `hora_brt`, `mandante`, `visitante`, `estadio`, `cidade`, `pais`.
- Registros: 89 (72 fase de grupos, 16 partidas de 32-avos, 1 final).
- Placeholders de equipes ainda não definidas: 6 slots de playoffs (UEFA e intercontinental).
- Fases eliminatórias acima dos 32-avos ausentes (oitavas em diante) aguardando divulgação oficial de calendário.
- País/cidade: 16 estádios (Canadá, México, EUA). Um registro contém texto placeholder no campo `pais` que deve ser normalizado.

## 2. Necessidades do Sistema
1. **Identificação estruturada de grupo e rodada** para suportar filtros (Struts/JSP) e geração de chaves.
2. **Enumeração consistente de fases** alinhada ao domínio (`JOG_FASE`), incluindo novos códigos para 32-avos e fases subsequentes.
3. **Preparação para scripts SQL** (`03-copa-2026-data.sql`) com inserções em `EQP_EQUIPE`, `JOG_JOGO` e futuras tabelas auxiliares.
4. **Rastreabilidade e atualizações incrementais** à medida que a FIFA divulga resultados de playoffs e agenda completa.

## 3. Estrutura Alvo Proposta
Adicionar/derivar os campos abaixo (além dos existentes):

| Campo             | Tipo      | Descrição                                                                                          |
|-------------------|-----------|----------------------------------------------------------------------------------------------------|
| `grupo`           | CHAR(1)   | Grupo A–L (preenchido para fase de grupos; vazio nas fases eliminatórias).                        |
| `rodada`          | TINYINT   | 1–3 para fase de grupos, `NULL` nas fases eliminatórias.                                           |
| `fase_codigo`     | INT       | Código numérico compatível com `JOG_FASE` (ex.: 11, 12, 13 para grupos; 16 para 32-avos; 8=oitavas; 4=quartas; 2=semifinal; 3=3º lugar; 1=final). |
| `fase_ordem`      | TINYINT   | Ordem cronológica da fase (1=Grupo, 2=32-avos, 3=Oitavas, ...).                                    |
| `mandante_slot`   | VARCHAR   | Slot textual padronizado para placeholders (ex.: `UEFA Playoff D`, `3o ABCDF`) — facilita atualização posterior. |
| `visitante_slot`  | VARCHAR   | Mesmo padrão do mandante.                                                                          |
| `fuso_lista`      | VARCHAR   | Campo opcional para referenciar o fuso oficial (BRT, local, UTC).                                  |

Regras adicionais:
- Manter `mandante`/`visitante` com nomes amigáveis; quando o nome for placeholder, duplicar informação no campo `*_slot` com formato padronizado.
- Normalizar `pais` para valores curtos (`Canadá`, `México`, `EUA`).
- Garantir UTF-8 sem BOM, separador `,`, cabeçalho consistente.

## 4. Fluxo de Geração de Dados
1. **Normalização do CSV existente** aplicando as colunas derivadas e correções de texto. *(21/02/2026: gerado `data/copa2026_tabela_brt_normalizado.csv` com os campos definidos acima, preservando o arquivo bruto original.)*
2. **Script Python** (`scripts/atualizar_copa2026_dataset.py`, a ser criado) para:
   - Atualizar placeholders após definição de playoffs.
   - Preencher fases eliminatórias restantes assim que a FIFA divulgar horários.
   - Exportar diretamente o script SQL (`03-copa-2026-data.sql`) a partir do CSV normalizado.
3. **Integrar no build/documentação**:
   - Atualizar `README-migracao.md` seção 4.8 com o novo fluxo.
   - Referenciar a coluna `fase_codigo` no mapeamento `JOG_FASE`.

## 5. Próximos Passos
1. Implementar a normalização do CSV conforme a estrutura alvo (subtarefa F6-T2-Dados).  
   - ✔ 21/02/2026: `data/copa2026_tabela_brt_normalizado.csv` gerado com `fase_codigo` (11/12/13/16), `rodada`, `fase_ordem` e placeholders padronizados.  
   - ✔ 21/02/2026: `scripts/atualizar_copa2026_dataset.py` executado gerando `data/sql/03-copa-2026-data.sql` e `data/copa2026_tabela_brt_final.csv`.  
2. Definir oficialmente o código numérico da fase de 32-avos (sugerido `16`) e refletir no schema/documentação.  
3. Criar o script automatizador para atualizar placeholders e gerar o SQL (`scripts/atualizar_copa2026_dataset.py`):  
   - Ler `data/copa2026_tabela_brt_normalizado.csv`.  
   - Atualizar slots concluídos (playoffs) a partir de entrada JSON/CSV auxiliar.  
   - Emitir `03-copa-2026-data.sql` com inserts ordenados por `fase_ordem`, `data_iso`, `hora_brt`.  
   - Opcional: gerar diff para revisar alterações (✔ feita versão inicial sem substituição de placeholders).  
4. Atualizar as telas e filtros para consumir `fase_codigo`/`grupo` ampliado, garantindo compatibilidade com o novo dataset.
