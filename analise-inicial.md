# Análise Inicial Obrigatória do Projeto (Deep Project Review)

Este documento detalha os passos da análise inicial profunda do projeto, conforme as diretrizes estabelecidas em `AGENTS.md`. O objetivo é fornecer um relatório estruturado sobre o inventário tecnológico, arquitetura, funcionalidades, qualidade, segurança, débitos técnicos e riscos.

## Status da Análise

- **Análise Inicial Completa:** PENDENTE (Será atualizado para "EM ANDAMENTO", "CONCLUÍDO" ou "CANCELADO")
- **Data de Início:** 17/02/2026 (Será atualizado para a data de conclusão)

---

## Passos da Análise

### 1. Visão Geral do Projeto e Tecnologias Primárias

**Objetivo:** Entender a estrutura do projeto e identificar as tecnologias principais (linguagem, frameworks, sistema de build, etc.).

**Ações:**
- Analisar `pom.xml` (se existir) e `build.xml` para identificar o sistema de build e dependências.
- Examinar a estrutura de diretórios (`src/`, `webapp/`, `WEB-INF/lib/`) para inferir a linguagem principal (Java), frameworks web (JSP/Servlets) e possíveis frameworks MVC (Struts/WebWork, dado `xwork.xml`).
- Identificar tecnologias de runtime (servidor de aplicação).

**Resultados:**
- **Sistema de Build:** Principalmente Maven, com `pom.xml` definindo o build principal e gerenciamento de dependências. Ant (`build.xml`) é utilizado para o empacotamento final do WAR.
- **Linguagem:** Java 1.8.
- **Empacotamento:** `war` (Web Archive), indicando uma aplicação web.
- **Framework Web (MVC):** WebWork 2.2.2 (baseado em XWork 1.1.3), JSP/Servlet 2.4, JSTL 1.1.2.
- **Framework de Segurança:** Acegi Security 1.0.0 (predecessor do Spring Security).
- **Persistência/ORM:** Hibernate 3.2.6.ga.
- **Banco de Dados:** MySQL (via `mysql-connector-java` 3.0.17).
- **Injeção de Dependências/IoC:** Spring Framework 1.2.8.
- **Pool de Conexões:** Commons DBCP 1.2.2 e Commons Pool 1.3.
- **Outras Bibliotecas Notáveis:**
    - DWR (Direct Web Remoting) 2.0.1 (para funcionalidades AJAX).
    - Quartz Scheduler 1.5.1 (para agendamento de tarefas).
    - EHCache 1.2.3 (para cache).
    - Cewolf 1.0 e JFreeChart 1.0.0 (para geração de gráficos).
    - Batik 1.7 (para manipulação de SVG).

**Conclusão Parcial:** Todas as tecnologias identificadas são **muito antigas e, em grande parte, sem suporte oficial**. Isso indica um débito técnico significativo e expõe a aplicação a diversos riscos de segurança e performance.

**Status:** CONCLUÍDO

### 2. Identificação de Arquivos de Configuração Chave

**Objetivo:** Localizar e compreender os principais arquivos de configuração que ditam o comportamento da aplicação.

**Ações:**
- Analisar arquivos `applicationContext-*.xml` (ex: `applicationContext-action.xml`, `applicationContext-hibernate.xml`, `applicationContext-security.xml`) para entender a configuração do Spring/IoC, persistência e segurança.
- Revisar `web.xml` (em `WEB-INF/`) para mapeamento de servlets, filtros, ouvintes e configurações de segurança web.
- Examinar `hibernate.xml` (ou configurações equivalentes dentro dos `applicationContext-hibernate.xml`) para detalhes de persistência.

