const DEBUG_LABEL = '[bolao:jogos]';

const state = {
  expandedMatchId: null,
  lastInlineTrigger: null,
  initialized: false,
  autoSaveTimers: {},
  globalStatusTimer: null,
  dirtyByMatch: {},
  pendingAdminRequests: 0,
  lastAdminTriggerByRow: {},
  lastSavedByMatch: {},
};

const FILTER_STORAGE_KEY = 'bolao:filtro:collapsed';

function syncDebugState() {
  window.__bolaoJogosDebug = window.__bolaoJogosDebug || {};
  window.__bolaoJogosDebug.pendingAdminRequests = state.pendingAdminRequests;
  window.__bolaoJogosDebug.hasDirtyMatch = getHasDirtyMatch();
}

function initFiltroColapsavel() {
  const portlet = document.querySelector('.match-filter-portlet');
  if (!portlet) return;
  // Em mobile (<768px) mantém aberto por padrão
  if (window.innerWidth < 768) return;

  const isCollapsed = sessionStorage.getItem(FILTER_STORAGE_KEY) !== 'false';
  const content = portlet.querySelector('.collapsible-portlet__content');
  const toggle = portlet.querySelector('[data-js="collapse-container"]');

  if (isCollapsed && content) {
    portlet.classList.add('filter-collapsed');
    if (toggle) toggle.src = toggle.src.replace('arrow_down', 'arrow_right');
  }

  if (toggle) {
    toggle.addEventListener('click', () => {
      const collapsed = portlet.classList.toggle('filter-collapsed');
      sessionStorage.setItem(FILTER_STORAGE_KEY, collapsed ? 'true' : 'false');
    });
  }
}

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

function syncGroupToggleA11y(button, expanded) {
  if (!button) {
    return;
  }
  button.setAttribute('aria-expanded', expanded ? 'true' : 'false');
  button.setAttribute('aria-label', expanded ? 'Ocultar palpites do grupo' : 'Ver palpites do grupo');
}

function requestGroupDetails(groupToggle) {
  if (!groupToggle) {
    return;
  }
  const hxGet = groupToggle.getAttribute('hx-get');
  const hxTarget = groupToggle.getAttribute('hx-target');
  if (!hxGet || !hxTarget) {
    debugWarn('Botão de grupo sem atributos HTMX para carregamento.', { groupToggle });
    return;
  }
  if (groupToggle.dataset.groupLoaded === 'true' || groupToggle.dataset.groupLoading === 'true') {
    return;
  }
  groupToggle.dataset.groupLoading = 'true';
  const target = document.querySelector(hxTarget);
  if (!target) {
    delete groupToggle.dataset.groupLoading;
    debugWarn('Alvo de palpites do grupo não encontrado.', { hxTarget });
    return;
  }
  target.innerHTML = '<tr><td colspan="3" class="text-center">Carregando palpites...</td></tr>';
  const requestUrl = new URL(hxGet, window.location.origin).toString();
  const loadWithFetch = () => fetch(requestUrl, {
    method: 'GET',
    headers: {
      'HX-Request': 'true',
    },
    credentials: 'same-origin',
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      return response.text();
    })
    .then((html) => {
      target.innerHTML = html;
      groupToggle.dataset.groupLoaded = 'true';
      delete groupToggle.dataset.groupLoading;
    })
    .catch((error) => {
      delete groupToggle.dataset.groupLoading;
      target.innerHTML = '<tr><td colspan="3" class="text-center">Erro ao carregar palpites do grupo.</td></tr>';
      debugWarn('Falha ao carregar palpites do grupo.', { error: error && error.message });
    });
  if (window.htmx && typeof window.htmx.ajax === 'function') {
    window.htmx.ajax('GET', hxGet, {
      // Usa o elemento já resolvido para evitar ambiguidades de resolução do alvo
      // em diferentes versões/integrações do HTMX.
      target,
      swap: 'innerHTML',
    });
    // Fallback defensivo: se o swap HTMX não ocorrer (ex.: conflito de integração),
    // recarrega via fetch sem depender de evento global.
    window.setTimeout(() => {
      if (groupToggle.dataset.groupLoading === 'true') {
        loadWithFetch();
      }
    }, 1200);
    return;
  }
  loadWithFetch();
}

function getBaseUrl() {
  return window.APP_BASE_URL || '';
}

