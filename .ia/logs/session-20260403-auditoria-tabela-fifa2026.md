# Log de Sessão: Auditoria da Tabela FIFA 2026 e Dados de Jogos

**Data:** 03/04/2026  
**Sessão:** Análise dos dados de jogos vs. tabela oficial FIFA 2026  
**Skill:** N/A  
**Status:** Concluído (análise) | Pendente (execução dos ajustes)

---

## Objetivo

Analisar a tabela completa da Copa do Mundo FIFA 2026 disponível em  
`https://www.fifa.com/pt/tournaments/mens/worldcup/canadamexicousa2026/articles/copa-mundo-2026-tabela-jogos`  
e verificar os ajustes necessários na carga de dados de jogos do sistema.

---

## Metodologia

1. Tentativa de leitura direta da URL — bloqueada (site SPA React, sem conteúdo estático)
2. Tentativa via browser (CDP) — indisponível no ambiente
3. Pesquisa web via múltiplas fontes (FIFA, NBC Sports, CBS Sports, Sky Sports, Fox Sports)
4. Dados dos grupos confirmados após playoffs de repescagem (concluídos em março/2026)
5. Comparação manual contra `data/sql/03-copa-2026-data.sql`

---

## Achados

### Grupos Oficiais Confirmados (pós-sorteio final + playoffs março/2026)

| Grupo | Seleções Confirmadas |
|-------|----------------------|
| A | México, Coreia do Sul, África do Sul, **República Tcheca** |
| B | Canadá, Qatar, Suíça, **Bósnia e Herzegovina** |
| C | Brasil, Marrocos, Escócia, Haiti |
| D | Estados Unidos, Paraguai, Austrália, **Turquia** |
| E | Alemanha, Costa do Marfim, Curaçao, Equador |
| F | Holanda, Japão, Tunísia, **Suécia** |
| G | Bélgica, Egito, Irã, Nova Zelândia |
| H | Espanha, Uruguai, Arábia Saudita, Cabo Verde |
| I | França, Senegal, Noruega, **Iraque** |
| J | Argentina, Áustria, Argélia, Jordânia |
| K | Portugal, Colômbia, Uzbequistão, **RD Congo** |
| L | Inglaterra, Croácia, Panamá, Gana |

### Divergências Encontradas no Banco de Dados

#### 🔴 Crítico — 6 Placeholders de Repescagem Desatualizados

O arquivo `data/sql/03-copa-2026-data.sql` (gerado em 28/03/2026) ainda usa nomes de placeholder para as vagas de repescagem que já foram preenchidas:

| ID  | Valor Atual no SQL             | Valor Correto         | Grupo | Impacto |
|-----|--------------------------------|-----------------------|-------|---------|
| 141 | `Repescagem Europeia D`        | `República Tcheca`    | A     | Alto    |
| 142 | `Repescagem Europeia A`        | `Bósnia e Herzegovina`| B     | Alto    |
| 143 | `Repescagem Europeia C`        | `Turquia`             | D     | Alto    |
| 144 | `Repescagem Europeia B`        | `Suécia`              | F     | Alto    |
| 146 | `Repescagem Intercontinental`  | `Iraque`              | I     | Alto    |
| 147 | `Repescagem Intercontinental 2`| `RD Congo`            | K     | Alto    |

**Observação:** O campo `EQP_GRUPO` está correto para todas essas equipes. Apenas o nome (`EQP_PAIS`) precisa ser atualizado. Os jogos associados têm datas, horários e locais já definidos.

#### 🟡 A Verificar — Inconsistências de Cidades nas Fases Eliminatórias

- Jogo 1079 (`2026-07-01`): local `Mexico City` — inconsistente com o padrão `Cidade do México` usado nos grupos
- Jogos 1075, 1078, 1087: horários `23:59:00` parecem ser placeholders de horário
- Jogo 1078: local `Arlington` — verificar se deve ser o nome oficial do estádio

#### 🟢 Dados Confirmados como Corretos

- Total de 104 jogos (IDs 1000–1103) ✅
- Datas: 11/06 a 19/07/2026 ✅
- Estrutura de grupos A–L, 12 grupos de 4 times ✅
- 36 equipes com nomes reais já confirmados ✅
- Fases: 11 (grupos), 16 (32-avos), 12 (oitavas), 13 (quartas), 14 (semis), 15 (disputa 3º/final) ✅

---

## Ações Registradas

- Nova subtarefa criada no `passo-a-passo.md` (Fase 2.7, linhas 83–98):
  **"Atualização dos Nomes das Equipes de Repescagem (Pós-Playoffs Março/2026)"**

---

## Próximos Passos

Aguardando aprovação para executar as correções:

1. Atualizar `EQP_PAIS` dos IDs 141, 142, 143, 144, 146, 147 no SQL
2. Padronizar nome da cidade `Mexico City` → `Cidade do México` nos jogos eliminatórios
3. Revisar horários `23:59:00` (possíveis placeholders)
4. Recarregar banco e validar telas

---

## Referências

- Tabela oficial FIFA 2026: `https://www.fifa.com/pt/tournaments/mens/worldcup/canadamexicousa2026/articles/copa-mundo-2026-tabela-jogos`
- Arquivo analisado: `data/sql/03-copa-2026-data.sql`
- Tarefa registrada: `passo-a-passo.md` (Fase 2.7, linha 83)

---

> Auto-Análise: [Risco: Médio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
