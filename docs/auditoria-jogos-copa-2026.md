# Relatório de Auditoria: Discrepâncias na Tabela de Jogos (Copa 2026)

**Data de Auditoria:** 14/06/2026
**Responsável:** Arquiteto Sênior (Time Mercúrio)
**Status:** Em revisão técnica

## 1. Objetivo
Este documento visa mapear as inconsistências encontradas entre os dados cadastrados na base do sistema (SQL) e o cronograma oficial da FIFA (fuso horário de Brasília - BRT). O objetivo é sanar discrepâncias de horários e datas antes do início das próximas rodadas.

## 2. Levantamento de Discrepâncias
Abaixo estão os registros identificados com horários padronizados (placeholders) ou divergentes da tabela oficial.

| ID | Data | Horário SQL | Horário Oficial (BRT) | Ação Requerida |
| :--- | :--- | :--- | :--- | :--- |
| **1029** | 19/06/2026 | 16:00:00 | **21:30** | Corrigir (Brasil x Haiti) |
| 1008 | 14/06/2026 | 14:00:00 | *A validar* | Placeholder (Houston) |
| 1020 | 17/06/2026 | 14:00:00 | *A validar* | Placeholder (Houston) |
| 1032 | 20/06/2026 | 14:00:00 | *A validar* | Placeholder (Houston) |
| 1040 | 22/06/2026 | 14:00:00 | *A validar* | Placeholder (Dallas) |
| 1044 | 23/06/2026 | 14:00:00 | *A validar* | Placeholder (Houston) |
| 1075 | 29/06/2026 | 14:00:00 | *A validar* | Placeholder (Houston) |
| 1078 | 30/06/2026 | 14:00:00 | *A validar* | Placeholder (Dallas) |
| 1090 | 05/07/2026 | 14:00:00 | *A validar* | Placeholder (NY/NJ) |
| 1094 | 07/07/2026 | 14:00:00 | *A validar* | Placeholder (Atlanta) |
| 1103 | 19/07/2026 | 16:00:00 | *A validar* | Placeholder (Final) |

---

## 3. Metodologia de Correção

1.  **Validação dos Placeholders:** Os horários de 14:00 e 16:00 foram identificados como *placeholders* automáticos gerados pelo script de carga original. É imprescindível confrontar cada um destes IDs com a tabela oficial da FIFA para evitar cadastros incorretos.
2.  **Execução:**
    *   Após a validação, faremos a atualização dos registros no banco via script SQL, garantindo a atomicidade e integridade dos dados.
    *   Utilizaremos a interface administrativa para conferência pós-update.

## 4. Próximos Passos
*   [ ] Validar os horários oficiais para os IDs marcados como "Placeholder" (A validar).
*   [ ] Gerar script SQL de correção final.
*   [ ] Solicitar autorização para execução do commit dos dados corrigidos.
