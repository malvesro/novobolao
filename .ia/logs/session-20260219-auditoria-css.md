# Sessao: Auditoria do CSS legado (Fase 2.5 - Tarefa 4)

**Data:** 2026-02-19  
**Autor:** Assistente Tecnico Lider/IA  
**Skills Utilizadas:** N/A

## 1. Prompt Principal (Input)

> prossiga

## 2. Resumo da Auditoria

Arquivo analisado: `webapp/css/estilo.css` (~520 linhas).

Principais achados:

1. **Layout fixo (840px)** – `#wrapper` e `#footer` travam largura e usam tabelas para grid. Impacto: falta de responsividade em viewports menores.
2. **Dependência intensa de imagens de fundo** (`fundo_*.png`, `fundo_prata.png`, `bolao_logo.png`) para elementos simples que poderiam ser representados com CSS moderno (gradientes, bordas).
3. **Uso recorrente de tabelas para layout** (classes `table.form`, tabelas em portlets) em vez de flexbox/grid.
4. **Tipografia e tokens hardcoded** – fontes, cores e espaçamentos repetidos sem variáveis ou classes utilitárias.
5. **Classes genéricas sem organização** – mistura de IDs e classes globais, ausência de escopos/componentização.
6. **Falta de modo dark/tema** – tudo codificado com cores fixas.

## 3. Recomendações

1. **Estrutura Responsiva**
   - Converter `#wrapper` para `max-width` + `padding`.
   - Introduzir layout flex/grid nas seções principais (header, content, footer).
2. **Tokens de Design**
   - Criar CSS custom properties (`--color-primary`, `--spacing-sm`, etc.) ou SCSS equivalente.
3. **Portlets e Formulários**
   - Reescrever `.portlet` com layout flexível, remover imagens de fundo, usar sombras/bordas CSS.
   - Substituir `table.form` por `form` com `display: grid`/`flex`.
4. **Tabelas de Dados**
   - Aplicar classes reutilizáveis para colunas, zebra striping com `nth-child`.
5. **Refatoração por Componentes**
   - Separar o CSS em módulos (`layout.css`, `components.css`, `utilities.css`) ou adotar BEM.
6. **Automação**
   - Integrar bundler (Vite/ESBuild) + PostCSS/autoprefixer para minificação e compatibilidade.

## 4. Próximos Passos Propostos

1. Criar `src/main/resources/static/css/` (ou equivalente) para abrigar novo stylesheet modular.
2. Definir guia de temas (cores, tipografia, spacing) e migrar gradualmente.
3. Refatorar página `login.jsp` como piloto responsivo; replicar padrão para `cadastro.jsp`, `principal.jsp`, etc.
4. Aplicar testes manuais em resoluções 320px–1920px e registrar evidências.
5. Atualizar `passo-a-passo.md` conforme cada etapa for concluída.

## 5. Validacao (Build/Teste)

- Comando: N/A  
- Resultado: N/A  
- Observacoes: Auditoria documental; nenhuma alteração aplicada nesta sessão.

## 6. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Recomenda-se iniciar a refatoração pelo `login.jsp` para validar tokens e layout responsivo antes de propagar para as demais telas.
