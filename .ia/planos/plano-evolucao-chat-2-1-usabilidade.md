# Plano de Evolução do Chat 2.1: Usabilidade, Segurança e Notificações Globais

Este plano detalha as melhorias de usabilidade, simplificação e a introdução do sistema de notificações de menções a nível de sistema para o Bolão da Copa 2026.

---

## 1. Objetivos Gerais (Visão UX & Arquitetura)

### Simplificação da Identidade (Hardening & UX)
*   **Diagnóstico:** Atualmente o chat permite a inserção de um apelido arbitrário (`chatApelido`), o que gera atrito para o usuário (campo adicional de input), permite falsificação de identidade (impostação visual de outro participante) e exige validação e sanitização extra.
*   **Ação:** Remover o input de apelido. O chat passará a usar automaticamente a identificação canônica do participante logado (`Participante.getNome()`), exibindo-o junto ao handle oficial de login (`@login`).

### Sistema de Menções (@usuário e @Todos)
*   **Diagnóstico:** Menções atuais no texto do chat são puramente textuais e passivas.
*   **Ação:** 
    1.  Parsear o texto da mensagem no momento de gravação para identificar padrões de citação (`@identificador` e `@todos`).
    2.  Registrar menções pendentes para os alvos que estiverem online no sistema.
    3.  Introduzir um mecanismo dinâmico de polling de menções no header do sistema (`menu.jspf` ou `cabecalho.jspf`), que alerta o usuário em tempo real por meio de um pop-up/toast responsivo, mesmo que ele esteja navegando em outras telas do sistema.

### Melhorias Adicionais de UX do Chat
*   **Trava de Scroll Inteligente:** Impedir que o Auto-Scroll do HTMX jogue o usuário para o fim da tela caso ele esteja deliberadamente lendo mensagens antigas no topo (scroll lock).
*   **Autocomplete de Usuários:** Sugerir nomes de participantes online ao iniciar a digitação do caractere `@` na caixa de mensagem.
*   **Formatação Rica Minimalista:** Suportar renderização segura de expressões básicas (`*negrito*`, `_itálico_`, `~tachado~`).

---

## 2. Abordagem de Implementação Técnica

### Fase 1: Simplificação do Form de Envio
*   **Frontend:**
    1.  Remover o elemento `<input type="text" id="chat-apelido" .../>` e sua respectiva linha no `batePapo.jsp`.
    2.  Remover a inclusão de `#chat-apelido` no atributo `hx-include` do form de envio.
*   **Backend (`ChatServiceImpl` & `ChatAction`):**
    1.  Descontinuar o processamento do parâmetro `chatApelido` no controle.
    2.  No `ChatServiceImpl.criarMensagem`, obter e consolidar o nome de exibição oficial usando `Participante.getNome()` ou `loginAutor` caso o nome esteja vazio. Excluir o gerenciamento de apelidos voláteis por sessão (`apelidosPorSessao`).

### Fase 2: Mecanismo de Alerta de Menções (Sistema Global)
*   **Provedor de Menções (Backend):**
    1.  Criar um registry em memória `ChatNotificationService` (Thread-safe) que armazena notificações de menção não-lidas:
        `Map<String, Queue<MentionNotification>> mencoesPendentes`
    2.  Ao salvar uma mensagem no `ChatService.criarMensagem`, disparar uma verificação de expressões regulares:
        *   Procurar `@([a-zA-Z0-9_\-\.]+)`.
        *   Caso encontre `@todos` ou `@Todos`, listar os participantes ativos e criar uma notificação para cada um deles (exceto o autor).
        *   Caso encontre um `@login` específico, validar a existência do participante em banco e criar a notificação.
    3.  Implementar `verificarMencoes(String login)` no serviço, retornando as menções não lidas e limpando-as da fila temporária.
    4.  Ao carregar a tela principal de chat `/seguro/batePapo.action`, invocar a limpeza completa das notificações para o usuário.
*   **Endpoint de Consulta (`ChatAction`):**
    1.  Adicionar `ChatAction.verificarMencoesPartial()` retornando um fragmento JSP de notificação. Se não houver nada, retorna HTTP 204.
*   **Integração no Menu do Sistema (`menu.jspf` ou `cabecalho.jspf`):**
    1.  Incluir uma div âncora HTMX oculta no menu que faz polling no endpoint a cada 15 segundos:
        ```html
        <div id="chat-mencoes-container"
             hx-get="${base}/seguro/chatMencoesNotification.action"
             hx-trigger="load, every 15s"
             hx-swap="outerHTML">
        </div>
        ```
