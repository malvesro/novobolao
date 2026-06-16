# ADR-20260616: Padronização de estados de gravação para palpites e resultados

## Status
Rascunho

## Contexto
As telas de palpites (`/seguro/palpites.action`) e resultados administrativos (`/admin/jogos.action`) evoluíram de forma incremental e passaram a ter múltiplos padrões de feedback de gravação (local por célula, loading implícito, mensagens pontuais).

Isso gerava três riscos:
1. baixa confiabilidade percebida do usuário (não saber se realmente salvou);
2. perda de edição por saída acidental da tela;
3. inconsistência operacional entre participante e admin.

## Decisão
Adotar contrato único de estados de gravação na camada de apresentação:
- `dirty`
- `saving`
- `saved`
- `error`
- `locked`

Aplicação da decisão:
- feedback local por célula/linha com texto explícito e timestamp quando aplicável;
- feedback global discreto de sessão (`role=status`, `aria-live=polite`);
- guard de saída via `beforeunload` somente quando existir pendência real;
- ação de retry para falhas transitórias (participante/admin);
- deduplicação de autosave para payload idêntico.

## Consequências
### Positivas
- aumenta clareza de estado transacional para o usuário final e operador admin;
- reduz risco de perda de edição;
- melhora previsibilidade da interface e rastreabilidade operacional;
- mantém aderência às diretrizes de frontend/CSP sem scripts inline novos.

### Trade-offs
- maior complexidade no módulo `src/frontend/pages/jogos.js`;
- necessidade de manter o contrato de estados sincronizado com i18n e fragmentos JSP.

## Guardrails
- preservar fluxo arquitetural Action -> Service -> DAO;
- não alterar regras de negócio de prazo/pontuação no frontend;
- manter compatibilidade com HTMX e mensagens acessíveis WCAG 2.1 AA.

## Referências
- `.ia/planos/plano-ux-performance-palpites-resultados-20260616.md`
- `.ia/logs/session-20260616-tarefa59-iteracao1-baseline.md`
- `.ia/logs/session-20260616-tarefa59-iteracoes2a8-ux-palpites-resultados.md`
