# L7 - Loop de ADR para Decisões Estruturais do Chat

## Tarefa repetida
Quando há mudança estrutural (ex.: estado durável, política de entrega), registrar ADR com trade-offs.

## Evidência de repetição
- 102 referências a ADR/decisão arquitetural no histórico local
- ADR específica recente: `ADR-20260629-chat-2-1-3-entrega-cross-screen-e-estado-duravel.md`

## Teste das 4 condições
1. Repete? **Sim (8/10)**
2. Definição clara de pronto? **Sim (8/10)**
3. Custo de tokens aceitável? **Médio (7/10)**
4. Ferramentas para executar/verificar? **Sim (8/10)**

## Nota final (0-10)
**7.6/10**

Motivo: importante para governança, mas não ocorre em toda iteração funcional.

## Agentes (paralelo)
- Executor: **Architect Agent**
- Revisor: **Reviewer Agent**
- Paralelismo: architect redige ADR enquanto developer fecha implementação/testes.

## Definição de pronto (mensurável, 100%)
- [ ] contexto/problema descritos
- [ ] alternativas e trade-offs listados
- [ ] decisão e justificativa explícitas
- [ ] impactos e rollback definidos
- [ ] link cruzado com plano/log da tarefa

**Cobertura exigida:** 5/5 critérios = **100%**.

## Resultado da execução
- Status: `Pendente`
- Data:
- Evidências:
