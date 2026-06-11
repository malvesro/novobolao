# ADR-20260611: Tempo Canonico do Dominio em America/Sao_Paulo

## Status
Aprovado

## Contexto
O dominio do sistema (Bolao da Copa 2026) possui regras sensiveis a tempo:
- bloqueio de palpites 1h antes do jogo;
- jobs de notificacao em horarios fixos;
- auditoria de alteracoes e mensagens de feedback temporal.

Historicamente havia combinacao de estrategias:
- alguns pontos usando timezone explicito (`BolaoTime`, `ZonedDateTime`),
- outros dependentes do timezone default do host/JVM/DB (`systemDefault`, `Calendar`, `CURRENT_TIMESTAMP` sem alinhamento de sessao).

Essa heterogeneidade aumenta o risco de deriva temporal entre ambientes.

### Cenário de produção (Hugging Face)
- O runtime de produção é iniciado via `Dockerfile` em infraestrutura do Hugging Face.
- O host/plataforma pode operar em timezone diferente de São Paulo.
- Os dados de jogos no banco já são gerenciados no referencial de São Paulo.
- Portanto, a aplicação não pode depender do timezone implícito do host para regras de negócio.

## Decisao
Padronizar o sistema para um **tempo canonico de dominio**:

1. Zona oficial do dominio: `America/Sao_Paulo`.
2. Regras de negocio e comparacoes temporais devem usar `BolaoTime`.
3. Scheduler Quartz deve declarar timezone explicito em todos os triggers.
4. Conexao JDBC/MySQL deve forcar timezone de sessao compativel com a zona oficial.
5. Calculos de dia devem usar API de calendario (`LocalDate.plusDays`) e nao aritmetica fixa em milissegundos.
6. Testes devem refletir explicitamente a zona do dominio para evitar falsos positivos/negativos.

## Implementacao associada
- `src/main/resources/applicationContext-scheduler.xml`:
  - triggers com `timeZone=America/Sao_Paulo`.
- `src/com/opendev/bolao/service/impl/PalpiteAuthorizationServiceImpl.java`:
  - relogio canonico via `BolaoTime`.
- `src/com/opendev/bolao/action/ParticipanteAction.java`:
  - remocao de `+86400000` e uso de `LocalDate.plusDays`.
- `src/com/opendev/bolao/service/impl/JogoServiceImpl.java`:
  - normalizacao de calculos para `BolaoTime`.
- `src/com/opendev/bolao/service/ErrorNotificationService.java` e `src/com/opendev/bolao/chat/BatePapo.java`:
  - timestamp/formatacao com timezone explicito do dominio.
- `src/main/resources/applicationContext-resources.xml` + `docker-compose.yml`:
  - alinhamento JDBC/MySQL (`connectionTimeZone`, `forceConnectionTimeZoneToSession`, `--default-time-zone=-03:00`).
- `tests/com/opendev/bolao/service/PalpiteAuthorizationServiceImplTest.java`:
  - ajuste da suite para timezone canonico.

## Consequencias
### Positivas
- reducao de deriva temporal entre ambientes;
- consistencia entre UI, regras e persistencia;
- maior previsibilidade de janelas de palpite e jobs.

### Riscos/Trade-offs
- dependencia explicita de uma zona de negocio unica (esperado pelo dominio);
- necessidade de manter testes e novos codigos alinhados ao padrao temporal.

## Guardrails
- toda nova logica temporal deve referenciar `BolaoTime`;
- evitar `Clock.systemDefaultZone()`, `Calendar.getInstance()` e aritmetica de milissegundos para regras de negocio;
- para persistencia de tempo, garantir compatibilidade entre app e sessao do banco.