**Resultados:**
- **`web.xml` (`webapp/WEB-INF/web.xml`):**
    - **Versão da Especificação Servlet:** 2.4.
    - **Configuração do Contexto Spring:** Carrega explicitamente os seguintes arquivos de configuração do Spring:
        - `classpath*:applicationContext-resources.xml`
        - `classpath*:applicationContext-hibernate.xml`
        - `classpath*:applicationContext-security.xml`
        - `classpath*:applicationContext-service.xml`
        - `classpath*:applicationContext-action.xml`
        - `classpath*:applicationContext-scheduler.xml`
    - **Filtros:**
        - `OpenSessionInViewFilter` (Spring/Hibernate): Gerencia a sessão do Hibernate na camada de visão.
        - `WebWorkFilter` (WebWork): Filtro principal do framework MVC, processa todas as requisições.
        - `SecurityFilter` (Acegi Security): Gerencia a segurança (autenticação/autorização) para diversas URLs e tipos de recursos (`.action`, `.jsp`, `.html`, `/dwr/*`).
    - **Listeners:**
        - `ContextLoaderListener` (Spring): Carrega o contexto da aplicação Spring.
        - `ContadorParticipantesOnline` (customizado): Um listener customizado para contagem de participantes online.
    - **Servlets:**
        - `webwork` (WebWork): Servlet principal do WebWork, mapeado para `*.action`.
        - `dwr-invoker` (DWR): Servlet para o Direct Web Remoting, mapeado para `/dwr/*` e com `debug` habilitado.
        - `CewolfServlet` (Cewolf): Servlet para renderização de gráficos, mapeado para `/cewolf/*`.
    - **Configuração de Sessão:** `session-timeout` configurado para 30 minutos.
    - **Página de Boas-Vindas:** `index.jsp`.
    - **Configuração JSP:** Define várias Taglibs (Acegi Security, WebWork, JSTL, Cewolf, opendev customizada) e utiliza `cabecalho.jspf` e `rodape.jspf` para templating.

- **`applicationContext-*.xml`:** Os arquivos foram localizados em `src/` e confirmados como sendo carregados pelo Spring, cobrindo recursos, Hibernate, segurança, serviços, ações (WebWork) e agendamento (Quartz).
- **`hibernate.xml`:** Não encontrado, indicando que a configuração do Hibernate está provavelmente embarcada em `applicationContext-hibernate.xml`.

**Conclusão Parcial:** A estrutura de configuração é típica de uma aplicação Java EE legada, utilizando Spring para DI/IoC, Hibernate para persistência e WebWork para o front-end MVC, com Acegi Security para segurança. A ativação do modo `debug` no DWR em produção pode ser um risco de segurança.

**Status:** CONCLUÍDO

### 3. Mapeamento do Modelo de Dados e Persistência

**Objetivo:** Entender a estrutura do banco de dados e como a aplicação interage com ele.

**Ações:**
- Analisar `bolao_datamodel.xml` para compreender o esquema lógico do banco de dados.
- Revisar as configurações de persistência em `applicationContext-hibernate.xml` (ou similar) para entender a ORM (provavelmente Hibernate) e suas configurações.
- Identificar as entidades de domínio (classes Java que representam o modelo de dados).

**Resultados:**
- **`bolao_datamodel.xml`:** Este arquivo XML detalha o esquema do banco de dados, confirmando **MySQL** como o SGBD. As tabelas principais identificadas são:
    - `PAR_PARTICIPANTE`: Informações dos usuários.
    - `EQP_EQUIPE`: Equipes/países participantes (com dados da Copa do Mundo de 2006).
    - `JOG_JOGO`: Detalhes dos jogos.
    - `PAL_PALPITE`: Palpites gerais dos participantes.
    - `PRI_PRIVILEGIO`: Papéis/privilégios dos usuários.
    - `BOI_BOLAO_INDIVIDUAL`: Detalhes de bolões individuais (apostas).
    - `PAI_PALPITE_INDIVIDUAL`: Palpites específicos dentro de um bolão individual.
    - `NOT_NOTICIA`: Artigos de notícias.
    O documento também define relacionamentos de chave estrangeira e tipos de dados.

