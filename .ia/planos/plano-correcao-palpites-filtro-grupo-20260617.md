# Plano: Correcao da Tela de Palpites e Resultados (Filtro + Botao "Ver palpites do grupo")

**Data:** 2026-06-17  
**Escopo:** `webapp/WEB-INF/content/seguro/jogos.jsp`, `webapp/WEB-INF/content/seguro/partials/*`, `src/frontend/pages/jogos.js`, `webapp/css/estilo.css`, testes frontend/backend associados.

## 1. Contexto e sintomas reportados

1. Possivel quebra de layout ao usar filtro de pesquisa na tela de palpites.
2. Botao "Ver palpites do grupo" por jogo nao abre/exibe os palpites.

## 2. Achados preliminares (analise estatica)

1. **Markup fragil no bloco de filtro (`jogos.jsp`)**:
   - trecho de `label` do campo `filtro_fase` aparenta fechamento inconsistente, com alto risco de gerar DOM malformado em alguns browsers;
   - bloco de filtro ainda usa estrutura tabular antiga e sensivel a pequenos erros de fechamento.
2. **Fluxo do botao de grupo depende de dois mecanismos em conjunto**:
   - HTMX (`hx-get` para `palpitesDoJogoPartial.action`) para carregar linhas no `tbody` alvo;
   - JS delegado (`data-js="toggle-group-details"`) para abrir/fechar a linha oculta.
3. **Risco de regressao silenciosa por falta de testes direcionados**:
   - nao existe teste frontend cobrindo especificamente:
   - click no `.btn-grupo-toggle`;
   - alternancia de classe `.hidden` na linha de detalhes;
   - atualizacao do conteudo via alvo `#group-content_<id>`;
   - comportamento do filtro apos submit (integridade estrutural).

## 3. Hipoteses de causa raiz (priorizadas)

1. **H1 - DOM malformado no filtro** (prioridade alta): tags mal fechadas no formulario podem quebrar alinhamento/colapso do layout.
2. **H2 - Fluxo JS/HTMX do botao de grupo inconsistente** (prioridade alta): evento abre/fecha sem garantir estado de carregamento/erro e sem teste de contrato.
3. **H3 - CSS acoplado a estrutura antiga** (prioridade media): classes de layout podem nao cobrir estados reais apos filtro/swap HTMX.
4. **H4 - Regressao por falta de cobertura** (prioridade alta): ausencia de testes permite retorno do bug em alteracoes futuras.

## 4. Objetivos de correcao

1. Garantir estrutura HTML valida e resiliente no filtro.
2. Restaurar funcionamento confiavel do botao "Ver palpites do grupo" com UX clara (carregando/erro/vazio).
3. Eliminar quebra visual apos interacoes de filtro.
4. Introduzir cobertura automatizada minima para prevenir regressao.

## 5. Estrategia de execucao (iterativa e segura)

### Fase A - Diagnostico reproduzivel
1. Reproduzir localmente os 2 sintomas com evidencia objetiva (passos, DOM resultante e pontos de quebra).
2. Isolar o gatilho principal (markup, JS, CSS ou combinacao).

### Fase B - Correcao estrutural do filtro
1. Corrigir fechamento semantico de tags no bloco de filtro.
2. Revisar estrutura das linhas/campos para evitar dependencia de comportamento tolerante do browser.
3. Validar visual desktop/mobile apos submit do filtro.

### Fase C - Correcao do botao de grupo
1. Endurecer contrato de toggle:
   - abrir/fechar confiavel da linha alvo;
   - estado `active` coerente no botao;
   - manutencao do modo accordion.
2. Endurecer contrato de carregamento HTMX:
   - estado de loading, vazio e erro sem travar a UI;
   - garantir alvo correto do swap.

### Fase D - Testes e regressao
1. Adicionar teste frontend para:
   - click no botao de grupo e alternancia da linha;
   - garantia de seletores/atributos esperados no `match-row.jspf`;
   - validacao de que o filtro permanece estruturalmente consistente.
2. Executar bateria focal e documentar resultado.

## 6. Restricoes arquiteturais

1. Nao alterar regras de negocio de palpite/autorizacao.
2. Nao alterar estrategia de cache de ranking.
3. Nao introduzir dependencias legadas (DWR/Prototype).
4. Manter interacoes assincronas em HTMX + JS nativo.

## 7. Criterios de aceite

1. Filtro nao quebra layout (desktop e mobile).
2. Botao "Ver palpites do grupo" abre e fecha corretamente em qualquer jogo.
3. Conteudo do grupo carrega corretamente e mostra estados de erro/vazio quando necessario.
4. Testes novos e relevantes passando em conjunto com regressao focal.

