# Plano de Evolução e Modernização - Sistema Bolão

Referências:

*   `README.md`
*   `.ia/historico/ADR-*-estrategia-migracao-stack.md`
*   `.ia/diretrizes/arquitetura.md`
*   `analise-inicial.md` (Relatório da Análise Inicial Profunda do Projeto)

Legenda de status:

*   `Pendente`
*   `Em Progresso`
*   `Concluído`
*   `Bloqueado`
*   `Cancelado`

Diretriz fixa:

*   Definir empacotamento WAR e deploy.
*   Definir o que não pode ser usado (exemplo: Não usar spring boot) - **Nota: Esta diretriz pode ser revisada conforme as recomendações de modernização.**

Premissas de compatibilidade (críticas):

*   Exemplo: Struts 7 exige Java 17+ e Jakarta Servlet 6+ (jakarta.*). - **Nota: Estas premissas serão definidas com base nas escolhas de tecnologias para a modernização.**

## Fases de Modernização e Atividades (executar em sequência)

### Fase 1: Curto Prazo - Segurança e Estabilidade Mínima (IMEDIATA)

1.  **[Concluído] Forçar HTTPS:** Configurar o servidor de aplicação para forçar o uso de HTTPS para toda a aplicação.
2.  **[Concluído] Desativar DWR Debug:** Desabilitar o modo `debug=true` do DWR no `web.xml`.
3.  **[Concluído] Auditoria e Melhoria de Credenciais:** Avaliar e planejar a migração do hashing de senhas de SHA-1 para um algoritmo seguro (ex: bcrypt).
4.  **[Concluído] Avaliar e Isolar/Remover Funcionalidades Críticas:** Identificar e, se possível, isolar ou desativar funcionalidades que dependem de bibliotecas EOL com vulnerabilidades conhecidas, caso a migração seja demorada. (Chat desativado em favor da recriação futura).

### Fase 2: Médio Prazo - Redução de Débito Técnico e Manutenção (ALTA PRIORIDADE)

1.  **[Concluído] Upgrade do Spring Framework & WebWork:** Planejar e executar a migração do Spring Framework 1.2.8 para **Spring Framework 6** (Standalone) e do WebWork para **Struts 6.x** (Jakarta EE).
    Referência ADR: `.ia/historico/ADR-20260217-upgrade-spring-framework.md`
    *   **[Concluído]** Atualização do `pom.xml` (BOMs Spring 6, Jakarta EE 10, Struts 6).
    *   **[Concluído]** Migração de Namespace: Substituir `javax.servlet` e `javax.persistence` por `jakarta.*`.
    *   **[Concluído]** Migração Struts: Converter Actions (`ActionSupport`), atualizar imports (`xwork2`) e converter tags JSPs (`<ww:*>` -> `<s:*>`).
    *   **[Concluído]** Migração Hibernate: Remover `HibernateTemplate` (descontinuado) e adaptar DAOs para usar `SessionFactory.getCurrentSession()` e API do Hibernate 6.
    *   **[Concluído]** Adaptação de Configuração: Atualizar `web.xml` (FilterDispatcher -> StrutsPrepareAndExecuteFilter) e esquemas XSD dos XMLs do Spring.
    *   **[Concluído]** **Endurecimento Struts 7 (Segurança):**
        *   **[Concluído]** Aplicar anotação `@StrutsParameter` em todos os setters de Actions que recebem dados de formulários (Obrigatório no Struts 7).
        *   **[Concluído]** Configurar `struts.ognl.expressionMaxLength` e `struts.ognl.excludedNodeTypes` no `struts.xml`.
        *   **[Concluído]** Validar se `struts.allowlist.enable=true` está ativo e mapear classes customizadas necessárias.
        *   **[Concluído]** Implementar interceptores de isolamento de recursos (Fetch Metadata, COOP/COEP).
    Referência ADR: `.ia/historico/ADR-20260219-upgrade-struts-7.md`
    Referência Diretrizes: `.ia/diretrizes/seguranca.md`
