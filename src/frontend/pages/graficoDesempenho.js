const CACHE_TTL_MS = 45_000;
const REQUEST_TIMEOUT_MS = 10_000;
const CHART_HEIGHT = 360;

let chartInstance = null;
let activeController = null;
let latestRequestToken = 0;
let apexChartsLoader = null;
let latestServerCacheVersion = 0;

const chartCache = new Map();

function hasSeriesData(payload) {
  if (!payload || !Array.isArray(payload.series) || payload.series.length === 0) {
    return false;
  }

  return payload.series.some((serie) => Array.isArray(serie.data) && serie.data.length > 0);
}

function getCachedPayload(cacheKey) {
  const cached = chartCache.get(cacheKey);
  if (!cached) {
    return null;
  }

  if (Date.now() - cached.createdAt > CACHE_TTL_MS) {
    chartCache.delete(cacheKey);
    return null;
  }

  if (latestServerCacheVersion > 0 && cached.cacheVersion > 0 && cached.cacheVersion < latestServerCacheVersion) {
    chartCache.delete(cacheKey);
    return null;
  }

  return cached.payload;
}

function setCachedPayload(cacheKey, payload, cacheVersion = 0) {
  chartCache.set(cacheKey, {
    createdAt: Date.now(),
    payload,
    cacheVersion,
  });
}

function normalizeCacheVersion(versionCandidate) {
  const version = Number.parseInt(versionCandidate, 10);
  if (!Number.isFinite(version) || version < 1) {
    return 0;
  }
  return version;
}

function syncCacheVersion(payload, response) {
  const payloadVersion = normalizeCacheVersion(payload?.cacheVersion);
  const headerVersion = normalizeCacheVersion(response?.headers?.get?.('X-Grafico-Cache-Version'));
  const nextVersion = Math.max(payloadVersion, headerVersion);
  if (nextVersion > latestServerCacheVersion) {
    latestServerCacheVersion = nextVersion;
  }
  return nextVersion;
}

function setStatus(statusEl, message, kind = 'idle') {
  if (!statusEl) {
    return;
  }

  statusEl.classList.remove(
    'chart-status--idle',
    'chart-status--loading',
    'chart-status--ready',
    'chart-status--error',
    'chart-status--warn',
  );
  statusEl.classList.add(`chart-status--${kind}`);
  statusEl.textContent = message;
}

function setChartBusy(chartEl, isBusy) {
  if (!chartEl) {
    return;
  }
  chartEl.setAttribute('aria-busy', isBusy ? 'true' : 'false');
}

function buildChartOptions(payload) {
  return {
    chart: {
      type: 'line',
      height: CHART_HEIGHT,
      fontFamily: 'inherit',
      animations: {
        enabled: true,
        easing: 'easeout',
        speed: 260,
      },
      toolbar: {
        show: false,
      },
    },
    noData: {
      text: '',
    },
    series: payload.series,
    xaxis: {
      type: 'datetime',
      labels: {
        datetimeUTC: false,
      },
    },
    yaxis: {
      forceNiceScale: true,
      decimalsInFloat: 0,
    },
    stroke: {
      curve: 'smooth',
      width: 3,
    },
    colors: ['#0b5cab', '#f4b400', '#1b7f4e', '#b9382c'],
    legend: {
      position: 'top',
      horizontalAlign: 'left',
    },
    tooltip: {
      x: {
        format: 'dd/MM/yyyy',
      },
      shared: true,
      intersect: false,
    },
    grid: {
      borderColor: 'rgba(40, 63, 8, 0.14)',
      strokeDashArray: 3,
    },
    responsive: [
      {
        breakpoint: 768,
        options: {
          chart: {
            height: 320,
          },
          legend: {
            position: 'bottom',
          },
        },
      },
    ],
  };
}

function destroyChartIfAny() {
  if (!chartInstance) {
    return;
  }
  chartInstance.destroy();
  chartInstance = null;
}

async function renderChart(chartEl, payload) {
  if (!apexChartsLoader) {
    apexChartsLoader = import('apexcharts').then((module) => module.default);
  }
  const ApexCharts = await apexChartsLoader;
  const options = buildChartOptions(payload);
  if (!chartInstance) {
    chartInstance = new ApexCharts(chartEl, options);
    await chartInstance.render();
    return;
  }
  await chartInstance.updateOptions(options, true, true, false);
}

function buildEndpoint(baseEndpoint, rivalId) {
  const endpoint = new URL(baseEndpoint, window.location.origin);
  if (rivalId) {
    endpoint.searchParams.set('rival', rivalId);
  } else {
    endpoint.searchParams.delete('rival');
  }
  return endpoint.toString();
}

function buildVersionEndpoint(baseEndpoint) {
  const endpoint = new URL(baseEndpoint, window.location.origin);
  endpoint.searchParams.set('cacheVersionOnly', 'true');
  return endpoint.toString();
}

async function syncServerCacheVersion(baseEndpoint) {
  const response = await fetch(buildVersionEndpoint(baseEndpoint), {
    method: 'GET',
    headers: {
      Accept: 'application/json',
      'X-Requested-With': 'XMLHttpRequest',
    },
    credentials: 'same-origin',
  });

  if (!response.ok) {
    return;
  }

  const payload = await response.json();
  const cacheVersion = syncCacheVersion(payload, response);
  if (cacheVersion > 0) {
    for (const [key, entry] of chartCache.entries()) {
      if ((entry?.cacheVersion || 0) < cacheVersion) {
        chartCache.delete(key);
      }
    }
  }
}

