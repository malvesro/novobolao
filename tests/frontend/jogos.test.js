import { beforeEach, describe, expect, it, vi } from 'vitest';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

function mountJogosFixture() {
  document.body.innerHTML = `
    <div id="jogos-page-wrapper"
      data-msg-tip-dirty="Alteracoes nao salvas."
      data-msg-tip-saved="Salvo as"
      data-msg-tip-saving="Salvando..."
    ></div>

    <table>
      <tbody>
        <tr class="match-row" data-jogo-id="101">
          <td class="match-table__palpite">
            <input id="p1_101" class="palpite-inputs__score" name="palpiteGolsEquipe1" value="1" />
          </td>
          <td class="match-table__separator"></td>
          <td class="match-table__palpite">
            <input id="p2_101" class="palpite-inputs__score" name="palpiteGolsEquipe2" value="0" />
          </td>
          <td class="match-table__status"></td>
        </tr>
      </tbody>
    </table>

    <div id="palpite-cell_101" data-edit-state="idle">
      <span id="palpite-feedback_101" class="palpite-cell-feedback"></span>
      <div data-palpite-meta="true"
        data-palpite-gols1="1"
        data-palpite-gols2="0"
        data-palpite-status="pending"
        data-palpite-status-label="Pendente"
        data-palpite-placeholder="-"
      ></div>
      <button type="button" data-js="retry-palpite" data-jogo-id="101">Tentar novamente</button>
    </div>

    <table>
      <tbody>
        <tr id="jogoTr_201" class="match-row match-row--admin-direct">
          <td>
            <input type="text" name="golsEquipe1" value="2" />
            <button type="button" data-js="retry-admin-save">Retry admin</button>
            <span class="admin-row-status"></span>
          </td>
        </tr>
      </tbody>
    </table>

    <table>
      <tbody>
        <tr>
          <td>
            <button type="button" class="btn-grupo-toggle" data-js="toggle-group-details" data-target="#group-row_301" data-group-loaded="" aria-expanded="false" hx-get="/seguro/palpitesDoJogoPartial.action?jogoId=301" hx-target="#group-content_301">👥</button>
          </td>
        </tr>
        <tr id="group-row_301" class="match-group-details-row hidden">
          <td>
            <table><tbody id="group-content_301"><tr><td>Detalhes 301</td></tr></tbody></table>
          </td>
        </tr>
        <tr>
          <td>
            <button type="button" class="btn-grupo-toggle active" data-js="toggle-group-details" data-target="#group-row_302" data-group-loaded="" aria-expanded="true" hx-get="/seguro/palpitesDoJogoPartial.action?jogoId=302" hx-target="#group-content_302">👥</button>
          </td>
        </tr>
        <tr id="group-row_302" class="match-group-details-row">
          <td>
            <table><tbody id="group-content_302"><tr><td>Detalhes 302</td></tr></tbody></table>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="match-filter-portlet">
      <div class="collapsible-portlet__content">Conteudo filtro</div>
      <img
        data-js="collapse-container"
        data-target="filtro_jogos"
        src="/img/arrow_down.png"
        alt="Toggle filtro"
      />
    </div>
  `;
}