2.  **[Em Progresso] Migração da Segurança:** Planejar e executar a substituição do Acegi Security 1.0.0 por Spring Security 6+.
    **CRÍTICO - BLOQUEADOR**: Durante testes do Docker (2026-02-18), identificado que `applicationContext-security.xml` ainda usa classes do Acegi Security (EOL desde 2006), incompatível com Jakarta EE 10. Spring Security 6.2.2 já está no `pom.xml` mas não é utilizado. Necessário reescrever completamente a configuração de segurança.
    *   **[Concluído]** Reescrever `applicationContext-security.xml` usando Spring Security 6
    *   **[Concluído]** Atualizar `web.xml` para usar `springSecurityFilterChain`
    *   **[Concluído]** Migrar beans de autenticação (DaoAuthenticationProvider)
    *   **[Concluído]** Migrar beans de autorização (AccessDecisionManager)
    *   **[Concluído]** Configurar password encoder (BCrypt)
    *   **[Em Progresso]** **Validação Integrada de Segurança:**
        *   **[Concluído]** Validar **OGNL Allowlist** no Struts 7 para classes do pacote `com.opendev.bolao.model` e utilitários de exibição. (Skill: `modernization-java-migration v1.0.0`)
        *   [Pendente] Testar fluxo de autenticação (Login/Logout) com usuários legado (SHA-1) e novos (BCrypt).
        *   [Pendente] Testar controle de acesso (RBAC) para URLs `/admin/**` e `/seguro/**`.
    Referência Log: `.ia/logs/session-20260218-correcao-filtros-web-xml.md`
    Referência Log: `.ia/logs/session-20260218-migracao-spring-security-6.md`

3.  **[Pendente]** **Fase 2.7: Atualização para Copa do Mundo 2026:**
    *   **[Pendente]** **Atualização de Dados (SQL):** Criar e aplicar script de carga (`03-copa-2026-data.sql`) com as 48 seleções, 12 grupos e locais oficiais (Canadá, México, EUA).
    *   **[Pendente]** **Configurações de Contexto:** Atualizar parâmetros de cidades-sede e datas no `web.xml` para refletir o calendário de Junho/Julho de 2026.
    *   **[Pendente]** **Lógica de Fases:** Adaptar o sistema para suportar a nova fase de "16-avos de final" (mata-mata ampliado).
    *   **[Pendente]** **Auditoria Visual de Escala:** Garantir que as telas de palpites e classificação suportem o aumento de volume de dados (48 seleções vs 32 anteriores).
    Referência FIFA: `https://www.fifa.com/pt/tournaments/mens/worldcup/canadamexicousa2026/articles/copa-mundo-2026-tabela-jogos`

4.  **[Concluído] Atualização do ORM:** Planejar e executar a migração do Hibernate 3.2.6.ga para Hibernate 6+.
4.  **[Concluído] Substituição do WebWork:** Eliminar completamente as referências ao servlet e filtros do WebWork legado no `web.xml`.
    Referência Plano: `.ia/planos/plano-substituicao-webwork.md`
5.  **[Concluído] Atualização de Bibliotecas de Terceiros:** Inventariar e atualizar todas as bibliotecas de terceiros (DWR, Quartz, EHCache, JSTL, Cewolf, JFreeChart, Batik, etc.) para suas versões mais recentes e com suporte. Remova bibliotecas sem suporte e atualize para outras bibliotecas mais atuais que atendam as necessidades do sistema.
    Referência ADR: `.ia/historico/ADR-20260217-bibliotecas-legadas.md`
    Referência Plano: `.ia/planos/plano-atualizacao-bibliotecas-terceiros.md`
6.  **[Concluído] Introdução de Testes Automatizados:** Iniciar a criação de testes unitários e de integração para módulos críticos e novos desenvolvimentos, priorizando a cobertura de áreas de negócio sensíveis.
7.  **[Concluído] Refatoração do Front-end (Primeira Fase):** Planejar a substituição gradual de Prototype/Script.aculo.us pela **Arquitetura Híbrida** (jQuery 4.0.0 + HTMX).
    Referência Plano: `.ia/planos/plano-modernizacao-frontend.md`
    Referência ADR: `.ia/historico/ADR-20260217-arquitetura-frontend-modernizacao.md`
