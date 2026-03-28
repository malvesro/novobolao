# ADR 002: Redesign da Visualização de Palpites do Grupo (Padrão Accordion)

**Data:** 28 de Março de 2026  
**Status:** Aceito

## Contexto

Na Iteração 6, foi implementada a visualização de "Palpites do Grupo" utilizando o padrão `<details>` com um popover absoluto (`.match-group-popover`) posicionado sobre a célula da tabela. 

Embora funcional tecnicamente, essa abordagem apresentava sérios problemas de UX:
1. **Restrição de Espaço**: O popover herda limitações de largura da tabela/célula, resultando em textos espremidos e fontes muito pequenas (13px).
2. **Quebra de Layout**: Em telas menores ou com zoom, o popover era frequentemente cortado (clipping) pelas bordas da janela ou do container da portlet.
3. **Dificuldade de Leitura**: A falta de contraste e o espaço reduzido tornavam a análise comparativa entre participantes do grupo cansativa.

## Decisão

Substituir o popover inline por um padrão de **Expansão de Linha (Accordion Master-Detail)**.

1. **Expansão Full-Width**: Ao acionar o ícone 👥, uma nova linha `<tr>` é renderizada imediatamente abaixo da linha do jogo, utilizando `colspan` para ocupar 100% da largura da tabela.
2. **Modo Accordion Clássico**: A abertura de um detalhe fecha automaticamente qualquer outro detalhe de grupo que esteja aberto, mantendo o foco do usuário e evitando o "pushdown" excessivo de conteúdo.
3. **Desacoplamento de Gatilho**: O controle de visibilidade foi movido para o módulo `jogos.js`, permitindo maior controle programático (fechar via ESC, animações de transição) do que o elemento nativo `<details>`.
4. **Identidade Visual**: A nova linha usa cores de destaque (`#f8fbff`) e bordas laterais para manter o vínculo visual com o "jogo pai".

## Consequências

**Positivas:**
* **Legibilidade Máxima**: Agora há espaço para colunas claras, paddings adequados e fontes padrão do sistema.
* **Consistência Mobile**: O layout se adapta naturalmente a telas verticais, pois utiliza o fluxo natural da tabela.
* **Melhor Controle por Teclado**: Inclusão de suporte à tecla ESC para fechamento rápido.

**Negativas:**
* **Movimentação de Layout**: A expansão "empurra" o restante da lista para baixo, o que pode causar um pequeno salto visual (amenizado pela animação e pelo modo accordion exclusivo).
* **DOM mais pesado**: Adicionamos uma linha extra (inicialmente oculta) para cada jogo na tabela.
