# ADR 2026-02-19 – Substituição do Overlib por Tippy.js

## Contexto

O frontend do Sistema Bolão ainda depende do **Overlib**, biblioteca de tooltips criada em 2002, carregada via `webapp/js/overlib.js` e utilizada em telas administrativas para exibir dicas e informações complementares. Problemas identificados:

- **Obsolescência:** Overlib é abandonado, não suporta navegadores modernos nem padrões de segurança atuais.
- **Incompatibilidade com CSP:** utiliza `document.write`, inline JavaScript e manipulação direta do DOM, impossibilitando uma Content-Security-Policy rígida.
- **Acessibilidade limitada:** ausência de atributos ARIA, gerenciamento de foco e suporte adequado a leitores de tela/teclado.
- **Acoplamento com Prototype/DWR:** reforça dependências de bibliotecas legadas que estamos descontinuando na Fase 2.5.

Na Fase 2.5 Tarefa 2 (Inventário e Análise de Scripts) avaliamos alternativas modernas, e a substituição do Overlib tornou-se um requisito prévio para remover bibliotecas EOL e aderir às diretrizes de segurança.

## Decisão

Adotar **Tippy.js v6** (baseado em Floating UI/Popper) como biblioteca oficial de tooltips.

Principais motivos:

1. **Acessibilidade:** gerenciamento automático de `role="tooltip"`, `aria-describedby`, foco por teclado e fallback sem JavaScript.
2. **Segurança:** compatível com CSP sem `unsafe-inline` ou `eval`; elimina `document.write`.
3. **Compatibilidade:** suporte a browsers evergreen (Chrome, Edge, Firefox, Safari), SSR e módulos ES.
4. **Customização visual:** fácil criação de temas para manter a identidade visual do Bolão.
5. **Integração:** pode ser inicializado após respostas HTMX e não exige framework adicional (Bootstrap, etc.).

## Alternativas Consideradas

1. **Popper.js puro**
   - Prós: máximo controle, sem camada extra.
   - Contras: seria necessário reconstruir toda a lógica de tooltips (ARIA, comportamento, temas), aumentando o esforço.

2. **Bootstrap 5 Tooltip**
   - Prós: implementação pronta, integra Popper.
   - Contras: exigiria trazer todo o framework Bootstrap (CSS e JS), conflitando com o layout atual e adicionando carga desnecessária.

3. **Mantê-lo como está (Overlib)**
   - Rejeitado por motivos de segurança, manutenção e incompatibilidade com browsers modernos/CSP.

## Consequências

### Positivas
- Melhora significativa de acessibilidade (WCAG 2.1) e alinhamento com o objetivo da Fase 2.5.
- Redução do risco de vulnerabilidades e suporte à futura política de CSP.
- Facilita a remoção de Prototype/DWR por eliminar um consumidor relevante dessas bibliotecas.

### Negativas / Impactos
- Necessidade de ajustar HTML/JSP para adotar atributos (`data-tooltip`, `title`) e inicialização Tippy.
- Introdução de nova dependência (Tippy + Floating UI ≈ 13 KB gzip) que precisa ser gerenciada pelo bundler.
- Trabalho de reestilização para reproduzir a aparência atual dos tooltips.

### Próximos Passos
1. Adicionar Tippy.js como dependência (via bundler ESBuild/Vite ou CDN temporária com SRI).
2. Criar módulo `tooltips.js` responsável por inicializar Tippy em elementos com atributo `data-tooltip`, incluindo reexecução após respostas HTMX.
3. Migrar gradualmente as chamadas `overlib(...)` para o novo padrão, removendo `overlib.js` após conclusão.
4. Atualizar o `passo-a-passo.md` e registrar logs das etapas de migração.
5. Validar acessibilidade (navegação por teclado, leitor de tela) e executar testes cross-browser.

## Data da Decisão
19 de fevereiro de 2026

## Responsáveis
Assistente Técnico Líder (Time Mercúrio)