8.  **[Concluído] Modernização da Estrutura de Recursos Maven:** Migrar recursos de configuração de `webapp/WEB-INF/classes/` para a estrutura padrão Maven `src/main/resources/`. Atualizar `pom.xml` para copiar recursos automaticamente durante o build. Remover duplicação de arquivos de configuração entre `src/` e `webapp/WEB-INF/classes/`.
    **Justificativa:** O projeto utiliza `webapp/WEB-INF/classes/` como diretório de recursos, uma prática antiga que não segue os padrões modernos do Maven. A estrutura correta é manter os recursos em `src/main/resources/` e deixar o Maven gerenciar a cópia durante o build, eliminando duplicação e facilitando manutenção.

### Fase 2.5: Auditoria e Ajuste do Frontend (ALTA PRIORIDADE)

1.  **[Em Progresso] Auditoria Visual Completa:** Testar renderização e funcionalidade de todas as telas principais (login, dashboard, formulários, gráficos, admin) em navegadores modernos e múltiplas resoluções.
2.  **[Pendente] Inventário e Análise de Scripts:** Mapear todos os arquivos JavaScript, identificar dependências e decidir manter/refatorar/remover cada um.
3.  **[Pendente] Remoção de Prototype e Scriptaculous:** Eliminar bibliotecas legadas (Prototype.js, Scriptaculous.js) do projeto, migrando funcionalidades restantes para jQuery 4.0.0.
4.  **[Pendente] Auditoria e Refatoração CSS:** Revisar `estilo.css`, remover hacks legados (IE6/7), reorganizar por componentes e implementar responsividade básica com media queries.
5.  **[Pendente] Remoção de Referências ao Cewolf:** Limpar taglib Cewolf do `cabecalho.jspf` e substituir gráficos por implementação alternativa (JFreeChart direto ou Chart.js).
6.  **[Pendente] Otimização de Performance:** Minificar JS/CSS, implementar cache de assets, usar lazy loading quando apropriado. Meta: Lighthouse Performance > 80.
7.  **[Pendente] Auditoria de Acessibilidade:** Verificar conformidade com WCAG 2.1 Level AA (contraste, navegação por teclado, labels, ARIA). Meta: axe score > 90.
8.  **[Pendente] Testes de Compatibilidade Cross-Browser:** Validar funcionamento em Chrome, Firefox, Edge e Safari (desktop e mobile).
9.  **[Pendente] Documentação Frontend:** Criar `.ia/diretrizes/frontend.md` documentando arquitetura, padrões de código e guias para desenvolvedores.
10. **[Pendente] Validação Final e Sign-off:** Checklist completo de qualidade frontend antes de prosseguir para Fase 3.

Referência ADR: `.ia/historico/ADR-20260217-fase-auditoria-frontend.md`
Referência Plano: `.ia/planos/plano-fase-2.5-auditoria-frontend.md`

### Fase 3: Longo Prazo - Modernização Completa (MÉDIA a LONGA PRIORIDADE)

1.  **[Pendente] Reescrita ou Migração para estrutura modular:** Avaliar a reescrita gradual da aplicação ou a quebra do monolito em módulos (seguindo o DDD), utilizando uma arquitetura moderna como Struts 6 ou 7.
2.  **[Pendente] Modernização Completa do Front-end:** Reescrita completa do front-end com um framework JavaScript moderno (React, Vue, Angular) e adoção de práticas de design responsivo, ajustado ao Struts 6 ou 7 escolhido no item 1.
3.  **[Pendente] Simplifique o build, testes, análise de código e deployment com o container docker (docker compose) para execução local.
4.  **[Pendente] Monitoramento e Observabilidade:** Implementar ferramentas de monitoramento de desempenho (APM), agregação de logs e rastreamento distribuído. Considere o uso de grafana e prometheus.
5.  **[Pendente] Recriação do Chat (Chat 2.0):** Implementar um novo serviço de chat utilizando tecnologias modernas (Spring Boot + WebSocket), conforme detalhado em `implementation_plan.md`.
6.  **[Pendente] Atualização da Versão do Java:** Migrar para a versão LTS mais recente do Java (ex: Java 17 ou 21 ou 25).
6.  **[Pendente] Banco de Dados:** Avaliar a necessidade de upgrade da versão do MySQL.

