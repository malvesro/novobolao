import { afterEach, vi } from 'vitest';

if (!window.htmx) {
  window.htmx = {
    ajax: vi.fn(),
    trigger: vi.fn(),
  };
}

// Alguns módulos usam `htmx` diretamente (sem prefixo `window.`).
globalThis.htmx = window.htmx;

if (!window.APP_BASE_URL) {
  window.APP_BASE_URL = '';
}

afterEach(() => {
  document.body.innerHTML = '';
  window.htmx.ajax.mockClear();
  window.htmx.trigger.mockClear();
});
