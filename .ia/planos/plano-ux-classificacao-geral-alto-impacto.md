# Plano: UX de Alto Impacto para Classificacao Geral (Top 10 + Variacao de Posicao)

**Data:** 2026-06-16  
**Escopo:** tela `webapp/WEB-INF/content/seguro/classificacao.jsp` e estilos correlatos em `webapp/css/estilo.css`  
**Objetivo:** elevar a percepcao de valor da classificacao geral sem quebrar layout existente, regras de negocio e estrategia de cache vigente.

## 1. Contexto atual (baseline)

1. A tela ja possui:
   - coluna de variacao (`ranking.delta`) com seta textual (`▲`, `▼`, `•`) e suporte de acessibilidade (`sr-only`);
   - destaque da linha do usuario logado (`ranking-highlight`);
   - tabela completa com metricas de pontuacao e legenda.
2. O backend ja calcula `variacaoPosicao` de forma consistente no cache do ranking (`ParticipanteServiceImpl.aplicarVariacaoPosicao(...)`), com testes cobrindo subida, descida, estabilidade e entradas sem historico.
3. O ranking e cache ja foram refinados para invalidacao por evento de placar admin (nao por timer), e essa governanca nao deve ser alterada.

## 2. Principios de design aplicados

1. **Yukai (agradavel ao olhar):** hierarquia visual forte, destaque dos lideres e leitura imediata da tabela.
2. **Meikai (intuitivo):** estado de variacao autoexplicativo, foco no que importa e baixo esforco cognitivo.
3. **Tsukai (emocionante ao operar):** sensacao de competicao viva com feedback de posicao e protagonismo do Top 10.

## 3. Diretrizes arquiteturais para nao regressao

1. Sem novas consultas SQL para render de classificacao.
2. Sem alteracao na regra de invalidacao de cache de ranking.
3. Reuso dos dados ja disponiveis em `participantes`.
4. Evolucao progressiva: manter tabela atual como base, adicionando camadas visuais sobre o markup existente.
5. Preservar acessibilidade WCAG 2.1 AA (contraste, leitura por leitor de tela, sem depender apenas de cor).

## 4. O que melhorar (visao UX senior)

1. **Bloco de destaque Top 10 (alto impacto):**
   - faixa visual acima da tabela com:
   - podio dos 3 primeiros (ouro/prata/bronze);
   - trilha de 4o a 10o em cards compactos.
2. **Semantica visual da variacao de posicao:**
   - badges com seta + numero + estado (`subiu`, `caiu`, `estavel`, `novo`);
   - reforco textual de acessibilidade (ja existe base em `sr-only`, expandir copy).
3. **Escaneabilidade da tabela:**
   - coluna de posicao com maior peso visual;
   - alinhamento numerico tabular em pontos/metricas;
   - cabeçalho mais legivel e linha do usuario logado com destaque mais sofisticado.
4. **Camada de contexto operacional:**
   - legenda compacta e didatica para variacao;
   - opcional: nota de desempate visivel no topo quando houver empate tecnico.
5. **Responsividade sem ruptura:**
   - mobile-first para top 10 (cards rolaveis horizontalmente);
   - tabela com fallback atual para colunas tecnicas e legenda resumida.

## 5. Estrategia de implementacao (segura e incremental)

1. Fase 1: melhorar percepcao visual mantendo DOM atual quase intacto (CSS + pequenos wrappers JSP).
2. Fase 2: introduzir bloco Top 10 reutilizando `participantes` ja carregados.
3. Fase 3: lapidar acessibilidade, responsividade e testes de regressao.

## 6. Riscos e mitigacoes

1. **Risco:** poluicao visual da tabela.
   - **Mitigacao:** aplicar tokens de densidade e limites claros de destaque (Top 10 + usuario logado).
2. **Risco:** regressao de leitura em mobile.
   - **Mitigacao:** validar breakpoints com fallback de tabela responsiva existente.
3. **Risco:** confusao de variacao nula (sem historico).
   - **Mitigacao:** copy explicita para estado "novo/sem historico".

## 7. Critérios de aceite (funcional + UX)

1. Top 10 destacado sem alterar ordenacao/ranking oficial.
2. Variacao de posicao legivel e acessivel em todos os estados.
3. Sem mudanca de comportamento de cache/invalidacao.
4. Sem regressao da coluna de variacao e das regras de empate.
5. Testes relevantes atualizados e validacao visual desktop/mobile concluida.