- **`applicationContext-hibernate.xml`:**
    - **Integração Hibernate:** Confirma a utilização do Spring com Hibernate 3 (`LocalSessionFactoryBean`, `HibernateTransactionManager`).
    - **DataSource:** Referencia um `defaultDataSource` (a ser configurado em `applicationContext-resources.xml`).
    - **Dialeto Hibernate:** `org.hibernate.dialect.MySQLDialect` confirma o MySQL.
    - **Mapeamento ORM:** Lista de arquivos `.hbm.xml` que mapeiam objetos Java para tabelas do banco de dados:
        - `/com/opendev/bolao/model/Participante.hbm.xml`
        - `/com/opendev/bolao/model/Equipe.hbm.xml`
        - `/com/opendev/bolao/model/Jogo.hbm.xml`
        - `/com/opendev/bolao/model/Palpite.hbm.xml`
        - `/com/opendev/bolao/model/Privilegio.hbm.xml`
        - `/com/opendev/bolao/model/BolaoIndividual.hbm.xml`
        - `/com/opendev/bolao/model/PalpiteBolaoIndividual.hbm.xml`
    - **DAOs:** Define implementações de DAOs baseadas em Hibernate para `Equipe`, `Jogo`, `Palpite`, `Participante` e `Privilegio`.
    - **Observação:** A tabela `NOT_NOTICIA` do `bolao_datamodel.xml` não possui um arquivo `.hbm.xml` ou DAO explícito configurado aqui, sugerindo um método de persistência diferente ou que não é gerenciada por este contexto Hibernate.

**Conclusão Parcial:** O modelo de dados é bem definido e a camada de persistência utiliza Hibernate 3 com mapeamento XML (`.hbm.xml`). As classes de domínio são esperadas no pacote `com.opendev.bolao.model`.

**Status:** CONCLUÍDO

### 4. Análise das Tecnologias de Front-end

**Objetivo:** Avaliar a pilha tecnológica utilizada para a interface do usuário.

**Ações:**
- Examinar arquivos `.jsp` (em `webapp/` e subpastas como `admin/`, `seguro/`) para identificar o uso de scriptlets, JSTL ou outras bibliotecas de tags.
- Analisar `webapp/css/estilo.css` para padrões de estilo e possíveis frameworks CSS.
- Revisar arquivos JavaScript em `webapp/js/` (ex: `prototype.js`, `scriptaculous.js`) para entender bibliotecas e comportamentos interativos.

**Resultados:**
- **Páginas JSP:** Foram encontrados 12 arquivos `.jsp` distribuídos em `webapp/` e subpastas (`admin/`, `seguro/`), que servem como as páginas da aplicação. O uso de `cabecalho.jspf` e `rodape.jspf` em `jsp-config` (`web.xml`) indica um mecanismo de templating simples baseado em includes.
- **CSS (`webapp/css/estilo.css`):**
    - **Estilo Customizado:** A aplicação utiliza CSS customizado, sem indicação de frameworks CSS modernos.
    - **Design Antigo:** O layout é fixo (`width: 840px`), e há forte dependência de imagens de fundo para elementos visuais (cabeçalhos, rodapés, botões, balões de chat), prática comum em designs mais antigos.
    - **Compatibilidade com IE:** A presença de imagens específicas para IE (`_ie.png`) e o uso de `filter: alpha(opacity=70);` para opacidade são fortes indicadores de suporte a versões antigas do Internet Explorer, reforçando a idade da codebase.
    - **Funcionalidades:** Estilos dedicados para um recurso de chat (`#batepapo`) e um sistema de popups/tooltips (`#balao_palpite`, `#overDiv`).
- **JavaScript (`webapp/js/`):**
    - **Bibliotecas Legadas:**
        - `prototype.js`: Framework JavaScript para manipulação de DOM e AJAX.
        - `scriptaculous.js`: Biblioteca de efeitos visuais e UI, construída sobre Prototype.
        - `overlib.js`: Biblioteca para popups/tooltips DHTML, já inferida do `web.xml` e CSS.
    - **Scripts Customizados:**
        - `BrowserDetector.js`: Provavelmente para detecção de navegadores antigos.
        - `effects.js`: Possíveis efeitos adicionais ou extensão do Script.aculo.us.
        - `engine.js`, `util.js`: Contêm lógica principal e funções utilitárias da aplicação.

**Conclusão Parcial:** A interface de usuário é construída com tecnologias de front-end da era pré-jQuery/modern JavaScript framework (meados dos anos 2000). A dependência de bibliotecas como Prototype.js e Script.aculo.us, juntamente com o CSS e o suporte a IE antigo, confirmam a maturidade (antiguidade) da pilha de front-end.

**Status:** CONCLUÍDO

### 5. Avaliação dos Aspectos de Segurança

**Objetivo:** Identificar potenciais vulnerabilidades e pontos fracos na segurança da aplicação.