function getPageWrapper() {
  return document.getElementById('jogos-page-wrapper');
}

function getUiMessage(key, fallback) {
  const wrapper = getPageWrapper();
  if (!wrapper) {
    return fallback;
  }
  const value = wrapper.dataset[key];
  return value && value.trim() ? value : fallback;
}

function getNowHHMM() {
  return new Date().toLocaleTimeString('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'America/Sao_Paulo',
  });
}

function announceGlobalStatus(message, kind = 'info') {
  const el = document.getElementById('jogos-global-status');
  if (!el || !message) {
    return;
  }

  el.textContent = message;
  el.classList.remove(
    'jogos-global-status--success',
    'jogos-global-status--error',
    'jogos-global-status--info',
    'jogos-global-status--visible',
  );
  el.classList.add('jogos-global-status--visible', `jogos-global-status--${kind}`);

  if (state.globalStatusTimer) {
    clearTimeout(state.globalStatusTimer);
  }
  state.globalStatusTimer = setTimeout(() => {
    el.classList.remove('jogos-global-status--visible');
  }, 3200);
}

function getMatchIdFromTrigger(trigger) {
  if (!trigger || !(trigger instanceof HTMLElement)) {
    return null;
  }
  if (trigger.dataset && trigger.dataset.jogoId) {
    return trigger.dataset.jogoId;
  }
  const form = trigger.closest('form');
  const hiddenJogoId = form ? form.querySelector('input[name="jogoId"]') : null;
  if (hiddenJogoId && hiddenJogoId.value) {
    return hiddenJogoId.value;
  }
  const cell = trigger.closest('[id^="palpite-cell_"]');
  if (cell && cell.id) {
    return cell.id.replace('palpite-cell_', '');
  }
  const row = trigger.closest('.match-row[data-jogo-id]');
  if (row && row.dataset.jogoId) {
    return row.dataset.jogoId;
  }
  return null;
}

function setMatchEditState(jogoId, editState) {
  if (!jogoId) {
    return;
  }
  const cell = document.getElementById(`palpite-cell_${jogoId}`);
  if (!cell) {
    return;
  }
  cell.dataset.editState = editState;
}

function markMatchDirty(jogoId, dirty) {
  if (!jogoId) {
    return;
  }
  state.dirtyByMatch[jogoId] = Boolean(dirty);
  syncDebugState();
}

function getHasDirtyMatch() {
  return Object.values(state.dirtyByMatch).some((value) => Boolean(value));
}

function getHasPendingChanges() {
  return getHasDirtyMatch() || state.pendingAdminRequests > 0;
}

function updateAdminRowStatus(row, message, modifier) {
  if (!row) {
    return;
  }
  const statusEl = row.querySelector('.admin-row-status');
  if (!statusEl) {
    return;
  }
  statusEl.textContent = message || '';
  statusEl.className = `admin-row-status${modifier ? ` admin-row-status--${modifier}` : ''}`;
}

function setAdminRetryVisible(row, visible) {
  if (!row) {
    return;
  }
  const retryButton = row.querySelector('[data-js="retry-admin-save"]');
  if (!retryButton) {
    return;
  }
  retryButton.hidden = !visible;
}

function buildPalpiteSignature(gols1, gols2) {
  if (gols1 === null || gols1 === undefined || gols2 === null || gols2 === undefined) {
    return null;
  }
  if (`${gols1}`.trim() === '' || `${gols2}`.trim() === '') {
    return null;
  }
  return `${gols1}:${gols2}`;
}

function getPalpiteSignatureFromForm(form) {
  if (!form) {
    return null;
  }
  const gols1 = form.querySelector('input[name="palpiteGolsEquipe1"]')?.value;
  const gols2 = form.querySelector('input[name="palpiteGolsEquipe2"]')?.value;
  return buildPalpiteSignature(gols1, gols2);
}

function getPalpiteSignatureFromRowInputs(jogoId) {
  if (!jogoId) {
    return null;
  }
  const gols1 = document.getElementById(`p1_${jogoId}`)?.value;
  const gols2 = document.getElementById(`p2_${jogoId}`)?.value;
  return buildPalpiteSignature(gols1, gols2);
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
  if (palpiteStatus !== undefined) { matchRow.dataset.palpiteStatus = palpiteStatus; }
  if (palpiteAllowed !== undefined) { matchRow.dataset.palpiteAllowed = palpiteAllowed; }
  if (palpiteGols1 !== undefined) { matchRow.dataset.palpiteGols1 = palpiteGols1; }
  if (palpiteGols2 !== undefined) { matchRow.dataset.palpiteGols2 = palpiteGols2; }
  if (palpiteStatusLabel !== undefined) { matchRow.dataset.palpiteStatusLabel = palpiteStatusLabel; }
  if (palpitePlaceholder !== undefined) { matchRow.dataset.palpitePlaceholder = palpitePlaceholder; }
  if (palpiteLockedReason !== undefined) {
    matchRow.dataset.palpiteLockedReason = palpiteLockedReason;
  } else {
    delete matchRow.dataset.palpiteLockedReason;
  }
  updateMatchRowUI(matchRow);
}

// Sincroniza data-attributes da linha pai a partir de um meta-elemento dentro da célula direta
function syncMatchRowFromCell(matchRow, meta) {
  if (!matchRow || !meta) { return; }
  const { palpiteStatus, palpiteAllowed, palpiteGols1, palpiteGols2,
          palpiteStatusLabel, palpitePlaceholder, palpiteLockedReason } = meta.dataset;
  if (palpiteStatus !== undefined) { matchRow.dataset.palpiteStatus = palpiteStatus; }
  if (palpiteAllowed !== undefined) { matchRow.dataset.palpiteAllowed = palpiteAllowed; }
  if (palpiteGols1 !== undefined) { matchRow.dataset.palpiteGols1 = palpiteGols1; }
  if (palpiteGols2 !== undefined) { matchRow.dataset.palpiteGols2 = palpiteGols2; }
  if (palpiteStatusLabel !== undefined) { matchRow.dataset.palpiteStatusLabel = palpiteStatusLabel; }
  if (palpitePlaceholder !== undefined) { matchRow.dataset.palpitePlaceholder = palpitePlaceholder; }
  if (palpiteLockedReason !== undefined) {
    matchRow.dataset.palpiteLockedReason = palpiteLockedReason;
  } else {
    delete matchRow.dataset.palpiteLockedReason;
  }
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
  
  if (expandRow) {
    if (resetContent) {
      restoreInlinePlaceholder(expandRow);
    }
    expandRow.hidden = true;
  }
  
  if (matchRow) {
    matchRow.classList.remove('match-row--expanded');
  }

  const trigger = document.querySelector(`[data-js="abrir-palpite-inline"][data-jogo-id="${matchId}"]`);
  if (trigger) {
    trigger.setAttribute('aria-expanded', 'false');
    trigger.dataset.inlineOpen = 'false';
    if (returnFocus) {
      trigger.focus();
    }
  }

  if (state.expandedMatchId === matchId) {
    state.expandedMatchId = null;
    state.lastInlineTrigger = null;
  }
}

function showCellFeedback(jogoId, msg, modifier) {
  const el = document.getElementById(`palpite-feedback_${jogoId}`);
  if (!el) return;
  el.textContent = msg;
  el.className = `palpite-cell-feedback${modifier ? ` palpite-cell-feedback--${modifier}` : ''}`;
  setMatchEditState(jogoId, modifier || 'idle');
}

function clearCellFeedback(jogoId) {
  const el = document.getElementById(`palpite-feedback_${jogoId}`);
  if (!el) return;
  el.textContent = '';
  el.className = 'palpite-cell-feedback';
  setMatchEditState(jogoId, 'idle');
}

function isAdminRequest(event, trigger) {
  if (trigger && trigger.closest('.match-row--admin-direct')) {
    return true;
  }

  const path = event?.detail?.requestConfig?.path;
  if (typeof path !== 'string') {
    return false;
  }

  return path.includes('/admin/atualizarResultadoJogo.action')
    || path.includes('/admin/salvarEdicaoEstrutural.action')
    || path.includes('/admin/jogosMaisJogosPartial.action')
    || path.includes('/admin/excluirJogo.action');
}

function getRequestConfig(event) {
  const requestConfig = event?.detail?.requestConfig;
  if (!requestConfig || typeof requestConfig !== 'object') {
    return null;
  }
  return requestConfig;
}

function startAdminPending(event) {
  const requestConfig = getRequestConfig(event);
  if (requestConfig) {
    if (requestConfig.__bolaoAdminTracked) {
      return;
    }
    requestConfig.__bolaoAdminTracked = true;
    requestConfig.__bolaoAdminSettled = false;
  }
  state.pendingAdminRequests += 1;
  syncDebugState();
}

