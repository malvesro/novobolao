# ADR 2026-02-20 – Manter Arquitetura Monolítica Struts 7

## Contexto
- Fase 2.5 concluiu a higienização do frontend: remoção de Prototype/DWR, modernização do CSS, adoção de Vite e correções de acessibilidade em andamento.
- A Tarefa Fase 3.1 propunha avaliar reescrita ou migração para uma estrutura modular.
- O backlog imediato ainda possui dependências adiadas (auditoria axe, testes cross-browser, sign-off) e não há requisito de negócio que exija escalabilidade adicional.

## Decisão
Manter o monolito Struts 7/Spring 6 no curto e médio prazo, evitando uma reescrita arquitetural agora.

## Justificativa
- Dívida técnica crítica já mitigada; o sistema está estável e testado.
- Reescrita modular demandaria alto investimento (tempo/risco) sem benefício imediato.
- Foco deve permanecer no fechamento das tarefas adiadas (acessibilidade automatizada e cross-browser) e nas iniciativas de médio prazo do Fase 3.
- Quando surgirem drivers fortes (novos domínios, escalabilidade, APIs independentes), revisitar esta decisão com estudo custo–benefício.

## Consequências
- Fase 3.1 (reconcepção modular) é adiada indefinidamente.
- Continua-se investindo na melhoria do monólito (observabilidade, documentação, testes, performance) dentro da arquitetura atual.
- Próximas fases podem concentrar esforços em infraestrutura, containerização e segurança.
