# ADR-20260223-aguardar-angus-quartz

**Data:** 2026-02-23
**Status:** Rascunho

## Contexto

O plano de remediação de vulnerabilidades identificou CVEs de alto impacto nas bibliotecas `org.eclipse.angus:jakarta.mail` (e seu módulo `angus-activation`) e `org.quartz-scheduler:quartz`. Tentativas de atualizar para as versões corrigidas (`2.0.4` e `2.5.2`, respectivamente) falharam porque os artefatos ainda não estão disponíveis no repositório corporativo `https://nx-mvn.tse.jus.br` e não podem ser baixados diretamente do Maven Central no ambiente atual. Registros dessas tentativas encontram-se em `.ia/logs/session-20260223-angus-upgrade-parada.md` e `.ia/logs/session-20260223-quartz-upgrade-parada.md`.

## Decisao

Adiar a atualização de Angus Mail/Activation e Quartz até que as versões corrigidas sejam publicadas no repositório corporativo. Enquanto isso:

1. Monitorar periodicamente o Nexus interno (ou solicitar ao time de infra) para sincronizar `jakarta.mail:2.0.4`, `angus-activation:2.0.4` e `quartz:2.5.2`.
2. Manter o `dependency-check` em execução a cada ciclo de build para evidenciar as vulnerabilidades conhecidas e registrar os relatórios.
3. Documentar o bloqueio no `passo-a-passo.md` e no plano de remediação, garantindo rastreabilidade para retomada imediata assim que os artefatos estiverem disponíveis.

## Alternativas Consideradas

1. **Baixar manualmente os JARs e instalá-los via `mvn install:install-file`** – descartado para evitar desalinhamento com o repositório corporativo e riscos de distribuição manual.
2. **Migrar para bibliotecas alternativas (por exemplo, substituir Quartz)** – adiado; implicaria esforço de refatoração significativo sem garantia de aceitação pelo time neste momento.

## Consequencias

- **Positivo:** Mantém o build estável e documenta o risco de forma rastreável até que a atualização seja possível.
- **Negativo:** CVEs permanecem presentes, exigindo monitoramento contínuo e comunicação clara com stakeholders; dependemos da publicação oficial das versões corrigidas.

## Responsaveis

- Time Mercúrio – Assistente Técnico Líder / IA

> NOTE: Renomeie o arquivo e promova para `docs/adr/` quando o bloqueio for resolvido e a decisão definitivo.
