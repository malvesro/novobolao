function getChatMessagesList() {
  return document.getElementById('chat-messages-list');
}

let activeSuggestionIndex = -1;
let currentSuggestions = [];
let userReadingHistory = false;
let mentionTokenContext = null;
let lockedScrollTop = 0;
let previousMessageCount = 0;
let unseenMessagesCount = 0;

const SEND_LABEL_DEFAULT = 'Enviar';
const SEND_LABEL_PENDING = 'Enviando...';
const MENTION_BASE_INTERVAL_MS = 15000;
const MENTION_MAX_INTERVAL_MS = 120000;
const MENTION_JITTER_MAX_MS = 3000;
const MENTION_RECOVERY_HIDE_DELAY_MS = 4000;
const MENTION_WARMUP_HIDE_DELAY_MS = 2600;
const MENTION_POLLER_IDS = new Set(['chat-mentions-poller', 'chat-mentions-badge-poller']);
const MENTION_BIND_FLAG = '__chatMentionPollingBound';
const MENTION_VISIBILITY_BIND_FLAG = '__chatMentionVisibilityBound';

let mentionPollingFailureCount = 0;
let mentionPollingRecoveryTimeoutId = 0;
let mentionPollingWarmupShown = false;
let mentionPollingPausedByVisibility = false;

const mentionAckInFlight = new Set();

function getNewMessagesIndicator() {
  return document.getElementById('chat-new-messages-indicator');
}

function getNewMessagesIndicatorLabel() {
  return document.getElementById('chat-new-messages-label');
}

function getCharCounter() {
  return document.getElementById('chat-char-counter');
}

function getReplyMessageIdInput() {
  return document.getElementById('chat-reply-message-id');
}

function getReplyContextContainer() {
  return document.getElementById('chat-reply-context');
}

function getReplyContextLabel() {
  return document.getElementById('chat-reply-label');
}

function getReplyContextPreview() {
  return document.getElementById('chat-reply-preview');
}

function getMessageCount(list) {
  if (!list) {
    return 0;
  }
  return list.querySelectorAll('.chat-message').length;
}

function getDistanceFromBottom(list) {
  if (!list) {
    return Number.POSITIVE_INFINITY;
  }
  return Math.max(0, list.scrollHeight - list.clientHeight - list.scrollTop);
}

