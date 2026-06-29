function getChatMessagesList() {
  return document.getElementById('chat-messages-list');
}

let activeSuggestionIndex = -1;
let currentSuggestions = [];
let userReadingHistory = false;

function isAtBottom(list) {
  if (!list) {
    return false;
  }
  return list.scrollTop + list.clientHeight >= list.scrollHeight - 16;
}

function scrollListToBottom(list) {
  if (!list) {
    return;
  }
  list.scrollTop = list.scrollHeight;
}

function autoScrollIfAllowed(list) {
  if (!list) {
    return;
  }
  if (!userReadingHistory) {
    scrollListToBottom(list);
  }
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

function parseKnownLogins() {
  const entries = Array.from(document.querySelectorAll('.chat-message__login'))
    .map((span) => (span.textContent || '').replace(/[()@]/g, '').trim())
    .filter((value) => value.length > 0);
  const unique = Array.from(new Set(entries));
  if (!unique.includes('Todos')) {
    unique.push('Todos');
  }
  return unique.sort((a, b) => a.localeCompare(b, undefined, { sensitivity: 'base' }));
}

function findMentionToken(value, cursorPosition) {
  const beforeCursor = value.slice(0, cursorPosition);
  const match = beforeCursor.match(/(?:^|\s)@([A-Za-z0-9._-]*)$/);
  if (!match) {
    return null;
  }
  const token = match[0];
  const prefix = match[1] || '';
  return {
    token,
    prefix,
    start: cursorPosition - token.length,
    end: cursorPosition,
  };
}

function getAutocompleteContainer() {
  return document.getElementById('chat-autocomplete');
}

function clearAutocomplete() {
  const container = getAutocompleteContainer();
  if (!container) {
    return;
  }
  container.innerHTML = '';
  container.hidden = true;
  activeSuggestionIndex = -1;
  currentSuggestions = [];
}

function setActiveSuggestion(index) {
  const container = getAutocompleteContainer();
  if (!container) {
    return;
  }
  const items = Array.from(container.querySelectorAll('.chat-autocomplete__item'));
  if (items.length === 0) {
    return;
  }
  if (activeSuggestionIndex >= 0 && activeSuggestionIndex < items.length) {
    items[activeSuggestionIndex].classList.remove('chat-autocomplete__item--active');
  }
  activeSuggestionIndex = index;
  if (activeSuggestionIndex < 0) {
    activeSuggestionIndex = 0;
  } else if (activeSuggestionIndex >= items.length) {
    activeSuggestionIndex = items.length - 1;
  }
  const active = items[activeSuggestionIndex];
  active.classList.add('chat-autocomplete__item--active');
  container.setAttribute('aria-activedescendant', active.id);
}

function acceptSuggestion(textarea, token, suggestion) {
  const value = textarea.value;
  const before = value.slice(0, token.start);
  const after = value.slice(token.end);
  const inserted = `@${suggestion} `;
  textarea.value = `${before}${inserted}${after}`;
  const cursor = before.length + inserted.length;
  textarea.setSelectionRange(cursor, cursor);
  textarea.focus();
  clearAutocomplete();
}

function renderAutocompleteSuggestions(prefix, textarea) {
  const container = getAutocompleteContainer();
  if (!container) {
    return;
  }

  const knownLogins = parseKnownLogins();
  const suggestions = knownLogins.filter((login) =>
    login.toLowerCase().startsWith(prefix.toLowerCase())
  );
  if (suggestions.length === 0) {
    clearAutocomplete();
    return;
  }

  currentSuggestions = suggestions;
  container.innerHTML = '';
  suggestions.forEach((suggestion, index) => {
    const item = document.createElement('div');
    item.id = `chat-autocomplete-item-${index}`;
    item.className = 'chat-autocomplete__item';
    item.role = 'option';
    item.tabIndex = -1;
    item.textContent = suggestion;
    item.addEventListener('mousedown', (event) => {
      event.preventDefault();
      const token = findMentionToken(textarea.value, textarea.selectionStart);
      if (token) {
        acceptSuggestion(textarea, token, suggestion);
      }
    });
    container.appendChild(item);
  });

  container.hidden = false;
  setActiveSuggestion(0);
}

function handleAutocompleteInput(event) {
  const textarea = event.target;
  const token = findMentionToken(textarea.value, textarea.selectionStart);
  if (!token) {
    clearAutocomplete();
    return;
  }
  renderAutocompleteSuggestions(token.prefix, textarea);
}

function handleAutocompleteKeyDown(event) {
  const textarea = event.target;
  const container = getAutocompleteContainer();
  if (!container || container.hidden) {
    return;
  }

  if (event.key === 'ArrowDown') {
    event.preventDefault();
    setActiveSuggestion(activeSuggestionIndex + 1);
    return;
  }
  if (event.key === 'ArrowUp') {
    event.preventDefault();
    setActiveSuggestion(activeSuggestionIndex - 1);
    return;
  }
  if (event.key === 'Enter') {
    const token = findMentionToken(textarea.value, textarea.selectionStart);
    if (token && currentSuggestions[activeSuggestionIndex]) {
      event.preventDefault();
      acceptSuggestion(textarea, token, currentSuggestions[activeSuggestionIndex]);
    }
    return;
  }
  if (event.key === 'Escape') {
    clearAutocomplete();
  }
}

function bindMentionsAutocomplete(form) {
  const textarea = form ? form.querySelector('textarea[name="chatMensagem"]') : null;
  if (!textarea) {
    return;
  }

  textarea.addEventListener('input', handleAutocompleteInput);
  textarea.addEventListener('keydown', handleAutocompleteKeyDown);
  textarea.addEventListener('blur', () => {
    window.setTimeout(clearAutocomplete, 100);
  });

  document.addEventListener('click', (event) => {
    const container = getAutocompleteContainer();
    if (!container || container.hidden) {
      return;
    }
    if (event.target instanceof Node && !container.contains(event.target) && event.target !== textarea) {
      clearAutocomplete();
    }
  });
}

function bindScrollLock(list) {
  if (!list) {
    return;
  }
  list.addEventListener('scroll', () => {
    userReadingHistory = !isAtBottom(list);
  });
}

export function initChatPage() {
  const form = document.getElementById('chat-send-form');
  if (!form) {
    return;
  }

  const list = getChatMessagesList();
  scrollListToBottom(list);
  userReadingHistory = false;
  hideEmptyStateWhenHasMessages();
  bindShortcutToSend(form);
  bindMentionsAutocomplete(form);
  bindScrollLock(list);

  document.addEventListener('htmx:afterSwap', (event) => {
    const target = event && event.detail ? event.detail.target : null;
    if (!target || target.id !== 'chat-messages-list') {
      return;
    }
    hideEmptyStateWhenHasMessages();
    autoScrollIfAllowed(getChatMessagesList());
  });

  document.addEventListener('htmx:responseError', (event) => {
    const elt = event?.detail?.elt;
    if (!elt || elt.id !== 'chat-send-form') {
      return;
    }
    setChatErrorFeedback('Falha ao enviar mensagem. Tente novamente.');
  });
}
