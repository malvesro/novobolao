# ADR 004: Split Inputs e Container-level (Multi-Tbody) HTMX Updates

## Status
Aceito (Implementado)

## Data
2026-03-30 (Revisado)

## Contexto
A interface de palpites utilizava uma coluna centralizada e unificada para os inputs de placar. Análises de UX indicaram que essa disposição exigia maior carga cognitiva. Mover os inputs para colunas diferentes da mesma linha (`<tr>`) desafiou o modelo de atualização parcial por célula (`<td>`) do ADR 001. Tentativas iniciais de "Row-level update" (`hx-target="closest tr"`) mostraram instabilidade quando o fragmento continha múltiplas linhas (ex.: linha de jogo + linha de detalhes do grupo), causando duplicação de IDs e "desmoronamento" do layout.

## Decisão
1. **Layout Split Inputs**: Posicionar o input de gols do Time Casa imediatamente à direita do seu nome/bandeira, e o input do Time Visitante imediatamente à esquerda do seu nome/bandeira.
2. **HTMX Container-level Target (Multi-Tbody)**: Em vez de mirar na linha (`<tr>`), agrupamos cada conjunto de partida (linha de dados + linha de detalhes) em um elemento `<tbody>`. O alvo do HTMX (`hx-target`) passa a ser o `closest tbody`.
3. **Swap via `innerHTML`**: O swap é realizado via `innerHTML` no `<tbody>`, substituindo atomicamente todo o bloco da partida. Isso garante integridade total do DOM mesmo após múltiplos salvamentos.
4. **Reuso de Fragmento (`match-row.jspf`)**: O fragmento contém o par de `<tr>` e é incluído tanto na carga inicial quanto na resposta parcial, garantindo conformidade visual (DRY).
5. **Sincronização de Dados**: Utilizar `hx-include="closest tbody"` (ou `closest tr`) para garantir que ambos os placares sejam enviados no POST de salvamento.

## Consequências
- **Positivas**: 
    - Estabilidade absoluta na renderização de tabelas complexas com HTMX.
    - Isolamento de cada partida em seu próprio fragmento de renderização.
    - Facilidade de manutenção de layouts do tipo "Expandable Row".
- **Negativas**: 
    - Estrutura da tabela levemente mais verbosa (uso de múltiplos `<tbody>`), porém 100% válida em HTML5.

## Referências
- `AGENTS.md`: Diretriz de Rastreabilidade e ADRs.
- `passo-a-passo.md`: Fase 7 (Modernização UX).
