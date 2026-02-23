# Plano: Evolução do `README-migracao.md` (2026)

**Data:** 2026-02-23  
**Responsável:** Assistente Técnico Líder (Time Mercúrio)  
**Objetivo:** Ampliar o `README-migracao.md` para fornecer visão completa de negócio, arquitetura, dados da Copa 2026, operação e orientações ao desenvolvedor, preservando o conteúdo consolidado existente.

## Etapas

1. **Inventário e Análise de Lacunas**
   - Revisar `README-migracao.md`, `README.md`, `analise-inicial.md`, ADRs relevantes e logs recentes (17/02–23/02).
   - Catalogue seções ausentes para públicos de negócio e técnicos (fluxos de negócio, modelos de dados, operação, troubleshooting, governança).

2. **Desenho de Estrutura Expandida**
   - Propor índice organizado contemplando: visão de negócio aprofundada, arquitetura lógica/física, dados Copa 2026, operações (build/deploy/monitoramento), governança/documentação e anexos.
   - Estrutura sugerida:
     1. Resumo Executivo
     2. Domínio e Jornadas de Usuário (personas, papéis, fluxos críticos)
     3. Contexto Copa do Mundo 2026 e Gestão de Dados (formato do torneio, pipeline de datasets, placeholders)
     4. Arquitetura e Stack
        - 4.1 Camadas lógicas e módulos-chave
        - 4.2 Integrações externas (MySQL, Quartz, Angus Mail, scripts de dados)
        - 4.3 Topologia de deploy (Tomcat, Docker, TLS)
     5. Experiência de Desenvolvimento (pré-requisitos, comandos, diretrizes, testes)
     6. Operação e Observabilidade (variáveis, monitoramento, troubleshooting, cron jobs)
     7. Postura de Segurança (controles aplicados, pendências, referências)
     8. Riscos e Pendências
     9. Roadmap e Próximas Ações
    10. Referências e Rastreabilidade
    11. Histórico do Documento
   - Validar se atende stakeholders (negócio, desenvolvimento, operações) e se mantém narrativa didática.

3. **Produção de Conteúdo**
   - Documentar regras de negócio (pontuação, fases, papéis), modelos de dados e integrações.
   - Descrever pipelines de dados FIFA (scripts, calendário), fluxos de build/deploy, monitoração e resolução de incidentes comuns.
   - Acrescentar seção “Guia para Desenvolvedores” com tecnologias, comandos essenciais, boas práticas, referências de ADRs/diretrizes.

4. **Validação Cruzada**
   - Garantir consistência com `passo-a-passo.md`, planos (.ia/planos/), ADRs e logs.
   - Inserir referências cruzadas no README para facilitar rastreabilidade.

5. **Governança e Publicação**
   - Versão anterior: mover para `.ia/documentacao/README-migracao-2026-v1.md` (se necessário).
   - Registrar log de sessão dedicado e atualizar `passo-a-passo.md`.
   - Submeter para revisão com stakeholders (negócio/engenharia) e incorporar feedback.

## Entregáveis
- `README-migracao.md` atualizado no diretório raiz contendo seções expandidas.
- Versão arquivada (se aplicável) no diretório `.ia/documentacao/`.
- Log de sessão documentando execução e fontes consultadas.
