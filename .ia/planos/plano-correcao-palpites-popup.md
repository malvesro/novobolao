# Plano: Correção do popup de palpites (HTMX + Struts 7)

**Contexto**
- Página `webapp/WEB-INF/content/seguro/jogos.jsp` deveria abrir o balão de palpites ao clicar na linha (`<tr data-jogo-id>`), mas o evento não dispara.
- Stack atual: Struts 7, Spring Security 6, HTMX, bundle Vite (`src/frontend/pages/jogos.js`) carregado via `cabecalho.jspf`.
- Sintoma reportado: clique não seleciona a linha nem exibe o balão (sem highlight JS), indicando ausência de listeners ou erro de execução do bundle.

**Objetivo**
Substituir o balão flutuante por uma experiência alinhada às diretrizes UX/Arquitetura (expansão inline ou painel lateral acessível), garantindo:
- fluxo centrado na tabela com botões explícitos (“Editar palpite”, “Ver palpites do grupo”);
- indicação clara de estado (registrado/pendente) via badges/ícones na célula;
- uso nativo de `<dialog>` apenas quando modal for indispensável;
- scripts modulares compatíveis com CSP rígida;
- comportamento mobile-first reaproveitando os componentes utilitários documentados.

---

## Etapas Planejadas

1. **Revisão funcional e coleta de requisitos UX**
   - Confirmar com os stakeholders que o balão flutuante será descontinuado.
   - Inventariar necessidades de edição/visualização (palpite próprio x grupo) e requisitos de acessibilidade.
   - Consolidar evidências para ADR/estória descrevendo a experiência alvo (inline ou painel lateral dentro de `#jogos-page-wrapper`).

2. **Desenho técnico da expansão inline / painel lateral**
   - Especificar markup alvo em `webapp/WEB-INF/content/seguro/jogos.jsp` com `<tr>` expansível ou painel `<aside>/<dialog>` dentro da hierarquia existente.
   - Definir endpoints HTMX (`hx-get`, `hx-target`) para carregar formulário de palpite e histórico.
   - Modelar estados visuais (badges, ícones, mensagens) reutilizando utilitários (`.tips-panel`, `.dashboard-section`) e planejando responsividade.

3. **Adequação dos módulos JS sob CSP rígida**
   - Refatorar `src/frontend/pages/jogos.js` para remover lógica do balão e lidar com expansão inline/painel.
   - Consolidar helpers (CSRF/HTMX) em `src/frontend/modules/` e importar via `type="module"` com nonce.
   - Remover qualquer `import()` dinâmico e alinhar o loader `cabecalho.jspf` para carregar bundles estáticos.

4. **Implementação iterativa e testes**
   - Implementar markup inicial com placeholders acessíveis e botões explícitos.
   - Completar interações HTMX, atualizando Struts Actions/partials adequadas.
   - Executar `npm run build`, `mvn clean package -Dfrontend.skip=false` e smoke manual (ROLE_USER/ROLE_ADMIN) registrando evidências.

5. **Validação mobile-first e documentação**
   - Exercitar breakpoints principais (≤480px, 768px, ≥1024px) ajustando layout conforme necessário.
   - Atualizar diretrizes/project docs e registrar plano de testes futuros (axe, cross-browser).
   - Encerrar a tarefa no `passo-a-passo.md` com lições aprendidas e próximos passos dependentes.

---

## Referências
- `webapp/WEB-INF/content/seguro/jogos.jsp`
- `src/frontend/pages/jogos.js`
- `webapp/WEB-INF/content/template/cabecalho.jspf`
- Logs HTMX anteriores: `.ia/logs/session-20260220-remocao-dwr-palpites-htmx.md`, `.ia/logs/session-20260220-remocao-dwr-jogos-popup.md`
- Diretriz frontend 24/02/2026 (HTMX + fragmentos)

---

## Execuções Registradas

### 26/02/2026 18:24 BRT – Coleta inicial com usuário `marcio.rosner`
- Autenticação via `curl` com os cookies `_csrf` sincronizados e armazenamento em `/tmp/bolao_marcio_cookies.txt`.
- Capturado o HTML completo da página `palpites.action` (arquivo arquivado em `telas/palpites-20260226-marcio-rosner.html`). As linhas exibem `data-palpite-allowed="true"` e IDs sequenciais (`jogoTr_*`) para jogos de 11/06/2026 em diante.
- Requisições autenticadas aos assets Vite (`/assets/.vite/manifest.json`, `/assets/js/app-bundle.js`) retornam **HTTP 403 – Forbidden**. Isso indica que Spring Security não libera `/assets/**` nem para usuários autenticados, impossibilitando o carregamento do bundle e dos módulos dinâmicos.
- Sem o bundle, `initJogosPage()` não executa e os listeners de clique/teclado não são associados às linhas da tabela de jogos, reproduzindo o sintoma reportado.