**Ações:**
- Revisar `applicationContext-security.xml` para entender a configuração de autenticação e autorização (Acegi Security).
- Analisar `web.xml` para configurações de segurança, restrições de URL e tratamento de erros (já abordado no Passo 2).
- Inspecionar arquivos JSP em busca de injeções (XSS, SQL Injection via scriptlets), falhas de controle de acesso (diretivas de página, includes), e exposição de informações sensíveis (será uma análise mais aprofundada nos próximos passos).
- Verificar dependências em `WEB-INF/lib/` em busca de CVEs conhecidas (requer ferramenta externa ou pesquisa manual, será abordado no Passo 6).

**Resultados:**
- **Framework de Segurança:** Confirmação do uso do Acegi Security 1.0.0 (predecessor do Spring Security). Esta é uma versão extremamente antiga e desatualizada.
- **Cadeia de Filtros de Segurança (`filterChainProxy`):** Define a sequência de filtros que processam as requisições, incluindo autenticação, autorização e tratamento de exceções de segurança.
- **Autenticação:**
    - **Provedor de Usuário:** `JdbcDaoImpl` busca credenciais de login (`PAR_LOGIN`, `PAR_SENHA`, `PAR_HABILITADO`) na tabela `PAR_PARTICIPANTE`.
    - **Carga de Autoridades (Roles):** As permissões (roles) são buscadas da tabela `PRI_PRIVILEGIO`, relacionada ao `PAR_PARTICIPANTE`.
    - **Codificação de Senha:** `ShaPasswordEncoder` com `encodeHashAsBase64=true`. O algoritmo SHA-1 é **altamente inseguro** para armazenamento de senhas atualmente, sendo vulnerável a ataques de colisão e tabelas arco-íris. Recomenda-se o uso de algoritmos adaptativos como bcrypt, scrypt ou Argon2.
    - **Processo de Login:** O `AuthenticationProcessingFilter` gerencia o login, redirecionando para `/login.jsp?status=invalido` em caso de falha e `/seguro/principal.jsp` em sucesso. O endpoint de processamento é `/j_security_check`.
- **Autorização (Controle de Acesso):**
    - **`FilterSecurityInterceptor`:** Aplica regras de autorização baseadas em padrões de URL (Apache Ant style) e roles atribuídas.
    - **Controle de Acesso por URL:**
        - `/login.jsp` é acessível a `ROLE_ANONYMOUS,admin,geral,restrito`.
        - `/dwr/index.html`, `/dwr/test/**` restritos a `admin`.
        - `/dwr/**` acessível a `admin,geral,restrito`. **Isto é uma preocupação**, pois o DWR está com `debug=true` no `web.xml`, o que, combinado com acesso tão amplo para usuários autenticados, pode expor serviços internos e dados sensíveis.
        - `/seguro/jogos.jsp` para `admin,geral`.
        - `/seguro/**` para `admin,geral,restrito`.
        - `/admin/**` restrito a `admin`.
    - **Controle de Acesso Baseado em Método:** `MethodSecurityInterceptor` protege métodos específicos (ex: `AdminAction.carregarInfoEquipes`) com base nas roles, restrito a `admin`.
- **Integração de Sessão:** `HttpSessionContextIntegrationFilter` liga o contexto de segurança à sessão HTTP.
- **Acesso Anônimo:** `AnonymousProcessingFilter` permite acesso anônimo com a role `ROLE_ANONYMOUS`.
- **Tratamento de Exceções de Segurança:** `ExceptionTranslationFilter` direciona para a página de login em caso de exceções de segurança.
- **HTTPS Não Forçado:** `forceHttps="false"` na configuração do `authenticationProcessingFilterEntryPoint` significa que a aplicação **não está forçando o uso de HTTPS para o formulário de login**, o que representa uma **vulnerabilidade crítica** pois credenciais são transmitidas em texto puro.

**Principais Preocupações de Segurança:**
1.  **Framework de Segurança Obsoleto:** Acegi Security 1.0.0 é vulnerável e sem suporte.
2.  **Armazenamento de Senhas Fraco:** SHA-1 é inadequado para hashing de senhas.
3.  **DWR com Debug Habilitado e Acesso Amplo:** Exposição de serviços DWR com debug ativo para usuários autenticados (e potencialmente anônimos) é um risco de divulgação de informação e execução indevida.
4.  **Ausência de HTTPS no Login:** Transmissão de credenciais em texto claro é uma falha de segurança grave.
5.  **Riscos de Injeção (SQL, XSS):** Dada a idade do stack (JSP, WebWork), a aplicação é altamente suscetível a SQL Injection e XSS se não houver sanitização robusta de entradas. CSRF também é uma preocupação.

