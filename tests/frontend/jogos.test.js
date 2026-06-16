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
  `;
}

describe('jogos.js estados criticos', () => {
  beforeEach(() => {
    vi.useRealTimers();
    vi.useFakeTimers();
    document.body.innerHTML = '';
    window.htmx.trigger.mockClear();
    window.htmx.ajax.mockClear();
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
    expect(palpiteCellMarkup).toContain('id="palpite-cell_${matchId}"');
    expect(palpiteCellMarkup).not.toContain('form class="palpite-inputs"');
  });

});