### 26/02/2026 18:34 BRT – Liberação dos assets na segurança
- Atualizado `applicationContext-security.xml` para adicionar `security:intercept-url pattern="/assets/**" access="permitAll"`, alinhando `/assets` com demais recursos estáticos liberados.
- `mvn -Dfrontend.skip=true test` executado com sucesso após a alteração.
- Próxima etapa: rebuild + deploy para revalidar o acesso aos assets e confirmar que o bundle Vite carrega corretamente.

### 26/02/2026 18:36 BRT – Assets servidos após redeploy
- Pipeline completo executado (`npm run build`, `mvn clean package -Dfrontend.skip=false`, `docker compose build app`, `docker compose up -d app`).
- Requisições autenticadas para `/assets/.vite/manifest.json` e `/assets/js/app-bundle.js` retornam HTTP 200 com o conteúdo esperado, confirmando que o bundle volta a ser entregue aos usuários.
- Pendência: validar no navegador que `initJogosPage()` está ativo e que o popup abre corretamente para o usuário.

### 26/02/2026 18:40 BRT – Ajuste do caminho do manifest e telemetria
- Atualizado `cabecalho.jspf` para apontar o carregador para `assets/.vite/manifest.json` (caminho correto gerado pelo Vite) e registrar logs informativos ao importar o bundle principal ou recorrer ao fallback.
- Necessário novo rebuild + deploy para aplicar o script atualizado e observar os logs (`Carregando bundle Vite ...` / `Carregando bundle fallback ...`) no console do navegador durante os testes manuais.

### 26/02/2026 19:05 BRT – Planejamento complementar (Iteração 4)

- **Subtarefa 3a – Instrumentar loader/módulo:** adicionar `console.info/error` no loader (`cabecalho.jspf`) e em `src/frontend/pages/jogos.js` (ex.: logar início de módulo, resultado de `bindRowEvents`) para identificar onde o fluxo quebra.
- **Subtarefa 3b – Revisar dependência `$j` em `menu.jspf`:** substituir o script inline por DOM nativo (`document.querySelector` e `classList.toggle`) para evitar erros caso jQuery não esteja mais presente.
- **Subtarefa 3c – Adequação CSP:** prepararnonce/token reaproveitável para os scripts inline obrigatórios e planejar migração para módulos externos, evitando bloqueios quando a política deixar de ser report-only.
- **Subtarefa 3d – Validação manual:** após aplicar os itens acima e republicar, executar nova rodada de testes (limpeza de cache, console aberto, captura de logs e evidência visual) para confirmar o retorno do popup.

### 27/02/2026 19:45 BRT – Execução subtarefas 3b e 3c

- `menu.jspf` refatorado para remover `$j` e `onclick`, adicionando atributos `data-menu-target`, `role="button"`, `aria-controls`/`aria-expanded` e foco acessível; novo módulo `src/frontend/modules/menuToggle.js` acopla os eventos e é carregado por `main.js`. CSS (`estilo.css`) recebeu `:focus-visible` e suporte a `div.menu_item[hidden]`.
- Build front-end recompilado (`npm run build`) gerando manifest `main-DRlidxp7.js`; `mvn -Dfrontend.skip=true test` validou 24 testes (aviso Log4j mantido). Resultado registrado no log `.ia/logs/session-20260227-menu-csp-ajuste.md`.
- Auditoria CSP listou scripts inline remanescentes: `template/cabecalho.jspf` (CSRF bootstrap + loader), `login.jsp` (animação de erro), `cadastro.jsp` (helper de sugestão) e `admin/inclusaoJogo.jsp` (configuração do formulário). Todos já utilizam `nonce="${cspNonce}"`; próximos passos preveem mover esses blocos para módulos dedicados e substituir manipuladores inline (`onfocus/onblur`) por data-attributes para suportar CSP em modo enforcement.

### 27/02/2026 20:18 BRT – Ajuste de posicionamento e versão 0.2.9-SNAPSHOT