**Status:** CONCLUÍDO

### 6. Identificação de Débitos Técnicos e Riscos

**Objetivo:** Listar áreas do código que podem ser problemáticas, desatualizadas ou que representam riscos futuros.

**Ações:**
- Com base nas tecnologias identificadas, inferir obsolescência (ex: JSP puro, Struts 1/WebWork 1, versões antigas de Spring/Hibernate/Acegi).
- Identificar padrões de código que possam levar a acoplamento excessivo ou baixa manutenibilidade.
- Avaliar a presença de testes automatizados (ou a falta deles) e sua cobertura.
- Pesquisar versões das dependências para verificar o status de suporte e vulnerabilidades conhecidas.

**Resultados:**
1.  **Obsolescência Extrema das Tecnologias Core:**
    - **Java 1.8:** Embora ainda com suporte de longo prazo por alguns provedores, não é a versão LTS mais recente, e a maioria das bibliotecas estão fixadas em versões compatíveis com ele, indicando que a aplicação não foi atualizada para tirar proveito de recursos mais recentes da linguagem.
    - **WebWork 2.2.2, XWork 1.1.3:** São frameworks **End-of-Life (EOL) há muitos anos**, substituídos pelo Apache Struts 2. Representam um grande débito técnico e risco de segurança.
    - **Acegi Security 1.0.0:** **EOL e altamente inseguro**. Foi sucedido pelo Spring Security e possui inúmeras vulnerabilidades conhecidas.
    - **Hibernate 3.2.6.ga:** **EOL e sem suporte**, com prováveis vulnerabilidades e performance não otimizada.
    - **Spring Framework 1.2.8:** **EOL há muito tempo**, com sérias implicações de segurança e falta de recursos modernos.
    - **MySQL Connector/J 3.0.17:** **Versão extremamente antiga e EOL**, com riscos de segurança e incompatibilidade com versões modernas do MySQL.
    - **JSP/Servlet 2.4, JSTL 1.1.2:** Especificações antigas.
    - **Bibliotecas JavaScript de Front-end (Prototype.js, Script.aculo.us, Overlib):** Todas são **muito antigas e não mantidas ativamente**, o que as torna vulneráveis a problemas de segurança e incompatibilidade com navegadores modernos.
    - **Outras Bibliotecas (Cewolf 1.0, DWR 2.0.1, Quartz Scheduler 1.5.1, EHCache 1.2.3, Batik 1.7, JFreeChart 1.0.0):** É altamente provável que todas estas bibliotecas estejam em versões antigas e EOL, com falhas de segurança conhecidas (CVEs) e bugs não corrigidos.

2.  **Riscos de Segurança Críticos (Reiterando e Expandindo Passo 5):**
    - **Hashing de Senha Fraco (SHA-1):** Permite ataques de força bruta e tabelas arco-íris, comprometendo a segurança das contas de usuário.
    - **Ausência de HTTPS para Login:** Credenciais de usuário transmitidas em texto puro, sujeitas a interceptação.
    - **DWR com Debug Habilitado e Acesso Amplo:** Exposição de funcionalidades internas que podem levar à divulgação de informações sensíveis ou execução de comandos não autorizados.
    - **Vulnerabilidades de Injeção (XSS, SQL Injection):** Dada a ausência de proteções modernas em frameworks desatualizados e a provável manipulação manual de dados em JSPs e DAOs, a aplicação é altamente suscetível a esses ataques.
    - **Vulnerabilidades de Cross-Site Request Forgery (CSRF):** A ausência de tokens CSRF nos formulários é esperada em aplicações dessa idade, tornando-a vulnerável a ataques.
    - **CVEs em Bibliotecas Obsoletas:** A alta quantidade de bibliotecas EOL torna quase certo que a aplicação contenha vulnerabilidades conhecidas publicamente.

