# Plano: UX e comportamento de salvamento de palpites (inline + HTMX)

**Contexto**
- Relato: o palpite é registrado, mas só permite alterar na primeira vez mesmo ao mudar os valores e confirmar novamente.
- Stack: JSP/Struts 7 + Spring Security 6 + HTMX + JS modular (Vite), CSP rígida.
- Fluxo atual: formulário inline (`palpite-inline-form.jspf`) envia via `hx-post`, retorna fragmento atualizado e sincroniza UI via `src/frontend/pages/jogos.js`.

**Objetivo**
- Garantir que o palpite possa ser atualizado multiple vezes dentro da janela permitida.
- Reduzir fricção de salvamento com feedback claro e acessível, mantendo compatibilidade com HTMX e CSP.

**Nao escopo**
- Alterar regra de janela de fechamento (1h antes do jogo).
- Reescrever backend fora do fluxo de palpite.

## Diretrizes UX (alvo)
1. **Controle + previsibilidade:** manter botao "Confirmar" para intencao clara e permitir salvar repetidamente.
2. **Auto-save seguro:** opcionalmente salvar ao perder foco (blur) ou com pequeno atraso apos mudanca, apenas quando os dois campos estao validos.
3. **Feedback imediato e acessivel:** status "Salvando...", "Salvo" e erros em `aria-live`.
4. **Evitar ruido:** nao salvar a cada tecla; usar debounce e evitar validacao precoce.

## Modelo de interacao proposto
- Estado padrao: mostra ultimo palpite salvo + badge de status.
- Ao alterar qualquer campo:
  - Mostrar indicador "Alteracoes nao salvas".
  - Habilitar botao "Confirmar".
- Auto-save opcional:
  - Disparar ao sair do campo (blur) ou apos `delay` curto (ex.: 600-900ms) sem nova digitacao.
  - Se os valores forem iguais aos salvos, nao enviar requisicao.
- Ao salvar com sucesso:
  - Atualizar badge, resumo da linha e mostrar "Salvo as HH:MM".
- Ao erro:
  - Manter valores digitados e exibir mensagem contextual (nao apagar o formulario).

## Desenho tecnico (resumo)
- **HTMX:** usar `hx-trigger` no form ou inputs com `change`/`blur` + `delay`, ou acionar `htmx.trigger()` via JS quando os dois campos estiverem validos.
- **JS:** gerir estado `dirty`, impedir submit concorrente, e sincronizar UI com a resposta HTMX.
- **Backend:** garantir que `palpitePermitido` permaneça verdadeiro durante a janela e retornar mensagem diferenciada quando nao houver mudanca real.

## Tarefas e subtarefas
1. **Diagnostico do bug atual**
   - Reproduzir o problema (ROLE_USER) e coletar o fragmento retornado apos a segunda tentativa.
   - Verificar se `palpitePermitido` esta sendo retornado como `false` no fragmento (data attributes).
   - Confirmar se a UI desabilita o botao ou se o backend retorna `ERROR`.
   - Evidencias: log da Action + HTML do fragmento.

2. **Decisao UX (fluxo final)**
   - Escolher entre:
     - A) Confirmacao explicita + auto-save no blur (recomendado).
     - B) Somente confirmacao explicita.
   - Definir textos e estados (salvando/salvo/erro) com i18n.

3. **Ajustes no frontend (JSP/HTMX)**
   - Adicionar marcador de estado ("Alteracoes nao salvas" e timestamp) em `palpite-inline-form.jspf`.
   - Inserir `aria-live` para feedback de salvamento.
   - Configurar `hx-trigger` e/ou `hx-on` para disparo controlado no form.

4. **Ajustes no frontend (JS)**
   - Implementar controle de `dirty` em `src/frontend/pages/jogos.js`.
   - Evitar multiplos submits concorrentes (lock por request).
   - Se auto-save ativo, disparar submit com debounce apenas quando ambos os campos estiverem validos.

5. **Ajustes no backend**
   - Garantir que `atualizarPalpiteHtmx` responda `SUCCESS` mesmo em updates consecutivos.
   - Opcional: se valores nao mudaram, retornar status "sem mudanca" sem gravar e com mensagem amigavel.
   - Atualizar logs para rastrear tentativas repetidas.

6. **Validacao e testes**
   - Manual: fluxo de alterar varias vezes (ROLE_USER) antes da janela encerrar.
   - Testes unitarios para `PalpiteAuthorizationService` e `PalpiteServiceImpl` (idempotencia).
   - Teste HTMX: garantir que o fragmento retorna `palpitePermitido=true` enquanto a janela esta aberta.

7. **Documentacao e rastreabilidade**
   - Atualizar `passo-a-passo.md` com subtarefa ligada ao item 22.
   - Registrar log de sessao e atualizar diretrizes de frontend se necessario.

## Criterios de aceite
- Usuario consegue alterar o palpite mais de uma vez dentro da janela.
- Feedback de "Salvando..."/"Salvo" aparece e e anunciado por leitores de tela.
- Sem requisicoes em excesso (debounce ativo).
- Sem regressao CSP (sem scripts inline).

