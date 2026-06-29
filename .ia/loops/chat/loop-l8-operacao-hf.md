# L8 - Loop de Operação HF (Polling, Degradação e Cold Start)

## Tarefa repetida
Ajustar e validar comportamento do chat em ambiente Hugging Face gratuito (sleep/restart/cpu limitada).

## Evidência de repetição
- Tarefas 103 e 104 explicitam contexto HF (polling, jitter, cold start, degradação)
- Revisões recorrentes de resiliência operacional no chat

## Teste das 4 condições
1. Repete? **Parcialmente (7/10)**
2. Definição clara de pronto? **Parcial (7/10)**
3. Custo de tokens aceitável? **Médio-alto (6/10)**
4. Ferramentas para executar/verificar? **Parcial (8/10)**

## Nota final (0-10)
**6.9/10**

Motivo: loop valioso, mas com maior variabilidade ambiental e maior custo de validação real.

## Agentes (paralelo)
- Executor: **Architect/Developer Agent**
- Revisor: **Tester Agent**
- Paralelismo: developer aplica ajustes de polling enquanto tester valida cenários de aba ativa/inativa e recuperação.

## Definição de pronto (mensurável, 100%)
- [ ] polling reduzido em aba inativa
- [ ] retomada correta em aba ativa
- [ ] fallback claro em falha/cold start
- [ ] ausência de rajada anômala de requisições
- [ ] evidência de estabilidade registrada no log

**Cobertura exigida:** 5/5 critérios = **100%**.

## Resultado da execução
- Status: `Pendente`
- Data:
- Evidências:
