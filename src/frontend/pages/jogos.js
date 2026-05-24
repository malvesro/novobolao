const DEBUG_LABEL = '[bolao:jogos]';

const state = {
  expandedMatchId: null,
  lastInlineTrigger: null,
  initialized: false,
  autoSaveTimers: {},
};

const FILTER_STORAGE_KEY = 'bolao:filtro:collapsed';

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
}

function clearCellFeedback(jogoId) {
  const el = document.getElementById(`palpite-feedback_${jogoId}`);
  if (!el) return;
  el.textContent = '';
  el.className = 'palpite-cell-feedback';
}

function handleBeforeRequest(event) {
  if (!state.initialized) {
    return;
  }
  const trigger = event.detail && event.detail.elt;
  if (!trigger) {
    return;
  }

  // Botão ✓ (confirmar palpite)
  if (trigger.matches('[data-js="confirmar-palpite"]')) {
    const jogoId = trigger.dataset.jogoId;
    trigger.disabled = true;
    trigger.setAttribute('aria-busy', 'true');
    showCellFeedback(jogoId, 'Salvando…', 'saving');
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
    }
    // Se salvou com sucesso, recarregar "meus palpites" se painel estiver aberto
    if (target.querySelector('.palpite-cell-feedback--saved')) {
      debugInfo('Palpite salvo (direct-inline).', { jogoId });
    }
    // Não forçamos focus na nova célula para não roubar o foco do usuário durante um auto-save em background
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
  const trigger = event.detail && event.detail.elt;
  if (!trigger) {
    return;
  }

  // progress refresh moved to HX-Trigger listener (server-confirmed)

  // Botão ✓ — restaurar após request (sucesso ou falha)
  if (trigger.matches('[data-js="confirmar-palpite"]')) {
    trigger.disabled = false;
    trigger.removeAttribute('aria-busy');
    if (!event.detail.successful) {
      const jogoId = trigger.dataset.jogoId;
      showCellFeedback(jogoId, '⚠ Erro ao salvar. Tente novamente.', 'error');
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
    event.preventDefault();
    const targetId = groupToggle.getAttribute('data-target');
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
        }
      });

      // Toggle do alvo
      targetRow.classList.toggle('hidden');
      groupToggle.classList.toggle('active');
      
      if (isOpening) {
        debugInfo('Expandindo detalhes do grupo (Accordion).', { targetId });
      }
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
      if (toggleBtn) toggleBtn.classList.remove('active');
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
    if (activeBtn) activeBtn.classList.remove('active');
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

function handleAutoSaveInput(event) {
  if (!state.initialized) {
    return;
  }
  const target = event.target;
  if (!target.classList.contains('palpite-inputs__score')) {
    return;
  }
  
  const form = target.closest('form.palpite-inputs');
  if (!form) {
    return;
  }
  
  const jogoIdInput = form.querySelector('input[name="jogoId"]');
  const jogoId = jogoIdInput ? jogoIdInput.value : null;
  if (!jogoId) {
    return;
  }

  if (state.autoSaveTimers[jogoId]) {
    clearTimeout(state.autoSaveTimers[jogoId]);
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
    const target = event.detail.target;
    const matchId = target.id.replace('palpite-cell_', '');
    const feedback = document.getElementById(`palpite-feedback_${matchId}`);
    if (feedback) {
      feedback.className = 'palpite-cell-feedback palpite-cell-feedback--error';
      feedback.textContent = `Erro (${xhr.status}): Falha ao salvar palpite.`;
    }
  });
  document.body.addEventListener('click', handleGlobalClick);
  document.body.addEventListener('input', handleAutoSaveInput);
  document.addEventListener('keydown', handleDocumentKeydown);
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
  initFiltroColapsavel();
  initPalpiteInline();
  initResultadosAdmin();
  initGlobalKeyListener();

  debugInfo('initJogosPage concluído.');
}
