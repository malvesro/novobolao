# Plano: Correções de Versão em Tela, Bandeira da França e Dataset da Copa 2026

**Data:** 2026-02-22  
**Responsável:** Assistente Técnico Líder  
**Contexto:** Após o rebuild, o rodapé exibe placeholders (`${maven.build.timestamp}`), a seleção da França continua com grafia incorreta (“FranÃ§a”) e a base de jogos permanece com dados divergentes da Copa do Mundo FIFA 2026. É necessário organizar as correções de forma sequencial, garantindo rastreabilidade e evitando a execução simultânea de tarefas.

## Objetivos
1. Corrigir a interpolação da versão/timestamp no rodapé, removendo o placeholder e mantendo timezone adequado.
2. Restaurar a grafia/acento da França e validar o asset PNG, garantindo o carregamento correto nas telas autenticadas.
3. Limpar e repopular os dados de equipes/jogos da Copa 2026 no banco, usando o dataset oficial normalizado.

## Etapas Planejadas
### Etapa 1 – Versão no rodapé
- Ajustar `version.properties` para usar a propriedade Maven correta, validar parsing no `BuildInfo`.
- Executar `mvn test`, rebuild Docker (`docker compose build app`) e `docker compose up -d app`.
- Verificar visualmente o rodapé e registrar evidência (tarefa posterior).

### Etapa 2 – França (grafia e bandeira)
- Auditar dados atuais da equipe França diretamente no banco (`EQP_EQUIPE`) e no cache da aplicação.
- Normalizar grafia (UTF-8) e garantir que `flags.properties`/assets estejam sincronizados.
- Executar testes pertinentes (`FlagUtilsTest`) e validar via container.

### Etapa 3 – Dataset Copa 2026
- Planejar limpeza das tabelas (`JOG_JOGO`, `EQP_EQUIPE`, relacionamentos) preservando integridade referencial.
- Ajustar o script `scripts/atualizar_copa2026_dataset.py` (incluir `TRUNCATE` ou `DELETE` quando apropriado).
- Gerar SQL atualizado a partir do Excel, aplicar no banco, rodar smoke (`mvn test`, Docker rebuild se necessário) e produzir evidências.

## Dependências e Riscos
- Aplicação depende do datasource MySQL (já configurado com `connectionCollation=utf8mb4_unicode_ci`).
- Ao limpar os dados da Copa, validar impacto em relatórios e funcionalidades dependentes.
- Garantir execução sequencial: somente avançar para a próxima etapa após concluir e documentar a anterior.

## Entregáveis
- Código/configuração corrigidos (rodapé, assets, script SQL).
- Logs de sessão específicos por etapa.
- Atualizações no `passo-a-passo.md`, evidências visuais e, se necessário, scripts SQL versionados em `data/sql/`.
