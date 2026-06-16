import { beforeEach, describe, expect, it, vi } from 'vitest';

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
          <td class="match-table__palpite"></td>
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
      <form class="palpite-inputs">
        <input type="hidden" name="jogoId" value="101" />
        <input class="palpite-inputs__score" name="palpiteGolsEquipe1" value="1" />
        <input class="palpite-inputs__score" name="palpiteGolsEquipe2" value="0" />
        <button type="button" class="btn-palpite-confirm" data-js="confirmar-palpite">Salvar</button>
      </form>
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

  it('deve cobrir deduplicacao de autosave, dirty/beforeunload e fluxos de retry', async () => {
    vi.resetModules();
    mountJogosFixture();

    const { initJogosPage } = await import('../../src/frontend/pages/jogos.js');
    initJogosPage();

    const palpiteCell = document.getElementById('palpite-cell_101');
    const form = palpiteCell.querySelector('form.palpite-inputs');
    const gols1Input = form.querySelector('input[name="palpiteGolsEquipe1"]');
    const feedback = document.getElementById('palpite-feedback_101');

    // Primeira sincronizacao para popular lastSavedByMatch com assinatura 1:0
    palpiteCell.dispatchEvent(new CustomEvent('htmx:afterSwap', { bubbles: true }));

    // 1) Deduplicacao: valor igual ao ultimo salvo nao dispara submit HTMX
    gols1Input.dispatchEvent(new Event('input', { bubbles: true }));
    expect(window.htmx.trigger).not.toHaveBeenCalled();
    expect(feedback.className).toContain('palpite-cell-feedback--saved');

    // 2) Dirty + autosave: alterar placar marca dirty e dispara submit apos debounce
    gols1Input.value = '2';
    gols1Input.dispatchEvent(new Event('input', { bubbles: true }));
    expect(feedback.className).toContain('palpite-cell-feedback--dirty');

    const pendingExitEvent = new Event('beforeunload', { cancelable: true });
    window.dispatchEvent(pendingExitEvent);
    expect(pendingExitEvent.defaultPrevented).toBe(true);
    expect(pendingExitEvent.returnValue).toBe(false);

    vi.advanceTimersByTime(810);
    expect(window.htmx.trigger).toHaveBeenCalledWith(form, 'submit');

    // 3) Sem pendencia: voltar ao valor salvo limpa dirty e nao bloqueia saida
    gols1Input.value = '1';
    gols1Input.dispatchEvent(new Event('input', { bubbles: true }));

    const cleanExitEvent = new Event('beforeunload', { cancelable: true });
    window.dispatchEvent(cleanExitEvent);
    expect(cleanExitEvent.defaultPrevented).toBe(false);
    expect(cleanExitEvent.returnValue).toBe(true);

    // 4) Retry participante: aciona submit do form
    const retryPalpiteButton = document.querySelector('[data-js="retry-palpite"]');
    retryPalpiteButton.click();
    expect(window.htmx.trigger).toHaveBeenCalledWith(form, 'submit');

    // 5) Retry admin: reaproveita ultimo campo alterado e dispara change/blur
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
});
