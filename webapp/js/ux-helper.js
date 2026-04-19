/**
 * UX Helper for Bolao 2026
 * Gerenciamento de foco e comportamentos inteligentes na tela de palpites.
 */
document.addEventListener('DOMContentLoaded', () => {
    
    // Auto-Advance logic
    document.body.addEventListener('keyup', (e) => {
        if (e.target.classList.contains('palpite-inputs__score')) {
            const input = e.target;
            const value = input.value;
            
            // Se o usuário digitou um número (ou atingiu o limite de caracteres esperado)
            // Aqui podemos ser mais conservadores: se o input está completo ou após um pequeno delay
            // Por simplicidade, vamos usar o evento 'change' ou uma tecla específica como 'Enter' ou 'Tab'
        }
    });

    // Delegar eventos para inputs dinâmicos (HTMX traz novos elementos)
    document.body.addEventListener('keydown', (e) => {
        if (e.target.classList.contains('palpite-inputs__score')) {
            const input = e.target;
            
            // Navegação por setas ou Enter
            if (e.key === 'Enter') {
                e.preventDefault();
                focusNextInput(input);
            }
        }
    });

    function focusNextInput(currentInput) {
        const inputs = Array.from(document.querySelectorAll('.palpite-inputs__score'));
        const index = inputs.indexOf(currentInput);
        if (index > -1 && index < inputs.length - 1) {
            inputs[index + 1].focus();
            inputs[index + 1].select();
        }
    }

    // Feedback visual para "Group Details"
    document.body.addEventListener('click', (e) => {
        const btn = e.target.closest('[data-js="toggle-group-details"]');
        if (btn) {
            const targetId = btn.getAttribute('data-target');
            const target = document.querySelector(targetId);
            if (target) {
                target.classList.toggle('hidden');
                btn.classList.toggle('btn-grupo-toggle--active');
            }
        }

        const closeBtn = e.target.closest('[data-js="close-details"]');
        if (closeBtn) {
            const targetId = closeBtn.getAttribute('data-target');
            const target = document.querySelector(targetId);
            if (target) {
                target.classList.add('hidden');
            }
        }
    });

    function updateProgress() {
        const rows = document.querySelectorAll('.match-row[data-jogo-id]');
        const total = rows.length;
        if (total === 0) return;

        let filled = 0;
        rows.forEach(row => {
            const p1 = row.querySelector('input[name="palpiteGolsEquipe1"]');
            const p2 = row.querySelector('input[name="palpiteGolsEquipe2"]');
            
            if (p1 && p2) {
                if (p1.value !== '' && p2.value !== '') {
                    filled++;
                }
            } else {
                const score = row.querySelector('.palpite-saved-score');
                if (score) filled++;
            }
        });

        const percent = Math.round((filled / total) * 100);
        const bar = document.getElementById('progressBarFill');
        const text = document.getElementById('progressText');
        const badge = document.getElementById('progressBadge');

        if (bar) bar.style.width = percent + '%';
        if (text) text.innerText = filled + ' / ' + total;
        
        if (badge) {
            if (percent === 100) {
                badge.innerText = 'Completo';
                badge.className = 'badge badge--registered';
            } else {
                badge.innerText = 'Incompleto';
                badge.className = 'badge badge--pending';
            }
        }
    }

    // Inicializar progresso
    updateProgress();

    // Listen to HTMX afterSwap to update progress when a match container is updated
    document.body.addEventListener('htmx:afterSwap', (e) => {
        // Se o swap for de um container de jogo (tbody ou match-row)
        if (e.detail.target.tagName === 'TBODY' || e.detail.target.classList.contains('match-row')) {
            updateProgress();
        }
    });

    // Gerenciamento do Side Drawer Administrativo (ESC to close)
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && document.body.classList.contains('drawer-open')) {
            closeDrawer();
        }
    });

    // Orquestração Automática via HTMX
    document.body.addEventListener('htmx:afterOnLoad', (e) => {
        // Se o conteúdo foi carregado no drawer, abre o painel
        if (e.detail.target.id === 'admin-drawer-content') {
            openDrawer();
            
            // Highlight da linha correspondente na tabela
            const gameId = e.detail.xhr.responseURL.split('id=')[1];
            if (gameId) {
                document.querySelectorAll('.match-row--admin').forEach(r => r.classList.remove('match-row--selected'));
                const row = document.getElementById('jogoTr_' + gameId);
                if (row) row.classList.add('match-row--selected');
            }
        }
    });

    function openDrawer() {
        document.body.classList.add('drawer-open');
    }

    function closeDrawer() {
        document.body.classList.remove('drawer-open');
        document.querySelectorAll('.match-row--admin').forEach(r => r.classList.remove('match-row--selected'));
    }

    // Feedback de Auto-Save para Administração
    document.body.addEventListener('htmx:afterRequest', (e) => {
        const target = e.detail.elt;
        if (target.classList.contains('form-control-inline') || target.classList.contains('score-input')) {
            if (e.detail.successful) {
                target.classList.add('save-flash');
                setTimeout(() => target.classList.remove('save-flash'), 1000);
            } else {
                target.classList.add('save-error');
                setTimeout(() => target.classList.remove('save-error'), 1000);
            }
        }
    });

    // Password Visibility Toggle
    document.body.addEventListener('change', (e) => {
        if (e.target.classList.contains('js-toggle-password')) {
            const isChecked = e.target.checked;
            const type = isChecked ? 'text' : 'password';
            document.querySelectorAll('.js-password-field').forEach(field => {
                field.setAttribute('type', type);
            });
        }
    });

    // Expor globalmente para botões inline (onclick)
    window.closeDrawer = closeDrawer;
});
