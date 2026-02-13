# Baseline de Dependencias (WEB-INF/lib)

Data: 2026-02-13
Fonte: `webapp/WEB-INF/lib/*.jar`

Objetivo: listar jars atuais e classificar impacto na migracao `javax` -> `jakarta`.

Legenda de compatibilidade:
- `javax` = dependencia baseada em `javax.*` (precisa ser substituida por equivalente Jakarta na fase Struts 7/Spring 6).
- `neutra` = nao depende diretamente de `javax.*` (ainda assim deve ser atualizada por seguranca).
- `substituir` = sera removida/substituida por nova tecnologia.
- `verificar` = dependencia com comportamento incerto; validar antes do corte.

## Tabela de dependencias

- `acegi-security.jar` | `substituir` | migrar para Spring Security 6 (Jakarta).
- `activation.jar` | `javax` | substituir por Jakarta Activation.
- `antlr.jar` | `neutra` | usada por Hibernate/OGNL.
- `asm-attrs.jar` | `neutra` | atualizacao necessaria por compatibilidade Java 17.
- `asm.jar` | `neutra` | atualizar junto com Hibernate/Struts.
- `batik.jar` | `verificar` | usada por Cewolf/JFreeChart (pode depender de javax).
- `cewolf.jar` | `verificar` | biblioteca legado de graficos; avaliar substituicao.
- `cglib.jar` | `neutra` | usada por Spring/hibernate legacy; substituir pelas dependencias do stack novo.
- `commons-codec.jar` | `neutra` | atualizar por CVEs.
- `commons-collections.jar` | `neutra` | atualizar por CVEs.
- `commons-dbcp.jar` | `javax` | substituir por DBCP2 (Jakarta) ou pool moderno.
- `commons-fileupload.jar` | `javax` | substituir por FileUpload 2 (Jakarta) se houver upload.
- `commons-lang.jar` | `neutra` | atualizar (commons-lang3).
- `commons-logging.jar` | `neutra` | manter ate troca do logging; avaliar slf4j.
- `commons-pool.jar` | `neutra` | atualizar para commons-pool2.
- `dom4j.jar` | `neutra` | atualizar por compatibilidade.
- `dwr.jar` | `substituir` | remover/ substituir por REST.
- `ehcache.jar` | `neutra` | atualizar para Ehcache 3/JCache.
- `hibernate.jar` | `javax` | migrar para Hibernate 6 (Jakarta).
- `jakarta-oro.jar` | `neutra` | legado do WebWork; revisar necessidade.
- `jcommon.jar` | `neutra` | dependencia do JFreeChart.
- `jfreechart.jar` | `neutra` | atualizar para versao suportada.
- `jsp-api.jar` | `javax` | substituir por Jakarta JSP API (container fornece).
- `jstl.jar` | `javax` | substituir por Jakarta JSTL.
- `jta.jar` | `javax` | substituir por Jakarta Transactions.
- `mail.jar` | `javax` | substituir por Jakarta Mail.
- `mysql-connector.jar` | `neutra` | atualizar para Connector/J 8.x (Java 17).
- `ognl.jar` | `neutra` | atualiza com Struts 7.
- `oscore.jar` | `neutra` | dependencia WebWork; sera removida com Struts 7.
- `quartz.jar` | `neutra` | atualizar para Quartz 2.x/3.x.
- `rife-continuations.jar` | `neutra` | legado WebWork; remover com Struts 7.
- `servlet-api.jar` | `javax` | substituir por Jakarta Servlet API (container fornece).
- `spring.jar` | `javax` | migrar para Spring 6 (Jakarta).
- `standard.jar` | `javax` | parte do JSTL antigo; substituir por Jakarta.
- `webwork.jar` | `substituir` | remover com Struts 7.
- `xwork.jar` | `substituir` | remover com Struts 7.

## Observacoes
- A compatibilidade final depende do corte `javax` -> `jakarta` e do Tomcat alvo (10.1+).
- A lista acima serve como guia inicial; revisar versoes e CVEs antes do upgrade.
