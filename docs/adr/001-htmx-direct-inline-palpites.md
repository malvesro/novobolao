# ADR 001: Adoção do Padrão "Direct Inline" via HTMX em Substituição a Modais Struts

**Data:** 27 de Março de 2026  
**Status:** Aceito

## Contexto

O fluxo antigo de apostas no sistema do `novobolao` operava quase restritamente em renderização baseada em chamadas pesadas por jQuery, Modais Customizados no DOM, JavaScripts acionadores com estados verbosos no frontend, gerenciamento de z-indexes com backdrops e ações complexas de recarregamento para feedback simples. 

Isso engessava a interface (limitando seu uso responsivo em dispositivos mobile pelo espaço exigido pelas modais) e degradava fortemente a velocidade da digitação, forçando múltiplas interações visuais.

## Decisão

Adotar e padronizar o fluxo "Zero Clique" (Direct Inline) da tabela de jogos aproveitando HTMX (Hypertext Markup Language Extension) em conjunto nativo com a tag `<details>` do HTML5, sem dependências de frameworks JavaScript de reatividade complexos (como React/Vue) em cima da fundação JSP original do Spring Boot 6.

1. **Auto-Save Fluído:** Todo palpite agora reage ao evento JS genérico de "input" com debounce (800ms) acionando requisições background com `htmx.trigger`.
2. **Respostas em Ilhas (Partials):** Usar Action classes (`ParticipanteAction.java`) para retornar unicamente micro-JSPFs de Feedback, substituindo tags em vez de carregar contextos pesados.
3. **PopOvers Nativos em Detalhes:** "Meus Palpites" e "Ver Grupo" carregam HTMX interceptado com `toggle once` encapsulados em tags de controle visual padronizadas via manipulação pura de CSS Absoluto (`.match-group-popover`).
4. **Resolução de Infra DEV:** Definida exclusividade semântica da action `/` via mapeamento vazio e `default-action-ref` garantindo suporte de index nativo em servidores Tomcat 10+ sem gerar falsos 404. Definido mapeamento de docker-local para o range `808x`.

## Consequências

**Positivas:**
* Redução imensa no consumo de banda na listagem de palpites (Páginas muito mais leves).
* Enxugamento massivo no tamanho e complexidade do módulo Javascript cliente (`jogos.js`).
* Experiência visual mobile muito otimizada e livre de travamentos em redimensionamento modal.

**Negativas:**
* Exigirá familiaridade dos desenvolvedores no paradigma HTMX-First e troca por `Swap OutHTml` ou `innerHTML` em vez das antigas manipulações completas de DOM e JSON.
* Fragmentação sutil em arquivos JSPF auxiliares requer atenção contra taglibs globais vazando.
