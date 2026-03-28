# Regra de Encerramento de Palpites

## Fonte da Regra
- `src/com/opendev/bolao/model/Jogo.java:135` – método `getPodeDarPalpite()`
- `src/com/opendev/bolao/action/ParticipanteAction.java:386` – método `prepararConteudoPalpite()`
- `webapp/WEB-INF/content/seguro/jogos.jsp:287` – cálculo do flag `data-palpite-allowed`

## Comportamento Atual
1. Ao avaliar se um palpite pode ser editado, `ParticipanteAction` chama `Jogo.getPodeDarPalpite()`.
2. `Jogo.getPodeDarPalpite()` obtém o horário atual do servidor (`Calendar.getInstance()`), soma **1 hora** e compara com a data/hora da partida (`getDataHora()`).
3. O retorno é **true** apenas enquanto `(agora + 1h) < dataHoraDoJogo`. Assim, os palpites são encerrados exatamente **1 hora antes do horário de início** do jogo registrado no banco.
4. A camada JSP (`jogos.jsp`) combina esse resultado com a verificação de papéis (`hasAnyRole('USER','ADMIN')`). Quando `palpitePermitido` é falso, o front-end exibe a mensagem “Palpites encerrados para esta partida.”

## Observações Técnicas
- `getDataHora()` monta a data/hora utilizando os campos `Jogo.data` (java.util.Date sem horário) e `Jogo.hora` (java.sql.Time). Qualquer divergência de fuso horário do servidor impacta diretamente a janela de apostas.
- O método `Jogo.jaOcorreu()` ainda retorna sempre `true` (legado). Ele não interfere no encerramento porque a lógica atual depende apenas de `getPodeDarPalpite()`, mas o método deve ser revisado em futuras correções para evitar inconsistências.
- Não há persistência do instante em que os palpites foram fechados; a decisão é calculada dinamicamente a cada requisição.

## Recomendação de Documentação/Follow-up
- Registrar em futuras ADRs ou no README a necessidade de alinhar o fuso horário do servidor com o calendário oficial da Copa 2026 para evitar encerramentos antecipados ou tardios.
- Avaliar se a regra “1 hora antes” continua válida para fases eliminatórias quando o calendário oficial confirmar horários exatos.
