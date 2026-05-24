const DEBUG_LABEL = '[bolao:menu]';

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

function applyState(header, target, expanded) {
  header.setAttribute('aria-expanded', expanded ? 'true' : 'false');
  if (expanded) {
    target.removeAttribute('hidden');
  } else {
    target.setAttribute('hidden', 'hidden');
  }
  header.classList.toggle('menu_header--expanded', expanded);
}

function bindHeader(header) {
  const targetId = header.getAttribute('data-menu-target');
  if (!targetId) {
    debugWarn('Cabeçalho de menu sem data-menu-target definido.', header);
    return;
  }
  const target = document.getElementById(targetId);
  if (!target) {
    debugWarn('Elemento alvo do menu não encontrado.', { targetId });
    return;
  }

  const initialExpanded = header.getAttribute('aria-expanded') !== 'false' && !target.hasAttribute('hidden');
  applyState(header, target, initialExpanded);

  const toggle = (event) => {
    if (event) {
      event.preventDefault();
    }
    const nextExpanded = header.getAttribute('aria-expanded') !== 'true';
    applyState(header, target, nextExpanded);
    debugInfo('Menu alternado.', { targetId, expanded: nextExpanded });
  };

  header.addEventListener('click', toggle);
  header.addEventListener('keydown', (event) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      toggle();
    }
  });
}

export function initMenuToggle() {
  const headers = document.querySelectorAll('.menu_header[data-menu-target]');
  if (!headers.length) {
    return;
  }

  debugInfo('Inicializando menu lateral.', { headers: headers.length });

  if (!window.__bolaoMenuDebug) {
    window.__bolaoMenuDebug = {};
  }
  window.__bolaoMenuDebug.initializedAt = new Date().toISOString();
  window.__bolaoMenuDebug.headers = headers.length;

  headers.forEach((header) => {
    bindHeader(header);
  });
}
