# Plano Detalhado - Correção do Gráfico de Desempenho (Timeout, Fluxo e Cache)

## 1. Contexto e Sintoma

- Tela afetada: `webapp/WEB-INF/content/seguro/graficoDesempenho.jsp`.
- Sintoma reportado: status permanece em **"Atualizando gráfico..."** e o gráfico não carrega.
- Fluxo atual identificado:
  - JSP prepara `data-chart-endpoint="/seguro/obterDadosGraficoJson.action"` e mensagens i18n.
  - `src/frontend/pages/graficoDesempenho.js` inicializa a página, faz `fetch`, aplica cache em memória (`Map`) e renderiza com ApexCharts.
  - Backend retorna JSON via `ParticipanteAction.obterDadosGraficoJson()` e `ParticipanteServiceImpl.construirGraficoDesempenho(...)`.

## 2. Causa Raiz Mais Provável

No frontend, em `loadChart()`:

- o estado vai para `loading` antes do `fetch`;
- quando ocorre `AbortError` (incluindo timeout local de 10s), o `catch` retorna sem promover estado de erro;
- o `finally` remove `aria-busy`, mas não altera o texto de status;
- resultado percebido: usuário continua vendo **"Atualizando gráfico..."** sem progresso claro.

Observação: em ambientes com latência variável (HF Spaces + Aiven), timeout de 10s pode acontecer sem falha lógica do backend.

## 3. Avaliação UX Sênior

### Problemas de UX atuais

- Estado ambíguo: "Atualizando gráfico..." pode persistir sem feedback de timeout/rede.
- Ausência de caminho de recuperação explícito no caso de timeout silencioso.
- Incerteza de confiança: usuário não sabe se o sistema travou, se está lento, ou se precisa agir.

### Objetivo de UX

- Tornar estados de carregamento e falha mutuamente exclusivos e sempre conclusivos.
- Diferenciar timeout/cancelamento por navegação rápida de erro real.
- Oferecer ação clara de recuperação (`retry`) com mensagem compreensível.

## 4. Estratégia Técnica Incremental

### Fase A - Correção do bug de estado (mínima e imediata)

1. Ajustar tratamento de `AbortError` no frontend:
   - se for timeout da requisição ativa, mostrar estado de erro transitório e ação de retry;
   - se for cancelamento por troca de rival (requisição obsoleta), não sobrescrever estado da requisição mais recente.
2. Garantir "state closure":
   - toda execução de `loadChart()` deve terminar em `ready`, `warn` (sem dados) ou `error`.
3. Adicionar testes frontend específicos para timeout.

### Fase B - Endurecimento de observabilidade e diagnóstico

1. Incluir telemetria de debug frontend (somente ambiente dev):
   - tempo de ida e volta da requisição;
   - motivo final (ready/cache/empty/error/timeout/aborted-stale).
2. Reforçar logs no backend (`ParticipanteAction`) com correlação básica por login/rival/elapsed.

### Fase C - Cache na tela de gráfico (decisão final)

Objetivo: reduzir latência percebida no fluxo real da tela sem custo adicional no login.

Decisão recomendada:

- manter cache primário no cliente por rival (TTL curto);
- ao acessar/reacessar a tela, validar versão de cache no servidor (`cacheVersionOnly=true`) antes de reutilizar payload local;
- evitar pré-aquecimento no login em ambiente restrito, priorizando aquecimento sob demanda na tela.

### Fase D - Invalidação de cache por atualização de resultados (admin)

Recomendação:

- invalidar cache de gráfico quando admin confirmar atualização de placar válida;
- aplicar invalidação em dois níveis:
  - servidor: versão/etag lógico por participante ou timestamp global de resultados;
  - cliente: bust de chave local ao detectar versão mais nova no payload/header.

## 5. Riscos e Mitigações

- Risco: corrigir abort indiscriminadamente e gerar regressão em trocas rápidas de rival.
  - Mitigação: usar `requestToken` para garantir que apenas requisição ativa altere a UI.
- Risco: reuso de cache local após atualização administrativa exibir dados defasados.
  - Mitigação: versionamento global e handshake de versão antes de usar cache local.
- Risco: invalidação excessiva reduzir benefício de cache.
  - Mitigação: invalidar somente em evento de resultado confirmado (admin), não em leitura.

## 6. Critérios de Aceite

1. Nunca permanecer em "Atualizando gráfico..." por estado terminal incorreto.
2. Timeout deve resultar em mensagem de erro acionável + retry.
3. Troca rápida de rival mantém somente resultado da seleção mais recente.
4. Testes frontend cobrem timeout, abort obsoleto e retry.
5. Estratégia de cache na tela e invalidação por admin documentada e rastreável.

## 7. Escopo Deliberadamente Fora (nesta rodada)

- reescrever arquitetura de dados do gráfico;
- alterar modelo de domínio de pontuação;
- migrar o componente para outro framework gráfico.
