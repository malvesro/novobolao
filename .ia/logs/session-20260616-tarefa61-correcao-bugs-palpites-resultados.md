# Sessao: Tarefa 61 - Correção de bugs em palpites e resultados

**Data:** 2026-06-16
**Autor:** Codex (GPT-5)
**Skills Utilizadas:** modern-javascript-patterns v1.0.0, htmx v1.0.0, architecture-guardian v1.0.0, senior-java-dev-legacy v1.0.0

## 1. Prompt Principal (Input)

> Investigar bugs: (1) alerta de saída indevido na atualização admin de resultados e (2) campo de palpite desaparecendo ao iniciar digitação.

## 2. Resposta da IA (Output Resumido)

> Correções implementadas em pequenas iterações após documentação do bug report.
>
> ### Bug 1 - Alerta de saída indevido no admin
> - Causa raiz: `pendingAdminRequests` podia permanecer > 0 quando o trigger era substituído por `hx-swap="outerHTML"`, impedindo `closest('.match-row--admin-direct')` no `afterRequest`.
> - Correção: adicionado detector resiliente de request admin por `requestConfig.path` em `src/frontend/pages/jogos.js` (`isAdminRequest(...)`), usado em `beforeRequest`/`afterRequest` para manter o contador consistente.
>
> ### Bug 2 - Campo de palpite desaparecendo
> - Causa raiz: mismatch entre contrato legado de HTMX no `match-row.jspf` (inputs soltos com target `closest tbody`) e contrato atual do JS (`palpite-cell` com `form.palpite-inputs`).
> - Correção:
>   - `webapp/WEB-INF/content/seguro/partials/match-row.jspf` atualizado para usar a célula dedicada de palpite via include de `palpite-cell-response.jspf`.
>   - removidos inputs legados de palpite nos blocos de time (home/away), preservando exibição do placar oficial do jogo.
>   - ajustado `colspan` da linha de detalhes do grupo para manter consistência da tabela.
>   - `webapp/WEB-INF/content/seguro/partials/palpite-cell-response.jsp` passou a retornar apenas o fragmento da célula + progresso OOB, alinhado ao `hx-target` por célula.
>
> ### Testes
> - Atualizado `tests/frontend/jogos.test.js` com validação do contrato por célula após `htmx:afterSwap`.

## 3. Validacao (Build/Teste)

- Comando: `npm run test:frontend`
- Resultado: Sucesso
- Observacoes: 3 arquivos de teste, 4 testes aprovados.

- Comando: `npm run build`
- Resultado: Sucesso
- Observacoes: build Vite concluído; warning informativo de chunk grande (ApexCharts), sem falha.

- Comando: `mvn -Dfrontend.skip=true test`
- Resultado: Sucesso
- Observacoes: 77 testes executados, 0 falhas, 0 erros.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** correção concluída com compatibilidade ao fluxo HTMX moderno e validação completa em frontend + backend.
