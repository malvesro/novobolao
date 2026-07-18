# Dashboard Animado de Evolução de Pontuação — Bolão

**Data:** 2026-07-17  
**Status:** Proposto (análise arquitetural concluída)  
**Escopo:** nova tela `/seguro/dashboardCorrida.action` + endpoint JSON de dados  
**Referências:** `graficoDesempenho.jsp`, `graficoDesempenho.js`, ADR-003, ADR cache gráfico (tarefa 60)

---

## 1. Descrição

Dashboard que apresenta a corrida de pontuação de todos os participantes jogo a jogo, do primeiro ao último, com animação de **bar race** e, ao final, tela de **pódio animada** destacando os 3 primeiros colocados. A experiência inicia em **play automático** e fica acessível como nova tela dedicada no sistema.

---

## 2. Estado atual no repositório

| Item | Status |
|------|--------|
| `ParticipanteServiceImpl.construirDadosDashboardCorrida()` | **Parcial** — implementado sem testes |
| `ParticipanteAction` + endpoint JSON | Pendente |
| `struts.xml` (action JSON + action JSP) | Pendente |
| `dashboardCorrida.jsp` / `dashboardCorrida.js` | Pendente |
| CSS em `webapp/css/estilo.css` | Pendente |
| Link no `menu.jspf` | Pendente |
| i18n (`messages.properties`) | Pendente |
| Tarefa no `passo-a-passo.md` | Pendente |

---

## 3. Proposed Changes

### Componente 1 — Backend: endpoint de dados do dashboard

O gráfico de desempenho atual (`obterDadosGraficoJson`) retorna dados apenas do participante logado + 1 rival. O dashboard de corrida precisa de **todos os participantes elegíveis** e granularidade **por jogo finalizado** (não por dia).

#### [MODIFY] `ParticipanteServiceImpl.java`

Método público `construirDadosDashboardCorrida()`:

- Busca participantes **não administradores** (ADR-003).
- Busca jogos finalizados em ordem cronológica.
- Constrói frames da corrida (um snapshot de ranking por jogo).
- Cache em memória alinhado a `GraficoDesempenhoCacheControl` (invalidação por evento admin, não por timer solto).
- Ordenação **oficial** reutilizando critério de `Participante` (`Comparable`), não apenas pontos brutos.

**Formato JSON retornado:**

```json
{
  "cacheVersion": 42,
  "totalJogos": 64,
  "totalParticipantes": 28,
  "jogos": [
    {
      "label": "BRA x ARG (11/06)",
      "ranking": [
        { "id": 12, "nome": "João", "pontos": 6, "posicao": 1 },
        { "id": 7, "nome": "Maria", "pontos": 3, "posicao": 2 }
      ]
    }
  ],
  "podio": [
    { "posicao": 1, "id": 7, "nome": "Maria", "pontos": 120 },
    { "posicao": 2, "id": 12, "nome": "João", "pontos": 110 },
    { "posicao": 3, "id": 3, "nome": "Pedro", "pontos": 105 }
  ]
}
```

> **Nota de implementação:** incluir `id` e `posicao` estabiliza animação (ApexCharts) e evita ambiguidade por nomes repetidos.

#### [MODIFY] `ParticipanteAction.java`

| Action | URL | Função |
|--------|-----|--------|
| `dashboardCorrida` | `/seguro/dashboardCorrida.action` | Renderiza JSP |
| `obterDadosDashboardCorridaJson` | `/seguro/obterDadosDashboardCorridaJson.action` | Serializa JSON |

**Headers (espelhar tarefa 60):**

- `Cache-Control: private, max-age=30, must-revalidate`
- `X-Grafico-Cache-Version: <versao>`
- Suporte a `cacheVersionOnly=true` (handshake leve, sem recomputar frames)

#### [MODIFY] `struts.xml`

- Registrar action JSON no package `bolao-json` (`result type="json"`, `root=graficoData` ou propriedade dedicada `dashboardCorridaData`).
- Registrar action JSP no package seguro existente.

---

### Componente 2 — Frontend: JavaScript do dashboard

#### [NEW] `src/frontend/pages/dashboardCorrida.js`

