# ADR-20260608-otimizacao-build-docker-multi-stage

**Data:** 2026-06-08
**Status:** Proposto

## Contexto
O processo atual de build via Docker é ineficiente. O `Dockerfile` executa o build do Frontend (Vite) de forma acoplada ao build do Backend (Maven). 
Isso causa os seguintes problemas:
1.  **Invalidação de Cache:** Qualquer alteração no código Java (`src/`) invalida a camada do Docker e força a reexecução completa do `npm install` e `npm build`, mesmo que não existam mudanças no frontend.
2.  **Redundância de Downloads:** Sem persistência do repositório local do Maven (`.m2`) e do cache do NPM entre as execuções do Docker Build, as dependências são verificadas ou baixadas repetidamente.
3.  **Tempo de Ciclo (Inner Loop):** O desenvolvedor espera vários minutos para testar uma mudança simples devido ao re-build completo do frontend.

## Decisão
Adotar uma estratégia de **Build Multi-stage Otimizado** e **Cache Mounts (BuildKit)** para separar as preocupações e acelerar o ciclo de feedback.

### Detalhes Técnicos:
1.  **Estágios Granulares:**
    *   **Stage 1 (frontend-builder):** Container Node.js dedicado exclusivamente ao `npm install` e `npm build`. Só é reexecutado se `package.json`, `vite.config.js` ou arquivos de assets mudarem.
    *   **Stage 2 (deps):** Container Maven para baixar dependências (`go-offline`).
    *   **Stage 3 (builder):** Container Maven que recebe os assets compilados do Stage 1 e compila o código Java, gerando o WAR final. Utiliza a flag `-Dfrontend.skip=true` para evitar re-build redundante do frontend via plugin Maven.
    *   **Stage 4 (runtime):** Imagem final (Tomcat) contendo apenas o WAR gerado.
2.  **BuildKit Cache Mounts:**
    *   Utilizar `--mount=type=cache,target=/root/.m2` para o Maven.
    *   Utilizar `--mount=type=cache,target=/root/.npm` para o Node.
3.  **Otimização do Contexto:** Configurar um `.dockerignore` rigoroso para evitar o envio de arquivos desnecessários (como `node_modules` e `target` locais) para o daemon do Docker.

## Consequências
*   **Positivas:** 
    *   Redução drástica no tempo de build incremental (mudanças apenas em Java ou apenas em Frontend).
    *   Uso mais eficiente de banda e disco.
    *   Separação clara entre as stacks de build (Node vs Java).
*   **Negativas:**
    *   Exige Docker BuildKit habilitado (padrão nas versões recentes do Docker).
    *   A sintaxe do Dockerfile torna-se ligeiramente mais complexa.

## Alternativas Consideradas
*   **Manter Build Acoplado:** Descartado devido à perda de produtividade.
*   **Build Externo (Script de CI):** Descartado para manter a filosofia de "Build reproducible dentro do container".

## Responsáveis
*   Arquiteto de Software Sênior (Time Mercúrio)
