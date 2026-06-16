# Sessao: Tarefa 57 - Hardening UX + Performance do Grafico de Desempenho

**Data:** 2026-06-16
**Autor:** Codex (Time Mercurio)
**Skills Utilizadas:** ui-ux-pro-max v1.0.0, modern-javascript-patterns v1.0.0, modern-css v1.0.0, security-audit v1.0.0, architecture-guardian v1.0.0

## 1. Prompt Principal (Input)

> Agora inicie a tarefa 57 passo a passo em pequenas iteracoes, uma subtarefa por vez ate finalizar completamente a tarefa.

## 2. Resposta da IA (Output Resumido)

> Hardening completo do grafico de desempenho com foco em robustez operacional, UX/A11y, CSP e performance percebida no contexto HF Spaces + Aiven.
> Principais entregas:
> - Extracao do JavaScript inline da JSP para modulo dedicado (`src/frontend/pages/graficoDesempenho.js`) com ciclo de vida explicito do grafico (instancia unica, update e destroy).
> - Controle de concorrencia com `AbortController` e token de requisicao para garantir "last write wins" quando o rival muda rapidamente.
> - Cache client-side por rival (TTL 45s) para reduzir round-trip ao endpoint JSON.
> - Evolucao de estados de UI (`loading/ready/warn/error`) com `role="status"` e `aria-live="polite"` na tela.
> - Ajustes de estabilidade visual e responsividade no CSS da tela de grafico.
> - Remocao de dependencia CDN de ApexCharts e empacotamento local via Vite com `dynamic import`.
> - Hardening do `CspNonceFilter` (remocao de `System.out.println` e uso de logging estruturado).
> - Otimizacoes leves no endpoint JSON (`Cache-Control` privado curto, `Vary`, fallback defensivo de response em cenarios fora de request web direta).
> - Inclusao/ajuste de testes de regressao para filtro CSP e endpoint JSON do grafico.

## 3. Linha de Base (Antes x Depois)

| Criterio | Antes (baseline tecnico) | Depois (tarefa 57) |
| --- | --- | --- |
| Carregamento ApexCharts | Dependencia externa via CDN (`cdn.jsdelivr`) no carregamento da pagina | Dependencia local via bundle Vite (`import('apexcharts')`) com chunk versionado |
| Script da pagina | Logica JS inline na JSP (alto acoplamento) | Modulo dedicado em `src/frontend/pages/graficoDesempenho.js` + bootstrap em `src/frontend/main.js` |
| Troca rapida de rival | Requisicoes concorrentes sem cancelamento explicito | `AbortController` + `latestRequestToken` (somente ultima selecao atualiza) |
| Reconsulta do mesmo rival | Sempre refetch para endpoint JSON | Cache em memoria por rival com TTL de 45s |
| Acessibilidade estados dinamicos | Feedback visual limitado | `role="status"` + `aria-live` + mensagens i18n de loading/erro/sem dados |
| CSP/observabilidade | Debug com `System.out.println` no filtro | Log estruturado SLF4J em `debug` |
| Endpoint JSON | Sem cache-control privado explicito e sem fallback completo de response | `Cache-Control: private, max-age=30, must-revalidate`, `Vary`, fallback `RequestContextHolder` |

### Evidencias objetivas de build/bundle

- `webapp/assets/js/main-FbyvFox8.js`: **20.455 bytes**
- `webapp/assets/js/apexcharts.esm-CmrCzkCA.js`: **574.119 bytes** (chunk isolado do grafico)
- `webapp/assets/js/app-bundle.js`: **20.469 bytes**

Observacao: o warning do Vite para chunk > 500kB permaneceu esperado para a biblioteca de grafico, agora isolada em chunk proprio (impacto reduzido no caminho inicial de outras telas).

## 4. Validacao (Build/Teste)

- Comando: `npm run build`
- Resultado: Sucesso
- Observacoes: build finalizado com chunk dedicado do ApexCharts e manifest atualizado.

- Comando: `mvn -Dfrontend.skip=true test`
- Resultado: Sucesso
- Observacoes: 71 testes executados, 0 falhas, 0 erros.

## 5. Checklist de Subtarefas 57.x

- [x] 57.1 Diagnostico e baseline registrados (tabela antes/depois + evidencias de bundle/fluxo).
- [x] 57.2 Ciclo de vida do grafico com instancia unica implementado.
- [x] 57.3 Controle de concorrencia com cancelamento de request implementado.
- [x] 57.4 Cache por rival com TTL implementado.
- [x] 57.5 Estados dinamicos acessiveis implementados.
- [x] 57.6 Estabilidade visual/responsividade ajustadas no CSS.
- [x] 57.7 Remocao de inline script/style da JSP concluida.
- [x] 57.8 Remocao de CDN e empacotamento local do ApexCharts concluida.
- [x] 57.9 Hardening do filtro CSP concluido com teste dedicado.
- [x] 57.10 Otimizacoes leves do endpoint JSON concluida sem quebra de contrato.
- [x] 57.11 Validacao de regressao concluida (`npm build` + `mvn test`).
- [x] 57.12 Rastreabilidade concluida (este log + atualizacao do plano).

## 6. Arquivos Impactados

- `webapp/WEB-INF/content/seguro/graficoDesempenho.jsp`
- `src/frontend/pages/graficoDesempenho.js`
- `src/frontend/main.js`
- `webapp/css/estilo.css`
- `src/main/resources/messages.properties`
- `src/messages.properties`
- `src/com/opendev/bolao/action/ParticipanteAction.java`
- `src/com/opendev/bolao/security/CspNonceFilter.java`
- `tests/com/opendev/bolao/action/ParticipanteActionTest.java`
- `tests/com/opendev/bolao/security/CspNonceFilterTest.java`
- `package.json`
- `package-lock.json`
- `webapp/assets/.vite/manifest.json`
- `webapp/assets/js/main-FbyvFox8.js`
- `webapp/assets/js/apexcharts.esm-CmrCzkCA.js`
- `webapp/assets/js/app-bundle.js`

## 7. Analise Humana (Veredito)

- [ ] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** nao foi executado smoke funcional em container nesta iteracao porque o objetivo da subtarefa 57.11 foi coberto por build frontend e regressao Maven completa; o fluxo funcional detalhado ficou rastreado por evidencia de codigo e testes automatizados.
