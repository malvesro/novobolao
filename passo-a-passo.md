# Plano de Evolução e Modernização - Sistema Bolão

Referências:

* `README.md`
* `.ia/historico/ADR-*-estrategia-migracao-stack.md`
* `.ia/diretrizes/arquitetura.md`
* `analise-inicial.md` (Relatório da Análise Inicial Profunda do Projeto)

Legenda de status:

* `Pendente`
* `Em Progresso`
* `Concluído`
* `Bloqueado`
* `Cancelado`

Diretriz fixa:

* Definir empacotamento WAR e deploy.
* Definir o que não pode ser usado (exemplo: Não usar spring boot) - **Nota: Esta diretriz pode ser revisada conforme as recomendações de modernização.**

Premissas de compatibilidade (críticas):

* Exemplo: Struts 7 exige Java 17+ e Jakarta Servlet 6+ (jakarta.*). - **Nota: Estas premissas serão definidas com base nas escolhas de tecnologias para a modernização.**

## Fases de Modernização e Atividades (executar em sequência)

### Fase 1: Curto Prazo - Segurança e Estabilidade Mínima (IMEDIATA)

1. **[Concluído] Forçar HTTPS:** Configurar o servidor de aplicação para forçar o uso de HTTPS para toda a aplicação.
2. **[Concluído] Desativar DWR Debug:** Desabilitar o modo `debug=true` do DWR no `web.xml`.
3. **[Concluído] Auditoria e Melhoria de Credenciais:** Avaliar e planejar a migração do hashing de senhas de SHA-1 para um algoritmo seguro (ex: bcrypt).
4. **[Concluído] Avaliar e Isolar/Remover Funcionalidades Críticas:** Identificar e, se possível, isolar ou desativar funcionalidades que dependem de bibliotecas EOL com vulnerabilidades conhecidas, caso a migração seja demorada. (Chat desativado em favor da recriação futura).

### Fase 2: Médio Prazo - Redução de Débito Técnico e Manutenção (ALTA PRIORIDADE)

1. **[Concluído] Upgrade do Spring Framework & WebWork:** Planejar e executar a migração do Spring Framework 1.2.8 para **Spring Framework 6** (Standalone) e do WebWork para **Struts 6.x** (Jakarta EE).
   Referência ADR: `.ia/historico/ADR-20260217-upgrade-spring-framework.md`
   
   * **[Concluído]** Atualização do `pom.xml` (BOMs Spring 6, Jakarta EE 10, Struts 6).
   * **[Concluído]** Migração de Namespace: Substituir `javax.servlet` e `javax.persistence` por `jakarta.*`.
   * **[Concluído]** Migração Struts: Converter Actions (`ActionSupport`), atualizar imports (`xwork2`) e converter tags JSPs (`<ww:*>` -> `<s:*>`).
   * **[Concluído]** Migração Hibernate: Remover `HibernateTemplate` (descontinuado) e adaptar DAOs para usar `SessionFactory.getCurrentSession()` e API do Hibernate 6.
   * **[Concluído]** Adaptação de Configuração: Atualizar `web.xml` (FilterDispatcher -> StrutsPrepareAndExecuteFilter) e esquemas XSD dos XMLs do Spring.
   * **[Concluído]** **Endurecimento Struts 7 (Segurança):**
     * **[Concluído]** Aplicar anotação `@StrutsParameter` em todos os setters de Actions que recebem dados de formulários (Obrigatório no Struts 7).
     * **[Concluído]** Configurar `struts.ognl.expressionMaxLength` e `struts.ognl.excludedNodeTypes` no `struts.xml`.
     * **[Concluído]** Validar se `struts.allowlist.enable=true` está ativo e mapear classes customizadas necessárias.
     * **[Concluído]** Implementar interceptores de isolamento de recursos (Fetch Metadata, COOP/COEP).
       Referência ADR: `.ia/historico/ADR-20260219-upgrade-struts-7.md`
       Referência Diretrizes: `.ia/diretrizes/seguranca.md`

