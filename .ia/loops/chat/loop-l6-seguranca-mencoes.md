# L6 - Loop de Segurança do Fluxo de Menções/ACK

## Tarefa repetida
Revisar a cada evolução do chat: autenticação, autorização, CSRF, idempotência de ACK e logs sem dados sensíveis.

## Evidência de repetição
- Subtarefas explícitas de segurança nas tarefas 98, 101, 103 e 104
- Referências recorrentes a hardening de segurança/privacidade e checklist OWASP

## Teste das 4 condições
1. Repete? **Sim (8/10)**
2. Definição clara de pronto? **Sim (8/10)**
3. Custo de tokens aceitável? **Médio (7/10)**
4. Ferramentas para executar/verificar? **Sim (10/10)**

## Nota final (0-10)
**8.2/10**

Motivo: essencial e recorrente, porém com validação mais cara e mais contextual.

## Agentes (paralelo)
- Executor: **Security Agent**
- Revisor: **Reviewer Agent**
- Paralelismo: security audita endpoints enquanto tester roda suíte de regressão.

## Definição de pronto (mensurável, 100%)
- [ ] ACK apenas por POST autenticado
- [ ] proteção CSRF comprovada em teste
- [ ] autorização valida (401/403 conforme cenário)
- [ ] logs sem payload sensível de mensagem
- [ ] checklist OWASP do fluxo atualizado no log

**Cobertura exigida:** 5/5 critérios = **100%**.

## Resultado da execução
- Status: `Pendente`
- Data:
- Evidências:
