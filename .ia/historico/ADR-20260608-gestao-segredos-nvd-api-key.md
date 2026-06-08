# ADR-20260608-gestao-segredos-nvd-api-key

**Data:** 2026-06-08
**Status:** Proposto

## Contexto
O plugin OWASP Dependency-Check exige uma `NVD_API_KEY` para evitar lentidão extrema e bloqueios por taxa de uso (rate-limiting) durante a verificação de vulnerabilidades no build. O projeto será publicado no Hugging Face Spaces, que possui mecanismos próprios de segredos.

## Decisão
Adotar o uso de **Docker Build Secrets** apenas como fallback, e remover a execução do **OWASP Dependency-Check** do processo de build do Docker para garantir performance máxima.

### Justificativa Técnica:
1.  **Performance:** A execução do `dependency-check` durante o build do Docker, mesmo com API Key, causa lentidão excessiva devido ao download/processamento da base NVD e problemas de concorrência com arquivos de lock no volume de cache do Maven.
2.  **Separação de Preocupações:** O build do container deve ser otimizado para entrega (deploy). A auditoria de segurança é movida para uma etapa manual ou de CI dedicada.

### Implementação:
- **Local:** Auditoria executada via script `./scripts/run-audit.sh`.
- **Dockerfile:** Removida qualquer chamada ao plugin durante as etapas de build (`mvn verify` ou `mvn package` sem skip).
- **Hugging Face:** Build focado apenas na montagem e execução da aplicação.

## Consequências
- **Positivas:** Build extremamente rápido e previsível.
- **Negativas:** Exige disciplina da equipe para rodar a auditoria manualmente ou configurar um pipeline de CI separado.

## Responsáveis
- Arquiteto de Software Sênior (Time Mercúrio)