2. **[Concluído] Migração da Segurança:** Planejar e executar a substituição do Acegi Security 1.0.0 por Spring Security 6+.
   **CRÍTICO - BLOQUEADOR**: Durante testes do Docker (2026-02-18), identificado que `applicationContext-security.xml` ainda usa classes do Acegi Security (EOL desde 2006), incompatível com Jakarta EE 10. Spring Security 6.2.2 já está no `pom.xml` mas não é utilizado. Necessário reescrever completamente a configuração de segurança.
   
   * **[Concluído]** Reescrever `applicationContext-security.xml` usando Spring Security 6
   * **[Concluído]** Atualizar `web.xml` para usar `springSecurityFilterChain`
   * **[Concluído]** Migrar beans de autenticação (DaoAuthenticationProvider)
   * **[Concluído]** Migrar beans de autorização (AccessDecisionManager)
   * **[Concluído]** Configurar password encoder (BCrypt)
    * **[Concluído]** **Validação Integrada de Segurança:**
     * **[Concluído]** Validar **OGNL Allowlist** no Struts 7 para classes do pacote `com.opendev.bolao.model` e utilitários de exibição. (Skill: `modernization-java-migration v1.0.0`)
     * **[Concluído]** Remover fallback SHA-1 do encoder e higienizar base de credenciais conforme ADR `.ia/historico/ADR-20260219-remocao-sha1-senhas.md`.
     * **[Concluído]** Configurar `WebSecurityExpressionHandler` para suportar `sec:authorize` nas JSPs. Declarados `DefaultHttpSecurityExpressionHandler` e `DefaultWebSecurityExpressionHandler` em `applicationContext-security.xml`; `login.jsp` e `principal.jsp` carregam sem HTTP 500. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260219-websecurity-expression-handler.md`
     * **[Concluído]** Testar fluxo de autenticação (Login/Logout) com usuários cadastrados utilizando apenas hashes `BCrypt` (preparar cenário com usuário seed e validação manual/automática). Fluxo validado com login `admin/admin123`, logout e tentativa inválida registrada. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260219-validacao-login-bcrypt-v2.md`.
     * **[Concluído]** Ajustar fallback da página principal quando `jogosDeHoje` estiver vazio para evitar redirecionamento recursivo (`principal.jsp` → `principal.action`). Exibida mensagem informativa no portlet quando não há jogos, mantendo o gráfico de liderança visível. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260219-principal-fallback-sem-jogos.md`.
     * **[Concluído]** Corrigir `JogoDaoImpl.buscarQuantidadeDeJogosOcorridos` para retornar `Long` sem cast para `Integer`, eliminando o `ClassCastException` ao acessar `/seguro/ranking.action`. Query tipada com retorno `long` e cobertura de teste unitário em `ParticipanteServiceImplTest`. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260219-correcao-jogos-ocorridos.md`.
     * **[Concluído]** Testar controle de acesso (RBAC) para URLs `/admin/**` e `/seguro/**`. Perfil `USER` recebe HTTP 403 em `/admin/jogos.action`; perfil `ADMIN` passa pela segurança (erro 500 remanescente devido a bug nas actions Struts). Referência Log: `.ia/logs/session-20260219-validacao-rbac.md`.
     * **[Concluído]** Corrigir `NoSuchMethodException` nas actions administrativas (`/admin/infoEquipes.action`, `/admin/participantes.action`). Ajustada a segurança para proteger métodos de serviço com CGLIB e rebuildado o container. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260219-correcao-actions-admin.md`.
     * **[Concluído]** Após a correção, revalidar o RBAC garantindo HTTP 200 para `ADMIN` e 403 para `USER` nos endpoints `/admin/**`. Testes via Docker registrados. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260219-correcao-actions-admin.md`.
       Referência Log: `.ia/logs/session-20260218-correcao-filtros-web-xml.md`
       Referência Log: `.ia/logs/session-20260218-migracao-spring-security-6.md`

3. **[Pendente]** **Fase 2.7: Atualização para Copa do Mundo 2026:**
   
   * **[Concluído]** **Atualização de Dados (SQL):** Criar e aplicar script de carga (`03-copa-2026-data.sql`). Concluído em 28/03/2026 com expansão para 104 jogos, mapeamento de cruzamentos e automação via script bash.
   * **[Concluído]** **Configurações de Contexto:** Atualizar parâmetros de cidades-sede e datas no `web.xml` para refletir o calendário de Junho/Julho de 2026 (`web.xml` ajustado em 21/02/2026 com datas 11/06–19/07, horários em BRT e estádios oficiais).
   * **[Concluído]** **Lógica de Fases:** Adaptar o sistema para suportar a nova fase de "16-avos de final" (mata-mata ampliado).
   * `Jogo` passou a expor `isFaseDeGrupos`/`descricaoFase` via `FaseUtils`; tabela de jogos exibe o nome da fase (ex.: 32-avos) quando não há grupo.
   * **[Pendente]** **Auditoria Visual de Escala:** Garantir que as telas de palpites e classificação suportem o aumento de volume de dados (48 seleções vs 32 anteriores).
     Referência FIFA: `https://www.fifa.com/pt/tournaments/mens/worldcup/canadamexicousa2026/articles/copa-mundo-2026-tabela-jogos`
     Subtarefa:
     * **[Pendente]** **Barra de progresso dos palpites (DB-based + HTMX):** ajustar a barra para refletir palpites preenchidos / total de jogos exibidos pelo filtro atual e atualizar após cada salvamento HTMX; incluir fragmento dedicado e/ou swap OOB, garantindo consistência com o recorte em tela.
   * **[Pendente]** **Atualização dos Nomes das Equipes de Repescagem (Pós-Playoffs Março/2026):** Os 6 lugares de repescagem estão confirmados após os playoffs de março/2026. O SQL atual ainda usa placeholders. Atualizar `data/sql/03-copa-2026-data.sql`, recarregar o banco e validar as telas.
      Análise realizada em 03/04/2026. IDs a atualizar:
      - ID 141: `Repescagem Europeia D` → `República Tcheca` (Grupo A)
      - ID 142: `Repescagem Europeia A` → `Bósnia e Herzegovina` (Grupo B)
      - ID 143: `Repescagem Europeia C` → `Turquia` (Grupo D)
      - ID 144: `Repescagem Europeia B` → `Suécia` (Grupo F)
      - ID 146: `Repescagem Intercontinental` → `Iraque` (Grupo I)
      - ID 147: `Repescagem Intercontinental 2` → `RD Congo` (Grupo K)
      Verificar também padronização dos nomes de cidades nas fases eliminatórias (ex: `Mexico City` → `Cidade do México`).
      Subtarefas:
      * **[Concluído]** Atualizar os 6 registros `EQP_PAIS` no `data/sql/03-copa-2026-data.sql` (03/04/2026)
      * **[Concluído]** Padronizar nomes de cidades/estádios nas fases eliminatórias (03/04/2026)
      * **[Concluído]** Recarregar banco e validar (`docker exec -i bolao-db mysql ... < 03-copa-2026-data.sql`)
      * **[Pendente]** Validar telas `/seguro/jogos.action` e área administrativa
      * **[Pendente]** Executar `mvn -Dfrontend.skip=true test`
      * **[Concluído]** Criar log de sessão em `.ia/logs/` (session-20260403-auditoria-tabela-fifa2026.md)

4. **[Concluído] Atualização do ORM:** Planejar e executar a migração do Hibernate 3.2.6.ga para Hibernate 6+.

5. **[Concluído] Substituição do WebWork:** Eliminar completamente as referências ao servlet e filtros do WebWork legado no `web.xml`.
   Referência Plano: `.ia/planos/plano-substituicao-webwork.md`

6. **[Concluído] Atualização de Bibliotecas de Terceiros:** Inventariar e atualizar todas as bibliotecas de terceiros (DWR, Quartz, EHCache, JSTL, Cewolf, JFreeChart, Batik, etc.) para suas versões mais recentes e com suporte. Remova bibliotecas sem suporte e atualize para outras bibliotecas mais atuais que atendam as necessidades do sistema.
   Referência ADR: `.ia/historico/ADR-20260217-bibliotecas-legadas.md`
   Referência Plano: `.ia/planos/plano-atualizacao-bibliotecas-terceiros.md`

7. **[Concluído] Introdução de Testes Automatizados:** Iniciar a criação de testes unitários e de integração para módulos críticos e novos desenvolvimentos, priorizando a cobertura de áreas de negócio sensíveis.

8. **[Concluído] Refatoração do Front-end (Primeira Fase):** Planejar a substituição gradual de Prototype/Script.aculo.us pela **Arquitetura Híbrida** (jQuery 4.0.0 + HTMX).
   Referência Plano: `.ia/planos/plano-modernizacao-frontend.md`
   Referência ADR: `.ia/historico/ADR-20260217-arquitetura-frontend-modernizacao.md`

9. **[Concluído] Modernização da Estrutura de Recursos Maven:** Migrar recursos de configuração de `webapp/WEB-INF/classes/` para a estrutura padrão Maven `src/main/resources/`. Atualizar `pom.xml` para copiar recursos automaticamente durante o build. Remover duplicação de arquivos de configuração entre `src/` e `webapp/WEB-INF/classes/`.
   **Justificativa:** O projeto utiliza `webapp/WEB-INF/classes/` como diretório de recursos, uma prática antiga que não segue os padrões modernos do Maven. A estrutura correta é manter os recursos em `src/main/resources/` e deixar o Maven gerenciar a cópia durante o build, eliminando duplicação e facilitando manutenção.
   **Atualização (09/06/2026):** Limpeza residual concluída para templates de e-mail HTML duplicados entre `src/com/.../templates` e `src/main/resources/.../templates`; removidas as cópias legadas e mantida `src/main/resources` como fonte canônica. Referência log: `.ia/logs/session-20260609-limpeza-templates-html-duplicados.md`. (Skills: `modernization-java-migration v1.0.0`, `architecture-guardian v1.0.0`)
   **Atualização (09/06/2026):** Template `cabecalho.html` modernizado para e-mail HTML responsivo (doctype atual, `meta charset`, `meta viewport`, container com `max-width`) preservando compatibilidade com `rodape.html` e placeholders existentes. Referência log: `.ia/logs/session-20260609-atualizacao-cabecalho-email-template.md`. (Skills: `modernization-java-migration v1.0.0`, `architecture-guardian v1.0.0`)
   **Atualização (09/06/2026):** Ajustes prioritários aplicados em templates e fragments: correção de encoding no template `recuperacao-senha-otp.html`, padronização de copy em templates de notificação (`notificacaoCadastroAprovado.html`, `auditoriaPalpiteAlterado.html`, `proximosJogos.html`) e remoção de diretivas `<%@taglib%>` em `jspf` de partials (`palpite-status.jspf`, `palpites-jogo-rows.jspf`) para alinhamento com diretriz HTMX/JSPF. Referência log: `.ia/logs/session-20260609-ajustes-prioritarios-templates-jsp.md`. (Skills: `modernization-java-migration v1.0.0`, `architecture-guardian v1.0.0`)

10. **[Concluído] 26/02/2026 Correção de autorização na tela de palpites:** Ajustadas as diretivas da página `webapp/WEB-INF/content/seguro/jogos.jsp` para usar `<sec:authorize>` com `ROLE_USER`/`ROLE_ADMIN`, restaurando o dataset `data-palpite-allowed` e mantendo a edição administrativa de resultados via Spring Security. Validação com `mvn test -Dfrontend.skip=true` e log `.ia/logs/session-20260226-correcao-palpites.md`. (Skill: `modernization-java-migration v1.0.0`)

11. **[Concluído] 05/04/2026 Correção da barra de progresso de palpites (sempre 0/0) — abordagem server-driven:** Substituída a lógica frágil de inspeção de DOM por valores autoritativos do servidor. `ParticipanteAction` expõe `totalJogos` e `totalPalpitesRealizados`; o JSP injeta esses valores como `data-*` no container da barra. Após salvar um palpite, o servidor emite `HX-Trigger: bolao:progressUpdate` com o `filled` real (`palpitesUsuario.size()`), garantindo que edições de palpites existentes não impactem o contador. `ux-helper.js` reescrito e limpo. Referências Log: `.ia/logs/session-20260404-correcao-barra-progresso.md`, `.ia/logs/session-20260405-barra-progresso-server-driven.md`. (Skills: `modern-javascript-patterns v1.0.0`, `modernization-java-migration v1.0.0`)

12. **[Concluído] 04/06/2026 Correção de `IllegalArgumentException` no envio de e-mail OTP/cadastro:** O método `Email.populateData()` usava `String.replaceAll(regex, replacement)` onde o segundo argumento (valor do placeholder) pode conter `$` ou `\`, que são interpretados como referências a grupos de captura pelo `Matcher`. Corrigido trocando para `String.replace()` (substituição literal). Adicionados 5 testes unitários em `EmailPopulateDataTest`. Referência Log: `.ia/logs/session-20260604-erro-email-otp.md`. (Skills: `modernization-java-migration v1.0.0`, `security-audit v1.0.0`)

13. **[Concluído] Carga inicial de dados no banco de produção (Aiven):** Dados da Copa 2026 já carregados no banco remoto Aiven — 104 jogos e 48 equipes presentes. O script `data/sql/03-copa-2026-data.sql` é idempotente e deve ser executado apenas uma vez em bancos sem dados. Referência Log: `.ia/logs/session-20260604-banco-producao-vazio.md`.

14. **[Pendente] Fluxo OTP de cadastro incompleto — tela de validação não exibida após envio do e-mail:**
    Após a correção do `Email.populateData()`, o e-mail com o código OTP passou a ser enviado com sucesso. Porém, o usuário permanece na tela de cadastro em vez de ser redirecionado para a tela de validação do código. A causa raiz é que as URLs do fluxo OTP não estão liberadas no Spring Security — o redirect para `/validacaoCadastro.action` é bloqueado e o usuário é redirecionado para `/login.action`.

    **Infraestrutura já implementada (não criar novamente):**
    - `ValidacaoCadastroAction` com métodos `exibir()`, `validar()` e `reenviar()`
    - `webapp/WEB-INF/content/validacaoCadastro.jsp` com formulário de entrada do código
    - `struts.xml` com mapeamentos `validacaoCadastro`, `validarCodigo` e `reenviarCodigo`
    - Bean `validacaoCadastroAction` registrado em `applicationContext-action.xml`

    **Subtarefas (executar em sequência):**

    * **[Concluído] 14.1 — Liberar URLs do fluxo OTP no Spring Security (09/06/2026):** Regras `permitAll` confirmadas em `applicationContext-security.xml` para o fluxo OTP de cadastro e recuperação.
      - `/validacaoCadastro.action*`
      - `/validarCodigo.action*`
      - `/reenviarCodigo.action*`
      - **[Concluído] 14.1.1 — Verificar e liberar URLs do fluxo OTP de recuperação de senha (iniciado no commit `7bbb307`):** `permitAll` confirmado para `/recuperarSenhaForm.action*`, `/enviarOtpRecuperacao.action*`, `/validarOtpRecuperacao.action*` e `/redefinirSenha.action*`, com aderência validada entre `applicationContext-security.xml`, `struts.xml` e JSPs.
      - **[Concluído] 14.1.2 — Verificação completa do fluxo de recuperação de senha (incluindo injeção do token):** validado o fluxo `/recuperarSenhaForm.action` → `/enviarOtpRecuperacao.action` → `/validarOtpRecuperacao.action` → `/redefinirSenha.action`; adicionada injeção de token CSRF em `redefinir-senha.jsp` e confirmada a presença em `recuperar-senha.jsp` (`${_csrf.parameterName}` / `${_csrf.token}`).
    Arquivo: `src/main/resources/applicationContext-security.xml`

    * **[Concluído] 14.2 — Validar o fluxo completo localmente (10/06/2026):** Executado `mvn -Dfrontend.skip=true test` com sucesso (50 testes, 0 falhas). Corrigida prioridade de propriedades em `EmailConfiguration.java` para permitir overrides via System Properties (necessário para testes em ambiente com variáveis globais).

    * **[Concluído] 14.3 — Build e redeploy local (Docker) (10/06/2026):** Pipeline completo executado (`mvn clean package`, `docker compose build`, `docker compose up`). Configurado `.env` local com credenciais de banco e validado startup saudável do Tomcat.

    * **[Pendente] 14.4 — Atualizar produção (HuggingFace Spaces):** Fazer commit, push e atualizar o ambiente de produção. Validar o fluxo completo em `novobolaodacopa-bolaocopa.hf.space`.

    * **[Pendente] 14.5 — Criar log de sessão e atualizar rastreabilidade:** Registrar em `.ia/logs/session-20260610-validacao-otp-config-fix.md`.

    * **[Concluído] 14.6 — Corrigir conflito de prefixo [opendev] em validacaoCadastro.jsp (10/06/2026):** Verificado que o arquivo já se encontra limpo de declarações redundantes, utilizando corretamente o prelude definido no `web.xml`.

    Referência: `src/main/resources/applicationContext-security.xml`, `struts.xml`, `ValidacaoCadastroAction.java`
    Skill: `modernization-java-migration v1.0.0`, `security-audit v1.0.0`

54. **[Concluído] Correção de Renderização de Placar 0x0:**
    Objetivo: Garantir que placares 0x0 sejam exibidos corretamente na página de resumos.
    * **[Concluído] 54.1 — Auditoria de Renderização:** Investigação concluída.
    * **[Concluído] 54.2 — Implementar Fix:** Fix aplicado via EL direta.
    * **[Concluído] 54.3 — Validação:** Ambiente reiniciado e fix validado.

55. **[Concluído] Otimização Arquitetural e Melhoria do Startup:**
    Objetivo: Resolver avisos de inicialização de beans, remover configurações obsoletas e reduzir o tempo de startup.
    * **[Concluído] 55.1 — Resolver BeanPostProcessorChecker:** Auditoria concluída; estrutura XML atual avaliada como estável; reversão de tentativas de lazy-init para preservar integridade.
    * **[Concluído] 55.2 — Limpeza de Configurações:** Dialeto explícito do Hibernate removido.
    * **[Concluído] 55.3 — Validação Arquitetural:** Sistema validado e estável após as limpezas.

56. **[Concluído] Modernização do Gráfico de Desempenho (Client-side Rendering):**
    Objetivo: Substituir JFreeChart (estático/lento) por biblioteca JS moderna (ApexCharts/Chart.js) para performance e interatividade.
    * **[Concluído] 56.1 — Limpeza de UI:** Remover botão `grafico_submit` e atualizar layout CSS para identidade Copa 2026.
    * **[Concluído] 56.2 — Implementar Componente JS:** Integrar biblioteca de gráficos no `graficoDesempenho.jsp` e consumir JSON.
    * **[Concluído] 56.3 — Cleanup Legado:** Remover `gerarGraficoDesempenhoImagem` e classes de suporte JFreeChart.
    * **[Concluído] 56.4 — Visual Temático:** Aplicar paleta de cores Copa 2026 e estilos no ApexCharts.
    * **[Concluído] 56.5 — Cleanup Legado:** Remover `gerarGraficoDesempenhoImagem` e classes de suporte JFreeChart.

57. **[Concluído] Hardening UX + Performance do Gráfico de Desempenho (HF Spaces + Aiven) (15/06/2026):**
    Objetivo: consolidar a modernização do gráfico com foco em robustez operacional, acessibilidade, menor latência percebida e aderência às diretrizes de frontend/CSP em ambiente restrito (Hugging Face Spaces + banco Aiven).
    Skills aplicadas: `ui-ux-pro-max v1.0.0`, `modern-javascript-patterns v1.0.0`, `modern-css v1.0.0`, `security-audit v1.0.0`, `architecture-guardian v1.0.0`.
    Referência de execução: `.ia/logs/session-20260616-tarefa57-hardening-grafico-desempenho.md`.

    **Subtarefas (iterações pequenas e sequenciais):**

    * **[Concluído] 57.1 — Diagnóstico objetivo e linha de base (UX + runtime):**
      Levantar tempo de carregamento do gráfico, payload JSON, custo de troca de rival e comportamento em rede lenta para estabelecer baseline antes de otimizações.
      Entregável: tabela comparativa "antes/depois" registrada no log da tarefa (métricas de bundle/fluxo e baseline técnico).

    * **[Concluído] 57.2 — Correção de ciclo de vida do gráfico no front-end:**
      Garantir instância única do ApexCharts por tela (destroy/update), evitando acúmulo de instâncias/listeners a cada mudança de rival.
      Entregável: módulo `src/frontend/pages/graficoDesempenho.js` com `destroyChartIfAny()` + `updateOptions()` para reuso de instância.

    * **[Concluído] 57.3 — Controle de concorrência de requests:**
      Adicionar `AbortController` (ou mecanismo equivalente) para cancelar requisições anteriores quando o usuário altera o rival rapidamente.
      Entregável: apenas a seleção mais recente atualiza a UI (`latestRequestToken` + cancelamento ativo).

    * **[Concluído] 57.4 — Cache client-side por rival (redução de round-trip no Aiven):**
      Implementar cache em memória por `rivalId` com TTL curto (ex.: 30–60s) para evitar refetch desnecessário ao alternar rival já consultado.
      Entregável: `Map` em memória com TTL de 45s por rival, reduzindo round-trip repetido no endpoint JSON.

    * **[Concluído] 57.5 — Acessibilidade dos estados dinâmicos do gráfico:**
      Evoluir feedback de "carregando/erro/sem dados" com `aria-live`, `role="status"` e mensagens claras para navegação por teclado/leitor de tela.
      Entregável: status acessíveis e textuais em `graficoDesempenho.jsp` + mensagens i18n em `messages.properties`.

    * **[Concluído] 57.6 — Estabilidade visual e responsividade:**
      Remover layout shift com altura mínima/reserva de espaço para o chart e revisar comportamento em mobile (largura reduzida e textos longos).
      Entregável: ajustes em `webapp/css/estilo.css` (`chart-wrapper--performance`, estados visuais e responsividade).

    * **[Concluído] 57.7 — Remoção de inline script/style e migração para assets versionados:**
      Extrair JavaScript inline do `graficoDesempenho.jsp` para módulo em `src/frontend/` e mover estilos inline para `webapp/css/estilo.css`.
      Entregável: JSP sem JS inline e sem estilos inline, inicialização centralizada via `src/frontend/main.js`.

    * **[Concluído] 57.8 — Remoção de CDN do ApexCharts e empacotamento local (Vite):**
      Empacotar a dependência no bundle local versionado, eliminando dependência de `cdn.jsdelivr.net` para reduzir risco externo e latência.
      Entregável: `apexcharts` instalado via npm e carregado por `import('apexcharts')`, gerando chunk local versionado.

    * **[Concluído] 57.9 — Hardening de segurança/observabilidade do filtro CSP:**
      Remover `System.out.println` de debug no `CspNonceFilter`, mantendo logging estruturado com nível apropriado.
      Entregável: `CspNonceFilter` com logging SLF4J em debug e cobertura em `CspNonceFilterTest`.

    * **[Concluído] 57.10 — Otimizações leves no endpoint JSON (sem quebra de contrato):**
      Revisar retorno e serialização para manter payload enxuto e estável; avaliar cache-control privado de curto prazo quando aplicável.
      Entregável: endpoint com fallback defensivo, cabeçalhos `Cache-Control`/`Vary` e teste de regressão no `ParticipanteActionTest`.

    * **[Concluído] 57.11 — Testes e validação de regressão:**
      Executar validação funcional (troca de rival, sem dados, erro de backend), regressão Maven e smoke em container local.
      Entregável: validações executadas com sucesso (`npm run build` e `mvn -Dfrontend.skip=true test`, 71 testes OK).

    * **[Concluído] 57.12 — Rastreabilidade final (log + atualização do plano):**
      Registrar sessão completa em `.ia/logs/` e atualizar status da tarefa/subtarefas no `passo-a-passo.md`.
      Entregável: evidências fechadas para auditoria técnica.

    **Cobertura das recomendações sugeridas (verificação):**
    * Recomendações de UX/performance contempladas por: `57.2`, `57.3`, `57.4`, `57.6`, `57.10`.
    * Recomendações visuais/acessibilidade contempladas por: `57.5`, `57.6`, `57.7`.
    * Recomendações de segurança/CSP contempladas por: `57.7`, `57.8`, `57.9`.
    * Recomendações de operação em HF Spaces + Aiven contempladas por: `57.1`, `57.4`, `57.8`, `57.11`.

---

15. **[Concluído] Otimização de Performance para Ambientes Restritos (Hugging Face Spaces):**
    
    **Diagnóstico:** Lentidão causada por processamento síncrono de 104 jogos em CPU limitada.
    **Estratégia:** Carga inicial mínima (Hoje/Próxima Data) + Carregamento progressivo via HTMX.

    * **[Concluído] 15.1 — Tuning de Infraestrutura (Dockerfile) (13/06/2026):**
        - `CATALINA_OPTS` atualizado para reduzir pausas de GC em carga (`G1GC`, `-Xms256m`, `-Xmx512m`, `MaxMetaspace=192m`, `MaxGCPauseMillis=200`), mantendo timezone canônico do domínio.
        - Removida propriedade inefetiva para Tomcat standalone (`server.tomcat.max-threads`) e aplicado tuning real de concorrência no `server.xml` (`maxThreads=60`, `minSpareThreads=10`, `acceptCount=100`, `keepAliveTimeout=15000`).
        - Build local do container validado com sucesso (`docker compose build app`).
        - Smoke pós-tuning executado no runtime local (`/health.txt` e `/login.action`) com respostas sub-milisegundo no container; registrar comparação externa em produção HF como próxima etapa de calibração fina.

    * **[Pendente] 15.2 — Lógica de Carga Inicial Mínima:**
        - Criar método no `JogoService` para encontrar a "Próxima Data com Jogos" a partir de hoje.
        - Ajustar `ParticipanteAction.prepararInfoPalpites` para aplicar este filtro por padrão se `usarFiltro == false`.
        - Adicionar testes unitários para a nova lógica de filtro.

    * **[Pendente] 15.3 — Implementação do Botão "Mais Jogos" (Backend):**
        - Criar action `palpitesMaisJogos.action` que recebe uma data de referência.
        - Implementar busca dos jogos da data imediatamente posterior à informada.
        - Criar fragmento JSP (`jogos-lista-fragmento.jsp`) para renderizar apenas os portlets das novas datas.

    * **[Pendente] 15.4 — Integração UI com HTMX (Frontend):**
        - Injetar o botão "Mais Jogos" ao final da lista no `jogos.jsp`.
        - Configurar `hx-get`, `hx-target` e `hx-swap="outerHTML"` para anexar novos jogos e substituir o botão.
        - Ajustar CSS para o estado de carregamento do botão.

    * **[Concluído] 15.5 — Validação de Regressão e Performance (10/06/2026):** 
        - Garantido que a Barra de Progresso global continue funcionando corretamente através da **globalização do cálculo** (ignora filtros de data para mostrar o progresso real no torneio, ex: 10/104).
        - Validada a compatibilidade dos novos fragmentos com os palpites inline.
        - Registrado log de sessão com os ganhos de arquitetura e UX.

54. **[Concluído] Correção de Renderização de Placar 0x0:**
    Objetivo: Garantir que placares 0x0 sejam exibidos corretamente na página de resumos.
    * **[Concluído] 54.1 — Auditoria de Renderização:** Investigação concluída.
    * **[Concluído] 54.2 — Implementar Fix:** Fix aplicado via EL direta.
    * **[Concluído] 54.3 — Validação:** Ambiente reiniciado e fix validado.

55. **[Concluído] Otimização Arquitetural e Melhoria do Startup:**
    Objetivo: Resolver avisos de inicialização de beans, remover configurações obsoletas e reduzir o tempo de startup.
    * **[Concluído] 55.1 — Resolver BeanPostProcessorChecker:** Auditoria concluída; estrutura XML atual avaliada como estável; reversão de tentativas de lazy-init para preservar integridade.
    * **[Concluído] 55.2 — Limpeza de Configurações:** Dialeto explícito do Hibernate removido.
    * **[Concluído] 55.3 — Validação Arquitetural:** Sistema validado e estável após as limpezas.

56. **[Concluído] Modernização do Gráfico de Desempenho (Client-side Rendering):**
    Objetivo: Substituir JFreeChart (estático/lento) por biblioteca JS moderna (ApexCharts/Chart.js) para performance e interatividade.
    * **[Concluído] 56.1 — Limpeza de UI:** Remover botão `grafico_submit` e atualizar layout CSS para identidade Copa 2026.
    * **[Concluído] 56.2 — Implementar Componente JS:** Integrar biblioteca de gráficos no `graficoDesempenho.jsp` e consumir JSON.
    * **[Concluído] 56.3 — Cleanup Legado:** Remover `gerarGraficoDesempenhoImagem` e classes de suporte JFreeChart.
    * **[Concluído] 56.4 — Visual Temático:** Aplicar paleta de cores Copa 2026 e estilos no ApexCharts.
    * **[Concluído] 56.5 — Cleanup Legado:** Remover `gerarGraficoDesempenhoImagem` e classes de suporte JFreeChart.

---

32. **[Concluído] Corrigir bug na funcionalidade de recuperação de senha (09/06/2026):**
    O envio do código de recuperação não está disparando a ação nem mudando a tela. O problema é a ausência de token CSRF no formulário.
    * **[Concluído]** 32.1 — Token CSRF confirmado no formulário da página `recuperar-senha.jsp` (ramificações `enviarOtpRecuperacao` e `validarOtpRecuperacao`) e também em `redefinir-senha.jsp`.
    * **[Concluído]** 32.2 — Submissão validada por rastreio técnico ponta a ponta: JSP (`form="formRecuperacao"`/`form="formRedefinir"` + `method="post"`) → `RecuperacaoSenhaAction` (`enviarOtpRecuperacao`, `validarOtpRecuperacao`, `redefinirSenha`) → `RecuperacaoSenhaServiceImpl` (`solicitarOtp` com envio de e-mail, `validarOtp`, `redefinirSenha`).
    * **[Concluído]** 32.3 — Log de sessão registrado em `.ia/logs/session-20260609-recuperacao-senha-pendencias-finalizacao.md`.
    * **[Concluído]** 32.4 — Correção i18n de mensagem exibida como `???recuperacao.otp.enviado.descricao???`: chave adicionada em `src/main/resources/messages.properties` para renderização correta na etapa de validação do OTP.
    * **[Concluído]** 32.5 — Varredura global de i18n nas telas JSP/JSPF e correção da chave faltante `chat.title` (usada em `batePapo.jsp`), adicionada em `src/main/resources/messages.properties`.
    **Atualização (09/06/2026):** identificada e corrigida causa adicional de não submissão: botões de `recuperar-senha.jsp`/`redefinir-senha.jsp` dependiam de `onclick` inline (`document.getElementById(...).submit()`), bloqueado pela política CSP atual (`script-src` sem `unsafe-inline` para event handlers). Ajustado para submit nativo com atributo `form=\"...\"`, eliminando dependência de JavaScript inline. Referência log: `.ia/logs/session-20260609-recuperacao-senha-submit-csp.md`. (Skills: `security-audit v1.0.0`, `modernization-java-migration v1.0.0`)

33. **[Concluído] Redução controlada de scripts inline e event handlers em JSP/JSPF (09/06/2026):**
    Mapeamento realizado em 09/06/2026 identificou blocos `<script>` inline e atributos `onclick`/eventos embutidos em páginas públicas e administrativas. Esta atividade deve migrar gradualmente esses trechos para módulos JS já existentes (`src/frontend/`) sem regressão funcional e mantendo conformidade com CSP.
    * **[Concluído]** 33.1 — Inventário atualizado: sem `onclick` remanescente em `webapp/WEB-INF/content`; pendências de evento inline remanescentes concentram-se em `cadastro.jsp` (`onfocus`/`onblur`) para tratamento futuro.
    * **[Concluído]** 33.2 — Migrar handlers inline de recuperação (`recuperar-senha.jsp`, `redefinir-senha.jsp`) para submit nativo sem `onclick` (log: `.ia/logs/session-20260609-recuperacao-senha-submit-csp.md`).
    * **[Concluído]** 33.3 — Migrar handlers inline administrativos remanescentes sem quebrar fluxo HTMX (`inclusaoJogo.jsp` e `admin-match-edit-panel.jsp`) com listeners delegados (log: `.ia/logs/session-20260609-csp-admin-handlers-inline.md`).
    * **[Concluído]** 33.4 — Validação pós-migração executada: varredura estática de handlers inline concluída com sucesso e testes Maven executados com sucesso (`mvn -q -Dmaven.repo.local=/tmp/.m2 -Dfrontend.skip=true -Dtest=RecuperacaoSenhaServiceImplTest test` e `mvn -q -Dmaven.repo.local=/tmp/.m2 -Dfrontend.skip=true test`). Evidências no log `.ia/logs/session-20260609-recuperacao-senha-pendencias-finalizacao.md`.

34. **[Pendente] Refatoração incremental de estilos inline em JSP/JSPF:**
    Mapeamento realizado em 09/06/2026 identificou estilos inline remanescentes (formulários de recuperação/validação e fragments de progresso). Esta atividade deve consolidar estilos em `webapp/css/estilo.css` com classes utilitárias e manter responsividade/acessibilidade.
    * **[Pendente]** 34.1 — Inventariar padrões repetidos de `style=\"...\"` e definir classes alvo.
    * **[Pendente]** 34.2 — Migrar telas de recuperação e validação de cadastro para classes CSS.
    * **[Pendente]** 34.3 — Migrar inline styles remanescentes em fragments HTMX (`palpite-progress-bar.jspf` e correlatos).
    * **[Pendente]** 34.4 — Validar regressões visuais (desktop/mobile) e registrar log de sessão.

35. **[Concluído] Estratégia UX para fundo com `brasao-fundo-email.png` em todos os e-mails (09/06/2026):**
    Como iniciativa de UX sênior, aplicar imagem de fundo comum aos e-mails com escurecimento leve para preservar legibilidade em clientes desktop/mobile, sem quebrar compatibilidade de renderização.
    **Regra de ouro (não regressão):** cada e-mail deve renderizar **apenas uma ocorrência** do fundo `brasao-fundo-email.png`, centralizada no template base (`cabecalho.html`). É proibido repetir o fundo nos templates de conteúdo para evitar duplicidade visual no mesmo envio.
    * **[Concluído]** 35.1 — Inventário e fonte da imagem: `webapp/img/brasao-fundo-email.png` localizado; validado tamanho 1660x2592 e peso 7,7 MB, definido como asset canônico inicial para fundo de e-mail.
    * **[Concluído]** 35.2 — Estratégia de entrega definida: uso de URL pública (`${emailBgUrl}`) em vez de CID/attachment, considerando pipeline atual (`BrevoEmailSender` com `htmlContent`) e menor acoplamento de backend.
    * **[Concluído]** 35.3 — Técnica de escurecimento aplicada: overlay semitransparente (`rgba(10, 18, 30, 0.42)`) sobre o fundo, com fallback por `background-color` sólido para clientes sem suporte a imagem.
    * **[Concluído]** 35.4 — Template base ajustado: `cabecalho.html` atualizado para fundo global + overlay e `rodape.html` compatibilizado com fechamento estrutural.
    * **[Concluído]** 35.5 — Revisão dos templates de conteúdo concluída: legibilidade preservada em bloco principal com fundo branco e placeholders funcionais mantidos sem alteração de contrato.
    * **[Concluído]** 35.6 — Ajuste backend aplicado: `Email.java` passou a expor `${emailBgUrl}` com normalização de `mail.property.systemurl`.
    * **[Concluído]** 35.7 — Checklist cross-client registrado no log da sessão (Gmail Web/Mobile, Outlook Web/Desktop, Apple Mail) com critérios de fallback visual.
    * **[Concluído]** 35.8 — Testes funcionais estáticos executados: validação de templates referenciados, disponibilidade pública de `/img/**`, e integridade de placeholders/markup sem regressão estrutural.
    * **[Concluído]** 35.9 — ADR registrada com decisão arquitetural URL pública vs CID e implicações operacionais.
    Referências técnicas iniciais: `src/main/resources/com/opendev/bolao/email/templates/cabecalho.html`, `src/main/resources/com/opendev/bolao/email/templates/rodape.html`, `src/com/opendev/bolao/email/Email.java`, `src/com/opendev/bolao/email/BrevoEmailSender.java`.
    Skills previstas: `architecture-guardian v1.0.0`, `modernization-java-migration v1.0.0`.

36. **[Concluído] Correção iterativa de handlers inline bloqueados por CSP em telas administrativas (09/06/2026):**
    Após a correção das telas de recuperação, foi identificado resíduo de `onclick` inline em telas admin, com potencial de bloquear ações de UI sob política CSP sem `unsafe-inline`.
    * **[Concluído]** 36.1 — Inventariar pontos críticos restantes e definir estratégia por arquivo (`inclusaoJogo.jsp` e `admin-match-edit-panel.jsp`), priorizando gatilhos nativos e/ou listeners delegados.
    * **[Concluído]** 36.2 — Ajustar `admin/inclusaoJogo.jsp`: removido `onclick` inline do botão de envio; formulário agora usa `submit` nativo com listener não-inline (`formCadastroJogo`) para acionar `submeterNovoJogo()`.
    * **[Concluído]** 36.3 — Ajustar `admin/partials/admin-match-edit-panel.jsp`: removidos `onclick` inline dos botões de fechar/cancelar; aplicado gatilho por `data-js="close-drawer"` e remoção de `hx-on::after-request` inline.
    * **[Concluído]** 36.4 — Ajustar suporte JS em `webapp/js/ux-helper.js`: adicionado listener delegado para `[data-js=\"close-drawer\"]` e fechamento automático do drawer no `htmx:afterRequest` de `.admin-edit-form-vertical`.
    * **[Concluído]** 36.5 — Validar impacto funcional e registrar evidências: varredura confirmou remoção dos atributos inline nos arquivos alvo; build Maven local não pôde ser concluído por autenticação 401 no Nexus corporativo (`https://nx-mvn.tse.jus.br`), sem evidência de regressão de markup/event binding nesta iteração.
    **Atualização (09/06/2026):** log da execução criado em `.ia/logs/session-20260609-csp-admin-handlers-inline.md`.
    Referências: `webapp/WEB-INF/content/admin/inclusaoJogo.jsp`, `webapp/WEB-INF/content/admin/partials/admin-match-edit-panel.jsp`, `webapp/js/ux-helper.js`, `src/com/opendev/bolao/security/CspNonceFilter.java`.
    Skills: `security-audit v1.0.0`, `modernization-java-migration v1.0.0`.

37. **[Concluído] Migração UX do fundo de e-mail para versão otimizada `.jpg` + layout natural sem caixa branca (09/06/2026):**
    Evolução do visual dos e-mails para melhorar legibilidade e desempenho: substituir o asset legado pesado por imagem otimizada, remover o bloco branco sólido e adotar painel escuro translúcido com tipografia clara.
    * **[Concluído]** 37.1 — Inventariar impacto da troca de asset (`.png` -> `.jpg`) e confirmar presença da nova imagem otimizada em `webapp/img/brasao-fundo-email.jpg`.
    * **[Concluído]** 37.2 — Ajustar backend de composição de e-mail (`Email.java`) para publicar `${emailBgUrl}` apontando para `/img/brasao-fundo-email.jpg`.
    * **[Concluído]** 37.3 — Atualizar `cabecalho.html` para layout “fundo natural”: manter imagem única no body, reforçar overlay escuro, substituir caixa branca por painel translúcido e padronizar texto claro.
    * **[Concluído]** 37.4 — Garantir contraste mínimo e fallback seguro (cor de fundo sólida quando imagem não carregar), preservando legibilidade em clientes de e-mail.
    * **[Concluído]** 37.5 — Registrar log da sessão com decisões de UX, justificativas e evidências técnicas da migração.
    * **[Concluído]** 37.6 — Ajuste fino de visibilidade do fundo (pós-validação visual): `background-size` alterado para `contain`, reposicionamento para `center bottom` e redução do overlay para `rgba(7,13,24,0.46)` em `cabecalho.html`, melhorando a percepção do brasão sem perder legibilidade.
    * **[Concluído]** 37.7 — Ajuste de previsibilidade cross-client: fundo configurado com escala fixa (`background-size: 960px auto`), posição `center top` e área mínima visual via `padding-bottom` no overlay, reduzindo cortes agressivos do brasão em e-mails curtos.
    * **[Concluído]** 37.8 — Alinhamento com arte enquadrada em canvas fixo: confirmada resolução real `900x1405` de `brasao-fundo-email.jpg` e template ajustado para `background-size: 900px auto` com `padding-bottom: 160px`, priorizando exibição mais completa do brasão em e-mails curtos.
    * **[Concluído]** 37.9 — Ajuste para arte horizontal redimensionada (`900x604`): `background-size` alterado para `100% auto`, overlay calibrado para `rgba(7,13,24,0.38)` e `padding-bottom` ajustado para `130px`, melhorando encaixe visual no Gmail sem perder legibilidade.
    * **[Concluído]** 37.10 — Cache-busting do fundo no envio de e-mail: `Email.java` passou a anexar `?v=<versao>` em `${emailBgUrl}` (prioriza `mail.property.emailbg.cachebuster`; fallback em `version.properties` com `build.timestamp`/`app.version`), mitigando reuso de imagem antiga por cache do Gmail/CDN.
    Referências: `webapp/img/brasao-fundo-email.jpg`, `src/com/opendev/bolao/email/Email.java`, `src/main/resources/com/opendev/bolao/email/templates/cabecalho.html`.
    Skills: `ui-ux-pro-max v1.0.0`, `modernization-java-migration v1.0.0`.

38. **[Concluído] Auditoria e Padronização de Fuso Horário Oficial (America/Sao_Paulo) no fluxo de negócio de palpites e agendamentos (11/06/2026):**
    Objetivo: garantir que todas as funcionalidades sensíveis a tempo (jobs/triggers, janela de palpites, registros de auditoria e persistência) operem estritamente no fuso oficial do Brasil (São Paulo), sem dependência implícita do timezone do host/container/banco.
    Domínio impactado: Bolão da Copa 2026 (regra crítica de prazo de palpite e notificações de jogos).
    Skills previstas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 38.1 — Inventário técnico de timezone por camada (11/06/2026):** mapeamento executado em `Action`, `Service`, `Model`, `Scheduler`, `infra` e SQL com classificação `Conforme/Parcial/Risco`, priorização de riscos e evidências técnicas consolidadas em `.ia/documentacao/fase-timezone-38-1-inventario.md`. Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 38.2 — Quartz/Scheduler com timezone explícito (11/06/2026):** `applicationContext-scheduler.xml` atualizado com `timeZone=America/Sao_Paulo` em todos os `CronTriggerFactoryBean` (`avisoJogo10/11/12/13/15/16Trigger`), removendo dependência implícita do timezone default da JVM para disparos de job. Evidência: `.ia/logs/session-20260611-timezone-tarefa38-2.md`. Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 38.3 — Janela de palpite com relógio canônico (11/06/2026):** `PalpiteAuthorizationServiceImpl` migrado de `Clock.systemDefaultZone()` para relógio canônico baseado em `BolaoTime` (`Clock.system(BolaoTime.getZoneId())`), com normalização de `Clock` injetável para a mesma zona oficial do domínio. Evidência: `.ia/logs/session-20260611-timezone-tarefa38-3.md`. Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 38.4 — Cálculo de datas sem aritmética em milissegundos (11/06/2026):** `ParticipanteAction.buscarMaisJogosHtmx` ajustado para converter `dataInicial` em `LocalDate` na zona canônica (`BolaoTime`) e avançar com `plusDays(1)`, removendo o incremento frágil `+86400000`. Evidência: `.ia/logs/session-20260611-timezone-tarefa38-4.md`. Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 38.5 — Serviços legados com tempo implícito (11/06/2026):** revisados e ajustados `JogoServiceImpl`, `ErrorNotificationService` e `BatePapo` para uso explícito do timezone do domínio (`BolaoTime`), removendo dependências implícitas de host em cálculo/registro de horários. Evidência: `.ia/logs/session-20260611-timezone-tarefa38-5.md`. Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 38.6 — Persistência JDBC/MySQL alinhada ao fuso oficial (11/06/2026):** JDBC atualizado com `connectionTimeZone=America/Sao_Paulo` e `forceConnectionTimeZoneToSession=true` em `applicationContext-resources.xml`; `docker-compose.yml` do MySQL ajustado com `TZ=America/Sao_Paulo` e `--default-time-zone=-03:00` para reduzir drift em colunas `TIMESTAMP`/`CURRENT_TIMESTAMP`. Evidência: `.ia/logs/session-20260611-timezone-tarefa38-6.md`. Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 38.7 — Contratos de exibição (JSP/i18n) e consistência de UI (11/06/2026):** validação estática de `regras.jsp`, `messages.properties` e fragmento HTMX de feedback (`palpite-cell-response.jspf`) confirmou aderência textual e renderização de horário ancorada no timezone oficial do runtime. Evidência: `.ia/logs/session-20260611-timezone-tarefa38-7-8-10.md`. Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 38.8 — Testes automatizados de regressão temporal (11/06/2026):** suíte ajustada em `PalpiteAuthorizationServiceImplTest` para zona canônica do domínio (`BolaoTime`) e reexecutada com sucesso (`mvn -Dfrontend.skip=true test`: 52 testes, 0 falhas). Evidência: `.ia/logs/session-20260611-timezone-tarefa38-7-8-10.md`. Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 38.9 — Evidências operacionais (11/06/2026):** validações executadas com sucesso: `mvn -Dfrontend.skip=true test` (52 testes, 0 falhas) e smoke runtime em container (`docker compose up -d`, health 200, login com CSRF e acesso autenticado a `/seguro/palpites.action` com HTTP 200). Evidência: `.ia/logs/session-20260611-timezone-tarefa38-9.md`. Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 38.10 — Documentação e ADR de decisão temporal (11/06/2026):** ADR registrada em `.ia/historico/ADR-20260611-timezone-canonico-sao-paulo.md` consolidando a decisão de tempo canônico do domínio e respectivos guardrails. Evidência: `.ia/logs/session-20260611-timezone-tarefa38-7-8-10.md`. Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 38.10.1 — Documentação inline das alterações (11/06/2026):** adicionados comentários explicativos/JavaDoc nos pontos alterados de código e configuração para explicitar a decisão temporal, incluindo o cenário de produção no Hugging Face (host com possível timezone diferente) e a premissa de dados de jogos no referencial São Paulo. Evidência: `.ia/logs/session-20260611-timezone-documentacao-inline.md`.

39. **[Concluído] Estabilização do endpoint de login para eliminar 404 em `j_security_check` (11/06/2026):**
    Objetivo: remover intermitência de erro HTTP 404 no fluxo de autenticação, tornando o endpoint de login consistente com Spring Security 6 e resiliente a acessos legados.
    Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 39.1 — Análise de causa:** reproduzido `GET /j_security_check` com 404 em runtime (Tomcat), confirmando fragilidade do endpoint legado quando acessado fora do POST esperado.
    * **[Concluído] 39.2 — Ajuste do processamento de autenticação:** `login-processing-url` migrado de `/j_security_check` para `/login.action`, mantendo o mesmo fluxo visual e reduzindo risco de rota órfã.
    * **[Concluído] 39.3 — Endurecimento do formulário de login:** `login.jsp` atualizado para action absoluta/context-aware (`/login.action`) com campo CSRF explícito no markup.
    * **[Concluído] 39.4 — Correção estrutural do Struts:** ajustada a ordem dos elementos (`default-action-ref` antes de `global-exception-mappings`) no pacote `bolao-default` para conformidade com DTD e inicialização estável do filtro Struts.
    * **[Concluído] 39.5 — Compatibilidade legada sem rota dedicada:** validado que `GET /j_security_check` retorna `302` para `/login.action` pelo fluxo padrão de segurança, eliminando 404 sem necessidade de `action` adicional no Struts.
    * **[Concluído] 39.6 — Validação técnica:** smoke runtime e validações HTTP (login GET 200, login POST 302 para `/seguro/principal.action`, acesso autenticado a `/seguro/palpites.action` com 200) documentados em `.ia/logs/session-20260611-login-jsecuritycheck-404.md`.

40. **[Concluído] Correção de inicialização no Hugging Face por bean ausente no Spring (11/06/2026):**
    Objetivo: eliminar falha de startup causada por `UnsatisfiedDependencyException` no interceptor de exceções quando executado no ambiente de produção do Hugging Face.
    Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 40.1 — Análise de causa raiz:** log de produção mostrou ausência de bean `ErrorNotificationService` no contexto Spring durante construção do `ExceptionLoggingInterceptor`.
    * **[Concluído] 40.2 — Correção de configuração:** registrado bean explícito `errorNotificationService` em `applicationContext-service.xml`, garantindo injeção em runtime para ambientes baseados em configuração XML.
    * **[Concluído] 40.3 — Documentação inline:** incluído comentário técnico no XML explicando o motivo do registro explícito para o cenário HF/Struts `SpringObjectFactory`.
    * **[Concluído] 40.4 — Validação técnica:** rebuild em container e verificação de logs sem `NoSuchBeanDefinitionException`; `health.txt` retornando HTTP 200 e suíte `mvn -Dfrontend.skip=true test` com 52 testes e 0 falhas. Evidência: `.ia/logs/session-20260611-hf-startup-missing-errornotification-bean.md`.

41. **[Em Progresso] Correção da atualização de resultados na tela administrativa de jogos (11/06/2026):**
    Objetivo: restabelecer o fluxo de atualização de resultado pelo admin, garantindo exibição dos campos de placar e persistência correta no banco.
    Skills previstas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 41.1 — Diagnóstico funcional ponta a ponta (11/06/2026):** reproduzido cenário em `/admin/jogos.action`; evidências mostram renderização do fragmento de palpites (`match-row.jspf`) no contexto admin, sem inputs `golsEquipe1/golsEquipe2`.
    * **[Concluído] 41.2 — Auditoria de renderização do fragmento admin (11/06/2026):** validado que `admin-match-row.jsp` já contém os campos de resultado e `hx-post` correto para `/admin/atualizarResultadoJogo.action`; bloqueio estava na seleção do include na lista compartilhada.
    * **[Concluído] 41.3 — Auditoria do fluxo HTMX/Struts (11/06/2026):** confirmado mapeamento em `struts.xml` e método `AdminAction.atualizarResultadoDoJogoHtmx`; endpoint e binding de parâmetros estão corretos quando a linha admin é renderizada.
    * **[Concluído] 41.4 — Correção incremental backend/frontend (11/06/2026):** implementada flag explícita de contexto admin (`adminResultadoView`) em `AdminAction.carregarJogos` e include condicional em `jogos-lista-fragmento.jsp` para usar `admin-match-row.jsp` no admin e `match-row.jspf` no seguro.
    * **[Concluído] 41.4.1 — Correção de curto prazo para efeitos colaterais de JSP compartilhada (11/06/2026):** ajustado `jogos-lista-fragmento.jsp` para (a) exibir coluna de cabeçalho de ações quando `adminResultadoView=true` e (b) renderizar o botão `Carregar Próxima Data` apenas em `telaPalpites=true`, evitando que o admin acione endpoint `/seguro/palpitesMaisJogosPartial.action` e perca a linha administrativa. Evidência: `.ia/logs/session-20260611-admin-jsp-compartilhada-curto-medio-prazo.md`.
    * **[Pendente] 41.5 — Validação de persistência em banco:** executar cenário real com admin, salvar resultado e confirmar atualização nas colunas de resultado do jogo e reflexos em consultas subsequentes. (Tentativa automatizada via `curl` no container retornou HTTP 403 no login por proteção de segurança; manter validação final via navegador autenticado ou teste E2E dedicado.)
    * **[Concluído] 41.6 — Testes e rastreabilidade (11/06/2026):** adicionado teste unitário em `AdminActionTest` para garantir marcação de contexto admin (`adminResultadoView`) no carregamento da tela; suíte executada com sucesso (`mvn -Dfrontend.skip=true test`: 53 testes, 0 falhas) e log registrado em `.ia/logs/session-20260611-admin-jogos-resultado-renderizacao.md`.

42. **[Concluído] Aplicar filtro de jogos pendentes na tela administrativa de resultados (11/06/2026):**
    Objetivo: evitar carga de todos os jogos na tela admin e priorizar um recorte operacional útil ao operador para correções de resultados.
    Regra alvo (revisada em 11/06/2026): por padrão, listar todos os jogos do início da Copa até a data atual (fuso oficial São Paulo), mantendo opção explícita para exibir o calendário completo.
    Skills previstas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 42.1 — Definição formal da regra de filtro (11/06/2026):** filtro padrão revisado para `início da Copa -> hoje` (não apenas pendências), preservando uso do fuso oficial São Paulo.
    * **[Concluído] 42.2 — Consulta de domínio para recorte até hoje (11/06/2026):** aplicado `FiltroBuscaJogos` com `dataFinal=hoje` no backend admin, sem depender de consulta dedicada por pendências.
    * **[Concluído] 42.3 — Ajuste de carregamento inicial da tela admin (11/06/2026):** `AdminAction.carregarJogos` agora aplica automaticamente o recorte até hoje e mantém opção explícita de listagem completa (`mostrarTodos=true`).
    * **[Concluído] 42.4 — UX de filtro alinhada à operação (11/06/2026):** banner da `jogos.jsp` revisado para comunicar o modo padrão “até hoje” e link rápido para “ver todos”.
    * **[Concluído] 42.5 — Fallback e observabilidade (11/06/2026):** atributos de request simplificados para estado da tela (`adminFiltroAteHojeAtivo`, `adminFiltroDataLimite`, `adminMostrandoTodos`), reduzindo ambiguidade operacional.
    * **[Concluído] 42.6 — Testes e validação final (11/06/2026):** `AdminActionTest` atualizado para cobrir fluxo padrão “até hoje” e modo `mostrarTodos`; suíte executada com sucesso (`mvn -Dfrontend.skip=true test`: 55 testes, 0 falhas). Evidência: `.ia/logs/session-20260611-admin-filtro-pendencias-tarefa42.md` e `.ia/logs/session-20260611-admin-filtro-ate-hoje-revisao.md`.

43. **[Pendente] Desacoplar a tela administrativa de jogos da JSP compartilhada (médio prazo) (11/06/2026):**
    Objetivo: reduzir regressões cruzadas entre os fluxos `/seguro` (palpites) e `/admin` (atualização de resultados), removendo dependência de contexto implícito em view compartilhada.
    Skills previstas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Pendente] 43.1 — Definir contrato da nova view admin:** mapear dados, colunas e ações obrigatórias da tela de admin e documentar diferenças em relação ao fluxo de palpites.
    * **[Pendente] 43.2 — Criar JSP raiz dedicada para admin:** introduzir `WEB-INF/content/admin/jogos-admin.jsp` com includes próprios e sem dependências de variáveis de palpites.
    * **[Pendente] 43.3 — Criar fragmento de lista dedicado ao admin:** extrair `admin-jogos-lista-fragmento.jsp` com tabela e paginação/filtros específicos do operador administrativo.
    * **[Pendente] 43.4 — Ajustar mapeamento Struts/admin action:** apontar `/admin/jogos.action` para a nova JSP dedicada e remover condicionais de contexto admin no fragmento compartilhado.
    * **[Pendente] 43.5 — Preservar componentes reaproveitáveis neutros:** manter apenas componentes realmente compartilháveis (ex.: renderização de seleção/bandeira) sem acoplamento de regra de negócio.
    * **[Pendente] 43.6 — Validar regressão cruzada:** executar smoke completo em `/seguro/palpites.action` e `/admin/jogos.action` + suíte `mvn -Dfrontend.skip=true test`.
    * **[Pendente] 43.7 — Rastreabilidade arquitetural:** criar ADR de desacoplamento da view admin (trade-offs e impacto em manutenção) e log de sessão com evidências.

44. **[Concluído] Reduzir ruído de logs no Hugging Face (NamedQuery DEBUG) sem mascarar falhas reais (11/06/2026):**
    Objetivo: eliminar stack traces diagnósticos repetitivos no startup/runtime (`exception just for purpose of providing stack trace`) e melhorar a triagem operacional no ambiente Hugging Face.
    Skills aplicadas: `senior-java-dev-legacy v1.0.0`, `docker-expert v1.0.0`.
    * **[Concluído] 44.1 — Diagnóstico da origem do ruído:** confirmado que a aplicação não possuía `logback.xml` explícito em `src/main/resources`, permitindo inicialização com perfil de logging padrão verboso.
    * **[Concluído] 44.2 — Configuração canônica de logging:** criado `src/main/resources/logback.xml` com `root=INFO` e ajustes direcionados para categorias de query (`org.springframework.data.jpa.repository.query.NamedQuery`, `org.hibernate.SQL`, `org.hibernate.orm.query`) para reduzir ruído não-fatal.
    * **[Concluído] 44.3 — Documentação inline das decisões:** adicionados comentários técnicos no próprio `logback.xml` explicando o cenário do Hugging Face, o trade-off de observabilidade e a razão de negócio da mudança.
    * **[Concluído] 44.4 — Rastreabilidade da execução:** log da sessão registrado em `.ia/logs/session-20260611-hf-logback-ruido-namedquery.md`.

45. **[Concluído] Evolução UX da Liderança na página principal como resumo operacional (11/06/2026):**
    Objetivo: manter o painel da home compacto, amigável e imediatamente útil, sem competir com a tela de Classificação Geral.
    Diretriz aprovada: exibir Top 3 com medalhas (ouro/prata/bronze) e nomes dos líderes, respeitando critérios oficiais de desempate já definidos em `regras.jsp`.
    Regra mandatória da tarefa: qualquer ordenação exibida na home (bloco textual, medalhas e/ou gráfico de liderança) deve seguir exatamente a mesma regra textual oficial de classificação/desempate, sem lógica alternativa local.
    Diretriz UX para empates amplos (início do bolão): a home deve priorizar síntese (Top 3 + contexto agregado de empate), enquanto o detalhamento de todos os empatados/posições fica centralizado na tela de Classificação Geral.
    Skills previstas: `ui-ux-pro-max v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    * **[Concluído] 45.1 — Definir contrato de dados do resumo Top 3 (11/06/2026):** contrato estabilizado com ordenação/desempate oficial unificada entre home e classificação geral, sem regra alternativa local.
    * **[Concluído] 45.1.1 — Verificação de consistência Home x Classificação Geral (11/06/2026):** validação estática confirmou que `principal.jsp` (via `graficoLiderancaImagem.action`) e `classificacao.jsp` consomem a mesma origem de ordenação (`ParticipanteService.buscarClassificacao()` + `Collections.sort(...)`), reduzindo risco de divergência entre telas por implementação separada.
    * **[Concluído] 45.1.2 — Validação contra Regras de desempate (11/06/2026):** identificado gap de aderência: `Participante.compareTo(...)` considera hoje apenas `pontuação total` e, em empate, `ordem alfabética`, enquanto `regras.jsp` descreve critérios intermediários (`acertos totais` e `acertos parciais com bônus`) antes do critério alfabético. Ajuste funcional pendente para alinhar implementação e regra oficial.
    * **[Concluído] 45.1.3 — Correção de aderência de desempate (11/06/2026):** `Participante.compareTo(...)` atualizado para aplicar a ordem textual oficial de desempate (`pontuação total` → `acertos totais (6 pts)` → `acertos parciais com bônus (3 pts)` → `ordem alfabética`). Cobertura adicionada em `ParticipanteTest` para cenários de empate.
    * **[Concluído] 45.1.4 — Ajuste das regras textuais de ordenação/desempate (11/06/2026):** validado que, após a correção da 45.1.3, o comportamento do sistema passou a aderir ao texto vigente de `regras.jsp`/`messages.properties`, sem necessidade de alteração textual adicional nesta iteração.
    * **[Concluído] 45.2 — Ajustar rendering da home (`principal.jsp`) (11/06/2026):** bloco textual Top 3 com medalhas ativo, gráfico mantido como apoio secundário e sem reordenação no frontend.
    * **[Concluído] 45.2.1 — Iteração inicial do resumo Top 3 na home (11/06/2026):** `ParticipanteAction.obterDadosPaginaPrincipal()` passou a carregar `lideresResumo` (top 3 já ordenado pela regra oficial) e `principal.jsp` ganhou bloco textual com posição/nome/pontos e marcador visual de medalha (ouro/prata/bronze), preservando o gráfico de liderança como apoio secundário.
    * **[Concluído] 45.2.2 — Cobertura inicial de regressão (11/06/2026):** adicionado teste em `ParticipanteActionLoadTest` validando limite top 3 e ordenação oficial no resumo da home.
    * **[Concluído] 45.3 — Estados de empate e ausência de dados (11/06/2026):** cenários de empate no topo, empate massivo e ausência de pontuação tratados com síntese na home e direcionamento ao detalhamento na Classificação Geral.
    * **[Concluído] 45.3.1 — Verificação de cenário inicial (todos com 0 pontos) (11/06/2026):** adicionado teste automatizado em `ParticipanteActionLoadTest` cobrindo cenário com todos os participantes zerados; validação confirma `success`, resumo estável com top 3 e fallback alfabético sem erro em runtime.
    * **[Concluído] 45.3.2 — Sinalização de desempate aplicado no topo (11/06/2026):** home passou a exibir aviso discreto quando os dois primeiros empatam por pontuação e o ranking depende dos critérios oficiais de desempate; cobertura adicionada em teste de action.
    * **[Concluído] 45.3.3 — Empate massivo por pontuação no início do bolão (11/06/2026):** home passou a calcular e expor `+N` participantes adicionais com a mesma pontuação do topo quando o empate excede o Top 3 exibido, reduzindo leitura enganosa no início do bolão.
    * **[Concluído] 45.3.4 — Critério de medalhas em cenários de empate (11/06/2026):** regra formalizada na home com texto explicativo explícito de que medalhas seguem a posição oficial do ranking (com desempate), e não apenas a pontuação isolada.
    * **[Concluído] 45.3.6 — Otimização de espaço da home em empate amplo (11/06/2026):** seção mantida compacta com Top 3 + indicadores agregados (`+N`) e sem expansão da lista de empatados na home; detalhamento ficou direcionado para a Classificação Geral.
    * **[Concluído] 45.3.7 — CTA explícito para detalhes na Classificação Geral (11/06/2026):** adicionada chamada curta na home (`Ver classificação completa`) quando houver empate massivo, orientando o usuário para o detalhamento oficial em `/seguro/ranking.action`.
    * **[Concluído] 45.3.5 — Testes de regressão para empates amplos (11/06/2026):** cobertura automatizada concluída para os cenários `todos empatados`, `empate do 1º ao 5º` e `empate parcial no pódio`, validando estabilidade do resumo e indicadores de contexto.
    * **[Concluído] 45.3.5.1 — Cobertura inicial de empate massivo (11/06/2026):** `ParticipanteActionLoadTest` passou a validar cenário com empate do 1º ao 5º e cálculo correto de `liderancaEmpatadosMesmoPontosRestantes`.
    * **[Concluído] 45.3.5.2 — Cobertura complementar de empate no pódio (11/06/2026):** adicionados testes para `todos empatados` dentro do Top 3 e `empate parcial no pódio`, assegurando ordenação oficial, sinalização de desempate e ausência de contagem `+N` indevida.
    * **[Concluído] 45.4 — Acessibilidade e legibilidade (11/06/2026):** ajustes de semântica, foco visível e reforço textual de medalhas concluídos, evitando dependência exclusiva de cor/ícone.
    * **[Concluído] 45.4.1 — Semântica e rotulagem acessível do Top 3 (11/06/2026):** resumo da liderança passou a usar `aria-labelledby`, `aria-label` por item com posição/nome/pontos/medalha e texto de pontos via i18n (sem abreviação ambígua); `alt` do gráfico atualizado para descrição funcional.
    * **[Concluído] 45.4.2 — Não depender somente de cor/ícone para medalhas (11/06/2026):** resumo passou a exibir rótulo textual visível (`Ouro`, `Prata`, `Bronze`) por item, mantendo bolha visual de medalha como apoio.
    * **[Concluído] 45.5 — Validação funcional e rastreabilidade (11/06/2026):** suíte `mvn -Dfrontend.skip=true test` executada com sucesso (64 testes) e smoke autenticado da home validando presença de `Top 3 da liderança`, regra textual de medalhas e rótulos visíveis de medalha após rebuild Docker.
    * **[Concluído] 45.6 — Inserir ícones gráficos de medalhas no Top 3 da home (11/06/2026):** substituída a bolha visual simples por ícone SVG de medalha (ouro/prata/bronze) discreto ao lado da posição (`1º`, `2º`, `3º`), mantendo semântica acessível e sem ocupar espaço excessivo no card.

46. **[Concluído] Reavaliar melhorias de médio impacto/ideais na tela de Classificação Geral (16/06/2026):**
    Objetivo: concentrar na tela de Classificação Geral os recursos analíticos e de exploração (filtros, evolução, insights), mantendo a home enxuta.
    Skills previstas: `ui-ux-pro-max v1.0.0`, `architecture-guardian v1.0.0`.
    Skills aplicadas nesta execução: `ui-ux-pro-max v1.0.0`, `architecture-guardian v1.0.0`.
    * **[Concluído] 46.1 — Revisão de escopo UX da Classificação Geral (16/06/2026):** mapeamento concluído das melhorias que devem migrar da proposta original da home para a Classificação Geral, mantendo a home enxuta e orientada a resumo:
      - **Variação de posição:** sair do resumo textual da home e ficar centralizada na Classificação Geral com delta por participante (`subiu`, `caiu`, `manteve`) em relação ao snapshot anterior.
      - **Filtros de período/fase:** priorizar filtros de recorte temporal e por fase (grupos, 32-avos, 16-avos etc.) diretamente na tela de ranking para leitura analítica sem sobrecarregar a home.
      - **Microinsights:** destacar cartões curtos de contexto (diferença para liderança, maior sequência positiva, maior eficiência recente), deixando o detalhamento completo na tabela.
      - **Princípio aplicado:** home permanece em modo síntese (Top 3 + contexto), enquanto a Classificação Geral vira o ponto único de exploração e diagnóstico.
    * **[Concluído] 46.1.1 — Estratégia de detalhamento de empatados (16/06/2026):** desenho funcional definido para blocos de empate com ordem oficial preservada e legível:
      - **Agrupamento por faixa de pontuação:** participantes com mesma pontuação passam a ser apresentados em bloco visual contínuo (ex.: `Empatados com 48 pontos`), sem duplicar o critério de rank.
      - **Posição oficial explícita:** cada linha mantém a posição oficial final (com desempate já aplicado), evitando interpretação de "mesma colocação absoluta".
      - **Indicador textual de desempate:** inserir marcador discreto (`Desempate aplicado`) quando houver empate por pontos na faixa, com referência à regra oficial publicada em `regras.jsp`.
      - **Expansão progressiva opcional:** para empates massivos, exibir resumo do bloco por padrão com expansão de detalhes sob demanda, protegendo legibilidade em mobile.
    * **[Concluído] 46.2 — Definir backlog incremental da classificação (16/06/2026):** backlog técnico/UX de baixa granularidade definido e priorizado por valor ao usuário:
      - **P0 (alto valor imediato):**
        - backend: expor `variacaoPosicao` por participante comparando ranking atual vs snapshot anterior.
        - JSP: renderizar coluna compacta de variação (`▲`, `▼`, `•`) com texto acessível.
        - testes: cobertura unitária da regra de variação (subiu/caiu/manteve/novo participante).
      - **P1 (clareza analítica):**
        - backend: suportar filtro por fase e por janela temporal (últimos jogos/rodadas).
        - JSP: barra de filtros com estado persistente na URL.
        - CSS: responsividade dos filtros em 2 linhas no mobile sem quebra de leitura da tabela.
        - testes: validação de contrato dos filtros e regressão de ordenação oficial.
      - **P1 (empates e regras):**
        - backend: sinalizador de empate por pontuação com desempate aplicado.
        - JSP: agrupamento visual de blocos empatados + marcador textual de regra.
        - testes: cenários de empate duplo, empate massivo e empate parcial no topo.
      - **P2 (microinsights):**
        - backend: métricas leves (`gapParaLider`, `eficienciaRecente`, `sequenciaPositiva`).
        - JSP: cards de insight acima da tabela, sem substituir dados oficiais.
        - testes: consistência dos cards com dados da classificação filtrada.
    * **[Concluído] 46.3 — Proposta de layout e navegação (16/06/2026):** hierarquia visual proposta para desktop/mobile com foco em leitura rápida e rastreabilidade da regra oficial:
      - **Bloco 1 (Topo - Resumo):** cartões curtos com posição do usuário logado, diferença para líder e aviso de empate ativo.
      - **Bloco 2 (Exploração):** barra de filtros (fase/período) com feedback de estado aplicado e botão de limpeza.
      - **Bloco 3 (Tabela oficial):** ranking completo como fonte primária da verdade, incluindo colunas de desempate e coluna de variação de posição.
      - **Bloco 4 (Contexto de regra):** painel compacto "Como o desempate foi aplicado neste recorte", com link para `regras.jsp`.
      - **Desktop:** filtros e microinsights em linha superior; tabela ocupa largura principal; legenda de siglas próxima ao rodapé da tabela.
      - **Mobile:** ordem vertical (resumo -> filtros -> tabela com scroll horizontal controlado -> regra), mantendo colunas críticas fixas visualmente no início.
    * **[Concluído] 46.4 — Critérios de aceite e métricas UX (16/06/2026):** critérios objetivos definidos para validar implementação sem ambiguidade:
      - **Critério A - Compreensão de posição:** usuário identifica sua posição atual e variação em até 10 segundos no primeiro acesso (teste moderado).
      - **Critério B - Clareza de desempate:** em cenários de empate, usuário reconhece que a ordem segue regra oficial (não apenas pontuação), com taxa de acerto >= 90% no roteiro de validação.
      - **Critério C - Consistência funcional:** ranking da tela permanece idêntico ao `ParticipanteService.buscarClassificacao()` no mesmo recorte (sem reordenação local em JSP/JS).
      - **Critério D - Responsividade:** sem quebra horizontal crítica em viewport de 360px, mantendo leitura de posição, nome e pontos.
      - **Critério E - Acessibilidade mínima:** estados de filtro/empate anunciados por texto (não apenas cor/ícone) e foco navegável por teclado na barra de filtros.

58. **[Concluído] Implementar P0 da Classificação Geral: variação de posição + coluna na JSP + testes (16/06/2026):**
    Objetivo: entregar a primeira evolução funcional da Classificação Geral com baixo risco, exibindo variação de posição por participante sem alterar a regra oficial de ordenação/desempate.
    Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`, `ui-ux-pro-max v1.0.0`.
    * **[Concluído] 58.1 — Backend: calcular e expor `variacaoPosicao` (16/06/2026):** `ParticipanteServiceImpl.buscarClassificacao()` passou a calcular variação por participante comparando posição atual (`index + 1`) com snapshot de posições anterior em memória (`cachePosicoesRankingAnterior`), mantendo `null` para primeiro cálculo/sem histórico.
    * **[Concluído] 58.2 — JSP: adicionar coluna de variação com acessibilidade (16/06/2026):** `classificacao.jsp` recebeu coluna `VAR` com estados `▲ +N`, `▼ -N`, `• 0` e `—`, sempre acompanhados de descrição textual para leitor de tela (`sr-only`) e legenda explícita.
    * **[Concluído] 58.3 — Testes de regressão da variação (16/06/2026):** novo teste em `ParticipanteServiceTest` valida subida/queda entre snapshots após expiração de cache de classificação, incluindo cenário inicial sem histórico.
    * **[Concluído] 58.4 — Validação e rastreabilidade (16/06/2026):** validação concluída com `mvn -Dfrontend.skip=true test` (69 testes, 0 falhas), atualização do plano e log de sessão criado em `.ia/logs/`.

59. **[Concluído] Estratégia UX + Performance para Palpites e Resultados (16/06/2026):**
    Objetivo: elevar confiabilidade percebida, reduzir perda de edição e melhorar produtividade operacional nas telas `/seguro/palpites.action` e `/admin/jogos.action`, com foco em feedback de gravação, proteção de saída e performance em ambiente restrito.
    Domínio impactado: registro de palpites dos participantes e atualização administrativa de resultados da Copa 2026.
    Plano de referência: `.ia/planos/plano-ux-performance-palpites-resultados-20260616.md`.
    Skills previstas: `ui-ux-pro-max v1.0.0`, `modern-javascript-patterns v1.0.0`, `architecture-guardian v1.0.0`, `security-audit v1.0.0`.

    * **[Concluído] 59.1 — Diagnóstico técnico-UX consolidado (baseline):**
      Mapear estados atuais dos fluxos (participante/admin), incluindo pontos de fricção, inconsistências de feedback e custo de interação por operação.
      Entregável: checklist objetivo de gaps + baseline registrado em `.ia/logs/session-20260616-tarefa59-iteracao1-baseline.md`.

    * **[Concluído] 59.2 — Padronizar feedback visual de gravação (linha/célula + sessão):**
      Implementar contrato único de estados (`dirty`, `saving`, `saved`, `error`, `locked`) com mensagens i18n e timestamp de confirmação.
      Entregável: feedback padronizado implementado em `jogos.jsp`, `palpite-cell-response.jspf`, `admin-match-row.jsp` e `jogos.js`, com status global `aria-live`.

    * **[Concluído] 59.3 — Guard de saída com alterações não salvas:**
      Adicionar proteção de navegação/refresh quando existir alteração pendente sem persistência confirmada.
      Entregável: `beforeunload` implementado com critério real de pendência (`dirty` e request admin em andamento), sem falso positivo em estado limpo.

    * **[Concluído] 59.4 — Recuperação de erro orientada a ação (retry):**
      Melhorar mensagens de erro e disponibilizar ação explícita de reenvio para falhas transitórias.
      Entregável: botões de retry por célula (participante) e por linha (admin), mantendo valores de edição.

    * **[Concluído] 59.5 — Otimização de requisições em edição rápida:**
      Reduzir requisições redundantes (deduplicação de payload idêntico, debounce calibrado e bloqueio de concorrência por item).
      Entregável: deduplicação de autosave por assinatura de payload + debounce/bloqueio de concorrência preservados.

    * **[Concluído] 59.6 — Produtividade da operação admin (resultados):**
      Evoluir experiência de atualização de placar com confirmação por linha salva e navegação eficiente por teclado.
      Entregável: status por linha implementado e navegação por Enter entre campos editáveis adicionada no fluxo admin.

    * **[Concluído] 59.7 — Consistência visual e acessibilidade WCAG 2.1 AA:**
      Revisar contraste, foco visível, semântica, rótulos e dependência de cor/ícone para estados de gravação.
      Entregável: estados textuais e regiões `aria-live` consolidados, reduzindo dependência exclusiva de cor/ícone.

    * **[Concluído] 59.8 — Testes de regressão e smoke funcional:**
      Cobrir cenários críticos (salvar múltiplas vezes, sair com pendência, erro+retry, edição admin sequencial, janela de prazo encerrada).
      Entregável: evidências registradas com `npm run build` e `mvn -Dfrontend.skip=true test` (71 testes, 0 falhas), além de checklist funcional em log.

    * **[Concluído] 59.9 — Rastreabilidade + ADR da decisão UX (se aplicável):**
      Registrar decisões, trade-offs e resultados em log de sessão e propor ADR para padrão oficial de estados de gravação.
      Entregável: logs de execução (`session-20260616-tarefa59-*`) e ADR rascunho criado em `.ia/historico/ADR-20260616-estados-gravacao-palpites-resultados.md`.

60. **[Concluído] Pacote mínimo de testes (ordem de impacto) para últimos fluxos críticos (16/06/2026):**
    Objetivo: elevar cobertura de regressão dos últimos commits focando primeiro nos fluxos de maior risco funcional (JS de interação/estado), depois nas bordas backend já mapeadas.
    Contexto: mudanças recentes introduziram estado transacional no frontend (`jogos.js`, `graficoDesempenho.js`) e novos caminhos de fallback no backend (`ParticipanteAction`, ranking com variação de posição).
    Plano de referência: `.ia/planos/plano-ux-performance-palpites-resultados-20260616.md` + análise de lacunas pós-commit.
    Skills previstas: `modern-javascript-patterns v1.0.0`, `architecture-guardian v1.0.0`, `security-audit v1.0.0`.

    * **[Concluído] 60.1 — Infra mínima de teste frontend (base para iterações curtas):**
      Configurar runner leve para testes JS de página (ex.: Vitest + jsdom) com escopo restrito aos módulos alterados.
      Entregável: `vitest.config.js`, scripts `test:frontend`/`test:frontend:watch`, setup em `tests/frontend/setup.js` e smoke validado via `npm run test:frontend`. Log: `.ia/logs/session-20260616-tarefa60-iteracao1-infra-testes-frontend.md`.

    * **[Concluído] 60.2 — Testes prioritários de estado em `jogos.js` (maior impacto) (16/06/2026):**
      Cobrir máquina de estados essencial (`dirty`/`saving`/`saved`/`error`), deduplicação de autosave, retry e guard `beforeunload`.
      Entregável: suíte mínima cobrindo cenários críticos de perda de edição/regressão de UX com novo arquivo `tests/frontend/jogos.test.js` (deduplicação de autosave, marcação `dirty`, guard `beforeunload`, retry de participante e retry admin), validada via `npm run test:frontend`.
      Skills aplicadas: `modern-javascript-patterns v1.0.0`, `htmx v1.0.0`, `architecture-guardian v1.0.0`.
      Log: `.ia/logs/session-20260616-tarefa60-iteracao2-jogos-state-tests.md`.

    * **[Concluído] 60.3 — Testes prioritários de concorrência/cache em `graficoDesempenho.js` (16/06/2026):**
      Validar comportamento de cancelamento (`AbortController`), política “última seleção vence” e fallback de erro/retry.
      Entregável: suíte `tests/frontend/graficoDesempenho.test.js` cobrindo cancelamento da requisição anterior, prevalência da seleção mais recente, uso de cache sem novo fetch para o mesmo rival e fallback com botão de retry após erro; validação em `npm run test:frontend`.
      Skills aplicadas: `modern-javascript-patterns v1.0.0`, `htmx v1.0.0`, `architecture-guardian v1.0.0`.
      Log: `.ia/logs/session-20260616-tarefa60-iteracao3-grafico-concorrencia-cache.md`.

    * **[Concluído] 60.4 — Borda backend do gráfico (`ParticipanteAction`) (16/06/2026):**
      Adicionar testes para cenários de fallback (`grafico == null`, ausência de contexto de response) preservando contrato JSON.
      Entregável: cobertura adicionada em `tests/com/opendev/bolao/action/ParticipanteActionTest.java` para os cenários de robustez (`grafico` nulo e ausência de `HttpServletResponse`) sem regressão do contrato de retorno (`series`/`categories` como listas vazias e retorno `success`).
      Skills aplicadas: `senior-java-dev-legacy v1.0.0`, `architecture-guardian v1.0.0`.
      Log: `.ia/logs/session-20260616-tarefa60-iteracao4-backend-grafico-fallback.md`.

    * **[Concluído] 60.5 — Borda backend da variação de ranking (`ParticipanteServiceImpl`) (16/06/2026):**
      Cobrir cenários `variacaoPosicao = 0`, participante sem histórico e participante com `id` nulo.
      Entregável: testes adicionados em `tests/com/opendev/bolao/service/impl/ParticipanteServiceTest.java` cobrindo manutenção de variação zero em posição estável, variação nula para participante novo sem snapshot anterior e tolerância a participante sem `id` sem regressão da ordenação oficial.
      Skills aplicadas: `senior-java-dev-legacy v1.0.0`, `architecture-guardian v1.0.0`.
      Log: `.ia/logs/session-20260616-tarefa60-iteracao5-variacao-ranking.md`.

    * **[Concluído] 60.6 — Sanity extra do filtro CSP (baixa complexidade) (16/06/2026):**
      Incluir teste opcional de não repetição de nonce entre requisições consecutivas.
      Entregável: cobertura adicional aplicada em `tests/com/opendev/bolao/security/CspNonceFilterTest.java`, validando nonces distintos por request e coerência do nonce gerado no header CSP correspondente.
      Skills aplicadas: `security-audit v1.0.0`, `architecture-guardian v1.0.0`.
      Log: `.ia/logs/session-20260616-tarefa60-iteracao6-csp-nonce.md`.

    * **[Concluído] 60.7 — Execução consolidada + rastreabilidade final (16/06/2026):**
      Rodar `npm run build`, suíte frontend e `mvn -Dfrontend.skip=true test`; registrar evidências e impacto de cobertura.
      Entregável: rodada consolidada executada com sucesso (`npm run build`, `npm run test:frontend`, `mvn -Dfrontend.skip=true test` com 77 testes Java aprovados) e rastreabilidade final registrada em log.
      Skills aplicadas: `modern-javascript-patterns v1.0.0`, `senior-java-dev-legacy v1.0.0`, `architecture-guardian v1.0.0`, `security-audit v1.0.0`.
      Log: `.ia/logs/session-20260616-tarefa60-iteracao7-execucao-consolidada.md`.

61. **[Concluído] Correção de regressões críticas na tela de palpites e resultados (16/06/2026):**
    Objetivo: corrigir dois bugs de alta severidade percebida em produção local: (1) alerta de saída indevido após atualização admin de resultados e (2) desaparecimento do campo de palpite ao iniciar digitação.
    Domínio impactado: UX transacional da operação administrativa (`/admin/jogos.action`) e registro de palpites do participante (`/seguro/palpites.action`).
    Causa raiz preliminar: desalinhamento entre contrato HTMX/JSP vigente e máquina de estados do módulo `src/frontend/pages/jogos.js`.
    Skills previstas: `modern-javascript-patterns v1.0.0`, `htmx v1.0.0`, `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.

    * **[Concluído] 61.1 — Documentar bug report técnico e rastreabilidade inicial (16/06/2026):**
      Registrar sintomas, cenário de reprodução e hipótese de causa raiz no plano + log de sessão.
      Entregável: subtarefa criada no `passo-a-passo.md` e log técnico inicial.

    * **[Concluído] 61.2 — Corrigir falso positivo de `beforeunload` no admin (16/06/2026):**
      Garantir decremento robusto de requisições pendentes admin (`pendingAdminRequests`) mesmo quando o elemento trigger for substituído via `hx-swap="outerHTML"`.
      Entregável: ajuste em `src/frontend/pages/jogos.js` com detecção resiliente de request admin por caminho (`requestConfig.path`) além de `closest`, evitando falso positivo do guard de saída após swaps HTMX de linha administrativa.

    * **[Concluído] 61.3 — Alinhar markup de palpites ao contrato HTMX atual (16/06/2026):**
      Remover contrato legado de inputs soltos por `tbody` e adotar célula dedicada `palpite-cell` com `form.palpite-inputs`/`hx-target` por célula.
      Entregável: ajustes em `match-row.jspf` e `palpite-cell-response.jsp` para uso consistente da célula direta (`palpite-cell-response.jspf`), preservando atualização OOB da barra de progresso.

    * **[Concluído] 61.4 — Testes de regressão direcionados (frontend) (16/06/2026):**
      Cobrir cenário de decremento admin resiliente e estabilidade da edição de palpite com contrato por célula.
      Entregável: suíte Vitest atualizada (`tests/frontend/jogos.test.js`) com cobertura do contrato `palpite-cell` após swap e validação completa via `npm run test:frontend` (4 testes aprovados).

    * **[Concluído] 61.5 — Validação consolidada + fechamento (16/06/2026):**
      Executar `npm run build`, `npm run test:frontend` e `mvn -Dfrontend.skip=true test`; atualizar status final e registrar evidências em log.
      Entregável: validação consolidada concluída (`npm run build`, `npm run test:frontend`, `mvn -Dfrontend.skip=true test` com 77 testes Java aprovados) e rastreabilidade final registrada.
      Log: `.ia/logs/session-20260616-tarefa61-correcao-bugs-palpites-resultados.md`.

62. **[Concluído] Hardening pós-auditoria dos últimos 4 commits (correções + testes) (16/06/2026):**
    Objetivo: eliminar riscos residuais e prevenir regressões da mesma família dos bugs recentes (troca agressiva de DOM em edição, estado pendente preso e guard de saída indevido), com cobertura de testes orientada a impacto.
    Escopo auditado: commits `3f095c4`, `45f2148`, `1ddf1c2` e `36273e2`.
    Skills previstas: `htmx v1.0.0`, `modern-javascript-patterns v1.0.0`, `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.

    * **[Concluído] 62.1 — Commit 3f095c4: blindar contrato HTMX/JSP/JS do palpite por célula (P0) (16/06/2026):**
      Validar e consolidar o contrato único de edição em `palpite-cell` (`hx-target` pontual + `hx-swap="outerHTML"`), removendo definitivamente qualquer fallback legado por `tbody`.
      Entregável: auditoria de markup final em `match-row.jspf`/`palpite-cell-response.jsp(f)` confirmando contrato por célula e inclusão de teste estrutural anti-regressão.
      Testes: `tests/frontend/jogos.test.js` ampliado para falhar explicitamente se reaparecer `hx-target="closest tbody"` ou `hx-swap="innerHTML"` no fluxo de edição de palpite.
      Critério de aceite: foco de digitação permanece estável e o campo de palpite não desaparece durante autosave/swap.

    * **[Concluído] 62.2 — Commit 3f095c4: robustecer contador de pendências admin e `beforeunload` (P0) (16/06/2026):**
      Fortalecer a contabilização `pendingAdminRequests` para cenários de concorrência, cancelamento e trocas de linha via `outerHTML`, evitando falso positivo de saída.
      Entregável: `src/frontend/pages/jogos.js` ajustado com rastreamento de request admin por `requestConfig` (`startAdminPending`/`finishAdminPending`) e fechamento resiliente em `afterRequest`/`responseError`.
      Testes: cenário novo em `tests/frontend/jogos.test.js` cobrindo requests admin simultâneas com finalização mista (sucesso + erro) sem travar o `beforeunload`.
      Critério de aceite: ao salvar resultado admin com sucesso, o aviso de sair sem salvar não aparece em estado limpo.

    * **[Concluído] 62.3 — Commit 45f2148: prevenção de regressão no gráfico (P1) (16/06/2026):**
      Revisar pontos de fragilidade no ciclo `fetch -> abort -> render -> retry` para evitar vazamento de estado visual (`aria-busy`, status loading) em transições rápidas.
      Entregável: hardening pontual em `src/frontend/pages/graficoDesempenho.js` para ignorar erros tardios de requisições obsoletas (`requestToken` desatualizado).
      Testes: `tests/frontend/graficoDesempenho.test.js` ampliado com caso de erro tardio da seleção anterior sem sobrescrever estado pronto da seleção mais recente.
      Critério de aceite: gráfico sempre retorna a estado consistente (status + busy) após erro, abort ou retry.

    * **[Concluído] 62.4 — Commit 1ddf1c2: prevenção de regressão da variação de posição (P1) (16/06/2026):**
      Revisar cálculo/exposição de `variacaoPosicao` para garantir estabilidade sem histórico, empate de posição e variação zero.
      Entregável: validação do contrato em `ParticipanteServiceImpl`/`DadosClassificacao` com foco em empate estável por regra de desempate oficial.
      Testes: `ParticipanteServiceTest` expandido com cenário de empate estrutural e ordem de entrada invertida entre snapshots, mantendo variação zero determinística.
      Critério de aceite: coluna VAR permanece coerente e determinística em ciclos sucessivos de cálculo.

    * **[Concluído] 62.5 — Commit 36273e2: reforço do pacote de regressão (P1) (16/06/2026):**
      Endurecer a suíte adicionada na tarefa 60 com casos de borda descobertos na auditoria (especialmente estados assíncronos e contratos HTMX).
      Entregável: atualização das suítes `tests/frontend/jogos.test.js` e `tests/frontend/graficoDesempenho.test.js`, além do reforço em `ParticipanteServiceTest`.
      Testes: validações direcionadas executadas com sucesso (`npm run test:frontend -- jogos.test.js`, `npm run test:frontend -- graficoDesempenho.test.js`, `mvn -Dfrontend.skip=true -Dtest=ParticipanteServiceTest test`).
      Critério de aceite: suíte de regressão cobre explicitamente todos os achados da auditoria dos 4 commits.

    * **[Concluído] 62.6 — Validação consolidada + rastreabilidade final (P0) (16/06/2026):**
      Rodar validação cruzada completa após as correções e registrar evidências técnicas.
      Entregável: execução concluída de `npm run build`, `npm run test:frontend` e `mvn -Dfrontend.skip=true test` (78 testes Java aprovados) + log final da tarefa com resultados e impacto.
      Logs: `.ia/logs/session-20260616-tarefa62-planejamento-correcao-achados-ultimos4commits.md` e `.ia/logs/session-20260616-tarefa62-execucao-completa.md`.

63. **[Concluído] Correção de UX: reposicionar campos de palpite ao lado dos times (16/06/2026):**
    Objetivo: restaurar a experiência de digitação do palpite junto aos nomes das equipes (como era antes), sem reintroduzir o contrato HTMX legado por `tbody`.
    Skills aplicadas: `htmx v1.0.0`, `modern-javascript-patterns v1.0.0`, `architecture-guardian v1.0.0`.

    * **[Concluído] 63.1 — Reposicionar inputs na linha das equipes com contrato HTMX seguro (16/06/2026):**
      Entregável: `match-row.jspf` atualizado para renderizar `p1/p2` ao lado dos times quando `palpitePermitido`, com `hx-target="#palpite-cell_<id>"` e `hx-swap="outerHTML"` (sem `closest tbody`).

    * **[Concluído] 63.2 — Ajustar fragmento de resposta para modo inline nos times (16/06/2026):**
      Entregável: `palpite-cell-response.jspf` simplificado para feedback/status/meta da célula, removendo formulário duplicado dentro da coluna de palpite.

    * **[Concluído] 63.3 — Compatibilizar máquina de estados JS com inputs inline (16/06/2026):**
      Entregável: `src/frontend/pages/jogos.js` adaptado para tratar `.palpite-inputs__score` em `beforeRequest/afterRequest`, suporte a retry por input e `dirty`/deduplicação sem dependência obrigatória de `form.palpite-inputs`.

    * **[Concluído] 63.4 — Regressão frontend e build (16/06/2026):**
      Entregável: testes atualizados e validados em `tests/frontend/jogos.test.js` + execução de `npm run test:frontend -- jogos.test.js`, `npm run test:frontend -- graficoDesempenho.test.js` e `npm run build`.
      Log: `.ia/logs/session-20260616-tarefa63-ux-posicao-campos-palpite.md`.

64. **[Concluído] Refinamento UX de alinhamento e centralização da tela de palpites (16/06/2026):**
    Objetivo: corrigir desalinhamento visual residual da grade de jogos, melhorando leitura, eixo de digitação e consistência entre estados editável/encerrado.
    Skills aplicadas: `ui-ux-pro-max v1.0.0`, `modern-css v1.0.0`, `htmx v1.0.0`, `architecture-guardian v1.0.0`.

    * **[Concluído] 64.1 — Criar subgrid fixo de EQUIPES com trilhos estáveis (16/06/2026):**
      Entregável: `webapp/css/estilo.css` ajustado para posicionar de forma determinística `time/bandeira/input` em ambos os lados (casa/fora), mantendo o separador central como eixo visual e alinhamento vertical consistente por linha.

    * **[Concluído] 64.2 — Padronizar dimensões e baseline dos elementos interativos (16/06/2026):**
      Entregável: inputs/placar estático/separador `x` unificados em uma mesma régua visual (altura, largura, peso tipográfico e contraste), com neutralização de margens legadas que causavam deslocamento lateral.

    * **[Concluído] 64.3 — Simplificar coluna PALPITE e alinhar ação lateral (16/06/2026):**
      Entregável: microcopy curta para estado bloqueado (`match.tip.state.locked.short`), redução de redundância visual na coluna PALPITE e alinhamento do botão lateral de grupo com dimensões/posição consistentes.

    * **[Concluído] 64.4 — Validação visual + regressão técnica (16/06/2026):**
      Entregável: validação executada com `npm run build` e `npm run test:frontend`, com evidências registradas em log de sessão (3 arquivos de teste frontend e 7 testes aprovados).
      Log: `.ia/logs/session-20260616-tarefa64-refino-ux-alinhamento-palpites.md`.

65. **[Concluído] Correção de janela de palpite (timezone) para jogos futuros (16/06/2026):**
    Objetivo: corrigir bloqueio indevido de palpite em jogo ainda dentro da janela permitida (até 1 hora antes), eliminando deriva de data por conversão de timezone.
    Skills aplicadas: `senior-java-dev-legacy v1.0.0`, `architecture-guardian v1.0.0`.

    * **[Concluído] 65.1 — Diagnóstico de causa raiz e rastreabilidade (16/06/2026):**
      Entregável: identificado risco de deslocamento de data na composição `Date + Time` quando `Date` chega como `java.sql.Date` e host roda em UTC, impactando `palpitePermitido`.

    * **[Concluído] 65.2 — Correção temporal em domínio/autorização (16/06/2026):**
      Entregável: composição `Date + Time` ajustada em `Jogo` e `PalpiteAuthorizationServiceImpl` para preservar o dia civil quando a data vem como `java.sql.Date`, mantendo timezone canônico `America/Sao_Paulo`.

    * **[Concluído] 65.3 — Testes de regressão de timezone (16/06/2026):**
      Entregável: testes adicionados em `JogoTest` e `PalpiteAuthorizationServiceImplTest` cobrindo cenário de jogo 22:00 às 18:33 (BRT) com `java.sql.Date`, validando que o palpite permanece permitido.

    * **[Concluído] 65.4 — Validação consolidada + fechamento (16/06/2026):**
      Entregável: suíte relevante executada com sucesso via `mvn -Dfrontend.skip=true -Dtest=PalpiteAuthorizationServiceImplTest,JogoTest test` (9 testes aprovados) e log atualizado com evidências.
      Log: `.ia/logs/session-20260616-tarefa65-correcao-janela-palpite-timezone.md`.

66. **[Concluído] Formalização do timezone oficial Brasil para jogos e UX de palpites (16/06/2026):**
    Objetivo: garantir que o sistema considere sempre timezone do Brasil (`America/Sao_Paulo`) para carga de jogos e feedback de horário ao usuário, independentemente do timezone do host/cliente.
    Skills aplicadas: `senior-java-dev-legacy v1.0.0`, `architecture-guardian v1.0.0`, `modern-javascript-patterns v1.0.0`.

    * **[Concluído] 66.1 — Backend: referência temporal canônica na carga mínima (16/06/2026):**
      Entregável: `ParticipanteAction.prepararInfoPalpites()` passou a usar início do dia em `BolaoTime.getZoneId()` como referência para buscar a próxima data com jogos.

    * **[Concluído] 66.2 — Frontend: horário de feedback fixado em Brasília/São Paulo (16/06/2026):**
      Entregável: `src/frontend/pages/jogos.js` atualizado para formatar `HH:mm` com `timeZone: 'America/Sao_Paulo'` nas mensagens de “salvo às”.

    * **[Concluído] 66.3 — Teste de regressão de referência temporal (16/06/2026):**
      Entregável: novo teste em `ParticipanteActionLoadTest` validando que a busca da carga mínima usa início do dia no timezone canônico do domínio.

67. **[Concluído] Correção defensiva de extração de hora para janela de palpite (16/06/2026):**
    Objetivo: eliminar bloqueio indevido de palpites quando houver divergência entre `Time.toLocalTime()` e o horário canônico derivado do epoch no fuso oficial (`America/Sao_Paulo`), cenário possível em materialização JDBC.
    Skills aplicadas: `senior-java-dev-legacy v1.0.0`, `architecture-guardian v1.0.0`.

    * **[Concluído] 67.1 — Normalização robusta da hora em `Jogo.getDataHora()` (16/06/2026):**
      Entregável: adicionada extração defensiva de hora (`extrairHoraLocal`) com reconciliação entre `toLocalTime()` e `Instant + ZoneId`.

    * **[Concluído] 67.2 — Normalização equivalente no serviço de autorização (16/06/2026):**
      Entregável: `PalpiteAuthorizationServiceImpl` passou a usar a mesma estratégia de extração de hora para manter consistência entre renderização e validação HTMX.

    * **[Concluído] 67.3 — Regressão com cenário de divergência de driver (16/06/2026):**
      Entregável: testes adicionados em `JogoTest` e `PalpiteAuthorizationServiceImplTest` simulando divergência de `toLocalTime()` e validando permissão correta para jogo às 22:00.

    * **[Concluído] 67.4 — Validação técnica da suíte focal (16/06/2026):**
      Entregável: execução de `mvn -Dfrontend.skip=true -Dtest=JogoTest,PalpiteAuthorizationServiceImplTest test` com `11` testes aprovados.

68. **[Concluído] Instrumentação de diagnóstico para bloqueio indevido de palpite (16/06/2026):**
    Objetivo: rastrear com precisão, por jogo e por requisição, por que a autorização ficou bloqueada, permitindo reproduzir e auditar cenários como horário divergente, role ausente e inconsistências de janela.
    Skills aplicadas: `senior-java-dev-legacy v1.0.0`, `architecture-guardian v1.0.0`.

    * **[Concluído] 68.1 — Log enriquecido no fluxo parcial de palpite (16/06/2026):**
      Entregável: `ParticipanteAction.prepararConteudoPalpite()` passou a logar `dataHoraJogo`, `agoraBrt`, `agoraBrtMais1h`, timezone default do host e `podeDarPalpite`.

    * **[Concluído] 68.2 — Diagnóstico por lista de jogos com detecção de anomalia (16/06/2026):**
      Entregável: adicionada rotina `diagnosticarPermissoesPalpiteNaLista(...)` em `ParticipanteAction` que cruza autorização, janela temporal e contexto de autenticação por `jogoId`.
      - Modo anomalia automático: gera `WARN` apenas quando o jogo deveria estar aberto e veio bloqueado.
      - Modo detalhado opcional: `INFO` por jogo ao abrir `/seguro/jogos.action?debugPalpite=true`.

    * **[Concluído] 68.3 — Validação de regressão técnica (16/06/2026):**
      Entregável: execução de `mvn -Dfrontend.skip=true -Dtest=ParticipanteActionLoadTest,PalpiteAuthorizationServiceImplTest,JogoTest test` com `19` testes aprovados.
      Observação: tentativa de incluir `ParticipanteActionTest` falhou no ambiente atual por dependência de X11 (JFreeChart), sem relação com as mudanças desta tarefa.

69. **[Concluído] UX de bloqueio para admin na tela de palpites (16/06/2026):**
    Objetivo: manter a regra de negócio que bloqueia admin para palpites, mas com feedback visual claro e consistente desde o render inicial da lista e nas respostas parciais HTMX.
    Skills aplicadas: `ui-ux-pro-max v1.0.0`, `senior-java-dev-legacy v1.0.0`, `architecture-guardian v1.0.0`.

    * **[Concluído] 69.1 — Ajustar render inicial para refletir `adminRestricted` (16/06/2026):**
      Entregável: `jogos-lista-fragmento.jsp` atualizado para calcular `palpitePermitido` sem perfil admin e preencher `palpiteBloqueioMotivo` com prioridade (`adminRestricted`, `roleMissing`, `timeWindow`, `unknown`).

    * **[Concluído] 69.2 — Melhorar mensagem de bloqueio na célula de palpite (16/06/2026):**
      Entregável: `palpite-cell-response.jspf` evoluído para exibir mensagem específica por motivo de bloqueio, incluindo texto explícito para admin (`match.tip.locked.adminRestricted`).

    * **[Concluído] 69.3 — Garantir cobertura de regra admin no serviço (16/06/2026):**
      Entregável: novo teste em `PalpiteAuthorizationServiceImplTest` validando bloqueio de admin com motivo `ADMIN_RESTRICTED`.

    * **[Concluído] 69.4 — Validação técnica focal (16/06/2026):**
      Entregável: execução de `mvn -Dfrontend.skip=true -Dtest=PalpiteAuthorizationServiceImplTest test` com `7` testes aprovados.

70. **[Concluído] Pontuação em tempo real a partir do início do jogo (16/06/2026):**
    Objetivo: permitir atualização dinâmica da classificação geral durante o jogo, considerando pontuação assim que o jogo iniciar e o resultado for atualizado pelo admin.
    Skills aplicadas: `senior-java-dev-legacy v1.0.0`, `architecture-guardian v1.0.0`.

    * **[Concluído] 70.1 — Alterar critério temporal de jogo ocorrido (16/06/2026):**
      Entregável: `Jogo.jaOcorreu()` atualizado para retornar verdadeiro a partir do horário de início da partida (sem espera fixa de +2h).

    * **[Concluído] 70.2 — Ajustar cache do ranking para sensibilidade temporal (16/06/2026):**
      Entregável: `ParticipanteServiceImpl.buscarClassificacao()` passou a invalidar cache global quando muda o minuto corrente no timezone canônico, garantindo reprocessamento automático quando um jogo entra em andamento.

    * **[Concluído] 70.3 — Atualizar e expandir regressão de testes (16/06/2026):**
      Entregável:
      - `JogoTest` adaptado para validar que `jaOcorreu()` fica true logo após o início.
      - `ParticipanteServiceTest` com novo teste para invalidação do cache por avanço de minuto.

    * **[Concluído] 70.4 — Validação técnica consolidada (16/06/2026):**
      Entregável: execução de `mvn -Dfrontend.skip=true -Dtest=JogoTest,ParticipanteServiceTest,PalpiteAuthorizationServiceImplTest test` com `20` testes aprovados.

71. **[Concluído] Refinamento da regra de cache e governança de atualização de placar (16/06/2026):**
    Objetivo: simplificar e tornar determinístico o comportamento do ranking em tempo real, invalidando cache apenas em atualização de placar pelo admin, e bloquear atualização de placar antes do início do jogo.
    Skills aplicadas: `senior-java-dev-legacy v1.0.0`, `architecture-guardian v1.0.0`, `modern-css v1.0.0`.

    * **[Concluído] 71.1 — Documentar regras operacionais de placar/ranking (16/06/2026):**
      Entregável: documentação dedicada criada em `.ia/planos/regras-negocio-placar-ranking-cache.md` com as regras acordadas:
      - admin só atualiza placar a partir do início do jogo;
      - jogos passados podem ser corrigidos;
      - cache de ranking invalida apenas em atualização de placar válida.

    * **[Concluído] 71.2 — Remover invalidação temporal periódica do ranking (16/06/2026):**
      Entregável: revertida a estratégia de invalidação por minuto em `ParticipanteServiceImpl`; ranking volta a ser invalidado por evento de domínio.

    * **[Concluído] 71.3 — Aplicar guarda de negócio na atualização de placar admin (16/06/2026):**
      Entregável: `AdminAction.atualizarResultadoDoJogo(...)` validando início da partida antes de persistir placar (`BusinessException` com HTTP 400 quando jogo ainda não iniciado).

    * **[Concluído] 71.4 — Ajustar pontos de invalidação de cache para evento de placar (16/06/2026):**
      Entregável: `JogoServiceImpl` ajustado para:
      - expirar cache de classificação apenas em `atualizarResultado` para jogo já iniciado;
      - não expirar classificação em `atualizarDadosEstruturaisJogo`.

    * **[Concluído] 71.5 — Cobertura de testes e regressão consolidada (16/06/2026):**
      Entregável:
      - `AdminActionTest` com cenários de bloqueio pré-jogo e sucesso pós-início;
      - remoção do teste de invalidação por minuto em `ParticipanteServiceTest`;
      - validação com `mvn -Dfrontend.skip=true -Dtest=AdminActionTest,ParticipanteServiceTest,JogoTest test` (`23` testes aprovados).

    * **[Concluído] 71.6 — UX preventiva no admin para jogos ainda não iniciados (16/06/2026):**
      Entregável:
      - `admin-match-row.jsp` atualizado para desabilitar inputs de placar (`golsEquipe1`/`golsEquipe2`) quando `jogo.podeAtualizarResultado=false`, evitando tentativa inválida e erro confuso de gravação;
      - feedback visual contextual adicionado na linha (`Liberado às HH:mm`) com suporte de acessibilidade (`aria-disabled`, `aria-label`) e mensagens i18n;
      - método de domínio `Jogo.getPodeAtualizarResultado()` criado para centralizar a regra temporal de edição de resultado na camada de modelo;
      - regressão validada com `mvn -Dfrontend.skip=true -Dtest=JogoTest,AdminActionTest test` (`17` testes aprovados).

72. **[Concluído] UX de Alto Impacto para Classificação Geral (Top 10 + variação de posição) (16/06/2026):**
    Objetivo: transformar a tela de Classificação Geral em uma experiência mais competitiva e legível, valorizando as 10 primeiras posições e a evolução de colocação, sem quebrar layout existente, funcionalidades e governança atual de cache.
    Skills aplicadas: `ui-ux-pro-max v1.0.0`, `architecture-guardian v1.0.0`, `modern-css v1.0.0`, `senior-java-dev-legacy v1.0.0`.
    Referência de plano: `.ia/planos/plano-ux-classificacao-geral-alto-impacto.md`.

    * **[Concluído] 72.1 — Diagnóstico UX + baseline técnico da classificação (16/06/2026):**
      Entregável:
      - inventário consolidado de `classificacao.jsp` + `estilo.css`, incluindo estado atual da coluna `VAR` e do destaque do usuário logado;
      - checklist arquitetural aplicado (sem nova query, sem alteração de invalidacão de cache e sem mudança da ordenação oficial);
      - critérios de aceite registrados no plano de execução.

    * **[Concluído] 72.2 — Criar camada visual de destaque para Top 10 (podium + trilha 4º–10º) (16/06/2026):**
      Entregável:
      - bloco Top 10 implementado em `webapp/WEB-INF/content/seguro/classificacao.jsp`, derivado da lista `participantes` já carregada em memória;
      - pódio explícito (1º/2º/3º) + trilha de cards de 4º a 10º com destaque contextual;
      - tabela oficial completa mantida como fonte canônica da classificação.

    * **[Concluído] 72.3 — Evoluir semântica visual da variação de posição (setas/badges/estado) (16/06/2026):**
      Entregável:
      - badges consistentes para `subiu`, `caiu`, `estável` e `novo/sem histórico` aplicados no Top 10 e na tabela;
      - semântica visual reforçada por texto e ícone (não dependente somente de cor);
      - chaves i18n novas adicionadas em `src/main/resources/messages.properties` e `src/messages.properties`.

    * **[Concluído] 72.4 — Refinar escaneabilidade da tabela sem quebrar leiaute existente (16/06/2026):**
      Entregável:
      - hierarquia visual reforçada nas colunas de posição, variação, nome e pontuação;
      - aplicação de `font-variant-numeric: tabular-nums` para leitura estável de métricas;
      - destaque do usuário logado evoluído com contraste e contorno mais perceptível.

    * **[Concluído] 72.5 — Responsividade e microinterações (desktop/mobile) (16/06/2026):**
      Entregável:
      - comportamento responsivo do bloco Top 10 com trilha rolável em mobile e cartões com `scroll-snap`;
      - ajustes de densidade visual nos breakpoints `1024px` e `768px`;
      - microinterações sutis com respeito a `prefers-reduced-motion`.

    * **[Concluído] 72.6 — Acessibilidade e conteúdo de apoio (legendas e contexto) (16/06/2026):**
      Entregável:
      - contraste e semântica revisados em badges/realces mantendo base WCAG 2.1 AA;
      - legenda compacta da coluna `VAR` adicionada no cabeçalho da tabela;
      - nota contextual de desempate exibida no bloco de destaque quando há empate de pontuação no topo.

    * **[Concluído] 72.7 — Cobertura de testes e validação de regressão (16/06/2026):**
      Entregável:
      - regressão da classificação executada em `ParticipanteServiceTest` (cálculo/variação/cache) e `ParticipanteActionLoadTest` (carga/ordenação/listagem);
      - comando executado: `mvn -Dfrontend.skip=true -Dtest=ParticipanteServiceTest,ParticipanteActionLoadTest test`;
      - resultado: `15` testes aprovados, `0` falhas.

    * **[Concluído] 72.8 — Encerramento de rastreabilidade (log + atualização do plano) (16/06/2026):**
      Entregável:
      - registro de execução em `.ia/logs/session-20260616-tarefa72-execucao-ux-classificacao-geral.md`;
      - atualização final desta tarefa/subtarefas no `passo-a-passo.md`;
      - backlog de evolução contínua recomendado no log (filtro por recortes, animação de entrada em swap HTMX e comparativo por usuário).

73. **[Concluído] Correção de cache-busting do CSS global para evitar quebra de layout pós-deploy (16/06/2026):**
    Objetivo: impedir que o navegador/CDN mantenha `estilo.css` antigo após deploy de novas versões, cenário que causou mismatch entre JSP novo e CSS desatualizado na tela de classificação.
    Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.

    * **[Concluído] 73.1 — Diagnóstico de causa raiz (16/06/2026):**
      Entregável:
      - identificado carregamento estático do CSS em `cabecalho.jspf` via `@IMPORT url("${base}/css/estilo.css")`, sem token de versão para invalidação de cache.

    * **[Concluído] 73.2 — Implementar versionamento de URL do CSS global (16/06/2026):**
      Entregável:
      - `cabecalho.jspf` ajustado para usar `link rel="stylesheet"` com query param versionado: `estilo.css?v=${buildInfo.version}` (fallback `dev` quando metadado não disponível).

    * **[Concluído] 73.3 — Validar não regressão e rastreabilidade (16/06/2026):**
      Entregável:
      - validação técnica rápida executada após ajuste;
      - log registrado em `.ia/logs/session-20260616-tarefa73-cache-busting-css-global.md`.

74. **[Concluído] Correção da tela de Palpites e Resultados: filtro de pesquisa + botão "Ver palpites do grupo" (17/06/2026):**
    Objetivo: eliminar quebra de layout associada ao uso do filtro e restaurar funcionamento confiável do botão de exibição de palpites do grupo por jogo, com cobertura de regressão.
    Skills aplicadas: `ui-ux-pro-max v1.0.0`, `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`, `modern-javascript-patterns v1.0.0`.
    Referência de plano: `.ia/planos/plano-correcao-palpites-filtro-grupo-20260617.md`.

    * **[Concluído] 74.1 — Diagnóstico reproduzível dos dois sintomas (17/06/2026):**
      Entregável:
      - análise estática consolidada apontou fragilidade de markup no filtro (fechamento semântico inconsistente de `label` no campo de fase e gestão frágil de `optgroup`);
      - identificado acoplamento JS+HTMX no botão de grupo sem cobertura dedicada (toggle de `.hidden` + alvo `#group-content_<id>`).

    * **[Concluído] 74.2 — Higienização estrutural do markup do filtro (`jogos.jsp`) (17/06/2026):**
      Entregável:
      - corrigido fechamento de `label` para `filtro_fase`;
      - padronizada inicialização de `grupoAnterior` em `filtroEquipe` e fechamento explícito de `optgroup` ao fim do loop;
      - reduzido risco de DOM malformado e quebra de layout em browsers mais estritos.

    * **[Concluído] 74.3 — Endurecimento do fluxo JS do botão de grupo (`jogos.js`) (17/06/2026):**
      Entregável:
      - guardas adicionadas para `event.target` não-elemento e `data-target` ausente;
      - sincronização de `aria-expanded` com estado real de abertura/fechamento;
      - manutenção robusta do modo accordion com reset de estado em botões não ativos;
      - warning de diagnóstico para alvo inexistente, evitando falha silenciosa.

    * **[Concluído] 74.4 — Endurecimento do carregamento HTMX de palpites do grupo (17/06/2026):**
      Entregável:
      - botão de grupo alterado para `hx-trigger="click"` (removido `once`) permitindo recarga/retry em interações subsequentes;
      - reforço semântico com `aria-controls` e `aria-expanded` no botão por jogo;
      - alvo HTMX preservado por `jogoId` (`#group-content_<id>`), mantendo contrato de swap no `tbody`.

    * **[Concluído] 74.5 — Ajustes CSS de robustez para filtro e painel de grupo (17/06/2026):**
      Entregável:
      - revisão das regras de filtro/painel de grupo executada, sem necessidade de novo patch CSS nesta rodada;
      - robustez principal obtida por correção estrutural de markup e endurecimento de estado JS/HTMX.

    * **[Concluído] 74.6 — Cobertura de testes frontend para evitar regressão (17/06/2026):**
      Entregável:
      - novos testes em `tests/frontend/jogos.test.js` cobrindo:
      - interação accordion do botão de grupo + `aria-expanded`;
      - contrato HTMX no `match-row.jspf` (`hx-trigger="click"`, `hx-target` e `aria-controls`);
      - verificação de fechamento semântico do `label` de filtro de fase em `jogos.jsp`.

    * **[Concluído] 74.7 — Regressão focal + smoke local (17/06/2026):**
      Entregável:
      - frontend: `npm run test:frontend -- tests/frontend/jogos.test.js` (`6` testes, `0` falhas);
      - backend focal: `mvn -Dfrontend.skip=true -Dtest=ParticipanteActionLoadTest test` (`8` testes, `0` falhas).

    * **[Concluído] 74.8 — Encerramento de rastreabilidade (17/06/2026):**
      Entregável:
      - log final registrado em `.ia/logs/session-20260617-tarefa74-execucao-palpites-filtro-grupo.md`;
      - status da tarefa/subtarefas atualizado neste `passo-a-passo.md`.

75. **[Concluído] Pacote mínimo de cobertura da tela de Palpites e Resultados (17/06/2026):**
    Objetivo: fortalecer regressão da tela de palpites/resultados com foco em interações sensíveis (detalhes de grupo, filtro colapsável e carregamento incremental HTMX), priorizando impacto funcional.
    Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`, `modern-javascript-patterns v1.0.0`.

    * **[Concluído] 75.1 — Cobrir fechamento dos detalhes de grupo (botão `X` + tecla `Esc`) e estado ARIA (17/06/2026):**
      Entregável:
      - novos cenários adicionados em `tests/frontend/jogos.test.js` cobrindo fechamento via `data-js="close-details"` e via teclado (`Escape`);
      - validação de sincronização de `aria-expanded` e classe visual `active/hidden`;
      - cenário defensivo adicionado para botão de grupo sem `data-target` (deve ser ignorado sem quebrar a tela).

    * **[Concluído] 75.2 — Cobrir comportamento do filtro colapsável (desktop/mobile + sessionStorage) (17/06/2026):**
      Entregável:
      - testes de `initFiltroColapsavel()` adicionados em `tests/frontend/jogos.test.js`;
      - cobertura validando persistência de estado colapsado em desktop (`sessionStorage`) e abertura padrão em mobile.

    * **[Concluído] 75.3 — Cobrir endpoint de carregamento incremental (`buscarMaisJogosHtmx`) com cenários de borda (17/06/2026):**
      Entregável:
      - testes backend adicionados em `ParticipanteActionLoadTest` para:
      - `dataInicial` inválida (fallback seguro para lista vazia);
      - ausência de próxima data disponível;
      - exceção no `JogoService` com manutenção do retorno estável da action (`SUCCESS` + lista vazia).

    * **[Concluído] 75.4 — Cobrir regra de permissão de palpite na montagem da lista (admin bloqueado x user habilitado) (17/06/2026):**
      Entregável:
      - teste de contrato de markup adicionado em `tests/frontend/jogos.test.js` validando presença das regras:
      - `usuarioAdmin` vs `usuarioComPapelPalpite`;
      - cálculo de `podeRegistrarPalpite` e `palpitePermitido`;
      - motivo de bloqueio `adminRestricted` preservado no fragmento.

    * **[Concluído] 75.5 — Regressão consolidada + rastreabilidade final da tarefa 75 (17/06/2026):**
      Entregável:
      - execução focal: `npm run test:frontend -- tests/frontend/jogos.test.js` e suíte backend associada;
      - resultados:
      - frontend `12` testes aprovados, `0` falhas;
      - backend `11` testes aprovados, `0` falhas;
      - logs de execução registrados em `.ia/logs/session-20260617-tarefa75-*.md` e atualização final desta tarefa no plano.

    * **[Concluído] 75.6 — Validação prévia de `dataInicial` inválida para reduzir ruído de log (17/06/2026):**
      Entregável:
      - `ParticipanteAction.buscarMaisJogosHtmx()` ajustado para validar `dataInicial` após parse;
      - quando inválida, fluxo retorna com lista vazia e log `WARN` objetivo (`[HTMX][LOAD_MORE]`) sem stacktrace;
      - `ERROR` com stacktrace mantido apenas para falhas inesperadas de serviço;
      - regressão backend reexecutada com sucesso via `mvn -Dfrontend.skip=true -Dtest=ParticipanteActionLoadTest test`.

76. **[Concluído] Hardening de validação do filtro de busca da tela de Palpites e Resultados (17/06/2026):**
    Objetivo: validar de forma explícita os campos de pesquisa do filtro (datas, fase, equipe e grupo), reduzindo inconsistências de entrada sem alterar as regras centrais de consulta.
    Skills aplicadas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`.

    * **[Concluído] 76.1 — Endurecer validação de datas no filtro completo (`usarFiltro=true`) (17/06/2026):**
      Entregável:
      - tratamento explícito para datas inválidas (`dataInicial`/`dataFinal`) no `obterFiltro`;
      - normalização de intervalo invertido (`dataFinal < dataInicial`) com swap controlado e log `WARN`.

    * **[Concluído] 76.2 — Validar whitelist de fase/grupo e pertencimento de equipe (17/06/2026):**
      Entregável:
      - `filtroFase` aceito apenas para fases permitidas no domínio;
      - `filtroGrupo` normalizado e aceito apenas no padrão `A..H`;
      - `filtroEquipe` aplicado somente quando o id pertence à lista de equipes reais disponíveis.

    * **[Concluído] 76.3 — Cobertura de testes backend para entradas válidas/inválidas do filtro (17/06/2026):**
      Entregável:
      - novos testes adicionados em `ParticipanteActionLoadTest` cobrindo:
      - saneamento de campos inválidos (`fase`, `grupo`, `equipe`) e swap de intervalo de datas invertido;
      - preservação de campos válidos (`fase`, `grupo`, `equipe`, datas no intervalo correto).

    * **[Concluído] 76.4 — Regressão focal + rastreabilidade final da tarefa 76 (17/06/2026):**
      Entregável:
      - execução focal de testes backend associados;
      - resultado: `mvn -Dfrontend.skip=true -Dtest=ParticipanteActionLoadTest test` com `13` testes aprovados e `0` falhas;
      - log técnico em `.ia/logs/session-20260617-tarefa76-validacao-filtro-busca.md` e atualização final desta tarefa no plano.

77. **[Concluído] Evolução UX/Arquitetura da tela de Palpites e Resultados (P0-P2) (17/06/2026):**
    Objetivo: reduzir atrito de uso e desperdício de requisições na tela de palpites/resultados sem quebrar regras funcionais existentes.
    Skills aplicadas: `ui-ux-pro-max v1.0.0`, `architecture-guardian v1.0.0`, `modern-javascript-patterns v1.0.0`.

    * **[Concluído] 77.1 — Remover duplicidade de listener global de teclado (`Escape`) (17/06/2026):**
      Entregável:
      - eliminado registro duplicado de `handleDocumentKeydown` na inicialização da página;
      - preservado comportamento funcional de fechamento por `Escape` no fluxo já registrado em `initPalpiteInline`.

    * **[Concluído] 77.2 — Reduzir requisições HTMX redundantes no botão “Ver palpites do grupo” (17/06/2026):**
      Entregável:
      - `match-row.jspf` ajustado para disparar HTMX apenas quando conteúdo ainda não estiver carregado (`hx-trigger` condicional por `dataset.groupLoaded`);
      - `jogos.js` ajustado para marcar botão como carregado após `htmx:afterSwap` em `#group-content_<id>`;
      - reduzido round-trip em reaberturas do mesmo painel no mesmo ciclo de tela.

    * **[Concluído] 77.3 — Cobertura de regressão frontend da otimização de grupo (17/06/2026):**
      Entregável:
      - testes atualizados em `tests/frontend/jogos.test.js` para contrato HTMX condicional e marcação `data-group-loaded` após swap.

    * **[Concluído] 77.4 — Melhorar feedback visual de saneamento do filtro no frontend (P1) (17/06/2026):**
      Entregável:
      - backend passou a expor `filtroAvisos` com mensagens de saneamento;
      - `jogos.jsp` atualizado para exibir aviso discreto em tela quando parâmetros de filtro forem ajustados/ignorados.

    * **[Concluído] 77.5 — Refinos de acessibilidade e microinteração do painel de grupo (P1/P2) (17/06/2026):**
      Entregável:
      - botões de grupo/fechamento com `aria-label` explícito no markup;
      - `jogos.js` ajustado para sincronizar `aria-label` e `aria-expanded` conforme estado aberto/fechado;
      - cobertura de regressão frontend atualizada para contrato de acessibilidade.

    * **[Concluído] 77.6 — Regressão focal desta iteração (17/06/2026):**
      Entregável:
      - `npm run test:frontend -- tests/frontend/jogos.test.js` com `13` testes aprovados;
      - `mvn -Dfrontend.skip=true -Dtest=ParticipanteActionLoadTest test` com `13` testes aprovados.

78. **[Concluído] BUG crítico: tela de Palpites/Resultados bloqueando todos os jogos como “Edição Encerrada” (17/06/2026):**
    Objetivo: eliminar regressão que marca todos os jogos como bloqueados (inclusive futuros), restaurando a regra oficial de janela de palpite (até 1h antes), com diagnóstico auditável e cobertura anti-reincidência.
    Skills previstas: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`, `ui-ux-pro-max v1.0.0`, `modern-javascript-patterns v1.0.0`.

    * **[Concluído] 78.1 — Diagnóstico reproduzível com evidência de causa raiz (backend + view-model):**
      Entregável:
      - reproduzir em ambiente local com usuário não-admin e jogo futuro;
      - registrar evidência de `palpiteBloqueioMotivo` retornado por jogo (`timeWindow` vs `roleMissing` vs `adminRestricted`);
      - consolidar causa raiz provável identificada no histórico: divergência de regra de autorização no JSP de lista (`jogos-lista-fragmento.jsp`) após hardening recente.

    * **[Concluído] 78.2 — Unificar regra de autorização de lista com o serviço canônico de autorização de palpite:**
      Entregável:
      - remover lógica frágil/duplicada de papel diretamente na JSP para decisão final de permissão;
      - passar a consumir decisão canônica de autorização por jogo (serviço/action), preservando bloqueio de admin e janela temporal oficial.
      Resultado:
      - `ParticipanteAction` passou a calcular `autorizacoesPalpitePorJogo` com base em `avaliarAutorizacao(...)` para cada jogo listado;
      - `jogos-lista-fragmento.jsp` deixou de usar `sec:authorize` para decidir permissão e passou a consumir o mapa canônico do backend.

    * **[Concluído] 78.3 — Ajustar feedback visual da causa de bloqueio por jogo:**
      Entregável:
      - garantir mensagem específica e consistente quando bloqueio for por perfil (`roleMissing`/`adminRestricted`) vs janela (`timeWindow`);
      - manter rótulo curto “Edição encerrada” sem perder explicação detalhada contextual.
      Resultado:
      - a origem de `palpiteBloqueioMotivo` passou a ser exclusivamente canônica (backend), mantendo o contrato visual existente em `palpite-cell-response.jspf` com mensagens específicas por motivo.

    * **[Concluído] 78.4 — Cobertura de regressão direcionada (cenários de permissão):**
      Entregável:
      - testes garantindo que usuário habilitado possa palpitar jogos futuros dentro da janela;
      - testes garantindo bloqueio de admin e bloqueio por janela encerrada;
      - testes cobrindo normalização de perfil/role para evitar falsos bloqueios por variação de prefixo (`ROLE_`).
      Resultado:
      - adicionados testes em `ParticipanteActionLoadTest` para validar preenchimento canônico de autorização por jogo na carga inicial e no “carregar mais jogos”;
      - atualizado teste de contrato frontend (`tests/frontend/jogos.test.js`) para garantir consumo de `autorizacoesPalpitePorJogo` e ausência da lógica local por `hasRole(...)` no fragmento.

    * **[Concluído] 78.5 — Regressão final + rastreabilidade da correção:**
      Entregável:
      - execução focal de testes frontend/backend associados;
      - log de sessão em `.ia/logs/` com evidências antes/depois e conclusão técnica.
      Resultado:
      - `npm run test:frontend -- tests/frontend/jogos.test.js` com 13/13 testes aprovados;
      - `mvn -Dfrontend.skip=true -Dtest=ParticipanteActionLoadTest,PalpiteAuthorizationServiceImplTest test` com 22/22 testes aprovados.

    * **[Concluído] 78.6 — Hotfix de regressão no botão “Palpites do Grupo” (17/06/2026):**
      Entregável:
      - removido `event.preventDefault()` do handler de `toggle-group-details` em `jogos.js`, permitindo que o `hx-get` do HTMX seja disparado no clique;
      - adicionado teste de regressão para garantir que o clique do botão de grupo não seja cancelado pelo JavaScript.
      Resultado:
      - `npm run test:frontend -- tests/frontend/jogos.test.js` com 14/14 testes aprovados.

    * **[Concluído] 78.7 — Ajuste estratégico anti-conflito entre autorização canônica e contexto da view (17/06/2026):**
      Entregável:
      - mantida autorização canônica via `autorizacoesPalpitePorJogo` como fonte principal;
      - adicionado fallback defensivo no JSP para cenário de divergência (`roleMissing`) quando a request web reconhece `ROLE_USER` (sem `ROLE_ADMIN`), respeitando estritamente a janela temporal (`jogo.podeDarPalpite`);
      - preservado bloqueio de admin e removida reintrodução de `sec:authorize`.
      Resultado:
      - `npm run test:frontend -- tests/frontend/jogos.test.js` com 14/14 testes aprovados;
      - `mvn -Dfrontend.skip=true -Dtest=ParticipanteActionLoadTest,PalpiteAuthorizationServiceImplTest test` com 22/22 testes aprovados.

    * **[Concluído] 78.8 — Robustez do botão “Ver palpites do grupo” (17/06/2026):**
      Entregável:
      - estratégia de carregamento do painel de grupo movida para `jogos.js`: ao abrir accordion, o script dispara `htmx.ajax('GET', ...)` explicitamente quando o grupo ainda não foi carregado;
      - remoção do `hx-trigger` declarativo no botão para evitar dependência de ordem de eventos/cancelamentos;
      - adicionado controle de estado `groupLoading/groupLoaded` com limpeza após `afterSwap`/`responseError`.
      Resultado:
      - `npm run test:frontend -- tests/frontend/jogos.test.js` com 15/15 testes aprovados.

    * **[Concluído] 78.9 — Correção final do botão de grupo sem regressão da janela de palpite (>1h) (17/06/2026):**
      Entregável:
      - fluxo de carregamento de detalhes do grupo consolidado em caminho único no frontend, priorizando `htmx.ajax('GET', ...)` e mantendo fallback `fetch` absoluto apenas para contingência;
      - botão de grupo marcado com `hx-trigger="none"` para eliminar disputa entre disparo automático e disparo manual controlado (evita request duplicada/intermitente no clique);
      - validação explícita de não regressão da regra de autorização temporal, mantendo a decisão canônica no backend (`PalpiteAuthorizationService`) para jogos futuros dentro da janela oficial.
      Resultado:
      - `npm run test:frontend -- tests/frontend/jogos.test.js` com 15/15 testes aprovados;
      - `mvn -q -Dfrontend.skip=true -Dtest=PalpiteAuthorizationServiceImplTest,ParticipanteActionLoadTest test` executado com sucesso.

    * **[Concluído] 78.10 — Hardening do alvo de swap HTMX no botão de grupo (17/06/2026):**
      Entregável:
      - identificado risco de compatibilidade no uso de `htmx.ajax` com `target` textual em objeto de contexto;
      - `requestGroupDetails` atualizado para usar o elemento DOM já resolvido (`target`) no contexto do `htmx.ajax`, reduzindo ambiguidade de resolução do alvo;
      - teste frontend ajustado para validar o novo contrato de chamada (`target` como `HTMLElement`).
      Resultado:
      - `npm run -s test:frontend -- tests/frontend/jogos.test.js` com 15/15 testes aprovados;
      - `mvn -q -Dfrontend.skip=true -Dtest=PalpiteAuthorizationServiceImplTest,ParticipanteActionLoadTest test` executado com sucesso.

    * **[Concluído] 78.11 — Fallback defensivo para carregamento do painel de grupo quando HTMX não efetiva swap (17/06/2026):**
      Entregável:
      - adicionado watchdog de 1200ms em `requestGroupDetails`: se `groupLoading` continuar ativo após tentativa `htmx.ajax`, o frontend reexecuta a carga via `fetch` com `HX-Request`;
      - preservado contrato visual/ARIA do accordion e tratamento de erro existente;
      - objetivo: cobrir cenário real de “botão fica verde, mas linhas de palpites não aparecem”.
      Resultado:
      - `npm run -s test:frontend -- tests/frontend/jogos.test.js` com 15/15 testes aprovados;
      - `mvn -q -Dfrontend.skip=true -Dtest=PalpiteAuthorizationServiceImplTest,ParticipanteActionLoadTest test` executado com sucesso.

    * **[Concluído] 78.12 — Correção de conflito entre scripts (duplo toggle) no botão de grupo (17/06/2026):**
      Entregável:
      - identificada causa raiz de produção local: `webapp/js/ux-helper.js` (legado) também registrava clique em `[data-js="toggle-group-details"]` e executava `target.classList.toggle('hidden')`, revertendo o estado já alterado por `src/frontend/pages/jogos.js`;
      - removido include legado da tela `jogos.jsp` para evitar dupla orquestração de eventos no mesmo botão;
      - adicionado teste anti-regressão em `tests/frontend/jogos.test.js` garantindo que `jogos.jsp` não volte a carregar `/js/ux-helper.js`.
      Resultado:
      - `npm run -s test:frontend -- tests/frontend/jogos.test.js` com 16/16 testes aprovados;
      - `mvn -q -Dfrontend.skip=true -Dtest=PalpiteAuthorizationServiceImplTest,ParticipanteActionLoadTest test` executado com sucesso.

47. **[Concluído] Incluir campo de data na edição administrativa da tela de atualização de resultados (13/06/2026):**
    Objetivo: permitir que o administrador ajuste também a **data** do jogo na mesma tela em que já ajusta hora, local e equipes, mantendo consistência com o fuso oficial São Paulo e sem regressão no fluxo HTMX de atualização.
    Skills previstas: `senior-java-dev-legacy v1.0.0`, `architecture-guardian v1.0.0`, `ui-ux-pro-max v1.0.0`.
    * **[Concluído] 47.1 — Diagnóstico de contrato atual da edição admin:** mapeamento concluído em `AdminAction`/`JogoServiceImpl`/JSPs; backend já persiste `data` na edição estrutural e o gap identificado estava na linha administrativa compartilhada (`admin-match-row.jsp`), onde o campo era enviado como `hidden` e não editável visualmente.
    * **[Concluído] 47.2 — Definir contrato de entrada para data (backend):** validação explícita aplicada em `AdminAction.salvarEdicaoEstruturalHtmx()` para `data` (`dd/MM/yyyy`) e `hora` (`HH:mm`) via parse + round-trip canônico, retornando HTTP 400 quando formato for inválido, sem quebrar payload existente.
    * **[Concluído] 47.3 — Ajustar persistência da data no serviço de jogos:** validado que `JogoServiceImpl.atualizarDadosEstruturaisJogo(...)` já persiste `jogo.setData(data)` e `jogo.setHora(hora)`; com a validação de formato adicionada na action, o serviço passa a receber apenas valores canônicos, preservando consistência temporal do domínio em São Paulo.
    * **[Concluído] 47.4 — Atualizar UI administrativa da linha editável:** campo de data adicionado à linha administrativa (`admin-match-row.jsp`) com envio HTMX incremental para `salvarEdicaoEstrutural.action`; aplicado ajuste visual discreto em `estilo.css` para manter data/hora compactos na célula de horário.
    * **[Concluído] 47.5 — Atualizar fragmento HTMX de retorno da linha:** confirmado em runtime que o fragmento retornado por `salvarEdicaoEstrutural.action` renderiza `select name="data"` na própria linha (`admin-match-row.jsp`) com `hx-swap="outerHTML"`, refletindo a data atualizada sem refresh completo.
    * **[Concluído] 47.6 — Cobertura de testes unitários (action/service):** `AdminActionTest` atualizado para formato real da UI (`dd/MM/yyyy`) e cenários de erro 400 (data ausente e data inválida), além de validação de data dinâmica em timezone São Paulo; `JogoServiceImplTest` já cobre persistência de `data`/`hora` em `atualizarDadosEstruturaisJogo`.
    * **[Concluído] 47.7 — Validação funcional e regressão cruzada:** suíte completa `mvn -Dfrontend.skip=true test` executada com sucesso (`66` testes, `0` falhas) e smoke autenticado em `/admin/jogos.action` após rebuild Docker confirmando presença do campo de data inline e contratos HTMX ativos.
    * **[Concluído] 47.8 — Rastreabilidade da execução:** logs registrados em `.ia/logs/session-20260613-tarefa47-iteracao1-data-inline-admin.md` e `.ia/logs/session-20260613-tarefa47-finalizacao.md` com evidências de build, testes e validação funcional.

48. **[Concluído] Adicionar botão "Carregar Próxima Data" na tela administrativa de resultados (14/06/2026):**
    Objetivo: permitir que o administrador carregue as próximas datas de jogos progressivamente via HTMX, mantendo a consistência com a tela de palpites.
    Skills previstas: `senior-java-dev-legacy v1.0.0`, `architecture-guardian v1.0.0`, `ui-ux-pro-max v1.0.0`.

    * **[Concluído] 48.1 — Implementar método `buscarMaisJogosHtmx` em `AdminAction`:** 
        Adicionado o método que recebe `data` (String dd/MM/yyyy), encontra a próxima data com jogos e carrega os dados marcando `adminResultadoView=true`.
    * **[Concluído] 48.2 — Mapear action no `struts.xml`:** 
        Registrada a action `jogosMaisJogosPartial` no package `admin` apontando para o fragmento de lista compartilhado.
    * **[Concluído] 48.3 — Ajustar `jogos-lista-fragmento.jsp` para exibição condicional:**
        A lógica do botão foi atualizada para suportar o contexto administrativo e alternar endpoints HTMX dinamicamente.
    * **[Concluído] 48.4 — Testes e Validação:**
        Validado via testes unitários em `AdminActionTest` e verificado via build Maven.

49. **[Concluído] Corrigir agrupamento replicado de datas na listagem de jogos (14/06/2026):**
    Objetivo: garantir que jogos da mesma data fiquem sob um único cabeçalho e corrigir o aninhamento de tags HTML no fragmento de lista.
    Skills previstas: `ui-ux-pro-max v1.0.0`, `architecture-guardian v1.0.0`.

    * **[Concluído] 49.1 — Refatorar lógica de agrupamento no `jogos-lista-fragmento.jsp`:**
        - Substituída a comparação de objetos `Date` (`ne`) por comparação de strings formatadas (`dataJogoFormatada`).
        - Implementado o fechamento sistemático de tags HTML do bloco anterior.
    * **[Concluído] 49.2 — Validar exibição da data 11/06/2026 no Admin:**
        - Lógica de agrupamento testada via análise de paridade de strings, eliminando duplicidade por componentes de milissegundos.
    * **[Concluído] 49.3 — Teste de regressão visual:**
        - Suíte de testes aprovada e integridade do fragmento validada para ambos os perfis (usuário/admin).

50. **[Concluído] Hardening de Performance: Otimização de Runtime e Latência (14/06/2026):**
    Objetivo: Reduzir tempo de resposta (TTFB) e latência percebida no Hugging Face Spaces para < 800ms, otimizando cache para padrões de acesso de um bolão (dados compartilhados).
    Skills: `performance-wizard v1.0.0`, `architecture-guardian v1.0.0`, `docker-master v1.0.0`.

    * **[Concluído] 50.1 — Tuning de JVM e Dockerfile**:
        - Heap fixo 1024M + G1GC MaxGCPauseMillis=100 + StringDeduplication + IOHP=35.
        - GZIP ativado no Connector Tomcat com compressionMinSize=1024 para fragmentos HTMX.
        - maxThreads=150, minSpareThreads=25 para maior throughput paralelo.
    * **[Concluído] 50.2 — Ativar GZIP e Tuning no Tomcat (`server.xml`)**:
        - Incorporado no 50.1 (sed direto no Dockerfile).
    * **[Concluído] 50.3 — Otimização de Persistência e Pool**:
        - HikariCP `minimumIdle` aumentado de 2 para 5 para manter conexões aquecidas com Aiven.
    * **[Concluído] 50.4 — Cache Local para Dados de Alta Leitura**:
        - `JogoServiceImpl.buscarJogosDeHoje()`: cache com TTL de 1 dia calendário (São Paulo).
        - `ParticipanteServiceImpl.buscarClassificacao()`: cache global de ranking invalidado por `Participante.expirarCacheDeClassificacao()`.
        - `EquipeServiceImpl.buscarApenasPaisesReais()`: cache permanente (lista estática durante o torneio).
        - Teste unitário adicionado: `deveRetornarJogosDeHojeSemChamadaRepetidaAoBanco` (68 testes).

51. **[Concluído] Correção Crítica: Estabilização de Cache e Struts (14/06/2026):**
    Objetivo: Resolver erro de login causado por lista imutável e falta de mapeamento de erro no Struts.
    Skills: `architecture-guardian v1.1.0`.

    * **[Concluído] 51.1 — Robustez no Struts**:
        - Adicionado `<global-results>` para `error` em `struts.xml`.
    * **[Concluído] 51.2 — Defesa de Cache**:
        - `ParticipanteServiceImpl` agora retorna cópia mutável da lista de ranking.
    * **[Concluído] 51.3 — Limpeza de Actions**:
        - Removida ordenação redundante em `ParticipanteAction`.

    52. **[Pendente] Ajuste Fino na Invalidação de Cache de Ranking:**
    Objetivo: Garantir consistência absoluta do ranking global, invalidando o cache sempre que palpites forem salvos ou atualizados.
    * **[Pendente] 52.1 — Instrumentar PalpiteService:** Incluir `Participante.expirarCacheDeClassificacao()` em métodos de persistência de palpites.
    * **[Pendente] 52.2 — Teste de Consistência:** Criar teste de integração para validar a atualização imediata do ranking após submissão de palpite.

    53. **[Concluído] Auditoria e Correção da Tabela de Jogos:**
    Objetivo: Garantir que todos os jogos da Copa 2026 estejam cadastrados com horários e datas corretos.
    * **[Pendente] 53.1 — Auditoria de Dados:** Comparar o dataset `Copa_do_Mundo_2026_Fase_de_Grupos_Completa_Brasilia.xlsx` com o site oficial da FIFA e identificar discrepâncias.
    * **[Pendente] 53.2 — Correção de Insumos:** Atualizar `src/main/resources/database/copa-2026-data.sql` e tabelas do banco com horários corretos (ex: 21:30).
    * **[Pendente] 53.3 — Validação de UI:** Confirmar que a opção '21:30' aparece nos dropdowns de inclusão/edição de jogos.

    54. **[Pendente] Correção de Renderização de Placar 0x0:**
        Objetivo: Garantir que placares 0x0 sejam exibidos corretamente na página de resumos (o campo não deve ficar vazio).
        * **[Pendente] 54.1 — Auditoria de Renderização:** Investigar por que `c:out` ou `c:if` está tratando `0` como `null/empty` no JSPF.
        * **[Pendente] 54.2 — Implementar Fix:** Ajustar a expressão EL para verificar explicitamente se o valor é `null` (ex: `test="${jogo.golsEquipe1 != null}"`).
        * **[Pendente] 54.3 — Validação:** Testar a renderização de jogos encerrados com placar 0x0 na tela principal.

    55. **[Pendente] Otimização Arquitetural e Melhoria do Startup:**
        Objetivo: Resolver avisos de inicialização de beans, remover configurações obsoletas e reduzir o tempo de startup.
        * **[Pendente] 55.1 — Resolver BeanPostProcessorChecker:** Refatorar `applicationContext-*.xml` para evitar inicialização precoce de beans (Segurança/JPA).
        * **[Pendente] 55.2 — Limpeza de Configurações:** Remover dialeto explícito do Hibernate e ajustar configurações de compressão do Tomcat.
        * **[Pendente] 55.3 — Validação Arquitetural:** Garantir que transações e segurança continuam funcionando após o reordenamento.

    56. **[Concluído] Modernização do Gráfico de Desempenho (Client-side Rendering):**
        Objetivo: Substituir JFreeChart (estático/lento) por biblioteca JS moderna (ApexCharts/Chart.js) para performance e interatividade.
        * **[Concluído] 56.1 — Limpeza de UI:** Remover botão `grafico_submit` e atualizar layout CSS para identidade Copa 2026.
        * **[Pendente] 56.2 — Implementar Componente JS:** Integrar biblioteca de gráficos no `graficoDesempenho.jsp` e consumir JSON.
        * **[Pendente] 56.3 — Cleanup Legado:** Remover `gerarGraficoDesempenhoImagem` e classes de suporte JFreeChart.

    ---

1. **[Concluído] Registro e Planejamento:** Formalizar a nova estratégia de build.
   * **[Concluído]** Registrar `ADR-20260608-otimizacao-build-docker-multi-stage.md`.
   * **[Concluído]** Criar tarefas detalhadas no plano de evolução.

2. **[Concluído] Iteração 1: Otimização do Contexto:**
   * **[Concluído]** Criar `.dockerignore` rigoroso para evitar envio de `target/` e `node_modules/`.

3. **[Concluído] Iteração 2: Refatoração do Dockerfile:**
   * **[Concluído]** Implementar Multi-stage Build (Frontend vs Backend).
   * **[Concluído]** Adicionar BuildKit Cache Mounts para `.m2` e `.npm`.

4. **[Concluído] Iteração 3: Ajustes no Maven:**
   * **[Concluído]** Garantir que o `pom.xml` suporte a propriedade `frontend.skip`.

5. **[Concluído] Iteração 4: Gestão de Segredos (NVD API Key):**
   * **[Concluído]** Registrar `ADR-20260608-gestao-segredos-nvd-api-key.md`.
   * **[Concluído]** Atualizar `Dockerfile` para usar `--mount=type=secret`.
   * **[Concluído]** Configurar `docker-compose.yml` com segredos locais.
   * **[Concluído]** Instruir criação do arquivo `.nvd_api_key` localmente (Confirmado pelo usuário).

### Fase 2.5: Auditoria e Ajuste do Frontend (ALTA PRIORIDADE)

1. **[Concluído] Auditoria Visual Completa:** Testar renderização e funcionalidade de todas as telas principais (login, dashboard, formulários, gráficos, admin) em navegadores modernos e múltiplas resoluções. Concluir esta etapa antes de iniciar novas otimizações.
   * **Evidências:** Sessão `.ia/logs/session-20260219-auditoria-visual-validacao-telas.md` registrou verificações via Docker (HTTP 200 nas páginas autenticadas, gráficos JFreeChart gerando PNGs válidos e RBAC retornando 403 para usuários sem papel ADMIN). Login público `login.jsp` ativo.
   * **Achados:** `cadastro.jsp` responde 302 redirecionando para `/login.jsp` devido à ausência de `permitAll` na configuração de segurança; sugerir ajuste específico antes de reabrir cadastros. Prototype/Scriptaculous continuam carregados no `cabecalho.jspf`, alinhado às tarefas 2 e 3.
   * **Limitações:** Auditoria cURL não substitui testes visuais responsivos; execução em navegadores reais permanece recomendada após higienização de scripts/CSS.
2. **[Concluído] Inventário e Análise de Scripts:** Mapear todos os arquivos JavaScript, identificar dependências e decidir manter/refatorar/remover cada um. Resultado alimentará as tarefas 3 e 4.
   * **Concluído (19/02/2026):** Inventário e análise concluídos (`.ia/logs/session-20260219-inventario-scripts-fase-2-5.md`). Bibliotecas legadas identificadas: Prototype/Scriptaculous (`webapp/js/prototype.js`, `scriptaculous.js`, `effects.js`), DWR (engine/util + interfaces geradas), Overlib (`overlib.js`) e BrowserDetector.
   * **Próximas subtarefas:**
     1. Documentar plano de substituição de Prototype/Scriptaculous por HTMX/Fetch e CSS transitions, alinhado com a remoção de dependências DWR. **Status:** Concluído em 19/02/2026 (`.ia/planos/plano-migracao-dwr-htmx.md`).
     2. Especificar migração dos fluxos críticos DWR (`webapp/seguro/jogos.jsp`, `webapp/admin/participantes.jsp`) para endpoints Struts REST + HTMX, incluindo impacto nas tags Struts. **Status:** Plano consolidado (19/02/2026); aguarda execução das tarefas derivadas.
     3. Selecionar alternativa moderna ao Overlib (ex.: Tippy.js) e planejar substituição dos tooltips com requisitos de acessibilidade. **Status:** Concluído (19/02/2026) – opção definida (Tippy.js v6) conforme `.ia/logs/session-20260219-avaliacao-tooltips-tippy.md`.
     4. Validar ausência de uso do `BrowserDetector.js`, propor remoção e adoção de feature detection (`@supports`, Modernizr slim se necessário). **Status:** Concluído (19/02/2026) – ver `.ia/logs/session-20260219-avaliacao-browserdetector.md`.
     5. Reavaliar dependência do `jquery-4.0.0.min.js` (versão alfa) e definir downgrade para 3.7.1 ou remoção completa após migrar interações restantes. **Status:** Concluído (19/02/2026) – ver `.ia/logs/session-20260219-avaliacao-jquery.md` e ADR `.ia/historico/ADR-20260219-jquery-remocao-gradual.md`. A dependência foi completamente eliminada em `session-20260219-remocao-jquery.md`.
     6. **[Concluído]** Propor adoção de bundler (Vite/ESBuild) para modularizar scripts, permitir CSP rígida e preparar minificação/versões com hash. Plano criado em 20/02/2026 (`.ia/planos/plano-bundler-frontend.md`) com log de sessão `.ia/logs/session-20260220-plano-bundler-frontend.md`. Estrutura inicial (`package.json`, `vite.config.js`, `src/frontend/`) e bundle fallback documentados em `.ia/logs/session-20260220-bundler-setup-parcial.md`. **Atualizações 20/02/2026:** `package.json` recebeu `engines`/`packageManager` e `pom.xml` foi configurado com `frontend-maven-plugin` (fase `generate-resources`, `frontend.skip=true` por padrão). `vite.config.js` agora emite bundles com hash, gera manifest (`webapp/assets/.vite/manifest.json`), mantém fallback `app-bundle.js` via plugin custom e o loader em `webapp/template/cabecalho.jspf` consome dinamicamente o manifest com degradação segura. Build local (`npm install && npm run build`) finalizado com sucesso; há 2 vulnerabilidades moderadas sinalizadas pelo npm para acompanhamento (`npm audit`). Referências log: `.ia/logs/session-20260220-bundler-pipeline-config.md`, `.ia/logs/session-20260220-bundler-manifest-config.md`, `.ia/logs/session-20260220-bundler-manifest-loader.md`, `.ia/logs/session-20260220-bundler-build-validacao.md`, `.ia/logs/session-20260220-bundler-hash-config.md`.
     7. Mapear condicionais e estilos específicos para Internet Explorer (ex.: `opendev:isIE`, hacks CSS) e planear remoção, garantindo compatibilidade apenas com navegadores suportados oficialmente. **Concluído (19/02/2026):** Inventário e limpeza executados (`.ia/logs/session-20260219-inventario-condicionais-ie.md`, `.ia/logs/session-20260219-remocao-condicionais-ie.md`); JSPs e CSS atualizados para uso de layout neutro e `opacity`.
3. **[Concluído] Remoção de Prototype e Scriptaculous (Sequência após Tarefa 2):** Eliminar bibliotecas legadas (Prototype.js, Scriptaculous.js) do projeto, migrando funcionalidades restantes para HTMX/JavaScript nativo.
   * **2026-02-20:** [Concluído] Tela `admin/participantes.jsp` convertida para HTMX, removendo chamadas DWR/Prototype para alteração de papel, autorização e exclusão; ver `.ia/logs/session-20260220-remocao-dwr-admin-participantes.md`.
   * **2026-02-20:** [Concluído] Painel “Meus palpites” (`seguro/jogos.jsp`) agora carrega via HTMX e scripts nativos, substituindo `DWRUtil` e `Effect` para essa funcionalidade; ver `.ia/logs/session-20260220-remocao-dwr-palpites-htmx.md`.
   * **2026-02-20:** [Concluído] Popups de palpites em `seguro/jogos.jsp` migrados para HTMX/fetch, substituindo DWR/Prototype e adicionando endpoints Struts para listagem/atualização; ver `.ia/logs/session-20260220-remocao-dwr-jogos-popup.md`.
   * **2026-02-20:** [Concluído] Cadastro administrativo de jogos (`admin/inclusaoJogo.jsp`) convertido para fetch/DOM nativo com endpoints Struts e remoção de DWR/Prototype; ver `.ia/logs/session-20260220-remocao-dwr-admin-inclusao-jogo.md`.
   * **2026-02-20:** [Concluído] Cadastro público (`cadastro.jsp`) reescrito para DOM nativo; libs DWR (`engine.js`, `util.js`) removidas do `cabecalho.jspf`; ver `.ia/logs/session-20260220-remocao-dwr-cadastro-publico.md`.
   * **2026-02-20:** [Concluído] Remoção completa do DWR servlet e dependências: `webapp/WEB-INF/web.xml` sem `dwr-invoker`, dependência `org.directwebremoting:dwr` excluída do `pom.xml` e build validado com `mvn test`; ver `.ia/logs/session-20260220-remocao-dwr-servlet.md`.
   * **2026-02-20:** [Parcial] Inventário inicial aponta ausência de código ativo do Cewolf, mas mantém bloco comentado da dependência no `pom.xml` e metadados `webapp/WEB-INF/lib/CVS`; registrar subtarefas para limpeza definitiva. Referência log: `.ia/logs/session-20260220-inventario-legados-pos-dwr.md`.
   * **2026-02-20:** [Concluído] Limpar bloco comentado da dependência Cewolf no `pom.xml` e ajustar comentários correlatos após validação de build; ver commit `refactor: remover menções a cewolf do pom`.
   * **2026-02-20:** [Concluído] Remover diretório legado `webapp/WEB-INF/lib/CVS` do repositório garantindo que nenhum artefato herdado de CVS permaneça; ver `.ia/logs/session-20260220-remocao-cvs-legado.md`.
   * **2026-02-20:** [Concluído] Remoção das bibliotecas Prototype/Scriptaculous: removidas referências no `cabecalho.jspf` e excluídos `webapp/js/prototype.js`, `webapp/js/scriptaculous.js`, `webapp/js/effects.js`; ver `.ia/logs/session-20260220-remocao-prototype-scriptaculous.md`.
   * **2026-02-20:** [Concluído] Migração dos tooltips legacy: removidos `webapp/js/overlib.js` e `webapp/js/BrowserDetector.js`, adicionada infraestrutura nativa em `webapp/js/tooltips.js` integrada ao HTMX e aplicada aos cabeçalhos de `seguro/classificacao.jsp` via `data-tooltip`.
   * **2026-02-20:** [Concluído] Remoção residual do DWR na segurança: interceptadores `/dwr/**` retirados de `applicationContext-security.xml` e excluído o arquivo `webapp/WEB-INF/dwr.xml` para evitar cargas indevidas.
4. **[Concluído] Auditoria e Refatoração CSS (Sequência após Tarefa 3):** Revisar `estilo.css`, remover hacks legados, reorganizar por componentes e implementar responsividade básica. Inventário final em 20/02/2026 confirmou ausência de estilos inline remanescentes via `rg "style=\"" webapp`; registros consolidados em `.ia/logs/session-20260219-auditoria-css.md` e ADR `.ia/historico/ADR-20260219-refatoracao-css.md`.
   4.1 **[Concluído] Modernização do HTML (Sequência da Tarefa 4):** Higienizar marcação JSP/HTML removendo atributos obsoletos (`align`, `cellpadding`, `width`, etc.), migrar estrutura de tabelas puramente visuais para classes utilitárias responsivas, padronizar uso de `aria-*` e preparar componentes para interações HTMX pós-remoção de Prototype/DWR. Registrar subtarefas por módulo (público, seguro, admin) e validar cada ajuste com `mvn test`. *Skill prevista:* N/A (refinamento frontend estruturado).
   * **2026-02-20:** Removidos atributos legados e adicionados wrappers responsivos nas páginas `login.jsp`, `cadastro.jsp`, `admin/inclusaoJogo.jsp`, `admin/participantes.jsp` e `seguro/jogos.jsp`; tabelas passaram a usar classes utilitárias e `mvn test` validou 5 cenários.
   * **2026-02-20:** Adicionados atributos `scope` às tabelas de `seguro/principal.jsp`, `seguro/classificacao.jsp` e `seguro/jogos.jsp`, reforçando acessibilidade e mantendo compatibilidade após `mvn test`.
   * **2026-02-20:** Formulários de `login.jsp`, `cadastro.jsp` e `admin/inclusaoJogo.jsp` migrados de tabelas para `form-grid` responsivo com novos utilitários CSS (`form-row`, `form-field-group`), preservando integrações DWR/HTMX e confirmando estabilidade via `mvn test`.
   * **2026-02-20:** Inventário final confirmou ausência de estilos inline/atributos legados nas JSPs restantes, permitindo concluir a subtarefa e avançar para a Tarefa 3.
5. **[Concluído] Migração do Cewolf (Gráficos):** Substituir o Cewolf por geração de gráficos com JFreeChart direto (server-side) ou Chart.js (client-side), removendo todas as dependências e taglibs legadas.
   * **[Concluído] Inventário de Uso:** Mapear páginas e tags `<cewolf:*>` (ex: `webapp/seguro/principal.jsp`, `webapp/seguro/graficoDesempenho.jsp`) e identificar dados necessários para cada gráfico.
   * **[Concluído] Implementação de Renderização:** Criar geradores de gráficos em Java (JFreeChart) e expor endpoints/Actions para servir PNG/SVG (ex: `/seguro/graficoLideranca.png`, `/seguro/graficoDesempenho.png`).
   * **[Concluído] Atualização das JSPs:** Substituir `<cewolf:chart>`/`<cewolf:img>` por `<img>` apontando para os novos endpoints e remover dependências do Cewolf nas telas.
   * **[Concluído] Remoção de Taglibs:** Remover `<%@taglib prefix="cewolf" ... %>` do `cabecalho.jspf` e das JSPs.
   * **[Concluído] Remoção Residual (Deploy/Cache):** Garantir que o `cabecalho.jspf` sem Cewolf seja aplicado no WAR/ROOT do Tomcat (rebuild/redeploy) e remover cache/artefatos que ainda referenciam `cewolf.tld`, desbloqueando `login.jsp`/`index.jsp`. Referência Log: `.ia/logs/session-20260219-remocao-cewolf-deploy-cache.md`
   * **[Concluído] Validação Funcional:** Testes automatizados (`GraficosJFreeChartTest`, `ParticipanteActionTest`) seguem verdes e, em 19/02/2026, a validação manual via Docker confirmou retorno HTTP 200 dos endpoints `/seguro/graficoLiderancaImagem.action` e `/seguro/graficoDesempenhoImagem.action` com PNGs válidos (assinatura `89 50 4E 47`). Referência Log: `.ia/logs/session-20260219-validacao-graficos-jfreechart-v2.md`. Skill: `modernization-java-migration v1.0.0`. Observação: `mvn test` executado integralmente em 19/02/2026 validou 5 cenários sem falhas após restabelecimento do Nexus TSE.
     Referência Log: `.ia/logs/session-20260219-migracao-cewolf-continuacao.md`, `.ia/logs/session-20260219-validacao-graficos-jfreechart-v2.md`
6. **[Concluído] Otimização de Performance (Fase 2.5 Tarefa 6):** Avaliação concluída em 20/02/2026. Bundles gerados pelo Vite permanecem <10 KB (gzip ~2.6 KB) e o CSS consolidado ~19 KB (gzip 4.1 KB); decidiu-se por manter a estratégia mínima (hashing + manifest + fallback) e aplicar ajustes de cache somente quando necessários. Referência ADR: `.ia/historico/ADR-20260220-otimizacao-minima-assets.md`. Inventário registrado em `.ia/logs/session-20260220-otimizacao-performance-inventario.md`. Futuras otimizações devem ser reavaliadas caso o volume de assets cresça significativamente.
7. **[Adiada] Auditoria de Acessibilidade:** Inventário inicial realizado em 20/02/2026 com foco em WCAG 2.1 AA. Correções prioritárias aplicadas em 20/02/2026 (alt descritivo, IDs únicos, diálogos acessíveis, landmarks). Execução do `axe` bloqueada no ambiente (logs `.ia/logs/session-20260220-acessibilidade-correcoes.md`, `.ia/logs/session-20260220-axe-cli-bloqueio.md`). Tarefa adiada até que exista ambiente externo com Chrome headless liberado; retomar após concluir as demais tarefas da fase.
8. **[Adiada] Testes de Compatibilidade Cross-Browser:** Validar funcionamento em Chrome, Firefox, Edge e Safari (desktop e mobile). Atividade dependente da conclusão da auditoria automatizada de acessibilidade; retomar após obter ambiente com suporte a Chrome headless e encerrar a Tarefa 7.
9. **[Concluído] Documentação Frontend:** Documento `.ia/diretrizes/frontend.md` criado em 20/02/2026 consolidando padrões (estrutura do bundler, HTMX, CSS utilitário, acessibilidade e fluxo de build). Referência log: `.ia/logs/session-20260220-acessibilidade-correcoes.md` (status atualizado) e esta sessão.
10. **[Adiada] Validação Final e Sign-off:** Checklist completo de qualidade frontend aguarda conclusão das tarefas 7 e 8 (auditoria axe + testes cross-browser). Retomar após execução em ambiente com Chrome headless disponível.

Referência ADR: `.ia/historico/ADR-20260217-fase-auditoria-frontend.md`
Referência Plano: `.ia/planos/plano-fase-2.5-auditoria-frontend.md`

11. **[Concluído] Renderização das Bandeiras via PNG:** Alinhar exibição das bandeiras às imagens em `webapp/img/bandeiras/`, mantendo fallback acessível. Plano detalhado em `.ia/planos/plano-correcao-bandeiras-e-dados.md`.
    * **[Concluído]** Corrigir assets específicos (Chile/França) garantindo download atualizado dos PNGs e regeneração opcional via script. **22/02/2026:** PNGs recriados programaticamente (`fr.png`, `cl.png`) com cores oficiais e validação via `FlagUtilsTest`. Log: `.ia/logs/session-20260222-bandeiras-charset-assets.md`.
    * **[Concluído]** Normalizar codificação/acentuação nas JSPs e configuração web (UTF-8 end-to-end) eliminando ocorrências como “FranÃ§a”. **22/02/2026:** Aplicado `CharacterEncodingFilter`, atualizado `webwork.i18n.encoding` para UTF-8, JSPs com `pageEncoding` e JDBC com `characterEncoding=utf8mb4`. Log: `.ia/logs/session-20260222-bandeiras-charset-assets.md`.
    * **[Concluído]** Reprocessar a base de equipes/jogos a partir da planilha `data/Copa_do_Mundo_2026_Fase_de_Grupos_Completa_Brasilia.xlsx`, gerando SQL/CSV atualizados e aplicando ao banco local. **22/02/2026:** Scripts executados com truncates automáticos e carga via `mysql --default-character-set=utf8mb4`. Logs: `.ia/logs/session-20260222-copa2026-dataset-atualizado.md`, `.ia/logs/session-20260222-dataset-reload.md`.
    * **[Concluído]** Corrigir grafia e asset da França, garantindo nome UTF-8 e `fr.png` ativo nas views. **22/02/2026:** Dataset recarregado com UTF-8, validado via consulta SQL; asset PNG permanece disponível. Log: `.ia/logs/session-20260222-dataset-reload.md`.
    * **[Concluído]** Atualizar o contexto `locais` do `web.xml` com os nomes das cidades-sede oficiais, mantendo alinhamento com o calendário e registrando testes. **22/02/2026:** `webapp/WEB-INF/web.xml` atualizado com 16 cidades (Dallas → Los Angeles); `mvn -q -Dfrontend.skip=true test` executado (Log4j warning conhecido). Log: `.ia/logs/session-20260222-locais-webxml-cidades.md`.
    * **[Concluído]** Validar todas as views que exibem bandeiras (seguro/admin) após atualização dos dados. **22/02/2026:** Validação visual confirmada pelo usuário sem linhas pendentes. Log: `.ia/logs/session-20260222-bandeiras-validacao-final.md`.
    * **[Concluído]** Executar testes automatizados, rebuild Docker, smoke manual autenticado e atualizar evidências em `telas/`. **22/02/2026:** `mvn -q -Dfrontend.skip=true test` + `docker compose build app && docker compose up -d app` executados; resposta do `login.action` validada via `curl`. Logs: `.ia/logs/session-20260222-bandeiras-charset-assets.md`, `.ia/logs/session-20260222-bandeiras-rebuild-final.md`.
    * **[Concluído (25/02/2026)]** Atualizar versão exibida para `0.2.2-SNAPSHOT` após rebuild completo (`npm run build`, `mvn clean package -Dfrontend.skip=false`, `docker compose build app`, `docker compose up -d app`). UI confirma novo número e timestamp. Log: `.ia/logs/session-20260225-autorizacao-dropdown-htmx-correcao.md`.
    * **[Concluído (25/02/2026 18:51)]** Versão atualizada para `0.2.3-SNAPSHOT` após instrumentação HTMX (pipeline completo + deploy). Evidência: `.ia/logs/session-20260225-versao-0-2-3-deploy.md`.
12. **[Concluído] Exibição da versão do sistema no frontend:** Expor número da versão e timestamp de build nas telas autenticadas. Plano detalhado em `.ia/planos/plano-versao-interface.md`.
    * **[Concluído]** Inventariar fontes de versão (`pom.xml`, `build.properties`) e definir estratégia única de geração (`version.properties`). **22/02/2026:** Maven configurado para filtrar `version.properties` com `${project.version}` e timestamp. Log: `.ia/logs/session-20260222-versao-ui-implementacao.md`.
    * **[Concluído]** Implementar utilitário/bean (`BuildInfoProvider`) fornecendo versão e horário para as views (Struts/Spring). **22/02/2026:** Classes `BuildInfo`/`BuildInfoProvider` criadas e registradas no contexto Spring. Log: `.ia/logs/session-20260222-versao-ui-implementacao.md`.
    * **[Concluído]** Atualizar fragments JSP/CSS (`rodape.jspf` ou equivalente) para exibir `Versão X.Y.Z - compilado em DD/MM/YYYY HH:MM`, respeitando acessibilidade e i18n. **22/02/2026:** Mensagem antiga removida, novo bloco `.system-version` aplicado no rodapé com fallback i18n. Log: `.ia/logs/session-20260222-versao-ui-implementacao.md`.
    * **[Concluído]** Corrigir placeholder `${maven.build.timestamp}` no rodapé, aplicando interpolação real e validando timezone. **22/02/2026:** `BuildInfo` passou a detectar placeholders, adicionando fallback baseado no artefato; `version.properties` filtrada com `${build.timestamp}`. Log: `.ia/logs/session-20260222-versao-ui-placeholder-fix.md`.
    * **[Concluído]** Cobrir com testes (unitários/integrados) e evidência visual após rebuild Docker, registrando log específico. **22/02/2026:** Teste `BuildInfoProviderTest` adicionado, `mvn -q -Dfrontend.skip=true test` executado e ambiente Docker reconstruído (`docker compose build app && docker compose up -d app`). Evidência visual pendente para próxima sessão. Logs: `.ia/logs/session-20260222-versao-ui-implementacao.md`, `.ia/logs/session-20260222-versao-ui-placeholder-fix.md`.
    * **[Concluído]** Registrar evidência visual do rodapé pós-correção; validação confirmada pelo usuário em 23/02/2026 (Skill: `modernization-java-migration v1.0.0`). (Plano: `.ia/planos/plano-correcao-versao-bandeiras-dados.md`)

13. **[Concluído] Correção do layout em `/admin/jogos.action`:** Eliminar linhas vazias entre as partidas e o rodapé, conforme evidência `telas/Erro-desing-tela.png`. Plano: `.ia/planos/plano-layout-admin-jogos.md`.
    * **[Concluído]** Diagnosticar markup e CSS envolvidos (`webapp/WEB-INF/content/seguro/jogos.jsp`, `webapp/css/estilo.css`), identificando `<div>` não fechados que geravam linhas vazias. Log: `.ia/logs/session-20260222-layout-admin-jogos-ajuste.md`.
    * **[Concluído]** Implementar ajustes no JSP/CSS garantindo acessibilidade e compatibilidade com HTMX/Struts. **22/02/2026:** Portlet passa a fechar corretamente e mantém espaçamento controlado. Log: `.ia/logs/session-20260222-layout-admin-jogos-ajuste.md`.
    * **[Concluído]** Sem mudanças em assets frontend; executado `mvn -q -Dfrontend.skip=true test` para validar backend. Log: `.ia/logs/session-20260222-layout-admin-jogos-ajuste.md`.
    * **[Concluído]** Rebuild Docker (`docker compose build app` / `docker compose up -d app`) e atualização do banco para exibir cidades (consulta `JOG_JOGO`). Log: `.ia/logs/session-20260222-layout-admin-jogos-ajuste.md`.

14. **[Concluído] README de Migração Consolidado (23/02/2026):** Criar `README-migracao.md` com panorama atual do projeto. Plano: `.ia/planos/plano-readme-migracao.md`. Evidências: `.ia/logs/session-20260223-readme-migracao-execucao.md`, `README-migracao.md`.
    * **[Concluído]** Inventariar fontes (README, `analise-inicial.md`, ADRs, logs, planos) e registrar achados (ver log 23/02/2026).
    * **[Concluído]** Definir estrutura do documento para públicos de negócio e técnicos.
    * **[Concluído]** Redigir o conteúdo completo, incluindo status de fases, riscos e próximos passos (arquivo `README-migracao.md`).
    * **[Concluído]** Revisar, atualizar rastreabilidade (`passo-a-passo.md`, logs) e publicar documento final.

15. **[Pendente] Remediação de Vulnerabilidades (Dependency-Check):** Avaliar e mitigar CVEs críticos reportados na execução do OWASP Dependency-Check. Plano: `.ia/planos/plano-remediacao-dependency-check.md`.
    * **[Concluído]** Coletar/validar relatório completo do Dependency-Check e confirmar eventuais falsos positivos (23/02/2026).
    * **[Concluído]** Analisar cada dependência afetada (Angus/Jakarta Mail, Commons FileUpload, JFreeChart, Protobuf, Quartz, Spring, Struts) identificando versões corrigidas e impacto (23/02/2026).
    * **[Concluído]** Definir estratégia de remediação e priorização (matriz de ações, riscos, esforço) com recomendações de upgrade documentadas (23/02/2026).
    * **[Concluído]** Atualizar plano `.ia/planos/plano-remediacao-dependency-check.md` com matriz de remediação e cronograma (23/02/2026).
    * **[Pendente]** Preparar upgrade Struts 7.1.1 + `commons-fileupload2` ≥ 2.0.0-M4 (avaliar breaking changes, ajustar dependências e planejar testes). (Skill: `modernization-java-migration v1.0.0`)
        * **23/02/2026:** `pom.xml` atualizado (Struts 7.1.1, `commons-fileupload2` M4, `commons-lang3` 3.18.0, `commons-text` 1.12.0). `mvn -q -Dfrontend.skip=true test` executado com sucesso após disponibilização do artefato no espelho `nx-mvn.tse.jus.br`.
        * **23/02/2026:** Nova execução do Dependency-Check (`mvn -Dfrontend.skip=true org.owasp:dependency-check-maven:check`) falhou com CVEs remanescentes (Angus Mail 2.0.3, JFreeChart 1.5.4, Quartz 2.3.2, Protobuf 3.25.1, Spring 6.1.4). Próximo passo: avançar para os upgrades planejados (Spring 6.1.14, Angus 2.0.4, Quartz 2.5.2, JFreeChart 1.5.6, Protobuf 3.25.5) antes de repetir o scan e realizar o smoke `/admin/*.action`.
    * **[Pendente]** Planejar upgrade Spring Framework 6.1.14 alinhando compatibilidade com Spring Security 6.2.2 e camada Struts.
        * **23/02/2026:** Plano detalhado adicionado em `.ia/planos/plano-remediacao-dependency-check.md` (seção “Plano Detalhado – Spring Framework 6.1.14”) com etapas de ajuste do `pom.xml`, testes (`mvn test`, smoke login/logout, `/admin/*.action`) e reexecução do Dependency-Check pós-upgrade.
        * **23/02/2026:** Upgrade concluído após disponibilização do BOM 6.1.14 no repositório corporativo; `mvn -q -Dfrontend.skip=true test` executado com sucesso (avisos de APIs depreciadas pendentes de saneamento). Dependency-Check ainda falha pelos CVEs remanescentes (Angus Mail 2.0.3/2.0.2, JFreeChart 1.5.4, Quartz 2.3.2, Protobuf 3.25.1). Log: `.ia/logs/session-20260223-spring-upgrade-parada.md`.
    * **[Pendente]** Planejar atualização do stack Angus Mail/Activation para 2.0.4, revisando configurações SMTP.
        * **23/02/2026:** Tentativa de upgrade para `org.eclipse.angus:jakarta.mail` 2.0.4 bloqueada; artefatos ainda não disponíveis no repositório `https://nx-mvn.tse.jus.br`. Versão revertida para 2.0.3 para manter o build íntegro. Log: `.ia/logs/session-20260223-angus-upgrade-parada.md`.
    * **[Pendente]** Orquestrar upgrades complementares (Quartz 2.5.2, JFreeChart 1.5.6, Protobuf 3.25.5, Log4j 2.25.3, Commons Lang 3.18.0, esbuild ≥ 0.25.0) com respectivos testes.
        * **23/02/2026:** Quartz 2.5.2 indisponível no `nx-mvn.tse.jus.br`; upgrade adiado, mantendo versão 2.3.2. Log: `.ia/logs/session-20260223-quartz-upgrade-parada.md`.
        * **23/02/2026:** Protobuf atualizado para 3.25.5 via `dependencyManagement`; testes OK e dependency-check sem alertas para Protobuf. Log: `.ia/logs/session-20260223-protobuf-upgrade.md`.
        * **23/02/2026:** JFreeChart atualizado para 1.5.6; dependency-check não lista mais CVEs do componente. Log: `.ia/logs/session-20260223-jfreechart-upgrade.md`.
    * **[Pendente]** Planejar execução (subtarefas por dependência, testes necessários, documentação).
    * **[Pendente]** Implementar atualizações/testes e gerar novo relatório OWASP com status final.

16. **[Concluído] Evolução README de Migração 2026 (23/02/2026):** Expandir e aprofundar o `README-migracao.md` com foco em negócio, arquitetura e operação para desenvolvedores. Plano: `.ia/planos/plano-evolucao-readme-migracao-2026.md`. Evidências: `README-migracao.md`, `.ia/logs/session-20260223-readme-migracao-planejamento-v2.md`, `.ia/logs/session-20260223-readme-migracao-execucao-v2.md`.
    * **[Concluído]** Inventariar conteúdo existente (README atual, `analise-inicial.md`, ADRs, logs) identificando lacunas relevantes para público técnico.
    * **[Concluído]** Propor nova estrutura do documento cobrindo visão de negócio, arquitetura, dados da Copa 2026, operação e governança.
    * **[Concluído]** Enriquecer conteúdo com fluxos de negócio, modelos de dados, operações e troubleshooting sem perda das seções atuais.
    * **[Concluído]** Validar alinhamento com diretrizes e artefatos (`passo-a-passo.md`, planos, ADRs), apontando referências explícitas.
    * **[Concluído]** Atualizar governança: versionar documento anterior se necessário, registrar log e revisar rastreabilidade.
    * **[Concluído]** Revisar e submeter para validação final com stakeholders (negócio e engenharia).
    * **[Concluído] 24/02/2026** Adicionar diagramas Mermaid e fluxos operacionais (navegação, palpites, autenticação) ao `README-migracao.md`, além da tabela de referência das ações administrativas. Evidência: `.ia/logs/session-20260224-readme-migracao-fluxos.md`. (Skill: N/A)
    * **[Concluído] 04/03/2026** Executar o plano `.ia/planos/plano-enriquecimento-readme-migracao.md`, adicionando mapa de componentes, fluxo end-to-end, visão HTMX, tabela de segurança multicamadas, observabilidade, pipeline Quartz, tabela de parâmetros críticos e roadmap arquitetural 2026-03. Evidências: `README-migracao.md`, `.ia/logs/session-20260304-readme-migracao-arquitetura.md`. Skill: N/A (nenhuma skill aplicável).

17. **[Concluído] Revisão da Política de Senhas (23/02/2026):** Permitir caracteres especiais seguros e alinhar mensagens ao usuário. Plano: `.ia/planos/plano-politica-senhas-20260223.md`.
    * **[Concluído]** Validações backend atualizadas (`ValidacaoUtils`, `ParticipanteAction`, `Participante`) para aceitar senhas de 8–64 caracteres com símbolos seguros e bloquear caracteres de controle (Skill: `modernization-java-migration v1.0.0`).
    * **[Concluído]** Mensagens de erro, tooltip do cadastro e documentação internacionalizada ajustadas para incentivar o uso de caracteres especiais.
    * **[Concluído]** Testes automatizados cobrindo as novas regras (`ValidacaoUtilsTest`) executados com `mvn -q -Dfrontend.skip=true test` e frontend rebuild (`npm run build`). Evidência registrada em `.ia/logs/session-20260223-politica-senhas-sanitizacao-execucao.md`.

18. **[Concluído] Sanitização Unificada das Entradas de Cadastro (23/02/2026):** Reforçar limpeza de login/nome/e-mail contra HTML/SQL. Plano: `.ia/planos/plano-sanitizacao-cadastro-20260223.md`.
    * **[Concluído]** Sanitização reforçada nos setters do modelo (`Participante`) e serviço (`ParticipanteServiceImpl`) com bloqueio de HTML e normalização consistente (Skill: `modernization-java-migration v1.0.0`).
    * **[Concluído]** Sanitização preventiva adicionada no frontend (`formSanitizer.js`) e testes unitários ampliados (`ParticipanteTest`) garantindo cobertura do fluxo.

19. **[Concluído] Modernização do Envio de E-mails (23/02/2026):** Atualizar o cliente SMTP para suportar TLS/STARTTLS, autenticação e configuração externa segura. Plano: `.ia/planos/plano-modernizacao-email-20260223.md`.
    * **[Concluído]** `Email.java` refatorado com `EmailConfiguration`, suporte a TLS/SSL, timeouts e `Session.getInstance`, mantendo autenticação segura (Skill: `modernization-java-migration v1.0.0`).
    * **[Concluído]** Sobreposição de credenciais via arquivo externo e variáveis de ambiente (`SMTP_*`), ajustes no `docker-compose.yml` e propriedades padrão atualizadas.
    * **[Concluído]** Testes automatizados (`EmailConfigurationTest`, `EmailSessionConfigurationTest`) cobrindo hierarquia de configuração e montagem do `MailContext`; documentação (`README-migracao.md`) revisada com instruções de SMTP.

20. **[Concluído (24/02/2026)] Publicar página de Regras do Bolão (24/02/2026):** Disponibilizar a opção “Regras” do menu com conteúdo acessível ao público.
    * Conteúdo das regras consolidado a partir do `README-migracao.md`, estruturando pontuação, prazos e critérios de desempate para visitantes.
    * View `webapp/WEB-INF/content/regras.jsp` criada com portlet e utilitários CSS existentes, garantindo carregamento público sem autenticação.
    * Action `regras` adicionada em `ParticipanteAction`/`struts.xml` com liberação correspondente em `applicationContext-security.xml`; build validado via `mvn -Dfrontend.skip=true test`.
    * `template/menu.jspf` atualizado para usar `c:url` até `/regras.action`, preservando navegação por teclado e atributos ARIA.
    * Documentação (`README-migracao.md`) e mensagens i18n alinhadas; Skill: N/A. Referência log: `.ia/logs/session-20260224-regras-menu.md`.

21. **[Concluído] Correção renderização portlet de Participantes (24/02/2026):** Ajustar declarações de taglibs e include do menu para evitar leakage de diretivas na tela administrativa.
    * **[Concluído]** Declarar taglibs antes do include em `admin/participantes.jsp` e garantir que `template/menu.jspf` contenha as diretivas necessárias.
    * **[Concluído]** Executar `mvn -Dfrontend.skip=true test` e rebuild docker para validar a renderização pós-correção. Evidências: `.ia/logs/session-20260224-participantes-taglibs.md`.
    * **[Concluído]** Higienizar `participantes-rows.jspf` removendo diretivas `<%@taglib ...%>` e garantir que os hosts (`participantes.jsp`, `participantes-table.jsp`) declarem `c`/`fmt` antes do include.
    * **[Concluído]** Validar renderização via menu + htmx após deploy (usuário confirmou ausência de diretivas escapadas; capturar nova evidência limpa na próxima sessão).
22. **[Pendente] Ajustes remanescentes da seed e validações HTMX (24/02/2026):** Ajustar a carga inicial para corrigir o nome do usuário padrão e alinhar a apresentação/armazenamento dos papéis exibidos na tela administrativa.
    * **[Concluído]** Corrigir o nome \"Usuário Teste\" na seed inicial de participantes garantindo encoding UTF-8 consistente.
    * **[Concluído]** Revisar privilégios seminais (`ROLE_*`) garantindo que admin e user possuam papéis adequados e únicos.
    * **[Concluído]** Atualizar a tela administrativa para mapear corretamente os papéis retornados do backend aos níveis exibidos no dropdown.
    * **[Concluído (25/02/2026)]** Executar `mvn -Dfrontend.skip=true test` e smoke pós-ajuste para validar apresentação e atualização HTMX dos papéis. (Skill: `modernization-java-migration v1.0.0`) Resultado: build verde com aviso conhecido do Log4j API ausente; smoke HTMX depende de navegador, manter acompanhamento.
    * **[Concluído (26/02/2026)]** Versão atualizada para `0.2.5-SNAPSHOT` após ajustes de template HTMX, com pipeline completo (`npm install`, `npm run build`, `mvn clean package -Dfrontend.skip=false`, `docker compose build app`, `docker compose up -d app`) e validação via `curl` exibindo o novo número de versão. Log: `.ia/logs/session-20260226-versao-0-2-5-deploy.md`.
    * **[Concluído (26/02/2026)]** Versão atualizada para `0.2.6-SNAPSHOT` após correções de cadastro (duplicidade) e rebuild completo (`npm install`, `npm run build`, `mvn clean package -Dfrontend.skip=false`, `docker compose build app`, `docker compose up -d app`). `curl` confirma “Versão 0.2.6-SNAPSHOT - compilado em 26/02/2026 13:34”. Log: `.ia/logs/session-20260226-versao-0-2-6-deploy.md`.
    * **[Concluído (26/02/2026)]** Versão atualizada para `0.2.7-SNAPSHOT`, com pipeline completo (`npm install`, `npm run build`, `mvn clean package -Dfrontend.skip=false`, `docker compose build app`, `docker compose up -d app`) e validação via `curl` exibindo a nova versão no rodapé. Evidência registrada em `.ia/logs/session-20260226-versao-0-2-7-deploy.md`. (Skill: `modernization-java-migration v1.0.0`)
    * **[Concluído (27/02/2026)]** Versão atualizada para `0.2.8-SNAPSHOT`, com rebuild completo (`npm install`, `npm run build`, `mvn clean package -Dfrontend.skip=false`, `docker compose build app`, `docker compose up -d app`) e validação via `curl` confirmando o rodapé atualizado. Evidência registrada em `.ia/logs/session-20260227-versao-0-2-8-deploy.md`. (Skill: `modernization-java-migration v1.0.0`)
    * **[Concluído (27/02/2026)]** Versão atualizada para `0.2.9-SNAPSHOT`, incluindo ajuste de posição dos balões de palpite com margem/viewport e `z-index`, seguido de pipeline completo (`npm run build`, `mvn clean package -Dfrontend.skip=false`, `docker compose build app`, `docker compose up -d app`). Rodapé confirma a nova versão (27/02/2026 15:57). Log: `.ia/logs/session-20260227-palpites-popup-ajuste.md`. (Skill: `modernization-java-migration v1.0.0`)
    * **[Concluído (27/02/2026)]** Versão atualizada para `0.2.10-SNAPSHOT`, com redesign dos balões (modais centralizados + backdrop), rebuild completo e publicação no Docker. Rodapé exibe “Versão 0.2.10-SNAPSHOT - compilado em 27/02/2026 16:12”. Log: `.ia/logs/session-20260227-palpites-popup-ajuste.md`. (Skill: `modernization-java-migration v1.0.0`)
    * **[Concluído (04/03/2026)]** Versão promovida para `0.2.11` (release) após atualização do `pom.xml`, rebuild completo (`npm install`, `npm run build`, `mvn clean package -Dfrontend.skip=false`) e publicação dos containers (`docker compose build app`, `docker compose up -d app`). Evidência consolidada em `.ia/logs/session-20260304-versao-0-2-11.md`. (Skill: `modernization-java-migration v1.0.0`)
    * **[Em Progresso] Remodelar fluxo de palpites (inline/painel):** Após a migração para `<sec:authorize>` o balão ainda não abre; recomendações UX/Arquitetura definiram a substituição do popup por experiência inline ou painel lateral com botões explícitos e badges de estado. A tarefa permanece ativa para redesenhar o markup/JS sob CSP rígida. Logs: `.ia/logs/session-20260226-correcao-palpites.md`, `.ia/logs/session-20260227-palpites-popup-ajuste.md`, `.ia/logs/session-20260227-palpites-inline-planejamento.md`. Plano dedicado (atualizado): `.ia/planos/plano-correcao-palpites-popup.md`. (Skill: `modernization-java-migration v1.0.0`)
        * **[Conclusão Parcial 26/02/2026] Capturar HTML renderizado e assets:** autenticar via `curl` para `seguro/palpites.action`, validar `data-*` e imports do bundle. HTML arquivado em `telas/palpites-20260226-marcio-rosner.html`; linhas exibem `data-palpite-allowed="true"` para `marcio.rosner`. Resultado indica que o backend fornece dados corretos.
        * **[Conclusão Parcial 26/02/2026] Verificar carregamento do bundle HTMX:** inspeção via `curl` revelou HTTP 403 nos assets; ajuste em `applicationContext-security.xml` liberou `/assets/**`, com testes verdes e nova publicação confirmando HTTP 200. Persistem erros no navegador (`Failed to fetch dynamically imported module: https://localhost:8443/`) apontando para problema no loader.
        * **[Concluído (27/02/2026)] Instrumentar loader e módulo (`palpites.popup` subtarefa 3a):** adicionados logs estruturados (`DEBUG_LABEL`) no loader `cabecalho.jspf`, telemetria em `src/frontend/pages/jogos.js` e flag `window.__bolaoJogosDebug` para acompanhar init/binding do popup. Loader ajustado para usar concatenação segura (sem template literals) e normalização de `BASE_URL`, evitando que o JSP EL remova o conteúdo e garantindo o fetch correto do `manifest.json`/fallback. `npm run build` gerou `main-DNF89Gpv.js` e `mvn -Dfrontend.skip=true test` (24 testes verdes) confirmaram integridade. Log: `.ia/logs/session-20260227-correcao-palpites-popup-instrumentacao.md`, `.ia/logs/session-20260227-csp-loader-ajuste.md`. (Skill: `modernization-java-migration v1.0.0`)
        * **[Concluído (27/02/2026)] Revisar dependências legadas do menu (`palpites.popup` subtarefa 3b):** `menu.jspf` agora usa atributos `data-menu-target`, removeu handlers inline e passa a delegar ao módulo `src/frontend/modules/menuToggle.js`; `main.js` chama `initMenuToggle()` e o CSS exibe focus visível. `npm run build` e `mvn -Dfrontend.skip=true test` executados sem falhas. (Skill: `modernization-java-migration v1.0.0`) Referência Log: `.ia/logs/session-20260227-menu-csp-ajuste.md`.
        * **[Concluído (27/02/2026)] Adequar CSP (subtarefa 3c):** eliminada a dependência de script inline no menu, auditados scripts remanescentes (`template/cabecalho.jspf`, `login.jsp`, `cadastro.jsp`, `admin/inclusaoJogo.jsp`) com nonce `${cspNonce}` e registrado plano de migração para módulos externos/HTMX-safe. (Skill: `modernization-java-migration v1.0.0`) Referências: `.ia/logs/session-20260227-csp-nonce-ajuste.md`, `.ia/logs/session-20260227-menu-csp-ajuste.md`.
        * **[Concluído (27/02/2026)] Mapear scripts inline em fragmentos HTMX (`palpites.popup` subtarefa 3c-1):** verificação confirmou ausência de `<script>` em respostas HTMX (`seguro/jogos.jsp`, `participantes-rows.jspf`); lista consolidada no log `.ia/logs/session-20260227-menu-csp-ajuste.md` para acompanhamento da migração.
        * **[Cancelado] Ajustar posicionamento/visibilidade dos balões (`palpites.popup` subtarefa 3b-1):** solução classificada como transitória; será removida quando a expansão inline/painel entrar em produção.
        * **[Pendente] Validação manual pós-ajustes (subtarefa 3d):** após concluir 3b/3c (remoção de inline scripts e reforço da CSP), refazer rebuild/deploy, limpar cache e registrar comportamento no console (ROLE_USER), coletando evidência do novo fluxo inline.
        * **27/02/2026 18:45:** Execução manual via `curl` identificou HTTP 500 em `/seguro/palpites.action` ao processar `${'match.tip.status.' concat palpiteStatus}`. Ajustado `jogos.jsp`/`palpite-inline-form.jspf` para montar a chave com `c:set`, seguido de `npm run build` + `mvn clean package -Dfrontend.skip=false` + atualização do WAR no Tomcat. Os endpoints HTMX (`palpiteFormPartial`, `atualizarPalpitePartial`) passam a responder 200, porém o retorno continua `text/plain` com o código JSP bruto, impedindo a renderização inline. Necessário revisar o `result` Struts (forward para `.jsp` compilável) antes de concluir a subtarefa. Evidência registrada em `.ia/logs/session-20260227-palpites-inline-validacao.md`.
        * **Ponto de parada:** ajustar o resultado Struts ou a forma de servir os fragments para que `palpiteFormPartial`/`atualizarPalpitePartial` retornem HTML processado; repetir a validação manual após o fix.
        * **01/03/2026 11:42:** Wrappers `.jsp` criados em `WEB-INF/content/seguro/partials/` e `struts.xml` atualizado para utilizá-los. `web.xml` recebeu `jsp-property-group` dedicado, build e redeploy executados. Os fragments agora renderizam, mas ainda incluem o prelude/coda globais (HTML completo). Próximo passo: ajustar o `jsp-property-group` ou mover os wrappers para evitar a inclusão de cabeçalho/rodapé antes de repetir os testes HTMX em navegador.
        * **[Concluído (27/02/2026)] Instrumentar `jogos.js` para confirmar binding:** Telemetria aplicada conforme subtarefa 3a, com registros de `initJogosPage`, binding de linhas e abertura do balão. Coberto no log `.ia/logs/session-20260227-correcao-palpites-popup-instrumentacao.md`. (Skill: `modernization-java-migration v1.0.0`)
        * **26/02/2026 16:52:** Linhas da tabela de jogos voltam a publicar `data-jogo-id` e `data-palpite-allowed` independentemente do papel. Ajuste combina a checagem de `hasAnyRole('USER','ADMIN')` com `Jogo.getPodeDarPalpite()`, reabilitando o binding JS do bundle (`jogos.js`). `mvn -Dfrontend.skip=true test` executado com sucesso; evidência registrada no log `.ia/logs/session-20260226-correcao-palpites.md`. Validação manual em navegador permanece pendente antes de concluir a subtarefa.
        * **[Concluído (27/02/2026)] Alinhamento UX/Negócio (`palpites.popup` subtarefa 4a):** decisão registrada no ADR `.ia/historico/ADR-20260227-palpites-inline-experiencia.md`, definindo expansão inline como padrão e painel lateral para histórico.
        * **[Concluído (27/02/2026)] Protótipo funcional (`palpites.popup` subtarefa 4b):** documentação `.ia/documentacao/prototipo-palpites-inline.md` define markup inline, painel lateral e cenários mobile-first.
        * **[Concluído (27/02/2026)] Refatoração de markup (`palpites.popup` subtarefa 4c):** `webapp/WEB-INF/content/seguro/jogos.jsp` remodelado com linhas expansíveis `match-expand`, colunas de status/ações e painel lateral; CSS (`webapp/css/estilo.css`) atualizado com badges, botões modulados e responsividade; mensagens i18n ampliadas. Builds executados: `npm run build`, `mvn -q -Dfrontend.skip=true test` (aviso Log4j conhecido). Log: `.ia/logs/session-20260227-palpites-markup-refatoracao.md`.
        * **[Em Progresso] Refatoração de scripts (`palpites.popup` subtarefa 4d):** `src/frontend/pages/jogos.js` reorganizado para controlar expansão inline, sincronizar badges/placares e eventos HTMX; próximos passos incluem concluir feedback pós-salvamento e testes integrados. Log: `.ia/logs/session-20260227-palpites-inline-scripts.md`.
        * **[Parcial 01/03/2026]** `ParticipanteAction` passou a marcar `skipTemplate` nas ações HTMX (`listarMeusPalpitesHtmx`, `listarPalpitesDoJogoHtmx`, `carregarPalpiteFormHtmx`, `atualizarPalpiteHtmx`), garantindo que `cabecalho.jspf` ignore prelude/coda nas respostas parciais e permitindo que o HTML inline seja renderizado sem o layout completo. Log: `.ia/logs/session-20260301-palpites-inline-skiptemplate.md`.
        * **[Parcial 01/03/2026]** Pipeline completo executado após os ajustes (npm build, mvn clean package -Dfrontend.skip=false, docker compose build/up) para publicar o WAR atualizado em `bolao-app`. Log: `.ia/logs/session-20260301-palpites-inline-deploy.md`.
        * **[Parcial 01/03/2026]** Corrigida a checagem de papéis em `ParticipanteAction` para aceitar rótulos com e sem prefixo `ROLE_`, restabelecendo `palpitePermitido` nos fragments HTMX. Log: `.ia/logs/session-20260301-palpites-inline-roles.md`.
        * **Próximo passo recomendado:** executar validação manual ROLE_USER/ROLE_ADMIN no fluxo inline (abrir/editar/cancelar/painel lateral) e registrar evidências antes de avançar para a subtarefa 4e.
        * **[Parcial 01/03/2026]** `ParticipanteAction` passou a consultar o `SecurityContextHolder` ao validar papéis (`ROLE_USER`/`ROLE_ADMIN`), garantindo `palpitePermitido` correto nos fragments HTMX. Log: `.ia/logs/session-20260301-palpites-inline-roles-fix.md`.
        * **[Parcial 01/03/2026]** Documentada a regra de encerramento de palpites (fechamento 1h antes do horário oficial) em `.ia/documentacao/palpites-encerramento.md`, com registro em `.ia/logs/session-20260301-palpites-encerramento.md` e referências aos pontos do backend que calculam `palpitePermitido`.
        * **[Parcial 02/03/2026]** Introduzido `PalpiteAuthorizationService` com resultado unificado (status/motivo/permissão), integrado na `ParticipanteAction` e replicado nos fragments JSP/JS (`palpite-inline-form.jspf`, `jogos.jsp`, `src/frontend/pages/jogos.js`) com mensagens i18n específicas para bloqueio por papel ou janela encerrada. Log: `.ia/logs/session-20260302-palpites-authorization-service.md`. Teste: `mvn -Dfrontend.skip=true test` (28 testes, Skill: `modernization-java-migration v1.0.0`).
        * **[Concluído (03/03/2026)]** Instrumentação de `ParticipanteAction.atualizarPalpiteHtmx` e `prepararConteudoPalpite` para registrar cabeçalhos HTMX, parâmetros recebidos, resultado do `PalpiteAuthorization` e resumo da resposta Struts, habilitando coleta de evidências nos logs. Skill: `modernization-java-migration v1.0.0`.
        * **[Parcial (03/03/2026)]** Reproduzir o POST via `curl` (ROLE_USER `palpiteuser`, ROLE_ADMIN `admin`) contra `palpiteFormPartial`/`atualizarPalpitePartial` para `jogoId=1000`; os logs `[HTMX][UPDATE]` registraram `login=null` e `resultado=ERROR motivo=usuarioNaoAutenticado`, explicando a mensagem “Não foi possível carregar o palpite selecionado.” Evidência: `.ia/logs/session-20260303-palpites-inline-reproducao.md`.
        * **[Concluído (03/03/2026)]** Ajustar `RequestUtils.getLoginParticipanteAutenticado()` para usar `SecurityContextHolder` como fallback. Logs `[SEC][HTMX]` confirmaram o principal `palpiteuser` e o POST HTMX voltou a retornar `resultado=success`, restaurando o fluxo inline. Evidência: `.ia/logs/session-20260303-requestutils-seguranca.md`. Skill: `modernization-java-migration v1.0.0`.
        * **[Concluído (03/03/2026)]** Acrescentado `RequestContextFilter` no `web.xml` (logo após o `springSecurityFilterChain`) e ajustado `RequestUtils.getRequest()` para priorizar o `ServletActionContext`, garantindo que `HttpServletRequest#getUserPrincipal()` esteja presente nas chamadas HTMX. As execuções com `curl` (ROLE_USER e ROLE_ADMIN) agora registram `[SEC][HTMX] principal recuperado via HttpServletRequest ...` e o fluxo `atualizarPalpitePartial` finaliza com `resultado=success`. Log consolidado: `.ia/logs/session-20260303-filtros-principal.md`. Skill: `modernization-java-migration v1.0.0`.
        * **[Pendente 02/03/2026] Diagnosticar erro no POST de palpites:** após informar o placar o HTMX retorna fragmento de erro (`match.tip.error.unavailable`). Instrumentar `atualizarPalpiteHtmx`/`prepararConteudoPalpite`, capturar resposta HTMX e revisar o fluxo de atualização para identificar a exceção. Log: `.ia/logs/session-20260302-palpites-inline-erro-salvar.md`.
        * **09/03/2026:** `ParticipanteAction.atualizarPalpiteHtmx` agora retorna `match.tip.locked.timeWindow` quando a janela expira (IllegalStateException), evitando mensagem generica para este caso. Registro no log `.ia/logs/session-20260309-palpites-inline-erro-salvar.md`.
        * **09/03/2026:** `PalpiteServiceImpl.atualizarPalpite` passou a validar janela antes de salvar, evitando gravacao quando o prazo ja encerrou; isso reduz erros e alinha a mensagem retornada pelo fluxo HTMX. Registro no log `.ia/logs/session-20260309-palpites-inline-erro-salvar.md`.
        * **11/03/2026:** `src/frontend/pages/jogos.js` passou a detectar sucesso no salvamento do palpite via HTMX e, se o painel “meus palpites” estiver aberto, recarregá-lo para refletir o novo placar. Log: `.ia/logs/session-20260311-palpites-inline-feedback.md`. (Skill: `modernization-java-migration v1.0.0`)
        * **11/03/2026:** Botões de ações inline/painel agora são desabilitados durante o request HTMX, com `aria-busy` e restauração do estado original ao final do request para evitar cliques duplicados. Log: `.ia/logs/session-20260311-palpites-inline-trigger-busy.md`. (Skill: `modernization-java-migration v1.0.0`)
        * **06/03/2026:** ParticipanteAction.atualizarPalpiteHtmx foi normalizado e agora sempre preenche palpiteErro com a mensagem de indisponibilidade em casos de erro (login nulo, parametros invalidos ou excecao nao relacionada a janela), mantendo prepararConteudoPalpite() para preservar o estado do fragmento. Registro no log `.ia/logs/session-20260306-palpites-inline-erro-salvar.md`.
        * **[Concluído (04/03/2026)] Revisar cálculo de data/hora:** zona padrão configurada para `America/Sao_Paulo`, `Jogo.getDataHora()` migrado para `ZonedDateTime` com janelas de palpite/encerramento reativadas, `ConversaoUtils` ajustado para BRT e dataset validado via `scripts/atualizar_copa2026_dataset.py --dry-run`. Evidência: `.ia/logs/session-20260304-palpites-timezone.md`. (Skill: `modernization-java-migration v1.0.0`)
        * **[Parcial 02/03/2026]** Pipeline recompilado após os ajustes (`npm run build`, `mvn clean package -Dfrontend.skip=false`, `docker compose build app`, `docker compose up -d app`). Container `bolao-app` recriado com a imagem atualizada `novobolao-app`. Log: `.ia/logs/session-20260302-palpites-inline-deploy.md`.
        * **[Pendente] Plano de correção aprofundado (01/03/2026 20:00)** — executar etapas 1–8 descritas em `.ia/planos/plano-correcao-palpites-popup.md` (instrumentação, validação de segurança, revisão de timezone, criação de `PalpiteAuthorizationService`, melhorias de UX, automação de testes, documentação e evidências). Log: `.ia/logs/session-20260301-palpites-inline-roles-fix.md` (registro inicial).
        * **[Pendente] Adequação CSP total (`palpites.popup` subtarefa 4e):** migrar scripts inline restantes para módulos `type="module"` com nonce.
        * **[Pendente] Validação e evidências (`palpites.popup` subtarefa 4f):** rodar pipeline completo (`npm run build`, `mvn clean package -Dfrontend.skip=false`, Docker) e registrar smoke + capturas.
        * **[Pendente] UX de salvamento do palpite (confirmacao + autosave controlado):** aplicar o plano `.ia/planos/plano-ux-palpites-autosave.md` para permitir múltiplas alterações dentro da janela, com feedback acessível e debounce. (Skill: N/A)
23. **[Concluído (25/02/2026)] Correção dropdown de autorização e renderização HTMX (24/02/2026):** Garantir encoding correto e resposta parcial consistente ao alterar o status dos participantes. Plano: `.ia/planos/plano-correcao-autorizacao-participantes.md`.
    * **[Concluído]** Diagnosticar origem do label "NÃ£o" e padronizar via i18n UTF-8 (mensagens `member.status.enabled/disabled` adicionadas).
    * **[Concluído]** Inspecionar resposta HTMX ao alternar autorização; `hx-select="#participantesTableBody"` adicionado para garantir fragmento correto.
    * **[Concluído]** Atualizar JSPs/JS para substituir apenas o `tbody` e validar interações com `hx-swap`.
    * **[Concluído (25/02/2026)]** Executar `mvn -Dfrontend.skip=true test` e rebuild Docker após ajustes CSRF; comandos `mvn clean package -Dfrontend.skip=false`, `docker compose build app` e `docker compose up -d app` finalizados sem erros (Skill: `modernization-java-migration v1.0.0`). Smoke manual da tela HTMX permanece pendente para coletar novas evidências após o desbloqueio.
    * **[Concluído (25/02/2026)]** Restaurar autenticação após erro CSRF (`planos/plano-correção-login-csrf-xor.md`); sincronização revista no `cabecalho.jspf`, login via `/j_security_check` validado com `curl` (admin/admin123) e ausência de `ArrayIndexOutOfBoundsException` confirmada nos logs do Tomcat (Skill: `modernization-java-migration v1.0.0`).
    * **[Concluído (25/02/2026)]** Corrigir resposta HTMX que retornava a página completa ao alternar o status dos participantes, garantindo swap somente do `<tbody>` com `HX-Request` detectado no backend e inclusão de `_csrf` no `hx-include`. Testes: `mvn -Dfrontend.skip=true test` (verde) e verificação manual via `curl` bloqueada pelos interceptors Fetch Metadata; validar via navegador. (Skill: `modernization-java-migration v1.0.0`)
24. **[Concluído (26/02/2026)] Diagnóstico aprofundado HTMX + Struts 7 na tela de participantes:** Investigar a persistência do bug (tabela sumia após alternar autorização) e estabilizar a UI. Plano: `.ia/planos/plano-htmx-struts7-participantes.md`.
    * Iterações 1 e 2 confirmaram cabeçalhos HTMX, ausência de bloqueios de segurança e isolaram o problema na cadeia de decorators do Struts.
    * Iteração 3 ajustou `cabecalho.jspf`/`rodape.jspf` para respeitar `skipTemplate`, evitando `return` precoce e permitindo respostas apenas com `<tbody>`.
    * Pipeline `mvn -Dfrontend.skip=true test`, `npm run build`, `mvn clean package -Dfrontend.skip=false` e `docker compose build app && docker compose up -d app` garantiu a versão `0.2.5-SNAPSHOT` com fragmentos HTMX estáveis.
    * Validação manual confirmou a renderização contínua da tabela; diretrizes frontend foram atualizadas com o padrão de preludes condicionais, encerrando a necessidade das Iterações 4-6.
    * Skill: `modernization-java-migration v1.0.0`. Referências: `.ia/logs/session-20260226-htmx-iteration3-template.md`, `.ia/logs/session-20260226-analise-htmx-participantes.md`.
25. **[Concluído (26/02/2026)] Corrigir link de retorno pós-cadastro (Bug público) – 26/02/2026:** Garantir que o link “Página principal” após sucesso do cadastro direcione corretamente o usuário.
    * Link atualizado para apontar `index.action` reutilizando a mensagem `menu.geral.principal`, mantendo conformidade com diretrizes de frontend. (Skill: `modernization-java-migration v1.0.0`)
    * `mvn -Dfrontend.skip=true test` executado com sucesso (22 testes) e registrado em `.ia/logs/session-20260226-cadastro-link.md`; smoke manual pendente de evidência visual. (Skill: N/A)
    * Plano de referência: `.ia/planos/plano-correcao-bugs-cadastro-20260226.md`.
26. **[Concluído (26/02/2026)] Tratar cadastros duplicados sem HTTP 500 – 26/02/2026:** Evitar `DataIntegrityViolationException` exibida ao repetir cadastro com os mesmos dados.
    * `ParticipanteAction` passou a verificar duplicidade de login/e-mail antes do `criarNovo`, reaproveitando `MensagemErro` e exibindo feedback amigável sem quebrar a sessão. (Skill: `modernization-java-migration v1.0.0`)
    * `ParticipanteDao`/`ParticipanteService` receberam o método `buscarPorEmail`; propriedades `messages.properties` ganharam mensagens de duplicidade com fallback seguro. (Skill: `modernization-java-migration v1.0.0`)
    * Testes atualizados (`ParticipanteActionTest`) cobrem cenários de duplicidade; `mvn -Dfrontend.skip=true test` confirma suíte verde (24 testes). Log: `.ia/logs/session-20260226-cadastro-duplicado.md`. (Skill: `modernization-java-migration v1.0.0`)
    * Plano de referência: `.ia/planos/plano-correcao-bugs-cadastro-20260226.md`.
27. **[Concluído (26/02/2026)] Revisão final das correções de cadastro – 26/02/2026:** Consolidar QA após execução das tarefas 25 e 26.
    * Smoke refeito com rebuild completo (npm/Maven/Docker) e validações via `curl`; versão 0.2.6-SNAPSHOT e labels renderizados confirmados. Log: `.ia/logs/session-20260226-cadastro-smoke.md`. (Skill: `modernization-java-migration v1.0.0`)
    * README atualizado com justificativa do uso de npm/Vite e orientações de ambiente. (Skill: N/A)
    * Preparar commit/PR conforme governança após registro desta sessão. (Skill: N/A)

28. **[Concluído (27/03/2026)] Redesign UX do fluxo de palpites – abordagem "Direct Inline":** Simplificar radicalmente o fluxo de palpites, substituindo a expansão de linha com múltiplos cliques por inputs diretamente visíveis na célula da tabela. Plano de referência: `implementation_plan.md` (brain da sessão 1cfeee87). Skills: `modernization-java-migration v1.0.0`, `architecture-guardian v1.0.0`.

    * **[Concluído (27/03/2026)] Iteração 1 – CSS e estrutura de colunas:** Reestruturar cabeçalho e células da tabela de jogos.
    * **[Concluído (27/03/2026)] Iteração 2 – Inputs inline funcionais por HTMX:** Campos editáveis diretamente na célula da tabela.
    * **[Concluído (27/03/2026)] Iteração 3 – Feedback inline na célula:** Resposta visual imediata ao salvar.
    * **[Concluído (27/03/2026)] Iteração 4 – Auto-save (blur + debounce 800ms):** Salvar ao sair do campo.
    * **[Concluído (27/03/2026)] Iteração 5 – Portlet "Meus Palpites" colapsável:** Substituir aside lateral.
    * **[Concluído (27/03/2026)] Iteração 6 – Painel "Ver Grupo" via `<details>` inline:** Ver palpites do grupo sem modal.
    * **[Concluído (27/03/2026)] Iteração 7 – Build completo, pipeline e documentação:** Encerrar e documentar.

29. **[Concluído] Ajustes UX da tela de login + recuperação de senha (09/06/2026):** Corrigir legibilidade do link "Cadastre-se agora!" e definir/implementar o fluxo de "Esqueci minha senha" com foco em acessibilidade, segurança e experiência. (Skill: `ui-ux-pro-max v1.0.0`)
    * **[Concluído] Diagnóstico UX/UI da tela de login:** mapear causa do texto oculto (contraste/cores/estados) no link de cadastro, validar comportamento em hover/focus/teclado e registrar evidência visual.
    * **[Concluído] Correção de legibilidade e foco do link de cadastro:** ajustar estilos específicos do bloco de login (contraste AA, underline/hover, foco visível, espaçamento e hierarquia do call-to-action secundário).
    * **[Concluído] Auditoria de conteúdo e i18n:** revisar `messages.properties` (ex.: `login.signin`) e padronizar o texto para clareza, mantendo consistência com o restante do site.
    * **[Concluído] Inventário de funcionalidade existente de senha:** validar se já existe fluxo de troca/recuperação e sua cobertura (action `trocaSenha` está em `/seguro` e não há JSP pública), registrando o gap técnico.
    * **[Concluído] Definir fluxo "Esqueci minha senha" (público):** desenhar jornada (solicitar e-mail → confirmar envio → OTP → redefinir senha), critérios de UX (feedback, estados vazios) e requisitos de segurança (uso único, expiração 30 minutos, mensagem neutra anti-enumeração, rate limit, antifraude).
    * **[Concluído] Arquitetura e contrato técnico (OTP em memória):** especificar endpoints/actions Struts, serviços, modelo de OTP (hash + expiração + tentativas), armazenamento **em memória**, e registro apenas de **data da última troca de senha** no participante; alinhar com ADR `.ia/historico/ADR-20260408-recuperacao-senha-token-db.md`.
    * **[Concluído] Ajuste de banco (autorizado):** adicionar a coluna `PAR_DH_ULTIMA_TROCA_SENHA` em `PAR_PARTICIPANTE` e remover a tabela `RST_RESET_TOKEN` do schema `docker/mysql/init/01-schema.sql`.
    * **[Concluído] Implementação do fluxo de recuperação (OTP):** telas JSP públicas (`recuperar-senha.jsp`, `redefinir-senha.jsp`), actions Struts (`RecuperacaoSenhaAction`) e serviço (`RecuperacaoSenhaServiceImpl`) validados com envio de e-mail/validação/redefinição.
    * **[Concluído] Corrigir bloqueio de segurança no fluxo (Bug Fix):** rotas de recuperação mapeadas com `permitAll` em `applicationContext-security.xml` (`/recuperarSenhaForm.action*`, `/enviarOtpRecuperacao.action*`, `/validarOtpRecuperacao.action*`, `/redefinirSenha.action*`), com evidência nas tarefas 14.1.x.
    * **[Concluído] Observabilidade e segurança:** fluxo utiliza mensagem neutra anti-enumeração, logs de auditoria em action/service, hash de OTP com `SHA-256 + salt`, expiração e limite de tentativas no `OtpStore`.
    * **[Concluído] Testes e QA:** validação funcional estática e de fluxo concluída, incluindo execução automatizada com sucesso via Maven (`RecuperacaoSenhaServiceImplTest` + suíte com `-Dfrontend.skip=true test`).
    * **[Concluído] Atualização de documentação e evidências:** sessões registradas em `.ia/logs/session-20260609-recuperacao-senha-submit-csp.md`, `.ia/logs/session-20260609-csp-admin-handlers-inline.md` e `.ia/logs/session-20260609-recuperacao-senha-pendencias-finalizacao.md`.

30. **[Concluído] Implementação da Troca de Senha Autenticada (17/04/2026):** Disponibilizar formulário para alteração de senha por usuários logados. (Skill: `modernization-java-migration v1.0.0`)
    * **[Concluído] Implementar UI de troca de senha:** Criar `webapp/WEB-INF/content/seguro/trocaSenha.jsp` com campos para senha atual, nova e confirmação.
*   **[Concluído] Refinamento UX/UI Sênior:** Aplicar `theme="simple"`, centralização de portlet e estilos modernos no `estilo.css` para resolver layout quebrado.
*   **[Concluído] Implementar lógica de backend:** Adicionar método `alterarSenha` em `ParticipanteAction` com validação de senha atual (BCrypt) e política de nova senha.
*   **[Concluído] Registrar data da última troca:** Garantir que a coluna `PAR_DH_ULTIMA_TROCA_SENHA` seja atualizada no sucesso.
*   **[Concluído] Testes e QA:** Executar `mvn test` e validar fluxos em ambiente Docker.

### Fase 2.8: Refinanciamento de Débito Técnico e Arquitetura (CONCLUÍDO)

1. **[Concluído]** **Migração para java.util.Optional:** Substituído o uso de retornos `null` por `Optional<T>` nos repositórios e serviços migrados.
2. **[Concluído]** **Adoção de Spring Data JPA:** Removidas as implementações manuais de DAO e substituídas por `JpaRepository`. Configuração movida para `JpaRepositoriesConfig.java`. (Fase 2.8.1, 2.8.2, 2.8.3)
3. **[Pendente]** **Modernização de Validação:** Migrar os métodos `validar()` manuais dos modelos para anotações do Jakarta Bean Validation (`@NotBlank`, `@Email`, etc.).

### Fase 3: Infraestrutura e Containerização (MODERNIZAÇÃO DE AMBIENTE)

1. **[Concluído] Containerização com Docker:** Criar `Dockerfile` multi-stage utilizando princípios distroless para a aplicação.
2. **[Concluído] Orquestração com Docker Compose:** Criar `docker-compose.yml` integrando a aplicação (Tomcat 10) e o banco de dados (MySQL 8).
3. **[Concluído] Persistência e Rede:** Configurar volumes para o banco de dados e redes isoladas entre os containers.
   * **Atualização 21/02/2026:** Rebuild sem cache da imagem `novobolao-app`, reinicialização controlada do volume `db_data` (`docker compose down -v`) para alinhar credenciais com o `.env`, e verificação de saúde via `docker compose ps` e `docker compose exec app curl` (Skill: N/A).
   * **Atualização 22/02/2026:** Autenticação validada; ajuste no `applicationContext-security.xml` liberou `/favicon.ico` para evitar HTTP 403 pós-login causado por requisições de favicon fora da allowlist (Skill: N/A).

### Fase 4: Segurança Progressiva (ALTA PRIORIDADE)

Referência Diretrizes: `.ia/diretrizes/seguranca.md`

1. **[Concluído] Auditoria de Vulnerabilidades:** Integrar o OWASP Maven Dependency Check no `pom.xml` para monitoramento contínuo de CVEs.
   * **Concluído (21/02/2026):** Plugin `dependency-check-maven` já configurado no `pom.xml` (goal `check`, `failBuildOnCVSS=7`, saída `ALL`). (Skill: `security-audit v1.0.0`)
   <security:intercept-url pattern="/seguro/**" access="hasAnyRole('ADMIN', 'USER')" />
2. **[Concluído] Proteção de Recursos Estáticos:** Mover todos os arquivos JSP para dentro de `WEB-INF/` (ex: `WEB-INF/content/`) para impedir o acesso direto via browser, forçando a passagem pelas Actions do Struts.
   * **Concluído (21/02/2026):** Includes administrativos atualizados para `/WEB-INF/content`, menu público apontando para `index.action`, novas actions Struts (`index`, `batePapo`, `trocaSenha`) e ajustes correspondentes no Spring Security para eliminar referências a `.jsp` públicas. Build validado com `mvn test -Dfrontend.skip=true`. (Skill: `security-audit v1.0.0`) Referência Log: `.ia/logs/session-20260221-protecao-recursos-estaticos-continuacao.md`.
3. **[Concluído] Proteção na Camada Web:** Configurar cabeçalhos de segurança (HSTS, CSP, X-Frame-Options) e proteção CSRF.
   * **Concluído (21/02/2026):** Reabilitado o CSRF com `CookieCsrfTokenRepository`, tokens injetados em formulários/HTMX/fetch via prelude JSP e logout convertido para POST seguro. Adicionados cabeçalhos HSTS, CSP e Referrer-Policy mantendo o X-Frame-Options. Build validado com `mvn test -Dfrontend.skip=true`. (Skill: `security-audit v1.0.0`) Referência Log: `.ia/logs/session-20260221-protecao-camada-web.md`.
4. **[Concluído] Sanitização e Validação:** Revisar validadores do Struts 6 e implementar proteção robusta contra XSS.
   * **Concluído (21/02/2026):** Criado utilitário `SanitizationUtils` com normalização Unicode, remoção de HTML e validações de formato. Ações `ParticipanteAction` e `AdminAction` passaram a sanitizar setters críticos e o fluxo de cadastro valida login/nome/e-mail/senha antes de persistir. Build validado com `mvn test -Dfrontend.skip=true`. (Skill: `security-audit v1.0.0`) Referência Log: `.ia/logs/session-20260221-sanitizacao-validacao.md`.
5. **[Concluído] Auditoria de Segredos:** Implementar varredura de credenciais e senhas em arquivos de configuração.
   * **Concluído (21/02/2026):** Removido fallback de senha do datasource (`applicationContext-resources.xml`), docker-compose passou a exigir variáveis obrigatórias e criado `scripts/scan-secrets.sh` para varredura baseada em `rg`. Documentação atualizada em `.ia/documentacao/README-migracao.md`. (Skill: `security-audit v1.0.0`) Referência Log: `.ia/logs/session-20260221-auditoria-segredos.md`.
6. **[Concluído (22/02/2026)] Diagnóstico 403 pós-login (Fluxo Autenticado):** Investigação do bloqueio após autenticação em ambiente Docker.
   * Requisições ao `favicon.ico` foram identificadas como origem do HTTP 403; `applicationContext-security.xml` passou a permitir `/favicon.ico`, eliminando o erro pós-login.
   * Logs do Tomcat e smoke manual confirmaram navegação limpa em `/seguro/principal.action` e `/admin/*.action` após o ajuste.
   * Skill: N/A. Referência log: `.ia/logs/session-20260222-login-403-favicon.md`.
7. **[Concluído] Correção das bandeiras dos países nas listagens de jogos:** Ajustar a renderização das flag icons exibidas em `admin/jogos.action` e demais telas que usam bandeiras.
   * **Concluído (22/02/2026):** Bandeiras migradas para emojis gerados a partir de códigos ISO (`FlagUtils` + métodos `getEmojiBandeira`/`getSiglaPais` em `Equipe`). JSPs (`seguro/principal.jsp`, `seguro/jogos.jsp`) usam novo componente com fallback textual e CSS (`.flag-icon`), eliminando dependência das imagens númericas `img/bandeiras/*.gif`. Log: `.ia/logs/session-20260222-bandeiras-emoji.md`. (Skill: N/A)

## Fase 5: Longo Prazo - Modernização se justificado *(Adiada)*

> **Decisão (21/02/2026):** toda a fase 5 foi adiada. Trata-se de uma reestruturação extensa (reavaliação modular, modernização completa de front-end, upgrades de Java/MySQL, observabilidade, etc.) e dependerá de planejamento futuro. As tarefas permanecerão registradas para reavaliação em momento oportuno.

1. **[Pendente] Reescrita ou Migração para estrutura modular:** Avaliar a necessidade de reescrita gradual da aplicação ou a quebra do monolito em módulos (seguindo o DDD) ou um pequeno ajuste com ganho justificado ou manter na situação atual. Avalie também uma arquitetura moderna como Struts 6 ou 7.
2. **[Pendente] Modernização Completa do Front-end:** Avaliar a necessidade de reescrita completa do front-end com um framework JavaScript moderno (React, Vue, Angular) e adoção de práticas de design responsivo, ajustado ao Struts 6 ou 7 escolhido no item 1. Tem que ser justificado para o usuário e seguir as diretrizes de arquitetura.
3. **[Pendente]** Simplifique o build, testes, análise de código e deployment com o container docker (docker compose) para execução local.
4. **[Pendente] Monitoramento e Observabilidade:** Implementar ferramentas de monitoramento de desempenho (APM), agregação de logs e rastreamento distribuído. Considere o uso de grafana e prometheus integrados ao container (docker-compose).
5. **[Pendente] Recriação do Chat (Chat 2.0):** Avaliar a necessidade de implementar um novo serviço de chat utilizando tecnologias modernas (Spring Boot + WebSocket), conforme detalhado em `implementation_plan.md` de forma justificada e seguindo as diretrizes de arquitetura.
6. **[Pendente] Atualização da Versão do Java:** Avaliar a necessidade de migrar para a versão LTS mais recente do Java (ex: Java 17 ou 21 ou 25).
7. **[Pendente] Banco de Dados:** Avaliar a necessidade de upgrade da versão do MySQL.
8. **[Pendente] Ajuste de Deploy Pós-Remoção DWR:** Validar ambientes provisionados (Docker/Tomcat externos) para remoção do servlet DWR, limpeza de WARs antigos e atualização de automações de deploy.

## Fase 6: Atualização de funcionalidades para a Copa do Mundo FIFA de 2026

1. **[Concluído] Analisar composição de times e chaves da copa 2026:** Avaliar com os dados obtidos em https://www.fifa.com/pt/tournaments/mens/worldcup/canadamexicousa2026/articles/copa-mundo-2026-tabela-jogos .
   * Resultado: Documento `.ia/documentacao/fase6-analise-grupos.md` consolidando as chaves A–L, vagas pendentes e lacunas do dataset `data/copa2026_tabela_brt.csv` (21/02/2026). Skills: N/A.
2. **[Concluído] Criar um Plano passo a passo com sugestões de melhorias para atender e ser usado pelos usuários para a copa fifa 2026.**
   * Plano registrado em `.ia/planos/plano-fase-6-copa-2026.md` com escopo, premissas e trilhas de execução (21/02/2026). Skills: N/A.
   * Subtarefas planejadas (status inicial `Pendente`):
     - **F6-T2-Dados:** normalizar `data/copa2026_tabela_brt.csv`, gerar `03-copa-2026-data.sql`, automatizar atualização pós-playoffs e documentar o processo.
       * Estrutura alvo definida em 21/02/2026 (`.ia/documentacao/fase6-normalizacao-dataset.md`). Dataset normalizado disponível em `data/copa2026_tabela_brt_normalizado.csv`; geração do script SQL e automação pós-playoffs permanecem pendentes.
       * Plano do script gerador registrado em `.ia/planos/plano-script-copa2026-sql.md`.
       * Script `scripts/atualizar_copa2026_dataset.py` implementado em 21/02/2026 (suporte a CSV normalizado, placeholders e geração do SQL). Execução efetuada em `--dry-run`; geração efetiva do SQL aguardará aprovação.
       * Arquivo de placeholders criado em `data/copa2026_placeholders.json` para futuras atualizações após repescagens (21/02/2026).
     - **F6-T2-Dominio:** inventariar o modelo atual (HBM, scripts SQL, constraints) e, se necessário, modelar 32-avos, ampliar entidades/fases, atualizar validações de palpites e criar testes cobrindo as novas regras.
       * Inventário concluído em 21/02/2026 (`.ia/documentacao/fase6-inventario-modelo-dados.md`). Próximos subitens permanecem pendentes.
       * Código da fase 32-avos (`JOG_FASE=16`) formalizado em 21/02/2026 (schema, modelo `Jogo` e seletores JSP atualizados; mensagens `filter.fase.16` criadas).
     - **F6-T2-Frontend:** reorganizar telas de grupos/classificação, atualizar filtros/dashboards, produzir nova identidade visual (`bolao_logo.png`), validar experiência mobile e atualizar diretrizes de frontend.
     - **F6-T2-Integracao:** revisar APIs/exports, monitorar performance/índices e reforçar logs/auditoria para os novos eventos.
     - **F6-T2-QA:** elaborar plano de testes, automatizar regressões, definir checklist de implantação e preparar comunicação aos usuários.
3. **[Concluído]** Criar nova imagem do bolão para a Copa 2026 inspirada no wallchart da FIFA.
   * Logo `webapp/img/bolao_logo.png` atualizado em 21/02/2026 com arte estática gerada programaticamente (cores dos países-sede, troféu estilizado e lettering `BOLÃO 2026`). em https://digitalhub.fifa.com/transform/cc8428b2-c395-456a-90ae-68fb771c9e90/FIFA-World-Cup-26-qualified-teams-wallchart-graphic-16x9?&io=transform:fill,width:1366&quality=75 para criar uma nova imagem para substituir a imagem do arquivo bolao_logo.png, mantendo as mesmas proporções para usar no sistema, sem prejudicar as telas que usam a imagem. Seja criativo e cirúrgico tecnicamente para a nova imagem. 

## Registro de Avanços

* 2026-02-17: **[Concluído]** Tarefa 3 da Fase 1. Implementada estratégia de `DelegatingPasswordEncoder` para auditoria e melhoria de credenciais, incluindo adição de `spring-security-crypto` ao `pom.xml`, criação de `DelegatingPasswordEncoder.java` e atualização de `applicationContext-security.xml` e `ParticipanteServiceImpl.java`. (Skills: `senior-java-dev-legacy v1.0.0`, `security-audit v1.0.0`)
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência ADR: `.ia/historico/ADR-20260217-seguranca-inicial.md`
* 2026-02-17: **[Concluído]** Tarefa 2 da Fase 1. Alterado o valor do parâmetro `debug` para `false` no servlet `dwr-invoker` no arquivo `web.xml`. (Skill: `senior-java-dev-legacy v1.0.0`)
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência ADR: `.ia/historico/ADR-20260217-seguranca-inicial.md`
* 2026-02-17: **[Concluído]** Tarefa 1 da Fase 1. Adicionado `security-constraint` ao `web.xml` para forçar o uso de HTTPS em toda a aplicação, definindo o `transport-guarantee` como `CONFIDENTIAL`. (Skill: `senior-java-dev-legacy v1.0.0`)
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência ADR: `.ia/historico/ADR-20260217-seguranca-inicial.md`
* 2026-02-17: **[Concluído]** Tarefa 4 da Fase 1. Isolado e desativado o chat legado (DWR) e substituído por uma mensagem de manutenção. (Skill: `senior-java-dev-legacy v1.0.0`)
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência ADR: `.ia/historico/ADR-20260217-isolamento-chat-legado.md`
* 2026-02-17: **[Concluído]** Migração Completa da Stack (Tarefa 1 da Fase 2). Finalizada a atualização para Spring Framework 6.1.4, Struts 6.3.0, Hibernate 6.4.4 e Jakarta EE 10. O build (`mvn clean compile`) está estável.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
* 2026-02-17: **[Concluído]** Introdução de Testes Automatizados (Tarefa 6 da Fase 2). Configurado framework de testes com JUnit 5, Mockito e AssertJ. Criado primeiro teste de unidade para `ParticipanteServiceImpl`. Resolvidos conflitos de repositórios Maven e migração para diretório dedicado `/tests`.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência ADR: `.ia/historico/ADR-20260217-estrategia-testes-automatizados.md`
  Referência ADR: `.ia/historico/ADR-20260217-upgrade-spring-framework.md`
* 2026-02-17: **[Concluído]** Atualização de Bibliotecas e Tags JSP (Tarefa 5 da Fase 2). Migradas tags JSP para Struts 2 (`s:`) e atualizadas dependências do Quartz (2.3.2) e EHCache (3.10.8) no `pom.xml` para compatibilidade com a nova stack.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência ADR: `.ia/historico/ADR-20260217-upgrade-spring-framework.md`
* 2026-02-17: **[Concluído]** Ajuste de Configuração Spring/Hibernate (Tarefa 1 da Fase 2). Atualizados beans `sessionFactory` e `txManager` para as classes do pacote `hibernate5` compatíveis com a nova versão.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
* 2026-02-17: **[Concluído]** Modernização Front-end (Tarefa 7 da Fase 2). Implementação da **Arquitetura Híbrida**: Integração de jQuery 4.0.0 e HTMX 1.9.10; migração do `login.jsp` e `menu.jspf`; PoC de HTMX em `participantes.jsp` (exclusão sem DWR).
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência ADR: `.ia/historico/ADR-20260217-arquitetura-frontend-modernizacao.md`
  Referência Plano: `.ia/planos/plano-modernizacao-frontend.md`
* 2026-02-17: **[Concluído]** Substituição Completa do WebWork (Tarefa 4 da Fase 2). Removida a declaração da taglib `/webwork` do `web.xml`; excluído o arquivo legado `xwork.xml`; padronizados os prefixos de taglib de `ww` para `s` em todos os arquivos JSP e JSPF. A aplicação agora utiliza exclusivamente o Struts 6 sem referências ao WebWork legado. **Nota**: Build Maven (`mvn clean compile`) continua falhando devido ao problema pré-existente com a dependência Cewolf (repositório descontinuado). Este problema será resolvido na Tarefa 5 (Atualização de Bibliotecas de Terceiros).
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Plano: `.ia/planos/plano-substituicao-webwork.md`
  Skill: `senior-java-dev-legacy v1.0.0`
* 2026-02-17: **[Concluído]** Atualização de Bibliotecas de Terceiros (Tarefa 5 da Fase 2). Inventariadas e analisadas todas as bibliotecas de terceiros do projeto. Removida dependência Cewolf (comentada no pom.xml) que causava falha no build. Verificado que a maioria das bibliotecas já está atualizada: JFreeChart 1.5.4, Batik 1.17, Quartz 2.3.2, EHCache 3.10.8, Commons Lang3 3.14.0, SLF4J 2.0.12, Logback 1.5.0. DWR 3.0.2 mantido temporariamente com migração gradual para HTMX em andamento. Build Maven (`mvn clean compile`) agora funciona com sucesso.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência ADR: `.ia/historico/ADR-20260217-bibliotecas-legadas.md`
  Referência Plano: `.ia/planos/plano-atualizacao-bibliotecas-terceiros.md`
  Skill: `senior-java-dev-legacy v1.0.0`
* 2026-02-17: **[Criada]** Fase 2.5: Auditoria e Ajuste do Frontend. Identificada necessidade crítica de validação e modernização do frontend após migrações de backend. Criado ADR documentando riscos de regressão visual, coexistência problemática de Prototype/jQuery, débito técnico CSS e falta de validação. Criado plano detalhado com 10 tarefas: auditoria visual, remoção de bibliotecas legadas, refatoração CSS, otimização de performance, acessibilidade e testes cross-browser. Estimativa: 36-53 horas (~1-1.5 semanas). Esta fase é obrigatória antes de prosseguir para Fase 3.
  Auto-Analise: [Risco de não fazer: Alto] | [Prioridade: Crítica] | [Veredito: Obrigatório]
  Referência ADR: `.ia/historico/ADR-20260217-fase-auditoria-frontend.md`
  Referência Plano: `.ia/planos/plano-fase-2.5-auditoria-frontend.md`
  Skill: `senior-frontend-dev v1.0.0`
* 2026-02-17: **[Concluído]** Atualização de Bibliotecas de Terceiros (Tarefa 5 da Fase 2). Removidos repositórios Maven descontinuados (maven.java.net); comentada dependência Cewolf (biblioteca descontinuada sem suporte Jakarta EE); build Maven (`mvn clean compile`) agora funciona com sucesso. Bibliotecas mantidas e atualizadas: JFreeChart 1.5.4, DWR 3.0.2, Batik 1.17, Quartz 2.3.2, EHCache 3.10.8. **Próximo passo**: Migrar funcionalidades que dependem do Cewolf para JFreeChart direto ou bibliotecas JS modernas.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência ADR: `.ia/historico/ADR-20260217-bibliotecas-legadas.md`
  Skill: `senior-java-dev-legacy v1.0.0`
* 2026-02-18: **[Concluído]** Correção de Configurações XML para Compatibilidade com Spring 6. Durante testes do Docker, identificado erro de inicialização devido a incompatibilidade dos arquivos XML de configuração. Migrados todos os 6 arquivos XML (hibernate, security, service, action, resources, scheduler) de DTD antigo para XSD schema do Spring 6. Corrigidas referências `<ref local="..."/>` para `<ref bean="..."/>` e sintaxe de propriedades para formato explícito compatível com Spring 6. Containers Docker parados para rebuild. **Próximo passo**: Rebuild da aplicação Docker e teste de acesso.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260218-correcao-xml-spring6.md`
  Skill: `senior-java-dev-legacy v1.0.0`
* 2026-02-18: **[Concluído]** Migração Spring Security 6 (Tarefa 2 da Fase 2). Substituída a stack legada Acegi Security (EOL) pela versão 6.2.2. Implementada nova configuração baseada no namespace Spring Security, migrado `applicationContext-security.xml` e atualizados namespaces `jakarta.*` em todo o fluxo de segurança. Resolvidos conflitos de `web.xml` e taglibs JSP. Implementado `LegacySha1PasswordEncoder` para suporte a usuários legados. Build estável e limpo de referências Acegi.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260218-migracao-spring-security-6.md`
  Skill: `modernization-java-migration v1.0.0`
* 2026-02-19: **[Concluído]** Ajustes de Build e Runtime (Fase 2). Corrigida a estrutura de recursos Maven (movendo XMLs para `src/main/resources`). Resolvidas incompatibilidades de schema XML (singleton, ref local), dependências faltantes (AspectJ, spring-context-support) e erros de conexão/schema no MySQL 8/Hibernate 6.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-correcoes-runtime-v1.md`
  Skill: `senior-java-dev-legacy v1.0.0`
* 2026-02-19: **[Concluído]** Endurecimento de Parâmetros Struts 7 (Fase 2). Refatoradas as Actions `ParticipanteAction` e `AdminAction` para utilizar anotação `@StrutsParameter`. Eliminado o uso direto de `HttpServletRequest.getParameter()` em favor de atributos de classe protegidos, garantindo o funcionamento de formulários e mitigando injeção de parâmetros maliciosos.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-struts-parameter-hardening.md`
  Skill: `modernization-java-migration v1.0.0`
* 2026-02-19: **[Concluído]** Endurecimento de Segurança Struts 7 (Fase 2). Implementadas restrições OGNL (comprimento e tipos de nós) e configurada nova stack de interceptores (`bolaoStack`) com proteções COOP, COEP e Fetch Metadata. Aplicação inicializando com as defesas proativas ativas.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-struts-ognl-hardening.md`
  Skill: `security-audit v1.0.0`
* 2026-02-19: **[Concluído]** Validação de OGNL Allowlist (Fase 2). Configuradas as permissões explícitas no `struts.xml` para as classes de domínio (`com.opendev.bolao.model`) e utilitários, garantindo que o Struts 7 consiga renderizar as propriedades dos objetos nas JSPs sob a nova política de segurança proativa.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-struts-ognl-allowlist.md`
  Skill: `modernization-java-migration v1.0.0`
* 2026-02-19: **[Concluído]** Migração do Cewolf (Fase 2.5). Criadas classes de gráfico com JFreeChart, reativada geração de datasets no serviço, adicionados endpoints Struts para PNG e atualizadas JSPs para usar `<img>` com novos endpoints; validação automatizada e runtime confirmadas (Docker) com arquivos PNG válidos.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-migracao-cewolf-continuacao.md`, `.ia/logs/session-20260219-validacao-graficos-jfreechart-v2.md`
  Skill: `modernization-java-migration v1.0.0`
* 2026-06-10: **[Concluído]** Correção de `InvalidDataAccessApiUsageException` no `JogoRepository`.
    * Subtarefa 1: Adicionado `-parameters` ao `maven-compiler-plugin` no `pom.xml` para garantir preservação de nomes de parâmetros em tempo de execução (requisito Spring 6/JPA 3).
    * Subtarefa 2: Refatorado `JogoRepository.findFirstDateWithGamesOnOrAfter` para usar nome de parâmetro explícito (`:dataReferencia`) com a anotação `@Param`.
    * Subtarefa 3: Recompilação do projeto com as novas configurações e verificação de integridade.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260610-correcao-jpa-exception.md`

* 2026-06-10: **[Concluído]** Ajustar fuso horário do timestamp de compilação para America/São_Paulo.
    * Subtarefa 1: Adicionado `build-helper-maven-plugin` ao `pom.xml` para gerar timestamp customizado.
    * Subtarefa 2: Configurado o plugin para fuso horário `America/Sao_Paulo`.
    * Subtarefa 3: Atualizado `src/main/resources/version.properties` para utilizar a nova propriedade `build.timestamp.sp`.
    * Subtarefa 4: Verificação local confirmou `build.timestamp=2026-06-10T16:17:16-03:00` no arquivo gerado.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
* 2026-02-19: **[Concluído]** Consolidação do plano de migração DWR → HTMX (Fase 2.5 - Tarefa 2, subtarefa 2). Documento criado (`.ia/planos/plano-migracao-dwr-htmx.md`) com estratégia por camadas, cronograma e próximos passos para substituir DWR/Prototype por REST + HTMX.
  Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
  Referência Log: `.ia/logs/session-20260219-plano-migracao-dwr-htmx.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-19: **[Concluído]** Avaliação de biblioteca de tooltips acessíveis (Fase 2.5 - Tarefa 2, subtarefa 3). Selecionado Tippy.js v6 (Floating UI) para substituir Overlib, garantindo suporte a navegadores modernos, acessibilidade e compatibilidade com CSP.
  Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
  Referência Log: `.ia/logs/session-20260219-avaliacao-tooltips-tippy.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-19: **[Concluído]** Avaliação e plano de remoção do `BrowserDetector.js` (Fase 2.5 - Tarefa 2, subtarefa 4). Confirmado que o script não é utilizado e definido plano para remoção segura e adoção de feature detection moderna.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-avaliacao-browserdetector.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-19: **[Concluído]** Avaliação da dependência jQuery (Fase 2.5 - Tarefa 2, subtarefa 5). Identificado uso mínimo de jQuery 4 alfa e definida estratégia para downgrade imediato para jQuery 3.7.1 estável.
  Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
  Referência Log: `.ia/logs/session-20260219-avaliacao-jquery.md`
  Referência ADR: `.ia/historico/ADR-20260219-jquery-remocao-gradual.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-19: **[Concluído]** Remoção do jQuery e refatoração do `login.jsp`. Arquivo `jquery-4.0.0.min.js` eliminado, `cabecalho.jspf` atualizado e efeito de mensagem convertido para JavaScript nativo/CSS.
  Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
  Referência Log: `.ia/logs/session-20260219-remocao-jquery.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-19: **[Concluído]** Remoção de condicionais IE (Fase 2.5 - Tarefa 2, subtarefa 7). Eliminados blocos `opendev:isIE`, imagens específicas `_ie` e hacks `filter: alpha`, padronizando o CSS moderno.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-inventario-condicionais-ie.md`, `.ia/logs/session-20260219-remocao-condicionais-ie.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-19: **[Em Progresso]** Auditoria do CSS (Fase 2.5 - Subtarefa 4). Levantamento de problemas do `estilo.css` e plano de refatoração responsiva registrados; wrapper e tela de login já atualizados para layout moderno.
  Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
  Referência Log: `.ia/logs/session-20260219-auditoria-css.md`, `.ia/logs/session-20260219-refatoracao-css-login.md`, `.ia/logs/session-20260219-refatoracao-css-cadastro.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-19: **[Concluído]** Auditoria Visual Completa (Fase 2.5 - Tarefa 1). Checklists das telas principais executados via Docker; páginas protegidas retornam HTTP 200, endpoints de gráficos entregam PNG válidos e RBAC bloqueia acessos não autorizados. Identificado redirecionamento 302 indevido em `cadastro.jsp` (ausência de `permitAll` na configuração de segurança) para tratar em tarefa futura.
  Auto-Analise: [Risco: Medio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
  Referência Log: `.ia/logs/session-20260219-auditoria-visual-validacao-telas.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-19: **[Concluído]** Remoção do fallback SHA-1 e padronização do encoder para BCrypt (Tarefa 2.6.2). Atualizado `applicationContext-security.xml`, removidos utilitários legados e confirmada suíte de testes (`mvn test -DskipITs`) com hashes modernos.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência ADR: `.ia/historico/ADR-20260219-remocao-sha1-senhas.md`
  Referência Log: `.ia/logs/session-20260219-remocao-sha1.md`
  Skill: `modernization-java-migration v1.0.0`
* 2026-02-19: **[Concluído]** Alinhamento de logs de sessão com `passo-a-passo.md`: atualização dos logs `session-20260219-encerramento.md` e `session-20260219-migracao-cewolf-parada.md` para refletir status executado conforme o plano.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-alinhamento-logs-status.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-19: **[Bloqueado]** Validação do fluxo de autenticação (Login/Logout) com `BCrypt`: bloqueada por erro HTTP 500 em `login.jsp`/`index.jsp` (taglib Cewolf não resolvida) e retorno de `status=invalido` no POST `/j_security_check` com usuários válidos.
  Auto-Analise: [Risco: Medio] | [Compatibilidade: Atencao] | [Veredito: Revisar]
  Referência Log: `.ia/logs/session-20260219-validacao-login-bcrypt.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-19: **[Concluído]** Remoção residual de Cewolf no deploy/cache do Tomcat: rebuild do container, confirmação de `cabecalho.jspf` atualizado no WAR/ROOT e `login.jsp` carregando sem erro.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-remocao-cewolf-deploy-cache.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-19: **[Concluído]** Configuração do `WebSecurityExpressionHandler` nas JSPs protegidas: declarados handlers HTTP e JSP no `applicationContext-security.xml`, rebuild Docker e validação de login via HTTPS sem erros `sec:authorize`.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-websecurity-expression-handler.md`
  Skill: `modernization-java-migration v1.0.0`
* 2026-02-19: **[Concluído]** Correção de `buscarQuantidadeDeJogosOcorridos`: DAO passa a retornar `long` com query tipada, eliminando `ClassCastException` no ranking e garantindo cobertura unitária em `ParticipanteServiceImplTest`.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-correcao-jogos-ocorridos.md`
  Skill: `modernization-java-migration v1.0.0`
* 2026-02-19: **[Concluído]** Validação do fluxo de Login/Logout com hashes BCrypt: cenários de sucesso, logout e senha inválida confirmados via Docker, sem redirecionamentos indevidos.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-validacao-login-bcrypt-v2.md`
  Skill: `modernization-java-migration v1.0.0`
* 2026-02-19: **[Concluído]** Validação de RBAC: `/seguro/**` acessível a usuários autenticados; `/admin/**` retorna 403 para perfil `USER`. Acesso `ADMIN` liberado, com erro funcional existente nas actions administrativas (NoSuchMethodException).
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: Atenção] | [Veredito: Revisar]
  Referência Log: `.ia/logs/session-20260219-validacao-rbac.md`
  Skill: `modernization-java-migration v1.0.0`
* 2026-02-19: **[Concluído]** Correção das actions administrativas e revalidação do RBAC após ajuste de proxies. Métodos do `EquipeService`, `JogoService` e `ParticipanteService` protegidos; `/admin/infoEquipes.action` e `/admin/participantes.action` respondendo 200 para ADMIN e 403 para USER.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260219-correcao-actions-admin.md`
  Skill: `modernization-java-migration v1.0.0`
* 2026-02-20: **[Concluído]** Refatoração CSS da página principal (Fase 2.5 - Tarefa 4). Atualizada `webapp/seguro/principal.jsp` para usar utilitários responsivos (`.table`, `.team-cell`, `.score-value`, `.chart-wrapper`) e centralizar a tabela de jogos em container flexível; adicionadas classes complementares em `webapp/css/estilo.css`. `mvn test` executado com sucesso garantindo integridade.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260220-refatoracao-css-principal.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-20: **[Concluído]** Refatoração CSS da página de classificação (Fase 2.5 - Tarefa 4). `webapp/seguro/classificacao.jsp` reestruturada para usar `dashboard-section`, `.table` e utilitários de alinhamento, com destaque do usuário autenticado via `ranking-highlight`; CSS atualizado em `webapp/css/estilo.css`. `mvn test` executado com sucesso (5 testes).
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260220-refatoracao-css-classificacao.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-20: **[Concluído]** Refatoração CSS da página de gráfico comparativo (Fase 2.5 - Tarefa 4). `webapp/seguro/graficoDesempenho.jsp` refatorada com `portlet-body`, `.form-section`, `.form-control` e `chart-wrapper`, removendo estilos inline; utilidades adicionadas no `estilo.css`. `mvn test` executado com sucesso (5 testes).
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260220-refatoracao-css-grafico.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-20: **[Concluído]** Refatoração CSS da página de bate-papo (Fase 2.5 - Tarefa 4). `webapp/seguro/batePapo.jsp` substitui estilos inline por `dashboard-section` e componente `.notice-card`, adicionando utilitários no `estilo.css`. `mvn test` executado com sucesso (5 testes).
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260220-refatoracao-css-batepapo.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-20: **[Concluído]** Refatoração CSS da página de jogos (Fase 2.5 - Tarefa 4). Filtro de palpites, painel “Meus palpites” e balões HTMX reorganizados com `match-filter`, `.tips-panel`, `.loading-inline`, `.balao-*` e classes de tabela responsivas; estilos inline removidos em `webapp/seguro/jogos.jsp`. `mvn test` executado com sucesso (5 testes).
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260220-refatoracao-css-jogos.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-20: **[Concluído]** Refatoração CSS da página Copa (Fase 2.5 - Tarefa 4). Estrutura de `webapp/seguro/copa.jsp` atualizada para usar `dashboard-section`, removendo container com `float/right`. `mvn test` executado com sucesso (5 testes).
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260220-refatoracao-css-copa.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-20: **[Concluído]** Refatoração CSS dos formulários públicos e telas administrativas (Fase 2.5 - Tarefa 4). Ajustados `webapp/cadastro.jsp`, `webapp/login.jsp`, `webapp/admin/inclusaoJogo.jsp`, `webapp/admin/participantes.jsp` e `webapp/template/menu.jspf` para usar utilitários (`text-left`, `icon-inline-top`, `table-spaced`, `dashboard-section`, `icon-button`, `hidden`, `mb-md`) e centralização via CSS. `mvn test` executado com sucesso (5 testes).
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260220-refatoracao-css-formularios.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-20: **[Concluído]** Migração dos tooltips legados (Fase 2.5 - Tarefas 2 e 3). Removidos `BrowserDetector.js` e `overlib.js`, criado `webapp/js/tooltips.js` com reuso em respostas HTMX, aplicados tooltips acessíveis aos cabeçalhos do ranking e concluída a sanitização final de estilos inline. `mvn test` validado pós-ajustes.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260220-remocao-overlib-tooltips.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-20: **[Concluído]** Finalização da remoção do DWR (Fase 2.5 - Tarefa 3). Retirados interceptadores `/dwr/**` da segurança, excluído `webapp/WEB-INF/dwr.xml` e confirmado que as telas migradas para HTMX permanecem funcionais após `mvn test`.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260220-remocao-dwr-finalizacao.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-21: **[Concluído]** Fase 6 - Tarefa 1 (análise das chaves). Criado o documento `.ia/documentacao/fase6-analise-grupos.md` consolidando grupos A–L, vagas pendentes (playoffs UEFA/Intercontinental) e lacunas do dataset `data/copa2026_tabela_brt.csv`. Nenhuma skill específica aplicada.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260221-fase6-analise-grupos.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-21: **[Concluído]** Fase 6 - Tarefa 2 (plano Copa 2026). Elaborado o plano `.ia/planos/plano-fase-6-copa-2026.md` com trilhas F6-T2-Dados, Domínio, Frontend, Integração e QA; passo-a-passo atualizado com subtarefas. Nenhuma skill específica aplicada.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260221-fase6-plano.md`, `.ia/logs/session-20260221-fase6-plano-ajuste.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-21: **[Concluído]** Normalização do dataset Copa 2026 (F6-T2-Dados). Gerado `data/copa2026_tabela_brt_normalizado.csv` com colunas auxiliares (`fase_codigo`, `fase_ordem`, `grupo`, `rodada`, placeholders padronizados) e documentado o fluxo em `.ia/documentacao/fase6-normalizacao-dataset.md`.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260221-fase6-dataset-analise.md`, `.ia/logs/session-20260221-fase6-dataset-normalizacao.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-21: **[Concluído]** Planejamento e implementação inicial do gerador SQL (F6-T2-Dados). Criado o plano `.ia/planos/plano-script-copa2026-sql.md` e o script `scripts/atualizar_copa2026_dataset.py`; execução validada em modo `--dry-run`.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260221-fase6-sql-plano.md`, `.ia/logs/session-20260221-fase6-sql-script.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-22: **[Concluído]** Diagnóstico 403 pós-login (Fase 4 - Item 6). Liberado `/favicon.ico` no `applicationContext-security.xml`, removendo o bloqueio após autenticação; smoke manual confirmou acesso às rotas `/seguro/principal.action` e `/admin/*.action`.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260222-login-403-favicon.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-24: **[Concluído]** Publicação da página de Regras do Bolão (Fase 2 - Item 20). Conteúdo consolidado, criação de `webapp/WEB-INF/content/regras.jsp`, action pública `regras.action`, menu atualizado e `mvn -Dfrontend.skip=true test` executado com sucesso.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260224-regras-menu.md`
  Skill: N/A (nenhuma skill aplicável)
* 2026-02-26: **[Concluído]** Diagnóstico HTMX + Struts 7 nos participantes (Fase 2 - Item 24). Ajustados `cabecalho.jspf`/`rodape.jspf` para `skipTemplate`, pipeline completo (`npm run build`, `mvn clean package -Dfrontend.skip=false`, `docker compose build app && docker compose up -d app`) e versão `0.2.5-SNAPSHOT` validada com fragmentos HTMX corretos.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260226-htmx-iteration3-template.md`, `.ia/logs/session-20260226-analise-htmx-participantes.md`
  Skill: `modernization-java-migration v1.0.0`
* 2026-04-06: **[Concluído]** Barra de progresso dos palpites (Fase 2.7). Cálculo por recorte do filtro atual (preenchidos/total) com atualização HTMX via swap OOB; fragmento dedicado JSPF integrado à tela e resposta parcial. Versão 0.3.1 publicada. Pipeline executado: `npm run build`, `mvn test`, `mvn package`, `docker compose up --build -d`.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260406-barra-progresso-palpites.md`
  Skill: `htmx v1.0.0`, `docker-expert v1.0.0`
* 2026-04-07: **[Concluído]** Hotfix barra de progresso HTMX (evento server-confirmed). Disparo via `HX-Trigger` no sucesso do salvamento do palpite, listener dedicado para recarregar `/seguro/palpiteProgressPartial.action`, e validação manual confirmada. Pipeline executado: `npm run build`, `mvn package -DskipTests`, `docker compose up --build -d`.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260406-hotfix-barra-progresso-htmx.md`
  Skill: `htmx v1.0.0`, `docker-expert v1.0.0`
* 2026-04-06: **[Concluído]** Recarga de dados do banco via Docker Compose (reset de volume). Stack reiniciada com `docker compose down --volumes` e rebuild completo para reaplicar scripts de inicialização em `docker/mysql/init`.
  Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
  Referência Log: `.ia/logs/session-20260406-reload-dados-docker.md`
  Skill: `docker-expert v1.0.0`
* 2026-02-20: **[Em Progresso]** Planejamento do bundler frontend (Fase 2.5 - Tarefa 2, subtarefa 6). Documento `.ia/planos/plano-bundler-frontend.md` detalha adoção do Vite/ESBuild, integração com WAR e próximos passos; registrado log `.ia/logs/session-20260220-plano-bundler-frontend.md`. Estrutura inicial criada e fallback `webapp/assets/js/app-bundle.js` anotados em `.ia/logs/session-20260220-bundler-setup-parcial.md`.
  Auto-Analise: [Risco: Médio] | [Compatibilidade: Atenção] | [Veredito: Revisar]
  Skill: N/A (nenhuma skill aplicável)
* 2026-03-28: **[Concluído]** Correção da Gravação de Palpites (Fase 2.5 - Iteração 8). Implementada estratégia de busca prévia no `PalpiteServiceImpl` para evitar conflitos de `NonUniqueObjectException`. Ajustada a Action para carregar o login do contexto de segurança. Log: `.ia/logs/session-20260328-fix-persistence-accordion-ux.md`.
* 2026-03-28: **[Concluído]** Redesign UX do Grupo - Accordion (Fase 2.5 - Iteração 9). Substituída a visualização popover por uma linha de detalhes expandida (full-width) com comportamento de accordion exclusivo. Adicionado suporte a ESC e animações. Log: `.ia/logs/session-20260328-fix-persistence-accordion-ux.md`. ADR: `docs/adr/002-accordion-group-details-ux.md`.
* 2026-03-28: **[Concluído]** Restrição de Perfil Admin (Fase 2.5 - Iteração 10). Perfil `ROLE_ADMIN` bloqueado de realizar palpites (com mensagem específica) e filtrado automaticamente do ranking e gráficos de desempenho. ADR: `docs/adr/003-admin-restriction-rule.md`.
* 2026-03-28: **[Concluído]** Registro e Automação Copa 2026 (Fase 6 - T2-Dados/Dominio). Validado suporte técnico para 32-avos e fases finais. Carga SQL `03-copa-2026-data.sql` gerada via `scripts/gerar_sql_copa2026.sh` e documentada.
* 2026-03-28: **[Concluído]** UX: Autosave e Debounce de Palpites (Fase 2.5 - Iteração 4). Implementado salvamento automático ao sair do campo (`blur`) com debounce de 800ms via HTMX, reduzindo drasticamente a carga cognitiva e cliques necessários.

## Fase 7: Modernização UX - Betting Console 2026 (Split Inputs)

* 2026-03-30: **[Concluído]** Modernização UX: Betting Console 2026. Implementação completa do conceito **Split Inputs** (Mapeamento Natural) e atualização por linha inteira via HTMX.
    * **Iteração 1 (Estrutural)**: Migração para `match-row.jspf` e configuração de `hx-target="closest tr"`. ADR: `docs/adr/004-split-inputs-row-level-htmx.md`.
    * **Iteração 2 (Design System)**: Adoção das fontes **Inter** e **Outfit**, paleta **Slate/Emerald** e efeitos de **Glassmorphism**.
    * **Iteração 3 (Feedback)**: Animação **Saved Flash** (glow verde esmeralda) em toda a linha do jogo para confirmação instantânea.
    * **Iteração 4 (Inteligência)**: Implementação de navegação de foco automática (Auto-Advance) e barra de progresso fixa (Sticky) no topo da tela via `ux-helper.js`.
    * Logs: `.ia/logs/Sessao-2026-03-30-UX-Split-Inputs-I1.md`.

### Fase 7.1: Correção de Bugs e Estabilidade UX
* 2026-03-30: **[Concluído]** Correção de Layout e Renderização.
    * Resolvida sobreposição do `sticky-header` no menu lateral via `z-index` (Task 1).
    * Eliminada redundância de `<tr>` e IDs duplicados entre `jogos.jsp` e `match-row.jspf` (Task 2).
    * Adicionado `settle:1.5s` no swap HTMX para suavizar transições de salvamento (Task 3).

    * Ajustado `ux-helper.js` para suportar o novo alvo de atualização.

### Fase 7.3: Correção de Contexto e Sincronização (Foco/Data Maintenance)
* 2026-03-30: **[Concluído]** Estabilização de Contexto e Concorrência.
    * Adicionado `jogoId` e `_csrf` via campos ocultos em cada `<tr>` para garantir integridade do POST.
    * Implementado `hx-sync="this:replace"` para evitar conflitos entre `blur` e `change`.
    * Validada estabilidade de renderização: os nomes dos times e bandeiras agora permanecem íntegros após cada atualização.

## Fase 8: Edição de Times dos Jogos pelo Administrador

> **Objetivo:** Permitir que o ADMIN edite, diretamente na tela `/admin/jogos.action`, os times (Equipe 1 e Equipe 2) de qualquer jogo — incluindo jogos do mata-mata (32-avos, oitavas, quartas, etc.) onde os times são definidos apenas após a fase anterior. Cada iteração é pequena, independente e terminable.

### Fase 8: Edição de Times dos Jogos pelo Administrador (CONCLUÍDO)

> **Objetivo:** Permitir que o ADMIN edite, diretamente na tela `/admin/jogos.action`, os times (Equipe 1 e Equipe 2) de qualquer jogo — incluindo jogos do mata-mata (32-avos, oitavas, quartas, etc.) onde os times são definidos apenas após a fase anterior. Cada iteração é pequena, independente e terminable.

### Iteração 1 — Endpoint de busca de equipes para selects (Backend) [Concluído]

* **[Concluído]** Verificar se já existe método `equipeService.buscarTodasEquipes()` exposto via action acessível por AJAX (provavelmente sim em `carregarInfoEquipes()`).
* **[Concluído]** Criar action `buscarEquipesJsonHtmx()` no `AdminAction` que retorna um fragmento HTML com opções `<option>` de todas as equipes, ordenadas por nome. Expor via Struts em `/admin/buscarEquipesJson.action`.
  * Adicionar `@StrutsParameter` nos setters necessários.
  * Liberação no `applicationContext-security.xml` em `/admin/**` (já deve estar coberta).
* **[Concluído]** Criar testes unitários para o novo método na action.

### Iteração 2 — Endpoint de edição de times do jogo (Backend) [Concluído]

* **[Concluído]** Adicionar método `atualizarDadosEstruturaisJogo(Long idJogo, Date data, Time hora, String local, int fase, Long idEquipe1, Long idEquipe2)` no `JogoService` / `JogoServiceImpl`.
  * Deve: buscar o `Jogo` pelo ID, associar as duas novas equipes (usando `EquipeRepository`) e salvar.
  * Validar entradas: todos os parâmetros obrigatórios, equipe1 ≠ equipe2.
* **[Concluído]** Expor via `AdminAction.salvarEdicaoEstruturalHtmx()`:
  * Receber `id`, `equipe1Id`, `equipe2Id` as `@StrutsParameter`.
  * Retornar HTTP 204 (sucesso) ou 400/500 (erro), sem body, seguindo padrão de `atualizarResultadoDoJogoHtmx()`.
* **[Concluído]** Adicionar entrada no `struts.xml` em namespace `/admin`.
* **[Concluído]** Testes unitários cobrindo: sucesso, equipes iguais (erro), jogo não encontrado.

### Iteração 3 — Fragmento JSP do formulário de edição de times (Frontend) [Concluído]

* **[Concluído]** Criar `webapp/WEB-INF/content/admin/partials/editar-times-form.jspf`:
  * Dois `<select>` (equipe1, equipe2) populados com a lista completa de equipes.
  * Atributo `selected` marcado no time atual do jogo.
  * Botão "Salvar Times" e link "Cancelar" (fecha o formulário inline).
  * Usar classes CSS existentes do design system (`form-grid`, `form-field-group`, `btn`, etc.).

### Iteração 4 — Botão e Trigger de edição inline na tabela de jogos (Frontend) [Concluído]

* **[Concluído]** Na JSP `webapp/WEB-INF/content/admin/jogos.jsp`, adicionar um botão "✏️ Times" visível apenas para `ROLE_ADMIN` (via `<sec:authorize>`).
  * O botão deve usar `hx-get` para carregar o fragmento JSP da Iteração 3 via HTMX.
  * O fragmento deve substituir a célula de times do jogo (swap inline na linha da tabela).
  * Usar `hx-target="closest tr"` ou célula específica para isolar o efeito.

### Iteração 5 — Submit HTMX do formulário de edição de times (Frontend) [Concluído]

* **[Concluído]** Configurar o formulário do fragmento (Iteração 3) com `hx-post` para `/admin/editarTimesDoJogoHtmx.action`.
  * Incluir `id`, `equipe1Id`, `equipe2Id` e `_csrf` no body do POST.
  * No sucesso (HTTP 204): fechar o formulário inline e recarregar a linha do jogo com os novos times via `hx-trigger` ou `hx-swap`.
  * No erro: exibir mensagem de erro inline sem recarregar a página.
* **[Concluído]** Fragmento de resposta parcial para recarregar a linha do jogo após edição (opcional: reutilizar `match-row.jspf` com os dados atualizados).

### Iteração 6 — Fragmento de linha do jogo atualizada (Backend + Frontend) [Concluído]

* **[Concluído]** Criar action `carregarLinhaJogoHtmx()` no `AdminAction` que:
  * Recebe o `id` do jogo.
  * Busca o jogo completo (com equipes).
  * Retorna o fragmento JSP de uma linha `<tr>` da tabela de admin com os dados atualizados.
* **[Concluído]** Criar fragmento `admin/partials/jogo-row.jspf` reutilizável com a linha da tabela de jogos (times, data, hora, fase, botões de ação).
* **[Concluído]** Integrar ao flow do submit da Iteração 5: após 204, fazer `hx-get` para `carregarLinhaJogoHtmx.action?id=X` e substituir o `<tr>` correto.

### Iteração 7 — Implementação e Refatoração (Edição Integrada)

* **[Concluído]** Criar `JogoService.atualizarDadosEstruturaisJogo`.
* **[Concluído]** Criar `AdminAction.prepararEdicaoEstruturalHtmx` e `salvarEdicaoEstruturalHtmx`.
* **[Concluído]** Refatorar `jogos.jsp` extraindo fragmentos JSP.
* **[Concluído]** Adicionar i18n em `messages.properties`.
* **[Concluído]** Executar `mvn -Dfrontend.skip=true test` e garantir suite verde.
* **[Concluído]** Revisar a OGNL allowlist em `struts.xml`: **Validado**.

### Iteração 8 — Deploy, validação manual e documentação

* **[Pendente]** Executar pipeline completo: `mvn clean package` → `docker compose up -d`.
* **[Concluído]** Incrementar versão no `pom.xml` para **0.3.3**.
* **[Concluído]** Registrar log de sessão em `.ia/logs/`.
* **[Concluído]** Atualizar `passo-a-passo.md` marcando iterações concluídas.
* **[Concluído]** Limpeza técnica de comentários e variáveis obsoletas em `jogos.jsp`.

### Fase 8.2: Estabilização e Melhoria da Edição de Jogos (UX/UI Admin)
*Referência:* `.ia/planos/edicao-jogos-estabilizacao.md`

*   **[Concluído] Subtarefa 8.2.1: Diagnóstico e Reforço do Backend (AdminAction/JogoService).**
    *   Auditar o tratamento de erro em `AdminAction.java` para respostas HTMX estruturais.
    *   Garantir logs de trace claros para operações de edição administrativa.
*   **[Concluído] Subtarefa 8.2.2: Estabilização do Markup de Visualização (admin-match-row.jsp).**
    *   Remover scripts de debug e simplificar o acionamento HTMX no botão de edição.
    *   Garantir IDs únicos e estáveis para as linhas da tabela.
*   **[Concluído] Subtarefa 8.2.3: Refatoração do Fragmento de Edição Inline (admin-match-edit-row.jsp).**
    *   Ajustar formulário para submissão resiliente e cancelamento preciso.
    *   Garantir propagação correta de tokens CSRF e indicadores de progresso.
*   **[Concluído] Subtarefa 8.2.4: Estilo de Feedback e Destaque Visual (UX Admin).**
    *   Adicionar classes CSS para destacar a linha em edição e transições de "salvando".
*   **[Concluído] Subtarefa 8.2.5: Validação Final e Registro de Log.**
    *   Realizar testes de fumaça e registrar em `.ia/logs/`.
*   **[Concluído]** Incrementar versão no `pom.xml` para **0.3.4**.
*   **[Concluído]** Executar deploy completo via `deploy.sh`.

### Fase 8.3: Modernização UX - Edição por Interação Direta (Zero-Button)
*Referência:* Princípios UX Sênior (Time Mercúrio)

*   **[Concluído] Subtarefa 8.3.1: Migrar Gatilhos HTMX para a Linha (TR).**
    *   Mover lógica de `hx-get` do botão para o elemento `<tr>`.
    *   Implementar `hx-trigger="dblclick"` para acionamento intuitivo.
*   **[Concluído] Subtarefa 8.3.2: Implementar Feedback de Interatividade (Hover/Cursor).**
    *   Ajustar CSS para que a linha reaja ao mouse, indicando capacidade de edição.
    *   Remover ícones de botões redundantes.
*   **[Concluído] Subtarefa 8.3.3: Ajuste de Acessibilidade e Teclado.**
    *   Garantir que a linha seja "focável" e responda à tecla Enter.
*   **[Concluído] Subtarefa 8.3.4: Validação em Ambiente Docker e Log.**

### Fase 8.4: Correção Crítica de Compilação JSP (Jasper Exception)
*Referência:* Log de erro `ClassNotFoundException` em `admin-match-edit-row.jsp`.

*   **[Concluído] Subtarefa 8.4.1: Saneamento de Sintaxe JSP.**
    *   Reescrever `admin-match-edit-row.jsp` de forma minimalista para evitar erros do Jasper.
*   **[Concluído] Subtarefa 8.4.2: Estabilização de Seletores HTMX (Target This).**
    *   Ajustar `admin-match-row.jsp` para usar `hx-target="this"` e remover atributos conflitantes.
*   **[Concluído] Subtarefa 8.4.3: Reforço de Mapeamento de Parâmetros na Action.**
    *   Aumentar verbosidade de logs na `AdminAction` para validar recepção do ID.
*   **[Concluído] Subtarefa 8.4.4: Redeploy e Teste de Fumaça.**

### Fase 8.5: Implementação de Painel Lateral Administrativo (Side Drawer UX)
*Referência:* Estratégia UX Sênior - Master-Detail Pattern.

*   **[Concluído] Subtarefa 8.5.1: Infraestrutura de Shell no Layout.**
    *   8.5.1.1: Adicionar container raiz `#admin-drawer-root` em `jogos.jsp`.
    *   8.5.1.2: Definir esqueleto CSS base para o Drawer (posicionamento fixo à direita).
*   **[Concluído] Subtarefa 8.5.2: Gatilho de Seleção e Estado Visual.**
    *   8.5.2.1: Ajustar `admin-match-row.jsp` para disparar carregamento no Drawer via `click`.
    *   8.5.2.2: Implementar classe CSS `.match-row--selected` para feedback de foco na tabela.
*   **[Concluído] Subtarefa 8.5.3: Criação do Fragmento de Edição (Painel).**
    *   8.5.3.1: Criar `admin-match-edit-panel.jsp` (Estrutura em `div`, sem tags de tabela).
    *   8.5.3.2: Mapear novo resultado na `AdminAction` ou ajustar o existente para ser agnóstico.
*   **[Concluído] Subtarefa 8.5.4: Refinamento de UX e Animações (Skill: `ui-ux-pro-max`).**
    *   8.5.4.1: Implementar animação de "Slide-in" e Overlay (escurecimento do fundo).
    *   8.5.4.2: Adicionar botão de fechar e suporte à tecla `ESC` para fechar o painel.
*   **[Concluído] Subtarefa 8.5.5: Ciclo de Salvamento e Atualização Reativa.**
    *   8.5.5.1: Configurar formulário do painel para disparar evento HTMX de atualização da linha.
    *   8.5.5.2: Validar feedback de sucesso/erro dentro do próprio painel.
*   **[Concluído] Subtarefa 8.5.6: Deploy, Validação e Registro.**

### Fase 8.6: Estabilização Definitiva do Side Drawer (UX Robustness)
*Referência:* Diagnóstico de falha de swap e visibilidade.

*   **[Concluído] Subtarefa 8.6.1: Reposicionamento do Shell Administrativo.**
    *   8.6.1.1: Mover containers do Drawer para a raiz de `jogos.jsp`.
    *   8.6.1.2: Garantir que o Drawer esteja fora de containers com `overflow:hidden`.
*   **[Concluído] Subtarefa 8.6.2: Refatoração do Gatilho de Ação.**
    *   8.6.2.1: Reintroduzir botão `<button>` explícito para ação "Gerenciar" em `admin-match-row.jsp`.
    *   8.6.2.2: Remover gatilhos de clique/duplo clique da tag `<tr>` para evitar conflitos.
*   **[Concluído] Subtarefa 8.6.3: Orquestração via Eventos JS (HTMX Events).**
    *   8.6.3.1: Centralizar abertura do Drawer no `ux-helper.js` via evento `htmx:afterOnLoad`.
    *   8.6.3.2: Implementar feedback visual de "linha ativa" via JS para maior precisão.
*   **[Concluído] Subtarefa 8.6.4: Reforço de CSS e Redeploy.**

### Fase 8.7: Edição Direta por Combos Administrativos (Auto-Save UX) (CONCLUÍDO)
*Referência:* Estratégia de Simplificação Radical (Princípio Meikai).

*   **[Concluído] Subtarefa 8.7.1: Transformation Visual da Tabela Admin.**
    *   8.7.1.1: Substituir textos estáticos por `<select>` e `<input>` em `admin-match-row.jsp`.
    *   8.7.1.2: Remover botão "Gerenciar" e colunas de ação redundantes.
*   **[Concluído] Subtarefa 8.7.2: Implementação de Persistência Imediata (Auto-Save).**
    *   8.7.2.1: Configurar `hx-post` e `hx-trigger="change"` em todos os campos editáveis.
    *   8.7.2.2: Garantir que todos os parâmetros necessários (id, data, hora, etc) sejam enviados em cada mudança.
*   **[Concluído] Subtarefa 8.7.3: Feedback Visual de Sucesso (Yukai).**
    *   8.7.3.1: Implementar classe CSS para "flash" de sucesso ao salvar.
*   **[Concluído] Subtarefa 8.7.4: Deploy Completo e Validação Final.**

### Fase 8.8: Padronização e Refinamento de Equipes (FIFA 2026)
*Referência:* Diretivas de nomes e bandeiras da Copa 2026.

*   **[Concluído] Subtarefa 8.8.1: Padronização de Nomes das Equipes.**
    *   8.8.1.1: Revisar e fixar nomes no SQL (RD Congo, Irã, Holanda, Catar, Coreia do Sul).
    *   8.8.1.2: Sincronizar `flags.properties` com as chaves normalizadas.
*   **[Concluído] Subtarefa 8.8.2: Refinamento de Listagem Administrativa.**
    *   8.8.2.1: Filtrar apenas seleções reais nos dropdowns (Remover placeholders do Grupo Z).
    *   8.8.2.2: Ordenar lista de jogos por cronologia (Data/Hora).
*   **[Concluído] Subtarefa 8.8.3: Correção de Renderização de Bandeiras HTMX.**
    *   8.8.3.1: Investigar falha na atualização visual da bandeira após Auto-Save.
    *   8.8.3.2: Ajustar fragmento `admin-match-row.jsp` para garantir integridade do DOM e URLs de assets.

**
    *   8.6.4.1: Ajustar `z-index` e transições para máxima prioridade visual.


### Fase 8.9: Adequação de Leiaute e Compactação (Anti-Scroll)
*Referência:* Problema de overflow horizontal na tabela administrativa.

*   **[Concluído] Subtarefa 8.9.1: Compactação de Controles Inline.**
    *   Implementar larguras máximas (max-width) para combos de Hora, Local, Fase e Equipes.
    *   Reduzir tamanho da fonte e padding horizontal nas células da tabela.
    *   Aproximar inputs de placar e bandeiras no container de time.

### Fase 8.10: Expansão do Leiaute (Wide Screen Support)
*Referência:* Feedback do usuário sobre componentes "espremidos" no centro.

*   **[Concluído] Subtarefa 8.10.1: Ampliação do Container Principal (#wrapper).**
    *   Aumentar largura máxima de 840px para 1200px no estilo.css.
*   **[Concluído] Subtarefa 8.10.2: Expansão das Seções Centrais.**
    *   Aumentar max-width de .dashboard-section e portlets de 720px para 1000px.
*   **[Concluído] Subtarefa 8.10.3: Alinhamento do Rodapé.**
    *   Sincronizar largura do #footer com o novo layout.

### Fase 8.11: Limpeza de Funcionalidades Redundantes (UX Cleanup)
*Referência:* Redundância da seção "Ver meus palpites" após implementação do Direct Inline.

*   **[Concluído] Subtarefa 8.11.1: Remoção da seção global de palpites.**
    *   Remover o componente `<details>` em `jogos.jsp`.
    *   Limpar Action, fragmentos JSP e scripts JS órfãos relacionados à listagem global.

### Fase 8.12: Correção Visual e Estrutural do Rodapé (Fix UX)
*Referência:* Quebra visual do rodapé (fora do wrapper e com estilos legados).

*   **[Concluído] Subtarefa 8.12.1: Correção de Aninhamento e Estilização.**
    *   Mover o `#footer` para dentro do `#wrapper` em `rodape.jspf`.
    *   Remover imagens de fundo e bordas legadas no `estilo.css`.
    *   Aplicar paleta de cores 2026 e tipografia moderna à versão do sistema.

### Fase 9: Deploy em Nuvem (Hugging Face Spaces + Aiven MySQL)

1.  **[Concluído] Preparação do Dockerfile:** Otimizar para porta 7860 e JVM tuning.
2.  **[Concluído] Configuração do Banco Remoto:** Mapear variáveis de ambiente para Aiven MySQL.
3.  **[Concluído] Inicialização Automática:** Criar scripts SQL de schema e carga inicial idempotente.
4.  **[Concluído] Configuração de E-mail:** Mapear segredos SMTP para o `EmailConfiguration.java`.
5.  **[Concluído] Migração para Git LFS (PLN-001):** Resolver rejeição de binários no Hugging Face.
    *   **[Concluído]** Subtarefa 9.5.1: Preparação do Ambiente e Inicialização do LFS.
    *   **[Concluído]** Subtarefa 9.5.2: Configuração de Tracking e .gitattributes.
    *   **[Concluído]** Subtarefa 9.5.3: Reescrita de Histórico com `git lfs migrate`.
    *   **[Concluído]** Subtarefa 9.5.4: Higienização Local (Reflog & GC).
    *   **[Concluído]** Subtarefa 9.5.5: Push Final e Validação no Hugging Face.
6.  **[Concluído] Integração do Script da Copa 2026 na Inicialização Automática (Cloud Fix):**
    *   **[Concluído]** Subtarefa 9.6.1: Copiar script `03-copa-2026-data.sql` para o classpath da aplicação.
    *   **[Concluído]** Subtarefa 9.6.2: Atualizar `applicationContext-resources.xml` para incluir a carga de dados na inicialização do Spring.
    *   **[Concluído]** Subtarefa 9.6.3: Implementar controle via variável de ambiente `DB_INITIALIZE`.
7.  **Refatoração do Contador de Usuários Online para sessões autenticadas:**
    *   **[Concluído]** Subtarefa 9.7.1: Habilitar o `SessionRegistry` no modulo de segurança (`applicationContext-security.xml`).
    *   **[Concluído]** Subtarefa 9.7.2: Refatorar `ContadorParticipantesOnline.java` para utilizar o registro de sessões autenticadas do Spring.
    *   Subtarefa 9.7.3: Validar a precisão do contador em ambiente local e nuvem.
8.  **Migração do módulo de e-mail de SMTP para API REST (Brevo):**
    > **Motivação:** O Hugging Face Spaces (free tier) bloqueia todas as conexões SMTP de saída (portas 465 e 587). A única alternativa confiável é usar uma API de e-mail transacional via HTTPS (porta 443).
    *   **[Concluído]** Subtarefa 9.8.1: Obter a API Key do Brevo (Disponibilizada como secret `CHAVE_API_BREVO`).
    *   **[Concluído]** Subtarefa 9.8.2: Criar classe `BrevoEmailSender.java` que encapsula o envio de e-mail via HTTP POST para `https://api.brevo.com/v3/smtp/email` usando `HttpClient` (Java 11+), sem dependências externas de SDK.
    *   **[Concluído]** Subtarefa 9.8.3: Criar interface `EmailSender.java` com método `enviar(EmailMessage)` para abstrair o mecanismo de transporte (SMTP vs API REST).
    *   **[Concluído]** Subtarefa 9.8.4: Refatorar a classe `Email.java` para delegar o envio ao `EmailSender` injetado via Spring, em vez de chamar `Transport.send()` diretamente.
    *   **[Concluído]** Subtarefa 9.8.5: Adicionar variável de ambiente `EMAIL_PROVIDER` (valores: `smtp` | `brevo`) para alternar o provider sem rebuild.
    *   **[Concluído]** Subtarefa 9.8.6: Adicionar a variável `CHAVE_API_BREVO` como **Secret** no Hugging Face e atualizar o `EmailConfiguration.java` para lê-la.
    *   **[Concluído]** Subtarefa 9.8.7: Atualizar `docs/deployment/HUGGING_FACE_AIVEN.md` com as novas variáveis de ambiente do Brevo.
    *   **[Pronto para Validação]** Subtarefa 9.8.8: Validar o envio de e-mail em produção com o Brevo (novo cadastro de participante).
    *   **[Concluído]** Subtarefa 9.8.9: Criar documentação técnica detalhada sobre o módulo de e-mail com diagramas Mermaid (`docs/architecture/EMAIL_SYSTEM.md`).
9.9. **Melhoria e Correção de Bugs no Módulo de E-mail:**
    *   **[Concluído]** Subtarefa 9.9.1: Corrigir o problema de encoding (caracteres especiais) nos e-mails enviados pelo Brevo (Garantir leitura e envio em UTF-8).
    *   **[Concluído]** Subtarefa 9.9.2: Corrigir bug no fluxo de cadastro: enviar e-mail de confirmação também para o usuário (além do admin).
    *   **[Concluído]** Subtarefa 9.9.3: Melhorar a granularidade dos logs no `BrevoEmailSender` para incluir Assunto e Remetente.

10. **Reforço de Segurança: Validação de Cadastro via OTP (6 caracteres):**
    *   **[Concluído]** Subtarefa 10.1: Implementar `OtpService` para geração de códigos alfanuméricos/gráficos aleatórios.
    *   **[Concluído]** Subtarefa 10.2: Adaptar `ParticipanteAction` para armazenar cadastro temporário na Sessão em vez de persistir no banco imediatamente.
    *   **[Concluído]** Subtarefa 10.3: Criar tela `validacaoCadastro.jsp` e `ValidacaoCadastroAction` para processamento do código.
    *   **[Concluído]** Subtarefa 10.4: Implementar limite de 3 tentativas e lógica de reenvio/correção de e-mail.
    *   **[Concluído]** Subtarefa 10.5: Disparar e-mails de Boas-vindas e Notificação de Admin apenas após validação do código.

54. **[Concluído] Correção de Renderização de Placar 0x0:**
    Objetivo: Garantir que placares 0x0 sejam exibidos corretamente na página de resumos.
    * **[Concluído] 54.1 — Auditoria de Renderização:** Investigação concluída.
    * **[Concluído] 54.2 — Implementar Fix:** Fix aplicado via EL direta.
    * **[Concluído] 54.3 — Validação:** Ambiente reiniciado e fix validado.

55. **[Concluído] Otimização Arquitetural e Melhoria do Startup:**
    Objetivo: Resolver avisos de inicialização de beans, remover configurações obsoletas e reduzir o tempo de startup.
    * **[Concluído] 55.1 — Resolver BeanPostProcessorChecker:** Auditoria concluída; estrutura XML atual avaliada como estável; reversão de tentativas de lazy-init para preservar integridade.
    * **[Concluído] 55.2 — Limpeza de Configurações:** Dialeto explícito do Hibernate removido.
    * **[Concluído] 55.3 — Validação Arquitetural:** Sistema validado e estável após as limpezas.

56. **[Concluído] Modernização do Gráfico de Desempenho (Client-side Rendering):**
    Objetivo: Substituir JFreeChart (estático/lento) por biblioteca JS moderna (ApexCharts/Chart.js) para performance e interatividade.
    * **[Concluído] 56.1 — Limpeza de UI:** Remover botão `grafico_submit` e atualizar layout CSS para identidade Copa 2026.
    * **[Concluído] 56.2 — Implementar Componente JS:** Integrar biblioteca de gráficos no `graficoDesempenho.jsp` e consumir JSON.
    * **[Concluído] 56.3 — Cleanup Legado:** Remover `gerarGraficoDesempenhoImagem` e classes de suporte JFreeChart.
    * **[Concluído] 56.4 — Visual Temático:** Aplicar paleta de cores Copa 2026 e estilos no ApexCharts.
    * **[Concluído] 56.5 — Cleanup Legado:** Remover `gerarGraficoDesempenhoImagem` e classes de suporte JFreeChart.

---

11. **Otimização do Git LFS — Eliminar Lentidão nos Commits Locais:**

    **Diagnóstico (2026-06-04):**
    O `git commit` ficou suspenso por vários minutos. A causa identificada é o
    conjunto de hooks do Git LFS nos eventos `post-commit`, `post-checkout` e
    `post-merge`. Embora o `git lfs post-commit` em si leve apenas ~24ms, o
    pipeline apresenta lentidão durante o processamento do filtro LFS
    (`git-lfs filter-process`) sobre os 159 arquivos binários rastreados, pois
    cada `git add` ou `git commit` passa **todos** os arquivos pelo filtro para
    calcular hashes e verificar ponteiros.

    **Causas Identificadas:**
    - 159 arquivos LFS rastreados (imagens PNG/GIF de bandeiras, wavs) — todos
      passam pelo `filter-process` mesmo quando não foram alterados.
    - Dois endpoints LFS configurados (`origin` = GitHub, `nuvem` = Hugging Face)
      ambos com `auth=none`. O LFS pode tentar verificar o estado remoto em
      operações locais quando mal configurado.
    - Os hooks `pre-push`, `post-checkout`, `post-merge` e `post-commit` estão
      todos ativos e invocam o daemon LFS a cada operação git.

    **Subtarefas de Otimização:**

    *   Subtarefa 11.1: **[Diagnóstico]** Verificar o `.gitattributes` e listar os
        tipos de arquivos LFS. Avaliar quais extensões realmente precisam do LFS
        (ex.: `.jar` pode ser removido; imagens pequenas < 1 MB podem ser
        rastreadas pelo Git normal sem LFS).

    *   Subtarefa 11.2: **[Limpeza de Rastreamento]** Remover do LFS os arquivos
        que não necessitam dele. Mover arquivos pequenos (imagens e WAVs < 100 KB)
        de volta ao rastreamento Git normal usando `git lfs untrack` e
        `git rm --cached`. Avaliar manter LFS apenas para arquivos ≥ 500 KB ou
        binários maiores que o Hugging Face não aceita via Git normal.

    *   Subtarefa 11.3: **[Configuração de Filtro]** Ativar o processamento
        paralelo e skip de arquivos não modificados. Configurar:
        ```
        git config lfs.concurrenttransfers 8
        git config lfs.fetchrecentalways false
        git config lfs.pruneoffsetdays 3
        ```

    *   Subtarefa 11.4: **[Hooks]** Revisar o hook `pre-push` para garantir que
        ele só transfira objetos novos, não re-verifique os existentes. Considerar
        desativar temporariamente `post-checkout` e `post-merge` caso não usem
        funcionalidades específicas do LFS no desenvolvimento local.

    *   Subtarefa 11.5: **[Validação]** Medir o tempo de `git add .` e
        `git commit` antes e após as mudanças usando `time git commit`. O objetivo
        é que o ciclo de commit local fique abaixo de **5 segundos**.

    *   Subtarefa 11.6: **[Documentação]** Atualizar `docs/deployment/HUGGING_FACE_AIVEN.md`
        com a configuração final do LFS e instruções para novos contribuidores
        configurarem o LFS corretamente em clones frescos.

### Fase 11: Análise Detalhada e Remediação de Vulnerabilidades (CRÍTICO)

> **Objetivo:** Analisar sistematicamente o relatório do OWASP Dependency Check e definir um plano de atualização seguro e compatível.

* **[Pendente] 11.1 - Extração e Análise do Relatório:** Analisar o relatório gerado (`target/dependency-check-report.html` ou `target/dependency-check-report.json`) para listar todas as dependências vulneráveis, CVEs relacionados e CVSS scores.
* **[Pendente] 11.2 - Pesquisa de Versões Seguras:** Pesquisar no Maven Central, Spring Framework Advisor e outras fontes confiáveis as versões seguras e compatíveis para cada dependência identificada, priorizando a compatibilidade com Java 17 e Spring 6.
* **[Pendente] 11.3 - Plano de Atualização:** Documentar a estratégia de atualização, definindo versões alvo, justificando a escolha e avaliando riscos de quebra de compatibilidade.
* **[Pendente] 11.4 - Execução da Remediação:** Aplicar as atualizações no `pom.xml` de forma incremental, compilando e testando o sistema após cada grupo de dependências atualizado.
* **[Pendente] 11.5 - Validação Final:** Executar build completo (`mvn clean install`) e suíte de testes unitários/integração para garantir a integridade do sistema e a ausência de vulnerabilidades críticas.

58. **[Concluído] Versionamento da aplicação para release 1.3.0 (17/06/2026):**
    Objetivo: alinhar a versão canônica do artefato Maven com a entrega funcional já estabilizada na sprint.
    * **[Concluído] 58.1 — Atualizar versão no `pom.xml`:** ajuste de `1.2.0` para `1.3.0`.
    * **[Concluído] 58.2 — Verificação de consistência:** confirmada ausência de hardcode residual da versão anterior nos arquivos de runtime.
    * **[Concluído] 58.3 — Rastreabilidade:** log de sessão criado em `.ia/logs/session-20260617-tarefa79-versionamento-1.3.0.md`.

59. **[Concluído] Correção de inconsistência visual de posições no ranking (17/06/2026):**
    Objetivo: eliminar divergência entre a posição exibida no destaque Top 10 e a posição oficial da tabela completa.
    * **[Concluído] 59.1 — Diagnóstico da causa raiz:** identificado cálculo incorreto em `classificacao.jsp` (`loopTop10.index + 4` com `begin=3`, iniciando cards em 7º).
    * **[Concluído] 59.2 — Correção da faixa Top 10:** ajustado cálculo da posição para `loopTop10.count + 3`, garantindo sequência correta da 4ª à 10ª posição.
    * **[Concluído] 59.3 — Alinhamento com ranking oficial:** removida lógica legada de empate apenas por pontuação na tabela e padronizada posição oficial por ordem classificada (`loop.count`).
    * **[Concluído] 59.4 — Validação técnica:** testes focados executados com `-Djava.awt.headless=true` (sucesso).

60. **[Concluído] Correção do travamento "Atualizando gráfico..." + estratégia de cache na tela e invalidação por resultado admin (18/06/2026):**
    Objetivo: eliminar o estado travado da tela de gráfico de desempenho, melhorar previsibilidade UX em latência alta e definir estratégia segura de cache com invalidação consistente.
    Skills aplicadas no planejamento: `architecture-guardian v1.0.0`, `senior-java-dev-legacy v1.0.0`, `ui-ux-pro-max v1.0.0`, `security-audit v1.0.0`, `htmx`.
    Referência de plano: `.ia/planos/plano-correcao-grafico-desempenho-timeout-cache-20260618.md`.

    * **[Concluído] 60.1 — Diagnóstico técnico reproduzível do travamento de status (18/06/2026):**
      Entregável:
      - mapear e reproduzir fluxo com timeout/cancelamento em `src/frontend/pages/graficoDesempenho.js`;
      - confirmar evidência de estado terminal incorreto (loading sem transição para erro/pronto).
      Status implementado:
      - diagnóstico confirmou falha de fechamento de estado em cenários de timeout/abort na requisição ativa, com permanência indevida de mensagem de loading.

    * **[Concluído] 60.2 — Correção incremental do estado UX para timeout/abort (18/06/2026):**
      Entregável:
      - ajuste de `loadChart()` para sempre finalizar em `ready`, `warn` ou `error`;
      - distinção entre abort obsoleto (troca de rival) e timeout da requisição ativa;
      - retry explícito preservado para falhas transitórias.
      Status implementado:
      - `TimeoutError` explícito no frontend, fechamento de estado com mensagem apropriada e botão de retry;
      - preservado comportamento de requisições obsoletas sem sobrescrever seleção mais recente.

    * **[Concluído] 60.3 — Cobertura de testes frontend focada em timeout e concorrência (18/06/2026):**
      Entregável:
      - ampliar `tests/frontend/graficoDesempenho.test.js` para cenário de timeout da requisição ativa;
      - garantir que timeout não deixe status em loading;
      - manter cobertura de não regressão para troca rápida de rival.
      Status implementado:
      - suíte frontend atualizada e validada (`tests/frontend/graficoDesempenho.test.js`, 3 testes aprovados).

    * **[Concluído] 60.4 — Prova funcional ponta a ponta do fluxo JSON do gráfico (18/06/2026):**
      Entregável:
      - validar contrato JSON em `/seguro/obterDadosGraficoJson.action` (success, sem dados e erro);
      - registrar tempos médios e comportamento percebido em rede mais lenta.
      Status implementado:
      - contrato validado via `ParticipanteActionTest` (payload, headers e fallback);
      - adicionado modo leve `cacheVersionOnly=true` para handshake de versão sem recomputação completa.

    * **[Concluído] 60.5 — Estratégia de pré-cache revisada por decisão arquitetural (18/06/2026):**
      Entregável:
      - avaliar pré-aquecimento assíncrono do histórico "self" após login (sem bloquear autenticação);
      - documentar trade-offs de custo/benefício e budget de execução;
      - manter fallback padrão caso o pré-cache falhe.
      Status implementado:
      - após revisão estratégica para ambiente restrito (HF free), pré-cache no login foi **despriorizado**;
      - estratégia adotada: cache e aquecimento na própria tela de gráfico, com reaproveitamento local por rival e invalidação por versão.

    * **[Concluído] 60.6 — Invalidação de cache em atualização de resultado admin (18/06/2026):**
      Entregável:
      - definir e implementar gatilho de invalidação na confirmação de placar pelo admin;
      - sincronizar invalidação entre cache cliente e sinalização backend (versão/timestamp lógico);
      - validar que atualização de resultado reflita no gráfico sem inconsistência.
      Status implementado:
      - `JogoServiceImpl.atualizarResultado(...)` incrementa versão global de cache (`GraficoDesempenhoCacheControl.invalidarCacheGlobal()`).
      - endpoint `/seguro/obterDadosGraficoJson.action` expõe `cacheVersion` no payload e `X-Grafico-Cache-Version` no header.
      - adicionado modo leve `cacheVersionOnly=true` para consulta de versão sem recomputar o gráfico.
      - frontend (`graficoDesempenho.js`) valida versão no servidor antes de reutilizar cache local por rival; em mudança de versão, remove entradas antigas e força atualização com dados recentes.
      - testes de regressão executados com sucesso (`tests/frontend/graficoDesempenho.test.js`, `ParticipanteActionTest`, `JogoServiceImplTest`).

    * **[Concluído] 60.7 — Revisão de segurança, documentação e rastreabilidade final (18/06/2026):**
      Entregável:
      - validação de segurança do endpoint/cache (dados privados por usuário);
      - atualização de documentação técnica e registro de sessão em `.ia/logs/`;
      - atualização final do status desta tarefa e subtarefas no plano.
      Status implementado:
      - cache mantido como `private` por usuário no endpoint JSON;
      - ADR registrada em `.ia/historico/ADR-20260618-cache-grafico-desempenho-versionado.md`;
      - logs técnicos registrados em `.ia/logs/session-20260618-tarefa60-iteracao-cache-admin-invalidation.md`.
      - encerramento consolidado registrado em `.ia/logs/session-20260618-tarefa60-encerramento-consolidado.md`.
      - ajuste final de consistência aplicado: removidos resquícios de pré-aquecimento no login em `ParticipanteAction` e `ParticipanteActionTest`, preservando apenas a estratégia de cache/aquecimento na tela de gráfico.

61. **[Concluído] Correção de falso aviso de saída na tela admin após salvar resultado (18/06/2026):**
    Objetivo: impedir aviso de "dados não salvos" ao navegar para outra tela quando o resultado já foi efetivamente gravado.
    * **[Concluído] 61.1 — Diagnóstico de estado pendente no frontend admin:** identificado cenário em `src/frontend/pages/jogos.js` em que `pendingAdminRequests` podia permanecer aberto após `htmx:afterRequest` sem `detail.elt`.
    * **[Concluído] 61.2 — Ajuste de liquidação de pendência:** `handleAfterRequest()` passou a liquidar fluxo admin com base em `requestConfig.path` mesmo sem elemento trigger, evitando warning falso em `beforeunload`.
    * **[Concluído] 61.3 — Regressão automatizada:** novo teste frontend em `tests/frontend/jogos.test.js` cobrindo `afterRequest` admin sem `detail.elt` e validando limpeza do bloqueio de saída.
