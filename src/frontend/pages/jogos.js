const DEBUG_LABEL = '[bolao:jogos]';

const state = {
  expandedMatchId: null,
  lastInlineTrigger: null,
  lastPanelTrigger: null,
  meusPalpitesLoaded: false,
  initialized: false,
};

function debugInfo(message, detail) {
  if (!window.console || !console.info) {
    return;
  }
  if (detail !== undefined) {
    console.info(`${DEBUG_LABEL} ${message}`, detail);
    return;
  }
  console.info(`${DEBUG_LABEL} ${message}`);
}

function debugWarn(message, detail) {
  if (!window.console || !console.warn) {
    return;
  }
  if (detail !== undefined) {
    console.warn(`${DEBUG_LABEL} ${message}`, detail);
    return;
  }
  console.warn(`${DEBUG_LABEL} ${message}`);
}

function getBaseUrl() {
  return window.APP_BASE_URL || '';
}

function toggleCollapse(containerId, trigger) {
  if (!containerId || !trigger) {
    return;
  }
  const content = document.getElementById(`${containerId}_content`);
  if (!content) {
    debugWarn('Container de colapso não encontrado.', { containerId });
    return;
  }
  const hidden = content.classList.toggle('collapsible-portlet__content--hidden');
  trigger.src = hidden ? `${getBaseUrl()}/img/arrow_right.png` : `${getBaseUrl()}/img/arrow_down.png`;
}

function atualizarResultado(input) {
  if (!input) {
    return;
  }
  const jogoId = input.id.substring(input.id.lastIndexOf('_') + 1);
  const golsEquipe1Field = document.getElementById(`golsEquipe1_tf_${jogoId}`);
  const golsEquipe1 = golsEquipe1Field && golsEquipe1Field.value !== '' ? golsEquipe1Field.value : '-1';
  const golsEquipe2 = input.value !== '' ? input.value : '-1';

  fetch(`${getBaseUrl()}/admin/atualizarResultadoJogo.action`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams({
      id: jogoId,
      golsEquipe1,
      golsEquipe2,
    }),
  }).catch(() => {
    debugWarn('Falha ao atualizar resultado.', { jogoId });
  });
}

function carregarMeusPalpites(force = false) {
  if (!window.htmx) {
    debugWarn('HTMX não disponível para carregar palpites.');
    return;
  }
  if (!force && state.meusPalpitesLoaded) {
    return;
  }
  debugInfo('Carregando meus palpites.', { force });
  const request = window.htmx.ajax('GET', `${getBaseUrl()}/seguro/meusPalpitesPartial.action`, {
    target: '#todos_palpites_table',
    swap: 'innerHTML',
  });
  request.addEventListener('loadend', () => {
    state.meusPalpitesLoaded = request.status >= 200 && request.status < 300;
  });
}

function mostrarMeusPalpites() {
  const painel = document.getElementById('todos_palpites_div');
  if (!painel) {
    return;
  }
  painel.classList.add('tips-panel--visible');
  carregarMeusPalpites(false);
}

function fecharMeusPalpites() {
  const painel = document.getElementById('todos_palpites_div');
  if (!painel) {
    return;
  }
  painel.classList.remove('tips-panel--visible');
}

function isMeusPalpitesOpen() {
  const painel = document.getElementById('todos_palpites_div');
  return painel ? painel.classList.contains('tips-panel--visible') : false;
}

function storeInlineInitialState() {
  document.querySelectorAll('.match-expand').forEach((row) => {
    if (!row.dataset.initialContent) {
      row.dataset.initialContent = row.innerHTML;
    }
  });
}

function restoreInlinePlaceholder(expandRow) {
  if (!expandRow) {
    return;
  }
  if (expandRow.dataset.initialContent) {
    expandRow.innerHTML = expandRow.dataset.initialContent;
  }
}

function findMatchRow(matchId) {
  if (!matchId) {
    return null;
  }
  return document.querySelector(`.match-row[data-jogo-id="${matchId}"]`);
}

