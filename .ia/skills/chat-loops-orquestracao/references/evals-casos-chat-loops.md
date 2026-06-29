# Evals - Casos da Skill chat-loops-orquestracao

## Caso 1 - Citação + resposta encadeada

Entrada do usuário:
"Evolua o chat para permitir citar uma mensagem e responder em thread curta."

Resultado esperado da skill:
- classificar como `CITACAO` + `RESPOSTA`;
- decompor em loop pequeno com executor/reviewer;
- exigir checklist de pronto com 100%;
- exigir testes backend/frontend e regressão.

## Caso 2 - Consulta avançada de histórico

Entrada do usuário:
"Quero filtrar mensagens por participante e por período."

Resultado esperado da skill:
- classificar como `CONSULTA`;
- propor contrato e UX incremental sem quebrar polling atual;
- incluir testes de busca/filtro e cenários de borda.

## Caso 3 - Pedido fora do escopo

Entrada do usuário:
"Otimize o Dockerfile do app."

Resultado esperado da skill:
- reconhecer como fora de escopo do chat loops;
- recomendar skill/fluxo de infraestrutura apropriado.