Implementar animação em ESM + ApexCharts (já bundlado):

1. **Fetch** de `/seguro/obterDadosDashboardCorridaJson.action` com timeout e estados terminais (`ready` / `warn` / `error`), seguindo `graficoDesempenho.js`.
2. **Bar race loop:** para cada frame, atualizar gráfico horizontal via `updateSeries` + `updateOptions`.
3. **Destaque visual:** top 3 com gradientes ouro/prata/bronze (CSS/SVG — evitar depender só de emoji).
4. **Destaque do usuário logado:** barra/contorno diferenciado quando `id` corresponder ao participante autenticado.
5. **Pódio final:** fadeOut do gráfico → fadeIn de `#podio-container` com `@keyframes riseUp` e contador 0→N.
6. **Controles:** Play / Pause / Reiniciar + slider de velocidade + **scrubber de frame** (opcional fase 2).
7. **Auto-play** com pausa em aba oculta (`document.visibilityState`).

**Opções ApexCharts base:**

```js
{
  chart: {
    type: 'bar',
    horizontal: true,
    animations: { enabled: true, speed: 350, dynamicAnimation: { enabled: true, speed: 350 } }
  },
  plotOptions: { bar: { borderRadius: 6, dataLabels: { position: 'top' } } }
}
```

#### [MODIFY] `src/frontend/main.js`

Registrar `initDashboardCorridaPage()` condicionalmente (detecção por `#bar-race-chart` ou `data-page="dashboard-corrida"`).

---

### Componente 3 — Interface (JSP e estilos)

#### [NEW] `webapp/WEB-INF/content/seguro/dashboardCorrida.jsp`

- Wrapper `dashboard-section` (padrão do projeto).
- Título e subtítulo via `fmt:message` (i18n).
- Subtítulo: total de jogos considerados.
- `#bar-race-chart` + `#bar-race-status` (`aria-live="polite"`).
- `#podio-container` oculto por padrão (`hidden`), layout 2º | 1º | 3º.
- Controles: `#btn-play`, `#btn-pause`, `#btn-restart`, `#speed-control`.
- Atributos `data-*` para mensagens i18n (loading, erro, vazio, timeout).

#### [MODIFY] `webapp/css/estilo.css`

> Usar **`estilo.css`** (canônico do projeto). Não criar `index.css` / `dashboard.css` avulsos.

Classes sugeridas:

- `.bar-race-*` — container, label do frame, status
- `.podio-container`, `.podio-column`, `.podio-1/2/3`
- `.podio-name`, `.podio-score`
- `@keyframes riseUp`, `@keyframes fadeIn`

#### [MODIFY] `webapp/template/menu.jspf`

Link para `/seguro/dashboardCorrida.action` com rótulo i18n.

#### [MODify] `src/main/resources/messages.properties` (+ espelho legado)

Chaves sugeridas: `race.title`, `race.subtitle`, `race.loading`, `race.error`, `race.empty`, `race.frame`, `race.controls.*`, `race.podium.*`, `menu.race`.

---

## 4. Verification Plan

### Automated Tests

```bash
# Backend
mvn -Dfrontend.skip=true -Dtest=ParticipanteServiceImplTest,ParticipanteActionTest test

# Frontend
npm run test:frontend -- tests/frontend/dashboardCorrida.test.js
npm run build
```

Cenários mínimos:

- serviço exclui administradores;
- ordenação coerente com classificação oficial;
- invalidação de cache após `GraficoDesempenhoCacheControl.invalidarCacheGlobal()`;
- action JSON com headers e `cacheVersionOnly=true`;
- frontend: timeout não deixa status em loading; pause/resume; estado vazio.

### Manual Verification

1. Ambiente Docker com ≥ 5 jogos finalizados.
2. Abrir `/seguro/dashboardCorrida.action` e validar animação frame a frame.
3. Conferir pódio vs. `/seguro/classificacao.action`.
4. Testar Play / Pause / Reiniciar / velocidade.
5. Mobile (≤ 768px) e `prefers-reduced-motion: reduce`.

---

## 5. Análise — Arquiteto de Software Sênior

