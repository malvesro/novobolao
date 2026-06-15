# Relatório de Erro: CSP Violation no Gráfico de Desempenho
**Data:** 14/06/2026
**Agente:** Arquiteto Sênior (Time Mercúrio)

## 1. Descrição do Problema
Após a modernização da tela de Gráfico de Desempenho para Client-side Rendering (ApexCharts), a aplicação apresentava falhas silenciosas na renderização. A investigação via Console do Navegador revelou violações de **Content Security Policy (CSP)**.

## 2. Diagnóstico
O navegador bloqueava o carregamento do ApexCharts (`cdn.jsdelivr.net`) e a execução de scripts inline (lógica de `loadChart`) pois estes não continham o atributo `nonce` (Number used once) exigido pela política de segurança da aplicação (`script-src 'nonce-...'`).

## 3. Solução Implementada
Ajustamos a JSP (`graficoDesempenho.jsp`) para incluir o nonce gerado pelo servidor em todos os blocos de script críticos:
- Adição de `nonce="${cspNonce}"` na tag `<script>` de carregamento da biblioteca.
- Adição de `nonce="${cspNonce}"` na tag `<script>` contendo a lógica de negócio (`loadChart`).

## 4. Conclusão
O problema foi resolvido garantindo conformidade com a CSP estrita do projeto. Esta experiência reforça a necessidade de aplicar tokens de segurança em qualquer novo script injetado na aplicação durante as fases de modernização.

---
Status: Resolvido
