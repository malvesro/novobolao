const CONTROL_CHARS_PATTERN = /[\u0000-\u001F\u007F]/g;
const HTML_LIKE_PATTERN = /<[^>]+>|&[a-z0-9#]{1,10};/gi;

function sanitizeValue(value) {
  if (!value) {
    return value;
  }

  let sanitized = value.normalize('NFKC');
  sanitized = sanitized.replace(CONTROL_CHARS_PATTERN, '');
  sanitized = sanitized.replace(HTML_LIKE_PATTERN, '');
  return sanitized;
}

function sanitizeField(field) {
  if (!field) {
    return;
  }

  const sanitized = sanitizeValue(field.value);
  if (sanitized !== field.value) {
    field.value = sanitized;
    field.dataset.sanitized = 'true';
  }
}

export function initCadastroSanitizer() {
  const form = document.querySelector('form[action$="cadastro.action"]');
  if (!form) {
    return;
  }

  const inputs = form.querySelectorAll('input[type="text"], input[type="password"], input[type="email"]');
  inputs.forEach((input) => {
    input.addEventListener('input', () => sanitizeField(input));
    input.addEventListener('blur', () => sanitizeField(input));
  });

  form.addEventListener('submit', () => {
    inputs.forEach((input) => sanitizeField(input));
  });
}
