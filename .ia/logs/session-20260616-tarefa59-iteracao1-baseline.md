# Sessao: Tarefa 59 - Iteracao 1 (59.1 Diagnostico baseline)

**Data:** 2026-06-16
**Autor:** Codex (Time Mercurio)
**Skills Utilizadas:** ui-ux-pro-max v1.0.0, modern-javascript-patterns v1.0.0, architecture-guardian v1.0.0, security-audit v1.0.0

## 1. Prompt Principal (Input)

> Agora inicie a tarefa 59 passo a passo em pequenas iterações, uma subtarefa por vez até finalizar completamente a tarefa.

## 2. Baseline objetivo (59.1)

### Fluxo participante (`/seguro/palpites.action`)

Pontos já implementados:
- Feedback local na célula (`palpite-cell-feedback--saving/saved/error`) e autosave com debounce (`jogos.js`).
- Atualização de progresso disparada por `HX-Trigger` (`palpiteProgressRefresh`).
- Estados de bloqueio por janela/regra no backend (`ParticipanteAction` + `PalpiteService`).

Gaps identificados:
- Sem confirmação global discreta de sessão (usuário pode perder o feedback local em listas longas).
- Sem guard de saída para alterações pendentes (não há `beforeunload` no fluxo de palpites).
- Requisições redundantes ainda possíveis quando payload não muda entre submissões próximas.
- Falta contrato único explícito de estado de edição por item (`dirty/saving/saved/error/locked`) compartilhado entre UI e JS.

### Fluxo admin (`/admin/jogos.action`)

Pontos já implementados:
- Edição inline HTMX por linha em `admin-match-row.jsp` para data/hora/local/equipes/placar.
- Persistência e rerender de linha já estáveis via `AdminAction`.

Gaps identificados:
- Sem feedback visual textual por linha salva (timestamp/estado), apenas indicador de carregamento.
- Sem estado consolidado de erro por linha com ação de recuperação orientada.
- Sem atalho de produtividade por teclado para sequenciar edição de placares.

### Baseline de risco UX/Performance
- Risco de baixa confiabilidade percebida em operações rápidas (participante/admin).
- Risco de perda de edição por navegação acidental.
- Oportunidade clara de reduzir round-trip com deduplicação de payload e controle de concorrência por item.

## 3. Checklist de gaps priorizados

- [x] Feedback local existe, mas falta feedback global de sessão.
- [x] Guard de saída com alteração pendente está ausente.
- [x] Retry orientado a ação precisa ser padronizado.
- [x] Admin carece de confirmação textual por linha salva.
- [x] Deduplicação de payload idêntico não está completa.

## 4. Validacao

- Comando: inspeção estática de código (`rg`/`sed`) em JSP/JS/CSS/Action.
- Resultado: baseline concluído com evidências técnicas suficientes para execução da 59.2.
- Observacoes: sem alteração funcional nesta iteração.

## 5. Analise Humana (Veredito)

- [ ] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** baseline alinhado ao plano `.ia/planos/plano-ux-performance-palpites-resultados-20260616.md` e pronto para evolução incremental da UX transacional.
