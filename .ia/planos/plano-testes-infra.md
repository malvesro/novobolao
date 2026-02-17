# Plano de Testes e Infraestrutura (Fase 2)

### [Automated Testing]

#### [MODIFY] [pom.xml](file:///home/rosner/projetosgit/sistema-bolao/pom.xml)
- Adicionar dependências: `junit-jupiter`, `mockito-core`, `spring-test`, `assertj-core`.

#### [NEW] [ParticipanteServiceTest.java](file:///home/rosner/projetosgit/sistema-bolao/src/test/java/com/opendev/bolao/service/ParticipanteServiceTest.java)
- Criar teste de unidade inicial para o serviço de participantes.

## Verification Plan

### Automated Tests
- `mvn test` para garantir que as mudanças no front-end não afetaram o backend (regressão básica).

### Manual Verification
- Validar visualmente os efeitos de Fade e Pulsate no login.
- Validar se o menu lateral continua funcionando (abrir/fechar).
- Verificar erros no console do navegador (F12) para garantir que não há conflitos entre jQuery e Prototype.
- N/A para esta fase, foco em automação.
