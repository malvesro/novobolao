# Sessao: Tarefa 78.12 - Correção de conflito de scripts no botão de grupo

**Data:** 2026-06-17
**Autor:** Codex (GPT-5)
**Skills Utilizadas:** architecture-guardian v1.0.0, senior-java-dev-legacy v1.0.0, htmx, security-audit v1.0.0

## 1. Prompt Principal (Input)

> "O problema permanece, o botão muda de cor mas não lista os palpites."

## 2. Resposta da IA (Output Resumido)

> Causa raiz identificada: havia dupla orquestração de clique para o mesmo botão (`data-js=toggle-group-details`).
>
> - `src/frontend/pages/jogos.js` (fluxo novo) abria/fechava o accordion e disparava carregamento dos palpites.
> - `webapp/js/ux-helper.js` (legado, ainda incluído em `jogos.jsp`) também fazia `target.classList.toggle('hidden')` no mesmo clique.
>
> Efeito prático: o segundo toggle revertia a abertura, mantendo o botão com estado visual alterado sem exibir a lista.
>
> Correção aplicada:
> - removido include de `ux-helper.js` em `webapp/WEB-INF/content/seguro/jogos.jsp`;
> - adicionado teste de regressão em `tests/frontend/jogos.test.js` para garantir que a tela não carregue mais o script legado.

## 3. Validacao (Build/Teste)

- Comando: `npm run -s test:frontend -- tests/frontend/jogos.test.js`
- Resultado: Sucesso
- Observacoes: 16 testes aprovados.

- Comando: `mvn -q -Dfrontend.skip=true -Dtest=PalpiteAuthorizationServiceImplTest,ParticipanteActionLoadTest test`
- Resultado: Sucesso
- Observacoes: sem regressão backend (incluindo janela temporal de palpites).

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** correção focada na remoção de conflito JS; regras de negócio permanecem inalteradas.

> NOTE: Este log deve ser criado para cada tarefa relevante.