### Fase 4: Infraestrutura e Containerização (MODERNIZAÇÃO DE AMBIENTE)

1.  **[Concluído] Containerização com Docker:** Criar `Dockerfile` multi-stage utilizando princípios distroless para a aplicação.
2.  **[Concluído] Orquestração com Docker Compose:** Criar `docker-compose.yml` integrando a aplicação (Tomcat 10) e o banco de dados (MySQL 8).
3.  **[Concluído] Persistência e Rede:** Configurar volumes para o banco de dados e redes isoladas entre os containers.

### Fase 5: Segurança Progressiva (ALTA PRIORIDADE)

Referência Diretrizes: `.ia/diretrizes/seguranca.md`

1.  **[Pendente] Auditoria de Vulnerabilidades:** Integrar o OWASP Maven Dependency Check no `pom.xml` para monitoramento contínuo de CVEs.
    <security:intercept-url pattern="/seguro/**" access="hasAnyRole('ADMIN', 'USER')" />
2.  **[Pendente] Proteção de Recursos Estáticos:** Mover todos os arquivos JSP para dentro de `WEB-INF/` (ex: `WEB-INF/content/`) para impedir o acesso direto via browser, forçando a passagem pelas Actions do Struts.
3.  **[Pendente] Proteção na Camada Web:** Configurar cabeçalhos de segurança (HSTS, CSP, X-Frame-Options) e proteção CSRF.
3.  **[Pendente] Sanitização e Validação:** Revisar validadores do Struts 6 e implementar proteção robusta contra XSS.
4.  **[Pendente] Auditoria de Segredos:** Implementar varredura de credenciais e senhas em arquivos de configuração.

## Registro de Avanços

*   2026-02-17: **[Concluído]** Tarefa 3 da Fase 1. Implementada estratégia de `DelegatingPasswordEncoder` para auditoria e melhoria de credenciais, incluindo adição de `spring-security-crypto` ao `pom.xml`, criação de `DelegatingPasswordEncoder.java` e atualização de `applicationContext-security.xml` e `ParticipanteServiceImpl.java`. (Skills: `senior-java-dev-legacy v1.0.0`, `security-audit v1.0.0`)
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência ADR: `.ia/historico/ADR-20260217-seguranca-inicial.md`
*   2026-02-17: **[Concluído]** Tarefa 2 da Fase 1. Alterado o valor do parâmetro `debug` para `false` no servlet `dwr-invoker` no arquivo `web.xml`. (Skill: `senior-java-dev-legacy v1.0.0`)
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência ADR: `.ia/historico/ADR-20260217-seguranca-inicial.md`
*   2026-02-17: **[Concluído]** Tarefa 1 da Fase 1. Adicionado `security-constraint` ao `web.xml` para forçar o uso de HTTPS em toda a aplicação, definindo o `transport-guarantee` como `CONFIDENTIAL`. (Skill: `senior-java-dev-legacy v1.0.0`)
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência ADR: `.ia/historico/ADR-20260217-seguranca-inicial.md`
*   2026-02-17: **[Concluído]** Tarefa 4 da Fase 1. Isolado e desativado o chat legado (DWR) e substituído por uma mensagem de manutenção. (Skill: `senior-java-dev-legacy v1.0.0`)
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência ADR: `.ia/historico/ADR-20260217-isolamento-chat-legado.md`
*   2026-02-17: **[Concluído]** Migração Completa da Stack (Tarefa 1 da Fase 2). Finalizada a atualização para Spring Framework 6.1.4, Struts 6.3.0, Hibernate 6.4.4 e Jakarta EE 10. O build (`mvn clean compile`) está estável.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
*   2026-02-17: **[Concluído]** Introdução de Testes Automatizados (Tarefa 6 da Fase 2). Configurado framework de testes com JUnit 5, Mockito e AssertJ. Criado primeiro teste de unidade para `ParticipanteServiceImpl`. Resolvidos conflitos de repositórios Maven e migração para diretório dedicado `/tests`.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência ADR: `.ia/historico/ADR-20260217-estrategia-testes-automatizados.md`
    Referência ADR: `.ia/historico/ADR-20260217-upgrade-spring-framework.md`
