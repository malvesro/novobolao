import { beforeEach, describe, expect, it, vi } from 'vitest';

const renderMock = vi.fn(async () => {});
const updateOptionsMock = vi.fn(async () => {});
const destroyMock = vi.fn(() => {});

vi.mock('apexcharts', () => ({
  default: class ApexChartsMock {
    constructor() {}
    render = renderMock;
    updateOptions = updateOptionsMock;
    destroy = destroyMock;
  },
}));

function jsonResponse(payload) {
  return {
    ok: true,
    status: 200,
    json: async () => payload,
  };
}

function createPayload(name) {
  return {
    series: [
      {
        name,
        data: [[1719705600000, 10], [1719792000000, 12]],
      },
    ],
  };
}

function mountGraficoFixture() {
  document.body.innerHTML = `
    <section class="chart-wrapper--performance"
      data-chart-endpoint="/seguro/graficoDesempenho.json"
      data-loading-message="Carregando grafico..."
      data-empty-message="Sem dados"
      data-error-message="Erro no grafico"
      data-cache-message="Dados do cache"
      data-retry-label="Tentar novamente"
      data-status-loading="Status carregando"
      data-status-ready="Status pronto"
      data-status-error="Status erro"
    >
      <select id="rival">
        <option value="">Todos</option>
        <option value="A">Rival A</option>
        <option value="B">Rival B</option>
      </select>
      <div id="performance-chart" aria-busy="false"></div>
      <p id="performance-chart-status" class="chart-status chart-status--idle"></p>
    </section>
  `;
}

async function flushPromises() {
  await Promise.resolve();
  await Promise.resolve();
}

async function flushAsyncWork() {
  for (let i = 0; i < 6; i += 1) {
    await flushPromises();
    vi.advanceTimersByTime(0);
  }
}

describe('graficoDesempenho.js concorrencia e cache', () => {
  beforeEach(() => {
    vi.useRealTimers();
    vi.useFakeTimers();
    vi.resetModules();
    renderMock.mockClear();
    updateOptionsMock.mockClear();
    destroyMock.mockClear();
    document.body.innerHTML = '';
  });

  it('deve abortar request anterior, priorizar ultima selecao, usar cache e permitir retry apos erro', async () => {
    mountGraficoFixture();

    let firstRivalSignal = null;

    const fetchMock = vi.fn();
    fetchMock
      // Carga inicial (sem rival)
      .mockImplementationOnce(() => Promise.resolve(jsonResponse(createPayload('Inicial'))))
      // Rival A (fica pendente e deve ser abortado)
      .mockImplementationOnce((_, options) => new Promise((resolve, reject) => {
        firstRivalSignal = options.signal;
        options.signal.addEventListener('abort', () => {
          const abortError = new Error('aborted');
          abortError.name = 'AbortError';
          reject(abortError);
        });
        // sem resolve intencionalmente
      }))
      // Rival B (resposta válida)
      .mockImplementationOnce(() => Promise.resolve(jsonResponse(createPayload('Rival B'))));

    global.fetch = fetchMock;

    const { initGraficoDesempenhoPage } = await import('../../src/frontend/pages/graficoDesempenho.js');
    initGraficoDesempenhoPage();

    await flushPromises();

    const rivalSelect = document.getElementById('rival');
    const statusEl = document.getElementById('performance-chart-status');
    const chartEl = document.getElementById('performance-chart');

    // Dispara troca para A (request pendente)
    rivalSelect.value = 'A';
    rivalSelect.dispatchEvent(new Event('change', { bubbles: true }));
    vi.advanceTimersByTime(130);

    // Dispara troca para B (deve abortar A e prevalecer)
    rivalSelect.value = 'B';
    rivalSelect.dispatchEvent(new Event('change', { bubbles: true }));
    vi.advanceTimersByTime(130);

    await flushPromises();

    expect(firstRivalSignal).not.toBeNull();
    expect(firstRivalSignal.aborted).toBe(true);
    expect(fetchMock).toHaveBeenCalledTimes(3);
    await flushAsyncWork();
    expect(statusEl.textContent).toBe('Status pronto');
    expect(statusEl.className).toContain('chart-status--ready');

    // Nova troca para B deve usar cache sem novo fetch
    rivalSelect.dispatchEvent(new Event('change', { bubbles: true }));
    vi.advanceTimersByTime(130);
    await flushAsyncWork();

    expect(fetchMock).toHaveBeenCalledTimes(3);
    expect(chartEl.getAttribute('aria-busy')).toBe('false');
    expect(statusEl.className).not.toContain('chart-status--error');

    // Força erro para rival A e valida botão de retry
    fetchMock.mockImplementationOnce(() => Promise.reject(new Error('falha de rede')));
    rivalSelect.value = 'A';
    rivalSelect.dispatchEvent(new Event('change', { bubbles: true }));
    vi.advanceTimersByTime(130);
    await flushPromises();

    await flushAsyncWork();
    expect(statusEl.textContent).toBe('Status erro');
    expect(statusEl.className).toContain('chart-status--error');

    const retryButton = chartEl.querySelector('.chart-retry');
    expect(retryButton).not.toBeNull();

    fetchMock.mockImplementationOnce(() => Promise.resolve(jsonResponse(createPayload('Rival A'))));
    retryButton.click();
    await flushPromises();

    await flushAsyncWork();
    expect(statusEl.textContent).toBe('Status pronto');
    expect(statusEl.className).toContain('chart-status--ready');
    expect(chartEl.getAttribute('aria-busy')).toBe('false');

    vi.useRealTimers();
  });

  it('deve ignorar erro tardio de requisicao obsoleta e manter estado da ultima selecao', async () => {
    mountGraficoFixture();

    const fetchMock = vi.fn();
    fetchMock
      .mockImplementationOnce(() => Promise.resolve(jsonResponse(createPayload('Inicial'))))
      .mockImplementationOnce(() => Promise.resolve(jsonResponse(createPayload('Rival A'))))
      .mockImplementationOnce(() => Promise.resolve(jsonResponse(createPayload('Rival B'))));
    global.fetch = fetchMock;

    let rejectObsoleteRender;
    updateOptionsMock
      .mockImplementationOnce(() => new Promise((_, reject) => {
        rejectObsoleteRender = reject;
      }))
      .mockImplementationOnce(async () => {});

    const { initGraficoDesempenhoPage } = await import('../../src/frontend/pages/graficoDesempenho.js');
    initGraficoDesempenhoPage();
    await flushAsyncWork();

    const rivalSelect = document.getElementById('rival');
    const statusEl = document.getElementById('performance-chart-status');

    rivalSelect.value = 'A';
    rivalSelect.dispatchEvent(new Event('change', { bubbles: true }));
    vi.advanceTimersByTime(130);
    await flushPromises();

    rivalSelect.value = 'B';
    rivalSelect.dispatchEvent(new Event('change', { bubbles: true }));
    vi.advanceTimersByTime(130);
    await flushAsyncWork();

    rejectObsoleteRender(new Error('falha tardia de render A'));
    await flushAsyncWork();

    expect(statusEl.textContent).toBe('Status pronto');
    expect(statusEl.className).toContain('chart-status--ready');
    expect(statusEl.className).not.toContain('chart-status--error');

    vi.useRealTimers();
  });
});
