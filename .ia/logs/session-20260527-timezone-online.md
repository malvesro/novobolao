# Sessão: Sincronização de Fuso Horário e Refatoração de Presença Online
Data: 2026-05-27

## Objetivos
1. Corrigir o fuso horário da aplicação para Brasília (UTC-3).
2. Refatorar o contador de usuários online para mostrar apenas usuários autenticados, evitando "fantasmas" gerados por robôs ou pings de saúde.

## Alterações Realizadas

### 1. Fuso Horário (Brasília)
- Modificado o **Dockerfile**:
    - Adicionada variável de ambiente `TZ=America/Sao_Paulo`.
    - Adicionado parâmetro `-Duser.timezone=America/Sao_Paulo` nos `CATALINA_OPTS` do Tomcat.
- Documentado o ajuste em `docs/deployment/HUGGING_FACE_AIVEN.md`.

### 2. Contador de Usuários Online (Autenticados)
- **Segurança**:
    - Habilitado `SessionRegistry` no `applicationContext-security.xml`.
    - Adicionado `HttpSessionEventPublisher` no `web.xml`.
- **Infraestrutura**:
    - Criado `SpringContextHolder` para permitir acesso estático a Beans do Spring.
    - Expandido o `component-scan` no `applicationContext-hibernate.xml` para detectar o pacote `util`.
- **Refatoração**:
    - Modificado `ContadorParticipantesOnline.java` para consultar o `SessionRegistry` em vez de incrementar sessões genéricas.
    - Agora, o site mostrará apenas o número de usuários reais logados.

## Resultado Esperado
- Horários de palpites consistentes com o Brasil.
- Contador de usuários online refletindo apenas participantes reais autenticados.
