# ADR-20260217-upgrade-spring-framework.md

## Título
Upgrade do Spring Framework 1.2.8 para Spring Boot 3+ (Spring Framework 6)

## Status
Proposto

## Contexto
O projeto atual utiliza **Spring Framework 1.2.8** (lançado em 2005), que está em **End-of-Life (EOL)** há mais de uma década.
Esta versão:
1.  Não recebe correções de segurança.
2.  É incompatível com versões modernas do Java (Java 17/21).
3.  Impede o uso de bibliotecas modernas (como Spring Security 6, Hibernate 6).
4.  Dificulta a implementação de novas features (ex: Chat com WebSockets).

## Decisão
## Decisão
Decidimos migrar para **Spring Framework 6.1.x (Standalone)** rodando em **Tomcat 10.1+ (Jakarta EE)**.
**Não utilizaremos Spring Boot** neste momento para minimizar a complexidade da mudança arquitetural.

A estratégia de migração será:
1.  **Modelo de Deployment (WAR):** Manteremos a estrutura de empacotamento WAR clássica e o deployment em container externo (Tomcat).
2.  **Adoção de Jakarta EE:** Substituição obrigatória de `javax.*` por `jakarta.*` (Servlet 6.0, JPA 3.1).
3.  **Spring Framework 6:** Upgrade das bibliotecas core do Spring (Context, ORM, Web, Security) gerenciadas via BOM (`spring-framework-bom`), sem a "mágica" de auto-configuração do Boot.
4.  **WebWork -> Struts 6:** Migração da camada web para Struts 6 (compatível com Jakarta EE), mantendo a arquitetura de `ActionSupport` e arquivos `struts.xml` (sucessor do `xwork.xml`).
5.  **Configuração Híbrida:** Manteremos os XMLs (`applicationContext.xml`) originais o máximo possível, apenas atualizando namespaces e classes, evitando a reescrita massiva para classes `@Configuration` que o Boot exigiria.

## Consequências
**Positivas:**
*   **Menor Complexidade de Migração:** Preserva a estrutura do `web.xml` e o ciclo de vida da aplicação que o time já conhece.
*   **Reaproveitamento de XML:** Permite manter as definições de beans complexos (Hibernate, Services) quase inalteradas.
*   **Controle Granular:** Sem "opiniões" do Boot sobre versões de terceiros ou configurações ocultas.

**Negativas:**
*   **Gerenciamento Manual de Versões:** Necessário alinhar versões compatíveis de Spring, Hibernate, Struts e libs satélites manualmente no POM (byla BOMs).
*   **Verbosidade:** A configuração XML continua sendo necessária e verbosa.

## Plano de Execução
1.  Atualizar `pom.xml` para usar `spring-framework-bom` e `jakartaee-bom`.
2.  Substituir dependências legadas por versões Jakarta-compatíveis (Spring 6, Hibernate 6, Struts 6, Spring Security 6).
3.  Atualizar `web.xml` para descritores Servlet 6.0 (Jakarta).
4.  Migrar código Java (`javax` -> `jakarta`).
5.  Ajustar arquivos XML do Spring e Hibernate para novos XSDs.