*   **Apresentação UX (Floating Toast):**
    1.  Renderizar uma caixinha de aviso visual (Glassmorphism, com cores do Bolão) posicionada no canto inferior direito da tela do usuário.
    2.  A notificação terá um convite de ação: `"@autor mencionou você no bate-papo! [Ir para o Chat]"` e sumirá automaticamente após 6 segundos usando CSS animations.

### Fase 3: Detecção de Scroll e UX do Chat
*   **Refatoração do script `chat.js`:**
    1.  Atualizar o método `scrollToBottomIfNeeded()` para verificar se o container de mensagens está scrollado perto do fundo (ex: `list.scrollHeight - list.scrollTop - list.clientHeight < 100`).
    2.  Caso esteja no fundo, aplica o scroll automático. Caso contrário, mantém a posição de leitura e exibe um selo sutil indicando `"Novas mensagens..."`.

---

## 3. Plano de Testes e Validação
*   **Testes Backend:**
    1.  Criar suíte de testes unitários para a detecção de menções em Regex (`@login` e `@todos`).
    2.  Validar concorrência do `ChatNotificationService` in-memory.
    3.  Ajustar testes do `ChatActionTest` para refletir a ausência de apelidos manuais.
*   **Testes de Interface (Contrato Frontend):**
    1.  Verificar ausência do input de apelido na tela `/seguro/batePapo.action`.
    2.  Simular uma menção via endpoint para o usuário logado e constatar o surgimento do modal Toast na página inicial.

---

## 4. Plano de Saneamento do Working Tree Antes do Fechamento

Esta seção complementa a Tarefa 101 após a revisão do working tree em 28/06/2026. A funcionalidade de Chat 2.1 só deve ser considerada concluída depois que estes itens forem executados, validados e registrados.

### 4.1 Inventário e Governança
1. Classificar arquivos modificados, deletados e não versionados entre código-fonte, configuração, testes, documentação, logs e assets gerados.
2. Confirmar se todos os arquivos não versionados fazem parte da entrega ou devem ser removidos por não pertencerem ao escopo.
3. Garantir que `passo-a-passo.md`, este plano e os logs contem a mesma história técnica.
4. Não executar commit automaticamente; preparar apenas a sugestão de mensagem quando a entrega estiver pronta.

### 4.2 Correções Funcionais e de Build Frontend
1. Alinhar `src/frontend/pages/chat.js` com os bundles Vite gerados, especialmente a regra de scroll lock e o autocomplete de menções.
2. Reexecutar `npm run build` após qualquer ajuste de fonte JavaScript.
3. Validar que `webapp/assets/.vite/manifest.json` aponta para o asset `main-*.js` correto.
4. Confirmar que assets antigos removidos não continuam referenciados por JSP, manifesto ou loader.

### 4.3 Correções de Testes
1. Corrigir a variável `form` fora de escopo em `tests/frontend/chat.test.js`.
2. Remover testes duplicados de autocomplete, preservando cobertura de abertura da lista e seleção via teclado.
3. Cobrir scroll lock, autocomplete, erro HTMX, preservação de texto digitado e feedback visual.
4. Reexecutar testes frontend relevantes e registrar resultado.

### 4.4 Higiene de Diff
1. Corrigir falhas de `git diff --check`.
2. Remover espaços finais e linha em branco excedente no EOF.
3. Evitar conversão acidental de fim de linha em XMLs Spring.
4. Manter alterações minimamente focadas no Chat 2.1.

### 4.5 Revisão Arquitetural e Segurança
1. Confirmar o fluxo `JSP/HTMX -> ChatAction -> ChatService -> ChatNotificationService`, sem acesso direto da camada web à persistência.
2. Validar autenticação obrigatória no endpoint de notificações.
3. Validar resposta `204 No Content` sem fragmento vazio quando não houver menções.
4. Garantir escape de saída no JSP (`c:out`/`fmt`) e ausência de XSS em preview, autor e texto de notificação.
5. Confirmar limite de fila, limpeza de notificações, concorrência e exclusão do autor em `@Todos`.
6. Verificar que logs não exponham conteúdo sensível de mensagens além do necessário para observabilidade.

### 4.6 Validação Final
1. Executar testes focados de backend: `ChatActionTest` e `ChatServiceImplTest`.
2. Executar testes frontend do chat.
3. Executar `npm run build`.
4. Executar `git diff --check`.
5. Executar suíte ampliada se qualquer ajuste tocar contratos compartilhados.
6. Registrar comandos, resultados, arquivos alterados e veredito no log final de sessão.
