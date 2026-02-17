# Plano de Modernização do Front-end (Fase 2 - Item 7)

Este plano descreve a transição das bibliotecas legadas (Prototype, Scriptaculous) utilizando uma abordagem de **Arquitetura Híbrida**: **jQuery 4.0.0** para estabilidade e **HTMX 1.9.10** para modernização do motor AJAX.

## Pilares da Estratégia
1.  **jQuery 4.0.0 (Global):** Fornece uma base moderna e segura para substituir scripts imperativos do Prototype/Scriptaculous sem reescritas massivas.
2.  **HTMX 1.9.10 (AJAX):** Define o novo padrão para interações assíncronas, permitindo que o Struts 6 retorne fragmentos de HTML/JSP em vez de JSON, alinhando-se à arquitetura original do projeto.
3.  **Coexistência:** Uso rigoroso de `jQuery.noConflict()` ($j) para garantir que o sistema continue operacional durante a migração gradual.

## Mudanças Propostas

### Infraestrutura
- [x] Download de `jquery-4.0.0.min.js` e `htmx.min.js`.
- [x] Integração no `cabecalho.jspf`.

### Migração de Componentes
- [x] **login.jsp:** Substituir efeitos de Fade/Pulsate do Scriptaculous por equivalentes jQuery.
- [x] **menu.jspf:** Migrar lógica de menu lateral para jQuery.
- [x] **Funcionalidade AJAX PoC:** Implementar HTMX em uma tela de consulta para exemplificar a redução de código JS.

## Plano de Verificação

### Testes Manuais
- [ ] Validar carregamento correto dos scripts no console do navegador.
- [ ] Testar animações no Login.
- [ ] Testar funcionalidade do menu principal.
- [ ] Validar que chamadas DWR (legadas) continuam funcionando sem interferência.
