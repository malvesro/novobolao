# Sessao: Tarefa 95 - Consistência visual da linha ao alterar data (admin)

**Data:** 2026-06-26
**Autor:** Codex (GPT-5)
**Skills Utilizadas:** architecture-guardian v1.0.0, htmx, security-audit v1.0.0, ui-ux-pro-max

## 1. Prompt Principal (Input)

> "Outro detalhe: Ao alterar a data de um jogo, a linha do jogo deveria desaparecer da data antiga e só aparecer na consulta da nova data (qua a qual foi alterada); SE a data nova está carregada na tela, deve ser recarregada."

## 2. Resposta da IA (Output Resumido)

> Implementada sincronização de agrupamento visual por data na tela admin após edição estrutural da linha.
>
> Mudanças principais:
> - Captura de transição de data por linha (`oldDate` -> `newDate`) no frontend antes do request HTMX.
> - Pós-swap HTMX com regra de reposicionamento:
>   - remove linha do agrupamento antigo;
>   - move para agrupamento da nova data quando já carregado na tela;
>   - remove portlet/grupo antigo quando ficar vazio;
>   - quando nova data não estiver carregada, remove a linha da tela atual e exibe mensagem global orientando a consulta da nova data.
> - Adicionados contratos de markup para localização dos agrupamentos e data da linha (`data-match-date-group`, `data-jogo-date`).
> - Incluídas mensagens i18n para feedback do cenário "nova data visível" e "nova data fora da tela".
> - Reforço de robustez no frontend para evitar listeners duplicados em reinicializações/testes.
> - Testes frontend ampliados para os três cenários de regressão do fluxo de mudança de data.
>
> Arquivos impactados:
> - `src/frontend/pages/jogos.js`
> - `webapp/WEB-INF/content/seguro/partials/jogos-lista-fragmento.jsp`
> - `webapp/WEB-INF/content/admin/partials/admin-match-row.jsp`
> - `webapp/WEB-INF/content/seguro/jogos.jsp`
> - `src/main/resources/messages.properties`
> - `src/messages.properties`
> - `tests/frontend/jogos.test.js`
> - `passo-a-passo.md`

## 3. Validacao (Build/Teste)

- Comando: `npm run test:frontend`
- Resultado: Sucesso
- Observacoes: 3 arquivos de teste, 36 testes em verde.

- Comando: `mvn -Dfrontend.skip=true -Dtest=AdminActionTest,JogoServiceImplTest test`
- Resultado: Sucesso
- Observacoes: 35 testes em verde.

- Comando: `npm run build`
- Resultado: Sucesso
- Observacoes: build Vite concluído; manifest/assets atualizados.

- Comando: `mvn -Dfrontend.skip=true test`
- Resultado: Sucesso
- Observacoes: 129 testes em verde.

- Comando: `mvn -Dfrontend.skip=true -DskipTests package`
- Resultado: Sucesso
- Observacoes: empacotamento WAR concluído.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Correção validada com cobertura de regressão dedicada e sem impacto negativo detectado em build/testes.
