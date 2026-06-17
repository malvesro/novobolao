# Sessao: Tarefa 78.8 - Robustez do botao Ver palpites do grupo

**Data:** 2026-06-17
**Autor:** Codex (GPT-5)
**Skills Utilizadas:** htmx (guideline), modern-javascript-patterns v1.0.0

## 1. Prompt Principal (Input)

> Usuario comum consegue palpitar, mas o botao "Ver palpites do grupo" parece nao fazer nada. Confirmar se deveria mostrar o palpite do proprio usuario.

## 2. Resposta da IA (Output Resumido)

> Confirmacao funcional: sim, ao clicar em "Ver palpites do grupo", a tela deve exibir os palpites registrados para o jogo, incluindo o do usuario logado quando existir.

Diagnostico tecnico:
- O fluxo estava sensivel a eventos de clique/HTMX declarativo, gerando a percepcao de "nao acontece nada" em alguns cenarios.

Ajuste aplicado (robustez):
- `jogos.js` passou a disparar explicitamente `htmx.ajax('GET', hx-get, { target, swap: 'innerHTML' })` ao abrir o accordion do grupo e quando ainda nao carregado.
- Mantido controle de estado por botao: `data-group-loading` e `data-group-loaded`.
- Limpeza de loading em `htmx:afterSwap` e `htmx:responseError`.
- `match-row.jspf`: removido `hx-trigger` declarativo do botao de grupo para evitar concorrencia/event-order entre handlers.

## 3. Validacao (Build/Teste)

- Comando: `npm run test:frontend -- tests/frontend/jogos.test.js`
- Resultado: **Sucesso** (`15` testes aprovados)
- Observacoes:
  - novo teste cobrindo disparo explicito de `htmx.ajax` ao abrir grupo;
  - teste de contrato atualizado para refletir remocao de `hx-trigger` no botao.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** mudanca focada em resiliencia de UX/HTMX sem alterar regra de negocio de permissao de palpite.
