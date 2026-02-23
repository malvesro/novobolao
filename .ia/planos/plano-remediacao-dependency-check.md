# Plano Emergencial: Remediação das Vulnerabilidades (Dependency-Check)

**Data:** 2026-02-22  
**Responsável:** Assistente Técnico Líder  
**Prioridade:** Alta (CVSS ≥ 7.0 detectados na execução `mvn -Dfrontend.skip=true org.owasp:dependency-check-maven:check`)

## Objetivo
avaliar e endereçar as vulnerabilidades críticas reportadas para as dependências:

- Angus / Jakarta Mail (2.0.2/2.0.3) – CVE-2025-7962 (confirmação necessária, CVSS 6.0)
- Commons FileUpload 2.0.0-M2 – CVE-2025-48976 (7.5)
- JFreeChart 1.5.4 – CVE-2023-52070, CVE-2024-23076, CVE-2024-22949, CVE-2024-23077 (até 9.1)
- Protobuf Java 3.25.1 – CVE-2024-7254 (8.7)
- Quartz 2.3.2 – CVE-2023-39017 (9.8)
- Spring Framework 6.1.4 (core/web) – CVE-2024-22259 (8.1)
- Struts 7.0.0 – CVE-2025-64775, CVE-2025-66675 (até 8.2)

## Etapas

1. **Coleta e Validação do Relatório**
   - Localizar (ou gerar novamente) o relatório completo do Dependency-Check (HTML/XML).
   - Validar se há falsos positivos/mapeamentos incorretos (ex.: CPE duplicado para Angus/Jakarta Mail).
   - Registrar hash e localização do relatório para rastreabilidade.

2. **Análise Individual das Dependências**
   - Para cada biblioteca listada:
     - Confirmar versão disponível no `pom.xml`.
     - Consultar changelog oficial / CVE para identificar versões corrigidas.
     - Avaliar compatibilidade com o stack atual (Java 17, Spring 6, Struts 7).
     - Determinar se substituição, remoção ou mitigação é viável (ex.: migrar de Angus para Jakarta Mail 2.1+, substituir Commons FileUpload por Jakarta Servlet multipart, etc.).

3. **Definição da Estratégia de Remediação**
   - Elaborar matriz (biblioteca → ação proposta → impacto → esforço → riscos).
   - Priorizar atualizações críticas (CVSS ≥ 8.0) e dependências transitivas antes das demais.
   - Identificar dependências provenientes de third-party transitive (pom chain) para evitar upgrades manuais desnecessários.

4. **Plano de Execução**
   - Sequenciar as atualizações em subtarefas específicas (ex.: “Atualizar JFreeChart para 1.5.5”).
   - Determinar testes obrigatórios por componente (unitários, integração, smoke UI).
   - Avaliar necessidade de ADR/documentação para upgrades estruturais (ex.: Struts patch level).

5. **Execução e Validação**
   - Atualizar `pom.xml`/lockfiles conforme plano.
   - Rodar `mvn -Dfrontend.skip=true test` e `dependency-check:check` pós-upgrade.
   - Registrar logs de sessão por conjunto de dependências.

6. **Encerramento**
   - Atualizar `README-migracao.md` (quando disponível) com a situação das bibliotecas.
   - Registrar resumo final em `.ia/logs/` incluindo versões finais e data.
   - Avaliar backlog para monitorar vulnerabilidades remanescentes ou mitigadas.

## Matriz de Remediação (Atualizado em 23/02/2026)

| Dependência | Versão atual | Ação proposta | Testes obrigatórios | Observações |
| --- | --- | --- | --- | --- |
| Struts 7.0.0 + `commons-fileupload2` 2.0.0-M2 | 7.0.0 / 2.0.0-M2 | Atualizar Struts para 7.1.1 e forçar `commons-fileupload2` ≥ 2.0.0-M4 via `dependencyManagement` | `mvn test`, smoke `/admin/*.action` | CVEs 2025-66675/64775; revisar upload multipart após upgrade |
| Spring Framework 6.1.4 (core/web) | 6.1.4 | Subir para 6.1.14 (release de segurança) mantendo compatibilidade com Spring Security 6.2.2 | `mvn test`, smoke login/logout | Cobre CVE-2024-22259 e CVE-2024-38820 |
| Angus Mail / Activation | 2.0.3 / 2.0.2 | Atualizar todos os módulos para 2.0.4; revalidar envio de e-mails | `MailServiceTest`, smoke cadastro | CVE-2025-7962; alinhar configurações SMTP |