3.  **Desafios de Manutenibilidade e Modernização:**
    - **Alto Acoplamento:** As integrações entre Spring 1.2.8, Hibernate 3 e WebWork 2.2.2 provavelmente resultam em código altamente acoplado, dificultando a modificação e a extensão.
    - **Configuração Baseada em XML:** A profusão de arquivos XML para configuração (Spring, Hibernate, WebWork, web.xml) é mais verbosa e menos produtiva do que abordagens baseadas em anotações ou Java Config.
    - **Ausência de Testes Automatizados/CI/CD:** Não há indícios de uma estratégia de testes automatizados ou pipeline de CI/CD, o que aumenta o risco de regressões e dificulta a evolução do sistema.
    - **Templating Básico:** O uso de `.jspf` para templating é rudimentar e não oferece os benefícios de componentização e reusabilidade de motores de template modernos (ex: Thymeleaf, FreeMarker).
    - **Customização de UI/UX:** O CSS e JavaScript customizados exigem grande esforço para mudanças de UI e para adaptar a aplicação a princípios de design responsivo.
    - **Processo de Deployment:** A presença de `build.xml` para o empacotamento WAR pode indicar um processo de deployment manual ou menos automatizado.

**Conclusão Parcial:** O projeto possui um **débito técnico extremamente elevado**, com tecnologias fundamentais obsoletas e uma série de vulnerabilidades de segurança críticas. A modernização é imperativa para garantir a segurança, manutenibilidade e escalabilidade.

**Status:** CONCLUÍDO

---

## Análise Inicial Completa

- **Análise Inicial Completa:** CONCLUÍDO
- **Data de Conclusão:** 17/02/2026

---

## Relatório Resumido da Análise Inicial (Deep Project Review)

### Visão Geral

O projeto "Sistema Bolao" é uma aplicação web desenvolvida em Java, com funcionalidade de bolão de apostas (aparentemente focado em resultados de jogos de futebol, possivelmente da Copa do Mundo de 2006, dados os dados de exemplo). A aplicação utiliza uma arquitetura MVC clássica com um conjunto de tecnologias que eram populares no início/meados dos anos 2000.

### Inventário Tecnológico

| Categoria                | Tecnologia/Componente      | Versão/Detalhe             | Status de Suporte | Observações                                                                        |
| :----------------------- | :------------------------- | :------------------------- | :---------------- | :--------------------------------------------------------------------------------- |
| **Linguagem**            | Java                       | 1.8                        | Ativo (LTS)       | Não é a versão LTS mais recente, limitando acesso a novos recursos da linguagem. |
| **Sistema de Build**     | Maven                      | -                          | Ativo             | Principal ferramenta de build e gerenciamento de dependências.                       |
|                          | Ant                        | -                          | Ativo             | Utilizado especificamente para empacotamento WAR (`build.xml`).                      |
| **Web Framework (MVC)**  | WebWork                    | 2.2.2                      | EOL (Obsoleto)    | Framework descontinuado, sucedido por Apache Struts 2. Risco de segurança e manutenção. |
|                          | XWork                      | 1.1.3                      | EOL (Obsoleto)    | Base do WebWork.                                                                     |
| **Servlets/JSP**         | Servlet API                | 2.4                        | EOL (Obsoleto)    | Especificação antiga.                                                                |
|                          | JSP API                    | 2.0                        | EOL (Obsoleto)    | Especificação antiga.                                                                |
|                          | JSTL                       | 1.1.2                      | EOL (Obsoleto)    | Versão antiga da biblioteca de tags padrão.                                          |
| **Injeção de Dependência** | Spring Framework           | 1.2.8                      | EOL (Obsoleto)    | Versão extremamente antiga e sem suporte.                                            |
| **Segurança**            | Acegi Security             | 1.0.0                      | EOL (Obsoleto)    | Predecessor do Spring Security. Sem suporte, conhecido por vulnerabilidades.          |
| **Persistência (ORM)**   | Hibernate                  | 3.2.6.ga                   | EOL (Obsoleto)    | Versão antiga e sem suporte.                                                         |
| **Banco de Dados**       | MySQL (Connector/J)        | 3.0.17                     | EOL (Obsoleto)    | Versão do driver extremamente antiga, potencial incompatibilidade e vulnerabilidades. |
| **Pool de Conexões**     | Commons DBCP               | 1.2.2                      | EOL (Obsoleto)    |                                                                                    |
|                          | Commons Pool               | 1.3                        | EOL (Obsoleto)    |                                                                                    |
| **AJAX**                 | DWR (Direct Web Remoting)  | 2.0.1                      | EOL (Obsoleto)    | Versão antiga. Modo debug ativo no `web.xml` (risco de segurança).                 |
| **Agendamento**          | Quartz Scheduler           | 1.5.1                      | EOL (Obsoleto)    |                                                                                    |
| **Cache**                | EHCache                    | 1.2.3                      | EOL (Obsoleto)    |                                                                                    |
| **Gráficos**             | Cewolf                     | 1.0                        | EOL (Obsoleto)    |                                                                                    |
|                          | JFreeChart                 | 1.0.0                      | EOL (Obsoleto)    |                                                                                    |
| **SVG**                  | Batik                      | 1.7                        | EOL (Obsoleto)    |                                                                                    |
| **Front-end JS**         | Prototype.js               | -                          | EOL (Obsoleto)    | Framework JS obsoleto.                                                             |
|                          | Script.aculo.us            | -                          | EOL (Obsoleto)    | Biblioteca JS de efeitos obsoleta.                                                 |
|                          | Overlib                    | -                          | EOL (Obsoleto)    | Biblioteca de popups/tooltips.                                                     |
| **Estilo**               | CSS Customizado            | -                          | -                 | Design fixo, hacks para IE, sem uso de frameworks CSS modernos.                    |
| **Dados**                | `bolao_datamodel.xml`      | -                          | -                 | Descreve o esquema do banco de dados MySQL.                                        |
| **Configuração**         | Arquivos `applicationContext-*.xml`, `web.xml` | -                          | -                 | Configurações de Spring, Hibernate, segurança e da aplicação web.                  |