function updatePalpiteSummaryCell(matchRow) {
  const palpiteCell = matchRow.querySelector('.match-table__palpite');
  if (!palpiteCell) {
    return;
  }
  const gols1 = matchRow.dataset.palpiteGols1;
  const gols2 = matchRow.dataset.palpiteGols2;
  const placeholder = matchRow.dataset.palpitePlaceholder || '';
  if (gols1 !== undefined && gols1 !== '' && gols2 !== undefined && gols2 !== '') {
    palpiteCell.innerHTML = `<span class="palpite-score">${gols1} x ${gols2}</span>`;
  } else {
    palpiteCell.innerHTML = `<span class="palpite-placeholder">${placeholder}</span>`;
  }
}

function updatePalpiteStatusBadge(matchRow) {
  const statusCell = matchRow.querySelector('.match-table__status');
  if (!statusCell) {
    return;
  }
  const status = matchRow.dataset.palpiteStatus || 'pending';
  const label = matchRow.dataset.palpiteStatusLabel || status;
  let badge = statusCell.querySelector('.badge');
  if (!badge) {
    badge = document.createElement('span');
    badge.classList.add('badge');
    statusCell.innerHTML = '';
    statusCell.appendChild(badge);
  }
  badge.className = `badge badge--${status}`;
  badge.textContent = label;
}

function updateMatchRowActions(matchRow) {
  const editButton = matchRow.querySelector('[data-js="abrir-palpite-inline"]');
  if (!editButton) {
    return;
  }
  const allowed = matchRow.dataset.palpiteAllowed === 'true';
  if (allowed) {
    editButton.disabled = false;
    editButton.removeAttribute('aria-disabled');
  } else {
    editButton.disabled = true;
    editButton.setAttribute('aria-disabled', 'true');
  }
}

function updateMatchRowUI(matchRow) {
  if (!matchRow) {
    return;
  }
  updatePalpiteSummaryCell(matchRow);
  updatePalpiteStatusBadge(matchRow);
  updateMatchRowActions(matchRow);
}

function syncMatchRowFromExpand(expandRow) {
  const parentId = expandRow.dataset.parent;
  if (!parentId) {
    return;
  }
  const meta = expandRow.querySelector('[data-palpite-meta="true"]');
  if (!meta) {
    return;
  }
  const matchRow = findMatchRow(parentId);
  if (!matchRow) {
    return;
  }

  const {
    palpiteStatus,
    palpiteAllowed,
    palpiteGols1,
    palpiteGols2,
    palpiteStatusLabel,
    palpitePlaceholder,
    palpiteLockedReason,
  } = meta.dataset;
  if (palpiteStatus !== undefined) {
    matchRow.dataset.palpiteStatus = palpiteStatus;
  }
  if (palpiteAllowed !== undefined) {
    matchRow.dataset.palpiteAllowed = palpiteAllowed;
  }
  if (palpiteGols1 !== undefined) {
    matchRow.dataset.palpiteGols1 = palpiteGols1;
  }
  if (palpiteGols2 !== undefined) {
    matchRow.dataset.palpiteGols2 = palpiteGols2;
  }
  if (palpiteStatusLabel !== undefined) {
    matchRow.dataset.palpiteStatusLabel = palpiteStatusLabel;
  }
  if (palpitePlaceholder !== undefined) {
    matchRow.dataset.palpitePlaceholder = palpitePlaceholder;
  }
  if (palpiteLockedReason !== undefined) {
    matchRow.dataset.palpiteLockedReason = palpiteLockedReason;
  } else {
    delete matchRow.dataset.palpiteLockedReason;
  }
  updateMatchRowUI(matchRow);
}

function openInlineRow(trigger) {
  const matchRow = trigger.closest('.match-row');
  if (!matchRow) {
    debugWarn('Botão de palpite sem linha associada.', trigger);
    return;
  }
  const matchId = matchRow.dataset.jogoId;
  if (!matchId) {
    debugWarn('match-row sem data-jogo-id.', matchRow);
    return;
  }

  if (state.expandedMatchId && state.expandedMatchId !== matchId) {
    closeInlineRow(state.expandedMatchId, { returnFocus: false, resetContent: false });
  }

  const expandRow = document.getElementById(`match-expand_${matchId}`);
  if (!expandRow) {
    debugWarn('Linha expandida não encontrada.', { matchId });
    return;
  }

  expandRow.hidden = false;
  matchRow.classList.add('match-row--expanded');
  trigger.setAttribute('aria-expanded', 'true');
  trigger.dataset.inlineOpen = 'true';

  const placeholder = expandRow.querySelector('.palpite-inline__placeholder');
  if (placeholder) {
    const loadingMessage = placeholder.dataset.loadingMessage || placeholder.textContent;
    placeholder.textContent = loadingMessage;
  }

  state.expandedMatchId = matchId;
  state.lastInlineTrigger = trigger;
}