async function fetchChartPayload(baseEndpoint, rivalId, requestToken) {
  const cacheKey = rivalId || '__self__';
  const cached = getCachedPayload(cacheKey);
  if (cached) {
    return { payload: cached, fromCache: true, stale: false };
  }

  if (activeController) {
    activeController.abort();
  }

  const controller = new AbortController();
  activeController = controller;
  let timeoutTriggered = false;
  const timeoutId = setTimeout(() => {
    timeoutTriggered = true;
    controller.abort();
  }, REQUEST_TIMEOUT_MS);

  try {
    const response = await fetch(buildEndpoint(baseEndpoint, rivalId), {
      method: 'GET',
      signal: controller.signal,
      headers: {
        Accept: 'application/json',
        'X-Requested-With': 'XMLHttpRequest',
      },
      credentials: 'same-origin',
    });

    if (!response.ok) {
      throw new Error(`Resposta inválida (${response.status})`);
    }

    const payload = await response.json();
    const cacheVersion = syncCacheVersion(payload, response);
    if (requestToken !== latestRequestToken) {
      return { payload: null, fromCache: false, stale: true };
    }

    setCachedPayload(cacheKey, payload, cacheVersion);
    return { payload, fromCache: false, stale: false };
  } catch (error) {
    if (error?.name === 'AbortError' && timeoutTriggered) {
      const timeoutError = new Error('Tempo limite ao carregar dados do gráfico.');
      timeoutError.name = 'TimeoutError';
      throw timeoutError;
    }
    throw error;
  } finally {
    clearTimeout(timeoutId);
  }
}

function mountRetryButton(chartEl, label, onRetry) {
  const retryButton = document.createElement('button');
  retryButton.type = 'button';
  retryButton.className = 'button button-inline chart-retry';
  retryButton.textContent = label;
  retryButton.addEventListener('click', onRetry, { once: true });
  chartEl.replaceChildren(retryButton);
}

export function initGraficoDesempenhoPage() {
  const chartEl = document.getElementById('performance-chart');
  const rivalSelectEl = document.getElementById('rival');
  const wrapperEl = chartEl ? chartEl.closest('.chart-wrapper--performance') : null;
  const statusEl = document.getElementById('performance-chart-status');

  if (!chartEl || !rivalSelectEl || !wrapperEl) {
    return;
  }

  const endpoint = wrapperEl.dataset.chartEndpoint;
  const loadingMessage = wrapperEl.dataset.loadingMessage || 'Carregando...';
  const emptyMessage = wrapperEl.dataset.emptyMessage || 'Sem dados para gerar o gráfico.';
  const errorMessage = wrapperEl.dataset.errorMessage || 'Erro ao carregar dados do gráfico.';
  const timeoutMessage = wrapperEl.dataset.timeoutMessage || errorMessage;
  const cacheMessage = wrapperEl.dataset.cacheMessage || 'Exibindo dados recentes em cache.';
  const retryLabel = wrapperEl.dataset.retryLabel || 'Tentar novamente';
  const statusLoading = wrapperEl.dataset.statusLoading || loadingMessage;
  const statusReady = wrapperEl.dataset.statusReady || 'Gráfico carregado.';
  const statusError = wrapperEl.dataset.statusError || errorMessage;
  const statusTimeout = wrapperEl.dataset.statusTimeout || timeoutMessage;

  if (!endpoint) {
    setStatus(statusEl, statusError, 'error');
    return;
  }

  let pendingLoad = null;

  const loadChart = async ({ forceNetwork = false } = {}) => {
    const rivalId = rivalSelectEl.value?.trim() || '';
    const cacheKey = rivalId || '__self__';
    if (forceNetwork) {
      chartCache.delete(cacheKey);
    }

    const requestToken = ++latestRequestToken;
    setChartBusy(chartEl, true);
    setStatus(statusEl, statusLoading, 'loading');
    wrapperEl.classList.add('chart-wrapper--loading');

    try {
      if (!forceNetwork && chartCache.has(cacheKey)) {
        await syncServerCacheVersion(endpoint);
      }
      const result = await fetchChartPayload(endpoint, rivalId, requestToken);
      if (!result || result.stale || requestToken !== latestRequestToken) {
        return;
      }

      if (!hasSeriesData(result.payload)) {
        destroyChartIfAny();
        chartEl.textContent = emptyMessage;
        setStatus(statusEl, emptyMessage, 'warn');
        return;
      }

      chartEl.textContent = '';
      await renderChart(chartEl, result.payload);
      if (result.fromCache) {
        setStatus(statusEl, cacheMessage, 'ready');
      } else {
        setStatus(statusEl, statusReady, 'ready');
      }
    } catch (error) {
      if (error?.name === 'AbortError') {
        return;
      }
      if (requestToken !== latestRequestToken) {
        return;
      }
      const timeoutOcorreu = error?.name === 'TimeoutError';
      destroyChartIfAny();
      setStatus(statusEl, timeoutOcorreu ? statusTimeout : statusError, 'error');
      chartEl.textContent = timeoutOcorreu ? timeoutMessage : errorMessage;
      mountRetryButton(chartEl, retryLabel, () => {
        loadChart({ forceNetwork: true }).catch(() => {
          // fallback tratado em loadChart
        });
      });
    } finally {
      if (requestToken === latestRequestToken) {
        wrapperEl.classList.remove('chart-wrapper--loading');
        setChartBusy(chartEl, false);
      }
    }
  };

  const handleRivalChange = () => {
    if (pendingLoad) {
      clearTimeout(pendingLoad);
    }
    pendingLoad = setTimeout(() => {
      loadChart().catch(() => {
        // fallback tratado em loadChart
      });
    }, 120);
  };

  rivalSelectEl.addEventListener('change', handleRivalChange);
  window.addEventListener('beforeunload', () => {
    if (activeController) {
      activeController.abort();
    }
    destroyChartIfAny();
  });

  loadChart().catch(() => {
    // fallback tratado em loadChart
  });
}
