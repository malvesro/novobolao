# Plano de Migracao Passo a Passo (Sistema Bolao)

Referencias:
- `README.md`
- `.ia/historico/ADR-20260213-estrategia-migracao-stack.md`
- `.ia/diretrizes/arquitetura.md`

Legenda de status:
- `Pendente`
- `Em progresso`
- `Concluido`
- `Bloqueado`

Diretriz fixa:
- Manter empacotamento WAR e deploy em Tomcat mais atual. Nao usar Spring Boot.

Premissas de compatibilidade (criticas):
- Struts 7 exige Java 17+ e Jakarta Servlet 6+ (jakarta.*). citeturn0search0turn0search3
- Spring Framework 6 exige Java 17+ e Jakarta EE 9+ (jakarta.*). citeturn0search5turn0search1
- Hibernate 6 usa Jakarta Persistence (JPA 3.1), logo requer stack Jakarta. citeturn0search2
- Consequencia: a virada para Struts 7 + Spring 6 precisa ser tratada como um corte unico de `javax.*` -> `jakarta.*` em codigo, dependencias e container.

## Atividades numeradas (executar em sequencia)

1. [Concluido] Criar `docs/adr/` e formalizar o ADR final em `docs/adr/ADR-20260213-estrategia-migracao-stack.md` a partir do rascunho em `.ia/historico/`.
2. [Concluido] Congelar baseline funcional: listar Actions (`src/xwork.xml`), endpoints DWR (`webapp/WEB-INF/dwr.xml`), JSPs (`webapp/`) e fluxos criticos.
3. [Concluido] Inventariar dependencias atuais (jars em `webapp/WEB-INF/lib/`) e classificar por compatibilidade `javax` vs `jakarta`.
4. [Concluido] Criar `.gitignore` para ignorar arquivos desnecessarios (ex: `Thumbs.db`, `*:Zone.Identifier`, caches de IDE e artefatos de build).
5. [Concluido] Rodar analise de risco de bibliotecas (CVE/idade) e documentar trocas obrigatorias (DWR, Acegi, JSTL/taglibs, libs JS antigas).
6. [Concluido] Definir e registrar no ADR a matriz de versoes alvo: Java 17, Tomcat 10.1, Struts 7, Spring 6, Hibernate 6, Spring Security 6 (avaliar Tomcat 11 apos estabilizacao).
7. [Concluido] Criar build reproduzivel (Maven ou Gradle) com empacotamento WAR, mantendo `webapp/` e `src/` (pom base).
8. [Concluido] Mapear jars atuais para coordenadas Maven oficiais (groupId/artifactId/version), incluindo equivalentes Jakarta quando aplicavel.
9. [Concluido] Atualizar `pom.xml` com dependencias reais (sem `systemPath`) **com base em** `docs/mapeamento-dependencias-maven.md`, definindo escopos corretos (ex: `provided` para servlet/jsp, `runtime` para JDBC).
10. [Concluido] Configurar repositorio Maven **somente via Nexus corporativo** e padronizar `maven-compiler-plugin` (source/target alinhados ao Java de build) e encoding.
11. [Em progresso] Substituir dependencias bloqueadas no Nexus por versoes aprovadas (mantendo `javax` enquanto necessario). **Para dependencias sem versao aprovada**, registrar ADR de substituicao por tecnologia atual (ex: WebWork/XWork -> Struts 7, Acegi -> Spring Security 6, Cewolf/JFreeChart -> Chart.js, DWR -> REST). Base: `docs/substituicao-dependencias-nexus.md`.
12. [Pendente] Atualizar o `pom.xml` com as substituicoes aprovadas no Nexus e remover dependencias bloqueadas.
13. [Pendente] Definir comandos padrao de build e testes (ex: `clean`, `compile`, `test`, `war`) e validar que o WAR sobe no Tomcat atual.
14. [Pendente] Externalizar configuracoes sensiveis (DB/SMTP) com override por ambiente e fallback local seguro.
15. [Pendente] Criar checklist de smoke test manual e, se possivel, 2 ou 3 testes automatizados de fluxo critico.

