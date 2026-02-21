# Fase 6 — Inventário do Modelo de Dados (21/02/2026)

## 1. Objetivo
Avaliar se o modelo de dados legado atende à Copa do Mundo 2026 (48 seleções, 12 grupos, fases eliminatórias iniciando nos 32-avos) sem alterações estruturais, bem como identificar ajustes necessários em código e UX.

## 2. Artefatos analisados
- Hibernate mappings em `src/com/opendev/bolao/model/*.hbm.xml`.
- Classes de domínio (`Jogo`, `Equipe`, `Palpite`).
- Scripts de schema/seed (`docker/mysql/init/01-schema.sql`, `02-seed-data.sql`).
- Documento `bolao_datamodel.xml`.
- JSPs e mensagens na pasta `webapp/WEB-INF/content/` e `src/main/resources/messages.properties`.

## 3. Situação atual
### 3.1 Tabelas principais
- **EQP_EQUIPE**: armazena nome e grupo (`CHAR(1)`). Estruturalmente comporta 12 grupos (A–L), porém o front-end lista apenas A–H.
- **JOG_JOGO**: guarda fase como `INT`. Comentário do schema ainda considera enumeração antiga (0–4). Na prática, o código usa valores `11,12,13` (rodadas da fase de grupos) e `8,4,2,3,1` (oitavas → final).
- **PAL_PALPITE**: chave composta participante+jogo; nenhum limite intrínseco para 104 partidas.

### 3.2 Regras e filtros
- `FiltroBuscaJogos` filtra grupos apenas quando `fase in (11,12,13)`. Continua válido para 3 rodadas por grupo.
- JSPs `seguro/jogos.jsp` e `admin/inclusaoJogo.jsp` fixam listas de grupos (`A–H`) e fases (`11,12,13,8,4,2,3,1`), ausência de opção para 32-avos.
- Mensagens `filter.fase.*` não contemplam 32-avos; não há chave para 5ª fase eliminatória.
- `ParticipanteService` e `Palpite` assumem apenas contagem total de jogos; permanecem compatíveis com 104 partidas.

### 3.3 Seed e documentação
- `02-seed-data.sql` fornece apenas grupos fictícios A–D; servirá como sample, mas não representa o volume real.
- `bolao_datamodel.xml` não precisa de ajustes imediatos, mas deve ser sincronizado após redefinir enumerações de fase.

## 4. Lacunas identificadas
1. **Enumeração de fases**: falta valor específico para 32-avos (sugerido `16` ou atualização da tabela de significados). Necessário alterar schema, seed, mensagens, filtros e formulário de cadastro de jogos.
2. **Listas de grupos no front-end**: atualizar combos/textos para abranger A–L. Considerar geração dinâmica a partir das equipes cadastradas.
3. **Comentário do schema `JOG_FASE`**: documentação desatualizada (0–4). Precisa refletir mapeamento real (11–13 para grupos, novos códigos para mata-mata).
4. **Scripts de seed**: substituir dummy data por carga real alinhada ao CSV consolidado (`data/copa2026_tabela_brt.csv`).
5. **Relatórios/filtragens auxiliares**: revisar se existirem relatórios externos que consumam `JOG_FASE` com intervalo fixo.

## 5. Conclusão
O modelo de dados suporta o aumento de seleções e jogos sem alteração estrutural (tabelas e colunas permanecem válidas), desde que:
- sejam estendidos os valores de `JOG_FASE` para contemplar a fase de 32-avos;
- sejam atualizados os componentes de UI/seed/documentação que ainda assumem apenas oito grupos ou fases antigas.

Os ajustes necessários são essencialmente **lógicos/configuracionais**, não envolvendo migração de schema além da atualização do domínio de `JOG_FASE`.

