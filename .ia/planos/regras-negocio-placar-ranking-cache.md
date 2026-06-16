# Regras de Negócio: Placar, Pontuação e Cache de Ranking

Data: 2026-06-16
Contexto: refinamento para ranking dinâmico sem invalidar cache por relógio.

## 1) Atualização de placar pelo administrador

1. O administrador só pode atualizar o placar de um jogo a partir do horário de início da partida.
2. Jogos passados permanecem editáveis para correções retroativas.
3. Tentativas de atualização antes do início devem ser rejeitadas com erro de validação (HTTP 400 no fluxo HTMX).

## 2) Cálculo de pontuação

1. Pontuação pode ser recalculada em tempo real durante o jogo, desde que o resultado já tenha sido atualizado pelo administrador.
2. A condição temporal de jogo ocorrido é a partir do horário de início da partida.

## 3) Invalidação de cache de ranking

1. O cache da classificação geral deve ser invalidado apenas por evento de domínio relevante:
   - atualização de placar válida (admin + jogo iniciado ou passado).
2. Não invalidar cache por timer/minuto apenas por passagem de tempo.
3. Alterações estruturais do jogo (data/hora/local/equipes/fase) não devem invalidar automaticamente o ranking.

## 4) Objetivo arquitetural

- Garantir previsibilidade e eficiência de cache.
- Evitar recomputações periódicas sem evento de negócio.
- Manter ranking responsivo quando placar é atualizado durante a partida.
