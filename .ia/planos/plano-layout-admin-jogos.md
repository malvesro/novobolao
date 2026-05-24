# Plano: Correção do Layout em `/admin/jogos.action`

**Data:** 2026-02-22  
**Responsável:** Assistente Técnico Líder  
**Contexto:** Evidência `telas/Erro-desing-tela.png` mostra múltiplas linhas vazias após a tabela de jogos do dia 27/06/2026 e antes do rodapé. O HTML e CSS atuais (analisados em 22/02/2026) indicam que a tela administrativa herda o portlet de jogos da visão autenticada (`seguro/jogos.jsp`), potencialmente renderizando placeholders quando não há linhas adicionais. Este plano detalha o diagnóstico e a implementação para eliminar o espaçamento indevido, mantendo acessibilidade e compatibilidade com Struts/HTMX.

## Objetivos
1. Identificar a origem das linhas vazias e removê-las sem afetar a tabela principal ou outras telas que reutilizam o componente.
2. Garantir que o layout do portlet permaneça alinhado às diretrizes de frontend (`.ia/diretrizes/frontend.md`) e acessível.

## Etapas Planejadas

### Etapa 1 – Diagnóstico do HTML/CSS
- Mapear o JSP respons responsável por `/admin/jogos.action` (Action `adminAction.carregarJogos`) e confirmar reaproveitamento de `seguro/jogos.jsp`.
- Inspecionar loops `<c:forEach>` e resets de `<tbody>` no trecho `webapp/WEB-INF/content/seguro/jogos.jsp` (`rows` 264–418), verificando se há `<table>` ou `<tbody>` abertos sem fechamento adequado que gerem linhas vazias.
- Revisar CSS relacionado:
  - `.collapsible-portlet`, `.table.conteudo` (linhas 959+ e 368+ de `webapp/css/estilo.css`), classes `.spacer`.
  - Identificar se há `border-spacing`, `padding`, `background` ou `spacer` com altura maior que a pretendida.
- Registrar achados em log dedicado (`.ia/logs/`).

### Etapa 2 – Ajustes de Layout
- Se o problema for markup:
  - Ajustar a estrutura para fechar a tabela antes de renderizar portlets vazios.
  - Evitar `<tr>` placeholders sem conteúdo.
  - Remover duplicação de `dataJogoFormatada` e garantir que novos portlets sejam criados apenas quando existirem jogos para a data.
- Se o problema for CSS:
  - Atualizar `.table.conteudo`, `.collapsible-portlet__content`, `.spacer` ou classes correlatas para evitar alturas redundantes.
  - Inserir comentários descrevendo ajustes não triviais (conforme diretriz).
- Validar que o layout continua consistente em resoluções desktop e mobile (responsividade básica).

### Etapa 3 – Testes e Evidências
- Rodar `npm run build` caso haja alteração de assets JS/CSS (esperado se os estilos mudarem).
- Executar `mvn -q -Dfrontend.skip=true test`.
- Rebuild Docker (`docker compose build app`) e iniciar container (`docker compose up -d app`).
- Capturar nova evidência visual (tela `telas/`) mostrando a seção sem linhas vazias.
- Registrar sessão de log com diagnóstico, alterações e validações.

### Etapa 4 – Documentação e Atualizações
- Atualizar `passo-a-passo.md` (Tarefa 13) refletindo o progresso e conclusão.
- Incluir observações em `.ia/diretrizes/frontend.md` caso novas regras de layout sejam estabelecidas.
- Avaliar necessidade de ADR se a mudança representar comportamento estrutural relevante.

## Riscos e Considerações
- Ajustes no JSP podem impactar outras views que reutilizam o componente; revisar Actions relacionadas (`seguro/jogos.action`, `/admin/jogos.action`).
- Garantir que as modificações não reintroduzam problemas de acessibilidade (ex.: manter `scope`/`aria` corretos, preservar interações HTMX).
- Observar dependências com tarefas futuras (ex.: auditorias de layout) e coordenar caso mais adequações sejam necessárias.

## Critérios de Conclusão
- Ao acessar `/admin/jogos.action`, a tela exibe apenas as linhas referentes aos jogos reais sem placeholders vazios, mantendo portlets e rodapé alinhados.
- Testes Maven e rebuild Docker finalizados sem falhas; evidência atualizada em `telas/`.
- Logs e documentação refletem as decisões e validações concluídas.
