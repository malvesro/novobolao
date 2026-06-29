# Loops de Engenharia - Ranking para Melhorias do Chat

Data da análise: 2026-06-29
Escopo: histórico em `passo-a-passo.md`, logs em `.ia/logs/` e artefatos do workspace.

## Evidências Quantitativas (base local)

- Referências a `passo-a-passo.md`: **279**
- Termos de execução multiagente (`multiagente`, `Trilha`, `Architect`, `Tester`, `Reviewer`, `Security`): **274**
- Execuções de `mvn -Dfrontend.skip=true test`: **120**
- Execuções de testes frontend (`test:frontend` chat/jogos): **127**
- Execuções de build frontend (`npm run build`/`npm run -s build`): **138**
- Referências a ADR/decisão arquitetural: **102**
- Referências a `git diff --check`: **16**

## Ranking (melhor candidata -> pior candidata)

1. **L1 - Validação Técnica Final Automatizada** (`loop-l1-validacao-tecnica-final.md`) — **9.8/10**
2. **L2 - Atualização de Rastreabilidade no Plano** (`loop-l2-rastreabilidade-plano.md`) — **9.5/10**
3. **L3 - Log de Sessão Estruturado** (`loop-l3-log-sessao.md`) — **9.2/10**
4. **L4 - Execução Multiagente com Revisão Paralela** (`loop-l4-multiagente-review.md`) — **9.0/10**
5. **L5 - Hardening + Testes de Chat por Iteração** (`loop-l5-hardening-chat.md`) — **8.7/10**
6. **L6 - Segurança do Fluxo de Menções/ACK** (`loop-l6-seguranca-mencoes.md`) — **8.2/10**
7. **L7 - Governança de ADR Arquitetural** (`loop-l7-adr-governanca.md`) — **7.6/10**
8. **L8 - Operação HF (Polling/Degradação/Cold Start)** (`loop-l8-operacao-hf.md`) — **6.9/10**

## Regra Comum de "Pronto" para todos os loops

Cada loop só encerra com:

1. executor concluiu as etapas técnicas;
2. reviewer validou 100% dos checkpoints do loop;
3. documentação markdown do loop atualizada com resultado;
4. cobertura de testes da própria definição de pronto em **100% dos critérios aplicáveis**.
