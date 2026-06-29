---
name: chat-loops-orquestracao
description: Use esta skill para planejar e executar melhorias do chat do bolão em loops curtos e paralelos (executor + reviewer), cobrindo citação, consulta de mensagens, respostas encadeadas, segurança e regressão com definição de pronto mensurável.
---

# Chat Loops Orquestração

## Quando esta skill deve ser usada
Use quando a solicitação mencionar ou implicar:

- melhoria da tela de chat;
- comunicação entre participantes;
- citação de mensagens;
- consulta/busca de histórico;
- respostas encadeadas (reply);
- hardening de menções, ACK e notificações;
- execução por loops com multiagentes.

Exemplos de gatilho:
- "melhorar chat"
- "citar mensagem e responder"
- "buscar mensagens antigas"
- "rodar em loops com executor e reviewer"

## Quando NÃO usar
Não usar esta skill para:

- tarefas sem relação com chat/comunicação;
- mudanças puramente de infraestrutura sem impacto no chat;
- correções isoladas que não precisam de ciclo iterativo com revisão paralela.

## Fluxo padrão do loop

1. **Diagnóstico rápido (estado atual)**
- Ler tarefas de chat recentes em `passo-a-passo.md`.
- Ler arquivos impactados de chat (action/service/frontend/testes).
- Classificar o loop: `CITACAO`, `CONSULTA`, `RESPOSTA`, `SEGURANCA`, `REGRESSAO`.

2. **Definir micro-objetivo (1 iteração)**
- Objetivo pequeno, testável e com risco explícito.
- Exemplo: "Adicionar `replyToMessageId` com renderização de contexto da mensagem pai".

3. **Executar em paralelo (obrigatório)**
- Executor implementa código e testes.
- Reviewer valida arquitetura, segurança, UX e qualidade de testes.

4. **Definição de pronto (mensurável)**
Use este checklist mínimo:
- [ ] funcionalidade implementada
- [ ] testes backend do cenário novo
- [ ] testes frontend do cenário novo
- [ ] regressão executada
- [ ] rastreabilidade atualizada (`passo-a-passo.md` + log)

**Regra:** pronto = 100% dos itens aplicáveis concluídos.

5. **Validação técnica**
Executar no mínimo (ou justificar suíte focada):
- `mvn -Dfrontend.skip=true test`
- `npm run -s test:frontend`
- `npm run -s build`
- `git diff --check`

6. **Registrar evidências**
- Atualizar plano em `passo-a-passo.md`.
- Registrar log em `.ia/logs/`.
- Registrar resultado do loop com template em `references/template-resultado-loop.md`.

## Próximos loops recomendados para Chat 2.2

- `LOOP-A`: Citação com contexto visual.
- `LOOP-B`: Respostas encadeadas (`replyToMessageId`).
- `LOOP-C`: Consulta avançada (termo/autor/período/paginação).
- `LOOP-D`: Hardening de segurança e privacidade.
- `LOOP-E`: Regressão final e prontidão operacional.

## Guardrails

- Preservar contratos estáveis entregues nas tarefas 101-104.
- Não introduzir dependências legadas.
- Evitar refatoração ampla fora do escopo do loop.
- Não fechar loop sem parecer do reviewer.

## Referências (carregar sob demanda)

- Avaliação das tarefas já executadas: `references/avaliacao-chat-98-104.md`
- Template de resultado do loop: `references/template-resultado-loop.md`
- Casos de avaliação da skill: `references/evals-casos-chat-loops.md`
