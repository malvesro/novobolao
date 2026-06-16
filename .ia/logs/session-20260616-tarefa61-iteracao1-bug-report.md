# Sessao: Tarefa 61.1 - Bug report técnico inicial

**Data:** 2026-06-16
**Autor:** Codex (GPT-5)
**Skills Utilizadas:** modern-javascript-patterns v1.0.0, htmx v1.0.0, architecture-guardian v1.0.0

## 1. Prompt Principal (Input)

> Investigar bugs: alerta de saída indevido no admin e desaparecimento do campo de palpite.

## 2. Resposta da IA (Output Resumido)

> Bugs documentados no plano com subtarefas específicas de correção.
>
> Diagnóstico técnico inicial:
> - Bug 1 (admin): contador `pendingAdminRequests` incrementa no `beforeRequest`, mas pode não decrementar no `afterRequest` quando o trigger é substituído por `hx-swap="outerHTML"` na linha admin.
> - Bug 2 (palpites): mismatch entre contrato esperado no JS atual (`palpite-cell` + `form.palpite-inputs`) e markup legado em `match-row.jspf` (inputs soltos com target `closest tbody`).

## 3. Validacao (Build/Teste)

- Comando: N/A (iteração de documentação e análise)
- Resultado: N/A
- Observacoes: execução técnica seguirá nas próximas subtarefas 61.2+.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** tarefa preparada para correção incremental com foco em regressão mínima e rastreabilidade completa.
