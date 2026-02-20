# Sessao: Avaliacao de Biblioteca de Tooltips (Substituicao Overlib)

**Data:** 2026-02-19  
**Autor:** Assistente Tecnico Lider/IA  
**Skills Utilizadas:** N/A

## 1. Prompt Principal (Input)

> Prossiga, escolhendo alguma opção que mantenha a funcinalidade e leiaute das telas, funcionando em browsers modernos, com segurança e acessibilidade. Pesquise profundamente e avalie com cuidado.

## 2. Resposta da IA (Output Resumido)

> Avaliadas alternativas modernas para substituir o Overlib (Popper.js puro, Tooltip do Bootstrap 5, Tippy.js). O Tippy.js v6 foi selecionado por combinar acessibilidade (ARIA, foco teclado), compatibilidade com browsers atuais, integração simples com HTMX/Struts e dependência mínima (Floating UI). Foram mapeadas recomendações de implementação: inicialização progressiva, fallback CSS, import modular via bundler futuro, remoção de comportamento inline inseguro e alinhamento visual com o layout existente.

## 3. Comparativo de Opcoes

| Biblioteca | Prós | Contras | Observações |
|------------|------|---------|-------------|
| **Popper.js (core)** | Base sólida para posicionamento, mantido pela Floating UI, compatível com browsers modernos. | Exige escrita manual de lógica de tooltip (aria-*), redefinição completa de estilos e eventos. | Maior flexibilidade, mas aumento do esforço; boa para casos customizados extensos. |
| **Bootstrap Tooltip (v5)** | Inclui estilos prontos, integra Popper, acessibilidade básica, documentação ampla. | Requer inclusão do framework Bootstrap (CSS/JS), risco de conflito com layout atual, sobrecarga de ~80 KB. | Adotar apenas tooltip implicaria dependência desnecessária. |
| **Tippy.js v6** | Baseado em Floating UI (Popper), pronto para uso, suporte ARIA, foco teclado, temas configuráveis, bundlers modernos, disponível em ES modules. | Pequeno overhead (~7 KB gzip) + dependência Floating UI (~6 KB). Requer conversão de tooltips existentes. | Permite templates HTML, integra com frameworks, oferece fallback sem JS via `title`. |

## 4. Recomendacao

Selecionar **Tippy.js v6** com Floating UI. Benefícios:

- **Acessibilidade:** Cria automaticamente `role="tooltip"`, gerenciamento de foco/teclado e atributos `aria-describedby`.
- **Seguranca:** Remove necessidade de `document.write` e inline JS; suporta CSP sem `unsafe-eval`.
- **Compatibilidade:** Suporta browsers evergreen (Chrome/Edge 90+, Firefox 88+, Safari 13+), com fallback nativo.
- **Layout:** Permite customizar temas via CSS, replicando cores do Overlib; suporta posicionamento adaptativo similar ao comportamento atual.

## 5. Proximos Passos de Implementacao

1. Incluir Tippy.js (ESM) via bundler futuro ou CDN temporária com Subresource Integrity.
2. Mapear tooltips existentes (`overlib(...)` chamadas) e substituí-las por atributos data (`data-tooltip`) consumidos pelo inicializador Tippy.
3. Criar módulo JS (`tooltips.js`) que inicializa Tippy nos elementos marcados após cada render HTMX.
4. Definir tema CSS customizado compatível com paleta atual (bordas verdes/tons amarelos).
5. Garantir fallback: se JS indisponível, manter atributo `title` nativo.
6. Atualizar testes manuais/automatizados para contemplar navegação por teclado e foco.

## 6. Validacao (Build/Teste)

- Comando: N/A  
- Resultado: N/A  
- Observacoes: Fase de planejamento; sem build executado.

## 7. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Implementar substituicao gradativa juntamente com remoção do Overlib, conforme migração DWR → HTMX e adoção do bundler.