### Riscos

1.  **Segurança Crítica:**
    *   **Vulnerabilidades em Componentes EOL:** A vasta maioria das dependências (Acegi Security, Spring, Hibernate, WebWork, MySQL Connector/J, DWR, etc.) está em versões EOL, com inúmeras CVEs conhecidas e não corrigidas.
    *   **Hashing de Senha Inseguro (SHA-1):** Permite ataques de força bruta e tabelas arco-íris, expondo credenciais de usuário.
    *   **Ausência de HTTPS para Login:** Transmissão de credenciais em texto puro, sujeita a interceptação (man-in-the-middle).
    *   **DWR com Debug Habilitado e Acesso Amplo:** Exposição de serviços internos que pode levar a vazamento de dados ou execução de código não autorizado.
    *   **Vulnerabilidades de Injeção:** Alta probabilidade de SQL Injection e XSS devido à falta de proteções modernas em frameworks antigos e código manual.
    *   **CSRF:** Sem mecanismos de proteção modernos, a aplicação é vulnerável a ataques de falsificação de requisição entre sites.
2.  **Instabilidade e Bugs:** Softwares EOL não recebem correções, levando a comportamentos imprevisíveis e bugs não resolvidos.
3.  **Dificuldade de Manutenção:** A base de código está presa a padrões e bibliotecas antigas, tornando difícil encontrar desenvolvedores com conhecimento específico e caro de manter.
4.  **Incompatibilidade:** Possíveis problemas com ambientes de execução, navegadores e bancos de dados modernos.
5.  **Performance:** Tecnologias antigas podem não ser otimizadas para o desempenho atual.

### Lacunas

1.  **Testes Automatizados:** Não há evidências de uma estratégia de testes automatizados (unitários, integração, e2e), aumentando o risco de regressões e dificultando refatorações.
2.  **CI/CD:** Ausência de um pipeline de Integração Contínua/Entrega Contínua para automatizar builds, testes e deployments.
3.  **Documentação:** Apenas o `AGENTS.md` e `passo-a-passo.md` (e o `bolao_datamodel.xml`) foram identificados como documentação significativa. A falta de documentação técnica interna dificulta a compreensão do sistema.
4.  **Monitoramento e Logs:** Embora `web.xml` configure um listener customizado, não há evidências de uma solução robusta de monitoramento e agregação de logs para produção.

### Recomendações

#### Curto Prazo (Foco em Segurança e Estabilidade Mínima)

