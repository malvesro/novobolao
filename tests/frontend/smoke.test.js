import { describe, expect, it } from 'vitest';
import { initJogosPage } from '../../src/frontend/pages/jogos.js';
import { initGraficoDesempenhoPage } from '../../src/frontend/pages/graficoDesempenho.js';

describe('frontend test harness smoke', () => {
  it('deve importar módulos de página e executar init sem lançar exceção', () => {
    document.body.innerHTML = '<div id="jogos-page-wrapper"></div>';

    expect(typeof initJogosPage).toBe('function');
    expect(typeof initGraficoDesempenhoPage).toBe('function');

    expect(() => initJogosPage()).not.toThrow();
    expect(() => initGraficoDesempenhoPage()).not.toThrow();
  });
});
