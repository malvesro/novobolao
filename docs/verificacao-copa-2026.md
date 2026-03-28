# Verificação de Prontidão: Copa do Mundo FIFA 2026 (Todas as Fases)

Este documento detalha o suporte do sistema Bolão para todas as etapas da competição, desde a fase de grupos até a grande final, seguindo o novo formato de 48 seleções.

## 1. Definição de Domínio e Fases

O sistema possui constantes e mapeamentos para todas as etapas da competição:

- **Mapeamento de Códigos (`JOG_FASE`)**:
    - `11, 12, 13`: Fase de Grupos (Rodadas 1, 2 e 3).
    - `16`: **32-avos de final** (Nova fase para 48 seleções).
    - `8`: **Oitavas de final**.
    - `4`: **Quartas de final**.
    - `2`: **Semifinais**.
    - `3`: **Disputa de 3º lugar**.
    - `1`: **Final**.

- **Consistência**: Todos os códigos estão mapeados em `com.opendev.bolao.util.FaseUtils` e possuem chaves de tradução correspondentes em `messages.properties` (`filter.fase.X`).

## 2. Lógica de Pontuação e Ranking

A lógica de cálculo de pontos é unificada e agnóstica à fase, garantindo integridade em todo o torneio:

- **Cálculo de Pontos**: O método `Palpite.getPontuacao()` aplica as mesmas regras (6, 3, 2, 1 ou 0 pontos) para qualquer partida, seja ela de grupos ou da final.
- **Acúmulo de Pontos**: O ranking geral (`Participante.getPontuacaoTotal()`) consolida os acertos de todas as fases sem distinção ou exclusão de códigos específicos.
- **Desempate**: O sistema utiliza o total de pontos acumulados e, em caso de empate, a ordem alfabética (conforme `Participante.compareTo`).

## 3. Comportamento da Interface (UI) e Filtros

- **Filtro de Fase**: O seletor em `jogos.jsp` utiliza o conjunto completo de tokens `11,12,13,16,8,4,2,3,1`, permitindo filtrar qualquer etapa da Copa 2026.
- **Filtro de Grupo**: O sistema suporta a filtragem pelos novos grupos (A até L), pois a lógica de grupos em `FiltroBuscaJogos.java` é baseada em strings dinâmicas vindas do banco de dados.
- **Knockout vs Grupos**: O método `Jogo.isFaseDeGrupos()` identifica corretamente apenas 11, 12 e 13 como fases de grupos, tratando todas as demais como fases eliminatórias.

## 4. Banco de Dados e Dataset

- **Carga SQL**: O arquivo `03-copa-2026-data.sql` já contempla os 104 jogos distribuídos entre todas as fases listadas acima, com datas e locais configurados conforme o calendário oficial FIFA 2026.

## Conclusão

O sistema atende integralmente aos requisitos estruturais e de regras para todas as fases da Copa 2026. Não foram detectadas inconsistências ou "gaps" de lógica que impeçam o funcionamento correto desde o jogo de abertura até a final.

> [!TIP]
> **Próxima Etapa Recomendada:** Realizar um "Smoke Test" visual na tela de palpites após a carga do SQL para confirmar a ordenação cronológica e a exibição das bandeiras/emojis das 48 seleções.
