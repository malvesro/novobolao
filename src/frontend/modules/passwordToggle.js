/**
 * Password Visibility Toggle Module
 */
const DEBUG_LABEL = '[bolao:pwd-toggle]';

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

export function initPasswordToggle() {
  debugInfo('Inicializando alternância de visibilidade de senha.');

  document.body.addEventListener('change', (e) => {
    if (e.target.classList.contains('js-toggle-password')) {
      const isChecked = e.target.checked;
      const type = isChecked ? 'text' : 'password';
      const fields = document.querySelectorAll('.js-password-field');

      debugInfo('Alternando visibilidade.', { isChecked, fieldsCount: fields.length });

      fields.forEach(field => {
        field.setAttribute('type', type);
      });
    }
  });
}
