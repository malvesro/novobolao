# ADR 2026-02-20 – Estratégia de Otimização Frontend Minimizada

## Contexto

- A Fase 2.5 – Tarefa 6 do `passo-a-passo.md` trata da otimização de performance do frontend (minificação, cache, lazy loading).
- O bundler Vite já gera o bundle principal (`js/main-<hash>.js`) com apenas ~8 KB (2.6 KB gzip) e mantém o fallback `app-bundle.js` no mesmo tamanho.
- O CSS consolidado (`webapp/css/estilo.css`) possui ~19 KB (4.1 KB gzip) e o único asset legado adicional é `webapp/js/htmx.min.js` (~47 KB).
- A infraestrutura de bundling com manifest e hashing foi finalizada, garantindo cache busting automático.
- Investimentos maiores (compressão adicional, pipelines complexos) teriam baixo impacto frente a outras prioridades (acessibilidade, testes cross-browser, documentação).

## Decisão

Adotar uma abordagem **mínima** para a Tarefa 6:

1. Manter o pipeline atual do Vite (hashing + manifest + fallback) como solução oficial de cache busting.
2. Documentar os tamanhos atuais como baseline e revisitar a otimização apenas quando o volume de assets crescer significativamente.
3. Limitar a implementação imediata a ajustes simples de cache HTTP (ex.: cabeçalhos `Cache-Control` via servidor/deploy) quando oportuno, sem esforço adicional de minificação.

## Consequências

- Tempo de implementação é redirecionado para tarefas prioritárias (acessibilidade, cross-browser, documentação).
- Caso novos módulos ampliem o tamanho dos bundles, o fluxo já está preparado para suportar minificação e hashing sem refatorações.
- A tarefa 6 permanece documentada como concluída com o escopo mínimo definido nesta decisão; novas otimizações exigirã nova revisão desta ADR.

