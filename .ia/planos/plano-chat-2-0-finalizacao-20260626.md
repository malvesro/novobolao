# Plano e Proposta — Finalização da Tela e Funcionalidades de Chat (Chat 2.0)

**Data:** 2026-06-26  
**Autores (multiagente):** Requisitos, Arquitetura, UX, Tester, Security  
**Referências:** `ADR-20260217-isolamento-chat-legado.md`, `plano-migracao-dwr-htmx.md`, `AGENTS.md`, `passo-a-passo.md`

## 1. Diagnóstico Atual (Estado Real do Código)

### 1.1 O que existe hoje
- Rota ativa: `/seguro/batePapo.action` (`struts.xml`) renderizando `WEB-INF/content/seguro/batePapo.jsp`.
- Action existente: `ParticipanteAction.batePapo()` retorna apenas `SUCCESS` sem regra funcional de chat.
- Tela atual (`batePapo.jsp`) exibe somente cartão de indisponibilidade ("Funcionalidade desativada temporariamente").
- Beans legados ainda registrados:
  - `com.opendev.bolao.chat.BatePapo`
  - `com.opendev.bolao.chat.FormatadorMensagem`
- Classes legadas de chat permanecem no código (`BatePapo`, `Mensagem`, `FormatadorMensagem`), porém sem integração com UI/fluxo moderno.
- Item de menu do chat está comentado em `menu.jspf` (sem acesso navegacional ao usuário).
- Não há endpoints HTMX/REST de chat nem JS moderno associado ao fluxo.

### 1.2 O que já foi decidido no histórico
- ADR aceita: chat legado DWR foi isolado/desativado por risco de segurança e obsolescência.
- Existe intenção histórica de "Chat 2.0", mas sem implementação funcional concluída.

## 2. Análise de Lacunas (O que falta para concluir)

Para considerar a funcionalidade "chat" concluída, faltam os seguintes blocos:

1. **Requisitos funcionais explícitos** (escopo MVP, limites e regras de moderação).
2. **Modelo de domínio persistente** (mensagens não podem ficar apenas em memória).
3. **Endpoints de leitura/escrita** com validação e autorização robustas.
4. **Atualização em tempo real** ou quase real para UX de conversa fluida.
5. **Tela funcional de chat** (lista de mensagens, envio, estado de carregamento/erro/vazio, presença).
6. **Regras antiabuso e segurança** (rate limit, sanitização, tamanho, flood, XSS).
7. **Observabilidade operacional** (logs, métricas, auditoria).
8. **Cobertura de testes** (unitário, integração e regressão frontend).
9. **Rastreabilidade completa** (plano, tarefa, log de sessão e ADR se necessário).

## 3. Proposta de Solução (MVP + Evolução)

## 3.1 Escopo MVP recomendado (entrega objetiva)
- Reativar item de menu "Sala de Bate-Papo".
- Implementar chat com:
  - envio de mensagem textual;
  - lista de mensagens recentes (janela deslizante);
  - atualização automática periódica (HTMX polling curto);
  - indicação de participantes online (reuso do contador já existente + presença de sala);
  - apelido opcional por sessão.
- Persistência em banco para histórico básico e resiliência entre reinícios.
- Segurança mínima obrigatória (sanitização, CSRF, validação de tamanho e rate limit).

## 3.2 Estratégia técnica recomendada
- **Fase 1 (MVP): HTMX + endpoints Struts/JSON/partials**
  - Menor risco de integração e aderência à diretriz de modernização do projeto.
  - Evita introdução de infraestrutura adicional já no primeiro corte.
- **Fase 2 (opcional): Evoluir para WebSocket/STOMP**
  - Só após estabilizar contrato funcional e segurança do MVP.
  - Requer ADR complementar e desenho de operação em produção.

## 4. Requisitos Funcionais Propostos (MVP)

1. Usuário autenticado acessa `/seguro/batePapo.action`.
2. Usuário envia mensagem com limite de tamanho (ex.: 300 chars).
3. Mensagens aparecem em ordem cronológica e com horário local (BRT canônico).
4. Tela atualiza automaticamente novas mensagens sem refresh completo.
5. Usuário define/atualiza apelido de exibição.
6. Histórico inicial carrega última janela (ex.: 50 mensagens).
7. Erros de envio e validação são exibidos de forma amigável.

## 5. Requisitos Não Funcionais

1. **Segurança:** sem HTML arbitrário; saída escapada; validação server-side.
2. **Resiliência:** histórico em banco; degradação elegante se endpoint falhar.
3. **Performance:** paginação/janela deslizante para evitar crescimento infinito no DOM.
4. **Acessibilidade:** região com `aria-live` para mensagens novas e foco previsível.
5. **Observabilidade:** logs estruturados (envio, erro, bloqueio por rate limit).

## 6. Riscos e Mitigações

1. **Flood/spam de mensagens**
   - Mitigação: rate limit por usuário e cooldown de envio.
2. **XSS em conteúdo de mensagem**
   - Mitigação: escape estrito + whitelist mínima de formatação.
3. **Carga por polling frequente**
   - Mitigação: polling incremental por `lastMessageId` e intervalo adaptativo.
4. **Acoplamento com legado**
   - Mitigação: criar módulo/chat novos sem reaproveitar runtime legado em memória.

## 7. Critérios de Pronto (DoD da funcionalidade)

1. Tela funcional de chat disponível no menu.
2. Mensagens enviadas e recebidas em tempo quase real no MVP.
3. Persistência validada (mensagens sobrevivem a restart da aplicação).
4. Testes automatizados cobrindo fluxo crítico.
5. Hardening básico de segurança validado.
6. Documentação e rastreabilidade atualizadas.

## 8. Próximos Passos Recomendados

1. Aprovar escopo MVP e limites (tamanho, janela, rate limit).
2. Implementar backlog faseado da tarefa de chat no `passo-a-passo.md`.
3. Reavaliar necessidade de WebSocket após operação do MVP em produção.

---
**Parecer multiagente consolidado:** viável, com risco controlável no MVP (HTMX + persistência + segurança).  
**Risco:** Médio (por envolver segurança e concorrência), mitigável por rollout faseado e testes.