### 5.1 Veredito geral

O plano é **viável e bem direcionado**. Reutiliza stack, padrões de cache e biblioteca gráfica já maduros no projeto. A base de serviço já existe; falta completar integração, governança e endurecimento de contrato/performance.

**Prontidão para implementação:** média-alta, **desde que** as melhorias abaixo sejam incorporadas antes de codificar frontend e endpoint.

### 5.2 Pontos fortes

1. **Separação clara** backend (agregação) vs. frontend (animação).
2. **Reuso de ApexCharts** — evita nova dependência.
3. **Exclusão de admins** — alinhada ao ADR-003.
4. **Tela dedicada** — não sobrecarrega `principal.jsp`.
5. **Plano de verificação** manual e automatizada previsto.

### 5.3 Riscos e inconsistências identificados

| # | Problema | Impacto | Recomendação |
|---|----------|---------|--------------|
| R1 | Plano cita **TTL 5 min** como estratégia principal; código existente usa **versão global** (`GraficoDesempenhoCacheControl`) | Dados defasados ou invalidação duplicada/confusa | Adotar **versão + invalidação por placar admin** como fonte de verdade; TTL curto apenas no cliente (como `graficoDesempenho.js`, ~45s) |
| R2 | Ranking por frame ordena só por `pontos` | Ordem diverge da classificação oficial (`Participante.compareTo`) | Reutilizar ordenação oficial ou extrair helper compartilhado |
| R3 | **Ranking completo** em todos os frames | Payload grande na Copa 2026 (dezenas de jogos × dezenas de usuários) | Enviar **Top 10–15 por frame** + metadados; pódio e último frame completos |
| R4 | URLs/nomes inconsistentes (`obterDadosDashboardCorrida` vs `...Json`) | Confusão na implementação | Padronizar: JSP `dashboardCorrida.action`, JSON `obterDadosDashboardCorridaJson.action` |
| R5 | CSS em `index.css` / `dashboard.css` | Quebra convenção do repo | Centralizar em `estilo.css` |
| R6 | Sem `cacheVersion` no JSON | Cliente não detecta atualização admin | Incluir `cacheVersion` + header `X-Grafico-Cache-Version` |
| R7 | Sem tarefa no `passo-a-passo.md` | Perda de rastreabilidade (AGENTS.md) | Registrar tarefa 101+ antes da execução |
| R8 | Testes citam `ParticipanteServiceTest` (não existe com esse nome) | CI falha | Usar `ParticipanteServiceImplTest` ou criar classe dedicada |
| R9 | Chaves de bar race só por `nome` | Barras trocam de posição incorretamente com homônimos | Incluir `id` estável por participante |
| R10 | Endpoint sem modo leve | Recomputação desnecessária ao revisitar tela | Suportar `cacheVersionOnly=true` |

### 5.4 Decisões arquiteturais recomendadas

1. **Contrato JSON versionado** — mesmo padrão da tarefa 60.
2. **Duas actions Struts** — página + JSON (não misturar).
3. **Top N configurável** — constante `DASHBOARD_CORRIDA_TOP_N = 12` no serviço.
4. **Sem pré-aquecimento no login** — aquecimento sob demanda na tela (decisão já tomada na tarefa 60).
5. **Log seguro** — `[DASHBOARD-CORRIDA]` com elapsedMs e contagens, sem PII desnecessária.
6. **ADR complementar** — registrar decisão de Top N + ordenação oficial (1 parágrafo em `.ia/historico/`).

---

## 6. Análise — UX Sênior (Dashboards)

### 6.1 Veredito UX

A proposta tem **alto apelo emocional** (corrida + pódio) e combina com o tom competitivo do bolão. Para dashboards animados, o maior risco não é visual — é **perda de confiança** (dados diferentes da classificação, loading infinito, animação ilegível no mobile).

### 6.2 Princípios aplicados

