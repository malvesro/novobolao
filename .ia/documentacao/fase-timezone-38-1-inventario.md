# Inventario Tecnico de Timezone por Camada (Tarefa 38.1)

**Data:** 11/06/2026  
**Escopo:** Auditoria de aderencia ao fuso oficial do Brasil (America/Sao_Paulo)  
**Dominio:** Bolao da Copa 2026 (janelas de palpite, jobs e auditoria temporal)

## 1. Criterios de classificacao

- **Conforme:** usa `America/Sao_Paulo` de forma explicita ou via abstração canônica (`BolaoTime`) sem dependência do host.
- **Parcial:** depende de timezone default do runtime (funciona no ambiente atual, mas com risco de deriva em outro runtime).
- **Risco:** possui lógica temporal com potencial de comportamento incorreto em mudança de ambiente/configuração (host/JVM/DB) ou técnica frágil para cálculo de datas.

## 2. Matriz por camada

| Camada | Evidencias | Classificacao | Risco principal |
|---|---|---|---|
| Model (Regra de jogo/palpite) | `Jogo` usa `BolaoTime.getZoneId()` e `ZonedDateTime.now(ZONE_ID)` | **Conforme** | Baixo |
| Infra Web Runtime | `TimeZoneInitializer` seta `TimeZone.setDefault(BolaoTime.getTimeZone())` e listener no `web.xml` | **Conforme** | Baixo |
| Conversoes utilitarias | `ConversaoUtils` configura `DATE_FORMAT`/`TIME_FORMAT` com `BolaoTime.getTimeZone()` | **Conforme** | Baixo |
| Build/Versionamento | `pom.xml` com `build-helper-maven-plugin` e `timeZone=America/Sao_Paulo` | **Conforme** | Baixo |
| Runtime Docker App | `Dockerfile` com `TZ=America/Sao_Paulo` e `-Duser.timezone=America/Sao_Paulo` | **Conforme** | Baixo |
| Scheduler Quartz | Triggers sem propriedade `timeZone` em `CronTriggerFactoryBean` | **Parcial** | Medio |
| Service de autorizacao de palpite | `PalpiteAuthorizationServiceImpl` default em `Clock.systemDefaultZone()` | **Risco** | Alto |
| Action (paginacao de datas) | `ParticipanteAction` usa `new Date()` e soma fixa `+86400000` | **Risco** | Alto |
| Service de avisos de jogos | `JogoServiceImpl` usa `Calendar.getInstance()` e `new Date()` | **Parcial** | Medio |
| Persistencia JDBC/MySQL | JDBC sem `connectionTimeZone`; schema com `TIMESTAMP DEFAULT CURRENT_TIMESTAMP` | **Risco** | Alto |
| Servicos auxiliares (erro/chat/email) | `LocalDateTime.now()` / `new Date()` sem zone explicita | **Parcial** | Medio |

## 3. Evidencias detalhadas (arquivos)

### 3.1 Conforme
- `src/com/opendev/bolao/util/BolaoTime.java`
- `src/com/opendev/bolao/infrastructure/TimeZoneInitializer.java`
- `webapp/WEB-INF/web.xml` (listener `TimeZoneInitializer`)
- `src/com/opendev/bolao/model/Jogo.java`
- `src/com/opendev/bolao/util/ConversaoUtils.java`
- `pom.xml` (plugin de timestamp com timezone de Sao Paulo)
- `Dockerfile` (`TZ` e `-Duser.timezone`)

### 3.2 Parcial
- `src/main/resources/applicationContext-scheduler.xml` (cron sem `timeZone` explicito)
- `src/com/opendev/bolao/service/impl/JogoServiceImpl.java` (`Calendar.getInstance()`)
- `src/com/opendev/bolao/service/ErrorNotificationService.java` (`LocalDateTime.now()`)
- `src/com/opendev/bolao/chat/BatePapo.java` (`new Date()`/`SimpleDateFormat`)

### 3.3 Risco
- `src/com/opendev/bolao/service/impl/PalpiteAuthorizationServiceImpl.java` (`Clock.systemDefaultZone()`)
- `src/com/opendev/bolao/action/ParticipanteAction.java` (`+86400000` em `buscarMaisJogosHtmx`)
- `src/main/resources/applicationContext-resources.xml` (JDBC sem `connectionTimeZone`)
- `src/main/resources/database/schema.sql` e `docker/mysql/init/01-schema.sql` (`TIMESTAMP ... CURRENT_TIMESTAMP`)

## 4. Priorizacao tecnica para as proximas iteracoes

1. **Critico (execucao imediata):**
   - 38.3: Padronizar `PalpiteAuthorizationServiceImpl` para usar zona canônica do dominio (`BolaoTime`).
   - 38.4: Remover aritmetica de milissegundos (`+86400000`) no fluxo de datas.
2. **Alto impacto operacional:**
   - 38.2: Definir timezone explicito em todos os triggers Quartz.
   - 38.6: Alinhar JDBC/MySQL para timezone explicito em conexao e runtime do banco.
3. **Consistencia complementar:**
   - 38.5/38.7: Normalizar servicos auxiliares e contratos de exibicao.
   - 38.8/38.9/38.10: Cobertura de testes, evidencias operacionais e ADR.

## 5. Conclusao da Subtarefa 38.1

Inventario concluido com rastreabilidade por camada e matriz de risco. O sistema ja possui base solida de timezone canônico em partes criticas, mas ainda depende de timezone implícito em componentes de autorizacao, scheduler e persistencia, o que pode causar deriva temporal em cenarios multiambiente.
