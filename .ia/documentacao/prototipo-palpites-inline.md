# Protótipo UX – Fluxo de Palpites Inline

**Data:** 2026-02-27  
**Responsável:** Time Mercúrio – Arquitetura Frontend  
**Contexto:** Subtarefas 4b/4c/4d do plano `plano-correcao-palpites-popup.md`

---

## 1. Objetivo
Descrever a experiência alvo para registro e visualização de palpites sem o balão flutuante herdado, priorizando:
- interação centrada na tabela de jogos;
- expansão inline acessível com botões explícitos;
- painel lateral opcional para histórico do grupo;
- compatibilidade com CSP rígida e HTMX.

---

## 2. Estrutura Geral

### 2.1 Cabeçalho da Tabela
- Mantém colunas atuais (Data, Jogo, Palpite, Status, Ações).
- Adiciona badge `span.badge` em “Status” com classes:
  - `.badge--success` → palpite registrado;
  - `.badge--warning` → pendente;
  - `.badge--locked` → bloqueado (jogo iniciado/finalizado).
- Tooltips existentes (infra Tippy) limitados a microinformações (tempo restante).

### 2.2 Linha do Jogo (Estado Colapsado)
```html
<tr class="match-row" data-jogo-id="123"
    data-palpite-allowed="true"
    data-palpite-status="registered">
  <td headers="col-data">
    <time datetime="2026-06-11T15:00:00-03:00">11/06 – 15:00</time><br />
    <span class="venue">Monterrey (BRT)</span>
  </td>
  <td headers="col-jogo">
    <span class="team">México</span> x <span class="team">África do Sul</span><br />
    <small class="phase">Fase de Grupos – Grupo A</small>
  </td>
  <td headers="col-palpite">
    <span class="user-pick">2 x 1</span>
  </td>
  <td headers="col-status">
    <span class="badge badge--success" aria-label="Palpite registrado">
      <i class="icon icon-check" aria-hidden="true"></i> Registrado
    </span>
  </td>
  <td headers="col-acoes">
    <button type="button"
            class="btn btn-inline"
            data-action="editar-palpite"
            hx-get="/seguro/palpiteFormPartial.action"
            hx-target="#match-expand-123"
            hx-trigger="click"
            hx-vals='{"jogoId":123}'
            aria-expanded="false"
            aria-controls="match-expand-123">
      <i class="icon icon-edit" aria-hidden="true"></i> Editar palpite
    </button>
    <button type="button"
            class="btn btn-secondary"
            data-action="ver-grupo"
            hx-get="/seguro/palpitesGrupoPartial.action"
            hx-target="#palpite-panel"
            hx-trigger="click"
            hx-vals='{"jogoId":123}'
            aria-haspopup="dialog">
      <i class="icon icon-users" aria-hidden="true"></i> Ver palpites do grupo
    </button>
  </td>
</tr>
```

### 2.3 Linha Expandida (Formulário Inline)
```html
<tr id="match-expand-123" class="match-expand" hidden>
  <td colspan="5">
    <section class="palpite-inline" aria-label="Editar palpite – México x África do Sul">
      <form id="palpite-form-123"
            hx-post="/seguro/salvarPalpite.action"
            hx-target="#match-expand-123"
            hx-swap="outerHTML"
            data-hx-spinner="#palpite-loading-123">
        <div class="palpite-grid">
          <div class="team-score">
            <label for="palpite-mexico-123">México</label>
            <input id="palpite-mexico-123"
                   name="palpite.golsTime1"
                   type="number" min="0" inputmode="numeric"
                   value="2"
                   required />
          </div>
          <div class="team-score">
            <label for="palpite-africa-123">África do Sul</label>
            <input id="palpite-africa-123"
                   name="palpite.golsTime2"
                   type="number" min="0" inputmode="numeric"
                   value="1"
                   required />
          </div>
          <div class="actions">
            <button type="submit" class="btn btn-primary">
              <i class="icon icon-save" aria-hidden="true"></i> Salvar
            </button>
            <button type="button"
                    class="btn btn-text"
                    data-action="cancelar"
                    data-target="#match-expand-123">
              Cancelar
            </button>
          </div>
        </div>
        <div id="palpite-loading-123" class="loading-inline" aria-hidden="true">
          <span class="spinner"></span> Processando...
        </div>
        <input type="hidden" name="_csrf" value="${csrfToken}"/>
        <input type="hidden" name="palpite.jogoId" value="123"/>
      </form>
    </section>
  </td>
</tr>
```

---

## 3. Painel Lateral de Histórico

### 3.1 Estrutura
```html
<aside id="palpite-panel" class="palpite-panel" hidden>
  <header>
    <h2>Palpites do grupo</h2>
    <button type="button" class="btn btn-icon" data-action="fechar-painel" aria-label="Fechar painel">
      <i class="icon icon-close" aria-hidden="true"></i>
    </button>
  </header>
  <div class="palpite-panel__content"
       hx-target="this"
       hx-swap="innerHTML">
    <!-- Conteúdo carregado via HTMX -->
  </div>
</aside>
```

