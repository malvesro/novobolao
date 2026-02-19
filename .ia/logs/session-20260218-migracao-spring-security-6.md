# Sessão: Migração para Spring Security 6

**Data:** 2026-02-18
**Autor:** Kiro (Assistente Técnico Líder)
**Skills Utilizadas:** senior-java-dev-legacy v1.0.0, modernization-java-migration

## 1. Prompt Principal (Input)

> Iniciar a migração completa do framework de segurança de Acegi Security 1.0.0 para Spring Security 6.2.2. A aplicação está bloqueada devido à incompatibilidade do Acegi com Jakarta EE 10 (Tomcat 10).

**Contexto:**
- O projeto usa Acegi Security 1.0.0 (EOL 2006).
- A stack foi migrada para Spring 6 e Jakarta EE 10.
- Acegi Security depende de `javax.servlet`, causando `NoClassDefFoundError` no Tomcat 10.
- `spring-security-config` e `spring-security-web` 6.2.2 já estão no `pom.xml`.

## 2. Análise do Problema (Bloqueador)

O Acegi Security é o predecessor do Spring Security e não possui suporte para o namespace `jakarta.*`. Como a aplicação agora roda no Tomcat 10, qualquer classe do Acegi que implemente interfaces de Servlet (como `Filter`) falha ao carregar.

**Achados:**
- `applicationContext-security.xml` está saturado de classes `org.acegisecurity.*`.
- O `web.xml` referencia um `securityFilter` que delega para o `filterChainProxy` do Acegi.

## 3. Estratégia de Migração

A migração será feita em etapas incrementais para garantir que o contexto do Spring volte a carregar:

1. **Configuração do web.xml**: Garantir que o `DelegatingFilterProxy` aponte para o bean correto. (Iniciado com `targetBeanName=filterChainProxy`).
2. **Substituição do XML de Configuração**: Reescrever o `applicationContext-security.xml` usando o namespace `<security:http>` do Spring Security moderno.
3. **Mapeamento de Beans**: Traduzir as funcionalidades do Acegi para os análogos do Spring Security 6:
    - `DaoAuthenticationProvider` (Acegi) -> `DaoAuthenticationProvider` (Spring Security)
    - `FilterInvocationDefinitionSource` -> `<security:intercept-url>`
    - `AccessDecisionManager` -> Gerenciamento de autorização nativo do Spring Security 6.
4. **Password Encoding**: Migrar de `ShaPasswordEncoder` (Acegi) para `BCryptPasswordEncoder` (Spring Security), mantendo suporte legado através do `DelegatingPasswordEncoder` (já esboçado na Fase 1).

## 4. Implementação (Etapa 1: Preparação)

### Arquivos Modificados nesta etapa:

1. **webapp/WEB-INF/web.xml**
    - Adição de explicitamente do `targetBeanName` como `filterChainProxy` para evitar ambiguidade e resolver o erro de bean não encontrado durante a fase de transição.

## 5. Próximos Passos

1. Reescrever `applicationContext-security.xml` para remover dependências de `org.acegisecurity`.
2. Atualizar as referências de Taglibs de segurança nos JSPs.
3. Testar o fluxo de autenticação e autorização no ambiente Docker.

---

**Auto-Análise:** [Risco: Médio] | [Compatibilidade: OK] | [Veredito: Aprovado]