- CSS atualizado para aplicar `z-index: 1050` aos balões de palpite (`#balao_palpite`, `#balao_palpites`), prevenindo clipping atrás da tabela. **Obs.: abordagem marcada como transitória até conclusão da expansão inline/painel.**
- `mostrarPopupPalpite()` passou a considerar altura/largura dos balões e limites da viewport, reposicionando-os com margens de segurança e fallback à esquerda. Coordenadas agora são validadas contra `scrollY`/`scrollX`, garantindo visibilidade. **Obs.: lógica será removida quando a experiência inline estiver ativa.**
- Pipeline completo executado (`npm run build`, `mvn clean package -Dfrontend.skip=false`, `docker compose build app`, `docker compose up -d app`) e versão incrementada para `0.2.9-SNAPSHOT`. Validação via `curl` confirma rodapé com timestamp 27/02/2026 15:57. Log: `.ia/logs/session-20260227-palpites-popup-ajuste.md`.

### 27/02/2026 22:25 BRT – Redirecionamento estratégico (inline/painel)

- Recomendação UX/Arquitetura aprovada: remover balão flutuante e adotar expansão inline da linha (com opção de painel lateral para histórico).
- Plano atualizado para refletir etapas de definição da experiência, redesenho do markup/JS sob CSP rígida e validação mobile-first.
- Logs anteriores permanecerão como histórico, porém classificados como mitigação transitória; novas execuções focarão na entrega inline/painel.
- Próximos passos: elaborar ADR/estória registrando a decisão, implementar protótipo HTML (mock) e iniciar refatoração de `jogos.js` e `jogos.jsp`.

### Subtarefas planejadas (27/02/2026)

1. **[Concluído 27/02/2026] Alinhamento UX/Negócio**
   - Consolidar requisitos com o time (expansão inline vs. painel lateral) e registrar estória/ADR (`ADR-20260227-palpites-inline-experiencia.md`).
2. **[Concluído 27/02/2026] Protótipo funcional**
   - Documento de referência: `.ia/documentacao/prototipo-palpites-inline.md` (markup inline, painel lateral, estados e testes planejados).
3. **[Concluído 27/02/2026] Refatoração de markup**
   - `webapp/WEB-INF/content/seguro/jogos.jsp` remodelado com linhas expansíveis (`match-expand`), colunas “Status”/“Ações” e botões HTMX explícitos.
   - Painel lateral `aside#palpite-panel` incorporado ao markup; estrutura de balões removida.
   - Estilos alinhados em `webapp/css/estilo.css` (badges, botões, painel, responsividade) e mensagens atualizadas (`messages.properties`).
   - Referência: `.ia/logs/session-20260227-palpites-markup-refatoracao.md`.
4. **[Em Progresso] Refatoração de scripts**
   - `src/frontend/pages/jogos.js` reorganizado para suportar expansão inline/painel, sincronizar badges/placares e remover dependências dos balões; ajustes adicionais previstos para finalizar feedback pós-salvamento e integração com backend.
   - Referência: `.ia/logs/session-20260227-palpites-inline-scripts.md`.
5. **[Pendente] Adequação CSP total**
   - Migrar scripts inline remanescentes para `src/frontend/modules/`, utilizando `<script type="module" nonce="${cspNonce}" src="...">`.
6. **[Pendente] Validação e evidências**
   - Executar `npm run build`, `mvn clean package -Dfrontend.skip=false`, rebuild Docker e registrar smoke (ROLE_USER/ROLE_ADMIN) com evidência visual.

### 27/02/2026 22:40 BRT – ADR de alinhamento

- Criado rascunho `ADR-20260227-palpites-inline-experiencia.md` consolidando a decisão e atualizando a subtarefa 1 para concluída.
- Referência adicional: `.ia/documentacao/prototipo-palpites-inline.md` descreve o markup alvo e encerra a subtarefa 2; próximas etapas migram para implementação (subtarefas 3–6).

### 01/03/2026 14:52 BRT – Ajuste `skipTemplate` nas respostas HTMX

