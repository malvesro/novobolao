---
title: Novobolao da Copa
emoji: ⚽
colorFrom: green
colorTo: yellow
sdk: docker
pinned: false
---

# Sistema Bolão (Java EE legado)

Este documento é uma análise técnica do projeto **Sistema Bolão**, com foco em arquitetura, tecnologias, funcionalidades e recomendações de evolução. O objetivo é tornar o entendimento rápido e permitir modernização **com o mínimo de alterações** no comportamento atual.

Consulte as **[Diretrizes de Segurança](.ia/diretrizes/seguranca.md)** para detalhes sobre as proteções implementadas.

## 1. Visão Geral

Aplicação web clássica (Java EE) para gerenciamento de um bolão de futebol, com cadastro de participantes, palpites, classificação, gráficos de desempenho e chat. O projeto é empacotado em **WAR** e deployado em **Tomcat**.

Principais sinais de legado:

- Stack baseada em **Spring + Hibernate 3 + WebWork/XWork + Acegi Security + DWR + Cewolf**.
- **Ant** apenas para empacotar o WAR (sem compilação).
- **Configurações e credenciais no código**.
- Tecnologias alinhadas à era 2006 (Copa 2006 aparece em mensagens).

## 2. Passo a Passo da Análise

1. **Entradas de build e deploy**: `build.xml` e `build.properties` indicam empacotamento do WAR e path para Tomcat 5.5.
2. **Web.xml**: filtros, servlets e listeners mostraram uso de WebWork, DWR, Acegi e OpenSessionInView.
3. **Spring contexts**: `applicationContext-*.xml` revelaram datasources, Hibernate, segurança, serviços e scheduler.
4. **Camada de ações**: classes em `src/com/opendev/bolao/action` indicam endpoints e funcionalidades.
5. **Serviços e DAOs**: lógica principal em `service/impl` e persistência em `dao/hibernate`.
6. **Domínio**: entidades em `model` e mapeamentos `*.hbm.xml` definem o banco.
7. **UI**: páginas JSP em `webapp/` e JS legado (Prototype/Scriptaculous).
8. **Integrações**: e-mail via JavaMail e agendamentos via Quartz.

## 3. Funcionalidades Identificadas

- **Cadastro e autenticação** de participantes.
- **Envio de palpites** por jogo (com bloqueio por horário).
- **Classificação geral** e estatísticas de desempenho.
- **Administração**: cadastro de jogos, atualização de resultados, gerenciamento de participantes e papéis.
- **Chat** em tempo real baseado em DWR.
- **Notificações por e-mail** (cadastro e alertas de jogos próximos).
- **Gráficos** de desempenho e líderes (Cewolf + JFreeChart).
- **Filtros de jogos** por fase, data, grupo, equipe.

## 4. Arquitetura Atual (Visão Lógica)

Camadas principais:

1. **Web (JSP + WebWork/DWR)**
2. **Actions (Controllers)**: `ParticipanteAction`, `AdminAction`
3. **Services**: regras de negócio, transações Spring
4. **DAOs**: persistência Hibernate
5. **Banco MySQL**

Fluxo típico:

JSP → WebWork Action → Service (transação) → DAO (Hibernate) → Banco

## 5. Estrutura de Pastas

- `src/` código Java e configurações Spring/XWork.
- `webapp/` páginas JSP, JS, CSS e libs (WAR root).
- `webapp/WEB-INF/lib/` jars locais (sem gerenciador de dependências).
- `webapp/WEB-INF/classes/` recursos duplicados (provável resultado de build antigo).
- `bolao_datamodel.xml` modelo do banco (MySQL).

## 6. Tecnologias e Bibliotecas

Pelos jars em `webapp/WEB-INF/lib/` e configurações:

- **Spring Framework (antigo)**
- **Hibernate 3** (mapeamentos `*.hbm.xml`)
- **WebWork/XWork** (base do Struts 2)
- **Acegi Security** (predecessor do Spring Security)
- **DWR** (AJAX remoting)
- **Quartz Scheduler**
- **Ehcache**
- **JFreeChart + Cewolf** (gráficos)
- **Commons DBCP, Commons Lang, Commons Logging**
- **JavaMail**
- **Prototype/Scriptaculous** (front-end)
- **MySQL JDBC Driver** (`com.mysql.jdbc.Driver`)

## 7. Configurações Principais

- Banco de dados: `src/applicationContext-resources.xml`
- Hibernate: `src/applicationContext-hibernate.xml`
- Segurança (Acegi): `src/applicationContext-security.xml`
- Scheduler: `src/applicationContext-scheduler.xml`
- WebWork/XWork: `src/xwork.xml`
- DWR: `webapp/WEB-INF/dwr.xml`
- E-mail: `src/com/opendev/bolao/email/email.properties`

## 8. Modelo de Dados (Entidades Principais)

Entidades mapeadas em `src/com/opendev/bolao/model/*.hbm.xml`:

- `Participante` → `PAR_PARTICIPANTE`
- `Privilegio` → `PRI_PRIVILEGIO`
- `Jogo` → `JOG_JOGO`
- `Equipe` → `EQP_EQUIPE`
- `Palpite` → `PAL_PALPITE` (chave composta: participante + jogo)
- `BolaoIndividual` / `PalpiteBolaoIndividual`
- `Noticia`

Observações:

- `Palpite` usa chave composta, o que dificulta migração para JPA sem ajustes.
- `Participante` possui cache local de pontuação (classe `Cache`).

## 9. Pontos de Atenção (Riscos e Dívidas Técnicas)

### **AVISO: Riscos Críticos de Segurança Identificados**

A análise inicial revelou vulnerabilidades de **alta criticidade** que necessitam de atenção **imediata**:
1.  **Credenciais de Banco de Dados Expostas:** Usuário e senha do banco de dados estão hardcoded em `src/applicationContext-resources.xml`.
2.  **Hashing de Senha Inseguro:** O sistema utiliza **SHA-1 sem salt** (`SegurancaUtils.java`), um algoritmo obsoleto e inseguro para armazenamento de senhas.
3.  **Falta de HTTPS:** A ausência de HTTPS no servidor de aplicação expõe todo o tráfego, incluindo credenciais de login, à interceptação.
4.  **Bibliotecas de Segurança Obsoletas:** O uso do **Acegi Security** (versão 1.0.0) é um risco grave, pois o framework é descontinuado e possui vulnerabilidades conhecidas.
5.  **DWR com Modo Debug Ativo:** A configuração do DWR com `debug=true` em produção pode expor informações sensíveis sobre a aplicação e seus serviços.
---

1. **Credenciais em código**: usuário e senha de DB hardcoded em `src/applicationContext-resources.xml`.
2. **Senha fraca**: hashing com **SHA-1 sem salt** em `src/com/opendev/bolao/util/SegurancaUtils.java`.
3. **Stack desatualizada**: Acegi, WebWork, Hibernate 3, DWR, JS legado.
4. **Build frágil**: Ant só empacota, não compila. Classes podem estar fora de sync.
5. **Chat em memória**: não persistente e sem suporte a cluster.
6. **Método `Jogo.jaOcorreu()` sempre retorna `true`**, afetando regra de pontuação.
7. **Configs duplicadas**: arquivos em `src/` e `webapp/WEB-INF/classes/`.
8. **Logs e tratamento de erro**: `printStackTrace` em produção.

## 10. Recomendações Imediatas (Baixo Impacto)

1. Externalizar configurações sensíveis (DB, SMTP) usando arquivos externos ou variáveis de ambiente.
2. Ajustar hashing de senha para **BCrypt** mantendo compatibilidade com legado.
3. Revisar `Jogo.jaOcorreu()` para não retornar `true` sempre.
4. Padronizar build e remover duplicidade entre `src/` e `webapp/WEB-INF/classes/`.
5. Adicionar logging estruturado em pontos críticos (cadastro, update de palpite, email).

## 11. Migração para Tecnologias Modernas com Mínimas Alterações

Estratégia: **evolução incremental**, preservando JSP, URL paths e fluxo atual.

### Passo 1 – Modernizar Build (sem mudar runtime)

- Migrar para **Maven/Gradle** com empacotamento WAR.
- Manter `webapp/` como root.
- Importar jars de `WEB-INF/lib` como dependências.

Resultado: build reproduzível, sem reescrever código.

### Passo 2 – Atualizar Runtime

- Subir para **Java 17 ou 21**.
- Migrar de **Tomcat 5.5** para **Tomcat 11.0.x** .

Resultado: segurança e compatibilidade modernas sem refatoração pesada.

### Passo 3 – Atualizar Segurança

- Substituir **Acegi** por **Spring Security**.
- Manter a URL de login `/j_security_check` (compatibilidade).
- Implementar **password rehash**: validar SHA-1 legado e salvar BCrypt na próxima autenticação.

Resultado: segurança atualizada sem alterar UI.

### Passo 4 – Atualizar WebWork

- Migrar **WebWork → Struts 7**.
- Converter `xwork.xml` para `struts.xml` com mínimas mudanças.

Resultado: elimina framework obsoleto mantendo Actions.

### Passo 5 – Atualizar Persistência

- Migrar Hibernate 3 → Hibernate 6.x (mantendo HBM no início).
- Em um segundo momento, converter para **JPA annotations**.

Resultado: compatibilidade com versões atuais do Spring.

### Passo 6 – Evoluir APIs Ajax

- Manter DWR no curto prazo.
- Criar endpoints REST paralelos para novas telas.

Resultado: coexistência gradual, sem reescrita total.

### Passo 7 – UI Opcional

- Manter JSP no início.
- Modernizar aos poucos (ex: substituir Prototype/Scriptaculous por libs atuais).

Resultado: UX evolui sem quebrar a base.

## 12. Roadmap de Modernização (Sugerido)

1. **Curto prazo** (1–2 sprints): build + externalização de configs + segurança.
2. **Médio prazo** (3–5 sprints): Spring Security + Struts 2 + Hibernate atualizado.
3. **Longo prazo**: evolucao para Struts 7 consolidado + REST + UI moderna (mantendo WAR).

