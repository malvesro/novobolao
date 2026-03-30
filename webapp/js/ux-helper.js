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
});
