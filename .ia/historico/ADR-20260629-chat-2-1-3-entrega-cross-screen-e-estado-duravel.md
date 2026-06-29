# ADR-20260629-chat-2-1-3-entrega-cross-screen-e-estado-duravel

**Data:** 2026-06-29  
**Status:** Aprovado

## Contexto

A evolução do Chat 2.1.2 melhorou o contrato HTTP das menções (GET sem efeito colateral e ACK via POST), mas ainda havia riscos críticos para produção:

- entrega de menções acoplada ao conceito de “online na tela de chat”, reduzindo confiabilidade cross-screen;
- pendências/histórico em memória local do processo, com perda de estado em restart/cold start e inconsistência em multi-instância;
- necessidade de reforçar governança de segurança do ACK (autorização + fluxo assíncrono válido) sem expor payload sensível em logs.

No ambiente alvo (Hugging Face gratuito), reinícios e variações de runtime são esperados, então estado efêmero para notificações não atende previsibilidade mínima.

## Decisão

Adotar **persistência durável em banco relacional** para menções do chat, com política de entrega cross-screen desacoplada de presença na tela:

1. **Política de destinatários (cross-screen):**
   - menção direta (`@login`) entrega para participante habilitado, independentemente de presença no chat;
   - menção ampla (`@Todos`) entrega para todos os participantes habilitados, exceto autor.

2. **Store durável de menções:**
   - manter cada menção em tabela persistente com unicidade por `destinatarioLogin + chatMensagemId`;
   - manter estado pendente/ack por registro (ACK idempotente por IDs enviados);
   - preservar histórico recente e aplicar limites de retenção para evitar crescimento anômalo.

3. **Segurança e contrato de ACK:**
   - ACK continua obrigatório via `POST` com sessão autenticada;
   - ACK exige requisição assíncrona válida (`X-Requested-With=XMLHttpRequest` ou `HX-Request=true`), além da proteção CSRF já aplicada pelo filtro de segurança da aplicação;
   - logs estruturados sem texto integral da mensagem.

## Alternativas Consideradas

1. **Manter fila em memória com sinalização de degradação**
   - Prós: menor esforço imediato.
   - Contras: não resolve restart/multi-instância; risco funcional permanece alto.

2. **Store externo dedicado (Redis/pub-sub)**
   - Prós: baixa latência e bom suporte a distribuição.
   - Contras: adiciona dependência de infraestrutura fora do escopo imediato e aumenta custo operacional para cenário HF gratuito.

3. **Persistência em banco relacional compartilhado (decisão adotada)**
   - Prós: confiabilidade em restart/multi-instância com stack já existente; boa rastreabilidade e governança.
   - Contras: maior custo de I/O no banco e necessidade de políticas de retenção.

## Consequências

- **Positivas:**
  - menções passam a ser confiáveis entre telas e instâncias;
  - ACK torna-se deterministicamente idempotente;
  - redução de risco de perda silenciosa de notificação.

- **Custos/Riscos aceitos:**
  - aumento moderado de operações de banco para registrar/consultar/ack menções;
  - necessidade de manutenção periódica de índices e observação de retenção.

## Plano de Rollback

Em caso de regressão crítica de produção:

1. retornar `ChatNotificationServiceImpl` para modo anterior em memória local;
2. manter contrato HTTP atual (GET leitura + POST ACK) para evitar impacto no frontend;
3. preservar schema/tabela de menções sem uso ativo até estabilização;
4. reativar persistência após correção e nova bateria completa de testes.

## Responsáveis

- Architect: definição da política cross-screen e decisão de estado durável.
- Developer: implementação backend/frontend compatível com contrato vigente.
- Security/Reviewer: validação de fluxo de ACK, logs e rastreabilidade.
