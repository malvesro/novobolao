# Mapeamento de Dependencias para Maven

Data: 2026-02-13
Fonte: `webapp/WEB-INF/lib/*.jar`

Objetivo: mapear cada JAR legado para coordenadas Maven oficiais (ou equivalentes) para permitir build com dependencias reais, evitando `systemPath`.

## 1. Mapeamento principal (legado -> Maven)

- **acegi-security.jar** -> `org.acegisecurity:acegi-security:1.0.0` (legado; substituir por Spring Security 6 no futuro)
- **activation.jar** -> `com.sun.activation:jakarta.activation:2.0.1`
- **antlr.jar** -> `antlr:antlr:2.7.7`
- **asm-attrs.jar** -> `asm:asm-attrs:1.5.3` (legado; remover com stack moderno)
- **asm.jar** -> `asm:asm:1.5.3` (legado; remover com stack moderno)
- **batik.jar** -> `org.apache.xmlgraphics:batik-all:1.7` (legado; validar compatibilidade)
- **cewolf.jar** -> `cewolf:cewolf:1.0` (legado; avaliar substituicao)
- **cglib.jar** -> `cglib:cglib:2.2` (legado; substituir com Spring 6)
- **commons-codec.jar** -> `commons-codec:commons-codec:1.3`
- **commons-collections.jar** -> `commons-collections:commons-collections:3.1`
- **commons-dbcp.jar** -> `commons-dbcp:commons-dbcp:1.2.2`
- **commons-fileupload.jar** -> `commons-fileupload:commons-fileupload:1.1`
- **commons-lang.jar** -> `commons-lang:commons-lang:2.1`
- **commons-logging.jar** -> `commons-logging:commons-logging:1.0`
- **commons-pool.jar** -> `commons-pool:commons-pool:1.3`
- **dom4j.jar** -> `dom4j:dom4j:1.6.1`
- **dwr.jar** -> `org.directwebremoting:dwr:2.0.1` (legado; substituir por REST)
- **ehcache.jar** -> `net.sf.ehcache:ehcache:1.2.3`
- **hibernate.jar** -> `org.hibernate:hibernate3:3.2.6.ga`
- **jakarta-oro.jar** -> `oro:oro:2.0.8`
- **jcommon.jar** -> `org.jfree:jcommon:1.0.0`
- **jfreechart.jar** -> `org.jfree:jfreechart:1.0.0`
- **jsp-api.jar** -> `javax.servlet.jsp:jsp-api:2.0` (escopo provided)
- **jstl.jar** -> `javax.servlet:jstl:1.1.2`
- **jta.jar** -> `javax.transaction:jta:1.0.1B`
- **mail.jar** -> `javax.mail:mail:1.3.1`
- **mysql-connector.jar** -> `mysql:mysql-connector-java:3.0.17`
- **ognl.jar** -> `ognl:ognl:2.6.9`
- **oscore.jar** -> `opensymphony:oscore:2.2.5`
- **quartz.jar** -> `org.quartz-scheduler:quartz:1.5.1`
- **rife-continuations.jar** -> `com.uwyn.rife:rife-continuations:1.0`
- **servlet-api.jar** -> `javax.servlet:servlet-api:2.4` (escopo provided)
- **spring.jar** -> `org.springframework:spring:1.2.8`
- **standard.jar** -> `taglibs:standard:1.1.2`
- **webwork.jar** -> `com.opensymphony:webwork:2.2.2`
- **xwork.jar** -> `com.opensymphony:xwork:1.1.3`

## 2. Observacoes e riscos

- As versoes acima refletem o legado; sao propositalmente antigas para reproduzir o build antes da migracao.
- Dependencias `javax.*` devem ficar com escopo `provided` quando o container fornece.
- Para a fase Jakarta, todas essas coordenadas precisarao de troca.
- Alguns artefatos podem nao existir no Maven Central atual; validar repositorio interno (Nexus) e espelhar se necessario.

## 3. Proximos passos recomendados

- Atualizar `pom.xml` com as dependencias mapeadas (passo 9 do plano).
- Definir repositorios (Nexus/central) no `pom.xml` ou `settings.xml`.
