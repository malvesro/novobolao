# ADR-20260217-isolamento-chat-legado.md

## Título
Isolamento e Desativação do Chat Legado (DWR)

## Status
Aceito

## Contexto
O sistema atual utiliza uma implementação de chat baseada em DWR (Direct Web Remoting) e armazenamento em memória.
A análise inicial identificou que:
1.  O framework DWR está obsoleto e possui vulnerabilidades conhecidas.
2.  A implementação atual consome recursos do servidor de aplicação (stateful) e não escala.
3.  O chat não é uma funcionalidade "core" para o funcionamento do bolão (apostas), mas apresenta alto risco de segurança.

## Decisão
Decidimos **desativar** a funcionalidade de chat atual imediatamente como parte da Fase 1 (Segurança e Estabilidade).
A funcionalidade será substituída por uma mensagem estática de "Manutenção" até que uma nova versão (Chat 2.0) seja desenvolvida.

## Consequências
**Positivas:**
*   Redução imediata da superfície de ataque (menos endpoints DWR expostos).
*   Liberação de recursos de memória no servidor.
*   Eliminação de código legado complexo e inseguro.

**Negativas:**
*   Usuários perdem a funcionalidade de comunicação em tempo real temporariamente.

## Plano de Migração
A funcionalidade será recriada na Fase 3 utilizando Spring Boot + WebSocket (STOMP), conforme detalhado no plano de implementação.
