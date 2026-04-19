import { initTooltips } from './modules/tooltips.js';
import { initMenuToggle } from './modules/menuToggle.js';
import { initJogosPage } from './pages/jogos.js';
import { initCadastroSanitizer } from './modules/formSanitizer.js';
import { initPasswordToggle } from './modules/passwordToggle.js';

function onReady(callback) {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', callback, { once: true });
    return;
  }
  callback();
}

onReady(() => {
  initTooltips();
  initMenuToggle();
  initJogosPage();
  initCadastroSanitizer();
  initPasswordToggle();
});
