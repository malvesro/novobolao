# ADR-20260213-estrategia-migracao-stack

Data: 2026-02-13
Status: Rascunho

## Contexto
O Sistema Bolao e um monolito Java EE legado (WebWork/XWork, Spring antigo, Hibernate 3, Acegi Security, DWR, Quartz, JSP, MySQL), empacotado em WAR e operando em Tomcat 5.5. O objetivo e modernizar o stack com o menor impacto possivel no comportamento atual, preservando URLs, fluxos e JSPs enquanto se reduz risco de regressao.

## Decisao
Adotar uma estrategia de migracao incremental em fases:
1. **Fase 1 (baixo impacto):** migrar WebWork/XWork para **Struts 7**, mantendo JSPs, URLs e o fluxo Action -> Service -> DAO. Atualizar o build para Maven/Gradle e ajustar dependencias, sem alterar logica de negocio.
2. **Fase 2 (ganho tecnico):** evoluir para **Spring MVC/Spring Boot**, substituindo gradualmente Actions por Controllers e modernizando configuracoes (Spring 6, Hibernate 6, Spring Security), mantendo compatibilidade por etapas.

## Alternativas Consideradas
1. **Migrar diretamente para Spring Boot + Spring MVC**
   - Pro: maior ganho tecnico imediato.
   - Contra: alto impacto no codigo e maior risco de regressao.

2. **Manter WebWork e apenas atualizar bibliotecas adjacentes**
   - Pro: minimo impacto no curto prazo.
   - Contra: perpetua tecnologia obsoleta e limita evolucao.

## Consequencias
- Reduz risco de regressao ao preservar o modelo atual de Actions e JSPs na primeira fase.
- Permite planejar e testar a migracao maior (Spring Boot) com menos pressao de curto prazo.
- Exige disciplina para manter compatibilidade com stack legado durante a Fase 1.
- Necessita planejamento de compatibilidade (Java 17/21, Jakarta namespaces) antes da Fase 2.

## Responsaveis
- Time Mercurio / Arquitetura

