# Sessao: Tarefa 78 - Correcao do bloqueio global de palpites na listagem

**Data:** 2026-06-17
**Autor:** Codex (GPT-5)
**Skills Utilizadas:** architecture-guardian v1.0.0, senior-java-dev-legacy v1.0.0, htmx (guideline)

## 1. Prompt Principal (Input)

> Novo bug: todos os jogos na tela de Palpites e Resultados aparecem como "Edicao Encerrada", incluindo jogos futuros. Prosseguir em pequenas iteracoes ate completar a tarefa 78.

## 2. Resposta da IA (Output Resumido)

> Causa raiz confirmada: o fragmento `jogos-lista-fragmento.jsp` recalculava permissao de palpite com `sec:authorize` local, divergindo da regra canonica do backend (`PalpiteAuthorizationService`).

Alteracoes aplicadas:
- `ParticipanteAction`:
  - novo mapa `autorizacoesPalpitePorJogo` (por `jogoId`);
  - novo metodo `prepararAutorizacoesPalpitePorJogo()` executado em `prepararInfoPalpites()` e `buscarMaisJogosHtmx()`;
  - getter publico para consumo na JSP.
- `jogos-lista-fragmento.jsp`:
  - remocao da logica de permissao baseada em `hasRole('ADMIN'/'USER')`;
  - consumo exclusivo de `autorizacoesPalpitePorJogo[jogo.id]` para `palpitePermitido`, `palpiteStatus` e `palpiteBloqueioMotivo`.
- Testes:
  - `ParticipanteActionLoadTest`: novos cenarios cobrindo mapa canonico na carga inicial e no carregamento incremental;
  - `tests/frontend/jogos.test.js`: contrato atualizado para garantir o uso da autorizacao canonica e ausencia de `hasRole(...)` no fragmento.

## 3. Validacao (Build/Teste)

- Comando: `npm run test:frontend -- tests/frontend/jogos.test.js`
- Resultado: **Sucesso** (`13` testes aprovados)
- Observacoes: contrato de markup/HTMX preservado apos remocao da logica duplicada de permissao.

- Comando: `mvn -Dfrontend.skip=true -Dtest=ParticipanteActionLoadTest,PalpiteAuthorizationServiceImplTest test`
- Resultado: **Sucesso** (`22` testes aprovados)
- Observacoes: inclui cobertura do mapa canonico por jogo e validacao da regra de autorizacao de palpite no servico.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Correcao reduz acoplamento view-regra e remove fonte de regressao por duplicidade de autorizacao entre JSP e backend.