- `ParticipanteAction` passou a invocar `marcarRespostaParcial()` antes de cada resultado parcial (`listarMeusPalpitesHtmx`, `listarPalpitesDoJogoHtmx`, `carregarPalpiteFormHtmx`, `atualizarPalpiteHtmx`), definindo `request.setAttribute("skipTemplate", Boolean.TRUE)` e garantindo que `cabecalho.jspf` omita o prelude/coda nas respostas HTMX. Log: `.ia/logs/session-20260301-palpites-inline-skiptemplate.md`.
- Mantido o `struts.xml` apontando para os wrappers `.jsp` criados em 01/03; as respostas agora retornam apenas o fragmento HTML, sem o layout completo.
- `mvn -Dfrontend.skip=true test` concluído sem falhas (aviso Log4j conhecido).
- Pipeline completo executado (npm run build, mvn clean package -Dfrontend.skip=false, docker compose build app, docker compose up -d app) publicando a nova imagem `novobolao-app`. Log: `.ia/logs/session-20260301-palpites-inline-deploy.md`.
- Atualizada a checagem de papéis em `ParticipanteAction` para aceitar rótulos com e sem prefixo `ROLE_`, evitando que `isUserInRole` retorne falso e garantindo `palpitePermitido` nos fragments HTMX. Log: `.ia/logs/session-20260301-palpites-inline-roles.md`.
- Próximo passo: repetir validação manual ROLE_USER/ROLE_ADMIN para confirmar renderização inline (abrir, editar, cancelar), registrar evidência e então avançar para o ajuste CSP total (subtarefa 4e).

### 01/03/2026 20:00 BRT – Plano de correção aprofundado (pendente)

1. **[Concluído 03/03/2026] Reproduzir e instrumentar** – Fluxo reexecutado via `curl` (ROLE_USER `palpiteuser`, ROLE_ADMIN `admin`) acionando `palpiteFormPartial`/`atualizarPalpitePartial` para `jogoId=1000`. Logs `[HTMX][UPDATE]` indicaram `login=null` e `resultado=ERROR motivo=usuarioNaoAutenticado`, confirmando que o principal não chega aos endpoints HTMX. Evidência registrada em `.ia/logs/session-20260303-palpites-inline-reproducao.md`.
2. **[Concluído 03/03/2026] Validar contexto de segurança** – Adicionado `RequestContextFilter` após o `springSecurityFilterChain` e ajustado `RequestUtils.getRequest()` para privilegiar o wrapper Struts, fazendo com que `HttpServletRequest#getUserPrincipal()` retorne o usuário nas chamadas HTMX. Logs `[SEC][HTMX] principal recuperado via HttpServletRequest name=palpiteuser/admin` e o fluxo `atualizarPalpitePartial` encerrando com `resultado=success` estão registrados em `.ia/logs/session-20260303-filtros-principal.md`. **Ponto de parada 03/03/2026:** prosseguir para a etapa 3 (revisão de timezone/calendário) e, na sequência, atacar as subtarefas remanescentes (CSP, UX e testes automatizados) registrando novas evidências a cada entrega.
3. **[Concluído 04/03/2026] Revisar cálculo de data/hora** – timezone padronizado em BRT (`TimeZoneInitializer`), `Jogo.getDataHora()` migrado para `ZonedDateTime` com janelas de palpite/encerramento reativadas e dataset `JOG_JOGO` validado via `scripts/atualizar_copa2026_dataset.py --dry-run`. (Log: `.ia/logs/session-20260304-palpites-timezone.md`)
4. **Introduzir `PalpiteAuthorizationService`** – encapsular lógica de janela temporal + papéis, fornecer método reutilizável (`podeEditarPalpite`) para Actions e JSPs, documentar dependências. (Parcial 02/03/2026 — Log: `.ia/logs/session-20260302-palpites-authorization-service.md`)
5. **Melhorar UX/feedback** – exibir mensagens distintas para bloqueio por tempo vs. permissão, incluir horário exato de encerramento, prevenir `hx-get` se `data-palpite-allowed=false`, atualizar i18n. (Log: _a gerar_)
6. **Automatizar testes** – adicionar testes unitários/integrados para o serviço de autorização (cenários tempo/perfil), criar teste HTMX (Spring MVC Test) e roteiro e2e (Playwright/Cypress) validando renderização do formulário inline. (Log: _a gerar_)
7. **Documentar** – atualizar `.ia/documentacao/palpites-encerramento.md` com timezone, fluxo de autorização e dependências; registrar novo log consolidando descobertas. (Log: _a gerar_)
8. **Pipeline e evidências** – após ajustes, rodar `npm run build`, `mvn clean package -Dfrontend.skip=false`, `docker compose build/up`; capturar evidências (ROLE_USER/ADMIN) demonstrando formulário ativo/bloqueado conforme regra. (Log: _a gerar_)
