# Avaliação Consolidada das Tarefas 98-104 (Chat)

## Resumo executivo
O ciclo 98-104 entregou uma base robusta para chat funcional, menções cross-screen, persistência durável de notificações e hardening operacional.

### Forte maturidade já alcançada
- envio e leitura incremental de mensagens;
- menções `@login` e `@Todos` com ACK idempotente;
- badge/toast cross-screen;
- observabilidade e degradação controlada para ambiente restrito;
- cobertura de testes em crescimento com regressões frequentes.

### Lacunas para o próximo ciclo (Chat 2.2)
1. **Citação de mensagem com contexto explícito**
- hoje há menção textual; falta mecanismo formal de “citar esta mensagem”.

2. **Resposta vinculada (`reply-to`)**
- envio existe, mas sem vínculo estruturado com mensagem de origem.

3. **Consulta avançada de histórico**
- leitura incremental existe; faltam busca/filtro por autor/período/termo com UX objetiva.

## Mapeamento por objetivo funcional

### Citação / menções
- Coberto: tarefas 101, 102, 103, 104.
- Gap: citação semântica (bloco de contexto do trecho citado).

### Consultar mensagens
- Coberto: tarefa 98 (janela inicial + incremental), hardening nas 99/103/104.
- Gap: consulta avançada orientada a usuário (busca/filtros/paginação histórica).

### Enviar respostas
- Coberto: envio funcional e robusto (98/99).
- Gap: resposta encadeada com `replyToMessageId` e renderização contextual.

## Próximo backlog orientado a loops
- Loop A: Citação com contexto e UX de seleção.
- Loop B: Reply encadeado com contrato backend e renderização frontend.
- Loop C: Consulta avançada (filtro/busca/paginação).
- Loop D: Hardening de segurança específico para os novos contratos.
- Loop E: Regressão e performance em HF.