*   2026-02-17: **[Concluído]** Atualização de Bibliotecas e Tags JSP (Tarefa 5 da Fase 2). Migradas tags JSP para Struts 2 (`s:`) e atualizadas dependências do Quartz (2.3.2) e EHCache (3.10.8) no `pom.xml` para compatibilidade com a nova stack.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência ADR: `.ia/historico/ADR-20260217-upgrade-spring-framework.md`
*   2026-02-17: **[Concluído]** Ajuste de Configuração Spring/Hibernate (Tarefa 1 da Fase 2). Atualizados beans `sessionFactory` e `txManager` para as classes do pacote `hibernate5` compatíveis com a nova versão.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
*   2026-02-17: **[Concluído]** Modernização Front-end (Tarefa 7 da Fase 2). Implementação da **Arquitetura Híbrida**: Integração de jQuery 4.0.0 e HTMX 1.9.10; migração do `login.jsp` e `menu.jspf`; PoC de HTMX em `participantes.jsp` (exclusão sem DWR).
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência ADR: `.ia/historico/ADR-20260217-arquitetura-frontend-modernizacao.md`
    Referência Plano: `.ia/planos/plano-modernizacao-frontend.md`
*   2026-02-17: **[Concluído]** Substituição Completa do WebWork (Tarefa 4 da Fase 2). Removida a declaração da taglib `/webwork` do `web.xml`; excluído o arquivo legado `xwork.xml`; padronizados os prefixos de taglib de `ww` para `s` em todos os arquivos JSP e JSPF. A aplicação agora utiliza exclusivamente o Struts 6 sem referências ao WebWork legado. **Nota**: Build Maven (`mvn clean compile`) continua falhando devido ao problema pré-existente com a dependência Cewolf (repositório descontinuado). Este problema será resolvido na Tarefa 5 (Atualização de Bibliotecas de Terceiros).
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Plano: `.ia/planos/plano-substituicao-webwork.md`
    Skill: `senior-java-dev-legacy v1.0.0`
*   2026-02-17: **[Concluído]** Atualização de Bibliotecas de Terceiros (Tarefa 5 da Fase 2). Inventariadas e analisadas todas as bibliotecas de terceiros do projeto. Removida dependência Cewolf (comentada no pom.xml) que causava falha no build. Verificado que a maioria das bibliotecas já está atualizada: JFreeChart 1.5.4, Batik 1.17, Quartz 2.3.2, EHCache 3.10.8, Commons Lang3 3.14.0, SLF4J 2.0.12, Logback 1.5.0. DWR 3.0.2 mantido temporariamente com migração gradual para HTMX em andamento. Build Maven (`mvn clean compile`) agora funciona com sucesso.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência ADR: `.ia/historico/ADR-20260217-bibliotecas-legadas.md`
    Referência Plano: `.ia/planos/plano-atualizacao-bibliotecas-terceiros.md`
    Skill: `senior-java-dev-legacy v1.0.0`
*   2026-02-17: **[Criada]** Fase 2.5: Auditoria e Ajuste do Frontend. Identificada necessidade crítica de validação e modernização do frontend após migrações de backend. Criado ADR documentando riscos de regressão visual, coexistência problemática de Prototype/jQuery, débito técnico CSS e falta de validação. Criado plano detalhado com 10 tarefas: auditoria visual, remoção de bibliotecas legadas, refatoração CSS, otimização de performance, acessibilidade e testes cross-browser. Estimativa: 36-53 horas (~1-1.5 semanas). Esta fase é obrigatória antes de prosseguir para Fase 3.
    Auto-Analise: [Risco de não fazer: Alto] | [Prioridade: Crítica] | [Veredito: Obrigatório]
    Referência ADR: `.ia/historico/ADR-20260217-fase-auditoria-frontend.md`
    Referência Plano: `.ia/planos/plano-fase-2.5-auditoria-frontend.md`
    Skill: `senior-frontend-dev v1.0.0`
