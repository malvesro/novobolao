const GAP_ICON = 206;
const BALLOON_OFFSET = 212;

let idJogoSelecionado = null;
let linhaSelecionada = null;
let meusPalpitesCarregado = false;
let previousFocusElement = null;

function getBaseUrl() {
  if (window.APP_BASE_URL) {
    return window.APP_BASE_URL;
  }
  return '';
}

function collapseContainer(containerId, imgElement) {
  const content = document.getElementById(`${containerId}_content`);
  if (!content || !imgElement) {
    return;
  }
  const isHidden = content.classList.toggle('collapsible-portlet__content--hidden');
  imgElement.src = isHidden ? `${getBaseUrl()}/img/arrow_right.png` : `${getBaseUrl()}/img/arrow_down.png`;
}

function getElementPosition(element) {
  const rect = element.getBoundingClientRect();
  return {
    x: rect.left + window.scrollX,
    y: rect.top + window.scrollY,
  };
}

function destacarLinha(row, highlight) {
  if (!row) {
    return;
  }
  if (highlight) {
    if (!row.dataset.originalClass) {
      row.dataset.originalClass = row.className;
    }
    if ((row.dataset.originalClass || '').indexOf('brasil') !== -1) {
      row.className = 'destacado_brasil';
    } else {
      row.className = 'destacado';
    }
  } else if (row.dataset.originalClass) {
    row.className = row.dataset.originalClass;
  }
}

function fecharIconeMouseOver(img) {
  if (!img) {
    return;
  }
  img.src = `${getBaseUrl()}/img/fechar_hover.gif`;
}

function fecharIconeMouseOut(img) {
  if (!img) {
    return;
  }
  img.src = `${getBaseUrl()}/img/fechar.gif`;
}

function destacarAtualizacao(row, sucesso) {
  if (!row) {
    return;
  }
  row.classList.remove('row-highlight--success', 'row-highlight--error');
  void row.offsetWidth;
  row.classList.add(sucesso ? 'row-highlight--success' : 'row-highlight--error');
}

function fecharBalao() {
  const palpiteDiv = document.getElementById('balao_palpite');
  const palpitesDiv = document.getElementById('balao_palpites');
  const statusContainer = document.getElementById('palpite-status');
  if (palpiteDiv) {
    palpiteDiv.classList.remove('balao-visible');
    palpiteDiv.setAttribute('aria-hidden', 'true');
  }
  if (palpitesDiv) {
    palpitesDiv.classList.remove('balao-visible');
    palpitesDiv.setAttribute('aria-hidden', 'true');
  }
  if (statusContainer) {
    statusContainer.innerHTML = '';
  }
  const loadingPalpite = document.getElementById('loading_span');
  const loadingPalpites = document.getElementById('loading_span_palpites');
  if (loadingPalpite) {
    loadingPalpite.classList.remove('loading-inline--visible');
  }
  if (loadingPalpites) {
    loadingPalpites.classList.remove('loading-inline--visible');
  }
  if (previousFocusElement) {
    previousFocusElement.focus();
    previousFocusElement = null;
  }
}

function carregarPalpitesDoJogo(jogoId) {
  const loading = document.getElementById('loading_span_palpites');
  if (loading) {
    loading.classList.add('loading-inline--visible');
  }
  const request = window.htmx.ajax('GET', `${getBaseUrl()}/seguro/palpitesDoJogoPartial.action`, {
    target: '#balao_table_palpites',
    swap: 'innerHTML',
    values: { jogoId },
  });
  request.addEventListener('loadend', () => {
    if (loading) {
      loading.classList.remove('loading-inline--visible');
    }
  });
}

function mostrarPopupPalpite(rowElement) {
  if (!rowElement) {
    return;
  }
  previousFocusElement = document.activeElement;
  if (!previousFocusElement || previousFocusElement === document.body) {
    previousFocusElement = rowElement;
  }
  const podeDarPalpite = rowElement.dataset.palpiteAllowed === 'true';
  const jogoId = Number(rowElement.dataset.jogoId);
  const palpiteDiv = document.getElementById('balao_palpite');
  const palpitesDiv = document.getElementById('balao_palpites');
  const statusContainer = document.getElementById('palpite-status');
  const loadingPalpite = document.getElementById('loading_span');
  const loadingPalpites = document.getElementById('loading_span_palpites');

  idJogoSelecionado = jogoId;
  linhaSelecionada = rowElement;

  if (statusContainer) {
    statusContainer.innerHTML = '';
  }

  if (loadingPalpite) {
    loadingPalpite.classList.remove('loading-inline--visible');
  }
  if (loadingPalpites) {
    loadingPalpites.classList.remove('loading-inline--visible');
  }

  if (palpiteDiv) {
    palpiteDiv.classList.remove('balao-visible');
    palpiteDiv.setAttribute('aria-hidden', 'true');
  }
  if (palpitesDiv) {
    palpitesDiv.classList.remove('balao-visible');
    palpitesDiv.setAttribute('aria-hidden', 'true');
  }

  const coords = getElementPosition(rowElement);

  if (podeDarPalpite) {
    if (palpiteDiv) {
      palpiteDiv.style.top = `${coords.y - 118}px`;
      palpiteDiv.style.left = `${coords.x + BALLOON_OFFSET}px`;
      palpiteDiv.classList.add('balao-visible');
      palpiteDiv.setAttribute('aria-hidden', 'false');
    }
    const gols1 = rowElement.dataset.palpiteGols1 || '';
    const gols2 = rowElement.dataset.palpiteGols2 || '';
    const inputGols1 = document.getElementById('palpite_gols_eq_1');
    const inputGols2 = document.getElementById('palpite_gols_eq_2');
    if (inputGols1) {
      inputGols1.value = gols1;
      inputGols1.focus();
    }
    if (inputGols2) {
      inputGols2.value = gols2;
    }
  } else {
    if (palpitesDiv) {
      palpiteDiv?.classList.remove('balao-visible');
      palpitesDiv.style.top = `${coords.y - GAP_ICON}px`;
      palpitesDiv.style.left = `${coords.x + BALLOON_OFFSET}px`;
      palpitesDiv.classList.add('balao-visible');
      palpitesDiv.setAttribute('aria-hidden', 'false');
      const closeButton = palpitesDiv.querySelector('[data-js="fechar-balao"]');
      if (closeButton) {
        closeButton.focus();
      }
    }
    carregarPalpitesDoJogo(jogoId);
  }
}

function atualizarPalpite() {
  if (idJogoSelecionado === null) {
    return;
  }
  const inputGols1 = document.getElementById('palpite_gols_eq_1');
  const inputGols2 = document.getElementById('palpite_gols_eq_2');
  if (!inputGols1 || !inputGols2) {
    return;
  }
  const gols1 = inputGols1.value === '' ? '0' : inputGols1.value;
  const gols2 = inputGols2.value === '' ? '0' : inputGols2.value;
  inputGols1.value = gols1;
  inputGols2.value = gols2;

  if (linhaSelecionada) {
    linhaSelecionada.dataset.palpiteGols1 = gols1;
    linhaSelecionada.dataset.palpiteGols2 = gols2;
  }

  const loading = document.getElementById('loading_span');
  if (loading) {
    loading.classList.add('loading-inline--visible');
  }

  const request = window.htmx.ajax('POST', `${getBaseUrl()}/seguro/atualizarPalpitePartial.action`, {
    target: '#palpite-status',
    swap: 'innerHTML',
    values: {
      jogoId: idJogoSelecionado,
      palpiteGolsEquipe1: gols1,
      palpiteGolsEquipe2: gols2,
    },
  });

  request.addEventListener('loadend', () => {
    if (loading) {
      loading.classList.remove('loading-inline--visible');
    }
    if (request.status >= 200 && request.status < 300) {
      const meusPalpitesPainel = document.getElementById('todos_palpites_div');
      if (meusPalpitesPainel && meusPalpitesPainel.classList.contains('tips-panel--visible')) {
        carregarMeusPalpites(true);
      }
    }
  });
}

