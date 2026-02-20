# Sessao: Inventario e Analise de Scripts (Fase 2.5 - Tarefa 2)

**Data:** 2026-02-19
**Autor:** Assistente Tecnico Lider/IA
**Skills Utilizadas:** N/A

## 1. Prompt Principal (Input)

> Verifique a necessidade de modernizar os scripts para browsers mais atuais e maior segurnça, pesquise oppões justificadas. Considere também outras opções como tags struts, tags html, etc...

## 2. Resposta da IA (Output Resumido)

> Mapeado todo o inventário de scripts do `webapp/js/` e os pontos de inclusão nas JSPs. Classifiquei cada dependência por uso atual e risco (Prototype/Scriptaculous, DWR Engine, Overlib, BrowserDetector, jQuery 4.0.0, HTMX). Avaliei o impacto sobre templates Struts e tags HTML, propondo estratégias de modernização (migração gradual para HTMX/fetch, substituição de tooltips e efeitos, remoção de sniffers legados, adoção de tags HTML5/Struts modernas). Registradas recomendações priorizadas para eliminar bibliotecas EOL e alinhar com navegadores atuais e requisitos de segurança.

## 5. Inventário de Scripts e Dependências

| Arquivo/Lib                          | Localização                     | Uso Atual Identificado                                                                 | Status/Risco                                                                                  | Observações Complementares                                                                                                                        |
|--------------------------------------|---------------------------------|-----------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `prototype.js`, `scriptaculous.js`, `effects.js` | `webapp/js/` (incluídos via `cabecalho.jspf:28-34`) | Base para Ajax legado (DWR) e animações (`webapp/seguro/jogos.jsp:102-249`, `admin/*.jsp`) | **EOL**, vulnerável a Prototype Pollution e incompatível com CSP rígida                      | Substituíveis por HTMX/Fetch + CSS/Vanilla JS. Remover reduz 150 KB por página e elimina conflitos com jQuery/HTMX.                                |
| `engine.js`, `util.js` (DWR 2.0)     | `webapp/js/` + interfaces `dwr/interface/*.js`     | RPC síncrona com `AdminAction`, `ParticipanteAction` (`jogos.jsp`, `admin/participantes.jsp`) | **Alto risco**: modelo RPC expõe superfícies internas; dependência de Prototype               | Migrar para endpoints REST Struts + HTMX. Após migração, remover `dwr.xml` e libs.                                                                |
| `overlib.js`                         | `webapp/js/`                    | Tooltips em telas admin (efeitos via `overlib(...)`)                                     | **EOL** e não acessível; usa `document.write`, quebra CSP                                      | Alternativas modernas (Tippy.js + Popper) ou `<dialog>`/ARIA.                                                                                     |
| `BrowserDetector.js`                 | `webapp/js/`                    | Carregado por `cabecalho.jspf`; sem referência direta                                   | **Obsoleto** (user-agent sniffing)                                                            | Sugerir remoção após validar ausência de uso; usar feature detection (`@supports`, Modernizr slim) se necessário.                                 |
| `jquery-4.0.0.min.js` (pre-release)  | `webapp/js/`                    | Carregado globalmente; usado no login (`$j=noConflict`) e pequenas interações            | **Versão alfa** sem suporte oficial                                                           | Rebaixar para 3.7.1 estável ou migrar pequenos trechos para HTMX/Vanilla.                                                                          |
| `htmx.min.js` (1.9.10)               | `webapp/js/`                    | PoC concluída em `admin/participantes.jsp` (`hx-post`, `hx-swap`)                        | **Atual**                                                                                    | Recomendar expansão para substituir DWR e formulários Prototype.                                                                                   |
| Tags Struts (`<s:form>`, `<s:textfield>`) | Diversas JSPs                    | Utilizadas para manter binding com OGNL                                                  | **OK**, porém avaliado para migração gradual a HTML5 puro                                     | Onde não houver lógica OGNL, trocar por `<form>` HTML5 para simplificar e habilitar validações nativas.                                           |
| Scripts inline (DWR, validações)     | `jogos.jsp`, `admin/*.jsp`, `cadastro.jsp`         | Manipulam DOM com Prototype + `DWRUtil`                                                  | **Risco médio** (dificultam CSP, segurança)                                                   | Migrar lógica para módulos JS dedicados, usar `type="module"` após novo bundler.                                                                  |

## 6. Avaliação e Modernização Proposta

1. **Substituir Prototype/Scriptaculous** por HTMX/Fetch + CSS transitions. Priorizar telas com RPC (`jogos.jsp`, `participantes.jsp`).  
2. **Planejar desligamento do DWR**: criar endpoints Struts RESTful, reutilizar services existentes; consumir via HTMX (`hx-post`, `hx-trigger`).  
3. **Migrar tooltips Overlib** para biblioteca moderna (Tippy.js) ou componentes Struts/HTML com ARIA (`aria-describedby`).  
4. **Remover BrowserDetector** após validar que não há condicionais dependentes; adotar feature detection (`CSS @supports`, `navigator.userAgentData`).  
5. **Reavaliar jQuery**: trocar por 3.7.1 estável enquanto houver dependências ou eliminar quando HTMX assumir fluxos.  
6. **Implantar bundler (Vite/ESBuild)** para modularizar scripts, gerar bundles minificados/assinados com hash e permitir CSP sem `unsafe-inline`.  
7. **Alinhar templates Struts/HTML**: onde possível, substituir `<s:form>` por `<form>` HTML5 e aproveitar validação nativa; manter tags Struts apenas para bindings OGNL indispensáveis.  
8. **Segurança**: com legado removido, aplicar Content-Security-Policy restritiva, desabilitar eval global e reforçar Subresource Integrity para libs CDN (caso mantidas).  
9. **Testes**: introduzir ESLint/Prettier e testes E2E (Playwright) após migração para garantir compatibilidade em Chrome, Firefox, Edge.

## 3. Validacao (Build/Teste)

- Comando: N/A
- Resultado: N/A
- Observacoes: Atividade de análise documental; nenhum build executado.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Ajustes propostos demandam tarefas futuras (migração DWR → HTMX/REST, substituição de Prototype/Scriptaculous, remoção de BrowserDetector). Nenhuma alteração aplicada nesta sessão.
