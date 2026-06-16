# Sessao: Tarefa 63 - Correcao UX de Posicao dos Campos de Palpite

**Data:** 2026-06-16
**Autor:** Codex (GPT-5)
**Skills Utilizadas:** htmx v1.0.0, modern-javascript-patterns v1.0.0, architecture-guardian v1.0.0

## 1. Prompt Principal (Input)

> "Na tela de palpites, os campos onde digitar o palpite mudou de posicao e ficou confuso, ficou a direita da tela. Antes cada campo para digitar o numero de gols ficava ao lado do nome do time."

## 2. Resposta da IA (Output Resumido)

> Ajuste implementado para restaurar UX dos inputs ao lado das equipes:
> - `match-row.jspf`: inputs `p1/p2` voltaram para as celulas de times (home/away) quando `palpitePermitido`.
> - Mantido contrato HTMX seguro por celula (`hx-target="#palpite-cell_<id>"`, `hx-swap="outerHTML"`), sem retorno ao modelo legado `closest tbody`.
> - `palpite-cell-response.jspf`: removido formulario duplicado da coluna PALPITE; mantidos feedback/status/meta.
> - `jogos.js`: fluxo de estado atualizado para lidar com triggers de `.palpite-inputs__score` (saving/error/retry/dirty sem dependencia obrigatoria de form no cell).
> - `tests/frontend/jogos.test.js`: atualizado para o novo contrato estrutural e fluxo de retry/input inline.

## 3. Validacao (Build/Teste)

- Comando: `npm run test:frontend -- jogos.test.js`
- Resultado: Sucesso (4 testes, 0 falhas)
- Observacoes: cenarios criticos de dirty/beforeunload, retry e contrato HTMX aprovados.

- Comando: `npm run test:frontend -- graficoDesempenho.test.js`
- Resultado: Sucesso (2 testes, 0 falhas)
- Observacoes: regressao cruzada do grafico preservada.

- Comando: `npm run build`
- Resultado: Sucesso
- Observacoes: bundle atualizado; warning conhecido de chunk grande do ApexCharts mantido.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Correcao focada em UX sem reintroduzir o padrao de swap amplo que causava regressao anterior.