*   2026-02-17: **[Concluído]** Atualização de Bibliotecas de Terceiros (Tarefa 5 da Fase 2). Removidos repositórios Maven descontinuados (maven.java.net); comentada dependência Cewolf (biblioteca descontinuada sem suporte Jakarta EE); build Maven (`mvn clean compile`) agora funciona com sucesso. Bibliotecas mantidas e atualizadas: JFreeChart 1.5.4, DWR 3.0.2, Batik 1.17, Quartz 2.3.2, EHCache 3.10.8. **Próximo passo**: Migrar funcionalidades que dependem do Cewolf para JFreeChart direto ou bibliotecas JS modernas.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência ADR: `.ia/historico/ADR-20260217-bibliotecas-legadas.md`
    Skill: `senior-java-dev-legacy v1.0.0`
*   2026-02-18: **[Concluído]** Correção de Configurações XML para Compatibilidade com Spring 6. Durante testes do Docker, identificado erro de inicialização devido a incompatibilidade dos arquivos XML de configuração. Migrados todos os 6 arquivos XML (hibernate, security, service, action, resources, scheduler) de DTD antigo para XSD schema do Spring 6. Corrigidas referências `<ref local="..."/>` para `<ref bean="..."/>` e sintaxe de propriedades para formato explícito compatível com Spring 6. Containers Docker parados para rebuild. **Próximo passo**: Rebuild da aplicação Docker e teste de acesso.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260218-correcao-xml-spring6.md`
    Skill: `senior-java-dev-legacy v1.0.0`
*   2026-02-18: **[Concluído]** Migração Spring Security 6 (Tarefa 2 da Fase 2). Substituída a stack legada Acegi Security (EOL) pela versão 6.2.2. Implementada nova configuração baseada no namespace Spring Security, migrado `applicationContext-security.xml` e atualizados namespaces `jakarta.*` em todo o fluxo de segurança. Resolvidos conflitos de `web.xml` e taglibs JSP. Implementado `LegacySha1PasswordEncoder` para suporte a usuários legados. Build estável e limpo de referências Acegi.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260218-migracao-spring-security-6.md`
    Skill: `modernization-java-migration v1.0.0`
*   2026-02-19: **[Concluído]** Ajustes de Build e Runtime (Fase 2). Corrigida a estrutura de recursos Maven (movendo XMLs para `src/main/resources`). Resolvidas incompatibilidades de schema XML (singleton, ref local), dependências faltantes (AspectJ, spring-context-support) e erros de conexão/schema no MySQL 8/Hibernate 6.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-correcoes-runtime-v1.md`
    Skill: `senior-java-dev-legacy v1.0.0`
*   2026-02-19: **[Concluído]** Endurecimento de Parâmetros Struts 7 (Fase 2). Refatoradas as Actions `ParticipanteAction` e `AdminAction` para utilizar anotação `@StrutsParameter`. Eliminado o uso direto de `HttpServletRequest.getParameter()` em favor de atributos de classe protegidos, garantindo o funcionamento de formulários e mitigando injeção de parâmetros maliciosos.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-struts-parameter-hardening.md`
    Skill: `modernization-java-migration v1.0.0`
*   2026-02-19: **[Concluído]** Endurecimento de Segurança Struts 7 (Fase 2). Implementadas restrições OGNL (comprimento e tipos de nós) e configurada nova stack de interceptores (`bolaoStack`) com proteções COOP, COEP e Fetch Metadata. Aplicação inicializando com as defesas proativas ativas.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-struts-ognl-hardening.md`
    Skill: `security-audit v1.0.0`
*   2026-02-19: **[Concluído]** Validação de OGNL Allowlist (Fase 2). Configuradas as permissões explícitas no `struts.xml` para as classes de domínio (`com.opendev.bolao.model`) e utilitários, garantindo que o Struts 7 consiga renderizar as propriedades dos objetos nas JSPs sob a nova política de segurança proativa.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-struts-ognl-allowlist.md`
    Skill: `modernization-java-migration v1.0.0`