| Princípio | Aplicação |
|-----------|-----------|
| **Clareza antes de espetáculo** | Label do frame: “Jogo 12 de 64 — BRA x ARG” |
| **Controle do usuário** | Play/Pause/Reiniciar + velocidade; auto-play pausa em aba oculta |
| **Confiança nos dados** | Ordem idêntica à classificação; link “Ver classificação completa” no pódio |
| **Acessibilidade** | `aria-live` no status; medalhas via SVG/CSS; não depender de 🥇🥈🥉 no leitor de tela |
| **Inclusive design** | `prefers-reduced-motion`: pular animação e mostrar frame final + pódio estático |
| **Estados conclusivos** | Nunca ficar em “Carregando…” (lição da tarefa 60) |

### 6.3 Melhorias UX propostas (incorporar ao escopo)

#### Must-have (MVP)

1. **Barra de progresso temporal** — “Jogo 8 / 64” acima do gráfico.
2. **Destaque do usuário logado** — contorno ou badge “Você” na barra correspondente.
3. **Estados vazios explícitos** — “Ainda não há jogos finalizados para montar a corrida.”
4. **CTA pós-pódio** — botão para `/seguro/classificacao.action`.
5. **Medalhas acessíveis** — reutilizar padrão SVG de `leaders-summary` em `principal.jsp`.
6. **Mobile first** — exibir top 8 no mobile; labels enxutos; controles empilhados.

#### Should-have (fase 2)

7. **Scrubber de timeline** — slider para ir a qualquer frame (padrão de dashboards temporais).
8. **Modo “último frame”** — atalho para ver situação atual sem assistir tudo.
9. **Nota de desempate** — quando houver empate técnico no top 3, exibir copy já usada em classificação.

#### Could-have (futuro)

10. **Micro-celebração** no pódio quando o usuário logado estiver no top 3.
11. **Compartilhamento** (screenshot/link) — somente se houver demanda.

### 6.4 Fluxo UX recomendado

```
[Entrada menu] → [Loading] → [Auto-play bar race]
       ↓                              ↓
   [Erro/Vazio]              [Pause / Velocidade / Reiniciar]
                                      ↓
                              [Pódio animado + CTA classificação]
```

---

## 7. Plano revisado de implementação (fases)

### Fase A — Governança (0,5 dia)

- Registrar tarefa 101 no `passo-a-passo.md`.
- Aprovar este plano revisado.

### Fase B — Backend (1 dia)

- Ajustar `construirDadosDashboardCorrida()` (ordenação oficial, Top N, `id`, `cacheVersion`).
- Criar actions Struts (JSP + JSON) e i18n.
- Testes unitários backend.

### Fase C — Frontend MVP (1,5 dia)

- `dashboardCorrida.jsp` + `dashboardCorrida.js` + CSS.
- Estados loading/erro/vazio; auto-play; controles básicos.
- Testes frontend de contrato/comportamento.

### Fase D — UX polish (0,5 dia)

- Pódio animado, destaque usuário, progresso temporal, CTA classificação.
- `prefers-reduced-motion`.

### Fase E — Validação (0,5 dia)

- Smoke manual Docker + mobile.
- Log de sessão em `.ia/logs/`.

**Estimativa total:** ~4 dias úteis.

---

## 8. Critérios de aceite consolidados

1. Nova tela acessível pelo menu autenticado.
2. Animação bar race coerente com classificação oficial (ordem e top 3 final).
3. Administradores excluídos (ADR-003).
4. Cache invalidado ao admin confirmar placar.
5. Estados UX sempre conclusivos (sem loading infinito).
6. Responsivo em ≤ 768px.
7. Acessível: teclado nos controles, `aria-live`, medalhas não dependem só de emoji.
8. Testes backend + frontend verdes; build Vite ok.

---

## 9. Parecer final

| Dimensão | Nota | Comentário |
|----------|------|------------|
| Arquitetura | **Aprovado com ressalvas** | Incorporar versão de cache, Top N, ordenação oficial e URLs padronizadas |
| UX | **Aprovado com melhorias MVP** | Manter espetáculo, mas priorizar confiança, acessibilidade e mobile |
| Risco geral | **Médio** | Controlável com escopo faseado e testes |

**Recomendação:** **GO** para implementação após registrar tarefa 101 e aplicar as melhorias Must-have das seções 5 e 6.
