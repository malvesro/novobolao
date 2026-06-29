import { beforeEach, describe, expect, it, vi } from 'vitest';

function mountChatFixture() {
  document.body.innerHTML = `
    <form id="logoutForm">
      <input type="hidden" name="_csrf" value="token-123" />
    </form>
    <span id="chat-mentions-health"
          hidden
          data-polling-degraded-template="Notificacoes de mencoes temporariamente indisponiveis. Nova tentativa em {0}s."
          data-polling-recovered-message="Notificacoes de mencoes normalizadas."
          data-polling-warmup-recovered-message="Notificacoes de mencoes sincronizadas nesta sessao."></span>
    <div id="chat-mentions-badge-poller"
         hx-trigger="mentions:refresh, load, every 15s"
         hx-swap="outerHTML"></div>
    <div id="chat-mentions-poller"
         hx-trigger="mentions:refresh, load, every 15s"
         hx-swap="outerHTML"></div>
    <form id="chat-send-form" aria-busy="false">
      <input type="hidden" id="chat-reply-message-id" name="chatReplyMensagemId" value="" />
      <div id="chat-reply-context" hidden aria-hidden="true">
        <p id="chat-reply-label"></p>
        <p id="chat-reply-preview"></p>
        <button type="button" id="chat-reply-cancel">Cancelar resposta</button>
      </div>
      <textarea name="chatMensagem" maxlength="300" aria-describedby="chat-mention-help chat-char-counter"></textarea>
      <div class="chat-form__meta">
        <p id="chat-mention-help" class="chat-mention-hint">Use @login ou @Todos para mencionar.</p>
        <span id="chat-char-counter" class="chat-char-counter">0/300</span>
      </div>
      <div class="chat-form__actions">
        <button type="button" id="chat-new-messages-indicator" hidden aria-hidden="true">
          <span id="chat-new-messages-label">Novas mensagens · Ir para o final</span>
        </button>
        <button type="submit">Enviar</button>
      </div>
    </form>
    <div id="chat-autocomplete" class="chat-autocomplete" role="listbox" aria-label="Sugestões de menção" hidden></div>
    <div id="chat-feedback" class="chat-feedback" role="status" aria-live="polite"></div>
    <p class="chat-empty">vazio</p>
    <form id="chat-query-form">
      <input type="text" name="chatBuscaTermo" />
      <input type="text" name="chatBuscaAutor" />
      <input type="date" name="chatBuscaDataInicio" />
      <input type="date" name="chatBuscaDataFim" />
      <button type="button" id="chat-query-clear">Limpar</button>
    </form>
    <div id="chat-query-results"><p class="chat-empty">placeholder</p></div>
    <ul id="chat-messages-list" style="max-height:100px;overflow:auto;">
      <li class="chat-message">
        <p>Mensagem inicial</p>
        <button type="button" class="chat-message__reply-action"
                data-chat-reply-id="1"
                data-chat-reply-author="Admin"
                data-chat-reply-text="Mensagem inicial">Responder</button>
      </li>
    </ul>
  `;
}

function extractEverySeconds(trigger) {
  const match = (trigger || '').match(/every (\d+)s/);
  return match ? Number.parseInt(match[1], 10) : null;
}

