function getChatMessagesList() {
  return document.getElementById('chat-messages-list');
}

function scrollToBottomIfNeeded() {
  const list = getChatMessagesList();
  if (!list) {
    return;
  }
  list.scrollTop = list.scrollHeight;
}

function hideEmptyStateWhenHasMessages() {
  const list = getChatMessagesList();
  if (!list) {
    return;
  }
  if (!list.querySelector('.chat-message')) {
    return;
  }
  const emptyState = document.querySelector('.chat-empty');
  if (emptyState) {
    emptyState.remove();
  }
}

function bindShortcutToSend(form) {
  const textarea = form ? form.querySelector('textarea[name="chatMensagem"]') : null;
  if (!textarea) {
    return;
  }
  textarea.addEventListener('keydown', (event) => {
    if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
      event.preventDefault();
      form.requestSubmit();
    }
  });
}

function setChatErrorFeedback(message) {
  const feedback = document.getElementById('chat-feedback');
  if (!feedback) {
    return;
  }
  feedback.classList.add('chat-feedback--error');
  feedback.textContent = message;
}

export function initChatPage() {
  const form = document.getElementById('chat-send-form');
  if (!form) {
    return;
  }

  scrollToBottomIfNeeded();
  hideEmptyStateWhenHasMessages();
  bindShortcutToSend(form);

  document.addEventListener('htmx:afterSwap', (event) => {
    const target = event && event.detail ? event.detail.target : null;
    if (!target || target.id !== 'chat-messages-list') {
      return;
    }
    hideEmptyStateWhenHasMessages();
    scrollToBottomIfNeeded();
  });

  document.addEventListener('htmx:responseError', (event) => {
    const elt = event?.detail?.elt;
    if (!elt || elt.id !== 'chat-send-form') {
      return;
    }
    setChatErrorFeedback('Falha ao enviar mensagem. Tente novamente.');
  });
}
