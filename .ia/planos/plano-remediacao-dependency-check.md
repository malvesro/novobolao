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

## Saídas Esperadas
- Matriz de análise das vulnerabilidades por dependência.
- Plano de execução priorizado.
- Dependências atualizadas/testadas ou justificativa documentada para exceções.
- Novo relatório OWASP Dependency-Check “clean” (ou com vulnerabilidades residuais justificadas).