### 3.2 Comportamento
- O painel utiliza `<dialog>` em navegadores que suportem `showModal()` para garantir foco e backdrop. Fallback: `<aside>` com overlay CSS.
- `hx-target="#palpite-panel"` carrega lista/HX parcial (`palpitesGrupoPartial.action`).
- Fechamento via botão, tecla `Escape` e clique no backdrop (`data-action="fechar-painel"`).

---

## 4. Interações HTMX e Scripts

### 4.1 Eventos Principais
| Evento | Origem | Ação |
|--------|--------|------|
| `click` no botão `Editar palpite` | `.match-row` | Executa `hx-get`, revela linha expandida (remove `hidden`), atualiza `aria-expanded`. |
| `htmx:afterSwap` no `match-expand-*` | HTMX | Foca primeiro `input` do formulário, ativa máscara numérica se necessário. |
| `submit` do formulário | `<form>` | `hx-post` salva palpite e retorna bloco atualizado (linha + badge). |
| `htmx:afterSettle` | `#palpite-panel` | Anuncia resultado em `aria-live` e garante foco inicial no título do painel. |
| `click` em botão `Cancelar` | `.actions` | Reoculta linha expandida, restaura `aria-expanded="false"`. |

### 4.2 Módulo JS (`src/frontend/pages/jogos.js`)
- Funções previstas:
  - `initMatchRows()` – delega clicks, controla `aria-expanded`, aplica classe `.match-row--expanded`.
  - `handleCancel(event)` – fecha expansão revertendo alterações de DOM/aria.
  - `initPalpitePanel()` – gerencia abertura/fechamento do painel lateral, inclusive fallback `<dialog>`.
  - `applyResponsiveLayout()` – adiciona classes auxiliares quando viewport < 720px (empilha colunas).
- Importações:
  - `import { attachCsrfHeaders } from '../modules/http.js';`
  - `import { initDialog } from '../modules/dialog.js';`
- Eliminar `import()` dinâmico; loader (`cabecalho.jspf`) passa a usar `<script type="module" src="${assetUrl}" nonce="${cspNonce}">`.

---

## 5. Layout e CSS

### 5.1 Classes Utilizadas
- `.match-expand` – linha expandida; toggles `hidden`.
- `.palpite-inline` – container flex/grid (coluna em mobile, linha em desktop).
- `.palpite-grid` – `display: grid` com template `repeat(auto-fit, minmax(160px, 1fr))`.
- `.palpite-panel` – painel lateral com largura `min(28rem, 90vw)`; em mobile assume `100vw` cobrindo a tela.
- `.badge`/`.badge--*` – reutilizar utilitários definidos em `estilo.css` (ou ampliar).
- `.btn` variantes: `.btn-inline`, `.btn-secondary`, `.btn-text`, `.btn-icon` (já presentes).

### 5.2 Mobile-first
- Breakpoint de 480px: linha colapsada mostra times empilhados, botões viram `display:block`.
- Breakpoint de 768px: painel lateral vira slide-over com overlay.
- Scroll locking ao abrir painel (`body.no-scroll`).

---

## 6. Acessibilidade
- Sempre que a linha expandir, `aria-expanded="true"` e `aria-controls` apontando para o `tr` correspondente.
- Formulário com labels associadas (`for`/`id`) e `inputmode="numeric"` para facilitar teclado numérico.
- Panel anuncia abertura em `role="dialog"` / `aria-modal="true"` (para `<dialog>`) ou `role="complementary"` (fallback).
- Mensagens de sucesso/erro exibidas em `<p role="status">`.

---

## 7. Testes Planejados
1. **Fluxo USER**: expandir, editar, salvar palpite antes e depois do limite de horário.
2. **Fluxo ADMIN**: acessar painel do grupo, conferir palpites de todos, bloquear edição (status `locked`).
3. **Acessibilidade**: navegação teclado (Tab/Shift+Tab), leitores (NVDA/VoiceOver), `Escape` no painel.
4. **Responsivo**: viewport 360px (mobile) e 1280px (desktop); verificação de overflow horizontal.
5. **CSP**: console sem violações em modo enforcement (scripts `type="module"` com nonce).

---

## 8. Pendências e Próximos Passos
- Implementar protótipo HTML estático (página mock em `telas/` ou `webapp/static/prototipo-palpites.html` para validação rápida).
- Atualizar diretrizes frontend após a implementação.
- Preparar plano de migração de dados do formulário (manter API Struts existente).

> **Nota:** Este protótipo serve como referência para as próximas subtarefas. Ajustes de wording/estilo serão feitos em conjunto com UX durante a implementação.
