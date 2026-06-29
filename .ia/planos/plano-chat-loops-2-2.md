# Plano Chat 2.2 - Loops de Evolução (Citação, Consulta e Respostas)

Data: 2026-06-29
Base técnica: Chat 2.1.3 concluído (tarefas 98-104).

## Objetivo
Evoluir a comunicação no chat com foco em:
- citação de mensagens;
- consulta avançada de histórico;
- respostas encadeadas.

## Estratégia por loops (paralelo)

### Loop A - Citação com contexto
- Executor: Developer
- Reviewer: UX + Reviewer
- Entregas:
  - seleção de mensagem para citação;
  - bloco de citação no composer e na mensagem enviada;
  - contrato backend para persistir referência citada.

### Loop B - Respostas encadeadas
- Executor: Developer
- Reviewer: Architect + Reviewer
- Entregas:
  - `replyToMessageId` no contrato;
  - renderização de contexto da mensagem pai;
  - fallback para pai inexistente/removido.

### Loop C - Consulta avançada
- Executor: Developer
- Reviewer: UX + Tester
- Entregas:
  - busca por termo;
  - filtro por autor/período;
  - paginação histórica sem quebrar incremental.

### Loop D - Segurança e privacidade
- Executor: Security
- Reviewer: Reviewer
- Entregas:
  - revisão de autorização/CSRF/XSS dos novos endpoints;
  - validação de logs sem payload sensível.

### Loop E - Testes e regressão
- Executor: Tester
- Reviewer: Reviewer
- Entregas:
  - cobertura dos novos cenários backend/frontend;
  - regressão completa verde.

## Critério de pronto por loop
- 100% dos checkpoints do loop concluídos.
- Executor + Reviewer aprovam.
- Evidências registradas em log + `passo-a-passo.md`.