function closeInlineRow(matchId, options = {}) {
  const { returnFocus = true, resetContent = true } = options;
  const matchRow = document.querySelector(`.match-row[data-jogo-id="${matchId}"]`);
  const expandRow = document.getElementById(`match-expand_${matchId}`);
  if (!matchRow || !expandRow) {
    return;
  }

  expandRow.hidden = true;
  if (resetContent) {
    restoreInlinePlaceholder(expandRow);
  }
  matchRow.classList.remove('match-row--expanded');

  const trigger = matchRow.querySelector('[data-js="abrir-palpite-inline"]');
  if (trigger) {
    trigger.setAttribute('aria-expanded', 'false');
    trigger.dataset.inlineOpen = 'false';
    if (returnFocus) {
      trigger.focus();
    }
  }

  if (state.expandedMatchId === matchId) {
    state.expandedMatchId = null;
  }
  if (returnFocus) {
    state.lastInlineTrigger = trigger || null;
  }
}

function isPanelOpen() {
  const panel = document.getElementById('palpite-panel');
  return panel ? !panel.hasAttribute('hidden') : false;
}

function openPanel(trigger) {
  const panel = document.getElementById('palpite-panel');
  const backdrop = document.getElementById('palpite-panel-backdrop');
  if (!panel || !backdrop) {
    debugWarn('Painel de palpites não encontrado.');
    return;
  }
  panel.removeAttribute('hidden');
  panel.setAttribute('aria-hidden', 'false');
  backdrop.removeAttribute('hidden');
  backdrop.setAttribute('aria-hidden', 'false');
  document.documentElement.classList.add('dialog-open');
  state.lastPanelTrigger = trigger;
}

function closePanel(options = {}) {
  const { returnFocus = true } = options;
  const panel = document.getElementById('palpite-panel');
  const backdrop = document.getElementById('palpite-panel-backdrop');
  if (!panel || !backdrop) {
    return;
  }
  panel.setAttribute('hidden', 'hidden');
  panel.setAttribute('aria-hidden', 'true');
  backdrop.setAttribute('hidden', 'hidden');
  backdrop.setAttribute('aria-hidden', 'true');
  document.documentElement.classList.remove('dialog-open');
  if (returnFocus && state.lastPanelTrigger) {
    state.lastPanelTrigger.focus();
  }
  state.lastPanelTrigger = null;
}

function handleBeforeRequest(event) {
  if (!state.initialized) {
    return;
  }
  const trigger = event.detail && event.detail.elt;
  if (!trigger) {
    return;
  }

  if (trigger.matches('[data-js="abrir-palpite-inline"]')) {
    if (!trigger.disabled) {
      trigger.dataset.wasDisabled = 'false';
      trigger.disabled = true;
      trigger.setAttribute('aria-busy', 'true');
    } else {
      trigger.dataset.wasDisabled = 'true';
    }
    openInlineRow(trigger);
  } else if (trigger.matches('[data-js="abrir-palpite-panel"]')) {
    if (!trigger.disabled) {
      trigger.dataset.wasDisabled = 'false';
      trigger.disabled = true;
      trigger.setAttribute('aria-busy', 'true');
    } else {
      trigger.dataset.wasDisabled = 'true';
    }
    openPanel(trigger);
  }
}

function focusFirstInteractive(target) {
  const focusable = target.querySelector('input, select, textarea, button, [tabindex]:not([tabindex="-1"])');
  if (focusable && typeof focusable.focus === 'function') {
    focusable.focus({ preventScroll: true });
  }
}

function handleAfterSwap(event) {
  if (!state.initialized) {
    return;
  }
  const { target } = event;
  if (!(target instanceof HTMLElement)) {
    return;
  }

  if (target.classList.contains('match-expand')) {
    target.hidden = false;
    const parentId = target.dataset.parent;
    if (parentId) {
      state.expandedMatchId = parentId;
      syncMatchRowFromExpand(target);
    }
    if (target.querySelector('.palpite-inline__feedback--success')) {
      if (isMeusPalpitesOpen()) {
        carregarMeusPalpites(true);
      }
      debugInfo('Palpite salvo; painel "meus palpites" atualizado.');
    }
    focusFirstInteractive(target);
  } else if (target.id === 'palpite-panel') {
    target.removeAttribute('hidden');
    target.setAttribute('aria-hidden', 'false');
    focusFirstInteractive(target);
  }
}

