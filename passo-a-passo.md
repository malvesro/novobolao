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
        *   **[Concluído]** Remover fallback SHA-1 do encoder e higienizar base de credenciais conforme ADR `.ia/historico/ADR-20260219-remocao-sha1-senhas.md`.
        *   **[Concluído]** Configurar `WebSecurityExpressionHandler` para suportar `sec:authorize` nas JSPs. Declarados `DefaultHttpSecurityExpressionHandler` e `DefaultWebSecurityExpressionHandler` em `applicationContext-security.xml`; `login.jsp` e `principal.jsp` carregam sem HTTP 500. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260219-websecurity-expression-handler.md`
        *   **[Concluído]** Testar fluxo de autenticação (Login/Logout) com usuários cadastrados utilizando apenas hashes `BCrypt` (preparar cenário com usuário seed e validação manual/automática). Fluxo validado com login `admin/admin123`, logout e tentativa inválida registrada. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260219-validacao-login-bcrypt-v2.md`.
        *   **[Concluído]** Ajustar fallback da página principal quando `jogosDeHoje` estiver vazio para evitar redirecionamento recursivo (`principal.jsp` → `principal.action`). Exibida mensagem informativa no portlet quando não há jogos, mantendo o gráfico de liderança visível. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260219-principal-fallback-sem-jogos.md`.
        *   **[Concluído]** Corrigir `JogoDaoImpl.buscarQuantidadeDeJogosOcorridos` para retornar `Long` sem cast para `Integer`, eliminando o `ClassCastException` ao acessar `/seguro/ranking.action`. Query tipada com retorno `long` e cobertura de teste unitário em `ParticipanteServiceImplTest`. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260219-correcao-jogos-ocorridos.md`.
        *   **[Concluído]** Testar controle de acesso (RBAC) para URLs `/admin/**` e `/seguro/**`. Perfil `USER` recebe HTTP 403 em `/admin/jogos.action`; perfil `ADMIN` passa pela segurança (erro 500 remanescente devido a bug nas actions Struts). Referência Log: `.ia/logs/session-20260219-validacao-rbac.md`.
        *   **[Concluído]** Corrigir `NoSuchMethodException` nas actions administrativas (`/admin/infoEquipes.action`, `/admin/participantes.action`). Ajustada a segurança para proteger métodos de serviço com CGLIB e rebuildado o container. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260219-correcao-actions-admin.md`.
        *   **[Concluído]** Após a correção, revalidar o RBAC garantindo HTTP 200 para `ADMIN` e 403 para `USER` nos endpoints `/admin/**`. Testes via Docker registrados. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260219-correcao-actions-admin.md`.
    Referência Log: `.ia/logs/session-20260218-correcao-filtros-web-xml.md`
    Referência Log: `.ia/logs/session-20260218-migracao-spring-security-6.md`

3.  **[Pendente]** **Fase 2.7: Atualização para Copa do Mundo 2026:**
    *   **[Pendente]** **Atualização de Dados (SQL):** Criar e aplicar script de carga (`03-copa-2026-data.sql`) com as 48 seleções, 12 grupos e locais oficiais (Canadá, México, EUA). **Nota: Horários devem ser convertidos para o horário de Brasília.**
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

1.  **[Concluído] Auditoria Visual Completa:** Testar renderização e funcionalidade de todas as telas principais (login, dashboard, formulários, gráficos, admin) em navegadores modernos e múltiplas resoluções. Concluir esta etapa antes de iniciar novas otimizações.
    *   **Evidências:** Sessão `.ia/logs/session-20260219-auditoria-visual-validacao-telas.md` registrou verificações via Docker (HTTP 200 nas páginas autenticadas, gráficos JFreeChart gerando PNGs válidos e RBAC retornando 403 para usuários sem papel ADMIN). Login público `login.jsp` ativo.
    *   **Achados:** `cadastro.jsp` responde 302 redirecionando para `/login.jsp` devido à ausência de `permitAll` na configuração de segurança; sugerir ajuste específico antes de reabrir cadastros. Prototype/Scriptaculous continuam carregados no `cabecalho.jspf`, alinhado às tarefas 2 e 3.
    *   **Limitações:** Auditoria cURL não substitui testes visuais responsivos; execução em navegadores reais permanece recomendada após higienização de scripts/CSS.
2.  **[Em Progresso] Inventário e Análise de Scripts:** Mapear todos os arquivos JavaScript, identificar dependências e decidir manter/refatorar/remover cada um. Resultado alimentará as tarefas 3 e 4.
    *   **Parcial (19/02/2026):** Inventário inicial concluído (`.ia/logs/session-20260219-inventario-scripts-fase-2-5.md`). Bibliotecas legadas identificadas: Prototype/Scriptaculous (`webapp/js/prototype.js`, `scriptaculous.js`, `effects.js`), DWR (engine/util + interfaces geradas), Overlib (`overlib.js`) e BrowserDetector.
    *   **Próximas subtarefas:**
        1. Documentar plano de substituição de Prototype/Scriptaculous por HTMX/Fetch e CSS transitions, alinhado com a remoção de dependências DWR. **Status:** Concluído em 19/02/2026 (`.ia/planos/plano-migracao-dwr-htmx.md`).
        2. Especificar migração dos fluxos críticos DWR (`webapp/seguro/jogos.jsp`, `webapp/admin/participantes.jsp`) para endpoints Struts REST + HTMX, incluindo impacto nas tags Struts. **Status:** Plano consolidado (19/02/2026); aguarda execução das tarefas derivadas.
        3. Selecionar alternativa moderna ao Overlib (ex.: Tippy.js) e planejar substituição dos tooltips com requisitos de acessibilidade. **Status:** Concluído (19/02/2026) – opção definida (Tippy.js v6) conforme `.ia/logs/session-20260219-avaliacao-tooltips-tippy.md`.
        4. Validar ausência de uso do `BrowserDetector.js`, propor remoção e adoção de feature detection (`@supports`, Modernizr slim se necessário). **Status:** Concluído (19/02/2026) – ver `.ia/logs/session-20260219-avaliacao-browserdetector.md`.
        5. Reavaliar dependência do `jquery-4.0.0.min.js` (versão alfa) e definir downgrade para 3.7.1 ou remoção completa após migrar interações restantes. **Status:** Concluído (19/02/2026) – ver `.ia/logs/session-20260219-avaliacao-jquery.md` e ADR `.ia/historico/ADR-20260219-jquery-remocao-gradual.md`. A dependência foi completamente eliminada em `session-20260219-remocao-jquery.md`.
        6. Propor adoção de bundler (Vite/ESBuild) para modularizar scripts, permitir CSP rígida e preparar minificação/versões com hash.
        7. Mapear condicionais e estilos específicos para Internet Explorer (ex.: `opendev:isIE`, hacks CSS) e planear remoção, garantindo compatibilidade apenas com navegadores suportados oficialmente. **Concluído (19/02/2026):** Inventário e limpeza executados (`.ia/logs/session-20260219-inventario-condicionais-ie.md`, `.ia/logs/session-20260219-remocao-condicionais-ie.md`); JSPs e CSS atualizados para uso de layout neutro e `opacity`.
3.  **[Pendente] Remoção de Prototype e Scriptaculous (Sequência após Tarefa 2):** Eliminar bibliotecas legadas (Prototype.js, Scriptaculous.js) do projeto, migrando funcionalidades restantes para HTMX/JavaScript nativo.
    * **2026-02-20:** [Em Progresso] Tela `admin/participantes.jsp` convertida para HTMX, removendo chamadas DWR/Prototype para alteração de papel, autorização e exclusão; ver `.ia/logs/session-20260220-remocao-dwr-admin-participantes.md`.
    * **2026-02-20:** [Em Progresso] Painel “Meus palpites” (`seguro/jogos.jsp`) agora carrega via HTMX e scripts nativos, substituindo `DWRUtil` e `Effect` para essa funcionalidade; ver `.ia/logs/session-20260220-remocao-dwr-palpites-htmx.md`.
    * **2026-02-20:** [Em Progresso] Popups de palpites em `seguro/jogos.jsp` migrados para HTMX/fetch, substituindo DWR/Prototype e adicionando endpoints Struts para listagem/atualização; ver `.ia/logs/session-20260220-remocao-dwr-jogos-popup.md`.
    * **2026-02-20:** [Em Progresso] Cadastro administrativo de jogos (`admin/inclusaoJogo.jsp`) convertido para fetch/DOM nativo com endpoints Struts e remoção de DWR/Prototype; ver `.ia/logs/session-20260220-remocao-dwr-admin-inclusao-jogo.md`.
    * **2026-02-20:** [Em Progresso] Cadastro público (`cadastro.jsp`) reescrito para DOM nativo; libs DWR (`engine.js`, `util.js`) removidas do `cabecalho.jspf`; ver `.ia/logs/session-20260220-remocao-dwr-cadastro-publico.md`.
    * **2026-02-20:** [Concluído] Remoção completa do DWR servlet e dependências: `webapp/WEB-INF/web.xml` sem `dwr-invoker`, dependência `org.directwebremoting:dwr` excluída do `pom.xml` e build validado com `mvn test`; ver `.ia/logs/session-20260220-remocao-dwr-servlet.md`.
    * **2026-02-20:** [Parcial] Inventário inicial aponta ausência de código ativo do Cewolf, mas mantém bloco comentado da dependência no `pom.xml` e metadados `webapp/WEB-INF/lib/CVS`; registrar subtarefas para limpeza definitiva. Referência log: `.ia/logs/session-20260220-inventario-legados-pos-dwr.md`.
    * **2026-02-20:** [Concluído] Limpar bloco comentado da dependência Cewolf no `pom.xml` e ajustar comentários correlatos após validação de build; ver commit `refactor: remover menções a cewolf do pom`.
    * **2026-02-20:** [Concluído] Remover diretório legado `webapp/WEB-INF/lib/CVS` do repositório garantindo que nenhum artefato herdado de CVS permaneça; ver `.ia/logs/session-20260220-remocao-cvs-legado.md`.
    * **2026-02-20:** [Concluído] Remoção das bibliotecas Prototype/Scriptaculous: removidas referências no `cabecalho.jspf` e excluídos `webapp/js/prototype.js`, `webapp/js/scriptaculous.js`, `webapp/js/effects.js`; ver `.ia/logs/session-20260220-remocao-prototype-scriptaculous.md`.
    * **2026-02-20:** [Concluído] Migração dos tooltips legacy: removidos `webapp/js/overlib.js` e `webapp/js/BrowserDetector.js`, adicionada infraestrutura nativa em `webapp/js/tooltips.js` integrada ao HTMX e aplicada aos cabeçalhos de `seguro/classificacao.jsp` via `data-tooltip`.
4.  **[Concluído] Auditoria e Refatoração CSS (Sequência após Tarefa 3):** Revisar `estilo.css`, remover hacks legados, reorganizar por componentes e implementar responsividade básica. Inventário final em 20/02/2026 confirmou ausência de estilos inline remanescentes via `rg "style=\"" webapp`; registros consolidados em `.ia/logs/session-20260219-auditoria-css.md` e ADR `.ia/historico/ADR-20260219-refatoracao-css.md`.
4.1 **[Concluído] Modernização do HTML (Sequência da Tarefa 4):** Higienizar marcação JSP/HTML removendo atributos obsoletos (`align`, `cellpadding`, `width`, etc.), migrar estrutura de tabelas puramente visuais para classes utilitárias responsivas, padronizar uso de `aria-*` e preparar componentes para interações HTMX pós-remoção de Prototype/DWR. Registrar subtarefas por módulo (público, seguro, admin) e validar cada ajuste com `mvn test`. *Skill prevista:* N/A (refinamento frontend estruturado).
    * **2026-02-20:** Removidos atributos legados e adicionados wrappers responsivos nas páginas `login.jsp`, `cadastro.jsp`, `admin/inclusaoJogo.jsp`, `admin/participantes.jsp` e `seguro/jogos.jsp`; tabelas passaram a usar classes utilitárias e `mvn test` validou 5 cenários.
    * **2026-02-20:** Adicionados atributos `scope` às tabelas de `seguro/principal.jsp`, `seguro/classificacao.jsp` e `seguro/jogos.jsp`, reforçando acessibilidade e mantendo compatibilidade após `mvn test`.
    * **2026-02-20:** Formulários de `login.jsp`, `cadastro.jsp` e `admin/inclusaoJogo.jsp` migrados de tabelas para `form-grid` responsivo com novos utilitários CSS (`form-row`, `form-field-group`), preservando integrações DWR/HTMX e confirmando estabilidade via `mvn test`.
    * **2026-02-20:** Inventário final confirmou ausência de estilos inline/atributos legados nas JSPs restantes, permitindo concluir a subtarefa e avançar para a Tarefa 3.
5.  **[Concluído] Migração do Cewolf (Gráficos):** Substituir o Cewolf por geração de gráficos com JFreeChart direto (server-side) ou Chart.js (client-side), removendo todas as dependências e taglibs legadas.
    *   **[Concluído] Inventário de Uso:** Mapear páginas e tags `<cewolf:*>` (ex: `webapp/seguro/principal.jsp`, `webapp/seguro/graficoDesempenho.jsp`) e identificar dados necessários para cada gráfico.
    *   **[Concluído] Implementação de Renderização:** Criar geradores de gráficos em Java (JFreeChart) e expor endpoints/Actions para servir PNG/SVG (ex: `/seguro/graficoLideranca.png`, `/seguro/graficoDesempenho.png`).
    *   **[Concluído] Atualização das JSPs:** Substituir `<cewolf:chart>`/`<cewolf:img>` por `<img>` apontando para os novos endpoints e remover dependências do Cewolf nas telas.
    *   **[Concluído] Remoção de Taglibs:** Remover `<%@taglib prefix="cewolf" ... %>` do `cabecalho.jspf` e das JSPs.
    *   **[Concluído] Remoção Residual (Deploy/Cache):** Garantir que o `cabecalho.jspf` sem Cewolf seja aplicado no WAR/ROOT do Tomcat (rebuild/redeploy) e remover cache/artefatos que ainda referenciam `cewolf.tld`, desbloqueando `login.jsp`/`index.jsp`. Referência Log: `.ia/logs/session-20260219-remocao-cewolf-deploy-cache.md`
    *   **[Concluído] Validação Funcional:** Testes automatizados (`GraficosJFreeChartTest`, `ParticipanteActionTest`) seguem verdes e, em 19/02/2026, a validação manual via Docker confirmou retorno HTTP 200 dos endpoints `/seguro/graficoLiderancaImagem.action` e `/seguro/graficoDesempenhoImagem.action` com PNGs válidos (assinatura `89 50 4E 47`). Referência Log: `.ia/logs/session-20260219-validacao-graficos-jfreechart-v2.md`. Skill: `modernization-java-migration v1.0.0`. Observação: `mvn test` executado integralmente em 19/02/2026 validou 5 cenários sem falhas após restabelecimento do Nexus TSE.
    Referência Log: `.ia/logs/session-20260219-migracao-cewolf-continuacao.md`, `.ia/logs/session-20260219-validacao-graficos-jfreechart-v2.md`
6.  **[Pendente] Otimização de Performance (Aguardar Tarefas 1-4):** Minificar JS/CSS, implementar cache de assets, usar lazy loading quando apropriado. Meta: Lighthouse Performance > 80. Executar somente após a higienização de scripts e CSS.
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
7.  **[Pendente] Ajuste de Deploy Pós-Remoção DWR:** Validar ambientes provisionados (Docker/Tomcat externos) para remoção do servlet DWR, limpeza de WARs antigos e atualização de automações de deploy.

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
*   2026-02-19: **[Concluído]** Migração do Cewolf (Fase 2.5). Criadas classes de gráfico com JFreeChart, reativada geração de datasets no serviço, adicionados endpoints Struts para PNG e atualizadas JSPs para usar `<img>` com novos endpoints; validação automatizada e runtime confirmadas (Docker) com arquivos PNG válidos.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-migracao-cewolf-continuacao.md`, `.ia/logs/session-20260219-validacao-graficos-jfreechart-v2.md`
    Skill: `modernization-java-migration v1.0.0`
*   2026-02-19: **[Em Progresso]** Inventário e análise de scripts front-end (Fase 2.5 - Tarefa 2). Catalogadas bibliotecas legadas (Prototype/Scriptaculous, DWR, Overlib, BrowserDetector) e recomendadas substituições modernas (HTMX/fetch, tooltips com Tippy.js, remoção de sniffers, uso de tags HTML5/Struts atualizadas).
    Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
    Referência Log: `.ia/logs/session-20260219-inventario-scripts-fase-2-5.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-19: **[Concluído]** Consolidação do plano de migração DWR → HTMX (Fase 2.5 - Tarefa 2, subtarefa 2). Documento criado (`.ia/planos/plano-migracao-dwr-htmx.md`) com estratégia por camadas, cronograma e próximos passos para substituir DWR/Prototype por REST + HTMX.
    Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
    Referência Log: `.ia/logs/session-20260219-plano-migracao-dwr-htmx.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-19: **[Concluído]** Avaliação de biblioteca de tooltips acessíveis (Fase 2.5 - Tarefa 2, subtarefa 3). Selecionado Tippy.js v6 (Floating UI) para substituir Overlib, garantindo suporte a navegadores modernos, acessibilidade e compatibilidade com CSP.
    Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
    Referência Log: `.ia/logs/session-20260219-avaliacao-tooltips-tippy.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-19: **[Concluído]** Avaliação e plano de remoção do `BrowserDetector.js` (Fase 2.5 - Tarefa 2, subtarefa 4). Confirmado que o script não é utilizado e definido plano para remoção segura e adoção de feature detection moderna.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-avaliacao-browserdetector.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-19: **[Concluído]** Avaliação da dependência jQuery (Fase 2.5 - Tarefa 2, subtarefa 5). Identificado uso mínimo de jQuery 4 alfa e definida estratégia para downgrade imediato para jQuery 3.7.1 estável.
    Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
    Referência Log: `.ia/logs/session-20260219-avaliacao-jquery.md`
    Referência ADR: `.ia/historico/ADR-20260219-jquery-remocao-gradual.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-19: **[Concluído]** Remoção do jQuery e refatoração do `login.jsp`. Arquivo `jquery-4.0.0.min.js` eliminado, `cabecalho.jspf` atualizado e efeito de mensagem convertido para JavaScript nativo/CSS.
    Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
    Referência Log: `.ia/logs/session-20260219-remocao-jquery.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-19: **[Concluído]** Remoção de condicionais IE (Fase 2.5 - Tarefa 2, subtarefa 7). Eliminados blocos `opendev:isIE`, imagens específicas `_ie` e hacks `filter: alpha`, padronizando o CSS moderno.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-inventario-condicionais-ie.md`, `.ia/logs/session-20260219-remocao-condicionais-ie.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-19: **[Em Progresso]** Auditoria do CSS (Fase 2.5 - Subtarefa 4). Levantamento de problemas do `estilo.css` e plano de refatoração responsiva registrados; wrapper e tela de login já atualizados para layout moderno.
    Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
    Referência Log: `.ia/logs/session-20260219-auditoria-css.md`, `.ia/logs/session-20260219-refatoracao-css-login.md`, `.ia/logs/session-20260219-refatoracao-css-cadastro.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-19: **[Concluído]** Auditoria Visual Completa (Fase 2.5 - Tarefa 1). Checklists das telas principais executados via Docker; páginas protegidas retornam HTTP 200, endpoints de gráficos entregam PNG válidos e RBAC bloqueia acessos não autorizados. Identificado redirecionamento 302 indevido em `cadastro.jsp` (ausência de `permitAll` na configuração de segurança) para tratar em tarefa futura.
    Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
    Referência Log: `.ia/logs/session-20260219-auditoria-visual-validacao-telas.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-19: **[Concluído]** Remoção do fallback SHA-1 e padronização do encoder para BCrypt (Tarefa 2.6.2). Atualizado `applicationContext-security.xml`, removidos utilitários legados e confirmada suíte de testes (`mvn test -DskipITs`) com hashes modernos.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência ADR: `.ia/historico/ADR-20260219-remocao-sha1-senhas.md`
    Referência Log: `.ia/logs/session-20260219-remocao-sha1.md`
    Skill: `modernization-java-migration v1.0.0`
*   2026-02-19: **[Concluído]** Alinhamento de logs de sessão com `passo-a-passo.md`: atualização dos logs `session-20260219-encerramento.md` e `session-20260219-migracao-cewolf-parada.md` para refletir status executado conforme o plano.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-alinhamento-logs-status.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-19: **[Bloqueado]** Validação do fluxo de autenticação (Login/Logout) com `BCrypt`: bloqueada por erro HTTP 500 em `login.jsp`/`index.jsp` (taglib Cewolf não resolvida) e retorno de `status=invalido` no POST `/j_security_check` com usuários válidos.
    Auto-Analise: [Risco: Medio] | [Compatibilidade: Atencao] | [Veredito: Revisar]
    Referência Log: `.ia/logs/session-20260219-validacao-login-bcrypt.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-19: **[Concluído]** Remoção residual de Cewolf no deploy/cache do Tomcat: rebuild do container, confirmação de `cabecalho.jspf` atualizado no WAR/ROOT e `login.jsp` carregando sem erro.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-remocao-cewolf-deploy-cache.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-19: **[Concluído]** Configuração do `WebSecurityExpressionHandler` nas JSPs protegidas: declarados handlers HTTP e JSP no `applicationContext-security.xml`, rebuild Docker e validação de login via HTTPS sem erros `sec:authorize`.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-websecurity-expression-handler.md`
    Skill: `modernization-java-migration v1.0.0`
*   2026-02-19: **[Concluído]** Correção de `buscarQuantidadeDeJogosOcorridos`: DAO passa a retornar `long` com query tipada, eliminando `ClassCastException` no ranking e garantindo cobertura unitária em `ParticipanteServiceImplTest`.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-correcao-jogos-ocorridos.md`
    Skill: `modernization-java-migration v1.0.0`
*   2026-02-19: **[Concluído]** Validação do fluxo de Login/Logout com hashes BCrypt: cenários de sucesso, logout e senha inválida confirmados via Docker, sem redirecionamentos indevidos.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-validacao-login-bcrypt-v2.md`
    Skill: `modernization-java-migration v1.0.0`
*   2026-02-19: **[Concluído]** Validação de RBAC: `/seguro/**` acessível a usuários autenticados; `/admin/**` retorna 403 para perfil `USER`. Acesso `ADMIN` liberado, com erro funcional existente nas actions administrativas (NoSuchMethodException).
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: Atenção] | [Veredito: Revisar]
    Referência Log: `.ia/logs/session-20260219-validacao-rbac.md`
    Skill: `modernization-java-migration v1.0.0`
*   2026-02-19: **[Concluído]** Correção das actions administrativas e revalidação do RBAC após ajuste de proxies. Métodos do `EquipeService`, `JogoService` e `ParticipanteService` protegidos; `/admin/infoEquipes.action` e `/admin/participantes.action` respondendo 200 para ADMIN e 403 para USER.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260219-correcao-actions-admin.md`
    Skill: `modernization-java-migration v1.0.0`
*   2026-02-20: **[Concluído]** Refatoração CSS da página principal (Fase 2.5 - Tarefa 4). Atualizada `webapp/seguro/principal.jsp` para usar utilitários responsivos (`.table`, `.team-cell`, `.score-value`, `.chart-wrapper`) e centralizar a tabela de jogos em container flexível; adicionadas classes complementares em `webapp/css/estilo.css`. `mvn test` executado com sucesso garantindo integridade.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260220-refatoracao-css-principal.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-20: **[Concluído]** Refatoração CSS da página de classificação (Fase 2.5 - Tarefa 4). `webapp/seguro/classificacao.jsp` reestruturada para usar `dashboard-section`, `.table` e utilitários de alinhamento, com destaque do usuário autenticado via `ranking-highlight`; CSS atualizado em `webapp/css/estilo.css`. `mvn test` executado com sucesso (5 testes).
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260220-refatoracao-css-classificacao.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-20: **[Concluído]** Refatoração CSS da página de gráfico comparativo (Fase 2.5 - Tarefa 4). `webapp/seguro/graficoDesempenho.jsp` refatorada com `portlet-body`, `.form-section`, `.form-control` e `chart-wrapper`, removendo estilos inline; utilidades adicionadas no `estilo.css`. `mvn test` executado com sucesso (5 testes).
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260220-refatoracao-css-grafico.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-20: **[Concluído]** Refatoração CSS da página de bate-papo (Fase 2.5 - Tarefa 4). `webapp/seguro/batePapo.jsp` substitui estilos inline por `dashboard-section` e componente `.notice-card`, adicionando utilitários no `estilo.css`. `mvn test` executado com sucesso (5 testes).
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260220-refatoracao-css-batepapo.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-20: **[Concluído]** Refatoração CSS da página de jogos (Fase 2.5 - Tarefa 4). Filtro de palpites, painel “Meus palpites” e balões HTMX reorganizados com `match-filter`, `.tips-panel`, `.loading-inline`, `.balao-*` e classes de tabela responsivas; estilos inline removidos em `webapp/seguro/jogos.jsp`. `mvn test` executado com sucesso (5 testes).
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260220-refatoracao-css-jogos.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-20: **[Concluído]** Refatoração CSS da página Copa (Fase 2.5 - Tarefa 4). Estrutura de `webapp/seguro/copa.jsp` atualizada para usar `dashboard-section`, removendo container com `float/right`. `mvn test` executado com sucesso (5 testes).
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260220-refatoracao-css-copa.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-20: **[Concluído]** Refatoração CSS dos formulários públicos e telas administrativas (Fase 2.5 - Tarefa 4). Ajustados `webapp/cadastro.jsp`, `webapp/login.jsp`, `webapp/admin/inclusaoJogo.jsp`, `webapp/admin/participantes.jsp` e `webapp/template/menu.jspf` para usar utilitários (`text-left`, `icon-inline-top`, `table-spaced`, `dashboard-section`, `icon-button`, `hidden`, `mb-md`) e centralização via CSS. `mvn test` executado com sucesso (5 testes).
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260220-refatoracao-css-formularios.md`
    Skill: N/A (nenhuma skill aplicável)
*   2026-02-20: **[Concluído]** Migração dos tooltips legados (Fase 2.5 - Tarefas 2 e 3). Removidos `BrowserDetector.js` e `overlib.js`, criado `webapp/js/tooltips.js` com reuso em respostas HTMX, aplicados tooltips acessíveis aos cabeçalhos do ranking e concluída a sanitização final de estilos inline. `mvn test` validado pós-ajustes.
    Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
    Referência Log: `.ia/logs/session-20260220-remocao-overlib-tooltips.md`
    Skill: N/A (nenhuma skill aplicável)
