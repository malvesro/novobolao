# Analise de Risco de Dependencias (CVE / Idade)

Data: 2026-02-13
Fonte: `webapp/WEB-INF/lib/*.jar` + manifestos (versoes)

## 1. Metodo
- Extraido `Implementation-Version`, `Specification-Version` ou `Bundle-Version` dos JARs.
- Classificacao por risco baseada em CVEs publicas e idade do componente.
- Foco nos componentes mais criticos para seguranca e na compatibilidade com Jakarta.

## 2. Riscos Criticos (troca obrigatoria)

1. **commons-fileupload.jar (1.1)**
   - Afetado por **CVE-2014-0050** (DoS por loop infinito em `MultipartStream`).
   - Afetado por **CVE-2016-3092** (DoS por boundary longo).
   - A pagina de seguranca do FileUpload indica correcoes em versoes posteriores e novos riscos (ex: CVE-2023-24998).
   - **Acao:** substituir por FileUpload atualizado (Jakarta) antes da migracao para Struts 7/Spring 6.

2. **commons-collections.jar (3.1)**
   - Biblioteca historicamente explorada em cadeias de deserializacao insegura.
   - O proprio projeto reconhece impacto em series 3.x e recomenda mitigacao.
   - **Acao:** substituir por versao moderna (ou remover do classpath se nao usada).

## 3. Riscos Altos (obsolescencia e compatibilidade)

- **acegi-security.jar (1.0.0-RC2)**: legado, substituido por Spring Security; nao compativel com Jakarta.
- **spring.jar (1.2.8)**: extremamente antigo; precisa migrar para Spring 6.
- **webwork.jar / xwork.jar**: legado, substituido por Struts 7.
- **jsp-api.jar / servlet-api.jar / jstl.jar / standard.jar / jta.jar / mail.jar / activation.jar**: stack `javax.*` antigo; precisa migrar para Jakarta.
- **mysql-connector.jar (3.0)**: muito antigo; atualizar para Connector/J 8.x (Java 17).

## 4. Riscos Medios (idade elevada)

- **commons-lang 2.1** -> migrar para commons-lang3.
- **commons-dbcp (1.x)** -> migrar para commons-dbcp2.
- **commons-pool (1.x)** -> migrar para commons-pool2.
- **dom4j 1.6.1**, **quartz 1.5.1**, **ehcache**: atualizar para versoes suportadas.
- **jfreechart 1.0.0 / jcommon 1.0.0 / cewolf 1.0**: stack legado de graficos, revisar compatibilidade com Java 17.

## 5. Observacoes importantes
- Varias dependencias nao expuseram versao no manifesto; confirmar por checksum ou inspecao manual quando necessario.
- A migracao para Struts 7/Spring 6 exige o corte `javax.*` -> `jakarta.*` e nao pode coexistir com jars antigos de servlet/jsp/jstl.

## 6. Referencias
- Apache Commons FileUpload Security Reports (CVE-2014-0050, CVE-2016-3092, CVE-2023-24998)
- NVD CVE-2014-0050
- Apache Commons Collections Security Reports
- CERT VU#576313 (deserializacao insegura)
