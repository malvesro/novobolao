import { initTooltips } from './modules/tooltips.js';
import { initJogosPage } from './pages/jogos.js';

function onReady(callback) {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', callback, { once: true });
    return;
  }
  callback();
}

onReady(() => {
  initTooltips();
  initJogosPage();
});
