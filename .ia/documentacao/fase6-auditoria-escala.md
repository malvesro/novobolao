# Fase 6 — Auditoria Visual de Escala (Status Parcial – 21/02/2026)

## 1. Contexto
Validação das telas principais para garantir suporte ao aumento de dados (48 seleções, 12 grupos, fase de 32-avos). A auditoria foi realizada por inspeção estática do código (sem execução do front-end).

## 2. Telas Revisadas
| Tela | Observações |
|------|-------------|
| `seguro/jogos.jsp` | Ajustada para exibir `descricaoFase` quando não houver grupo (ex.: 32-avos). Se o jogo pertence à fase de grupos, mantém “Grupo X”. Seletores de filtros usam lista dinâmica de grupos (`equipes`). |
| `seguro/principal.jsp` | Atualizada para utilizar `descricaoFase` nos jogos do dia (evita coluna vazia nas fases eliminatórias). |
| `admin/inclusaoJogo.jsp` | Combos de equipe agrupados dinamicamente de acordo com os dados. Administrador deve selecionar fase manualmente (lista já inclui 32-avos). |
| `seguro/classificacao.jsp` | Layout independente de grupos; mantém responsividade. |
| `admin/participantes.jsp` | Sem impacto relacionado aos novos grupos/fases. |

## 3. Achados Principais
1. **Suporte textual às fases** — Propriedades `isFaseDeGrupos` e `descricaoFase` adicionadas ao modelo `Jogo`. Necessário validar em ambiente real se OGNL mapeia corretamente as novas propriedades (coberto por testes unitários? não).  
2. **Filtros de grupos** — Agora baseados em `equipes`, evitando listas fixas A–H; requer que `equipes` esteja ordenada para renderização consistente (já tratada no DAO).  
3. **Interação visual** — Não foi possível validar o volume final (mínimo 104 jogos). Recomenda-se sessão manual após carregar o dataset completo.

## 4. Pendências / Recomendações
- Executar auditoria manual em navegadores, após disponibilização do dataset final (Copa completa).  
- Ajustar tooltips/legendas se necessário quando as fases eliminatórias forem adicionadas às páginas de estatísticas (gráficos/performance).  
- Confirmar tradução inglesa/espanhola de `filter.fase.16` caso o sistema ofereça fallback.

## 5. Próximos Passos
- Aguardando dados oficiais para finalizar subtarefa “Atualização de Dados (SQL)”.  
- Após carga final, repetir a validação visual (desktop e mobile) para confirmar que os portlets permanecem utilizáveis com 48 seleções, especialmente em “Jogos” e “Classificação”.

