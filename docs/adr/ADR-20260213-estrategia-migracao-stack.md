# ADR-20260213-estrategia-migracao-stack

Data: 2026-02-13
Status: Rascunho

## Contexto

O Sistema Bolao e um monolito Java EE legado (WebWork/XWork, Spring antigo, Hibernate 3, Acegi Security, DWR, Quartz, JSP, MySQL), empacotado em WAR e operando em Tomcat 5.5. O objetivo e modernizar o stack com o menor impacto possivel no comportamento atual, preservando URLs, fluxos e JSPs enquanto se reduz risco de regressao.

## Decisao

Adotar uma estrategia de migracao incremental em fases:

1. Fase 1: **migrar WebWork/XWork para Struts 7**, mantendo JSPs, URLs e o fluxo Action -> Service -> DAO. Atualizar o build para Maven/Gradle e ajustar dependencias, sem alterar logica de negocio. **Gerar WAR com Java 17 + Tomcat 10.1** (estabilizar primeiro).  
2. Fase 2: Modernizar configuracoes (Spring 6, Hibernate 6, Spring Security), mantendo compatibilidade por etapas. **Avaliar upgrade para Tomcat 11 apos estabilizacao do stack Jakarta**.

Matriz alvo inicial (fase 1/2):
- Java 17
- Tomcat 10.1
- Struts 7
- Spring 6
- Hibernate 6
- Spring Security 6

## Consequencias

- Reduz risco de regressao ao preservar o modelo atual de Actions e JSPs na primeira fase com ajustes para o Struts 7.
- Exige disciplina para manter compatibilidade entre as tecnologias usadas e para o corte `javax` -> `jakarta`.
- Necessita planejamento de compatibilidade (Java 17/21, Jakarta namespaces) antes da Fase 2.
- Upgrade para Tomcat 11 fica condicionado a estabilidade do ambiente com Tomcat 10.1.

## Responsaveis

- Time Mercurio / Arquitetura
