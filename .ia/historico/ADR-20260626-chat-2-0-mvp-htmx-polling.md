# ADR-20260626-chat-2-0-mvp-htmx-polling

**Data:** 2026-06-26
**Status:** Aprovado

## Contexto

O chat legado havia sido desativado por risco técnico e de segurança, permanecendo apenas a rota `/seguro/batePapo.action` com mensagem de manutenção e sem operação funcional.
Havia ambiguidade histórica entre duas direções:

1. reconstrução imediata com WebSocket/STOMP;
2. retomada incremental aderente ao stack atual do sistema (Struts 7 + JSP + HTMX + Spring Data JPA).

Para liberar valor com menor risco de regressão e menor custo de operação, era necessário decidir o transporte e o recorte funcional do Chat 2.0.

## Decisao

Foi adotado **MVP de Chat 2.0 com HTMX polling incremental**, mantendo a arquitetura atual do monólito:

- UI JSP/HTMX;
- Action dedicada (`ChatAction`);
- Service dedicado (`ChatService`);
- Repository JPA (`ChatMensagemRepository`);
- persistência em banco (`CHT_CHAT_MENSAGEM` via JPA/Hibernate `hbm2ddl=update`).

Escopo aprovado no MVP:

- envio de mensagens por POST;
- atualização incremental por `lastMessageId`;
- presença online por janela de atividade;
- apelido opcional por sessão;
- validação de entrada, bloqueio de HTML, rate limit e resposta HTTP coerente (`400`, `429`, `401`).

Evolução para WebSocket permanece possível em fase futura, sem quebrar os contratos funcionais já consolidados no MVP.

## Alternativas Consideradas

1. **WebSocket/STOMP imediato**
   - Maior complexidade de infraestrutura, observabilidade e operação neste momento.
2. **Manter chat desativado**
   - Menor risco técnico, porém sem entrega funcional para usuários.
3. **MVP HTMX polling incremental (decisão adotada)**
   - Melhor relação risco/valor para o estágio atual da arquitetura.

## Consequencias

- Positivas:
  - reativação funcional do chat com baixo acoplamento e rastreabilidade completa;
  - alinhamento ao padrão arquitetural vigente (Action -> Service -> Repository);
  - redução de dívida técnica com remoção do runtime legado de chat.
- Riscos/Custos:
  - polling periódico implica custo contínuo de requisições;
  - presença e rate limit em memória local exigem revisão quando houver múltiplas instâncias.

## Responsaveis

- Arquitetura e Implementação: Time de Engenharia (Codex + revisão multiagente)
