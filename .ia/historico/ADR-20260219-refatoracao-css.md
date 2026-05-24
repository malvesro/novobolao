# ADR 2026-02-19 – Estratégia de Refatoração do CSS

## Contexto

- O stylesheet principal (`webapp/css/estilo.css`) foi criado para um layout fixo (840px) e navegadores legados (IE6/7).
- O código utiliza tabelas para layout, imagens de fundo para efeitos visuais simples, e tokens de cores/spacing hardcoded.
- A Fase 2.5 do plano de modernização exige responsividade, acessibilidade e compatibilidade com navegadores modernos.

## Decisão

Implementar uma refatoração gradual do CSS com os seguintes princípios:

1. **Layout Responsivo:** substituir o layout fixo por `max-width` + `padding`, aplicar flex/grid nas seções principais e garantir breakpoints móveis.
2. **Tokens de Design:** introduzir variáveis (CSS custom properties ou preprocessor) para cores, tipografia, espaçamentos e sombras.
3. **Componentização:** dividir o stylesheet em módulos (`layout`, `components`, `utilities`) ou adotar metodologia BEM para facilitar manutenção.
4. **Redução de Imagens:** eliminar imagens de fundo ornamentais, utilizando gradientes, bordas e sombras CSS modernas.
5. **Formulários e Portlets:** reescrever portlets e formulários com flex/grid, garantindo alinhamento sem tabelas.
6. **Automação:** integrar bundler (Vite/ESBuild) + PostCSS/autoprefixer para gerar bundles minificados com hashes, permitindo CSP rígida.

## Alternativas Consideradas

- **Manter CSS atual com ajustes pontuais:** rejeitado por não resolver problemas estruturais de responsividade e manutenção.
- **Migrar diretamente para framework CSS completo (ex.: Bootstrap/Tailwind):** opção válida, mas adotaremos abordagem própria primeiro para preservar identidade visual, podendo incorporar framework no futuro se necessário.

## Consequências

### Positivas
- Responsividade real em múltiplas resoluções.
- Redução de débito técnico, eliminação de hacks legados e melhor performance.
- Base consistente para evoluir o frontend (Fase 3).

### Negativas / Riscos
- Refatoração extensa exige testes regressivos cuidadosos.
- Possível necessidade de ajustar assets e portlets existentes conforme novos estilos.

## Plano de Implementação

1. Criar camada de tokens (cores, fontes, spacing) e reorganizar o CSS em módulos.
2. Refatorar `login.jsp` como piloto responsivo; repetir para `cadastro.jsp` e páginas `seguro/*`.
3. Substituir tabelas de layout por flex/grid; manter tabelas apenas para dados tabulares.
4. Remover dependências de imagens de fundo e atualizar portlets com estilos modernos.
5. Configurar bundler (Vite/ESBuild) + PostCSS para build automatizado.
6. Atualizar documentação (`passo-a-passo.md`, logs) e executar testes cross-browser/responsivos em cada etapa.

## Data
19 de fevereiro de 2026

## Responsável
Assistente Técnico Líder (Time Mercúrio)
