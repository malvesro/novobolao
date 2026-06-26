# Sessao: Priorizacao arquitetural de riscos funcionais pos-tarefa 82

**Data:** 2026-06-26
**Autor:** Codex (Arquiteto de Software Senior)
**Skills Utilizadas:** architecture-guardian v1.0.0

## 1. Prompt Principal (Input)

> Faça uma priorização arquitetural dos riscos funcionais identificados após a tarefa 82 (filtro de grupo A-H vs domínio 2026, preservação de período no load-more admin, parâmetro filtroSemPalpite na UI sem processamento backend, lacunas de testes de borda). Entregue recomendação objetiva com severidade e plano de subtarefas pequenas e rastreáveis para passo-a-passo.md.

## 2. Resposta da IA (Output Resumido)

Priorizacao registrada no `passo-a-passo.md` como tarefa **84**, com severidade e ordem de execucao:

- P0: corrigir dominio de grupos (`A-H` -> `A-L`) em `AdminAction` e `ParticipanteAction`.
- P1: alinhar contrato do `filtroSemPalpite` no admin (implementar backend ou remover propagacao na UI).
- P1: garantir preservacao de periodo (`dataInicial`/`dataFinal`) no fluxo incremental "Carregar Proxima Data".
- P2: fechar lacunas de testes de borda com foco em grupos `I-L`, filtro sem palpite e composicao de filtros no load-more.

Subtarefas 84.1 a 84.5 foram descritas com entregaveis pequenos e auditaveis.

Arquivo alterado:
- `passo-a-passo.md`

## 3. Validacao (Build/Teste)

- Comando: N/A (atividade de planejamento e rastreabilidade, sem alteracao funcional de codigo)
- Resultado: N/A
- Observacoes: priorizacao baseada em evidencias do log da tarefa 82 e leitura direta dos pontos de codigo/ testes relacionados.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Registro criado para manter rastreabilidade da decisao arquitetural e da fila de execucao proposta.

> NOTE: Este log deve ser criado para cada tarefa relevante.