describe('jogos.js estados criticos', () => {
  beforeEach(() => {
    vi.useRealTimers();
    vi.useFakeTimers();
    document.body.innerHTML = '';
    sessionStorage.clear();
    window.htmx.trigger.mockClear();
    window.htmx.ajax.mockClear();
  });

  it('deve alternar painel de grupo em modo accordion e atualizar aria-expanded', async () => {
    vi.resetModules();
    mountJogosFixture();

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const btn301 = document.querySelector('.btn-grupo-toggle[data-target="#group-row_301"]');
    const row301 = document.getElementById('group-row_301');
    const btn302 = document.querySelector('.btn-grupo-toggle[data-target="#group-row_302"]');
    const row302 = document.getElementById('group-row_302');

    btn301.click();

    expect(row301.classList.contains('hidden')).toBe(false);
    expect(btn301.classList.contains('active')).toBe(true);
    expect(btn301.getAttribute('aria-expanded')).toBe('true');
    expect(btn301.getAttribute('aria-label')).toBe('Ocultar palpites do grupo');
    expect(row302.classList.contains('hidden')).toBe(true);
    expect(btn302.classList.contains('active')).toBe(false);
    expect(btn302.getAttribute('aria-expanded')).toBe('false');
    expect(btn302.getAttribute('aria-label')).toBe('Ver palpites do grupo');

    btn301.click();

    expect(row301.classList.contains('hidden')).toBe(true);
    expect(btn301.classList.contains('active')).toBe(false);
    expect(btn301.getAttribute('aria-expanded')).toBe('false');
    expect(btn301.getAttribute('aria-label')).toBe('Ver palpites do grupo');
  });

  it('nao deve cancelar o click do botao de grupo para permitir hx-get do HTMX', async () => {
    vi.resetModules();
    mountJogosFixture();

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const btn301 = document.querySelector('.btn-grupo-toggle[data-target="#group-row_301"]');
    const clickEvent = new MouseEvent('click', { bubbles: true, cancelable: true });
    btn301.dispatchEvent(clickEvent);

    expect(clickEvent.defaultPrevented).toBe(false);
  });

  it('deve disparar carregamento HTMX do grupo ao abrir accordion quando ainda nao carregado', async () => {
    vi.resetModules();
    mountJogosFixture();

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const btn301 = document.querySelector('.btn-grupo-toggle[data-target="#group-row_301"]');
    btn301.click();

    expect(window.htmx.ajax).toHaveBeenCalledWith(
      'GET',
      '/seguro/palpitesDoJogoPartial.action?jogoId=301',
      expect.objectContaining({
        target: document.getElementById('group-content_301'),
        swap: 'innerHTML',
      }),
    );
    expect(btn301.dataset.groupLoading).toBe('true');
  });

  it('deve fechar detalhes do grupo pelo botao de fechamento e sincronizar aria-expanded', async () => {
    vi.resetModules();
    mountJogosFixture();

    const row302 = document.getElementById('group-row_302');
    row302.querySelector('td').insertAdjacentHTML(
      'beforeend',
      '<button type="button" data-js="close-details" data-target="#group-row_302">Fechar</button>',
    );

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const btn302 = document.querySelector('.btn-grupo-toggle[data-target="#group-row_302"]');
    const closeBtn = row302.querySelector('[data-js="close-details"]');
    expect(row302.classList.contains('hidden')).toBe(false);
    expect(btn302.getAttribute('aria-expanded')).toBe('true');

    closeBtn.click();
    expect(row302.classList.contains('hidden')).toBe(true);
    expect(btn302.classList.contains('active')).toBe(false);
    expect(btn302.getAttribute('aria-expanded')).toBe('false');
    expect(btn302.getAttribute('aria-label')).toBe('Ver palpites do grupo');
  });

  it('deve fechar detalhes do grupo com Escape e manter botao sincronizado', async () => {
    vi.resetModules();
    mountJogosFixture();

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const btn302 = document.querySelector('.btn-grupo-toggle[data-target="#group-row_302"]');
    const row302 = document.getElementById('group-row_302');

    expect(row302.classList.contains('hidden')).toBe(false);
    expect(btn302.classList.contains('active')).toBe(true);
    expect(btn302.getAttribute('aria-expanded')).toBe('true');

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));

    expect(row302.classList.contains('hidden')).toBe(true);
    expect(btn302.classList.contains('active')).toBe(false);
    expect(btn302.getAttribute('aria-expanded')).toBe('false');
    expect(btn302.getAttribute('aria-label')).toBe('Ver palpites do grupo');
  });

  it('deve ignorar clique no botao de grupo sem data-target', async () => {
    vi.resetModules();
    mountJogosFixture();

    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

    document.body.insertAdjacentHTML(
      'beforeend',
      '<button type="button" data-js="toggle-group-details">Sem target</button>',
    );

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const invalidBtn = document.querySelector('button[data-js="toggle-group-details"]:not(.btn-grupo-toggle)');
    const row301 = document.getElementById('group-row_301');
    invalidBtn.click();

    expect(row301.classList.contains('hidden')).toBe(true);
    expect(warnSpy).toHaveBeenCalled();

    warnSpy.mockRestore();
  });

  it('deve inicializar filtro colapsado em desktop quando sessionStorage indicar colapso', async () => {
    vi.resetModules();
    mountJogosFixture();

    Object.defineProperty(window, 'innerWidth', {
      configurable: true,
      value: 1366,
    });
    sessionStorage.setItem('bolao:filtro:collapsed', 'true');

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const portlet = document.querySelector('.match-filter-portlet');
    const toggle = portlet.querySelector('[data-js="collapse-container"]');
    expect(portlet.classList.contains('filter-collapsed')).toBe(true);
    expect(toggle.getAttribute('src')).toContain('arrow_right');
  });

  it('deve manter filtro expandido em mobile mesmo com sessionStorage colapsado', async () => {
    vi.resetModules();
    mountJogosFixture();

    Object.defineProperty(window, 'innerWidth', {
      configurable: true,
      value: 375,
    });
    sessionStorage.setItem('bolao:filtro:collapsed', 'true');

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const portlet = document.querySelector('.match-filter-portlet');
    expect(portlet.classList.contains('filter-collapsed')).toBe(false);
  });

  it('deve marcar botao de grupo como carregado apos swap do conteudo HTMX', async () => {
    vi.resetModules();
    mountJogosFixture();

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const target = document.getElementById('group-content_301');
    const btn301 = document.querySelector('.btn-grupo-toggle[data-target="#group-row_301"]');
    expect(btn301.dataset.groupLoaded).toBe('');

    target.dispatchEvent(new CustomEvent('htmx:afterSwap', { bubbles: true }));

    expect(btn301.dataset.groupLoaded).toBe('true');
  });

  it('deve balancear pendencias admin em concorrencia com sucesso e erro sem travar beforeunload', async () => {
    vi.resetModules();
    mountJogosFixture();

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const adminField = document.querySelector('#jogoTr_201 input[name="golsEquipe1"]');
    const requestConfig1 = { path: '/admin/atualizarResultadoJogo.action' };
    const requestConfig2 = { path: '/admin/salvarEdicaoEstrutural.action' };

    adminField.dispatchEvent(new CustomEvent('htmx:beforeRequest', {
      bubbles: true,
      detail: { elt: adminField, requestConfig: requestConfig1 },
    }));
    adminField.dispatchEvent(new CustomEvent('htmx:beforeRequest', {
      bubbles: true,
      detail: { elt: adminField, requestConfig: requestConfig2 },
    }));

    const pendingExit = new Event('beforeunload', { cancelable: true });
    window.dispatchEvent(pendingExit);
    expect(pendingExit.defaultPrevented).toBe(true);

    adminField.dispatchEvent(new CustomEvent('htmx:afterRequest', {
      bubbles: true,
      detail: { elt: adminField, requestConfig: requestConfig1, successful: true },
    }));

    const stillPendingExit = new Event('beforeunload', { cancelable: true });
    window.dispatchEvent(stillPendingExit);
    expect(stillPendingExit.defaultPrevented).toBe(true);

    const adminRow = document.getElementById('jogoTr_201');
    adminRow.dispatchEvent(new CustomEvent('htmx:responseError', {
      bubbles: true,
      detail: {
        elt: adminField,
        target: adminRow,
        requestConfig: requestConfig2,
        xhr: { status: 500 },
      },
    }));

    adminField.dispatchEvent(new CustomEvent('htmx:afterRequest', {
      bubbles: true,
      detail: { elt: adminField, requestConfig: requestConfig2, successful: false },
    }));

    const cleanExit = new Event('beforeunload', { cancelable: true });
    window.dispatchEvent(cleanExit);
    expect(cleanExit.defaultPrevented).toBe(false);
  });

  it('deve limpar pendencia admin quando afterRequest chegar sem detail.elt', async () => {
    vi.resetModules();
    mountJogosFixture();

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const adminField = document.querySelector('#jogoTr_201 input[name="golsEquipe1"]');
    const requestConfig = { path: '/admin/atualizarResultadoJogo.action' };

    adminField.dispatchEvent(new CustomEvent('htmx:beforeRequest', {
      bubbles: true,
      detail: { elt: adminField, requestConfig },
    }));

    const pendingExit = new Event('beforeunload', { cancelable: true });
    window.dispatchEvent(pendingExit);
    expect(pendingExit.defaultPrevented).toBe(true);

    document.body.dispatchEvent(new CustomEvent('htmx:afterRequest', {
      bubbles: true,
      detail: { requestConfig, successful: true },
    }));

    const cleanExit = new Event('beforeunload', { cancelable: true });
    window.dispatchEvent(cleanExit);
    expect(cleanExit.defaultPrevented).toBe(false);
  });

  it('deve cobrir deduplicacao de autosave, dirty/beforeunload e fluxos de retry', async () => {
    vi.resetModules();
    mountJogosFixture();

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const palpiteCell = document.getElementById('palpite-cell_101');
    const gols1Input = document.getElementById('p1_101');
    const feedback = document.getElementById('palpite-feedback_101');

    // Primeira sincronizacao para popular lastSavedByMatch com assinatura 1:0
    palpiteCell.dispatchEvent(new CustomEvent('htmx:afterSwap', { bubbles: true }));

    // 1) Deduplicacao: valor igual ao ultimo salvo nao dispara submit HTMX
    gols1Input.dispatchEvent(new Event('input', { bubbles: true }));
    expect(window.htmx.trigger).not.toHaveBeenCalled();
    expect(feedback.className).toContain('palpite-cell-feedback--saved');

    // 2) Dirty: alterar placar marca dirty e bloqueia saida
    gols1Input.value = '2';
    gols1Input.dispatchEvent(new Event('input', { bubbles: true }));
    expect(feedback.className).toContain('palpite-cell-feedback--dirty');

    const pendingExitEvent = new Event('beforeunload', { cancelable: true });
    window.dispatchEvent(pendingExitEvent);
    expect(pendingExitEvent.defaultPrevented).toBe(true);
    expect(pendingExitEvent.returnValue).toBe(false);

    // Simula ciclo HTMX de salvamento para input inline
    gols1Input.dispatchEvent(new CustomEvent('htmx:beforeRequest', {
      bubbles: true,
      detail: { elt: gols1Input, requestConfig: { path: '/seguro/atualizarPalpitePartial.action' } },
    }));

    palpiteCell.innerHTML = `
      <span id="palpite-feedback_101" class="palpite-cell-feedback palpite-cell-feedback--saved">Salvo</span>
      <div data-palpite-meta="true" data-palpite-gols1="2" data-palpite-gols2="0" data-palpite-status="pending" data-palpite-status-label="Pendente" data-palpite-placeholder="-"></div>
      <button type="button" data-js="retry-palpite" data-jogo-id="101">Tentar novamente</button>
    `;
    palpiteCell.dispatchEvent(new CustomEvent('htmx:afterSwap', { bubbles: true }));
    gols1Input.dispatchEvent(new CustomEvent('htmx:afterRequest', {
      bubbles: true,
      detail: { elt: gols1Input, requestConfig: { path: '/seguro/atualizarPalpitePartial.action' }, successful: true },
    }));

    const cleanExitAfterSave = new Event('beforeunload', { cancelable: true });
    window.dispatchEvent(cleanExitAfterSave);
    expect(cleanExitAfterSave.defaultPrevented).toBe(false);
    expect(cleanExitAfterSave.returnValue).toBe(true);

    // 3) Retry participante: aciona change no input do time da casa
    const retryPalpiteButton = document.querySelector('[data-js="retry-palpite"]');
    retryPalpiteButton.click();
    expect(window.htmx.trigger).toHaveBeenCalledWith(gols1Input, 'change');

    // 4) Retry admin: reaproveita ultimo campo alterado e dispara change/blur
    const adminField = document.querySelector('#jogoTr_201 input[name="golsEquipe1"]');
    adminField.dispatchEvent(new CustomEvent('htmx:beforeRequest', {
      bubbles: true,
      detail: { elt: adminField },
    }));

    const retryAdminButton = document.querySelector('[data-js="retry-admin-save"]');
    retryAdminButton.click();

    expect(window.htmx.trigger).toHaveBeenCalledWith(adminField, 'change');
    expect(window.htmx.trigger).toHaveBeenCalledWith(adminField, 'blur');

    vi.useRealTimers();
  });

  it('deve manter contrato de palpite por celula apos swap HTMX', async () => {
    vi.resetModules();
    mountJogosFixture();

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const palpiteCell = document.getElementById('palpite-cell_101');
    palpiteCell.dispatchEvent(new CustomEvent('htmx:afterSwap', { bubbles: true }));

    const homeInput = document.getElementById('p1_101');
    const awayInput = document.getElementById('p2_101');
    const formInCell = document.querySelector('#palpite-cell_101 form.palpite-inputs');

    expect(homeInput).not.toBeNull();
    expect(awayInput).not.toBeNull();
    expect(formInCell).toBeNull();
  });

  it('deve impedir regressao de contrato HTMX por tbody no fluxo de palpite', () => {
    const currentDir = path.dirname(fileURLToPath(import.meta.url));
    const projectRoot = path.resolve(currentDir, '..', '..');
    const matchRowPath = path.join(projectRoot, 'webapp/WEB-INF/content/seguro/partials/match-row.jspf');
    const palpiteCellPath = path.join(projectRoot, 'webapp/WEB-INF/content/seguro/partials/palpite-cell-response.jspf');

    const matchRowMarkup = fs.readFileSync(matchRowPath, 'utf8');
    const palpiteCellMarkup = fs.readFileSync(palpiteCellPath, 'utf8');

    expect(matchRowMarkup).not.toContain('hx-target="closest tbody"');
    expect(matchRowMarkup).not.toContain('hx-swap="innerHTML');
    expect(matchRowMarkup).toContain('id="p1_${jogo.id}"');
    expect(matchRowMarkup).toContain('id="p2_${jogo.id}"');
    expect(matchRowMarkup).toContain('hx-target="#palpite-cell_${jogo.id}"');
    expect(matchRowMarkup).toContain('hx-swap="outerHTML"');
    expect(matchRowMarkup).toContain('data-js="toggle-group-details"');
    expect(matchRowMarkup).toContain('hx-target="#group-content_${jogo.id}"');
    expect(matchRowMarkup).not.toContain('hx-trigger="click[!this.dataset.groupLoaded]"');
    expect(matchRowMarkup).not.toContain('hx-trigger="click once"');
    expect(matchRowMarkup).toContain('data-group-loaded=""');
    expect(matchRowMarkup).toContain('aria-controls="group-row_${jogo.id}"');
    expect(matchRowMarkup).toContain("<fmt:message key='match.tip.group.view' />");
    expect(matchRowMarkup).toContain('aria-label="Fechar painel de palpites do grupo"');
    expect(palpiteCellMarkup).toContain('id="palpite-cell_${matchId}"');
    expect(palpiteCellMarkup).not.toContain('form class="palpite-inputs"');
  });

  it('deve manter fechamento semantico do label de filtro de fase', () => {
    const currentDir = path.dirname(fileURLToPath(import.meta.url));
    const projectRoot = path.resolve(currentDir, '..', '..');
    const jogosPagePath = path.join(projectRoot, 'webapp/WEB-INF/content/seguro/jogos.jsp');

    const jogosMarkup = fs.readFileSync(jogosPagePath, 'utf8');

    expect(jogosMarkup).toContain('<label for="filtro_fase">');
    expect(jogosMarkup).toContain('</label>');
  });

  it('nao deve carregar script legado ux-helper.js na tela de jogos', () => {
    const currentDir = path.dirname(fileURLToPath(import.meta.url));
    const projectRoot = path.resolve(currentDir, '..', '..');
    const jogosPagePath = path.join(projectRoot, 'webapp/WEB-INF/content/seguro/jogos.jsp');

    const jogosMarkup = fs.readFileSync(jogosPagePath, 'utf8');

    expect(jogosMarkup).not.toContain('/js/ux-helper.js');
  });

  it('deve manter contrato de permissao de palpite via autorizacao canonica do backend', () => {
    const currentDir = path.dirname(fileURLToPath(import.meta.url));
    const projectRoot = path.resolve(currentDir, '..', '..');
    const fragmentPath = path.join(projectRoot, 'webapp/WEB-INF/content/seguro/partials/jogos-lista-fragmento.jsp');

    const fragmentMarkup = fs.readFileSync(fragmentPath, 'utf8');

    expect(fragmentMarkup).toContain('autorizacoesPalpitePorJogo[jogo.id]');
    expect(fragmentMarkup).toContain('autorizacaoPalpite.status.key');
    expect(fragmentMarkup).toContain('autorizacaoPalpite.reason.key');
    expect(fragmentMarkup).toContain("pageContext.request.isUserInRole('ROLE_USER')");
    expect(fragmentMarkup).toContain("pageContext.request.isUserInRole('ROLE_ADMIN')");
    expect(fragmentMarkup).toContain("palpiteBloqueioMotivo eq 'roleMissing'");
    expect(fragmentMarkup).not.toContain("hasRole('ADMIN')");
    expect(fragmentMarkup).not.toContain("hasRole('USER')");
  });

  it('deve preservar contrato do load-more admin sem propagar periodo implicito da carga padrao', () => {
    const currentDir = path.dirname(fileURLToPath(import.meta.url));
    const projectRoot = path.resolve(currentDir, '..', '..');
    const fragmentPath = path.join(projectRoot, 'webapp/WEB-INF/content/seguro/partials/jogos-lista-fragmento.jsp');

    const fragmentMarkup = fs.readFileSync(fragmentPath, 'utf8');

    expect(fragmentMarkup).toContain('/admin/jogosMaisJogosPartial.action');
    expect(fragmentMarkup).toContain('<c:if test="${not adminFiltroAteHojeAtivo and not empty filtro and not empty filtro.dataInicialFormatada}">');
    expect(fragmentMarkup).toContain('<c:if test="${not adminFiltroAteHojeAtivo and not empty filtro and not empty filtro.dataFinalFormatada}">');
    expect(fragmentMarkup).toContain('<c:if test="${usarFiltro}">');
    expect(fragmentMarkup).toContain('<c:param name="filtroFase" value="${filtro.fase}" />');
    expect(fragmentMarkup).toContain('<c:param name="filtroEquipe" value="${filtro.idEquipe}" />');
    expect(fragmentMarkup).toContain('<c:param name="filtroGrupo" value="${filtro.grupo}" />');
    expect(fragmentMarkup).toContain('<c:param name="filtroJogosNaoOcorreram" value="true" />');
  });

});
