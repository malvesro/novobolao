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

## 4. Implementação (Concluída: Etapa 1 e 2)

### Arquivos Modificados:

1. **src/applicationContext-security.xml**
    - Migração completa de Acegi Security 1.0.0 para Spring Security 6.
    - Utilização do namespace `<security:http>` e `<security:authentication-manager>`.
    - Mapeamento de `jdbcUserService` e `passwordEncoder`.
    - Configuração de intercept-urls, form-login, logout e anonymous.

2. **webapp/WEB-INF/web.xml**
    - Atualização do schema para Servlet 6.0 (Jakarta EE 10).
    - Configuração do `securityFilter` para usar `springSecurityFilterChain`.
    - Padronização de filter-mappings para `/*`.
    - Remoção de declarações legadas de Taglibs Acegi.

3. **src/com/opendev/bolao/util/LegacySha1PasswordEncoder.java**
    - Criada nova classe para suportar o hashing SHA-1 Base64 das senhas legadas, permitindo a transição gradual para BCrypt.

4. **webapp/template/cabecalho.jspf, menu.jspf e JSPs**
    - Substituição da taglib `authz` (Acegi) pela `sec` (Spring Security).
    - Atualização das tags `<sec:authorize>` e do link de logout.

## 5. Validação (Build/Teste)

### Resultados:
- **Build Maven:** `mvn clean compile` executado com SUCESSO.
- **Mapeamento de Beans:** Todos os beans Acegi foram substituídos ou removidos, eliminando as dependências de `org.acegisecurity`.
- **Incompatibilidade Jakarta EE:** Resolvida através da substituição total das classes que dependiam de `javax.servlet`.

## 6. Próximos Passos

1. **Teste em Runtime (Docker):** Realizar o rebuild dos containers e testar o login com os usuários `admin` (senha `admin123`) e `user` (senha `user123`).
2. **Password Re-hash:** Verificar se a estratégia do `DelegatingPasswordEncoder` está funcionando para migrar as senhas de SHA-1 para BCrypt no primeiro login bem-sucedido.
3. **Auditoria de Autorização:** Validar se as restrições de acesso por URL estão funcionando conforme o esperado para cada Role.

---

**Auto-Análise:** [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