> **Status 23/02/2026:** Artefatos `org.eclipse.angus:jakarta.mail:2.0.4` ainda não disponíveis no repositório `nx-mvn.tse.jus.br`; reavaliar assim que publicados.
| Quartz Scheduler | 2.3.2 | Atualizar para 2.5.2 (Jakarta) ou remover módulos não usados (`quartz-jobs`) | Testes de agendamento (`ProcessadorPalpitesJob`) | CVE-2023-39017; avaliar dependência de `c3p0` legada |
> **Status 23/02/2026:** Artefato `org.quartz-scheduler:quartz:2.5.2` ainda não disponível no `nx-mvn.tse.jus.br`; manter 2.3.2 até sincronização.
| JFreeChart | 1.5.4 | Migrar para 1.5.6 e revisar geração de PNGs | `GraficosJFreeChartTest`, smoke dashboards | CVEs contestados; upgrade reduz alertas falsos |
| Protobuf Java | 3.25.1 (transitivo) | Fixar `protobuf-java` 3.25.5 no `pom.xml` | `mvn test` | Dependência trazida pelo MySQL Connector 8.3.0 |
| Log4j API | 2.24.2 (transitivo Struts 7.1.1) | Atualizar para 2.25.3 via `dependencyManagement` | `mvn test`, smoke logs | CVE-2025-68161 (score 4.8; acompanhar classificações futuras) |
| esbuild (devDependency) | 0.21.5 | Subir para ≥ 0.25.0 e reconstruir bundles | `npm run build`, `mvn package` | Requer Node 18+ (já atendido) |
| Commons Lang | 3.14.0 | Atualizar para 3.18.0 | `mvn test` | CVE-2025-48924 (média) |
| IH outros (monitorar) | — | Manter `dependency-check` após upgrades para garantir limpeza | `dependency-check:check` | Registrar relatório final |

## Próximas Ações Planejadas

1. **Planejamento e aprovação dos upgrades críticos (Struts, Spring, Angus) com avaliação de compatibilidade e impacto.**
2. **Execução sequencial das atualizações**, começando por Struts/FileUpload (bloqueia CVEs de maior risco na camada web), seguida de Spring Framework e dependências associadas.
3. **Atualizações complementares** (Quartz, Protobuf, JFreeChart, Log4j, Commons Lang, esbuild) agrupadas em lotes com testes dedicados.
4. **Reexecução do Dependency-Check**, arquivando relatórios e documentando exceções justificadas.

### Plano Detalhado – Spring Framework 6.1.14

1. Atualizar `pom.xml`:
   - Ajustar propriedade `<spring.version>` para `6.1.14`.
   - Confirmar que o BOM `spring-framework-bom` é resolvido pelo novo valor (garante versões coerentes para `spring-*`, `micrometer-*`, `spring-test`).
2. Validar compatibilidade:
   - Verificar se `spring-security` 6.2.2 permanece compatível (revisar changelog de breaking changes; ajustar se necessário).
   - Garantir que o plugin `struts2-spring-plugin` 7.1.1 continue alinhado (nenhuma ação adicional esperada).
3. Testar e observar:
   - Executar `mvn -q -Dfrontend.skip=true test`.
   - Realizar smoke login/logout e fluxo `/admin/*.action`.
4. Segurança:
   - Reexecutar `mvn -Dfrontend.skip=true org.owasp:dependency-check-maven:check` para confirmar resolução do CVE-2024-22259/38820.
5. Registrar:
   - Atualizar `passo-a-passo.md` e criar log de sessão com evidências dos testes.

## Saídas Esperadas
- Matriz de análise das vulnerabilidades por dependência.
- Plano de execução priorizado.
- Dependências atualizadas/testadas ou justificativa documentada para exceções.
- Novo relatório OWASP Dependency-Check “clean” (ou com vulnerabilidades residuais justificadas).
