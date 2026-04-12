# ADR-005: Edição Administrativa Integrada na Tela de Resultados

**Data:** 2026-04-12
**Status:** Aceito
**Contexto:**
Historicamente, no Bolão legado, a criação de jogos era feita em uma tela separada (`inclusaoJogo.jsp`), mas não havia uma forma simples de editar a estrutura (times, local, data) de jogos já criados sem recorrer ao banco de dados ou a fluxos complexos. Com a modernização para HTMX, surgiu a necessidade de permitir essa edição para administradores.

**Decisão:**
Em vez de criar uma nova página de gerenciamento (`admin/gerenciamentoJogos.jsp`), decidimos reaproveitar a tela de "Atualizar Resultados" (`jogos.action` com perfil ADMIN) para incluir a edição estrutural inline.

**Justificativa:**
1. **Contexto Preservado:** O administrador já utiliza essa tela para inserir placares após os jogos. Frequentemente, a necessidade de trocar um time (ex: fase de mata-mata) ou ajustar um local ocorre no mesmo fluxo mental de atualizar o status do jogo.
2. **Coerência com HTMX:** A abordagem de substituição de linha (`hx-target="closest tr"`) é extremamente eficiente e reduz o tempo de carregamento comparado a uma navegação completa para outra página.
3. **Menos Código Duplicado:** Evita a criação de uma segunda listagem de jogos quase idêntica à de resultados.

**Consequências:**
- **Lado Positivo:** UX superior, maior agilidade administrativa, código frontend centralizado em fragmentos (`admin-match-row.jsp`).
- **Lado Negativo:** Aumenta ligeiramente a complexidade da Action administrativa, que agora gerencia múltiplos estados de visualização/edição HTMX.

**Tecnologias Envolvidas:**
- HTMX 1.9.10
- Struts 7 partial results
- Spring Security 6 integration