function finishAdminPending(event) {
  const requestConfig = getRequestConfig(event);
  if (requestConfig) {
    if (!requestConfig.__bolaoAdminTracked || requestConfig.__bolaoAdminSettled) {
      return false;
    }
    requestConfig.__bolaoAdminSettled = true;
    state.pendingAdminRequests = Math.max(0, state.pendingAdminRequests - 1);
    syncDebugState();
    return true;
  }
  state.pendingAdminRequests = Math.max(0, state.pendingAdminRequests - 1);
  syncDebugState();
  return true;
}

function handleBeforeRequest(event) {
  if (!state.initialized) {
    return;
  }
  const trigger = event.detail && event.detail.elt;
  if (!trigger) {
    return;
  }

  const adminRow = trigger.closest('.match-row--admin-direct');
  if (isAdminRequest(event, trigger)) {
    const savingMessage = getUiMessage('msgAdminSaving', 'Salvando resultado...');
    startAdminPending(event);
    if (adminRow && trigger.getAttribute('name')) {
      state.lastAdminTriggerByRow[adminRow.id] = trigger.getAttribute('name');
    }
    if (adminRow) {
      updateAdminRowStatus(adminRow, savingMessage, 'saving');
      setAdminRetryVisible(adminRow, false);
    }
    return;
  }

  // Botão ✓ (confirmar palpite)
  if (trigger.matches('[data-js="confirmar-palpite"], form.palpite-inputs, .palpite-inputs__score')) {
    const jogoId = getMatchIdFromTrigger(trigger);
    const savingMessage = getUiMessage('msgTipSaving', 'Salvando...');
    const confirmButton = trigger.matches('[data-js="confirmar-palpite"]')
      ? trigger
      : trigger.querySelector('[data-js="confirmar-palpite"]');
    if (confirmButton) {
      confirmButton.disabled = true;
      confirmButton.setAttribute('aria-busy', 'true');
    }
    showCellFeedback(jogoId, savingMessage, 'saving');
    markMatchDirty(jogoId, false);
    return;
  }

  // Botão legado (abrir-palpite-inline)
  if (trigger.matches('[data-js="abrir-palpite-inline"]')) {
    if (!trigger.disabled) {
      trigger.dataset.wasDisabled = 'false';
      trigger.disabled = true;
      trigger.setAttribute('aria-busy', 'true');
    } else {
      trigger.dataset.wasDisabled = 'true';
    }
    openInlineRow(trigger);
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

  // Novo padrão: célula de palpite direto (#palpite-cell_N — outerHTML swap)
  if (target.id && target.id.startsWith('palpite-cell_')) {
    const jogoId = target.id.replace('palpite-cell_', '');
    const matchRow = findMatchRow(jogoId);
    const meta = target.querySelector('[data-palpite-meta="true"]');
    if (meta && matchRow) {
      syncMatchRowFromCell(matchRow, meta);
      const signature = buildPalpiteSignature(meta.dataset.palpiteGols1, meta.dataset.palpiteGols2);
      if (signature) {
        state.lastSavedByMatch[jogoId] = signature;
      }
    }
    if (target.querySelector('.palpite-cell-feedback--saved')) {
      const savedLabel = getUiMessage('msgTipSaved', 'Salvo as');
      const sessionSaved = getUiMessage('msgSessionSaved', 'Palpite salvo com sucesso.');
      announceGlobalStatus(sessionSaved, 'success');
      showCellFeedback(jogoId, `${savedLabel} ${getNowHHMM()}`, 'saved');
      markMatchDirty(jogoId, false);
      debugInfo('Palpite salvo (direct-inline).', { jogoId });
    } else if (target.querySelector('.palpite-cell-feedback--error')) {
      const tipError = getUiMessage('msgTipError', 'Falha ao salvar.');
      announceGlobalStatus(getUiMessage('msgSessionError', 'Erro ao salvar palpite.'), 'error');
      showCellFeedback(jogoId, tipError, 'error');
      markMatchDirty(jogoId, true);
    } else if (target.querySelector('.palpite-cell-feedback--locked')) {
      showCellFeedback(jogoId, getUiMessage('msgTipLocked', 'Edicao encerrada.'), 'locked');
      markMatchDirty(jogoId, false);
    } else {
      setMatchEditState(jogoId, 'idle');
    }
    // Não forçamos focus na nova célula para não roubar o foco do usuário durante um auto-save em background
    return;
  }

  if (target.id && target.id.startsWith('jogoTr_') && target.classList.contains('match-row--admin-direct')) {
    const savedMessage = `${getUiMessage('msgAdminSaved', 'Resultado salvo as')} ${getNowHHMM()}`;
    updateAdminRowStatus(target, savedMessage, 'saved');
    setAdminRetryVisible(target, false);
    announceGlobalStatus(getUiMessage('msgAdminSessionSaved', 'Resultado atualizado com sucesso.'), 'success');
    return;
  }

  if (target.id && target.id.startsWith('group-content_')) {
    const jogoId = target.id.replace('group-content_', '');
    const toggleBtn = document.querySelector(`.btn-grupo-toggle[data-target="#group-row_${jogoId}"]`);
    if (toggleBtn) {
      toggleBtn.dataset.groupLoaded = 'true';
      delete toggleBtn.dataset.groupLoading;
    }
    return;
  }

  // Padrão legado: linha de expansão (.match-expand)
  if (target.classList.contains('match-expand')) {
    target.hidden = false;
    const parentId = target.dataset.parent;
    if (parentId) {
      state.expandedMatchId = parentId;
      syncMatchRowFromExpand(target);
    }
    if (target.querySelector('.palpite-inline__feedback--success')) {
      debugInfo('Palpite salvo; painel "meus palpites" atualizado.');
    }
    // Focar o primeiro input para manter flow de digitação
    focusFirstInteractive(target);
  }
}

function handleAfterRequest(event) {
  if (!state.initialized) {
    return;
  }

  // Em alguns cenários de swap (ex.: outerHTML da linha admin), o elemento
  // disparador pode não estar disponível no afterRequest. Nesse caso usamos
  // apenas o requestConfig.path para liquidar pendências administrativas e
  // evitar warning falso de beforeunload.
  if (isAdminRequest(event, null)) {
    const trigger = event.detail && event.detail.elt;
    const adminRow = trigger instanceof HTMLElement
      ? trigger.closest('.match-row--admin-direct')
      : null;
    const path = event?.detail?.requestConfig?.path || '';
    finishAdminPending(event);
    if (event.detail.successful && path.includes('/admin/excluirJogo.action')) {
      announceGlobalStatus(getUiMessage('msgAdminDeleteSaved', 'Jogo excluido com sucesso.'), 'success');
      return;
    }
    if (!event.detail.successful && adminRow) {
      const adminError = path.includes('/admin/excluirJogo.action')
        ? getUiMessage('msgAdminDeleteError', 'Falha ao excluir jogo.')
        : getUiMessage('msgAdminError', 'Erro ao salvar resultado.');
      updateAdminRowStatus(adminRow, adminError, 'error');
      setAdminRetryVisible(adminRow, true);
      const globalError = path.includes('/admin/excluirJogo.action')
        ? getUiMessage('msgAdminDeleteError', 'Falha ao excluir jogo.')
        : getUiMessage('msgAdminSessionError', 'Erro ao atualizar resultado.');
      announceGlobalStatus(globalError, 'error');
      const debugMessage = path.includes('/admin/excluirJogo.action')
        ? 'Falha ao excluir jogo no admin.'
        : 'Falha ao atualizar resultado no admin.';
      debugWarn(debugMessage, { rowId: adminRow.id });
    }
    return;
  }

  const trigger = event.detail && event.detail.elt;
  if (!trigger) {
    return;
  }

  // progress refresh moved to HX-Trigger listener (server-confirmed)

  // Fluxo de palpite (botão ✓ ou submit de form)
  if (trigger.matches('[data-js="confirmar-palpite"], form.palpite-inputs, .palpite-inputs__score')) {
    const jogoId = getMatchIdFromTrigger(trigger);
    const confirmButton = trigger.matches('[data-js="confirmar-palpite"]')
      ? trigger
      : trigger.querySelector('[data-js="confirmar-palpite"]');
    if (confirmButton) {
      confirmButton.disabled = false;
      confirmButton.removeAttribute('aria-busy');
    }
    if (!event.detail.successful) {
      const errorMessage = getUiMessage('msgTipError', 'Erro ao salvar. Tente novamente.');
      showCellFeedback(jogoId, errorMessage, 'error');
      markMatchDirty(jogoId, true);
      announceGlobalStatus(getUiMessage('msgSessionError', 'Erro ao salvar palpite.'), 'error');
      debugWarn('Falha ao salvar palpite (rede).', { jogoId });
    }
    return;
  }

  // Botões legados (abrir-palpite-inline)
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
  const target = event.target;
  if (!(target instanceof Element)) {
    return;
  }

  const retryTipButton = target.closest('[data-js="retry-palpite"]');
  if (retryTipButton) {
    event.preventDefault();
    const matchId = retryTipButton.dataset.jogoId;
    const cell = document.getElementById(`palpite-cell_${matchId}`);
    const form = cell ? cell.querySelector('form.palpite-inputs') : null;
    if (form && window.htmx) {
      showCellFeedback(matchId, getUiMessage('msgTipSaving', 'Salvando...'), 'saving');
      htmx.trigger(form, 'submit');
    } else if (window.htmx) {
      const homeInput = document.getElementById(`p1_${matchId}`);
      if (homeInput) {
        showCellFeedback(matchId, getUiMessage('msgTipSaving', 'Salvando...'), 'saving');
        htmx.trigger(homeInput, 'change');
      }
    }
    return;
  }

  const retryAdminButton = target.closest('[data-js="retry-admin-save"]');
  if (retryAdminButton) {
    event.preventDefault();
    const adminRow = retryAdminButton.closest('.match-row--admin-direct');
    if (!adminRow || !window.htmx) {
      return;
    }
    const lastFieldName = state.lastAdminTriggerByRow[adminRow.id];
    const targetField = lastFieldName
      ? adminRow.querySelector(`[name="${lastFieldName}"]`)
      : adminRow.querySelector('input[name="golsEquipe1"], input[name="golsEquipe2"], select[name="hora"], select[name="data"]');
    if (targetField) {
      setAdminRetryVisible(adminRow, false);
      htmx.trigger(targetField, 'change');
      htmx.trigger(targetField, 'blur');
    }
    return;
  }
  
  // Botão Cancelar Palpite Inline
  const cancelButton = target.closest('[data-js="cancelar-palpite-inline"]');
  if (cancelButton) {
    event.preventDefault();
    const expandRow = cancelButton.closest('.match-expand');
    const parentId = expandRow && expandRow.dataset.parent;
    if (parentId) {
      closeInlineRow(parentId);
    }
    return;
  }

  // Botão Fechar Palpite (Geral)
  if (target.closest('[data-js="fechar-palpite-inline"]')) {
    if (state.expandedMatchId) {
      closeInlineRow(state.expandedMatchId);
    }
    return;
  }

  // BOTÃO TOGGLE GRUPO (ACCORDION)
  const groupToggle = target.closest('[data-js="toggle-group-details"]');
  if (groupToggle) {
    const targetId = groupToggle.getAttribute('data-target');
    if (!targetId) {
      debugWarn('Botão de grupo sem data-target.', { groupToggle });
      return;
    }
    const targetRow = document.querySelector(targetId);
    
    if (targetRow) {
      const isOpening = targetRow.classList.contains('hidden');
      
      // MODO ACCORDION: Fecha todos os outros antes de abrir o novo
      document.querySelectorAll('.match-group-details-row').forEach(row => {
        if (row !== targetRow) {
          row.classList.add('hidden');
        }
      });
      document.querySelectorAll('.btn-grupo-toggle').forEach(btn => {
        if (btn !== groupToggle) {
          btn.classList.remove('active');
          syncGroupToggleA11y(btn, false);
        }
      });

      // Toggle do alvo
      targetRow.classList.toggle('hidden');
      groupToggle.classList.toggle('active');
      syncGroupToggleA11y(groupToggle, !targetRow.classList.contains('hidden'));
      
      if (isOpening) {
        requestGroupDetails(groupToggle);
        debugInfo('Expandindo detalhes do grupo (Accordion).', { targetId });
      }
    } else {
      debugWarn('Linha de detalhes do grupo não encontrada para toggle.', { targetId });
    }
    return;
  }

  // BOTÃO CLOSE DETALHES DO GRUPO
  const closeDetails = target.closest('[data-js="close-details"]');
  if (closeDetails) {
    event.preventDefault();
    const targetId = closeDetails.getAttribute('data-target');
    const targetRow = document.querySelector(targetId);
    if (targetRow) {
      targetRow.classList.add('hidden');
      const toggleBtn = document.querySelector(`.btn-grupo-toggle[data-target="${targetId}"]`);
      if (toggleBtn) {
        toggleBtn.classList.remove('active');
        syncGroupToggleA11y(toggleBtn, false);
      }
    }
  }
}

function handleDocumentKeydown(event) {
  if (event.key !== 'Escape') {
    return;
  }
  if (state.expandedMatchId) {
    event.preventDefault();
    closeInlineRow(state.expandedMatchId);
    return;
  }
  
  // Fecha detalhes do grupo abertos se houver
  const activeDetailRow = document.querySelector('.match-group-details-row:not(.hidden)');
  if (activeDetailRow) {
    event.preventDefault();
    activeDetailRow.classList.add('hidden');
    const activeBtn = document.querySelector('.btn-grupo-toggle.active');
    if (activeBtn) {
      activeBtn.classList.remove('active');
      syncGroupToggleA11y(activeBtn, false);
    }
  }
}

function handleBeforeUnload(event) {
  if (!state.initialized) {
    return undefined;
  }
  if (!getHasPendingChanges()) {
    return undefined;
  }
  const message = getUiMessage('msgTipDirty', 'Existem alteracoes nao salvas.');
  event.preventDefault();
  event.returnValue = message;
  return message;
}

function getAdminEditableFields() {
  return Array.from(document.querySelectorAll(
    '.match-row--admin-direct input[name="golsEquipe1"], '
    + '.match-row--admin-direct input[name="golsEquipe2"], '
    + '.match-row--admin-direct select[name="data"], '
    + '.match-row--admin-direct select[name="hora"], '
    + '.match-row--admin-direct select[name="local"], '
    + '.match-row--admin-direct select[name="fase"], '
    + '.match-row--admin-direct select[name="equipe1Id"], '
    + '.match-row--admin-direct select[name="equipe2Id"]',
  ));
}

function handleAdminKeydown(event) {
  if (event.key !== 'Enter') {
    return;
  }
  const target = event.target;
  if (!(target instanceof HTMLElement)) {
    return;
  }
  if (!target.closest('.match-row--admin-direct')) {
    return;
  }
  const fields = getAdminEditableFields();
  const currentIndex = fields.indexOf(target);
  if (currentIndex < 0) {
    return;
  }
  event.preventDefault();
  const nextField = fields[currentIndex + 1];
  if (nextField) {
    nextField.focus();
    nextField.select?.();
  }
}

function handleAdminFieldChange(event) {
  const target = event.target;
  if (!(target instanceof HTMLElement)) {
    return;
  }
  const adminRow = target.closest('.match-row--admin-direct');
  if (!adminRow) {
    return;
  }
  updateAdminRowStatus(adminRow, getUiMessage('msgAdminDirty', 'Alteracoes pendentes.'), 'saving');
}

function initCollapsePortlets() {
  document.querySelectorAll('[data-js="collapse-container"]').forEach((icon) => {
    icon.addEventListener('click', () => {
      const targetId = icon.getAttribute('data-target');
      toggleCollapse(targetId, icon);
    });
  });
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
  document.body.addEventListener('keydown', handleAdminKeydown);
  document.body.addEventListener('change', handleAdminFieldChange);
}

function handleAutoSaveInput(event) {
  if (!state.initialized) {
    return;
  }
  const target = event.target;
  if (!target.classList.contains('palpite-inputs__score')) {
    return;
  }
  
  const form = target.closest('form.palpite-inputs');
  const jogoId = getMatchIdFromTrigger(target);
  if (!jogoId) {
    return;
  }

  const currentSignature = form
    ? getPalpiteSignatureFromForm(form)
    : getPalpiteSignatureFromRowInputs(jogoId);
  if (currentSignature && state.lastSavedByMatch[jogoId] === currentSignature) {
    markMatchDirty(jogoId, false);
    showCellFeedback(jogoId, `${getUiMessage('msgTipSaved', 'Salvo as')} ${getNowHHMM()}`, 'saved');
    return;
  }

  if (state.autoSaveTimers[jogoId]) {
    clearTimeout(state.autoSaveTimers[jogoId]);
  }

  markMatchDirty(jogoId, true);
  showCellFeedback(jogoId, getUiMessage('msgTipDirty', 'Alteracoes nao salvas.'), 'dirty');

  if (!form) {
    return;
  }

  state.autoSaveTimers[jogoId] = setTimeout(() => {
    delete state.autoSaveTimers[jogoId];
    if (form.checkValidity() && document.body.contains(form)) {
      const btn = form.querySelector('.btn-palpite-confirm');
      // Submete via HTMX se o botão não estiver bloqueado (request já em andamento)
      // e os valores do input não estiverem vazios
      if (btn && !btn.disabled) {
        debugInfo('Auto-save acionado após debounce.', { jogoId });
        if (window.htmx) {
          htmx.trigger(form, 'submit');
        }
      }
    }
  }, 800);
}

function initPalpiteInline() {
  storeInlineInitialState();
  document.body.addEventListener('htmx:beforeRequest', handleBeforeRequest);
  document.body.addEventListener('htmx:afterSwap', handleAfterSwap);
  document.body.addEventListener('htmx:afterRequest', handleAfterRequest);
  document.body.addEventListener('palpiteProgressRefresh', (event) => {
    if (!state.initialized || !window.htmx) {
      return;
    }
    window.htmx.ajax('GET', `${getBaseUrl()}/seguro/palpiteProgressPartial.action`, {
      target: '#palpiteProgressContainer',
      swap: 'outerHTML'
    });
  });
  document.body.addEventListener('htmx:responseError', (event) => {
    const xhr = event.detail.xhr;
    const target = event.detail.target instanceof HTMLElement ? event.detail.target : event.target;
    const trigger = event?.detail?.elt;
    const path = event?.detail?.requestConfig?.path || '';
    if (!(target instanceof HTMLElement)) {
      return;
    }
    if (target.id && target.id.startsWith('palpite-cell_')) {
      const matchId = target.id.replace('palpite-cell_', '');
      const feedback = document.getElementById(`palpite-feedback_${matchId}`);
      if (feedback) {
        feedback.className = 'palpite-cell-feedback palpite-cell-feedback--error';
        feedback.textContent = `Erro (${xhr.status}): ${getUiMessage('msgTipError', 'Falha ao salvar palpite.')}`;
      }
      announceGlobalStatus(getUiMessage('msgSessionError', 'Erro ao salvar palpite.'), 'error');
      return;
    }

    if (target.id && target.id.startsWith('jogoTr_')) {
      if (isAdminRequest(event, trigger) || target.classList.contains('match-row--admin-direct')) {
        finishAdminPending(event);
      }
      const adminRow = target.closest('.match-row--admin-direct') || document.getElementById(target.id);
      const adminRowError = path.includes('/admin/excluirJogo.action')
        ? getUiMessage('msgAdminDeleteError', 'Falha ao excluir jogo.')
        : getUiMessage('msgAdminError', 'Erro ao salvar resultado.');
      if (adminRow) {
        updateAdminRowStatus(adminRow, adminRowError, 'error');
        setAdminRetryVisible(adminRow, true);
      }
      const adminGlobalError = path.includes('/admin/excluirJogo.action')
        ? getUiMessage('msgAdminDeleteError', 'Falha ao excluir jogo.')
        : getUiMessage('msgAdminSessionError', 'Erro ao atualizar resultado.');
      announceGlobalStatus(adminGlobalError, 'error');
      return;
    }

    if (target.id && target.id.startsWith('group-content_')) {
      const jogoId = target.id.replace('group-content_', '');
      const toggleBtn = document.querySelector(`.btn-grupo-toggle[data-target="#group-row_${jogoId}"]`);
      if (toggleBtn) {
        delete toggleBtn.dataset.groupLoading;
      }
      target.innerHTML = '<tr><td colspan="3" class="text-center">Erro ao carregar palpites do grupo.</td></tr>';
    }
  });
  document.body.addEventListener('click', handleGlobalClick);
  document.body.addEventListener('input', handleAutoSaveInput);
  document.addEventListener('keydown', handleDocumentKeydown);
  window.addEventListener('beforeunload', handleBeforeUnload);
}

function initGlobalKeyListener() {
  // Listener global de teclado já é registrado em initPalpiteInline.
  // Mantemos este método para retrocompatibilidade sem registrar duplicado.
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
  syncDebugState();

  initCollapsePortlets();
  initFiltroColapsavel();
  initPalpiteInline();
  initResultadosAdmin();
  initGlobalKeyListener();

  debugInfo('initJogosPage concluído.');
}
