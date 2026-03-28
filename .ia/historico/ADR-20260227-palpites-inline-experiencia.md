# ADR-20260227-palpites-inline-experiencia

**Data:** 2026-02-27
**Status:** Rascunho

## Contexto

- Após a migração para Spring Security 6 e Struts 7, o fluxo de palpites na página `seguro/jogos.jsp` manteve o padrão de balão flutuante (`position: absolute/fixed`) herdado do legado Prototype/DWR.
- Mesmo com correções recentes (CSP, bundler Vite, modais temporários), a experiência continua apresentando problemas: falta de abertura consistente, dependência de coordenadas calculadas manualmente, baixa acessibilidade (foco/teclado) e comportamento frágil em resoluções mobile.
- As diretrizes UX/Arquitetura (2026-02-27) recomendam remover o balão e centralizar a interação na tabela, com botões explícitos, estados visuais claros e, quando necessário, uso do `<dialog>` nativo.

## Decisao

Adotar uma experiência inline/painel para gestão de palpites:

1. **Expansão Inline como padrão**  
   - Ao acionar “Editar palpite” na linha, um `<tr>` adicional (colspan total) será inserido via HTMX contendo o formulário do palpite.  
   - O estado atual (registrado/pendente) será exibido na célula principal com badge/ícone acessível.

2. **Painel lateral opcional para histórico**  
   - Um painel dentro de `#jogos-page-wrapper` (elemento `<aside>` com `<dialog>` como fallback) exibirá “Palpites do grupo”.  
   - O painel é carregado via `hx-target="#palpite-panel"` mantendo hierarquia e foco controlado.

3. **Scripts modulares com CSP rígida**  
   - `src/frontend/pages/jogos.js` será refatorado para cuidar apenas de highlight, gestão de expansão e interações HTMX.  
   - Scripts inline restantes serão migrados para `src/frontend/modules/` e importados com `<script type="module" nonce="${cspNonce}" src="...">`.

4. **Mobile-first e acessibilidade**  
   - Utilizar utilitários existentes (`.tips-panel`, `.dashboard-section`) e breakpoints para garantir usabilidade em telas pequenas.  
   - Garantir suporte completo a teclado e leitores de tela (foco gerenciado, `aria-live`, labels).

5. **Documentação e rastreabilidade**  
   - Atualizar diretrizes frontend, plano de modernização e logs com o novo padrão; evidenciar fluxo em README/estória.

## Alternativas Consideradas

1. **Manter o balão flutuante atual** – descartado pela baixa acessibilidade, dependência de coordenadas e histórico de bugs.
2. **Migrar para modal central (`<dialog>`) como padrão** – aceitável para casos específicos, mas não resolve o foco na tabela nem reduz o atrito em mobile; fica reservado apenas para histórico/painel quando inline não atender.

## Consequencias

- **Positivas**
  - Melhora significativa de acessibilidade (teclado, leitores) e experiência mobile.
  - Redução de scripts customizados (sem cálculo de posição) e aderência à CSP rígida.
  - Fluxo consistente com diretrizes HTMX já adotadas no projeto.
- **Negativas / Custos**
  - Necessidade de refatorar JSPs/JS existentes e atualizar testes manuais.  
  - Demanda de protótipos UX/QA adicionais antes de liberar em produção.

## Responsaveis

- Time Mercúrio – Arquitetura Frontend & UX  
- Apoio: Engenharia Backend (ajustes em Actions HTMX)  
- Validação: Product Owner / Stakeholders de negócio
