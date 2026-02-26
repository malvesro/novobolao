# Plano: Correção Dropdown Autorização e Renderização HTMX dos Participantes

**Data:** 2026-02-24  
**Responsável:** Assistente Técnico Líder/IA  
**Contexto:** Tela `admin/participantes.action` apresenta duas anomalias após atualizações recentes:
1. O combo de autorização exibe "NÃ£o" (encoding incorreto) em vez de "Não".
2. Ao selecionar "Não" para um participante, o fragmento HTMX retornado corrompe o layout (logo carregado dentro do portlet), sugerindo que o include completo está sendo inserido sem normalização de fragmento.

## Objetivos
- Garantir consistência de encoding e i18n para as opções do combo `Autorizado`.
- Investigar e corrigir o fluxo HTMX para que a atualização do status não quebre o layout.

## Escopo
- JSPs: `webapp/WEB-INF/content/admin/participantes.jsp`, `webapp/WEB-INF/content/admin/partials/participantes-table.jsp`, `webapp/WEB-INF/content/admin/partials/participantes-rows.jspf`.
- Backend: `AdminAction#atualizarStatusParticipanteHtmx`, `ParticipanteServiceImpl#atualizarAutorizacao` (se necessário).
- Mensagens i18n: `src/main/resources/messages.properties`, `src/messages.properties`.
- Evidências e testes HTMX (interação no browser/Docker).

## Etapas Propostas
1. **Diagnóstico Encoding**
   - Revisar origem do label "Não" (JSP literal vs. `fmt:message`).
   - Verificar `pageEncoding` e content-type dos JSPs envolvidos.
   - Ajustar para reutilizar mensagens i18n (`member.status.enabled/disabled`), garantindo UTF-8.
2. **Análise HTMX Layout**
   - Capturar resposta HTMX (`hx-request`) ao alterar o combo para `false`.
   - Identificar se o backend está retornando página inteira ou fragmento inadequado.
   - Revisar `AdminAction#atualizarStatusParticipanteHtmx` para garantir retorno `NONE` ou JSP fragment.
   - Ajustar JSP/target (`hx-target`, `hx-swap`) para substituir apenas o `<tbody>` esperado.
3. **Correções**
   - Implementar ajustes de encoding/i18n.
   - Garantir que `participantes-table.jsp` retorne apenas `<tbody>` com `participantes-rows.jspf`.
   - Se necessário, criar endpoint dedicado que retorne fragmento via forward/Result Struts.
4. **Testes**
   - `mvn -q -Dfrontend.skip=true test`.
   - Smoke manual (Docker) alterando os combos de autorização para ambos valores, verificando layout.
   - Capturar nova evidência (`telas/`) após correção.
5. **Documentação/Rastreabilidade**
   - Atualizar `passo-a-passo.md` (novo item).
   - Registrar log de sessão com achados e evidências.
   - Avaliar necessidade de atualizar diretrizes caso solução envolva padrão reutilizável.

## Riscos
- Possível dependência de caching HTMX/Struts que reintroduz fragmentos completos.
- Alterações em encoding podem afetar outras telas caso haja inconsistência de charset.

## Critérios de Aceite
- Combo exibe "Sim"/"Não" corretamente em UTF-8.
- Mudança de status via HTMX não quebra layout e mantém menu/logo intactos.
- Logs de aplicação sem erros ao alternar autorização.
- Evidências capturadas e anexadas.