function atualizarResultado(input) {
  if (!input) {
    return;
  }
  const jogoId = input.id.substring(input.id.lastIndexOf('_') + 1);
  const tr = document.getElementById(`jogoTr_${jogoId}`);
  const golsEq1Field = document.getElementById(`golsEquipe1_tf_${jogoId}`);
  const golsEq1 = golsEq1Field && golsEq1Field.value !== '' ? golsEq1Field.value : '-1';
  const golsEq2 = input.value !== '' ? input.value : '-1';

  fetch(`${getBaseUrl()}/admin/atualizarResultadoJogo.action`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      id: jogoId,
      golsEquipe1: golsEq1,
      golsEquipe2: golsEq2,
    }),
  })
    .then((response) => {
      destacarAtualizacao(tr, response.ok);
    })
    .catch(() => {
      destacarAtualizacao(tr, false);
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
  if (painel) {
    painel.classList.remove('tips-panel--visible');
  }
}

function carregarMeusPalpites(force) {
  if (!force && meusPalpitesCarregado) {
    return;
  }
  const request = window.htmx.ajax('GET', `${getBaseUrl()}/seguro/meusPalpitesPartial.action`, {
    target: '#todos_palpites_table',
    swap: 'innerHTML',
  });
  request.addEventListener('loadend', () => {
    meusPalpitesCarregado = request.status >= 200 && request.status < 300;
  });
}

function bindRowEvents() {
  const rows = document.querySelectorAll('[data-jogo-id]');
  rows.forEach((row) => {
    row.setAttribute('tabindex', '0');
    row.setAttribute('role', 'button');
    row.addEventListener('mouseover', () => destacarLinha(row, true));
    row.addEventListener('mouseout', () => destacarLinha(row, false));
    row.addEventListener('click', () => mostrarPopupPalpite(row));
    row.addEventListener('keydown', (event) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        mostrarPopupPalpite(row);
      }
    });
  });
}

function bindCloseIcons() {
  const closeIcons = document.querySelectorAll('[data-js="fechar-balao"]');
  closeIcons.forEach((icon) => {
    icon.addEventListener('click', () => fecharBalao());
    icon.addEventListener('mouseover', () => fecharIconeMouseOver(icon));
    icon.addEventListener('mouseout', () => fecharIconeMouseOut(icon));
    icon.setAttribute('tabindex', '0');
    icon.setAttribute('role', 'button');
    icon.addEventListener('keydown', (event) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        fecharBalao();
      }
      if (event.key === 'Escape') {
        event.preventDefault();
        fecharBalao();
      }
    });
  });
}

function bindTipsPanelControls() {
  const mostrarLink = document.getElementById('mostrarMeusPalpitesLink');
  if (mostrarLink) {
    mostrarLink.addEventListener('click', (event) => {
      event.preventDefault();
      mostrarMeusPalpites();
    });
  }

  const closeButton = document.querySelector('[data-js="fechar-meus-palpites"]');
  if (closeButton) {
    closeButton.addEventListener('click', () => fecharMeusPalpites());
  }

  const refreshButton = document.querySelector('[data-js="recarregar-meus-palpites"]');
  if (refreshButton) {
    refreshButton.addEventListener('click', () => carregarMeusPalpites(true));
  }
}

function handleGlobalKeyDown(event) {
  if (event.key !== 'Escape') {
    return;
  }
  const palpiteDiv = document.getElementById('balao_palpite');
  const palpitesDiv = document.getElementById('balao_palpites');
  const palpiteVisivel = palpiteDiv && palpiteDiv.classList.contains('balao-visible');
  const palpitesVisivel = palpitesDiv && palpitesDiv.classList.contains('balao-visible');
  if (palpiteVisivel || palpitesVisivel) {
    event.preventDefault();
    fecharBalao();
  }
}

export function initJogosPage() {
  const jogosWrapper = document.getElementById('jogos-page-wrapper');
  if (!jogosWrapper) {
    return;
  }

  bindRowEvents();
  bindCloseIcons();
  bindTipsPanelControls();

  const collapseIcons = document.querySelectorAll('[data-js="collapse-container"]');
  collapseIcons.forEach((icon) => {
    const targetId = icon.getAttribute('data-target');
    icon.addEventListener('click', () => collapseContainer(targetId, icon));
  });

  const atualizarPalpiteButton = document.getElementById('confirmar_palpite_button');
  if (atualizarPalpiteButton) {
    atualizarPalpiteButton.addEventListener('click', () => atualizarPalpite());
  }

  const resultadoInputs = document.querySelectorAll('[data-js="resultado-input"]');
  resultadoInputs.forEach((input) => {
    input.addEventListener('change', () => atualizarResultado(input));
  });

  // Expose legacy functions for inline handlers que ainda não foram removidos.
  window.collapseContainer = collapseContainer;
  window.destacarLinha = destacarLinha;
  window.mostrarPopupPalpite = mostrarPopupPalpite;
  window.atualizarPalpite = atualizarPalpite;
  window.atualizarResultado = atualizarResultado;
  window.fecharBalao = fecharBalao;
  window.fecharIconeMouseOver = fecharIconeMouseOver;
  window.fecharIconeMouseOut = fecharIconeMouseOut;
  window.mostrarMeusPalpites = mostrarMeusPalpites;
  window.fecharMeusPalpites = fecharMeusPalpites;

  document.addEventListener('keydown', handleGlobalKeyDown);
}
