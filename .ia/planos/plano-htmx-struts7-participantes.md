# Plano: Análise Profunda HTMX + Struts 7 na Tela de Participantes

**Data:** 2026-02-25  
**Responsável:** Assistente Técnico Líder/IA  
**Status:** Em elaboração  
**Contexto:** Mesmo após ajustes recentes, a tela `admin/participantes.action` continua perdendo o corpo da tabela ao alternar o status via HTMX. Precisamos compreender a causa estrutural (Struts 7 + interceptores + HTMX) e avaliar alternativas mais simples para a UI.

## Objetivos
1. Reproduzir e isolar o comportamento problemático, diferenciando falhas de backend (resultado Struts/decorators/interceptores) e frontend (swap HTMX, composição do DOM).
2. Mapear as cadeias de interceptação do Struts 7 (COOP/COEP/Fetch Metadata, decorators, tokens CSRF) e identificar impactos em requisições HTMX `POST`.
3. Avaliar se a resposta parcial pode ser entregue por meio alternativo (JSON/REST) ou por uma action dedicada sem decorators.
4. Propor estratégias de simplificação da tela (ex.: componentes server-driven simples, HTMX com `hx-get` + partial render, ou reimplementação minimalista).

## Referências e Material de Apoio
- `src/com/opendev/bolao/action/AdminAction.java`
- `src/main/resources/struts.xml`
- `src/main/resources/applicationContext-security.xml` (configuração CSRF/Fetch Metadata)
- `webapp/WEB-INF/content/admin/participantes.jsp` e fragments associados
- Logs recentes `.ia/logs/session-20260225-*`

## Iterações Planejadas

### Iteração 1 – Reproduzir e capturar evidências (Diagnóstico Básico) — **Concluída em 25/02/2026**
- Requisição HTMX reproduzida via `curl` autenticado -> resposta HTML completa (layout inteiro) comprovando que o *result* `success` está sendo usado.
- Cabeçalhos `HX-Request`, `X-Requested-With`, `Sec-Fetch-*`, `Origin` e `Referer` presentes; resposta HTTP 200 (sem bloqueio de segurança).
- Evidência salva (arquivo de resposta capturado) indica que o fragmento não é entregue; avançar para inspeção de interceptores/decorators.

### Iteração 2 – Análise de Interceptores e Segurança
- Mapear a configuração Fetch Metadata (`FetchMetadataInterceptor`) e políticas personalizadas: garantir que `POST` same-site com header `X-Requested-With` seja permitido.
- Validar CSRF: confirmar token presente no body e nos heades, inspecionar `CookieCsrfTokenRepository`.
- Checar logs do Tomcat para ver se há entradas `Potential CSRF` ou `FetchMetadata` rejeitando requests HTMX.
- Criar testes `curl` com headers equivalentes aos navegadores para verificar divergências.
- **2026-02-25 (manhã):** Instrumentação adicionada (`HtmxDebugInterceptor` + logs no `AdminAction` com `ServletRequestAware/ServletResponseAware`) registrando `HX-*`, `X-Requested-With` e `Sec-Fetch-*` antes e depois da stack de segurança.
- **2026-02-25 (tarde):** Requisição autenticada reproduzida via `curl` com `_csrf`, `X-XSRF-TOKEN` e cabeçalhos HTMX; logs `[HTMX-TRACE]` confirmam passagem pelos interceptores sem bloqueio. Resposta segue retornando o layout completo (decorado), indicando que a próxima etapa deve focar nos resultados Struts/Sitemesh. **Status:** Concluída, seguir para Iteração 3.

### Iteração 3 – Estrutura Struts/Decorator
- Investigar se há `result-type` custom (ex.: Sitemesh decorator) aplicando layout ao resultado `fragment`. Revisar `struts.xml` e qualquer configuração Sitemesh.
- Avaliar viabilidade de usar `result type="stream"` ou `dispatcher` com `chain` para evitar decorators em chamadas HTMX.
- Checar se `participantes-table.jsp` depende de atributos request/stack não presentes na execução HTMX (ex.: `base` ou `participantes`).
- **2026-02-26:** Ajustados `cabecalho.jspf` e `rodape.jspf` para respeitar `skipTemplate` sem encerrar a renderização, garantindo que respostas HTMX retornem apenas o `<tbody>` ao usar o resultado `fragment`. Build `mvn -Dfrontend.skip=true test` validado; pendente validar manualmente a renderização no navegador e monitorar logs em runtime.

### Iteração 4 – Alternativas de Implementação
- Considerar endpoint JSON (`json-default`) retornando estado dos participantes; no frontend, substituir `hx-post` por fetch + render JS nativo mínimo.
- Estudar uso de `hx-get` com `hx-trigger="changed"` apontando para uma action `GET` que reconstrua o `tbody`, eliminando `POST`.
- Avaliar simplificação radical: tela com `form` completo e submissão tradicional (sem HTMX) + reload parcial via redirect, se aceitável.
- Analisar reimplementação com HTMX `swap:oob` ou componentes `hx-target="closest tr"` para substituir apenas a linha alterada.

### Iteração 5 – Prova de Conceito
- Criar PoC mínima (action e JSP isolados) para validar padrão escolhido (ex.: action `admin/participantesStatusPartial.action` respondendo JSON/fragment sem decorators).
- Validar PoC com testes automatizados (JUnit para action) e manual via navegador.

### Iteração 6 – Implementação e Limpeza
- Selecionar abordagem final com base em experimentos (critério: simplicidade, segurança, manutenção).
- Atualizar código, JSP e testes.
- Revisar documentação (`.ia/diretrizes/frontend.md`, README) e registrar ADR se necessário.

## Ponto de Parada Atual (26/02/2026)
- Iterações 1 e 2 concluídas com instrumentação ativa.
- Iteração 3 concluída com ajuste do template (`skipTemplate`), aguardando validação manual da resposta HTMX.
- Iteração 4 permanece pendente até confirmarmos que o fragmento está estável ou que uma alternativa mais simples é necessária.

## Próximos Passos Imediatos
1. Validar a tela `admin/participantes` via navegador autenticado para confirmar que o `<tbody>` permanece visível após interações HTMX.
2. Revisar logs de aplicação (COOP/COEP/Fetch Metadata) para garantir ausência de regressões após o ajuste de template.
3. Decidir, com base nos resultados, se é necessário seguir para a Iteração 4 (alternativas de implementação) ou se a solução atual pode ser consolidada e documentada.

## Critérios de Aceite
- Requisições HTMX não removem cabeçalho/tabela; swap restringe-se ao trecho esperado.
- Logs sem novos avisos de CSRF/FetchMetadata.
- Código da tela fica mais simples (menos dependências, menor fragmentação JSP) ou com justificativa forte para abordagem atual.
- Planos, tarefas e documentação atualizados com decisões.