## 13. Como Rodar (Legado)

Dependências esperadas:

- Java 1.4–1.6 (original)
- Tomcat 5.5
- MySQL 5.x

Processo (legado):

1. Ajustar `build.properties` com path do Tomcat.
2. Garantir classes compiladas em `webapp/WEB-INF/classes/`.
3. Executar `ant deploy` (gera WAR).

## 13.1. Como Rodar (Moderno - Docker)

**Recomendado para desenvolvimento e testes.**

A aplicação modernizada pode ser executada facilmente usando Docker Compose. Consulte a documentação completa em:

📖 **[docker/README.md](docker/README.md)**

### Quick Start

```bash
# Setup de ambiente e build
wsl bash -c "cp .env.example .env"
wsl bash -c "docker-compose up --build -d"

# Verificar logs
wsl bash -c "docker-compose logs -f app"

# Acessar aplicação
# http://localhost:8080
```

### Credenciais Padrão

| Login | Senha | Papel |
|-------|-------|-------|
| admin | admin123 | ADMIN |
| user | user123 | USER |

⚠️ **IMPORTANTE:** Troque as senhas padrão após o primeiro acesso!

### 13.2. Publicação em Nuvem (Custo Zero - Hugging Face & Aiven)

Para implantar em um ambiente de produção gratuito com Hugging Face Spaces e Aiven MySQL, siga o guia especializado:

📖 **[docs/deployment/HUGGING_FACE_AIVEN.md](docs/deployment/HUGGING_FACE_AIVEN.md)**

Esta configuração utiliza a porta **7860** (obrigatória para o Hugging Face) e ajustes otimizados de JVM.

---

Para mais detalhes sobre:
- Troubleshooting
- Variáveis de ambiente
- Comandos de desenvolvimento
- Health checks
- Estrutura do banco de dados

Consulte: **[docker/README.md](docker/README.md)**

## 14. Próximos Passos Recomendados

1. Criar build moderno (Maven/Gradle).
2. Testar aplicação em Java 17 + Tomcat 8.5.
3. Migrar Acegi → Spring Security com rehash de senha.
4. Remover configs duplicadas e segredos hardcoded.

---

Este README serve como base para tomada de decisão técnica e execução incremental, preservando o comportamento atual e reduzindo risco de regressão.

## 15. Manutenção de Dados (Copa 2026)

O sistema utiliza um dataset normalizado para a carga inicial de equipes e jogos. Para atualizar os dados da Copa 2026 (seleções qualificadas, resultados, sedes ou partidas do mata-mata):

1. **Editar Dataset**: Modifique o arquivo `data/copa2026_tabela_brt_normalizado.csv`.
2. **Gerar SQL**: Execute o script helper para regerar o script de carga do banco.

```bash
# Execução via bash (Ubuntu/WSL)
bash scripts/gerar_sql_copa2026.sh
```

O script gerará o arquivo `data/sql/03-copa-2026-data.sql` que é utilizado no processo de deploy/banco.

## 12. Arquitetura Frontend (Modernização 2026)

A partir da **Fase 7**, o projeto adotou uma estratégia de modernização de UI baseada em **HTMX**, substituindo tecnologias legadas (DWR e Prototype).

### Princípios de Design
- **Mapeamento Natural (Split Inputs)**: Inputs de placar posicionados adjacentes ao nome de cada time para intuitividade imediata.
- **Feedback Visual Atômico**: Uso de animações CSS ("Saved Flash") em toda a linha de dados para confirmação de salvamento.
- **Design System**: Tipografia baseada em **Inter** e **Outfit**, com paleta moderna e efeitos de **Glassmorphism**.

### Estratégia Técnica (HTMX)
Para garantir a estabilidade em tabelas complexas com linhas de detalhe expansíveis, utilizamos os seguintes padrões:

1.  **Padrão Multi-Tbody**: Cada partida na listagem de jogos é encapsulada em seu próprio elemento `<tbody>`. Isso permite que o HTMX atualize o container da partida de forma isolada e atômica.
2.  **Fragmentos JSP (`.jspf`)**: A lógica de renderização da linha de jogo está centralizada em `match-row.jspf`. Este fragmento é incluído:
    - Na renderização inicial da página (`jogos.jsp`).
    - Na resposta parcial de ações HTMX (`palpite-cell-response.jsp`).
3.  **Swap Containers**: As atualizações de palpite utilizam `hx-target="closest tbody"` e `hx-swap="innerHTML"`. Isso substitui o conteúdo interno do `<tbody>` do jogo, garantindo integridade do DOM e evitando duplicidade de IDs.
4.  **UX Helper JS**: O arquivo `ux-helper.js` gerencia comportamentos complementares como navegação automática de foco (Auto-Advance) e barra de progresso em tempo real.

Consulte os registros de decisão para detalhes profundos:
- **[ADR 001](docs/adr/001-htmx-direct-inline-palpites.md)**: Introdução do HTMX.
- **[ADR 004](docs/adr/004-split-inputs-row-level-htmx.md)**: Mapeamento Natural e Multi-Tbody.
