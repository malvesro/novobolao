# ADR-20260618: Cache versionado do gráfico de desempenho com invalidação por resultado administrativo

## Status
Aprovado

## Contexto
A tela de gráfico de desempenho (`/seguro/graficoDesempenho.action`) passou a operar com cache client-side por rival e endpoint JSON com cache curto.

Foi identificado um problema operacional:
1. risco de estado de loading sem fechamento em timeout;
2. risco de exibição de dados defasados após atualização de resultado pelo admin;
3. necessidade de reduzir latência percebida no primeiro acesso do usuário ao gráfico.

## Decisão
Adotar estratégia híbrida de cache com versionamento lógico global:

1. **Versionamento global de cache do gráfico**
   - novo controle `GraficoDesempenhoCacheControl` com contador atômico de versão.
   - versão atual é incluída no payload JSON (`cacheVersion`) e no header `X-Grafico-Cache-Version`.

2. **Invalidação por evento de domínio**
   - ao confirmar atualização de resultado em `JogoServiceImpl.atualizarResultado(...)`, a versão global é incrementada.
   - `ParticipanteServiceImpl` sincroniza a versão e descarta cache em memória local quando detecta mudança.

3. **Cache e aquecimento na tela de gráfico (sem pré-aquecimento no login por padrão)**
   - aquecimento ocorre ao acessar `/seguro/graficoDesempenho.action`, com cache client-side por rival;
   - ao sair e voltar para a tela, o gráfico reaproveita cache local válido (TTL + versão);
   - handshake de versão (`cacheVersionOnly=true`) evita reuso de dados obsoletos.

4. **Fechamento explícito de estados UX em timeout**
   - timeout da requisição ativa passa a estado terminal de erro com retry explícito.
   - cancelamentos obsoletos por troca rápida de rival não sobrescrevem estado da seleção mais recente.

## Consequências
### Positivas
- melhora da consistência entre atualização admin e leitura do gráfico;
- redução de latência percebida no retorno à tela de gráfico com cache local válido;
- maior previsibilidade de UX (sem "loading infinito" silencioso);
- estratégia incremental sem quebrar contrato Action -> Service -> DAO.

### Trade-offs
- aumento de complexidade no gerenciamento de cache (versão + TTL + invalidadores);
- requisição extra de verificação de versão em cenários de reuso de cache.

## Guardrails
- manter cache de dados sensíveis como **privado por usuário**;
- evitar aquecimento no login em ambiente restrito, priorizando aquecimento sob demanda na tela;
- invalidar em evento de atualização de resultado, não em leitura.

## Referências
- `.ia/planos/plano-correcao-grafico-desempenho-timeout-cache-20260618.md`
- `.ia/logs/session-20260618-tarefa60-iteracao-cache-admin-invalidation.md`
- `.ia/logs/session-20260618-tarefa60-encerramento-consolidado.md`
