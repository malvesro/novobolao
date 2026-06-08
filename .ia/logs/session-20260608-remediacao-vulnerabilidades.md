# Log de Sessão: Remediação de Vulnerabilidades (Junho 2026)

**Data:** 08/06/2026  
**Responsável:** Arquiteto de Software Sênior (Time Mercúrio)  
**Status:** Concluído

## Objetivos
Resolver as vulnerabilidades críticas (CVSS ≥ 7.0) identificadas pela execução do OWASP Dependency-Check em 08/06/2026.

## Ações Realizadas

### 1. Atualização de Dependências Maven (pom.xml)
Foram atualizadas as seguintes bibliotecas para suas versões mais recentes e seguras identificadas em Junho de 2026:

- **Protobuf Java:** 3.25.5 → **4.35.0** (Corrige CVE-2026-0994)
- **Jakarta Mail (Angus):** 2.0.3 → **2.0.5** (Corrige CVE-2025-7962)
- **Quartz Scheduler:** 2.3.2 → **2.5.1** (Corrige CVE-2023-39017 e alinha com Jakarta EE)
- **Log4j Bridge (log4j-to-slf4j):** 2.24.2 → **2.26.0** (Corrige CVEs de 2026)

### 2. Atualização de Dependências NPM (package.json)
Devido ao fato de as vulnerabilidades residirem em dependências transitivas do `@axe-core/cli`, foi implementada a estratégia de `overrides` no `package.json`:

- **@axe-core/cli:** ^4.11.1 → **^4.11.3**
- **Axios:** ^1.13.5 → **1.17.0** (overridden) - Corrige múltiplos CVEs e ataques de supply chain.
- **Basic-FTP:** ^5.0.2 → **5.3.1** (overridden) - Corrige CVE-2026-44240 (DoS).
- **Follow-Redirects:** 1.15.11 → **1.16.0** (overridden) - Corrige CVE-2026-40895.
- **Tmp:** 0.2.5 → **0.2.6** (overridden) - Corrige GHSA-ph9p-34f9-6g65 (Path Traversal).
- **WS (WebSocket):** 8.19.0 → **8.21.0** (overridden) - Corrige CVE-2026-45736.

## Validação

- **NPM:** Executado `npm ls` confirmando que os overrides foram aplicados corretamente.
- **Maven:** Executado `mvn dependency:tree` confirmando o uso das novas versões.
- **Testes Unitários:** Executado `mvn test -Dfrontend.skip=true`. Todos os 50 testes passaram com sucesso.
- **Integridade:** O sistema continua funcional e as bibliotecas atualizadas são compatíveis com o stack Java 17 / Spring 6 / Struts 7.

## Próximos Passos
- Monitorar novas execuções do Dependency-Check.
- Reavaliar a necessidade de `overrides` conforme as bibliotecas pai atualizarem suas próprias dependências.
- Manter o plano de modernização seguindo o `passo-a-passo.md`.