describe('chat.js comportamento', () => {
  beforeEach(() => {
    vi.resetModules();
    vi.restoreAllMocks();
    vi.useRealTimers();
    mountChatFixture();
    delete window.fetch;
  });

  it('deve manter scroll lock quando o usuário está em leitura retrospectiva', async () => {
    const list = document.getElementById('chat-messages-list');
    Object.defineProperty(list, 'scrollHeight', { value: 500, configurable: true });
    Object.defineProperty(list, 'clientHeight', { value: 120, configurable: true });
    list.scrollTop = 0;

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    expect(list.scrollTop).toBe(500);
    list.scrollTop = 0;
    list.dispatchEvent(new Event('scroll'));
    const lockedPosition = list.scrollTop;

    Object.defineProperty(list, 'scrollHeight', { value: 650, configurable: true });
    document.dispatchEvent(new CustomEvent('htmx:afterSwap', {
      detail: { target: list },
      bubbles: true,
    }));

    expect(list.scrollTop).toBe(lockedPosition);
  });

  it('deve exibir indicador persistente de novas mensagens e permitir ir ao fim da lista', async () => {
    const list = document.getElementById('chat-messages-list');
    Object.defineProperty(list, 'scrollHeight', { value: 420, configurable: true });
    Object.defineProperty(list, 'clientHeight', { value: 120, configurable: true });
    list.scrollTop = 0;

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    list.scrollTop = 0;
    list.dispatchEvent(new Event('scroll'));
    document.dispatchEvent(new CustomEvent('htmx:beforeSwap', {
      detail: { target: list },
      bubbles: true,
    }));
    list.insertAdjacentHTML('beforeend', '<li class="chat-message"><p>Nova 1</p></li>');
    list.insertAdjacentHTML('beforeend', '<li class="chat-message"><p>Nova 2</p></li>');
    document.dispatchEvent(new CustomEvent('htmx:afterSwap', {
      detail: { target: list },
      bubbles: true,
    }));

    const indicator = document.getElementById('chat-new-messages-indicator');
    const indicatorLabel = document.getElementById('chat-new-messages-label');
    expect(indicator.hidden).toBe(false);
    expect(indicatorLabel.textContent).toContain('Novas mensagens (2)');

    indicator.click();
    expect(indicator.hidden).toBe(true);
    expect(list.scrollTop).toBe(420);
  });

  it('deve abrir autocomplete, navegar por teclado e selecionar menção', async () => {
    document.body.insertAdjacentHTML(
      'beforeend',
      '<div class="chat-message__login">(@admin)</div><div class="chat-message__login">(@alice)</div>'
    );
    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    const textarea = document.querySelector('textarea[name="chatMensagem"]');
    textarea.value = 'Olá @a';
    textarea.selectionStart = textarea.value.length;
    textarea.dispatchEvent(new Event('input', { bubbles: true }));

    const suggestions = document.getElementById('chat-autocomplete');
    expect(suggestions.hidden).toBe(false);
    expect(suggestions.querySelectorAll('.chat-autocomplete__item').length).toBe(2);
    expect(suggestions.getAttribute('aria-activedescendant')).toBe('chat-autocomplete-item-0');

    textarea.dispatchEvent(new KeyboardEvent('keydown', {
      key: 'ArrowDown',
      bubbles: true,
      cancelable: true,
    }));

    expect(suggestions.getAttribute('aria-activedescendant')).toBe('chat-autocomplete-item-1');
    textarea.dispatchEvent(new KeyboardEvent('keydown', {
      key: 'Enter',
      bubbles: true,
      cancelable: true,
    }));

    expect(textarea.value).toContain('@alice ');
    expect(textarea.value.startsWith('Olá')).toBe(true);
    expect(suggestions.hidden).toBe(true);
  });

  it('deve preservar texto digitado quando ocorrer erro de envio HTMX', async () => {
    const form = document.getElementById('chat-send-form');
    const textarea = form.querySelector('textarea[name="chatMensagem"]');
    textarea.value = 'mensagem que nao deve sumir';

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    document.dispatchEvent(new CustomEvent('htmx:responseError', {
      detail: {
        elt: form,
        target: document.getElementById('chat-messages-list'),
      },
      bubbles: true,
    }));

    expect(textarea.value).toBe('mensagem que nao deve sumir');
  });

  it('deve exibir feedback visual quando ocorrer responseError no envio', async () => {
    const form = document.getElementById('chat-send-form');

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    document.dispatchEvent(new CustomEvent('htmx:responseError', {
      detail: {
        elt: form,
        target: document.getElementById('chat-messages-list'),
      },
      bubbles: true,
    }));

    const feedback = document.getElementById('chat-feedback');
    expect(feedback.classList.contains('chat-feedback--error')).toBe(true);
    expect(feedback.textContent).toContain('Falha ao enviar mensagem');
  });

  it('deve atualizar estado do botao durante envio e resetar apos sucesso/erro', async () => {
    const form = document.getElementById('chat-send-form');
    const submitButton = form.querySelector('button[type="submit"]');
    const textarea = form.querySelector('textarea[name="chatMensagem"]');
    textarea.value = 'mensagem';

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    document.dispatchEvent(new CustomEvent('htmx:beforeRequest', {
      detail: { elt: form },
      bubbles: true,
    }));
    expect(submitButton.disabled).toBe(true);
    expect(submitButton.textContent).toBe('Enviando...');
    expect(form.getAttribute('aria-busy')).toBe('true');

    document.dispatchEvent(new CustomEvent('htmx:afterRequest', {
      detail: { elt: form, successful: true },
      bubbles: true,
    }));
    expect(submitButton.disabled).toBe(false);
    expect(submitButton.textContent).toBe('Enviar');
    expect(form.getAttribute('aria-busy')).toBe('false');
    expect(textarea.value).toBe('');

    textarea.value = 'nova tentativa';
    document.dispatchEvent(new CustomEvent('htmx:beforeRequest', {
      detail: { elt: form },
      bubbles: true,
    }));
    document.dispatchEvent(new CustomEvent('htmx:responseError', {
      detail: { elt: form, target: document.getElementById('chat-messages-list') },
      bubbles: true,
    }));
    expect(submitButton.disabled).toBe(false);
    expect(submitButton.textContent).toBe('Enviar');
    expect(form.getAttribute('aria-busy')).toBe('false');
    expect(textarea.value).toBe('nova tentativa');
  });

  it('deve atualizar contador de caracteres conforme digitacao', async () => {
    const form = document.getElementById('chat-send-form');
    const textarea = form.querySelector('textarea[name="chatMensagem"]');
    const counter = document.getElementById('chat-char-counter');

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    expect(counter.textContent).toBe('0/300');
    textarea.value = 'abcde';
    textarea.dispatchEvent(new Event('input', { bubbles: true }));
    expect(counter.textContent).toBe('5/300');
  });

  it('deve enviar ACK explicito das mencoes apos swap do toast', async () => {
    window.fetch = vi.fn().mockResolvedValue({ ok: true, status: 204 });
    const mentionPoller = document.getElementById('chat-mentions-poller');
    mentionPoller.dataset.chatMentionsAckUrl = '/seguro/chatMencoesAckPartial.action';
    mentionPoller.dataset.chatMentionsAckIds = '10,20';

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    document.dispatchEvent(new CustomEvent('htmx:afterSwap', {
      detail: { target: mentionPoller },
      bubbles: true,
    }));
    await Promise.resolve();

    expect(window.fetch).toHaveBeenCalledTimes(1);
    expect(window.fetch).toHaveBeenCalledWith(
      '/seguro/chatMencoesAckPartial.action',
      expect.objectContaining({
        method: 'POST',
        credentials: 'same-origin',
      })
    );
    const fetchOptions = window.fetch.mock.calls[0][1];
    expect(fetchOptions.body).toContain('chatMencoesAckIds=10%2C20');
    expect(fetchOptions.body).toContain('_csrf=token-123');
  });

  it('deve preencher contexto de resposta ao clicar em responder e limpar ao cancelar', async () => {
    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    const replyButton = document.querySelector('.chat-message__reply-action');
    const replyInput = document.getElementById('chat-reply-message-id');
    const replyContext = document.getElementById('chat-reply-context');
    const replyPreview = document.getElementById('chat-reply-preview');

    replyButton.click();
    expect(replyInput.value).toBe('1');
    expect(replyContext.hidden).toBe(false);
    expect(replyPreview.textContent).toContain('Mensagem inicial');

    document.getElementById('chat-reply-cancel').click();
    expect(replyInput.value).toBe('');
    expect(replyContext.hidden).toBe(true);
  });

  it('deve limpar filtros de consulta e restaurar mensagem padrão', async () => {
    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    const form = document.getElementById('chat-query-form');
    form.querySelector('input[name="chatBuscaTermo"]').value = 'gol';
    form.querySelector('input[name="chatBuscaAutor"]').value = 'admin';
    document.getElementById('chat-query-results').innerHTML = '<p>resultado antigo</p>';

    document.getElementById('chat-query-clear').click();

    expect(form.querySelector('input[name="chatBuscaTermo"]').value).toBe('');
    expect(form.querySelector('input[name="chatBuscaAutor"]').value).toBe('');
    expect(document.getElementById('chat-query-results').textContent).toContain('Use os filtros acima');
  });

  it('deve liberar chave de ACK em sucesso para permitir novo envio do mesmo lote', async () => {
    window.fetch = vi.fn().mockResolvedValue({ ok: true, status: 204 });
    const mentionPoller = document.getElementById('chat-mentions-poller');
    mentionPoller.dataset.chatMentionsAckUrl = '/seguro/chatMencoesAckPartial.action';
    mentionPoller.dataset.chatMentionsAckIds = '10,20';

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    document.dispatchEvent(new CustomEvent('htmx:afterSwap', {
      detail: { target: mentionPoller },
      bubbles: true,
    }));
    await Promise.resolve();

    document.dispatchEvent(new CustomEvent('htmx:afterSwap', {
      detail: { target: mentionPoller },
      bubbles: true,
    }));
    await Promise.resolve();

    expect(window.fetch).toHaveBeenCalledTimes(2);
  });

  it('deve pausar polling com aba oculta e retomar com jitter ao voltar para aba visivel', async () => {
    const mentionPoller = document.getElementById('chat-mentions-poller');
    const badgePoller = document.getElementById('chat-mentions-badge-poller');
    const visibilityStateGetter = vi.fn(() => 'visible');
    Object.defineProperty(document, 'visibilityState', {
      configurable: true,
      get: visibilityStateGetter,
    });
    const randomSpy = vi.spyOn(Math, 'random').mockReturnValue(0.4);

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    visibilityStateGetter.mockReturnValue('hidden');
    document.dispatchEvent(new Event('visibilitychange'));
    expect(mentionPoller.getAttribute('hx-trigger')).toBe('mentions:refresh');
    expect(badgePoller.getAttribute('hx-trigger')).toBe('mentions:refresh');

    visibilityStateGetter.mockReturnValue('visible');
    document.dispatchEvent(new Event('visibilitychange'));
    const resumedMentionEvery = extractEverySeconds(mentionPoller.getAttribute('hx-trigger'));
    const resumedBadgeEvery = extractEverySeconds(badgePoller.getAttribute('hx-trigger'));
    expect(resumedMentionEvery).toBe(16);
    expect(resumedBadgeEvery).toBe(16);
    expect(randomSpy).toHaveBeenCalled();
  });

  it('deve exibir aviso discreto de sincronizacao no primeiro sucesso de polling', async () => {
    vi.useFakeTimers();
    const mentionPoller = document.getElementById('chat-mentions-poller');
    const health = document.getElementById('chat-mentions-health');

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    document.dispatchEvent(new CustomEvent('htmx:afterRequest', {
      detail: { elt: mentionPoller, successful: true },
      bubbles: true,
    }));

    expect(health.hidden).toBe(false);
    expect(health.textContent).toContain('sincronizadas nesta sessao');

    vi.advanceTimersByTime(2600);
    expect(health.hidden).toBe(true);
  });

  it('deve entrar em degradacao leve no polling de mencoes e recuperar no proximo sucesso', async () => {
    vi.useFakeTimers();
    const mentionPoller = document.getElementById('chat-mentions-poller');
    const badgePoller = document.getElementById('chat-mentions-badge-poller');
    const health = document.getElementById('chat-mentions-health');

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    document.dispatchEvent(new CustomEvent('htmx:afterRequest', {
      detail: { elt: mentionPoller, successful: false },
      bubbles: true,
    }));

    expect(health.hidden).toBe(false);
    expect(health.textContent).toContain('Nova tentativa em 30s');
    const degradedPollerEvery = extractEverySeconds(mentionPoller.getAttribute('hx-trigger'));
    const degradedBadgeEvery = extractEverySeconds(badgePoller.getAttribute('hx-trigger'));
    expect(degradedPollerEvery).not.toBeNull();
    expect(degradedBadgeEvery).not.toBeNull();
    expect(degradedPollerEvery).toBeGreaterThanOrEqual(30);
    expect(degradedPollerEvery).toBeLessThanOrEqual(33);
    expect(degradedBadgeEvery).toBeGreaterThanOrEqual(30);
    expect(degradedBadgeEvery).toBeLessThanOrEqual(33);

    document.dispatchEvent(new CustomEvent('htmx:afterRequest', {
      detail: { elt: mentionPoller, successful: true },
      bubbles: true,
    }));

    expect(health.hidden).toBe(false);
    expect(health.textContent).toContain('normalizadas');
    const recoveredPollerEvery = extractEverySeconds(mentionPoller.getAttribute('hx-trigger'));
    const recoveredBadgeEvery = extractEverySeconds(badgePoller.getAttribute('hx-trigger'));
    expect(recoveredPollerEvery).not.toBeNull();
    expect(recoveredBadgeEvery).not.toBeNull();
    expect(recoveredPollerEvery).toBeGreaterThanOrEqual(15);
    expect(recoveredPollerEvery).toBeLessThanOrEqual(18);
    expect(recoveredBadgeEvery).toBeGreaterThanOrEqual(15);
    expect(recoveredBadgeEvery).toBeLessThanOrEqual(18);

    vi.advanceTimersByTime(4000);
    expect(health.hidden).toBe(true);
  });

  it('nao deve causar efeito colateral quando afterSwap ocorrer fora do alvo do chat', async () => {
    const list = document.getElementById('chat-messages-list');
    Object.defineProperty(list, 'scrollHeight', { value: 512, configurable: true });
    Object.defineProperty(list, 'clientHeight', { value: 100, configurable: true });
    list.scrollTop = 10;
    document.body.insertAdjacentHTML('beforeend', '<div id="outro-alvo"></div>');

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    list.scrollTop = 5;
    document.body.insertAdjacentHTML('beforeend', '<p class="chat-empty">vazio extra</p>');
    const feedback = document.getElementById('chat-feedback');
    const feedbackBefore = feedback.className;
    document.dispatchEvent(new CustomEvent('htmx:afterSwap', {
      detail: { target: document.getElementById('outro-alvo') },
      bubbles: true,
    }));

    const queryPlaceholder = document.querySelector('#chat-query-results .chat-empty');
    expect(queryPlaceholder).not.toBeNull();
    expect(queryPlaceholder.textContent).toContain('placeholder');
    expect(document.body.textContent).toContain('vazio extra');
    expect(list.scrollTop).toBe(5);
    expect(document.getElementById('chat-feedback').className).toBe(feedbackBefore);
  });
});