function handleAfterRequest(event) {
  if (!state.initialized) {
    return;
  }
  const trigger = event.detail && event.detail.elt;
  if (!trigger) {
    return;
  }
  if (trigger.dataset && trigger.dataset.wasDisabled === 'false') {
    trigger.disabled = false;
    trigger.removeAttribute('aria-busy');
  }
  if (trigger.dataset) {
    delete trigger.dataset.wasDisabled;
  }
  if (!event.detail.successful && trigger.matches('[data-js="abrir-palpite-inline"]')) {
    debugWarn('Falha ao carregar formulário de palpite.', { status: event.detail.xhr && event.detail.xhr.status });
  }
}

function handleGlobalClick(event) {
  const cancelButton = event.target.closest('[data-js="cancelar-palpite-inline"]');
  if (cancelButton) {
    event.preventDefault();
    const expandRow = cancelButton.closest('.match-expand');
    const parentId = expandRow && expandRow.dataset.parent;
    if (parentId) {
      closeInlineRow(parentId);
    }
    return;
  }

  const closePanelButton = event.target.closest('[data-js="fechar-palpite-panel"]');
  if (closePanelButton) {
    event.preventDefault();
    closePanel();
    return;
  }

  if (event.target.id === 'palpite-panel-backdrop') {
    event.preventDefault();
    closePanel();
  }
}

function handleDocumentKeydown(event) {
  if (event.key !== 'Escape') {
    return;
  }
  if (isPanelOpen()) {
    event.preventDefault();
    closePanel();
    return;
  }
  if (state.expandedMatchId) {
    event.preventDefault();
    closeInlineRow(state.expandedMatchId);
  }
}

function initCollapsePortlets() {
  document.querySelectorAll('[data-js="collapse-container"]').forEach((icon) => {
    icon.addEventListener('click', () => {
      const targetId = icon.getAttribute('data-target');
      toggleCollapse(targetId, icon);
    });
  });
}

function initMeusPalpites() {
  const mostrarLink = document.getElementById('mostrarMeusPalpitesLink');
  if (mostrarLink) {
    mostrarLink.addEventListener('click', (event) => {
      event.preventDefault();
      mostrarMeusPalpites();
    });
  }
  const fecharButton = document.querySelector('[data-js="fechar-meus-palpites"]');
  if (fecharButton) {
    fecharButton.addEventListener('click', () => fecharMeusPalpites());
  }
  const recarregarButton = document.querySelector('[data-js="recarregar-meus-palpites"]');
  if (recarregarButton) {
    recarregarButton.addEventListener('click', () => carregarMeusPalpites(true));
  }
}

function initResultadosAdmin() {
  document.body.addEventListener('change', (event) => {
    const input = event.target;
    if (!(input instanceof HTMLInputElement)) {
      return;
    }
    if (input.dataset.js === 'resultado-input') {
      atualizarResultado(input);
    }
  });
}

function initPalpiteInline() {
  storeInlineInitialState();
  document.body.addEventListener('htmx:beforeRequest', handleBeforeRequest);
  document.body.addEventListener('htmx:afterSwap', handleAfterSwap);
  document.body.addEventListener('htmx:afterRequest', handleAfterRequest);
  document.body.addEventListener('click', handleGlobalClick);
}

function initGlobalKeyListener() {
  document.addEventListener('keydown', handleDocumentKeydown);
}

export function initJogosPage() {
  const wrapper = document.getElementById('jogos-page-wrapper');
  if (!wrapper) {
    debugInfo('Wrapper de jogos não encontrado; initJogosPage ignorado.');
    return;
  }
  if (state.initialized) {
    debugInfo('initJogosPage já executado; ignorando chamada subsequente.');
    return;
  }
  state.initialized = true;

  window.__bolaoJogosDebug = window.__bolaoJogosDebug || {};
  window.__bolaoJogosDebug.moduleLoadedAt = new Date().toISOString();

  initCollapsePortlets();
  initPalpiteInline();
  initMeusPalpites();
  initResultadosAdmin();
  initGlobalKeyListener();

  debugInfo('initJogosPage concluído.');
}
