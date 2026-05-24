# Sessão: Remoção da seção "Ver meus palpites" - 17/04/2026

## Contexto
O usuário solicitou a remoção da seção "Ver meus palpites" na tela de Palpites e Resultados. Esta seção tornou-se redundante após a implementação da edição e visualização direta de palpites em cada linha de jogo.

## Ações Realizadas

### 1. Limpeza de Frontend (JSP)
- Removido o elemento `<details>` e spacers associados em `webapp/WEB-INF/content/seguro/jogos.jsp` que exibiam a lista global de palpites.
- Excluídos os fragmentos JSP obsoletos:
    - `webapp/WEB-INF/content/seguro/partials/meus-palpites-rows.jsp`
    - `webapp/WEB-INF/content/seguro/partials/meus-palpites-rows.jspf`

### 2. Limpeza de Scripts (JS)
- Removidas funções e estados órfãos em `src/frontend/pages/jogos.js`:
    - Função `carregarMeusPalpites()`
    - Função `isMeusPalpitesOpen()`
    - Propriedade de estado `meusPalpitesLoaded`

### 3. Limpeza de Backend (Java & Struts)
- Removido o método `listarMeusPalpitesHtmx()` da classe `ParticipanteAction.java`.
- Removida a definição da action `meusPalpitesPartial` no arquivo `src/main/resources/struts.xml`.

### 4. Saneamento de Internacionalização (i18n)
- Removidas chaves não mais utilizadas em `src/main/resources/messages.properties`:
    - `match.tip.now`
    - `match.tip.date`
    - `match.tip.hour`
    - `match.tip.teams`

## Impacto Técnico
- **Risco:** Mínimo. Remoção de funcionalidade redundante e código morto.
- **Performance:** Leve melhoria no carregamento da página e redução do tamanho do bundle JS.
- **Manutenibilidade:** Código mais limpo e focado na nova experiência de usuário (Direct Inline).

## Conclusão
A interface está agora mais limpa e livre de componentes redundantes, direcionando o usuário para o fluxo de palpites inline que é mais eficiente.

> **Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]**
