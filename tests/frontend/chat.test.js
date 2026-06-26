import { beforeEach, describe, expect, it, vi } from 'vitest';

function mountChatFixture() {
  document.body.innerHTML = `
    <form id="chat-send-form">
      <textarea name="chatMensagem"></textarea>
    </form>
    <div id="chat-feedback" class="chat-feedback" role="status" aria-live="polite"></div>
    <p class="chat-empty">vazio</p>
    <ul id="chat-messages-list" style="max-height:100px;overflow:auto;">
      <li class="chat-message"><p>Mensagem inicial</p></li>
    </ul>
  `;
}

describe('chat.js comportamento', () => {
  beforeEach(() => {
    vi.resetModules();
    mountChatFixture();
  });

  it('deve remover estado vazio e rolar para o final ao iniciar', async () => {
    const list = document.getElementById('chat-messages-list');
    Object.defineProperty(list, 'scrollHeight', { value: 420, configurable: true });
    list.scrollTop = 0;

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    expect(document.querySelector('.chat-empty')).toBeNull();
    expect(list.scrollTop).toBe(420);
  });

  it('deve enviar formulario com Ctrl+Enter no textarea', async () => {
    const form = document.getElementById('chat-send-form');
    const textarea = form.querySelector('textarea[name="chatMensagem"]');
    const requestSubmitSpy = vi.spyOn(form, 'requestSubmit');

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    textarea.dispatchEvent(new KeyboardEvent('keydown', {
      key: 'Enter',
      ctrlKey: true,
      bubbles: true,
      cancelable: true,
    }));

    expect(requestSubmitSpy).toHaveBeenCalledTimes(1);
  });

  it('deve reagir ao afterSwap no alvo de mensagens', async () => {
    const list = document.getElementById('chat-messages-list');
    Object.defineProperty(list, 'scrollHeight', { value: 300, configurable: true });
    list.scrollTop = 0;

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    document.body.insertAdjacentHTML('beforeend', '<p class="chat-empty">vazio 2</p>');
    list.insertAdjacentHTML('beforeend', '<li class="chat-message"><p>nova</p></li>');
    document.dispatchEvent(new CustomEvent('htmx:afterSwap', {
      detail: { target: list },
      bubbles: true,
    }));

    expect(document.querySelectorAll('.chat-empty').length).toBe(0);
    expect(list.scrollTop).toBe(300);
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

  it('deve manter atalho Ctrl+Enter funcional apos estado de erro', async () => {
    const form = document.getElementById('chat-send-form');
    const textarea = form.querySelector('textarea[name="chatMensagem"]');
    const requestSubmitSpy = vi.spyOn(form, 'requestSubmit');

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    document.dispatchEvent(new CustomEvent('htmx:responseError', {
      detail: {
        elt: form,
        target: document.getElementById('chat-messages-list'),
      },
      bubbles: true,
    }));

    textarea.dispatchEvent(new KeyboardEvent('keydown', {
      key: 'Enter',
      ctrlKey: true,
      bubbles: true,
      cancelable: true,
    }));

    expect(requestSubmitSpy).toHaveBeenCalledTimes(1);
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

  it('deve manter feedback de erro no DOM quando fragmento OOB atualizar o estado', async () => {
    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    const feedback = document.getElementById('chat-feedback');
    feedback.outerHTML = `
      <div id="chat-feedback" class="chat-feedback chat-feedback--error" role="status" aria-live="polite">
        Erro ao enviar mensagem.
      </div>
    `;

    const list = document.getElementById('chat-messages-list');
    document.dispatchEvent(new CustomEvent('htmx:afterSwap', {
      detail: { target: list },
      bubbles: true,
    }));

    const updatedFeedback = document.getElementById('chat-feedback');
    expect(updatedFeedback).not.toBeNull();
    expect(updatedFeedback.classList.contains('chat-feedback--error')).toBe(true);
    expect(updatedFeedback.textContent).toContain('Erro ao enviar mensagem.');
  });

  it('nao deve interferir no chat quando afterSwap ocorrer em alvo diferente', async () => {
    const list = document.getElementById('chat-messages-list');
    Object.defineProperty(list, 'scrollHeight', { value: 512, configurable: true });
    list.scrollTop = 10;
    document.body.insertAdjacentHTML('beforeend', '<div id="outro-alvo"></div>');

    const { initChatPage } = await import('../../src/frontend/pages/chat.js');
    initChatPage();

    list.scrollTop = 5;
    document.body.insertAdjacentHTML('beforeend', '<p class="chat-empty">vazio extra</p>');
    document.dispatchEvent(new CustomEvent('htmx:afterSwap', {
      detail: { target: document.getElementById('outro-alvo') },
      bubbles: true,
    }));

    expect(document.querySelectorAll('.chat-empty').length).toBe(1);
    expect(list.scrollTop).toBe(5);
  });
});
