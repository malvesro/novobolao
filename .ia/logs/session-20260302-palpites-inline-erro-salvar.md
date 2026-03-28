# Sessao: Palpites inline - erro ao salvar

**Data:** 2026-03-02
**Autor:** Assistente Tecnico Lider/IA
**Skills Utilizadas:** modernization-java-migration v1.0.0

## 1. Prompt Principal (Input)

> Nova situação: Agora aparecem os campos para informar o palpite, mas após informar os valores numéricos resulta no erro: "Não foi possível carregar o palpite selecionado. Tente novamente." Registre a nova situação, planeje estratégias de correção e atualize o passo-a-passo.md.

## 2. Resumo da Situação

> Após a publicação do fluxo inline, o formulário rende corretamente, porém o submit HTMX (`/seguro/atualizarPalpitePartial.action`) retorna estado de erro e o fragmento exibe a mensagem "Não foi possível carregar o palpite selecionado.". Precisamos investigar se a action retorna ERROR por falha na consulta `getPalpiteService().buscarPalpiteDoJogo`, se há exceção na atualização ou algum dado inválido (ex.: `jogoId`/CSRF/timezone).

## 3. Estratégia Proposta

1. **Instrumentação rápida:** logar temporariamente parâmetros e exceções em `atualizarPalpiteHtmx`/`prepararConteudoPalpite` para capturar stack trace e valores (`jogoId`, horários, retorno do serviço). Registrar resposta completa (status/headers) via navegador ou `curl` com cookies.
2. **Validação de dados:** confirmar se `jogoService.buscarPorId(jogoId)` está retornando `null` após o POST (possível falta de binding `jogoId` ou sanitização) e revisar `PalpiteService.atualizarPalpite` quanto ao uso de timezone e persistência.
3. **Fluxo de retorno HTMX:** garantir que, mesmo em caso de erro, a action devolva fragmento válido com detalhe da mensagem (evitar fallback genérico). Considerar `PalpiteAuthorizationService` para recalcular status após a atualização.
4. **Testes automatizados:** preparar teste de integração (Spring MVC Test) simulando o POST para reproduzir cenário futuro e evitar regressões.

## 4. Próximos Passos

- Atualizar `passo-a-passo.md` apontando a nova investigação dentro da subtarefa 4d.
- Complementar o plano `.ia/planos/plano-correcao-palpites-popup.md` com esta etapa de diagnóstico/telemetria.