function isAtBottom(list) {
  return getDistanceFromBottom(list) <= 16;
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
  if (userReadingHistory) {
    const maxTop = Math.max(0, list.scrollHeight - list.clientHeight);
    list.scrollTop = Math.min(lockedScrollTop, maxTop);
  } else {
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
  const emptyState = document.querySelector('.chat-stream .chat-empty');
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

function bindCharCounter(form) {
  const textarea = form ? form.querySelector('textarea[name="chatMensagem"]') : null;
  if (!textarea) {
    return;
  }
  updateCharCounter(textarea);
  textarea.addEventListener('input', () => {
    updateCharCounter(textarea);
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

function clearChatFeedbackError() {
  const feedback = document.getElementById('chat-feedback');
  if (!feedback) {
    return;
  }
  feedback.classList.remove('chat-feedback--error');
}

function isMentionPollerElement(element) {
  return Boolean(element && MENTION_POLLER_IDS.has(element.id));
}

function getMentionPollers() {
  return Array.from(document.querySelectorAll('#chat-mentions-poller, #chat-mentions-badge-poller'));
}

function getMentionsHealthElement() {
  return document.getElementById('chat-mentions-health');
}

function getMentionIntervalMs(failureCount) {
  if (failureCount <= 0) {
    return MENTION_BASE_INTERVAL_MS;
  }
  return Math.min(MENTION_BASE_INTERVAL_MS * (2 ** failureCount), MENTION_MAX_INTERVAL_MS);
}

function getMentionJitterMs() {
  if (mentionPollingPausedByVisibility) {
    return 0;
  }
  return Math.floor(Math.random() * (MENTION_JITTER_MAX_MS + 1));
}

function applyMentionPollInterval(intervalMs) {
  const normalizedInterval = Math.max(1000, intervalMs + getMentionJitterMs());
  const seconds = Math.max(1, Math.round(normalizedInterval / 1000));
  const trigger = mentionPollingPausedByVisibility
    ? 'mentions:refresh'
    : `mentions:refresh, every ${seconds}s`;
  getMentionPollers().forEach((poller) => {
    poller.setAttribute('hx-trigger', trigger);
    if (window.htmx && typeof window.htmx.process === 'function') {
      window.htmx.process(poller);
    }
  });
}

function setMentionsHealthDegraded(intervalMs) {
  const health = getMentionsHealthElement();
  if (!health) {
    return;
  }
  const seconds = Math.max(1, Math.round(intervalMs / 1000));
  const template = health.dataset.pollingDegradedTemplate
    || 'Notificacoes de mencoes temporariamente indisponiveis. Nova tentativa em {0}s.';
  health.textContent = template.replace('{0}', String(seconds));
  health.hidden = false;
  health.setAttribute('aria-hidden', 'false');
}

function setMentionsHealthRecovered() {
  const health = getMentionsHealthElement();
  if (!health) {
    return;
  }
  const message = health.dataset.pollingRecoveredMessage || 'Notificacoes de mencoes normalizadas.';
  health.textContent = message;
  health.hidden = false;
  health.setAttribute('aria-hidden', 'false');

  if (mentionPollingRecoveryTimeoutId) {
    window.clearTimeout(mentionPollingRecoveryTimeoutId);
  }
  mentionPollingRecoveryTimeoutId = window.setTimeout(() => {
    const current = getMentionsHealthElement();
    if (!current) {
      return;
    }
    current.hidden = true;
    current.setAttribute('aria-hidden', 'true');
    current.textContent = '';
    mentionPollingRecoveryTimeoutId = 0;
  }, MENTION_RECOVERY_HIDE_DELAY_MS);
}

function setMentionsHealthWarmupRecovered() {
  const health = getMentionsHealthElement();
  if (!health) {
    return;
  }
  const message = health.dataset.pollingWarmupRecoveredMessage || 'Notificacoes de mencoes sincronizadas.';
  health.textContent = message;
  health.hidden = false;
  health.setAttribute('aria-hidden', 'false');

  if (mentionPollingRecoveryTimeoutId) {
    window.clearTimeout(mentionPollingRecoveryTimeoutId);
  }
  mentionPollingRecoveryTimeoutId = window.setTimeout(() => {
    const current = getMentionsHealthElement();
    if (!current) {
      return;
    }
    current.hidden = true;
    current.setAttribute('aria-hidden', 'true');
    current.textContent = '';
    mentionPollingRecoveryTimeoutId = 0;
  }, MENTION_WARMUP_HIDE_DELAY_MS);
}

function markMentionPollingFailure() {
  mentionPollingFailureCount += 1;
  const intervalMs = getMentionIntervalMs(mentionPollingFailureCount);
  applyMentionPollInterval(intervalMs);
  setMentionsHealthDegraded(intervalMs);
}

function markMentionPollingSuccess() {
  if (!mentionPollingWarmupShown) {
    mentionPollingWarmupShown = true;
    setMentionsHealthWarmupRecovered();
  }

  if (mentionPollingFailureCount <= 0) {
    applyMentionPollInterval(MENTION_BASE_INTERVAL_MS);
    return;
  }
  mentionPollingFailureCount = 0;
  applyMentionPollInterval(MENTION_BASE_INTERVAL_MS);
  setMentionsHealthRecovered();
}

function parseMentionAckIds(rawValue) {
  if (!rawValue) {
    return [];
  }
  const unique = new Set();
  return rawValue
    .split(',')
    .map((value) => Number.parseInt(value.trim(), 10))
    .filter((value) => Number.isInteger(value) && value > 0)
    .filter((value) => {
      if (unique.has(value)) {
        return false;
      }
      unique.add(value);
      return true;
    });
}

function buildMentionAckBody(ids) {
  const params = new URLSearchParams();
  params.set('chatMencoesAckIds', ids.join(','));
  const csrfTokenField = document.querySelector('#logoutForm input[type="hidden"][name][value], input[type="hidden"][name="_csrf"]');
  if (csrfTokenField instanceof HTMLInputElement
    && csrfTokenField.name
    && csrfTokenField.value) {
    params.set(csrfTokenField.name, csrfTokenField.value);
  }
  return params.toString();
}

function refreshMentionPollers() {
  if (!window.htmx || typeof window.htmx.trigger !== 'function') {
    return;
  }
  getMentionPollers().forEach((poller) => {
    window.htmx.trigger(poller, 'mentions:refresh');
  });
}

function bindMentionPollingVisibility() {
  if (document[MENTION_VISIBILITY_BIND_FLAG]) {
    return;
  }
  document[MENTION_VISIBILITY_BIND_FLAG] = true;

  const onVisibilityChange = () => {
    const hidden = document.visibilityState === 'hidden';
    mentionPollingPausedByVisibility = hidden;
    const intervalMs = getMentionIntervalMs(mentionPollingFailureCount);
    applyMentionPollInterval(intervalMs);
    if (!hidden) {
      refreshMentionPollers();
    }
  };

  document.addEventListener('visibilitychange', onVisibilityChange);
  onVisibilityChange();
}

async function acknowledgeMentionBatch(mentionContainer) {
  if (!mentionContainer) {
    return;
  }
  const ackUrl = mentionContainer.dataset.chatMentionsAckUrl;
  const mentionIds = parseMentionAckIds(mentionContainer.dataset.chatMentionsAckIds || '');
  if (!ackUrl || mentionIds.length === 0 || typeof window.fetch !== 'function') {
    return;
  }

  const requestKey = `${ackUrl}::${mentionIds.join(',')}`;
  if (mentionAckInFlight.has(requestKey)) {
    return;
  }
  mentionAckInFlight.add(requestKey);

  try {
    const response = await window.fetch(ackUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
        Accept: 'text/html, */*; q=0.01',
        'X-Requested-With': 'XMLHttpRequest',
      },
      credentials: 'same-origin',
      body: buildMentionAckBody(mentionIds),
    });
    if (!response.ok && response.status !== 204) {
      throw new Error(`ACK failed with status ${response.status}`);
    }
    refreshMentionPollers();
  } catch (error) {
    markMentionPollingFailure();
  } finally {
    mentionAckInFlight.delete(requestKey);
  }
}

function bindMentionPollingFeedback() {
  if (document[MENTION_BIND_FLAG]) {
    return;
  }
  document[MENTION_BIND_FLAG] = true;

  document.addEventListener('htmx:afterRequest', (event) => {
    const elt = event?.detail?.elt;
    if (!isMentionPollerElement(elt)) {
      return;
    }
    if (event?.detail?.successful) {
      markMentionPollingSuccess();
      return;
    }
    markMentionPollingFailure();
  });

  document.addEventListener('htmx:afterSwap', (event) => {
    const target = event?.detail?.target;
    if (!target || target.id !== 'chat-mentions-poller') {
      return;
    }
    acknowledgeMentionBatch(target);
  });
}

function hideNewMessagesIndicator() {
  const indicator = getNewMessagesIndicator();
  if (!indicator) {
    return;
  }
  indicator.hidden = true;
  indicator.setAttribute('aria-hidden', 'true');
  unseenMessagesCount = 0;
}

function showNewMessagesIndicator() {
  const indicator = getNewMessagesIndicator();
  const label = getNewMessagesIndicatorLabel();
  if (!indicator || !label) {
    return;
  }
  const suffix = unseenMessagesCount > 1 ? ` (${unseenMessagesCount})` : '';
  label.textContent = `Novas mensagens${suffix} · Ir para o final`;
  indicator.hidden = false;
  indicator.setAttribute('aria-hidden', 'false');
}

function onMessagesAppended(newMessages) {
  if (newMessages <= 0) {
    return;
  }
  if (userReadingHistory) {
    unseenMessagesCount += newMessages;
    showNewMessagesIndicator();
    return;
  }
  hideNewMessagesIndicator();
}

function updateCharCounter(textarea) {
  const counter = getCharCounter();
  if (!counter || !textarea) {
    return;
  }
  const max = Number.parseInt(textarea.getAttribute('maxlength') || '300', 10);
  const current = textarea.value.length;
  counter.textContent = `${current}/${max}`;
}

function clearReplyContext() {
  const input = getReplyMessageIdInput();
  const container = getReplyContextContainer();
  const label = getReplyContextLabel();
  const preview = getReplyContextPreview();
  if (input) {
    input.value = '';
  }
  if (label) {
    label.textContent = '';
  }
  if (preview) {
    preview.textContent = '';
  }
  if (container) {
    container.hidden = true;
    container.setAttribute('aria-hidden', 'true');
  }
}

function bindReplyActions(list, form) {
  if (!list || !form) {
    return;
  }
  const input = getReplyMessageIdInput();
  const container = getReplyContextContainer();
  const label = getReplyContextLabel();
  const preview = getReplyContextPreview();
  const textarea = form.querySelector('textarea[name="chatMensagem"]');
  const cancelButton = document.getElementById('chat-reply-cancel');

  if (cancelButton) {
    cancelButton.addEventListener('click', () => {
      clearReplyContext();
      if (textarea) {
        textarea.focus();
      }
    });
  }

  list.addEventListener('click', (event) => {
    const button = event.target instanceof Element
      ? event.target.closest('.chat-message__reply-action')
      : null;
    if (!button) {
      return;
    }
    const replyId = Number.parseInt(button.getAttribute('data-chat-reply-id') || '', 10);
    const replyAuthor = (button.getAttribute('data-chat-reply-author') || '').trim();
    const replyText = (button.getAttribute('data-chat-reply-text') || '').trim();
    if (!Number.isInteger(replyId) || replyId <= 0 || !input || !container || !label || !preview) {
      return;
    }
    input.value = String(replyId);
    label.textContent = `Respondendo a ${replyAuthor || 'mensagem'}`;
    preview.textContent = replyText.length > 120 ? `${replyText.slice(0, 120).trim()}...` : replyText;
    container.hidden = false;
    container.setAttribute('aria-hidden', 'false');
    if (textarea) {
      textarea.focus();
    }
  });
}

function bindQueryClear() {
  const clearButton = document.getElementById('chat-query-clear');
  const form = document.getElementById('chat-query-form');
  const results = document.getElementById('chat-query-results');
  if (!clearButton || !form || !results) {
    return;
  }
  clearButton.addEventListener('click', () => {
    form.reset();
    results.innerHTML = '<p class="chat-empty">Use os filtros acima para consultar mensagens antigas sem perder o fluxo atual.</p>';
  });
}

function setSendButtonState(form, sending) {
  if (!form) {
    return;
  }
  const button = form.querySelector('button[type="submit"]');
  if (!button) {
    return;
  }
  button.disabled = sending;
  button.textContent = sending ? SEND_LABEL_PENDING : SEND_LABEL_DEFAULT;
  form.setAttribute('aria-busy', sending ? 'true' : 'false');
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
  const match = beforeCursor.match(/(^|\s)@([A-Za-z0-9._-]*)$/);
  if (!match) {
    return null;
  }
  const prefix = match[2] || '';
  const tokenStart = beforeCursor.length - match[0].length;
  return {
    prefix,
    start: tokenStart,
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
  container.removeAttribute('aria-activedescendant');
  activeSuggestionIndex = -1;
  currentSuggestions = [];
  mentionTokenContext = null;
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
    items[activeSuggestionIndex].setAttribute('aria-selected', 'false');
  }
  activeSuggestionIndex = index;
  if (activeSuggestionIndex < 0) {
    activeSuggestionIndex = 0;
  } else if (activeSuggestionIndex >= items.length) {
    activeSuggestionIndex = items.length - 1;
  }
  const active = items[activeSuggestionIndex];
  active.classList.add('chat-autocomplete__item--active');
  active.setAttribute('aria-selected', 'true');
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
    item.setAttribute('aria-selected', 'false');
    item.textContent = suggestion;
    item.addEventListener('mousedown', (event) => {
      event.preventDefault();
    });
    item.addEventListener('mouseenter', () => {
      setActiveSuggestion(index);
    });
    item.addEventListener('click', () => {
      const token = mentionTokenContext || findMentionToken(textarea.value, textarea.selectionStart);
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
  mentionTokenContext = token;
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
    const token = mentionTokenContext || findMentionToken(textarea.value, textarea.selectionStart);
    if (token && currentSuggestions[activeSuggestionIndex]) {
      event.preventDefault();
      acceptSuggestion(textarea, token, currentSuggestions[activeSuggestionIndex]);
    }
    return;
  }
  if (event.key === 'Tab') {
    const token = mentionTokenContext || findMentionToken(textarea.value, textarea.selectionStart);
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
  const updateReadState = () => {
    lockedScrollTop = list.scrollTop;
    userReadingHistory = !isAtBottom(list);
    if (!userReadingHistory) {
      hideNewMessagesIndicator();
    }
  };
  updateReadState();
  list.addEventListener('scroll', () => {
    updateReadState();
  });
}

function bindNewMessagesCTA(list) {
  const indicator = getNewMessagesIndicator();
  if (!indicator || !list) {
    return;
  }
  indicator.addEventListener('click', () => {
    userReadingHistory = false;
    scrollListToBottom(list);
    hideNewMessagesIndicator();
  });
}

export function initChatPage() {
  bindMentionPollingFeedback();
  bindMentionPollingVisibility();

  const form = document.getElementById('chat-send-form');
  if (!form) {
    return;
  }

  const list = getChatMessagesList();
  scrollListToBottom(list);
  userReadingHistory = false;
  lockedScrollTop = 0;
  previousMessageCount = getMessageCount(list);
  unseenMessagesCount = 0;
  hideEmptyStateWhenHasMessages();
  hideNewMessagesIndicator();
  bindShortcutToSend(form);
  bindMentionsAutocomplete(form);
  bindCharCounter(form);
  bindScrollLock(list);
  bindNewMessagesCTA(list);
  bindReplyActions(list, form);
  bindQueryClear();
  setSendButtonState(form, false);
  clearReplyContext();

  document.addEventListener('htmx:beforeSwap', (event) => {
    const target = event?.detail?.target;
    if (!target || target.id !== 'chat-messages-list') {
      return;
    }
    lockedScrollTop = target.scrollTop;
    userReadingHistory = !isAtBottom(target);
    previousMessageCount = getMessageCount(target);
  });

  document.addEventListener('htmx:afterSwap', (event) => {
    const target = event && event.detail ? event.detail.target : null;
    if (!target || target.id !== 'chat-messages-list') {
      return;
    }
    hideEmptyStateWhenHasMessages();
    const currentCount = getMessageCount(target);
    onMessagesAppended(currentCount - previousMessageCount);
    previousMessageCount = currentCount;
    autoScrollIfAllowed(getChatMessagesList());
  });

  document.addEventListener('htmx:beforeRequest', (event) => {
    const elt = event?.detail?.elt;
    if (!elt || elt.id !== 'chat-send-form') {
      return;
    }
    setSendButtonState(elt, true);
  });

  document.addEventListener('htmx:afterRequest', (event) => {
    const elt = event?.detail?.elt;
    if (!elt || elt.id !== 'chat-send-form') {
      return;
    }
    setSendButtonState(elt, false);
    const textarea = elt.querySelector('textarea[name="chatMensagem"]');
    updateCharCounter(textarea);
    if (event?.detail?.successful && textarea) {
      textarea.value = '';
      updateCharCounter(textarea);
      clearReplyContext();
      clearChatFeedbackError();
    }
  });

  document.addEventListener('htmx:responseError', (event) => {
    const elt = event?.detail?.elt;
    if (!elt || elt.id !== 'chat-send-form') {
      return;
    }
    setSendButtonState(elt, false);
    setChatErrorFeedback('Falha ao enviar mensagem. Tente novamente.');
  });
}