1.  **Forçar HTTPS:** Configurar o servidor de aplicação para forçar o uso de HTTPS para toda a aplicação.
2.  **Desativar DWR Debug:** Desabilitar o modo debug do DWR em produção. Revisar os mapeamentos do DWR para garantir que apenas serviços necessários e seguros sejam expostos.
3.  **Auditoria de Credenciais:** Implementar uma validação de senhas mais robusta na camada de aplicação (se possível) e comunicar a necessidade urgente de migrar o hashing de senhas.
4.  **Isolar/Remover Funcionalidades Críticas:** Avaliar a possibilidade de isolar ou remover funcionalidades menos críticas que dependem de bibliotecas EOL com vulnerabilidades conhecidas, se a migração for demorada.

#### Médio Prazo (Foco em Redução de Débito Técnico e Melhoria de Manutenção)

1.  **Upgrade do Spring Framework:** Migrar para uma versão moderna do Spring (ex: Spring Boot 3+ com Spring 6), o que permitirá modernizar outras partes do sistema de forma incremental.
2.  **Migração da Segurança:** Substituir Acegi Security por Spring Security 6+, aproveitando suas funcionalidades atualizadas e mais seguras.
3.  **Atualização do ORM:** Migrar Hibernate 3 para Hibernate 6+, mantendo a estratégia de ORM, mas com uma versão moderna e com suporte.
4.  **Substituição do WebWork:** Migrar o framework MVC de WebWork para Spring MVC ou Spring WebFlux (se a arquitetura permitir).
5.  **Atualização de Bibliotecas de Terceiros:** Atualizar todas as bibliotecas de terceiros (DWR, Quartz, EHCache, JSTL, etc.) para suas versões mais recentes e com suporte.
6.  **Introdução de Testes Automatizados:** Iniciar a criação de testes unitários e de integração para módulos críticos e novos desenvolvimentos.
7.  **Refatoração do Front-end (Primeira Fase):** Começar a substituir Prototype/Script.aculo.us por bibliotecas JavaScript modernas (ex: jQuery para transição, ou um framework SPA como React/Vue/Angular para reescrita parcial).

#### Longo Prazo (Foco em Modernização Completa e Escalabilidade)

1.  **Reescrita ou Migração para Microserviços:** Avaliar a reescrita gradual da aplicação ou a quebra em microserviços, utilizando uma arquitetura moderna (ex: Spring Boot, RESTful APIs).
2.  **Modernização do Front-end (Completa):** Reescrita do front-end com um framework JavaScript moderno (React, Vue, Angular) e adoção de práticas de design responsivo.
3.  **Automação de CI/CD:** Implementar um pipeline robusto de CI/CD para automatizar build, testes, análise de código e deployment.
4.  **Monitoramento e Observabilidade:** Implementar ferramentas de monitoramento de desempenho (APM), agregação de logs e rastreamento distribuído.
5.  **Atualização da Versão do Java:** Migrar para a versão LTS mais recente do Java (ex: Java 17 ou 21) para aproveitar as melhorias de desempenho e novos recursos.
6.  **Banco de Dados:** Avaliar a necessidade de upgrade da versão do MySQL ou migração para um banco de dados mais adequado às necessidades futuras (ex: PostgreSQL, nuvem).

### Impacto

O não endereçamento destas questões resultará em:
- **Risco de Segurança Elevadíssimo:** Ameaça constante de ataques cibernéticos, perda de dados e comprometimento da reputação.
- **Custos de Manutenção Crescentes:** Dificuldade em manter e escalar a aplicação, com o aumento do custo de contratação de desenvolvedores e da resolução de bugs.
- **Performance Degradada:** Potenciais problemas de desempenho à medida que a base de usuários cresce ou a demanda aumenta.
- **Dificuldade de Evolução:** Incapacidade de adicionar novas funcionalidades ou integrar-se com sistemas modernos.
- **Incompatibilidade Contínua:** Quebras com novas versões de navegadores, sistemas operacionais e outros softwares.

### Prioridade

- **IMEDIATA:** Todas as recomendações de **Curto Prazo** e as questões de **Segurança Crítica**.
- **ALTA:** A maioria das recomendações de **Médio Prazo** para estabilizar e modernizar os componentes core.
- **MÉDIA a LONGA:** Recomendações de **Longo Prazo**, dependendo da estratégia de negócio e do ciclo de vida esperado para a aplicação.