16. [Pendente] Remover ou substituir dependencias nao compativeis com Jakarta antes do corte (DWR, Acegi, JSTL/Taglibs antigas, WebWork/XWork).
17. [Pendente] Preparar conversao `javax.*` -> `jakarta.*` no codigo e configs (classes, JSPs, `web.xml`, `applicationContext-*.xml`).
18. [Pendente] Atualizar runtime para Java 17 e Tomcat 10.1+ (ambiente paralelo) e validar deploy do WAR vazio.
19. [Pendente] Rodar build completo (incluindo testes) e registrar baseline de erros/avisos antes do corte Jakarta.

20. [Pendente] Introduzir Struts 7 e configurar filtro/servlet no `web.xml`, mantendo rotas atuais.
21. [Pendente] Migrar `xwork.xml` para `struts.xml` mantendo namespaces e resultados (JSPs existentes).
22. [Pendente] Ajustar Actions para Struts 7 sem mudar contratos publicos.
23. [Pendente] Ajustar taglibs/JSTL nas JSPs para compatibilidade Jakarta.
24. [Pendente] Migrar Acegi para Spring Security 6 com estrategia de rehash de senha (SHA-1 legado -> BCrypt).
25. [Pendente] Atualizar Spring para 6 (com Jakarta) e validar injecoes/transactions por XML.
26. [Pendente] Atualizar Hibernate 3 para Hibernate 6 mantendo HBM inicialmente; converter para JPA annotations apenas quando estabilizado.
27. [Pendente] Substituir Cewolf/JFreeChart por biblioteca de graficos atual (ex: Chart.js) e ajustar JSPs.
28. [Pendente] Configurar e executar OWASP Dependency-Check Maven Plugin apos atualizar bibliotecas para versoes atuais.
29. [Pendente] Executar build completo e smoke tests; comparar com baseline funcional (passo 15).
30. [Pendente] Adicionar teste de regressao minimo para login e palpite (automatizado ou roteiro manual repetivel).

31. [Pendente] Substituir DWR por endpoints REST equivalentes, um fluxo por vez (quando aplicavel).
32. [Pendente] Modernizar UI por modulos (mantendo JSPs onde necessario) e remover libs JS legadas gradualmente.
33. [Pendente] Encerrar legado (remover WebWork, Acegi, DWR e libs obsoletas) apos cobertura dos fluxos equivalentes.

## Registro de avancos

- 2026-02-13: Plano criado. Nenhuma atividade iniciada.
- 2026-02-13: Tarefa 1 concluida (ADR formalizado em `docs/adr/ADR-20260213-estrategia-migracao-stack.md`).
- 2026-02-13: Tarefa 2 concluida (baseline em `docs/baseline-rotas-fluxos.md`).
- 2026-02-13: Tarefa 3 concluida (baseline em `docs/baseline-dependencias.md`).
- 2026-02-13: Tarefa 4 concluida (criacao do `.gitignore`).
- 2026-02-13: Tarefa 5 concluida (analise em `docs/analise-risco-dependencias.md`).
- 2026-02-13: Tarefa 6 concluida (matriz de versoes registrada no ADR).
- 2026-02-13: Tarefa 7 concluida (build Maven em `pom.xml`).
- 2026-02-13: Tarefa 8 concluida (mapeamento em `docs/mapeamento-dependencias-maven.md`).
- 2026-02-13: Tarefa 9 concluida (pom atualizado com dependencias reais).
- 2026-02-13: Tarefa 11 iniciada (analise de substituicao no Nexus em `docs/substituicao-dependencias-nexus.md`).
- 2026-02-13: Tarefa 10 concluida (repositorio Nexus e compiler/encoding no pom).
