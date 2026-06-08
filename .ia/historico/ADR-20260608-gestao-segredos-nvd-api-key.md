# ADR-20260608-gestao-segredos-nvd-api-key

**Data:** 2026-06-08
**Status:** Proposto

## Contexto
O plugin OWASP Dependency-Check exige uma `NVD_API_KEY` para evitar lentidão extrema e bloqueios por taxa de uso (rate-limiting) durante a verificação de vulnerabilidades no build. O projeto será publicado no Hugging Face Spaces, que possui mecanismos próprios de segredos.

## Decisão
Adotar o uso de **Docker Build Secrets** (`--mount=type=secret`) como o método primário para injetar a chave em tempo de build, tanto localmente quanto na nuvem.

### Justificativa Técnica:
1.  **Segurança:** Segredos montados via `type=secret` não são persistidos nas camadas da imagem Docker. Usar `ENV` ou `ARG` para segredos é desencorajado por deixar rastros no histórico da imagem.
2.  **Transparência:** O build do Hugging Face Spaces suporta nativamente a passagem de segredos configurados na interface para o comando `docker build`.
3.  **Desenvolvimento Local:** O Docker Compose permite mapear arquivos locais para segredos do BuildKit, mantendo o ambiente de dev seguro e isolado.

### Implementação:
- **Local:** Criar arquivo `.nvd_api_key` (ignorado pelo git) e mapeá-lo no `docker-compose.yml`.
- **Hugging Face:** Configurar segredo com nome `NVD_API_KEY` nas configurações do Space.
- **Dockerfile:** Utilizar `--mount=type=secret,id=NVD_API_KEY,env=NVD_API_KEY` nos estágios de build.

## Consequências
- **Positivas:** Build rápido e seguro; chave protegida contra vazamentos acidentais em imagens públicas.
- **Negativas:** Exige que o usuário local crie o arquivo manualmente ou o build será lento (fallback).

## Responsáveis
- Arquiteto de Software Sênior (Time Mercúrio)
