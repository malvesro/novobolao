# Relatório de Erro: CSP Violation e Correção de Fontes
**Data:** 14/06/2026
**Agente:** Arquiteto Sênior (Time Mercúrio)

## 1. Descrição do Problema
Após a implementação da renderização de gráficos via ApexCharts, a aplicação exibia erros de bloqueio no console do navegador devido a violações da Content Security Policy (CSP). Especificamente, as fontes do Google Fonts estavam sendo bloqueadas e os novos scripts de gráfico não estavam integrando corretamente com o mecanismo de nonce da aplicação.

## 2. Diagnóstico
- As diretivas `style-src` e `font-src` não permitiam as origens do Google Fonts (`fonts.googleapis.com`, `fonts.gstatic.com`).
- A CSP estrita exigia a inclusão do atributo `nonce` em novos scripts, o que não foi aplicado inicialmente para o script de carregamento do ApexCharts e para os scripts inline adicionados.

## 3. Solução Implementada
- **Ajuste na `CspNonceFilter.java`:** Atualização da `buildPolicy` para incluir explicitamente as fontes e estilos do Google Fonts nas diretivas correspondentes.
- **Ajuste nas JSPs:** Adição do atributo `nonce="${cspNonce}"` às tags `<script>` do ApexCharts e scripts inline em `graficoDesempenho.jsp`.

## 4. Conclusão
O problema foi resolvido. A aplicação agora carrega os recursos externos permitidos e respeita a política de segurança estrita sem comprometer a funcionalidade da interface moderna.

---
Status: Resolvido
